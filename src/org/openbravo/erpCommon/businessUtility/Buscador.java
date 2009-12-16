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
 * All portions are Copyright (C) 2001-2009 Openbravo SL 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.erpCommon.businessUtility;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Random;
import java.util.Vector;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.data.FieldProvider;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.utility.ComboTableData;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.utils.FormatUtilities;
import org.openbravo.xmlEngine.XmlDocument;

public class Buscador extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;
  private static final int MAX_TEXTBOX_LENGTH = 150;
  private static final int MAX_TEXTBOX_DISPLAY = 30;

  public void init(ServletConfig config) {
    super.init(config);
    boolHist = false;
  }

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (vars.commandIn("DEFAULT")) {
      vars.setSessionValue("Buscador.inpTabId", vars.getRequiredStringParameter("inpTabId"));
      vars.setSessionValue("Buscador.inpWindow", vars.getRequiredStringParameter("inpWindow"));
      vars.setSessionValue("Buscador.inpWindowId", vars.getStringParameter("inpWindowId"));

      printPageFS(response, vars);
    }

    if (vars.commandIn("FRAME1")) {
      String strTab = vars.getSessionValue("Buscador.inpTabId");
      String strWindow = vars.getSessionValue("Buscador.inpWindow");
      String strWindowId = vars.getSessionValue("Buscador.inpWindowId");
      String strIsSOTrx = vars.getSessionValue(strWindowId + "|issotrxtab");
      String strShowAudit = Utility.getContext(this, vars, "ShowAudit", strWindowId);
      BuscadorData[] data;

      // assumption Buscador servlet is only called from generated windows
      // get url path working on windows in core & modules and use this instead
      // of the incoming window path
      // always use _Relation form url as it matches old behavior in ToolBar.java and
      // both views are mapped to the same servlet
      strWindow = Utility.getTabURL(this, strTab, "R");

      if (!BuscadorData.hasSelectionColumns(this, strTab).equals("0"))
        data = BuscadorData.select(this, vars.getLanguage(), strTab, strShowAudit);
      else
        data = BuscadorData.selectIdentifiers(this, vars.getLanguage(), strTab, strShowAudit);

      if (data == null || data.length == 0) {
        if (log4j.isDebugEnabled())
          log4j.debug("there're no selection columns and no identifiers defined for this table");
        bdError(request, response, "SearchNothing", vars.getLanguage());
      } else {
        data = removeParents(data, strTab);
        if (loadParameters(vars, data, strTab)) {
          for (int i = 0; i < data.length; i++) {
            if (data[i].reference.equals("10") || data[i].reference.equals("14")
                || data[i].reference.equals("34"))
              data[i].value = "%";
            else
              data[i].value = "";
          }
        }
        if (data == null || data.length == 0) {
          if (log4j.isDebugEnabled())
            log4j.debug("The columns defined were parent keys");
          bdError(request, response, "SearchNothing", vars.getLanguage());
        } else
          printPage(response, vars, strTab, data, strWindow, strWindowId, strIsSOTrx);
      }
    } else
      pageError(response);
  }

  private void printPageFS(HttpServletResponse response, VariablesSecureApp vars)
      throws IOException, ServletException {

    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/businessUtility/Buscador_FS").createXmlDocument();

    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  private BuscadorData[] removeParents(BuscadorData[] data, String strTab) throws ServletException {
    String parentColumn = BuscadorData.parentsColumnName(this, strTab);
    if (data == null || data.length == 0)
      return data;
    if (parentColumn.equals(""))
      return data;
    Vector<Object> vec = new Vector<Object>();
    BuscadorData[] result = null;
    for (int i = 0; i < data.length; i++) {
      if (!parentColumn.equalsIgnoreCase(data[i].columnname)
          || data[i].isselectioncolumn.equals("Y"))
        vec.addElement(data[i]);
    }
    if (vec.size() > 0) {
      result = new BuscadorData[vec.size()];
      vec.copyInto(result);
    }
    return result;
  }

  private boolean loadParameters(VariablesSecureApp vars, BuscadorData[] data, String strTab)
      throws ServletException {
    if (data == null || data.length == 0)
      return false;
    boolean isEmpty = true;
    for (int i = 0; i < data.length; i++) {
      data[i].value = vars.getSessionValue(strTab + "|param"
          + FormatUtilities.replace(data[i].columnname));
      if (!data[i].value.equals(""))
        isEmpty = false;
    }
    return isEmpty;
  }

  private void printPage(HttpServletResponse response, VariablesSecureApp vars, String strTab,
      BuscadorData[] data, String strWindow, String strWindowId, String strIsSOTrx)
      throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: Frame 1 of the attributes seeker");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/businessUtility/Buscador").createXmlDocument();

    xmlDocument.setParameter("calendar", vars.getLanguage().substring(0, 2));
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("theme", vars.getTheme());
    StringBuffer script = new StringBuffer();
    Vector<StringBuffer> vecScript = new Vector<StringBuffer>();
    xmlDocument.setParameter("data", generateHtml(vars, data, strTab, strWindowId, script,
        strIsSOTrx, vecScript));
    xmlDocument.setParameter("scripsJS", vecScript.elementAt(0).toString());
    xmlDocument.setParameter("script",
        (script.toString() + generateScript(data, strWindow, strTab)));
    xmlDocument.setParameter("tab", strTab);
    xmlDocument.setParameter("window", strWindow);
    {
      OBError myMessage = vars.getMessage(strTab);
      vars.removeMessage(strTab);
      if (myMessage != null) {
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

  private String generateScript(BuscadorData[] fields, String strWindow, String strTab) {
    StringBuffer strHtml = new StringBuffer();
    StringBuffer strCombo = new StringBuffer();
    strHtml.append("function aceptar() {\n");
    strHtml.append("  var frm = document.forms[0];\n");
    strHtml.append("  var paramsData = new Array();\n");
    strHtml.append("  var count = 0;\n");
    boolean isHighVolume = false;
    try {
      isHighVolume = BuscadorData.isHighVolume(this, strTab).equals("Y");
    } catch (ServletException e) {
      log4j.error(e);
    }
    StringBuffer paramsData = new StringBuffer();
    StringBuffer params = new StringBuffer();
    if (fields != null && fields.length != 0 && isHighVolume) {
      for (int i = 0; i < fields.length; i++) {

        paramsData.append("paramsData[count++] = new Array(\"inpParam").append(
            FormatUtilities.replace(fields[i].columnname)).append("\" , ");
        params.append(", \"inpParam").append(FormatUtilities.replace(fields[i].columnname)).append(
            "\",");
        params.append(" escape(");
        if (fields[i].reference.equals("17")
            || fields[i].reference.equals("18")
            || fields[i].reference.equals("19")
            || fields[i].equals("20")
            || (fields[i].reference.equals("30") && (fields[i].referencevalue == null || fields[i].referencevalue
                .equals("")))) { // Combo
          paramsData.append("((frm.inpParam").append(FormatUtilities.replace(fields[i].columnname))
              .append(".selectedIndex!=-1)?");
          paramsData.append("frm.inpParam").append(FormatUtilities.replace(fields[i].columnname))
              .append(".options[");
          paramsData.append("frm.inpParam").append(FormatUtilities.replace(fields[i].columnname))
              .append(".selectedIndex].value:");
          paramsData.append("\"\"));\n");
          params.append("((frm.inpParam").append(FormatUtilities.replace(fields[i].columnname))
              .append(".selectedIndex!=-1)?");
          params.append("frm.inpParam").append(FormatUtilities.replace(fields[i].columnname))
              .append(".options[");
          params.append("frm.inpParam").append(FormatUtilities.replace(fields[i].columnname))
              .append(".selectedIndex].value:");
          params.append("\"\")");
        } else if (Utility.isDecimalNumber(fields[i].reference)
            || Utility.isIntegerNumber(fields[i].reference)
            || Utility.isDateTime(fields[i].reference)) {
          paramsData.append("frm.inpParam").append(FormatUtilities.replace(fields[i].columnname))
              .append(".value").append(");\n");
          paramsData.append("paramsData[count++] = new Array(\"inpParam").append(
              FormatUtilities.replace(fields[i].columnname)).append("_f\", ");
          paramsData.append("frm.inpParam").append(FormatUtilities.replace(fields[i].columnname))
              .append("_f.value);\n");
          params.append("frm.inpParam").append(FormatUtilities.replace(fields[i].columnname))
              .append(".value");
          params.append("), \"inpParam").append(FormatUtilities.replace(fields[i].columnname))
              .append("_f\",");
          params.append(" escape(");
          params.append("frm.inpParam").append(FormatUtilities.replace(fields[i].columnname))
              .append("_f.value");
        } else {
          paramsData.append("frm.inpParam").append(FormatUtilities.replace(fields[i].columnname))
              .append(".value);\n");
          params.append("frm.inpParam").append(FormatUtilities.replace(fields[i].columnname))
              .append(".value");
        }
        params.append(")");

      }
    } else if (fields != null) {
      for (int i = 0; i < fields.length; i++) {
        paramsData.append("paramsData[count++] = new Array(\"inpParam").append(
            FormatUtilities.replace(fields[i].columnname)).append("\", ");
        params.append(", \"inpParam").append(FormatUtilities.replace(fields[i].columnname)).append(
            "\"");
        params.append(", ");
        params.append("escape(");
        if (fields[i].reference.equals("17")
            || fields[i].reference.equals("18")
            || fields[i].reference.equals("19")
            || fields[i].reference.equals("20")
            || (fields[i].reference.equals("30") && (fields[i].referencevalue == null || fields[i].referencevalue
                .equals("")))) { // Combo
          paramsData.append("((frm.inpParam").append(FormatUtilities.replace(fields[i].columnname))
              .append(".selectedIndex!=-1)?");
          paramsData.append("frm.inpParam").append(FormatUtilities.replace(fields[i].columnname))
              .append(".options[");
          paramsData.append("frm.inpParam").append(FormatUtilities.replace(fields[i].columnname))
              .append(".selectedIndex].value:");
          paramsData.append("\"\"));\n");
          params.append("((frm.inpParam").append(FormatUtilities.replace(fields[i].columnname))
              .append(".selectedIndex!=-1)?");
          params.append("frm.inpParam").append(FormatUtilities.replace(fields[i].columnname))
              .append(".options[");
          params.append("frm.inpParam").append(FormatUtilities.replace(fields[i].columnname))
              .append(".selectedIndex].value:");
          params.append("\"\")");
        } else if (Utility.isDecimalNumber(fields[i].reference)
            || Utility.isIntegerNumber(fields[i].reference)
            || Utility.isDateTime(fields[i].reference)) {
          paramsData.append("frm.inpParam").append(FormatUtilities.replace(fields[i].columnname))
              .append(".value);\n");
          paramsData.append("paramsData[count++] = new Array(\"inpParam").append(
              FormatUtilities.replace(fields[i].columnname)).append("_f\", ");
          paramsData.append("frm.inpParam").append(FormatUtilities.replace(fields[i].columnname))
              .append("_f.value);\n");
          params.append("frm.inpParam").append(FormatUtilities.replace(fields[i].columnname))
              .append(".value");
          params.append("), \"inpParam").append(FormatUtilities.replace(fields[i].columnname))
              .append("_f\",");
          params.append(" escape(");
          params.append("frm.inpParam").append(FormatUtilities.replace(fields[i].columnname))
              .append("_f.value");
        } else {
          paramsData.append("frm.inpParam").append(FormatUtilities.replace(fields[i].columnname))
              .append(".value);\n");
          params.append("frm.inpParam").append(FormatUtilities.replace(fields[i].columnname))
              .append(".value");
        }
        params.append(")");
      }
    }
    strHtml.append("\n").append(paramsData);
    strHtml
        .append("  if (parent.window.opener.selectFilters) parent.window.opener.selectFilters(paramsData);\n");
    strHtml.append("  else parent.window.opener.submitFormGetParams(\"SEARCH\", \"" + strWindow
        + "\"" + params.toString() + ");\n");
    strHtml.append("  parent.window.close();\n");
    strHtml.append("  return true;\n");
    strHtml.append("}\n");

    strHtml.append("\nfunction reloadComboReloads(changedField) {\n");
    strHtml.append("  submitCommandForm(changedField, false, null, '../ad_callouts/ComboReloads"
        + strTab + ".html', 'hiddenFrame', null, null, true);\n");
    strHtml.append("  return true;\n");
    strHtml.append("}\n");

    strHtml.append("function onloadFunctions() {\n");
    strHtml.append("  enableLocalShortcuts();\n");
    strHtml.append(strCombo);
    strHtml.append("  return true;\n");
    strHtml.append("}\n");
    return strHtml.toString();
  }

  private String generateHtml(VariablesSecureApp vars, BuscadorData[] fields, String strTab,
      String strWindow, StringBuffer script, String strIsSOTrx, Vector<StringBuffer> vecScript)
      throws IOException, ServletException {
    if (fields == null || fields.length == 0)
      return "";
    StringBuffer strHtml = new StringBuffer();
    boolean scriptCalendar = false;
    boolean scriptClock = false;
    boolean scriptCalculator = false;
    boolean scriptTime = false;
    boolean scriptKeyboard = false;
    boolean scriptSearch = false;
    boolean scriptSelect = false;
    int randomId4Num1 = 0;
    int randomId4Num2 = 0;
    int randomId4Num3 = 0;
    Random rnd = new Random();
    Vector<Object> vecKeys = new Vector<Object>();

    // get list of fields with comboreloads
    Vector<String> comboReloadFields = getComboReloadFields(this, strTab);

    // store in session all the fields in the pup up, to be used when loading session parameters
    StringBuffer strAllFields = new StringBuffer();
    for (BuscadorData field : fields) {
      strAllFields.append("|").append(field.columnname).append("|");
    }
    vars.setSessionValue("buscador.searchFilds", strAllFields.toString());

    for (int i = 0; i < fields.length; i++) {
      randomId4Num1 = rnd.nextInt(10000);
      randomId4Num2 = rnd.nextInt(10000);
      randomId4Num3 = rnd.nextInt(10000);
      if (Integer.valueOf(fields[i].displaylength).intValue() > MAX_TEXTBOX_DISPLAY)
        fields[i].displaylength = Integer.toString(MAX_TEXTBOX_DISPLAY);
      strHtml.append("<tr><td class=\"TitleCell\"> <span class=\"LabelText\">");
      if (Utility.isDecimalNumber(fields[i].reference)
          || Utility.isIntegerNumber(fields[i].reference)
          || Utility.isDateTime(fields[i].reference))
        strHtml.append(fields[i].name).append(" ").append(
            Utility.messageBD(this, "From", vars.getLanguage()));
      else
        strHtml.append(fields[i].name);
      strHtml.append("</span></td>\n");
      if (fields[i].reference.equals("17")
          || fields[i].reference.equals("18")
          || fields[i].reference.equals("19")
          || fields[i].reference.equals("20")
          || (fields[i].reference.equals("30") && (fields[i].referencevalue == null || fields[i].referencevalue
              .equals("")))) {// List,
        // Table,
        // TableDir, Yes/No, direct search
        scriptSelect = true;
        strHtml.append("<td class=\"Combo_ContentCell\" colspan=\"3\">");
        strHtml.append("<select ");
        strHtml.append("name=\"inpParam").append(FormatUtilities.replace(fields[i].columnname))
            .append("\" ");
        // attach comboReload call if needed
        if (isInVector(comboReloadFields, fields[i].columnname)) {
          strHtml.append("onchange=\"reloadComboReloads(this.name);return true; \" id=\"idParam")
              .append(FormatUtilities.replace(fields[i].columnname)).append("\" ");
        } else {
          strHtml.append("onchange=\"return true; \" id=\"idParam").append(
              FormatUtilities.replace(fields[i].columnname)).append("\" ");
        }
        if (Integer.valueOf(fields[i].fieldlength).intValue() < (MAX_TEXTBOX_LENGTH / 4)) {
          strHtml.append("class=\"Combo Combo_OneCell_width\"");
        } else if (Integer.valueOf(fields[i].fieldlength).intValue() < (MAX_TEXTBOX_LENGTH / 2)) {
          strHtml.append("class=\"Combo Combo_TwoCells_width\"");
        } else {
          strHtml.append("class=\"Combo Combo_ThreeCells_width\"");
        }
        strHtml.append(">");
        strHtml.append("<option value=\"\"></option>\n");
        try {

          String reference;
          if (fields[i].reference.equals("20")) {
            // Special case Yes/No reference: set list reference and select the Yes/No subreference
            reference = "17";
          } else if (fields[i].reference.equals("30")) {
            // Special case Search without search value: use as table dir
            reference = "19";
          } else {
            reference = fields[i].reference;
          }
          String subreference = fields[i].reference.equals("20") ? "47209D76F3EE4B6D84222C5BDF170AA2"
              : fields[i].referencevalue;
          ComboTableData comboTableData = new ComboTableData(vars, this, reference,
              fields[i].columnname, subreference, fields[i].adValRuleId, Utility.getContext(this,
                  vars, "#AccessibleOrgTree", strWindow), Utility.getContext(this, vars,
                  "#User_Client", strWindow), 0);
          comboTableData.fillParametersFromSearch(strTab, strWindow);
          FieldProvider[] data = comboTableData.select(false);
          comboTableData = null;
          for (int j = 0; j < data.length; j++) {
            strHtml.append("<option value=\"");
            strHtml.append(data[j].getField("ID"));
            strHtml.append("\" ");
            if (data[j].getField("ID").equalsIgnoreCase(fields[i].value))
              strHtml.append("selected");
            strHtml.append(">");
            strHtml.append(data[j].getField("NAME"));
            strHtml.append("</option>\n");
          }
        } catch (Exception ex) {
          throw new ServletException(ex);
        }
        strHtml.append("</select>\n");
      } else if (fields[i].reference.equals("15") || fields[i].reference.equals("16")) { // DATE ||
        // Date-time
        scriptCalendar = true;
        strHtml.append("<td class=\"TextBox_btn_ContentCell\">\n");
        strHtml
            .append("<table border=\"0\" cellspacing=\"0\" cellpadding=\"0\" summary=\"\"  style=\"padding-top: 0px;\">\n");
        strHtml.append("<tr>\n");
        strHtml.append("<td class=\"TextBox_ContentCell\">\n");
        strHtml
            .append("<input dojoType=\"openbravo:DateTextbox\" type=\"text\" class=\"TextBox_btn_OneCell_width\" ");
        strHtml.append("displayFormat=\"").append(vars.getSessionValue("#AD_SqlDateFormat"))
            .append("\" ");
        strHtml.append("saveFormat=\"").append(vars.getSessionValue("#AD_SqlDateFormat")).append(
            "\" ");
        strHtml.append("name=\"inpParam").append(FormatUtilities.replace(fields[i].columnname))
            .append("\" ");
        strHtml.append("maxlength=\"").append(vars.getSessionValue("#AD_SqlDateFormat").length())
            .append("\" ");
        strHtml.append("value=\"").append(fields[i].value).append("\" ");
        strHtml.append("id=\"inpParam").append(FormatUtilities.replace(fields[i].columnname))
            .append("\" ");
        strHtml.append("onkeyup=\"autoCompleteDate(this.textbox);\"></input> ");
        strHtml.append("<script>djConfig.searchIds.push(\"").append("inpParam").append(
            FormatUtilities.replace(fields[i].columnname)).append("\") </script>");
        strHtml.append("</td>\n");
        strHtml.append("<td class=\"FieldButton_ContentCell\">");
        strHtml.append(
            "<a href=\"#\" class=\"FieldButtonLink\" onclick=\"showCalendar('frmMain.inpParam")
            .append(FormatUtilities.replace(fields[i].columnname)).append("', ");
        strHtml.append("document.frmMain.inpParam").append(
            FormatUtilities.replace(fields[i].columnname)).append(".value, false, '").append(
            vars.getSessionValue("#AD_SqlDateFormat")).append("');");
        strHtml
            .append("return false;\" onfocus=\"setWindowElementFocus(this); window.status='Calendar'; return true;\" onblur=\"window.status=''; return true;\" onkeypress=\"this.className='FieldButtonLink_active'; return true;\" onkeyup=\"this.className='FieldButtonLink_focus'; return true;\">\n");
        strHtml
            .append("<table class=\"FieldButton\" onmouseout=\"this.className='FieldButton';window.status='';return true;\" onmouseover=\"this.className='FieldButton_hover';window.status='Show calendar';return true;\" onmousedown=\"this.className='FieldButton_active';return true;\" onmouseup=\"this.className='FieldButton';return true;\">\n");
        strHtml.append("<tr>\n");
        strHtml.append("<td class=\"FieldButton_bg\">");
        strHtml
            .append(
                "<img alt=\"Calendar\" class=\"FieldButton_Icon FieldButton_Icon_Calendar\" title=\"Calendar\" src=\"")
            .append(strReplaceWith).append("/images/blank.gif\" border=\"0\"></img>\n");
        strHtml.append("</td>\n");
        strHtml.append("</tr>\n");
        strHtml.append("</table>\n");
        strHtml.append("</a>\n");
        strHtml.append("</td>\n");
        strHtml.append("</tr>\n");
        strHtml.append("</table>\n");
        strHtml.append("</td>\n");
      } else if (fields[i].reference.equals("24")) { // time
        scriptTime = true;
        strHtml.append("<td class=\"TextBox_btn_ContentCell\">\n");
        strHtml
            .append("<table border=\"0\" cellspacing=\"0\" cellpadding=\"0\" summary=\"\"  style=\"padding-top: 0px;\">\n");
        strHtml.append("<tr>\n");
        strHtml.append("<td class=\"TextBox_ContentCell\">\n");
        strHtml
            .append("<input type=\"text\" class=\"dojoValidateValid TextBox_btn_OneCell_width\" ");
        strHtml.append("displayFormat=\"%H:%M:%S\" ");
        strHtml.append("saveFormat=\"%H:%M:%S\" ");
        strHtml.append("name=\"inpParam").append(FormatUtilities.replace(fields[i].columnname))
            .append("\" ");
        strHtml.append("maxlength=\"19\" ");
        strHtml.append("value=\"").append(fields[i].value).append("\" ");
        strHtml.append("id=\"inpParam").append(FormatUtilities.replace(fields[i].columnname))
            .append("\" ");

        strHtml
            .append("onkeyup=\"autoCompleteTime(this);\" onchange=\"validateTimeTextBox(this);logChanges(this);return true;\"></input> ");
        strHtml.append("<script>djConfig.searchIds.push(\"").append("inpParam").append(
            FormatUtilities.replace(fields[i].columnname)).append("\") </script>");
        strHtml.append("</td>\n");
        strHtml.append("</td>\n");
        strHtml.append("</tr>\n");
        strHtml.append("</table>\n");
        strHtml.append("</td>\n");
      } else if (fields[i].reference.equals("30") || fields[i].reference.equals("21")
          || fields[i].reference.equals("31") || fields[i].reference.equals("25")
          || fields[i].reference.equals("800011")) { // Search
        strHtml.append("<td class=\"TextBox_btn_ContentCell\" colspan=\"3\">\n");
        strHtml.append("<table border=\"0\" cellspacing=\"0\" cellpadding=\"0\" summary=\"\">\n");
        scriptSearch = true;
        strHtml.append("<tr>\n<td>\n");
        strHtml.append("<input type=\"hidden\" name=\"inpParam").append(
            FormatUtilities.replace(fields[i].columnname)).append("\" ");
        strHtml.append("value=\"").append(
            (!fields[i].value.equals("") && !fields[i].value.equals("%")) ? fields[i].value : "")
            .append("\">");
        strHtml.append("</td>\n");
        strHtml.append("<td class=\"TextBox_ContentCell\">\n");
        if (Integer.valueOf(fields[i].fieldlength).intValue() < (MAX_TEXTBOX_LENGTH / 4)) {
          strHtml
              .append("<input dojoType=\"openbravo:ValidationTextBox\" type=\"text\" class=\"TextBox_btn_OneCell_width\" ");
        } else if (Integer.valueOf(fields[i].fieldlength).intValue() < (MAX_TEXTBOX_LENGTH / 2)) {
          strHtml
              .append("<input dojoType=\"openbravo:ValidationTextBox\" type=\"text\" class=\"TextBox_btn_TwoCells_width\" ");
        } else {
          strHtml
              .append("<input dojoType=\"openbravo:ValidationTextBox\" type=\"text\" class=\"TextBox_btn_ThreeCells_width\" ");
        }
        strHtml.append("name=\"inpParam").append(FormatUtilities.replace(fields[i].columnname))
            .append("_DES\" ");
        strHtml.append("id=\"inpParam").append(FormatUtilities.replace(fields[i].columnname))
            .append("_DES\" ");
        strHtml.append("maxlength=\"").append(fields[i].fieldlength).append("\" ");

        if (!fields[i].value.equals("") && !fields[i].value.equals("%")) {
          String strSearchTableName = BuscadorData.selectSearchTableName(this,
              fields[i].referencevalue);
          if (strSearchTableName.equals(""))
            strSearchTableName = fields[i].columnname.substring(0,
                fields[i].columnname.length() - 3);
          String strSearchName = BuscadorData.selectSearchName(this, strSearchTableName,
              fields[i].value, vars.getLanguage());
          strHtml.append("value=\"").append(strSearchName).append("\" ");
        }

        strHtml
            .append((!fields[i].reference.equals("21") && !fields[i].reference.equals("35")) ? ""
                : "readonly=\"true\" ");
        strHtml.append("><script>djConfig.searchIds.push(\"").append("inpParam").append(
            FormatUtilities.replace(fields[i].columnname)).append("_DES\") </script></td>\n");
        String strMethod = "";
        if (fields[i].reference.equals("21")) {
          strMethod = locationCommands(fields[i]);
        } else if (fields[i].reference.equals("31")) {
          strMethod = locatorCommands(fields[i], false, strWindow);
        } else {
          strMethod = searchsCommand(fields[i], false, strTab, strWindow, strIsSOTrx);
        }

        strMethod = "new keyArrayItem(\"ENTER\", \"" + strMethod + "\", \"inpParam"
            + FormatUtilities.replace(fields[i].columnname) + "_DES\", \"null\")";
        vecKeys.addElement(strMethod);

        if (fields[i].reference.equals("21")) {
          strHtml.append(location(fields[i]));
        } else if (fields[i].reference.equals("31")) {
          strHtml.append(locator(fields[i], strWindow));
        } else {
          strHtml.append(searchs(fields[i], strTab, strWindow, strIsSOTrx));
        }
      } else if (Utility.isDecimalNumber(fields[i].reference)
          || Utility.isIntegerNumber(fields[i].reference)) {
        scriptCalculator = true;
        if (Utility.isDecimalNumber(fields[i].reference)
            || Utility.isIntegerNumber(fields[i].reference)) {
          strHtml.append("<td class=\"TextBox_btn_ContentCell\">\n");
        } else {
          strHtml.append("<td class=\"TextBox_ContentCell\">\n");
        }
        strHtml
            .append("<table border=\"0\" cellspacing=\"0\" cellpadding=\"0\" summary=\"\" class=\"\">\n");
        strHtml.append("<tr>");
        strHtml.append("<td class=\"TextBox_ContentCell\">");
        strHtml.append("<input type=\"text\" ");
        strHtml.append("class=\"dojoValidateValid TextBox_btn_OneCell_width\" ");
        strHtml.append("name=\"inpParam").append(FormatUtilities.replace(fields[i].columnname))
            .append("\" ");
        strHtml.append("maxlength=\"").append(fields[i].fieldlength).append("\" ");
        strHtml.append("value=\"").append(fields[i].value).append("\" ");
        if (Utility.isDecimalNumber(fields[i].reference))
          strHtml
              .append("outputformat=\"qtyEdition\" ")
              .append(
                  "onfocus=\"numberInputEvent('onfocus', this);\" onblur=\"numberInputEvent('onblur', this);\" onkeydown=\"numberInputEvent('onkeydown', this, event);\" onchange=\"numberInputEvent('onchange', this);\" ")
              .append("id=\"").append(randomId4Num1).append("\" ");
        else if (Utility.isIntegerNumber(fields[i].reference))
          strHtml
              .append("outputformat=\"qtyEdition\" ")
              .append(
                  "onfocus=\"numberInputEvent('onfocus', this);\" onblur=\"numberInputEvent('onblur', this);\" onkeydown=\"numberInputEvent('onkeydown', this, event);\" onchange=\"numberInputEvent('onchange', this);\" ")
              .append("id=\"").append(randomId4Num1).append("\" ");
        strHtml.append(">");
        if (Utility.isDecimalNumber(fields[i].reference)
            || Utility.isIntegerNumber(fields[i].reference)) {
          strHtml.append("<td class=\"FieldButton_ContentCell\">\n");
          strHtml
              .append("<a class=\"FieldButtonLink\" href=\"#\" onfocus=\"setWindowElementFocus(this); window.status='Calculator'; return true;\" onblur=\"window.status=''; return true;\" onkeypress=\"this.className='FieldButtonLink_active'; return true;\" onkeyup=\"this.className='FieldButtonLink_focus'; return true;\" ");
          strHtml.append("onclick=\"calculator('frmMain.");
          strHtml.append("inpParam").append(FormatUtilities.replace(fields[i].columnname)).append(
              "', ");
          strHtml.append("document.frmMain.inpParam").append(
              FormatUtilities.replace(fields[i].columnname)).append(
              ".value, false);return false;\">\n");
          strHtml.append("<table class=\"FieldButton\" onclick=\"calculator('frmMain.");
          strHtml.append("inpParam").append(FormatUtilities.replace(fields[i].columnname)).append(
              "', ");
          strHtml.append("document.frmMain.inpParam").append(
              FormatUtilities.replace(fields[i].columnname)).append(
              ".value, false);return false;\" ");
          strHtml
              .append("onmouseout=\"this.className='FieldButton';window.status='';return true;\" onmouseover=\"this.className='FieldButton_hover';window.status='Show calculator';return true;\" onmousedown=\"this.className='FieldButton_active';return true;\" onmouseup=\"this.className='FieldButton';return true;\">");
          strHtml.append("<tr>\n<td class=\"FieldButton_bg\">\n");
          strHtml
              .append(
                  "<img alt=\"Calculator\" class=\"FieldButton_Icon FieldButton_Icon_Calc\" title=\"Calculator\" src=\"")
              .append(strReplaceWith).append("/images/blank.gif\" border=\"0\"></img>\n");
          strHtml.append("</td>\n</tr>\n</table>\n</td>\n</tr>\n</table>\n</a>\n");
          strHtml.append("<span class=\"invalid\" style=\"display: none;\" id=\"").append(
              randomId4Num1).append("invalidSpan\">* The value entered is not valid.</span>");
          strHtml.append("<span class=\"missing\" style=\"display: none;\" id=\"").append(
              randomId4Num1).append("missingSpan\">* This value is required.</span>");
          strHtml
              .append("<span class=\"range\" style=\"display: none;\">* This value is out of range.</span>");
          strHtml.append("</td>");
        }
      } else if ((Integer.valueOf(fields[i].fieldlength).intValue() > MAX_TEXTBOX_LENGTH)) { // Memo
        // //
        // REplace
        // with
        // reference
        // //
        // 1-2-3
        // cells
        // doing
        // <
        // MAX_TEXTBOX_LENGTH/4
        // /2
        // >
        // /2
        strHtml.append("<td>");
        strHtml
            .append("<textarea class=\"dojoValidateValid TextArea_TwoCells_width TextArea_Medium_height\" ");
        strHtml.append("name=\"inpParam").append(FormatUtilities.replace(fields[i].columnname))
            .append("\" ");
        strHtml.append("cols=\"50\" rows=\"3\" ");
        strHtml.append(">");
        strHtml.append(fields[i].value);
        strHtml.append("</textarea>\n");
      } else {
        strHtml.append("<td class=\"TextBox_ContentCell\">");
        strHtml.append("<input type=\"text\" class=\"dojoValidateValid TextBox_OneCell_width\" ");
        strHtml.append("name=\"inpParam").append(FormatUtilities.replace(fields[i].columnname))
            .append("\" ");
        strHtml.append("maxlength=\"").append(fields[i].fieldlength).append("\" ");
        strHtml.append("value=\"").append(fields[i].value).append("\" ");
        if (Utility.isDecimalNumber(fields[i].reference)) {
          scriptCalculator = true;
          strHtml
              .append("outputformat=\"qtyEdition\" ")
              .append(
                  "onfocus=\"numberInputEvent('onfocus', this);\" onblur=\"numberInputEvent('onblur', this);\" onkeydown=\"numberInputEvent('onkeydown', this, event);\" onchange=\"numberInputEvent('onchange', this);\" ");
          strHtml.append("id=\"").append(randomId4Num2).append("\" ");
        } else if (Utility.isIntegerNumber(fields[i].reference)) {
          scriptCalculator = true;
          strHtml
              .append("outputformat=\"qtyEdition\" ")
              .append(
                  "onfocus=\"numberInputEvent('onfocus', this);\" onblur=\"numberInputEvent('onblur', this);\" onkeydown=\"numberInputEvent('onkeydown', this, event);\" onchange=\"numberInputEvent('onchange', this);\" ");
          strHtml.append("id=\"").append(randomId4Num2).append("\" ");
        }
        strHtml.append(">");
        strHtml.append("</td>");// <td class=\"FieldButton_bg\">");
      }

      if (Utility.isDecimalNumber(fields[i].reference)
          || Utility.isIntegerNumber(fields[i].reference)
          || Utility.isDateTime(fields[i].reference)) {
        String value = vars.getSessionValue(strTab + "|param"
            + FormatUtilities.replace(fields[i].columnname) + "_f");
        strHtml.append("<td class=\"TitleCell\"> <span class=\"LabelText\">");
        strHtml.append(Utility.messageBD(this, "To", vars.getLanguage()));
        strHtml.append("</span></td>\n");

        if (Utility.isDecimalNumber(fields[i].reference)
            || Utility.isIntegerNumber(fields[i].reference)) {
          strHtml.append("<td class=\"TextBox_btn_ContentCell\">\n");
          strHtml
              .append("<table border=\"0\" cellspacing=\"0\" cellpadding=\"0\" summary=\"\" class=\"\">\n");
          strHtml.append("<tr>");
          strHtml.append("<td class=\"TextBox_ContentCell\">");
          strHtml.append("<input type=\"text\" ");
          strHtml.append("class=\"dojoValidateValid TextBox_btn_OneCell_width\" ");
          strHtml.append("name=\"inpParam").append(FormatUtilities.replace(fields[i].columnname))
              .append("_f\" ");
          strHtml.append("maxlength=\"").append(fields[i].fieldlength).append("\" ");
          strHtml.append("value=\"").append(value).append("\" ");
          if (Utility.isDecimalNumber(fields[i].reference))
            strHtml
                .append("outputformat=\"qtyEdition\" ")
                .append(
                    "onfocus=\"numberInputEvent('onfocus', this);\" onblur=\"numberInputEvent('onblur', this);\" onkeydown=\"numberInputEvent('onkeydown', this, event);\" onchange=\"numberInputEvent('onchange', this);\" ")
                .append("id=\"").append(randomId4Num3).append("\" ");
          else if (Utility.isIntegerNumber(fields[i].reference))
            strHtml
                .append("outputformat=\"qtyEdition\" ")
                .append(
                    "onfocus=\"numberInputEvent('onfocus', this);\" onblur=\"numberInputEvent('onblur', this);\" onkeydown=\"numberInputEvent('onkeydown', this, event);\" onchange=\"numberInputEvent('onchange', this);\" ")
                .append("id=\"").append(randomId4Num3).append("\" ");
          strHtml.append(">");

          strHtml
              .append("<td class=\"FieldButton_ContentCell\">\n<table class=\"FieldButton\" onclick=\"calculator('frmMain.");
          strHtml.append("inpParam").append(FormatUtilities.replace(fields[i].columnname)).append(
              "_f', ");
          strHtml.append("document.frmMain.inpParam").append(
              FormatUtilities.replace(fields[i].columnname)).append(
              "_f.value, false);return false;\" ");
          strHtml
              .append("onmouseout=\"this.className='FieldButton';window.status='';return true;\" onmouseover=\"this.className='FieldButton_hover';window.status='Show calculator';return true;\" onmousedown=\"this.className='FieldButton_active';return true;\" onmouseup=\"this.className='FieldButton';return true;\">");
          strHtml.append("<tr>\n<td class=\"FieldButton_bg\">\n");
          strHtml
              .append(
                  "<img alt=\"Calculator\" class=\"FieldButton_Icon FieldButton_Icon_Calc\" title=\"Calculator\" src=\"")
              .append(strReplaceWith).append("/images/blank.gif\" border=\"0\"></img>\n");
          strHtml.append("</td>\n</tr>\n</table>\n</td>\n</tr>\n</table>\n");
          strHtml.append("<span class=\"invalid\" style=\"display: none;\" id=\"").append(
              randomId4Num3).append("invalidSpan\">* The value entered is not valid.</span>");
          strHtml.append("<span class=\"missing\" style=\"display: none;\" id=\"").append(
              randomId4Num3).append("missingSpan\">* This value is required.</span>");
          strHtml
              .append("<span class=\"range\" style=\"display: none;\">* This value is out of range.</span>");
          strHtml.append("</td>");
        } else if (fields[i].reference.equals("15") || fields[i].reference.equals("16")) { // DATE
          strHtml.append("<td class=\"TextBox_btn_ContentCell\">\n");
          strHtml
              .append("<table border=\"0\" cellspacing=\"0\" cellpadding=\"0\" summary=\"\"  style=\"padding-top: 0px;\">\n");
          strHtml.append("<tr>\n");
          strHtml.append("<td class=\"TextBox_ContentCell\">\n");
          strHtml
              .append("<input dojoType=\"openbravo:DateTextbox\" type=\"text\" class=\"TextBox_btn_OneCell_width\" ");
          strHtml.append("displayFormat=\"").append(vars.getSessionValue("#AD_SqlDateFormat"))
              .append("\" ");
          strHtml.append("saveFormat=\"").append(vars.getSessionValue("#AD_SqlDateFormat")).append(
              "\" ");
          strHtml.append("name=\"inpParam").append(FormatUtilities.replace(fields[i].columnname))
              .append("_f\" ");
          strHtml.append("maxlength=\"").append(vars.getSessionValue("#AD_SqlDateFormat").length())
              .append("\" ");
          strHtml.append("value=\"").append(value).append("\" ");
          strHtml.append("id=\"inpParam").append(FormatUtilities.replace(fields[i].columnname))
              .append("_f\" ");
          strHtml.append("onkeyup=\"autoCompleteDate(this.textbox);\"></input> ");
          strHtml.append("<script>djConfig.searchIds.push(\"").append("inpParam").append(
              FormatUtilities.replace(fields[i].columnname)).append("_f\") </script>");
          strHtml.append("</td>\n");
          strHtml.append("<td class=\"FieldButton_ContentCell\">");
          strHtml.append(
              "<a href=\"#\" class=\"FieldButtonLink\" onclick=\"showCalendar('frmMain.inpParam")
              .append(FormatUtilities.replace(fields[i].columnname)).append("_f', ");
          strHtml.append("document.frmMain.inpParam").append(
              FormatUtilities.replace(fields[i].columnname)).append("_f.value, false, '").append(
              vars.getSessionValue("#AD_SqlDateFormat")).append("');");
          strHtml
              .append("return false;\" onfocus=\"setWindowElementFocus(this); window.status='Calendar'; return true;\" onblur=\"window.status=''; return true;\" onkeypress=\"this.className='FieldButtonLink_active'; return true;\" onkeyup=\"this.className='FieldButtonLink_focus'; return true;\">\n");
          strHtml
              .append("<table class=\"FieldButton\" onmouseout=\"this.className='FieldButton';window.status='';return true;\" onmouseover=\"this.className='FieldButton_hover';window.status='Show calendar';return true;\" onmousedown=\"this.className='FieldButton_active';return true;\" onmouseup=\"this.className='FieldButton';return true;\">\n");
          strHtml.append("<tr>\n");
          strHtml.append("<td class=\"FieldButton_bg\">");
          strHtml
              .append(
                  "<img alt=\"Calendar\" class=\"FieldButton_Icon FieldButton_Icon_Calendar\" title=\"Calendar\" src=\"")
              .append(strReplaceWith).append("/images/blank.gif\" border=\"0\"></img>\n");
          strHtml.append("</td>\n");
          strHtml.append("</tr>\n");
          strHtml.append("</table>\n");
          strHtml.append("</a>\n");
          strHtml.append("</td>\n");
          strHtml.append("</tr>\n");
          strHtml.append("</table>\n");
          strHtml.append("</td>\n");
        } else { // time
          strHtml.append("<td class=\"TextBox_btn_ContentCell\">\n");
          strHtml
              .append("<table border=\"0\" cellspacing=\"0\" cellpadding=\"0\" summary=\"\"  style=\"padding-top: 0px;\">\n");
          strHtml.append("<tr>\n");
          strHtml.append("<td class=\"TextBox_ContentCell\">\n");
          strHtml
              .append("<input type=\"text\" class=\"dojoValidateValid TextBox_btn_OneCell_width\" ");
          strHtml.append("displayFormat=\"%H:%M:%S\" ");
          strHtml.append("saveFormat=\"%H:%M:%S\" ");
          strHtml.append("name=\"inpParam").append(FormatUtilities.replace(fields[i].columnname))
              .append("_f\" ");
          strHtml.append("maxlength=\"19\" ");
          strHtml.append("value=\"").append(value).append("\" ");
          strHtml.append("id=\"inpParam").append(FormatUtilities.replace(fields[i].columnname))
              .append("_f\" ");

          strHtml
              .append("onkeyup=\"autoCompleteTime(this);\" onchange=\"validateTimeTextBox(this);logChanges(this);return true;\"></input> ");
          strHtml.append("<script>djConfig.searchIds.push(\"").append("inpParam").append(
              FormatUtilities.replace(fields[i].columnname)).append("\") </script>");
          strHtml.append("</td>\n");
          strHtml.append("</td>\n");
          strHtml.append("</tr>\n");
          strHtml.append("</table>\n");
          strHtml.append("</td>\n");
        }
        // strHtml.append("</td></tr></table>\n");
      }
      // strHtml.append("</td></tr></table>\n");
      strHtml.append("</td></tr>\n");
    }
    // vecKeys.addElement("new keyArrayItem(\"B\", \"aceptar()\", null, \"ctrlKey\")");
    // vecKeys.addElement("new keyArrayItem(\"ESCAPE\", \"window.close()\", null, null)");
    script.append("\nfunction enableLocalShortcuts() {\n");
    if (vecKeys.size() > 0) {
      // script.append("var keyArray = new Array();\n");
      for (int i = 0; i < vecKeys.size(); i++) {
        script.append("  keyArray[keyArray.length] = ").append(vecKeys.elementAt(i).toString())
            .append(";\n");
      }
      // script.append("enableShortcuts();\n");
    } else {
      script.append("\n");
    }
    script.append("}\n");
    StringBuffer scrScr = new StringBuffer();
    if (scriptKeyboard) {
      scrScr.append("<script language=\"JavaScript\" src=\"").append(strReplaceWith).append(
          "/js/keyboard.js\" type=\"text/javascript\"></script>");
      scrScr.append("<script language=\"JavaScript\" src=\"").append(strReplaceWith).append(
          "/js/keys.js\" type=\"text/javascript\"></script>");
    }
    if (scriptClock) {
      scrScr.append("<script language=\"JavaScript\" src=\"").append(strReplaceWith).append(
          "/js/time.js\" type=\"text/javascript\"></script>");
    }
    if (scriptCalendar) {
      scrScr.append("<script language=\"JavaScript\" src=\"").append(strReplaceWith).append(
          "/js/jscalendar/calendar.js\" type=\"text/javascript\"></script>\n");
      scrScr.append("<script language=\"JavaScript\" src=\"").append(strReplaceWith).append(
          "/js/jscalendar/lang/calendar-").append(vars.getLanguage().substring(0, 2)).append(
          ".js\" type=\"text/javascript\"></script>\n");
      scrScr.append("<script language=\"JavaScript\" src=\"").append(strReplaceWith).append(
          "/js/default/DateTextBox.js\" type=\"text/javascript\"></script>");
    }
    if (scriptCalculator) {
      scrScr.append("<script language=\"JavaScript\" src=\"").append(strReplaceWith).append(
          "/js/calculator.js\" type=\"text/javascript\"></script>");
    }
    if (scriptSearch) {
      scrScr.append("<script language=\"JavaScript\" src=\"").append(strReplaceWith).append(
          "/js/searchs.js\" type=\"text/javascript\"></script>");
    }
    if (scriptTime) {
      scrScr.append("<script language=\"JavaScript\" src=\"").append(strReplaceWith).append(
          "/js/default/TimeTextBox.js\" type=\"text/javascript\"></script>");
    }
    if (scriptSelect) {
    }
    vecScript.addElement(scrScr);
    return strHtml.toString();
  }

  private String locationCommands(BuscadorData efd) {
    StringBuffer html = new StringBuffer();

    html.append(
        "openLocation(null, null, '../info/Location.html', null, false, 'frmMain', 'inpParam")
        .append(FormatUtilities.replace(efd.columnname)).append("', 'inpParam").append(
            FormatUtilities.replace(efd.columnname)).append("_DES', document.frmMain.inpParam")
        .append(FormatUtilities.replace(efd.columnname)).append(
            ".value, 'inpwindowId', document.frmMain.inpwindowId.value);");
    return html.toString();
  }

  private String location(BuscadorData efd) {
    StringBuffer html = new StringBuffer();
    html.append("<td class=\"FieldButton_bg\">");
    html.append("<a href=\"#\" class=\"FieldButtonLink\" ");
    html.append("onClick=\"").append(locationCommands(efd)).append("return false;\" ");
    html
        .append(
            "onmouseout=\"this.className='FieldButton';window.status='';return true;\" onmouseover=\"this.className='FieldButton_hover';window.status='Search';return true;\" onmousedown=\"this.className='FieldButton_active';return true;\" onmouseup=\"this.className='FieldButton';return true;\">\n<img width=\"16\" height=\"16\" alt=\"")
        .append(efd.searchname.trim()).append("\" title=\"").append(efd.searchname.trim()).append(
            "\" ");
    html.append("class=\"FieldButton_Icon FieldButton_Icon_").append(
        FormatUtilities.replace(efd.searchname.trim())).append("\" ");
    ;
    html.append("border=\"0\" src=\"").append(strReplaceWith).append("/images/blank.gif\"></a>");
    return html.toString();
  }

  private String searchsCommand(BuscadorData efd, boolean fromButton, String tabId,
      String windowId, String strIsSOTrx) {
    StringBuffer params = new StringBuffer();
    StringBuffer html = new StringBuffer();
    String strMethodName = "openSearch";
    if (!fromButton) {
      params.append(", 'Command'");
      params.append(", 'KEY'");
    }
    params.append(", 'WindowID'");
    params.append(", '").append(windowId).append("'");
    if (strIsSOTrx.equals("Y") || strIsSOTrx.equals("N")) {
      params.append(", 'inpisSOTrxTab'");
      params.append(", '").append(strIsSOTrx).append("'");
    }
    String searchName = (efd.reference.equals("25") ? "/info/Account" : ("/info/" + (efd.reference
        .equals("800011") ? "ProductComplete" : FormatUtilities.replace(efd.searchname.trim()))))
        + ".html";
    BuscadorData[] data = null;
    try {
      data = BuscadorData.selectSearchs(this, "I", efd.referencevalue);
    } catch (ServletException ex) {
      ex.printStackTrace();
    }
    if (data != null && data.length > 0) {
      searchName = data[0].mappingname;
    }

    if (efd.searchname.toUpperCase().startsWith("ATTRIBUTE")) {
      strMethodName = "openPAttribute";
      params.append(", 'inpKeyValue'");
      params.append(", document.frmMain.inpParam").append(FormatUtilities.replace(efd.columnname))
          .append(".value");
      params.append(", 'inpwindowId'");
      params.append(", '").append(windowId).append("'");
      params.append(", 'inpProduct'");
      params.append(", document.frmMain.inpParam").append(FormatUtilities.replace("M_Product_ID"))
          .append(".value");
      params.append(", 'inpLocatorId'");
      params.append(", ((document.frmMain.inpParam")
          .append(FormatUtilities.replace("M_Locator_ID"));
      params.append("!=null)?document.frmMain.inpParam");
      params.append(FormatUtilities.replace("M_Locator_ID")).append(".value:'')");
    }
    html.append(strMethodName).append("(null, null, '..").append(searchName).append(
        "', null, false, 'frmMain', 'inpParam").append(FormatUtilities.replace(efd.columnname))
        .append("', 'inpParam").append(FormatUtilities.replace(efd.columnname)).append(
            "_DES', document.frmMain.inpParam").append(FormatUtilities.replace(efd.columnname))
        .append("_DES.value").append(params.toString()).append(");");
    return html.toString();
  }

  private String searchs(BuscadorData efd, String tabId, String windowId, String strIsSOTrx) {
    StringBuffer html = new StringBuffer();
    if (efd.searchname.toUpperCase().indexOf("BUSINESS") != -1) {
      html.append("<input type=\"hidden\" name=\"inpParam").append(
          FormatUtilities.replace(efd.columnname)).append("_LOC\">\n");
      html.append("<input type=\"hidden\" name=\"inpParam").append(
          FormatUtilities.replace(efd.columnname)).append("_CON\">\n");
    } else if (efd.searchname.equalsIgnoreCase("PRODUCT")) {
      html.append("<input type=\"hidden\" name=\"inpParam").append(
          FormatUtilities.replace(efd.columnname)).append("_PLIST\">\n");
      html.append("<input type=\"hidden\" name=\"inpParam").append(
          FormatUtilities.replace(efd.columnname)).append("_PSTD\">\n");
      html.append("<input type=\"hidden\" name=\"inpParam").append(
          FormatUtilities.replace(efd.columnname)).append("_UOM\">\n");
      html.append("<input type=\"hidden\" name=\"inpParam").append(
          FormatUtilities.replace(efd.columnname)).append("_PLIM\">\n");
      html.append("<input type=\"hidden\" name=\"inpParam").append(
          FormatUtilities.replace(efd.columnname)).append("_CURR\">\n");
    }
    html.append("<td class=\"FieldButton_bg\">");
    html.append("<a href=\"#\" class=\"FieldButtonLink\" ");
    html.append("onClick=\"").append(searchsCommand(efd, true, tabId, windowId, strIsSOTrx))
        .append("return false;\" ");
    html
        .append(
            "onmouseout=\"this.className='FieldButton';window.status='';return true;\" onmouseover=\"this.className='FieldButton_hover';window.status='Search';return true;\" onmousedown=\"this.className='FieldButton_active';return true;\" onmouseup=\"this.className='FieldButton';return true;\">\n<img width=\"16\" height=\"16\" alt=\"")
        .append(efd.searchname.trim()).append("\" title=\"").append(efd.searchname.trim()).append(
            "\" ");
    html.append("class=\"FieldButton_Icon FieldButton_Icon_").append(
        FormatUtilities.replace(efd.searchname.trim())).append("\" ");
    ;
    html.append("border=\"0\" src=\"").append(strReplaceWith).append(
        "/images/blank.gif\"></a></td></table>");
    return html.toString();
  }

  private String locatorCommands(BuscadorData efd, boolean fromButton, String windowId) {
    StringBuffer params = new StringBuffer();
    StringBuffer html = new StringBuffer();
    if (!fromButton) {
      params.append(", 'Command'");
      params.append(", 'KEY'");
    }
    params.append(", 'WindowID'");
    params.append(", '").append(windowId).append("'");
    html.append("openSearch(null, null, '../info/Locator.html', null, false, 'frmMain', 'inpParam")
        .append(FormatUtilities.replace(efd.columnname)).append("', 'inpParam").append(
            FormatUtilities.replace(efd.columnname)).append("_DES', document.frmMain.inpParam")
        .append(FormatUtilities.replace(efd.columnname)).append("_DES.value").append(
            params.toString()).append(");");
    return html.toString();
  }

  private String locator(BuscadorData efd, String windowId) {
    StringBuffer html = new StringBuffer();
    html.append("<td class=\"FieldButton_bg\">");
    html.append("<a href=\"#\"  class=\"FieldButtonLink\" ");
    html.append("onClick=\"").append(locatorCommands(efd, true, windowId)).append(
        "return false;\" ");
    html
        .append("onmouseout=\"this.className='FieldButton';window.status='';return true;\" onmouseover=\"this.className='FieldButton_hover';window.status='Search';return true;\" onmousedown=\"this.className='FieldButton_active';return true;\" onmouseup=\"this.className='FieldButton';return true;\">\n");
    ;
    html.append("<img width=\"16\" height=\"16\" alt=\"").append(efd.searchname.trim()).append(
        "\" title=\"").append(efd.searchname.trim()).append("\" ");
    html.append("class=\"FieldButton_Icon FieldButton_Icon_").append(
        FormatUtilities.replace(efd.searchname.trim())).append("\" ");
    ;
    html.append("border=\"0\" src=\"").append(strReplaceWith).append(
        "/images/blank.gif\"></a></td>");
    return html.toString();
  }

  /**
   * Gets list of fields which have a comboReload associated to trigger reloads of dependent combos.
   * Change in logic needs to be synchronized with copy in wad
   * 
   * @return List of columnnames of fields with have a comboReload associated
   */
  private static Vector<String> getComboReloadFields(ConnectionProvider pool, String strTab)
      throws ServletException {
    final BuscadorData[] dataReload = BuscadorData.selectValidationTab(pool, strTab);

    final Vector<String> vecReloads = new Vector<String>();
    if (dataReload != null && dataReload.length > 0) {
      for (int z = 0; z < dataReload.length; z++) {
        String code = dataReload[z].whereclause
            + ((!dataReload[z].whereclause.equals("") && !dataReload[z].referencevalue.equals("")) ? " AND "
                : "") + dataReload[z].referencevalue;

        if (code.equals("") && dataReload[z].type.equals("R"))
          code = "@AD_Org_ID@";
        getComboReloadText(code, vecReloads, dataReload[z].columnname);
      }
    }
    return vecReloads;
  }

  private static void getComboReloadText(String token, Vector<String> vecComboReload,
      String columnname) {
    int i = token.indexOf("@");
    while (i != -1) {
      token = token.substring(i + 1);
      if (!token.startsWith("SQL")) {
        i = token.indexOf("@");
        if (i != -1) {
          String strAux = token.substring(0, i);
          token = token.substring(i + 1);
          getComboReloadTextTranslate(strAux, vecComboReload, columnname);
        }
      }
      i = token.indexOf("@");
    }
  }

  private static void getComboReloadTextTranslate(String token, Vector<String> vecComboReload,
      String columnname) {
    if (token == null || token.trim().equals(""))
      return;
    if (!token.equalsIgnoreCase(columnname))
      if (!isInVector(vecComboReload, token))
        vecComboReload.addElement(token);
  }

  private static boolean isInVector(Vector<String> vec, String field) {
    for (String aux : vec) {
      if (aux.equalsIgnoreCase(field))
        return true;
    }
    return false;
  }

  public String getServletInfo() {
    return "Servlet to render the search popup";
  }
}
