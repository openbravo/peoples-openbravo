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
 * All portions are Copyright (C) 2009-2010 Openbravo SL 
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
 * This class is used to maintain session information which will be used for audit purposes.
 * 
 */
public class SessionInfo {
  static Logger log4j = Logger.getLogger(SessionInfo.class);
  private static boolean isAuditActive = false;
  private static ThreadLocal<String> sessionId = new ThreadLocal<String>();
  private static ThreadLocal<String> userId = new ThreadLocal<String>();
  private static ThreadLocal<String> processType = new ThreadLocal<String>();
  private static ThreadLocal<String> processId = new ThreadLocal<String>();
  private static ThreadLocal<Connection> sessionConnection = new ThreadLocal<Connection>();
  private static ThreadLocal<Boolean> changedInfo = new ThreadLocal<Boolean>();

  /**
   * Sets to null all session information
   */
  static public void init() {
    sessionId.set(null);
    userId.set(null);
    processType.set(null);
    processId.set(null);
    changedInfo.set(null);
    // close connection
    Connection conn = sessionConnection.get();
    try {
      if (conn != null && !conn.isClosed()) {
        if (log4j.isDebugEnabled()) {
          log4j.debug("Close session's connection");
        }
        conn.setAutoCommit(true);
        conn.close();
      }
    } catch (SQLException e) {
      log4j.error("Error closing connection", e);
    }
    sessionConnection.set(null);
  }

  /**
   * Creates the needed infrastructure for audit. Which is temporary session table for PostgreSQL
   * connections
   * 
   * @param conn
   *          Connection to database
   * @param rdbms
   *          Database, only action is take for POSTGRESQL
   */
  static public void initDB(Connection conn, String rdbms) {
    try {
      if (rdbms != null && rdbms.equals("POSTGRE")) {
        // Create temporary table
        ResultSet rs = getPreparedStatement(
            conn,
            "select count(*) from information_schema.tables where table_name='ad_context_info' and table_type = 'LOCAL TEMPORARY'")
            .executeQuery();

        if (rs.next() && rs.getString(1).equals("0")) {
          StringBuffer sql = new StringBuffer();
          sql.append("CREATE GLOBAL TEMPORARY TABLE AD_CONTEXT_INFO");
          sql.append("(AD_USER_ID VARCHAR(32), ");
          sql.append("  AD_SESSION_ID VARCHAR(32),");
          sql.append("  PROCESSTYPE VARCHAR(60), ");
          sql.append("  PROCESSID VARCHAR(32)) on commit preserve rows");
          getPreparedStatement(conn, sql.toString()).execute();
        }
      }
    } catch (Exception e) {
      log4j.error("Error initializating audit infrastructure", e);
    }
  }

  /**
   * Inserts in the session table the information about the Openbravo session
   * 
   * 
   * @param conn
   *          Connection where the session information will be stored in
   * @param onlyIfChanged
   *          Updates database info only in case there are changes since the last time it was set
   */
  static public void setDBSessionInfo(Connection conn, boolean onlyIfChanged) {
    if (!isAuditActive || (onlyIfChanged && (changedInfo.get() == null || !changedInfo.get()))) {
      if (log4j.isDebugEnabled()) {
        log4j.debug("No session info set isAuditActive: " + isAuditActive + " - changes in info: "
            + changedInfo.get());
      }
      return;
    }
    setDBSessionInfo(conn);
  }

  static public void setDBSessionInfo(Connection conn) {
    try {
      if (!isAuditActive) {
        return;
      }

      if (log4j.isDebugEnabled()) {
        log4j.debug("set session info");
      }
      // Clean up temporary table
      getPreparedStatement(conn, "delete from ad_context_info").executeUpdate();

      PreparedStatement ps = getPreparedStatement(
          conn,
          "insert into ad_context_info (ad_user_id, ad_session_id, processType, processId) values (?, ?, ?, ?)");
      ps.setString(1, SessionInfo.getUserId());
      ps.setString(2, SessionInfo.getSessionId());
      ps.setString(3, SessionInfo.getProcessType());
      ps.setString(4, SessionInfo.getProcessId());
      ps.executeUpdate();
      changedInfo.set(false);

    } catch (Exception e) {
      log4j.error("Error setting audit info", e);
    }
  }

  static public Connection getSessionConnection() {
    Connection conn = sessionConnection.get();
    try {
      if (conn == null || conn.isClosed()) {
        return null;
      }
    } catch (SQLException e) {
      log4j.error("Error checking connection", e);
      return null;
    }
    if (log4j.isDebugEnabled()) {
      log4j.debug("Reuse session's connection");
    }
    return conn;
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
    if (user == null || !user.equals(getUserId())) {
      userId.set(user);
      changedInfo.set(true);
    }
  }

  static public String getUserId() {
    return userId.get();
  }

  public static void setProcessId(String processId) {
    if (processId == null || !processId.equals(getProcessId())) {
      SessionInfo.processId.set(processId);
      changedInfo.set(true);
    }
  }

  public static String getProcessId() {
    return processId.get();
  }

  public static void setProcessType(String processType) {
    if (processType == null || !processType.equals(getProcessType())) {
      SessionInfo.processType.set(processType);
      changedInfo.set(true);
    }
  }

  public static String getProcessType() {
    return processType.get();
  }

  public static void setSessionId(String session) {
    if (session == null || !session.equals(getSessionId())) {
      sessionId.set(session);
      changedInfo.set(true);
    }
  }

  public static String getSessionId() {
    return sessionId.get();
  }

  public static void setAuditActive(boolean isAuditActive) {
    SessionInfo.isAuditActive = isAuditActive;
  }

  public static void setSessionConnection(Connection conn) {
    sessionConnection.set(conn);
  }
}
