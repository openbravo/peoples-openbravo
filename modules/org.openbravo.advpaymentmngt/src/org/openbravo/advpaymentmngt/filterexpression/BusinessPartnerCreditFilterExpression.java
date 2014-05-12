/*
 ************************************************************************************
 * Copyright (C) 2014 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.advpaymentmngt.filterexpression;

import java.math.BigDecimal;
import java.util.Map;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.advpaymentmngt.dao.AdvPaymentMngtDao;
import org.openbravo.client.application.FilterExpression;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.common.businesspartner.BusinessPartner;
import org.openbravo.model.common.enterprise.Organization;

public class BusinessPartnerCreditFilterExpression implements FilterExpression {

  private static final Logger log = Logger.getLogger(BusinessPartnerCreditFilterExpression.class);
  private AdvPaymentMngtDao dao;

  @Override
  public String getExpression(Map<String, String> requestMap) {
    BigDecimal customerCredit;
    dao = new AdvPaymentMngtDao();
    String strBusinessPartnerId = null;
    String strOrgId = null;
    String strReceipt = null;
    try {
      OBContext.setAdminMode(true);
      JSONObject context = new JSONObject((String) requestMap.get("context"));
      if (context.get("inpcBpartnerId") != null && !context.get("inpcBpartnerId").equals("")
          && !context.get("inpcBpartnerId").equals("null")) {
        strBusinessPartnerId = (String) context.get("inpcBpartnerId");
      }
      if (context.get("inpadOrgId") != null && !context.get("inpadOrgId").equals("")
          && !context.get("inpadOrgId").equals("null")) {
        strOrgId = (String) context.get("inpadOrgId");
      }
      if (context.get("inpissotrx") != null && !context.get("inpissotrx").equals("")
          && !context.get("inpissotrx").equals("null")) {
        strReceipt = (String) context.get("inpissotrx");
      }
      if (strBusinessPartnerId == null || strOrgId == null || strReceipt == null) {
        return null;
      }
      customerCredit = dao.getCustomerCredit(
          OBDal.getInstance().get(BusinessPartner.class, strBusinessPartnerId),
          "Y".equals(strReceipt) ? true : false,
          OBDal.getInstance().get(Organization.class, strOrgId));
      return customerCredit.toString();
    } catch (JSONException e) {
      log.error(
          "Error trying to get Customer Credit for Business Partner on BusinessPartnerCreditFilterExpression class: "
              + e.getMessage(), e);
      return null;
    } finally {
      OBContext.restorePreviousMode();
    }
  }
}
