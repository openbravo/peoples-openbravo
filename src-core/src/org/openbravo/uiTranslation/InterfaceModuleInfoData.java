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

public class InterfaceModuleInfoData implements FieldProvider {
    static Logger log4j = Logger.getLogger(InterfaceModuleInfoData.class);
    private String InitRecordNumber = "0";
    public String moduleid;
    public String modulelanguage;
    public String name;
    public String description;
    public String help;

    public String getInitRecordNumber() {
        return InitRecordNumber;
    }

    public String getField(String fieldName) {
        if (fieldName.equalsIgnoreCase("MODULEID"))
            return moduleid;
        else if (fieldName.equalsIgnoreCase("MODULELANGUAGE"))
            return modulelanguage;
        else if (fieldName.equalsIgnoreCase("NAME"))
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

    public static InterfaceModuleInfoData[] selectTabModuleLang(
            ConnectionProvider connectionProvider, String ad_tab_id)
            throws ServletException {
        return selectTabModuleLang(connectionProvider, ad_tab_id, 0, 0);
    }

    public static InterfaceModuleInfoData[] selectTabModuleLang(
            ConnectionProvider connectionProvider, String ad_tab_id,
            String keyValue, String keyName, int numberRegisters)
            throws ServletException {
        boolean existsKey = false;
        String strSql = "";
        strSql = strSql
                + "      SELECT "
                + "	  type.name, type.description, type.help, module.ad_module_id as moduleId, module.ad_language as moduleLanguage "
                + "	FROM " + "	  ad_module module, ad_tab type " + "	WHERE "
                + "	  module.ad_module_id = type.ad_module_id "
                + "	  and type.ad_tab_id = ? ";

        ResultSet result;
        Vector<java.lang.Object> vector = new Vector<java.lang.Object>(0);
        PreparedStatement st = null;

        int iParameter = 0;
        try {
            st = connectionProvider.getPreparedStatement(strSql);
            iParameter++;
            UtilSql.setValue(st, iParameter, 12, null, ad_tab_id);

            result = st.executeQuery();
            long countRecord = 0;
            long initRecord = 0;
            boolean searchComplete = false;
            while (result.next() && !searchComplete) {
                countRecord++;
                InterfaceModuleInfoData objectInterfaceModuleInfoData = new InterfaceModuleInfoData();
                objectInterfaceModuleInfoData.moduleid = UtilSql.getValue(
                        result, "MODULEID");
                objectInterfaceModuleInfoData.modulelanguage = UtilSql
                        .getValue(result, "MODULELANGUAGE");
                objectInterfaceModuleInfoData.name = UtilSql.getValue(result,
                        "NAME");
                objectInterfaceModuleInfoData.description = UtilSql.getValue(
                        result, "DESCRIPTION");
                objectInterfaceModuleInfoData.help = UtilSql.getValue(result,
                        "HELP");

                objectInterfaceModuleInfoData.InitRecordNumber = Long
                        .toString(initRecord);
                if (!existsKey)
                    existsKey = (objectInterfaceModuleInfoData
                            .getField(keyName).equalsIgnoreCase(keyValue));
                vector.addElement(objectInterfaceModuleInfoData);
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
        if (existsKey) {
            InterfaceModuleInfoData objectInterfaceModuleInfoData[] = new InterfaceModuleInfoData[vector
                    .size()];
            vector.copyInto(objectInterfaceModuleInfoData);
            return (objectInterfaceModuleInfoData);
        }
        return (new InterfaceModuleInfoData[0]);
    }

    public static InterfaceModuleInfoData[] selectTabModuleLang(
            ConnectionProvider connectionProvider, String ad_tab_id,
            int firstRegister, int numberRegisters) throws ServletException {
        String strSql = "";
        strSql = strSql
                + "      SELECT "
                + "	  type.name, type.description, type.help, module.ad_module_id as moduleId, module.ad_language as moduleLanguage "
                + "	FROM " + "	  ad_module module, ad_tab type " + "	WHERE "
                + "	  module.ad_module_id = type.ad_module_id "
                + "	  and type.ad_tab_id = ? ";

        ResultSet result;
        Vector<java.lang.Object> vector = new Vector<java.lang.Object>(0);
        PreparedStatement st = null;

        int iParameter = 0;
        try {
            st = connectionProvider.getPreparedStatement(strSql);
            iParameter++;
            UtilSql.setValue(st, iParameter, 12, null, ad_tab_id);

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
                InterfaceModuleInfoData objectInterfaceModuleInfoData = new InterfaceModuleInfoData();
                objectInterfaceModuleInfoData.moduleid = UtilSql.getValue(
                        result, "MODULEID");
                objectInterfaceModuleInfoData.modulelanguage = UtilSql
                        .getValue(result, "MODULELANGUAGE");
                objectInterfaceModuleInfoData.name = UtilSql.getValue(result,
                        "NAME");
                objectInterfaceModuleInfoData.description = UtilSql.getValue(
                        result, "DESCRIPTION");
                objectInterfaceModuleInfoData.help = UtilSql.getValue(result,
                        "HELP");
                objectInterfaceModuleInfoData.InitRecordNumber = Integer
                        .toString(firstRegister);
                vector.addElement(objectInterfaceModuleInfoData);
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
        InterfaceModuleInfoData objectInterfaceModuleInfoData[] = new InterfaceModuleInfoData[vector
                .size()];
        vector.copyInto(objectInterfaceModuleInfoData);
        return (objectInterfaceModuleInfoData);
    }

    public static InterfaceModuleInfoData[] selectFormModuleLang(
            ConnectionProvider connectionProvider, String classname)
            throws ServletException {
        return selectFormModuleLang(connectionProvider, classname, 0, 0);
    }

    public static InterfaceModuleInfoData[] selectFormModuleLang(
            ConnectionProvider connectionProvider, String classname,
            String keyValue, String keyName, int numberRegisters)
            throws ServletException {
        boolean existsKey = false;
        String strSql = "";
        strSql = strSql
                + "      SELECT "
                + "	  type.name, type.description, type.help, module.ad_module_id as moduleId, module.ad_language as moduleLanguage "
                + "	FROM " + "	  ad_module module, ad_form type " + "	WHERE "
                + "	  module.ad_module_id = type.ad_module_id "
                + "	  and type.classname = ? ";

        ResultSet result;
        Vector<java.lang.Object> vector = new Vector<java.lang.Object>(0);
        PreparedStatement st = null;

        int iParameter = 0;
        try {
            st = connectionProvider.getPreparedStatement(strSql);
            iParameter++;
            UtilSql.setValue(st, iParameter, 12, null, classname);

            result = st.executeQuery();
            long countRecord = 0;
            long initRecord = 0;
            boolean searchComplete = false;
            while (result.next() && !searchComplete) {
                countRecord++;
                InterfaceModuleInfoData objectInterfaceModuleInfoData = new InterfaceModuleInfoData();
                objectInterfaceModuleInfoData.moduleid = UtilSql.getValue(
                        result, "MODULEID");
                objectInterfaceModuleInfoData.modulelanguage = UtilSql
                        .getValue(result, "MODULELANGUAGE");
                objectInterfaceModuleInfoData.name = UtilSql.getValue(result,
                        "NAME");
                objectInterfaceModuleInfoData.description = UtilSql.getValue(
                        result, "DESCRIPTION");
                objectInterfaceModuleInfoData.help = UtilSql.getValue(result,
                        "HELP");
                objectInterfaceModuleInfoData.InitRecordNumber = Long
                        .toString(initRecord);
                if (!existsKey)
                    existsKey = (objectInterfaceModuleInfoData
                            .getField(keyName).equalsIgnoreCase(keyValue));
                vector.addElement(objectInterfaceModuleInfoData);
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
        if (existsKey) {
            InterfaceModuleInfoData objectInterfaceModuleInfoData[] = new InterfaceModuleInfoData[vector
                    .size()];
            vector.copyInto(objectInterfaceModuleInfoData);
            return (objectInterfaceModuleInfoData);
        }
        return (new InterfaceModuleInfoData[0]);
    }

    public static InterfaceModuleInfoData[] selectFormModuleLang(
            ConnectionProvider connectionProvider, String classname,
            int firstRegister, int numberRegisters) throws ServletException {
        String strSql = "";
        strSql = strSql
                + "      SELECT "
                + "	  type.name, type.description, type.help, module.ad_module_id as moduleId, module.ad_language as moduleLanguage "
                + "	FROM " + "	  ad_module module, ad_form type " + "	WHERE "
                + "	  module.ad_module_id = type.ad_module_id "
                + "	  and type.classname = ? ";

        ResultSet result;
        Vector<java.lang.Object> vector = new Vector<java.lang.Object>(0);
        PreparedStatement st = null;

        int iParameter = 0;
        try {
            st = connectionProvider.getPreparedStatement(strSql);
            iParameter++;
            UtilSql.setValue(st, iParameter, 12, null, classname);

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
                InterfaceModuleInfoData objectInterfaceModuleInfoData = new InterfaceModuleInfoData();
                objectInterfaceModuleInfoData.moduleid = UtilSql.getValue(
                        result, "MODULEID");
                objectInterfaceModuleInfoData.modulelanguage = UtilSql
                        .getValue(result, "MODULELANGUAGE");
                objectInterfaceModuleInfoData.name = UtilSql.getValue(result,
                        "NAME");
                objectInterfaceModuleInfoData.description = UtilSql.getValue(
                        result, "DESCRIPTION");
                objectInterfaceModuleInfoData.help = UtilSql.getValue(result,
                        "HELP");
                objectInterfaceModuleInfoData.InitRecordNumber = Integer
                        .toString(firstRegister);
                vector.addElement(objectInterfaceModuleInfoData);
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
        InterfaceModuleInfoData objectInterfaceModuleInfoData[] = new InterfaceModuleInfoData[vector
                .size()];
        vector.copyInto(objectInterfaceModuleInfoData);
        return (objectInterfaceModuleInfoData);
    }

    public static InterfaceModuleInfoData[] selectProcessModuleLang(
            ConnectionProvider connectionProvider, String ad_tab_id)
            throws ServletException {
        return selectProcessModuleLang(connectionProvider, ad_tab_id, 0, 0);
    }

    public static InterfaceModuleInfoData[] selectProcessModuleLang(
            ConnectionProvider connectionProvider, String ad_tab_id,
            String keyValue, String keyName, int numberRegisters)
            throws ServletException {
        boolean existsKey = false;
        String strSql = "";
        strSql = strSql
                + "      SELECT "
                + "	  type.name, type.description, type.help, module.ad_module_id as moduleId, module.ad_language as moduleLanguage "
                + "	FROM " + "	  ad_module module, ad_process type "
                + "	WHERE " + "	  module.ad_module_id = type.ad_module_id "
                + "	  and type.ad_process_id = ? ";

        ResultSet result;
        Vector<java.lang.Object> vector = new Vector<java.lang.Object>(0);
        PreparedStatement st = null;

        int iParameter = 0;
        try {
            st = connectionProvider.getPreparedStatement(strSql);
            iParameter++;
            UtilSql.setValue(st, iParameter, 12, null, ad_tab_id);

            result = st.executeQuery();
            long countRecord = 0;
            long initRecord = 0;
            boolean searchComplete = false;
            while (result.next() && !searchComplete) {
                countRecord++;
                InterfaceModuleInfoData objectInterfaceModuleInfoData = new InterfaceModuleInfoData();
                objectInterfaceModuleInfoData.moduleid = UtilSql.getValue(
                        result, "MODULEID");
                objectInterfaceModuleInfoData.modulelanguage = UtilSql
                        .getValue(result, "MODULELANGUAGE");
                objectInterfaceModuleInfoData.name = UtilSql.getValue(result,
                        "NAME");
                objectInterfaceModuleInfoData.description = UtilSql.getValue(
                        result, "DESCRIPTION");
                objectInterfaceModuleInfoData.help = UtilSql.getValue(result,
                        "HELP");
                objectInterfaceModuleInfoData.InitRecordNumber = Long
                        .toString(initRecord);
                if (!existsKey)
                    existsKey = (objectInterfaceModuleInfoData
                            .getField(keyName).equalsIgnoreCase(keyValue));
                vector.addElement(objectInterfaceModuleInfoData);
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
        if (existsKey) {
            InterfaceModuleInfoData objectInterfaceModuleInfoData[] = new InterfaceModuleInfoData[vector
                    .size()];
            vector.copyInto(objectInterfaceModuleInfoData);
            return (objectInterfaceModuleInfoData);
        }
        return (new InterfaceModuleInfoData[0]);
    }

    public static InterfaceModuleInfoData[] selectProcessModuleLang(
            ConnectionProvider connectionProvider, String ad_tab_id,
            int firstRegister, int numberRegisters) throws ServletException {
        String strSql = "";
        strSql = strSql
                + "      SELECT "
                + "	  type.name, type.description, type.help, module.ad_module_id as moduleId, module.ad_language as moduleLanguage "
                + "	FROM " + "	  ad_module module, ad_process type "
                + "	WHERE " + "	  module.ad_module_id = type.ad_module_id "
                + "	  and type.ad_process_id = ? ";

        ResultSet result;
        Vector<java.lang.Object> vector = new Vector<java.lang.Object>(0);
        PreparedStatement st = null;

        int iParameter = 0;
        try {
            st = connectionProvider.getPreparedStatement(strSql);
            iParameter++;
            UtilSql.setValue(st, iParameter, 12, null, ad_tab_id);

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
                InterfaceModuleInfoData objectInterfaceModuleInfoData = new InterfaceModuleInfoData();
                objectInterfaceModuleInfoData.moduleid = UtilSql.getValue(
                        result, "MODULEID");
                objectInterfaceModuleInfoData.modulelanguage = UtilSql
                        .getValue(result, "MODULELANGUAGE");
                objectInterfaceModuleInfoData.name = UtilSql.getValue(result,
                        "NAME");
                objectInterfaceModuleInfoData.description = UtilSql.getValue(
                        result, "DESCRIPTION");
                objectInterfaceModuleInfoData.help = UtilSql.getValue(result,
                        "HELP");
                objectInterfaceModuleInfoData.InitRecordNumber = Integer
                        .toString(firstRegister);
                vector.addElement(objectInterfaceModuleInfoData);
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
        InterfaceModuleInfoData objectInterfaceModuleInfoData[] = new InterfaceModuleInfoData[vector
                .size()];
        vector.copyInto(objectInterfaceModuleInfoData);
        return (objectInterfaceModuleInfoData);
    }

}
