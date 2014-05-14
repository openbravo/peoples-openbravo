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
package org.openbravo.advpaymentmngt.filterexpression;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.advpaymentmngt.dao.AdvPaymentMngtDao;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.client.kernel.RequestContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.utility.OBDateUtils;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.common.businesspartner.BusinessPartner;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.service.db.CallStoredProcedure;
import org.openbravo.service.db.DalConnectionProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
abstract class AddPaymentDefaultValuesHandler {
  private static final Logger log = LoggerFactory.getLogger(AddPaymentDefaultValuesHandler.class);

  abstract String getDefaultExpectedAmount(Map<String, String> requestMap) throws JSONException;

  abstract String getDefaultActualAmount(Map<String, String> requestMap) throws JSONException;

  abstract String getDefaultIsSOTrx(Map<String, String> requestMap);

  abstract String getDefaultTransactionType(Map<String, String> requestMap);

  String getDefaultCurrencyTo(Map<String, String> requestMap) throws JSONException {
    String strBPartnerId = getBusinessPartner(requestMap);
    if (StringUtils.isNotEmpty(strBPartnerId)) {
      BusinessPartner bPartner = OBDal.getInstance().get(BusinessPartner.class, strBPartnerId);
      if (bPartner.getAccount() != null) {
        return bPartner.getAccount().getCurrency().getId();
      } else {
        return null;
      }
    } else {
      return null;
    }
  }

  String getDefaultCustomerCredit(Map<String, String> requestMap) throws JSONException {
    String strBusinessPartnerId = getBusinessPartner(requestMap);
    JSONObject context = new JSONObject(requestMap.get("context"));
    String strOrgId = context.getString("inpadOrgId");
    String strReceipt = getDefaultIsSOTrx(requestMap);
    if (StringUtils.isEmpty(strBusinessPartnerId) || strOrgId == null || strReceipt == null) {
      return null;
    }
    BusinessPartner bpartner = OBDal.getInstance().get(BusinessPartner.class, strBusinessPartnerId);
    Organization org = OBDal.getInstance().get(Organization.class, strOrgId);
    BigDecimal customerCredit = new AdvPaymentMngtDao().getCustomerCredit(bpartner,
        "Y".equals(strReceipt), org);
    return customerCredit.toPlainString();

  }

  String getDefaultDocumentNo(Map<String, String> requestMap) throws JSONException {
    ConnectionProvider conn = new DalConnectionProvider(false);
    VariablesSecureApp vars = RequestContext.get().getVariablesSecureApp();
    JSONObject context = new JSONObject(requestMap.get("context"));

    // get DocumentNo
    final List<Object> parameters = new ArrayList<Object>();
    parameters.add(vars.getClient());
    parameters.add(context.get("inpadOrgId"));
    parameters.add("Y".equals(getDefaultIsSOTrx(requestMap)) ? "ARR" : "APP");
    String strDocTypeId = (String) CallStoredProcedure.getInstance().call("AD_GET_DOCTYPE",
        parameters, null);

    // Last parameter false to not to update database yet, in action handler should be true
    String strDocNo = Utility.getDocumentNo(conn, vars, "AddPaymentFromInvoice", "FIN_Payment",
        strDocTypeId, strDocTypeId, false, false);

    return "<" + strDocNo + ">";
  }

  private String getBusinessPartner(Map<String, String> requestMap) throws JSONException {
    JSONObject context = new JSONObject(requestMap.get("context"));
    if (context.has("inpcBpartnerId") && context.get("inpcBpartnerId") != null) {
      return context.getString("inpcBpartnerId");
    }
    return "";
  }

  public String getDefaultFinancialAccount(Map<String, String> requestMap) throws JSONException {
    JSONObject context = new JSONObject(requestMap.get("context"));
    if (context.has("inpfinFinancialAccountId") && context.get("inpfinFinancialAccountId") != null
        && StringUtils.isNotEmpty(context.getString("inpfinFinancialAccountId"))) {
      return context.getString("inpfinFinancialAccountId");
    }
    String strBPartnerId = getBusinessPartner(requestMap);

    if (StringUtils.isNotEmpty(strBPartnerId)) {
      BusinessPartner businessPartner = OBDal.getInstance().get(BusinessPartner.class,
          context.get("inpcBpartnerId"));
      boolean isSOTrx = "Y".equals(getDefaultIsSOTrx(requestMap));
      if (isSOTrx && businessPartner.getAccount() != null) {
        return businessPartner.getAccount().getId();
      } else if (!isSOTrx && businessPartner.getPOFinancialAccount() != null) {
        return businessPartner.getPOFinancialAccount().getId();
      }
    }
    return null;
  }

  String getDefaultPaymentDate(Map<String, String> requestMap) throws JSONException {
    return OBDateUtils.formatDate(new Date());
  }

  String getDefaultPaymentMethod(Map<String, String> requestMap) throws JSONException {
    JSONObject context = new JSONObject(requestMap.get("context"));
    if (context.has("inpfinPaymentmethodId") && context.get("inpfinPaymentmethodId") != null
        && StringUtils.isNotBlank("inpfinPaymentmethodId")) {
      return context.getString("inpfinPaymentmethodId");
    }
    String strBPartnerId = getBusinessPartner(requestMap);
    if (StringUtils.isNotEmpty(strBPartnerId)) {
      BusinessPartner businessPartner = OBDal.getInstance().get(BusinessPartner.class,
          context.get("inpcBpartnerId"));
      boolean isSOTrx = "Y".equals(getDefaultIsSOTrx(requestMap));
      if (isSOTrx && businessPartner.getPaymentMethod() != null) {
        return businessPartner.getPaymentMethod().getId();
      } else if (!isSOTrx && businessPartner.getPOPaymentMethod() != null) {
        return businessPartner.getPOPaymentMethod().getId();
      }
    }
    return null;
  }

  String getDefaultReceivedFrom(Map<String, String> requestMap) throws JSONException {
    return getBusinessPartner(requestMap);
  }

}
