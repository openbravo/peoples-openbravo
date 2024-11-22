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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.email.EmailEventManager;
import org.openbravo.email.EmailUtils;
import org.openbravo.mobile.core.process.WebServiceAbstractServlet;
import org.openbravo.model.ad.access.User;
import org.openbravo.model.ad.access.UserPasswordResetToken;
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
  public static final String EVT_FORGOT_PASSWORD = "forgotPassword";

  @Inject
  private EmailEventManager emailManager;

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    JSONObject result = new JSONObject();
    try {
      OBContext.setAdminMode(true);
      JSONObject body = new JSONObject(
          request.getReader().lines().collect(Collectors.joining(System.lineSeparator())));

      String strAppName = body.optString("appName");
      if (strAppName.isEmpty()) {
        throw new Exception("The request has not been done in the scope of POS2");
      }

      String strOrgId = body.optString("organization");
      if (strOrgId.isEmpty()) {
        throw new Exception("The request has not an organization defined");
      }

      String strClientId = body.optString("client");

      Organization org = OBDal.getInstance().get(Organization.class, strOrgId);
      Client client = OBDal.getInstance().get(Client.class, strClientId);

      EmailServerConfiguration emailConfig = getEmailConfiguration(result, org, client);

      if (emailConfig != null) {
        result.put("hasEmailConfiguration", true);

        Runnable r = () -> {
          try {
            OBContext.setAdminMode(true);
            String userOrEmail = body.getString("userOrEmail");
            List<User> users = OBDal.getInstance()
                .createCriteria(User.class)
                .add(Restrictions.or(Restrictions.eq(User.PROPERTY_USERNAME, userOrEmail),
                    Restrictions.eq(User.PROPERTY_EMAIL, userOrEmail)))
                .setFilterOnActive(true)
                .setFilterOnReadableClients(false)
                .setFilterOnReadableOrganization(false)
                .list();

            if (users == null || users.size() == 0) {
              return;
            }
            if (users.size() > 1) {
              log.info("Forgot password service: More than one user has the same email configured");
              return;
            }

            boolean userCompliesWithRules = checkUser(users.get(0));
            if (!userCompliesWithRules) {
              throw new Exception("The selected user does not comply with the expected rules");
            }

            User user = users.get(0);
            String token = generateAndPersistToken(user, client, org);

            Map<String, Object> emailData = new HashMap<String, Object>();
            emailData.put("user", user);
            emailData.put("changePasswordURL", generateChangePasswordURL(token));

            emailManager.sendEmail(EVT_FORGOT_PASSWORD, user.getEmail(), emailData, emailConfig);
          } catch (JSONException ex) {
            log.error("Error parsing JSON", ex);
          } catch (Exception ex) {
            log.error("Error with forgot password service", ex);
          } finally {
            OBContext.restorePreviousMode();
          }
        };
        new Thread(r).start();
      }
    } catch (JSONException ex) {
      log.error("Error parsing JSON", ex);
    } catch (Exception ex) {
      log.error("Error with forgot password service", ex);
    } finally {
      OBContext.restorePreviousMode();
      writeResult(response, new JSONObject(Map.of("response", result)).toString());
    }
  }

  private EmailServerConfiguration getEmailConfiguration(JSONObject result, Organization org,
      Client client) throws JSONException {
    EmailServerConfiguration emailConfig = EmailUtils.getEmailConfiguration(org, client);
    if (emailConfig == null) {
      emailConfig = EmailUtils.getEmailConfiguration(org);
      if (emailConfig == null) {
        result.put("hasEmailConfiguration", false);
      }
    }
    return emailConfig;
  }

  private String generateAndPersistToken(User user, Client client, Organization org) {
    String token = UUID.randomUUID().toString();
    UserPasswordResetToken resetToken = OBProvider.getInstance().get(UserPasswordResetToken.class);
    resetToken.setClient(client);
    resetToken.setOrganization(org);
    resetToken.setUsertoken(token);
    resetToken.setUser(user);

    OBDal.getInstance().save(resetToken);
    return token;
  }

  private String generateChangePasswordURL(String token) {
    return String.format("http://localhost:3000/?token=%s", token); // TODO: Set custom url
  }

  private boolean checkUser(User user) {
    return user.isActive() && !user.getEmail().isEmpty() && !user.isLocked() && !user.isSsoonly();
  }

}
