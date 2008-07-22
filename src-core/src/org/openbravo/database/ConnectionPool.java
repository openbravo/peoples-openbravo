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
import java.sql.*;
import javax.sql.DataSource;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.*;
import org.apache.log4j.Logger;
import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.PoolingDataSource;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.pool.KeyedObjectPoolFactory;
import org.apache.commons.pool.impl.StackKeyedObjectPoolFactory;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.openbravo.database.OpenbravoDriverManagerConnectionFactory;

/**
 * ConnectionPool
 * Pool of database connections with internal pool of PreparedStatements.
 * Creates and manages a pool of database connections and a pool of PreparedStatements.
 * @version 1.0.1 26/01/2006
 * @author Openbravo S.L.
*/
public class ConnectionPool {
  static Logger log4j = Logger.getLogger(ConnectionPool.class);
  private DataSource myPool;
  private String rdbms = "";

  public ConnectionPool(String namespace) throws ServletException {
    try {
      Context initContext = new InitialContext();
      Context envContext  = (Context)initContext.lookup("java:/comp/env");
      myPool = (DataSource)envContext.lookup(namespace);
    } catch (NamingException nex) {
      nex.printStackTrace();
      throw new ServletException(nex.toString());
    }
    if(log4j.isDebugEnabled()) log4j.debug("Starting ConnectionPool Version 1.0.1:");
  }//End ConnectionPool()

  public ConnectionPool(String _driver, String _server, String _login, String _password, int _minConns, int _maxConns, double _maxConnTime, String _dbSessionConfig, String _rdbms) throws ServletException {
    rdbms = _rdbms;
    if (log4j.isDebugEnabled()) log4j.debug("Loading underlying JDBC driver.");
    try {
      Class.forName(_driver);
    } catch (ClassNotFoundException e) {
      throw new ServletException(e.toString());
    }
    if (log4j.isDebugEnabled()) log4j.debug("Done.");


    GenericObjectPool connectionPool = new GenericObjectPool(null);
    connectionPool.setWhenExhaustedAction(GenericObjectPool.WHEN_EXHAUSTED_GROW);
    connectionPool.setMaxActive(_maxConns);
    connectionPool.setTestOnBorrow(false); 
    connectionPool.setTestOnReturn(false); 
    connectionPool.setTestWhileIdle(false);

    KeyedObjectPoolFactory keyedObject = new StackKeyedObjectPoolFactory();
    ConnectionFactory connectionFactory = new OpenbravoDriverManagerConnectionFactory(_server, _login, _password, _dbSessionConfig);
    @SuppressWarnings("unused") //required by dbcp
	PoolableConnectionFactory poolableConnectionFactory = new PoolableConnectionFactory(connectionFactory,connectionPool,keyedObject, null,false,true);

    PoolingDataSource dataSource = new PoolingDataSource(connectionPool);

    myPool = dataSource;
  }


  /**
   * getConnection
   * Return the first free connection of the pool.
   * Loop over the pool searching for the first free connection.
   */
  public Connection getConnection() {
    Connection conn=null;
    try {
      conn = myPool.getConnection();
    } catch (SQLException ex) {
      ex.printStackTrace();
    }
    return conn;
  }

  public String getRDBMS() {
    return rdbms;
  }

  /**
   * releaseConnection
   * Free a specific connection but get them alive (unclosed).
   */
  public boolean releaseConnection(Connection conn) {
    try {
      conn.setAutoCommit(true);
      conn.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return true;
  }

  public PreparedStatement getPreparedStatement(String SQLPreparedStatement) {
    if (log4j.isDebugEnabled()) log4j.debug("connection requested");
    Connection conn = getConnection();
    if (log4j.isDebugEnabled()) log4j.debug("connection established");
    return getPreparedStatement(conn, SQLPreparedStatement);
  }

  public PreparedStatement getPreparedStatement(Connection conn, String SQLPreparedStatement) {
    try {
      PreparedStatement ps;
      if (log4j.isDebugEnabled()) log4j.debug("preparedStatement requested");
      ps = conn.prepareStatement(SQLPreparedStatement, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
      if (log4j.isDebugEnabled()) log4j.debug("preparedStatement received");
      return(ps);
    } catch(SQLException e) {
      log4j.error("getPreparedStatement: " + SQLPreparedStatement);
      releaseConnection(conn);
      e.printStackTrace();
      return null;
    }
  }

  public CallableStatement getCallableStatement(String SQLCallableStatement) {
    Connection conn = getConnection();
    return getCallableStatement(conn, SQLCallableStatement);
  }

  public CallableStatement getCallableStatement(Connection conn, String SQLCallableStatement) {
    try {
      CallableStatement cs;
      cs = conn.prepareCall(SQLCallableStatement);
      return(cs);
    } catch(SQLException e) {
      log4j.error("getCallableStatement: " + SQLCallableStatement);
      releaseConnection(conn);
      e.printStackTrace();
      return null;
    }
  }

  public Statement getStatement() {
    Connection conn = getConnection();
    return getStatement(conn);
  }

  public Statement getStatement(Connection conn) {
    try {
      return(conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY));
    } catch(SQLException e) {
      releaseConnection(conn);
      e.printStackTrace();
      return null;
    }
  }

  public void releasePreparedStatement(PreparedStatement preparedStatement) {
    try {
      Connection conn = preparedStatement.getConnection();
      releaseConnection(conn);
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public void releaseCallableStatement(CallableStatement callableStatement) {
    try {
      Connection conn = callableStatement.getConnection();
      releaseConnection(conn);
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public void releaseStatement(Statement statement) {
    try {
      Connection conn = statement.getConnection();
      statement.close();
      releaseConnection(conn);
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }


  /**
   * destroy
   * Destroy de thread releasing every connections to the database
   */
  public void destroy() { 
    myPool = null;
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
