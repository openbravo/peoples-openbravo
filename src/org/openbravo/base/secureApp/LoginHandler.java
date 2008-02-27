/*
 ************************************************************************************
 * Copyright (C) 2001-2006 Openbravo S.L.
 * Licensed under the Apache Software License version 2.0
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to  in writing,  software  distributed
 * under the License is distributed  on  an  "AS IS"  BASIS,  WITHOUT  WARRANTIES  OR
 * CONDITIONS OF ANY KIND, either  express  or  implied.  See  the  License  for  the
 * specific language governing permissions and limitations under the License.
 ************************************************************************************
*/
package org.openbravo.base.secureApp;

import org.openbravo.xmlEngine.XmlDocument;
import org.openbravo.utils.FormatUtilities;
import org.openbravo.base.*;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;



public class LoginHandler extends HttpBaseServlet{
  private static final long serialVersionUID = 1L;
  String strServletPorDefecto;


  public void init (ServletConfig config) {
    super.init(config);
    strServletPorDefecto = config.getServletContext().getInitParameter("DefaultServlet");
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {

   log4j.info("start doPost");
    VariablesSecureApp vars = new VariablesSecureApp(req);

        // Empty session
        req.getSession(true).setAttribute("#Authenticated_user", null);

      if (vars.getStringParameter("user").equals("")) {
        res.sendRedirect(res.encodeRedirectURL(strDireccion + "/security/Login_F1.html"));
      } else {
            String strUser = vars.getRequiredStringParameter("user");
            String strPass = FormatUtilities.sha1Base64(vars.getStringParameter("password"));
            String strUserAuth = SeguridadData.valido(this, strUser, strPass);

            if (!strUserAuth.equals("-1")) {
                req.getSession(true).setAttribute("#Authenticated_user", strUserAuth);
                goToTarget(res, vars);

            } else {
                goToRetry(res);
      }
    }
  }

    private void goToTarget(HttpServletResponse response, VariablesSecureApp vars) throws IOException {

        String target = vars.getSessionValue("target");
        if (target.equals("")) {
            response.sendRedirect(strDireccion + "/security/Menu.html");
        } else {
            response.sendRedirect(target);
        }
  }

  private void goToRetry(HttpServletResponse res) throws IOException {
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/base/secureApp/HtmlErrorLogin").createXmlDocument();

    res.setContentType("text/html");
    PrintWriter out = res.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  public String getServletInfo() {
    return "User-login control Servlet";
  } // end of getServletInfo() method
}
