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
import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;
import javax.persistence.Tuple;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openbravo.authentication.AuthenticatedUser;
import org.openbravo.authentication.AuthenticationException;
import org.openbravo.authentication.AuthenticationType;
import org.openbravo.authentication.ExternalAuthenticationManager;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.model.ad.access.User;
import org.openbravo.model.authentication.AuthenticationProvider;
import org.openbravo.model.authentication.OAuth2TokenAuthenticationProvider;

/**
 * Allows to authenticate with an external authentication provider by receiving an already processed
 * token in the authorization provider. The token will be validated against the keys directory and
 * the user information stored in the token will be returned
 */
@AuthenticationType("OAUTH2TOKEN")
public class ApiOAuth2TokenAuthenticationManager extends ExternalAuthenticationManager {
  private static final Logger log = LogManager.getLogger();

  @Inject
  private JWTTokenDataProvider oauth2TokenDataProvider;

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
    return handleAuthorizationResponse(request);
  }

  private String handleAuthorizationResponse(HttpServletRequest request) {

    try {
      OBContext.setAdminMode(true);
      String authorizationHeader = request.getHeader("Authorization");

      if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
        log.error("The authentication header token has not been received");
        throw new AuthenticationException(buildError("MISSING_AUTHORIZATION_HEADER_TOKEN"));
      }

      AuthenticationProvider authProvider = OBDal.getInstance()
          .createQuery(AuthenticationProvider.class, "where type = :type")
          .setNamedParameter("type", "OAUTH2TOKEN")
          .uniqueResult();

      Optional<OAuth2TokenAuthenticationProvider> oauthTokenConfig = authProvider
          .getOAuth2TokenAuthenticationProviderList()
          .stream()
          .filter(l -> l.isActive())
          .findFirst();

      if (oauthTokenConfig.isEmpty()) {
        log.error("The oauth token configuration has not been defined");
        throw new AuthenticationException(buildError("MISSING_OAUTH_TOKEN_CONFIGURATION"));
      }

      OAuth2TokenAuthenticationProvider config = oauthTokenConfig.get();

      return getUser(authorizationHeader.substring(7), config).getId();
    } catch (OAuth2TokenVerificationException ex) {
      log.error("The token verification failed", ex);
      throw new AuthenticationException(buildError("AUTHENTICATION_DATA_VERIFICATION_FAILURE"));
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  /**
   * Based on the provided token retrieves the ID of the authenticated {@link User}.
   *
   * @param tokenID
   *          Access token received in the request to be authenticated
   * @param configuration
   *          the OAuth 2.0 Token configuration with the information that will be used to verify the
   *          token like the URL to get the public keys required by the algorithm used for
   *          encrypting the token data.
   *
   * @return the {@link AuthenticatedUser} with the information of the authenticated {@link User}
   *
   * @throws OAuth2TokenVerificationException
   *           If it is not possible to verify the token or extract the authentication data
   * @throws AuthenticationException
   *           If there is no user linked to the retrieved user identifier
   */
  protected AuthenticatedUser getUser(String tokenID,
      OAuth2TokenAuthenticationProvider configuration) throws OAuth2TokenVerificationException {

    Map<String, Object> authData = oauth2TokenDataProvider.getData(tokenID,
        configuration.getJwksUrl(), configuration.getTokenProperty());
    String userIdentifierValue = (String) authData.get(configuration.getTokenProperty());

    if (StringUtils.isBlank(userIdentifierValue)) {
      throw new OAuth2TokenVerificationException(
          "The user " + configuration.getTokenProperty() + " was not found");
    }

    //@formatter:off
    String hql = "select u.id as id, u.username as userName" +
                 "  from ADUser u" +
                 " where oauth2TokenValue = :tokenValue";
    //@formatter:on
    Tuple user = OBDal.getInstance()
        .getSession()
        .createQuery(hql, Tuple.class)
        .setParameter("tokenValue", userIdentifierValue)
        .setMaxResults(1)
        .uniqueResult();

    if (user == null) {
      throw new AuthenticationException(buildError("UNKNOWN_TOKEN_VALUE_AUTHENTICATION_FAILURE"));
    }

    return new AuthenticatedUser((String) user.get("id"), (String) user.get("userName"));
  }

  private OBError buildError(String message) {
    OBError errorMsg = new OBError();
    errorMsg.setType("Error");
    errorMsg.setTitle("AUTHENTICATION_FAILURE");
    errorMsg.setMessage(message);
    return errorMsg;
  }

}
