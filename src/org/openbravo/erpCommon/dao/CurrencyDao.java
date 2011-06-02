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
 * All portions are Copyright (C) 2011 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  Enterprise Intelligence Systems (http://www.eintel.com.au).
 ************************************************************************
 */
package org.openbravo.erpCommon.dao;

import java.util.Date;

import org.hibernate.criterion.Restrictions;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.common.currency.ConversionRate;
import org.openbravo.model.common.currency.Currency;

/**
 * This class contains common methods for querying and working with Currencies within Openbravo.
 * 
 * @author eintelau (ben.sommerville@eintel.com.au)
 */
public class CurrencyDao {

  public static long CONVERSION_RATE_PRECISION = 6;

  /**
   * Determine the conversion rate from one currency to another on a given date. Will use the spot
   * conversion rate defined by the system for that date
   * 
   * @param fromCurrency
   *          Currency to convert from
   * @param toCurrency
   *          Currency being converted to
   * @param conversionDate
   *          Date conversion is being performed
   * @return A valid conversion rate for the parameters, or null if no conversion rate can be found
   */
  public ConversionRate getConversionRate(Currency fromCurrency, Currency toCurrency,
      Date conversionDate) {
    java.util.List<ConversionRate> conversionRateList;
    ConversionRate conversionRate;

    OBContext.setAdminMode();
    try {

      final OBCriteria<ConversionRate> obcConvRate = OBDal.getInstance().createCriteria(
          ConversionRate.class);
      obcConvRate.add(Restrictions.eq(ConversionRate.PROPERTY_CURRENCY, fromCurrency));
      obcConvRate.add(Restrictions.eq(ConversionRate.PROPERTY_TOCURRENCY, toCurrency));
      obcConvRate.add(Restrictions.le(ConversionRate.PROPERTY_VALIDFROMDATE, conversionDate));
      obcConvRate.add(Restrictions.ge(ConversionRate.PROPERTY_VALIDTODATE, conversionDate));

      conversionRateList = obcConvRate.list();

      if ((conversionRateList != null) && (conversionRateList.size() != 0))
        conversionRate = conversionRateList.get(0);
      else
        conversionRate = null;

    } finally {
      OBContext.restorePreviousMode();
    }

    return conversionRate;

  }

  public Currency getCurrency(String currencyId) {
    return OBDal.getInstance().get(Currency.class, currencyId);
  }

}
