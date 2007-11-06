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
package org.openbravo.erpCommon.ad_reports;

import org.openbravo.base.secureApp.*;
import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class RptM_Production extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void init (ServletConfig config) {
    super.init(config);
    boolHist = false;
  }

  public void doPost (HttpServletRequest request, HttpServletResponse response) throws IOException,ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (vars.commandIn("DEFAULT")) {
      String strId = vars.getStringParameter("inpmProductionlineId");
      RptMProductionData[] data = RptMProductionData.select(this, strId);
      vars.removeSessionValue("Rpt_Etiquetas|cBpartnerId");
      vars.removeSessionValue("Rpt_Etiquetas|mProductId");
      vars.removeSessionValue("Rpt_Etiquetas|mAttributesetinstanceId");
      vars.removeSessionValue("Rpt_Etiquetas|qty");
      if (data!=null && data.length>0) {
        vars.setSessionValue("Rpt_Etiquetas|mProductId", data[0].mProductId);
        vars.setSessionValue("Rpt_Etiquetas|mAttributesetinstanceId", data[0].mAttributesetinstanceId);
        vars.setSessionValue("Rpt_Etiquetas|qty", data[0].qty);
      }
      response.sendRedirect(strDireccion + "/ad_reports/Rpt_Etiquetas.html");
    } else pageError(response);
  }

  public String getServletInfo() {
    return "Servlet configuration";
  } // end of the getServletInfo() method
}
