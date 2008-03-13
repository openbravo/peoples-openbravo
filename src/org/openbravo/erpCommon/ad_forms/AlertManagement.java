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
 * All portions are Copyright (C) 2007 Openbravo SL 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
*/

package org.openbravo.erpCommon.ad_forms;
import org.openbravo.data.Sqlc;

import org.openbravo.erpCommon.utility.ToolBar;
import org.openbravo.erpCommon.businessUtility.WindowTabs;

import org.openbravo.erpCommon.utility.*;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.xmlEngine.XmlDocument;
import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

import org.openbravo.utils.FormatUtilities;

public class AlertManagement extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void doPost (HttpServletRequest request, HttpServletResponse response) throws IOException,ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);


    if (vars.commandIn("DEFAULT")) {
      String strAlertRuleId=vars.getRequestGlobalVariable("inpAlertRule", "AlertManagement|AlertRule");
      String strActiveFilter=vars.getStringParameter("inpActiveFilter","Y").equals("")?"N":"Y";
      String strFixedFilter=vars.getRequestGlobalVariable("inpFixedFilter", "FixedFilter|AlertRule").equals("")?"N":"Y";
      printPageDataSheet(response, vars, strAlertRuleId, strActiveFilter, strFixedFilter);
    } else if (vars.commandIn("FIND")) {
      String strAlertRuleId=vars.getRequestGlobalVariable("inpAlertRule", "AlertManagement|AlertRule");
      String strActiveFilter=vars.getStringParameter("inpActiveFilter").equals("")?"N":"Y";
      String strFixedFilter=vars.getRequestGlobalVariable("inpFixedFilter", "FixedFilter|AlertRule").equals("")?"N":"Y";
      printPageDataSheet(response, vars, strAlertRuleId, strActiveFilter, strFixedFilter);
    } else if (vars.commandIn("SAVE_EDIT_EDIT")) {
      updateValues(request, vars);
      String strAlertRuleId=vars.getRequestGlobalVariable("inpAlertRule", "AlertManagement|AlertRule");
      String strActiveFilter=vars.getStringParameter("inpActiveFilter").equals("")?"N":"Y";
      String strFixedFilter=vars.getRequestGlobalVariable("inpFixedFilter", "FixedFilter|AlertRule").equals("")?"N":"Y";
      printPageDataSheet(response, vars, strAlertRuleId, strActiveFilter, strFixedFilter);
    } else pageError(response);
  }

  void updateValues(HttpServletRequest request, VariablesSecureApp vars) throws IOException, ServletException {
    String[] strAlertId = request.getParameterValues("strAlertID");
    if (log4j.isDebugEnabled()) log4j.debug("update: alerts"+strAlertId.length);
    for (int i=0; i<strAlertId.length; i++) {
      
      String note = vars.getStringParameter("strNotes"+strAlertId[i]);
      String fixed = vars.getStringParameter("strFixed"+strAlertId[i]).equals("on")?"Y":"N";
      String active = vars.getStringParameter("strActive"+strAlertId[i]).equals("on")?"Y":"N";
      
      if (log4j.isDebugEnabled()) log4j.debug("updating:"+strAlertId[i]+" - fixed:"+fixed+" - active:"+active);
      AlertManagementData.update(this, note, fixed, active, strAlertId[i]);
    }
  }
                    
  void printPageDataSheet(HttpServletResponse response, VariablesSecureApp vars, String strAlertRuleId, String strActiveFilter, String strFixedFilter) throws IOException, ServletException {
    if (log4j.isDebugEnabled()) log4j.debug("Output: dataSheet");
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    XmlDocument xmlDocument=xmlEngine.readXmlTemplate("org/openbravo/erpCommon/ad_forms/AlertManagement").createXmlDocument();
    

    AlertManagementData[] rules = AlertManagementData.selectAlertRules(this, vars.getLanguage(),vars.getUser(), vars.getRole(),strAlertRuleId);
    AlertManagementData[][] alerts=new AlertManagementData[rules.length][];

    
    if (rules!=null && rules.length!=0) {
      for (int i=0; i<rules.length; i++) {
        String strWhere = new UsedByLink().getWhereClause(vars,"",rules[i].filterclause);
        try {
          AlertManagementData[] data = AlertManagementData.select(this, strActiveFilter, strFixedFilter, 
                                                                   Utility.getContext(this, vars, "#User_Client", ""), 
                                                                   Utility.getContext(this, vars, "#User_Org", ""), 
                                                                   vars.getUser(), vars.getRole(), rules[i].adAlertruleId,
                                                                   strWhere);
          
          if (data==null || data.length==0) 
            rules[i].display = "none";
          else 
          for (int j=0; j<data.length; j++) {
            data[j].url = FormatUtilities.replace(data[j].windowname) + "/" + FormatUtilities.replace(data[j].tabname) + "_Edition.html";
            data[j].columnname = "inp"+Sqlc.TransformaNombreColumna(data[j].columnname);  
          }
          alerts[i] = data;
        } catch (Exception ex) {
          log4j.error("Error in Alert Query, alertRule="+rules[i].adAlertruleId+" error:"+ex.toString());
        }       
      }
    }
    
    
    xmlDocument.setParameter("adAlertruleId", strAlertRuleId);
    /*try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR", "AD_AlertRule_ID", "", "", Utility.getContext(this, vars, "#User_Org", "AlertManagement"), Utility.getContext(this, vars, "#User_Client", "AlertManagement"), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, "AlertManagement", strAlertRuleId);
      xmlDocument.setData("reportAD_ALERTRULE","liststructure", comboTableData.select(false));
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }*/
    
    xmlDocument.setData("reportAD_ALERTRULE","liststructure", AlertManagementData.selectComboAlertRules(this, vars.getLanguage(),vars.getUser(), vars.getRole()));
    
    xmlDocument.setData("structure1", rules);
    xmlDocument.setDataArray("reportAlertManagement","structure2", alerts);
    xmlDocument.setParameter("active",strActiveFilter);
    xmlDocument.setParameter("fixed",strFixedFilter);
	xmlDocument.setParameter("direction", "var baseDirection = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("paramLanguage", "LNG_POR_DEFECTO=\"" + vars.getLanguage() + "\";");
    
    ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "AlertManagement", false, "", "", "",false, "ad_forms",  strReplaceWith, false,  true);
    toolbar.prepareSimpleSaveToolBarTemplate();
    xmlDocument.setParameter("toolbar", toolbar.toString()); 
    
    // New interface paramenters
      try {
        WindowTabs tabs = new WindowTabs(this, vars, "org.openbravo.erpCommon.ad_forms.AlertManagement");
        xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
        xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
        xmlDocument.setParameter("childTabContainer", tabs.childTabs());
        xmlDocument.setParameter("theme", vars.getTheme());
        NavigationBar nav = new NavigationBar(this, vars.getLanguage(), "AlertManagement.html", classInfo.id, classInfo.type, strReplaceWith, tabs.breadcrumb());
        xmlDocument.setParameter("navigationBar", nav.toString());
        LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(), "AlertManagement.html", strReplaceWith);
        xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
      } catch (Exception ex) {
        throw new ServletException(ex);
      }
      {
        OBError myMessage = vars.getMessage("AlertManagement");
        vars.removeMessage("AlertManagement");
        if (myMessage!=null) {
          xmlDocument.setParameter("messageType", myMessage.getType());
          xmlDocument.setParameter("messageTitle", myMessage.getTitle());
          xmlDocument.setParameter("messageMessage", myMessage.getMessage());
        }
      }
      
     ////----

    out.println(xmlDocument.print());
    out.close();
  }

}

