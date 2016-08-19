/*
 ************************************************************************************
 * Copyright (C) 2015-2016 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.retail.posterminal.importprocess;

import java.util.Arrays;
import java.util.List;

import javax.enterprise.event.Observes;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.model.Property;
import org.openbravo.client.kernel.event.EntityNewEvent;
import org.openbravo.client.kernel.event.EntityPersistenceEventObserver;
import org.openbravo.dal.service.OBDal;
import org.openbravo.mobile.core.servercontroller.SynchronizedServerProcessCaller;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.retail.posterminal.OBPOSApplications;
import org.openbravo.service.importprocess.ImportEntry;
import org.openbravo.service.importprocess.ImportProcessUtils;

/**
 * Sets the pos terminal in the {@link ImportEntry}.
 * 
 * @author mtaal
 */
public class POSImportEntryProcessor extends EntityPersistenceEventObserver {

  private static final Logger log = Logger.getLogger(POSImportEntryProcessor.class);

  private static final Entity IMPORT_ENTRY_ENTITY = ModelProvider.getInstance().getEntity(
      ImportEntry.ENTITY_NAME);

  private static final Entity[] ENTITIES = { IMPORT_ENTRY_ENTITY };
  private static final Property STATUSPROPERTY = IMPORT_ENTRY_ENTITY
      .getProperty(ImportEntry.PROPERTY_IMPORTSTATUS);
  private static final Property ERRORINFO_PROPERTY = IMPORT_ENTRY_ENTITY
      .getProperty(ImportEntry.PROPERTY_ERRORINFO);
  private static final Property POSTERMINAL_PROPERTY = IMPORT_ENTRY_ENTITY
      .getProperty(ImportEntry.PROPERTY_OBPOSPOSTERMINAL);
  private static final Property ORGANIZATION_PROPERTY = IMPORT_ENTRY_ENTITY
      .getProperty(ImportEntry.PROPERTY_ORGANIZATION);

  private static List<String> POSTTYPEOFDATA = Arrays.asList("Order", "BusinessPartner",
      "BusinessPartnerLocation", "OBPOS_App_Cashup", "FIN_Finacc_Transaction",
      "OBPOS_RejectQuotation", "OBPOS_VoidLayaway",
      SynchronizedServerProcessCaller.SYNCHRONIZED_DATA_TYPE);

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
      if (!POSTTYPEOFDATA.contains(importEntry.getTypeofdata())) {
        return;
      }
      JSONObject jsonObject = new JSONObject(importEntry.getJsonInfo());

      // TODO: using 2 different ways of writing posTerminal is just not nice...
      String posTerminalId = ImportProcessUtils.getJSONProperty(jsonObject, "posterminal");
      if (posTerminalId == null) {
        posTerminalId = ImportProcessUtils.getJSONProperty(jsonObject, "posTerminal");
      }
      if (posTerminalId == null) {
        posTerminalId = ImportProcessUtils.getJSONProperty(jsonObject, "pos");
      }
      // handle special case that webpos can send "null" in case of coding error in webpos
      // handle it here
      if (posTerminalId == null || "null".equals(posTerminalId)) {
        log.warn("No posterminal can be determined from json " + jsonObject);
        event.setCurrentState(STATUSPROPERTY, "Error");
        event.setCurrentState(ERRORINFO_PROPERTY, "No posterminal can be determined from the json");
        return;
      }

      // as we are loading proxy the posTerminal object will always be not null
      // even in case of an invalid id, therefore the check on "null" above
      final OBPOSApplications posTerminal = OBDal.getInstance().getProxy(OBPOSApplications.class,
          posTerminalId);
      event.setCurrentState(POSTERMINAL_PROPERTY, posTerminal);

      // determine the organization without reading the pos terminal
      // at the moment only specific structured json is supported
      Organization organization = null;
      final JSONObject content = new JSONObject(importEntry.getJsonInfo());
      if (content.has("organization") && content.get("organization") instanceof String) {
        organization = OBDal.getInstance().getProxy(Organization.class,
            content.getString("organization"));
      } else if (content.has("data") && content.get("data") instanceof JSONArray) {
        final JSONArray data = content.getJSONArray("data");
        if (data.length() > 0 && data.get(0) instanceof JSONObject) {
          final JSONObject json = data.getJSONObject(0);
          if (json.has("organization") && json.get("organization") instanceof String) {
            organization = OBDal.getInstance().getProxy(Organization.class,
                json.getString("organization"));
          }
        }
      }
      // not found read it from the posterminal which will get loaded
      if (organization == null) {
        log.warn("Not possible to determine organization from json, reading the org from posterminal "
            + content);
        organization = posTerminal.getOrganization();
      }
      event.setCurrentState(ORGANIZATION_PROPERTY, organization);
    } catch (JSONException e) {
      throw new OBException("Exception occurred " + importEntry.getJsonInfo(), e);
    }
  }
}
