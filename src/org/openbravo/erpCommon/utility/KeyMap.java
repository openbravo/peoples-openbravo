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
package org.openbravo.erpCommon.utility;

import org.openbravo.database.ConnectionProvider;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.utils.FormatUtilities;
import java.util.*;
import org.apache.log4j.Logger ;


/**
 * @author Fernando Iriazabal
 *
 * Class in charge of the keymap building for each window type.
 */
public class KeyMap {
  static Logger log4j = Logger.getLogger(KeyMap.class);
  private VariablesSecureApp vars;
  private ConnectionProvider conn;
  private String TabID = "";
  private Properties myData = new Properties();
  private Vector<Properties> structure = new Vector<Properties>();

  /**
   * Constructor
   * 
   * @param _conn: Handler for the database connection.
   * @param _vars: Handler for the session info.
   * @param _tabId: String with the tab's id.
   * @param _windowId: String with the window's id.
   * @throws Exception
   */
  public KeyMap(ConnectionProvider _conn, VariablesSecureApp _vars, String _tabId, String _windowId) throws Exception {
    if (_conn==null || _vars==null || _tabId==null || _tabId.equals("") || _windowId==null || _windowId.equals("")) throw new Exception("Missing parameters");
    this.conn = _conn;
    this.vars = _vars;
    this.TabID = _tabId;
    generateStructure();
  }
  
  /**
   * Constructor
   * 
   * @param _conn: Handler for the database connection.
   * @param _vars: Handler for the session info.
   * @param _action: String with the window type (form, report, process...)
   * @throws Exception
   */
  public KeyMap(ConnectionProvider _conn, VariablesSecureApp _vars, String _action) throws Exception {
    if (_conn==null || _vars==null || _action==null || _action.equals("")) throw new Exception("Missing parameters");
    this.conn = _conn;
    this.vars = _vars;
  }

  /**
   * Setter for any internal attribute.
   * 
   * @param name: String with the name of the attribute.
   * @param value: String with the value of the attribute.
   */
  private void setData(String name, String value) {
    if (name==null || name.equals("")) return;
    if (this.myData==null) this.myData = new Properties();
    this.myData.setProperty(name, value);
  }

  /**
   * Getter for any internal attribute.
   * 
   * @param name: String with the name of the attribute.
   * @return String with the value of the attribute.
   */
  private String getData(String name) {
    if (name==null || name.equals("") || this.myData==null) return "";
    String aux = this.myData.getProperty(name);
    if (aux == null) return "";
    else return aux;
  }

  /**
   * Adds new structure of additional information for this tab.
   * 
   * @param _prop: Properties with all the additional info for this tab.
   */
  public void addStructure(Properties _prop) {
    if (_prop==null) return;
    if (this.structure == null) this.structure = new Vector<Properties>();
    this.structure.addElement(_prop);
  }

  /**
   * Gets the additional information for this tab.
   * 
   * @return Vector with the additional information.
   */
  public Vector<Properties> getStructure() {
    return this.structure;
  }

  /**
   * Gets the keymap for the sort tab window type.
   * 
   * @return String with the javascript for the keynap.
   */
  public String getSortTabKeyMaps() {
    StringBuffer script = new StringBuffer();
    script.append("\nvar keyArray = new Array(\n");
    script.append("new keyArrayItem(\"M\", \"menuShowHide('buttonMenu');\", null, \"ctrlKey\")\n");
    script.append(");\n");
    script.append("enableShortcuts();");
    return script.toString();
  }

  /**
   * Gets the keymap for the Relation window type.
   * 
   * @return String with the javascript for the keynap.
   */
  public String getRelationKeyMaps() {
    StringBuffer script = new StringBuffer();
    script.append("\nvar keyArray = new Array(\n");
    script.append("new keyArrayItem(\"M\", \"menuShowHide('buttonMenu');\", null, \"ctrlKey\"),\n");
    script.append("new keyArrayItem(\"N\", \"submitCommandForm('NEW', false, null, '").append(getData("TabNameUrl")).append("_Edition.html', '_self');\", null, \"ctrlKey\"),\n");
    script.append("new keyArrayItem(\"E\", \"submitCommandForm('EDIT', true, null, '").append(getData("TabNameUrl")).append("_Edition.html', '_self');\", null, \"ctrlKey\"),\n");
    script.append("new keyArrayItem(\"B\", \"abrirBusqueda('../businessUtility/Buscador.html', 'BUSCADOR', document.frmMain.inpTabId.value, '").append(getData("WindowNameUrl")).append("/").append(getData("TabNameUrl")).append("_Edition.html', document.frmMain.inpwindowId.value, true);\", null, \"ctrlKey\")\n");
    script.append(");\n");
    script.append("enableShortcuts();");
    return script.toString();
  }

  /**
   * Gets the keymap for the Edition window type.
   * 
   * @param isNew: Boolean to indicate if is a new record or not.
   * @return String with the javascript for the keynap.
   */
  public String getEditionKeyMaps(boolean isNew) {
    StringBuffer script = new StringBuffer();
    script.append("\nvar keyArray = new Array(\n");
    script.append("new keyArrayItem(\"M\", \"menuShowHide('buttonMenu');\", null, \"ctrlKey\")\n");
    script.append(",new keyArrayItem(\"N\", \"submitCommandForm('NEW', false, null, '").append(getData("TabNameUrl")).append("_Edition.html', '_self', null, true, null, true);\", null, \"ctrlKey\")\n");
    script.append(",new keyArrayItem(\"L\", \"submitCommandForm('RELATION', true, null, '").append(getData("TabNameUrl")).append("_Relation.html', '_self', null, true, null, true);\", null, \"ctrlKey\")\n");
    script.append(",new keyArrayItem(\"B\", \"abrirBusqueda('../businessUtility/Buscador.html', 'BUSCADOR', document.frmMain.inpTabId.value, '").append(getData("WindowNameUrl")).append("/").append(getData("TabNameUrl")).append("_Edition.html', document.frmMain.inpwindowId.value, true);\", null, \"ctrlKey\")\n");
    if (!getData("IsTabReadOnly").equals("Y")) {
      if (!isNew) {
        script.append(",new keyArrayItem(\"D\", \"submitCommandForm('DELETE', true, null, '").append(getData("TabNameUrl")).append("_Relation.html', '_self');\", null, \"ctrlKey\")\n");
        script.append(",new keyArrayItem(\"S\", \"submitCommandForm('SAVE_EDIT_EDIT', true, null, '").append(getData("TabNameUrl")).append("_Relation.html', '_self', true, null, null, true);\", null, \"ctrlKey\")\n");
        script.append(",new keyArrayItem(\"G\", \"submitCommandForm('SAVE_EDIT_RELATION', true, null, '").append(getData("TabNameUrl")).append("_Relation.html', '_self', true, null, null, true);\", null, \"ctrlKey\")\n");
        script.append(",new keyArrayItem(\"H\", \"submitCommandForm('SAVE_EDIT_NEW', true, null, '").append(getData("TabNameUrl")).append("_Relation.html', '_self', true, null, null, true);\", null, \"ctrlKey\")\n");
        script.append(",new keyArrayItem(\"A\", \"submitCommandForm('SAVE_EDIT_NEXT', true, null, '").append(getData("TabNameUrl")).append("_Relation.html', '_self', true, null, null, true);\", null, \"ctrlKey\")\n");
      } else {
        script.append(",new keyArrayItem(\"S\", \"submitCommandForm('SAVE_NEW_EDIT', true, null, '").append(getData("TabNameUrl")).append("_Relation.html', '_self', true, null, null, true);\", null, \"ctrlKey\")\n");
        script.append(",new keyArrayItem(\"G\", \"submitCommandForm('SAVE_NEW_RELATION', true, null, '").append(getData("TabNameUrl")).append("_Relation.html', '_self', true, null, null, true);\", null, \"ctrlKey\")\n");
        script.append(",new keyArrayItem(\"H\", \"submitCommandForm('SAVE_NEW_NEW', true, null, '").append(getData("TabNameUrl")).append("_Relation.html', '_self', true, null, null, true);\", null, \"ctrlKey\")\n");
      }
    }
    script.append(",new keyArrayItem(\"REPAGE\", \"submitCommandForm('FIRST', false, null, '").append(getData("TabNameUrl")).append("_Edition.html', '_self', null, true);\", null, \"ctrlKey\")\n");
    script.append(",new keyArrayItem(\"AVPAGE\", \"submitCommandForm('LAST', false, null, '").append(getData("TabNameUrl")).append("_Edition.html', '_self', null, true);\", null, \"ctrlKey\")\n");
    script.append(",new keyArrayItem(\"RIGHTARROW\", \"submitCommandForm('NEXT', false, null, '").append(getData("TabNameUrl")).append("_Edition.html', '_self', null, true);\", null, \"ctrlKey\")\n");
    script.append(",new keyArrayItem(\"LEFTARROW\", \"submitCommandForm('PREVIOUS', false, null, '").append(getData("TabNameUrl")).append("_Edition.html', '_self', null, true);\", null, \"ctrlKey\")\n");
    script.append(");\n");
    script.append("enableShortcuts();");

    return script.toString();
  }
  
    /**
   * Gets the keymap for the Action button window type.
   * 
   * @return String with the javascript for the keynap.
   */
  public String getActionButtonKeyMaps() {
    StringBuffer script = new StringBuffer();
    script.append("\nvar keyArray = new Array(\n");
    script.append("new keyArrayItem(\"\", \"\", null, null)\n");
    script.append(");\n");
    script.append("enableShortcuts();");

    return script.toString();
  }

    /**
   * Gets the keymap for the Form window type.
   * 
   * @return String with the javascript for the keynap.
   */
  public String getFormKeyMaps() {
    StringBuffer script = new StringBuffer();
    script.append("\nvar keyArray = new Array(\n");
    script.append("new keyArrayItem(\"M\", \"menuShowHide('buttonMenu');\", null, \"ctrlKey\")\n");
    script.append(");\n");
    script.append("enableShortcuts();");

    return script.toString();
  }

    /**
   * Gets the keymap for the Report window type.
   * 
   * @return String with the javascript for the keynap.
   */
  public String getReportKeyMaps() {
    StringBuffer script = new StringBuffer();
    script.append("\nvar keyArray = new Array(\n");
    script.append("new keyArrayItem(\"M\", \"menuShowHide('buttonMenu');\", null, \"ctrlKey\")\n");
    script.append(");\n");
    script.append("enableShortcuts();");

    return script.toString();
  }

  /**
   * Generates the needed info to build the keymap.
   * 
   * @throws Exception
   */
  private void generateStructure() throws Exception {
    TableSQLQueryData[] data = TableSQLQueryData.selectStructure(this.conn, this.TabID, this.vars.getLanguage());
    if (data==null || data.length==0) throw new Exception("Couldn't get structure for tab " + this.TabID);
    String primaryKey = "";
    String secondaryKey = "";
    setData("TabName", data[0].tabName);
    setData("TabNameUrl", FormatUtilities.replace(data[0].tabName));
    setData("WindowName", data[0].windowName);
    setData("WindowNameUrl", FormatUtilities.replace(data[0].windowName));
    setData("IsTabReadOnly", data[0].istabreadonly);
    for (int i=0;i<data.length;i++) {
      Properties prop = new Properties();
      prop.setProperty("ColumnName", data[i].columnname);
      prop.setProperty("AD_Reference_ID", data[i].adReferenceId);
      prop.setProperty("AD_Reference_Value_ID", data[i].adReferenceValueId);
      prop.setProperty("AD_Val_Rule_ID", data[i].adValRuleId);
      prop.setProperty("FieldLength", data[i].fieldlength);
      prop.setProperty("DefaultValue", data[i].defaultvalue);
      prop.setProperty("IsKey", data[i].iskey);
      prop.setProperty("IsParent", data[i].isparent);
      prop.setProperty("IsMandatory", data[i].ismandatory);
      prop.setProperty("IsUpdateable", data[i].isupdateable);
      prop.setProperty("ReadOnlyLogic", data[i].readonlylogic);
      prop.setProperty("IsIdentifier", data[i].isidentifier);
      prop.setProperty("SeqNo", data[i].seqno);
      prop.setProperty("IsTranslated", data[i].istranslated);
      prop.setProperty("IsEncrypted", data[i].isencrypted);
      prop.setProperty("VFormat", data[i].vformat);
      prop.setProperty("ValueMin", data[i].valuemin);
      prop.setProperty("ValueMax", data[i].valuemax);
      prop.setProperty("IsSelectionColumn", data[i].isselectioncolumn);
      prop.setProperty("AD_Process_ID", data[i].adProcessId);
      prop.setProperty("IsSessionAttr", data[i].issessionattr);
      prop.setProperty("IsSecondaryKey", data[i].issecondarykey);
      prop.setProperty("IsDesencryptable", data[i].isdesencryptable);
      prop.setProperty("AD_CallOut_ID", data[i].adCalloutId);
      prop.setProperty("Name", data[i].name);
      prop.setProperty("AD_FieldGroup_ID", data[i].adFieldgroupId);
      prop.setProperty("IsDisplayed", data[i].isdisplayed);
      prop.setProperty("DisplayLogic", data[i].displaylogic);
      prop.setProperty("DisplayLength", data[i].displaylength);
      prop.setProperty("IsReadOnly", data[i].isreadonly);
      prop.setProperty("SortNo", data[i].sortno);
      prop.setProperty("IsSameLine", data[i].issameline);
      prop.setProperty("IsHeading", data[i].isheading);
      prop.setProperty("IsFieldOnly", data[i].isfieldonly);
      prop.setProperty("ShowInRelation", data[i].showinrelation);
      addStructure(prop);
      if (primaryKey.equals("") && data[i].iskey.equals("Y")) {
        primaryKey = data[i].columnname;
      } else if (secondaryKey.equals("") && data[i].issecondarykey.equals("Y")) {
        secondaryKey = data[i].columnname;
      }
    }
    if (!primaryKey.equals("")) setData("KeyColumn", primaryKey);
    else if (!secondaryKey.equals("")) setData("KeyColumn", secondaryKey);
    else throw new Exception("No column key defined for this tab");
  }
}
