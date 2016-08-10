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

  private static Entity[] ENTITIES = { ModelProvider.getInstance().getEntity(
      ImportEntry.ENTITY_NAME) };

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

//    try {
      if (POSTTYPEOFDATA.contains(importEntry.getTypeofdata())) {

        throw new OBException("import entry error");
        
//        // TODO: using 2 different ways of writing posTerminal is just not nice...
//        String posTerminalId = ImportProcessUtils.getJSONProperty(jsonObject, "posterminal");
//        if (posTerminalId == null) {
//          posTerminalId = ImportProcessUtils.getJSONProperty(jsonObject, "posTerminal");
//        }
//        if (posTerminalId == null) {
//          posTerminalId = ImportProcessUtils.getJSONProperty(jsonObject, "pos");
//        }
//        if (posTerminalId != null) {
//          OBPOSApplications posTerminal = OBDal.getInstance().getProxy(OBPOSApplications.class,
//              posTerminalId);
//          final Entity importEntryEntity = ModelProvider.getInstance().getEntity(
//              ImportEntry.ENTITY_NAME);
//
//          final Property posTerminalProperty = importEntryEntity
//              .getProperty(ImportEntry.PROPERTY_OBPOSPOSTERMINAL);
//          event.setCurrentState(posTerminalProperty, posTerminal);
//
//          // determine the organization without reading the pos terminal
//          // at the moment only specific structured json is supported
//          Organization organization = null;
//          final JSONObject content = new JSONObject(importEntry.getJsonInfo());
//          if (content.has("organization") && content.get("organization") instanceof String) {
//            organization = OBDal.getInstance().getProxy(Organization.class,
//                content.getString("organization"));
//          } else if (content.has("data") && content.get("data") instanceof JSONArray) {
//            final JSONArray data = content.getJSONArray("data");
//            if (data.length() > 0 && data.get(0) instanceof JSONObject) {
//              final JSONObject json = data.getJSONObject(0);
//              if (json.has("organization") && json.get("organization") instanceof String) {
//                organization = OBDal.getInstance().getProxy(Organization.class,
//                    json.getString("organization"));
//              }
//            }
//          }
//          // not found read it from the posterminal which will get loaded
//          if (organization == null) {
//            organization = posTerminal.getOrganization();
//          }
//          final Property orgProperty = importEntryEntity
//              .getProperty(ImportEntry.PROPERTY_ORGANIZATION);
//          event.setCurrentState(orgProperty, organization);
//        }
      }
//    } catch (JSONException e) {
//      throw new OBException(e);
//    }
  }
}
