//Sqlc generated V1.O00-1
package org.openbravo.uiTranslation;

import java.sql.*;

import org.apache.log4j.Logger;

import javax.servlet.ServletException;

import org.openbravo.data.FieldProvider;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.data.UtilSql;
import java.util.*;

public class FieldLabelsData implements FieldProvider {
static Logger log4j = Logger.getLogger(FieldLabelsData.class);
  private String InitRecordNumber="0";
  public String adColumnId;
  public String colName;
  public String colColumnname;
  public String elementName;
  public String elementPrintname;
  public String fieldName;
  public String fieldtrlName;
  public String elmttrlName;
  public String elmttrlPrintname;

  public String getInitRecordNumber() {
    return InitRecordNumber;
  }

  public String getField(String fieldName) {
    if (fieldName.equalsIgnoreCase("AD_COLUMN_ID") || fieldName.equals("adColumnId"))
      return adColumnId;
    else if (fieldName.equalsIgnoreCase("COL_NAME") || fieldName.equals("colName"))
      return colName;
    else if (fieldName.equalsIgnoreCase("COL_COLUMNNAME") || fieldName.equals("colColumnname"))
      return colColumnname;
    else if (fieldName.equalsIgnoreCase("ELEMENT_NAME") || fieldName.equals("elementName"))
      return elementName;
    else if (fieldName.equalsIgnoreCase("ELEMENT_PRINTNAME") || fieldName.equals("elementPrintname"))
      return elementPrintname;
    else if (fieldName.equalsIgnoreCase("FIELD_NAME") || fieldName.equals("fieldName"))
      return fieldName;
    else if (fieldName.equalsIgnoreCase("FIELDTRL_NAME") || fieldName.equals("fieldtrlName"))
      return fieldtrlName;
    else if (fieldName.equalsIgnoreCase("ELMTTRL_NAME") || fieldName.equals("elmttrlName"))
      return elmttrlName;
    else if (fieldName.equalsIgnoreCase("ELMTTRL_PRINTNAME") || fieldName.equals("elmttrlPrintname"))
      return elmttrlPrintname;
   else {
     log4j.debug("Field does not exist: " + fieldName);
     return null;
   }
 }

  public static FieldLabelsData[] select(ConnectionProvider connectionProvider, String ad_tab_id, String language)    throws ServletException {
    return select(connectionProvider, ad_tab_id, language, 0, 0);
  }

  public static FieldLabelsData[] select(ConnectionProvider connectionProvider, String ad_tab_id, String language, String keyValue, String keyName, int numberRegisters)    throws ServletException {
    boolean existsKey = false;
    String strSql = "";
    strSql = strSql + 
      "      select " +
      "   	colum.ad_column_id as AD_COLUMN_ID " +
      "	  , colum.name as COL_NAME " +
      " 	, colum.columnname as COL_COLUMNNAME " +
      "	  , elemnt.name as ELEMENT_NAME " +
      "	  , elemnt.printname as ELEMENT_PRINTNAME " +
      "	  , field.name as FIELD_NAME " +
      "	  , fieldTrl.name as FIELDTRL_NAME" +
      "	  , elmtTrl.name as ELMTTRL_NAME " +
      "	  , elmtTrl.printname as ELMTTRL_PRINTNAME " +
      " from " +
      "	  ad_column colum" +
      "	  , ad_field field  " +
      "	  , ad_field_trl fieldTrl" +
      "	  , ad_element elemnt " +
      "	  , AD_ELEMENT_TRL elmtTrl " +
      " where " +
      "  	colum.ad_table_id = (select tab.ad_table_id from ad_tab tab where tab.ad_tab_id = ?) " +
      "	  and colum.ad_column_id = field.ad_column_id" +
      "	  and colum.ad_element_id = elemnt.ad_element_id " +
      "	  and ((field.ad_field_id = fieldTrl.ad_field_id AND fieldTrl.ad_language = ?) or fieldTrl.ad_field_trl_id is null)" +
      "	  and ((elemnt.ad_element_id = elmtTrl.ad_element_id AND elmtTrl.ad_language = ?) or elmtTrl.ad_element_trl_id is null)" +
      "	  and elemnt.isactive = 'Y'" +
      "	  and fieldTrl.isactive = 'Y'" +
      " UNION " +
      " select " +
      "   colum.ad_column_id as AD_COLUMN_ID " +
      "	  , colum.name as COL_NAME " +
      "	  , colum.columnname as COL_COLUMNNAME " +
      "	  , elemnt.name as ELEMENT_NAME " +
      "	  , elemnt.printname as ELEMENT_PRINTNAME " +
      "	  , elemnt.name as FIELD_NAME " +
      "	  , elmtTrl.name as FIELDTRL_NAME" +
      "	  , elmtTrl.name as ELMTTRL_NAME " +
      "	  , elmtTrl.printname as ELMTTRL_PRINTNAME " +
      " from " +
      "	  ad_column colum " +
      "        , ad_element elemnt " +
      "        , ad_element_trl elmtTrl " +
      " where " +
      "  colum.ad_table_id = (select tab.ad_table_id from ad_tab tab where tab.ad_tab_id = ?) " +
      "  and ((elemnt.ad_element_id = elmtTrl.ad_element_id AND elmtTrl.ad_language = ?) or elmtTrl.ad_element_trl_id is null) " +
      "  and colum.ad_element_id = elemnt.ad_element_id " +
      "  and colum.ad_column_id not in ( " +
      "    select " +
      "      field.ad_column_id " +
      "    from " +
      "      ad_column colum " +
      "      , ad_field field " +
      "    where " +
      "      field.ad_tab_id = ? " +
      "      and colum.ad_column_id = field.ad_column_id)";

    ResultSet result;
    Vector<java.lang.Object> vector = new Vector<java.lang.Object>(0);
    PreparedStatement st = null;

    int iParameter = 0;
    try {
    st = connectionProvider.getPreparedStatement(strSql);
      iParameter++; UtilSql.setValue(st, iParameter, 12, null, ad_tab_id);
      iParameter++; UtilSql.setValue(st, iParameter, 12, null, language);
      iParameter++; UtilSql.setValue(st, iParameter, 12, null, language);
      iParameter++; UtilSql.setValue(st, iParameter, 12, null, ad_tab_id);
      iParameter++; UtilSql.setValue(st, iParameter, 12, null, language);
      iParameter++; UtilSql.setValue(st, iParameter, 12, null, ad_tab_id);

      result = st.executeQuery();
      long countRecord = 0;
      long initRecord = 0;
      boolean searchComplete = false;
      while(result.next() && !searchComplete) {
        countRecord++;
        FieldLabelsData objectFieldLabelsData = new FieldLabelsData();
        objectFieldLabelsData.adColumnId = UtilSql.getValue(result, "AD_COLUMN_ID");
        objectFieldLabelsData.colName = UtilSql.getValue(result, "COL_NAME");
        objectFieldLabelsData.colColumnname = UtilSql.getValue(result, "COL_COLUMNNAME");
        objectFieldLabelsData.elementName = UtilSql.getValue(result, "ELEMENT_NAME");
        objectFieldLabelsData.elementPrintname = UtilSql.getValue(result, "ELEMENT_PRINTNAME");
        objectFieldLabelsData.fieldName = UtilSql.getValue(result, "FIELD_NAME");
        objectFieldLabelsData.fieldtrlName = UtilSql.getValue(result, "FIELDTRL_NAME");
        objectFieldLabelsData.elmttrlName = UtilSql.getValue(result, "ELMTTRL_NAME");
        objectFieldLabelsData.elmttrlPrintname = UtilSql.getValue(result, "ELMTTRL_PRINTNAME");
        objectFieldLabelsData.InitRecordNumber = Long.toString(initRecord);
        if (!existsKey) existsKey = (objectFieldLabelsData.getField(keyName).equalsIgnoreCase(keyValue));
        vector.addElement(objectFieldLabelsData);
        if (countRecord == numberRegisters) {
          if (existsKey) searchComplete=true;
          else {
            countRecord = 0;
            initRecord += numberRegisters;
            vector.clear();
          }
        }
      }
      result.close();
    } catch(SQLException e){
      log4j.error("SQL error in query: " + strSql + "Exception:"+ e);
      throw new ServletException("@CODE=" + Integer.toString(e.getErrorCode()) + "@" + e.getMessage());
    } catch(Exception ex){
      log4j.error("Exception in query: " + strSql + "Exception:"+ ex);
      throw new ServletException("@CODE=@" + ex.getMessage());
    } finally {
      try {
        connectionProvider.releasePreparedStatement(st);
      } catch(Exception ignore){
        ignore.printStackTrace();
      }
    }
    if (existsKey) {
      FieldLabelsData objectFieldLabelsData[] = new FieldLabelsData[vector.size()];
      vector.copyInto(objectFieldLabelsData);
      return(objectFieldLabelsData);
    }
    return(new FieldLabelsData[0]);
  }

  public static FieldLabelsData[] select(ConnectionProvider connectionProvider, String ad_tab_id, String language, int firstRegister, int numberRegisters)    throws ServletException {
    String strSql = "";
    strSql = strSql + 
      " select " +
      "  	colum.ad_column_id as AD_COLUMN_ID " +
      "	  , colum.name as COL_NAME " +
      "	  , colum.columnname as COL_COLUMNNAME " +
      "	  , elemnt.name as ELEMENT_NAME " +
      "	  , elemnt.printname as ELEMENT_PRINTNAME " +
      "	  , field.name as FIELD_NAME " +
      "	  , fieldTrl.name as FIELDTRL_NAME" +
      "	  , elmtTrl.name as ELMTTRL_NAME " +
      "	  , elmtTrl.printname as ELMTTRL_PRINTNAME " +
      " from " +
      "	  ad_column colum" +
      "	  , ad_field field  " +
      "	  , ad_field_trl fieldTrl" +
      "	  , ad_element elemnt " +
      "	  , AD_ELEMENT_TRL elmtTrl " +
      " where " +
      "  	colum.ad_table_id = (select tab.ad_table_id from ad_tab tab where tab.ad_tab_id = ?) " +
      "	  and colum.ad_column_id = field.ad_column_id" +
      " 	and colum.ad_element_id = elemnt.ad_element_id " +
      "	  and ((field.ad_field_id = fieldTrl.ad_field_id AND fieldTrl.ad_language = ?) or fieldTrl.ad_field_trl_id is null)" +
      " 	and ((elemnt.ad_element_id = elmtTrl.ad_element_id AND elmtTrl.ad_language = ?) or elmtTrl.ad_element_trl_id is null)" +
      "	  and elemnt.isactive = 'Y'" +
      " 	and fieldTrl.isactive = 'Y'" +
      " UNION " +
      "  select " +
      "   colum.ad_column_id as AD_COLUMN_ID " +
      "	  , colum.name as COL_NAME " +
      "	  , colum.columnname as COL_COLUMNNAME " +
      "	  , elemnt.name as ELEMENT_NAME " +
      "	  , elemnt.printname as ELEMENT_PRINTNAME " +
      "	  , elemnt.name as FIELD_NAME " +
      "	  , elmtTrl.name as FIELDTRL_NAME" +
      "	  , elmtTrl.name as ELMTTRL_NAME " +
      "	  , elmtTrl.printname as ELMTTRL_PRINTNAME " +
      " from " +
      "	  ad_column colum" +
      "        , ad_element elemnt" +
      "        , ad_element_trl elmtTrl" +
      " where " +
      "  colum.ad_table_id = (select tab.ad_table_id from ad_tab tab where tab.ad_tab_id = ?) " +
      "  and ((elemnt.ad_element_id = elmtTrl.ad_element_id AND elmtTrl.ad_language = ?) or elmtTrl.ad_element_trl_id is null)" +
      "  and colum.ad_element_id = elemnt.ad_element_id " +
      "  and colum.ad_column_id not in (" +
      "    select " +
      "      field.ad_column_id" +
      "    from " +
      "      ad_column colum" +
      "      , ad_field field" +
      "    where " +
      "      field.ad_tab_id = ?" +
      "      and colum.ad_column_id = field.ad_column_id)";

    ResultSet result;
    Vector<java.lang.Object> vector = new Vector<java.lang.Object>(0);
    PreparedStatement st = null;

    int iParameter = 0;
    try {
    st = connectionProvider.getPreparedStatement(strSql);
      iParameter++; UtilSql.setValue(st, iParameter, 12, null, ad_tab_id);
      iParameter++; UtilSql.setValue(st, iParameter, 12, null, language);
      iParameter++; UtilSql.setValue(st, iParameter, 12, null, language);
      iParameter++; UtilSql.setValue(st, iParameter, 12, null, ad_tab_id);
      iParameter++; UtilSql.setValue(st, iParameter, 12, null, language);
      iParameter++; UtilSql.setValue(st, iParameter, 12, null, ad_tab_id);

      result = st.executeQuery();
      long countRecord = 0;
      long countRecordSkip = 1;
      boolean continueResult = true;
      while(countRecordSkip < firstRegister && continueResult) {
        continueResult = result.next();
        countRecordSkip++;
      }
      while(continueResult && result.next()) {
        countRecord++;
        FieldLabelsData objectFieldLabelsData = new FieldLabelsData();
        objectFieldLabelsData.adColumnId = UtilSql.getValue(result, "AD_COLUMN_ID");
        objectFieldLabelsData.colName = UtilSql.getValue(result, "COL_NAME");
        objectFieldLabelsData.colColumnname = UtilSql.getValue(result, "COL_COLUMNNAME");
        objectFieldLabelsData.elementName = UtilSql.getValue(result, "ELEMENT_NAME");
        objectFieldLabelsData.elementPrintname = UtilSql.getValue(result, "ELEMENT_PRINTNAME");
        objectFieldLabelsData.fieldName = UtilSql.getValue(result, "FIELD_NAME");
        objectFieldLabelsData.fieldtrlName = UtilSql.getValue(result, "FIELDTRL_NAME");
        objectFieldLabelsData.elmttrlName = UtilSql.getValue(result, "ELMTTRL_NAME");
        objectFieldLabelsData.elmttrlPrintname = UtilSql.getValue(result, "ELMTTRL_PRINTNAME");
        objectFieldLabelsData.InitRecordNumber = Integer.toString(firstRegister);
        vector.addElement(objectFieldLabelsData);
        if (countRecord >= numberRegisters && numberRegisters != 0) {
          continueResult = false;
        }
      }
      result.close();
    } catch(SQLException e){
      log4j.error("SQL error in query: " + strSql + "Exception:"+ e);
      throw new ServletException("@CODE=" + Integer.toString(e.getErrorCode()) + "@" + e.getMessage());
    } catch(Exception ex){
      log4j.error("Exception in query: " + strSql + "Exception:"+ ex);
      throw new ServletException("@CODE=@" + ex.getMessage());
    } finally {
      try {
        connectionProvider.releasePreparedStatement(st);
      } catch(Exception ignore){
        ignore.printStackTrace();
      }
    }
    FieldLabelsData objectFieldLabelsData[] = new FieldLabelsData[vector.size()];
    vector.copyInto(objectFieldLabelsData);
    return(objectFieldLabelsData);
  }

  public static FieldLabelsData[] selectModuleFieldLabels(ConnectionProvider connectionProvider, String ad_tab_id)    throws ServletException {
    return selectModuleFieldLabels(connectionProvider, ad_tab_id, 0, 0);
  }

  public static FieldLabelsData[] selectModuleFieldLabels(ConnectionProvider connectionProvider, String ad_tab_id, String keyValue, String keyName, int numberRegisters)    throws ServletException {
    boolean existsKey = false;
    String strSql = "";
    strSql = strSql + 
      "      select " +
      "					colum.ad_column_id as AD_COLUMN_ID " +
      "					, colum.name as COL_NAME " +
      "					, colum.columnname as COL_COLUMNNAME " +
      "					, elemnt.name as ELEMENT_NAME " +
      "					, elemnt.printname as ELEMENT_PRINTNAME " +
      "					, field.name as FIELD_NAME " +
      "				from " +
      "					ad_column colum" +
      "					, ad_field field  " +
      "					, ad_element elemnt " +
      "				where " +
      "				colum.ad_table_id = (select tab.ad_table_id from ad_tab tab where tab.ad_tab_id = ?) " +
      "					and colum.ad_column_id = field.ad_column_id" +
      "					and colum.ad_element_id = elemnt.ad_element_id " +
      "					and elemnt.isactive = 'Y'" +
      "			UNION" +
      "				select " +
      "				  colum.ad_column_id as AD_COLUMN_ID " +
      "					, colum.name as COL_NAME " +
      "					, colum.columnname as COL_COLUMNNAME " +
      "					, elemnt.name as ELEMENT_NAME " +
      "					, elemnt.printname as ELEMENT_PRINTNAME " +
      "					, elemnt.printname as FIELD_NAME " +
      "				from " +
      "					ad_column colum" +
      "				        , ad_element elemnt" +
      "				where " +
      "				  colum.ad_table_id = (select tab.ad_table_id from ad_tab tab where tab.ad_tab_id = ?) " +
      "				  and colum.ad_element_id = elemnt.ad_element_id " +
      "				  and colum.ad_column_id not in (" +
      "				    select " +
      "				      field.ad_column_id" +
      "				    from " +
      "				      ad_column colum" +
      "				      , ad_field field" +
      "				    where " +
      "				      field.ad_tab_id = ?" +
      "				      and colum.ad_column_id = field.ad_column_id)";

    ResultSet result;
    Vector<java.lang.Object> vector = new Vector<java.lang.Object>(0);
    PreparedStatement st = null;

    int iParameter = 0;
    try {
    st = connectionProvider.getPreparedStatement(strSql);
      iParameter++; UtilSql.setValue(st, iParameter, 12, null, ad_tab_id);
      iParameter++; UtilSql.setValue(st, iParameter, 12, null, ad_tab_id);
      iParameter++; UtilSql.setValue(st, iParameter, 12, null, ad_tab_id);

      result = st.executeQuery();
      long countRecord = 0;
      long initRecord = 0;
      boolean searchComplete = false;
      while(result.next() && !searchComplete) {
        countRecord++;
        FieldLabelsData objectFieldLabelsData = new FieldLabelsData();
        objectFieldLabelsData.adColumnId = UtilSql.getValue(result, "AD_COLUMN_ID");
        objectFieldLabelsData.colName = UtilSql.getValue(result, "COL_NAME");
        objectFieldLabelsData.colColumnname = UtilSql.getValue(result, "COL_COLUMNNAME");
        objectFieldLabelsData.elementName = UtilSql.getValue(result, "ELEMENT_NAME");
        objectFieldLabelsData.elementPrintname = UtilSql.getValue(result, "ELEMENT_PRINTNAME");
        objectFieldLabelsData.fieldName = UtilSql.getValue(result, "FIELD_NAME");
        objectFieldLabelsData.InitRecordNumber = Long.toString(initRecord);
        if (!existsKey) existsKey = (objectFieldLabelsData.getField(keyName).equalsIgnoreCase(keyValue));
        vector.addElement(objectFieldLabelsData);
        if (countRecord == numberRegisters) {
          if (existsKey) searchComplete=true;
          else {
            countRecord = 0;
            initRecord += numberRegisters;
            vector.clear();
          }
        }
      }
      result.close();
    } catch(SQLException e){
      log4j.error("SQL error in query: " + strSql + "Exception:"+ e);
      throw new ServletException("@CODE=" + Integer.toString(e.getErrorCode()) + "@" + e.getMessage());
    } catch(Exception ex){
      log4j.error("Exception in query: " + strSql + "Exception:"+ ex);
      throw new ServletException("@CODE=@" + ex.getMessage());
    } finally {
      try {
        connectionProvider.releasePreparedStatement(st);
      } catch(Exception ignore){
        ignore.printStackTrace();
      }
    }
    if (existsKey) {
      FieldLabelsData objectFieldLabelsData[] = new FieldLabelsData[vector.size()];
      vector.copyInto(objectFieldLabelsData);
      return(objectFieldLabelsData);
    }
    return(new FieldLabelsData[0]);
  }

  public static FieldLabelsData[] selectModuleFieldLabels(ConnectionProvider connectionProvider, String ad_tab_id, int firstRegister, int numberRegisters)    throws ServletException {
    String strSql = "";
    strSql = strSql + 
      "      select " +
      "					colum.ad_column_id as AD_COLUMN_ID " +
      "					, colum.name as COL_NAME " +
      "					, colum.columnname as COL_COLUMNNAME " +
      "					, elemnt.name as ELEMENT_NAME " +
      "					, elemnt.printname as ELEMENT_PRINTNAME " +
      "					, field.name as FIELD_NAME " +
      "				from " +
      "					ad_column colum" +
      "					, ad_field field  " +
      "					, ad_element elemnt " +
      "				where " +
      "				colum.ad_table_id = (select tab.ad_table_id from ad_tab tab where tab.ad_tab_id = ?) " +
      "					and colum.ad_column_id = field.ad_column_id" +
      "					and colum.ad_element_id = elemnt.ad_element_id " +
      "					and elemnt.isactive = 'Y'" +
      "			UNION" +
      "				select " +
      "				  colum.ad_column_id as AD_COLUMN_ID " +
      "					, colum.name as COL_NAME " +
      "					, colum.columnname as COL_COLUMNNAME " +
      "					, elemnt.name as ELEMENT_NAME " +
      "					, elemnt.printname as ELEMENT_PRINTNAME " +
      "					, elemnt.printname as FIELD_NAME " +
      "				from " +
      "					ad_column colum" +
      "				        , ad_element elemnt" +
      "				where " +
      "				  colum.ad_table_id = (select tab.ad_table_id from ad_tab tab where tab.ad_tab_id = ?) " +
      "				  and colum.ad_element_id = elemnt.ad_element_id " +
      "				  and colum.ad_column_id not in (" +
      "				    select " +
      "				      field.ad_column_id" +
      "				    from " +
      "				      ad_column colum" +
      "				      , ad_field field" +
      "				    where " +
      "				      field.ad_tab_id = ?" +
      "				      and colum.ad_column_id = field.ad_column_id)";

    ResultSet result;
    Vector<java.lang.Object> vector = new Vector<java.lang.Object>(0);
    PreparedStatement st = null;

    int iParameter = 0;
    try {
    st = connectionProvider.getPreparedStatement(strSql);
      iParameter++; UtilSql.setValue(st, iParameter, 12, null, ad_tab_id);
      iParameter++; UtilSql.setValue(st, iParameter, 12, null, ad_tab_id);
      iParameter++; UtilSql.setValue(st, iParameter, 12, null, ad_tab_id);

      result = st.executeQuery();
      long countRecord = 0;
      long countRecordSkip = 1;
      boolean continueResult = true;
      while(countRecordSkip < firstRegister && continueResult) {
        continueResult = result.next();
        countRecordSkip++;
      }
      while(continueResult && result.next()) {
        countRecord++;
        FieldLabelsData objectFieldLabelsData = new FieldLabelsData();
        objectFieldLabelsData.adColumnId = UtilSql.getValue(result, "AD_COLUMN_ID");
        objectFieldLabelsData.colName = UtilSql.getValue(result, "COL_NAME");
        objectFieldLabelsData.colColumnname = UtilSql.getValue(result, "COL_COLUMNNAME");
        objectFieldLabelsData.elementName = UtilSql.getValue(result, "ELEMENT_NAME");
        objectFieldLabelsData.elementPrintname = UtilSql.getValue(result, "ELEMENT_PRINTNAME");
        objectFieldLabelsData.fieldName = UtilSql.getValue(result, "FIELD_NAME");
        objectFieldLabelsData.InitRecordNumber = Integer.toString(firstRegister);
        vector.addElement(objectFieldLabelsData);
        if (countRecord >= numberRegisters && numberRegisters != 0) {
          continueResult = false;
        }
      }
      result.close();
    } catch(SQLException e){
      log4j.error("SQL error in query: " + strSql + "Exception:"+ e);
      throw new ServletException("@CODE=" + Integer.toString(e.getErrorCode()) + "@" + e.getMessage());
    } catch(Exception ex){
      log4j.error("Exception in query: " + strSql + "Exception:"+ ex);
      throw new ServletException("@CODE=@" + ex.getMessage());
    } finally {
      try {
        connectionProvider.releasePreparedStatement(st);
      } catch(Exception ignore){
        ignore.printStackTrace();
      }
    }
    FieldLabelsData objectFieldLabelsData[] = new FieldLabelsData[vector.size()];
    vector.copyInto(objectFieldLabelsData);
    return(objectFieldLabelsData);
  }
}
