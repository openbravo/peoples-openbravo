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

package org.openbravo.materialmgmt;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.HashMap;

import org.hibernate.Query;
import org.openbravo.base.exception.OBException;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.erpCommon.utility.OBDateUtils;
import org.openbravo.model.common.order.OrderLine;
import org.openbravo.model.common.plm.Product;
import org.openbravo.model.common.plm.ServicePriceRuleVersion;
import org.openbravo.model.pricing.pricelist.PriceList;
import org.openbravo.model.pricing.pricelist.PriceListVersion;
import org.openbravo.model.pricing.pricelist.ProductPrice;
import org.openbravo.model.pricing.pricelist.ServicePriceRule;
import org.openbravo.model.pricing.pricelist.ServicePriceRuleRange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServicePriceUtils {
  private static final Logger log = LoggerFactory.getLogger(ServicePriceUtils.class);
  private static final String PERCENTAGE = "P";

  public static BigDecimal getServiceAmount(OrderLine orderline) {
    BigDecimal servicePrice = getServiceAmount(orderline, null);
    return servicePrice;
  }

  public static BigDecimal getServiceAmount(OrderLine orderline, BigDecimal linesTotalAmount) {
    if (linesTotalAmount != null && linesTotalAmount.compareTo(BigDecimal.ZERO) == 0) {
      return BigDecimal.ZERO;
    }
    BigDecimal serviceBasePrice = getProductPrice(orderline.getOrderDate(), orderline
        .getSalesOrder().getPriceList(), orderline.getProduct());
    if (serviceBasePrice == null) {
      throw new OBException("@ServiceProductPriceListVersionNotFound@ "
          + orderline.getProduct().getIdentifier() + ", @Date@: "
          + OBDateUtils.formatDate(orderline.getOrderDate()));
    }
    BigDecimal serviceRelatedPrice = BigDecimal.ZERO;
    boolean isPriceRuleBased = orderline.getProduct().isPricerulebased();
    if (!isPriceRuleBased) {
      return BigDecimal.ZERO;
    } else {
      ServicePriceRule servicePriceRule = getServicePriceRule(orderline.getProduct(),
          orderline.getOrderDate());
      if (servicePriceRule == null) {
        throw new OBException("@ServicePriceRuleVersionNotFound@ "
            + orderline.getProduct().getIdentifier() + ", @Date@: "
            + OBDateUtils.formatDate(orderline.getOrderDate()));
      }
      BigDecimal relatedAmount = BigDecimal.ZERO;
      BigDecimal relatedQty = BigDecimal.ZERO;
      if (linesTotalAmount != null) {
        relatedAmount = linesTotalAmount;
      } else {
        HashMap<String, BigDecimal> relatedAmountAndQuatity = getRelatedAmountAndQty(orderline);
        relatedAmount = relatedAmountAndQuatity.get("amount");
        // TODO: APPLY quantities
        relatedQty = relatedAmountAndQuatity.get("quantity");
      }

      if (PERCENTAGE.equals(servicePriceRule.getRuletype())) {
        serviceRelatedPrice = relatedAmount.multiply(new BigDecimal(servicePriceRule
            .getPercentage()).divide(new BigDecimal("100.00"), orderline.getCurrency()
            .getStandardPrecision().intValue(), RoundingMode.HALF_UP));
      } else {
        ServicePriceRuleRange range = getRange(servicePriceRule, relatedAmount);
        if (range == null) {
          throw new OBException("@ServicePriceRuleRangeNotFound@. @ServicePriceRule@: "
              + servicePriceRule.getIdentifier() + ", @AmountUpTo@: " + linesTotalAmount);
        }
        if (PERCENTAGE.equals(range.getRuleType())) {
          serviceRelatedPrice = relatedAmount.multiply(new BigDecimal(range.getPercentage())
              .divide(new BigDecimal("100.00"), orderline.getCurrency().getStandardPrecision()
                  .intValue(), RoundingMode.HALF_UP));
        } else {
          serviceRelatedPrice = getProductPrice(orderline.getOrderDate(), range.getPriceList(),
              orderline.getProduct());
          if (serviceRelatedPrice == null) {
            throw new OBException("@ServiceProductPriceListVersionNotFound@ "
                + orderline.getProduct().getIdentifier() + ", @Date@: "
                + OBDateUtils.formatDate(orderline.getOrderDate()));
          }
        }
      }
      return serviceRelatedPrice;
    }
  }

  /**
   * Method that returns a range for a certain Service Price Rule for the given amount
   * 
   * @param servicePriceRule
   *          Service Price Rule
   * @param relatedAmount
   *          Amount
   * @return
   */
  private static ServicePriceRuleRange getRange(ServicePriceRule servicePriceRule,
      BigDecimal relatedAmount) {
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
    sprrQry.setNamedParameter("servicePriceRuleId", servicePriceRule.getId());
    sprrQry.setNamedParameter("amount", relatedAmount);
    sprrQry.setMaxResult(1);
    return sprrQry.uniqueResult();
  }

  public static HashMap<String, BigDecimal> getRelatedAmountAndQty(OrderLine orderLine) {
    StringBuffer strQuery = new StringBuffer();
    strQuery.append("select coalesce(sum(e.amount),0), coalesce(sum(e.quantity),0)");
    strQuery.append(" from OrderlineServiceRelation as e");
    strQuery.append(" where e.salesOrderLine.id = :orderLineId");
    Query query = OBDal.getInstance().getSession().createQuery(strQuery.toString());
    query.setParameter("orderLineId", orderLine.getId());
    query.setMaxResults(1);
    HashMap<String, BigDecimal> result = new HashMap<String, BigDecimal>();
    Object[] values = (Object[]) query.uniqueResult();
    result.put("amount", (BigDecimal) values[0]);
    result.put("quantity", (BigDecimal) values[1]);
    return result;
  }

  /**
   * Method that returns the listPrice of a product in a Price List on a given date
   * 
   * @param date
   *          Order Date of the Sales Order
   * @param priceList
   *          Price List assigned in the Service Price Rule Range
   * @param product
   *          Product to search in Price List
   * @return
   */
  public static BigDecimal getProductPrice(Date date, PriceList priceList, Product product)
      throws OBException {

    StringBuffer where = new StringBuffer();
    where.append(" select pp." + ProductPrice.PROPERTY_LISTPRICE + " as listPrice");
    where.append(" from " + ProductPrice.ENTITY_NAME + " as pp");
    where.append("   join pp." + ProductPrice.PROPERTY_PRICELISTVERSION + " as plv");
    where.append("   join plv." + PriceListVersion.PROPERTY_PRICELIST + " as pl");
    where.append(" where pp." + ProductPrice.PROPERTY_PRODUCT + ".id = :productId");
    where.append("   and plv." + PriceListVersion.PROPERTY_VALIDFROMDATE + " <= :date");
    where.append("   and pl.id = :pricelistId");
    where.append("   and pl." + PriceList.PROPERTY_ACTIVE + " = true");
    where.append("   and pp." + ProductPrice.PROPERTY_ACTIVE + " = true");
    where.append("   and plv." + PriceListVersion.PROPERTY_ACTIVE + " = true");
    where.append(" order by pl." + PriceList.PROPERTY_DEFAULT + " desc, plv."
        + PriceListVersion.PROPERTY_VALIDFROMDATE + " desc");

    Query ppQry = OBDal.getInstance().getSession().createQuery(where.toString());
    ppQry.setParameter("productId", product.getId());
    ppQry.setParameter("date", date);
    ppQry.setParameter("pricelistId", priceList.getId());

    ppQry.setMaxResults(1);
    return (BigDecimal) ppQry.uniqueResult();
  }

  /**
   * Method that returns for a "Price Rule Based" Service product the Service Price Rule on the
   * given date
   * 
   * @param serviceProduct
   *          Service Product
   * @param orderDate
   *          Order Date of the Sales Order
   * @return
   */
  public static ServicePriceRule getServicePriceRule(Product serviceProduct, Date orderDate) {

    StringBuffer where = new StringBuffer();
    where.append(" select " + ServicePriceRuleVersion.PROPERTY_SERVICEPRICERULE);
    where.append(" from " + ServicePriceRuleVersion.ENTITY_NAME + " as sprv");
    where.append(" where sprv." + ServicePriceRuleVersion.PROPERTY_PRODUCT
        + ".id = :serviceProductId");
    where.append(" and sprv." + ServicePriceRuleVersion.PROPERTY_VALIDFROMDATE + " <= :orderDate");
    where.append("   and sprv." + ServicePriceRuleVersion.PROPERTY_ACTIVE + " = true");
    where.append(" order by sprv." + ServicePriceRuleVersion.PROPERTY_VALIDFROMDATE
        + " desc, sprv." + ServicePriceRuleVersion.PROPERTY_CREATIONDATE + " desc");
    Query sprvQry = OBDal.getInstance().getSession().createQuery(where.toString());
    sprvQry.setParameter("serviceProductId", serviceProduct.getId());
    sprvQry.setParameter("orderDate", orderDate);
    sprvQry.setMaxResults(1);
    return (ServicePriceRule) sprvQry.uniqueResult();
  }

  /**
   * 
   * Method that returns the next Line Number of a Sales Order
   * 
   * @param order
   *          Order
   */
  public static Long getNewLineNo(String orderId) {
    StringBuffer where = new StringBuffer();
    where.append(" as ol");
    where.append(" where ol." + OrderLine.PROPERTY_SALESORDER + ".id = :orderId");
    where.append(" order by ol." + OrderLine.PROPERTY_LINENO + " desc");
    OBQuery<OrderLine> olQry = OBDal.getInstance().createQuery(OrderLine.class, where.toString());
    olQry.setNamedParameter("orderId", orderId);
    if (olQry.count() > 0) {
      OrderLine ol = olQry.list().get(0);
      return ol.getLineNo() + 10L;
    }
    return 10L;
  }
}
