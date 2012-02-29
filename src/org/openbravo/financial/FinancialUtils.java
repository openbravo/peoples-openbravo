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
 * The Initial Developer of the Original Code is Openbravo SLU
 * All portions are Copyright (C) 2012 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 *************************************************************************
 */
package org.openbravo.financial;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.exception.OBException;
import org.openbravo.dal.core.DalUtil;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.DateUtility;
import org.openbravo.model.common.currency.ConversionRate;
import org.openbravo.model.common.currency.Currency;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.common.plm.Product;
import org.openbravo.model.pricing.pricelist.PriceList;
import org.openbravo.model.pricing.pricelist.PriceListVersion;
import org.openbravo.model.pricing.pricelist.ProductPrice;

public class FinancialUtils {
  protected static Logger log4j = Logger.getLogger(FinancialUtils.class);

  public static final String PRECISION_STANDARD = "A";
  public static final String PRECISION_COSTING = "C";
  public static final String PRECISION_PRICE = "P";

  public static BigDecimal getProductStdPrice(Product product, boolean useSalesPriceList,
      Date date, Currency currency, Organization organization) {
    ProductPrice pp = getProductPrice(product, useSalesPriceList, date);
    BigDecimal price = pp.getStandardPrice();
    if (!DalUtil.getId(pp.getPriceListVersion().getPriceList().getCurrency()).equals(
        currency.getId())) {
      // Conversion is needed.
      price = getConvertedAmount(price, pp.getPriceListVersion().getPriceList().getCurrency(),
          currency, date, organization, PRECISION_PRICE);
    }

    return price;
  }

  public static ProductPrice getProductPrice(Product product, boolean useSalesPriceList, Date date)
      throws OBException {
    OBCriteria<ProductPrice> ppc = OBDal.getInstance().createCriteria(ProductPrice.class);
    ppc.createAlias(ProductPrice.PROPERTY_PRICELISTVERSION, "plv");
    ppc.createAlias("plv." + PriceListVersion.PROPERTY_PRICELIST, "pl");
    ppc.add(Restrictions.eq(ProductPrice.PROPERTY_PRODUCT, product));
    ppc.add(Restrictions.eq("pl." + PriceList.PROPERTY_SALESPRICELIST, useSalesPriceList));
    ppc.add(Restrictions.le("plv." + PriceListVersion.PROPERTY_VALIDFROMDATE, date));
    ppc.addOrderBy("pl." + PriceList.PROPERTY_DEFAULT, false);
    ppc.addOrderBy("plv." + PriceListVersion.PROPERTY_VALIDFROMDATE, false);
    ppc.setMaxResults(1);

    List<ProductPrice> ppList = ppc.list();
    if (ppList.isEmpty()) {
      // No product price found.
      throw new OBException("@PriceListVersionNotFound@. @Product@: " + product.getIdentifier()
          + " @Date@: " + DateUtility.formatDate(date));
    }
    return ppList.get(0);
  }

  /**
   * Method to get the conversion rate defined at system level. If there is not a conversion rate
   * defined on the given Organization it is searched recursively on its parent organization until
   * one is found. If no conversion rate is found on the parent organization tree null is returned.
   * 
   * @param date
   *          Date conversion is being performed.
   * @param fromCurrency
   *          Currency to convert from.
   * @param toCurrency
   *          Currency to convert to.
   * @param org
   *          Organization of the document that needs to be converted.
   * @return a valid ConversionRate for the given parameters, null if none is found.
   */
  public static ConversionRate getConversionRate(Date date, Currency fromCurrency,
      Currency toCurrency, Organization org) {
    ConversionRate conversionRate;
    // Readable Client Org filters to false as organization is filtered explicitly.
    OBContext.setAdminMode(false);
    try {
      final OBCriteria<ConversionRate> obcConvRate = OBDal.getInstance().createCriteria(
          ConversionRate.class);
      obcConvRate.add(Restrictions.eq(ConversionRate.PROPERTY_ORGANIZATION, org));
      obcConvRate.add(Restrictions.eq(ConversionRate.PROPERTY_CURRENCY, fromCurrency));
      obcConvRate.add(Restrictions.eq(ConversionRate.PROPERTY_TOCURRENCY, toCurrency));
      obcConvRate.add(Restrictions.le(ConversionRate.PROPERTY_VALIDFROMDATE, date));
      obcConvRate.add(Restrictions.ge(ConversionRate.PROPERTY_VALIDTODATE, date));
      conversionRate = (ConversionRate) obcConvRate.uniqueResult();
      if (conversionRate != null) {
        return conversionRate;
      }
      if ("0".equals(org.getId())) {
        return null;
      } else {
        return getConversionRate(date, fromCurrency, toCurrency, OBContext.getOBContext()
            .getOrganizationStructureProvider().getParentOrg(org));
      }
    } catch (Exception e) {
      log4j.error(e);
      return null;
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  /**
   * Converts an amount.
   * 
   * @param amount
   *          BigDecimal amount to convert.
   * @param curFrom
   *          Currency to convert from.
   * @param curTo
   *          Currency to convert to.
   * @param date
   *          Date conversion is being performed.
   * @param organization
   *          Organization of the document that needs to be converted.
   * @param strPrecision
   *          type of precision to be used to round the converted amount.
   * @return a BigDecimal representing the converted amount.
   * @throws OBException
   *           when no Conversion Rate is found for the given parameters.
   */

  public static BigDecimal getConvertedAmount(BigDecimal amount, Currency curFrom, Currency curTo,
      Date date, Organization organization, String strPrecision) throws OBException {
    if (curFrom.getId().equals(curTo.getId()) || amount.signum() == 0) {
      return amount;
    }
    ConversionRate cr = getConversionRate(date, curFrom, curTo, organization);
    if (cr == null) {
      // FIXME: improve message with context information.
      throw new OBException("@NoCurrencyConversion@");
    }
    Long precision = curTo.getStandardPrecision();
    if (PRECISION_COSTING.equals(strPrecision)) {
      precision = curTo.getCostingPrecision();
    } else if (PRECISION_PRICE.equals(strPrecision)) {
      precision = curTo.getPricePrecision();
    }
    return amount.multiply(cr.getMultipleRateBy()).setScale(precision.intValue());
  }
}
