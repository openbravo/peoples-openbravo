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
 * All portions are Copyright (C) 2001-2007 Openbravo SL 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
*/

  dojo.provide("openbravo.manifest");
  dojo.require("dojo.string.extras");

  dojo.registerNamespaceResolver("openbravo",
    function(name){ 
      switch (name.toLowerCase())
      {
      case "button":
        return "openbravo.widget.Button";
        break;
      case "checkbox":
        return "openbravo.widget.Checkbox";
        break;
      case "combobox":
        return "openbravo.widget.ComboBox";
        break;
      case "contentpane":
        return "openbravo.widget.ContentPane";
        break;
      case "datagrid":
        return "openbravo.widget.DataGrid";
        break;
      case "datetextbox":
        return "openbravo.widget.DateTextbox";
        break;
      case "hyperlink":
        return "openbravo.widget.HyperLink";
        break;
      case "integertextbox":
        return "openbravo.widget.IntegerTextbox";
        break;
      case "layoutcontainer":
        return "openbravo.widget.LayoutContainer";
        break;
      case "messagebox":
        return "openbravo.widget.MessageBox";
        break;
      case "realnumbertextbox":
        return "openbravo.widget.RealNumberTextbox";
        break;
      case "select":
        return "openbravo.widget.Select";
        break;
      case "tabcontainer":
        return "openbravo.widget.TabContainer";
        break;
      case "timetextbox":
        return "openbravo.widget.TimeTextbox";
        break;
      case "titlepane":
        return "openbravo.widget.TitlePane";
        break;
      case "validationtextarea":
        return "openbravo.widget.ValidationTextArea";
        break;
      case "validationtextbox":
        return "openbravo.widget.ValidationTextbox";
        break;
      case "widget":
        return "openbravo.widget.Widget";
        break;
      default:
        return "openbravo.widget."+dojo.string.capitalize(name);
      }
    }
  );
