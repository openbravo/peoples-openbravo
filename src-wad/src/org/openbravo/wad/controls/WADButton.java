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
package org.openbravo.wad.controls;

import java.util.*;
import org.openbravo.utils.FormatUtilities;
import org.openbravo.xmlEngine.XmlDocument;

public class WADButton extends WADControl {

  public WADButton() {
  }

  public WADButton(Properties prop) {
    setInfo(prop);
    initialize();
  }

  public void initialize() {
    generateJSCode();
  }

  private void generateJSCode() {
    setValidation("");
    setCalloutJS();
  }

  public String getType() {
    return "Button_CenterAlign";
  }

  public String editMode() {
    XmlDocument xmlDocument = getReportEngine().readXmlTemplate("org/openbravo/wad/controls/WADButton").createXmlDocument();

    xmlDocument.setParameter("columnName", getData("ColumnName"));
    xmlDocument.setParameter("columnNameInp", getData("ColumnNameInp"));
    xmlDocument.setParameter("name", getData("Name"));

    xmlDocument.setParameter("callout", getOnChangeCode());
    StringBuffer text = new StringBuffer();
    if (getData("MappingName").equals("")) {
      text.append("openServletNewWindow('BUTTON").append(FormatUtilities.replace(getData("ColumnName"))).append(getData("AD_Process_ID"));
      text.append("', false, '").append(getData("TabName")).append("_Edition.html', 'BUTTON', null, true");
      if (getData("ColumnName").equalsIgnoreCase("CreateFrom")) text.append(",600, 900");
      else text.append(", 600, 900");
      text.append(");");
    } else {
      text.append("openServletNewWindow('DEFAULT', false, '..");
      if (!getData("MappingName").startsWith("/")) text.append('/');
      text.append(getData("MappingName")).append("', 'BUTTON', '").append(getData("AD_Process_ID")).append("', true");
      text.append(",600, 900);");
    }
    /*if (getData("IsReadOnly").equals("Y") || getData("IsReadOnlyTab").equals("Y") || getData("IsUpdateable").equals("N")) {
      xmlDocument.setParameter("action", "");
    } else */xmlDocument.setParameter("action", text.toString());
    return replaceHTML(xmlDocument.print());
  }

  public String newMode() {
    XmlDocument xmlDocument = getReportEngine().readXmlTemplate("org/openbravo/wad/controls/WADButton").createXmlDocument();

    xmlDocument.setParameter("columnName", getData("ColumnName"));
    xmlDocument.setParameter("columnNameInp", getData("ColumnNameInp"));
    xmlDocument.setParameter("name", getData("Name"));

    xmlDocument.setParameter("callout", getOnChangeCode());
    StringBuffer text = new StringBuffer();
    if (getData("MappingName").equals("")) {
      text.append("openServletNewWindow('BUTTON").append(FormatUtilities.replace(getData("ColumnName"))).append(getData("AD_Process_ID"));
      text.append("', false, '").append(getData("TabName")).append("_Edition.html', 'BUTTON', null, true");
      if (getData("ColumnName").equalsIgnoreCase("CreateFrom")) text.append(",600, 900");
      else text.append(", 600, 900");
      text.append(");");
    } else {
      text.append("openServletNewWindow('DEFAULT', false, '..");
      if (!getData("MappingName").startsWith("/")) text.append('/');
      text.append(getData("MappingName")).append("', 'BUTTON', '").append(getData("AD_Process_ID")).append("', true");
      text.append(",600, 900);");
    }
    /*if (getData("IsReadOnly").equals("Y") || getData("IsReadOnlyTab").equals("Y")) {
      xmlDocument.setParameter("action", "");
    } else */xmlDocument.setParameter("action", text.toString());
    return replaceHTML(xmlDocument.print());
  }

  public String toXml() {
    StringBuffer text = new StringBuffer();
    if (getData("IsParameter").equals("Y")) {
      text.append("<PARAMETER id=\"").append(getData("ColumnName"));
      text.append("\" name=\"").append(getData("ColumnName"));
      text.append("\" attribute=\"value\"/>");
      if (getData("IsDisplayed").equals("Y") && !getData("AD_Reference_Value_ID").equals("") && !getData("ColumnName").equalsIgnoreCase("ChangeProjectStatus")) {
        text.append("\n<PARAMETER id=\"").append(getData("ColumnName")).append("_BTN\" name=\"");
        text.append(getData("ColumnName"));
        text.append("_BTN\" replaceCharacters=\"htmlPreformated\"/>");
      }
    } else {
      text.append("<FIELD id=\"").append(getData("ColumnName"));
      text.append("\" attribute=\"value\">").append(getData("ColumnName")).append("</FIELD>");
      if (getData("IsDisplayed").equals("Y") && !getData("AD_Reference_Value_ID").equals("") && !getData("ColumnName").equalsIgnoreCase("ChangeProjectStatus")) {
        text.append("\n<FIELD id=\"").append(getData("ColumnName")).append("_BTN\" replaceCharacters=\"htmlPreformated\">");
        text.append(getData("ColumnName")).append("_BTN</FIELD>");
      }
    }
    return text.toString();
  }

  public String toJava() {
    return "";
  }
}
