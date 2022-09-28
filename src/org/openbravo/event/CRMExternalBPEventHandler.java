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

import java.util.ArrayList;
import java.util.Arrays;

import javax.enterprise.event.Observes;

import org.openbravo.base.exception.OBException;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.model.Property;
import org.openbravo.client.kernel.event.EntityNewEvent;
import org.openbravo.client.kernel.event.EntityPersistenceEvent;
import org.openbravo.client.kernel.event.EntityPersistenceEventObserver;
import org.openbravo.client.kernel.event.EntityUpdateEvent;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.model.externalbpartner.ExternalBusinessPartnerConfigLocation;
import org.openbravo.model.externalbpartner.ExternalBusinessPartnerConfigProperty;

public class CRMExternalBPEventHandler extends EntityPersistenceEventObserver {
  private static Entity[] entities = {
      ModelProvider.getInstance().getEntity(ExternalBusinessPartnerConfigLocation.ENTITY_NAME) };
  final Entity externalBPAddressEntity = ModelProvider.getInstance()
      .getEntity(ExternalBusinessPartnerConfigLocation.ENTITY_NAME);
  final String multIntegrationType = "Multi_integration";

  @Override
  protected Entity[] getObservedEntities() {
    return entities;
  }

  public void onUpdate(@Observes EntityUpdateEvent event) {
    if (!isValidEvent(event)) {
      return;
    }
    checkEmptyCountry(event);
    checkMandatoryProperties(event);
  }

  public void onSave(@Observes EntityNewEvent event) {
    if (!isValidEvent(event)) {
      return;
    }
    checkEmptyCountry(event);
    checkMandatoryProperties(event);
  }

  private void checkEmptyCountry(EntityPersistenceEvent event) {
    final Property externalBPAddressProperty = externalBPAddressEntity
        .getProperty(ExternalBusinessPartnerConfigLocation.PROPERTY_COUNTRY);
    final ExternalBusinessPartnerConfigProperty externalBPAddressCountry = (ExternalBusinessPartnerConfigProperty) event
        .getCurrentState(externalBPAddressProperty);
    if (externalBPAddressCountry == null) {
      throw new OBException(OBMessageUtils.getI18NMessage("ExtBPCountryMandatory", null));
    }
  }

  private void checkIfNonMandatoryProperty(ExternalBusinessPartnerConfigProperty propertyToCheck) {
    if (propertyToCheck != null && !propertyToCheck.isMandatory()) {
      throw new OBException(OBMessageUtils.getI18NMessage("ExtBPAddressPropertyMandatory", null));
    }
  }

  private void checkMandatoryProperties(EntityPersistenceEvent event) {
    ExternalBusinessPartnerConfigLocation extBpPartnerConfigLocation = (ExternalBusinessPartnerConfigLocation) event
        .getTargetInstance();
    if (extBpPartnerConfigLocation.getCRMConnectorConfiguration()
        .getTypeOfIntegration()
        .equals(multIntegrationType)) {
      final ArrayList<ExternalBusinessPartnerConfigProperty> propertiesToCheck = new ArrayList<ExternalBusinessPartnerConfigProperty>(
          Arrays.asList(
              (ExternalBusinessPartnerConfigProperty) event.getCurrentState(externalBPAddressEntity
                  .getProperty(ExternalBusinessPartnerConfigLocation.PROPERTY_ADDRESSLINE1)),
              (ExternalBusinessPartnerConfigProperty) event.getCurrentState(externalBPAddressEntity
                  .getProperty(ExternalBusinessPartnerConfigLocation.PROPERTY_ADDRESSLINE2)),
              (ExternalBusinessPartnerConfigProperty) event.getCurrentState(externalBPAddressEntity
                  .getProperty(ExternalBusinessPartnerConfigLocation.PROPERTY_CITYNAME)),
              (ExternalBusinessPartnerConfigProperty) event.getCurrentState(externalBPAddressEntity
                  .getProperty(ExternalBusinessPartnerConfigLocation.PROPERTY_POSTALCODE)),
              (ExternalBusinessPartnerConfigProperty) event.getCurrentState(externalBPAddressEntity
                  .getProperty(ExternalBusinessPartnerConfigLocation.PROPERTY_REGION))));

      propertiesToCheck.forEach(
          (ExternalBusinessPartnerConfigProperty propertyToCheck) -> checkIfNonMandatoryProperty(
              propertyToCheck));

    }
  }

}
