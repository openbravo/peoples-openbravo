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
package org.openbravo.wad;


import java.sql.*;
import java.io.*;
import org.apache.xerces.parsers.DOMParser;
import org.xml.sax.*;
import org.w3c.dom.*;
import org.openbravo.exception.*;
import org.openbravo.database.*;
import org.apache.log4j.Logger;

public class WadConnection implements ConnectionProvider {
  static Logger log4j = Logger.getLogger(WadConnection.class);
  protected Connection myPool;
  String defaultPoolName = "";
  String bbdd = "";
  String rdbms = "";
  String contextName = "openbravo";

  public WadConnection (String xmlPoolFile) {
    if (myPool == null) {
      try {
        connect(xmlPoolFile);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  public void connect(String file) throws ClassNotFoundException, SQLException {
    if (log4j.isDebugEnabled()) log4j.debug("Creating Connection");
    try {
      DOMParser parser = new DOMParser();
      try {
          parser.parse((file.toUpperCase().startsWith("FILE:///")?"":"file:///") + file);
      } catch (SAXException se) {
        log4j.error(se);
        throw new SQLException("Couldn't load parse for pool file " + file);
      } catch (IOException ioe) {
        log4j.error(ioe);
        throw new SQLException("Couldn't load pool file " + file + " for input/output operations");
      }
      Document xmlPool = parser.getDocument();
      NodeList nodeList = xmlPool.getElementsByTagName("pool");
      log4j.debug("Pool's elements: " + nodeList.getLength());
      Node child = null;
      for (int i=0;i<nodeList.getLength();i++) {
        Node pool = nodeList.item(i);
        NamedNodeMap atributos = pool.getAttributes();
        String poolName = atributos.item(0).getNodeValue();
        if (log4j.isDebugEnabled()) log4j.debug("poolName: " + poolName);
        this.defaultPoolName = poolName;

        child = pool.getFirstChild();
        child = child.getNextSibling();
        String dbDriver = ((Text)child.getFirstChild()).getData();
        if (log4j.isDebugEnabled()) log4j.debug("dbDriver: " + dbDriver);
        Class.forName(dbDriver);
        child = child.getNextSibling();
        child = child.getNextSibling();
        String dbServer = ((Text)child.getFirstChild()).getData();
        if (log4j.isDebugEnabled()) log4j.debug("dbServer: " + dbServer);
        this.bbdd = dbServer;
        child = child.getNextSibling();
        child = child.getNextSibling();
        Text textDbLogin = (Text)child.getFirstChild();
        String dbLogin;
        if (textDbLogin != null) {
          dbLogin  = textDbLogin.getData();
          if (log4j.isDebugEnabled()) log4j.debug("dbLogin: " + dbLogin);
        } else dbLogin = null;
        child = child.getNextSibling();
        child = child.getNextSibling();
        Text textDbPassword = (Text)child.getFirstChild();
        String dbPassword;
        if (textDbPassword != null) {
          dbPassword = textDbPassword.getData();
          if (log4j.isDebugEnabled()) log4j.debug("dbPassword: " + dbPassword);
        } else dbPassword = null;
        this.myPool=DriverManager.getConnection(dbServer, dbLogin, dbPassword);
        this.myPool.setAutoCommit(true);
        
        child = child.getNextSibling();
        child = child.getNextSibling();
        int minConns   = Integer.valueOf(((Text)child.getFirstChild()).getData()).intValue();
        if (log4j.isDebugEnabled()) log4j.debug("minConns: " + minConns);
        child = child.getNextSibling();
        child = child.getNextSibling();
        int maxConns   = Integer.valueOf(((Text)child.getFirstChild()).getData()).intValue();
        if (log4j.isDebugEnabled()) log4j.debug("maxConns: " + maxConns);
        child = child.getNextSibling();
        child = child.getNextSibling();
        double maxConnTime   = Double.valueOf(((Text)child.getFirstChild()).getData()).doubleValue();
        if (log4j.isDebugEnabled()) log4j.debug("maxConnTime: " + Double.toString(maxConnTime));
        child = child.getNextSibling();
        child = child.getNextSibling();
        String dbSessionConfig  = ((Text)child.getFirstChild()).getData();
        if (log4j.isDebugEnabled()) log4j.debug("dbSessionConfig: " + dbSessionConfig);
        child = child.getNextSibling();
        child = child.getNextSibling();
        String myrdbms  = ((Text)child.getFirstChild()).getData();
        if (log4j.isDebugEnabled()) log4j.debug("rdbms: " + myrdbms);


        this.rdbms = myrdbms;
      }
    }
    catch (Exception e) {
      log4j.error(e);
      throw new SQLException("Failed when creating database connections pool");
    }
  }

  public void destroy() {
    try {
      if (myPool!=null) myPool.close();
      myPool = null;
    } catch(SQLException e){
      log4j.error("SQL error in closeConnection: " + e);
    }
  }

  public Connection getConnection() throws NoConnectionAvailableException {
    return this.myPool;
  }

  public String getRDBMS() {
    return rdbms;
  }

  public boolean releaseConnection(Connection conn) {
    if (conn==null) return false;
    try {
      conn.setAutoCommit(true);
      //conn.close();
    } catch (Exception ex) {
      ex.printStackTrace();
      return false;
    }
    return true;
  }

  public Connection getTransactionConnection() throws NoConnectionAvailableException, SQLException {
    Connection conn = getConnection();
    if (conn==null) throw new NoConnectionAvailableException("Couldn´t get an available connection");
    conn.setAutoCommit(false);
    return conn;
  }

  public void releaseCommitConnection(Connection conn) throws SQLException {
    if (conn==null) return;
    conn.commit();
    releaseConnection(conn);
  }

  public void releaseRollbackConnection(Connection conn) throws SQLException {
    if (conn==null) return;
    conn.rollback();
    releaseConnection(conn);
  }

  public PreparedStatement getPreparedStatement(String poolName, String SQLPreparedStatement) throws Exception {
    return getPreparedStatement(SQLPreparedStatement);
  }

  public PreparedStatement getPreparedStatement(String SQLPreparedStatement) throws Exception {
    if (log4j.isDebugEnabled()) log4j.debug("connection requested");
    Connection conn = getConnection();
    if (log4j.isDebugEnabled()) log4j.debug("connection established");
    return getPreparedStatement(conn, SQLPreparedStatement);
  }

  public PreparedStatement getPreparedStatement(Connection conn, String SQLPreparedStatement) throws SQLException {
    if (conn == null || SQLPreparedStatement==null || SQLPreparedStatement.equals("")) return null;
    PreparedStatement ps = null;
    try {
      if (log4j.isDebugEnabled()) log4j.debug("preparedStatement requested");
      ps = conn.prepareStatement(SQLPreparedStatement, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
      if (log4j.isDebugEnabled()) log4j.debug("preparedStatement received");
    } catch(SQLException e) {
      log4j.error("getPreparedStatement: " + SQLPreparedStatement + "\n" + e);
      releaseConnection(conn);
      throw e;
    }
    return(ps);
  }

  public CallableStatement getCallableStatement(String poolName, String SQLCallableStatement) throws Exception {
    return getCallableStatement(SQLCallableStatement);
  }

  public CallableStatement getCallableStatement(String SQLCallableStatement) throws Exception {
    Connection conn = getConnection();
    return getCallableStatement(conn, SQLCallableStatement);
  }

  public CallableStatement getCallableStatement(Connection conn, String SQLCallableStatement) throws SQLException {
    if (conn==null || SQLCallableStatement==null || SQLCallableStatement.equals("")) return null;
    CallableStatement cs = null;
    try {
      cs = conn.prepareCall(SQLCallableStatement);
    } catch(SQLException e) {
      log4j.error("getCallableStatement: " + SQLCallableStatement + "\n" + e);
      releaseConnection(conn);
      throw e;
    }
    return(cs);
  }

  public Statement getStatement(String name) throws Exception {
    return getStatement();
  }

  public Statement getStatement() throws Exception {
    Connection conn = getConnection();
    return getStatement(conn);
  }

  public Statement getStatement(Connection conn) throws SQLException {
    if (conn == null) return null;
    try {
      return(conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY));
    } catch(SQLException e) {
      log4j.error("getStatement: " + e);
      releaseConnection(conn);
      throw e;
    }
  }

  public void releasePreparedStatement(PreparedStatement preparedStatement) throws SQLException {
    if (preparedStatement == null) return;
    Connection conn = null;
    try {
      conn = preparedStatement.getConnection();
      preparedStatement.close();
      releaseConnection(conn);
    } catch (SQLException e) {
      log4j.error("releasePreparedStatement: " + e);
      releaseConnection(conn);
      throw e;
    }
  }

  public void releaseCallableStatement(CallableStatement callableStatement) throws SQLException {
    if (callableStatement == null) return;
    Connection conn = null;
    try {
      conn = callableStatement.getConnection();
      callableStatement.close();
      releaseConnection(conn);
    } catch (SQLException e) {
      log4j.error("releaseCallableStatement: " + e);
      releaseConnection(conn);
      throw e;
    }
  }

  public void releaseStatement(Statement statement) throws SQLException {
    if (statement == null) return;
    Connection conn = null;
    try {
      conn = statement.getConnection();
      statement.close();
      releaseConnection(conn);
    } catch (SQLException e) {
      log4j.error("releaseStatement: " + e);
      releaseConnection(conn);
      throw e;
    }
  }

  public void releaseTransactionalStatement(Statement statement) throws SQLException {
    if (statement == null) return;
    statement.close();
  }

  public void releaseTransactionalPreparedStatement(PreparedStatement preparedStatement) throws SQLException {
    if (preparedStatement==null) return;
    preparedStatement.close();
  }

  /**
  * Returns the actual status of the dynamic pool.
  */
  public String getStatus() {
    StringBuffer strResultado = new StringBuffer();
    strResultado.append("Not implemented yet");
    return strResultado.toString();
  }//End getStatus()
}
