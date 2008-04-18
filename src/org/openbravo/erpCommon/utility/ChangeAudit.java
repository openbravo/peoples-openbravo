package org.openbravo.erpCommon.utility;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;

public class ChangeAudit extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;
  public void doPost (HttpServletRequest request, HttpServletResponse response) throws IOException,ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);
    String strWindowId = vars.getStringParameter("inpwindowId");

    String strCurrentValueAudit = vars.getSessionValue("P|"+strWindowId+"|ShowAudit");
    if (strCurrentValueAudit.equals("")) strCurrentValueAudit = vars.getSessionValue("#ShowAudit");
    String newValue;
    if (strCurrentValueAudit.equals("Y")) newValue = "N";
    else newValue="Y";
    vars.setSessionValue("P|"+strWindowId+"|ShowAudit", newValue); 
    PrintWriter out = response.getWriter();
    out.println("Audit value:"+newValue+" - window:"+strWindowId);
    out.close();
  }

}
