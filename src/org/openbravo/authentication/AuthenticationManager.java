/*
 ************************************************************************************
 * Copyright (C) 2001-2011 Openbravo S.L.U.
 * Licensed under the Apache Software License version 2.0
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to  in writing,  software  distributed
 * under the License is distributed  on  an  "AS IS"  BASIS,  WITHOUT  WARRANTIES  OR
 * CONDITIONS OF ANY KIND, either  express  or  implied.  See  the  License  for  the
 * specific language governing permissions and limitations under the License.
 ************************************************************************************
 */

package org.openbravo.authentication;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.openbravo.authentication.basic.DefaultAuthenticationManager;
import org.openbravo.base.HttpBaseUtils;
import org.openbravo.base.VariablesBase;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.security.SessionLogin;
import org.openbravo.model.ad.access.Session;
import org.openbravo.service.db.DalConnectionProvider;

/**
 * 
 * @author adrianromero
 * @author iperdomo
 */
public abstract class AuthenticationManager {

  private static final Logger log4j = Logger.getLogger(AuthenticationManager.class);

  protected ConnectionProvider conn = null;
  protected String defaultServletUrl = null;
  protected String localAdress = null;

  public AuthenticationManager() {
  }

  public AuthenticationManager(HttpServlet s) throws AuthenticationException {
    init(s);
  }

  protected void bdErrorAjax(HttpServletResponse response, String strType, String strTitle,
      String strText) throws IOException {
    response.setContentType("text/xml; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n");
    out.println("<xml-structure>\n");
    out.println("  <status>\n");
    out.println("    <type>" + strType + "</type>\n");
    out.println("    <title>" + strTitle + "</title>\n");
    out.println("    <description><![CDATA[" + strText + "]]></description>\n");
    out.println("  </status>\n");
    out.println("</xml-structure>\n");
    out.close();
  }

  public void init(HttpServlet s) throws AuthenticationException {
    if (s instanceof ConnectionProvider) {
      conn = (ConnectionProvider) s;
    } else {
      conn = new DalConnectionProvider();
    }
    defaultServletUrl = s.getServletConfig().getServletContext()
        .getInitParameter("ServletSinIdentificar");
  }

  /**
   * Used in the service method of the {@link HttpSecureAppServlet} to know if the request is
   * authenticated or not. This method calls the <b>doAuthenticate</b> that makes the actual checks
   * and could be easily extended by sub-classes. Returns the user id if the user is already logged
   * in or null if is not authenticated.
   * 
   * @param request
   *          HTTP request object to handle parameters and session attributes
   * @param response
   *          HTTP response object to handle possible redirects
   * @return the value of AD_User_ID if the user is already authenticated or <b>null</b> if not
   */
  public final String authenticate(HttpServletRequest request, HttpServletResponse response)
      throws AuthenticationException, ServletException, IOException {

    if (localAdress == null) {
      localAdress = HttpBaseUtils.getLocalAddress(request);
    }

    final String userId = doAuthenticate(request, response);

    final VariablesSecureApp vars = new VariablesSecureApp(request, false);
    if (StringUtils.isEmpty(vars.getSessionValue("#AD_SESSION_ID"))) {
      final String sessionId = createDBSession(request, "", userId);
      vars.setSessionValue(userId, sessionId);
      vars.setSessionValue("#AD_SESSION_ID", sessionId);
    }

    if (userId == null && !response.isCommitted()) {
      response.sendRedirect(localAdress + defaultServletUrl);
      return null;
    }

    return userId;
  }

  /**
   * Clears all session attributes and calls the <b>doLogout</b> method
   */
  public final void logout(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    VariablesBase vars = new VariablesBase(request);
    vars.clearSession(true);

    doLogout(request, response);
  }

  /**
   * Called from the <b>authenticate</b> method makes the necessary processing to check if the
   * request is authenticated or not. The simplest way to check is if the #Authenticated_user
   * session attribute is present and return it.
   * 
   * @param request
   *          HTTP request object, used for handling parameters and session attributes
   * @param response
   * @return <ul>
   *         <li>The user id (AD_User_ID) if the request is already authenticated or the
   *         authentication process succeeded</li>
   *         <li><b>null</b> if the request is not authenticated or authentication process failed
   *         (e.g. wrong password)</li>
   *         </ul>
   * @see DefaultAuthenticationManager
   */
  protected abstract String doAuthenticate(HttpServletRequest request, HttpServletResponse response)
      throws AuthenticationException, ServletException, IOException;

  /**
   * Method called from the <b>logout</b> method after clearing all session attributes. The usual
   * process is to redirect the user to the login page
   * 
   * @param request
   *          HTTP request object
   * @param response
   *          HTTP response object
   */
  protected abstract void doLogout(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException;

  protected final String createDBSession(HttpServletRequest req, String strUser, String strUserAuth) {
    try {
      String usr = strUserAuth == null ? "0" : strUserAuth;

      final SessionLogin sl = new SessionLogin(req, "0", "0", usr);

      if (strUserAuth == null) {
        sl.setStatus("F");
      } else {
        sl.setStatus("S");
      }

      sl.setUserName(strUser);
      sl.setServerUrl(HttpBaseUtils.getLocalAddress(req));
      sl.save();
      return sl.getSessionID();
    } catch (Exception e) {
      log4j.error("Error creating DB session", e);
      return null;
    }
  }

  protected final void updateDBSession(String sessionId, boolean sessionActive, String status) {
    try {
      OBContext.setAdminMode();
      Session session = OBDal.getInstance().get(Session.class, sessionId);
      session.setSessionActive(sessionActive);
      session.setLoginStatus(status);
      OBDal.getInstance().flush();
    } catch (Exception e) {
      log4j.error("Error updating session in DB", e);
    } finally {
      OBContext.restorePreviousMode();
    }

  }

}
