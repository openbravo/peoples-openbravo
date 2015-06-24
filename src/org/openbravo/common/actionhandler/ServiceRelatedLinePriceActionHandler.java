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
import java.util.Date;
import java.util.Map;

import org.codehaus.jettison.json.JSONObject;
import org.hibernate.Query;
import org.openbravo.base.exception.OBException;
import org.openbravo.client.kernel.BaseActionHandler;
import org.openbravo.client.kernel.RequestContext;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.erpCommon.utility.OBDateUtils;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.model.common.order.OrderLine;
import org.openbravo.model.common.plm.Product;
import org.openbravo.model.common.plm.ServicePriceRuleVersion;
import org.openbravo.model.pricing.pricelist.PriceList;
import org.openbravo.model.pricing.pricelist.PriceListVersion;
import org.openbravo.model.pricing.pricelist.ProductPrice;
import org.openbravo.model.pricing.pricelist.ServicePriceRule;
import org.openbravo.model.pricing.pricelist.ServicePriceRuleRange;
import org.openbravo.service.db.DalConnectionProvider;
import org.openbravo.service.json.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServiceRelatedLinePriceActionHandler extends BaseActionHandler {
  private static final Logger log = LoggerFactory
      .getLogger(ServiceRelatedLinePriceActionHandler.class);

  private static int currencyPrecission;
  private static String strServiceProductId;
  private static Product product;

  @Override
  protected JSONObject execute(Map<String, Object> parameters, String content) {
    JSONObject jsonRequest = null;
    OBContext.setAdminMode(true);
    JSONObject errorMessage = new JSONObject();
    JSONObject result = new JSONObject();
    try {
      jsonRequest = new JSONObject(content);
      log.debug("{}", jsonRequest);

      final JSONObject record = jsonRequest.getJSONObject("record");
      final String strOrderLineId = record.getString("id");
      final String strDateOrdered = record.getString("orderDate");
      Date orderDate = JsonUtils.createDateFormat().parse(strDateOrdered);
      strServiceProductId = jsonRequest.getString("serviceProductId");
      product = OBDal.getInstance().get(Product.class, strServiceProductId);

      BigDecimal amount = BigDecimal.ZERO;

      if (!product.isPricerulebased()) {
        result.put("amount", amount);
        return result;
      }

      final OrderLine relatedOrderLine = OBDal.getInstance().get(OrderLine.class, strOrderLineId);
      currencyPrecission = relatedOrderLine.getCurrency().getStandardPrecision().intValue();

      // Get Service Price Rule Version of the Service Product for given date
      StringBuffer where = new StringBuffer();
      where.append(" select " + ServicePriceRuleVersion.PROPERTY_SERVICEPRICERULE);
      where.append(" from " + ServicePriceRuleVersion.ENTITY_NAME + " as sprv");
      where.append(" where sprv." + ServicePriceRuleVersion.PROPERTY_PRODUCT
          + ".id = :serviceProductId");
      where
          .append(" and sprv." + ServicePriceRuleVersion.PROPERTY_VALIDFROMDATE + " <= :orderDate");
      where.append(" order by sprv." + ServicePriceRuleVersion.PROPERTY_VALIDFROMDATE
          + " desc, sprv." + ServicePriceRuleVersion.PROPERTY_CREATIONDATE + " desc");
      Query sprvQry = OBDal.getInstance().getSession().createQuery(where.toString());
      sprvQry.setParameter("serviceProductId", strServiceProductId);
      sprvQry.setParameter("orderDate", orderDate);
      sprvQry.setMaxResults(1);
      ServicePriceRule spr = (ServicePriceRule) sprvQry.uniqueResult();

      if (spr == null) {
        throw new OBException("@ServicePriceRuleVersionNotFound@. @Product@: "
            + product.getIdentifier() + ", @Date@: " + OBDateUtils.formatDate(orderDate));
      }

      amount = relatedOrderLine.getLineNetAmount();

      if ("P".equals(spr.getRuletype())) {
        BigDecimal percentage = new BigDecimal(spr.getPercentage());
        if (percentage.compareTo(BigDecimal.ZERO) == 0) {
          amount = BigDecimal.ZERO;
        } else {
          amount = amount.divide(percentage, currencyPrecission, RoundingMode.HALF_UP);
        }
      } else {
        amount = getServiceRuleRangeAmount(spr.getId(), amount, orderDate);
      }

      result.put("amount", amount);
    } catch (Exception e) {
      log.error("Error in ServiceRelatedLinePriceActionHandler Action Handler", e);
      try {
        result = new JSONObject();
        String message = OBMessageUtils.parseTranslation(new DalConnectionProvider(false),
            RequestContext.get().getVariablesSecureApp(), OBContext.getOBContext().getLanguage()
                .getLanguage(), e.getMessage());
        errorMessage = new JSONObject();
        errorMessage.put("severity", "error");
        errorMessage.put("title", "Error");
        errorMessage.put("text", message);
        result.put("message", errorMessage);

      } catch (Exception e2) {
        log.error(e.getMessage(), e2);
      }
    } finally {
      OBContext.restorePreviousMode();
    }
    return result;
  }

  /**
   * Method that calculates the service amount to be related to a Sales Order Line based on the Line
   * Net Amount of the Sales Order Line and the Service Price Rule of Rule type 'Ranges' assigned to
   * the Service Product.
   * 
   * @param sprId
   *          Service Price Rule Id
   * @param lineamount
   *          Line Net Amount of the related Order Line
   * @param orderDate
   *          Order Date of the Sales Order
   * @return
   */
  private BigDecimal getServiceRuleRangeAmount(String sprId, BigDecimal lineamount, Date orderDate) {

    BigDecimal amount = BigDecimal.ZERO;
    StringBuffer where = new StringBuffer();
    where.append("  as sprr");
    where.append(" where " + ServicePriceRuleRange.PROPERTY_SERVICEPRICERULE
        + ".id = :servicePriceRuleId");
    where.append(" and (" + ServicePriceRuleRange.PROPERTY_AMOUNTUPTO + " >= :amount or "
        + ServicePriceRuleRange.PROPERTY_AMOUNTUPTO + " is null)");
    where.append(" order by " + ServicePriceRuleRange.PROPERTY_AMOUNTUPTO + ", "
        + ServicePriceRuleVersion.PROPERTY_CREATIONDATE + " desc");
    OBQuery<ServicePriceRuleRange> sprrQry = OBDal.getInstance().createQuery(
        ServicePriceRuleRange.class, where.toString());
    sprrQry.setNamedParameter("servicePriceRuleId", sprId);
    sprrQry.setNamedParameter("amount", lineamount);
    sprrQry.setMaxResult(1);
    ServicePriceRuleRange sprr = sprrQry.uniqueResult();

    if (sprr == null) {
      final ServicePriceRule spr = OBDal.getInstance().get(ServicePriceRule.class, sprId);
      throw new OBException("@ServicePriceRuleRangeNotFound@. @ServicePriceRule@: "
          + spr.getIdentifier() + ", @AmountUpTo@: " + lineamount);
    }

    if ("P".equals(sprr.getRuleType())) {
      BigDecimal percentage = new BigDecimal(sprr.getPercentage());
      if (percentage.compareTo(BigDecimal.ZERO) == 0) {
        amount = BigDecimal.ZERO;
      } else {
        amount = lineamount.divide(percentage, currencyPrecission, RoundingMode.HALF_UP);
      }
    } else {
      amount = getProductPrice(orderDate, sprr.getPriceList());
    }
    return amount;
  }

  /**
   * Method that returns the listPrice of a product in a Price List on a given date
   * 
   * @param date
   *          Order Date of the Sales Order
   * @param priceList
   *          Price List assigned in the Service Price Rule Range
   * @return
   */
  private BigDecimal getProductPrice(Date date, PriceList priceList) throws OBException {

    StringBuffer where = new StringBuffer();
    where.append(" select pp." + ProductPrice.PROPERTY_LISTPRICE + " as listPrice");
    where.append(" from " + ProductPrice.ENTITY_NAME + " as pp");
    where.append("   join pp." + ProductPrice.PROPERTY_PRICELISTVERSION + " as plv");
    where.append("   join plv." + PriceListVersion.PROPERTY_PRICELIST + " as pl");
    where.append(" where pp." + ProductPrice.PROPERTY_PRODUCT + ".id = :productId");
    where.append("   and plv." + PriceListVersion.PROPERTY_VALIDFROMDATE + " <= :date");
    where.append("   and pl = :pricelist");
    where.append(" order by pl." + PriceList.PROPERTY_DEFAULT + " desc, plv."
        + PriceListVersion.PROPERTY_VALIDFROMDATE + " desc");

    Query ppQry = OBDal.getInstance().getSession().createQuery(where.toString());
    ppQry.setParameter("productId", strServiceProductId);
    ppQry.setParameter("date", date);
    ppQry.setParameter("pricelist", priceList);

    ppQry.setMaxResults(1);
    BigDecimal listPrice = (BigDecimal) ppQry.uniqueResult();
    if (listPrice == null) {
      throw new OBException("@PriceListVersionNotFound@. @Product@: " + product.getIdentifier()
          + ", @Date@: " + OBDateUtils.formatDate(date));
    }
    return listPrice;
  }
}
