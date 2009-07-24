/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.0  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html 
 * Software distributed under the License  is  distributed  on  an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific  language  governing  rights  and  limitations
 * under the License. 
 * The Original Code is Openbravo ERP. 
 * The Initial Developer of the Original Code is Openbravo SL 
 * All portions are Copyright (C) 2009 Openbravo SL 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.service.model.common.invoice;

import java.math.BigDecimal;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.hibernate.criterion.Expression;
import org.hibernate.criterion.Order;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.session.OBPropertiesProvider;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.data.FieldProvider;
import org.openbravo.erpCommon.businessUtility.GetPriceOfferData;
import org.openbravo.erpCommon.businessUtility.Tax;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.ad.access.User;
import org.openbravo.model.common.businesspartner.BusinessPartner;
import org.openbravo.model.common.businesspartner.Location;
import org.openbravo.model.common.enterprise.DocumentType;
import org.openbravo.model.common.enterprise.Warehouse;
import org.openbravo.model.common.invoice.Invoice;
import org.openbravo.model.common.invoice.InvoiceLine;
import org.openbravo.model.common.plm.Product;
import org.openbravo.model.financialmgmt.tax.TaxRate;
import org.openbravo.model.pricing.pricelist.PriceList;
import org.openbravo.model.pricing.pricelist.PriceListVersion;
import org.openbravo.model.pricing.pricelist.ProductPrice;
import org.openbravo.service.db.DalConnectionProvider;

/**
 * The invoice completer provides a simple-to-use interface to let most information in an
 * {@link Invoice} and InvoiceLine be computed on the basis of limited information which has been
 * set in the {@link InvoiceLine}.
 * 
 * Some fields in the {@link Invoice} and {@link InvoiceLine} must be preset by the caller. This is
 * visible in the IllegalArgumentExceptions thrown in the beginning of the
 * {@link #setInvoiceProperties(Invoice)} and {@link #setInvoiceLineProperties(InvoiceLine)}
 * methods.
 * 
 * This class is assumed to be used single threaded (it has a private member for date formatting).
 * 
 * @author mtaal
 */
public class InvoicePropertySetter {

  private final SimpleDateFormat sqlDateFormatter;

  public InvoicePropertySetter() {
    final String sqlDateFormat = OBPropertiesProvider.getInstance().getOpenbravoProperties()
        .getProperty("dateFormat.sql");
    final String javaDateFormat = OBPropertiesProvider.getInstance().getOpenbravoProperties()
        .getProperty("dateFormat.java");
    if (!javaDateFormat.toLowerCase().equals(sqlDateFormat.toLowerCase())) {
      throw new IllegalStateException("The invoice property setter uses the "
          + " property dataFormat.java, for its correct working in this class "
          + " this property must have the same pattern "
          + " as the dateFormat.sql property (both in Openbravo.properties). "
          + "Apparently the patterns are different: java:" + javaDateFormat + " and sql: "
          + sqlDateFormat);
    }
    sqlDateFormatter = new SimpleDateFormat(javaDateFormat);
  }

  /**
   * Set the properties of an invoice and its invoicelines.
   * 
   * @param invoice
   *          the invoice to set, also its invoicelines are handled.
   * @see Invoice#getInvoiceLineList()
   */
  public void setInvoiceProperties(Invoice invoice) {
    if (invoice.getBusinessPartner() == null) {
      throw new IllegalArgumentException(
          "Businesspartner value must be set on invoice before calling this method");
    }
    if (invoice.isSalesTransaction() == null) {
      throw new IllegalArgumentException(
          "SalesTransaction value must be set on invoice before calling this method");
    }
    if (invoice.getTransactionDocument() == null) {
      throw new IllegalArgumentException(
          "The transaction document (c_targetdoctype_id) value should be set before calling this method");
    }

    final BusinessPartner bp = invoice.getBusinessPartner();

    if (invoice.getClient() == null) {
      invoice.setClient(OBContext.getOBContext().getCurrentClient());
    }
    if (invoice.getOrganization() == null) {
      invoice.setOrganization(OBContext.getOBContext().getCurrentOrganization());
    }
    invoice.setActive(true);

    // set to doc type 0
    if (invoice.getDocumentType() == null) {
      invoice.setDocumentType(OBDal.getInstance().get(DocumentType.class, "0"));
    }

    invoice.setDocumentStatus("DR");
    invoice.setDocumentAction("CO");

    // strange columnname is processing which should have value N
    invoice.setProcessNow(false);
    invoice.setProcessed(false);
    invoice.setPosted("N");

    if (invoice.getDocumentNo() == null) {
      // this was specced, the choice was made to use an existing utility method
      // **documentno -> AD_SEQUENCE_DOCTYPE(#c_doctypetarget_id, '1000000', 'Y',
      // p_documentno OUT parameter)
      // MT>> second parameter is always 1000000
      final Connection conn = OBDal.getInstance().getConnection();
      final String documentNo = Utility.getDocumentNoConnection(conn, new DalConnectionProvider(),
          OBContext.getOBContext().getCurrentClient().getId(), Invoice.TABLE_NAME, true);
      invoice.setDocumentNo(documentNo);
    }

    if (invoice.getSalesRepresentative() == null && bp.getSalesRepresentative() != null) {
      // taken from AD_User_SalesRep table reference
      // EXISTS (SELECT * FROM C_BPartner bp WHERE AD_User.C_BPartner_ID=bp.C_BPartner_ID AND
      // bp.IsSalesRep='Y')
      final User user = OBDal.getInstance().get(User.class, bp.getSalesRepresentative().getId());
      invoice.setSalesRepresentative(user);
    }

    if (invoice.getInvoiceDate() == null) {
      invoice.setInvoiceDate(new Date());
    }

    if (invoice.getAccountingDate() == null) {
      invoice.setAccountingDate(invoice.getInvoiceDate());
    }

    if (invoice.getPartnerAddress() == null) {
      for (Location location : bp.getBusinessPartnerLocationList()) {
        if (!location.isActive()) {
          continue;
        }
        if (invoice.isSalesTransaction() && location.isInvoiceToAddress()) {
          invoice.setPartnerAddress(location);
          break;
        }

        // choose anything, but not a shipto
        // TODO: is this correct?
        if (!invoice.isSalesTransaction() && !location.isShipToAddress()) {
          invoice.setPartnerAddress(location);
        }
      }
    }

    if (invoice.isPrintDiscount() == null) {
      invoice.setPrintDiscount(bp.isPrintDiscount());
    }

    if (invoice.getPriceList() == null) {
      if (invoice.isSalesTransaction()) {
        invoice.setPriceList(bp.getPurchasePricelist());
      } else {
        invoice.setPriceList(bp.getPriceList());
      }
    }

    if (invoice.getCurrency() == null && invoice.getPriceList() != null) {
      invoice.setCurrency(invoice.getPriceList().getCurrency());
    }

    if (invoice.getFormOfPayment() == null) {
      if (invoice.isSalesTransaction()) {
        invoice.setFormOfPayment(bp.getPOFormOfPayment());
      } else {
        invoice.setFormOfPayment(bp.getFormOfPayment());
      }
    }

    if (invoice.getPaymentTerms() == null) {
      if (invoice.isSalesTransaction()) {
        invoice.setPaymentTerms(bp.getPOPaymentTerms());
      } else {
        invoice.setPaymentTerms(bp.getPaymentTerms());
      }
    }

    if (invoice.getTaxDate() == null) {
      invoice.setTaxDate(invoice.getInvoiceDate());
    }

    for (InvoiceLine invoiceLine : invoice.getInvoiceLineList()) {
      setInvoiceLineProperties(invoiceLine);
    }

    if (invoice.getSummedLineAmount() == null) {
      BigDecimal sum = new BigDecimal(0);
      for (InvoiceLine invoiceLine : invoice.getInvoiceLineList()) {
        sum = sum.add(invoiceLine.getLineNetAmount());
      }
      invoice.setSummedLineAmount(sum);
    }
    if (invoice.getGrandTotalAmount() == null) {
      invoice.setGrandTotalAmount(BigDecimal.ZERO);
    }

  }

  /**
   * Sets the values of an InvoiceLine.
   * 
   * @param invoiceLine
   *          the invoiceLine
   */
  public void setInvoiceLineProperties(InvoiceLine invoiceLine) {
    final Invoice invoice = invoiceLine.getInvoice();
    if (invoice == null) {
      throw new IllegalArgumentException(
          "The invoice header should be set before calling this method");
    }
    if (!invoice.getInvoiceLineList().contains(invoiceLine)) {
      throw new IllegalArgumentException(
          "The invoice line should be part of the invoice header line list");
    }
    if (invoiceLine.getProduct() == null) {
      throw new IllegalArgumentException("The product should be set before calling this method");
    }
    if (invoiceLine.getInvoicedQuantity() == null) {
      throw new IllegalArgumentException(
          "The invoice quantity needs to be set before calling this method");
    }
    if (invoice.getPriceList() == null) {
      throw new IllegalArgumentException("Pricelist must be set on the invoice");
    }
    if (invoice.getInvoiceDate() == null) {
      throw new IllegalArgumentException("Invoice date must be set on the invoice");
    }

    if (invoiceLine.getClient() == null) {
      invoiceLine.setClient(invoiceLine.getInvoice().getClient());
    }

    if (invoiceLine.getOrganization() == null) {
      invoiceLine.setOrganization(invoiceLine.getInvoice().getOrganization());
    }

    if (invoiceLine.getLineNo() == null) {
      final int index = invoice.getInvoiceLineList().indexOf(invoiceLine);
      invoiceLine.setLineNo((long) (1 + index) * 10);
    }

    final PriceListVersion priceListVersion = getPriceListVersion(invoice.getPriceList(), invoice
        .getInvoiceDate());
    final ProductPrice productPrice = getProductPrice(invoiceLine.getProduct(), priceListVersion);

    // **pricelist -> m_productprice.pricelist where m_pricelist_version_id =
    // #currentpricelistversion# and m_product_id = #m_product_id
    if (isNullOrZero(invoiceLine.getListPrice()) && productPrice != null) {
      invoiceLine.setListPrice(productPrice.getListPrice());
    }

    // **pricestd -> m_productprice.pricestandard where m_pricelist_version_id =
    // #currentpricelistversion# and m_product_id = #m_product_id
    if (isNullOrZero(invoiceLine.getStandardPrice()) && productPrice != null) {
      invoiceLine.setStandardPrice(productPrice.getStandardPrice());
    }

    if (invoiceLine.getStandardPrice() == null) {
      invoiceLine.setStandardPrice(BigDecimal.ZERO);
    }

    // **pricelimit -> m_productprice.pricelimit where m_pricelist_version_id =
    // #currentpricelistversion# and m_product_id = #m_product_id
    if (isNullOrZero(invoiceLine.getPriceLimit()) && productPrice != null) {
      invoiceLine.setPriceLimit(productPrice.getPriceLimit());
    }

    if (invoiceLine.getPriceLimit() == null) {
      invoiceLine.setPriceLimit(BigDecimal.ZERO);
    }

    // **priceactual ->
    // round(m_get_offers_price(#currentinvoice.dateinvoiced,#currentinvoice.c_bpar
    // tner_id,#m_product_id,#pricestd,#qtyInvoiced,#currentinvoice.m_pricelist_id)
    // ,#currentinvoice.c_currency.priceprecision)
    // check only for null, maybe someone set it explicitly to zero
    if (invoiceLine.getUnitPrice() == null) {
      invoiceLine.setUnitPrice(getOffersPrice(invoiceLine));
    }

    // **linenetamt -> #qtyInvoiced*pricestd
    // TODO: should we not use unit price?
    if (invoiceLine.getLineNetAmount() == null && invoiceLine.getInvoicedQuantity() != null
        && invoiceLine.getUnitPrice() != null) {
      final BigDecimal netAmount = invoiceLine.getInvoicedQuantity().multiply(
          invoiceLine.getUnitPrice());
      invoiceLine.setLineNetAmount(netAmount);
    }

    // **c_tax_id -> c_getTax(#m_product_id, #currentInvoice.dateinvoiced,
    // #currentInvoice.ad_org_id, #context.m_warehouse_id,
    // #currentinvoice.c_businesspartner_location_id,
    // #currentinvoice.c_businesspartner_location_id, #currentinvoice.c_project_id,
    // #currentinvoice.issotrx)
    if (invoiceLine.getTax() == null) {
      final TaxRate tax = getTax(invoiceLine);
      if (tax == null) {
        String msg = "INVOICE_NO_TAX_FOUND";
        // try to translate the message
        final String language = OBContext.getOBContext().getLanguage().getId();
        final FieldProvider fieldProvider = Utility.locateMessage(new DalConnectionProvider(), msg,
            language);
        if (fieldProvider != null) {
          msg = fieldProvider.getField("msgtext");
          msg = msg.replace("@invoiceline@", invoiceLine.getLineNo() + "");
        }
        throw new OBException(msg);
      }
      invoiceLine.setTax(tax);
    }

    if (invoiceLine.getUOM() == null) {
      invoiceLine.setUOM(invoiceLine.getProduct().getUOM());
    }
  }

  private BigDecimal getOffersPrice(InvoiceLine invoiceLine) {
    final Invoice invoice = invoiceLine.getInvoice();
    final String dateInvoicedStr = sqlDateFormatter.format(invoice.getInvoiceDate());

    try {
      // TODO Formatting to a database string uses toString()
      final String result = GetPriceOfferData.getOffersPriceCurrency(new DalConnectionProvider(),
          dateInvoicedStr, invoice.getBusinessPartner().getId(), invoiceLine.getProduct().getId(),
          invoiceLine.getStandardPrice().toString(), invoiceLine.getInvoicedQuantity().toString(),
          invoice.getPriceList().getId(), invoice.getCurrency().getId());
      if (result == null || result.length() == 0) {
        return null;
      }

      // TODO: is this correct parsing of the string?
      return new BigDecimal(result);
    } catch (Exception e) {
      throw new OBException(e);
    }

  }

  private PriceListVersion getPriceListVersion(PriceList priceList, Date invoiceDate) {

    // This logic implements the following pseudo code:
    // #currentpricelistversion# = The first (sorted by validfrom
    // desc) pricelist_version where pricelist_id=#currentinvoice.m_pricelist_id
    // and isactive='Y' and validfrom <= #currentinvoice.dateinvoiced

    final OBCriteria<PriceListVersion> obc = OBDal.getInstance().createCriteria(
        PriceListVersion.class);
    obc.add(Expression.and(Expression.eq(PriceListVersion.PROPERTY_PRICELIST, priceList),
        Expression.le(PriceListVersion.PROPERTY_VALIDFROMDATE, invoiceDate)));
    // obc.add(Expression.eq(PriceListVersion.PROPERTY_PRICELIST, priceList));
    obc.addOrder(Order.desc(PriceListVersion.PROPERTY_VALIDFROMDATE));
    final List<PriceListVersion> plvs = obc.list();
    if (plvs.isEmpty()) {
      return null;
    }
    return plvs.get(0);
  }

  private ProductPrice getProductPrice(Product product, PriceListVersion priceListVersion) {
    final OBCriteria<ProductPrice> obc = OBDal.getInstance().createCriteria(ProductPrice.class);
    obc.add(Expression.and(Expression.eq(ProductPrice.PROPERTY_PRICELISTVERSION, priceListVersion),
        Expression.eq(ProductPrice.PROPERTY_PRODUCT, product)));
    if (obc.list().isEmpty()) {
      return null;
    }
    return obc.list().get(0);
  }

  private boolean isNullOrZero(BigDecimal bigDecimal) {
    return bigDecimal == null || bigDecimal.compareTo(BigDecimal.ZERO) == 0;
  }

  private TaxRate getTax(InvoiceLine invoiceLine) {
    final Invoice invoice = invoiceLine.getInvoice();
    String warehouseId = null;
    final Warehouse warehouse = OBContext.getOBContext().getUser().getDefaultWarehouse();
    if (warehouse != null && warehouse.isActive()) {
      warehouseId = warehouse.getId();
    }
    final String dateStr = sqlDateFormatter.format(invoice.getInvoiceDate());

    try {
      final String taxId = Tax.get(new DalConnectionProvider(), invoiceLine.getProduct().getId(),
          dateStr, getIdOrNull(invoice.getOrganization()), warehouseId, getIdOrNull(invoice
              .getPartnerAddress()), getIdOrNull(invoice.getPartnerAddress()), getIdOrNull(invoice
              .getProject()), invoice.isSalesTransaction());
      if (taxId == null || taxId.length() == 0) {
        return null;
      }
      return OBDal.getInstance().get(TaxRate.class, taxId);
    } catch (Exception e) {
      throw new OBException(e);
    }
  }

  private String getIdOrNull(BaseOBObject bob) {
    if (bob == null) {
      return null;
    }
    return (String) bob.getId();
  }
}
