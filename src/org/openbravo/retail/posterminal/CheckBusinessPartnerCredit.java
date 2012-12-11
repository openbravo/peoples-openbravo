/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.retail.posterminal;

import java.math.BigDecimal;

import javax.servlet.ServletException;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.common.businesspartner.BusinessPartner;
import org.openbravo.service.json.JsonConstants;

public class CheckBusinessPartnerCredit extends JSONProcessSimple {

  @Override
  public JSONObject exec(JSONObject jsonsent) throws JSONException, ServletException {

    JSONObject respObject = new JSONObject();

    String businessPartnerId = jsonsent.getString("businessPartnerId");
    Double doubleTotalPending = jsonsent.getDouble("totalPending");
    BigDecimal totalPending = new BigDecimal(doubleTotalPending);
    BusinessPartner businessPartner = OBDal.getInstance().get(BusinessPartner.class,
        businessPartnerId);
    BigDecimal creditLimit = businessPartner.getCreditLimit();
    BigDecimal creditUsed = businessPartner.getCreditUsed();
    BigDecimal actualCredit = creditLimit.subtract(creditUsed);

    boolean enoughCredit = false;

    if (actualCredit.compareTo(totalPending) == -1) {
      enoughCredit = false;
    } else {
      enoughCredit = true;
    }

    respObject.put("enoughCredit", enoughCredit);
    respObject.put("actualCredit", actualCredit);
    respObject.put("bpName", businessPartner.getName());

    JSONObject result = new JSONObject();
    result.put(JsonConstants.RESPONSE_DATA, respObject);
    result.put(JsonConstants.RESPONSE_STATUS, JsonConstants.RPCREQUEST_STATUS_SUCCESS);
    return result;

  }
}