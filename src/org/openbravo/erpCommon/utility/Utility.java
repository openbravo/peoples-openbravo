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
 * All portions are Copyright (C) 2001-2008 Openbravo SL 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
*/
package org.openbravo.erpCommon.utility;

import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.reference.*;
import org.openbravo.data.FieldProvider;
import org.openbravo.base.secureApp.OrgTree;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.data.Sqlc;
import org.openbravo.erpCommon.utility.DateTimeData;
import org.openbravo.utils.Replace;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Vector;
import java.util.Calendar;
import java.text.ParseException;
import java.text.DateFormat;
import java.sql.Connection;
import javax.servlet.ServletException;
import java.io.*;
import org.apache.log4j.Logger ;

/**
 * @author Fernando Iriazabal
 *
 * Utility class
 */
public class Utility {
  static Logger log4j = Logger.getLogger(Utility.class);

  /**
   * Checks if the references is a decimal number type.
   * 
   * @param reference: String with the reference.
   * @return True if is a decimal or false if not.
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

  /**
   * Checks if the references is an integer number type.
   * 
   * @param reference: String with the reference.
   * @return True if is an integer or false if not.
   */
  public static boolean isIntegerNumber (String reference) {
    if (reference==null || reference.equals("")) return false;
    switch (Integer.valueOf(reference).intValue()) {
    case 11:
    case 13:
    case 25: return true;
    }
    return false;
  }

  /**
   * Checks if the references is a datetime type.
   * 
   * @param reference: String with the reference.
   * @return True if is a datetime or false if not.
   */
  public static boolean isDateTime (String reference) {
    if (reference==null || reference.equals("")) return false;
    switch (Integer.valueOf(reference).intValue()) {
    case 15:
    case 16:
    case 24: return true;
    }
    return false;
  }
  
  /**
   * Returns an String with the date in the specified format
   * 
   * @param date: Date to be formatted.
   * @param pattern: Format expected for the output.
   * @return String formatted.
   */  
  public static String formatDate( Date date, String pattern )
  {
    SimpleDateFormat dateFormatter = new SimpleDateFormat( pattern );
    return dateFormatter.format( date );
  }  

  /**
   * Checks if the record has attachments associated.
   * 
   * @param conn: Handler for the database connection.
   * @param vars: Handler for the session info.
   * @param strTab: String with the tab id.
   * @param recordId: String with the record id.
   * @return True if the record has attachments or false if not.
   * @throws ServletException
   */
  public static boolean hasTabAttachments(ConnectionProvider conn, VariablesSecureApp vars, String strTab, String recordId) throws ServletException {
    return UtilityData.hasTabAttachments(conn, Utility.getContext(conn, vars, "#User_Client", ""), Utility.getContext(conn, vars, "#User_Org", ""), strTab, recordId);
  } 

  /**
   * Translate the given code into some message from the application dictionary.
   * 
   * @param conn: Handler for the database connection.
   * @param strCode: String with the code to search.
   * @param strLanguage: String with the translation language.
   * @return String with the translated message.
   */
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

  /**
   * Gets the value of the given preference.
   * 
   * @param vars: Handler for the session info.
   * @param context: String with the preference.
   * @param window: String with the window id.
   * @return String with the value.
   */
  public static String getPreference(VariablesSecureApp vars, String context, String window) {
    if (context==null || context.equals("")) throw new IllegalArgumentException("getPreference - require context");
    String retValue="";

    retValue = vars.getSessionValue("P|" + window + "|" + context);
    if (retValue.equals("")) retValue = vars.getSessionValue("P|" + context);

    return (retValue);
  }

  /**
   * Gets the transactional range defined.
   * 
   * @param conn: Handler for the database connection.
   * @param vars: Handler for the session info.
   * @param window: String with the window id.
   * @return String with the value.
   */
  public static String getTransactionalDate(ConnectionProvider conn, VariablesSecureApp vars, String window) {
    String retValue="";

    try {
      retValue = getContext(conn, vars, "Transactional$Range", window);
    } catch (IllegalArgumentException ignored) {}

    if (retValue.equals("")) return "1";
    return retValue;
  }

  /**
   * Gets a value from the context. For client 0 is always added (used for references), to check if it must by added
   * or not use the getContext with accesslevel method.
   * 
   * @param conn: Handler for the database connection.
   * @param vars: Handler for the session info.
   * @param context: String with the parameter to search.
   * @param window: String with the window id.
   * @return String with the value.
   */
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
      
      String userLevel = vars.getSessionValue("#User_Level");
      
      if (context.equalsIgnoreCase("#AccessibleOrgTree")) {
        if (!retValue.equals("'0'") && !retValue.startsWith("'0',") && retValue.indexOf(",'0'")==-1) {// add *
          retValue = "'0'" + (retValue.equals("")?"":",") + retValue;
        }
      }
          
      if (context.equalsIgnoreCase("#User_Org")) {
        if (userLevel.contains("S")||userLevel.equals(" C ")) return "'0'"; //force org *
        
        if (userLevel.equals("  O")) { // remove *
          if (retValue.equals("'0'")) 
            retValue="";
          else if (retValue.startsWith("'0',"))
            retValue = retValue.substring(2);
          else
            retValue = retValue.replace(",'0'","");
        } else { // add *
          if (!retValue.equals("0") && !retValue.startsWith("'0',") && retValue.indexOf(",'0'")==-1) {// Any: current list and *
            retValue = "'0'" + (retValue.equals("")?"":",") + retValue;
          }
        }
      }
      
      if (context.equalsIgnoreCase("#User_Client")) {
        if (retValue!="'0'" && !retValue.startsWith("'0',") && retValue.indexOf(",'0'")==-1) {
          retValue = "'0'" + (retValue.equals("")?"":",") + retValue;
        }
      }
    }

    return retValue;
  }
  
  /**
   * Gets a value from the context.
   * Access level values:
   * 1  Organization
   * 3  Client/Organization
   * 4  System only
   * 6  System/Client
   * 7  All
   * @param conn: Handler for the database connection.
   * @param vars: Handler for the session info.
   * @param context: String with the parameter to search.
   * @param window: String with the window id.
   * @param accessLevel
   * @return String with the value.
   */
  public static String getContext(ConnectionProvider conn, VariablesSecureApp vars, String context, String window, int accessLevel) {
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
      
      String userLevel = vars.getSessionValue("#User_Level");
      if (context.equalsIgnoreCase("#AccessibleOrgTree")) {
        if (!retValue.equals("0") && !retValue.startsWith("'0',") && retValue.indexOf(",'0'")==-1) {// add *
          retValue = "'0'" + (retValue.equals("")?"":",") + retValue;
        }
      }
      if (context.equalsIgnoreCase("#User_Org") ) {
        if (accessLevel==4||accessLevel==6) return "'0'"; //force to be org *
      
        if ((accessLevel==1) || (userLevel.equals("  O"))) { //No *: remove 0 from current list
          if (retValue.equals("'0'")) 
            retValue="";
          else if (retValue.startsWith("'0',"))
            retValue = retValue.substring(4);
          else
            retValue = retValue.replace(",'0'","");
        } else {// Any: add 0 to current list 
          if (!retValue.equals("'0'") && !retValue.startsWith("'0',") && retValue.indexOf(",'0'")==-1) {// Any: current list and *
            retValue = "'0'" + (retValue.equals("")?"":",") + retValue;
          } 
        }
      }
      
      if (context.equalsIgnoreCase("#User_Client")) {
        if (accessLevel==4) {
          if (userLevel.contains("S")) return "'0'"; //force client 0
          else return "";
        }
        
        if ((accessLevel==1)||(accessLevel==3))  { //No 0
          if (userLevel.contains("S")) return "";
          if (retValue.equals("'0'")) 
            retValue="";
          else if (retValue.startsWith("'0',"))
            retValue = retValue.substring(2);
          else
            retValue = retValue.replace(",'0'","");
        } else if (userLevel.contains("S")){ //Any: add 0
          if (retValue!="'0'" && !retValue.startsWith("'0',") && retValue.indexOf(",'0'")==-1) {
            retValue = "'0'" + (retValue.equals("")?"":",") + retValue;
          }
        }
      }
    }
    log4j.debug("getContext(" + context + "):.. " + retValue);
    return retValue;
   }

  /**
   * Returns the list of referenceables organizations from the current one. 
   * This includes all its ancestors and descendants. 
   * @param vars
   * @param currentOrg
   * @return
   */
  public static String getReferenceableOrg(VariablesSecureApp vars, String currentOrg) {
    OrgTree tree = (OrgTree) vars.getSessionObject("#CompleteOrgTree");
    return tree.getLogicPath(currentOrg).toString();
  }
  
  /**
   * Returns the list of referenceables organizations from the current one. 
   * This includes all its ancestors and descendants. 
   * This method takes into account accessLevel and user level: useful to calculate org list for child tabs
   * @param conn
   * @param vars
   * @param currentOrg
   * @param window
   * @param accessLevel
   * @return
   */
  public static String getReferenceableOrg(ConnectionProvider conn, VariablesSecureApp vars, String currentOrg,  String window, int accessLevel) {
    if (accessLevel==4||accessLevel==6) return "'0'"; //force to be org *
    Vector<String> vComplete = getStringVector(getReferenceableOrg(vars, currentOrg));
    Vector<String> vAccessible = getStringVector(getContext(conn, vars, "#User_Org", window, accessLevel));
    return getVectorToString(getIntersectionVector(vComplete, vAccessible));
  }
  
  /**
   * Returns the organization list for selectors, two cases are possible: <br>
   *   <li>Organization is empty (null or ""): accessible list of organizations will be returned. 
   *   This case is used in calls from filters to selectors.
   *   <li>Organization is not empty: referenceable from current organization list of organizations will be returned.
   *   This is the way it is called from wad windows. 
   *   
   * @param conn: Handler for the database connection
   * @param vars
   * @param currentOrg
   * @return
   */
  public static String  getSelectorOrgs(ConnectionProvider conn, VariablesSecureApp vars, String currentOrg) {
    if ((currentOrg == null) || (currentOrg.equals("")))
       return getContext(conn, vars, "#AccessibleOrgTree", "Selectors");
    else
      return getReferenceableOrg(vars, currentOrg);
  }
  /**
   * Gets a default value.
   * 
   * @param conn: Handler for the database connection.
   * @param vars: Handler for the session info.
   * @param columnname: String with the column name.
   * @param context: String with the parameter.
   * @param window: String with the window id.
   * @param defaultValue: String with the default value.
   * @return String with the value.
   */
  public static String getDefault(ConnectionProvider conn, VariablesSecureApp vars, String columnname, String context, String window, String defaultValue) {
    if (columnname == null || columnname.equals("")) return "";
    String defStr = getPreference(vars, columnname, window);
    if (!defStr.equals("")) return defStr;

    if (context.indexOf("@")==-1) //Tokenize just when contains @ 
      defStr = context;
    else {
      StringTokenizer st = new StringTokenizer(context, ",;", false);
      while (st.hasMoreTokens()) {
        String token = st.nextToken().trim();
        if (token.indexOf("@")==-1) defStr = token;
        else defStr = parseContext(conn, vars, token, window);
        if (!defStr.equals("")) return defStr;
      }
    }
    if (defStr.equals("")) defStr = vars.getSessionValue("#" + columnname);
    if (defStr.equals("")) defStr = vars.getSessionValue("$" + columnname);
    if (defStr.equals("") && defaultValue!=null) defStr=defaultValue;
    log4j.debug("getDefault(" + columnname + "): " + defStr);
    return defStr;
  }

  /**
   * Returns a Vector<String> composed by the comma separated elements in String s 
   * @param s
   * @return
   */
  public static Vector<String> getStringVector(String s){
    Vector<String> v = new Vector<String>();
    StringTokenizer st = new StringTokenizer(s, ",", false);
    while (st.hasMoreTokens()) {
      String token = st.nextToken().trim();
      if (!v.contains(token)) v.add(token);
    }
    return v;    
  }
  
  /**
   * Returns a Vector<String> with the elements that appear in both v1 and v2 Vectors
   * @param v1
   * @param v2
   * @return
   */
  public static Vector<String> getIntersectionVector(Vector<String> v1,  Vector<String> v2){
    Vector<String> v = new Vector<String>();
    for (int i=0; i<v1.size(); i++){
      if (v2.contains(v1.elementAt(i)) && !v.contains(v1.elementAt(i))) v.add(v1.elementAt(i));
    }
    return v;
  }
  
  /**
   * Returns the elements in Vector v as an String separating with commas the elements
   * @param v
   * @return
   */
  public static String getVectorToString(Vector<String> v){
    StringBuffer s = new StringBuffer();
    for (int i=0; i<v.size(); i++) {
      if (s.length()!=0) s.append(", ");
      s.append(v.elementAt(i));
    }
    return s.toString();
  }
  
  /**
   * Parse the given string searching the @ elements to translate with the correct values.
   * 
   * @param conn: Handler for the database connection.
   * @param vars: Handler for the session info.
   * @param context: String to parse.
   * @param window: String with the window id.
   * @return String parsed.
   */
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

  /**
   * Gets the document number from the database.
   * 
   * @param conn: Handler for the database connection.
   * @param vars: Handler for the session info.
   * @param WindowNo: Window id.
   * @param TableName: Table name.
   * @param C_DocTypeTarget_ID: Id of the doctype target.
   * @param C_DocType_ID: id of the doctype.
   * @param onlyDocType: Search only for doctype.
   * @param updateNext: Save the new sequence in database.
   * @return String with the new document number.
   */
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

  /**
   * Gets the document number from database.
   * 
   * @param conn: Handler for the database connection.
   * @param AD_Client_ID: String with the client id.
   * @param TableName: Table name.
   * @param updateNext: Save the new sequence in database.
   * @return String with the new document number.
   */
  public static String getDocumentNo (ConnectionProvider conn, String AD_Client_ID, String TableName, boolean updateNext) {
    if (TableName == null || TableName.length() == 0) throw new IllegalArgumentException("Utility.getDocumentNo - required parameter missing");

    CSResponse cs=null;
    try {
      cs = DocumentNoData.nextDoc(conn, "DocumentNo_" + TableName, AD_Client_ID, (updateNext?"Y":"N"));
    } catch (ServletException e) {}


    if (cs==null || cs.razon==null) return "";
    else return cs.razon;
  }

  /**
   * Gets the document number from database.
   * 
   * @param conn: Handler for the database connection.
   * @param AD_Client_ID: String with the client id.
   * @param TableName: Table name.
   * @param updateNext: Save the new sequence in database.
   * @return String with the new document number.
   */
  public static String getDocumentNoConnection (Connection conn, ConnectionProvider con, String AD_Client_ID, String TableName, boolean updateNext) {
    if (TableName == null || TableName.length() == 0) throw new IllegalArgumentException("Utility.getDocumentNo - required parameter missing");

    CSResponse cs=null;
    try {
      cs = DocumentNoData.nextDocConnection(conn, con, "DocumentNo_" + TableName, AD_Client_ID, (updateNext?"Y":"N"));
    } catch (ServletException e) {}


    if (cs==null || cs.razon==null) return "";
    else return cs.razon;
  }
  /**
   * Adds the system element to the given list.
   * 
   * @param list: String with the list.
   * @return String with the modified list.
   */
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


  /**
   * Checks if the user can make modifications in the window.
   * 
   * @param conn: Handler for the database connection.
   * @param vars: Handler for the session info.
   * @param AD_Client_ID: Id of the client.
   * @param AD_Org_ID: Id of the organization.
   * @param window: Window id.
   * @return True if has permission, false if not.
   * @throws ServletException
   */
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


  /**
   * Parse the text searching @ parameters to translate.
   * 
   * @param conn: Handler for the database connection.
   * @param vars: Handler for the session info.
   * @param language: String with the language to translate.
   * @param text: String with the text to translate.
   * @return String translated.
   */
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

  /**
   * For each token found in the parseTranslation method, this method is called
   * to find the correct translation.
   * 
   * @param conn: Handler for the database connection.
   * @param vars: Handler for the session info.
   * @param token: String with the token to translate.
   * @param language: String with the language to translate.
   * @return String with the token translated.
   */
  public static String translate(ConnectionProvider conn, VariablesSecureApp vars, String token, String language) {
    String strTranslate = token;
    strTranslate = vars.getSessionValue(token);
    if (!strTranslate.equals("")) return strTranslate;
    strTranslate = messageBD(conn, token, language);
    if (strTranslate.equals("")) return token;
    return strTranslate;
  }

  /**
   * Checks if the value exists in the given array of FieldProviders.
   * 
   * @param data: Array of FieldProviders.
   * @param fieldName: Name of the field to search.
   * @param key: The value to search.
   * @return True if exists or false if not.
   */
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

  /**
   * Deprecated. Used in the old order by window.
   * @deprecated
   * @param SQL
   * @param fields
   * @return
   */
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

  /**
   * Gets the window id for a tab.
   * 
   * @param conn: Handler for the database connection.
   * @param strTabID: Id of the tab.
   * @return String with the id of the window.
   * @throws ServletException
   */
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

  /**
   * Saves the content into a fisical file.
   * 
   * @param strPath: path for the file.
   * @param strFile: name of the file.
   * @param data: content of the file.
   * @return true if everything is ok or false if not.
   */
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
  /**
   * Checks if the tab is declared as a tree tab.
   * 
   * @param conn: Handler for the database connection.
   * @param stradTabId: Id of the tab.
   * @return True if is a tree tab or false if isn't.
   * @throws ServletException
   */
  public static boolean isTreeTab(ConnectionProvider conn, String stradTabId) throws ServletException {
    return UtilityData.isTreeTab(conn, stradTabId);
  }

  /**
   * Fill the parameters of the sql with the session values or FieldProvider values.
   * Used in the combo fields.
   * 
   * @param conn: Handler for the database connection.
   * @param vars: Handler for the session info.
   * @param data: FieldProvider with the columns values.
   * @param cmb: ComboTableData object.
   * @param window: Window id.
   * @param actual_value: actual value for the combo.
   * @throws ServletException
   */
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

  /**
   * Fill the parameters of the sql with the session values or FieldProvider values.
   * Used in the combo relation's grids.
   * 
   * @param conn: Handler for the database connection.
   * @param vars: Handler for the session info.
   * @param data: FieldProvider with the columns values.
   * @param cmb: TableSQLData object.
   * @param window: Window id.
   * @throws ServletException
   */
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

  /**
   * Auxiliar method, used by fillSQLParameters and fillTableSQLParameters to get the
   * values for each parameter.
   * 
   * @param conn: Handler for the database connection.
   * @param vars: Handler for the session info.
   * @param data: FieldProvider with the columns values.
   * @param name: Name of the parameter.
   * @param window: Window id.
   * @param actual_value: Actual value.
   * @return String with the parsed parameter.
   * @throws Exception
   */
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

  /**
   * Gets the Message for the instance of the processes.
   * 
   * @param conn: Handler for the database connection.
   * @param vars: Handler for the session info.
   * @param pinstanceData: Array with the instance information.
   * @return Object with the message.
   * @throws ServletException
   */
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

  /**
   * Translate the message, searching the @ parameters, and making use of the
   * ErrorTextParser class to get the appropiated message.
   * 
   * @param conn: Handler for the database connection.
   * @param vars: Handler for the session info.
   * @param strLanguage: Language to translate.
   * @param message: Strin with the message to translate.
   * @return Object with the message.
   */
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

  /**
   * Search a message in the database.
   * 
   * @param conn: Handler for the database connection.
   * @param strCode: Message to search.
   * @param strLanguage: Language to translate.
   * @return FieldProvider with the message info.
   */
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

  
  /**
   * Checks if an element is in a list. List is an string like "(e1, e2, e3,...)" where en are elements. 
   * It is inteeded to be used for checking user client and organizations.
   * 
   * @param strList: List to check in
   * @param strClient: Element to check in the list
   * @return true in case the element is in the list
   */
  public static boolean isElementInList(String strList, String strElement){
    strList = strList.replace("(", "").replace(")", "");
    StringTokenizer st = new StringTokenizer(strList, ",", false);
    strElement = strElement.replaceAll("'", "");
    
    while (st.hasMoreTokens()) {
      String token = st.nextToken().trim().replaceAll("'", "");
      if (token.equals(strElement)) return true;
    }
    return false;
  }
 
  /**
   * Returns a JavaScript function to be used on selectors
   * Depending on what element you want to focus, you pass the id
   * @param id the html tag id to focus on
   * @return a String JavaScript function
   */
  public static String focusFieldJS(String id) {
    String r = "\n function focusOnField() { \n" + 
               " setWindowElementFocus('" + id + "', 'id'); \n" +
               " return true; \n" +
               "} \n";
    return r;
  }

  /**
   * Write the output to a file
   * @param fileLocation: the file where you are going to write
   * @param outputstream: the data source
   * @return nothing. It creates a file in the file location writing the content of the outputstream
   */  
  public static void dumpFile(String fileLocation, OutputStream outputstream)
  {
    byte dataPart[] = new byte[4096];
    try
    {
      BufferedInputStream bufferedinputstream = new BufferedInputStream( new FileInputStream( new File( fileLocation ) ) );
      int i;
      while( (i = bufferedinputstream.read(dataPart, 0, 4096) ) != -1) 
        outputstream.write(dataPart, 0, i);
      bufferedinputstream.close();
    }
    catch(Exception exception) { }
  }  
  
  /**
   * Returns a string list comma separated as SQL strings.
   * @param list
   * @return
   */
  public static String stringList(String list) {
	  String ret="";
	  boolean hasBrackets = list.startsWith("(") && list.endsWith(")");
	  if (hasBrackets) list = list.substring(1, list.length()-1);
	  StringTokenizer st = new StringTokenizer(list, ",", false);
	  while (st.hasMoreTokens()) {
	    String token = st.nextToken().trim();
	    if (!ret.equals("")) ret += ", ";
	    if (!(token.startsWith("'") && token.endsWith("'"))) token = "'"+token+"'";
	    ret += token;
	  }
	  if (hasBrackets) ret = "("+ret+")";
	  return ret;
  }
  
  /**
   * Determines if a string of characters is an Openbravo UUID (Universal Unique Identifier),
   * i.e., if it is a 32 length hexadecimal string.
   *  
   * @param CharacterString: A string of characters.
   * @return Returns true if this string of characters is an UUID.
   */
  public static boolean isUUIDString(String CharacterString) {
    if(CharacterString.length() == 32) {
      for (int i=0;i<CharacterString.length();i++) {
        if (!isHexStringChar(CharacterString.charAt(i))) return false;
      } return true;
    }
    return false;
  }

  /**
   * Returns true if the input argument character is
   * A-F, a-f or 0-9.
   * 
   * @param c: A single character.
   * @return Returns true if this character is hexadecimal.
   */
  public static final boolean isHexStringChar(char c) {
    return (("0123456789abcdefABCDEF".indexOf(c)) >= 0);
  }
  
  /**
   * Returns the ID of the base currency of the given client
   * 
   * @param strClientId: ID of client.
   * @return Returns String strBaseCurrencyId with the ID of the base currency.
   * @throws ServletException 
   */
  public static String stringBaseCurrencyId(ConnectionProvider conn, String strClientId) throws ServletException {
    String strBaseCurrencyId = UtilityData.getBaseCurrencyId(conn, strClientId);
    return strBaseCurrencyId;
  }
  
  /**
   * Returns the ISO code plus the symbol of the given currency
   * in the form (ISO-SYM), e.g., (USD-$)
   * 
   * @param strCurrencyID: ID of the currency.
   * @return Returns String strISOSymbol with the ISO code plus the symbol of the currency.
   * @throws ServletException 
   */
  public static String stringISOSymbol(ConnectionProvider conn, String strCurrencyID) throws ServletException {
    String strISOSymbol = UtilityData.getISOSymbol(conn, strCurrencyID);
    return strISOSymbol;
  }
  
  @Deprecated
  public static boolean hasFormAccess (ConnectionProvider conn, VariablesSecureApp vars, String process) {
  	return hasFormAccess (conn, vars, process, "");
  }
	   
  @Deprecated
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
	   
  @Deprecated
  public static boolean hasProcessAccess (ConnectionProvider conn, VariablesSecureApp vars, String process) {
  	return hasProcessAccess (conn, vars, process, "");
  }
	   
  @Deprecated
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
	   
  @Deprecated
  public static boolean hasTaskAccess (ConnectionProvider conn, VariablesSecureApp vars, String task) {
  	return hasTaskAccess(conn, vars, task, "");
	}
	   
  @Deprecated
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
	   
  @Deprecated
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
	   
  @Deprecated
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
	  
  @Deprecated
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
  
  @ Deprecated // in 2.50
  public static boolean hasAttachments(ConnectionProvider conn, String userClient, String userOrg, String tableId, String recordId) throws ServletException {
    if (tableId.equals("") || recordId.equals("")) return false;
    else return UtilityData.select(conn, userClient, userOrg, tableId, recordId);
  }
  
  /**
   * Determines the labor days between two dates
   * 
   * @param strDate1: Date 1.
   * @param strDate2: Date 2.
   * @param DateFormatter: Format of the dates.
   * @return strLaborDays as the number of days between strDate1 and strDate2.
   */
  public static String calculateLaborDays(String strDate1, String strDate2, DateFormat DateFormatter) throws ParseException {
    String strLaborDays = "";
    if (strDate1 != null && strDate1 != "" && strDate2 != null && strDate2 != "") { 
      Integer LaborDays = 0;
      if(Utility.isBiggerDate(strDate1, strDate2, DateFormatter)) {
        do {
          strDate2 = Utility.addDaysToDate(strDate2, "1", DateFormatter); //Adds a day to the Date 2 until it reaches the Date 1
          if (!Utility.isWeekendDay(strDate2, DateFormatter)) LaborDays ++; //If it is not a weekend day, it adds a day to the labor days
        } while (!strDate2.equals(strDate1));
      }
      else {
        do {
          strDate1 = Utility.addDaysToDate(strDate1, "1", DateFormatter); //Adds a day to the Date 1 until it reaches the Date 2
          if (!Utility.isWeekendDay(strDate1, DateFormatter)) LaborDays ++; //If it is not a weekend day, it adds a day to the labor days
        } while (!strDate1.equals(strDate2));
      }
      strLaborDays = LaborDays.toString();
    }
    return strLaborDays;
  }
  
  /**
   * Adds an integer number of days to a given date 
   * 
   * @param strDate: Start date.
   * @param strDays: Number of days to add.
   * @param DateFormatter: Format of the date.
   * @return strFinalDate as the sum of strDate plus strDays.
   */
  public static String addDaysToDate(String strDate, String strDays, DateFormat DateFormatter) throws ParseException {
    String strFinalDate = "";
    if (strDate != null && strDate != "" && strDays != null && strDays != "") {      
      Calendar FinalDate = Calendar.getInstance();
      FinalDate.setTime((Date)DateFormatter.parse(strDate)); //FinalDate equals to strDate
      FinalDate.add(Calendar.DATE, Integer.parseInt(strDays)); //FinalDate equals to strDate plus one day
      strFinalDate = DateFormatter.format(FinalDate.getTime());
    }
    return strFinalDate;
  }
  
  /**
   * Determines the format of the date
   * 
   * @param vars: Global variables.
   * @return DateFormatter as the format of the date.
   */
  public static DateFormat getDateFormatter (VariablesSecureApp vars) {
  String strFormat = vars.getSessionValue("#AD_SqlDateFormat").toString();
  strFormat = strFormat.replace('Y', 'y'); //Java accepts 'yy' for the year
  strFormat = strFormat.replace('D', 'd'); //Java accepts 'dd' for the day of the date
  DateFormat DateFormatter = new SimpleDateFormat(strFormat);
  return DateFormatter;
  }
  
  /**
   * Determines if a day is a day of the weekend, i.e., Saturday or Sunday
   * 
   * @param strDay: Given Date.
   * @param DateFormatter: Format of the date.
   * @return true if the date is a Sunday or a Saturday.
   */
  public static boolean isWeekendDay(String strDay, DateFormat DateFormatter) throws ParseException{   
    Calendar Day = Calendar.getInstance();
    Day.setTime((Date)DateFormatter.parse(strDay));
    int weekday = Day.get(Calendar.DAY_OF_WEEK); //Gets the number of the day of the week: 1-Sunday, 2-Monday, 3-Tuesday, 4-Wednesday, 5-Thursday, 6-Friday, 7-Saturday
    if (weekday == 1 || weekday == 7) return true; //1-Sunday, 7-Saturday
    return false;
  }
  
  /**
   * Determines if a date 1 is bigger than a date 2 
   * 
   * @param strDate1: Date 1.
   * @param strDate2: Date 2.
   * @param DateFormatter: Format of the dates.
   * @return true if strDate1 is bigger than strDate2.
   */
  public static boolean isBiggerDate(String strDate1, String strDate2, DateFormat DateFormatter) throws ParseException{   
    Calendar Date1 = Calendar.getInstance();
    Date1.setTime((Date)DateFormatter.parse(strDate1));
    long MillisDate1 = Date1.getTimeInMillis();
    Calendar Date2 = Calendar.getInstance();    
    Date2.setTime((Date)DateFormatter.parse(strDate2));
    long MillisDate2 = Date2.getTimeInMillis();
    if (MillisDate1 > MillisDate2) return true; //Date 1 is bigger than Date 2
    return false;
  }

}
