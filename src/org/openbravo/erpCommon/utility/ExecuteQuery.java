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
import org.openbravo.data.FieldProvider;
import java.util.Vector;
import java.sql.*;
import org.openbravo.data.UtilSql;
import org.apache.log4j.Logger;
import javax.servlet.ServletException;


public class ExecuteQuery {
  static Logger log4j = Logger.getLogger(ExecuteQuery.class);
  private ConnectionProvider pool;
  private Vector<String> parameters = new Vector<String>();
  private String sql;

  public ExecuteQuery() {
  }

  public ExecuteQuery(ConnectionProvider _conn, String _sql, Vector<String> _parameters) throws Exception {
    setPool(_conn);
    setSQL(_sql);
    setParameters(_parameters);
  }

  public void setPool(ConnectionProvider _conn) throws Exception {
    if (_conn==null) throw new Exception("The pool is null");
    this.pool = _conn;
  }
  
  public ConnectionProvider getPool() {
    return this.pool;
  }

  public void setSQL(String _sql) throws Exception {
    this.sql = ((_sql==null)?"":_sql);
  }

  public String getSQL() {
    return this.sql;
  }

  public void setParameters(Vector<String> _parameters) throws Exception {
    this.parameters = _parameters;
  }

  public Vector<String> getParameters() {
    return this.parameters;
  }

  public void addParameter(String _value) {
    if (this.parameters==null) this.parameters = new Vector<String>();
    if (_value==null || _value.equals("")) this.parameters.addElement("");
    else this.parameters.addElement(_value);
  }

  public String getParameter(int position) {
    if (this.parameters == null || this.parameters.size() < position) return "";
    else return this.parameters.elementAt(position);
  }

  public FieldProvider[] select(int startPosition, int rangeLength) throws ServletException {
    PreparedStatement st = null ;
    ResultSet result;
    Vector<SQLReturnObject> vector = new Vector<SQLReturnObject>(0);
    boolean hasRange = !(startPosition==0 && rangeLength==0);
    boolean hasRangeLimit = (rangeLength==0);
    if (hasRange) {
      if (getPool().getRDBMS().equalsIgnoreCase("ORACLE")) {
        addParameter(Integer.toString(startPosition));
        if (hasRangeLimit) addParameter(Integer.toString(startPosition+rangeLength));
      } else {
        if (hasRangeLimit) addParameter(Integer.toString(rangeLength));
        addParameter(Integer.toString(startPosition));
      }
    }  
    StringBuffer strSQL = new StringBuffer();
    if (hasRange) {
      strSQL.append(" SELECT * FROM (\n");
      if (getPool().getRDBMS().equalsIgnoreCase("ORACLE")) strSQL.append("SELECT ROWNUM AS rn1, A.* FROM (\n");
      strSQL.append(getSQL());
      strSQL.append(") A\n");
      if (getPool().getRDBMS().equalsIgnoreCase("ORACLE")) {
        strSQL.append(")  WHERE rn1 ");
        if (hasRangeLimit) strSQL.append("BETWEEN ? AND ?");
        else strSQL.append(">= ?");
      } else {
        if (hasRangeLimit) strSQL.append(" LIMIT TO_NUMBER(?)");
        strSQL.append(" OFFSET TO_NUMBER(?)");
      }
    } else strSQL.append(getSQL());
    if (log4j.isDebugEnabled()) log4j.debug("SQL: " + strSQL.toString());

    try {
      st = getPool().getPreparedStatement(strSQL.toString());
      Vector<String> params = getParameters();
      if (params!=null) {
        for (int iParameter=0;iParameter<params.size();iParameter++) {
          if (log4j.isDebugEnabled()) log4j.debug("PARAMETER " + iParameter + ":" + getParameter(iParameter));
          UtilSql.setValue(st, iParameter+1, 12, null, getParameter(iParameter));
        }
      }
      result = st.executeQuery();
         
      boolean first = true;
      int numColumns=0;
      int rowNum = 0;
      Vector<String> names = new Vector<String>(0);
      while(result.next()) {
        if (first) {
          ResultSetMetaData rmeta=result.getMetaData();
          numColumns=rmeta.getColumnCount();
          for (int i=1;i<=numColumns;i++) {
            names.addElement(rmeta.getColumnName(i));
          }
          first=false;
        }
        SQLReturnObject sqlReturnObject = new SQLReturnObject();
        for (int i=0;i<numColumns;i++) {
          sqlReturnObject.setData(names.elementAt(i), UtilSql.getValue(result, names.elementAt(i)));
        }
        vector.addElement(sqlReturnObject);
        rowNum++;
      }
      result.close();
    } catch(SQLException e){
      log4j.error("SQL error in query: " + strSQL.toString() + "Exception:"+ e);
      throw new ServletException("@CODE=" + Integer.toString(e.getErrorCode()) + "@" + e.getMessage());
    } catch(Exception ex){
      log4j.error("Exception in query: " + strSQL.toString() + "Exception:"+ ex);
      throw new ServletException("@CODE=@" + ex.getMessage());
    } finally {
      try {
        getPool().releasePreparedStatement(st);
      } catch (Exception ignore) {
        ignore.printStackTrace();
      }
    }
    FieldProvider objectListData[] = new FieldProvider[vector.size()];
    vector.copyInto(objectListData);
    return(objectListData);
  }


  public int executeStatement() throws ServletException {
    if (log4j.isDebugEnabled()) log4j.debug("SQL: " + getSQL());
    PreparedStatement st = null;
    int total = 0;

    try {
      st = getPool().getPreparedStatement(getSQL());
      Vector<String> params = getParameters();
      if (params!=null) {
        for (int iParameter=0;iParameter<params.size();iParameter++) {
          UtilSql.setValue(st, iParameter+1, 12, null, getParameter(iParameter));
        }
      }
      total = st.executeUpdate();
    } catch(SQLException e){
      log4j.error("SQLException:"+ e);
      throw new ServletException("@CODE=" + Integer.toString(e.getErrorCode()) + "@" + e.getMessage());
    } catch(Exception ex){
      log4j.error("Exception:"+ ex);
      throw new ServletException("@CODE=@" + ex.getMessage());
    } finally {
      try {
        getPool().releasePreparedStatement(st);
      } catch (Exception ignore) {}
    }
    return(total);
  }
}
