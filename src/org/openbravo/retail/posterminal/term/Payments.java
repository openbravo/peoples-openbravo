/*
 ************************************************************************************
 * Copyright (C) 2012-2014 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.retail.posterminal.term;

import java.math.BigDecimal;

import javax.servlet.ServletException;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.Query;
import org.hibernate.Session;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.client.kernel.RequestContext;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.mobile.core.process.SimpleQueryBuilder;
import org.openbravo.retail.posterminal.JSONProcessSimple;
import org.openbravo.service.json.DataResolvingMode;
import org.openbravo.service.json.DataToJsonConverter;
import org.openbravo.service.json.JsonConstants;

public class Payments extends JSONProcessSimple {

  @Override
  public JSONObject exec(JSONObject jsonsent) throws JSONException, ServletException {
    JSONObject result = new JSONObject();
    OBContext.setAdminMode(true);

    try {

      JSONArray respArray = new JSONArray();
      String posId = RequestContext.get().getSessionAttribute("POSTerminal").toString();
      String hqlPayments = "select p as payment, p.paymentMethod as paymentMethod, "
          + "c_currency_rate(p.financialAccount.currency, p.obposApplications.organization.currency, null, null, p.obposApplications.client.id, p.obposApplications.organization.id) as rate, c_currency_rate(p.obposApplications.organization.currency, p.financialAccount.currency, null, null, p.obposApplications.client.id, p.obposApplications.organization.id) as mulrate, "
          + "p.financialAccount.currency.iSOCode as isocode, "
          + "p.financialAccount.currency.symbol as symbol, p.financialAccount.currency.currencySymbolAtTheRight as currencySymbolAtTheRight, "
          + "p.financialAccount.currentBalance as currentBalance "
          + "from OBPOS_App_Payment as p where p.obposApplications.id=? "
          + "and p.$readableCriteria order by p.line, p.commercialName";

      SimpleQueryBuilder querybuilder = new SimpleQueryBuilder(hqlPayments, OBContext
          .getOBContext().getCurrentClient().getId(), OBContext.getOBContext()
          .getCurrentOrganization().getId(), null);

      final Session session = OBDal.getInstance().getSession();
      final Query paymentsquery = session.createQuery(querybuilder.getHQLQuery());
      paymentsquery.setString(0, posId);

      DataToJsonConverter converter = new DataToJsonConverter();

      for (Object objLine : paymentsquery.list()) {
        Object[] objPayment = (Object[]) objLine;
        JSONObject payment = new JSONObject();
        payment.put("payment",
            converter.toJsonObject((BaseOBObject) objPayment[0], DataResolvingMode.FULL));
        payment.put("paymentMethod",
            converter.toJsonObject((BaseOBObject) objPayment[1], DataResolvingMode.FULL));

        payment.put("rate", objPayment[2]);
        BigDecimal mulrate = BigDecimal.ZERO;
        BigDecimal rate = new BigDecimal((String) objPayment[2]);
        if (rate.compareTo(BigDecimal.ZERO) != 0) {
          mulrate = BigDecimal.ONE.divide(rate, 12, 4);
        }
        payment.put("mulrate", mulrate.toPlainString());

        payment.put("isocode", objPayment[4]);
        payment.put("symbol", objPayment[5]);
        payment.put("currencySymbolAtTheRight", objPayment[6]);
        payment.put("currentBalance", objPayment[7]);
        respArray.put(payment);

      }

      result.put(JsonConstants.RESPONSE_DATA, respArray);
      result.put(JsonConstants.RESPONSE_STATUS, JsonConstants.RPCREQUEST_STATUS_SUCCESS);

      return result;

    } finally {
      OBContext.restorePreviousMode();
    }
  }

}
