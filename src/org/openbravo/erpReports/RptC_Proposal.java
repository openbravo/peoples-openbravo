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
 * All portions are Copyright (C) 2001-2009 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.erpReports;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.xmlEngine.XmlDocument;

public class RptC_Proposal extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void init(ServletConfig config) {
    super.init(config);
    boolHist = false;
  }

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (vars.commandIn("DEFAULT")) {
      String strClave = vars.getSessionValue("RptC_Proposal.inpcProjectproposalId_R");
      if (strClave.equals(""))
        strClave = vars.getSessionValue("RptC_Proposal.inpcProjectproposalId");
      printPagePartePDF(response, vars, strClave);
    } else
      pageError(response);
  }

  private void printPagePartePDF(HttpServletResponse response, VariablesSecureApp vars,
      String strClave) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: pdf");
    RptCProposalData[] data = RptCProposalData.select(this, Utility.getContext(this, vars,
        "#User_Client", "RptC_Proposal"), Utility.getContext(this, vars, "#AccessibleOrgTree",
        "RptC_Proposal"), strClave);
    String discard[] = { "" };
    if (data == null || data.length == 0)
      discard[0] = new String("sectionDetailHeader");
    RptCProposalData[][] dataHeader = new RptCProposalData[data.length][];
    RptCProposalData[][] dataLines = new RptCProposalData[data.length][];
    RptCProposalData[][] dataFootnote = new RptCProposalData[data.length][];

    for (int i = 0; i < data.length; i++) {
      dataHeader[i] = RptCProposalData.selectHeader(this, data[i].cBpartnerId,
          data[i].cProjectproposalId);
      if (dataHeader[i] == null || dataHeader[i].length == 0)
        dataHeader[i] = RptCProposalData.set();
      dataLines[i] = RptCProposalData.selectLines(this, data[i].cProjectproposalId);
      if (dataLines[i] == null || dataLines[i].length == 0)
        dataLines[i] = RptCProposalData.set();
      dataFootnote[i] = RptCProposalData.selectFootnote(this, data[i].cProjectproposalId);
      if (dataFootnote[i] == null || dataFootnote[i].length == 0)
        dataFootnote[i] = RptCProposalData.set();
    }
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpReports/RptC_Proposal",
        discard).createXmlDocument();
    // here we pass the familiy-ID with report.setData
    xmlDocument.setData("structure", data);
    xmlDocument.setDataArray("reportProposalHeader", "structure1", dataHeader);
    xmlDocument.setDataArray("reportProposalLines", "structure2", dataLines);
    xmlDocument.setDataArray("reportProposalFootnote", "structure3", dataFootnote);

    String strResult = xmlDocument.print();
    if (log4j.isDebugEnabled())
      log4j.debug(strResult);
    renderFO(strResult, response);
  }

  public String getServletInfo() {
    return "Servlet that presents the RptCOrders seeker";
  } // End of getServletInfo() method
}
