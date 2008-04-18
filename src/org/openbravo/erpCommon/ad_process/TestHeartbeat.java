package org.openbravo.erpCommon.ad_process;

import java.io.IOException;
import java.io.PrintWriter;
import java.security.GeneralSecurityException;

import javax.net.ssl.SSLHandshakeException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.erpCommon.ad_background.PeriodicHeartbeat;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.xmlEngine.XmlDocument;

public class TestHeartbeat extends HttpSecureAppServlet {

  private static final long serialVersionUID = 1L;

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    VariablesSecureApp vars = new VariablesSecureApp(request);
    
    if (!PeriodicHeartbeat.isInternetAvailable(this)) {
      String message = Utility.messageBD(myPool, "HB_INTERNET_UNAVAILABLE", vars.getLanguage());
      advisePopUp(response, "ERROR", "Heartbeat", message);
      return;
    } else {
      String message = null;
      PeriodicHeartbeat phb = new PeriodicHeartbeat();
      try {
        phb.beat(myPool);
        message = Utility.messageBD(myPool, "HB_SUCCESS", vars.getLanguage());
        adviseHeartbeatConfirm(response, vars, "SUCCESS", "Heartbeat Configuration", message);
      } catch (IOException e) {
        if (e instanceof SSLHandshakeException) {
          message = Utility.messageBD(myPool, "HB_SECURE_CONNECTION_ERROR", vars.getLanguage());
          advisePopUp(response, "ERROR", "Heartbeat", message);
        } else {
          message = Utility.messageBD(myPool, "HB_SEND_ERROR", vars.getLanguage());
          advisePopUp(response, "ERROR", "Heartbeat", message);
        }
      } catch (ServletException e) {
        message = Utility.messageBD(myPool, "HB_INTERNAL_ERROR", vars.getLanguage());
        advisePopUp(response, "ERROR", "Heartbeat Configuration", message);
      } catch (GeneralSecurityException e) {
        message = Utility.messageBD(myPool, "HB_CERTIFICATE_ERROR", vars.getLanguage());
        advisePopUp(response, "ERROR", "Heartbeat Configuration", message);
      }
    }
  }
  
  public void adviseHeartbeatConfirm(HttpServletResponse response, VariablesSecureApp vars, String strTipo, String strTitulo, String strTexto) throws IOException {
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/ad_forms/HeartbeatConfirm").createXmlDocument();

    xmlDocument.setParameter("ParamTipo", strTipo.toUpperCase());
    xmlDocument.setParameter("ParamTitulo", strTitulo);
    xmlDocument.setParameter("ParamTexto", strTexto);
    
    xmlDocument.setParameter("result", strTexto);
    xmlDocument.setParameter("direction", "var baseDirection = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("language", "LNG_POR_DEFECTO=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("theme", vars.getTheme());
    
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }
  

}
