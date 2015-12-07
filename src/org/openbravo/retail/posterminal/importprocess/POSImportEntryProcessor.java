/*
 ************************************************************************************
 * Copyright (C) 2015 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.retail.posterminal.importprocess;

import java.util.Arrays;
import java.util.List;

import javax.enterprise.event.Observes;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.model.Property;
import org.openbravo.client.kernel.event.EntityNewEvent;
import org.openbravo.client.kernel.event.EntityPersistenceEventObserver;
import org.openbravo.dal.service.OBDal;
import org.openbravo.retail.posterminal.OBPOSApplications;
import org.openbravo.service.importprocess.ImportEntry;
import org.openbravo.service.importprocess.ImportProcessUtils;

/**
 * Sets the pos terminal in the {@link ImportEntry}.
 * 
 * @author mtaal
 */
public class POSImportEntryProcessor extends EntityPersistenceEventObserver {

  private static Entity[] ENTITIES = { ModelProvider.getInstance().getEntity(
      ImportEntry.ENTITY_NAME) };

  private static List<String> POSTTYPEOFDATA = Arrays.asList("Order", "BusinessPartner",
      "BusinessPartnerLocation", "OBPOS_App_Cashup", "FIN_Finacc_Transaction",
      "OBPOS_RejectQuotation", "OBPOS_VoidLayaway");

  @Override
  protected Entity[] getObservedEntities() {
    return ENTITIES;
  }

  public void onSave(@Observes EntityNewEvent event) {
    if (!isValidEvent(event)) {
      return;
    }

    final ImportEntry importEntry = (ImportEntry) event.getTargetInstance();

    try {
      if (POSTTYPEOFDATA.contains(importEntry.getTypeofdata())) {
        JSONObject jsonObject = new JSONObject(importEntry.getJsonInfo());

        // TODO: using 2 different ways of writing posTerminal is just not nice...
        String posTerminalId = ImportProcessUtils.getJSONProperty(jsonObject, "posterminal");
        if (posTerminalId == null) {
          posTerminalId = ImportProcessUtils.getJSONProperty(jsonObject, "posTerminal");
        }
        if (posTerminalId == null) {
          posTerminalId = ImportProcessUtils.getJSONProperty(jsonObject, "pos");
        }
        if (posTerminalId != null) {
          OBPOSApplications posTerminal = OBDal.getInstance().get(OBPOSApplications.class,
              posTerminalId);
          final Entity importEntryEntity = ModelProvider.getInstance().getEntity(
              ImportEntry.ENTITY_NAME);

          final Property posTerminalProperty = importEntryEntity
              .getProperty(ImportEntry.PROPERTY_OBPOSPOSTERMINAL);
          event.setCurrentState(posTerminalProperty, posTerminal);

          final Property orgProperty = importEntryEntity
              .getProperty(ImportEntry.PROPERTY_ORGANIZATION);
          event.setCurrentState(orgProperty, posTerminal.getOrganization());
        }
      }
    } catch (JSONException e) {
      throw new OBException(e);
    }
  }
}
