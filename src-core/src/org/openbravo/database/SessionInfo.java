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
 * All portions are Copyright (C) 2009 Openbravo SL 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Logger;

/**
 * This class is used to maitain session information which will be used for audit purposes.
 * 
 */
public class SessionInfo {
  static Logger log4j = Logger.getLogger(SessionInfo.class);
  private static ThreadLocal<String> sessionId = new ThreadLocal<String>();
  private static ThreadLocal<String> userId = new ThreadLocal<String>();
  private static ThreadLocal<String> processType = new ThreadLocal<String>();
  private static ThreadLocal<String> processId = new ThreadLocal<String>();

  /**
   * Sets to null all session information
   */
  static public void init() {
    sessionId.set(null);
    userId.set(null);
    processType.set(null);
    processId.set(null);
  }

  /**
   * Inserts in the session table the information about the Openbravo session
   * 
   * @param conn
   *          Connection where the session information will be stored in
   */
  static public void setDBSessionInfo(Connection conn) {
    try {
      getPreparedStatement(conn, "delete from ad_context_info").executeUpdate();
      PreparedStatement ps = getPreparedStatement(
          conn,
          "insert into ad_context_info (ad_user_id, ad_session_id, processType, processId) values (?, ?, ?, ?)");
      ps.setString(1, SessionInfo.getUserId());
      ps.setString(2, SessionInfo.getSessionId());
      ps.setString(3, SessionInfo.getProcessType());
      ps.setString(4, SessionInfo.getProcessId());
      ps.executeUpdate();
    } catch (Exception e) {
      log4j.error("Error setting audit info", e);
    }
  }

  private static PreparedStatement getPreparedStatement(Connection conn, String SQLPreparedStatement)
      throws SQLException {
    if (conn == null || SQLPreparedStatement == null || SQLPreparedStatement.equals(""))
      return null;
    PreparedStatement ps = null;

    try {
      if (log4j.isDebugEnabled())
        log4j.debug("preparedStatement requested");
      ps = conn.prepareStatement(SQLPreparedStatement, ResultSet.TYPE_SCROLL_INSENSITIVE,
          ResultSet.CONCUR_READ_ONLY);
      if (log4j.isDebugEnabled())
        log4j.debug("preparedStatement received");
    } catch (SQLException e) {
      log4j.error("getPreparedStatement: " + SQLPreparedStatement + "\n" + e);
      if (conn != null) {
        try {
          conn.setAutoCommit(true);
          conn.close();
        } catch (Exception ex) {
          ex.printStackTrace();
        }
      }
    }
    return (ps);
  }

  static public void setUserId(String user) {
    userId.set(user);
  }

  static public String getUserId() {
    return userId.get();
  }

  public static void setProcessId(String processId) {
    SessionInfo.processId.set(processId);
  }

  public static String getProcessId() {
    return processId.get();
  }

  public static void setProcessType(String processType) {
    SessionInfo.processType.set(processType);
  }

  public static String getProcessType() {
    return processType.get();
  }

  public static void setSessionId(String session) {
    sessionId.set(session);
  }

  public static String getSessionId() {
    return sessionId.get();
  }
}
