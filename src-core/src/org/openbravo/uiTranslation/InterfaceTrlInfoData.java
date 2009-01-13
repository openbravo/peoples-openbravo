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

public class InterfaceTrlInfoData implements FieldProvider {
    static Logger log4j = Logger.getLogger(InterfaceTrlInfoData.class);
    private String InitRecordNumber = "0";
    public String name;
    public String description;
    public String help;

    public String getInitRecordNumber() {
        return InitRecordNumber;
    }

    public String getField(String fieldName) {
        if (fieldName.equalsIgnoreCase("NAME"))
            return name;
        else if (fieldName.equalsIgnoreCase("DESCRIPTION"))
            return description;
        else if (fieldName.equalsIgnoreCase("HELP"))
            return help;
        else {
            log4j.debug("Field does not exist: " + fieldName);
            return null;
        }
    }

    public static InterfaceTrlInfoData[] selectTabTrlInfo(
            ConnectionProvider connectionProvider, String ad_tab_id,
            String langugae) throws ServletException {
        return selectTabTrlInfo(connectionProvider, ad_tab_id, langugae, 0, 0);
    }

    public static InterfaceTrlInfoData[] selectTabTrlInfo(
            ConnectionProvider connectionProvider, String ad_tab_id,
            String langugae, int firstRegister, int numberRegisters)
            throws ServletException {
        String strSql = "";
        strSql = strSql + "    SELECT "
                + "      typeTrl.name, typeTrl.description, typeTrl.help "
                + "    FROM " + "      ad_tab type, ad_tab_trl typeTrl "
                + "    WHERE " + "      type.ad_tab_id = typeTrl.ad_tab_id "
                + "      and type.ad_tab_id = ?"
                + "      and typeTrl.ad_language = ? ";

        ResultSet result;
        Vector<java.lang.Object> vector = new Vector<java.lang.Object>(0);
        PreparedStatement st = null;

        int iParameter = 0;
        try {
            st = connectionProvider.getPreparedStatement(strSql);
            iParameter++;
            UtilSql.setValue(st, iParameter, 12, null, ad_tab_id);
            iParameter++;
            UtilSql.setValue(st, iParameter, 12, null, langugae);

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
                InterfaceTrlInfoData objectInterfaceTrlInfoData = new InterfaceTrlInfoData();
                objectInterfaceTrlInfoData.name = UtilSql.getValue(result,
                        "NAME");
                objectInterfaceTrlInfoData.description = UtilSql.getValue(
                        result, "DESCRIPTION");
                objectInterfaceTrlInfoData.help = UtilSql.getValue(result,
                        "HELP");
                objectInterfaceTrlInfoData.InitRecordNumber = Integer
                        .toString(firstRegister);
                vector.addElement(objectInterfaceTrlInfoData);
                if (countRecord >= numberRegisters && numberRegisters != 0) {
                    continueResult = false;
                }
            }
            result.close();
        } catch (SQLException e) {
            log4j.error("SQL error in query: " + strSql + "Exception:" + e);
            throw new ServletException("@CODE="
                    + Integer.toString(e.getErrorCode()) + "@" + e.getMessage());
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
        InterfaceTrlInfoData objectInterfaceTrlInfoData[] = new InterfaceTrlInfoData[vector
                .size()];
        vector.copyInto(objectInterfaceTrlInfoData);
        return (objectInterfaceTrlInfoData);
    }

    public static InterfaceTrlInfoData[] selectProcessTrlInfo(
            ConnectionProvider connectionProvider, String ad_tab_id,
            String langugae) throws ServletException {
        return selectProcessTrlInfo(connectionProvider, ad_tab_id, langugae, 0,
                0);
    }

    public static InterfaceTrlInfoData[] selectProcessTrlInfo(
            ConnectionProvider connectionProvider, String ad_tab_id,
            String langugae, int firstRegister, int numberRegisters)
            throws ServletException {
        String strSql = "";
        strSql = strSql + "    SELECT "
                + "      typeTrl.name, typeTrl.description, typeTrl.help "
                + "    FROM "
                + "      ad_process type, ad_process_trl typeTrl "
                + "    WHERE "
                + "      type.ad_process_id = typeTrl.ad_process_id "
                + "      and type.ad_process_id = ?"
                + "      and typeTrl.ad_language = ? ";

        ResultSet result;
        Vector<java.lang.Object> vector = new Vector<java.lang.Object>(0);
        PreparedStatement st = null;

        int iParameter = 0;
        try {
            st = connectionProvider.getPreparedStatement(strSql);
            iParameter++;
            UtilSql.setValue(st, iParameter, 12, null, ad_tab_id);
            iParameter++;
            UtilSql.setValue(st, iParameter, 12, null, langugae);

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
                InterfaceTrlInfoData objectInterfaceTrlInfoData = new InterfaceTrlInfoData();
                objectInterfaceTrlInfoData.name = UtilSql.getValue(result,
                        "NAME");
                objectInterfaceTrlInfoData.description = UtilSql.getValue(
                        result, "DESCRIPTION");
                objectInterfaceTrlInfoData.help = UtilSql.getValue(result,
                        "HELP");
                objectInterfaceTrlInfoData.InitRecordNumber = Integer
                        .toString(firstRegister);
                vector.addElement(objectInterfaceTrlInfoData);
                if (countRecord >= numberRegisters && numberRegisters != 0) {
                    continueResult = false;
                }
            }
            result.close();
        } catch (SQLException e) {
            log4j.error("SQL error in query: " + strSql + "Exception:" + e);
            throw new ServletException("@CODE="
                    + Integer.toString(e.getErrorCode()) + "@" + e.getMessage());
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
        InterfaceTrlInfoData objectInterfaceTrlInfoData[] = new InterfaceTrlInfoData[vector
                .size()];
        vector.copyInto(objectInterfaceTrlInfoData);
        return (objectInterfaceTrlInfoData);
    }

    public static InterfaceTrlInfoData[] selectFormTrlInfo(
            ConnectionProvider connectionProvider, String ad_tab_id,
            String langugae) throws ServletException {
        return selectFormTrlInfo(connectionProvider, ad_tab_id, langugae, 0, 0);
    }

    public static InterfaceTrlInfoData[] selectFormTrlInfo(
            ConnectionProvider connectionProvider, String ad_tab_id,
            String langugae, int firstRegister, int numberRegisters)
            throws ServletException {
        String strSql = "";
        strSql = strSql + "    SELECT "
                + "      typeTrl.name, typeTrl.description, typeTrl.help "
                + "    FROM " + "      ad_form type, ad_form_trl typeTrl "
                + "    WHERE " + "      type.ad_form_id = typeTrl.ad_form_id "
                + "      and type.classname = ?"
                + "      and typeTrl.ad_language = ? ";

        ResultSet result;
        Vector<java.lang.Object> vector = new Vector<java.lang.Object>(0);
        PreparedStatement st = null;

        int iParameter = 0;
        try {
            st = connectionProvider.getPreparedStatement(strSql);
            iParameter++;
            UtilSql.setValue(st, iParameter, 12, null, ad_tab_id);
            iParameter++;
            UtilSql.setValue(st, iParameter, 12, null, langugae);

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
                InterfaceTrlInfoData objectInterfaceTrlInfoData = new InterfaceTrlInfoData();
                objectInterfaceTrlInfoData.name = UtilSql.getValue(result,
                        "NAME");
                objectInterfaceTrlInfoData.description = UtilSql.getValue(
                        result, "DESCRIPTION");
                objectInterfaceTrlInfoData.help = UtilSql.getValue(result,
                        "HELP");
                objectInterfaceTrlInfoData.InitRecordNumber = Integer
                        .toString(firstRegister);
                vector.addElement(objectInterfaceTrlInfoData);
                if (countRecord >= numberRegisters && numberRegisters != 0) {
                    continueResult = false;
                }
            }
            result.close();
        } catch (SQLException e) {
            log4j.error("SQL error in query: " + strSql + "Exception:" + e);
            throw new ServletException("@CODE="
                    + Integer.toString(e.getErrorCode()) + "@" + e.getMessage());
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
        InterfaceTrlInfoData objectInterfaceTrlInfoData[] = new InterfaceTrlInfoData[vector
                .size()];
        vector.copyInto(objectInterfaceTrlInfoData);
        return (objectInterfaceTrlInfoData);
    }
}
