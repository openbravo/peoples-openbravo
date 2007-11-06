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

//import org.openbravo.xmlEngine.Report;
import org.openbravo.base.*;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;



public class ServletGoBack extends HttpBaseServlet {
  private static final long serialVersionUID = 1L;
  String strServletPorDefecto;

  public class Variables extends VariablesHistory {

    public Variables(HttpServletRequest request) {
      super(request);
      String sufix = getCurrentHistoryIndex();
      removeSessionValue("reqHistory.servlet" + sufix);
      removeSessionValue("reqHistory.path" + sufix);
      removeSessionValue("reqHistory.command" + sufix);
      downCurrentHistoryIndex();
    }
  }

  public void init (ServletConfig config) {
    super.init(config);
    strServletPorDefecto = config.getServletContext().getInitParameter("DefaultServlet");
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
    log4j.info("start doPost");
    Variables vars = new Variables(req);
    String strUrl = strDireccion + vars.getCurrentServletPath(strServletPorDefecto) + "?Command=" + vars.getCurrentServletCommand();
    res.sendRedirect(res.encodeRedirectURL(strUrl));
  }

  public String getServletInfo() {
    return "Servlet that receives and redirects go back requests, using history information registered in the httpSession";
  } // end of getServletInfo() method
}
