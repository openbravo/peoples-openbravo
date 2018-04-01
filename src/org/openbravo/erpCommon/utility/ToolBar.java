/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.1  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html 
 * Software distributed under the License  is  distributed  on  an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific  language  governing  rights  and  limitations
 * under the License. 
 * The Original Code is Openbravo ERP. 
 * The Initial Developer of the Original Code is Openbravo SLU 
 * All portions are Copyright (C) 2001-2018 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.erpCommon.utility;

import java.util.Hashtable;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.obps.ActivationKey;
import org.openbravo.model.ad.ui.Tab;

public class ToolBar {
  private static final Logger log = Logger.getLogger(ToolBar.class);
  private ConnectionProvider conn;
  private String language = "en_US";
  private String servlet_action = "";
  private boolean isNew = false;
  private String keyfield = "";
  private String grid_id = "";
  private String pdf = "";
  private boolean isDirectPrint = false;
  private String base_direction = "";
  private boolean debug = false;
  private boolean isSrcWindow = false;
  private boolean isFrame = false;
  private boolean isRelation = false;
  private boolean isEditable = false;
  private boolean hasAttachments = false;
  private boolean email = false;
  private String tabId;
  private boolean deleteable = true;
  private boolean hasNewButton = true;

  public void setEmail(boolean email) {
    this.email = email;
  }

  /**
   * If the ToolBar is created for a generated window, this functions sets the tabId of that Tab.
   * 
   * Used inside the class to enable/disable functionality based on the concrete tab
   * 
   * @param tabId
   *          tabId of the creator tab, if the ToolBar is for a generated window
   */
  public void setTabId(String tabId) {
    this.tabId = tabId;
  }

  public void setDeleteable(boolean deleteable) {
    this.deleteable = deleteable;
  }

  Hashtable<String, HTMLElement> buttons = new Hashtable<String, HTMLElement>();

  /**
   * Constructor used by the grid view of all generated windows.
   */
  public ToolBar(ConnectionProvider _conn, String _language, String _action, boolean _isNew,
      String _keyINP, String _gridID, String _PDFName, boolean _isDirectPrinting,
      String _windowName, String _baseDirection, boolean _debug) {
    this(_conn, true, _language, _action, _isNew, _keyINP, _gridID, _PDFName, _isDirectPrinting,
        _windowName, _baseDirection, _debug, false);
  }

  public ToolBar(ConnectionProvider _conn, String _language, String _action, boolean _isNew,
      String _keyINP, String _gridID, String _PDFName, boolean _isDirectPrinting,
      String _windowName, String _baseDirection, boolean _debug, boolean _isSrcWindow) {
    this(_conn, true, _language, _action, _isNew, _keyINP, _gridID, _PDFName, _isDirectPrinting,
        _windowName, _baseDirection, _debug, _isSrcWindow, false);
  }

  public ToolBar(ConnectionProvider _conn, String _language, String _action, boolean _isNew,
      String _keyINP, String _gridID, String _PDFName, boolean _isDirectPrinting,
      String _windowName, String _baseDirection, boolean _debug, boolean _isSrcWindow,
      boolean _isFrame) {
    this(_conn, true, _language, _action, _isNew, _keyINP, _gridID, _PDFName, _isDirectPrinting,
        _windowName, _baseDirection, _debug, _isSrcWindow, _isFrame);
  }

  public ToolBar(ConnectionProvider _conn, String _language, String _action, boolean _isNew,
      String _keyINP, String _gridID, String _PDFName, boolean _isDirectPrinting,
      String _windowName, String _baseDirection, boolean _debug, boolean _isSrcWindow,
      boolean _isFrame, boolean _hasAttachements) {
    this(_conn, true, _language, _action, _isNew, _keyINP, _gridID, _PDFName, _isDirectPrinting,
        _windowName, _baseDirection, _debug, _isSrcWindow, _isFrame, _hasAttachements, true);
  }

  public ToolBar(ConnectionProvider _conn, boolean _isEditable, String _language, String _action,
      boolean _isNew, String _keyINP, String _gridID, String _PDFName, boolean _isDirectPrinting,
      String _windowName, String _baseDirection, boolean _debug) {
    this(_conn, _isEditable, _language, _action, _isNew, _keyINP, _gridID, _PDFName,
        _isDirectPrinting, _windowName, _baseDirection, _debug, false);
  }

  public ToolBar(ConnectionProvider _conn, boolean _isEditable, String _language, String _action,
      boolean _isNew, String _keyINP, String _gridID, String _PDFName, boolean _isDirectPrinting,
      String _windowName, String _baseDirection, boolean _debug, boolean _isSrcWindow) {
    this(_conn, _isEditable, _language, _action, _isNew, _keyINP, _gridID, _PDFName,
        _isDirectPrinting, _windowName, _baseDirection, _debug, _isSrcWindow, false);
  }

  public ToolBar(ConnectionProvider _conn, boolean _isEditable, String _language, String _action,
      boolean _isNew, String _keyINP, String _gridID, String _PDFName, boolean _isDirectPrinting,
      String _windowName, String _baseDirection, boolean _debug, boolean _isSrcWindow,
      boolean _isFrame) {
    this(_conn, _isEditable, _language, _action, _isNew, _keyINP, _gridID, _PDFName,
        _isDirectPrinting, _windowName, _baseDirection, _debug, _isSrcWindow, false, false, true);
  }

  public ToolBar(ConnectionProvider _conn, boolean _isEditable, String _language, String _action,
      boolean _isNew, String _keyINP, String _gridID, String _PDFName, boolean _isDirectPrinting,
      String _windowName, String _baseDirection, boolean _debug, boolean _isSrcWindow,
      boolean _isFrame, boolean _hasAttachments) {
    this(_conn, _isEditable, _language, _action, _isNew, _keyINP, _gridID, _PDFName,
        _isDirectPrinting, _windowName, _baseDirection, _debug, _isSrcWindow, _isFrame,
        _hasAttachments, true);
  }

  /**
   * Constructor used by the edit view of all generated windows.
   */
  public ToolBar(ConnectionProvider _conn, boolean _isEditable, String _language, String _action,
      boolean _isNew, String _keyINP, String _gridID, String _PDFName, boolean _isDirectPrinting,
      String _windowName, String _baseDirection, boolean _debug, boolean _isSrcWindow,
      boolean _isFrame, boolean _hasAttachments, boolean _hasNewButton) {
    this.conn = _conn;
    this.language = _language;
    this.servlet_action = _action;
    this.isNew = _isNew;
    this.keyfield = _keyINP;
    if (_gridID != null)
      this.grid_id = _gridID;
    if (_PDFName != null)
      this.pdf = _PDFName;
    this.isDirectPrint = _isDirectPrinting;
    this.base_direction = _baseDirection;
    this.debug = _debug;
    this.isFrame = _isFrame;
    this.isEditable = _isEditable;
    this.isSrcWindow = _isSrcWindow;
    this.hasAttachments = _hasAttachments;
    this.hasNewButton = _hasNewButton;
    createAllButtons();
  }

  public void removeElement(String name) {
    try {
      if (buttons != null && !buttons.isEmpty())
        buttons.remove(name);
    } catch (final NullPointerException ignored) {
    }
  }

  private String getButtonScript(String name) {
    if (name.equals("RELATED_INFO")) {
      return (this.isNew ? "logClick(null);" : "")
          + "openServletNewWindow('DEFAULT', true, '../utility/UsedByLink.html', 'LINKS', null, true, 500, 600, true);";
    } else if (name.startsWith("SAVE")) {
      return "submitCommandForm('SAVE_" + (isNew ? "NEW" : "EDIT") + name.substring(4)
          + "', true, null, '" + servlet_action + (isSrcWindow ? "" : "_Relation")
          + ".html', '_self', true, false);";
    } else if (name.equals("ATTACHMENT")) {
      return ((grid_id == null || grid_id.equals("")) ? ""
          : "if (dijit.byId('"
              + grid_id
              + "').getSelectedRows()=='') {showJSMessage(23);resizeArea(true);calculateMsgBoxWidth();return false;} ")
          + " openServletNewWindow('DEFAULT', false, '../businessUtility/TabAttachments_FS.html?inpKey=' + "
          + ((grid_id == null || grid_id.equals("")) ? keyfield + ".value" : "dijit.byId('"
              + grid_id + "').getSelectedRows()")
          + "+'&inpEditable="
          + (isEditable ? "Y" : "N")
          + "', 'ATTACHMENT', null, true, 600, 700, true);";
    } else if (name.equals("EXCEL")) {
      return "openExcel('" + servlet_action + "_Excel.xls?Command=RELATION_XLS', '_blank');";
    } else if (name.equals("GRIDEXCEL")) {
      // implementation does not longer exist
      return "";
    } else if (name.equals("GRIDCSV")) {
      // implementation does not longer exist
      return "";
    } else if (name.equals("GRIDPDF")) {
      // implementation does not longer exist
      return "";
    } else if (name.equals("PRINT")) {
      return "openPDFSession('"
          + pdf
          + "', '"
          + (isDirectPrint ? "Printing" : "")
          + "', "
          + keyfield
          + ".name, "
          + ((grid_id == null || grid_id.equals("")) ? "null" : "dijit.byId('" + grid_id
              + "').getSelectedRows()") + ", "
          + ((grid_id == null || grid_id.equals("")) ? "true" : "null") + ");";
    } else if (name.equals("EMAIL")) {
      return "openPDFSession('"
          + pdf.replaceAll("print.html", "send.html")
          + "', '"
          + (isDirectPrint ? "Printing" : "")
          + "', "
          + keyfield
          + ".name, "
          + ((grid_id == null || grid_id.equals("")) ? "null" : "dijit.byId('" + grid_id
              + "').getSelectedRows()") + ", "
          + ((grid_id == null || grid_id.equals("")) ? "true" : "null") + ");";
    } else if (name.equals("AUDIT_EDITION")) {
      return "changeAuditStatus();";
    } else if (name.equals("AUDIT_RELATION")) {
      return "changeAuditStatusRelation();";
    } else if (name.equals("AUDIT_TRAIL")) {
      // open audit trail popup if exactly one record is selected, otherwise show error message
      return ((grid_id == null || grid_id.equals("")) ? ""
          : "if (dijit.byId('"
              + grid_id
              + "').getSelectedRows().length > 1) {showJSMessage(28);resizeArea(true);calculateMsgBoxWidth();return false;} ")
          + " openServletNewWindow('POPUP_HISTORY', false, '../businessUtility/AuditTrail.html?inpRecordId=' + "
          + ((grid_id == null || grid_id.equals("")) ? keyfield + ".value" : "dijit.byId('"
              + grid_id + "').getSelectedRows()")
          + ", 'AuditTrail', null, true, 600, 900, true, null, null, null, true);";
    } else if (grid_id != null && !grid_id.equals("") && name.equals("PREVIOUS")) {
      return "dijit.byId('grid').goToPreviousRow();";
    } else if (grid_id != null && !grid_id.equals("") && name.equals("NEXT")) {
      return "dijit.byId('grid').goToNextRow();";
    } else if (name.equals("DELETE_RELATION")) {
      return "dijit.byId('grid').deleteRow();";
    } else if (name.equals("FIRST_RELATION")) {
      return "dijit.byId('grid').goToFirstRow();";
    } else if (name.equals("LAST_RELATION")) {
      return "dijit.byId('grid').goToLastRow();";
    } else if (name.equals("GRID_VIEW")) {
      return !isRelation ? "submitCommandForm('RELATION', isUserChanges, null, '" + servlet_action
          + (isSrcWindow ? "" : "_Relation") + ".html', '_self', null, true);" : "";
    } else if (name.equals("FORM_VIEW")) {
      return isRelation ? "submitCommandForm('EDIT', true, null, '" + servlet_action
          + (isSrcWindow ? "" : "_Relation") + ".html', '_self', null, false);" : "";
    } else {
      String action = "";

      if ("NEW".equals(name)) {
        action = "disableToolBarButton('linkButtonNew'); ";
      }
      return action + "submitCommandForm('" + (name.equals("REFRESH") ? "DEFAULT" : name) + "', "
          + (name.equals("NEW") && (this.grid_id.equals("")) ? "true" : "false") + ", null, '"
          + servlet_action + (isSrcWindow ? "" : "_Relation") + ".html', '"
          + (isFrame ? "_parent" : "_self") + "', null, " + (debug ? "true" : "false") + ");";
    }
  }

  private void createAllButtons() {
    if (hasNewButton) {
      buttons.put("NEW",
          new ToolBar_Button(base_direction, "New", Utility.messageBD(conn, "New", language),
              getButtonScript("NEW")));
    }

    buttons.put("FIND",
        new ToolBar_Button(base_direction, "Find", Utility.messageBD(conn, "Find", language),
            getButtonScript("FIND")));
    buttons.put("SEPARATOR2", new ToolBar_Space(base_direction));
    buttons.put("SEPARATOR3", new ToolBar_Space(base_direction));
    if (isEditable) {
      buttons.put("DELETE_RELATION",
          new ToolBar_Button(base_direction, "Delete", Utility.messageBD(conn, "Delete", language),
              getButtonScript("DELETE_RELATION")));
    }
    buttons.put("SEPARATOR4", new ToolBar_Space(base_direction));

    if (Utility.isNewUI()) {
      buttons.put(
          "REFRESH",
          new ToolBar_Button(base_direction, "Refresh", Utility
              .messageBD(conn, "Refresh", language), getButtonScript("REFRESH")));
    }

    buttons.put(
        "ATTACHMENT",
        new ToolBar_Button(base_direction, "Attachment", Utility.messageBD(conn, "Attachment",
            language), getButtonScript("ATTACHMENT"), null, hasAttachments ? "AttachedDocuments"
            : "Attachment"));
    buttons.put(
        "EXCEL",
        new ToolBar_Button(base_direction, "Excel", Utility
            .messageBD(conn, "ExportExcel", language), getButtonScript("EXCEL")));
    buttons.put(
        "GRIDEXCEL",
        new ToolBar_Button(base_direction, "ExportExcel", Utility.messageBD(conn, "ExportExcel",
            language), getButtonScript("GRIDEXCEL")));
    buttons.put(
        "GRIDCSV",
        new ToolBar_Button(base_direction, "ExportCsv", Utility.messageBD(conn, "ExportCsv",
            language), getButtonScript("GRIDCSV")));
    buttons.put(
        "GRIDPDF",
        new ToolBar_Button(base_direction, "ExportPDF", Utility.messageBD(conn, "ExportPDF",
            language), getButtonScript("GRIDPDF")));

    if (pdf != null && !pdf.equals("") && !pdf.equals("..")) {
      buttons.put("PRINT",
          new ToolBar_Button(base_direction, "Print", Utility.messageBD(conn, "Print", language),
              getButtonScript("PRINT")));
      buttons.put("EMAIL",
          new ToolBar_Button(base_direction, "Email", Utility.messageBD(conn, "Email", language),
              getButtonScript("EMAIL")));
    }
    buttons.put("AUDIT_SHOW_RELATION_DISABLED",
        new ToolBar_Button(base_direction, "Audit", Utility.messageBD(conn, "ShowAudit", language),
            getButtonScript("AUDIT_RELATION"), false));
    buttons.put(
        "AUDIT_TRAIL",
        new ToolBar_Button(base_direction, "AuditTrail", Utility.messageBD(conn, "AuditTrail",
            language), getButtonScript("AUDIT_TRAIL"), false));
    buttons.put("SEPARATOR5", new ToolBar_Space(base_direction));
    buttons.put("FIRST_RELATION",
        new ToolBar_Button(base_direction, "First", Utility.messageBD(conn, "GotoFirst", language),
            getButtonScript("FIRST_RELATION")));
    buttons.put(
        "PREVIOUS",
        new ToolBar_Button(base_direction, "Previous", Utility.messageBD(conn, "GotoPrevious",
            language), getButtonScript("PREVIOUS")));
    buttons.put("NEXT",
        new ToolBar_Button(base_direction, "Next", Utility.messageBD(conn, "GotoNext", language),
            getButtonScript("NEXT")));
    buttons.put("LAST_RELATION",
        new ToolBar_Button(base_direction, "Last", Utility.messageBD(conn, "GotoLast", language),
            getButtonScript("LAST_RELATION")));

    buttons.put("SEPARATOR6", new ToolBar_Space(base_direction));
    // buttons.put("PREVIOUS_RELATION", new ToolBar_Button(base_direction,
    // "PreviousRange", Utility.messageBD(conn, "GotoPreviousRange",
    // language), getButtonScript("PREVIOUS_RELATION")));
    buttons.put(
        "PREVIOUS_RELATION",
        new ToolBar_Button(base_direction, "Previous", Utility.messageBD(conn, "GotoPreviousRange",
            language), getButtonScript("PREVIOUS_RELATION")));
    buttons.put("PREVIOUS_RELATION_DISABLED", new ToolBar_Button(base_direction,
        "PreviousRangeDisabled", Utility.messageBD(conn, "GotoPreviousRange", language), ""));
    // buttons.put("NEXT_RELATION", new ToolBar_Button(base_direction,
    // "NextRange", Utility.messageBD(conn, "GotoNextRange", language),
    // getButtonScript("NEXT_RELATION")));
    buttons.put(
        "NEXT_RELATION",
        new ToolBar_Button(base_direction, "Next", Utility.messageBD(conn, "GotoNextRange",
            language), getButtonScript("NEXT_RELATION")));
    buttons.put("NEXT_RELATION_DISABLED", new ToolBar_Button(base_direction, "NextRangeDisabled",
        Utility.messageBD(conn, "GotoNextRange", language), ""));

    buttons.put("SEPARATOR7", new ToolBar_Space(base_direction));
    buttons.put("HR1", new ToolBar_HR());
    buttons.put(
        "RELATED_INFO",
        new ToolBar_Button(base_direction, "RelatedInfo", Utility.messageBD(conn, "Linked Items",
            language), getButtonScript("RELATED_INFO")));
  }

  /**
   * Utility function to change the visibility of the 'Audit Trail' icon.
   * 
   * community instances: always shown
   * 
   * activated instances: shown if isFullyAudited for the table
   */
  private void changeAuditTrailVisibility() {
    // always hide button in the audit trail window
    if ("3690EB6BA1614375A6F058BBA61B19BC".equals(tabId)) {
      removeElement("AUDIT_TRAIL");
      return;
    }
    if (tabId != null) {
      OBContext.setAdminMode();
      try {
        // ActivationKey already initialized in i.e. HSAS, so just take static info
        if (ActivationKey.isActiveInstance()) {
          // is an obps instance
          Tab tab = OBDal.getInstance().get(Tab.class, tabId);
          if (!tab.getTable().isFullyAudited()) {
            // remove icon
            removeElement("AUDIT_TRAIL");
          }
        }
      } finally {
        OBContext.restorePreviousMode();
      }
    }
  }

  public void prepareRelationTemplateNoSearch() {
    isRelation = true;
    removeElement("FIND");
    removeElement("EXCEL");

    removeElement("PREVIOUS_RELATION");
    removeElement("PREVIOUS_RELATION_DISABLED");
    removeElement("NEXT_RELATION");
    removeElement("NEXT_RELATION_DISABLED");

    if (!deleteable) {
      removeElement("DELETE_RELATION");
    }

    if (pdf != null && !pdf.contains("orders") && !pdf.contains("invoices")
        && !pdf.contains("payments")) {
      removeElement("EMAIL");
    }

    changeAuditTrailVisibility();
  }

  public void prepareSimpleToolBarTemplate() {
    removeElement("NEW");
    removeElement("FIND");
    removeElement("SEPARATOR2");
    removeElement("DELETE_RELATION");
    removeElement("ATTACHMENT");
    removeElement("EXCEL");
    removeElement("GRIDEXCEL");
    removeElement("GRIDCSV");
    removeElement("GRIDPDF");
    removeElement("FIRST_RELATION");
    removeElement("PREVIOUS");
    removeElement("NEXT");
    removeElement("LAST_RELATION");
    removeElement("PREVIOUS_RELATION");
    removeElement("PREVIOUS_RELATION_DISABLED");
    removeElement("NEXT_RELATION");
    removeElement("NEXT_RELATION_DISABLED");
    removeElement("EMAIL");
    removeElement("PRINT");
    if (pdf != null && !pdf.equals("") && !pdf.equals("..")) {
      buttons.put("PRINT",
          new ToolBar_Button(base_direction, "Print", Utility.messageBD(conn, "Print", language),
              pdf));

    }
    if (email) {
      buttons.put("EMAIL",
          new ToolBar_Button(base_direction, "Email", Utility.messageBD(conn, "Email", language),
              pdf));
    }
    removeElement("RELATED_INFO");

    removeElement("AUDIT_SHOW_RELATION_DISABLED");
    removeElement("AUDIT_TRAIL");
  }

  public void prepareRelationBarTemplate(boolean hasPrevious, boolean hasNext) {
    prepareRelationBarTemplate(hasPrevious, hasNext, "");
  }

  public void prepareRelationBarTemplate(boolean hasPrevious, boolean hasNext, String excelScript) {
    removeElement("NEW");
    removeElement("FIND");
    removeElement("SEPARATOR2");
    removeElement("DELETE_RELATION");
    removeElement("ATTACHMENT");
    removeElement("EXCEL");
    removeElement("GRIDEXCEL");
    removeElement("GRIDCSV");
    removeElement("GRIDPDF");
    removeElement("FIRST_RELATION");
    removeElement("PREVIOUS");
    removeElement("NEXT");
    removeElement("LAST_RELATION");
    removeElement("EMAIL");
    removeElement("PRINT");

    removeElement(hasPrevious ? "PREVIOUS_RELATION_DISABLED" : "PREVIOUS_RELATION");
    removeElement(hasNext ? "NEXT_RELATION_DISABLED" : "NEXT_RELATION");

    removeElement("RELATED_INFO"); // Modified
    if (pdf != null && !pdf.equals("") && !pdf.equals("..")) {
      buttons.put("PRINT",
          new ToolBar_Button(base_direction, "Print", Utility.messageBD(conn, "Print", language),
              pdf));

    }
    if (email) {
      buttons.put("EMAIL",
          new ToolBar_Button(base_direction, "Email", Utility.messageBD(conn, "Email", language),
              pdf));
    }
    if (!excelScript.equals("") && excelScript != null)
      buttons.put(
          "EXCEL",
          new ToolBar_Button(base_direction, "Excel", Utility.messageBD(conn, "ExportExcel",
              language), excelScript));

    removeElement("AUDIT_SHOW_RELATION_DISABLED");
    removeElement("AUDIT_TRAIL");
  }

  public void prepareSimpleExcelToolBarTemplate(String excelScript) {
    removeElement("NEW");
    removeElement("FIND");
    removeElement("SEPARATOR2");
    removeElement("DELETE_RELATION");
    removeElement("ATTACHMENT");
    removeElement("GRIDEXCEL");
    removeElement("GRIDCSV");
    removeElement("GRIDPDF");
    removeElement("FIRST_RELATION");
    removeElement("PREVIOUS");
    removeElement("NEXT");
    removeElement("LAST_RELATION");
    removeElement("PREVIOUS_RELATION");
    removeElement("PREVIOUS_RELATION_DISABLED");
    removeElement("NEXT_RELATION");
    removeElement("NEXT_RELATION_DISABLED");

    if (!excelScript.equals("") && excelScript != null)
      buttons.put(
          "EXCEL",
          new ToolBar_Button(base_direction, "Excel", Utility.messageBD(conn, "ExportExcel",
              language), excelScript));

    removeElement("AUDIT_SHOW_RELATION_DISABLED");
    removeElement("AUDIT_TRAIL");

  }

  public void prepareQueryTemplate(boolean hasPrevious, boolean hasNext, boolean isTest) {
    removeElement("NEW");
    removeElement("DELETE_RELATION");
    removeElement("GRIDEXCEL");
    removeElement("GRIDCSV");
    removeElement("GRIDPDF");
    removeElement("ATTACHMENT");
    removeElement("FIRST_RELATION");
    removeElement("LAST_RELATION");
    removeElement("PREVIOUS");
    removeElement("NEXT");

    if (!hasPrevious)
      removeElement("PREVIOUS_RELATION");
    else
      removeElement("PREVIOUS_RELATION_DISABLED");
    if (!hasNext)
      removeElement("NEXT_RELATION");
    else
      removeElement("NEXT_RELATION_DISABLED");

    removeElement("AUDIT_SHOW_RELATION_DISABLED");
    removeElement("AUDIT_TRAIL");
    removeElement("RELATED_INFO");
  }

  private String transformElementsToString(HTMLElement element, Vector<String> vecLastType,
      boolean isReference) {
    Vector<String> localVecLastType = vecLastType;
    if (element == null)
      return "";
    if (localVecLastType == null)
      localVecLastType = new Vector<String>(0);
    final StringBuffer sbElement = new StringBuffer();
    String lastType = "";
    if (localVecLastType.size() > 0)
      lastType = localVecLastType.elementAt(0);
    if (lastType.equals("SPACE") && element.elementType().equals("SPACE"))
      return "";
    if (isReference) {
      sbElement.append("<td width=\"1\">");
      sbElement.append("<img src=\"").append(base_direction)
          .append("/images/blank.gif\" class=\"Main_ToolBar_textlabel_bg_left\" border=\"0\">");
      sbElement.append("</td>\n");
      sbElement.append("<td class=\"Main_ToolBar_textlabel_bg_body\">");
      sbElement
          .append(
              "<a class=\"Main_ToolBar_text_relatedinfo\" href=\"#\" onclick=\""
                  + (this.isNew ? "logClick(null);" : "")
                  + "openServletNewWindow('DEFAULT', true, '../utility/UsedByLink.html', 'LINKS', null, true, 500, 600, true);\">")
          .append(Utility.messageBD(conn, "Linked Items", language)).append("</a></td>\n");
    }
    sbElement.append("<td ");
    if (isReference)
      sbElement.append("class=\"Main_ToolBar_textlabel_bg_right\" ");
    if (element.elementType().equals("SPACE"))
      sbElement.append("class=\"Main_ToolBar_Separator_cell\" ");
    else if (!element.elementType().equals("HR"))
      sbElement.append("width=\"").append(element.getWidth()).append("\" ");
    else
      sbElement.append("class=\"Main_ToolBar_Space\"");
    sbElement.append(">");
    if (!element.elementType().equals("HR"))
      sbElement.append(element);
    sbElement.append("</td>\n");
    localVecLastType.clear();
    localVecLastType.addElement(element.elementType());
    return sbElement.toString();
  }

  @Override
  public String toString() {
    final StringBuffer toolbar = new StringBuffer();
    toolbar.append("<table class=\"Main_ContentPane_ToolBar Main_ToolBar_bg\" id=\"tdToolBar\">\n");
    toolbar.append("<tr>\n");
    if (buttons != null) {
      final Vector<String> lastType = new Vector<String>(0);

      // In case of using new UI, add in toolbar grid and edition buttons
      if (Utility.isNewUI() && !isSrcWindow) {
        buttons.put(
            "FORM_VIEW",
            new ToolBar_Button(base_direction, "Edition", Utility.messageBD(conn, "Form View",
                language), getButtonScript("FORM_VIEW"), !isRelation, "Edition"
                + (isNew ? "_new" : "")));
        buttons.put(
            "GRID_VIEW",
            new ToolBar_Button(base_direction, "Relation", Utility.messageBD(conn, "Grid View",
                language), getButtonScript("GRID_VIEW"), isRelation));
        buttons.put("SEPARATOR_NEWUI", new ToolBar_Space(base_direction));
        toolbar.append(transformElementsToString(buttons.get("FORM_VIEW"), lastType, false));
        toolbar.append(transformElementsToString(buttons.get("GRID_VIEW"), lastType, false));
        toolbar.append(transformElementsToString(buttons.get("SEPARATOR_NEWUI"), lastType, false));
      }

      toolbar.append(transformElementsToString(buttons.get("NEW"), lastType, false));
      toolbar.append(transformElementsToString(buttons.get("FIND"), lastType, false));
      toolbar.append(transformElementsToString(buttons.get("SEPARATOR2"), lastType, false));
      toolbar.append(transformElementsToString(buttons.get("SEPARATOR3"), lastType, false));
      toolbar.append(transformElementsToString(buttons.get("DELETE_RELATION"), lastType, false));
      toolbar.append(transformElementsToString(buttons.get("SEPARATOR4"), lastType, false));
      toolbar.append(transformElementsToString(buttons.get("REFRESH"), lastType, false));
      toolbar.append(transformElementsToString(buttons.get("ATTACHMENT"), lastType, false));
      toolbar.append(transformElementsToString(buttons.get("EXCEL"), lastType, false));
      // if some module still runs this code path -> Log error with StackTrace
      if (buttons.containsKey("GRIDEXCEL") || buttons.containsKey("GRIDCSV")
          || buttons.containsKey("GRIDPDF")) {
        log.error("Old 2.50 ExportGrid functionality does no longer exist.", new Throwable());
      }
      toolbar.append(transformElementsToString(buttons.get("PRINT"), lastType, false));
      toolbar.append(transformElementsToString(buttons.get("EMAIL"), lastType, false));
      toolbar.append(transformElementsToString(buttons.get("AUDIT_SHOW_RELATION_DISABLED"),
          lastType, false));
      toolbar.append(transformElementsToString(buttons.get("AUDIT_TRAIL"), lastType, false));
      toolbar.append(transformElementsToString(buttons.get("SEPARATOR5"), lastType, false));
      toolbar.append(transformElementsToString(buttons.get("FIRST_RELATION"), lastType, false));
      toolbar.append(transformElementsToString(buttons.get("PREVIOUS"), lastType, false));
      // toolbar.append("<td class=\"TB_Bookmark\" width=\"5px\"><nobr id=\"bookmark\"></nobr></td>\n"
      // );
      toolbar.append(transformElementsToString(buttons.get("NEXT"), lastType, false));
      toolbar.append(transformElementsToString(buttons.get("LAST_RELATION"), lastType, false));
      if (isRelation) {
        toolbar.append("<td width=\"1\"><img src=\"").append(base_direction)
            .append("/images/blank.gif\" style=\"width: 7px;\" border=\"0\">");
        toolbar.append("<td width=\"1\"><img src=\"").append(base_direction)
            .append("/images/blank.gif\" class=\"Main_ToolBar_textlabel_bg_left\" border=\"0\">");
        toolbar.append("</td>\n");
        toolbar.append("<td class=\"Main_ToolBar_textlabel_bg_body\">\n");
        toolbar.append("<div id=\"bookmark\">\n");
        toolbar.append("</div>\n");
        toolbar.append("</td>\n");
        toolbar.append("<td width=\"1\" class=\"Main_ToolBar_textlabel_bg_right\">");
        toolbar.append("<div style=\"padding: 0; margin: 0; border: 0; width: 9px;\" />");
        toolbar.append("</td>\n");
      }
      toolbar.append(transformElementsToString(buttons.get("SEPARATOR6"), lastType, false));
      toolbar.append(transformElementsToString(buttons.get("PREVIOUS_RELATION"), lastType, false));
      toolbar.append(transformElementsToString(buttons.get("PREVIOUS_RELATION_DISABLED"), lastType,
          false));
      toolbar.append(transformElementsToString(buttons.get("NEXT_RELATION"), lastType, false));
      toolbar.append(transformElementsToString(buttons.get("NEXT_RELATION_DISABLED"), lastType,
          false));
      toolbar.append(transformElementsToString(buttons.get("SEPARATOR7"), lastType, false));
      toolbar.append(transformElementsToString(buttons.get("HR1"), lastType, false));
      toolbar.append(transformElementsToString(buttons.get("RELATED_INFO"), lastType, true));
    }
    toolbar.append("</tr>\n");
    toolbar.append("</table>\n");
    return toolbar.toString();
  }
}
