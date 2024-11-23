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
import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.persistence.Tuple;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.authentication.hashing.PasswordHash;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.mobile.core.process.WebServiceAbstractServlet;
import org.openbravo.model.ad.access.User;
import org.openbravo.service.password.PasswordStrengthChecker;

@SuppressWarnings("serial")
public class ResetPasswordWithTokenService extends WebServiceAbstractServlet {

  private static final Logger log = LogManager.getLogger();

  @Inject
  private PasswordStrengthChecker passwordStrengthChecker;

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    JSONObject result = new JSONObject();
    try {
      OBContext.setAdminMode(true);
      JSONObject body = new JSONObject(
          request.getReader().lines().collect(Collectors.joining(System.lineSeparator())));

      String token = body.optString("token");
      if (!isValidUUID(token)) {
        throw new ChangePasswordException("The UUID provided is not valid!");
      }

      String newPwd = body.optString("newPassword");
      if (!passwordStrengthChecker.isStrongPassword(newPwd)) {
        throw new ChangePasswordException(
            "Please provide a stronger one. Passwords must have at least 8 characters and contain at least three of the following: uppercase letters, lowercase letters, numbers and symbols.");
      }

      String hql_token = "select user.id as userId, redeemed as isRedeemed, creationDate as created from ADUserPasswordResetToken where usertoken = :token";

      Tuple tokenEntry = OBDal.getInstance()
          .getSession()
          .createQuery(hql_token, Tuple.class)
          .setParameter("token", token)
          .uniqueResult();

      if (tokenEntry == null) {
        throw new ChangePasswordException("No entry found with token " + token);
      }

      String userId = (String) tokenEntry.get(0);
      Boolean isRedeemed = (Boolean) tokenEntry.get(1);
      Timestamp creationDate = (Timestamp) tokenEntry.get(2);

      if (!checkExpirationOfToken(creationDate, isRedeemed)) {
        throw new ChangePasswordException("The token has expired");
      }

      // All checks have been successfully passed
      User user = OBDal.getInstance().get(User.class, userId);
      user.setPassword(PasswordHash.generateHash(newPwd));
      OBDal.getInstance().flush();

    } catch (ChangePasswordException ex) {
      log.info("Error while validating the reset password request: " + ex.getMessage());
      try {
        result = new JSONObject(Map.of("error", generateError(ex.getMessage())));
      } catch (JSONException e) {
        // Should not happen
      }
    } catch (JSONException ex) {
      log.error("Error parsing JSON", ex);
      result = new JSONObject(Map.of("error", ex.getMessage()));
    } catch (Exception ex) {
      log.error("Error processing ", ex);
      result = new JSONObject(Map.of("error", ex.getMessage()));
    } finally {
      OBContext.restorePreviousMode();
    }
    writeResult(response, new JSONObject(Map.of("response", result)).toString());
  }

  private boolean checkExpirationOfToken(Timestamp creationDate, boolean isRedeemed) {
    LocalDateTime tokenDate = creationDate.toLocalDateTime();

    return !isRedeemed && Duration.between(tokenDate, LocalDateTime.now()).toSeconds() < 15 * 60;
  }

  private JSONObject generateError(String errorMsg) throws JSONException {
    JSONObject error = new JSONObject();
    JSONObject errorResponse = new JSONObject();
    errorResponse.put("messageTitle", "Error during new password generation");
    errorResponse.put("messageText", errorMsg);
    error.put("response", errorResponse);
    return error;
  }

  /**
   * Security check to validate whether the token is valid or not
   *
   * @param token
   * @return true if the token is valid
   */
  public boolean isValidUUID(String token) {
    String uuidRegex = "^[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}$";
    Pattern pattern = Pattern.compile(uuidRegex);
    return pattern.matcher(token).matches();
  }

}
