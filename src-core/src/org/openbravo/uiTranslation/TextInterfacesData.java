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

public class TextInterfacesData implements FieldProvider {
  static Logger log4j = Logger.getLogger(TextInterfacesData.class);
  private String InitRecordNumber = "0";
  public String filename;
  public String text;
  public String isused;
  public String moduleid;
  public String trllang;
  public String trltext;
  public String istranslated;
  public String orderseq;

  public String getInitRecordNumber() {
    return InitRecordNumber;
  }

  public String getField(String fieldName) {
    if (fieldName.equalsIgnoreCase("FILENAME"))
      return filename;
    else if (fieldName.equalsIgnoreCase("TEXT"))
      return text;
    else if (fieldName.equalsIgnoreCase("ISUSED"))
      return isused;
    else if (fieldName.equalsIgnoreCase("MODULEID"))
      return moduleid;
    else if (fieldName.equalsIgnoreCase("TRLLANG"))
      return trllang;
    else if (fieldName.equalsIgnoreCase("TRLTEXT"))
      return trltext;
    else if (fieldName.equalsIgnoreCase("ISTRANSLATED"))
      return istranslated;
    else if (fieldName.equalsIgnoreCase("ORDERSEQ"))
      return orderseq;
    else {
      log4j.debug("Field does not exist: " + fieldName);
      return null;
    }
  }

  /**
   * Return a listing of text values for the a page
   */
  public static TextInterfacesData[] selectText(ConnectionProvider connectionProvider,
      String htmlFile, String language) throws ServletException {
    return selectText(connectionProvider, htmlFile, language, 0, 0);
  }

  /**
   * Return a listing of text values for the a page
   */
  public static TextInterfacesData[] selectText(ConnectionProvider connectionProvider,
      String htmlFile, String language, String keyValue, String keyName, int numberRegisters)
      throws ServletException {
    boolean existsKey = false;
    String strSql = "";
    strSql = strSql
        + "      select "
        + "		  text.filename as filename, text.text as text, text.isused as isUsed, text.ad_module_id as moduleId,"
        + "		  texttrl.ad_language as trlLang, texttrl.text as trlText, texttrl.istranslated as isTranslated, 3 as orderSeq"
        + "		from " + "		  ad_textinterfaces text," + "		  ad_textinterfaces_trl texttrl"
        + "		where" + "		  text.ad_textinterfaces_id = texttrl.ad_textinterfaces_id "
        + "		  and text.filename = '";
    strSql = strSql + ((htmlFile == null || htmlFile.equals("")) ? "" : htmlFile);
    strSql = strSql + "' " + "		  and text.isused = 'Y'" + "		  and texttrl.ad_language = '";
    strSql = strSql + ((language == null || language.equals("")) ? "" : language);
    strSql = strSql
        + "'"
        + "		UNION"
        + "		select "
        + "		  text.filename as filename, text.text as text, text.isused as isUsed, text.ad_module_id as moduleId,"
        + "		  texttrl.ad_language as trlLang, texttrl.text as trlText, texttrl.istranslated as isTranslated, 2 as orderSeq"
        + "		from " + "		  ad_textinterfaces text," + "		  ad_textinterfaces_trl texttrl"
        + "		where" + "		  text.ad_textinterfaces_id = texttrl.ad_textinterfaces_id "
        + "		  and text.filename is null" + "		  and text.isused = 'Y' and texttrl.ad_language = '";
    strSql = strSql + ((language == null || language.equals("")) ? "" : language);
    strSql = strSql + "'" + "		UNION" + "		select "
        + "		  text.filename as filename, text.text as text, text.isused as isUsed, "
        + "		  text.ad_module_id as moduleId, '";
    strSql = strSql + ((language == null || language.equals("")) ? "" : language);
    strSql = strSql + "' as trlLang, "
        + "		  text.text as trlText, 'N' as isTranslated, 1 as orderSeq" + "		from "
        + "		  ad_textinterfaces text" + "		where" + "		  text.ad_textinterfaces_id NOT IN "
        + "			(SELECT t.ad_textinterfaces_id" + "			  FROM ad_textInterfaces t,"
        + "				  ad_textinterfaces_trl trl" + "			  WHERE "
        + "				t.ad_textinterfaces_id = trl.ad_textinterfaces_id" + "				and trl.ad_language = '";
    strSql = strSql + ((language == null || language.equals("")) ? "" : language);
    strSql = strSql + "')" + "		  and (text.filename is null" + "			OR text.filename = '";
    strSql = strSql + ((htmlFile == null || htmlFile.equals("")) ? "" : htmlFile);
    strSql = strSql + "' )" + "		  and text.isused = 'Y'" + "		order by orderSeq";

    ResultSet result;
    Vector<Object> vector = new Vector<Object>(0);
    PreparedStatement st = null;

    try {
      st = connectionProvider.getPreparedStatement(strSql);
      if (htmlFile != null && !(htmlFile.equals(""))) {
      }
      if (language != null && !(language.equals(""))) {
      }
      if (language != null && !(language.equals(""))) {
      }
      if (language != null && !(language.equals(""))) {
      }
      if (language != null && !(language.equals(""))) {
      }
      if (htmlFile != null && !(htmlFile.equals(""))) {
      }

      result = st.executeQuery();
      long countRecord = 0;
      long initRecord = 0;
      boolean searchComplete = false;
      while (result.next() && !searchComplete) {
        countRecord++;
        TextInterfacesData objectTextInterfacesData = new TextInterfacesData();
        objectTextInterfacesData.filename = UtilSql.getValue(result, "FILENAME");
        objectTextInterfacesData.text = UtilSql.getValue(result, "TEXT");
        objectTextInterfacesData.isused = UtilSql.getValue(result, "ISUSED");
        objectTextInterfacesData.moduleid = UtilSql.getValue(result, "MODULEID");
        objectTextInterfacesData.trllang = UtilSql.getValue(result, "TRLLANG");
        objectTextInterfacesData.trltext = UtilSql.getValue(result, "TRLTEXT");
        objectTextInterfacesData.istranslated = UtilSql.getValue(result, "ISTRANSLATED");
        objectTextInterfacesData.orderseq = UtilSql.getValue(result, "ORDERSEQ");
        objectTextInterfacesData.InitRecordNumber = Long.toString(initRecord);
        if (!existsKey)
          existsKey = (objectTextInterfacesData.getField(keyName).equalsIgnoreCase(keyValue));
        vector.addElement(objectTextInterfacesData);
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
      TextInterfacesData objectTextInterfacesData[] = new TextInterfacesData[vector.size()];
      vector.copyInto(objectTextInterfacesData);
      return (objectTextInterfacesData);
    }
    return (new TextInterfacesData[0]);
  }

  /**
   * Return a listing of text values for the a page
   */
  public static TextInterfacesData[] selectText(ConnectionProvider connectionProvider,
      String htmlFile, String language, int firstRegister, int numberRegisters)
      throws ServletException {
    String strSql = "";
    strSql = strSql
        + "      select "
        + "		  text.filename as filename, text.text as text, text.isused as isUsed, text.ad_module_id as moduleId,"
        + "		  texttrl.ad_language as trlLang, texttrl.text as trlText, texttrl.istranslated as isTranslated, 3 as orderSeq"
        + "		from " + "		  ad_textinterfaces text," + "		  ad_textinterfaces_trl texttrl"
        + "		where" + "		  text.ad_textinterfaces_id = texttrl.ad_textinterfaces_id "
        + "		  and text.filename = '";
    strSql = strSql + ((htmlFile == null || htmlFile.equals("")) ? "" : htmlFile);
    strSql = strSql + "' " + "		  and text.isused = 'Y'" + "		  and texttrl.ad_language = '";
    strSql = strSql + ((language == null || language.equals("")) ? "" : language);
    strSql = strSql
        + "'"
        + "		UNION"
        + "		select "
        + "		  text.filename as filename, text.text as text, text.isused as isUsed, text.ad_module_id as moduleId,"
        + "		  texttrl.ad_language as trlLang, texttrl.text as trlText, texttrl.istranslated as isTranslated, 2 as orderSeq"
        + "		from " + "		  ad_textinterfaces text," + "		  ad_textinterfaces_trl texttrl"
        + "		where" + "		  text.ad_textinterfaces_id = texttrl.ad_textinterfaces_id "
        + "		  and text.filename is null" + "		  and text.isused = 'Y' and texttrl.ad_language = '";
    strSql = strSql + ((language == null || language.equals("")) ? "" : language);
    strSql = strSql + "'" + "		UNION" + "		select "
        + "		  text.filename as filename, text.text as text, text.isused as isUsed, "
        + "		  text.ad_module_id as moduleId, '";
    strSql = strSql + ((language == null || language.equals("")) ? "" : language);
    strSql = strSql + "' as trlLang, "
        + "		  text.text as trlText, 'N' as isTranslated, 1 as orderSeq" + "		from "
        + "		  ad_textinterfaces text" + "		where" + "		  text.ad_textinterfaces_id NOT IN "
        + "			(SELECT t.ad_textinterfaces_id" + "			  FROM ad_textInterfaces t,"
        + "				  ad_textinterfaces_trl trl" + "			  WHERE "
        + "				t.ad_textinterfaces_id = trl.ad_textinterfaces_id" + "				and trl.ad_language = '";
    strSql = strSql + ((language == null || language.equals("")) ? "" : language);
    strSql = strSql + "')" + "		  and (text.filename is null" + "			OR text.filename = '";
    strSql = strSql + ((htmlFile == null || htmlFile.equals("")) ? "" : htmlFile);
    strSql = strSql + "' )" + "		  and text.isused = 'Y'" + "		order by orderSeq";

    ResultSet result;
    Vector<Object> vector = new Vector<Object>(0);
    PreparedStatement st = null;

    try {
      st = connectionProvider.getPreparedStatement(strSql);
      if (htmlFile != null && !(htmlFile.equals(""))) {
      }
      if (language != null && !(language.equals(""))) {
      }
      if (language != null && !(language.equals(""))) {
      }
      if (language != null && !(language.equals(""))) {
      }
      if (language != null && !(language.equals(""))) {
      }
      if (htmlFile != null && !(htmlFile.equals(""))) {
      }

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
        TextInterfacesData objectTextInterfacesData = new TextInterfacesData();
        objectTextInterfacesData.filename = UtilSql.getValue(result, "FILENAME");
        objectTextInterfacesData.text = UtilSql.getValue(result, "TEXT");
        objectTextInterfacesData.isused = UtilSql.getValue(result, "ISUSED");
        objectTextInterfacesData.moduleid = UtilSql.getValue(result, "MODULEID");
        objectTextInterfacesData.trllang = UtilSql.getValue(result, "TRLLANG");
        objectTextInterfacesData.trltext = UtilSql.getValue(result, "TRLTEXT");
        objectTextInterfacesData.istranslated = UtilSql.getValue(result, "ISTRANSLATED");
        objectTextInterfacesData.orderseq = UtilSql.getValue(result, "ORDERSEQ");
        objectTextInterfacesData.InitRecordNumber = Integer.toString(firstRegister);
        vector.addElement(objectTextInterfacesData);
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
    TextInterfacesData objectTextInterfacesData[] = new TextInterfacesData[vector.size()];
    vector.copyInto(objectTextInterfacesData);
    return (objectTextInterfacesData);
  }
}
