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
 * All portions are Copyright (C) 2015 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.common.actionhandler;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.client.application.process.BaseProcessActionHandler;
import org.openbravo.client.kernel.RequestContext;
import org.openbravo.dal.core.DalUtil;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.erpCommon.utility.SequenceIdData;
import org.openbravo.materialmgmt.ServicePriceUtils;
import org.openbravo.model.ad.system.Client;
import org.openbravo.model.common.currency.Currency;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.common.order.OrderLine;
import org.openbravo.model.common.order.OrderlineServiceRelation;
import org.openbravo.model.common.plm.Product;
import org.openbravo.service.db.DalConnectionProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServiceOrderLineRelate extends BaseProcessActionHandler {
  private static final Logger log = LoggerFactory.getLogger(ServiceOrderLineRelate.class);
  private static final String UNIQUE_QUANTITY = "UQ";
  private static final String RFC_ORDERLINE_TAB_ID = "AF4090093D471431E040007F010048A5";

  @Override
  protected JSONObject doExecute(Map<String, Object> parameters, String content) {
    JSONObject jsonRequest = null;
    OBContext.setAdminMode(true);
    JSONObject errorMessage = new JSONObject();
    try {
      jsonRequest = new JSONObject(content);
      log.debug("{}", jsonRequest);

      JSONArray selectedLines = jsonRequest.getJSONObject("_params").getJSONObject("grid")
          .getJSONArray("_selection");

      final String tabId = jsonRequest.getString("inpTabId");
      final BigDecimal signum = RFC_ORDERLINE_TAB_ID.equals(tabId) ? new BigDecimal("-1")
          : BigDecimal.ONE;

      BigDecimal totalQuantity = BigDecimal.ZERO;
      BigDecimal totalPositiveLinesQuantity = BigDecimal.ZERO;
      BigDecimal totalNegativeLinesQuantity = BigDecimal.ZERO;
      BigDecimal totalPositiveLinesAmount = BigDecimal.ZERO;
      BigDecimal totalNegativeLinesAmount = BigDecimal.ZERO;

      OrderLine secondOrderline = null;

      final Client serviceProductClient = (Client) OBDal.getInstance().getProxy(Client.ENTITY_NAME,
          jsonRequest.getString("inpadClientId"));
      final Organization serviceProductOrg = (Organization) OBDal.getInstance().getProxy(
          Organization.ENTITY_NAME, jsonRequest.getString("inpadOrgId"));
      OrderLine mainOrderLine = (OrderLine) OBDal.getInstance().getProxy(OrderLine.ENTITY_NAME,
          jsonRequest.getString("inpcOrderlineId"));
      final Product serviceProduct = mainOrderLine.getProduct();
      final String orderId = mainOrderLine.getSalesOrder().getId();
      final Long lineNo = ServicePriceUtils.getNewLineNo(orderId);

      // Check if the order line has positive or negative relations. If it has no relations then
      // false;

      boolean existingLinesNegative = existsNegativeLines(mainOrderLine);

      // Delete existing rows
      StringBuffer where = new StringBuffer();
      where.append(" as rol");
      where.append(" where " + OrderlineServiceRelation.PROPERTY_SALESORDERLINE
          + ".id = :orderLineId");
      OBQuery<OrderlineServiceRelation> rol = OBDal.getInstance().createQuery(
          OrderlineServiceRelation.class, where.toString());

      rol.setNamedParameter("orderLineId", mainOrderLine.getId());
      rol.setMaxResult(1000);

      final ScrollableResults scroller = rol.scroll(ScrollMode.FORWARD_ONLY);
      while (scroller.next()) {
        final OrderlineServiceRelation or = (OrderlineServiceRelation) scroller.get()[0];
        OBDal.getInstance().remove(or);
      }
      OBDal.getInstance().flush();

      mainOrderLine = (OrderLine) OBDal.getInstance().getProxy(OrderLine.ENTITY_NAME,
          jsonRequest.getString("inpcOrderlineId"));

      boolean positiveLines = false;
      boolean negativeLines = false;

      // Check if there are negative quantity and positive quantity lines
      for (int i = 0; i < selectedLines.length(); i++) {
        JSONObject selectedLine = selectedLines.getJSONObject(i);
        BigDecimal lineQuantity = new BigDecimal(selectedLine.getDouble("relatedQuantity"));
        if (lineQuantity.compareTo(BigDecimal.ZERO) < 0) {
          // There are negative quantity lines
          negativeLines = true;
        }
        if (lineQuantity.compareTo(BigDecimal.ZERO) > 0) {
          // There are positive quantity lines
          positiveLines = true;
        }
        if (negativeLines && positiveLines) {
          break;
        }
      }

      // Adding new rows
      for (int i = 0; i < selectedLines.length(); i++) {
        JSONObject selectedLine = selectedLines.getJSONObject(i);
        log.debug("{}", selectedLine);
        final OrderLine orderLine = (OrderLine) OBDal.getInstance().getProxy(OrderLine.ENTITY_NAME,
            selectedLine.getString(OrderLine.PROPERTY_ID));

        // Check if deferred sale is allowed for the service, does not apply for returns
        if (!RFC_ORDERLINE_TAB_ID.equals(tabId)) {
          ServicePriceUtils.deferredSaleAllowed(mainOrderLine, orderLine);
        }

        BigDecimal lineAmount = new BigDecimal(selectedLine.getDouble("amount"));
        BigDecimal lineQuantity = new BigDecimal(selectedLine.getDouble("relatedQuantity"));

        if (lineQuantity.compareTo(BigDecimal.ZERO) < 0) {
          totalNegativeLinesQuantity = totalNegativeLinesQuantity.add(lineQuantity);
          totalNegativeLinesAmount = totalNegativeLinesAmount.add(lineAmount);
          // New order line due to there are positive quantity and negative quantity relations
          if (secondOrderline == null && positiveLines) {
            secondOrderline = (OrderLine) DalUtil.copy(mainOrderLine, false);
            secondOrderline.setLineNo(lineNo);
            secondOrderline.setId(SequenceIdData.getUUID());
            secondOrderline.setNewOBObject(true);
            OBDal.getInstance().save(secondOrderline);
            OBDal.getInstance().flush();
            OBDal.getInstance().refresh(secondOrderline);
          }
        } else {
          totalPositiveLinesQuantity = totalPositiveLinesQuantity.add(lineQuantity);
          totalPositiveLinesAmount = totalPositiveLinesAmount.add(lineAmount);
        }

        OrderlineServiceRelation olsr = OBProvider.getInstance()
            .get(OrderlineServiceRelation.class);
        olsr.setClient(serviceProductClient);
        olsr.setOrganization(serviceProductOrg);
        olsr.setOrderlineRelated(orderLine);
        if ((lineQuantity.compareTo(BigDecimal.ZERO) < 0 && positiveLines && !existingLinesNegative)
            || (lineQuantity.compareTo(BigDecimal.ZERO) > 0 && existingLinesNegative && negativeLines)) {
          olsr.setSalesOrderLine(secondOrderline);
        } else {
          olsr.setSalesOrderLine(mainOrderLine);
        }
        olsr.setAmount(lineAmount.multiply(signum));
        olsr.setQuantity(lineQuantity.multiply(signum));
        OBDal.getInstance().save(olsr);
        totalQuantity = totalQuantity.add(lineQuantity);
        if ((i % 100) == 0) {
          OBDal.getInstance().flush();
          OBDal.getInstance().getSession().clear();
        }
      }

      // Update orderlines

      BigDecimal baseProductPrice = ServicePriceUtils.getProductPrice(mainOrderLine.getSalesOrder()
          .getOrderDate(), mainOrderLine.getSalesOrder().getPriceList(), serviceProduct);

      BigDecimal firstLineQuantity = BigDecimal.ZERO;
      BigDecimal secondLineQuantity = BigDecimal.ZERO;
      BigDecimal firstLineAmount = BigDecimal.ZERO;
      BigDecimal secondLineAmount = BigDecimal.ZERO;

      // Conditions to check which order line has negative relations and which one has positive
      // relations

      if ((!existingLinesNegative && negativeLines && !positiveLines)
          || (existingLinesNegative && negativeLines)) {
        firstLineQuantity = totalNegativeLinesQuantity;
        firstLineAmount = totalNegativeLinesAmount;
        secondLineQuantity = totalPositiveLinesQuantity;
        secondLineAmount = totalPositiveLinesAmount;
        if (UNIQUE_QUANTITY.equals(serviceProduct.getQuantityRule())
            || firstLineQuantity.compareTo(BigDecimal.ZERO) == 0) {
          firstLineQuantity = new BigDecimal("-1");
        }
        if (UNIQUE_QUANTITY.equals(serviceProduct.getQuantityRule())
            || secondLineQuantity.compareTo(BigDecimal.ZERO) == 0) {
          secondLineQuantity = BigDecimal.ONE;
        }
      } else {
        firstLineQuantity = totalPositiveLinesQuantity;
        firstLineAmount = totalPositiveLinesAmount;
        secondLineQuantity = totalNegativeLinesQuantity;
        secondLineAmount = totalNegativeLinesAmount;
        if (UNIQUE_QUANTITY.equals(serviceProduct.getQuantityRule())
            || firstLineQuantity.compareTo(BigDecimal.ZERO) == 0) {
          firstLineQuantity = BigDecimal.ONE;
        }
        if (UNIQUE_QUANTITY.equals(serviceProduct.getQuantityRule())
            || secondLineQuantity.compareTo(BigDecimal.ZERO) == 0) {
          secondLineQuantity = new BigDecimal("-1");
        }
      }

      // Update main order line total values
      updateOrderline(mainOrderLine, firstLineAmount, firstLineQuantity, baseProductPrice, signum);

      // Update new created sales order line total values
      if (secondOrderline != null) {
        updateOrderline(secondOrderline, secondLineAmount, secondLineQuantity, baseProductPrice,
            signum);
      }
      OBDal.getInstance().flush();

      errorMessage.put("severity", "success");
      errorMessage.put("title", OBMessageUtils.messageBD("Success"));
      jsonRequest.put("message", errorMessage);
    } catch (Exception e) {
      log.error("Error in ServiceOrderLineRelate Action Handler", e);
      OBDal.getInstance().rollbackAndClose();
      try {
        jsonRequest = new JSONObject();
        String message = OBMessageUtils.parseTranslation(new DalConnectionProvider(false),
            RequestContext.get().getVariablesSecureApp(), OBContext.getOBContext().getLanguage()
                .getLanguage(), e.getMessage());
        errorMessage = new JSONObject();
        errorMessage.put("severity", "error");
        errorMessage.put("text", message);
        jsonRequest.put("message", errorMessage);

      } catch (Exception e2) {
        log.error(e.getMessage(), e2);
      }
    } finally {
      OBContext.restorePreviousMode();
    }
    return jsonRequest;
  }

  private void updateOrderline(OrderLine mainOrderLine, BigDecimal lineAmount,
      BigDecimal lineQuantity, BigDecimal baseProductPrice, BigDecimal signum) {
    BigDecimal listPrice = BigDecimal.ZERO;
    final Currency currency = mainOrderLine.getCurrency();

    BigDecimal serviceAmount = ServicePriceUtils.getServiceAmount(mainOrderLine, lineAmount)
        .setScale(currency.getPricePrecision().intValue(), RoundingMode.HALF_UP);

    BigDecimal servicePrice = baseProductPrice.add(serviceAmount.divide(lineQuantity, currency
        .getPricePrecision().intValue(), RoundingMode.HALF_UP));
    serviceAmount = serviceAmount.add(baseProductPrice.multiply(lineQuantity)).setScale(
        currency.getPricePrecision().intValue(), RoundingMode.HALF_UP);

    if (mainOrderLine.getSalesOrder().isPriceIncludesTax()) {
      mainOrderLine.setGrossUnitPrice(servicePrice);
      mainOrderLine.setLineGrossAmount(serviceAmount);
      mainOrderLine.setBaseGrossUnitPrice(servicePrice);
      listPrice = mainOrderLine.getGrossListPrice();
    } else {
      mainOrderLine.setUnitPrice(servicePrice);
      mainOrderLine.setLineNetAmount(serviceAmount);
      mainOrderLine.setStandardPrice(servicePrice);
      listPrice = mainOrderLine.getListPrice();
    }
    mainOrderLine.setTaxableAmount(serviceAmount);
    // Multiply with signum depending if the process is executed from Sales Order Line or from RFC
    // Line
    mainOrderLine.setOrderedQuantity(lineQuantity.multiply(signum));

    // Calculate discount
    BigDecimal discount = listPrice.subtract(servicePrice).multiply(new BigDecimal("100"))
        .divide(listPrice, currency.getPricePrecision().intValue(), RoundingMode.HALF_UP);
    mainOrderLine.setDiscount(discount);
    OBDal.getInstance().save(mainOrderLine);
  }

  private boolean existsNegativeLines(OrderLine mainOrderLine) {
    StringBuffer where = new StringBuffer();
    where.append(" as olsr");
    where.append(" where olsr." + OrderlineServiceRelation.PROPERTY_SALESORDERLINE
        + " = :salesorderline");
    OBQuery<OrderlineServiceRelation> olsrQry = OBDal.getInstance().createQuery(
        OrderlineServiceRelation.class, where.toString());
    olsrQry.setNamedParameter("salesorderline", mainOrderLine);
    olsrQry.setMaxResult(1);
    OrderlineServiceRelation osr = olsrQry.uniqueResult();
    if (osr != null) {
      return osr.getQuantity().compareTo(BigDecimal.ZERO) < 0 ? true : false;
    }
    return false;
  }
}
