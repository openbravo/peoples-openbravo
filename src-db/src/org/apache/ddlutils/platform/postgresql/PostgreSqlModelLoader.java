/*
 * PostgreSqlModelLoader.java
 *
 * Created on 26 de noviembre de 2007, 17:31
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.apache.ddlutils.platform.postgresql;

import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import org.apache.ddlutils.Platform;
import org.apache.ddlutils.model.Function;
import org.apache.ddlutils.model.Parameter;
import org.apache.ddlutils.platform.ModelLoaderBase;
import org.apache.ddlutils.platform.RowConstructor;
import org.apache.ddlutils.platform.RowFiller;
import org.apache.ddlutils.util.ExtTypes;

/**
 *
 * @author adrian
 */
public class PostgreSqlModelLoader extends ModelLoaderBase {

    protected PreparedStatement _stmt_functionparams;
    protected PreparedStatement _stmt_paramtypes;
    
    protected Map<Integer, Integer> _paramtypes =  new HashMap<Integer, Integer>();
    
    /** Creates a new instance of PostgreSqlModelLoader */
    public PostgreSqlModelLoader(Platform p) {
        super(p);
    }    
    
    protected String readName() {
        return "PostgreSql server";
    }
    
    protected void initMetadataSentences() throws SQLException {

        _stmt_listtables = _connection.prepareStatement("SELECT TABLE_NAME FROM USER_TABLES WHERE TABLE_NAME <> 'AD_SYSTEM_MODEL' AND TABLE_NAME <> 'PLAN_TABLE' ORDER BY TABLE_NAME");
        _stmt_pkname = _connection.prepareStatement("SELECT CONSTRAINT_NAME FROM USER_CONSTRAINTS WHERE CONSTRAINT_TYPE = 'P' AND TABLE_NAME = ?");
        _stmt_listcolumns = _connection.prepareStatement("SELECT COLUMN_NAME, DATA_TYPE, CHAR_COL_DECL_LENGTH, DATA_LENGTH ,DATA_PRECISION, DATA_SCALE, NULLABLE, DATA_DEFAULT FROM USER_TAB_COLUMNS WHERE TABLE_NAME = ? ORDER BY COLUMN_ID");
        _stmt_pkcolumns = _connection.prepareStatement("SELECT COLUMN_NAME FROM USER_CONS_COLUMNS WHERE CONSTRAINT_NAME = ? ORDER BY POSITION");
        _stmt_listchecks = _connection.prepareStatement("SELECT CONSTRAINT_NAME, SEARCH_CONDITION FROM USER_CONSTRAINTS WHERE CONSTRAINT_TYPE = 'C' AND TABLE_NAME = ? ORDER BY CONSTRAINT_NAME");
        _stmt_listfks = _connection.prepareStatement("SELECT CONSTRAINT_NAME, FK_TABLE, DELETE_RULE, 'A' FROM USER_CONSTRAINTS WHERE CONSTRAINT_TYPE = 'R' AND TABLE_NAME = ? ORDER BY CONSTRAINT_NAME");
        // _stmt_fkcolumns = _connection.prepareStatement("SELECT COLUMN_NAMES, FK_COLUMN_NAMES FROM USER_CONSTRAINTS WHERE CONSTRAINT_NAME = ?");
        _stmt_fkcolumns = _connection.prepareStatement("select  upper(pa1.attname) AS column_name, upper(pa2.attname) AS foreign_column_name   from pg_constraint pc, pg_class pc1, pg_attribute pa1, pg_class pc2, pg_attribute pa2 where pc.contype='f'   and pc.conrelid= pc1.oid   and upper(pc.conname) = upper(?)   and pa1.attrelid = pc1.oid   and pa1.attnum = ANY(pc.conkey)   and pc.confrelid = pc2.oid   and pa2.attrelid = pc2.oid   and pa2.attnum = ANY(pc.confkey)");
        _stmt_listindexes = _connection.prepareStatement("SELECT INDEX_NAME, UNIQUENESS FROM USER_INDEXES WHERE TABLE_NAME = ? AND INDEX_TYPE = 'NORMAL' AND INDEX_NAME NOT IN (SELECT INDEX_NAME FROM USER_CONSTRAINTS WHERE CONSTRAINT_TYPE = 'U' OR CONSTRAINT_TYPE = 'P') ORDER BY INDEX_NAME");
        _stmt_indexcolumns = _connection.prepareStatement("SELECT COLUMN_NAME FROM USER_IND_COLUMNS WHERE INDEX_NAME = ? ORDER BY COLUMN_POSITION");

        _stmt_listuniques = _connection.prepareStatement("SELECT CONSTRAINT_NAME FROM USER_CONSTRAINTS WHERE CONSTRAINT_TYPE = 'U' AND TABLE_NAME = ? ORDER BY CONSTRAINT_NAME");
        _stmt_uniquecolumns = _connection.prepareStatement("SELECT COLUMN_NAME FROM USER_CONS_COLUMNS WHERE CONSTRAINT_NAME = ? ORDER BY POSITION");

        _stmt_listviews = _connection.prepareStatement("SELECT upper(viewname), definition FROM pg_views " +
                "WHERE schemaname NOT IN ('pg_catalog', 'information_schema') AND viewname !~ '^pg_' " +
                "AND viewname <> 'dual' AND viewname <> 'user_cons_columns' AND viewname <> 'user_tables' " +
                "AND viewname <> 'user_constraints' AND viewname <> 'user_indexes' AND viewname <> 'user_ind_columns' " +
                "AND viewname <> 'user_objects' AND viewname <> 'user_tab_columns' AND viewname <> 'user_triggers'");

        _stmt_listsequences = _connection.prepareStatement("SELECT upper(relname), 1, 1 FROM pg_class WHERE relkind = 'S'");

        _stmt_listtriggers = _connection.prepareStatement("SELECT upper(trg.tgname) AS trigger_name, upper(tbl.relname) AS table_name, " +
                "CASE trg.tgtype & cast(3 as int2) " +
                "WHEN 0 THEN 'AFTER EACH STATEMENT' " +
                "WHEN 1 THEN 'AFTER EACH ROW' " +
                "WHEN 2 THEN 'BEFORE EACH STATEMENT' " +
                "WHEN 3 THEN 'BEFORE EACH ROW' " +
                "END AS trigger_type, " +
                "CASE trg.tgtype & cast(28 as int2) WHEN 16 THEN 'UPDATE' " +
                "WHEN  8 THEN 'DELETE' " +
                "WHEN  4 THEN 'INSERT' " +
                "WHEN 20 THEN 'INSERT, UPDATE' " +
                "WHEN 28 THEN 'INSERT, UPDATE, DELETE' " +
                "WHEN 24 THEN 'UPDATE, DELETE' " +
                "WHEN 12 THEN 'INSERT, DELETE' " +
                "END AS trigger_event, " +
                "p.proname AS function_name " +
                "FROM pg_trigger trg, pg_class tbl, pg_proc p " +
                "WHERE trg.tgrelid = tbl.oid AND trg.tgfoid = p.oid AND tbl.relname !~ '^pg_' AND trg.tgname !~ '^RI'");
        
        _stmt_listfunctions = _connection.prepareStatement(
                "select distinct upper(proname) from pg_proc p, pg_namespace n " +
                "where  pronamespace = n.oid " +
                "and n.nspname=current_schema() " +
                "and p.oid not in (select tgfoid " +
                "from pg_trigger) " +
                "and lower(p.proname) not in ('exist_language', " +
                "'insert_pg_language', 'create_language', 'dateformat', 'to_number', 'to_date', 'to_timestamp', " +
                "'to_char', 'round', 'rpad', 'substr', 'to_interval', 'add_months', 'add_days', 'type_oid', 'substract_days', " +
                "'trunc', 'instr', 'last_day', 'is_trigger_enabled', 'drop_view') and lower(p.proname) not in ( " +
                "'ad_script_disable_triggers', 'ad_script_disable_constraints', 'ad_script_enable_triggers', 'ad_script_enable_constraints', " +
                "'ad_script_drop_recreate_indexes', 'ad_script_execute', 'dba_getattnumpos', 'dba_getstandard_search_text', 'dump', 'negation')");
        
        _stmt_functioncode = _connection.prepareStatement("select 'function ' || ?"); // dummy sentence        
        
        _stmt_functionparams = _connection.prepareStatement(
                "  SELECT " +
                "         pg_proc.prorettype," +
                "         pg_proc.proargtypes," +
                "         pg_proc.proallargtypes," +
                "         pg_proc.proargmodes," +
                "         pg_proc.proargnames" +
                "    FROM pg_catalog.pg_proc" +
                "         JOIN pg_catalog.pg_namespace" +
                "         ON (pg_proc.pronamespace = pg_namespace.oid)" +
                "   WHERE pg_proc.prorettype <> 'pg_catalog.cstring'::pg_catalog.regtype" +
                "     AND (pg_proc.proargtypes[0] IS NULL" +
                "      OR pg_proc.proargtypes[0] <> 'pg_catalog.cstring'::pg_catalog.regtype)" +
                "     AND NOT pg_proc.proisagg" +
                "     AND pg_catalog.pg_function_is_visible(pg_proc.oid)" +
                "     AND upper(pg_proc.proname) = ?" +
                "         ORDER BY pg_proc.proargtypes DESC");
        _stmt_paramtypes = _connection.prepareStatement("SELECT pg_catalog.format_type(?, NULL)");        
        
        
//  SELECT 
//         pg_proc.prorettype,
//         pg_proc.proargtypes,
//         pg_proc.proallargtypes,
//         pg_proc.proargmodes,
//         pg_proc.proargnames
//    FROM pg_catalog.pg_proc
//         JOIN pg_catalog.pg_namespace
//         ON (pg_proc.pronamespace = pg_namespace.oid)
//   WHERE pg_proc.prorettype <> 'pg_catalog.cstring'::pg_catalog.regtype
//     AND (pg_proc.proargtypes[0] IS NULL
//      OR pg_proc.proargtypes[0] <> 'pg_catalog.cstring'::pg_catalog.regtype)
//     AND NOT pg_proc.proisagg
//     AND pg_catalog.pg_function_is_visible(pg_proc.oid)
//     AND upper(pg_proc.proname) = 'C_CURRENCY_CONVERT'
//
//SELECT pg_catalog.format_type(1700, NULL);        
    }
    
    protected void closeMetadataSentences() throws SQLException {
        super.closeMetadataSentences();
        _stmt_functionparams.close();
        _stmt_paramtypes.close();
    }
    
    protected Function readFunction(String name) throws SQLException {
        
        final Function f = new Function();
        f.setName(name);
        
        final FinalBoolean firststep = new FinalBoolean();
            
        _stmt_functionparams.setString(1, name);
        fillList(_stmt_functionparams, new RowFiller() { public void fillRow(ResultSet r) throws SQLException {

            if (firststep.get()) {
                // just set defaults
                Integer[] atypes = getIntArray(r, 2);
                Integer[] aalltypes = getIntArray2(r, 3);
                if (aalltypes != null) {    
                    atypes = aalltypes;
                }     
                for (int i = atypes.length; i < f.getParameterCount(); i++) {
                    f.getParameter(i).setDefaultValue("0"); // a dummy default value
                }
                
            } else {
                int ireturn = r.getInt(1);
                Integer[] atypes = getIntArray(r, 2);
                Integer[] aalltypes = getIntArray2(r, 3);
                String[] modes = getStringArray(r, 4);
                String[] names = getStringArray(r, 5);

                if (aalltypes == null) {
                    f.setTypeCode(getParamType(ireturn));                  
                } else {
                    f.setTypeCode(Types.NULL);    
                    atypes = aalltypes;
                }

                for (int i = 0; i < atypes.length; i++) {
                    Parameter p = new Parameter();
                    p.setTypeCode(getParamType(atypes[i]));
                    if (modes == null) {
                        p.setModeCode(Parameter.MODE_IN);
                    } else {
                        p.setModeCode("i".equals(modes[i]) 
                                ? Parameter.MODE_IN
                                : Parameter.MODE_OUT);
                    }
                    if (names != null) {
                        p.setName(names[i]);
                    }

                    f.addParameter(p);
                }          
                firststep.set(true);
            }
        }});        
        
        
        f.setBody("/********/");           
        return f;
    }    
    
    protected Integer[] getIntArray(ResultSet r, int iposition) throws SQLException {
        
        String s = r.getString(iposition);
        if (s == null) {
            return null;
        } else {
            ArrayList<Integer> list = new ArrayList<Integer>();        
        
            StringTokenizer st = new StringTokenizer(s);

            while (st.hasMoreTokens()) {
                list.add(Integer.parseInt(st.nextToken()));
            }        

            return list.toArray(new Integer[list.size()]);
        }
    }
    
    protected Integer[] getIntArray2(ResultSet r, int iposition) throws SQLException {
        
        String s = r.getString(iposition);
        if (s == null) {
            return null;
        } else {
            ArrayList<Integer> list = new ArrayList<Integer>();        
            if (s.length() > 1 && s.charAt(0) == '{' && s.charAt(s.length() - 1) == '}') {
                s = s.substring(1, s.length() - 1);
            }
        
            StringTokenizer st = new StringTokenizer(s, ",");

            while (st.hasMoreTokens()) {
                list.add(Integer.parseInt(st.nextToken()));
            }        

            return list.toArray(new Integer[list.size()]);
        }
    }
    
    protected String[] getStringArray(ResultSet r, int iposition) throws SQLException {
        
        String s = r.getString(iposition);
        if (s == null) {
            return null;
        } else {
            ArrayList<String> list = new ArrayList<String>();
            if (s.length() > 1 && s.charAt(0) == '{' && s.charAt(s.length() - 1) == '}') {
                s = s.substring(1, s.length() - 1);
            }
        
            StringTokenizer st = new StringTokenizer(s, ",");

            while (st.hasMoreTokens()) {
                list.add(st.nextToken());
            }        

            return list.toArray(new String[list.size()]);
        }
    }
    
    protected int getParamType(int pgtype) throws SQLException {
        
        if (!_paramtypes.containsKey(pgtype)) {

            _stmt_paramtypes.setInt(1, pgtype);
            String stype = (String) readRow(_stmt_paramtypes, new RowConstructor() { public Object getRow(ResultSet r) throws SQLException {
                return r.getString(1);
            }});

            _paramtypes.put(pgtype, translateParamType(stype));    
        }
        
        return _paramtypes.get(pgtype);
    }
    
    protected boolean translateRequired(String required) {
        return "f".equals(required);
    }
    
    protected String translateDefault(String value, int type) {
        
        switch (type) {
            case Types.CHAR:
            case Types.VARCHAR:
            case ExtTypes.NCHAR:
            case ExtTypes.NVARCHAR:
            case Types.LONGVARCHAR:
                if (value.endsWith("::character varying")) {
                    value = value.substring(0, value.length() - 19);
                }
                if (value.endsWith("::bpchar")) {
                    value = value.substring(0, value.length() - 8);
                }

                if (value.length() >= 2 && value.startsWith("'") && value.endsWith("'")) {
                    value = value.substring(1, value.length() - 1);
                    int i = 0;
                    StringBuffer sunescaped = new StringBuffer();
                    while (i < value.length()) {
                        char c = value.charAt(i);
                        if (c == '\'') {
                            i++;
                            if (i < value.length()) {
                                sunescaped.append(c);
                                i++;                                    
                            }                                
                        } else {
                            sunescaped.append(c);
                            i++;
                        }
                    }
                    if(sunescaped.length() == 0) return null;
                    else return sunescaped.toString();
                } else {
                    return value;
                }
            case Types.TIMESTAMP:
                if ("now()".equalsIgnoreCase(value)) {
                    return "SYSDATE";
                } else {
                    return value;
                }
            default: return value;
        }
    }
    
    protected int translateColumnType(String nativeType) {
        
        if (nativeType == null) {
            return Types.NULL;
        } else if ("BPCHAR".equalsIgnoreCase(nativeType)) {
            return Types.CHAR;
        } else if ("VARCHAR".equalsIgnoreCase(nativeType)) {
            return Types.VARCHAR;
        } else if ("NUMERIC".equalsIgnoreCase(nativeType)) {
            return Types.DECIMAL;
        } else if ("TIMESTAMP".equalsIgnoreCase(nativeType)) {
            return Types.TIMESTAMP;
        } else if ("TEXT".equalsIgnoreCase(nativeType)) {
            return Types.CLOB;
        } else if ("BYTEA".equalsIgnoreCase(nativeType)) {
            return Types.BLOB;
        } else if ("OID".equalsIgnoreCase(nativeType)) {
            return Types.BLOB;
        } else {
            return Types.VARCHAR;
        }
    }
    
    protected int translateParamType(String nativeType) {
        
        if (nativeType == null) {
            return Types.NULL;
        } else if ("VOID".equalsIgnoreCase(nativeType)) {
            return Types.NULL;
        } else if ("CHARACTER".equalsIgnoreCase(nativeType)) {
            return Types.CHAR;
        } else if ("BPCHAR".equalsIgnoreCase(nativeType)) {
            return Types.CHAR;
        } else if ("VARCHAR".equalsIgnoreCase(nativeType)) {
            return Types.VARCHAR;
        } else if ("NUMERIC".equalsIgnoreCase(nativeType)) {
            return Types.NUMERIC;
        } else if ("TIMESTAMP".equalsIgnoreCase(nativeType)) {
            return Types.TIMESTAMP;
        } else if (nativeType.startsWith("timestamp")) {
            return Types.TIMESTAMP;
        } else if ("TEXT".equalsIgnoreCase(nativeType)) {
            return Types.CLOB;
        } else if ("BYTEA".equalsIgnoreCase(nativeType)) {
            return Types.BLOB;
        } else if ("OID".equalsIgnoreCase(nativeType)) {
            return Types.BLOB;
        } else {
            return Types.VARCHAR;
        }
    }    
    
    protected int translateFKEvent(String fkevent) {
        if ("C".equalsIgnoreCase(fkevent)) {
            return DatabaseMetaData.importedKeyCascade;
        } else if ("N".equalsIgnoreCase(fkevent)) {
            return DatabaseMetaData.importedKeySetNull;
        } else if ("R".equalsIgnoreCase(fkevent)) {
            return DatabaseMetaData.importedKeyRestrict;
        } else {
            return DatabaseMetaData.importedKeyNoAction;
        }
    }     
    
    private static class FinalBoolean {
        private boolean b = false;
        public boolean get() {
            return b;
        }
        public void set(boolean value) {
            b = value;
        }        
    }
}
