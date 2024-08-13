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

import java.time.Duration;
import java.util.Optional;

import org.openbravo.cache.TimeInvalidatedCache;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.authentication.AuthenticationProvider;
import org.openbravo.model.authentication.OAuth2TokenAuthenticationProvider;

/**
 * Provides the configuration properties defined to make authorization with OAuth 2.0 with tokens
 */
public class OAuth2TokenDataProvider {
  private static final String CONFIG_ID = "OAuth2TokenConfig";

  private TimeInvalidatedCache<String, OAuth2TokenAuthenticationProvider> oauthTokenConfig = TimeInvalidatedCache
      .newBuilder()
      .name("OAuth 2.0 Token Authentication Provider")
      .expireAfterDuration(Duration.ofMinutes(10))
      .build(this::getConfiguration);

  private OAuth2TokenAuthenticationProvider getConfiguration(String configId) {
    try {
      OBContext.setAdminMode(true);
      AuthenticationProvider authProvider = OBDal.getInstance()
          .createQuery(AuthenticationProvider.class,
              "where type='OAUTH2TOKEN' and application.value = 'API' and active = 'Y'")
          .uniqueResult();

      if (authProvider != null) {
        Optional<OAuth2TokenAuthenticationProvider> configResult = authProvider
            .getOAuth2TokenAuthenticationProviderList()
            .stream()
            .filter(l -> l.isActive())
            .findFirst();

        if (configResult.isPresent()) {
          return configResult.get();
        }
      }
      return null;
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  /**
   * Return the configuration properties defined for oauth 2.0 with tokens
   * 
   * @return {@link OAuth2TokenAuthenticationProvider}
   * 
   */
  public OAuth2TokenAuthenticationProvider get() {
    return oauthTokenConfig.get(CONFIG_ID);
  }

  /**
   * Informs if there is a configuration record to make authorization with oauth 2.0 with tokens
   * 
   * @return true if the configuration record is defined
   */
  public boolean existsOAuth2TokenConfig() {
    return oauthTokenConfig.get(CONFIG_ID) != null;
  }

  /**
   * Removes the configuration record saved in cache
   */
  public void invalidateCache() {
    oauthTokenConfig.invalidate(CONFIG_ID);
  }
}
