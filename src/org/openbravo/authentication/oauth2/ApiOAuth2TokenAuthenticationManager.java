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
 * All portions are Copyright (C) 2024 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.authentication.oauth2;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Map;

import javax.inject.Inject;
import javax.persistence.Tuple;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.authentication.AuthenticatedUser;
import org.openbravo.authentication.AuthenticationException;
import org.openbravo.authentication.AuthenticationType;
import org.openbravo.authentication.ExternalAuthenticationManager;
import org.openbravo.authentication.LoginStateHandler;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.model.ad.access.User;
import org.openbravo.model.authentication.ApiOAuth2TokenAuthMgr;

/**
 * Allows to authenticate with an external authentication provider using OAuth2.
 */
@AuthenticationType("OAUTH2TOKEN")
public class ApiOAuth2TokenAuthenticationManager extends ExternalAuthenticationManager {
  private static final Logger log = LogManager.getLogger();

  @Inject
  private LoginStateHandler authStateHandler;

  @Inject
  private JWTDataProvider jwtDataProvider;

  @Override
  public AuthenticatedUser doExternalAuthentication(HttpServletRequest request,
      HttpServletResponse response) {
    throw new UnsupportedOperationException("doLogout is not implemented");
  }

  @Override
  protected void doLogout(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    throw new UnsupportedOperationException("doLogout is not implemented");
  }

  @Override
  public String doWebServiceAuthenticate(HttpServletRequest request) {
    // if (!isValidAuthorizationResponse(request)) {
    // log.error("The authorization response validation was not passed");
    // throw new AuthenticationException(buildError());
    // }
    return handleAuthorizationResponse(request);
  }

  private String handleAuthorizationResponse(HttpServletRequest request) {
    try {
      OBContext.setAdminMode(true);
      String authHeader = request.getHeader("Authorization");
      String token = null;
      if (authHeader != null && authHeader.startsWith("Bearer ")) {
        token = authHeader.substring("Bearer ".length());
      }

      ApiOAuth2TokenAuthMgr config = authStateHandler
          .getConfiguration(
              ApiOAuth2TokenAuthMgr.class,
              request.getParameter("state"));

      return getUser(token, config).getId();
    } catch (AuthenticationException ex) {
      throw ex;
    } catch (OAuth2TokenVerificationException ex) {
      log.error("The token verification failed", ex);
      throw new AuthenticationException(buildError("AUTHENTICATION_DATA_VERIFICATION_FAILURE"));
    } catch (Exception ex) {
      log.error("Error handling the authorization response", ex);
      throw new AuthenticationException(buildError());
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  /**
   * Retrieves the ID of the authenticated {@link User}. By default this method assumes that the
   * provided response data contains an OAuth2 Token which includes a key which is used to find the
   * authenticated user.
   *
   * @param requestData
   *          The data obtained in the response of the access token request
   * @param configuration
   *          the OAuth 2.0 configuration with information that can be used to verify the token like
   *          the URL to get the public keys required by the algorithm used for encrypting the token
   *          data.
   *
   * @return the {@link AuthenticatedUser} with the information of the authenticated {@link User}
   *
   * @throws JSONException
   *           If it is not possible to parse the response data as JSON or if the "id_token"
   *           property is not present in the response
   * @throws OAuth2TokenVerificationException
   *           If it is not possible to verify the token or extract the authentication data
   * @throws AuthenticationException
   *           If there is no user linked to the retrieved email
   */
  protected AuthenticatedUser getUser(String requestData,
      ApiOAuth2TokenAuthMgr configuration)
      throws JSONException, OAuth2TokenVerificationException {
    JSONObject tokenData = new JSONObject(requestData);
    String idToken = tokenData.getString("id_token");
    Map<String, Object> authData = jwtDataProvider.getData(idToken, configuration);
    String tokenValue = (String) authData.get(configuration.getTokenProperty());

    if (StringUtils.isBlank(tokenValue)) {
      throw new OAuth2TokenVerificationException("The user with the specific token value "
          + configuration.getTokenProperty() + " was not found");
    }

    //@formatter:off
    String hql = "select u.id as id, u.username as userName" +
                 "  from ADUser u" +
                 " where oauth2TokenValue\n"
                 + " = :tokenValue";
    //@formatter:on
    Tuple user = OBDal.getInstance()
        .getSession()
        .createQuery(hql, Tuple.class)
        .setParameter("tokenValue", tokenValue)
        .setMaxResults(1)
        .uniqueResult();

    if (user == null) {
      throw new AuthenticationException(buildError("UNKNOWN_TOKEN_VALUE_AUTHENTICATION_FAILURE"));
    }

    return new AuthenticatedUser((String) user.get("id"), (String) user.get("userName"));
  }

  private boolean isValidAuthorizationResponse(HttpServletRequest request) {
    String code = request.getParameter("code");
    String state = request.getParameter("state");
    Enumeration<String> params = request.getParameterNames();
    while (params.hasMoreElements()) {
      System.out.println("Param: " + params.nextElement());
    }
    log.trace("Authorization response parameters: code = {}, state = {}", code, state);
    return code != null && authStateHandler.isValidKey(state);
  }

  private OBError buildError() {
    return buildError("EXTERNAL_AUTHENTICATION_FAILURE");
  }

  private OBError buildError(String message) {
    OBError errorMsg = new OBError();
    errorMsg.setType("Error");
    errorMsg.setTitle("AUTHENTICATION_FAILURE");
    errorMsg.setMessage(message);
    return errorMsg;
  }

}
