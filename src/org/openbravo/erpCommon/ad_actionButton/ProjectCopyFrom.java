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
 * All portions are Copyright (C) 2001-2008 Openbravo SL 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
*/
package org.openbravo.erpCommon.ad_actionButton;

import org.openbravo.erpCommon.utility.SequenceIdData;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.utils.FormatUtilities;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.xmlEngine.XmlDocument;
import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

// imports for transactions
import java.sql.Connection;


public class ProjectCopyFrom extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;
  

  public void init (ServletConfig config) {
    super.init(config);
    boolHist = false;
  }

  public void doPost (HttpServletRequest request, HttpServletResponse response) throws IOException,ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (vars.commandIn("DEFAULT")) {
      String strProcessId = vars.getStringParameter("inpProcessId");
      String strWindow = vars.getStringParameter("inpwindowId");
      String strTab = vars.getStringParameter("inpTabId");
      String strProject = vars.getStringParameter("inpProject", "");
      String strKey = vars.getRequiredGlobalVariable("inpcProjectId", strWindow + "|C_Project_ID");
      printPage(response, vars, strKey, strProject, strWindow, strTab, strProcessId);
    } else if (vars.commandIn("SAVE")) {
      String strWindow = vars.getStringParameter("inpwindowId");
      String strProject = vars.getStringParameter("inpProject");
      String strKey = vars.getRequestGlobalVariable("inpcProjectId", strWindow + "|C_Project_ID");
      String strTab = vars.getStringParameter("inpTabId");
      ActionButtonDefaultData[] tab = ActionButtonDefaultData.windowName(this, strTab);
      String strWindowPath="", strTabName="";
      if (tab!=null && tab.length!=0) {
        strTabName = FormatUtilities.replace(tab[0].name);
        strWindowPath = "../" + FormatUtilities.replace(tab[0].description) + "/" + strTabName + "_Relation.html";
      } else strWindowPath = strDefaultServlet;
      OBError myMessage = processButton(vars, strKey, strProject, strWindow);
      vars.setMessage(strTab, extracted(myMessage));
      printPageClosePopUp(response, vars, strWindowPath);
    } else pageErrorPopUp(response);
  }


  OBError processButton(VariablesSecureApp vars, String strKey, String strProject, String windowId) {
    Connection conn = null;
    OBError myMessage = null;
    if (strProject == null || strProject == ""){
      extracted(myMessage).setType("Error");
      extracted(myMessage).setTitle(Utility.messageBD(this, "Error", vars.getLanguage()));
      extracted(myMessage).setMessage(Utility.messageBD(this, "NoProjectSelected", vars.getLanguage()));
      return extracted(myMessage);
    } 
	    try {
	      conn = this.getTransactionConnection();
	      String projectCategory = ProjectCopyFromData.selectProjectCategory(this, strKey);
	      ProjectSetTypeData[] dataProject = ProjectSetTypeData.selectProject(this, strKey);
	      if (projectCategory.equals("S")){
	        ProjectCopyFromData[] data = ProjectCopyFromData.select(this, strProject);		    
	        String strProjectPhase = "";
	        String strProjectTask = "";
	        for (int i=0;data!=null && i<data.length;i++){
	          strProjectPhase = SequenceIdData.getSequence(this, "C_ProjectPhase", dataProject[0].adClientId);
	          if (!ProjectCopyFromData.hasPhase(this, strKey, data[i].cPhaseId)){
	            try {
  	            if (ProjectCopyFromData.insertProjectPhase(conn, this, strKey, dataProject[0].adClientId, dataProject[0].adOrgId, vars.getUser(), data[i].description, data[i].mProductId, data[i].cPhaseId, strProjectPhase, data[i].help, data[i].name, data[i].qty, data[i].seqno)==1){
  	              ProjectCopyFromData[] data1 = ProjectCopyFromData.selectTask(this, data[i].cProjectphaseId);
  	              for (int j=0;data1!=null && j<data1.length;j++){
  	                  strProjectTask = SequenceIdData.getSequence(this, "C_ProjectTask", dataProject[0].adClientId);
  	                  ProjectCopyFromData.insertProjectTask(conn, this,strProjectTask,data1[j].cTaskId, dataProject[0].adClientId, dataProject[0].adOrgId, vars.getUser(), data1[j].seqno, data1[j].name,data1[j].description, data1[j].help, data1[j].mProductId, strProjectPhase, data1[j].qty);
  	              }
  	            }
	            } catch(ServletException ex) {
	              myMessage = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
	              releaseRollbackConnection(conn);
	              return extracted(myMessage);
	            }
	          }
	        }
	      }else {		        
	    	  ProjectCopyFromData[] dataServ = ProjectCopyFromData.selectServ(this, strProject);
		      String strProjectLine = "";
		      for (int i=0;dataServ!=null && i<dataServ.length;i++){	      
		        strProjectLine = SequenceIdData.getSequence(this, "C_ProjectLine", dataProject[0].adClientId);
		        try {
		          ProjectCopyFromData.insertProjectLine(conn, this, strProjectLine, strKey, dataProject[0].adClientId, dataProject[0].adOrgId, vars.getUser(), dataServ[i].line, dataServ[i].description, dataServ[i].plannedqty, dataServ[i].mProductId, dataServ[i].mProductCategoryId, dataServ[i].productDescription, dataServ[i].productName, dataServ[i].productValue);
		        } catch(ServletException ex) {
		          myMessage = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
		          releaseRollbackConnection(conn);
		          return extracted(myMessage);
		        }
		      }
	      }
	      String strProjectType = ProjectCopyFromData.selectProjectType(this, strProject);
	      String strProjectCategory = "";
	      if (strProjectType == null || strProjectType == ""){
	    	  strProjectCategory = ProjectCopyFromData.selectProjCategory(this, strProject);
	      }else {
	    	  strProjectCategory = ProjectSetTypeData.selectProjectCategory(this, strProjectType);
	      }
	      try {
	        ProjectSetTypeData.update(conn, this, vars.getUser(), strProjectType, strProjectCategory, strKey);
	      } catch(ServletException ex) {
	        myMessage = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
	        releaseRollbackConnection(conn);
	        return extracted(myMessage);
	      }	
	      
	      releaseCommitConnection(conn);
	      myMessage = new OBError();
	      extracted(myMessage).setType("Success");
	      extracted(myMessage).setTitle(Utility.messageBD(this, "Success", vars.getLanguage()));
	      extracted(myMessage).setMessage(Utility.messageBD(this, "ProcessOK", vars.getLanguage()));	      
	    } catch (Exception e) {
	      try {
	        releaseRollbackConnection(conn);
	      } catch (Exception ignored) {}
	      e.printStackTrace();
	      log4j.warn("Rollback in transaction");
	      myMessage = Utility.translateError(this, vars, vars.getLanguage(), "ProcessRunError");
	    }
	    return extracted(myMessage);
  }

  private OBError extracted(OBError myMessage) {
    return myMessage;
  }


  void printPage(HttpServletResponse response, VariablesSecureApp vars, String strKey, String strProject, String windowId, String strTab, String strProcessId)
    throws IOException, ServletException {
      if (log4j.isDebugEnabled()) log4j.debug("Output: Button process Project set Type");

      ActionButtonDefaultData[] data = null;
      String strHelp="", strDescription="";
      if (vars.getLanguage().equals("en_US")) data = ActionButtonDefaultData.select(this, strProcessId);
      else data = ActionButtonDefaultData.selectLanguage(this, vars.getLanguage(), strProcessId);

      if (data!=null && data.length!=0) {
        strDescription = data[0].description;
        strHelp = data[0].help;
      }
      String[] discard = {""};
      if (strHelp.equals("")) discard[0] = new String("helpDiscard");
      XmlDocument xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/ad_actionButton/ProjectCopyFrom", discard).createXmlDocument();
      xmlDocument.setParameter("key", strKey);
      xmlDocument.setParameter("window", windowId);
      xmlDocument.setParameter("tab", strTab);
      xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
      xmlDocument.setParameter("question", Utility.messageBD(this, "StartProcess?", vars.getLanguage()));
      xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
      xmlDocument.setParameter("theme", vars.getTheme());
      xmlDocument.setParameter("description", strDescription);
      xmlDocument.setParameter("help", strHelp);

      xmlDocument.setData("reportcProjectId", "liststructure", ProjectCopyFromData.selectC_Project_ID(this, Utility.getContext(this, vars, "#User_Org", ""), Utility.getContext(this, vars, "#User_Client", ""), strKey, strKey));
      xmlDocument.setParameter("Project", strProject);

      response.setContentType("text/html; charset=UTF-8");
      PrintWriter out = response.getWriter();
      out.println(xmlDocument.print());
      out.close();
    }

  public String getServletInfo() {
    return "Servlet Project set Type";
  } // end of getServletInfo() method
}

