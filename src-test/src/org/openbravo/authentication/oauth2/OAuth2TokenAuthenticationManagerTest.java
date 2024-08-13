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

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.After;
import org.junit.Test;
import org.mockito.Mockito;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.weld.test.WeldBaseTest;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.ad.access.User;
import org.openbravo.model.authentication.OAuth2TokenAuthenticationProvider;
import org.openbravo.test.base.TestConstants;

/**
 * Tests the authentication management performed to make login with oauth 2.0 with tokens managed by
 * {@link OAuth2TokenAuthenticationManager}
 */
public class OAuth2TokenAuthenticationManagerTest extends WeldBaseTest {
  @After
  public void cleanUp() {
    rollback();
  }

  @Test
  public void externalAuthenticationNotImplemented() {
    OAuth2TokenAuthenticationManager authManager = new OAuth2TokenAuthenticationManager();
    HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
    HttpServletResponse response = Mockito.mock(HttpServletResponse.class);

    assertThrows(UnsupportedOperationException.class, () -> {
      authManager.doExternalAuthentication(request, response);
    });
  }

  @Test
  public void doLogoutNotImplemented() {
    OAuth2TokenAuthenticationManager authManager = new OAuth2TokenAuthenticationManager();
    HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
    HttpServletResponse response = Mockito.mock(HttpServletResponse.class);

    assertThrows(UnsupportedOperationException.class, () -> {
      authManager.doLogout(request, response);
    });
  }

  @Test
  public void missingAuthorizationHeader() {
    OAuth2TokenAuthenticationManager authManager = new OAuth2TokenAuthenticationManager();
    HttpServletRequest request = Mockito.mock(HttpServletRequest.class);

    assertNull(authManager.doWebServiceAuthenticate(request));
  }

  @Test
  public void wrongFormatAuthorizationHeader() {
    OAuth2TokenAuthenticationManager authManager = new OAuth2TokenAuthenticationManager();
    HttpServletRequest request = Mockito.mock(HttpServletRequest.class);

    Mockito.when(request.getParameter("Authorization")).thenReturn("TOKEN_VALUE");

    assertNull(authManager.doWebServiceAuthenticate(request));
  }

  @Test
  public void missingTokenConfiguration() {
    OAuth2TokenAuthenticationManager authManager = new OAuth2TokenAuthenticationManager();
    HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
    OAuth2TokenDataProvider provider = Mockito.mock(OAuth2TokenDataProvider.class);

    Mockito.when(request.getParameter("Authorization")).thenReturn("Bearer TOKEN_VALUE");
    Mockito.when(provider.existsOAuth2TokenConfig()).thenReturn(false);

    assertNull(authManager.doWebServiceAuthenticate(request));
  }

  @Test
  public void userDoesNotMatchWithProvidedProperty() {
    OAuth2TokenAuthenticationManager authManager = new OAuth2TokenAuthenticationManager();
    HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
    OAuth2TokenDataProvider provider = Mockito.mock(OAuth2TokenDataProvider.class);
    JWTDataProvider jwtProv = Mockito.mock(JWTDataProvider.class);

    Map<String, Object> userProperties = new HashMap<>();
    userProperties.put("TOKEN_PROPERTY", "");

    Mockito.when(request.getParameter("Authorization")).thenReturn("Bearer TOKEN_VALUE");
    Mockito.when(provider.existsOAuth2TokenConfig()).thenReturn(true);
    Mockito.when(provider.get()).thenReturn(getProviderConfiguration());
    try {
      Mockito.when(jwtProv.getData("Bearer TOKEN_VALUE", "CERTIFICATE_URL", "TOKEN_PROPERTY"))
          .thenReturn(userProperties);
    } catch (OAuth2TokenVerificationException ex) {
      throw new OBException(ex);
    }

    assertNull(authManager.doWebServiceAuthenticate(request));
  }

  @Test
  public void userWithPropertyNotFound() {
    OAuth2TokenAuthenticationManager authManager = new OAuth2TokenAuthenticationManager();
    HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
    OAuth2TokenDataProvider provider = Mockito.mock(OAuth2TokenDataProvider.class);
    JWTDataProvider jwtProv = Mockito.mock(JWTDataProvider.class);

    Map<String, Object> userProperties = new HashMap<>();
    userProperties.put("TOKEN_PROPERTY", "PROPERTY_VALUE");

    Mockito.when(request.getParameter("Authorization")).thenReturn("Bearer TOKEN_VALUE");
    Mockito.when(provider.existsOAuth2TokenConfig()).thenReturn(true);
    Mockito.when(provider.get()).thenReturn(getProviderConfiguration());
    try {
      Mockito.when(jwtProv.getData("Bearer TOKEN_VALUE", "CERTIFICATE_URL", "TOKEN_PROPERTY"))
          .thenReturn(userProperties);
    } catch (OAuth2TokenVerificationException ex) {
      throw new OBException(ex);
    }

    assertNull(authManager.doWebServiceAuthenticate(request));
  }

  @Test
  public void userWithPropertyFound() {
    OAuth2TokenAuthenticationManager authManager = new OAuth2TokenAuthenticationManager();
    HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
    OAuth2TokenDataProvider provider = Mockito.mock(OAuth2TokenDataProvider.class);
    JWTDataProvider jwtProv = Mockito.mock(JWTDataProvider.class);

    Map<String, Object> userProperties = new HashMap<>();
    userProperties.put("TOKEN_PROPERTY", "PROPERTY_VALUE");

    Mockito.when(request.getParameter("Authorization")).thenReturn("Bearer TOKEN_VALUE");
    Mockito.when(provider.existsOAuth2TokenConfig()).thenReturn(true);
    Mockito.when(provider.get()).thenReturn(getProviderConfiguration());

    try {
      Mockito.when(jwtProv.getData("Bearer TOKEN_VALUE", "CERTIFICATE_URL", "TOKEN_PROPERTY"))
          .thenReturn(userProperties);
    } catch (OAuth2TokenVerificationException ex) {
      throw new OBException(ex);
    }

    try {
      OBContext.setAdminMode(false);
      updateUserWithTokenProperty();
      OBDal.getInstance().flush();
    } finally {
      OBContext.restorePreviousMode();
    }

    assertNull(authManager.doWebServiceAuthenticate(request));
  }

  private OAuth2TokenAuthenticationProvider getProviderConfiguration() {
    OAuth2TokenAuthenticationProvider config = new OAuth2TokenAuthenticationProvider();
    config.setJwksUrl("CERTIFICATE_URL");
    config.setTokenProperty("TOKEN_PROPERTY");

    return config;
  }

  private void updateUserWithTokenProperty() {
    User user = OBDal.getInstance().get(User.class, TestConstants.Users.OPENBRAVO);
    user.setOauth2TokenValue("PROPERTY_VALUE");
    OBDal.getInstance().save(user);
  }
}
