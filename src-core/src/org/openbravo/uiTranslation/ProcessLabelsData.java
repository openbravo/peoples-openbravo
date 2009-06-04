//Sqlc generated V1.O00-1
package org.openbravo.uiTranslation;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;

import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.openbravo.data.FieldProvider;
import org.openbravo.data.UtilSql;
import org.openbravo.database.ConnectionProvider;

class ProcessLabelsData implements FieldProvider {
  static Logger log4j = Logger.getLogger(ProcessLabelsData.class);
  private String InitRecordNumber = "0";
  public String adprocessid;
  public String processname;
  public String processparaid;
  public String processparaname;
  public String processparacolumnname;
  public String processparatrlname;

  public String getInitRecordNumber() {
    return InitRecordNumber;
  }

  public String getField(String fieldName) {
    if (fieldName.equalsIgnoreCase("ADPROCESSID"))
      return adprocessid;
    else if (fieldName.equalsIgnoreCase("PROCESSNAME"))
      return processname;
    else if (fieldName.equalsIgnoreCase("PROCESSPARAID"))
      return processparaid;
    else if (fieldName.equalsIgnoreCase("PROCESSPARANAME"))
      return processparaname;
    else if (fieldName.equalsIgnoreCase("PROCESSPARACOLUMNNAME"))
      return processparacolumnname;
    else if (fieldName.equalsIgnoreCase("PROCESSPARATRLNAME"))
      return processparatrlname;
    else {
      log4j.debug("Field does not exist: " + fieldName);
      return null;
    }
  }

  public static ProcessLabelsData[] select(ConnectionProvider connectionProvider,
      String ad_process_id, String language) throws ServletException {
    return select(connectionProvider, ad_process_id, language, 0, 0);
  }

  public static ProcessLabelsData[] select(ConnectionProvider connectionProvider,
      String ad_process_id, String language, String keyValue, String keyName, int numberRegisters)
      throws ServletException {
    boolean existsKey = false;
    String strSql = "";
    strSql = strSql + "		select " + "		  process.ad_process_id as adProcessId"
        + "		  , process.name as processName"
        + "		  , processPara.ad_process_para_id as processParaId"
        + "		  , processPara.name as processParaName"
        + "     , processPara.columnname as processParaColumnName"
        + "		  , processParaTrl.name as processParaTrlName" + "		from "
        + "		  ad_process process, ad_process_para processPara"
        + "		  , ad_process_para_trl processParaTrl" + "		where " + "		  process.ad_process_id = ?"
        + "		  and process.ad_process_id = processPara.ad_process_id"
        + "		  and processPara.ad_process_para_id = processParaTrl.ad_process_para_id"
        + "		  and processParaTrl.ad_language = ?";

    ResultSet result;
    Vector<Object> vector = new Vector<Object>(0);
    PreparedStatement st = null;

    int iParameter = 0;
    try {
      st = connectionProvider.getPreparedStatement(strSql);
      iParameter++;
      UtilSql.setValue(st, iParameter, 12, null, ad_process_id);
      iParameter++;
      UtilSql.setValue(st, iParameter, 12, null, language);

      result = st.executeQuery();
      long countRecord = 0;
      long initRecord = 0;
      boolean searchComplete = false;
      while (result.next() && !searchComplete) {
        countRecord++;
        ProcessLabelsData objectProcessLabelsData = new ProcessLabelsData();
        objectProcessLabelsData.adprocessid = UtilSql.getValue(result, "ADPROCESSID");
        objectProcessLabelsData.processname = UtilSql.getValue(result, "PROCESSNAME");
        objectProcessLabelsData.processparaid = UtilSql.getValue(result, "PROCESSPARAID");
        objectProcessLabelsData.processparaname = UtilSql.getValue(result, "PROCESSPARANAME");
        objectProcessLabelsData.processparacolumnname = UtilSql.getValue(result,
            "PROCESSPARACOLUMNNAME");
        objectProcessLabelsData.processparatrlname = UtilSql.getValue(result, "PROCESSPARATRLNAME");
        objectProcessLabelsData.InitRecordNumber = Long.toString(initRecord);
        if (!existsKey)
          existsKey = (objectProcessLabelsData.getField(keyName).equalsIgnoreCase(keyValue));
        vector.addElement(objectProcessLabelsData);
        if (countRecord == numberRegisters) {
          if (existsKey)
            searchComplete = true;
          else {
            countRecord = 0;
            initRecord += numberRegisters;
            vector.clear();
          }
        }
      }
      result.close();
    } catch (SQLException e) {
      log4j.error("SQL error in query: " + strSql + "Exception:" + e);
      throw new ServletException("@CODE=" + Integer.toString(e.getErrorCode()) + "@"
          + e.getMessage());
    } catch (Exception ex) {
      log4j.error("Exception in query: " + strSql + "Exception:" + ex);
      throw new ServletException("@CODE=@" + ex.getMessage());
    } finally {
      try {
        connectionProvider.releasePreparedStatement(st);
      } catch (Exception ignore) {
        ignore.printStackTrace();
      }
    }
    if (existsKey) {
      ProcessLabelsData objectProcessLabelsData[] = new ProcessLabelsData[vector.size()];
      vector.copyInto(objectProcessLabelsData);
      return (objectProcessLabelsData);
    }
    return (new ProcessLabelsData[0]);
  }

  public static ProcessLabelsData[] select(ConnectionProvider connectionProvider,
      String ad_process_id, String language, int firstRegister, int numberRegisters)
      throws ServletException {
    String strSql = "";
    strSql = strSql + "		select " + "		  process.ad_process_id as adProcessId"
        + "		  , process.name as processName"
        + "		  , processPara.ad_process_para_id as processParaId"
        + "		  , processPara.name as processParaName"
        + "     , processPara.columnname as processParaColumnName"
        + "		  , processParaTrl.name as processParaTrlName" + "		from "
        + "		  ad_process process, ad_process_para processPara"
        + "		  , ad_process_para_trl processParaTrl" + "		where " + "		  process.ad_process_id = ?"
        + "		  and process.ad_process_id = processPara.ad_process_id"
        + "		  and processPara.ad_process_para_id = processParaTrl.ad_process_para_id"
        + "		  and processParaTrl.ad_language = ?";

    ResultSet result;
    Vector<Object> vector = new Vector<Object>(0);
    PreparedStatement st = null;

    int iParameter = 0;
    try {
      st = connectionProvider.getPreparedStatement(strSql);
      iParameter++;
      UtilSql.setValue(st, iParameter, 12, null, ad_process_id);
      iParameter++;
      UtilSql.setValue(st, iParameter, 12, null, language);

      result = st.executeQuery();
      long countRecord = 0;
      long countRecordSkip = 1;
      boolean continueResult = true;
      while (countRecordSkip < firstRegister && continueResult) {
        continueResult = result.next();
        countRecordSkip++;
      }
      while (continueResult && result.next()) {
        countRecord++;
        ProcessLabelsData objectProcessLabelsData = new ProcessLabelsData();
        objectProcessLabelsData.adprocessid = UtilSql.getValue(result, "ADPROCESSID");
        objectProcessLabelsData.processname = UtilSql.getValue(result, "PROCESSNAME");
        objectProcessLabelsData.processparaid = UtilSql.getValue(result, "PROCESSPARAID");
        objectProcessLabelsData.processparaname = UtilSql.getValue(result, "PROCESSPARANAME");
        objectProcessLabelsData.processparacolumnname = UtilSql.getValue(result,
            "PROCESSPARACOLUMNNAME");
        objectProcessLabelsData.processparatrlname = UtilSql.getValue(result, "PROCESSPARATRLNAME");
        objectProcessLabelsData.InitRecordNumber = Integer.toString(firstRegister);
        vector.addElement(objectProcessLabelsData);
        if (countRecord >= numberRegisters && numberRegisters != 0) {
          continueResult = false;
        }
      }
      result.close();
    } catch (SQLException e) {
      log4j.error("SQL error in query: " + strSql + "Exception:" + e);
      throw new ServletException("@CODE=" + Integer.toString(e.getErrorCode()) + "@"
          + e.getMessage());
    } catch (Exception ex) {
      log4j.error("Exception in query: " + strSql + "Exception:" + ex);
      throw new ServletException("@CODE=@" + ex.getMessage());
    } finally {
      try {
        connectionProvider.releasePreparedStatement(st);
      } catch (Exception ignore) {
        ignore.printStackTrace();
      }
    }
    ProcessLabelsData objectProcessLabelsData[] = new ProcessLabelsData[vector.size()];
    vector.copyInto(objectProcessLabelsData);
    return (objectProcessLabelsData);
  }

  public static ProcessLabelsData[] selectOriginalParameters(ConnectionProvider connectionProvider,
      String ad_process_id) throws ServletException {
    return selectOriginalParameters(connectionProvider, ad_process_id, 0, 0);
  }

  public static ProcessLabelsData[] selectOriginalParameters(ConnectionProvider connectionProvider,
      String ad_process_id, String keyValue, String keyName, int numberRegisters)
      throws ServletException {
    boolean existsKey = false;
    String strSql = "";
    strSql = strSql + "		select " + "		  processPara.ad_process_para_id as processParaId   "
        + "		  , processPara.name as processParaName "
        + "     , processPara.columnname as processParaColumnName " + "		from "
        + "		  ad_process_para processPara " + "		where " + "		  processPara.ad_process_id = ? ";

    ResultSet result;
    Vector<Object> vector = new Vector<Object>(0);
    PreparedStatement st = null;

    int iParameter = 0;
    try {
      st = connectionProvider.getPreparedStatement(strSql);
      iParameter++;
      UtilSql.setValue(st, iParameter, 12, null, ad_process_id);

      result = st.executeQuery();
      long countRecord = 0;
      long initRecord = 0;
      boolean searchComplete = false;
      while (result.next() && !searchComplete) {
        countRecord++;
        ProcessLabelsData objectProcessLabelsData = new ProcessLabelsData();
        objectProcessLabelsData.processparaid = UtilSql.getValue(result, "PROCESSPARAID");
        objectProcessLabelsData.processparaname = UtilSql.getValue(result, "PROCESSPARANAME");
        objectProcessLabelsData.processparacolumnname = UtilSql.getValue(result,
            "PROCESSPARACOLUMNNAME");
        objectProcessLabelsData.InitRecordNumber = Long.toString(initRecord);
        if (!existsKey)
          existsKey = (objectProcessLabelsData.getField(keyName).equalsIgnoreCase(keyValue));
        vector.addElement(objectProcessLabelsData);
        if (countRecord == numberRegisters) {
          if (existsKey)
            searchComplete = true;
          else {
            countRecord = 0;
            initRecord += numberRegisters;
            vector.clear();
          }
        }
      }
      result.close();
    } catch (SQLException e) {
      log4j.error("SQL error in query: " + strSql + "Exception:" + e);
      throw new ServletException("@CODE=" + Integer.toString(e.getErrorCode()) + "@"
          + e.getMessage());
    } catch (Exception ex) {
      log4j.error("Exception in query: " + strSql + "Exception:" + ex);
      throw new ServletException("@CODE=@" + ex.getMessage());
    } finally {
      try {
        connectionProvider.releasePreparedStatement(st);
      } catch (Exception ignore) {
        ignore.printStackTrace();
      }
    }
    if (existsKey) {
      ProcessLabelsData objectProcessLabelsData[] = new ProcessLabelsData[vector.size()];
      vector.copyInto(objectProcessLabelsData);
      return (objectProcessLabelsData);
    }
    return (new ProcessLabelsData[0]);
  }

  public static ProcessLabelsData[] selectOriginalParameters(ConnectionProvider connectionProvider,
      String ad_process_id, int firstRegister, int numberRegisters) throws ServletException {
    String strSql = "";
    strSql = strSql + "		select " + "		  processPara.ad_process_para_id as processParaId "
        + "		  , processPara.name as processParaName "
        + "     , processPara.columnname as processParaColumnName  " + "		from "
        + "		  ad_process_para processPara " + "		where " + "		  processPara.ad_process_id = ? ";

    ResultSet result;
    Vector<Object> vector = new Vector<Object>(0);
    PreparedStatement st = null;

    int iParameter = 0;
    try {
      st = connectionProvider.getPreparedStatement(strSql);
      iParameter++;
      UtilSql.setValue(st, iParameter, 12, null, ad_process_id);

      result = st.executeQuery();
      long countRecord = 0;
      long countRecordSkip = 1;
      boolean continueResult = true;
      while (countRecordSkip < firstRegister && continueResult) {
        continueResult = result.next();
        countRecordSkip++;
      }
      while (continueResult && result.next()) {
        countRecord++;
        ProcessLabelsData objectProcessLabelsData = new ProcessLabelsData();
        objectProcessLabelsData.processparaid = UtilSql.getValue(result, "PROCESSPARAID");
        objectProcessLabelsData.processparaname = UtilSql.getValue(result, "PROCESSPARANAME");
        objectProcessLabelsData.processparacolumnname = UtilSql.getValue(result,
            "PROCESSPARACOLUMNNAME");
        objectProcessLabelsData.InitRecordNumber = Integer.toString(firstRegister);
        vector.addElement(objectProcessLabelsData);
        if (countRecord >= numberRegisters && numberRegisters != 0) {
          continueResult = false;
        }
      }
      result.close();
    } catch (SQLException e) {
      log4j.error("SQL error in query: " + strSql + "Exception:" + e);
      throw new ServletException("@CODE=" + Integer.toString(e.getErrorCode()) + "@"
          + e.getMessage());
    } catch (Exception ex) {
      log4j.error("Exception in query: " + strSql + "Exception:" + ex);
      throw new ServletException("@CODE=@" + ex.getMessage());
    } finally {
      try {
        connectionProvider.releasePreparedStatement(st);
      } catch (Exception ignore) {
        ignore.printStackTrace();
      }
    }
    ProcessLabelsData objectProcessLabelsData[] = new ProcessLabelsData[vector.size()];
    vector.copyInto(objectProcessLabelsData);
    return (objectProcessLabelsData);
  }

  public static ProcessLabelsData[] selectTranslatedParameters(
      ConnectionProvider connectionProvider, String ad_process_id, String language)
      throws ServletException {
    return selectTranslatedParameters(connectionProvider, ad_process_id, language, 0, 0);
  }

  public static ProcessLabelsData[] selectTranslatedParameters(
      ConnectionProvider connectionProvider, String ad_process_id, String language,
      String keyValue, String keyName, int numberRegisters) throws ServletException {
    boolean existsKey = false;
    String strSql = "";
    strSql = strSql + "		select " + "		  processPara.ad_process_para_id as processParaId"
        + "		  , processPara.name as processParaName"
        + "     , processPara.columnname as processParaColumnName"
        + "		  , processParaTrl.name as processParaTrlName" + "		from "
        + "		  ad_process_para processPara" + "		  , ad_process_para_trl processParaTrl"
        + "		where " + "		  processPara.ad_process_id = ?"
        + "		  and processPara.ad_process_para_id = processParaTrl.ad_process_para_id"
        + "		  and processParaTrl.ad_language = ?";

    ResultSet result;
    Vector<Object> vector = new Vector<Object>(0);
    PreparedStatement st = null;

    int iParameter = 0;
    try {
      st = connectionProvider.getPreparedStatement(strSql);
      iParameter++;
      UtilSql.setValue(st, iParameter, 12, null, ad_process_id);
      iParameter++;
      UtilSql.setValue(st, iParameter, 12, null, language);

      result = st.executeQuery();
      long countRecord = 0;
      long initRecord = 0;
      boolean searchComplete = false;
      while (result.next() && !searchComplete) {
        countRecord++;
        ProcessLabelsData objectProcessLabelsData = new ProcessLabelsData();
        objectProcessLabelsData.processparaid = UtilSql.getValue(result, "PROCESSPARAID");
        objectProcessLabelsData.processparaname = UtilSql.getValue(result, "PROCESSPARANAME");
        objectProcessLabelsData.processparacolumnname = UtilSql.getValue(result,
            "PROCESSPARACOLUMNNAME");
        objectProcessLabelsData.processparatrlname = UtilSql.getValue(result, "PROCESSPARATRLNAME");
        objectProcessLabelsData.InitRecordNumber = Long.toString(initRecord);
        if (!existsKey)
          existsKey = (objectProcessLabelsData.getField(keyName).equalsIgnoreCase(keyValue));
        vector.addElement(objectProcessLabelsData);
        if (countRecord == numberRegisters) {
          if (existsKey)
            searchComplete = true;
          else {
            countRecord = 0;
            initRecord += numberRegisters;
            vector.clear();
          }
        }
      }
      result.close();
    } catch (SQLException e) {
      log4j.error("SQL error in query: " + strSql + "Exception:" + e);
      throw new ServletException("@CODE=" + Integer.toString(e.getErrorCode()) + "@"
          + e.getMessage());
    } catch (Exception ex) {
      log4j.error("Exception in query: " + strSql + "Exception:" + ex);
      throw new ServletException("@CODE=@" + ex.getMessage());
    } finally {
      try {
        connectionProvider.releasePreparedStatement(st);
      } catch (Exception ignore) {
        ignore.printStackTrace();
      }
    }
    if (existsKey) {
      ProcessLabelsData objectProcessLabelsData[] = new ProcessLabelsData[vector.size()];
      vector.copyInto(objectProcessLabelsData);
      return (objectProcessLabelsData);
    }
    return (new ProcessLabelsData[0]);
  }

  public static ProcessLabelsData[] selectTranslatedParameters(
      ConnectionProvider connectionProvider, String ad_process_id, String language,
      int firstRegister, int numberRegisters) throws ServletException {
    String strSql = "";
    strSql = strSql + "		select " + "		  processPara.ad_process_para_id as processParaId"
        + "		  , processPara.name as processParaName"
        + "     , processPara.columnname as processParaColumnName"
        + "		  , processParaTrl.name as processParaTrlName" + "		from "
        + "		  ad_process_para processPara" + "		  , ad_process_para_trl processParaTrl"
        + "		where " + "		  processPara.ad_process_id = ?"
        + "		  and processPara.ad_process_para_id = processParaTrl.ad_process_para_id"
        + "		  and processParaTrl.ad_language = ?";

    ResultSet result;
    Vector<Object> vector = new Vector<Object>(0);
    PreparedStatement st = null;

    int iParameter = 0;
    try {
      st = connectionProvider.getPreparedStatement(strSql);
      iParameter++;
      UtilSql.setValue(st, iParameter, 12, null, ad_process_id);
      iParameter++;
      UtilSql.setValue(st, iParameter, 12, null, language);

      result = st.executeQuery();
      long countRecord = 0;
      long countRecordSkip = 1;
      boolean continueResult = true;
      while (countRecordSkip < firstRegister && continueResult) {
        continueResult = result.next();
        countRecordSkip++;
      }
      while (continueResult && result.next()) {
        countRecord++;
        ProcessLabelsData objectProcessLabelsData = new ProcessLabelsData();
        objectProcessLabelsData.processparaid = UtilSql.getValue(result, "PROCESSPARAID");
        objectProcessLabelsData.processparaname = UtilSql.getValue(result, "PROCESSPARANAME");
        objectProcessLabelsData.processparacolumnname = UtilSql.getValue(result,
            "PROCESSPARACOLUMNNAME");
        objectProcessLabelsData.processparatrlname = UtilSql.getValue(result, "PROCESSPARATRLNAME");
        objectProcessLabelsData.InitRecordNumber = Integer.toString(firstRegister);
        vector.addElement(objectProcessLabelsData);
        if (countRecord >= numberRegisters && numberRegisters != 0) {
          continueResult = false;
        }
      }
      result.close();
    } catch (SQLException e) {
      log4j.error("SQL error in query: " + strSql + "Exception:" + e);
      throw new ServletException("@CODE=" + Integer.toString(e.getErrorCode()) + "@"
          + e.getMessage());
    } catch (Exception ex) {
      log4j.error("Exception in query: " + strSql + "Exception:" + ex);
      throw new ServletException("@CODE=@" + ex.getMessage());
    } finally {
      try {
        connectionProvider.releasePreparedStatement(st);
      } catch (Exception ignore) {
        ignore.printStackTrace();
      }
    }
    ProcessLabelsData objectProcessLabelsData[] = new ProcessLabelsData[vector.size()];
    vector.copyInto(objectProcessLabelsData);
    return (objectProcessLabelsData);
  }
}
