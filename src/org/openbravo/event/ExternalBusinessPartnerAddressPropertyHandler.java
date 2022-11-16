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
 * All portions are Copyright (C) 2022 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.event;

import java.util.List;

import javax.enterprise.event.Observes;

import org.hibernate.criterion.Restrictions;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.client.kernel.event.EntityNewEvent;
import org.openbravo.client.kernel.event.EntityPersistenceEvent;
import org.openbravo.client.kernel.event.EntityPersistenceEventObserver;
import org.openbravo.client.kernel.event.EntityUpdateEvent;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.model.externalbpartner.ExternalBusinessPartnerConfig;
import org.openbravo.model.externalbpartner.ExternalBusinessPartnerConfigLocation;
import org.openbravo.model.externalbpartner.ExternalBusinessPartnerConfigProperty;

public class ExternalBusinessPartnerAddressPropertyHandler extends EntityPersistenceEventObserver {
  private static Entity[] entities = {
      ModelProvider.getInstance().getEntity(ExternalBusinessPartnerConfigProperty.ENTITY_NAME) };
  final Entity externalBPAddressEntity = ModelProvider.getInstance()
      .getEntity(ExternalBusinessPartnerConfigProperty.ENTITY_NAME);
  private static final String MULTI_INTEGRATION_TYPE = "MI";

  @Override
  protected Entity[] getObservedEntities() {
    return entities;
  }

  public void onUpdate(@Observes EntityUpdateEvent event) {
    if (!isValidEvent(event)) {
      return;
    }
    checkMandatoryPropertiesIfMultiIntegration(event);
  }

  public void onSave(@Observes EntityNewEvent event) {
    if (!isValidEvent(event)) {
      return;
    }
    checkMandatoryPropertiesIfMultiIntegration(event);
  }

  private void checkMandatoryPropertiesIfMultiIntegration(EntityPersistenceEvent event) {
    ExternalBusinessPartnerConfigProperty externalBusinessParterProperty = (ExternalBusinessPartnerConfigProperty) event
        .getTargetInstance();
    ExternalBusinessPartnerConfig externalBusinessPartnerConfiguration = externalBusinessParterProperty
        .getExternalBusinessPartnerIntegrationConfiguration();
    if (MULTI_INTEGRATION_TYPE.equals(externalBusinessPartnerConfiguration.getTypeOfIntegration())
        && externalBusinessParterProperty.isAddressProperty()) {

      // Since there is no link to the addressMapping table we will need to do queries and iterate
      final OBCriteria<ExternalBusinessPartnerConfigLocation> criteria = OBDal.getInstance()
          .createCriteria(ExternalBusinessPartnerConfigLocation.class);

      criteria.add(Restrictions.eq(ExternalBusinessPartnerConfigLocation.PROPERTY_ACTIVE, true));
      criteria.add(
          Restrictions.eq(ExternalBusinessPartnerConfigLocation.PROPERTY_CRMCONNECTORCONFIGURATION,
              externalBusinessPartnerConfiguration));
      // In theory, it should not be possible to have more than one record, but just in case we will
      // iterate through all of them
      List<ExternalBusinessPartnerConfigLocation> addressMappingConfigurations = criteria.list();
      for (ExternalBusinessPartnerConfigLocation addressMappingConfiguration : addressMappingConfigurations) {
        if (addressMappingConfiguration.getAddressLine1() != null
            && addressMappingConfiguration.getAddressLine1()
                .getApiKey()
                .equals(addressMappingConfiguration.getAddressLine1().getApiKey())
            && !externalBusinessParterProperty.isMandatory()) {
          throw new OBException(OBMessageUtils.messageBD("UnnasignExtBPAddressPropertyMandatory"));
        }

      }

    }
  }
}
