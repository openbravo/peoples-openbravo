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

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.exception.OBException;
import org.openbravo.client.application.Parameter;
import org.openbravo.client.application.ParameterUtils;
import org.openbravo.client.kernel.BaseActionHandler;
import org.openbravo.dal.core.DalUtil;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBDao;
import org.openbravo.model.ad.ui.Tab;
import org.openbravo.model.ad.utility.Attachment;
import org.openbravo.model.ad.utility.AttachmentMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AttachmentsAH extends BaseActionHandler {

  private static final Logger log = LoggerFactory.getLogger(AttachmentsAH.class);

  @Inject
  private AttachImplementationManager aim;

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

      if ("EDIT".equals(action)) {
        JSONObject params = request.getJSONObject("_params");
        recordIds = params.getString("inpKey");
        final String attachmentId = (String) parameters.get("attachmentId");
        final String strAttMethodId = (String) parameters.get("attachmentMethod");
        AttachmentMethod attachMethod = OBDal.getInstance().get(AttachmentMethod.class,
            strAttMethodId);
        Map<String, String> metadata = new HashMap<String, String>();
        for (Parameter param : AttachmentUtils.getMethodMetadataParameters(attachMethod, tab)) {
          String value;
          if (param.isFixed()) {
            if (!param.isUserEditable() && param.getPropertyPath() != null) {
              value = "Property Path";
            } else if (param.isEvaluateFixedValue()) {
              value = ParameterUtils.getParameterFixedValue(metadata, param).toString();
            } else {
              value = param.getFixedValue();
            }
          } else {
            value = URLDecoder.decode(params.get(param.getDBColumnName()).toString(), "UTF-8");
          }
          metadata.put(param.getId(), value);
        }

        aim.update(attachmentId, tabId, recordIds, metadata);

        JSONObject obj = getAttachmentJSONObject(tab, recordIds);
        obj.put("buttonId", params.getString("buttonId"));
        return obj;
      } else if (parameters.get("Command").equals("DELETE")) {

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
      } else {
        return new JSONObject();
      }
    } catch (JSONException e) {
      throw new OBException("Error while removing file", e);
    } catch (UnsupportedEncodingException e) {
      throw new OBException("Error decoding parameter", e);
    } catch (OBException e) {
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
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  public static JSONObject getAttachmentJSONObject(Tab tab, String recordIds) {
    List<JSONObject> attachments = AttachmentUtils.getTabAttachmentsForRows(tab,
        recordIds.split(","));
    JSONObject jsonobj = new JSONObject();
    try {
      jsonobj.put("attachments", new JSONArray(attachments));
    } catch (JSONException e) {
      throw new OBException(e);
    }
    return jsonobj;

  }
}