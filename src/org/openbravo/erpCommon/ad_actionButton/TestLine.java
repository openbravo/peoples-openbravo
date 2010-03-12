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
 * All portions are Copyright (C) 2001-2006 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.erpCommon.ad_actionButton;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;

import org.openbravo.base.MultipartRequest;
import org.openbravo.base.VariablesBase;
import org.openbravo.data.FieldProvider;
import org.openbravo.database.ConnectionProvider;

public class TestLine extends MultipartRequest implements FieldProvider {
  public String comando;
  public String arg1;
  public String arg2;
  public String arg3;

  public Pattern pattern;
  public Matcher matcher;
  public String currentWindow;
  public String currentFrame;
  public String currentTab;
  public boolean generatedWindow = false;
  public boolean generatedWindowPopup = false;

  public ConnectionProvider conn;

  public TestLine(VariablesBase _vars, String _filename, boolean _firstLineHeads,
      String _currentWindow, String _currentFrame, boolean _generatedWindow,
      ConnectionProvider _conn) throws IOException {
    super();
    this.currentWindow = _currentWindow;
    this.currentFrame = _currentFrame;
    this.generatedWindow = _generatedWindow;
    this.conn = _conn;
    init(_vars, _filename, _firstLineHeads, "C", null);
    readSubmittedFile();
  }

  public TestLine(String _currentWindow, String _currentFrame, boolean _generatedWindow,
      ConnectionProvider _conn) {
    this.currentWindow = _currentWindow;
    this.currentFrame = _currentFrame;
    this.generatedWindow = _generatedWindow;
    this.conn = _conn;
  }

  public String getField(String fieldName) {
    if (fieldName.equalsIgnoreCase("comando"))
      return comando;
    else if (fieldName.equalsIgnoreCase("arg1"))
      return arg1;
    else if (fieldName.equalsIgnoreCase("arg2"))
      return arg2;
    else if (fieldName.equalsIgnoreCase("arg3"))
      return arg3;
    else {
      return null;
    }
  }

  public FieldProvider lineFixedSize(String linea) {
    return null;
  }

  public FieldProvider lineSeparatorFormated(String linea) {
    if (linea.length() < 1)
      return null;
    TestLine TestLine = new TestLine(currentWindow, currentFrame, generatedWindow, conn);
    if (linea.substring(0, 2).equals("//"))
      return TestLine;

    // To replace the completed-process popup
    pattern = Pattern.compile("setAlertWindow\\( \".+?\",[0-9]{1,2}\\)");
    matcher = pattern.matcher("");
    matcher.reset(linea);
    if (matcher.find()) {
      TestLine.comando = "800127";
      return TestLine;
    }

    // To replace setTest by setField
    pattern = Pattern.compile("setText\\(\"(.+?)\",\"(.+?)\",[0-9]{1,2}\\)");
    matcher = pattern.matcher("");
    matcher.reset(linea);
    if (matcher.find()) {
      if (generatedWindow) {
        // To make it get all the login fields, because it's still
        // unknown if it's generated
        if (matcher.group(1).equals("user") || matcher.group(1).equals("password"))
          TestLine.comando = "800152";
        else if (isCheckTextField(currentTab, argErase(matcher.group(1))))
          TestLine.comando = "800108";
        else
          TestLine.comando = "800156";
      } else
        TestLine.comando = "800152";
      // if(!generatedWindow) TestLine.comando = "800162"; The textarea is
      // not considered in non-generated
      TestLine.arg1 = matcher.group(1);
      TestLine.arg2 = matcher.group(2);
      // To make it not show BLANK in the logout
      if (TestLine.arg2.equals("_QE_BLANK"))
        TestLine.comando = "";
      return TestLine;
    }

    // To replace the drop-down SeletItem
    pattern = Pattern.compile("selectItem\\(\"report(.+?)_S\",\"(.+?)\",[0-9]{1,2}\\)");
    matcher = pattern.matcher("");
    matcher.reset(linea);
    if (matcher.find()) {
      TestLine.comando = "800111";
      TestLine.arg1 = matcher.group(1);
      TestLine.arg2 = matcher.group(2);
      return TestLine;
    }
    // To solve the non-generated windows drop-down problem
    pattern = Pattern.compile("selectItem\\(\"(.+?)\",\"(.+?)\",[0-9]{1,2}\\)");
    matcher = pattern.matcher("");
    matcher.reset(linea);
    if (matcher.find()) {
      TestLine.comando = "800154";
      TestLine.arg1 = matcher.group(1);
      TestLine.arg2 = matcher.group(2);
      return TestLine;
    }
    // Click on the id-less image
    pattern = Pattern.compile("clickImage\\(\"(http.+?)\",[0-9]{1,2}\\)");
    matcher = pattern.matcher("");
    matcher.reset(linea);
    if (matcher.find()) {
      TestLine.comando = "800153";
      TestLine.arg1 = matcher.group(1);
      return TestLine;
    }
    // Click on the popup with id
    pattern = Pattern.compile("clickImage\\(\"(command.+?)\",[0-9]{1,2}\\)");
    matcher = pattern.matcher("");
    matcher.reset(linea);
    if (matcher.find()) {
      TestLine.comando = "800155";
      TestLine.arg1 = matcher.group(1);
      return TestLine;
    }
    // Click on the button panel
    pattern = Pattern.compile("clickImage\\(\"(button.+?)\",[0-9]{1,2}\\)");
    matcher = pattern.matcher("");
    matcher.reset(linea);
    if (matcher.find()) {
      TestLine.comando = "800140";
      TestLine.arg1 = matcher.group(1);
      return TestLine;
    }
    // Click on the openbravo image
    pattern = Pattern.compile("clickImage\\(\"openbravoLogo\",[0-9]{1,2}\\)");
    matcher = pattern.matcher("");
    matcher.reset(linea);
    if (matcher.find()) {
      TestLine.comando = "800139";
      return TestLine;
    }
    // Click on the tab's title
    pattern = Pattern.compile("clickElement\\(\"tabtitle(.+?)\",[0-9]{1,2}\\)");
    matcher = pattern.matcher("");
    matcher.reset(linea);
    if (matcher.find()) {
      TestLine.comando = "800148";
      TestLine.arg1 = matcher.group(1);
      currentTab = matcher.group(1);
      return TestLine;
    }
    // Click on the process button
    pattern = Pattern.compile("clickElement\\(\"(.+?)\",[0-9]{1,2}\\)");
    matcher = pattern.matcher("");
    matcher.reset(linea);
    if (matcher.find()) {
      TestLine.comando = "800128";
      TestLine.arg1 = matcher.group(1);
      TestLine.arg2 = matcher.group(1);
      return TestLine;
    }
    // LEFT MENU
    // We capture the clicks on the left side menu. Window
    pattern = Pattern.compile("clickLink\\(\"childwindow(.+?)\",[0-9]{1,2}\\)");
    matcher = pattern.matcher("");
    matcher.reset(linea);
    if (matcher.find()) {
      TestLine.comando = "800141";
      TestLine.arg1 = "ad_menu-" + matcher.group(1);
      generatedWindow = checkIsGenerated("window", matcher.group(1));
      if (generatedWindow) {
        try {
          currentTab = TestLineData.setTabId(conn, matcher.group(1));
        } catch (ServletException ex) {
          ex.printStackTrace();
        }
      } else
        currentTab = "";
      return TestLine;
    }
    // We capture the clicks on the left side menu.Process
    pattern = Pattern.compile("clickLink\\(\"childprocess(.+?)\",[0-9]{1,2}\\)");
    matcher = pattern.matcher("");
    matcher.reset(linea);
    if (matcher.find()) {
      TestLine.comando = "800142";
      TestLine.arg1 = "ad_menu-" + matcher.group(1);
      generatedWindow = checkIsGenerated("process", matcher.group(1));
      if (generatedWindow) {
        try {
          currentTab = TestLineData.setTabId(conn, matcher.group(1));
        } catch (ServletException ex) {
          ex.printStackTrace();
        }
      } else
        currentTab = "";
      return TestLine;
    }
    // We capture the clicks on the left side menu.Report
    pattern = Pattern.compile("clickLink\\(\"childreport(.+?)\",[0-9]{1,2}\\)");
    matcher = pattern.matcher("");
    matcher.reset(linea);
    if (matcher.find()) {
      TestLine.comando = "800144";
      TestLine.arg1 = "ad_menu-" + matcher.group(1);
      generatedWindow = false;
      currentTab = "";
      return TestLine;
    }
    // We capture the clicks on the left side menu.Form
    pattern = Pattern.compile("clickLink\\(\"childform(.+?)\",[0-9]{1,2}\\)");
    matcher = pattern.matcher("");
    matcher.reset(linea);
    if (matcher.find()) {
      TestLine.comando = "800145";
      TestLine.arg1 = "ad_menu-" + matcher.group(1);
      generatedWindow = false;
      currentTab = "";
      return TestLine;
    }
    // We capture the clicks on the left side menu.WorkFlow
    pattern = Pattern.compile("clickLink\\(\"childwf(.+?)\",[0-9]{1,2}\\)");
    matcher = pattern.matcher("");
    matcher.reset(linea);
    if (matcher.find()) {
      TestLine.comando = "800160";
      TestLine.arg1 = "ad_menu-" + matcher.group(1);
      generatedWindow = true;
      try {
        currentTab = TestLineData.setTabId(conn, matcher.group(1));
      } catch (ServletException ex) {
        ex.printStackTrace();
      }
      return TestLine;
    }
    // We capture the clicks on the left side menu.Task
    pattern = Pattern.compile("clickLink\\(\"childtask(.+?)\",[0-9]{1,2}\\)");
    matcher = pattern.matcher("");
    matcher.reset(linea);
    if (matcher.find()) {
      TestLine.comando = "800161";
      TestLine.arg1 = "ad_menu-" + matcher.group(1);
      generatedWindow = true;
      return TestLine;
    }
    // We capture the clicks on the left side menu.Folder
    pattern = Pattern.compile("clickLink\\(\"folderHref(.+?)\",[0-9]{1,2}\\)");
    matcher = pattern.matcher("");
    matcher.reset(linea);
    if (matcher.find()) {
      TestLine.comando = "800147";
      TestLine.arg1 = "ad_menu-" + matcher.group(1);
      return TestLine;
    }
    // We capture the clicks to change the tab
    pattern = Pattern.compile("clickLink\\(\"tabname(.+?)\",[0-9]{1,2}\\)");
    matcher = pattern.matcher("");
    matcher.reset(linea);
    if (matcher.find()) {
      TestLine.comando = "800150";
      TestLine.arg1 = "ad_tab-" + matcher.group(1);
      generatedWindow = checkIsGenerated("windowtab", matcher.group(1));
      currentTab = matcher.group(1);
      return TestLine;
    }
    // We capture the popup's accept button click
    pattern = Pattern.compile("clickButton\\(\"Button\",[0-9]{1,2}\\)");
    matcher = pattern.matcher("");
    matcher.reset(linea);
    if (matcher.find()) {
      return TestLine;
    }
    // We capture the button clicks
    pattern = Pattern.compile("clickButton\\(\"(inp.+?)_?([0-9]{1,2})?\",[0-9]{1,2}\\)");
    matcher = pattern.matcher("");
    matcher.reset(linea);
    if (matcher.find()) {
      TestLine.comando = "800119";
      TestLine.arg1 = matcher.group(1);
      TestLine.arg2 = "0";
      return TestLine;
    }
    // We capture the static-checkbox set's clicks
    pattern = Pattern.compile("clickButton\\(\"param(.+?)_M_?([0-9]{1,2})?\",[0-9]{1,2}\\)");
    matcher = pattern.matcher("");
    matcher.reset(linea);
    if (matcher.find()) {
      TestLine.comando = "800159";
      TestLine.arg1 = matcher.group(1);
      return TestLine;
    }
    // We capture the static-checkbox set's click. READY FOR THE "_" BY
    // REPETITON
    pattern = Pattern.compile("clickButton\\(\"(.+?)_?([0-9]{1,2})?\",[0-9]{1,2}\\)");
    matcher = pattern.matcher("");
    matcher.reset(linea);
    if (matcher.find()) {
      TestLine.comando = "800117";
      TestLine.arg1 = matcher.group(1);
      return TestLine;
    }

    // The static menu
    pattern = Pattern.compile("clickLink\\(\"(.+?)\",[0-9]{1,2}\\)");
    matcher = pattern.matcher("");
    matcher.reset(linea);
    if (matcher.find()) {
      TestLine.comando = "800146";
      TestLine.arg1 = matcher.group(1);
      return TestLine;
    }
    // For the ASSERT of an element
    pattern = Pattern.compile("showContextMenu\\(\"(.+?)\",[0-9]{1,2}\\)");
    matcher = pattern.matcher("");
    matcher.reset(linea);
    if (matcher.find()) {
      TestLine.comando = "800114";
      TestLine.arg1 = matcher.group(1);
      return TestLine;
    }
    // To replace SETWINDOW Y frame in caps (invented) by seekandsetWindow
    pattern = Pattern.compile("setWindow\\(\\s\"(.+?)_([A-Z]+)_?([0-9]{1,2})?\",[0-9]{1,2}\\)");// gorka
    matcher = pattern.matcher("");
    matcher.reset(linea);
    if (matcher.find()) {
      int compWindow = matcher.group(1).compareTo(currentWindow);
      if (compWindow != 0) {
        TestLine.arg1 = matcher.group(1);
        TestLine.arg2 = "false";
        TestLine.comando = "800116";
        if (!currentWindow.equals("Aplicación de gestión")
            && matcher.group(1).equals("Aplicación de gestión"))
          generatedWindow = generatedWindowPopup;
        else if (currentWindow.equals("Aplicación de gestión")
            && !matcher.group(1).equals("Aplicación de gestión")) {
          generatedWindowPopup = generatedWindow;
          generatedWindow = false;
        }
        currentWindow = matcher.group(1);

      }
      return TestLine;
    }
    // to replace SETWINDOW by seekandsetWindow and setFrame
    // pattern =
    // Pattern.compile("setWindow\\(\\s\"(.+?)_([\\w]+)_?([0-9]{1,2})?\",[0-9]{1,2}\\)");
    pattern = Pattern.compile("setWindow\\(\\s\"(.+?)_([\\w]+?)_?([0-9]{1,2})?\",[0-9]{1,2}\\)");
    matcher = pattern.matcher("");
    matcher.reset(linea);
    if (matcher.find()) {
      int compWindow = matcher.group(1).compareTo(currentWindow);
      int compFrame = matcher.group(2).compareTo(currentFrame);
      if (!currentWindow.equals("Aplicación de gestión")
          && matcher.group(1).equals("Aplicación de gestión"))
        generatedWindow = generatedWindowPopup;
      else if (currentWindow.equals("Aplicación de gestión")
          && !matcher.group(1).equals("Aplicación de gestión")) {
        generatedWindowPopup = generatedWindow;
        generatedWindow = false;
      }

      if (compWindow != 0) {
        TestLine.arg1 = matcher.group(1);
        TestLine.arg2 = matcher.group(2);
        TestLine.comando = "800149";
        currentWindow = matcher.group(1);
        currentFrame = matcher.group(2);
      } else if (compFrame != 0) {
        TestLine.arg1 = matcher.group(2);
        TestLine.comando = "800107";
        currentFrame = matcher.group(2);
      }
      return TestLine;
    }

    // To close a window
    // pattern =
    // Pattern.compile("closeWindow\\(\\s\"(.+?)_([\\w]+)\",[0-9]{1,2}\\)");
    // matcher = pattern.matcher("");
    // matcher.reset(linea);
    // if (matcher.find()) {
    // TestLine.comando = "800151";
    // return TestLine;
    // }
    return TestLine;
  }

  // With this class we know if a window is either manual or wad-generated
  public boolean checkIsGenerated(String element, String generatedId) {
    TestLineData[] comprobar = null;
    // To know if a Window is manual
    if (element.equals("window")) {
      try {
        comprobar = TestLineData.selectWindow(conn, generatedId);
        if (comprobar.length == 1) {
          return true;
        }
        if (comprobar.length == 0) {
          return false;
        }
      } catch (ServletException ex) {
        ex.printStackTrace();
      }
    }

    // To know if a Process is manual
    if (element.equals("process")) {
      try {
        comprobar = TestLineData.selectProcess(conn, generatedId);
        if (comprobar.length == 1) {
          return true;
        }
        if (comprobar.length == 0) {
          return false;
        }
      } catch (ServletException ex) {
        ex.printStackTrace();
      }
    }

    // To check if a Tab belongs to a manual window
    if (element.equals("windowtab")) {
      try {
        comprobar = TestLineData.selectWindowTab(conn, generatedId);
        if (comprobar.length == 1) {
          return true;
        }
        if (comprobar.length == 0) {
          return false;
        }
      } catch (ServletException ex) {
        ex.printStackTrace();
      }
    }
    return false;
  }

  // With this class we know if it is a field or a TextArea
  public boolean isCheckTextField(String tab, String column) {
    // TestLineData[] comprobar=null;
    String comprobar = "";
    // To check the length of a field
    try {
      comprobar = TestLineData.checkFieldOrArea(conn, tab, column);
    } catch (ServletException ex) {
      ex.printStackTrace();
    }
    if (Integer.valueOf(comprobar).intValue() <= 110) {
      return true;
    } else {
      // if(comprobar.length>110){
      return false;
    }

    // return false;
  }

  // With this class we know if it's a checkBox
  public boolean isCheck(String tab, String column) {
    // TestLineData[] comprobar=null;
    String comprobar = "";
    // To check the length of a field
    try {
      comprobar = TestLineData.checkButtonOrCheck(conn, tab, column);
    } catch (ServletException ex) {
      ex.printStackTrace();
    }
    if (comprobar.equals("20")) {
      return true;
    }

    return false;
  }

  // With this class we knoe if it's a button
  public boolean isButton(String tab, String column) {
    // TestLineData[] comprobar=null;
    String comprobar = "";
    // To check the length of a field
    try {
      comprobar = TestLineData.checkButtonOrCheck(conn, tab, column);
    } catch (ServletException ex) {
      ex.printStackTrace();
    }
    if (comprobar.equals("28")) {
      return true;
    }

    return false;
  }

  private String argErase(String arg) {
    if (arg.endsWith("_D"))
      arg = arg.substring(0, arg.lastIndexOf("_"));
    else if (arg.endsWith("_BTN"))
      arg = arg.substring(0, arg.lastIndexOf("_"));

    return arg;
  }

}
