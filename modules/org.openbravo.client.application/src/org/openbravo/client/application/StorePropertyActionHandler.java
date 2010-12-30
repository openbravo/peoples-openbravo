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
 * All portions are Copyright (C) 2009 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.client.application;

import java.util.Map;

import javax.enterprise.context.ApplicationScoped;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.client.kernel.BaseActionHandler;
import org.openbravo.client.kernel.StaticResourceComponent;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.businessUtility.Preferences;

/**
 * Is responsible for storing a preference.
 * 
 * @author mtaal
 * @see StaticResourceComponent
 */
@ApplicationScoped
public class StorePropertyActionHandler extends BaseActionHandler {
  private static final Logger log = Logger.getLogger(StorePropertyActionHandler.class);

  protected JSONObject execute(Map<String, Object> parameters, String data) {
    OBContext.setAdminMode();
    try {
      final String propertyName = (String) parameters.get(ApplicationConstants.PROPERTY_PARAMETER);
      if (propertyName == null) {
        log.warn("No property name in request, ignoring property store request");
        return new JSONObject(
            "{msg: 'No property name in request, ignoring property store request'}");
      }
      final String cleanedData = data == null ? null : data.replaceAll("\\n", "");
      Preferences.setPreferenceValue(propertyName, cleanedData, true, OBContext.getOBContext()
          .getCurrentClient(), OBContext.getOBContext().getCurrentOrganization(), OBContext
          .getOBContext().getUser(), OBContext.getOBContext().getRole(), null, null);
      OBDal.getInstance().flush();
      return new JSONObject("{msg: 'Property Stored'}");
    } catch (Exception e) {
      throw new IllegalStateException(e);
    } finally {
      OBContext.restorePreviousMode();
    }
  }
}
