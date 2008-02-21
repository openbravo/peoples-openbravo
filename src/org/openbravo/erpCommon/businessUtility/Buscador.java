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
package org.openbravo.erpCommon.businessUtility;

import java.io.*;
import java.util.Vector;
import javax.servlet.*;
import javax.servlet.http.*;
import org.openbravo.base.secureApp.*;
import org.openbravo.data.FieldProvider;
import org.openbravo.erpCommon.utility.*;
import org.openbravo.utils.FormatUtilities;
import org.openbravo.xmlEngine.XmlDocument;



public class Buscador extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;
  protected static final int MAX_TEXTBOX_LENGTH=150;
  protected static final int MAX_TEXTBOX_DISPLAY=30;

  public void init (ServletConfig config) {
    super.init(config);
    boolHist = false;
  }

  public void doPost (HttpServletRequest request, HttpServletResponse response) throws IOException,ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (vars.commandIn("DEFAULT")) {
      String strTab = vars.getRequiredStringParameter("inpTabId");
      String strWindow = vars.getRequiredStringParameter("inpWindow");
      String strWindowId = vars.getStringParameter("inpWindowId");
      String strIsSOTrx = vars.getSessionValue(strWindowId + "|issotrxtab");
      BuscadorData[] data = BuscadorData.select(this, vars.getLanguage(), strTab);
      if (data==null || data.length==0) data = BuscadorData.selectIdentifiers(this, vars.getLanguage(), strTab);
      if (data==null || data.length==0) {
        if (log4j.isDebugEnabled()) log4j.debug("there're no selection columns and no identifiers defined for this table");
        bdError(response,"SearchNothing" ,vars.getLanguage());
      } else {
        data = removeParents(data, strTab);
        if (loadParameters(vars, data, strTab)) {
          for (int i=0;i<data.length;i++) {
            if (data[i].reference.equals("10") || data[i].reference.equals("14") || data[i].reference.equals("34")) data[i].value = "%";
            else if (data[i].reference.equals("20")) data[i].value="N";
            else data[i].value="";
          }
        }
        if (data==null || data.length==0) {
          if (log4j.isDebugEnabled()) log4j.debug("The columns defined were parent keys");
          bdError(response,"SearchNothing" ,vars.getLanguage());
        } else printPage(response, vars, strTab, data, strWindow, strWindowId, strIsSOTrx);
      }
    } else pageError(response);
  }

  BuscadorData[] removeParents(BuscadorData[] data, String strTab) throws ServletException {
    String parentColumn = BuscadorData.parentsColumnName(this, strTab);
    if (data==null || data.length==0) return data;
    if (parentColumn.equals("")) return data;
    Vector<Object> vec = new Vector<Object>();
    BuscadorData[] result=null;
    for (int i=0;i<data.length;i++) {
      if (!parentColumn.equalsIgnoreCase(data[i].columnname) || data[i].isselectioncolumn.equals("Y")) vec.addElement(data[i]);
    }
    if (vec.size()>0) {
      result = new BuscadorData[vec.size()];
      vec.copyInto(result);
    }
    return result;
  }

  boolean loadParameters(VariablesSecureApp vars, BuscadorData[] data, String strTab) throws ServletException {
    if (data==null || data.length==0) return false;
    boolean isEmpty=true;
    for (int i=0;i<data.length;i++) {
      data[i].value = vars.getSessionValue(strTab + "|param" + FormatUtilities.replace(data[i].columnname));
      if (!data[i].value.equals("")) isEmpty=false;
    }
    return isEmpty;
  }


  void printPage(HttpServletResponse response, VariablesSecureApp vars, String strTab, BuscadorData[] data, String strWindow, String strWindowId, String strIsSOTrx) throws IOException, ServletException {
    if (log4j.isDebugEnabled()) log4j.debug("Output: Frame 1 of the attributes seeker");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/businessUtility/Buscador").createXmlDocument();

    xmlDocument.setParameter("calendar", vars.getLanguage().substring(0,2));
    xmlDocument.setParameter("direction", "var baseDirection = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("language", "LNG_POR_DEFECTO=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("theme", vars.getTheme());
    StringBuffer script = new StringBuffer();
    Vector<StringBuffer> vecScript = new Vector<StringBuffer>();
    xmlDocument.setParameter("data", generateHtml(vars, data, strTab, strWindowId, script, strIsSOTrx, vecScript));
    xmlDocument.setParameter("scripsJS", vecScript.elementAt(0).toString());
    xmlDocument.setParameter("script", (script.toString() + generateScript(data, strWindow, strTab)));
    xmlDocument.setParameter("tab", strTab);
    xmlDocument.setParameter("window", strWindow);
    {
      OBError myMessage = vars.getMessage(strTab);
      vars.removeMessage(strTab);
      if (myMessage!=null) {
        xmlDocument.setParameter("messageType", myMessage.getType());
        xmlDocument.setParameter("messageTitle", myMessage.getTitle());
        xmlDocument.setParameter("messageMessage", myMessage.getMessage());
      }
    }
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  String generateScript(BuscadorData[] fields, String strWindow, String strTab) {
    StringBuffer strHtml = new StringBuffer();
    StringBuffer strCombo = new StringBuffer();
    strHtml.append("function aceptar() {\n");
    strHtml.append("  var frm = document.forms[0];\n");
    strHtml.append("  var paramsData = new Array();\n");
    strHtml.append("  var count = 0;\n");
    boolean isHighVolume=false;
    try {
      isHighVolume = BuscadorData.isHighVolume(this, strTab).equals("Y");
    } catch (ServletException e) {
      log4j.error(e);
    }
    StringBuffer paramsData = new StringBuffer();
    StringBuffer params = new StringBuffer();
    if (fields!=null && fields.length!=0 && isHighVolume) {
      strHtml.append("if (");
      for (int i=0;i<fields.length;i++) {
        if (i>0) strHtml.append(" && ");
        paramsData.append("paramsData[count++] = new Array(\"inpParam").append(FormatUtilities.replace(fields[i].columnname)).append("\" , ");
        params.append(", \"inpParam").append(FormatUtilities.replace(fields[i].columnname)).append("\",");
        params.append(" escape(");
        if (fields[i].reference.equals("20")) {
          paramsData.append("((radioValue(frm.inpParam").append(FormatUtilities.replace(fields[i].columnname)).append(")!=null)?");
          paramsData.append("radioValue(frm.inpParam").append(FormatUtilities.replace(fields[i].columnname)).append("):");
          paramsData.append("\"\"));\n");
          params.append("((radioValue(frm.inpParam").append(FormatUtilities.replace(fields[i].columnname)).append(")!=null)?");
          params.append("radioValue(frm.inpParam").append(FormatUtilities.replace(fields[i].columnname)).append("):");
          params.append("\"\")");
        } else if (fields[i].reference.equals("17") || fields[i].reference.equals("18") || fields[i].reference.equals("19")) { //Select
          paramsData.append("((frm.inpParam").append(FormatUtilities.replace(fields[i].columnname)).append(".selectedIndex!=-1)?");
          paramsData.append("frm.inpParam").append(FormatUtilities.replace(fields[i].columnname)).append(".options[");
          paramsData.append("frm.inpParam").append(FormatUtilities.replace(fields[i].columnname)).append(".selectedIndex].value:");
          paramsData.append("\"\"));\n");
          params.append("((frm.inpParam").append(FormatUtilities.replace(fields[i].columnname)).append(".selectedIndex!=-1)?");
          params.append("frm.inpParam").append(FormatUtilities.replace(fields[i].columnname)).append(".options[");
          params.append("frm.inpParam").append(FormatUtilities.replace(fields[i].columnname)).append(".selectedIndex].value:");
          params.append("\"\")");
          strCombo.append("  new TypeAheadCombo(\"idParam").append(FormatUtilities.replace(fields[i].columnname)).append("\");\n");
        } else if (Utility.isDecimalNumber(fields[i].reference) || Utility.isIntegerNumber(fields[i].reference) || Utility.isDateTime(fields[i].reference)) {
          paramsData.append("frm.inpParam").append(FormatUtilities.replace(fields[i].columnname)).append(".value").append(");\n");
          paramsData.append("paramsData[count++] = new Array(\"inpParam").append(FormatUtilities.replace(fields[i].columnname)).append("_f\", ");
          paramsData.append("frm.inpParam").append(FormatUtilities.replace(fields[i].columnname)).append("_f.value);\n");
          params.append("frm.inpParam").append(FormatUtilities.replace(fields[i].columnname)).append(".value");
          params.append("), \"inpParam").append(FormatUtilities.replace(fields[i].columnname)).append("_f\",");
          params.append(" escape(");
          params.append("frm.inpParam").append(FormatUtilities.replace(fields[i].columnname)).append("_f.value");
        } else {
          paramsData.append("frm.inpParam").append(FormatUtilities.replace(fields[i].columnname)).append(".value);\n");
          params.append("frm.inpParam").append(FormatUtilities.replace(fields[i].columnname)).append(".value");
        }
        params.append(")");
        if (fields[i].reference.equals("17") || fields[i].reference.equals("18") || fields[i].reference.equals("19")) { //Select
          strHtml.append("frm.inpParam").append(FormatUtilities.replace(fields[i].columnname)).append(".selectedIndex!=-1");
        } else if (Utility.isDecimalNumber(fields[i].reference) || Utility.isIntegerNumber(fields[i].reference) || Utility.isDateTime(fields[i].reference)) {
          strHtml.append("(frm.inpParam").append(FormatUtilities.replace(fields[i].columnname)).append(".value==null || ");
          strHtml.append("frm.inpParam").append(FormatUtilities.replace(fields[i].columnname)).append(".value==\"\") ");
          strHtml.append("&& (frm.inpParam").append(FormatUtilities.replace(fields[i].columnname)).append("_f.value==null || ");
          strHtml.append("frm.inpParam").append(FormatUtilities.replace(fields[i].columnname)).append("_f.value==\"\") ");
        } else {
          strHtml.append("(frm.inpParam").append(FormatUtilities.replace(fields[i].columnname)).append(".value==null || ");
          strHtml.append("frm.inpParam").append(FormatUtilities.replace(fields[i].columnname)).append(".value==\"\") ");
        }
      }
      strHtml.append(") {\n");
      strHtml.append("    mensaje(1);\n");
      strHtml.append("    return false;\n");
      strHtml.append("  }\n");
    } else if (fields!=null) {
      for (int i=0;i<fields.length;i++) {
        paramsData.append("paramsData[count++] = new Array(\"inpParam").append(FormatUtilities.replace(fields[i].columnname)).append("\", ");
        params.append(", \"inpParam").append(FormatUtilities.replace(fields[i].columnname)).append("\"");
        params.append(", ");
        params.append("escape(");
        if (fields[i].reference.equals("20")) {
          paramsData.append("((radioValue(frm.inpParam").append(FormatUtilities.replace(fields[i].columnname)).append(")!=null)?");
          paramsData.append("radioValue(frm.inpParam").append(FormatUtilities.replace(fields[i].columnname)).append("):");
          paramsData.append("\"\"));\n");
          params.append("((radioValue(frm.inpParam").append(FormatUtilities.replace(fields[i].columnname)).append(")!=null)?");
          params.append("radioValue(frm.inpParam").append(FormatUtilities.replace(fields[i].columnname)).append("):");
          params.append("\"\")");
        } else if (fields[i].reference.equals("17") || fields[i].reference.equals("18") || fields[i].reference.equals("19")) {
          paramsData.append("((frm.inpParam").append(FormatUtilities.replace(fields[i].columnname)).append(".selectedIndex!=-1)?");
          paramsData.append("frm.inpParam").append(FormatUtilities.replace(fields[i].columnname)).append(".options[");
          paramsData.append("frm.inpParam").append(FormatUtilities.replace(fields[i].columnname)).append(".selectedIndex].value:");
          paramsData.append("\"\"));\n");
          params.append("((frm.inpParam").append(FormatUtilities.replace(fields[i].columnname)).append(".selectedIndex!=-1)?");
          params.append("frm.inpParam").append(FormatUtilities.replace(fields[i].columnname)).append(".options[");
          params.append("frm.inpParam").append(FormatUtilities.replace(fields[i].columnname)).append(".selectedIndex].value:");
          params.append("\"\")");
          strCombo.append("  new TypeAheadCombo(\"idParam").append(FormatUtilities.replace(fields[i].columnname)).append("\");\n");
        } else if (Utility.isDecimalNumber(fields[i].reference) || Utility.isIntegerNumber(fields[i].reference) || Utility.isDateTime(fields[i].reference)) {
          paramsData.append("frm.inpParam").append(FormatUtilities.replace(fields[i].columnname)).append(".value);\n");
          paramsData.append("paramsData[count++] = new Array(\"inpParam").append(FormatUtilities.replace(fields[i].columnname)).append("_f\", ");
          paramsData.append("frm.inpParam").append(FormatUtilities.replace(fields[i].columnname)).append("_f.value);\n");
          params.append("frm.inpParam").append(FormatUtilities.replace(fields[i].columnname)).append(".value");
          params.append("), \"inpParam").append(FormatUtilities.replace(fields[i].columnname)).append("_f\",");
          params.append(" escape(");
          params.append("frm.inpParam").append(FormatUtilities.replace(fields[i].columnname)).append("_f.value");
        } else {
          paramsData.append("frm.inpParam").append(FormatUtilities.replace(fields[i].columnname)).append(".value);\n");
          params.append("frm.inpParam").append(FormatUtilities.replace(fields[i].columnname)).append(".value");
        }
        params.append(")");
      }
    }
    strHtml.append("\n").append(paramsData);
    strHtml.append("  if (window.opener.selectFilters) window.opener.selectFilters(paramsData);\n");
    strHtml.append("  else window.opener.submitFormGetParams(\"SEARCH\", \"../" + strWindow + "\"" + params.toString() + ");\n");
    strHtml.append("  window.close();\n");
    strHtml.append("  return true;\n");
    strHtml.append("}\n");
    strHtml.append("function onloadFunctions() {\n");
    strHtml.append(strCombo);
    strHtml.append("  return true;\n");
    strHtml.append("}\n");
    return strHtml.toString();
  }

  String generateHtml(VariablesSecureApp vars, BuscadorData[] fields, String strTab, String strWindow, StringBuffer script, String strIsSOTrx, Vector<StringBuffer> vecScript) throws IOException, ServletException {
    if (fields==null || fields.length==0) return "";
    StringBuffer strHtml = new StringBuffer();
    boolean scriptCalendar=false;
    boolean scriptClock=false;
    boolean scriptCalculator=false;
    boolean scriptKeyboard=false;
    boolean scriptSearch=false;
    boolean scriptSelect=false;
    Vector<Object> vecKeys = new Vector<Object>();
    for (int i=0;i<fields.length;i++) {
      if (Integer.valueOf(fields[i].displaylength).intValue()>MAX_TEXTBOX_DISPLAY) fields[i].displaylength=Integer.toString(MAX_TEXTBOX_DISPLAY);
      strHtml.append("<tr><td class=\"TitleCell\"> <SPAN class=\"LabelText\">");
      if (Utility.isDecimalNumber(fields[i].reference) || Utility.isIntegerNumber(fields[i].reference) || Utility.isDateTime(fields[i].reference)) strHtml.append(fields[i].name).append(" ").append(Utility.messageBD(this, "From", vars.getLanguage()));
      else strHtml.append(fields[i].name);
      strHtml.append("</SPAN></td>\n");
      if (fields[i].reference.equals("17") || fields[i].reference.equals("18") || fields[i].reference.equals("19")) {//List, Table, TableDir
        scriptSelect = true;
        strHtml.append("<td class=\"Combo_ContentCell\" colspan=\"3\">");
        strHtml.append("<select ");
        strHtml.append("name=\"inpParam").append(FormatUtilities.replace(fields[i].columnname)).append("\" ");
        strHtml.append("onchange=\"return true; \" id=\"idParam").append(FormatUtilities.replace(fields[i].columnname)).append("\" ");
        if (Integer.valueOf(fields[i].fieldlength).intValue() < (MAX_TEXTBOX_LENGTH/4)) {
          strHtml.append("class=\"Combo Combo_OneCell_width\"");
        } else if (Integer.valueOf(fields[i].fieldlength).intValue() < (MAX_TEXTBOX_LENGTH/2)) {
          strHtml.append("class=\"Combo Combo_TwoCells_width\"");
        } else {
          strHtml.append("class=\"Combo Combo_ThreeCells_width\"");
        }
        strHtml.append(">");
        strHtml.append("<option value=\"\"></option>\n");
        try {
          ComboTableData comboTableData = new ComboTableData(vars, this, fields[i].reference, fields[i].columnname, fields[i].referencevalue, fields[i].adValRuleId, Utility.getContext(this, vars, "#User_Org", strWindow), Utility.getContext(this, vars, "#User_Client", strWindow), 0);
          Utility.fillSQLParameters(this, vars, null, comboTableData, strWindow, fields[i].value);
          FieldProvider[] data = comboTableData.select(false);
          comboTableData = null;
          for (int j=0;j<data.length;j++) {
            strHtml.append("<option value=\"");
            strHtml.append(data[j].getField("ID"));
            strHtml.append("\" ");
            if (data[j].getField("ID").equalsIgnoreCase(fields[i].value)) strHtml.append("selected");
            strHtml.append(">");
            strHtml.append(data[j].getField("NAME"));
            strHtml.append("</option>\n");
          }
        } catch (Exception ex) {
          throw new ServletException(ex);
        }
        strHtml.append("</select>\n");
      } else if (fields[i].reference.equals("15")) { //DATE
        scriptCalendar = true;
        strHtml.append("<td class=\"TextBox_btn_ContentCell\">\n");
        strHtml.append("<table border=\"0\" cellspacing=\"0\" cellpadding=\"0\" summary=\"\"  style=\"padding-top: 0px;\">\n");
        strHtml.append("<tr>\n");
        strHtml.append("<TD class=\"TextBox_ContentCell\">\n");
        strHtml.append("<input dojoType=\"openbravo:DateTextbox\" type=\"text\" class=\"TextBox_btn_OneCell_width\" ");
        strHtml.append("displayFormat=\"").append(FormatUtilities.replace(vars.getSessionValue("#AD_SqlDateFormat"))).append("\" ");
        strHtml.append("saveFormat=\"").append(FormatUtilities.replace(vars.getSessionValue("#AD_SqlDateFormat"))).append("\" ");
        strHtml.append("name=\"inpParam").append(FormatUtilities.replace(fields[i].columnname)).append("\" ");
        strHtml.append("maxlength=\"").append(fields[i].fieldlength).append("\" ");
        strHtml.append("value=\"").append(fields[i].value).append("\" ");
        strHtml.append("id=\"inpParam").append(FormatUtilities.replace(fields[i].columnname)).append("\" ");
        strHtml.append("onkeyup=\"auto_complete_date(this.textbox);\"></input> ");
        strHtml.append("<script>djConfig.searchIds.push(\"").append("inpParam").append(FormatUtilities.replace(fields[i].columnname)).append("\") </script>");
        strHtml.append("</td>\n");
        strHtml.append("<td class=\"FieldButton_ContentCell\">");
        strHtml.append("<a href=\"#\" class=\"FieldButtonLink\" onclick=\"showCalendar('frmMain.inpParam").append(FormatUtilities.replace(fields[i].columnname)).append("', ");
        strHtml.append("document.frmMain.inpParam").append(FormatUtilities.replace(fields[i].columnname)).append(".value, false, '").append(FormatUtilities.replace(vars.getSessionValue("#AD_SqlDateFormat"))).append("');");
        strHtml.append("return false;\" onfocus=\"this.className='FieldButtonLink_hover'; window.status='Calendar'; return true;\" onblur=\"this.className='FieldButtonLink'; window.status=''; return true;\" onkeypress=\"this.className='FieldButtonLink_active'; return true;\" onkeyup=\"this.className='FieldButtonLink_hover'; return true;\">\n");
        strHtml.append("<table class=\"FieldButton\" onmouseout=\"this.className='FieldButton';window.status='';return true;\" onmouseover=\"this.className='FieldButton_hover';window.status='Show calendar';return true;\" onmousedown=\"this.className='FieldButton_active';return true;\" onmouseup=\"this.className='FieldButton';return true;\">\n");
        strHtml.append("<tr>\n");
        strHtml.append("<td class=\"FieldButton_bg\">");
        strHtml.append("<IMG alt=\"Calendar\" class=\"FieldButton_Icon FieldButton_Icon_Calendar\" title=\"Calendar\" src=\"").append(strReplaceWith).append("/images/blank.gif\" border=\"0\"></IMG>\n");
        strHtml.append("</td>\n");
        strHtml.append("</tr>\n");
        strHtml.append("</table>\n");
        strHtml.append("</a>\n");
        strHtml.append("</td>\n");
        strHtml.append("</tr>\n");
        strHtml.append("</table>\n");
        strHtml.append("</td>\n");
      } else if (fields[i].reference.equals("20")) { //YesNo
        strHtml.append("<TD class=\"Radio_Check_ContentCell\">\n");
        strHtml.append("<input type=\"checkbox\" value=\"Y\"  name=\"inpParam").append(FormatUtilities.replace(fields[i].columnname)).append("\" ");
        if (fields[i].value.equals("Y")) strHtml.append("checked");
        strHtml.append(">\n");
      } else if (fields[i].reference.equals("30") || fields[i].reference.equals("21") || fields[i].reference.equals("31") || fields[i].reference.equals("35") || fields[i].reference.equals("25") || fields[i].reference.equals("800011")) { //Search
        strHtml.append("<td class=\"TextBox_btn_ContentCell\" colspan=\"3\">\n");
        strHtml.append("<table border=\"0\" cellspacing=\"0\" cellpadding=\"0\" summary=\"\">\n");
        scriptSearch = true;
        strHtml.append("<TR>\n<TD>\n");
        strHtml.append("<input type=\"hidden\" name=\"inpParam").append(FormatUtilities.replace(fields[i].columnname)).append("\" ");
        strHtml.append("value=\"").append((!fields[i].value.equals("") && !fields[i].value.equals("%"))?fields[i].value:"").append("\">");
        strHtml.append("</TD>\n");
        strHtml.append("<TD class=\"TextBox_ContentCell\">\n");
        if (Integer.valueOf(fields[i].fieldlength).intValue() < (MAX_TEXTBOX_LENGTH/4)) {
          strHtml.append("<input dojoType=\"openbravo:ValidationTextBox\" type=\"text\" class=\"TextBox_btn_OneCell_width\" ");
        } else if (Integer.valueOf(fields[i].fieldlength).intValue() < (MAX_TEXTBOX_LENGTH/2)) {
          strHtml.append("<input dojoType=\"openbravo:ValidationTextBox\" type=\"text\" class=\"TextBox_btn_TwoCells_width\" ");
        } else {
          strHtml.append("<input dojoType=\"openbravo:ValidationTextBox\" type=\"text\" class=\"TextBox_btn_ThreeCells_width\" ");
        }
        strHtml.append("name=\"inpParam").append(FormatUtilities.replace(fields[i].columnname)).append("_DES\" ");
        strHtml.append("id=\"inpParam").append(FormatUtilities.replace(fields[i].columnname)).append("_DES\" ");
        strHtml.append("maxlength=\"").append(fields[i].fieldlength).append("\" ");

         if (!fields[i].value.equals("") && !fields[i].value.equals("%")) {
          String strSearchTableName = BuscadorData.selectSearchTableName(this, fields[i].referencevalue); 
          if (strSearchTableName.equals("")) strSearchTableName = fields[i].columnname.substring(0,fields[i].columnname.length() - 3);
          String strSearchName = BuscadorData.selectSearchName(this, strSearchTableName, fields[i].value, vars.getLanguage());
          strHtml.append("value=\"").append(strSearchName).append("\" ");
         }
        
        strHtml.append((!fields[i].reference.equals("21") && !fields[i].reference.equals("35"))?"":"readonly=\"true\" ");
        strHtml.append("><script>djConfig.searchIds.push(\"").append("inpParam").append(FormatUtilities.replace(fields[i].columnname)).append("_DES\") </script></td>\n");
        String strMethod = "";
        if (fields[i].reference.equals("21")) {
          strMethod = locationCommands(fields[i]);
        } else if (fields[i].reference.equals("31")) {
          strMethod = locatorCommands(fields[i], false, strWindow);
        } else {
          strMethod = searchsCommand(fields[i], false, strTab, strWindow, strIsSOTrx);
        }

        strMethod = "new keyArrayItem(\"ENTER\", \"" + strMethod + "\", \"inpParam" + FormatUtilities.replace(fields[i].columnname) + "_DES\", \"null\")";
        vecKeys.addElement(strMethod);

        if (fields[i].reference.equals("21")) {
          strHtml.append(location(fields[i]));
        } else if (fields[i].reference.equals("31")) {
          strHtml.append(locator(fields[i], strWindow));
        } else {
          strHtml.append(searchs(fields[i], strTab, strWindow, strIsSOTrx));
        }
      } else if (Utility.isDecimalNumber(fields[i].reference) || Utility.isIntegerNumber(fields[i].reference)) {
        scriptCalculator = true;
        if (Utility.isDecimalNumber(fields[i].reference) || Utility.isIntegerNumber(fields[i].reference)) {
          strHtml.append("<td class=\"TextBox_btn_ContentCell\">\n");
        }
        else {
          strHtml.append("<td class=\"TextBox_ContentCell\">\n");
        }
        strHtml.append("<table border=\"0\" cellspacing=\"0\" cellpadding=\"0\" summary=\"\" class=\"\">\n");
        strHtml.append("<tr>");
        strHtml.append("<td class=\"TextBox_ContentCell\">");
        strHtml.append("<input type=\"text\" ");
        strHtml.append("class=\"dojoValidateValid TextBox_btn_OneCell_width\" ");
        strHtml.append("name=\"inpParam").append(FormatUtilities.replace(fields[i].columnname)).append("\" ");
        strHtml.append("maxlength=\"").append(fields[i].fieldlength).append("\" ");
        strHtml.append("value=\"").append(fields[i].value).append("\" ");
        if (Utility.isDecimalNumber(fields[i].reference)) strHtml.append("onkeydown=\"validateNumberBox(this.id);auto_completar_numero(this, true, true);return true;\" ");
        else if (Utility.isIntegerNumber(fields[i].reference)) strHtml.append("onkeydown=\"validateNumberBox(this.id);auto_completar_numero(this, false, false);return true;\" ");
        strHtml.append(">");
        if (Utility.isDecimalNumber(fields[i].reference) || Utility.isIntegerNumber(fields[i].reference)) {
          strHtml.append("<td class=\"FieldButton_ContentCell\">\n<TABLE class=\"FieldButton\" onclick=\"calculator('frmMain.");
          strHtml.append("inpParam").append(FormatUtilities.replace(fields[i].columnname)).append("', "); 
          strHtml.append("document.frmMain.inpParam").append(FormatUtilities.replace(fields[i].columnname)).append(".value, false);return false;\" ");
          strHtml.append("onmouseout=\"this.className='FieldButton';window.status='';return true;\" onmouseover=\"this.className='FieldButton_hover';window.status='Show calculator';return true;\" onmousedown=\"this.className='FieldButton_active';return true;\" onmouseup=\"this.className='FieldButton';return true;\">");
          strHtml.append("<tr>\n<td class=\"FieldButton_bg\">\n");
          strHtml.append("<IMG alt=\"Calculator\" class=\"FieldButton_Icon FieldButton_Icon_Calc\" title=\"Calculator\" src=\"").append(strReplaceWith).append("/images/blank.gif\" border=\"0\"></IMG>\n");
          strHtml.append("</td>\n</tr>\n</table>\n</td>\n</tr>\n</table>\n");
          strHtml.append("<SPAN class=\"invalid\" style=\"display: none;\">* The value entered is not valid.</SPAN>");
          strHtml.append("<SPAN class=\"missing\" style=\"display: none;\">* This value is required.</SPAN>");
          strHtml.append("<SPAN class=\"range\" style=\"display: none;\">* This value is out of range.</SPAN>");
          strHtml.append("</td>");
        }
      } else if ((Integer.valueOf(fields[i].fieldlength).intValue() > MAX_TEXTBOX_LENGTH)) { //Memo // REplace with reference // 1-2-3 cells doing < MAX_TEXTBOX_LENGTH/4 /2 > /2
        strHtml.append("<td>");
        strHtml.append("<textarea class=\"dojoValidateValid TextArea_TwoCells_width TextArea_Medium_height\" ");
        strHtml.append("name=\"inpParam").append(FormatUtilities.replace(fields[i].columnname)).append("\" ");
        strHtml.append("cols=\"50\" rows=\"3\" ");
        strHtml.append(">");
        strHtml.append(fields[i].value);
        strHtml.append("</textarea>\n");
      } else {
        strHtml.append("<td class=\"TextBox_ContentCell\">");
        strHtml.append("<input type=\"text\" class=\"dojoValidateValid TextBox_OneCell_width\" ");
        strHtml.append("name=\"inpParam").append(FormatUtilities.replace(fields[i].columnname)).append("\" ");
        strHtml.append("maxlength=\"").append(fields[i].fieldlength).append("\" ");
        strHtml.append("value=\"").append(fields[i].value).append("\" ");
        if (Utility.isDecimalNumber(fields[i].reference)) {
          scriptCalculator = true;
          strHtml.append("onkeydown=\"auto_completar_numero(this, true, true);return true;\" ");
        } else if (Utility.isIntegerNumber(fields[i].reference)) {
          scriptCalculator = true;
          strHtml.append("onkeydown=\"auto_completar_numero(this, false, false);return true;\" ");
        }
        strHtml.append(">");
        strHtml.append("</td>");//<td class=\"FieldButton_bg\">");
      }

      if (Utility.isDecimalNumber(fields[i].reference) || Utility.isIntegerNumber(fields[i].reference) || Utility.isDateTime(fields[i].reference)) {
        String value = vars.getSessionValue(strTab + "|param" + FormatUtilities.replace(fields[i].columnname) + "_f");
        strHtml.append("<td class=\"TitleCell\"> <SPAN class=\"LabelText\">");
        strHtml.append(Utility.messageBD(this, "To", vars.getLanguage()));
        strHtml.append("</SPAN></td>\n");

        if (Utility.isDecimalNumber(fields[i].reference) || Utility.isIntegerNumber(fields[i].reference)) {
          strHtml.append("<td class=\"TextBox_btn_ContentCell\">\n");
          strHtml.append("<table border=\"0\" cellspacing=\"0\" cellpadding=\"0\" summary=\"\" class=\"\">\n");
          strHtml.append("<tr>");
          strHtml.append("<td class=\"TextBox_ContentCell\">");
          strHtml.append("<input type=\"text\" ");
          strHtml.append("class=\"dojoValidateValid TextBox_btn_OneCell_width\" ");
          strHtml.append("name=\"inpParam").append(FormatUtilities.replace(fields[i].columnname)).append("_f\" ");
          strHtml.append("maxlength=\"").append(fields[i].fieldlength).append("\" ");
          strHtml.append("value=\"").append(value).append("\" ");
          if (Utility.isDecimalNumber(fields[i].reference)) strHtml.append("onkeydown=\"validateNumberBox(this.id);auto_completar_numero(this, true, true);return true;\" ");
          else if (Utility.isIntegerNumber(fields[i].reference)) strHtml.append("onkeydown=\"validateNumberBox(this.id);auto_completar_numero(this, false, false);return true;\" ");
          strHtml.append(">");

          strHtml.append("<td class=\"FieldButton_ContentCell\">\n<TABLE class=\"FieldButton\" onclick=\"calculator('frmMain.");
          strHtml.append("inpParam").append(FormatUtilities.replace(fields[i].columnname)).append("_f', "); 
          strHtml.append("document.frmMain.inpParam").append(FormatUtilities.replace(fields[i].columnname)).append("_f.value, false);return false;\" ");
          strHtml.append("onmouseout=\"this.className='FieldButton';window.status='';return true;\" onmouseover=\"this.className='FieldButton_hover';window.status='Show calculator';return true;\" onmousedown=\"this.className='FieldButton_active';return true;\" onmouseup=\"this.className='FieldButton';return true;\">");
          strHtml.append("<tr>\n<td class=\"FieldButton_bg\">\n");
          strHtml.append("<IMG alt=\"Calculator\" class=\"FieldButton_Icon FieldButton_Icon_Calc\" title=\"Calculator\" src=\"").append(strReplaceWith).append("/images/blank.gif\" border=\"0\"></IMG>\n");
          strHtml.append("</td>\n</tr>\n</table>\n</td>\n</tr>\n</table>\n");
          strHtml.append("<SPAN class=\"invalid\" style=\"display: none;\">* The value entered is not valid.</SPAN>");
          strHtml.append("<SPAN class=\"missing\" style=\"display: none;\">* This value is required.</SPAN>");
          strHtml.append("<SPAN class=\"range\" style=\"display: none;\">* This value is out of range.</SPAN>");
          strHtml.append("</td>");
        } else if (fields[i].reference.equals("15")) { //DATE
          strHtml.append("<td class=\"TextBox_btn_ContentCell\">\n");
          strHtml.append("<table border=\"0\" cellspacing=\"0\" cellpadding=\"0\" summary=\"\"  style=\"padding-top: 0px;\">\n");
          strHtml.append("<tr>\n");
          strHtml.append("<TD class=\"TextBox_ContentCell\">\n");
          strHtml.append("<input dojoType=\"openbravo:DateTextbox\" type=\"text\" class=\"TextBox_btn_OneCell_width\" ");
          strHtml.append("displayFormat=\"").append(FormatUtilities.replace(vars.getSessionValue("#AD_SqlDateFormat"))).append("\" ");
          strHtml.append("saveFormat=\"").append(FormatUtilities.replace(vars.getSessionValue("#AD_SqlDateFormat"))).append("\" ");
          strHtml.append("name=\"inpParam").append(FormatUtilities.replace(fields[i].columnname)).append("_f\" ");
          strHtml.append("maxlength=\"").append(fields[i].fieldlength).append("\" ");
          strHtml.append("value=\"").append(value).append("\" ");
          strHtml.append("id=\"inpParam").append(FormatUtilities.replace(fields[i].columnname)).append("_f\" ");
          strHtml.append("onkeyup=\"auto_complete_date(this.textbox);\"></input> ");
          strHtml.append("<script>djConfig.searchIds.push(\"").append("inpParam").append(FormatUtilities.replace(fields[i].columnname)).append("_f\") </script>");
          strHtml.append("</td>\n");
          strHtml.append("<td class=\"FieldButton_ContentCell\">");
          strHtml.append("<a href=\"#\" class=\"FieldButtonLink\" onclick=\"showCalendar('frmMain.inpParam").append(FormatUtilities.replace(fields[i].columnname)).append("_f', ");
          strHtml.append("document.frmMain.inpParam").append(FormatUtilities.replace(fields[i].columnname)).append("_f.value, false, '").append(FormatUtilities.replace(vars.getSessionValue("#AD_SqlDateFormat"))).append("');");
          strHtml.append("return false;\" onfocus=\"this.className='FieldButtonLink_hover'; window.status='Calendar'; return true;\" onblur=\"this.className='FieldButtonLink'; window.status=''; return true;\" onkeypress=\"this.className='FieldButtonLink_active'; return true;\" onkeyup=\"this.className='FieldButtonLink_hover'; return true;\">\n");
          strHtml.append("<table class=\"FieldButton\" onmouseout=\"this.className='FieldButton';window.status='';return true;\" onmouseover=\"this.className='FieldButton_hover';window.status='Show calendar';return true;\" onmousedown=\"this.className='FieldButton_active';return true;\" onmouseup=\"this.className='FieldButton';return true;\">\n");
          strHtml.append("<tr>\n");
          strHtml.append("<td class=\"FieldButton_bg\">");
          strHtml.append("<IMG alt=\"Calendar\" class=\"FieldButton_Icon FieldButton_Icon_Calendar\" title=\"Calendar\" src=\"").append(strReplaceWith).append("/images/blank.gif\" border=\"0\"></IMG>\n");
          strHtml.append("</td>\n");
          strHtml.append("</tr>\n");
          strHtml.append("</table>\n");
          strHtml.append("</a>\n");
          strHtml.append("</td>\n");
          strHtml.append("</tr>\n");
          strHtml.append("</table>\n");
          strHtml.append("</td>\n");
        } else {
          strHtml.append("<input type=\"text\" ");
          strHtml.append("name=\"inpParam").append(FormatUtilities.replace(fields[i].columnname)).append("_f\" ");
          strHtml.append(fields[i].fieldlength).append("\" ");
          strHtml.append("value=\"").append(value).append("\" ");
          strHtml.append(">");
        }
        //strHtml.append("</td></tr></table>\n");
      }
      //strHtml.append("</td></tr></table>\n");
      strHtml.append("</td></tr>\n");
    }
    vecKeys.addElement("new keyArrayItem(\"B\", \"aceptar()\", null, \"ctrlKey\")");
    vecKeys.addElement("new keyArrayItem(\"ESCAPE\", \"window.close()\", null, null)");
    if (vecKeys.size()>0) {
      script.append("var keyArray = new Array();\n");
      script.append("function enableLocalShortcuts() {\n");
      script.append("\n");
      for (int i=0;i<vecKeys.size();i++) {
        script.append("keyArray[").append(i).append("] = ").append(vecKeys.elementAt(i).toString()).append(";\n");
      }
      script.append("enableShortcuts();\n");
      script.append("}\n");
    }
    StringBuffer scrScr = new StringBuffer();
    if (scriptKeyboard) {
      scrScr.append("<SCRIPT language=\"JavaScript\" src=\"").append(strReplaceWith).append("/js/keyboard.js\" type=\"text/javascript\"></SCRIPT>");
      scrScr.append("<SCRIPT language=\"JavaScript\" src=\"").append(strReplaceWith).append("/js/keys.js\" type=\"text/javascript\"></SCRIPT>");
    }
    if (scriptClock) {
      scrScr.append("<SCRIPT language=\"JavaScript\" src=\"").append(strReplaceWith).append("/js/time.js\" type=\"text/javascript\"></SCRIPT>");
    }
    if (scriptCalendar) {
      scrScr.append("<SCRIPT language=\"JavaScript\" src=\"").append(strReplaceWith).append("/js/jscalendar/calendar.js\" type=\"text/javascript\"></SCRIPT>\n");
      scrScr.append("<SCRIPT language=\"JavaScript\" src=\"").append(strReplaceWith).append("/js/jscalendar/lang/calendar-").append(vars.getLanguage().substring(0,2)).append(".js\" type=\"text/javascript\"></SCRIPT>\n");
      scrScr.append("<SCRIPT language=\"JavaScript\" src=\"").append(strReplaceWith).append("/js/default/DateTextBox.js\" type=\"text/javascript\"></SCRIPT>");
    }
    if (scriptCalculator) {
      scrScr.append("<SCRIPT language=\"JavaScript\" src=\"").append(strReplaceWith).append("/js/calculator.js\" type=\"text/javascript\"></SCRIPT>");
    }
    if (scriptSearch) {
      scrScr.append("<SCRIPT language=\"JavaScript\" src=\"").append(strReplaceWith).append("/js/searchs.js\" type=\"text/javascript\"></SCRIPT>");
    }
    if (scriptSelect) {
      scrScr.append("<SCRIPT language=\"JavaScript\" src=\"").append(strReplaceWith).append("/js/String.js\" type=\"text/javascript\"></SCRIPT>");
      scrScr.append("<SCRIPT language=\"JavaScript\" src=\"").append(strReplaceWith).append("/js/TypeAheadCombo.js\" type=\"text/javascript\"></SCRIPT>");
    }
    vecScript.addElement(scrScr);
    return strHtml.toString();
  }

  public String locationCommands(BuscadorData efd) {
    StringBuffer html = new StringBuffer();

    html.append("openLocation(null, null, '../info/Location_FS.html', null, false, 'frmMain', 'inpParam").append(FormatUtilities.replace(efd.columnname)).append("', 'inpParam").append(FormatUtilities.replace(efd.columnname)).append("_DES', document.frmMain.inpParam").append(FormatUtilities.replace(efd.columnname)).append(".value, 'inpwindowId', document.frmMain.inpwindowId.value);");
    return html.toString();
  }

  public String location(BuscadorData efd) {
    StringBuffer html = new StringBuffer();
    html.append("<td class=\"FieldButton_bg\"><a href=\"#\"");
    html.append("<a href=\"#\"");
    html.append("onClick=\"").append(locationCommands(efd)).append("return false;\" ");
    html.append("onmouseout=\"this.className='FieldButton';window.status='';return true;\" onmouseover=\"this.className='FieldButton_hover';window.status='Search';return true;\" onmousedown=\"this.className='FieldButton_active';return true;\" onmouseup=\"this.className='FieldButton';return true;\">\n<img width=\"16\" height=\"16\" alt=\"").append(efd.searchname.trim()).append("\" title=\"").append(efd.searchname.trim()).append("\" ");
    html.append("class=\"FieldButton_Icon FieldButton_Icon_").append(FormatUtilities.replace(efd.searchname.trim())).append("\" ");;
    html.append("border=\"0\" src=\"").append(strReplaceWith).append("/images/blank.gif\"></a>");
    return html.toString();
  }

  public String searchsCommand(BuscadorData efd, boolean fromButton, String tabId, String windowId, String strIsSOTrx) {
    StringBuffer params = new StringBuffer();
    StringBuffer html = new StringBuffer();
    String strMethodName = "openSearch";
    if (!fromButton) {
      params.append(", 'Command'");
      params.append(", 'KEY'");
    }
    params.append(", 'WindowID'");
    params.append(", '").append(windowId).append("'");
    if (!strIsSOTrx.equals("")) {
      params.append(", 'inpisSOTrxTab'");
      params.append(", '").append(strIsSOTrx).append("'");
    }
    String searchName = (efd.reference.equals("25")?"/info/Account":("/info/" + (efd.reference.equals("800011")?"ProductComplete":FormatUtilities.replace(efd.searchname.trim())))) + "_FS.html";
    BuscadorData[] data = null;
    try {
      data = BuscadorData.selectSearchs(this, "I", efd.referencevalue);
    } catch (ServletException ex) {
      ex.printStackTrace();
    }
    if (data!=null && data.length>0) {
      searchName = data[0].mappingname;
    }

    if (efd.searchname.toUpperCase().startsWith("ATTRIBUTE")) {
      strMethodName = "openPAttribute";
      params.append(", 'inpKeyValue'");
      params.append(", document.frmMain.inpParam").append(FormatUtilities.replace(efd.columnname)).append(".value");
      params.append(", 'inpwindowId'");
      params.append(", '").append(windowId).append("'");
      params.append(", 'inpProduct'");
      params.append(", document.frmMain.inpParam").append(FormatUtilities.replace("M_Product_ID")).append(".value");
      params.append(", 'inpLocatorId'");
      params.append(", ((document.frmMain.inpParam").append(FormatUtilities.replace("M_Locator_ID"));
      params.append("!=null)?document.frmMain.inpParam");
      params.append(FormatUtilities.replace("M_Locator_ID")).append(".value:'')");
    }
    html.append(strMethodName).append("(null, null, '..").append(searchName).append("', null, false, 'frmMain', 'inpParam").append(FormatUtilities.replace(efd.columnname)).append("', 'inpParam").append(FormatUtilities.replace(efd.columnname)).append("_DES', document.frmMain.inpParam").append(FormatUtilities.replace(efd.columnname)).append("_DES.value").append(params.toString()).append(");");
    return html.toString();
  }

  public String searchs(BuscadorData efd, String tabId, String windowId, String strIsSOTrx) {
    StringBuffer html = new StringBuffer();
    if (efd.searchname.toUpperCase().indexOf("BUSINESS")!=-1) {
      html.append("<input type=\"hidden\" name=\"inpParam").append(FormatUtilities.replace(efd.columnname)).append("_LOC\">\n");
      html.append("<input type=\"hidden\" name=\"inpParam").append(FormatUtilities.replace(efd.columnname)).append("_CON\">\n");
    } else if (efd.searchname.equalsIgnoreCase("PRODUCT")) {
      html.append("<input type=\"hidden\" name=\"inpParam").append(FormatUtilities.replace(efd.columnname)).append("_PLIST\">\n");
      html.append("<input type=\"hidden\" name=\"inpParam").append(FormatUtilities.replace(efd.columnname)).append("_PSTD\">\n");
      html.append("<input type=\"hidden\" name=\"inpParam").append(FormatUtilities.replace(efd.columnname)).append("_UOM\">\n");
      html.append("<input type=\"hidden\" name=\"inpParam").append(FormatUtilities.replace(efd.columnname)).append("_PLIM\">\n");
      html.append("<input type=\"hidden\" name=\"inpParam").append(FormatUtilities.replace(efd.columnname)).append("_CURR\">\n");
    }
    html.append("<td class=\"FieldButton_bg\"><a href=\"#\"");
    html.append("onClick=\"").append(searchsCommand(efd, true, tabId, windowId, strIsSOTrx)).append("return false;\" ");
    html.append("onmouseout=\"this.className='FieldButton';window.status='';return true;\" onmouseover=\"this.className='FieldButton_hover';window.status='Search';return true;\" onmousedown=\"this.className='FieldButton_active';return true;\" onmouseup=\"this.className='FieldButton';return true;\">\n<img width=\"16\" height=\"16\" alt=\"").append(efd.searchname.trim()).append("\" title=\"").append(efd.searchname.trim()).append("\" ");
    html.append("class=\"FieldButton_Icon FieldButton_Icon_").append(FormatUtilities.replace(efd.searchname.trim())).append("\" ");;
    html.append("border=\"0\" src=\"").append(strReplaceWith).append("/images/blank.gif\"></a></td></table>");
    return html.toString();
  }

  public String locatorCommands(BuscadorData efd, boolean fromButton, String windowId) {
    StringBuffer params = new StringBuffer();
    StringBuffer html = new StringBuffer();
    if (!fromButton) {
      params.append(", 'Command'");
      params.append(", 'KEY'");
    }
    params.append(", 'WindowID'");
    params.append(", '").append(windowId).append("'");
    html.append("openSearch(null, null, '../info/Locator_FS.html', null, false, 'frmMain', 'inpParam").append(FormatUtilities.replace(efd.columnname)).append("', 'inpParam").append(FormatUtilities.replace(efd.columnname)).append("_DES', document.frmMain.inpParam").append(FormatUtilities.replace(efd.columnname)).append("_DES.value").append(params.toString()).append(");");
    return html.toString();
  }

  public String locator(BuscadorData efd, String windowId) {
    StringBuffer html = new StringBuffer();
    html.append("<td class=\"FieldButton_bg\"><a href=\"#\"");
    html.append("onClick=\"").append(locatorCommands(efd, true, windowId)).append("return false;\" ");
    html.append("onmouseout=\"this.className='FieldButton';window.status='';return true;\" onmouseover=\"this.className='FieldButton_hover';window.status='Search';return true;\" onmousedown=\"this.className='FieldButton_active';return true;\" onmouseup=\"this.className='FieldButton';return true;\">\n");;
    html.append("<img width=\"16\" height=\"16\" alt=\"").append(efd.searchname.trim()).append("\" title=\"").append(efd.searchname.trim()).append("\" ");
    html.append("class=\"FieldButton_Icon FieldButton_Icon_").append(FormatUtilities.replace(efd.searchname.trim())).append("\" ");;
    html.append("border=\"0\" src=\"").append(strReplaceWith).append("/images/blank.gif\"></a></td>");
    return html.toString();
  }

  public String getServletInfo() {
		// FIXME: Should be in English
    return "Servlet que presenta el buscador";
  } // end of getServletInfo() method
}
