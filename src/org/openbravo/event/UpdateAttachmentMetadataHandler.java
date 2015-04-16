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
 * All portions are Copyright (C) 2015 Openbravo SLU
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.event;

import java.util.Map;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.weld.WeldUtils;
import org.openbravo.client.application.window.AttachImplementationManager;
import org.openbravo.client.kernel.BaseActionHandler;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.model.ad.utility.Attachment;
import org.openbravo.model.ad.utility.AttachmentMetadata;
import org.openbravo.model.ad.utility.AttachmentMethod;
import org.openbravo.service.db.DbUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Process in charge of updating Metadata records from attachment method.
 * 
 * When this process is called with the INITIALIZE action, it will provide the data needed to
 * initialize a popup that displays the metadata of attachment method configured.
 * 
 * 
 * 
 * @author daniel.ruiz
 * 
 */
public class UpdateAttachmentMetadataHandler extends BaseActionHandler {
  final static private Logger log = LoggerFactory.getLogger(UpdateAttachmentMetadataHandler.class);

  @Override
  protected JSONObject execute(Map<String, Object> parameters, String content) {
    JSONObject response = new JSONObject();
    try {
      OBContext.setAdminMode();
      final JSONObject request = new JSONObject(content);

      final String action = request.getString("action");
      AttachImplementationManager aim = WeldUtils
          .getInstanceFromStaticBeanManager(AttachImplementationManager.class);
      // Action INITIALIZE: Populates the attachment metadata pop up
      // Action EDIT populates the attachment metadata popup that the values that already have in
      // the attachment
      if ("INITIALIZE".equals(action) || "EDIT".equals(action)) {
        AttachmentMethod attMethod = null;
        Attachment attachment = null;
        if ("INITIALIZE".equals(action)) {
          attMethod = aim.getAttachmenMethod(OBContext.getOBContext().getCurrentClient());
        } else {
          final String attachId = request.getString("attachId");
          attachment = OBDal.getInstance().get(Attachment.class, attachId);
          attMethod = attachment.getAttachmentMethod();
        }
        JSONArray metadataArray = new JSONArray();

        for (AttachmentMetadata am : attMethod.getCAttachmentMetadataList()) {
          JSONObject metadata = new JSONObject();
          metadata.put("Name", am.getName());
          metadata.put("SearchKey", am.getValue());
          // metadata.put("value", "");
          metadataArray.put(metadata);

        }
        response.put("attMetadataList", metadataArray);
        if ("EDIT".equals(action)) { // get MetadataValues
          aim.getMetadataValues(attachment, metadataArray);
        }
        return response;
      }

    } catch (Exception e) {
      OBDal.getInstance().rollbackAndClose();
      log.error("UpdateAttachmentMetadata error: " + e.getMessage(), e);
      Throwable ex = DbUtility.getUnderlyingSQLException(e);
      String message = OBMessageUtils.translateError(ex.getMessage()).getMessage();
      try {
        JSONObject errorMessage = new JSONObject();
        errorMessage.put("severity", "error");
        errorMessage.put("text", message);
        response.put("message", errorMessage);
      } catch (JSONException ignore) {
      }
    } finally {
      OBContext.restorePreviousMode();
    }
    return response;
  }
}