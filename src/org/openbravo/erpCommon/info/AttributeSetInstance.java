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
package org.openbravo.erpCommon.info;

import org.openbravo.base.secureApp.*;
import org.openbravo.xmlEngine.XmlDocument;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.Utility;
import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import org.openbravo.utils.Replace;
import org.openbravo.erpCommon.utility.DateTimeData;

import org.openbravo.erpCommon.utility.SequenceIdData;

// imports for transactions
import org.openbravo.database.ConnectionProvider;
import java.sql.Connection;


public class AttributeSetInstance extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void init (ServletConfig config) {
    super.init(config);
    boolHist = false;
  }

  public void doPost (HttpServletRequest request, HttpServletResponse response) throws IOException,ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (vars.commandIn("DEFAULT")) {
      String strNameValue = vars.getRequestGlobalVariable("inpKeyValue", "AttributeSetInstance.instance");
      String strProduct = vars.getRequestGlobalVariable("inpProduct", "AttributeSetInstance.product");
      vars.getRequestGlobalVariable("inpwindowId", "AttributeSetInstance.windowId");
      vars.getRequestGlobalVariable("inpLocatorId", "AttributeSetInstance.locatorId");
      vars.getRequestGlobalVariable("inpTabId", "AttributeSetInstance.tabId");
      String strAttributeSet="";
      String strProductInstance="";
      if (strNameValue.equals("") || strNameValue.equals("0")) {
        vars.setSessionValue("AttributeSetInstance.instance", "");
        AttributeSetInstanceData[] data = AttributeSetInstanceData.selectProductAttr(this, strProduct);
        if (data!=null && data.length>0 && !data[0].mAttributesetId.equals("")) {
          strAttributeSet = data[0].mAttributesetId;
          strProductInstance = data[0].mAttributesetinstanceId;
        }
      } else {
        strAttributeSet = AttributeSetInstanceData.selectAttributeSet(this, strNameValue);
      }
      vars.setSessionValue("AttributeSetInstance.attribute", strAttributeSet);
      vars.setSessionValue("AttributeSetInstance.productInstance", strProductInstance);
      vars.setSessionValue("AttributeSetInstance.close", "N");
      if (strAttributeSet.equals("") || strAttributeSet.equals("0")) advisePopUp(response, "Info", Utility.messageBD(this, "PAttributeNoSelection", vars.getLanguage()));
      else printPageFS(response, vars);
    } else if (vars.commandIn("FRAME1")) {
      String strNameValue = vars.getGlobalVariable("inpInstance", "AttributeSetInstance.instance", "");
      String strAttributeSet = vars.getGlobalVariable("inpAttribute", "AttributeSetInstance.attribute");
      String strProductInstance = vars.getGlobalVariable("inpProductInstance", "AttributeSetInstance.productInstance", "");
      String strWindowId = vars.getGlobalVariable("inpwindowId", "AttributeSetInstance.windowId", "");
      String strTabId = vars.getGlobalVariable("inpTabId", "AttributeSetInstance.tabId", "");
      String strLocator = vars.getGlobalVariable("inpLocatorId", "AttributeSetInstance.locatorId", "");
      String strProduct = vars.getGlobalVariable("inpProduct", "AttributeSetInstance.product", "");
      String strIsSOTrx = Utility.getContext(this, vars, "isSOTrx", strWindowId);
      if (log4j.isDebugEnabled()) log4j.debug("strNameValue: " + strNameValue);
      if (!strAttributeSet.equals("")) printPageFrame1(response, vars, strNameValue, strAttributeSet, strProductInstance, strWindowId, strTabId, strLocator, strIsSOTrx, strProduct);
      else advisePopUp(response, "Info", Utility.messageBD(this, "PAttributeNoSelection", vars.getLanguage()));
    } else if (vars.commandIn("FRAME2")) {
      printPageFrame2(response, vars);
    } else if (vars.commandIn("SAVE")) {
      String strAttributeSet = vars.getRequiredStringParameter("inpAttribute");
      String strInstance = vars.getStringParameter("inpInstance");
      String strWindowId = vars.getStringParameter("inpwindowId");
      String strTabId = vars.getStringParameter("inpTabId");
      String strProduct = vars.getRequestGlobalVariable("inpProduct", "AttributeSetInstance.product");
      String strIsSOTrx = Utility.getContext(this, vars, "isSOTrx", strWindowId);
      OBError myMessage = writeFields(this, vars, AttributeSetInstanceData.select(this, strAttributeSet), strAttributeSet, strInstance, strWindowId, strIsSOTrx, strProduct);
      vars.setSessionValue("AttributeSetInstance.attribute", strAttributeSet);
      vars.setSessionValue("AttributeSetInstance.close", "Y");
      vars.setMessage(strTabId,myMessage);
      //vars.setSessionValue("AttributeSetInstance.message", strMessage);      
      response.sendRedirect(strDireccion + request.getServletPath() + "?Command=FRAME1");
    } else pageErrorPopUp(response);
  }

  String getDescription(VariablesSecureApp vars, AttributeSetInstanceData[] data, String strIsSOTrx) {
    if (data==null || data.length==0) return "";
    String description="";
    try {
      //AttributeSet header
      String serno = "", lot="", guaranteedate="", lockDescription="", description_first="";
      if (data[0].islot.equals("Y")) {
        lot = vars.getStringParameter("inplot");
        if (!data[0].mLotctlId.equals("") && strIsSOTrx.equals("N")) {
          return "";
        }
        description_first += (description_first.equals("")?"":"_") + "L" + lot;
      } 
      if (data[0].isserno.equals("Y")) {
        serno = vars.getStringParameter("inpserno");
        if (!data[0].mSernoctlId.equals("") && strIsSOTrx.equals("N")) {
          return "";
        }
        description_first += (description_first.equals("")?"":"_") + "#" + serno;
      }
      if (data[0].isguaranteedate.equals("Y")) {
        guaranteedate = vars.getStringParameter("inpDateFrom");
        description_first += (description_first.equals("")?"":"_") + guaranteedate;
      }
      if (data[0].islockable.equals("Y")) {
        lockDescription = vars.getStringParameter("inplockDescription");
        description_first += (description_first.equals("")?"":"_") + lockDescription;
      }

      if (!data[0].elementname.equals("")) {
        for (int i=0;i<data.length;i++) {
          String strValue = "";
          if (data[i].ismandatory.equals("Y")) strValue = vars.getRequiredStringParameter("inp" + replace(data[i].elementname));
          else strValue = vars.getStringParameter("inp" + replace(data[i].elementname));
          String strDescValue = strValue;
          if (data[i].islist.equals("Y")) strDescValue = AttributeSetInstanceData.selectAttributeValue(this, strValue);
          description += (description.equals("")?"":"_") + strDescValue;
        }
      }
      if (!description_first.equals("")) description += (description.equals("")?"":"_") + description_first;
    } catch (ServletException e) {
      return "";
    }
    return description;
  }

  OBError writeFields(ConnectionProvider cp, VariablesSecureApp vars, AttributeSetInstanceData[] data, String strAttributeSet, String strInstance, String strWindow, String strIsSOTrx, String strProduct) throws ServletException {
    String strNewInstance="";
    
    OBError myMessage = null;    
	myMessage = new OBError();
	myMessage.setTitle("");
	myMessage.setType("Success");
    myMessage.setMessage(Utility.messageBD(this, "Success", vars.getLanguage()));
    if (data==null || data.length==0)  {
    	myMessage.setType("Error");
        myMessage.setMessage(Utility.messageBD(this, "FindZeroRecords", vars.getLanguage()));
    	//Utility.messageBD(this, "FindZeroRecords", vars.getLanguage());
    	return myMessage;
    }

    boolean isinstance = !AttributeSetInstanceData.isInstanceAttribute(this, strAttributeSet).equals("0");
    String strDescription = getDescription(vars, data, strIsSOTrx);
    Connection conn = null;
    try {
      conn = cp.getTransactionConnection();
      String serno = "", lot="", guaranteedate="", locked="", lockDescription="", description="", description_first="";
      if (data[0].islot.equals("Y")) {
        lot = vars.getStringParameter("inplot");
        if (!data[0].mLotctlId.equals("") && strIsSOTrx.equals("N")) {
          lot = AttributeSetInstanceData.selectNextLot(this, data[0].mLotctlId);
          AttributeSetInstanceData.updateLotSequence(conn, this, vars.getUser(), data[0].mLotctlId);
        }
        description_first += (description_first.equals("")?"":"_") + "L" + lot;
      }
      if (data[0].isserno.equals("Y")) {
        serno = vars.getStringParameter("inpserno");
        if (!data[0].mSernoctlId.equals("") && strIsSOTrx.equals("N")) {
          serno = AttributeSetInstanceData.selectNextSerNo(conn, this, data[0].mSernoctlId);
          AttributeSetInstanceData.updateSerNoSequence(conn, this, vars.getUser(), data[0].mSernoctlId);
        }
        description_first += (description_first.equals("")?"":"_") + "#" + serno;
      }
      if (data[0].isguaranteedate.equals("Y")) {
        guaranteedate = vars.getStringParameter("inpDateFrom");
        description_first += (description_first.equals("")?"":"_") + guaranteedate;
      }
      if (data[0].islockable.equals("Y")) {
        locked = vars.getStringParameter("inpislocked", "N");
        lockDescription = vars.getStringParameter("inplockDescription");
        description_first += (description_first.equals("")?"":"_") + lockDescription;
      }
      if (!isinstance) {
        strNewInstance = AttributeSetInstanceData.hasIdentical(this, strDescription,data[0].mAttributesetId);
      }
      boolean hasToUpdate = false;
      if ((!strInstance.equals("")) && (isinstance)) {//Si if it's existant and requestable, it edits it
        hasToUpdate = true;
        if (AttributeSetInstanceData.updateHeader(conn, this, vars.getUser(), data[0].mAttributesetId, serno, lot, guaranteedate, data[0].mLotctlId, locked, lockDescription, strInstance) == 0) {
          AttributeSetInstanceData.insertHeader(conn, this, strInstance, vars.getClient(), vars.getOrg(), vars.getUser(), data[0].mAttributesetId, serno, lot, guaranteedate, data[0].mLotctlId, locked, lockDescription);
        }
      } else if ((isinstance) || (strNewInstance.equals(""))) { //New or editable,if it's requestable or doesn't exist the identic, then it inserts a new one
        hasToUpdate = true;
        strNewInstance = SequenceIdData.getSequence(this, "M_AttributeSetInstance", vars.getClient());
        AttributeSetInstanceData.insertHeader(conn, this, strNewInstance, vars.getClient(), vars.getOrg(), vars.getUser(), data[0].mAttributesetId, serno, lot, guaranteedate, data[0].mLotctlId, locked, lockDescription);
      }
      if (hasToUpdate) {
        if (!data[0].elementname.equals("")) {
          for (int i=0;i<data.length;i++) {
            String strValue = "";
            if (data[i].ismandatory.equals("Y")) strValue = vars.getRequiredStringParameter("inp" + replace(data[i].elementname));
            else strValue = vars.getStringParameter("inp" + replace(data[i].elementname));
            String strDescValue = strValue;
            if (data[i].islist.equals("Y")) strDescValue = AttributeSetInstanceData.selectAttributeValue(this, strValue);
            if (!strNewInstance.equals("")) {
              if (AttributeSetInstanceData.update(conn, this, vars.getUser(), (data[i].islist.equals("Y")?strValue:""), strDescValue, strNewInstance, data[i].mAttributeId) == 0) {
                AttributeSetInstanceData.insert(conn, this, strNewInstance, data[i].mAttributeId, vars.getClient(), vars.getOrg(), vars.getUser(), (data[i].islist.equals("Y")?strValue:""), strDescValue);
              }
            } else {
              if (AttributeSetInstanceData.update(conn, this, vars.getUser(), (data[i].islist.equals("Y")?strValue:""), strDescValue, strInstance, data[i].mAttributeId) == 0) {
                AttributeSetInstanceData.insert(conn, this, strInstance, data[i].mAttributeId, vars.getClient(), vars.getOrg(), vars.getUser(), (data[i].islist.equals("Y")?strValue:""), strDescValue);
              }
            }
            description += (description.equals("")?"":"_") + strDescValue;
          }
        }
        if (!description_first.equals("")) description += (description.equals("")?"":"_") + description_first;
        AttributeSetInstanceData.updateHeaderDescription(conn, this, vars.getUser(), description, (strNewInstance.equals("")?strInstance:strNewInstance));
      }
      releaseCommitConnection(conn);
      vars.setSessionValue("AttributeSetInstance.instance", (strNewInstance.equals("")?strInstance:strNewInstance));
    } catch(Exception e) {
      try {
        releaseRollbackConnection(conn);
      } catch (Exception ignored) {}
      log4j.error("Rollback in transaction: " + e);
    }
    return myMessage;
  }

  void printPageFS(HttpServletResponse response, VariablesSecureApp vars) throws IOException, ServletException {
    if (log4j.isDebugEnabled()) log4j.debug("Output: Frame Set del buscador de atributos");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/info/AttributeSetInstance_FS").createXmlDocument();

    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  void printPageFrame1(HttpServletResponse response, VariablesSecureApp vars, String strInstance, String strAttributeSet, String strProductInstance, String strWindowId, String strTabId, String strLocator, String strIsSOTrx, String strProduct) throws IOException, ServletException {
    if (log4j.isDebugEnabled()) log4j.debug("Output: Frame 1 of the attributes seeker");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/info/AttributeSetInstance_F1").createXmlDocument();

    xmlDocument.setParameter("calendar", vars.getLanguage().substring(0,2));
    xmlDocument.setParameter("direction", "var baseDirection = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("language", "LNG_POR_DEFECTO=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("theme", vars.getTheme());
    xmlDocument.setParameter("attribute", strAttributeSet);
    xmlDocument.setParameter("windowId", strWindowId);
    xmlDocument.setParameter("tabId", strTabId);
    xmlDocument.setParameter("locatorId", strLocator);
    
    {
        OBError myMessage = vars.getMessage("AttributeSetInstance");
        vars.removeMessage("AttributeSetInstance");
        if (myMessage!=null) {
          xmlDocument.setParameter("messageType", myMessage.getType());
          xmlDocument.setParameter("messageTitle", myMessage.getTitle());
          xmlDocument.setParameter("messageMessage", myMessage.getMessage());
        }
      }
    String message = "";
    if (vars.getSessionValue("AttributeSetInstance.close").equals("Y")) message = "printMessage('')";
    xmlDocument.setParameter("body", message);
    /*String message = vars.getSessionValue("AttributeSetInstance.message");    
    vars.removeSessionValue("AttributeSetInstance.message");
    if (!message.equals("")) {
      if (!message.equals("OK")) message = "alert('" + message + "');";
      else message = "printMessage('');";
    }
    xmlDocument.setParameter("body", message);
    */
    
    if (strInstance.equals("") && AttributeSetInstanceData.isInstanceAttribute(this, strAttributeSet).equals("0")) strInstance = strProductInstance;

    xmlDocument.setParameter("instance", strInstance);
    xmlDocument.setParameter("product", strProduct);
    String strName = Utility.messageBD(this, "Description", vars.getLanguage());
    xmlDocument.setParameter("nameDescription", strName.equals("")?"Description":strName);
    xmlDocument.setParameter("description", AttributeSetInstanceData.selectDescription(this, (strInstance.equals("")?strProductInstance:strInstance)));
    AttributeSetInstanceData[] data = AttributeSetInstanceData.select(this, strAttributeSet);
    xmlDocument.setParameter("data", generateHtml(vars, data, AttributeSetInstanceData.selectInstance(this, (strInstance.equals("")?strProductInstance:strInstance)), strInstance, strIsSOTrx));
    xmlDocument.setParameter("script", generateScript(vars, data));
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  String instanceValue(AttributeSetInstanceData[] instanceData, String strAttributeId, boolean isList) {
    if (instanceData==null || instanceData.length==0) return "";
    for (int i=0;i<instanceData.length;i++) {
      if (instanceData[i].mAttributeId.equals(strAttributeId)) {
        if (isList) return instanceData[i].mAttributevalueId;
        else return instanceData[i].value;
      }
    }
    return "";
  }

  String generateScript(VariablesSecureApp vars, AttributeSetInstanceData[] fields) throws IOException, ServletException {
    if (fields==null || fields.length==0) return "";
    StringBuffer strHtml = new StringBuffer();
    strHtml.append("function onloadFunctions() {\n");
    if (!fields[0].elementname.equals("")) {
      for (int i=0;i<fields.length;i++) {
        if (fields[i].islist.equals("Y")) strHtml.append("  new TypeAheadCombo(\"").append(replace(fields[i].elementname)).append("\");\n");
      }
    }
    strHtml.append("  return true;\n");
    strHtml.append("}\n");
    return strHtml.toString();
  }

  String generateHtml(VariablesSecureApp vars, AttributeSetInstanceData[] fields, AttributeSetInstanceData[] instanceData, String strAttributeInstance, String strIsSOTrx) throws IOException, ServletException {
    if (fields==null || fields.length==0) return "";
    StringBuffer strHtml = new StringBuffer();
    if (!fields[0].elementname.equals("")) {
      for (int i=0;i<fields.length;i++) {
        strHtml.append("<tr><td class=\"TitleCell\"><span class=\"LabelText\">");
        String strName = Utility.messageBD(this, fields[i].elementname, vars.getLanguage());
        strHtml.append(strName.equals("")?fields[i].elementname:strName);
        strHtml.append("</span></td>\n");
        strHtml.append("<td class=\"LabelText\">");
        String strValue = instanceValue(instanceData, fields[i].mAttributeId, fields[i].islist.equals("Y"));
        if (fields[i].islist.equals("Y")) {
          strHtml.append("<select ");
          strHtml.append("name=\"inp" + replace(fields[i].elementname) + "\" ");
          strHtml.append("class=\"Combo");
          if (fields[i].ismandatory.equals("Y")) strHtml.append("Key");
          strHtml.append(" Combo_OneCell_width\" ");
          strHtml.append(" id=\"").append(replace(fields[i].elementname)).append("\">");
          AttributeSetInstanceData[] data = AttributeSetInstanceData.selectList(this, fields[i].mAttributeId);
          if (!fields[i].ismandatory.equals("Y")) strHtml.append("<option value=\"\"></option>\n");
          for (int j=0;j<data.length;j++) {
            strHtml.append("<option value=\"");
            strHtml.append(data[j].value);
            strHtml.append("\" ");
            if (data[j].value.equalsIgnoreCase(strValue)) strHtml.append("selected");
            strHtml.append(">");
            strHtml.append(data[j].name);
            strHtml.append("</option>\n");
          }
          strHtml.append("</select>");
        } else {
          strHtml.append("<textarea ");
          strHtml.append("name=\"inp" + replace(fields[i].elementname) + "\" ");
          strHtml.append("class=\"dojoValidateValid TextArea_OneCell_width TextArea_OneCell_height");
          if (fields[i].ismandatory.equals("Y")) strHtml.append(" required");
          strHtml.append("\"");
          strHtml.append(">");
          strHtml.append(strValue);
          strHtml.append("</textarea>");
        }
        strHtml.append("</td><td></td><td></td></tr>\n");
      }
    }
    if (fields[0].islot.equals("Y")) {
      strHtml.append("<tr><td class=\"TitleCell\"><span class=\"LabelText\">");
      String strName = Utility.messageBD(this, "Lot", vars.getLanguage());
      strHtml.append(strName.equals("")?"Lot":strName);
      strHtml.append("</span></td>\n");
      strHtml.append("<td ");
      strHtml.append("class=\"TextBox_ContentCell\"><input type=\"text\" ");
      strHtml.append("name=\"inplot\" ");
      strHtml.append("maxlength=\"20\" ");
      strHtml.append("class=\"dojoValidateValid TextBox_OneCell_width");
      //strHtml.append("onkeydown=\"auto_completar_numero(this, true, true);return true;\" ");
      if (!fields[0].mLotctlId.equals("") && strIsSOTrx.equals("N")) {
        strHtml.append(" readonly\" readonly=true ");
      } else {
        strHtml.append("\" ");
      }
      if (strAttributeInstance.equals("") && strIsSOTrx.equals("N")) strHtml.append("value=\"" + AttributeSetInstanceData.selectNextLot(this, fields[0].mLotctlId) + "\" ");
      else strHtml.append("value=\"" + ((instanceData!=null && instanceData.length>0)?instanceData[0].lot:"") + "\" ");
      strHtml.append("></td><td></td><td></td></tr>\n");
    }
    if (fields[0].isserno.equals("Y")) {
      strHtml.append("<tr><td class=\"TitleCell\"><span class=\"LabelText\">");
      String strName = Utility.messageBD(this, "SerNo", vars.getLanguage());
      strHtml.append(strName.equals("")?"SerNo":strName);
      strHtml.append("</span></td>\n");
      strHtml.append("<td class=\"TextBox_ContentCell\"><input type=\"text\" ");
      strHtml.append("name=\"inpserno\" ");
      strHtml.append("maxlength=\"20\" ");
      strHtml.append("class=\"dojoValidateValid TextBox_OneCell_width");
      //strHtml.append("onkeydown=\"auto_completar_numero(this, true, true);return true;\" ");
      if (!fields[0].mSernoctlId.equals("") && strIsSOTrx.equals("N")) {
        strHtml.append(" readonly\" readonly=true ");
      } else {
        strHtml.append("\" ");
      }
      if (strAttributeInstance.equals("") && strIsSOTrx.equals("N")) strHtml.append("value=\"" + AttributeSetInstanceData.selectNextSerNo(this, fields[0].mSernoctlId) + "\" ");
      else strHtml.append("value=\"" + ((instanceData!=null && instanceData.length>0)?instanceData[0].serno:"") + "\" ");
      strHtml.append("></td><td></td><td></td></tr>\n");
    }
    if (fields[0].isguaranteedate.equals("Y")) {
      if (log4j.isDebugEnabled()) log4j.debug("GuaranteeDate:"+((instanceData!=null && instanceData.length > 0)?instanceData[0].guaranteedate:""));
      String strGuaranteeDate = null;
      
      if (strAttributeInstance.equals("") && strIsSOTrx.equals("N"))
      strGuaranteeDate = DateTimeData.nDaysAfter(this, DateTimeData.today(this), fields[0].guaranteedays);
      else
      strGuaranteeDate = (instanceData!=null && instanceData.length > 0)?instanceData[0].guaranteedate:"";
      
      strHtml.append("<tr><td class=\"TitleCell\"><span class=\"LabelText\">");
      String strName = Utility.messageBD(this, "GuaranteeDate", vars.getLanguage());
      strHtml.append(strName.equals("")?"GuaranteeDate":strName);
      strHtml.append("</span></td>\n");
      strHtml.append("<TD class=\"TextBox_btn_ContentCell\">");
      strHtml.append("<TABLE border=\"0\" cellspacing=\"0\" cellpadding=\"0\" summary=\"\" style=\"padding-top: 0px;\">");
      strHtml.append("<TR>");
      strHtml.append("<TD class=\"TextBox_ContentCell\">");
      strHtml.append("<INPUT dojoType=\"openbravo:DateTextbox\" displayFormat=\""+vars.getSessionValue("#AD_SqlDateFormat")+"\" saveFormat=\""+vars.getSessionValue("#AD_SqlDateFormat")+"\" class=\"TextBox_btn_OneCell_width\"  type=\"text\" name=\"inpDateFrom\" id=\"paramDateFrom\" size=\"10\" maxlength=\"10\" value=\""+strGuaranteeDate+"\" onkeyup=\"auto_complete_date(this.textbox, this.displayFormat);return true;\"></INPUT><SCRIPT>djConfig.searchIds.push(\"paramDateFrom\");</SCRIPT>");
      strHtml.append("</TD>");
      strHtml.append("<TD class=\"FieldButton_ContentCell\">");
      strHtml.append("<a class=\"FieldButtonLink\" href=\"#\"");
      strHtml.append("onfocus=\"this.className='FieldButtonLink_hover'; window.status='Show calendar'; return true;\"");
      strHtml.append("onblur=\"this.className='FieldButtonLink'; window.status=''; return true;\"");
      strHtml.append("onkeypress=\"this.className='FieldButtonLink_active'; return true;\"");
      strHtml.append("onkeyup=\"this.className='FieldButtonLink_hover'; return true;\"");
      strHtml.append("onClick=\"showCalendar('frmMain.");
      strHtml.append("inpDateFrom', "); 
      strHtml.append("document.frmMain.inpDateFrom.value, false);return false;\">");
      strHtml.append("<table class=\"FieldButton\"");
      strHtml.append("onmousedown=\"this.className='FieldButton_active'; return true;\"");
      strHtml.append("onmouseup=\"this.className='FieldButton'; return true;\"");
      strHtml.append("onmouseover=\"this.className='FieldButton_hover'; window.status='Show calendar'; return true;\"");
      strHtml.append("onmouseout=\"this.className='FieldButton'; window.status=''; return true;\">");
      strHtml.append("<tr>");
      strHtml.append("<td class=\"FieldButton_bg\">");
      strHtml.append("<img alt=\"Calendar\" class=\"FieldButton_Icon FieldButton_Icon_Calendar\" title=\"Calendar\" src=\"").append(strReplaceWith).append("/images/blank.gif\" border=\"0\"\"/>");
      strHtml.append("</td>");
      strHtml.append("</tr>");
      strHtml.append("</table>");
      strHtml.append("</a>");
      strHtml.append("</TD>");
      strHtml.append("</TR>");
      strHtml.append("</TABLE>");
      strHtml.append("</TD><td></td><td></td>");
      strHtml.append("</TR>");
    }
    if (fields[0].islockable.equals("Y")) {
      strHtml.append("<tr><td class=\"TitleCell\"><span class=\"LabelText\">");
      String strName = Utility.messageBD(this, "IsLocked", vars.getLanguage());
      strHtml.append(strName.equals("")?"IsLocked":strName);
      strHtml.append("</span></td>\n");
      strHtml.append("<td class=\"Radio_Check_ContentCell\"><input type=\"checkbox\" ");
      strHtml.append("name=\"inpislocked\" ");
      strHtml.append("value=\"Y\" ");
      if (instanceData!=null && instanceData.length>0 && instanceData[0].islocked.equals("Y")) strHtml.append("checked ");
      strHtml.append("></td></tr>\n");
      strHtml.append("<tr><td class=\"TitleCell\"><span class=\"LabelText\">");
      strName = Utility.messageBD(this, "LockDescription", vars.getLanguage());
      strHtml.append(strName.equals("")?"LockDescription":strName);
      strHtml.append("</span></td>\n");
      strHtml.append("<td>");
      strHtml.append("<textarea ");
      strHtml.append("name=\"inplockDescription\" ");
      strHtml.append("class=\"dojoValidateValid TextArea_OneCell_width TextArea_OneCell_height");
      strHtml.append("\">");
      if (instanceData!=null && instanceData.length>0) strHtml.append(instanceData[0].lockDescription);
      strHtml.append("</textarea>");
      strHtml.append("</td><td></td><td></td></tr>\n");
    }
    return strHtml.toString();
  }

  String replace(String strIni) {
    //delete characters: " ","&",","
    return Replace.replace(Replace.replace(Replace.replace(Replace.replace(Replace.replace(Replace.replace(strIni, "#", "")," ",""),"&",""),",",""),"(",""),")","");
  }

  void printPageFrame2(HttpServletResponse response, VariablesSecureApp vars) throws IOException, ServletException {
    if (log4j.isDebugEnabled()) log4j.debug("Output: Frame 3 of the attributes seeker");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/info/AttributeSetInstance_F2").createXmlDocument();
    xmlDocument.setParameter("direction", "var baseDirection = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("language", "LNG_POR_DEFECTO=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("theme", vars.getTheme());
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  public String getServletInfo() {
    return "Servlet that presents attributes seeker";
  } // end of getServletInfo() method
}

