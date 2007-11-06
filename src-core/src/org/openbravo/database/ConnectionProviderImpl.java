/*
 ************************************************************************************
 * Copyright (C) 2001-2006 Openbravo S.L.
 * Licensed under the Apache Software License version 2.0
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to  in writing,  software  distributed
 * under the License is distributed  on  an  "AS IS"  BASIS,  WITHOUT  WARRANTIES  OR
 * CONDITIONS OF ANY KIND, either  express  or  implied.  See  the  License  for  the
 * specific language governing permissions and limitations under the License.
 ************************************************************************************
*/
package org.openbravo.database;

import org.apache.xerces.parsers.DOMParser;
import org.xml.sax.*;
import org.w3c.dom.*;
import org.apache.log4j.Logger;
import org.apache.commons.pool.ObjectPool;
import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.pool.KeyedObjectPoolFactory;
import org.apache.commons.pool.impl.StackKeyedObjectPoolFactory;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.apache.commons.dbcp.PoolingDriver;
import org.openbravo.exception.*;

import java.sql.*;
import java.io.*;


public class ConnectionProviderImpl implements ConnectionProvider {
  static Logger log4j = Logger.getLogger(ConnectionProviderImpl.class);
  String defaultPoolName = "";
  String bbdd = "";
  String rdbms = "";
  String contextName = "openbravo";

  public ConnectionProviderImpl (String file) throws PoolNotFoundException {
    this(file, false, "openbravo");
  }

  public ConnectionProviderImpl (String file, String _context) throws PoolNotFoundException {
    this(file, false, _context);
  }


  public ConnectionProviderImpl (String file, boolean isRelative, String _context) throws PoolNotFoundException {
    if (log4j.isDebugEnabled()) log4j.debug("Creating ConnectionProviderImpl");
    if (_context!=null && !_context.equals("")) contextName = _context;
    try {
      DOMParser parser = new DOMParser();
      try {
        parser.parse((!isRelative?(file.toUpperCase().startsWith("FILE:///")?"":"file:///"):"") + file);
      } catch (SAXException se) {
        log4j.error(se);
        throw new PoolNotFoundException("Couldn't load parse for pool file " + file);
      } catch (IOException ioe) {
        log4j.error(ioe);
        throw new PoolNotFoundException("Couldn't load pool file " + file + " for input/output operations");
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

        child = pool.getFirstChild();
        child = child.getNextSibling();
        String dbDriver = ((Text)child.getFirstChild()).getData();
        if (log4j.isDebugEnabled()) log4j.debug("dbDriver: " + dbDriver);
        child = child.getNextSibling();
        child = child.getNextSibling();
        String dbServer = ((Text)child.getFirstChild()).getData();
        if (log4j.isDebugEnabled()) log4j.debug("dbServer: " + dbServer);
        child = child.getNextSibling();
        child = child.getNextSibling();
        Text textDbLogin = (Text)child.getFirstChild();
        String dbLogin;
        if (textDbLogin != null) {
          dbLogin  = textDbLogin.getData();
          if (log4j.isDebugEnabled()) log4j.debug("dbLogin: " + dbLogin);
        } else
          dbLogin = null;
        child = child.getNextSibling();
        child = child.getNextSibling();
        Text textDbPassword = (Text)child.getFirstChild();
        String dbPassword;
        if (textDbPassword != null) {
          dbPassword = textDbPassword.getData();
          if (log4j.isDebugEnabled()) log4j.debug("dbPassword: " + dbPassword);
        } else
          dbPassword = null;
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
        String rdbms  = ((Text)child.getFirstChild()).getData();
        if (log4j.isDebugEnabled()) log4j.debug("rdbms: " + rdbms);


        addNewPool(dbDriver,dbServer,dbLogin,dbPassword, minConns,maxConns,maxConnTime,dbSessionConfig, rdbms, poolName);
      }
    }
    catch (Exception e) {
      log4j.error(e);
      throw new PoolNotFoundException("Failed when creating database connections pool");
    }
  }

  public void destroy(String name) throws Exception {
    PoolingDriver driver = (PoolingDriver) DriverManager.getDriver("jdbc:apache:commons:dbcp:");
    driver.closePool(name);
  }

  public void destroy() throws Exception {
    destroy(defaultPoolName);
  }

  public void addNewPool(String dbDriver, String dbServer, String dbLogin, String dbPassword, int minConns, int maxConns, double maxConnTime, String dbSessionConfig, String rdbms, String name) throws Exception {
    if (log4j.isDebugEnabled()) log4j.debug("Loading underlying JDBC driver.");
    try {
      Class.forName(dbDriver);
    } catch (ClassNotFoundException e) {
      throw new Exception(e);
    }
    if (log4j.isDebugEnabled()) log4j.debug("Done.");


    GenericObjectPool connectionPool = new GenericObjectPool(null);
    connectionPool.setWhenExhaustedAction(GenericObjectPool.WHEN_EXHAUSTED_GROW);
    connectionPool.setMaxActive(maxConns);
    connectionPool.setTestOnBorrow(false); 
    connectionPool.setTestOnReturn(false); 
    connectionPool.setTestWhileIdle(false);

    KeyedObjectPoolFactory keyedObject = new StackKeyedObjectPoolFactory();
    ConnectionFactory connectionFactory = new OpenBravoDriverManagerConnectionFactory(dbServer, dbLogin, dbPassword, dbSessionConfig);
    @SuppressWarnings("unused") //required by dbcp
	PoolableConnectionFactory poolableConnectionFactory = new PoolableConnectionFactory(connectionFactory,connectionPool,keyedObject,null,false,true);

    Class.forName("org.apache.commons.dbcp.PoolingDriver");
    PoolingDriver driver = (PoolingDriver) DriverManager.getDriver("jdbc:apache:commons:dbcp:");
    driver.registerPool(contextName + "_" + name, connectionPool);

    if (this.defaultPoolName==null || this.defaultPoolName.equals("")) {
      this.defaultPoolName = name;
      this.bbdd = dbServer;
      this.rdbms = rdbms;
    }
  }

  public ObjectPool getPool(String poolName) throws PoolNotFoundException {
    if (poolName==null || poolName.equals("")) throw new PoolNotFoundException("Couldn´t get an unnamed pool");
    ObjectPool connectionPool = null;
    try {
      PoolingDriver driver = (PoolingDriver) DriverManager.getDriver("jdbc:apache:commons:dbcp:");
      connectionPool = driver.getConnectionPool(contextName + "_" + poolName);
    } catch (SQLException ex) {
      log4j.error(ex);
    }
    if (connectionPool == null)
      throw new PoolNotFoundException(poolName + " not found");
    else
      return connectionPool;
  }

  public ObjectPool getPool() throws PoolNotFoundException {
    return getPool(defaultPoolName);
  }

  public Connection getConnection() throws NoConnectionAvailableException {
    return getConnection(defaultPoolName);
  }

  public Connection getConnection(String poolName) throws NoConnectionAvailableException {
    if (poolName==null || poolName.equals("")) throw new NoConnectionAvailableException("Couldn´t get a connection for an unnamed pool");
    Connection conn=null;
    try {
      conn = DriverManager.getConnection("jdbc:apache:commons:dbcp:" + contextName + "_" + poolName);
    } catch (SQLException ex) {
      log4j.error(ex);
      throw new NoConnectionAvailableException("There are no connections available in jdbc:apache:commons:dbcp:" + contextName + "_" + poolName);
    }
    return conn;
  }

  public String getRDBMS() {
    return rdbms;
  }

  public boolean releaseConnection(Connection conn) {
    if (conn==null) return false;
    try {
      conn.setAutoCommit(true);
      conn.close();
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

  public PreparedStatement getPreparedStatement(String SQLPreparedStatement) throws Exception {
    return getPreparedStatement(defaultPoolName, SQLPreparedStatement);
  }

  public PreparedStatement getPreparedStatement(String poolName, String SQLPreparedStatement) throws Exception {
    if (poolName == null || poolName.equals("")) throw new PoolNotFoundException("Can't get the pool. No pool name specified");
    if (log4j.isDebugEnabled()) log4j.debug("connection requested");
    Connection conn = getConnection(poolName);
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

  public CallableStatement getCallableStatement(String SQLCallableStatement) throws Exception {
    return getCallableStatement(defaultPoolName, SQLCallableStatement);
  }

  public CallableStatement getCallableStatement(String poolName, String SQLCallableStatement) throws Exception {
    if (poolName == null || poolName.equals("")) throw new PoolNotFoundException("Can't get the pool. No pool name specified");
    Connection conn = getConnection(poolName);
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

  public Statement getStatement() throws Exception {
    return getStatement(defaultPoolName);
  }

  public Statement getStatement(String poolName) throws Exception {
    if (poolName == null || poolName.equals("")) throw new PoolNotFoundException("Can't get the pool. No pool name specified");
    Connection conn = getConnection(poolName);
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
