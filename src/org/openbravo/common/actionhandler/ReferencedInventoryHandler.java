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

package org.openbravo.common.actionhandler;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.ScrollableResults;
import org.openbravo.client.application.process.BaseProcessActionHandler;
import org.openbravo.client.application.process.ResponseActionsBuilder.MessageType;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.materialmgmt.refinventory.ReferencedInventoryProcessor.GridJS;
import org.openbravo.materialmgmt.refinventory.ReferencedInventoryProcessor.StorageDetailJS;
import org.openbravo.materialmgmt.refinventory.ReferencedInventoryUtil;
import org.openbravo.model.materialmgmt.onhandquantity.ReferencedInventory;
import org.openbravo.model.materialmgmt.onhandquantity.StorageDetail;
import org.openbravo.service.db.DbUtility;

/**
 * Handler to centralize common things for Box and Unbox activities
 */
public abstract class ReferencedInventoryHandler extends BaseProcessActionHandler {
  private static final Logger logger = LogManager.getLogger();

  private static final String PARAMS = "_params";
  private static final String PARAM_GRID_STOCK = "stock";
  private static final String PARAM_GRID_REFINVENTORY = "referencedInventory";
  private static final String PARAM_GRID_SELECTION = "_selection";

  protected JSONObject requestJson;
  protected JSONObject paramsJson;
  private JSONArray storageDetailsToProcess;

  @Override
  protected JSONObject doExecute(Map<String, Object> parameters, String content) {
    try {
      this.requestJson = new JSONObject(content);
      this.paramsJson = requestJson.getJSONObject(PARAMS);
      this.storageDetailsToProcess = paramsJson.getJSONObject(PARAM_GRID_STOCK)
          .getJSONArray(PARAM_GRID_SELECTION);

      OBContext.setAdminMode(true);
      validateSelectionOrThrowException();
      appendNestedStorageDetailsFromSelectedRefInventories();
      run();
    } catch (Exception e) {
      try {
        final Throwable ex = DbUtility.getUnderlyingSQLException(e);
        final String message = OBMessageUtils.translateError(ex.getMessage()).getMessage();

        return getResponseBuilder()
            .showMsgInProcessView(MessageType.ERROR, OBMessageUtils.messageBD("Error"),
                StringUtils.isBlank(message) ? ex.toString() : message, true)
            .retryExecution()
            .build();
      } catch (Exception ignore) {
        logger.warn("Exception trying to build error message", ignore);
      }
    } finally {
      OBContext.restorePreviousMode();
    }

    return getResponseBuilder()
        .showMsgInView(MessageType.SUCCESS, OBMessageUtils.messageBD("Success"),
            OBMessageUtils.messageBD("Success"))
        .build();
  }

  /**
   * Hook to validate user selection. Throw an exception in case of errors
   */
  protected void validateSelectionOrThrowException() throws Exception {
  }

  /**
   * Adds the nested storage details within the selected referenced inventories into the storage
   * details to process. The selected storage details will be later on boxed or unboxed
   */
  private void appendNestedStorageDetailsFromSelectedRefInventories() throws JSONException {
    generateNewStorageDetailJS(getSelectedReferencedInventories()).stream()
        .forEach(sd -> storageDetailsToProcess.put(sd.toJSONObject()));
  }

  protected JSONArray getSelectedReferencedInventories() throws JSONException {
    return paramsJson.getJSONObject(PARAM_GRID_REFINVENTORY).getJSONArray(PARAM_GRID_SELECTION);
  }

  /**
   * Returns a collection of StorageDetailJS representation of all the storage details included in
   * the selected referenced inventories. This storage details will be appended with the rest of
   * individual storage details selected by the user (if any), to finally run the box/unbox logic.
   * 
   * The storage detail's quantity is always the total quantity available in the referenced
   * inventory. The new storage bin will vary for box and unbox.
   */
  protected Collection<StorageDetailJS> generateNewStorageDetailJS(final JSONArray selectedRIs)
      throws JSONException {
    final Collection<StorageDetailJS> sdInNestedRIs = new HashSet<>();
    for (int i = 0; i < selectedRIs.length(); i++) {
      try (ScrollableResults sdScroll = ReferencedInventoryUtil
          .getStorageDetails(selectedRIs.getJSONObject(i).getString(GridJS.ID), true)) {
        while (sdScroll.next()) {
          final StorageDetail sd = (StorageDetail) sdScroll.get(0);
          final StorageDetailJS sdJS = new StorageDetailJS(sd.getId(), sd.getQuantityOnHand(),
              getNewStorageBin(selectedRIs.getJSONObject(i)));
          sdInNestedRIs.add(sdJS);
        }
      }
    }
    return sdInNestedRIs;
  }

  /**
   * The new storage bin will vary for box and unbox, so each one should implement the right logic.
   */
  protected abstract String getNewStorageBin(final JSONObject selectedRefInventoryJS)
      throws JSONException;

  /**
   * Executes the box / unbox logic
   */
  protected abstract void run() throws Exception;

  protected ReferencedInventory getReferencedInventory() throws JSONException {
    return OBDal.getInstance()
        .getProxy(ReferencedInventory.class, requestJson.getString("inpmRefinventoryId"));
  }

  protected JSONArray getSelectedStorageDetails() {
    return storageDetailsToProcess;
  }

}
