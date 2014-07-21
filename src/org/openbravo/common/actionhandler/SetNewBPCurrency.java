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
 * All portions are Copyright (C) 2014 Openbravo SLU 
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
import org.openbravo.advpaymentmngt.utility.FIN_Utility;
import org.openbravo.client.application.process.BaseProcessActionHandler;
import org.openbravo.client.kernel.RequestContext;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.model.common.businesspartner.BusinessPartner;
import org.openbravo.model.common.currency.ConversionRate;
import org.openbravo.model.common.currency.Currency;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.service.db.DbUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SetNewBPCurrency extends BaseProcessActionHandler {
  private static final Logger log = LoggerFactory.getLogger(SetNewBPCurrency.class);

  @Override
  protected JSONObject doExecute(Map<String, Object> parameters, String content) {

    JSONObject jsonRequest = null;
    OBContext.setAdminMode(true);
    try {
      jsonRequest = new JSONObject(content);
      JSONObject params = jsonRequest.getJSONObject("_params");
      final String strOrgId = jsonRequest.getString("inpadOrgId");
      final String strFromCurrencyId = jsonRequest.getString("inpbpCurrencyId");
      final String strToCurrencyId = params.getString("C_Currency_ID");
      final String strRate = params.getString("Rate");
      final String strAmount = params.getString("Foreign_Amount");
      final boolean strSetAmount = params.getBoolean("Amount");
      final boolean strUseDefaultConversion = params.getBoolean("Default_Conversion_Rate");
      final String strBpartnerId = jsonRequest.getString("C_BPartner_ID");
      BigDecimal creditUsed = BigDecimal.ZERO;
      BigDecimal rate = BigDecimal.ZERO;
      Double amount = new Double(0);
      if (strSetAmount && !"null".equals(strAmount)) {
        amount = Double.parseDouble(strAmount);
      }

      if (strUseDefaultConversion && !strSetAmount) {
        rate = getConversionRate(strOrgId, strFromCurrencyId, strToCurrencyId);
        if (rate == BigDecimal.ZERO && !strFromCurrencyId.equals(strToCurrencyId)) {
          try {
            jsonRequest = new JSONObject();
            String message = OBMessageUtils.messageBD("NoCurrencyConversion");
            JSONObject errorMessage = new JSONObject();
            errorMessage.put("severity", "error");
            errorMessage.put("text", message);
            jsonRequest.put("message", errorMessage);
          } catch (Exception e) {
            OBDal.getInstance().rollbackAndClose();
            log.error(e.getMessage(), e);
          }
          return jsonRequest;
        }
      } else {
        rate = "null".equals(strRate) ? BigDecimal.ZERO : BigDecimal.valueOf(Double
            .parseDouble(strRate));
      }
      BusinessPartner businessPartner = OBDal.getInstance().get(BusinessPartner.class,
          strBpartnerId);
      creditUsed = businessPartner.getCreditUsed();
      if (strSetAmount && creditUsed.compareTo(BigDecimal.valueOf(amount)) != 0) {
        businessPartner.setCreditUsed(BigDecimal.valueOf(amount));
      }
      if (!strToCurrencyId.equals(strFromCurrencyId) && strToCurrencyId != null
          && !"null".equals(strToCurrencyId)) {
        businessPartner.setCurrency(OBDal.getInstance().get(Currency.class, strToCurrencyId));
        if (rate.compareTo(BigDecimal.ZERO) != 0 && creditUsed.compareTo(BigDecimal.ZERO) != 0
            && !strSetAmount) {
          businessPartner.setCreditUsed(creditUsed.multiply(rate));
        }
      }

      String messageText = OBMessageUtils.messageBD("CurrencyUpdated");
      JSONObject msg = new JSONObject();
      msg.put("severity", "success");
      msg.put("text", OBMessageUtils.parseTranslation(messageText));
      jsonRequest.put("message", msg);

    } catch (Exception e) {
      OBDal.getInstance().rollbackAndClose();
      log.error("Error in set new currency Action Handler", e);

      try {
        jsonRequest = new JSONObject();
        Throwable ex = DbUtility.getUnderlyingSQLException(e);
        String message = OBMessageUtils.translateError(ex.getMessage()).getMessage();
        JSONObject errorMessage = new JSONObject();
        errorMessage.put("severity", "error");
        errorMessage.put("text", message);
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

  private BigDecimal getConversionRate(String strOrgId, String strFromCurrencyId,
      String strToCurrencyId) {
    final Date today = new Date();
    BigDecimal exchangeRate = BigDecimal.ZERO;
    // Apply default conversion rate
    int conversionRatePrecision = FIN_Utility.getConversionRatePrecision(RequestContext.get()
        .getVariablesSecureApp());
    Organization organization = OBDal.getInstance().get(Organization.class, strOrgId);
    Currency fromCurrency = OBDal.getInstance().get(Currency.class, strFromCurrencyId);
    Currency toCurrency = OBDal.getInstance().get(Currency.class, strToCurrencyId);
    final ConversionRate conversionRate = FIN_Utility.getConversionRate(fromCurrency, toCurrency,
        today, organization);
    if (conversionRate != null) {
      exchangeRate = conversionRate.getMultipleRateBy().setScale(conversionRatePrecision,
          RoundingMode.HALF_UP);
    } else {
      exchangeRate = BigDecimal.ZERO;
    }
    return exchangeRate;
  }
}