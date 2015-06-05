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

package org.openbravo.client.application.window;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.Query;
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.exception.OBException;
import org.openbravo.client.application.Parameter;
import org.openbravo.dal.core.DalUtil;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBDao;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.model.ad.system.Client;
import org.openbravo.model.ad.ui.Tab;
import org.openbravo.model.ad.utility.Attachment;
import org.openbravo.model.ad.utility.AttachmentConfig;
import org.openbravo.model.ad.utility.AttachmentMethod;

public class AttachmentUtils {
  private static Map<String, String> clientConfigs = new HashMap<String, String>();
  public static final String DEFAULT_METHOD = "Default";
  public static final String DEFAULT_METHOD_ID = "D7B1319FC2B340799283BBF8E838DF9F";

  /**
   * Gets the Attachment Configuration associated to the active client
   * 
   * @param client
   *          Client using openbravo
   * @return Activated Attachment Configuration for this client
   */
  public static AttachmentConfig getAttachmentConfig(Client client) {
    String strAttachmentConfigId = clientConfigs.get(DalUtil.getId(client));
    if (strAttachmentConfigId == null) {
      // Only one active AttachmentConfig is allowed per client.
      OBCriteria<AttachmentConfig> critAttConf = OBDal.getInstance().createCriteria(
          AttachmentConfig.class);
      critAttConf.add(Restrictions.eq(AttachmentConfig.PROPERTY_CLIENT, client));
      if (!OBDal.getInstance().isActiveFilterEnabled()) {
        critAttConf.setFilterOnActive(true);
      }
      critAttConf.setMaxResults(1);
      AttachmentConfig attConf = (AttachmentConfig) critAttConf.uniqueResult();
      if (attConf != null) {
        clientConfigs.put((String) DalUtil.getId(client), attConf.getId());
      }
      return attConf;
    }
    return OBDal.getInstance().get(AttachmentConfig.class, strAttachmentConfigId);
  }

  public static void setAttachmentConfig(String strClient, String strAttConfig) {
    if (strAttConfig == null) {
      clientConfigs.remove(strClient);
    } else {
      clientConfigs.put(strClient, strAttConfig);
    }
  }

  /**
   * Gets the Attachment Configuration associated to the context client
   * 
   * @return Activated Attachment Configuration for this context
   */
  public static AttachmentConfig getAttachmentConfig() {
    Client client = OBContext.getOBContext().getCurrentClient();
    return getAttachmentConfig(client);
  }

  /**
   * Gets the default Attachment Method
   * 
   * @return Default Attachment Method
   */
  public static AttachmentMethod getDefaultAttachmentMethod() {
    AttachmentMethod attMethod = OBDal.getInstance().get(AttachmentMethod.class, DEFAULT_METHOD_ID);
    if (attMethod != null) {
      return attMethod;
    } else {
      throw new OBException(OBMessageUtils.messageBD("OBUIAPP_NoMethod"));
    }
  }

  /**
   * Gets the list of parameters associated to an Attachment Method ad a Tab
   * 
   * @param attachMethod
   *          active attachment method
   * @param tab
   *          tab to take metadata
   * @return List of parameters by attachment method and tab
   */
  public static List<Parameter> getMethodMetadataParameters(AttachmentMethod attachMethod, Tab tab) {
    StringBuilder where = new StringBuilder();
    where.append(Parameter.PROPERTY_ATTACHMENTMETHOD + "= :attMethod");
    where.append(" and (" + Parameter.PROPERTY_TAB + " is null or " + Parameter.PROPERTY_TAB
        + " = :tab)");
    final OBQuery<Parameter> qryParams = OBDal.getInstance().createQuery(Parameter.class,
        where.toString());
    qryParams.setNamedParameter("attMethod", attachMethod);
    qryParams.setNamedParameter("tab", tab);
    return qryParams.list();
  }

  /**
   * Get JSONObject list with data of the attachments in given tab and records
   * 
   * @param tab
   *          tab to take attachments
   * @param recordIds
   *          list of record IDs where taken attachments
   * @return List of JSONOject with attachments information values
   */
  public static List<JSONObject> getTabAttachmentsForRows(Tab tab, String[] recordIds) {
    String tableId = (String) DalUtil.getId(tab.getTable());
    OBCriteria<Attachment> attachmentFiles = OBDao.getFilteredCriteria(Attachment.class,
        Restrictions.eq("table.id", tableId), Restrictions.in("record", recordIds));
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
        String attachmentMethod = DEFAULT_METHOD_ID;
        if (attachment.getAttachmentConf() != null) {
          attachmentMethod = (String) DalUtil.getId(attachment.getAttachmentConf()
              .getAttachmentMethod());
        }
        attachmentobj.put("attmethod", attachmentMethod);
      } catch (JSONException ignore) {
      }
      attachments.add(attachmentobj);
    }
    return attachments;
  }

  /**
   * Get the String value of a parameter with a property path
   * 
   * @param parameter
   *          parameter in which is defined the property path
   * @param tabId
   *          table which stores the record with the desired value
   * @param recordId
   *          record which has the column with the value to search
   * @return the String value of the column indicated in the property path
   * @throws OBException
   *           generated if there is distinct than one record to search
   */
  public static String getPropertyPathValue(Parameter parameter, String tabId, String recordId)
      throws OBException {
    Tab tab = OBDal.getInstance().get(Tab.class, tabId);
    final String hql = "SELECT a." + parameter.getPropertyPath() + " FROM "
        + tab.getTable().getName() + " AS a WHERE a.id=:recordId";
    final Query query = OBDal.getInstance().getSession().createQuery(hql);
    query.setString("recordId", recordId);
    query.setMaxResults(1);
    try {
      return (String) query.uniqueResult();
    } catch (Exception e) {
      throw new OBException(OBMessageUtils.messageBD("OBUIAPP_PropPathNotOneRecord"));
    }
  }

}
