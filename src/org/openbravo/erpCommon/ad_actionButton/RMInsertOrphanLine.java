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
 * All portions are Copyright (C) 2012 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.erpCommon.ad_actionButton;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openbravo.base.exception.OBException;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.utility.OBDateUtils;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.model.common.order.Order;
import org.openbravo.model.common.order.OrderLine;
import org.openbravo.model.common.order.ReturnReason;
import org.openbravo.model.common.plm.AttributeSetInstance;
import org.openbravo.model.common.plm.Product;
import org.openbravo.model.financialmgmt.tax.TaxRate;
import org.openbravo.model.materialmgmt.transaction.MaterialTransaction;
import org.openbravo.model.pricing.pricelist.PriceList;
import org.openbravo.model.pricing.pricelist.PriceListVersion;
import org.openbravo.model.pricing.pricelist.ProductPrice;
import org.openbravo.scheduling.ProcessBundle;
import org.openbravo.service.db.CallStoredProcedure;

public class RMInsertOrphanLine implements org.openbravo.scheduling.Process {

  @Override
  public void execute(ProcessBundle bundle) throws Exception {
    final String language = bundle.getContext().getLanguage();
    final ConnectionProvider conProvider = bundle.getConnection();
    final VariablesSecureApp vars = bundle.getContext().toVars();

    OBError msg = new OBError();
    msg.setType("Success");
    msg.setTitle(OBMessageUtils.messageBD("Success"));

    final String strOrderId = (String) bundle.getParams().get("C_Order_ID");
    final String strProductId = (String) bundle.getParams().get("mProductId");
    final String strASIId = (String) bundle.getParams().get("mAttributesetinstanceId");
    final String strReturnedQty = (String) bundle.getParams().get("returned");
    final BigDecimal returnedQty = new BigDecimal(strReturnedQty);
    final String strUnitPrice = (String) bundle.getParams().get("pricestd");
    final String strTaxId = (String) bundle.getParams().get("cTaxId");
    final String strReturnReason = (String) bundle.getParams().get("cReturnReasonId");

    Order order = OBDal.getInstance().get(Order.class, strOrderId);
    Product product = OBDal.getInstance().get(Product.class, strProductId);
    AttributeSetInstance asi = null;
    if (strASIId.isEmpty()) {
      asi = OBDal.getInstance().get(AttributeSetInstance.class, "0");
    } else {
      asi = OBDal.getInstance().get(AttributeSetInstance.class, strASIId);
    }

    // Check attributesetinstance has been used with the product
    StringBuffer where = new StringBuffer();
    where.append(" as trx");
    where.append(" where trx." + MaterialTransaction.PROPERTY_PRODUCT + " = :product");
    where.append("   and trx." + MaterialTransaction.PROPERTY_ATTRIBUTESETVALUE + " = :asi");
    OBQuery<MaterialTransaction> attrQry = OBDal.getInstance().createQuery(
        MaterialTransaction.class, where.toString());
    attrQry.setNamedParameter("product", product);
    attrQry.setNamedParameter("asi", asi);
    if (attrQry.count() == 0) {
      Map<String, String> parameters = new HashMap<String, String>();
      parameters.put("product", product.getName());
      parameters.put("attribute", asi.getDescription());
      String message = OBMessageUtils.messageBD("WrongAttributeForProduct");
      msg.setMessage(OBMessageUtils.parseTranslation(message, parameters));
      msg.setTitle(OBMessageUtils.messageBD("Error"));
      msg.setType("Error");
      bundle.setResult(msg);
      return;
    }

    OBContext.setAdminMode(true);
    try {
      OrderLine newOrderLine = OBProvider.getInstance().get(OrderLine.class);
      newOrderLine.setSalesOrder(order);
      newOrderLine.setOrganization(order.getOrganization());
      newOrderLine.setLineNo(getNewLineNo(order));
      newOrderLine.setOrderDate(order.getOrderDate());
      newOrderLine.setWarehouse(order.getWarehouse());
      newOrderLine.setCurrency(order.getCurrency());
      newOrderLine.setProduct(product);
      newOrderLine.setAttributeSetValue(asi);
      newOrderLine.setUOM(product.getUOM());
      newOrderLine.setOrderedQuantity(returnedQty.negate());

      if (strUnitPrice.isEmpty()) {
        ProductPrice productPrice = getProductPrice(product, order.getOrderDate(),
            order.isSalesTransaction(), order.getPriceList());
        newOrderLine.setUnitPrice(productPrice.getStandardPrice());
        newOrderLine.setListPrice(productPrice.getListPrice());
        newOrderLine.setPriceLimit(productPrice.getPriceLimit());
        newOrderLine.setStandardPrice(productPrice.getStandardPrice());
      } else {
        BigDecimal unitPrice = new BigDecimal(strUnitPrice);
        newOrderLine.setUnitPrice(unitPrice);
        newOrderLine.setListPrice(unitPrice);
        newOrderLine.setPriceLimit(unitPrice);
        newOrderLine.setStandardPrice(unitPrice);
      }
      // tax
      TaxRate tax = null;
      if (strTaxId.isEmpty()) {
        List<Object> parameters = new ArrayList<Object>();
        parameters.add(product.getId());
        parameters.add(order.getOrderDate());
        parameters.add(order.getOrganization().getId());
        parameters.add(order.getWarehouse().getId());
        parameters.add(order.getPartnerAddress().getId());
        parameters.add(order.getInvoiceAddress().getId());
        if (order.getProject() != null) {
          parameters.add(order.getProject().getId());
        } else {
          parameters.add(null);
        }
        parameters.add("Y");

        String strDefaultTaxId = (String) CallStoredProcedure.getInstance().call("C_Gettax",
            parameters, null);
        if (strDefaultTaxId == null || strDefaultTaxId.equals("")) {
          OBDal.getInstance().rollbackAndClose();
          Map<String, String> errorParameters = new HashMap<String, String>();
          errorParameters.put("product", product.getName());
          String message = OBMessageUtils.messageBD("InsertOrphanNoTaxFoundForProduct");
          msg.setMessage(OBMessageUtils.parseTranslation(message, errorParameters));
          msg.setTitle(OBMessageUtils.messageBD("Error"));
          msg.setType("Error");
          bundle.setResult(msg);
          return;
        }
        tax = OBDal.getInstance().get(TaxRate.class, strDefaultTaxId);
      } else {
        tax = OBDal.getInstance().get(TaxRate.class, strTaxId);
      }

      newOrderLine.setTax(tax);

      if (strReturnReason.isEmpty()) {
        newOrderLine.setReturnReason(OBDal.getInstance().get(ReturnReason.class, strReturnReason));
      } else {
        newOrderLine.setReturnReason(order.getReturnReason());
      }

      List<OrderLine> orderLines = order.getOrderLineList();
      orderLines.add(newOrderLine);
      order.setOrderLineList(orderLines);

      OBDal.getInstance().save(newOrderLine);
      OBDal.getInstance().save(order);
      OBDal.getInstance().flush();

    } finally {
      OBContext.restorePreviousMode();
    }

    bundle.setResult(msg);
  }

  private Long getNewLineNo(Order order) {
    StringBuffer where = new StringBuffer();
    where.append(" as ol");
    where.append(" where ol." + OrderLine.PROPERTY_SALESORDER + " = :order");
    where.append(" order by ol." + OrderLine.PROPERTY_LINENO + " desc");
    OBQuery<OrderLine> olQry = OBDal.getInstance().createQuery(OrderLine.class, where.toString());
    olQry.setNamedParameter("order", order);
    if (olQry.count() > 0) {
      OrderLine ol = olQry.list().get(0);
      return ol.getLineNo() + 10L;
    }
    return 10L;
  }

  private ProductPrice getProductPrice(Product product, Date date, boolean useSalesPriceList,
      PriceList priceList) throws OBException {
    StringBuffer where = new StringBuffer();
    where.append(" as pp");
    where.append("   join pp." + ProductPrice.PROPERTY_PRICELISTVERSION + " as plv");
    where.append("   join plv." + PriceListVersion.PROPERTY_PRICELIST + " as pl");
    where.append(" where pp." + ProductPrice.PROPERTY_PRODUCT + " = :product");
    where.append("   and plv." + PriceListVersion.PROPERTY_VALIDFROMDATE + " <= :date");
    if (priceList != null) {
      where.append("   and pl = :pricelist");
    } else {
      where.append("   and pl." + PriceList.PROPERTY_SALESPRICELIST + " = :salespricelist");
    }
    where.append(" order by pl." + PriceList.PROPERTY_DEFAULT + " desc, plv."
        + PriceListVersion.PROPERTY_VALIDFROMDATE + " desc");

    OBQuery<ProductPrice> ppQry = OBDal.getInstance().createQuery(ProductPrice.class,
        where.toString());
    ppQry.setNamedParameter("product", product);
    ppQry.setNamedParameter("date", date);
    if (priceList != null) {
      ppQry.setNamedParameter("pricelist", priceList);
    } else {
      ppQry.setNamedParameter("salespricelist", useSalesPriceList);
    }

    List<ProductPrice> ppList = ppQry.list();
    if (ppList.isEmpty()) {
      // No product price found.
      throw new OBException("@PriceListVersionNotFound@. @Product@: " + product.getIdentifier()
          + " @Date@: " + OBDateUtils.formatDate(date));
    }
    return ppList.get(0);
  }
}
