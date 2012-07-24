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
 * All portions are Copyright (C) 2011-2012 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.common.actionhandler;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.client.application.process.BaseProcessActionHandler;
import org.openbravo.client.kernel.RequestContext;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.financial.FinancialUtils;
import org.openbravo.model.common.order.Order;
import org.openbravo.model.common.order.OrderLine;
import org.openbravo.model.common.order.ReturnReason;
import org.openbravo.model.common.plm.AttributeSetInstance;
import org.openbravo.model.common.plm.Product;
import org.openbravo.model.common.uom.UOM;
import org.openbravo.model.financialmgmt.tax.TaxRate;
import org.openbravo.model.materialmgmt.transaction.ShipmentInOutLine;
import org.openbravo.model.pricing.pricelist.ProductPrice;
import org.openbravo.service.db.CallStoredProcedure;
import org.openbravo.service.db.DalConnectionProvider;

/**
 * 
 * @author gorkaion
 * 
 */
public class SRMOPickEditLines extends BaseProcessActionHandler {
  private static Logger log = Logger.getLogger(SRMOPickEditLines.class);

  @Override
  protected JSONObject doExecute(Map<String, Object> parameters, String content) {
    JSONObject jsonRequest = null;
    OBContext.setAdminMode();
    try {
      jsonRequest = new JSONObject(content);
      log.debug(jsonRequest);
      // When the focus is NOT in the tab of the button (i.e. any child tab) and the tab does not
      // contain any record, the inpcOrderId parameter contains "null" string. Use C_Order_ID
      // instead because it always contains the id of the selected order.
      // Issue 20585: https://issues.openbravo.com/view.php?id=20585
      final String strOrderId = jsonRequest.getString("C_Order_ID");
      Order order = OBDal.getInstance().get(Order.class, strOrderId);
      if (cleanOrderLines(order)) {
        createOrderLines(jsonRequest);
      }

    } catch (Exception e) {
      OBDal.getInstance().rollbackAndClose();
      VariablesSecureApp vars = RequestContext.get().getVariablesSecureApp();
      log.error(e.getMessage(), e);

      try {
        jsonRequest = new JSONObject();

        JSONObject errorMessage = new JSONObject();
        errorMessage.put("severity", "error");
        errorMessage.put("text",
            Utility.messageBD(new DalConnectionProvider(), e.getMessage(), vars.getLanguage()));

        jsonRequest.put("message", errorMessage);
      } catch (Exception e2) {
        log.error(e.getMessage(), e2);
        // do nothing, give up
      }
    } finally {
      OBContext.restorePreviousMode();
    }
    return jsonRequest;
  }

  private boolean cleanOrderLines(Order order) {
    if (order == null) {
      return false;
    } else if (order.getOrderLineList().isEmpty()) {
      // nothing to delete.
      return true;
    }
    try {
      order.getOrderLineList().clear();
      OBDal.getInstance().save(order);
      OBDal.getInstance().flush();
    } catch (Exception e) {
      log.error(e.getMessage(), e);
      return false;
    }
    return true;
  }

  private void createOrderLines(JSONObject jsonRequest) throws JSONException, OBException {
    JSONArray selectedLines = jsonRequest.getJSONArray("_selection");
    // if no lines selected don't do anything.
    if (selectedLines.length() == 0) {
      return;
    }
    final String strOrderId = jsonRequest.getString("C_Order_ID");
    Order order = OBDal.getInstance().get(Order.class, strOrderId);
    boolean isSOTrx = order.isSalesTransaction();
    for (long i = 0; i < selectedLines.length(); i++) {
      JSONObject selectedLine = selectedLines.getJSONObject((int) i);
      log.debug(selectedLine);
      if (selectedLine.get("returned").equals(null)) {
        continue;
      }
      OrderLine newOrderLine = OBProvider.getInstance().get(OrderLine.class);
      newOrderLine.setSalesOrder(order);
      newOrderLine.setOrganization(order.getOrganization());
      newOrderLine.setLineNo((i + 1L) * 10L);
      newOrderLine.setOrderDate(order.getOrderDate());
      newOrderLine.setWarehouse(order.getWarehouse());
      newOrderLine.setCurrency(order.getCurrency());

      ShipmentInOutLine shipmentLine = OBDal.getInstance().get(ShipmentInOutLine.class,
          selectedLine.getString("goodsShipmentLine"));
      Product product = OBDal.getInstance().get(Product.class, selectedLine.getString("product"));
      AttributeSetInstance asi = null;
      if (!selectedLine.get("attributeSetValue").equals(null)) {
        asi = OBDal.getInstance().get(AttributeSetInstance.class,
            selectedLine.getString("attributeSetValue"));
      }
      UOM uom = OBDal.getInstance().get(UOM.class, selectedLine.get("uOM"));

      newOrderLine.setGoodsShipmentLine(shipmentLine);
      newOrderLine.setProduct(product);
      newOrderLine.setAttributeSetValue(asi);
      newOrderLine.setUOM(uom);
      // Ordered Quantity = returned quantity.
      BigDecimal qtyReturned = new BigDecimal(selectedLine.getString("returned")).negate();
      newOrderLine.setOrderedQuantity(qtyReturned);

      TaxRate tax = null;
      if (shipmentLine != null && shipmentLine.getSalesOrderLine() != null) {
        tax = shipmentLine.getSalesOrderLine().getTax();
      } else {
        String taxId = "";
        if (selectedLine.get("tax").equals(null)) {
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

          taxId = (String) CallStoredProcedure.getInstance().call("C_Gettax", parameters, null);
          if (taxId == null || "".equals(taxId)) {
            Map<String, String> errorParameters = new HashMap<String, String>();
            errorParameters.put("product", product.getName());
            String message = OBMessageUtils.messageBD("NoTaxFoundForProduct");
            throw new OBException(OBMessageUtils.parseTranslation(message, errorParameters));
          }
        } else {
          taxId = selectedLine.getString("tax");
        }
        tax = OBDal.getInstance().get(TaxRate.class, taxId);
      }
      newOrderLine.setTax(tax);

      // Price
      BigDecimal unitPrice, netPrice, grossPrice, stdPrice, limitPrice, grossAmt, netListPrice, grossListPrice;
      stdPrice = BigDecimal.ZERO;
      final int pricePrecision = order.getCurrency().getPricePrecision().intValue();
      final int stdPrecision = order.getCurrency().getStandardPrecision().intValue();

      if (selectedLine.get("unitPrice").equals(null)) {
        try {
          final ProductPrice pp = FinancialUtils.getProductPrice(product, order.getOrderDate(),
              isSOTrx, order.getPriceList());
          unitPrice = pp.getStandardPrice();
          limitPrice = pp.getPriceLimit();
          netListPrice = pp.getListPrice();
          grossListPrice = pp.getListPrice();
          stdPrice = pp.getStandardPrice();
        } catch (OBException e) {
          // Product not found in price list. Prices default to ZERO
          unitPrice = limitPrice = netListPrice = grossListPrice = stdPrice = BigDecimal.ZERO;
        }
      } else {
        unitPrice = new BigDecimal(selectedLine.getString("unitPrice"));
        if (shipmentLine != null && shipmentLine.getSalesOrderLine() != null) {
          limitPrice = shipmentLine.getSalesOrderLine().getPriceLimit();
          netListPrice = shipmentLine.getSalesOrderLine().getListPrice();
          grossListPrice = shipmentLine.getSalesOrderLine().getGrossListPrice();
          stdPrice = shipmentLine.getSalesOrderLine().getStandardPrice();
        } else {
          limitPrice = netListPrice = grossListPrice = stdPrice = unitPrice;
        }
      }

      if (order.getPriceList().isPriceIncludesTax()) {
        grossPrice = unitPrice;
        grossAmt = grossPrice.multiply(qtyReturned)
            .setScale(stdPrecision, BigDecimal.ROUND_HALF_UP);
        netPrice = FinancialUtils.calculateNetFromGross(tax.getId(), grossAmt, pricePrecision,
            grossAmt, qtyReturned);
        limitPrice = netPrice;
        stdPrice = netPrice;
        netListPrice = netPrice;
      } else {
        netPrice = unitPrice;
        grossListPrice = grossAmt = grossPrice = BigDecimal.ZERO;
      }

      newOrderLine.setUnitPrice(netPrice);
      newOrderLine.setGrossUnitPrice(grossPrice);
      newOrderLine.setListPrice(netListPrice);
      newOrderLine.setGrossListPrice(grossListPrice);
      newOrderLine.setPriceLimit(limitPrice);
      newOrderLine.setStandardPrice(stdPrice);
      newOrderLine.setLineNetAmount(netPrice.multiply(qtyReturned).setScale(stdPrecision,
          BigDecimal.ROUND_HALF_UP));
      newOrderLine.setLineGrossAmount(grossAmt);

      if (!selectedLine.get("returnReason").equals(null)) {
        newOrderLine.setReturnReason(OBDal.getInstance().get(ReturnReason.class,
            selectedLine.getString("returnReason")));
      } else {
        newOrderLine.setReturnReason(order.getReturnReason());
      }

      List<OrderLine> orderLines = order.getOrderLineList();
      orderLines.add(newOrderLine);
      order.setOrderLineList(orderLines);

      OBDal.getInstance().save(newOrderLine);
      OBDal.getInstance().save(order);
      OBDal.getInstance().flush();
    }
  }
}
