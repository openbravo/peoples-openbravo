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
 * All portions are Copyright (C) 2008 Openbravo SL 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
*/

package org.openbravo.erpCommon.ad_process;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.scheduling.ProcessBundle;
import org.openbravo.scheduling.ProcessRunner;

public class TestHeartbeat extends HttpSecureAppServlet {

  private static final long serialVersionUID = 1L;
  
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) 
      throws ServletException, IOException {
    VariablesSecureApp vars = new VariablesSecureApp(request);
    
    try {
      ProcessBundle bundle = new ProcessBundle("1005800000", vars).init(this);
      new ProcessRunner(bundle).execute(this);
      
      String msg = Utility.messageBD(this, "HB_SUCCESS", vars.getLanguage());
      advisePopUp(response, "SUCCESS", "Heartbeat Configuration", msg);
   
    } catch (Exception e) {
      e.printStackTrace();
      advisePopUp(response, "ERROR", "Heartbeat Configuration", e.getMessage());
    }
  }

}
