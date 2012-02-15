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
 * All portions are Copyright (C) 2012 Openbravo SLU
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.erpCommon.utility;

import java.util.Map;

import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.client.kernel.RequestContext;
import org.openbravo.dal.core.OBContext;
import org.openbravo.data.FieldProvider;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.reference.PInstanceProcessData;
import org.openbravo.service.db.DalConnectionProvider;

public class MessageUtility {
  static Logger log4j = Logger.getLogger(MessageUtility.class);

  /**
   * Translate the given code into some message from the application dictionary. It searches first
   * in AD_Message table and if there are not matchings then in AD_Element table.
   * 
   * It uses DalConnectionProvider to get the ConnectionProvider and the context's language to
   * translate the message.
   * 
   * @see Utility#messageBD(ConnectionProvider, String, String, boolean)
   * 
   * @param strCode
   *          String with the search key to search.
   * @return String with the translated message.
   */
  public static String messageBD(String strCode) {
    String language = OBContext.getOBContext().getLanguage().getLanguage();
    ConnectionProvider conn = new DalConnectionProvider(false);
    return BasicUtility.messageBD(conn, strCode, language);
  }

  /**
   * @see MessageUtility#messageBD(ConnectionProvider, String, String, boolean)
   */
  public static String messageBD(ConnectionProvider conn, String strCode, String strLanguage) {
    return BasicUtility.messageBD(conn, strCode, strLanguage, true);
  }

  /**
   * Translate the given code into some message from the application dictionary.
   * 
   * @param conn
   *          Handler for the database connection.
   * @param strCode
   *          String with the code to search.
   * @param strLanguage
   *          String with the translation language.
   * @param escape
   *          Escape \n and " characters
   * @return String with the translated message.
   */
  public static String messageBD(ConnectionProvider conn, String strCode, String strLanguage,
      boolean escape) {
    return BasicUtility.messageBD(conn, strCode, strLanguage, escape);
  }

  /**
   * 
   * Formats a message String into a String for html presentation. Escapes the &, <, >, " and ®, and
   * replace the \n by <br/>
   * and \r for space.
   * 
   * IMPORTANT! : this method is designed to transform the output of Utility.messageBD method, and
   * this method replaces \n by \\n and \" by &quote. Because of that, the first replacements revert
   * this previous replacements.
   * 
   * @param message
   *          message with java formating
   * @return html format message
   */
  public static String formatMessageBDToHtml(String message) {
    return BasicUtility.formatMessageBDToHtml(message);
  }

  /**
   * Parse the text searching @ parameters to translate.
   * 
   * @param text
   *          String with the text to translate.
   * @return String translated.
   */
  public static String parseTranslation(String text) {
    final VariablesSecureApp vars = RequestContext.get().getVariablesSecureApp();
    final String language = OBContext.getOBContext().getLanguage().getLanguage();
    return parseTranslation(new DalConnectionProvider(false), vars, null, language, text);
  }

  /**
   * Parse the text searching @ parameters to translate.
   * 
   * @param conn
   *          Handler for the database connection.
   * @param vars
   *          Handler for the session info.
   * @param language
   *          String with the language to translate.
   * @param text
   *          String with the text to translate.
   * @return String translated.
   */
  public static String parseTranslation(ConnectionProvider conn, VariablesSecureApp vars,
      String language, String text) {
    return parseTranslation(conn, vars, null, language, text);
  }

  /**
   * Parse the text searching @ parameters to translate. If replaceMap is not null and contains a
   * replacement value for a token then it will be used, otherwise the return value of the translate
   * method will be used for the translation.
   * 
   * @param conn
   *          Handler for the database connection.
   * @param vars
   *          Handler for the session info.
   * @param replaceMap
   *          optional Map containing replacement values for the tokens
   * @param language
   *          String with the language to translate.
   * @param text
   *          String with the text to translate.
   * @return String translated.
   */
  public static String parseTranslation(ConnectionProvider conn, VariablesSecureApp vars,
      Map<String, String> replaceMap, String language, String text) {
    if (text == null || text.length() == 0) {
      return text;
    }

    String inStr = text;
    String token;
    final StringBuffer outStr = new StringBuffer();

    int i = inStr.indexOf("@");
    while (i != -1) {
      outStr.append(inStr.substring(0, i));
      inStr = inStr.substring(i + 1, inStr.length());

      final int j = inStr.indexOf("@");
      if (j < 0) {
        inStr = "@" + inStr;
        break;
      }

      token = inStr.substring(0, j);
      if (replaceMap != null && replaceMap.containsKey(token)) {
        outStr.append(replaceMap.get(token));
      } else {
        outStr.append(translate(conn, vars, token, language));
      }

      inStr = inStr.substring(j + 1, inStr.length());
      i = inStr.indexOf("@");
    }

    outStr.append(inStr);
    return outStr.toString();
  }

  /**
   * For each token found in the parseTranslation method, this method is called to find the correct
   * translation.
   * 
   * @param conn
   *          Handler for the database connection.
   * @param vars
   *          Handler for the session info.
   * @param token
   *          String with the token to translate.
   * @param language
   *          String with the language to translate.
   * @return String with the token translated.
   */
  public static String translate(ConnectionProvider conn, VariablesSecureApp vars, String token,
      String language) {
    String strTranslate = vars.getSessionValue(token);
    if (!strTranslate.equals("")) {
      return strTranslate;
    }
    strTranslate = messageBD(conn, token, language);
    if (strTranslate.equals("")) {
      return token;
    }
    return strTranslate;
  }

  /**
   * Gets the Message for the instance of the processes.
   * 
   * @param conn
   *          Handler for the database connection.
   * @param vars
   *          Handler for the session info.
   * @param pinstanceData
   *          Array with the instance information.
   * @return Object with the message.
   * @throws ServletException
   */
  public static OBError getProcessInstanceMessage(ConnectionProvider conn, VariablesSecureApp vars,
      PInstanceProcessData[] pinstanceData) throws ServletException {
    OBError myMessage = new OBError();
    if (pinstanceData == null || pinstanceData.length == 0) {
      return myMessage;
    }
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
      title = messageBD(conn, "Success", vars.getLanguage());
    } else if (pinstanceData[0].result.equals("0")) {
      type = "Error";
      title = messageBD(conn, "Error", vars.getLanguage());
    } else {
      type = "Warning";
      title = messageBD(conn, "Warning", vars.getLanguage());
    }

    final int errorPos = message.indexOf("@ERROR=");
    if (errorPos != -1) {
      myMessage = translateError(conn, vars, vars.getLanguage(),
          "@CODE=@" + message.substring(errorPos + 7));
      log4j.debug("Error Message returned: " + myMessage.getMessage());
      if (message.substring(errorPos + 7).equals(myMessage.getMessage())) {
        myMessage.setMessage(parseTranslation(conn, vars, vars.getLanguage(),
            myMessage.getMessage()));
      }
      if (errorPos > 0) {
        message = message.substring(0, errorPos);
      } else {
        message = "";
      }
    }
    if (!message.equals("") && message.indexOf("@") != -1) {
      message = parseTranslation(conn, vars, vars.getLanguage(), message);
    }
    myMessage.setType(type);
    myMessage.setTitle(title);
    myMessage.setMessage(message + ((!message.equals("") && errorPos != -1) ? " <br> " : "")
        + myMessage.getMessage());

    return myMessage;
  }

  /**
   * Translate the message, searching the @ parameters, and making use of the ErrorTextParser class
   * to get the appropriated message.
   * 
   * @param message
   *          String with the message to translate.
   * @return
   */
  public static OBError translateError(String message) {
    VariablesSecureApp vars = RequestContext.get().getVariablesSecureApp();
    final String strLanguage = OBContext.getOBContext().getLanguage().getLanguage();
    return translateError(new DalConnectionProvider(), vars, strLanguage, message);
  }

  /**
   * Translate the message, searching the @ parameters, and making use of the ErrorTextParser class
   * to get the appropriated message.
   * 
   * @param conn
   *          Handler for the database connection.
   * @param vars
   *          Handler for the session info.
   * @param strLanguage
   *          Language to translate.
   * @param message
   *          String with the message to translate.
   * @return Object with the message.
   */
  public static OBError translateError(ConnectionProvider conn, VariablesSecureApp vars,
      String strLanguage, String _message) {
    String message = _message;
    final OBError myError = new OBError();
    myError.setType("Error");
    myError.setMessage(message);
    if (message == null || message.equals("")) {
      return myError;
    }
    String code = "";
    log4j.debug("translateError - message: " + message);
    if (message.startsWith("@CODE=@")) {
      message = message.substring(7);
    } else if (message.startsWith("@CODE=")) {
      message = message.substring(6);
      final int pos = message.indexOf("@");
      if (pos == -1) {
        code = message;
        message = "";
      } else {
        code = message.substring(0, pos);
        message = message.substring(pos + 1);
      }
    }
    myError.setMessage(message);
    log4j.debug("translateError - code: " + code + " - message: " + message);

    // BEGIN Checking if is a pool problem
    if (code != null && code.equals("NoConnectionAvailable")) {
      myError.setType("Error");
      myError.setTitle("Critical Error");
      myError.setConnectionAvailable(false);
      myError.setMessage("No database connection available");
      return myError;
    }
    // END Checking if is a pool problem

    // BEGIN Parsing message text
    if (message != null && !message.equals("")) {
      final String rdbms = conn.getRDBMS();
      ErrorTextParser myParser = null;
      try {
        final Class<?> c = Class.forName("org.openbravo.erpCommon.utility.ErrorTextParser"
            + rdbms.toUpperCase());
        myParser = (ErrorTextParser) c.newInstance();
      } catch (final ClassNotFoundException ex) {
        log4j.warn("Couldn´t find class: org.openbravo.erpCommon.utility.ErrorTextParser"
            + rdbms.toUpperCase());
        myParser = null;
      } catch (final Exception ex1) {
        log4j.warn("Couldn´t initialize class: org.openbravo.erpCommon.utility.ErrorTextParser"
            + rdbms.toUpperCase());
        myParser = null;
      }
      if (myParser != null) {
        myParser.setConnection(conn);
        myParser.setLanguage(strLanguage);
        myParser.setMessage(message);
        myParser.setVars(vars);
        try {
          final OBError myErrorAux = myParser.parse();
          if (myErrorAux != null
              && !myErrorAux.getMessage().equals("")
              && (code == null || code.equals("") || code.equals("0") || !myErrorAux.getMessage()
                  .equalsIgnoreCase(message)))
            return myErrorAux;
        } catch (final Exception ex) {
          log4j.error("Error while parsing text: " + ex);
        }
      }
    } else {
      myError.setMessage(code);
    }
    // END Parsing message text

    // BEGIN Looking for error code in AD_Message
    if (code != null && !code.equals("")) {
      final FieldProvider fldMessage = locateMessage(conn, code, strLanguage);
      if (fldMessage != null) {
        myError.setType((fldMessage.getField("msgtype").equals("E") ? "Error" : (fldMessage
            .getField("msgtype").equals("I") ? "Info"
            : (fldMessage.getField("msgtype").equals("S") ? "Success" : "Warning"))));
        myError.setMessage(fldMessage.getField("msgtext"));
        return myError;
      }
    }
    // END Looking for error code in AD_Message

    return myError;
  }

  /**
   * Search a message in the database.
   * 
   * @param conn
   *          Handler for the database connection.
   * @param strCode
   *          Message to search.
   * @param strLanguage
   *          Language to translate.
   * @return FieldProvider with the message info.
   */
  public static FieldProvider locateMessage(ConnectionProvider conn, String strCode,
      String strLanguage) {
    FieldProvider[] fldMessage = null;

    try {
      log4j.debug("locateMessage - Message Code: " + strCode);
      fldMessage = MessageBDData.messageInfo(conn, strLanguage, strCode);
    } catch (final Exception ignore) {
    }
    if (fldMessage != null && fldMessage.length > 0) {
      return fldMessage[0];
    } else {
      return null;
    }
  }

}
