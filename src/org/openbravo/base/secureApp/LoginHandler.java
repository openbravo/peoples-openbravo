/*
 ************************************************************************************
 * Copyright (C) 2001-2009 Openbravo S.L.
 * Licensed under the Apache Software License version 2.0
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to  in writing,  software  distributed
 * under the License is distributed  on  an  "AS IS"  BASIS,  WITHOUT  WARRANTIES  OR
 * CONDITIONS OF ANY KIND, either  express  or  implied.  See  the  License  for  the
 * specific language governing permissions and limitations under the License.
 ************************************************************************************
 */
package org.openbravo.base.secureApp;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.base.HttpBaseServlet;
import org.openbravo.dal.core.OBContext;
import org.openbravo.erpCommon.obps.ActivationKey;
import org.openbravo.xmlEngine.XmlDocument;

public class LoginHandler extends HttpBaseServlet {
  private static final long serialVersionUID = 1L;
  private String strServletPorDefecto;

  @Override
  public void init(ServletConfig config) {
    super.init(config);
    strServletPorDefecto = config.getServletContext().getInitParameter("DefaultServlet");
  }

  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException,
      ServletException {

    if (log4j.isDebugEnabled()) {
      log4j.debug("start doPost");
    }
    final VariablesSecureApp vars = new VariablesSecureApp(req);

    // Empty session
    req.getSession(true).setAttribute("#Authenticated_user", null);

    if (vars.getStringParameter("user").equals("")) {
      res.sendRedirect(res.encodeRedirectURL(strDireccion + "/security/Login_F1.html"));
    } else {
      final String strUser = vars.getRequiredStringParameter("user");
      final String strPass = vars.getStringParameter("password");
      final String strUserAuth = LoginUtils.getValidUserId(myPool, strUser, strPass);

      if (strUserAuth != null) {
        req.getSession(true).setAttribute("#Authenticated_user", strUserAuth);
        checkLicenseAndGo(res, vars);
      } else {
        goToRetry(res, vars, null, "Identification failure. Try again.", "Error",
            "../security/Login_FS.html");
      }
    }
  }

  private void checkLicenseAndGo(HttpServletResponse res, VariablesSecureApp vars)
      throws IOException {
    OBContext.setAdminContext();
    try {
      ActivationKey ak = new ActivationKey();

      switch (ak.checkOPSLimitations()) {
      case NUMBER_OF_CONCURRENT_USERS_REACHED:
        String msg = "You have exceeded the number of Global Concurrent Users licensed to use this system.<br/>";
        msg += "Please wait until one or more users log out of the system and then retry again.<br/>";
        msg += "Contact your Openbravo Business Partner if you want to purchase a subscription for additional users.";
        String msgType = "Error";
        String action = "../security/Login_FS.html";
        goToRetry(res, vars, msg, "Maximum number of concurrent users reached", msgType, action);
        break;
      case NUMBER_OF_SOFT_USERS_REACHED:
        msg = "You have exceeded the number of Global Concurrent Users licensed to use this system.<br/>";
        msg += "Contact your Openbravo Business Partner if you want to purchase a subscription for additional users.";
        action = "../security/Menu.html";
        msgType = "Warning";
        goToRetry(res, vars, msg, "Maximum number of concurrent users reached", msgType, action);
        break;
      case OPS_INSTANCE_NOT_ACTIVE:
        msg = "Your Professional Subscription has expired. However no data has been lost.<br/>";
        msg += "To renew your Professional Subscription, contact your assigned partner.";
        action = "../security/Menu.html";
        msgType = "Warning";
        goToRetry(res, vars, msg, "Expired subscription", msgType, action);
        break;
      default:
        goToTarget(res, vars);
      }
    } finally {
      OBContext.setOBContext((OBContext) null);
    }

  }

  private void goToTarget(HttpServletResponse response, VariablesSecureApp vars) throws IOException {

    final String target = vars.getSessionValue("target");
    if (target.equals("")) {
      response.sendRedirect(strDireccion + "/security/Menu.html");
    } else {
      response.sendRedirect(target);
    }
  }

  private void goToRetry(HttpServletResponse response, VariablesSecureApp vars, String message,
      String title, String msgType, String action) throws IOException {
    String discard[] = { "" };

    if (msgType.equals("Error")) {
      discard[0] = "continueButton";
    } else {
      discard[0] = "backButton";
    }

    final XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/base/secureApp/HtmlErrorLogin", discard).createXmlDocument();

    // pass relevant mesasge to show inside the error page
    xmlDocument.setParameter("theme", vars.getTheme());
    xmlDocument.setParameter("messageType", msgType);
    xmlDocument.setParameter("action", action);
    xmlDocument.setParameter("messageTitle", title);
    xmlDocument
        .setParameter(
            "messageMessage",
            (message != null && !message.equals("")) ? message
                : "Please enter your username and password. "
                    + "<br>You must also ensure that your browser accepts cookies.<br>Press back to return.");

    response.setContentType("text/html");
    final PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  @Override
  public String getServletInfo() {
    return "User-login control Servlet";
  } // end of getServletInfo() method
}
