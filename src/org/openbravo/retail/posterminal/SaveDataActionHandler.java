/*
 ************************************************************************************
 * Copyright (C) 2012-2022 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.retail.posterminal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.client.kernel.BaseActionHandler;
import org.openbravo.client.kernel.RequestContext;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.SequenceIdData;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.mobile.core.process.DataSynchronizationProcess;
import org.openbravo.service.db.DalConnectionProvider;
import org.openbravo.service.importprocess.ImportEntry;
import org.openbravo.service.importprocess.ImportEntryManager;
import org.openbravo.service.importprocess.ImportEntryManager.ImportEntryProcessorSelector;
import org.openbravo.service.importprocess.ImportEntryPostProcessor;
import org.openbravo.service.json.JsonConstants;

@ApplicationScoped
public class SaveDataActionHandler extends BaseActionHandler {
  private static final Logger log = LogManager.getLogger();

  @Inject
  @Any
  private Instance<POSDataSynchronizationProcess> syncProcesses;

  @Inject
  private ImportEntryManager importEntryManager;

  @Inject
  @Any
  private Instance<ImportEntryPostProcessor> importEntryPostProcessors;

  @Override
  public JSONObject execute(Map<String, Object> parameters, String content) {
    JSONArray errorIds = null;
    List<OBPOSErrors> errors = new ArrayList<OBPOSErrors>();
    boolean errorb = false;
    try {
      errorIds = new JSONArray(content);
      for (int i = 0; i < errorIds.length(); i++) {
        String errorId = errorIds.getString(i);
        OBPOSErrors error = OBDal.getInstance().get(OBPOSErrors.class, errorId);
        errors.add(error);
      }

      Collections.sort(errors, new ErrorComparator());

      for (OBPOSErrors error : errors) {
        String errorId = error.getId();
        String type = error.getTypeofdata();

        POSDataSynchronizationProcess syncProcess = null;
        syncProcess = syncProcesses.select(new DataSynchronizationProcess.Selector(type)).get();

        JSONObject record = new JSONObject(error.getJsoninfo());
        record.put("posErrorId", errorId);
        JSONArray jsonArray = new JSONArray();
        jsonArray.put(record);
        JSONObject data = new JSONObject();
        data.put("data", jsonArray);

        String entryId = SequenceIdData.getUUID();
        importEntryManager.createImportEntry(entryId, syncProcess.getImportEntryQualifier(),
            data.toString(), false);
        importEntryManager.setImportEntryProcessed(entryId);
        ImportEntry entry = OBDal.getInstance().get(ImportEntry.class, entryId);

        JSONObject result = syncProcess.exec(data, true);

        if (result != null && result.get(JsonConstants.RESPONSE_STATUS)
            .equals(JsonConstants.RPCREQUEST_STATUS_FAILURE)) {
          errorb = true;
          if (data.has("data") && result.has("errorids")) {
            JSONObject jsonData = new JSONObject();
            jsonData.put("data", new JSONArray());
            entry.setJsonInfo(jsonData.toString());
          }
        }

        // Execute post process hooks.
        for (ImportEntryPostProcessor importEntryPostProcessor : importEntryPostProcessors
            .select(new ImportEntryProcessorSelector(type))) {
          importEntryPostProcessor.afterProcessing(entry);
        }

        updateErrorStatus(Arrays.asList(error), !errorb);
      }

      if (errorb) {
        JSONObject result = new JSONObject();
        try {
          result.put("message",
              Utility.messageBD(new DalConnectionProvider(false), "OBPOS_ErrorWhileSaving",
                  RequestContext.get().getVariablesSecureApp().getLanguage()));
        } catch (JSONException e) {
          // won't happen
        }
        return result;
      } else {
        JSONObject result = new JSONObject();
        try {
          result.put("message",
              Utility.messageBD(new DalConnectionProvider(false), "OBPOS_OrderSavedSuccessfully",
                  RequestContext.get().getVariablesSecureApp().getLanguage()));
        } catch (JSONException e) {
          // won't happen
        }
        return result;
      }
    } catch (Exception e) {// won't' happen
      log.error("Error while processing the record", e);
      // Rollback
      OBDal.getInstance().rollbackAndClose();

      // Update OBPOSErrors Processing status
      updateErrorStatus(errors, false);

      JSONObject result = new JSONObject();
      try {
        result.put("message", Utility.messageBD(new DalConnectionProvider(false),
            "OBPOS_ErrorWhileSaving", RequestContext.get().getVariablesSecureApp().getLanguage()));
      } catch (JSONException je) {
        // won't happen
      }
      return result;
    }
  }

  private void updateErrorStatus(final List<OBPOSErrors> errors, final boolean orderStatus) {
    OBContext.setAdminMode(true);
    try {
      for (OBPOSErrors error : errors) {
        OBPOSErrors obposErrors = OBDal.getInstance().get(OBPOSErrors.class, error.getId());
        if (orderStatus) {
          obposErrors.setOrderstatus("Y");
        }
        obposErrors.setProcessNow(false);
        OBDal.getInstance().save(obposErrors);
      }
      OBDal.getInstance().flush();
      OBDal.getInstance().commitAndClose();
    } finally {
      OBContext.restorePreviousMode();
    }
  }
}
