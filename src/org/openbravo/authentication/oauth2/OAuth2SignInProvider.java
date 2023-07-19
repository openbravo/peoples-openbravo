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

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.hibernate.Hibernate;
import org.hibernate.criterion.Restrictions;
import org.openbravo.cache.TimeInvalidatedCache;
import org.openbravo.client.kernel.Template;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.security.SignInProvider;
import org.openbravo.model.authentication.LoginProvider;

/**
 * Provides to the login page the sign in buttons for each of the authentication provider
 * configurations defined to use OAuth 2.0.
 */
@ApplicationScoped
public class OAuth2SignInProvider implements SignInProvider {
  private static final String TEMPLATE_ID = "64F02C64E2A14E09BCD145D74F2DE93F";
  private static final String OAUTH2 = "OAUTH2";
  private Template template;

  @Inject
  private OAuth2LoginButtonGenerator buttonGenerator;

  private TimeInvalidatedCache<String, List<OAuth2Config>> configs = TimeInvalidatedCache
      .newBuilder()
      .name("OAuth 2.0 Configurations")
      .expireAfterDuration(Duration.ofMinutes(10))
      .build(this::getLoginProviderConfigs);

  @Override
  public String getLoginPageSignInHTMLCode() {
    return buttonGenerator.generate();
  }

  /**
   * @return the template used to build the sign in buttons for each of the OAuth 2.0 authentication
   *         provider configurations
   */
  Template getTemplate() {
    if (template == null) {
      template = OBDal.getInstance().get(Template.class, TEMPLATE_ID);
      Hibernate.initialize(template.getModule());
    }
    return template;
  }

  /**
   * @return the active OAuth 2.0 authentication provider configurations
   */
  List<OAuth2Config> getOAuth2LoginProviderConfigs() {
    return configs.get(OAUTH2);
  }

  private List<OAuth2Config> getLoginProviderConfigs(String type) {
    OBContext.setAdminMode(false);
    try {
      return OBDal.getInstance()
          .createCriteria(LoginProvider.class)
          .add(Restrictions.eq(LoginProvider.PROPERTY_TYPE, type))
          .addOrderBy(LoginProvider.PROPERTY_SEQUENCENUMBER, true)
          .list()
          .stream()
          .map(lp -> lp.getOAuth2LoginProviderList().stream().filter(l -> l.isActive()).findFirst())
          .filter(c -> c.isPresent())
          .map(Optional::get)
          .map(OAuth2Config::new)
          .collect(Collectors.toList());
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  /**
   * Invalidates the cache of OAuth 2.0 authentication provider configurations
   */
  void invalidateCache() {
    configs.invalidate(OAUTH2);
  }
}
