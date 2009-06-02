/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.0  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html 
 * Software distributed under the License  is  distributed  on  an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific  language  governing  rights  and  limitations
 * under the License. 
 * The Original Code is Openbravo ERP. 
 * The Initial Developer of the Original Code is Openbravo SL 
 * All portions are Copyright (C) 2009 Openbravo SL 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.erpCommon.security;

import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.apache.log4j.Logger;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;

public class SessionListener implements HttpSessionListener {

  private static final Logger log = Logger.getLogger(SessionListener.class);

  @Override
  public void sessionCreated(HttpSessionEvent event) {
    // do nothing
  }

  /**
   * This method is called whenever the session is destroyed because of user action or time out.
   * 
   * It deactivates the session in db
   */
  @Override
  public void sessionDestroyed(HttpSessionEvent event) {
    String sessionId = "";
    try {
      sessionId = (String) event.getSession().getAttribute("#AD_SESSION_ID");
      if (sessionId != null) {
        OBContext.getOBContext().setInAdministratorMode(true);
        org.openbravo.model.ad.access.Session dalSession = OBDal.getInstance().get(
            org.openbravo.model.ad.access.Session.class, sessionId);
        if (dalSession != null) {
          dalSession.setProcessed(true);
        }
        log.info("Clossed session:" + sessionId);
      }
    } catch (Exception e) {
      log.error("Error closing session:" + sessionId);
      e.printStackTrace();
    }
  }

}
