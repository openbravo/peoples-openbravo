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
package org.openbravo.erpCommon.ad_callouts;

import java.math.BigDecimal;
import java.util.Date;

import javax.servlet.ServletException;

import org.hibernate.criterion.Restrictions;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.base.util.Convert;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.common.currency.ConversionRate;
import org.openbravo.model.common.currency.Currency;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.financialmgmt.payment.FIN_FinancialAccount;

/**
 * Update exchange rate and financial txn amount
 * 
 * @author eintelau (ben.sommerville@eintel.com.au)*
 */
public class SE_Payment_Currency extends SimpleCallout {

  private static final long serialVersionUID = 1L;

  @Override
  protected void execute(CalloutInfo info) throws ServletException {
    VariablesSecureApp vars = info.vars;
    String financialAccountId = vars.getStringParameter("inpfinFinancialAccountId");
    String currencyId = vars.getStringParameter("inpcCurrencyId");
    String strOrgId = vars.getStringParameter("inpadOrgId");

    String finAccConvertRate = "";
    String finAccTxnAmount = "";

    FIN_FinancialAccount financialAccount = OBDal.getInstance().get(FIN_FinancialAccount.class,
        financialAccountId);
    Currency currency = OBDal.getInstance().get(Currency.class, currencyId);

    if (financialAccount != null && currency != null) {
      String strAmount = vars.getNumericParameter("inpamount");
      final Currency financialAccountCurrency = financialAccount.getCurrency();
      if (currency.equals(financialAccountCurrency)) {
        finAccConvertRate = "1";
        finAccTxnAmount = strAmount;
      } else {
        String paymentDate = vars.getStringParameter("inppaymentdate");
        final ConversionRate conversionRate = getConversionRate(currency, financialAccountCurrency,
            Convert.toDate(paymentDate), OBDal.getInstance().get(Organization.class, strOrgId));

        BigDecimal amount = Convert.toAmount(strAmount);
        if (amount != null && conversionRate != null) {
          // TODO the multiplied rate taken from the conversion rate should be rounded before
          // operating (use FIN_Utility.getConversionRatePrecision)
          BigDecimal converted = amount.multiply(conversionRate.getMultipleRateBy());
          finAccTxnAmount = Convert.toStringWithPrecision(converted, financialAccountCurrency
              .getStandardPrecision());
        }
      }
    }
    info.addResult("inpfinaccTxnConvertRate", finAccConvertRate);
    info.addResult("inpfinaccTxnAmount", finAccTxnAmount);

  }

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
  private ConversionRate getConversionRate(Currency fromCurrency, Currency toCurrency,
      Date conversionDate, Organization org) {
    java.util.List<ConversionRate> conversionRateList;
    ConversionRate conversionRate;
    OBContext.setAdminMode(true);
    try {
      final OBCriteria<ConversionRate> obcConvRate = OBDal.getInstance().createCriteria(
          ConversionRate.class);
      obcConvRate.setFilterOnReadableOrganization(false);
      obcConvRate.add(Restrictions.eq(ConversionRate.PROPERTY_ORGANIZATION, org));
      obcConvRate.add(Restrictions.eq(ConversionRate.PROPERTY_CURRENCY, fromCurrency));
      obcConvRate.add(Restrictions.eq(ConversionRate.PROPERTY_TOCURRENCY, toCurrency));
      obcConvRate.add(Restrictions.le(ConversionRate.PROPERTY_VALIDFROMDATE, conversionDate));
      obcConvRate.add(Restrictions.ge(ConversionRate.PROPERTY_VALIDTODATE, conversionDate));
      conversionRateList = obcConvRate.list();
      if ((conversionRateList != null) && (conversionRateList.size() != 0)) {
        conversionRate = conversionRateList.get(0);
      } else {
        if ("0".equals(org.getId())) {
          conversionRate = null;
        } else {
          return getConversionRate(fromCurrency, toCurrency, conversionDate, OBDal.getInstance()
              .get(
                  Organization.class,
                  OBContext.getOBContext().getOrganizationStructureProvider().getParentOrg(
                      org.getId())));
        }
      }
    } catch (Exception e) {
      log4j.error(e);
      return null;
    } finally {
      OBContext.restorePreviousMode();
    }
    return conversionRate;
  }
}
