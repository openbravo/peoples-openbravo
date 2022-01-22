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
 * All portions are Copyright (C) 2017-2022 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.client.application;

import javax.servlet.ServletException;
import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openbravo.base.exception.OBException;
import org.openbravo.client.kernel.KernelConstants;
import org.openbravo.client.kernel.SessionDynamicTemplateComponent;
import org.openbravo.erpCommon.ad_process.HeartbeatProcess;
import org.openbravo.erpCommon.ad_process.HeartbeatProcess.HeartBeatOrRegistration;
import org.openbravo.service.db.DalConnectionProvider;

/**
 * The component responsible for generating the content of the function used to determine if the
 * HeartBeat or any kind of registration pop-up should be displayed once the user has logged in into
 * the application.
 */
public class HeartBeatPopUpComponent extends SessionDynamicTemplateComponent {

  private static final Logger log = LogManager.getLogger();
  private static final String COMPONENT_ID = "HeartbeatRegistration";
  private static final String TEMPLATE_ID = "EE5CEC203AEA4B039CCDAD0BE8E07E3C";

  @Override
  public String getId() {
    return COMPONENT_ID;
  }

  @Override
  protected String getTemplateId() {
    return TEMPLATE_ID;
  }

  public String getHeartBeatRegistrationFunction() {
    try {
      switch (getPopUpToShow()) {
        case InstancePurpose:
          return "OB.Layout.ClassicOBCompatibility.Popup.openInstancePurpose()";
        case HeartBeat:
          return "OB.Layout.ClassicOBCompatibility.Popup.openHeartbeat()";
        default:
          return "return";
      }
    } catch (Exception e) {
      throw new OBException(e);
    }
  }

  private HeartBeatOrRegistration getPopUpToShow() throws ServletException {
    Object sessionObject = getParameters().get(KernelConstants.HTTP_SESSION);
    if (sessionObject == null) {
      // not showing any pop-up
      log.info("Could not check the type of registration pop-up to be displayed");
      return HeartBeatOrRegistration.None;
    }
    HttpSession session = (HttpSession) sessionObject;
    String roleId = (String) session.getAttribute("#AD_ROLE_ID");
    String javaDateFormat = (String) session.getAttribute("#AD_JAVADATEFORMAT");
    return HeartbeatProcess.isLoginPopupRequired(roleId, javaDateFormat,
        new DalConnectionProvider(false));
  }
}
