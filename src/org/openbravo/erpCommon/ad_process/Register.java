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
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.UUID;

import javax.net.ssl.SSLHandshakeException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.erpCommon.ad_background.PeriodicHeartbeat;
import org.openbravo.erpCommon.ad_forms.Registration;
import org.openbravo.erpCommon.utility.HttpsUtils;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.xmlEngine.XmlDocument;

public class Register extends HttpSecureAppServlet {
 
  static Logger log4j = Logger.getLogger(PeriodicHeartbeat.class);
  
  private static final long serialVersionUID = 1L;
  
  public static final String PROTOCOL = "https";
  public static final String HOST = "butler.openbravo.com";
  public static final int PORT = 443;
  public static final String PATH = "/heartbeat-server/register";
  
  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    VariablesSecureApp vars = new VariablesSecureApp(request);
    
    if (!PeriodicHeartbeat.isInternetAvailable(this)) {
      String message = Utility.messageBD(myPool, "HB_INTERNET_UNAVAILABLE", vars.getLanguage());
      log4j.error(message);
      advisePopUp(response, "ERROR", "Registration", message);
    } else {
      
      RegisterData[] data = RegisterData.select(this);
      if (data.length > 0) {
        RegisterData rd = data[0];
        if (rd.isregistrationactive == null || rd.isregistrationactive.equals("")) {
          rd.isregistrationactive = "N";
          RegisterData.updateIsRegistrationActive(this, "N");
        }
        if (rd.registrationId == null || rd.registrationId.equals("")) {
          String registrationId = UUID.randomUUID().toString();
          rd.registrationId = registrationId;
          RegisterData.updateRegistrationId(this, registrationId);
        }
        String queryStr = createQueryString(rd);
        String encodedQueryStr = HttpsUtils.encode(queryStr, "UTF-8");
        String result = null;
        String message = null;
        try {
          result = register(encodedQueryStr);
          if (result == null || result.equals(rd.registrationId)) {
            // TODO Something went wrong. Handle.
          }
          message = Utility.messageBD(myPool, "REG_SUCCESS", vars.getLanguage());
          adviseRegistrationConfirm(response, vars, "SUCCESS", "Registration", message);
        } catch (IOException e) {
          if (e instanceof SSLHandshakeException) {
            message = Utility.messageBD(myPool, "HB_SECURE_CONNECTION_ERROR", vars.getLanguage());
            advisePopUp(response, "ERROR", "Registration", message);
          } else {
            message = Utility.messageBD(myPool, "HB_SEND_ERROR", vars.getLanguage());
            advisePopUp(response, "ERROR", "Registration", message);
          }
        } catch (GeneralSecurityException e) {
          message = Utility.messageBD(myPool, "HB_CERTIFICATE_ERROR", vars.getLanguage());
          advisePopUp(response, "ERROR", "Registration", message);
        }
      }
    }
  }
  
  public void adviseRegistrationConfirm(HttpServletResponse response, VariablesSecureApp vars, String strTipo, String strTitulo, String strTexto) throws IOException {
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/ad_forms/RegistrationConfirm").createXmlDocument();

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
  
  public String register(String encodedQueryStr) 
      throws GeneralSecurityException, IOException  {
    URL url = null;
    try {
      url = new URL(PROTOCOL, HOST, PORT, PATH);
    } catch (MalformedURLException e) {
      e.printStackTrace(); // Won't happen.
    }
    log4j.info(("Sending registration info: '" + encodedQueryStr + "'"));
    return HttpsUtils.sendSecure(url, encodedQueryStr, "changeit");
  }
  
  private String createQueryString(RegisterData data) {  
    
    String registrationId = data.registrationId;
    String isregistrationactive = data.isregistrationactive;
    String companyname = data.companyname;
    String companyaddress = data.companyaddress;
    String contacttitle = data.contacttitle;
    String contactname = data.contactname;
    String contactemail = data.contactemail;
    String contactphone = data.contactphone;
    String companyindustry = data.companyindustry;
    String companyrevenue = data.companyrevenue;
    String companynumEmployees = data.companynumEmployees;
    String issubscribecommercial = data.issubscribecommercial;
    String issubscribedevelopment = data.issubscribedevelopment;
    String iscommercialcontact = data.iscommercialcontact;
    
    StringBuilder sb = new StringBuilder();
    sb.append("registrationId=" + (registrationId == null ? "" : registrationId) + "&");
    sb.append("isregistrationactive=" + (isregistrationactive == null ? "" : isregistrationactive) + "&");
    sb.append("companyname=" + (companyname == null ? "" : companyname) + "&");
    sb.append("companyaddress=" + (companyaddress == null ? "" : companyaddress) + "&");
    sb.append("contacttitle=" + (contacttitle == null ? "" : contacttitle) + "&");
    sb.append("contactname=" + (contactname == null ? "" : contactname) + "&");
    sb.append("contactemail=" + (contactemail == null ? "" : contactemail) + "&");
    sb.append("contactphone=" + (contactphone == null ? "" : contactphone) + "&");
    sb.append("companyindustry=" + (companyindustry == null ? "" : companyindustry) + "&");
    sb.append("companyrevenue=" + (companyrevenue == null ? "" : companyrevenue) + "&");
    sb.append("companynumEmployees=" + (companynumEmployees == null ? "" : companynumEmployees) + "&");
    sb.append("issubscribecommercial=" + (issubscribecommercial == null ? "" : issubscribecommercial) + "&");
    sb.append("issubscribedevelopment=" + (issubscribedevelopment == null ? "" : issubscribedevelopment) + "&");
    sb.append("iscommercialcontact=" + (iscommercialcontact == null ? "" : iscommercialcontact) + "&");
    
    return sb.toString();
  }
}
