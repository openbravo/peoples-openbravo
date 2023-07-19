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
 * All portions are Copyright (C) 2023 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.authentication.oauth2;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.stream.Collectors;

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
import org.openbravo.authentication.AuthenticationException;
import org.openbravo.authentication.AuthenticationType;
import org.openbravo.authentication.ExternalAuthenticationManager;
import org.openbravo.authentication.LoginStateHandler;
import org.openbravo.base.HttpBaseUtils;
import org.openbravo.client.kernel.RequestContext;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.model.ad.access.User;
import org.openbravo.model.authentication.OAuth2AuthenticationProvider;
import org.openbravo.utils.FormatUtilities;

/**
 * Allows to authenticate with an external authentication provider using OAuth 2.0.
 */
@AuthenticationType("OAUTH2")
public class OAuth2AuthenticationManager extends ExternalAuthenticationManager {
  private static final Logger log = LogManager.getLogger();
  private static final String DEFAULT_REDIRECT_PATH = "/secureApp/LoginHandler.html?loginMethod=OAUTH2";

  @Inject
  private LoginStateHandler authStateHandler;

  @Inject
  private OpenIDTokenDataProvider openIDTokenDataProvider;

  @Override
  public String doAuthenticate(HttpServletRequest request, HttpServletResponse response) {
    if (!isValidAuthorizationResponse(request)) {
      log.error("The authorization response validation was not passed");
      throw new AuthenticationException(buildError());
    }
    return handleAuthorizationResponse(request);
  }

  @Override
  protected void doLogout(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    throw new UnsupportedOperationException("doLogout is not implemented");
  }

  private boolean isValidAuthorizationResponse(HttpServletRequest request) {
    String code = request.getParameter("code");
    String state = request.getParameter("state");
    log.trace("Authorization response parameters: code = {}, state = {}", code, state);
    return code != null && authStateHandler.isValidKey(state);
  }

  private String handleAuthorizationResponse(HttpServletRequest request) {
    try {
      OBContext.setAdminMode(true);
      OAuth2AuthenticationProvider config = authStateHandler
          .getConfiguration(OAuth2AuthenticationProvider.class, request.getParameter("state"));

      HttpRequest accessTokenRequest = buildAccessTokenRequest(request, config);
      HttpResponse<String> tokenResponse = HttpClient.newBuilder()
          .connectTimeout(Duration.ofSeconds(30))
          .build()
          .send(accessTokenRequest, HttpResponse.BodyHandlers.ofString());
      int responseCode = tokenResponse.statusCode();
      if (responseCode >= 200 && responseCode < 300) {
        return getUser(tokenResponse.body(), config);
      }
      log.error("The token request failed with a {} error {}", responseCode, tokenResponse.body());
      throw new AuthenticationException(buildError());
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

  private HttpRequest buildAccessTokenRequest(HttpServletRequest request,
      OAuth2AuthenticationProvider config) throws ServletException {
    //@formatter:off
    Map<String, String> params = Map.of("grant_type", "authorization_code",
                                        "code", request.getParameter("code"),
                                        "redirect_uri", getRedirectURL(request),
                                        "client_id", config.getClientID(),
                                        "client_secret", FormatUtilities.encryptDecrypt(config.getClientSecret(), false));
    //@formatter:on
    String requestBody = params.entrySet()
        .stream()
        .map(e -> URLEncoder.encode(e.getKey(), StandardCharsets.UTF_8) + "="
            + URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8))
        .collect(Collectors.joining("&"));

    log.trace("Access token request parameters: {}", requestBody);

    return HttpRequest.newBuilder()
        .uri(URI.create(config.getAccessTokenURL()))
        .header("Content-Type", "application/x-www-form-urlencoded")
        .timeout(Duration.ofSeconds(30))
        .POST(HttpRequest.BodyPublishers.ofString(requestBody))
        .build();
  }

  /**
   * Retrieves the ID of the authenticated {@link User}. By default this method assumes that the
   * provided response data contains an OpenID token which includes an email which is used to find
   * the authenticated user.
   *
   * @param responseData
   *          The data obtained in the response of the access token request
   * @param configuration
   *          the OAuth 2.0 configuration with information that can be used to verify the token like
   *          the URL to get the public keys required by the algorithm used for encrypting the token
   *          data.
   *
   * @return the ID of the authenticated {@link User}
   *
   * @throws JSONException
   *           If it is not possible to parse the response data as JSON or if the "id_token"
   *           property is not present in the response
   * @throws OAuth2TokenVerificationException
   *           If it is not possible to verify the token or extract the authentication data
   * @throws AuthenticationException
   *           If there is no user linked to the retrieved email
   */
  protected String getUser(String responseData, OAuth2AuthenticationProvider configuration)
      throws JSONException, OAuth2TokenVerificationException {
    JSONObject tokenData = new JSONObject(responseData);
    String idToken = tokenData.getString("id_token");
    Map<String, Object> authData = openIDTokenDataProvider.getData(idToken, configuration);
    String email = (String) authData.get("email");

    if (StringUtils.isBlank(email)) {
      throw new OAuth2TokenVerificationException("The user e-mail was not found");
    }

    //@formatter:off
    String hql = "select u.id as id, u.username as userName" +
                 "  from ADUser u" +
                 " where email = :email";
    //@formatter:on
    Tuple user = OBDal.getInstance()
        .getSession()
        .createQuery(hql, Tuple.class)
        .setParameter("email", email)
        .setMaxResults(1)
        .uniqueResult();

    if (user == null) {
      throw new AuthenticationException(buildError("UNKNOWN_EMAIL_AUTHENTICATION_FAILURE"));
    }

    loginName.set((String) user.get("userName"));

    return (String) user.get("id");
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

  /**
   * Retrieves the standard URL where OAuth 2.0 requests coming from the external provided should be
   * redirected by using the information of the request in the {@link RequestContext}.
   * 
   * @see #getRedirectURL(HttpServletRequest)
   * 
   * @return the standard URL where OAuth 2.0 requests coming from the external provided should be
   *         redirected
   */
  static String getRedirectURL() {
    return getRedirectURL(RequestContext.get().getRequest());
  }

  /**
   * Retrieves the standard URL where OAuth 2.0 requests coming from the external provided should be
   * redirected by using the information in the provided request.
   * 
   * @param request
   *          the HTTP request
   * @return the standard URL where OAuth 2.0 requests coming from the external provided should be
   *         redirected
   */
  private static String getRedirectURL(HttpServletRequest request) {
    return HttpBaseUtils.getLocalAddress(request) + DEFAULT_REDIRECT_PATH;
  }
}
