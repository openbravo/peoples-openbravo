/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.1  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html 
 * Software distributed under the License  is  distributed  on  an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific  language  governing  rights  and  limitations
 * under the License. 
 * The Original Code is Openbravo ERP. 
 * The Initial Developer of the Original Code is Openbravo SLU 
 * All portions are Copyright (C) 2018 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.common.actionhandler.createlinesfromprocess;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;

import javax.enterprise.context.Dependent;
import javax.servlet.ServletException;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.session.OBPropertiesProvider;
import org.openbravo.client.kernel.ComponentProvider.Qualifier;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.businessUtility.Tax;
import org.openbravo.model.common.businesspartner.BusinessPartner;
import org.openbravo.model.common.businesspartner.Location;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.common.enterprise.Warehouse;
import org.openbravo.model.common.order.OrderLine;
import org.openbravo.model.common.plm.Product;
import org.openbravo.model.financialmgmt.tax.TaxRate;
import org.openbravo.model.materialmgmt.transaction.ShipmentInOutLine;
import org.openbravo.model.project.Project;
import org.openbravo.service.db.DalConnectionProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Dependent
@Qualifier(CreateLinesFromProcessHook.CREATE_LINES_FROM_PROCESS_HOOK_QUALIFIER)
class UpdateTax extends CreateLinesFromProcessHook {
  private static final Logger log = LoggerFactory.getLogger(UpdateTax.class);

  @Override
  public int getOrder() {
    return -10;
  }

  /**
   * Update order line tax, taxable amount and tax amount. Throws an exception if no taxes are
   * found.
   */
  @Override
  public void exec() {
    updateTaxRate();
    updateTaxAmount();
    updateTaxableAmount();
  }

  private void updateTaxRate() {
    TaxRate tax = OBDal.getInstance().getProxy(TaxRate.class,
        getCurrentTaxId(getInvoiceLine().getProduct()));
    getInvoiceLine().setTax(tax);
  }

  /**
   * Gets the current tax according order information and selected product. If any tax is found an
   * exception is thrown.
   * 
   * @param product
   *          The product where taxes are searching for
   * @return The Tax ID or an exception if it is not found
   * @throws IOException
   * @throws ServletException
   */
  private String getCurrentTaxId(final Product product) {
    String taxID = "";
    if (isCopiedFromOrderLine()
        || CreateLinesFromUtil.hasRelatedOrderLine((ShipmentInOutLine) getCopiedFromLine())) {
      if (isCopiedFromOrderLine()) {
        taxID = ((OrderLine) getCopiedFromLine()).getTax().getId();
      } else {
        taxID = ((ShipmentInOutLine) getCopiedFromLine()).getSalesOrderLine().getTax().getId();
      }
    } else {
      Warehouse warehouse = ((ShipmentInOutLine) getCopiedFromLine()).getShipmentReceipt()
          .getWarehouse();
      Project project = ((ShipmentInOutLine) getCopiedFromLine()).getProject();
      BusinessPartner businessPartner = ((ShipmentInOutLine) getCopiedFromLine())
          .getBusinessPartner();
      Organization organization = ((ShipmentInOutLine) getCopiedFromLine()).getOrganization();
      boolean isSalesTransaction = ((ShipmentInOutLine) getCopiedFromLine()).getShipmentReceipt()
          .isSalesTransaction();
      Date scheduledDeliveryDate = ((ShipmentInOutLine) getCopiedFromLine()).getShipmentReceipt()
          .getMovementDate();

      String bpLocationId = getMaxBusinessPartnerLocationId(businessPartner);
      String strDatePromised = DateFormatUtils.format(scheduledDeliveryDate, OBPropertiesProvider
          .getInstance().getOpenbravoProperties().getProperty("dateFormat.java"));

      try {
        taxID = Tax.get(new DalConnectionProvider(), product.getId(), strDatePromised,
            organization.getId(), (warehouse != null) ? warehouse.getId() : "", bpLocationId,
            bpLocationId, (project != null) ? project.getId() : "", isSalesTransaction);
      } catch (IOException | ServletException e) {
        log.error("Error in CopyFromProcess while retrieving the TaxID for a Product", e);
        throw new OBException(e);
      }
      if (StringUtils.isEmpty(taxID)) {
        throw new OBException("@TaxNotFound@");
      }
    }
    return taxID;
  }

  /**
   * Returns the last business partner location ID
   * 
   * @param businessPartner
   *          The business partner where the location will be searched
   * @return the last business partner location ID
   */
  private String getMaxBusinessPartnerLocationId(final BusinessPartner businessPartner) {
    OBCriteria<Location> obc = OBDal.getInstance().createCriteria(Location.class);
    obc.add(Restrictions.eq(Location.PROPERTY_BUSINESSPARTNER, businessPartner));
    obc.add(Restrictions.eq(Location.PROPERTY_ACTIVE, true));
    obc.setProjection(Projections.max(Location.PROPERTY_ID));
    obc.setMaxResults(1);
    return (String) obc.uniqueResult();
  }

  private void updateTaxAmount() {
    int stdPrecision = getInvoice().getCurrency().getStandardPrecision().intValue();
    BigDecimal taxAmt = getInvoiceLine().getLineNetAmount()
        .multiply(getInvoiceLine().getTax().getRate())
        .divide(new BigDecimal("100"), RoundingMode.HALF_EVEN)
        .setScale(stdPrecision, RoundingMode.HALF_UP);
    getInvoiceLine().setTaxAmount(taxAmt);
  }

  private void updateTaxableAmount() {
    BigDecimal taxBaseAmt = getInvoiceLine().getLineNetAmount();
    if (isCopiedFromOrderLine()
        || CreateLinesFromUtil.hasRelatedOrderLine((ShipmentInOutLine) getCopiedFromLine())) {
      taxBaseAmt = calculateAlternateTaxBaseAmtProrating(taxBaseAmt);
    }
    getInvoiceLine().setTaxableAmount(taxBaseAmt);
  }

  private BigDecimal calculateAlternateTaxBaseAmtProrating(final BigDecimal invoiceLineNetAmt) {
    BigDecimal taxBaseAmt = invoiceLineNetAmt;
    final OrderLine originalOrderLine = (isCopiedFromOrderLine() ? (OrderLine) getCopiedFromLine()
        : ((ShipmentInOutLine) getCopiedFromLine()).getSalesOrderLine());
    if (originalOrderLine.getTaxableAmount() != null) {
      BigDecimal originalOrderedQuantity = originalOrderLine.getOrderedQuantity();
      BigDecimal qtyOrdered = CreateLinesFromUtil.getOrderedQuantity(getPickExecJSONObject());
      taxBaseAmt = originalOrderLine.getTaxableAmount();
      if (originalOrderedQuantity.compareTo(BigDecimal.ZERO) != 0) {
        taxBaseAmt = taxBaseAmt.multiply(qtyOrdered).divide(originalOrderedQuantity,
            getInvoice().getCurrency().getStandardPrecision().intValue(), RoundingMode.HALF_UP);
      }
    }
    return taxBaseAmt;
  }
}
