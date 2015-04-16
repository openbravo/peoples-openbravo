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
 * All portions are Copyright (C) 2011-2015 Openbravo SLU
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.client.application.window;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.weld.WeldUtils;
import org.openbravo.client.kernel.BaseActionHandler;
import org.openbravo.dal.core.DalUtil;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBDao;
import org.openbravo.model.ad.ui.Tab;
import org.openbravo.model.ad.utility.Attachment;
import org.openbravo.model.ad.utility.AttachmentMetadata;
import org.openbravo.model.ad.utility.AttachmentMethod;

public class AttachmentsAH extends BaseActionHandler {

  private static final Logger log = Logger.getLogger(AttachmentsAH.class);

  @Override
  protected JSONObject execute(Map<String, Object> parameters, String content) {
    OBContext.setAdminMode();
    String tabId = "";
    Tab tab = null;
    if (parameters.get("tabId") != null) {
      tabId = parameters.get("tabId").toString();
      tab = OBDal.getInstance().get(Tab.class, tabId);
    }

    String recordIds = "";
    try {
      final JSONObject request = new JSONObject(content);
      String action = "";
      if (!request.isNull("action")) {
        action = request.getString("action");
      }
      AttachImplementationManager aim = WeldUtils
          .getInstanceFromStaticBeanManager(AttachImplementationManager.class);

      if ("INITIALIZE".equals(action) || "INITIALIZE_EDIT".equals(action)) {
        JSONObject response = new JSONObject();
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
          metadataArray.put(metadata);

        }
        response.put("attMetadataList", metadataArray);
        if ("INITIALIZE_EDIT".equals(action)) { // get MetadataValues
          aim.getMetadataValues(attachment, metadataArray);
        }
        return response;
      }

      else if (parameters.get("Command").equals("DELETE")) {

        recordIds = parameters.get("recordIds").toString();
        String attachmentId = (String) parameters.get("attachId");

        String tableId = (String) DalUtil.getId(tab.getTable());

        OBCriteria<Attachment> attachmentFiles = OBDao.getFilteredCriteria(Attachment.class,
            Restrictions.eq("table.id", tableId), Restrictions.in("record", recordIds.split(",")));
        // do not filter by the attachment's organization
        // if the user has access to the record where the file its attached, it has access to all
        // its attachments
        attachmentFiles.setFilterOnReadableOrganization(false);
        if (attachmentId != null) {
          attachmentFiles.add(Restrictions.eq(Attachment.PROPERTY_ID, attachmentId));
        }
        for (Attachment attachment : attachmentFiles.list()) {
          aim.delete(attachment);

        }
        JSONObject obj = getAttachmentJSONObject(tab, recordIds);
        obj.put("buttonId", parameters.get("buttonId"));
        return obj;
      } else if (parameters.get("Command").equals("EDIT")) {
        recordIds = parameters.get("recordId").toString();
        String attachmentId = (String) parameters.get("attachId");
        AttachmentMethod attachMethod = OBDal.getInstance().get(Attachment.class, attachmentId)
            .getAttachmentMethod();
        JSONObject metadataJson = new JSONObject(parameters.get("updatedMetadata").toString());

        Map<String, String> metadata = new HashMap<String, String>();
        for (AttachmentMetadata met : attachMethod.getCAttachmentMetadataList()) {
          metadata.put(met.getValue(), metadataJson.get(met.getValue()).toString());
        }

        aim.update(attachmentId, tabId, metadata);

        JSONObject obj = getAttachmentJSONObject(tab, recordIds);
        obj.put("buttonId", parameters.get("buttonId"));
        return obj;
      } else {
        return new JSONObject();
      }
    } catch (JSONException e) {
      throw new OBException("Error while removing file", e);
    } catch (OBException e) {
      // throw new OBException(e.getMessage(), e);
      OBDal.getInstance().rollbackAndClose();
      log.error(e.getMessage());
      JSONObject obj = getAttachmentJSONObject(tab, recordIds);
      try {
        obj.put("buttonId", parameters.get("buttonId"));
        obj.put("viewId", parameters.get("viewId"));
        obj.put("status", -1);
        obj.put("errorMessage", e.getMessage());
      } catch (Exception ex) {
        // do nothing
      }

      return obj;
      // // throw new OBException("", e);
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  public static JSONObject getAttachmentJSONObject(Tab tab, String recordIds) {
    String tableId = (String) DalUtil.getId(tab.getTable());
    OBCriteria<Attachment> attachmentFiles = OBDao.getFilteredCriteria(Attachment.class,
        Restrictions.eq("table.id", tableId), Restrictions.in("record", recordIds.split(",")));
    attachmentFiles.addOrderBy("creationDate", false);
    List<JSONObject> attachments = new ArrayList<JSONObject>();
    // do not filter by the attachment's organization
    // if the user has access to the record where the file its attached, it has access to all its
    // attachments
    attachmentFiles.setFilterOnReadableOrganization(false);
    for (Attachment attachment : attachmentFiles.list()) {
      JSONObject attachmentobj = new JSONObject();
      try {
        attachmentobj.put("id", attachment.getId());
        attachmentobj.put("name", attachment.getName());
        attachmentobj.put("age", (new Date().getTime() - attachment.getUpdated().getTime()));
        attachmentobj.put("updatedby", attachment.getUpdatedBy().getName());
        attachmentobj.put("description", attachment.getText());
      } catch (Exception e) {
        throw new OBException("Error while reading attachments:", e);
      }
      attachments.add(attachmentobj);
    }
    JSONObject jsonobj = new JSONObject();
    try {
      jsonobj.put("attachments", new JSONArray(attachments));
    } catch (JSONException e) {
      throw new OBException(e);
    }
    return jsonobj;

  }
}