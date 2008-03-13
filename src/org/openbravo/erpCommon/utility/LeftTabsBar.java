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
import org.apache.log4j.Logger ;

public class LeftTabsBar {
  static Logger log4j = Logger.getLogger(LeftTabsBar.class);
  ConnectionProvider conn;
  String language = "en_US";
  String servlet_action = "";
  String base_direction = "";

  public LeftTabsBar(ConnectionProvider _conn, String _language, String _action, String _baseDirection) {
    this.conn = _conn;
    this.language = _language;
    this.servlet_action = _action;
    this.base_direction = _baseDirection;
  }

  public String editionTemplate() {
    StringBuffer text = new StringBuffer();
    text.append("<table cellpadding=\"0\" cellspacing=\"0\" class=\"Main_ContentPane_LeftTabsBar\" id=\"tdLeftTabsBars\">\n");
    text.append("  <tr>\n");
    text.append("    <td class=\"Main_LeftTabsBar_bg_body\">\n");
    text.append("      <table cellpadding=\"0\" cellspacing=\"0\" class=\"Main_LeftTabsBar\">\n");
    text.append("        <tr>\n");
    text.append("          <td class=\"Main_LeftTabsBar_bg_top\">\n");
    text.append("          </td>\n");
    text.append("        </tr>\n");
    text.append("        <tr>\n");
    text.append("          <td>\n");
    text.append("              <A class=\"Main_LeftTabsBar_ButtonLeft\" href=\"#\" onclick=\"menuShowHide('buttonMenu');return false;\">\n");
    text.append("                <IMG class=\"Main_LeftTabsBar_ButtonLeft_Icon Main_LeftTabsBar_ButtonLeft_Icon_arrow_hide\" src=\"").append(base_direction).append("/images/blank.gif\" border=\"0\" id=\"buttonMenu\"></IMG>\n");
    text.append("              </A>\n");
    text.append("          </td>\n");
    text.append("        </tr>\n");
    text.append("        <tr>\n");
    text.append("          <td class=\"Main_LeftTabsBar_bg_separator_cell\">\n");
    text.append("          </td>\n");
    text.append("        </tr>\n");
    text.append("        <tr>\n");
    text.append("          <td>\n");
    text.append("                <DIV class=\"Main_LeftTabsBar_ButtonRight_selected\">\n");
    text.append("                  <IMG class=\"Main_LeftTabsBar_ButtonRight_Icon Main_LeftTabsBar_ButtonRight_Icon_edition_selected\" src=\"").append(base_direction).append("/images/blank.gif\" border=\"0\"></IMG>\n");
    text.append("                </DIV>\n");
    text.append("          </td>\n");
    text.append("        </tr>\n");
    text.append("        <tr>\n");
    text.append("          <td>\n");
    text.append("                <A class=\"Main_LeftTabsBar_ButtonRight\" href=\"#\" onClick=\"submitCommandForm('RELATION', false, null, '").append(servlet_action).append("', '_self', null, true);return false;\" id=\"buttonRelation\">\n");
    text.append("                  <IMG class=\"Main_LeftTabsBar_ButtonRight_Icon Main_LeftTabsBar_ButtonRight_Icon_relation\" src=\"").append(base_direction).append("/images/blank.gif\" border=\"0\"></IMG>\n");
    text.append("                </A>\n");
    text.append("          </td>\n");
    text.append("        </tr>\n");
    text.append("        <tr>\n");
    text.append("          <td class=\"Main_LeftTabsBar_bg_empty_cell\">\n");
    text.append("          </td>\n");
    text.append("        </tr>\n");
    text.append("      </table>\n");
    text.append("    </td>\n");
    text.append("  </tr>\n");
    text.append("  <tr class=\"Main_LeftTabsBar_bg_empty_cell\">\n");
    text.append("    <td class=\"Main_LeftTabsBar_bg_empty_cell\">\n");
    text.append("    </td>\n");
    text.append("  </tr>\n");
    text.append("</table>\n");
    return text.toString();
  }

  public String relationTemplate() {
    StringBuffer text = new StringBuffer();
    text.append("<table cellpadding=\"0\" cellspacing=\"0\" class=\"Main_ContentPane_LeftTabsBar\" id=\"tdLeftTabsBars\">\n");
    text.append("  <tr>\n");
    text.append("    <td class=\"Main_LeftTabsBar_bg_body\">\n");
    text.append("      <table cellpadding=\"0\" cellspacing=\"0\" class=\"Main_LeftTabsBar\">\n");
    text.append("        <tr>\n");
    text.append("          <td class=\"Main_LeftTabsBar_bg_top\">\n");
    text.append("          </td>\n");
    text.append("        </tr>\n");
    text.append("        <tr>\n");
    text.append("          <td>\n");
    text.append("              <A class=\"Main_LeftTabsBar_ButtonLeft\" href=\"#\" onclick=\"menuShowHide('buttonMenu');return false;\">\n");
    text.append("                <IMG class=\"Main_LeftTabsBar_ButtonLeft_Icon Main_LeftTabsBar_ButtonLeft_Icon_arrow_hide\" src=\"").append(base_direction).append("/images/blank.gif\" border=\"0\" id=\"buttonMenu\"></IMG>\n");
    text.append("              </A>\n");
    text.append("          </td>\n");
    text.append("        </tr>\n");
    text.append("        <tr>\n");
    text.append("          <td class=\"Main_LeftTabsBar_bg_separator_cell\">\n");
    text.append("          </td>\n");
    text.append("        </tr>\n");
    text.append("        <tr>\n");
    text.append("          <td>\n");
    text.append("                <A class=\"Main_LeftTabsBar_ButtonRight\" href=\"#\" onClick=\"submitCommandForm('EDIT', true, null, '").append(servlet_action).append("', '_self', null, false);return false;\" id=\"buttonEdition\">\n");
    text.append("                  <IMG class=\"Main_LeftTabsBar_ButtonRight_Icon Main_LeftTabsBar_ButtonRight_Icon_edition\" src=\"").append(base_direction).append("/images/blank.gif\" border=\"0\"></IMG>\n");
    text.append("                </A>\n");
    text.append("          </td>\n");
    text.append("        </tr>\n");
    text.append("        <tr>\n");
    text.append("          <td>\n");
    text.append("                <DIV class=\"Main_LeftTabsBar_ButtonRight_selected\">\n");
    text.append("                  <IMG class=\"Main_LeftTabsBar_ButtonRight_Icon Main_LeftTabsBar_ButtonRight_Icon_relation_selected\" src=\"").append(base_direction).append("/images/blank.gif\" border=\"0\"></IMG>\n");
    text.append("                </DIV>\n");
    text.append("          </td>\n");
    text.append("        </tr>\n");
    text.append("        <tr>\n");
    text.append("          <td class=\"Main_LeftTabsBar_bg_empty_cell\">\n");
    text.append("          </td>\n");
    text.append("        </tr>\n");
    text.append("      </table>\n");
    text.append("    </td>\n");
    text.append("  </tr>\n");
    text.append("  <tr class=\"Main_LeftTabsBar_bg_empty_cell\">\n");
    text.append("    <td class=\"Main_LeftTabsBar_bg_empty_cell\">\n");
    text.append("    </td>\n");
    text.append("  </tr>\n");
    text.append("</table>\n");
    return text.toString();
  }

  public String manualTemplate() {
    StringBuffer text = new StringBuffer();
    text.append("<table cellpadding=\"0\" cellspacing=\"0\" class=\"Main_ContentPane_LeftTabsBar\" id=\"tdLeftTabsBars\">\n");
    text.append("  <tr>\n");
    text.append("    <td class=\"Main_LeftTabsBar_bg_body\">\n");
    text.append("      <table cellpadding=\"0\" cellspacing=\"0\" class=\"Main_LeftTabsBar\">\n");
    text.append("        <tr>\n");
    text.append("          <td class=\"Main_LeftTabsBar_bg_top\">\n");
    text.append("          </td>\n");
    text.append("        </tr>\n");
    text.append("        <tr>\n");
    text.append("          <td>\n");
    text.append("              <A class=\"Main_LeftTabsBar_ButtonLeft\" href=\"#\" onclick=\"menuShowHide('buttonMenu');return false;\">\n");
    text.append("                <IMG class=\"Main_LeftTabsBar_ButtonLeft_Icon Main_LeftTabsBar_ButtonLeft_Icon_arrow_hide\" src=\"").append(base_direction).append("/images/blank.gif\" border=\"0\" id=\"buttonMenu\"></IMG>\n");
    text.append("              </A>\n");
    text.append("          </td>\n");
    text.append("        </tr>\n");
    text.append("        <tr>\n");
    text.append("          <td class=\"Main_LeftTabsBar_bg_separator_cell\">\n");
    text.append("          </td>\n");
    text.append("        </tr>\n");
    text.append("        <tr>\n");
    text.append("          <td class=\"Main_LeftTabsBar_bg_empty_cell\">\n");
    text.append("          </td>\n");
    text.append("        </tr>\n");
    text.append("      </table>\n");
    text.append("    </td>\n");
    text.append("  </tr>\n");
    text.append("  <tr class=\"Main_LeftTabsBar_bg_empty_cell\">\n");
    text.append("    <td class=\"Main_LeftTabsBar_bg_empty_cell\">\n");
    text.append("    </td>\n");
    text.append("  </tr>\n");
    text.append("</table>\n");
    return text.toString();
  }
}
