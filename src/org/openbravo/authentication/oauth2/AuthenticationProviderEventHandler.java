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

import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.hibernate.criterion.Restrictions;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.client.kernel.event.EntityDeleteEvent;
import org.openbravo.client.kernel.event.EntityNewEvent;
import org.openbravo.client.kernel.event.EntityPersistenceEventObserver;
import org.openbravo.client.kernel.event.EntityUpdateEvent;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.model.authentication.AuthenticationProvider;

/**
 * Used to invalidate the cache of configurations kept by {@link OpenIDSignInProvider} when changes
 * regarding an authentication provider configuration are detected. Note that in case of working in
 * a clustered environment, this mechanism will only invalidate the cache in the node were the
 * changes occurred. For the rest of the nodes in the cluster it will be necessary to wait for the
 * expiration of the cache entry.
 *
 * It also checks that the authentication providers of type OpenID are correctly configured.
 *
 * @see OpenIDSignInProvider#invalidateCache()
 */
class AuthenticationProviderEventHandler extends EntityPersistenceEventObserver {
  private static final Entity[] ENTITIES = {
      ModelProvider.getInstance().getEntity(AuthenticationProvider.ENTITY_NAME) };

  @Inject
  private OAuth2SignInProvider oauth2SignInProvider;

  @Override
  protected Entity[] getObservedEntities() {
    return ENTITIES;
  }

  public void onDelete(@Observes EntityDeleteEvent event) {
    if (!isValidEvent(event)) {
      return;
    }
    invalidateOAuth2ConfigurationCache((AuthenticationProvider) event.getTargetInstance());
  }

  public void onSave(@Observes EntityNewEvent event) {
    if (!isValidEvent(event)) {
      return;
    }
    AuthenticationProvider authProvider = (AuthenticationProvider) event.getTargetInstance();
    checkSupportedAppAndFlow(authProvider);
    invalidateOAuth2ConfigurationCache(authProvider);
    validateUniquenessApiConfiguration(authProvider);
  }

  public void onUpdate(@Observes EntityUpdateEvent event) {
    if (!isValidEvent(event)) {
      return;
    }
    AuthenticationProvider authProvider = (AuthenticationProvider) event.getTargetInstance();
    checkSupportedAppAndFlow(authProvider);
    invalidateOAuth2ConfigurationCache(authProvider);
    validateUniquenessApiConfiguration(authProvider);
  }

  private void invalidateOAuth2ConfigurationCache(AuthenticationProvider authProvider) {
    oauth2SignInProvider.invalidateCache(authProvider.getType());
  }

  private void checkSupportedAppAndFlow(AuthenticationProvider authProvider) {
    if ("OPENID".equals(authProvider.getType())
        && (OAuth2SignInProvider.BACKOFFICE_APP.equals(authProvider.getApplication().getId())
            && !"LOGIN".equals(authProvider.getFlow()))) {
      throw new OBException(OBMessageUtils.messageBD("AuthProviderUnsupportedAppFlow"));
    }

    String appSearchKey = (String) authProvider.getApplication().get("value");

    if (("OAUTH2TOKEN".equals(authProvider.getType())
        && (!"API".equals(appSearchKey) || !"LOGIN".equals(authProvider.getFlow())))
        || (!"OAUTH2TOKEN".equals(authProvider.getType()) && "API".equals(appSearchKey))) {
      throw new OBException(OBMessageUtils.messageBD("AuthProviderUnsupportedAppFlow"));
    }
  }

  private void validateUniquenessApiConfiguration(AuthenticationProvider authProvider) {
    OBCriteria<AuthenticationProvider> criteria = OBDal.getInstance()
        .createCriteria(AuthenticationProvider.class);
    criteria.add(Restrictions.eq(AuthenticationProvider.PROPERTY_APPLICATION,
        authProvider.getApplication()));
    criteria.add(Restrictions
        .not(Restrictions.eq(AuthenticationProvider.PROPERTY_ID, authProvider.getId())));

    criteria.setMaxResults(1);
    if (criteria.uniqueResult() != null) {
      throw new OBException(OBMessageUtils.messageBD("AuthProviderApiAppNotUniqueConf"));
    }
  }
}
