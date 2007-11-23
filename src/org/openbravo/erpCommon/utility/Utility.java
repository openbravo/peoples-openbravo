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
import org.openbravo.erpCommon.reference.*;
import org.openbravo.data.FieldProvider;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.data.Sqlc;
import org.openbravo.erpCommon.utility.DateTimeData;
import org.openbravo.utils.Replace;
import java.util.StringTokenizer;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Vector;
import javax.servlet.ServletException;
import java.io.*;
import org.apache.log4j.Logger ;

public class Utility {
  static Logger log4j = Logger.getLogger(Utility.class);
/*
  public static String truncate(String s, int i) {
    if(s == null && s.length() == 0) return "";
    if(i < s.length()) s = s.substring(0, i) + "...";
    return s;
  }

  public static String replaceTildes(String strIni) {
    //Delete tilde characters
    return Replace.replace( Replace.replace( Replace.replace( Replace.replace( Replace.replace( Replace.replace( Replace.replace( Replace.replace( Replace.replace( Replace.replace( strIni, "á", "a"), "é", "e"), "í", "i"), "ó", "o"), "ú", "u"), "Á", "A"), "É", "E"), "Í", "I"), "Ó", "O"), "Ú", "U");
  }

  public static String replace(String strIni) {
    //delete characters: " ","&",","
    return Replace.replace( Replace.replace( Replace.replace( Replace.replace( Replace.replace( Replace.replace( Replace.replace( Replace.replace( replaceTildes(strIni), "-",""), "/", ""), "#", ""), " ", ""), "&", ""), ",", ""), "(", ""), ")", "");
  }

  public static String replaceJS(String strIni) {
    return replaceJS(strIni, true);
  }

  public static String replaceJS(String strIni, boolean isUnderQuotes) {
    return Replace.replace( Replace.replace(Replace.replace(Replace.replace(strIni, "'", (isUnderQuotes?"\\'":"&#039;")), "\"", "\\\""), "\n", "\\n"), "\r", "");
  }
*/
  public static boolean isDecimalNumber (String reference) {
    if (reference==null || reference.equals("")) return false;
    switch (Integer.valueOf(reference).intValue()) {
    case 12:
    case 22:
    case 29: 
    case 80008: return true;
    }
    return false;
  }

  public static boolean isIntegerNumber (String reference) {
    if (reference==null || reference.equals("")) return false;
    switch (Integer.valueOf(reference).intValue()) {
    case 11:
    case 13:
    case 25: return true;
    }
    return false;
  }

  public static boolean isDateTime (String reference) {
    if (reference==null || reference.equals("")) return false;
    switch (Integer.valueOf(reference).intValue()) {
    case 15:
    case 16:
    case 24: return true;
    }
    return false;
  }

  public static boolean hasAttachments(ConnectionProvider conn, String userClient, String userOrg, String tableId, String recordId) throws ServletException {
    if (tableId.equals("") || recordId.equals("")) return false;
    else return UtilityData.select(conn, userClient, userOrg, tableId, recordId);
  }

  public static String messageBD(ConnectionProvider conn, String strCode, String strLanguage) {
    String strMessage="";
    if (strLanguage==null || strLanguage.equals("")) strLanguage = "en_US";

    try{
      log4j.debug("Utility.messageBD - Message Code: " + strCode);
      if (strLanguage.equals("en_US")) strMessage = MessageBDData.message(conn, strCode);
      else strMessage = MessageBDData.messageLanguage(conn, strCode, strLanguage);
    } catch(Exception ignore){}
    log4j.debug("Utility.messageBD - Message description: " + strMessage);
    if (strMessage==null || strMessage.equals("")) {
      try {
        if (strLanguage.equals("en_US")) strMessage = MessageBDData.columnname(conn, strCode);
        else strMessage = MessageBDData.columnnameLanguage(conn, strCode, strLanguage);
      } catch(Exception e) {
        strMessage = strCode;
      }
    }
    if (strMessage==null || strMessage.equals("")) strMessage = strCode;
    return Replace.replace(Replace.replace(strMessage, "\n", "\\n"), "\"", "&quot;");
  }

  public static String getPreference(VariablesSecureApp vars, String context, String window) {
    if (context==null || context.equals("")) throw new IllegalArgumentException("getPreference - require context");
    String retValue="";

    retValue = vars.getSessionValue("P|" + window + "|" + context);
    if (retValue.equals("")) retValue = vars.getSessionValue("P|" + context);

    return (retValue);
  }

  public static String getTransactionalDate(ConnectionProvider conn, VariablesSecureApp vars, String window) {
    String retValue="";

    try {
      retValue = getContext(conn, vars, "Transactional$Range", window);
    } catch (IllegalArgumentException ignored) {}

    if (retValue.equals("")) return "1";
    return retValue;
  }

  public static String getContext(ConnectionProvider conn, VariablesSecureApp vars, String context, String window) {
    if (context==null || context.equals("")) throw new IllegalArgumentException("getContext - require context");
    String retValue="";

    if (!context.startsWith("#") && !context.startsWith("$")) {
      retValue = getPreference(vars, context, window);
      if (!window.equals("") && retValue.equals("")) retValue = vars.getSessionValue(window + "|" + context);
      if (retValue.equals("")) retValue = vars.getSessionValue("#" + context);
      if (retValue.equals("")) retValue = vars.getSessionValue("$" + context);
    } else {
      try {
        if (context.equalsIgnoreCase("#Date")) return DateTimeData.today(conn);
      } catch (ServletException e) {}
      retValue = vars.getSessionValue(context);
      if (context.equalsIgnoreCase("#User_Org") || context.equalsIgnoreCase("#User_Client")) {
        if (retValue!="0" && !retValue.startsWith("0,") && retValue.indexOf(",0")==-1) {
          retValue = "0" + (retValue.equals("")?"":",") + retValue;
        }
        log4j.debug("getContext(" + context + "):.. " + retValue);
      }
    }

    return retValue;
  }

  public static String getDefault(ConnectionProvider conn, VariablesSecureApp vars, String columnname, String context, String window, String defaultValue) {
    if (columnname == null || columnname.equals("")) return "";
    String defStr = getPreference(vars, columnname, window);
    if (!defStr.equals("")) return defStr;
    StringTokenizer st = new StringTokenizer(context, ",;", false);
    
    while (st.hasMoreTokens()) {
      String token = st.nextToken().trim();
      if (token.indexOf("@")==-1) defStr = token;
      else defStr = parseContext(conn, vars, token, window);
      if (!defStr.equals("")) return defStr;
    }
    if (defStr.equals("")) defStr = vars.getSessionValue("#" + columnname);
    if (defStr.equals("")) defStr = vars.getSessionValue("$" + columnname);
    if (defStr.equals("") && defaultValue!=null) defStr=defaultValue;
    log4j.debug("getDefault(" + columnname + "): " + defStr);
    return defStr;
  }

  public static String parseContext(ConnectionProvider conn, VariablesSecureApp vars, String context, String window) {
    if (context==null || context.equals("")) return "";
    StringBuffer strOut = new StringBuffer();
    String value = new String(context);
    String token, defStr;
    int i = value.indexOf("@");
    while (i!=-1) {
      strOut.append(value.substring(0,i));
      value = value.substring(i+1);
      int j=value.indexOf("@");
      if (j==-1) {
        strOut.append(value);
        return strOut.toString();
      }
      token = value.substring(0, j);
      defStr=getContext(conn, vars, token, window);
      if (defStr.equals("")) return "";
      strOut.append(defStr);
      value=value.substring(j+1);
      i=value.indexOf("@");
    }
    return strOut.toString();
  }

  public static String getDocumentNo (ConnectionProvider conn, VariablesSecureApp vars, String WindowNo, String TableName, String C_DocTypeTarget_ID, String C_DocType_ID, boolean onlyDocType, boolean updateNext) {
    if (TableName == null || TableName.length() == 0) throw new IllegalArgumentException("Utility.getDocumentNo - required parameter missing");
    String AD_Client_ID = getContext(conn, vars, "AD_Client_ID", WindowNo);

    String cDocTypeID = (C_DocTypeTarget_ID.equals("")?C_DocType_ID:C_DocTypeTarget_ID);
    if (cDocTypeID.equals("")) return getDocumentNo (conn, AD_Client_ID, TableName, updateNext);

    if (AD_Client_ID.equals("0")) throw new UnsupportedOperationException("Utility.getDocumentNo - Cannot add System records");

    CSResponse cs=null;
    try {
      cs = DocumentNoData.nextDocType(conn, cDocTypeID, AD_Client_ID, (updateNext?"Y":"N"));
    } catch (ServletException e) {}

    if (cs==null || cs.razon==null || cs.razon.equals("")) {
        if (!onlyDocType) return getDocumentNo (conn, AD_Client_ID, TableName, updateNext);
        else return "0";
    } else return cs.razon;
  }

  public static String getDocumentNo (ConnectionProvider conn, String AD_Client_ID, String TableName, boolean updateNext) {
    if (TableName == null || TableName.length() == 0) throw new IllegalArgumentException("Utility.getDocumentNo - required parameter missing");

    CSResponse cs=null;
    try {
      cs = DocumentNoData.nextDoc(conn, "DocumentNo_" + TableName, AD_Client_ID, (updateNext?"Y":"N"));
    } catch (ServletException e) {}


    if (cs==null || cs.razon==null) return "";
    else return cs.razon;
  }

  public static String addSystem (String list) {
    String retValue = "";

    Hashtable<String, String> ht = new Hashtable<String, String>();
    ht.put("0", "0");

    StringTokenizer st = new StringTokenizer(list, ",", false);
    while (st.hasMoreTokens()) ht.put(st.nextToken(), "x");

    Enumeration<String> e = ht.keys();
    while (e.hasMoreElements()) retValue += e.nextElement() + ",";

    retValue = retValue.substring(0, retValue.length()-1);
    return retValue;
  }

  public static boolean hasFormAccess (ConnectionProvider conn, VariablesSecureApp vars, String process) {
    return hasFormAccess (conn, vars, process, "");
  }

  public static boolean hasFormAccess (ConnectionProvider conn, VariablesSecureApp vars, String process, String processName) {
    try {
      if (process.equals("") && processName.equals("")) return true;
      else if (!process.equals("")) {
        if (!WindowAccessData.hasFormAccess(conn, vars.getRole(), process)) return false;
      } else {
        if (!WindowAccessData.hasFormAccessName(conn, vars.getRole(), processName)) return false;
      }
    } catch (ServletException e) {
      return false;
    }
    return true;
  }

  public static boolean hasProcessAccess (ConnectionProvider conn, VariablesSecureApp vars, String process) {
    return hasProcessAccess (conn, vars, process, "");
  }

  public static boolean hasProcessAccess (ConnectionProvider conn, VariablesSecureApp vars, String process, String processName) {
    try {
      if (process.equals("") && processName.equals("")) return true;
      else if (!process.equals("")) {
        if (!WindowAccessData.hasProcessAccess(conn, vars.getRole(), process)) return false;
      } else {
        if (!WindowAccessData.hasProcessAccessName(conn, vars.getRole(), processName)) return false;
      }
    } catch (ServletException e) {
      return false;
    }
    return true;
  }

  public static boolean hasTaskAccess (ConnectionProvider conn, VariablesSecureApp vars, String task) {
    return hasTaskAccess(conn, vars, task, "");
  }

  public static boolean hasTaskAccess (ConnectionProvider conn, VariablesSecureApp vars, String task, String taskName) {
    try {
      if (task.equals("") && taskName.equals("")) return true;
      else if (!task.equals("")) {
        if (!WindowAccessData.hasTaskAccess(conn, vars.getRole(), task)) return false;
      } else if (!WindowAccessData.hasTaskAccessName(conn, vars.getRole(), taskName)) return false;
    } catch (ServletException e) {
      return false;
    }
    return true;
  }

  public static boolean hasWorkflowAccess (ConnectionProvider conn, VariablesSecureApp vars, String workflow) {
    try {
      if (workflow.equals("")) return true;
      else {
        if (!WindowAccessData.hasWorkflowAccess(conn, vars.getRole(), workflow)) return false;
      }
    } catch (ServletException e) {
      return false;
    }
    return true;
  }

  public static boolean hasAccess (ConnectionProvider conn, VariablesSecureApp vars, String TableLevel, String AD_Client_ID, String AD_Org_ID, String window, String tab) {
    String command = vars.getCommand();
    try {
      if (!canViewInsert(conn, vars, TableLevel, window)) return false;
      else if (!WindowAccessData.hasWindowAccess(conn, vars.getRole(), window)) return false;
      else if (WindowAccessData.hasNoTableAccess(conn, vars.getRole(), tab)) return false;
      else if (command.toUpperCase().startsWith("SAVE")) {
        if (!canUpdate (conn, vars, AD_Client_ID, AD_Org_ID, window)) return false;
      } else if (command.toUpperCase().startsWith("DELETE")) {
        if (!canUpdate (conn, vars, AD_Client_ID, AD_Org_ID, window)) return false;
      }
    } catch (ServletException e) {
      return false;
    }
    return true;
  }

  public static boolean canUpdate (ConnectionProvider conn, VariablesSecureApp vars, String AD_Client_ID, String AD_Org_ID, String window) throws ServletException {
    String User_Level = getContext(conn, vars, "#User_Level", window);

    if (User_Level.indexOf("S") != -1) return true;

    boolean retValue = true;
    String whatMissing = "";

    if (AD_Client_ID.equals("0") && AD_Org_ID.equals("0") && User_Level.indexOf("S") == -1) {
      retValue = false;
      whatMissing += "S";
    } else if (!AD_Client_ID.equals("0") && AD_Org_ID.equals("0") && User_Level.indexOf("C") == -1) {
      retValue = false;
      whatMissing += "C";
    } else if (!AD_Client_ID.equals("0") && !AD_Org_ID.equals("0") && User_Level.indexOf("O") == -1) {
      retValue = false;
      whatMissing += "O";
    }

    if (!WindowAccessData.hasWriteAccess(conn, window, vars.getRole())) retValue = false;

    return retValue;
  }

  public static boolean canViewInsert(ConnectionProvider conn, VariablesSecureApp vars, String TableLevel, String window) {
    String User_Level = getContext(conn, vars, "#User_Level", window);

    boolean retValue = true;

    if (TableLevel.equals("4") && User_Level.indexOf("S") == -1) retValue = false;
    else if (TableLevel.equals("1") && User_Level.indexOf("O") == -1) retValue = false;
    else if (TableLevel.equals("3") && (!(User_Level.indexOf("C")!=-1 || User_Level.indexOf("O")!=-1)) ) retValue = false;
    else if (TableLevel.equals("6") && (!(User_Level.indexOf("S")!=-1 || User_Level.indexOf("C")!=-1)) ) retValue = false;

    if (retValue) return retValue;

    return retValue;
  }

  public static String parseTranslation(ConnectionProvider conn, VariablesSecureApp vars, String language, String text) {
    if (text == null || text.length() == 0) return text;

    String inStr = text;
    String token;
    StringBuffer outStr = new StringBuffer();

    int i = inStr.indexOf("@");
    while (i != -1) {
      outStr.append(inStr.substring(0, i));
      inStr = inStr.substring(i+1, inStr.length());

      int j = inStr.indexOf("@");
      if (j < 0) {
        inStr = "@" + inStr;
        break;
      }

      token = inStr.substring(0, j);
      outStr.append(translate(conn, vars, token, language));

      inStr = inStr.substring(j+1, inStr.length());
      i = inStr.indexOf("@");
    }

    outStr.append(inStr);
    return outStr.toString();
  }

  public static String translate(ConnectionProvider conn, VariablesSecureApp vars, String token, String language) {
    String strTranslate = token;
    strTranslate = vars.getSessionValue(token);
    if (!strTranslate.equals("")) return strTranslate;
    strTranslate = messageBD(conn, token, language);
    if (strTranslate.equals("")) return token;
    return strTranslate;
  }

  public static boolean isInFieldProvider(FieldProvider[] data, String fieldName, String key) {
    if (data==null || data.length==0) return false;
    else if (fieldName==null || fieldName.trim().equals("")) return false;
    else if (key==null || key.trim().equals("")) return false;
    String f="";
    for (int i=0;i<data.length;i++) {
      try {
        f = data[i].getField(fieldName);
      } catch (Exception e) {
        log4j.error("Utility.isInFieldProvider - " + e);
        return false;
      }
      if (f!=null && f.equalsIgnoreCase(key)) return true;
    }
    return false;
  }

  public static String getOrderByFromSELECT(String[] SQL, Vector<String> fields) {
    if (SQL==null || SQL.length==0) return "";
    else if (fields==null || fields.size()==0) return "";
    StringBuffer script = new StringBuffer();
    for (int i=0;i<fields.size();i++) {
      String token = fields.elementAt(i);
      token = token.trim();
      boolean isnegative = false;
      if (token.startsWith("-")) {
        token = token.substring(1);
        isnegative = true;
      }
      if (Integer.valueOf(token).intValue()>SQL.length) log4j.error("Field not found in select - at position: " + token);
      if (!script.toString().equals("")) script.append(", ");
      String strAux = SQL[Integer.valueOf(token).intValue() - 1];
      strAux = strAux.toUpperCase().trim();
      int pos = strAux.indexOf(" AS ");
      if (pos!=-1) strAux = strAux.substring(0, pos);
      strAux = strAux.trim();
      script.append(strAux);
      if (isnegative) script.append(" DESC");
    }
    return script.toString();
  }

  public static String getWindowID(ConnectionProvider conn, String strTabID) throws ServletException {
    return UtilityData.getWindowID(conn, strTabID);
  }
/*
  public static String getRegistryKey(String key) {
    RegistryKey aKey = null; 
    RegStringValue regValue = null; 

    try{
      aKey = com.ice.jni.registry.Registry.HKEY_LOCAL_MACHINE.openSubKey("SYSTEM\\CurrentControlSet\\Control\\Session Manager\\Environment"); 
      regValue = (RegStringValue)aKey.getValue("PATH"); 
    } catch(NoSuchValueException e) {
    //Key value does not exist.
    } catch(RegistryException e) {
    //Any other registry API error.
    }
    return regValue.toString();
  }*/

  public static boolean generateFile(String strPath, String strFile, String data) {
    try {
      File fileData = new File(strPath, strFile);
      FileWriter fileWriterData = new FileWriter(fileData);
      PrintWriter printWriterData = new PrintWriter(fileWriterData);
      printWriterData.print(data);
      fileWriterData.close();
    } catch (IOException e) {
      e.printStackTrace();
      log4j.error("Problem of IOExceptio in file: " + strPath + " - " + strFile);
      return false;
    }
    return true;
  }
/*
  public static String sha1Base64(String text) throws ServletException {
    if (text==null || text.trim().equals("")) return "";
    String result = text;
    result = CryptoSHA1BASE64.encriptar(text);
    return result;
  }

  public static String encryptDecrypt(String text, boolean encrypt) throws ServletException {
    if (text==null || text.trim().equals("")) return "";
    String result = text;
    if (encrypt) result = CryptoUtility.encrypt(text);
    else result = CryptoUtility.decrypt(text);
    return result;
  }
*/
  public static boolean isTreeTab(ConnectionProvider conn, String stradTabId) throws ServletException {
    return UtilityData.isTreeTab(conn, stradTabId);
  }

  public static void fillSQLParameters(ConnectionProvider conn, VariablesSecureApp vars, FieldProvider data, ComboTableData cmb, String window, String actual_value) throws ServletException {
    Vector<String> vAux = cmb.getParameters();
    if (vAux!=null && vAux.size()>0) {
      if (log4j.isDebugEnabled()) log4j.debug("Combo Parameters: " + vAux.size());
      for (int i=0;i<vAux.size();i++) {
        String strAux = vAux.elementAt(i);
        try {
          String value = parseParameterValue(conn, vars, data, strAux, window, actual_value);
          if (log4j.isDebugEnabled()) log4j.debug("Combo Parameter: " + strAux + " - Value: " + value);
          cmb.setParameter(strAux, value);
        } catch (Exception ex) {
          throw new ServletException(ex);
        }
      }
    }
  }

  public static void fillTableSQLParameters(ConnectionProvider conn, VariablesSecureApp vars, FieldProvider data, TableSQLData cmb, String window) throws ServletException {
    Vector<String> vAux = cmb.getParameters();
    if (vAux!=null && vAux.size()>0) {
      if (log4j.isDebugEnabled()) log4j.debug("Combo Parameters: " + vAux.size());
      for (int i=0;i<vAux.size();i++) {
        String strAux = vAux.elementAt(i);
        try {
          String value = parseParameterValue(conn, vars, data, strAux, window, "");
          if (log4j.isDebugEnabled()) log4j.debug("Combo Parameter: " + strAux + " - Value: " + value);
          cmb.setParameter(strAux, value);
        } catch (Exception ex) {
          throw new ServletException(ex);
        }
      }
    }
  }

  public static String parseParameterValue(ConnectionProvider conn, VariablesSecureApp vars, FieldProvider data, String name, String window, String actual_value) throws Exception {
    String strAux = null;
    if (name.equalsIgnoreCase("@ACTUAL_VALUE@")) return actual_value;
    if (data!=null) strAux = data.getField(name);
    if (strAux==null) {
      strAux = vars.getStringParameter("inp" + Sqlc.TransformaNombreColumna(name));
      if (log4j.isDebugEnabled()) log4j.debug("parseParameterValues - getStringParameter(inp" + Sqlc.TransformaNombreColumna(name) + "): " + strAux);
      if (strAux==null || strAux.equals("")) strAux = getContext(conn, vars, name, window);
    }
    return strAux;
  }

  public static OBError getProcessInstanceMessage(ConnectionProvider conn, VariablesSecureApp vars, PInstanceProcessData[] pinstanceData) throws ServletException {
    OBError myMessage = new OBError();
    if (pinstanceData!=null && pinstanceData.length>0) {
      String message = "";
      String title = "Error";
      String type = "Error";
      if (!pinstanceData[0].errormsg.equals("")) {
        message = pinstanceData[0].errormsg;
      } else if (!pinstanceData[0].pMsg.equals("")) {
        message = pinstanceData[0].pMsg;
      }

      if (pinstanceData[0].result.equals("1")) {
        type = "Success";
        title = Utility.messageBD(conn, "Success", vars.getLanguage());
      } else if (pinstanceData[0].result.equals("0")) {
        type = "Error";
        title = Utility.messageBD(conn, "Error", vars.getLanguage());
      } else {
        type = "Warning";
        title = Utility.messageBD(conn, "Warning", vars.getLanguage());
      }

      int errorPos = message.indexOf("@ERROR=");
      if (errorPos!=-1) {
        myMessage = Utility.translateError(conn, vars, vars.getLanguage(), "@CODE=@" + message.substring(errorPos+7));
        if (log4j.isDebugEnabled()) log4j.debug("Error Message returned: " + myMessage.getMessage());
        if (message.substring(errorPos+7).equals(myMessage.getMessage())) {
          myMessage.setMessage(parseTranslation(conn, vars, vars.getLanguage(), myMessage.getMessage()));
        }
        if (errorPos > 0) message = message.substring(0, errorPos);
        else message = "";
      }
      if (!message.equals("") && message.indexOf("@")!=-1) message = Utility.parseTranslation(conn, vars, vars.getLanguage(), message);
      myMessage.setType(type);
      myMessage.setTitle(title);
      myMessage.setMessage(message + ((!message.equals("") && errorPos!=-1)?" <br> ":"") + myMessage.getMessage());
    }
    return myMessage;
  }

  public static OBError translateError(ConnectionProvider conn, VariablesSecureApp vars, String strLanguage, String message) {
    OBError myError = new OBError();
    myError.setType("Error");
    myError.setMessage(message);
    if (message!=null && !message.equals("")) {
      String code = "";
      if (log4j.isDebugEnabled()) log4j.debug("translateError - message: " + message);
      if (message.startsWith("@CODE=@")) message = message.substring(7);
      else if (message.startsWith("@CODE=")) {
        message = message.substring(6);
        int pos = message.indexOf("@");
        if (pos==-1) {
          code = message;
          message = "";
        } else {
          code = message.substring(0, pos);
          message = message.substring(pos+1);
        }
      }
      myError.setMessage(message);
      if (log4j.isDebugEnabled()) log4j.debug("translateError - code: " + code + " - message: " + message);

      //BEGIN Checking if is a pool problem
      if (code!=null && code.equals("NoConnectionAvailable")) {
        myError.setType("Error");
        myError.setTitle("Critical Error");
        myError.setConnectionAvailable(false);
        myError.setMessage("No database connection available");
        return myError;
      }
      //END Checking if is a pool problem

      //BEGIN Parsing message text
      if (message!=null && !message.equals("")) {
        String rdbms = conn.getRDBMS();
        ErrorTextParser myParser = null;
        try {
          Class<?> c = Class.forName("org.openbravo.erpCommon.utility.ErrorTextParser" + rdbms.toUpperCase());
          myParser = (ErrorTextParser) c.newInstance();
        } catch (ClassNotFoundException ex) {
          log4j.warn("Couldn´t find class: org.openbravo.erpCommon.utility.ErrorTextParser" + rdbms.toUpperCase());
          myParser = null;
        } catch (Exception ex1) {
          log4j.warn("Couldn´t initialize class: org.openbravo.erpCommon.utility.ErrorTextParser" + rdbms.toUpperCase());
          myParser = null;
        }
        if (myParser!=null) {
          myParser.setConnection(conn);
          myParser.setLanguage(strLanguage);
          myParser.setMessage(message);
          myParser.setVars(vars);
          try {
            OBError myErrorAux = myParser.parse();
            if (myErrorAux!=null && !myErrorAux.getMessage().equals("") && (code==null || code.equals("") || code.equals("0") || !myErrorAux.getMessage().equalsIgnoreCase(message))) return myErrorAux;
          } catch (Exception ex) {
            log4j.error("Error while parsing text: " + ex);
          }
        }
      } else myError.setMessage(code);
      //END Parsing message text

      //BEGIN Looking for error code in AD_Message
      if (code!=null && !code.equals("")) {
        FieldProvider fldMessage = locateMessage(conn, code, strLanguage);
        if (fldMessage!=null) {
          myError.setType((fldMessage.getField("msgtype").equals("E")?"Error":(fldMessage.getField("msgtype").equals("I")?"Info":(fldMessage.getField("msgtype").equals("S")?"Success":"Warning"))));
          myError.setMessage(fldMessage.getField("msgtext"));
          return myError;
        }
      }
      //END Looking for error code in AD_Message
    }
    return myError;
  }

  public static FieldProvider locateMessage(ConnectionProvider conn, String strCode, String strLanguage) {
    FieldProvider[] fldMessage = null;

    try{
      if (log4j.isDebugEnabled()) log4j.debug("Utility.messageBD - Message Code: " + strCode);
      fldMessage = MessageBDData.messageInfo(conn, strLanguage, strCode);
    } catch(Exception ignore){}
    if (fldMessage!=null && fldMessage.length>0) return fldMessage[0];
    else return null;
  }

  public String getServletInfo() {
    return "This servlet add some functions";
  }
}
