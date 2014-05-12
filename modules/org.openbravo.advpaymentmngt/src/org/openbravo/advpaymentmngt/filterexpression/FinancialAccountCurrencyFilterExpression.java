/*
 ************************************************************************************
 * Copyright (C) 2014 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.advpaymentmngt.filterexpression;

import java.util.Map;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.client.application.FilterExpression;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.common.businesspartner.BusinessPartner;

public class FinancialAccountCurrencyFilterExpression implements FilterExpression {

  private static final Logger log = Logger
      .getLogger(FinancialAccountCurrencyFilterExpression.class);

  @Override
  public String getExpression(Map<String, String> requestMap) {
    BusinessPartner businessPartner = null;
    try {
      OBContext.setAdminMode(true);
      JSONObject context = new JSONObject((String) requestMap.get("context"));
      if (context.get("inpcBpartnerId") != null && !context.get("inpcBpartnerId").equals("")
          && !context.get("inpcBpartnerId").equals("null")) {
        businessPartner = OBDal.getInstance().get(BusinessPartner.class,
            context.get("inpcBpartnerId"));
        if (businessPartner.getAccount() != null) {
          return businessPartner.getAccount().getCurrency().getId();
        } else {
          return null;
        }
      } else {
        return null;
      }
    } catch (JSONException e) {
      log.error(
          "Error trying to get Currency of Financial Account on FinancialAccountCurrencyFilterExpression class: "
              + e.getMessage(), e);
      return null;
    } finally {
      OBContext.restorePreviousMode();
    }
  }
}
