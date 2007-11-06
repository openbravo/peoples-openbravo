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
import org.openbravo.xmlEngine.XmlDocument;

public class WADImage extends WADControl {

  public WADImage() {
  }

  public WADImage(Properties prop) {
    setInfo(prop);
    initialize();
  }

  public void initialize() {
    addImport("searchs", "../../../../../web/js/searchs.js");
    generateJSCode();
  }

  private void generateJSCode() {
    if (getData("IsMandatory").equals("Y")) {
      XmlDocument xmlDocument = getReportEngine().readXmlTemplate("org/openbravo/wad/controls/WADImageJSValidation").createXmlDocument();

      xmlDocument.setParameter("columnNameInp", getData("ColumnNameInp"));
      setValidation(replaceHTML(xmlDocument.print()));
    }
    setCalloutJS();
  }

  public String getType() {
    return "Image";
  }

  public String editMode() {
    String[] discard = {"buttonxx"};
    if (!getData("IsReadOnly").equals("Y") && !getData("IsReadOnlyTab").equals("Y") && !getData("IsUpdateable").equals("N")) discard[0] = "";
    XmlDocument xmlDocument = getReportEngine().readXmlTemplate("org/openbravo/wad/controls/WADImage", discard).createXmlDocument();

    xmlDocument.setParameter("columnName", getData("ColumnName"));
    xmlDocument.setParameter("columnNameInp", getData("ColumnNameInp"));
    xmlDocument.setParameter("name", getData("Name"));

    xmlDocument.setParameter("callout", getOnChangeCode());

    return replaceHTML(xmlDocument.print());
  }

  public String newMode() {
    String[] discard = {"buttonxx"};
    if (!getData("IsReadOnly").equals("Y") && !getData("IsReadOnlyTab").equals("Y")) discard[0] = "";
    XmlDocument xmlDocument = getReportEngine().readXmlTemplate("org/openbravo/wad/controls/WADImage", discard).createXmlDocument();

    xmlDocument.setParameter("columnName", getData("ColumnName"));
    xmlDocument.setParameter("columnNameInp", getData("ColumnNameInp"));
    xmlDocument.setParameter("name", getData("Name"));

    xmlDocument.setParameter("callout", getOnChangeCode());

    return replaceHTML(xmlDocument.print());
  }

  public String toXml() {
    String[] discard = {"xx_PARAM", "xx_PARAM_R"};
    if (getData("IsParameter").equals("Y")) {
      discard[0] = new String("xx");
      discard[1] = new String("xx_R");
    }
    XmlDocument xmlDocument = getReportEngine().readXmlTemplate("org/openbravo/wad/controls/WADImageXML", discard).createXmlDocument();

    xmlDocument.setParameter("columnName", getData("ColumnName"));
    return replaceHTML(xmlDocument.print());
  }
}
