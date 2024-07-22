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
import java.util.Optional;
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
import org.hibernate.criterion.Restrictions;
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

  /**
   * Login authentication will be performed by calling this method
   */
  @Override
  public AuthenticatedUser doExternalAuthentication(HttpServletRequest request,
      HttpServletResponse response) {

    try {
      // Extract body content from the request
      String body = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));

      // Try to extract credential object from the request. In case it is present the parameters
      // will be taken from it, otherwise, it will be taken from the servelet request
      JSONObject credential = !body.isEmpty() ? new JSONObject(body).getJSONObject("credential")
          : null;

      JSONObject requestParams;

      if (credential != null) {
        requestParams = getParametersFromJsonObject(credential);
      } else {
        requestParams = getParametersFromServeletRequest(request);
      }

      Tuple user = doOpenIdAuthentication(requestParams);
      return new AuthenticatedUser((String) user.get("id"), (String) user.get("userName"));

    } catch (IOException | JSONException e) {
      throw new AuthenticationException(buildError());
    }
  }

  @Override
  protected void doLogout(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    throw new UnsupportedOperationException("doLogout is not implemented");
  }

  /**
   * Approvals authentication will be executed by calling this method
   */
  @Override
  public Optional<User> authenticate(String authProvider, JSONObject credential)
      throws JSONException {

    JSONObject requestParams = getParametersFromJsonObject(credential);

    Tuple userData = doOpenIdAuthentication(requestParams);

    User user = (User) OBDal.getInstance()
        .createCriteria(User.class)
        .add(Restrictions.eq(User.PROPERTY_USERNAME, (String) userData.get("userName")))
        .setFilterOnActive(true)
        .setFilterOnReadableClients(false)
        .setFilterOnReadableOrganization(false)
        .uniqueResult();

    return Optional.of(user);
  }

  private Tuple doOpenIdAuthentication(JSONObject requestParams) {
    try {
      if (requestParams.has("id_token")) {
        return handleAuthenticatedRequest(requestParams.toString(),
            requestParams.getString("state"));
      }

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

  private boolean isValidAuthorizationResponse(String code, String state) {
    log.trace("Authorization response parameters: code = {}, state = {}", code, state);
    return code != null && authStateHandler.isValidKey(state);
  }

  private Tuple handleAuthorizationResponse(String code, String state, String redirectUri) {
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
   * Process the received request params to be transformed into a legible object
   * 
   * @param params
   *          object with the parameters received in the request
   * @return processed parameters
   */
  private JSONObject getParametersFromJsonObject(JSONObject params) throws JSONException {
    JSONObject result = new JSONObject();

    if (params.has("tokenId")) {

      result.put("id_token", params.get("tokenId"));
      result.put("state", authStateHandler.addNewConfiguration(params.getString("state")));

    } else {

      result.put("code", params.getString("code"));
      result.put("state", authStateHandler.addNewConfiguration(params.getString("state")));
      result.put("validateState", params.getBoolean("validateState"));
      result.put("redirectUri", params.getString("redirectUri"));
    }

    return result;
  }

  /**
   * Process the received paramters in the servelet request to be transformed into a legible object
   * 
   * @param params
   *          object with the parameters received in the request
   * @return processed parameters
   */
  private JSONObject getParametersFromServeletRequest(HttpServletRequest request)
      throws JSONException {
    JSONObject result = new JSONObject();

    result.put("code", request.getParameter("code"));
    result.put("state", request.getParameter("state"));
    result.put("validateState", Boolean.valueOf(request.getParameter("validateState")));
    result.put("redirectUri", getRedirectURL(request));

    return result;
  }

  /**
   * Based on the received token id value and the configuration id obtained from the state the
   * authenticated user will be obtained
   * 
   * @param tokenID
   *          - json object containing the token id data
   * @param state
   *          - contains the authorization provider configuration id
   * @return the authenticated user data
   */
  private Tuple handleAuthenticatedRequest(String tokenID, String state) {
    try {
      OBContext.setAdminMode(true);
      OAuth2AuthenticationProvider config = authStateHandler
          .getConfiguration(OAuth2AuthenticationProvider.class, state);

      return getUser(tokenID, config);
    } catch (JSONException | OAuth2TokenVerificationException ex) {
      log.error("Error handling the token id obtained in the request", ex);
      throw new AuthenticationException(buildError());
    } finally {
      OBContext.restorePreviousMode();
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
  protected Tuple getUser(String responseData, OAuth2AuthenticationProvider configuration)
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

    return user;
    // return new AuthenticatedUser((String) user.get("id"), (String) user.get("userName"));
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
