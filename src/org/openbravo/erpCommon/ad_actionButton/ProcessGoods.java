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
 * The Initial Developer of the Original Code is Openbravo SLU
 * All portions are Copyright (C) 2012 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 *************************************************************************
 */

package org.openbravo.erpCommon.ad_actionButton;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.base.filter.IsIDFilter;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.data.FieldProvider;
import org.openbravo.erpCommon.reference.PInstanceProcessData;
import org.openbravo.erpCommon.utility.OBDateUtils;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.ad.process.ProcessInstance;
import org.openbravo.model.ad.ui.Process;
import org.openbravo.model.materialmgmt.transaction.ShipmentInOut;
import org.openbravo.service.db.CallProcess;
import org.openbravo.xmlEngine.XmlDocument;

public class ProcessGoods extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;
  private static final String M_Inout_Post_ID = "109";
  private static final String M_Inout_Table_ID = "319";
  private static final String Goods_Document_Action = "135";
  private static final String Goods_Receipt_Window = "184";

  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (vars.commandIn("DEFAULT")) {
      final String strWindowId = vars.getGlobalVariable("inpwindowId", "ProcessGoods|Window_ID",
          IsIDFilter.instance);
      final String strTabId = vars.getGlobalVariable("inpTabId", "ProcessGoods|Tab_ID",
          IsIDFilter.instance);

      final String strM_Inout_ID = vars.getGlobalVariable("inpmInoutId", strWindowId
          + "|M_Inout_ID", "", IsIDFilter.instance);

      final String strdocaction = vars.getStringParameter("inpdocaction");
      final String strProcessing = vars.getStringParameter("inpprocessing", "Y");
      final String strOrg = vars.getRequestGlobalVariable("inpadOrgId", "ProcessGoods|Org_ID",
          IsIDFilter.instance);
      final String strClient = vars.getStringParameter("inpadClientId", IsIDFilter.instance);

      final String strdocstatus = vars.getRequiredStringParameter("inpdocstatus");
      final int accesslevel = 1;

      if ((org.openbravo.erpCommon.utility.WindowAccessData.hasReadOnlyAccess(this, vars.getRole(),
          strTabId))
          || !(Utility.isElementInList(
              Utility.getContext(this, vars, "#User_Client", strWindowId, accesslevel), strClient) && Utility
              .isElementInList(
                  Utility.getContext(this, vars, "#User_Org", strWindowId, accesslevel), strOrg))) {
        OBError myError = Utility.translateError(this, vars, vars.getLanguage(),
            Utility.messageBD(this, "NoWriteAccess", vars.getLanguage()));
        vars.setMessage(strTabId, myError);
        printPageClosePopUp(response, vars);
      } else {
        printPageDocAction(response, vars, strM_Inout_ID, strdocaction, strProcessing,
            strdocstatus, M_Inout_Table_ID, strWindowId);
      }
    } else if (vars.commandIn("SAVE_BUTTONDocAction109")) {
      final String strWindowId = vars.getGlobalVariable("inpwindowId", "ProcessGoods|Window_ID",
          IsIDFilter.instance);
      final String strTabId = vars.getGlobalVariable("inpTabId", "ProcessGoods|Tab_ID",
          IsIDFilter.instance);
      final String strM_Inout_ID = vars
          .getGlobalVariable("inpKey", strWindowId + "|M_Inout_ID", "");
      final String strVoidMinoutDate = vars.getStringParameter("inpVoidedDocumentDate");
      final String strVoidMinoutAcctDate = vars.getStringParameter("inpVoidedDocumentAcctDate");
      String strdocaction = vars.getStringParameter("inpdocaction");

      OBError myMessage = null;
      try {
        ShipmentInOut goods = (ShipmentInOut) OBDal.getInstance().getProxy(
            ShipmentInOut.ENTITY_NAME, strM_Inout_ID);
        goods.setDocumentAction(strdocaction);
        OBDal.getInstance().save(goods);
        OBDal.getInstance().flush();

        Process process = null;
        try {
          OBContext.setAdminMode(true);
          process = (Process) OBDal.getInstance().getProxy(Process.ENTITY_NAME, M_Inout_Post_ID);
        } finally {
          OBContext.restorePreviousMode();
        }

        Map<String, String> parameters = null;
        if (!strVoidMinoutDate.isEmpty() && !strVoidMinoutAcctDate.isEmpty()) {
          Date voidDate = null;
          Date voidAcctDate = null;
          try {
            voidDate = OBDateUtils.getDate(strVoidMinoutDate);
            voidAcctDate = OBDateUtils.getDate(strVoidMinoutAcctDate);
          } catch (ParseException pe) {
            voidDate = new Date();
            voidAcctDate = new Date();
            log4j.error("Not possible to parse the following date: " + strVoidMinoutDate, pe);
            log4j.error("Not possible to parse the following date: " + strVoidMinoutAcctDate, pe);
          }
          parameters = new HashMap<String, String>();
          parameters.put("voidedDocumentDate", OBDateUtils.formatDate(voidDate, "yyyy-MM-dd"));
          parameters.put("voidedDocumentAcctDate",
              OBDateUtils.formatDate(voidAcctDate, "yyyy-MM-dd"));
        }

        final ProcessInstance pinstance = CallProcess.getInstance().call(process, strM_Inout_ID,
            parameters);
        OBDal.getInstance().getSession().refresh(goods);
        goods.setProcessGoodsJava(goods.getDocumentAction());
        OBDal.getInstance().save(goods);
        OBDal.getInstance().flush();
        OBDal.getInstance().commitAndClose();

        final PInstanceProcessData[] pinstanceData = PInstanceProcessData.select(this,
            pinstance.getId());
        myMessage = Utility.getProcessInstanceMessage(this, vars, pinstanceData);
        log4j.debug(myMessage.getMessage());
        vars.setMessage(strTabId, myMessage);

        String strWindowPath = Utility.getTabURL(strTabId, "R", true);
        if (strWindowPath.equals("")) {
          strWindowPath = strDefaultServlet;
        }
        printPageClosePopUp(response, vars, strWindowPath);

      } catch (ServletException ex) {
        myMessage = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
        if (!myMessage.isConnectionAvailable()) {
          bdErrorConnection(response);
          return;
        } else {
          vars.setMessage(strTabId, myMessage);
        }
      }

    }
  }

  void printPageDocAction(HttpServletResponse response, VariablesSecureApp vars,
      String strM_Inout_ID, String strdocaction, String strProcessing, String strdocstatus,
      String stradTableId, String strWindowId) throws IOException, ServletException {
    log4j.debug("Output: Button process 104");
    String[] discard = { "newDiscard" };
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_actionButton/DocAction", discard).createXmlDocument();
    xmlDocument.setParameter("key", strM_Inout_ID);
    xmlDocument.setParameter("processing", strProcessing);
    xmlDocument.setParameter("form", "ProcessGoods.html");
    xmlDocument.setParameter("window", strWindowId);
    xmlDocument.setParameter("css", vars.getTheme());
    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("dateDisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("processId", M_Inout_Post_ID);
    xmlDocument.setParameter("cancel", Utility.messageBD(this, "Cancel", vars.getLanguage()));
    xmlDocument.setParameter("ok", Utility.messageBD(this, "OK", vars.getLanguage()));

    OBError myMessage = vars.getMessage(M_Inout_Post_ID);
    vars.removeMessage(M_Inout_Post_ID);
    if (myMessage != null) {
      xmlDocument.setParameter("messageType", myMessage.getType());
      xmlDocument.setParameter("messageTitle", myMessage.getTitle());
      xmlDocument.setParameter("messageMessage", myMessage.getMessage());
    }

    String processDescription = null;
    try {
      OBContext.setAdminMode(true);
      Process process = (Process) OBDal.getInstance()
          .getProxy(Process.ENTITY_NAME, M_Inout_Post_ID);
      processDescription = process.getDescription();
    } finally {
      OBContext.restorePreviousMode();
    }

    xmlDocument.setParameter("docstatus", strdocstatus);
    if (strWindowId.equals(Goods_Receipt_Window)) {
      // VOID action: Reverse goods receipt/shipment by default inherits the document date and
      // accounting date from the voided document
      ShipmentInOut shipmentInOut = (ShipmentInOut) OBDal.getInstance().getProxy(
          ShipmentInOut.ENTITY_NAME, strM_Inout_ID);
      String movementDate = OBDateUtils.formatDate(shipmentInOut.getMovementDate());
      String accountingDate = OBDateUtils.formatDate(shipmentInOut.getAccountingDate());
      xmlDocument.setParameter("voidedDocumentDate", movementDate);
      xmlDocument.setParameter("voidedDocumentAcctDate", accountingDate);
      xmlDocument.setParameter("documentDate", movementDate);
      xmlDocument.setParameter("documentAcctDate", accountingDate);
    }
    xmlDocument.setParameter("adTableId", stradTableId);
    xmlDocument.setParameter("processId", M_Inout_Post_ID);
    xmlDocument.setParameter("processDescription", processDescription);
    xmlDocument.setParameter("docaction", (strdocaction.equals("--") ? "CL" : strdocaction));
    FieldProvider[] dataDocAction = ActionButtonUtility.docAction(this, vars, strdocaction,
        Goods_Document_Action, strdocstatus, strProcessing, stradTableId);
    xmlDocument.setData("reportdocaction", "liststructure", dataDocAction);
    StringBuffer dact = new StringBuffer();
    if (dataDocAction != null) {
      dact.append("var arrDocAction = new Array(\n");
      for (int i = 0; i < dataDocAction.length; i++) {
        dact.append("new Array(\"" + dataDocAction[i].getField("id") + "\", \""
            + dataDocAction[i].getField("name") + "\", \""
            + dataDocAction[i].getField("description") + "\")\n");
        if (i < dataDocAction.length - 1)
          dact.append(",\n");
      }
      dact.append(");");
    } else
      dact.append("var arrDocAction = null");
    xmlDocument.setParameter("array", dact.toString());

    out.println(xmlDocument.print());
    out.close();

  }

  public String getServletInfo() {
    return "Servlet to Process Goods Shipment and Goods Receipt";
  }
}
