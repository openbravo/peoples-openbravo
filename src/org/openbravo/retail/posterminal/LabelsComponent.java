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
 * All portions are Copyright (C) 2012 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.retail.posterminal;

import java.util.Properties;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.Query;
import org.openbravo.base.session.OBPropertiesProvider;
import org.openbravo.client.kernel.BaseTemplateComponent;
import org.openbravo.client.kernel.KernelConstants;
import org.openbravo.client.kernel.Template;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;

public class LabelsComponent extends BaseTemplateComponent {
  private static final String TEMPLATE_ID = "A92865DA3F58419B9906A0307F41D705";
  private static final Logger log = Logger.getLogger(LabelsComponent.class);

  @Override
  protected Template getComponentTemplate() {
    return OBDal.getInstance().get(Template.class, TEMPLATE_ID);
  }

  public String getLabelsObj() {
    StringBuffer sb = new StringBuffer();
    sb.append(getLabels(null));
    return sb.toString();

  }

  public static JSONObject getLabels(String languageId) {
    try {
      String moduleIds = POSUtils.getRetailDependantModuleIds();
      JSONObject labels = new JSONObject();
      String hqlLabel = "select message.searchKey, message.messageText "
          + "from ADMessage message " + "where module.id in " + moduleIds;
      Query qryLabel = OBDal.getInstance().getSession().createQuery(hqlLabel);
      for (Object qryLabelObject : qryLabel.list()) {
        final Object[] qryLabelObjectItem = (Object[]) qryLabelObject;
        labels.put(qryLabelObjectItem[0].toString(), qryLabelObjectItem[1].toString());
      }

      String langId = languageId != null ? languageId : OBContext.getOBContext().getLanguage()
          .getId();
      String hqlTrlLabels = "select trl.message.searchKey, trl.messageText from ADMessageTrl trl where trl.message.module.id in "
          + moduleIds + " and trl.language.id='" + langId + "'";
      Query qryTrlLabels = OBDal.getInstance().getSession().createQuery(hqlTrlLabels);
      for (Object qryTrlObj : qryTrlLabels.list()) {
        final Object[] qryTrlObject = (Object[]) qryTrlObj;
        labels.put(qryTrlObject[0].toString(), qryTrlObject[1].toString());
      }
      return labels;
    } catch (Exception e) {
      log.error("There was an exception while generating the Web POS labels", e);
      return new JSONObject();
    }
  }

  public String getFormat() {

    try {
      JSONObject item = new JSONObject();
      final Properties props = OBPropertiesProvider.getInstance().getOpenbravoProperties();
      item.put("date", props.getProperty(KernelConstants.DATE_FORMAT_PROPERTY, "dd-MM-yyyy"));
      item.put("dateTime",
          props.getProperty(KernelConstants.DATETIME_FORMAT_PROPERTY, "dd-MM-yyyy HH:mm:ss"));
      return item.toString();
    } catch (Exception e) {
      log.error("There was an exception while generating the format", e);
    }
    return null;
  }

  @Override
  public boolean bypassAuthentication() {
    return true;
  }
}
