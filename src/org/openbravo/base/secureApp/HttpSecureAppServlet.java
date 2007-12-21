/*
 ************************************************************************************
 * Copyright (C) 2001-2007 Openbravo S.L.
 * Licensed under the Apache Software License version 2.0
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to  in writing,  software  distributed
 * under the License is distributed  on  an  "AS IS"  BASIS,  WITHOUT  WARRANTIES  OR
 * CONDITIONS OF ANY KIND, either  express  or  implied.  See  the  License  for  the
 * specific language governing permissions and limitations under the License.
 ************************************************************************************
*/
package org.openbravo.base.secureApp;

import org.openbravo.base.HttpBaseServlet;
import org.openbravo.xmlEngine.XmlDocument;
import org.openbravo.erpCommon.utility.*;
import org.openbravo.erpCommon.ad_background.*;
import org.openbravo.utils.Replace;

import java.io.*;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.xml.parsers.*;
import java.util.*;
import java.sql.Connection;
import org.w3c.dom.*;
import org.openbravo.authentication.AuthenticationException;
import org.openbravo.authentication.AuthenticationManager;
import org.openbravo.authentication.basic.DefaultAuthenticationManager;
import org.openbravo.erpCommon.security.SessionLogin;
import org.openbravo.erpCommon.security.SessionLoginData;
import org.openbravo.erpCommon.security.AccessData;
import org.openbravo.data.FieldProvider;



import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.export.JExcelApiExporter;
import net.sf.jasperreports.engine.export.JRHtmlExporter;
import net.sf.jasperreports.engine.export.JRHtmlExporterParameter;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import net.sf.jasperreports.engine.export.JExcelApiExporterParameter;

public class HttpSecureAppServlet extends HttpBaseServlet{
  private static final long serialVersionUID = 1L;
  public boolean boolHist = true;
  String strServletIdentificacion;
  String strServletSinIdentificar;
  String myTheme = "";
  public String strServletGoBack;
  public String strFTPDirectory;
  public String strPeriodicBackgroundTime;
  public String strLogFileAcctServer;
  public ClassInfoData classInfo;
  protected AuthenticationManager m_AuthManager = null;

  String servletClass = this.getClass().getName();
  protected static Hashtable<String, PeriodicBackground> backgroundProcess;

  public class Variables extends VariablesHistory {
    public Variables(HttpServletRequest request) {
      super(request);
    }

    public void updateHistory(HttpServletRequest request) {
      if (boolHist) {
        String sufix = getCurrentHistoryIndex();
        if (!(servletClass.equals(getSessionValue("reqHistory.servlet" + sufix,"")))) {
          upCurrentHistoryIndex();
          sufix = getCurrentHistoryIndex();
          setSessionValue("reqHistory.servlet" + sufix, servletClass);
          setSessionValue("reqHistory.path" + sufix, request.getServletPath());
          setSessionValue("reqHistory.command" + sufix, "DEFAULT");
        }
      }
    }
    public void setHistoryCommand(String strCommand) {
      String sufix = getCurrentHistoryIndex();
      setSessionValue("reqHistory.command" + sufix, strCommand);
    }
  }

  public void init (ServletConfig config) {
    super.init(config);

    // Authentication manager load
    // String sAuthManagerClass = config.getServletContext().getInitParameter("AuthenticationManager"); 
    String sAuthManagerClass = HttpBaseServlet.getOBProperty("authentication.class");
    if (sAuthManagerClass == null  || sAuthManagerClass.equals("")) {
        // If not defined, load default
        sAuthManagerClass = "org.openbravo.authentication.basic.DefaultAuthenticationManager";
    }    
    
    try {
        m_AuthManager = (AuthenticationManager) Class.forName(sAuthManagerClass).newInstance();
    } catch (Exception e) {
        log4j.error("Authentication manager not defined", e);
        m_AuthManager = new DefaultAuthenticationManager();
    }
    
    try {
        m_AuthManager.init(this);
    } catch (AuthenticationException e) {
        log4j.error("Unable to initialize authentication manager", e);
    }
    
    strServletIdentificacion = config.getServletContext().getInitParameter("LoginServlet");
    strServletSinIdentificar = config.getServletContext().getInitParameter("ServletSinIdentificar");
    if (log4j.isDebugEnabled()) log4j.debug("strdireccion: " + strDireccion);
    strServletGoBack = config.getServletContext().getInitParameter("ServletGoBack");
    strFTPDirectory = config.getServletContext().getInitParameter("AttachmentDirectory");
    strFTPDirectory = strFTPDirectory.replace("@actual_path_context@", prefix);
    try {
      File f = new File(strFTPDirectory);
      if (!f.exists()) f.mkdir();
    } catch (Exception e) {
      e.printStackTrace();
    }
    if (log4j.isDebugEnabled()) log4j.debug("strServletGoBack: " + strServletGoBack);
    strPeriodicBackgroundTime = config.getServletContext().getInitParameter("PeriodicBackgroundTime");
    strLogFileAcctServer = config.getServletContext().getInitParameter("LogFileAcctServer");
    strLogFileAcctServer = prefix + "/" + strBaseConfigPath + "/" + strLogFileAcctServer;
    if (backgroundProcess == null && strPeriodicBackgroundTime!=null && !strPeriodicBackgroundTime.equals("") && strLogFileAcctServer!=null && !strLogFileAcctServer.equals("")) {
      backgroundProcess = new Hashtable<String, PeriodicBackground>();
      SystemPreferencesData[] backgroundData = null;
      try {
        backgroundData = SystemPreferencesData.selectBackground(this);
      } catch (ServletException sex) {
        sex.printStackTrace();
      }
      if (backgroundData!=null && backgroundData.length>0) {
        for (int countBack = 0;countBack<backgroundData.length;countBack++) {
          PeriodicBackground object = null;
          try {
          object = new PeriodicBackground(this, Long.parseLong(strPeriodicBackgroundTime), strLogFileAcctServer, backgroundData[countBack].id, backgroundData[countBack].classname);
          } catch (IOException ioe) {
            ioe.printStackTrace();
          }
          if (object!=null) {
            object.start();
            backgroundProcess.put(backgroundData[countBack].id, object);
          }
        }
      }
    }
  }

  public void service(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
    Variables variables = new Variables(request);
  //  VariablesSecureApp vars = new VariablesSecureApp(request);
    
    try {
      myTheme = variables.getSessionValue("#Theme");
    } catch (Exception ignored) {
      myTheme = "Default";
    }
    try {
      if (log4j.isDebugEnabled()) log4j.debug("Servlet request for class info: " + request.getServletPath());
      ClassInfoData[] classInfoAux = ClassInfoData.select(this, variables.getLanguage(), request.getServletPath());
      if (classInfoAux!=null && classInfoAux.length>0) classInfo = classInfoAux[0];
      else {
        classInfoAux = ClassInfoData.set();
        classInfo = classInfoAux[0];
      }
    } catch (Exception ex) {
      ex.printStackTrace();
      ClassInfoData[] classInfoAux = ClassInfoData.set();
      classInfo = classInfoAux[0];
    }
    
  //  bdErrorGeneral(response, "Error", "No access");
    
    if (log4j.isDebugEnabled()) log4j.debug("class info type: " + classInfo.type + " - ID: " + classInfo.id + " - NAME: " + classInfo.name);
    String strAjax = "";
    String strHidden = "";
    String strPopUp = "";
    try {
      strAjax = request.getParameter("IsAjaxCall");
    } catch (Exception ignored) {}
    try {
      strHidden = request.getParameter("IsHiddenCall");
    } catch (Exception ignored) {}
    try {
      strPopUp = request.getParameter("IsPopUpCall");
    } catch (Exception ignored) {}
    
    try {
      String strUserAuth = m_AuthManager.authenticate(request, response);
      if (strUserAuth != null) {
        if (variables.getRole().equals("") || !SeguridadData.loggedOK(this, variables.getDBSession())) {
          String strLanguage = "";
          String strRole = "";
          String strClient = "";
          String strOrg = "";
          String strWarehouse = "";
          
          strRole = DefaultOptionsData.defaultRole(this, strUserAuth);
          if(strRole == null)
        	  strRole = DefaultOptionsData.getDefaultRole(this, strUserAuth);
          validateDefault(strRole, strUserAuth, "Role");
          
          strOrg = DefaultOptionsData.defaultOrg(this, strUserAuth);
          if(strOrg == null )
        	  strOrg = DefaultOptionsData.getDefaultOrg(this, strRole);
          validateDefault(strOrg, strRole, "Org");
          
          strClient = DefaultOptionsData.defaultClient(this, strUserAuth);
          if(strClient == null)
        	  strClient = DefaultOptionsData.getDefaultClient(this, strRole);
          validateDefault(strClient, strRole, "Client");
          
          strWarehouse = DefaultOptionsData.defaultWarehouse(this, strUserAuth);
          if(strWarehouse == null) {
        	  if(!strRole.equals("0")) {
        		  strWarehouse = DefaultOptionsData.getDefaultWarehouse(this, strClient);
        		  validateDefault(strWarehouse, strClient, "Warehouse");
        	  }
        	  else
        		  strWarehouse = "";
          }
          
          strLanguage = DefaultOptionsData.defaultLanguage(this, strUserAuth);          
          if(strLanguage == null) {
            strLanguage = DefaultOptionsData.getDefaultLanguage(this); 
          }
                              
          VariablesSecureApp vars = new VariablesSecureApp(request);
          if (LoginUtils.fillSessionArguments(this, vars, strUserAuth, strLanguage, strRole, strClient, strOrg, strWarehouse)) {
            readProperties(vars, prefix + strBaseConfigPath + "/Openbravo.properties");
            readNumberFormat(vars, prefix + strBaseConfigPath + "/Format.xml");
            saveLoginBD(request, vars, strClient, strOrg);
          } else {
            // Re-login
            log4j.error("Unable to fill session Arguments for: " +  strUserAuth);
            logout(request, response);
            return;
          }          
        } else variables.updateHistory(request);
      }
      log4j.info("Call to HttpBaseServlet.service");
    } catch (Exception e) {
      // Re-login
      log4j.error(e);
      logout(request, response);
      return;
    }
    
    try {
      
      super.initialize(request,response);
      VariablesSecureApp vars1 = new VariablesSecureApp(request, false); 
      if (vars1.getRole().equals("") || hasAccess(vars1))
        super.serviceInitialized(request,response);
      else
        bdError(response, "AccessTableNoView", vars1.getLanguage());
    } catch (ServletException ex) {
      log4j.error("Error captured: " + ex);
      ex.printStackTrace();
      OBError myError = Utility.translateError(this, null, variables.getLanguage(), ex.getMessage());
      if (strAjax!=null && !strAjax.equals("")) bdErrorAjax(response, myError.getType(), myError.getTitle(), myError.getMessage());
      else if (strHidden!=null && !strHidden.equals("")) bdErrorHidden(response, myError.getType(), myError.getTitle(), myError.getMessage());
      else if (!myError.isConnectionAvailable()) bdErrorConnection(response);
      else if (strPopUp!=null && !strPopUp.equals("")) bdErrorGeneralPopUp(response, myError.getTitle(), myError.getMessage());
      else bdErrorGeneral(response, myError.getTitle(), myError.getMessage());
    } catch (Exception e) {
      log4j.error("Error captured: " + e);
      e.printStackTrace();
      bdErrorGeneral(response, "Error", e.toString());
    }
  }
  
  
  /**
   * Cheks access passing all the parameters
   * @param vars
   * @param type: type of element
   * @param id: id for the element
   * @return true in case it has access false if not
   */
  protected boolean hasGeneralAccess(VariablesSecureApp vars, String type, String id){
    try {
      if (type.equals("W"))
        return hasLevelAccess(vars,AccessData.selectAccessLevel(this, type, id))
            && AccessData.selectAccess(this, vars.getRole(), "TABLE", id).equals("0") 
            && !AccessData.selectAccess(this, vars.getRole(), type, id).equals("0");
      else if (type.equals("S")||type.equals("C")||type.equals("P"))
        return true;
      else 
        return hasLevelAccess(vars,AccessData.selectAccessLevel(this, type, id))
            && !AccessData.selectAccess(this, vars.getRole(), type, id).equals("0");
    } catch (Exception e) {
      log4j.error("Error checking access: " + e.toString());
      return false;
    }
  
  }
  /**
   * Checks if the user has access to the window
   * */
  private boolean hasAccess(VariablesSecureApp vars) {
    try {
      if (classInfo==null || classInfo.id.equals("") || classInfo.type.equals(""))
        return true;
     return hasGeneralAccess(vars, classInfo.type, classInfo.id);
      
    } catch(Exception e) {
      log4j.error("Error checking access: " + e.toString());
      return false;
    }
  }

  /**
   * Checks if the level access is correct.
   * 
   */
  private boolean hasLevelAccess(VariablesSecureApp vars, String accessLevel) {
    String userLevel = vars.getSessionValue("#User_Level");

    boolean retValue = true;

    if (accessLevel.equals("4") && userLevel.indexOf("S") == -1) retValue = false;
    else if (accessLevel.equals("1") && userLevel.indexOf("O") == -1) retValue = false;
    else if (accessLevel.equals("3") && (!(userLevel.indexOf("C")!=-1 || userLevel.indexOf("O")!=-1)) ) retValue = false;
    else if (accessLevel.equals("6") && (!(userLevel.indexOf("S")!=-1 || userLevel.indexOf("C")!=-1)) ) retValue = false;

    return retValue;
  }
  
  /**
   * Validates if a selected default value is null or empty String
   * @param strValue
   * @param strKey
   * @param strError
   * @throws Exeption 
   * */
  private void validateDefault(String strValue, String strKey, String strError) throws Exception {
	  if(strValue == null || strValue.equals(""))
		  throw new Exception("Unable to read default "+ strError +" for:" + strKey);
  }

  public void logout(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    vars.clearSession(true);
    if (log4j.isDebugEnabled()) log4j.debug("Cerrando session");
    if (!vars.getDBSession().equals("")) SessionLoginData.saveProcessed(this, vars.getUser(), vars.getDBSession());
    
    m_AuthManager.logout(request, response);
  }

  public void setHistoryCommand(HttpServletRequest request, String strCommand) {
    Variables vars = new Variables(request);
    vars.setHistoryCommand(strCommand);
  }

  public void advise(HttpServletResponse response, String strTitulo, String strTexto) throws IOException {
    advise(response, "INFO", strTitulo, strTexto);
  }

  public void advise(HttpServletResponse response, String strTipo, String strTitulo, String strTexto) throws IOException {
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/base/secureApp/Advise").createXmlDocument();

    xmlDocument.setParameter("theme", myTheme);
    xmlDocument.setParameter("ParamTipo", strTipo.toUpperCase());
    xmlDocument.setParameter("ParamTitulo", strTitulo);
    xmlDocument.setParameter("ParamTexto", strTexto);
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

 public void advisePopUp(HttpServletResponse response, String strTitulo, String strTexto) throws IOException {
    advisePopUp(response, "Error", strTitulo, strTexto);
  }

  public void advisePopUp(HttpServletResponse response, String strTipo, String strTitulo, String strTexto) throws IOException {
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/base/secureApp/AdvisePopUp").createXmlDocument();

    xmlDocument.setParameter("theme", myTheme);
    xmlDocument.setParameter("ParamTipo", strTipo.toUpperCase());
    xmlDocument.setParameter("ParamTitulo", strTitulo);
    xmlDocument.setParameter("ParamTexto", strTexto);
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  public void bdError(HttpServletResponse response, String strCode, String strLanguage) throws IOException {
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/base/secureApp/Error").createXmlDocument();

    xmlDocument.setParameter("theme", myTheme);
    xmlDocument.setParameter("ParamTitulo", strCode);
    xmlDocument.setParameter("ParamTexto", Utility.messageBD(this, strCode, strLanguage));
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  public void bdErrorGeneralPopUp(HttpServletResponse response, String strTitle, String strText) throws IOException {
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/base/secureApp/ErrorPopUp").createXmlDocument();

    xmlDocument.setParameter("theme", myTheme);
    xmlDocument.setParameter("ParamTipo", "ERROR");
    xmlDocument.setParameter("ParamTitulo", strTitle);
    xmlDocument.setParameter("ParamTexto", strText);
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  public void bdErrorGeneral(HttpServletResponse response, String strTitle, String strText) throws IOException {
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/base/secureApp/Error").createXmlDocument();

    xmlDocument.setParameter("ParamTitulo", strTitle);
    xmlDocument.setParameter("ParamTexto", strText);
    
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  public void bdErrorConnection(HttpServletResponse response) throws IOException {
    if (log4j.isDebugEnabled()) log4j.debug("Output: Error connection");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/base/secureApp/ErrorConnection").createXmlDocument();

    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  public void bdErrorAjax(HttpServletResponse response, String strType, String strTitle, String strText) throws IOException {
    response.setContentType("text/xml; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n");
    out.println("<xml-structure>\n");
    out.println("  <status>\n");
    out.println("    <type>" + strType + "</type>\n");
    out.println("    <title>" + strTitle + "</title>\n");
    out.println("    <description><![CDATA[" + strText + "]]></description>\n");
    out.println("  </status>\n");
    out.println("</xml-structure>\n");
    out.close();
  }

  public void bdErrorHidden(HttpServletResponse response, String strType, String strTitle, String strText) throws IOException {
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/ad_callouts/CallOut").createXmlDocument();

    StringBuffer resultado = new StringBuffer();
    resultado.append("var calloutName='';\n\n");
    resultado.append("var respuesta = new Array(\n");

    resultado.append("new Array(\"MESSAGE\", \"");
    resultado.append(strText);
    resultado.append("\")");
    resultado.append("\n);");

    xmlDocument.setParameter("array", resultado.toString());
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  public void pageError(HttpServletResponse response) throws IOException {
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/base/secureApp/HtmlError").createXmlDocument();

    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  public void pageErrorPopUp(HttpServletResponse response) throws IOException {
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/base/secureApp/HtmlErrorPopUp").createXmlDocument();

    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  public void whitePage(HttpServletResponse response) throws IOException {
    whitePage(response, "");
  }

  public void whitePage(HttpServletResponse response, String strAlert) throws IOException {
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/base/secureApp/HtmlWhitePage").createXmlDocument();
    if (strAlert==null) strAlert="";
    xmlDocument.setParameter("body", strAlert);

    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  public void printPageClosePopUp(HttpServletResponse response, VariablesSecureApp vars, String path) throws IOException, ServletException {
    if (log4j.isDebugEnabled()) log4j.debug("Output: PopUp Response");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/base/secureApp/PopUp_Response").createXmlDocument();
    xmlDocument.setParameter("language", "LNG_POR_DEFECTO=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("href", path.equals("")?"null":"'" + path + "'");
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  public void printPageClosePopUp(HttpServletResponse response, VariablesSecureApp vars) throws IOException, ServletException {
    printPageClosePopUp(response, vars, "");
  }

  public void printPageClosePopUpWindow(HttpServletResponse response, VariablesSecureApp vars) throws IOException, ServletException {
    if (log4j.isDebugEnabled()) log4j.debug("Output: PopUp Response");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/base/secureApp/PopUp_Close").createXmlDocument();
    xmlDocument.setParameter("language", "LNG_POR_DEFECTO=\"" + vars.getLanguage() + "\";");
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  public void pageErrorCallOut(HttpServletResponse response) throws IOException {
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/base/secureApp/HtmlErrorCallOut").createXmlDocument();

    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  public PeriodicBackground getBackgroundProcess(String id) {
    if (backgroundProcess == null || backgroundProcess.isEmpty()) return null;
    PeriodicBackground bg = backgroundProcess.get(id);
    return bg;
  }
  public void updateBackgroundProcess(String id, PeriodicBackground object) {
    if (backgroundProcess == null) backgroundProcess = new Hashtable<String, PeriodicBackground>();
    backgroundProcess.remove(id);
    backgroundProcess.put(id, object);
  }

  private void readProperties(VariablesSecureApp vars, String strFileProperties) {
    //  Read properties file.
    Properties properties = new Properties();
    try {
      log4j.info("strFileProperties: " + strFileProperties);
      properties.load(new FileInputStream(strFileProperties));
      String javaDateFormat = properties.getProperty("dateFormat.java");
      log4j.info("javaDateFormat: " + javaDateFormat);
      vars.setSessionValue("#AD_JavaDateFormat", javaDateFormat);
      String javaDateTimeFormat = properties.getProperty("dateTimeFormat.java");
      log4j.info("javaDateTimeFormat: " + javaDateTimeFormat);
      vars.setSessionValue("#AD_JavaDateTimeFormat", javaDateTimeFormat);
      String jsDateFormat = properties.getProperty("dateFormat.js");
      log4j.info("jsDateFormat: " + jsDateFormat);
      vars.setSessionValue("#AD_JsDateFormat", jsDateFormat);
      String sqlDateFormat = properties.getProperty("dateFormat.sql");
      log4j.info("sqlDateFormat: " + sqlDateFormat);
      vars.setSessionValue("#AD_SqlDateFormat", sqlDateFormat);
    } catch (IOException e) { 
     // catch possible io errors from readLine()     
     e.printStackTrace();
    }
 }
 
 private void readNumberFormat(VariablesSecureApp vars, String strFormatFile) {    	
    String strNumberFormat = "###,##0.00"; // Default number format
    String strGroupingSeparator = ","; // Default grouping separator
    String strDecimalSeparator = "."; // Default decimal separator
    String strName = "euroInform"; // Name of the format to use    
    try{
      // Reading number format configuration
      DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
      DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
      Document doc = docBuilder.parse (new File(strFormatFile));
      doc.getDocumentElement().normalize();
      NodeList listOfNumbers = doc.getElementsByTagName("Number");
      int totalNumbers = listOfNumbers.getLength();
      for(int s=0; s<totalNumbers; s++) {
        Node NumberNode = listOfNumbers.item(s);
        if(NumberNode.getNodeType() == Node.ELEMENT_NODE){
          Element NumberElement = (Element)NumberNode;
          String strNumberName = NumberElement.getAttributes().getNamedItem("name").getNodeValue();
          if(strNumberName.equals(strName)) {
            strDecimalSeparator = NumberElement.getAttributes().getNamedItem("decimal").getNodeValue();
            strGroupingSeparator = NumberElement.getAttributes().getNamedItem("grouping").getNodeValue();
            break;
          }
        }
      }          
    } catch(Exception e) {
      log4j.error(e.getMessage());
    }
    vars.setSessionValue("#AD_ReportNumberFormat", strNumberFormat);
    vars.setSessionValue("#AD_ReportGroupingSeparator", strGroupingSeparator);
    vars.setSessionValue("#AD_ReportDecimalSeparator", strDecimalSeparator);    
 }
 
  private void saveLoginBD(HttpServletRequest request, VariablesSecureApp vars, String strCliente, String strOrganizacion) throws ServletException {
    SessionLogin sl = new SessionLogin(request, strCliente, strOrganizacion, vars.getSessionValue("#AD_User_ID"));
    sl.save(this);
    vars.setSessionValue("#AD_Session_ID", sl.getSessionID());
  }

  public void renderJR(VariablesSecureApp variables, HttpServletResponse response, String strReportName, String strOutputType, HashMap<String, Object> designParameters, FieldProvider[] data, Map<Object, Object> exportParameters) throws ServletException{
  
    if (strReportName == null || strReportName.equals("")) strReportName = PrintJRData.getReportName(this, classInfo.id);

    String strAttach = strFTPDirectory + "/284-" +classInfo.id;
    
    String strLanguage = variables.getLanguage();
    Locale locLocale = new Locale(strLanguage.substring(0,2),strLanguage.substring(3,5));
    
    if (strBaseDesignPath.endsWith("/")) strDefaultDesignPath = strDefaultDesignPath.substring(0, strDefaultDesignPath.length()-1);
    String strNewAddBase = strDefaultDesignPath;
    String strFinal = strBaseDesignPath;
    if (!strLanguage.equals("") && !strLanguage.equals("en_US")) strNewAddBase = strLanguage;
    if (!strFinal.endsWith("/" + strNewAddBase)) strFinal += "/" + strNewAddBase;
    String strBaseDesign = prefix + "/" + strFinal;
    
    strReportName = Replace.replace(Replace.replace(strReportName,"@basedesign@",strBaseDesign),"@attach@",strAttach);
    String strFileName = strReportName.substring(strReportName.lastIndexOf("/")+1);

    ServletOutputStream os = null;
    try {
      JasperDesign jasperDesign= JRXmlLoader.load(strReportName);
      JasperReport jasperReport= JasperCompileManager.compileReport(jasperDesign);
      if (designParameters == null) designParameters = new HashMap<String, Object>();

      Boolean pagination = true;
      if (strOutputType.equals("pdf")) pagination = false;

      designParameters.put("IS_IGNORE_PAGINATION",  pagination );
      designParameters.put("BASE_WEB", strReplaceWithFull );
      designParameters.put("BASE_DESIGN", strBaseDesign);
      designParameters.put("ATTACH", strAttach);
      designParameters.put("USER_CLIENT", Utility.getContext(this, variables, "#User_Client", ""));
      designParameters.put("USER_ORG", Utility.getContext(this, variables, "#User_Org", ""));
      designParameters.put("LANGUAGE", strLanguage);
      designParameters.put("LOCALE", locLocale);
      
      DecimalFormatSymbols dfs = new DecimalFormatSymbols();
      dfs.setDecimalSeparator(variables.getSessionValue("#AD_ReportDecimalSeparator").charAt(0));
      dfs.setGroupingSeparator(variables.getSessionValue("#AD_ReportGroupingSeparator").charAt(0));
      DecimalFormat NumberFormat = new DecimalFormat(variables.getSessionValue("#AD_ReportNumberFormat"), dfs);
      designParameters.put("NUMBERFORMAT", NumberFormat);
      
      if (log4j.isDebugEnabled()) log4j.debug("creating the format factory: " + variables.getJavaDateFormat());
      JRFormatFactory jrFormatFactory = new JRFormatFactory();
      jrFormatFactory.setDatePattern(variables.getJavaDateFormat());
      designParameters.put(JRParameter.REPORT_FORMAT_FACTORY, jrFormatFactory);

      JasperPrint jasperPrint;
      Connection con = null;
      try {
        con = getTransactionConnection();
        if (data != null) {
          designParameters.put("REPORT_CONNECTION", con);
          jasperPrint = JasperFillManager.fillReport(jasperReport, designParameters, new JRFieldProviderDataSource(data, variables.getJavaDateFormat()));
        } else {
          jasperPrint = JasperFillManager.fillReport(jasperReport, designParameters, con);
        }
      } catch (Exception e){
        throw new ServletException(e.getMessage());
      } finally {
        releaseRollbackConnection(con);
      }

      os = response.getOutputStream();
      if (exportParameters == null) exportParameters = new HashMap<Object, Object>();
      if (strOutputType == null || strOutputType.equals("")) strOutputType = "html";
      if (strOutputType.equals("html")){
        if (log4j.isDebugEnabled()) log4j.debug("JR: Print HTML");
        response.setHeader( "Content-disposition", "inline" + "; filename=" + strFileName + "." +strOutputType);
        JRHtmlExporter exporter = new JRHtmlExporter();
        exportParameters.put(JRHtmlExporterParameter.JASPER_PRINT, jasperPrint);
        exportParameters.put(JRHtmlExporterParameter.IS_USING_IMAGES_TO_ALIGN, Boolean.FALSE);
        exportParameters.put(JRHtmlExporterParameter.SIZE_UNIT, JRHtmlExporterParameter.SIZE_UNIT_POINT);
        exportParameters.put(JRHtmlExporterParameter.OUTPUT_STREAM, os);
        exporter.setParameters(exportParameters);
        exporter.exportReport();
      } else if (strOutputType.equals("pdf")){
        response.setContentType("application/pdf");
        response.setHeader( "Content-disposition", "attachment" + "; filename=" + strFileName + "." +strOutputType);
        JasperExportManager.exportReportToPdfStream(jasperPrint, os);
      } else if (strOutputType.equals("xls")){
        response.setContentType("application/vnd.ms-excel");
        response.setHeader( "Content-disposition", "attachment" + "; filename=" + strFileName + "." +strOutputType);
        JExcelApiExporter exporter = new JExcelApiExporter();
        exportParameters.put(JRExporterParameter.JASPER_PRINT, jasperPrint);
        exportParameters.put(JRExporterParameter.OUTPUT_STREAM, os);
        exportParameters.put(JExcelApiExporterParameter.IS_ONE_PAGE_PER_SHEET, Boolean.FALSE);
        exportParameters.put(JExcelApiExporterParameter.IS_REMOVE_EMPTY_SPACE_BETWEEN_ROWS, Boolean.TRUE);
    
        exporter.setParameters(exportParameters);
        exporter.exportReport();
      
      } else {
        throw new ServletException("Output format no supported");
      }
    } catch (JRException e) {
      if (log4j.isDebugEnabled()) log4j.debug("JR: Error: " + e);
      e.printStackTrace();
      throw new ServletException(e.getMessage());
    } catch (Exception e) {
      throw new ServletException(e.getMessage());
    } finally {
      try {
        os.close();
      } catch (Exception e) { }
    }
  }

  public String getServletInfo() {
    return "This servlet add some functions (autentication, privileges, application menu, ...) over HttpBaseServlet";
  }
}
