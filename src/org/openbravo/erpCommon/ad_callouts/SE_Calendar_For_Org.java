package org.openbravo.erpCommon.ad_callouts;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.xmlEngine.XmlDocument;

public class SE_Calendar_For_Org extends HttpSecureAppServlet {

  private static final long serialVersionUID = 1L;

  public void init(ServletConfig config) {
    super.init(config);
    boolHist = false;
  }

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);
    if (vars.commandIn("DEFAULT")) {
      String strOrgId = vars.getStringParameter("inpadOrgId");
      String strChanged = vars.getStringParameter("inpLastFieldChanged");
      String strWindowId = vars.getStringParameter("inpwindowId");
      try {
        printPage(response, vars, strWindowId, strOrgId, strChanged);
      } catch (ServletException ex) {
        pageErrorCallOut(response);
      }
    } else
      pageError(response);
  }

  private void printPage(HttpServletResponse response, VariablesSecureApp vars, String strWindowId,
      String strOrgId, String strChanged) throws IOException, ServletException {

    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_callouts/CallOut").createXmlDocument();

    StringBuffer resultado = new StringBuffer();
    resultado.append("var calloutName='SE_Calendar_For_Org';\n\n");
    resultado.append("var respuesta = new Array(");

    if (strChanged.equals("inpadOrgId") && !strOrgId.equals("")) {
      SEPeriodNoData[] tdv = null;
      // Update the Calendar
      try {
        tdv = SEPeriodNoData.getCalendar(this, strOrgId);
      } catch (Exception ex) {
        throw new ServletException(ex);
      }
      resultado.append("new Array(\"inpcCalendarId\", ");
      if (tdv != null && tdv.length > 0) {
        for (int i = 0; i < tdv.length; i++) {
          resultado.append("new Array(\"" + tdv[i].getField("id") + "\", \""
              + tdv[i].getField("Name") + "\")");
          if (i < tdv.length - 1)
            resultado.append(",\n");
        }
        resultado.append("\n)");
      } else
        resultado.append("null");
      resultado.append("\n)");
    }
    resultado.append(");");
    xmlDocument.setParameter("array", resultado.toString());
    xmlDocument.setParameter("frameName", "appFrame");
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();

  }
}
