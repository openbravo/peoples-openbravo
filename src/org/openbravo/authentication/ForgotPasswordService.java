/*
 ************************************************************************************
 * Copyright (C) 2024 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at https://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.authentication;

import java.io.IOException;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.criterion.Restrictions;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.email.EmailUtils;
import org.openbravo.mobile.core.process.WebServiceAbstractServlet;
import org.openbravo.model.ad.access.User;
import org.openbravo.model.ad.system.Client;
import org.openbravo.model.common.enterprise.EmailServerConfiguration;
import org.openbravo.model.common.enterprise.Organization;

/**
 * Loads into a core2 application the authentication provider configurations that are available for
 * that application
 */
@SuppressWarnings("serial")
public class ForgotPasswordService extends WebServiceAbstractServlet {
  private static final Logger log = LogManager.getLogger();

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    JSONObject result = new JSONObject();
    try {
      OBContext.setAdminMode(true);
      JSONObject body = new JSONObject(
          request.getReader().lines().collect(Collectors.joining(System.lineSeparator())));

      String strOrgId = body.getString("org");
      Organization org = OBDal.getInstance().get(Organization.class, strOrgId);
      String strClientId = body.getString("client");
      Client client = OBDal.getInstance().get(Client.class, strClientId);

      Boolean resetPasswordEnabled = client.getClientInformationList()
          .get(0)
          .isEnableResetPasswordByEmail();

      result.put("resetPasswordEnabled", resetPasswordEnabled);

      EmailServerConfiguration emailConfig = EmailUtils.getEmailConfiguration(org, client);
      if (emailConfig == null) {
        emailConfig = EmailUtils.getEmailConfiguration(org);
        if (emailConfig == null) {
          result.put("hasEmailConfiguration", false);
        }
      }
      if (emailConfig != null) {
        result.put("hasEmailConfiguration", true);
        String userOrEmail = body.getString("userOrEmail");
        User user = (User) OBDal.getInstance()
            .createCriteria(User.class)
            .add(Restrictions.or(Restrictions.eq(User.PROPERTY_USERNAME, userOrEmail),
                Restrictions.eq(User.PROPERTY_EMAIL, userOrEmail)))
            .setFilterOnActive(true)
            .setFilterOnReadableClients(false)
            .setFilterOnReadableOrganization(false)
            .uniqueResult();
        if (user != null) {
          String email = user.getEmail();
        }
      }
    } catch (JSONException ex) {
      log.error("Error with forgot password service", ex);
      result = new JSONObject(Map.of("error", ex.getMessage()));
    } finally {
      OBContext.restorePreviousMode();
    }
    writeResult(response, new JSONObject(Map.of("response", result)).toString());
  }

}
