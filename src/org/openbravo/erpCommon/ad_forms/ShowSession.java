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
package org.openbravo.erpCommon.ad_forms;

import org.openbravo.erpCommon.businessUtility.WindowTabs;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.xmlEngine.XmlDocument;
import org.openbravo.erpCommon.utility.*;
import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

import java.util.*;


public class ShowSession extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;
  protected static final String windowId = "0";
  protected static final String tableLevel = "2";

  public void doPost (HttpServletRequest request, HttpServletResponse response) throws IOException,ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (!Utility.hasFormAccess(this, vars, "", "org.openbravo.erpCommon.ad_forms.ShowSession")) {
      bdError(response, "AccessTableNoView", vars.getLanguage());
      return;
    }

    if (vars.commandIn("REMOVE")) {
      String preferences = vars.getRequestGlobalVariable("inpPreference", "ShowSession|preferences");
      String global = vars.getRequestGlobalVariable("inpGlobal", "ShowSession|global");
      String accounting = vars.getRequestGlobalVariable("inpAccounting", "ShowSession|accounting");
      String windowG = vars.getRequestGlobalVariable("inpWindowGlobal", "ShowSession|windowGlobal");
      String window = vars.getRequestGlobalVariable("inpWindow", "ShowSession|window");
      String strSessionValue = vars.getRequiredStringParameter("inpSessionValue");
      vars.removeSessionValue(strSessionValue);
      printPageDataSheet(request, response, vars, preferences, global, accounting, windowG, window);
    } else if (vars.commandIn("FIND")) {
      String preferences = vars.getRequestGlobalVariable("inpPreference", "ShowSession|preferences");
      String global = vars.getRequestGlobalVariable("inpGlobal", "ShowSession|global");
      String accounting = vars.getRequestGlobalVariable("inpAccounting", "ShowSession|accounting");
      String windowG = vars.getRequestGlobalVariable("inpWindowGlobal", "ShowSession|windowGlobal");
      String window = vars.getRequestGlobalVariable("inpWindow", "ShowSession|window");
      printPageDataSheet(request, response, vars, preferences, global, accounting, windowG, window);
    } else if (vars.commandIn("SAVE_NEW")) {
      String strNombre = vars.getRequiredStringParameter("inpNombreVariable");
      String strValor = vars.getStringParameter("inpValorVariable");
      vars.setSessionValue(strNombre, strValor);
      response.sendRedirect(strDireccion + request.getServletPath());
    } else if(vars.commandIn("SESSION")) {
      String preferences = vars.getGlobalVariable("inpPreference", "ShowSession|preferences", "Y");
      String global = vars.getGlobalVariable("inpGlobal", "ShowSession|global", "Y");
      String accounting = vars.getGlobalVariable("inpAccounting", "ShowSession|accounting", "Y");
      String windowG = vars.getGlobalVariable("inpWindowGlobal", "ShowSession|windowGlobal", "Y");
      String window = vars.getGlobalVariable("inpWindow", "ShowSession|window", "0");
      printPageDataSheet(request, response, vars, preferences, global, accounting, windowG, window);
    } else if (vars.commandIn("SAVE_PREFERENCES")) {
      String strTranslate = vars.getStringParameter("inpTranslate", "N");
      String strAccounting = vars.getStringParameter("inpAccounting", "N");
      String strFecha = vars.getStringParameter("inpFecha");
      String strTest = vars.getStringParameter("inpTest", "N");
      String strRecordRange = vars.getGlobalVariable("inpRecordRange", "#RecordRange");
      String strRecordRangeInfo = vars.getGlobalVariable("inpRecordRangeInfo", "#RecordRangeInfo");
      String strTheme = vars.getGlobalVariable("inpTheme", "#Theme");
      String strTransactionalRange = vars.getGlobalVariable("inpTransactionalRange", "#Transactional$Range");
      vars.setSessionValue("#Date", strFecha);
      vars.setSessionValue("#ShowTrl", strTranslate);
      String strPreference = ShowSessionData.selectPreference(this, Utility.getContext(this, vars, "#User_Client", "ShowSession"), Utility.getContext(this, vars, "#User_Org", "ShowSession"), vars.getUser(), "ShowTrl");
      ShowSessionData.updateRange(this, vars.getUser(), strRecordRange, strRecordRangeInfo, strTransactionalRange, strTheme);
      if (!strPreference.equals("")) ShowSessionData.update(this, vars.getUser(), strTranslate, strPreference);
      else {
        strPreference = SequenceIdData.getSequence(this, "AD_Preference", vars.getClient());
        ShowSessionData.insert(this, strPreference, vars.getClient(), vars.getOrg(), vars.getUser(), "ShowTrl", strTranslate);
      }
      vars.setSessionValue("#ShowAcct", strAccounting);
      strPreference = ShowSessionData.selectPreference(this, Utility.getContext(this, vars, "#User_Client", "ShowSession"), Utility.getContext(this, vars, "#User_Org", "ShowSession"), vars.getUser(), "ShowAcct");
      if (!strPreference.equals("")) ShowSessionData.update(this, vars.getUser(), strAccounting, strPreference);
      else {
        strPreference = SequenceIdData.getSequence(this, "AD_Preference", vars.getClient());
        ShowSessionData.insert(this, strPreference, vars.getClient(), vars.getOrg(), vars.getUser(), "ShowAcct", strAccounting);
      }
      vars.setSessionValue("#ShowTest", strTest);
      strPreference = ShowSessionData.selectPreference(this, Utility.getContext(this, vars, "#User_Client", "ShowSession"), Utility.getContext(this, vars, "#User_Org", "ShowSession"), vars.getUser(), "ShowTest");
      ShowSessionData.updateRange(this, vars.getUser(), strRecordRange, strRecordRangeInfo, strTransactionalRange, strTheme);
      if (!strPreference.equals("")) ShowSessionData.update(this, vars.getUser(), strTest, strPreference);
      else {
        strPreference = SequenceIdData.getSequence(this, "AD_Preference", vars.getClient());
        ShowSessionData.insert(this, strPreference, vars.getClient(), vars.getOrg(), vars.getUser(), "ShowTest", strTest);
      }
      response.sendRedirect(strDireccion + request.getServletPath());
    } else {
      printPagePreferences(response, vars);
    }
  }

  boolean existsWindow(Vector<Object> windows, String windowId) {
    if (windows.size()==0) return false;
    for (int i=0;i<windows.size();i++) {
      String aux = (String)windows.elementAt(i);
      if (aux.equals(windowId)) return true;
    }
    return false;
  }

  String windowName(ShowSessionData[] windows, String windowId) {
    if (windows==null || windowId==null || windowId.equals("")) return "";
    for (int i=0;i<windows.length;i++) {
      if (windows[i].id.equals(windowId)) return windows[i].name;
    }
    return "";
  }

  ShowSessionStructureData[] orderStructure(ShowSessionStructureData[] data, ShowSessionData[] windows, boolean preferences, boolean global, boolean accounting, boolean windowGlobal, String window) {
    ShowSessionStructureData[] resData=null;
    try {
      Vector<Object> vecPreferences = new Vector<Object>();
      if (preferences && (window.equals("") || window.equals("0"))) {
        for (int i=0;i<data.length;i++) {
          if (data[i].isPreference && (data[i].window==null || data[i].window.equals(""))) {
            boolean insertado=false;
            data[i].window="";
            data[i].windowName="";
            for (int j=0;j<vecPreferences.size() && !insertado;j++) {
              ShowSessionStructureData element = (ShowSessionStructureData) vecPreferences.elementAt(j);
              if (element.name.compareTo(data[i].name)>=0) {
                vecPreferences.insertElementAt(data[i], j);
                insertado=true;
              }
            }
            if (!insertado) {
              vecPreferences.addElement(data[i]);
            }
          }
        }
      }
      Vector<Object> vecPreferencesW = new Vector<Object>();
      if (preferences && !window.equals("")) {
        for (int i=0;i<data.length;i++) {
          if (data[i].isPreference && (data[i].window!=null && !data[i].window.equals("") && (data[i].window.equals(window) || window.equals("0")))) {
            boolean insertado = false;
            data[i].windowName = windowName(windows, data[i].window);
            for (int j=0;j<vecPreferencesW.size() && !insertado;j++) {
              ShowSessionStructureData element = (ShowSessionStructureData) vecPreferencesW.elementAt(j);
              if (element.windowName.compareTo(data[i].windowName)>0) {
                vecPreferencesW.insertElementAt(data[i], j);
                insertado=true;
              } else if (element.windowName.compareTo(data[i].windowName)==0) {
                if (element.name.compareTo(data[i].name)>=0) {
                  vecPreferencesW.insertElementAt(data[i], j);
                  insertado=true;
                }
              }
            }
            if (!insertado) {
              vecPreferencesW.addElement(data[i]);
            }
          }
        }
      }

      Vector<Object> vecGlobal = new Vector<Object>();
      if (global) {
        for (int i=0;i<data.length;i++) {
          if (data[i].isGlobal) {
            boolean insertado = false;
            data[i].window="";
            data[i].windowName="";
            for (int j=0;j<vecGlobal.size() && !insertado;j++) {
              ShowSessionStructureData element = (ShowSessionStructureData) vecGlobal.elementAt(j);
              if (element.name.compareTo(data[i].name)>=0) {
                vecGlobal.insertElementAt(data[i], j);
                insertado=true;
              }
            }
            if (!insertado) {
              vecGlobal.addElement(data[i]);
            }
          }
        }
      }

      Vector<Object> vecAccounting = new Vector<Object>();
      if (accounting) {
        for (int i=0;i<data.length;i++) {
          if (data[i].isAccounting) {
            boolean insertado = false;
            data[i].window="";
            data[i].windowName="";
            for (int j=0;j<vecAccounting.size() && !insertado;j++) {
              ShowSessionStructureData element = (ShowSessionStructureData) vecAccounting.elementAt(j);
              if (element.name.compareTo(data[i].name)>=0) {
                vecAccounting.insertElementAt(data[i], j);
                insertado=true;
              }
            }
            if (!insertado) {
              vecAccounting.addElement(data[i]);
            }
          }
        }
      }

      Vector<Object> vecWindowG = new Vector<Object>();
      if (windowGlobal) {
        for (int i=0;i<data.length;i++) {
          if (!data[i].isAccounting && !data[i].isGlobal && !data[i].isPreference && (data[i].window==null || data[i].window.equals(""))) {
            boolean insertado = false;
            data[i].window="";
            data[i].windowName="";
            for (int j=0;j<vecWindowG.size() && !insertado;j++) {
              ShowSessionStructureData element = (ShowSessionStructureData) vecWindowG.elementAt(j);
              if (element.name.compareTo(data[i].name)>=0) {
                vecWindowG.insertElementAt(data[i], j);
                insertado=true;
              }
            }
            if (!insertado) {
              vecWindowG.addElement(data[i]);
            }
          }
        }
      }

      Vector<Object> vecWindow = new Vector<Object>();
      if (!window.equals("")) {
        for (int i=0;i<data.length;i++) {
          if (!data[i].isAccounting && !data[i].isGlobal && !data[i].isPreference && (data[i].window!=null && !data[i].window.equals("") && (data[i].window.equals(window) || window.equals("0")))) {
            boolean insertado = false;
            data[i].windowName = windowName(windows, data[i].window);
            for (int j=0;j<vecWindow.size() && !insertado;j++) {
              ShowSessionStructureData element = (ShowSessionStructureData) vecWindow.elementAt(j);
              if (element.windowName.compareTo(data[i].windowName)>0) {
                vecWindow.insertElementAt(data[i], j);
                insertado=true;
              } else if (element.windowName.compareTo(data[i].windowName)==0) {
                if (element.name.compareTo(data[i].name)>=0) {
                  vecWindow.insertElementAt(data[i], j);
                  insertado=true;
                }
              }
            }
            if (!insertado) {
              vecWindow.addElement(data[i]);
            }
          }
        }
      }

      Vector<Object> vecCompleto = new Vector<Object>();
      for (int i=0;i<vecPreferences.size();i++) {
        vecCompleto.addElement(vecPreferences.elementAt(i));
      }
      for (int i=0;i<vecPreferencesW.size();i++) {
        vecCompleto.addElement(vecPreferencesW.elementAt(i));
      }
      for (int i=0;i<vecGlobal.size();i++) {
        vecCompleto.addElement(vecGlobal.elementAt(i));
      }
      for (int i=0;i<vecAccounting.size();i++) {
        vecCompleto.addElement(vecAccounting.elementAt(i));
      }
      for (int i=0;i<vecWindowG.size();i++) {
        vecCompleto.addElement(vecWindowG.elementAt(i));
      }
      for (int i=0;i<vecWindow.size();i++) {
        vecCompleto.addElement(vecWindow.elementAt(i));
      }
      resData = new ShowSessionStructureData[vecCompleto.size()];
      vecCompleto.copyInto(resData);
      if (log4j.isDebugEnabled()) log4j.debug("ShowSession - orderStructure - Total: " + resData.length + "-" + resData[0].name);
    } catch (Exception e) {
      log4j.error("ShowSession - orderStructure - Ordering Session variables error " + e);
    }
    return resData;
  }

  ShowSessionStructureData[] compoundSession(HttpServletRequest request, VariablesSecureApp vars, Vector<Object> windows) {
    if (log4j.isDebugEnabled()) log4j.debug("ShowSession - compoundSession - view session");
    ShowSessionStructureData[] data=null;
    HttpSession session = request.getSession(true);
    Vector<Object> texto = new Vector<Object>();
    try {
      String sessionName;
      Enumeration<?> e = session.getAttributeNames();
      while (e.hasMoreElements()) {
        sessionName = (String)e.nextElement();
        if (log4j.isDebugEnabled()) log4j.debug("ShowSession - compoundSession - session name: " + sessionName);
        String realName = sessionName;
        ShowSessionStructureData data1 = new ShowSessionStructureData();
        if (realName.startsWith("P|")) {
          data1.isPreference = true;
          realName = realName.substring(2);
        }
        if (realName.startsWith("$")) {
          data1.isAccounting = true;
          realName = realName.substring(1);
        }
        if (realName.startsWith("#")) {
          data1.isGlobal = true;
          realName = realName.substring(1);
        }
        int pos=realName.indexOf("|");
        if (pos!=-1) {
          data1.window = realName.substring(0,pos);
          if (!existsWindow(windows, data1.window)) windows.addElement(data1.window);
          realName = realName.substring(pos+1);
        }
        
        data1.completeName = sessionName;
        data1.name = realName;
        data1.value = vars.getSessionValue(sessionName);
        texto.addElement(data1);
      }
      data = new ShowSessionStructureData[texto.size()];
      texto.copyInto(data);
    } catch (Exception e) {
      log4j.error("ShowSession - compoundSession - Session variables error " + e);
    }
    return data;
  }

  void printPageDataSheet(HttpServletRequest request, HttpServletResponse response, VariablesSecureApp vars, String preferences, String global, String accounting, String windowG, String window) throws IOException, ServletException {
    if (log4j.isDebugEnabled()) log4j.debug("ShowSession - printPageDataSheet - Output: data sheet");
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    Vector<Object> windows = new Vector<Object>();
    ShowSessionStructureData[] data = compoundSession(request, vars, windows);
    XmlDocument xmlDocument;
    if (data==null || data.length==0) {
      String[] discard = {"sectionDetail"};
      xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/ad_forms/ShowSession", discard).createXmlDocument();
    } else {
      xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/ad_forms/ShowSession").createXmlDocument();
    }
    StringBuffer strWindows = new StringBuffer();
    Vector<Object> vecWindows = new Vector<Object>();
    if (windows.size()!=0) {
      strWindows.append("(");
      for (int i=0;i<windows.size();i++) {
        String aux = (String)windows.elementAt(i);
        try {
          Integer.valueOf(aux).intValue(); // To catch illegal number conversion
          if (i>0) strWindows.append(", ");
          strWindows.append(aux);
        } catch (Exception e) {
          ShowSessionData d = new ShowSessionData();
          d.id=aux;
          d.name=aux;
          vecWindows.addElement(d);
        }
      }
      strWindows.append(")");
    }
    ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "ShowSession", false, "", "", "",false, "ad_forms",  strReplaceWith, false,  true);
    toolbar.prepareSimpleToolBarTemplate();
    xmlDocument.setParameter("toolbar", toolbar.toString());
    xmlDocument.setParameter("direction", "var baseDirection = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("language", "LNG_POR_DEFECTO=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("preference", preferences);
    xmlDocument.setParameter("accounting", accounting);
    xmlDocument.setParameter("global", global);
    xmlDocument.setParameter("windowGlobal", windowG);
    xmlDocument.setParameter("window", window);
    try {
      WindowTabs tabs = new WindowTabs(this, vars, "org.openbravo.erpCommon.ad_forms.ShowSession");
      xmlDocument.setParameter("theme", vars.getTheme());
      NavigationBar nav = new NavigationBar(this, vars.getLanguage(), "ShowSession.html", classInfo.id, classInfo.type, strReplaceWith, tabs.breadcrumb());
      xmlDocument.setParameter("navigationBar", nav.toString());
      LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(), "ShowSession.html", strReplaceWith);
      xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    {
      OBError myMessage = vars.getMessage("ShowSession");
      vars.removeMessage("ShowSession");
      if (myMessage!=null) {
        xmlDocument.setParameter("messageType", myMessage.getType());
        xmlDocument.setParameter("messageTitle", myMessage.getTitle());
        xmlDocument.setParameter("messageMessage", myMessage.getMessage());
      }
    }
    ShowSessionData[] windowsData = (vars.getLanguage().equals("en_US")?ShowSessionData.select(this, strWindows.toString()):ShowSessionData.selectTrl(this, strWindows.toString(), vars.getLanguage()));
    {
      Vector<Object> v=new Vector<Object>();
      ShowSessionData d = new ShowSessionData();
      d.id="0";
      d.name="All";
      v.addElement(d);
      for (int i=0;i<windowsData.length;i++) {
        v.addElement(windowsData[i]);
      }
      for (int i=0;i<vecWindows.size();i++) {
        v.addElement(vecWindows.elementAt(i));
      }
      windowsData = new ShowSessionData[v.size()];
      v.copyInto(windowsData);
    }
    data = orderStructure(data, windowsData, preferences.equals("Y"), global.equals("Y"), accounting.equals("Y"), windowG.equals("Y"), window);
    xmlDocument.setData("windows", windowsData);
    xmlDocument.setData("structure1", data);

    out.println(xmlDocument.print());
    out.close();
  }

  void printPagePreferences(HttpServletResponse response, VariablesSecureApp vars) throws IOException, ServletException {
    if (log4j.isDebugEnabled()) log4j.debug("ShowSession - printPagePreferences - Output: preferences");
    
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/ad_forms/ShowSessionPreferences").createXmlDocument();
    
    xmlDocument.setParameter("calendar", vars.getLanguage().substring(0,2));
    xmlDocument.setParameter("language", "LNG_POR_DEFECTO=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("direction", "var baseDirection = \"" + strReplaceWith + "/\";\n");

    xmlDocument.setParameter("translate", vars.getSessionValue("#ShowTrl", "N"));
    xmlDocument.setParameter("accounting", vars.getSessionValue("#ShowAcct", "N"));
    xmlDocument.setParameter("fecha", vars.getSessionValue("#Date", ""));
    xmlDocument.setParameter("fechadisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("fechasaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("transactionalRange", vars.getSessionValue("#Transactional$Range", ""));
    xmlDocument.setParameter("password", "N");
    xmlDocument.setParameter("test", vars.getSessionValue("#ShowTest", "N"));
    xmlDocument.setParameter("recordRange", vars.getSessionValue("#RecordRange"));
    xmlDocument.setParameter("recordRangeInfo", vars.getSessionValue("#RecordRangeInfo"));
    xmlDocument.setParameter("info", getInfo(vars));
    xmlDocument.setParameter("theme", vars.getTheme());

    ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "ShowSession", false, "", "", "",false, "ad_forms",  strReplaceWith, false,  true);
    toolbar.prepareSimpleToolBarTemplate();
    xmlDocument.setParameter("toolbar", toolbar.toString());
    try {
      WindowTabs tabs = new WindowTabs(this, vars, "org.openbravo.erpCommon.ad_forms.ShowSession");
      xmlDocument.setParameter("theme", vars.getTheme());
      NavigationBar nav = new NavigationBar(this, vars.getLanguage(), "ShowSessionPreferences.html", classInfo.id, classInfo.type, strReplaceWith, tabs.breadcrumb());
      xmlDocument.setParameter("navigationBar", nav.toString());
      LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(), "ShowSessionPreferences.html", strReplaceWith);
      xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    {
      OBError myMessage = vars.getMessage("ShowSession");
      vars.removeMessage("ShowSession");
      if (myMessage!=null) {
        xmlDocument.setParameter("messageType", myMessage.getType());
        xmlDocument.setParameter("messageTitle", myMessage.getTitle());
        xmlDocument.setParameter("messageMessage", myMessage.getMessage());
      }
    }

    ComboTableData comboTableData = null;
    try {
      comboTableData = new ComboTableData(vars, this, "LIST", "Theme", "800102", "", Utility.getContext(this, vars, "#User_Org", "ShowSession"), Utility.getContext(this, vars, "#User_Client", "ShowSession"), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, "ShowSession", "");
      xmlDocument.setData("reportTheme", "liststructure", comboTableData.select(true));
    } catch (Exception ex) {}
    comboTableData = null;

    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  private String getInfo(VariablesSecureApp vars) throws ServletException {
    StringBuffer script = new StringBuffer();
    script.append(Utility.messageBD(this, "User", vars.getLanguage())).append(": ").append(ShowSessionData.usuario(this, vars.getUser())).append("\n");
    script.append(Utility.messageBD(this, "Role", vars.getLanguage())).append(": ").append(ShowSessionData.rol(this, vars.getRole())).append("\n");
    script.append(Utility.messageBD(this, "Client", vars.getLanguage())).append(": ").append(ShowSessionData.cliente(this, vars.getClient())).append("\n");
    script.append(Utility.messageBD(this, "Org", vars.getLanguage())).append(": ").append(ShowSessionData.organizacion(this, vars.getOrg())).append("\n");
    script.append(Utility.messageBD(this, "Web", vars.getLanguage())).append(": ").append(strReplaceWith).append("\n");
    script.append(Utility.messageBD(this, "DB", vars.getLanguage())).append(": ").append(strBBDD).append("\n");
    script.append(Utility.messageBD(this, "RecordRange", vars.getLanguage())).append(": ").append(vars.getSessionValue("#RecordRange")).append("\n");
    script.append(Utility.messageBD(this, "SearchsRecordRange", vars.getLanguage())).append(": ").append(vars.getSessionValue("#RecordRangeInfo")).append("\n");
    if (strVersion!=null && !strVersion.equals("")) script.append(Utility.messageBD(this, "SourceVersion", vars.getLanguage())).append(": ").append(strVersion).append("\n");
    if (strParentVersion!=null && !strParentVersion.equals("")) script.append(Utility.messageBD(this, "VerticalSourceVersion", vars.getLanguage())).append(": ").append(strParentVersion).append("\n");
    String strBBDDVersion = ShowSessionData.versionBBDD(this);
    if (strBBDDVersion!=null && !strBBDDVersion.equals("")) script.append(Utility.messageBD(this, "DBVersion", vars.getLanguage())).append(": ").append(strBBDDVersion).append("\n");
    String strBBDDParentVersion = ShowSessionData.versionVerticalBBDD(this);
    if (strBBDDParentVersion!=null && !strBBDDParentVersion.equals("")) script.append(Utility.messageBD(this, "VerticalDBVersion", vars.getLanguage())).append(": ").append(strBBDDParentVersion).append("\n");
    script.append(Utility.messageBD(this, "JavaVM", vars.getLanguage())).append(": ").append(System.getProperty("java.vm.name")).append("\n");
    script.append(Utility.messageBD(this, "VersionJavaVM", vars.getLanguage())).append(": ").append(System.getProperty("java.vm.version")).append("\n");
    script.append(Utility.messageBD(this, "SystemLanguage", vars.getLanguage())).append(": ").append(strSystemLanguage).append("\n");
    script.append(Utility.messageBD(this, "JavaTMP", vars.getLanguage())).append(": ").append(System.getProperty("java.io.tmpdir")).append("\n");
    script.append(Utility.messageBD(this, "UserFolder", vars.getLanguage())).append(": ").append(strFileProperties).append("\n");
    script.append(Utility.messageBD(this, "OS", vars.getLanguage())).append(": ").append(System.getProperty("os.name")).append(" ").append(System.getProperty("os.version"));
    script.append(" ").append(System.getProperty("sun.os.patch.level"));
;

    return script.toString();
  }


  public String getServletInfo() {
    return "Servlet ShowSession. This Servlet was made by Wad constructor";
  } // end of getServletInfo() method
}

