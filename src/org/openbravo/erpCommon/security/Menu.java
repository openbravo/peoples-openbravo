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
 * All portions are Copyright (C) 2001-2006 Openbravo SL 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
*/

package org.openbravo.erpCommon.security;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.xmlEngine.XmlDocument;

public class Menu extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

    /** Creates a new instance of Menu */
    public Menu() {
    }
    
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        
        VariablesSecureApp vars = new VariablesSecureApp(request);
        String targetmenu = vars.getSessionValue("targetmenu");
        printPageFrameIdentificacion(response, "../utility/VerticalMenu.html", (targetmenu.equals("") ? "../utility/Home.html" : targetmenu));
    }

    private void printPageFrameIdentificacion(HttpServletResponse response, String strMenu, String strDetalle) throws IOException, ServletException {
        XmlDocument xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/security/Login_FS").createXmlDocument();

        xmlDocument.setParameter("frameMenu", strMenu);
        xmlDocument.setParameter("frame1", strDetalle);
        response.setContentType("text/html; charset=UTF-8");
        PrintWriter out = response.getWriter();
        out.println(xmlDocument.print());
        out.close();
    }    
}
