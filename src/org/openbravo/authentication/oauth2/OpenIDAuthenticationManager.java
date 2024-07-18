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
 * All portions are Copyright (C) 2023-2024 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.authentication.oauth2;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
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
import org.openbravo.authentication.AuthenticatedUser;
import org.openbravo.authentication.AuthenticationException;
import org.openbravo.authentication.AuthenticationType;
import org.openbravo.authentication.ExternalAuthenticationManager;
import org.openbravo.authentication.LoginStateHandler;
import org.openbravo.base.HttpBaseUtils;
import org.openbravo.base.HttpClientManager;
import org.openbravo.client.kernel.RequestContext;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.model.ad.access.User;
import org.openbravo.model.authentication.OAuth2AuthenticationProvider;
import org.openbravo.utils.FormatUtilities;

/**
 * Allows to authenticate with an external authentication provider using OpenID.
 */
@AuthenticationType("OPENID")
public class OpenIDAuthenticationManager extends ExternalAuthenticationManager {
  private static final Logger log = LogManager.getLogger();
  private static final String DEFAULT_REDIRECT_PATH = "/secureApp/LoginHandler.html?loginMethod=OPENID";

  @Inject
  private LoginStateHandler authStateHandler;

  @Inject
  private OpenIDTokenDataProvider openIDTokenDataProvider;

  @Inject
  private HttpClientManager httpClientManager;

  @Override
  public AuthenticatedUser doExternalAuthentication(HttpServletRequest request,
      HttpServletResponse response) {
    JSONObject requestParams = getRequestParameters(request);

    try {
      String code = requestParams.getString("code");
      String state = requestParams.getString("state");
      Boolean validateState = requestParams.getBoolean("validateState");
      String redirectUri = requestParams.getString("redirectUri");

      if (validateState && !isValidAuthorizationResponse(code, requestParams.getString("state"))) {
        log.error("The authorization response validation was not passed");
        throw new AuthenticationException(buildError());
      } else if (!validateState) {
        log.trace("Authorization response parameters: code = {}", code);
      }

      return handleAuthorizationResponse(code, state, redirectUri);
    } catch (JSONException e) {
      throw new AuthenticationException(buildError());
    }
  }

  @Override
  protected void doLogout(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    throw new UnsupportedOperationException("doLogout is not implemented");
  }

  private boolean isValidAuthorizationResponse(String code, String state) {
    log.trace("Authorization response parameters: code = {}, state = {}", code, state);
    return code != null && authStateHandler.isValidKey(state);
  }

  private AuthenticatedUser handleAuthorizationResponse(String code, String state,
      String redirectUri) {
    try {
      OBContext.setAdminMode(true);
      OAuth2AuthenticationProvider config = authStateHandler
          .getConfiguration(OAuth2AuthenticationProvider.class, state);

      HttpRequest accessTokenRequest = buildAccessTokenRequest(code, redirectUri, config);
      HttpResponse<String> tokenResponse = httpClientManager.send(accessTokenRequest);
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

  /**
   * Depending on the request received the request parameters will be obtained in diferent ways. Up
   * to the moment they can be found in the request body in a property called credentials or in the
   * request as parameters
   * 
   * Ther parameters that will be stored in one of the mentioned ways will be the following.
   * 
   * <ul>
   * <li>code (mandatory): will containe the authorization code returned on redirection by the
   * authorization server
   * <li>state (mandatory): state parameter used during the communication, it will contain the
   * provider configuration id
   * <li>validateState (no mandatory): will inform if it is required to validate the state or it has
   * alredy been done, by default it will be validated
   * <li>redirectUri (mandatory): contain the redirection url
   * </ul>
   * 
   * @param request
   *          - authentication request
   * @return json object containing all the properties found in the request
   */
  private JSONObject getRequestParameters(HttpServletRequest request) {
    try {
      JSONObject params = new JSONObject();

      String body = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));

      Boolean validateState = true;
      JSONObject credentials = !body.isEmpty() ? new JSONObject(body).getJSONObject("credential")
          : null;
      if (credentials != null) {

        if (credentials.has("validateState")) {
          validateState = credentials.getBoolean("validateState");
        }

        params.put("code", credentials.getString("code"));
        params.put("state", authStateHandler.addNewConfiguration(credentials.getString("state")));
        params.put("validateState", credentials.getBoolean("validateState"));
        params.put("redirectUri", credentials.getString("redirectUri"));
      } else {

        if (request.getParameter("validateState") != null) {
          validateState = Boolean.valueOf(request.getParameter("validateState"));
        }

        params.put("code", request.getParameter("code"));
        params.put("state", request.getParameter("state"));
        params.put("validateState", validateState);
        params.put("redirectUri", getRedirectURL(request));
      }
      return params;
    } catch (Exception ex) {
      throw new AuthenticationException(buildError());
    }
  }

  private HttpRequest buildAccessTokenRequest(String code, String redirectUri,
      OAuth2AuthenticationProvider config) throws ServletException {
    //@formatter:off
    Map<String, String> params = Map.of("grant_type", "authorization_code",
                                        "code", code,
                                        "redirect_uri", redirectUri,
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
        .header("Accept", "application/json")
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
  protected AuthenticatedUser getUser(String responseData,
      OAuth2AuthenticationProvider configuration)
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

    return new AuthenticatedUser((String) user.get("id"), (String) user.get("userName"));
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
