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

import java.util.Vector;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.apache.log4j.Logger;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.database.ConnectionProvider;

public class SessionListener implements HttpSessionListener, ServletContextListener {

  private static final Logger log = Logger.getLogger(SessionListener.class);

  private static Vector<String> sessionsInContext = new Vector<String>();

  /**
   * This method is called whenever the session is destroyed because of user action or time out.
   * 
   * It deactivates the session in db
   */
  @Override
  public void sessionDestroyed(HttpSessionEvent event) {
    log.debug("Destroying session");
    String sessionId = (String) event.getSession().getAttribute("#AD_SESSION_ID");
    if (sessionId != null) {
      deactivateSession(sessionId);

    }
  }

  /**
   * This method is invoked when the server is shot down, it deactivates all sessions in this
   * context
   */
  @Override
  public void contextDestroyed(ServletContextEvent event) {
    log.info("Destroy context");

    for (String sessionId : sessionsInContext) {
      try {
        // cannot use dal at this point, use sqlc
        SessionLoginData.deactivate((ConnectionProvider) event.getServletContext().getAttribute(
            "openbravoPool"), sessionId);
        log.info("Deactivated session:" + sessionId);
      } catch (ServletException e1) {
        log.error(e1);
      }
    }

  }

  /**
   * Add a session to session tracking. This will be used when shut dowing the server
   * 
   * @param sessionId
   *          db id for the session to keep track
   */
  public static void addSession(String sessionId) {
    sessionsInContext.add(sessionId);
  }

  @Override
  public void contextInitialized(ServletContextEvent arg0) {
    // do nothing
  }

  @Override
  public void sessionCreated(HttpSessionEvent event) {
    // do nothing
  }

  private void deactivateSession(String sessionId) {
    try {

      sessionsInContext.remove(sessionId);

      OBContext.getOBContext().setInAdministratorMode(true);
      org.openbravo.model.ad.access.Session dalSession = OBDal.getInstance().get(
          org.openbravo.model.ad.access.Session.class, sessionId);
      if (dalSession != null) {
        dalSession.setSessionActive(false);
      }
      log.info("Clossed session:" + sessionId);

    } catch (Exception e) {
      log.error("Error closing session:" + sessionId);
      e.printStackTrace();
    }
  }

}
