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

  public BigDecimal getServiceAmount(OrderLine orderline) {
    BigDecimal serviceBasePrice = getProductPrice(orderline.getOrderDate(), orderline
        .getSalesOrder().getPriceList(), orderline.getProduct());
    BigDecimal serviceRelatedPrice = BigDecimal.ZERO;
    boolean isPriceRuleBased = orderline.getProduct().isPricerulebased();
    if (!isPriceRuleBased) {
      return serviceBasePrice;
    } else {
      ServicePriceRule servicePriceRule = getServicePriceRule(orderline.getProduct(),
          orderline.getOrderDate());
      HashMap<String, BigDecimal> relatedAmountAndQuatity = getRelatedAmountAndQty(orderline);
      BigDecimal relatedAmount = relatedAmountAndQuatity.get("amount");
      // TODO: APPLY quantities
      BigDecimal relatedQty = relatedAmountAndQuatity.get("quantity");
      if (PERCENTAGE.equals(servicePriceRule.getRuletype())) {
        serviceRelatedPrice = relatedAmount.multiply(new BigDecimal(servicePriceRule
            .getPercentage()).divide(new BigDecimal("100.00"), orderline.getCurrency()
            .getStandardPrecision().intValue(), RoundingMode.HALF_UP));
      } else {
        ServicePriceRuleRange range = getRange(servicePriceRule, relatedAmount);
        if (PERCENTAGE.equals(range.getRuleType())) {
          serviceRelatedPrice = relatedAmount.multiply(new BigDecimal(range.getPercentage())
              .divide(new BigDecimal("100.00"), orderline.getCurrency().getStandardPrecision()
                  .intValue(), RoundingMode.HALF_UP));
        } else {
          serviceRelatedPrice = getProductPrice(orderline.getOrderDate(), orderline.getSalesOrder()
              .getPriceList(), orderline.getProduct());
        }
      }
      return serviceBasePrice.add(serviceRelatedPrice);
    }
  }

  private ServicePriceRuleRange getRange(ServicePriceRule servicePriceRule, BigDecimal relatedAmount) {
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

  HashMap<String, BigDecimal> getRelatedAmountAndQty(OrderLine orderLine) {
    StringBuffer strQuery = new StringBuffer();
    strQuery.append("select coalesce(sum(e.amount),0), coalesce(sum(e.quantity),0)");
    strQuery.append(" from OrderlineServiceRelation as e");
    strQuery.append(" where e.salesOrderLine.id = :orderLineId");
    Query query = OBDal.getInstance().getSession().createQuery(strQuery.toString());
    query.setParameter("orerLineId", orderLine.getId());
    query.setMaxResults(1);
    HashMap<String, BigDecimal> result = new HashMap<String, BigDecimal>();
    BigDecimal[] values = (BigDecimal[]) query.uniqueResult();
    result.put("amount", values[0]);
    result.put("quantity", values[1]);
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
  public BigDecimal getProductPrice(Date date, PriceList priceList, Product product)
      throws OBException {

    StringBuffer where = new StringBuffer();
    where.append(" select pp." + ProductPrice.PROPERTY_LISTPRICE + " as listPrice");
    where.append(" from " + ProductPrice.ENTITY_NAME + " as pp");
    where.append("   join pp." + ProductPrice.PROPERTY_PRICELISTVERSION + " as plv");
    where.append("   join plv." + PriceListVersion.PROPERTY_PRICELIST + " as pl");
    where.append(" where pp." + ProductPrice.PROPERTY_PRODUCT + ".id = :productId");
    where.append("   and plv." + PriceListVersion.PROPERTY_VALIDFROMDATE + " <= :date");
    where.append("   and pl.id = :pricelistId");
    where.append(" order by pl." + PriceList.PROPERTY_DEFAULT + " desc, plv."
        + PriceListVersion.PROPERTY_VALIDFROMDATE + " desc");

    Query ppQry = OBDal.getInstance().getSession().createQuery(where.toString());
    ppQry.setParameter("productId", product.getId());
    ppQry.setParameter("date", date);
    ppQry.setParameter("pricelistId", priceList.getId());

    ppQry.setMaxResults(1);
    return (BigDecimal) ppQry.uniqueResult();
  }

  public ServicePriceRule getServicePriceRule(Product serviceProduct, Date orderDate) {
    // Get Service Price Rule Version of the Service Product for given date
    StringBuffer where = new StringBuffer();
    where.append(" select " + ServicePriceRuleVersion.PROPERTY_SERVICEPRICERULE);
    where.append(" from " + ServicePriceRuleVersion.ENTITY_NAME + " as sprv");
    where.append(" where sprv." + ServicePriceRuleVersion.PROPERTY_PRODUCT
        + ".id = :serviceProductId");
    where.append(" and sprv." + ServicePriceRuleVersion.PROPERTY_VALIDFROMDATE + " <= :orderDate");
    where.append(" order by sprv." + ServicePriceRuleVersion.PROPERTY_VALIDFROMDATE
        + " desc, sprv." + ServicePriceRuleVersion.PROPERTY_CREATIONDATE + " desc");
    Query sprvQry = OBDal.getInstance().getSession().createQuery(where.toString());
    sprvQry.setParameter("serviceProductId", serviceProduct.getId());
    sprvQry.setParameter("orderDate", orderDate);
    sprvQry.setMaxResults(1);
    return (ServicePriceRule) sprvQry.uniqueResult();
  }
}
