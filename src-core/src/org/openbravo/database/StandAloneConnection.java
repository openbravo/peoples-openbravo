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

import javax.servlet.ServletException;

import org.apache.xerces.parsers.DOMParser;
import org.xml.sax.*;
import org.w3c.dom.*;

import java.io.*;
import java.sql.*;
import org.openbravo.exception.*;
import java.util.Hashtable;
import java.util.Properties;

import org.apache.log4j.Logger;

public class StandAloneConnection implements ConnectionProvider {
  public static ConnectionPool myPool;
  protected static Hashtable<String, ConnectionPool> pools = new Hashtable<String, ConnectionPool>();
  String strDriver;
  String strURL;
  Connection connection;
  static Logger log4j = Logger.getLogger(StandAloneConnection.class);
  static String sqlDateFormat;

  public void createPool(String poolDir) {
    try {

      DOMParser parser = new DOMParser();
      try {
        parser.parse(poolDir + "/XmlPool.xml");
      } catch (SAXException se) {
        se.printStackTrace();
      } catch (IOException ioe) {
        ioe.printStackTrace();
      }
      Document xmlPool = parser.getDocument();
      NodeList nodeList = xmlPool.getElementsByTagName("pool");
      log4j.info("pool elements: " + nodeList.getLength());
      Node child = null;
      for (int i=0;i<nodeList.getLength();i++) {
        Node pool = nodeList.item(i); // We obtain the pool
        NamedNodeMap atributos = pool.getAttributes(); // We get its attributes to obtain its name
        String poolName = atributos.item(0).getNodeValue(); // We get the name of the pool, which is the first and unique attribute of the pool
        log4j.info("Pool name: " + poolName);

        /* We obtain the different nodes of the correspondant pool, which are the pool's configuration parameters, each one separated by two nodes
        */
        child = pool.getFirstChild();
        child = child.getNextSibling();
        String dbDriver = ((Text)child.getFirstChild()).getData();
        log4j.info("Driver: " + dbDriver);
        child = child.getNextSibling();
        child = child.getNextSibling();
        String dbServer = ((Text)child.getFirstChild()).getData();
        log4j.info("Server: " + dbServer);
        child = child.getNextSibling();
        child = child.getNextSibling();
        Text textDbLogin = (Text)child.getFirstChild();
        String dbLogin;
        if (textDbLogin != null) {
          dbLogin  = textDbLogin.getData();
          log4j.info("Login: " + dbLogin);
        } else
          dbLogin = null;
        child = child.getNextSibling();
        child = child.getNextSibling();
        Text textDbPassword = (Text)child.getFirstChild();
        String dbPassword;
        if (textDbPassword != null) {
          dbPassword = textDbPassword.getData();
          log4j.info("Password: " + dbPassword);
        } else
          dbPassword = null;
        child = child.getNextSibling();
        child = child.getNextSibling();
        int minConns   = Integer.valueOf(((Text)child.getFirstChild()).getData()).intValue();
        log4j.info("Min conns: " + minConns);
        child = child.getNextSibling();
        child = child.getNextSibling();
        int maxConns   = Integer.valueOf(((Text)child.getFirstChild()).getData()).intValue();
        log4j.info("Max conns: " + maxConns);
        child = child.getNextSibling();
        child = child.getNextSibling();
        double maxConnTime   = Double.valueOf(((Text)child.getFirstChild()).getData()).doubleValue();
        log4j.info("maxConnTime: " + Double.toString(maxConnTime));
        child = child.getNextSibling();
        child = child.getNextSibling();
        String dbSessionConfig  = ((Text)child.getFirstChild()).getData();
        log4j.info("dbSessionConfig: " + dbSessionConfig);
        child = child.getNextSibling();
        child = child.getNextSibling();
        String rdbms  = ((Text)child.getFirstChild()).getData();
        log4j.info("rdbms: " + rdbms);

        readProperties(poolDir+"/Openbravo.properties");

        ConnectionPool myLocalPool = new ConnectionPool(dbDriver,dbServer,dbLogin,dbPassword, minConns,maxConns, maxConnTime,dbSessionConfig, rdbms);

        if (myLocalPool==null)
          log4j.error("Initialization failed on pool: " + i);
        pools.put(poolName, myLocalPool);
        if (i==0) {
          myPool = myLocalPool;
        }
      }
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }

  // methods of Connection provider
  private ConnectionPool getPool(String poolName) throws PoolNotFoundException {
    ConnectionPool myPool = pools.get(poolName);
    if (myPool == null)
      throw new PoolNotFoundException(poolName + " not found");
    else
      return myPool;
  }

  private ConnectionPool getPool() throws PoolNotFoundException {
    if (myPool == null)
      throw new PoolNotFoundException("Default pool not found");
    else
      return myPool;
  }

  public Connection getConnection() {
    try {
      return (getPool().getConnection());
    }
    catch (Exception ignored){}
    return null;
  }

  public String getRDBMS() {
    try {
      return (getPool().getRDBMS());
    }
    catch (Exception ignored){}
    return null;
  }

  public Connection getTransactionConnection() {
    try {
      Connection conn = getPool().getConnection();
      conn.setAutoCommit(false);
      return conn;
    } catch (PoolNotFoundException ignored){
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return null;
  }

  public void releaseCommitConnection(Connection conn) {
    try {
      conn.commit();
      myPool.releaseConnection(conn);
      return;
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return;
  }

  public void releaseRollbackConnection(Connection conn) {
    try {
      conn.rollback();
      myPool.releaseConnection(conn);
      return;
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return;
  }

  public PreparedStatement getPreparedStatement(String poolName, String strSql) {
    try {
      return (getPool(poolName).getPreparedStatement(strSql));
    }
    catch (PoolNotFoundException ignored){}
    return null;
  }

  public PreparedStatement getPreparedStatement(String strSql) {
    try {
      return (getPool().getPreparedStatement(strSql));
    }
    catch (PoolNotFoundException ignored){}
    return null;
  }

  public PreparedStatement getPreparedStatement(Connection conn, String strSql) {
    try {
      return (getPool().getPreparedStatement(conn, strSql));
    }
    catch (PoolNotFoundException ignored){}
    return null;
  }

  public void releasePreparedStatement(PreparedStatement preparedStatement){
    try {
      Connection conn = preparedStatement.getConnection();
      preparedStatement.close();
      myPool.releaseConnection(conn);
    }
    catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public Statement getStatement(String poolName) {
    try {
      return (getPool(poolName).getStatement());
    }
    catch (PoolNotFoundException ignored){}
    return null;
  }

  public Statement getStatement() {
    try {
      return (getPool().getStatement());
    }
    catch (PoolNotFoundException ignored){}
    return null;
  }

  public Statement getStatement(Connection conn) {
    try {
      return (getPool().getStatement(conn));
    }
    catch (PoolNotFoundException ignored){}
    return null;
  }

  public void releaseStatement(Statement statement){
    try {
      Connection conn = statement.getConnection();
      statement.close();
      myPool.releaseConnection(conn);
    }
    catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public void releaseTransactionalStatement(Statement statement){
    try {
      statement.close();
    }
    catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public void releaseTransactionalPreparedStatement(PreparedStatement preparedStatement){
    try {
      preparedStatement.close();
    }
    catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public CallableStatement getCallableStatement(String poolName, String strSql) {
    try {
      return (getPool(poolName).getCallableStatement(strSql));
    }
    catch (PoolNotFoundException ignored){}
    return null;
  }

  public CallableStatement getCallableStatement(String strSql) {
    try {
      return (getPool().getCallableStatement(strSql));
    }
    catch (PoolNotFoundException ignored){}
    return null;
  }

  public CallableStatement getCallableStatement(Connection conn, String strSql) {
    try {
      return (getPool().getCallableStatement(conn, strSql));
    }
    catch (PoolNotFoundException ignored){}
    return null;
  }

  public void releaseCallableStatement(CallableStatement callableStatement){
    try {
      Connection conn = callableStatement.getConnection();
      callableStatement.close();
      myPool.releaseConnection(conn);
    }
    catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public void connect() throws ClassNotFoundException, SQLException {
    log4j.info("Driver loading: " + strDriver);
    Class.forName(strDriver);
    log4j.info("Driver loaded");
    log4j.info("Connection with: "+ strURL);
    connection=DriverManager.getConnection(strURL);
    log4j.info("connect made");
  }

  public void closeConnection() {
    try {
      connection.close();
    } catch(SQLException e){
      log4j.error("SQL error in closeConnection: " + e);
    }
  }

  public static int dynamicQuery(ConnectionProvider conn, String strSql) throws ServletException {
    Statement stmt = null;
    int updateCount = 0;

    try {
      stmt = conn.getStatement();
      updateCount = stmt.executeUpdate(strSql);
    } catch(Exception e){
      log4j.error("SQL error in query: " + strSql + "\nException:"+ e);
      throw new ServletException(e.toString());
    } finally {
      try {
        conn.releaseStatement(stmt);
      } catch(Exception ignored) {}
    }
    return(updateCount);
  }

  public static String preformatedColumn(String strValue, String type) {
    StringBuffer script = new StringBuffer();
    script.append("(CASE ").append(strValue).append(" WHEN NULL THEN 'NULL' ELSE ");
    if (strValue==null || strValue.length()==0) return "NULL";
    else if (type.equalsIgnoreCase("NUMBER")) script.append("TO_CHAR(" + strValue + ")");
    else if (type.equalsIgnoreCase("DATE")) script.append("TO_CHAR(" + strValue + ", '"+sqlDateFormat+" HH24:MI:SS')");
    else script.append(strValue);
    script.append(" END)");
    return script.toString();
  }

  public static String formatedColumn(String strValue, String type) {
    if (strValue==null || strValue.length()==0) return "NULL";
    else if (strValue.equalsIgnoreCase("NULL") || type.equalsIgnoreCase("NUMBER")) return strValue;
    else if (type.equalsIgnoreCase("DATE")) return "TO_DATE('" + strValue + "', '"+sqlDateFormat+" HH24:MI:SS')";
    else {
      strValue = strValue.replace("'", "''");
      strValue = strValue.replace("\r","' || chr(10) || '");
      strValue = strValue.replace("\n","' || chr(13) || '");
      return "'" + strValue + "'";
    }
  }

  public void readProperties(String strFileProperties) {
    //  Read properties file.
    Properties properties = new Properties();
    try {
      log4j.info("strFileProperties: " + strFileProperties);
      properties.load(new FileInputStream(strFileProperties));
      sqlDateFormat = properties.getProperty("dateFormat.sql");
      log4j.info("sqlDateFormat: " + sqlDateFormat);
    } catch (IOException e) { 
      // catch possible io errors from readLine()
      System.out.println("Uh oh, got an IOException error!");
      e.printStackTrace();
    }
  }

  public static void destroy() {
    myPool.destroy();
  }
}
