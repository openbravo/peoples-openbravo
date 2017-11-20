/*
 ************************************************************************************
 * Copyright (C) 2017 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.retail.posterminal.ad_callouts;

import javax.servlet.ServletException;

import org.apache.commons.lang.StringUtils;
import org.openbravo.erpCommon.ad_callouts.SimpleCallout;
import org.openbravo.erpCommon.utility.ISOCurrencyPrecision;
import org.openbravo.erpCommon.utility.Utility;

/**
 * Callout used to validate that the currency standard precision is correct. This is, validate with
 * the specified currency precision in ISO 4217 Currency codes. If the new precision is higher than
 * expected in the specification, then a warning is shown to user.
 *
 * @author JGA
 *
 */
public class POSCurrencyPrecision extends SimpleCallout {

  private static final int DEFAULT_CURRENCY_STANDARD_PRECISION = 2;

  @Override
  protected void execute(CalloutInfo info) throws ServletException {

    // Parameters
    final String paramStandardPOSPrecision = info.getStringParameter("inpemObposPosprecision");
    final String paramStandardPricePrecision = info.getStringParameter("inppriceprecision");
    final String paramISOCode = info.getStringParameter("inpisoCode");

    // Check POS Precision
    if (StringUtils.isNotEmpty(paramStandardPOSPrecision) && StringUtils.isNotEmpty(paramISOCode)) {
      final int stdPrecision = Integer.parseInt(paramStandardPOSPrecision);
      if (stdPrecision < 0) {
        info.addResult("inpstdprecision", DEFAULT_CURRENCY_STANDARD_PRECISION);
        info.addResult("ERROR", Utility.messageBD(this, "OBPOS_CALLOUT_CurrencyPOSPrecisionNegative",
            info.vars.getLanguage()));
      } else {
        int isoCurrencyPrecision = ISOCurrencyPrecision.getCurrencyPrecisionInISO4217Spec(paramISOCode);
        if (stdPrecision > isoCurrencyPrecision) {
          info.addResult("WARNING", String.format(
              Utility.messageBD(this, "OBPOS_CALLOUT_CurrencyPOSPrecisionHigherThanISOSpec",
                  info.vars.getLanguage()), stdPrecision, isoCurrencyPrecision, paramISOCode));
        }
      }
      // Check Price Precision if POS precision is empty
    } else if (StringUtils.isNotEmpty(paramStandardPricePrecision) && StringUtils.isNotEmpty(paramISOCode)) {
      final int stdPrecision = Integer.parseInt(paramStandardPricePrecision);
      if (stdPrecision < 0) {
        info.addResult("inpstdprecision", DEFAULT_CURRENCY_STANDARD_PRECISION);
        info.addResult("ERROR",Utility.messageBD(this, "OBPOS_CALLOUT_CurrencyPricePrecisionNegative",
            info.vars.getLanguage()));
      } else {
        int isoCurrencyPrecision = ISOCurrencyPrecision.getCurrencyPrecisionInISO4217Spec(paramISOCode);
        if (stdPrecision > isoCurrencyPrecision) {
          info.addResult("WARNING", String.format(
              Utility.messageBD(this, "OBPOS_CALLOUT_CurrencyPricePrecisionHigherThanISOSpec",
                  info.vars.getLanguage()), stdPrecision, isoCurrencyPrecision, paramISOCode));
        }
      }
    }
  }

}
