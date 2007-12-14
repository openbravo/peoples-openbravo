/*
 * BasicModelLoader.java
 *
 * Created on 13 de noviembre de 2007, 12:21
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.apache.ddlutils.platform;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.ddlutils.Platform;
import org.apache.ddlutils.model.Check;
import org.apache.ddlutils.model.Column;
import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.model.ForeignKey;
import org.apache.ddlutils.model.Function;
import org.apache.ddlutils.model.Reference;
import org.apache.ddlutils.model.Table;
import org.apache.ddlutils.model.Index;
import org.apache.ddlutils.model.IndexColumn;
import org.apache.ddlutils.model.Parameter;
import org.apache.ddlutils.model.Sequence;
import org.apache.ddlutils.model.Trigger;
import org.apache.ddlutils.model.Unique;
import org.apache.ddlutils.model.View;
import org.apache.ddlutils.util.ExtTypes;

/**
 *
 * @author adrian
 */
public abstract class ModelLoaderBase implements ModelLoader {
    
    
    protected Connection _connection;
    
       
    protected PreparedStatement _stmt_listtables;
    protected PreparedStatement _stmt_pkname;
    protected PreparedStatement _stmt_listcolumns;
    protected PreparedStatement _stmt_pkcolumns;
    protected PreparedStatement _stmt_listchecks;
    
    protected PreparedStatement _stmt_listfks;
    protected PreparedStatement _stmt_fkcolumns;
    
    protected PreparedStatement _stmt_listindexes;
    protected PreparedStatement _stmt_indexcolumns;
    
    protected PreparedStatement _stmt_listuniques;
    protected PreparedStatement _stmt_uniquecolumns;
    
    protected PreparedStatement _stmt_listviews;
    
    protected PreparedStatement _stmt_listsequences;
    
    protected PreparedStatement _stmt_listtriggers;
    
    protected PreparedStatement _stmt_listfunctions;
    protected PreparedStatement _stmt_functioncode;
    
    private Pattern _pFunctionHeader = Pattern.compile(
        "\\A\\s*([Ff][Uu][Nn][Cc][Tt][Ii][Oo][Nn]|[Pp][Rr][Oo][Cc][Ee][Dd][Uu][Rr][Ee])\\s+?.+?\\s*?(\\((.*?)\\))??" +
        "\\s*?([Rr][Ee][Tt][Uu][Rr][Nn]\\s+?(.+?))?" +
        "\\s+[Aa][Ss]\\s+");
    private Pattern _pFunctionParam = Pattern.compile(
        "^\\s*(.+?)\\s+(([Ii][Nn]|[Oo][Uu][Tt])\\s+)?(.+?)(\\s+[Dd][Ee][Ff][Aa][Uu][Ll][Tt]\\s+(.+?))?\\s*?$");

    
    /** Creates a new instance of BasicModelLoader */
    public ModelLoaderBase(Platform p) {
    }
    
    
    public Database getDatabase(Connection connection) throws SQLException {
        
        try {
            _connection = connection;

            initMetadataSentences();

            return readDatabase();
            
        } finally {
            closeMetadataSentences();
        }       
    }
    
    protected void initMetadataSentences() throws SQLException {

        _stmt_listtables = _connection.prepareStatement("SELECT TABLE_NAME FROM USER_TABLES WHERE TABLE_NAME <> 'AD_SYSTEM_MODEL' AND TABLE_NAME <> 'PLAN_TABLE' ORDER BY TABLE_NAME");
        _stmt_pkname = _connection.prepareStatement("SELECT CONSTRAINT_NAME FROM USER_CONSTRAINTS WHERE CONSTRAINT_TYPE = 'P' AND TABLE_NAME = ?");
        _stmt_listcolumns = _connection.prepareStatement("SELECT COLUMN_NAME, DATA_TYPE, CHAR_COL_DECL_LENGTH, DATA_LENGTH ,DATA_PRECISION, DATA_SCALE, NULLABLE, DATA_DEFAULT FROM USER_TAB_COLUMNS WHERE TABLE_NAME = ? ORDER BY COLUMN_ID");
        _stmt_pkcolumns = _connection.prepareStatement("SELECT COLUMN_NAME FROM USER_CONS_COLUMNS WHERE CONSTRAINT_NAME = ? ORDER BY POSITION");
        _stmt_listchecks = _connection.prepareStatement("SELECT CONSTRAINT_NAME, SEARCH_CONDITION FROM USER_CONSTRAINTS WHERE CONSTRAINT_TYPE = 'C' AND GENERATED = 'USER NAME' AND TABLE_NAME = ? ORDER BY CONSTRAINT_NAME");
        _stmt_listfks = _connection.prepareStatement("SELECT C.CONSTRAINT_NAME, C2.TABLE_NAME, C.DELETE_RULE, 'NO ACTION' FROM USER_CONSTRAINTS C, USER_CONSTRAINTS C2 WHERE C.R_CONSTRAINT_NAME = C2.CONSTRAINT_NAME AND C.CONSTRAINT_TYPE = 'R' AND C.TABLE_NAME = ? ORDER BY C.CONSTRAINT_NAME");
        _stmt_fkcolumns = _connection.prepareStatement("SELECT C.COLUMN_NAME, C2.COLUMN_NAME FROM USER_CONS_COLUMNS C, USER_CONSTRAINTS K, USER_CONS_COLUMNS C2, USER_CONSTRAINTS K2 WHERE C.CONSTRAINT_NAME = K.CONSTRAINT_NAME AND C2.CONSTRAINT_NAME = K2.CONSTRAINT_NAME AND K.R_CONSTRAINT_NAME = K2.CONSTRAINT_NAME AND C.CONSTRAINT_NAME = ? ORDER BY C.POSITION");

        _stmt_listindexes = _connection.prepareStatement("SELECT INDEX_NAME, UNIQUENESS FROM USER_INDEXES WHERE TABLE_NAME = ? AND INDEX_TYPE = 'NORMAL' AND INDEX_NAME NOT IN (SELECT INDEX_NAME FROM USER_CONSTRAINTS WHERE CONSTRAINT_TYPE = 'U' OR CONSTRAINT_TYPE = 'P') ORDER BY INDEX_NAME");
        _stmt_indexcolumns = _connection.prepareStatement("SELECT COLUMN_NAME FROM USER_IND_COLUMNS WHERE INDEX_NAME = ? ORDER BY COLUMN_POSITION");

        _stmt_listuniques = _connection.prepareStatement("SELECT CONSTRAINT_NAME FROM USER_CONSTRAINTS WHERE CONSTRAINT_TYPE = 'U' AND TABLE_NAME = ? ORDER BY CONSTRAINT_NAME");
        _stmt_uniquecolumns = _connection.prepareStatement("SELECT COLUMN_NAME FROM USER_CONS_COLUMNS WHERE CONSTRAINT_NAME = ? ORDER BY POSITION");

        _stmt_listviews = _connection.prepareStatement("SELECT VIEW_NAME, TEXT FROM USER_VIEWS");

        _stmt_listsequences = _connection.prepareStatement("SELECT SEQUENCE_NAME, LAST_NUMBER, INCREMENT_BY FROM USER_SEQUENCES");

        _stmt_listtriggers = _connection.prepareStatement("SELECT TRIGGER_NAME, TABLE_NAME, TRIGGER_TYPE, TRIGGERING_EVENT, TRIGGER_BODY FROM USER_TRIGGERS");

        _stmt_listfunctions = _connection.prepareStatement("SELECT DISTINCT NAME FROM USER_SOURCE WHERE TYPE = 'PROCEDURE' OR TYPE = 'FUNCTION'");
        _stmt_functioncode = _connection.prepareStatement("SELECT TEXT FROM USER_SOURCE WHERE NAME = ? ORDER BY LINE");
    }
    
    protected void closeMetadataSentences() throws SQLException {
        
        _stmt_listtables.close();
        _stmt_pkname.close();
        _stmt_listcolumns.close();
        _stmt_pkcolumns.close();
        _stmt_listchecks.close();
        _stmt_listfks.close();
        _stmt_fkcolumns.close();

        _stmt_listindexes.close();
        _stmt_indexcolumns.close();

        _stmt_listuniques.close();
        _stmt_uniquecolumns.close();    

        _stmt_listviews.close();

        _stmt_listsequences.close();

        _stmt_listtriggers.close();

        _stmt_listfunctions.close();
    }
    
    protected Database readDatabase()  throws SQLException{

        Database db = new Database();
        
        db.addTables(readTables());
        db.addViews(readViews());
        db.addSequences(readSequences());
        db.addTriggers(readTriggers());
        db.addFunctions(readFunctions());
        
        return db;
    }
    
    protected Collection readTables() throws SQLException {

        return readList(_stmt_listtables, 
            new RowConstructor() { public Object getRow(ResultSet r) throws SQLException {
                return readTable(r.getString(1));
            }});
    }
    
    protected Table readTable(String tablename) throws SQLException {
        
        final Table t = new Table();
        
        t.setName(tablename);
        
        _stmt_pkname.setString(1, tablename);
        fillRow(_stmt_pkname, new RowFiller() { public void fillRow(ResultSet r) throws SQLException {                
            t.setPrimaryKey(r.getString(1));
        }});
        
        // Columns
        t.addColumns(readColumns(tablename));
        
        // PKS
        if (t.getPrimaryKey() != null && !t.getPrimaryKey().equals("")) {
            _stmt_pkcolumns.setString(1, t.getPrimaryKey());
            fillList(_stmt_pkcolumns, new RowFiller() { public void fillRow(ResultSet r) throws SQLException {                
                t.findColumn(r.getString(1)).setPrimaryKey(true);
            }});            
        }
       
        // Checks
        t.addChecks(readChecks(tablename));
        
        // FKS
        t.addForeignKeys(readForeignKeys(tablename));
        
        // Indexes
        t.addIndices(readIndexes(tablename));
        
        // Uniques
        t.adduniques(readUniques(tablename));
        
        return t;
    }
    
    protected Collection readColumns(String tablename) throws SQLException {
        
        _stmt_listcolumns.setString(1, tablename);
        
        return readList(_stmt_listcolumns, 
            new RowConstructor() { public Object getRow(ResultSet r) throws SQLException {
                return readColumn(r);
            }});       
    }
    
    protected Column readColumn(ResultSet rs) throws SQLException {
        
        Column c = new Column();

        c.setName(rs.getString(1));
        c.setTypeCode(translateColumnType(rs.getString(2)));
        
        if (c.getTypeCode() == Types.DECIMAL) {
            int size = rs.getInt(5);
            if (size == 0) {
                c.setSize(null);
            } else {                
                c.setSizeAndScale(rs.getInt(5), rs.getInt(6));
            }
        } else if (c.getTypeCode() == Types.CHAR || c.getTypeCode() == Types.VARCHAR || c.getTypeCode() == ExtTypes.NCHAR || c.getTypeCode() == ExtTypes.NVARCHAR) {
            c.setSizeAndScale(rs.getInt(3), null);
        } else if (c.getTypeCode() == Types.TIMESTAMP) {
            c.setSizeAndScale(7, null);
        } else if (c.getTypeCode() == Types.CLOB || c.getTypeCode() == Types.BLOB) {
            c.setSizeAndScale(rs.getInt(4), null);
        }
        c.setRequired(translateRequired(rs.getString(7)));  
        c.setDefaultValue(translateDefault(rs.getString(8), c.getTypeCode()));
        
        return c;
    }
    
    protected Collection readChecks(String tablename) throws SQLException {
        
        _stmt_listchecks.setString(1, tablename);
        return readList(_stmt_listchecks, 
            new RowConstructor() { public Object getRow(ResultSet r) throws SQLException {
                return readCheck(r);
            }});
    }
      
    protected Check readCheck(ResultSet rs) throws SQLException {
        
        Check c = new Check();
        
        c.setName(rs.getString(1));
        c.setCondition(rs.getString(2));
        
        return c;
    }
    
    protected Collection readForeignKeys(String tablename) throws SQLException {
        
        _stmt_listfks.setString(1, tablename);
        return readList(_stmt_listfks, 
            new RowConstructor() { public Object getRow(ResultSet r) throws SQLException {
                return readForeignKey(r);
            }});
    }
    
    protected ForeignKey readForeignKey(ResultSet rs) throws SQLException {
        
        final ForeignKey fk = new ForeignKey();
        
        fk.setName(rs.getString(1));
        fk.setForeignTableName(rs.getString(2));
        fk.setOnDeleteCode(translateFKEvent(rs.getString(3)));
        fk.setOnUpdateCode(translateFKEvent(rs.getString(4)));
        
        _stmt_fkcolumns.setString(1, fk.getName());
        fillList(_stmt_fkcolumns, new RowFiller() { public void fillRow(ResultSet r) throws SQLException {
            Reference ref = new Reference();
            ref.setLocalColumnName(r.getString(1));
            ref.setForeignColumnName(r.getString(2));            
            fk.addReference(ref);
        }});
        
        return fk;
    }
    
    protected Collection readIndexes(String tablename) throws SQLException {
        
        _stmt_listindexes.setString(1, tablename);
        return readList(_stmt_listindexes, 
            new RowConstructor() { public Object getRow(ResultSet r) throws SQLException {
                return readIndex(r);
            }});
    }
    
    protected Index readIndex(ResultSet rs) throws SQLException {
        
        final Index inx = new Index();
        
        inx.setName(rs.getString(1));
        inx.setUnique(translateUniqueness(rs.getString(2)));        
        
        _stmt_indexcolumns.setString(1, inx.getName());
        fillList(_stmt_indexcolumns, new RowFiller() { public void fillRow(ResultSet r) throws SQLException {
            IndexColumn inxcol = new IndexColumn();
            inxcol.setName(r.getString(1));            
            inx.addColumn(inxcol);
        }});
        
        return inx;
    }
    
    protected Collection readUniques(String tablename) throws SQLException {
        
        _stmt_listuniques.setString(1, tablename);
        return readList(_stmt_listuniques, 
            new RowConstructor() { public Object getRow(ResultSet r) throws SQLException {
                return readUnique(r);
            }});
    }
    
    protected Unique readUnique(ResultSet rs) throws SQLException {
        
        final Unique uni = new Unique();
        
        uni.setName(rs.getString(1));
        
        _stmt_uniquecolumns.setString(1, uni.getName());
        fillList(_stmt_uniquecolumns, new RowFiller() { public void fillRow(ResultSet r) throws SQLException {
            IndexColumn inxcol = new IndexColumn();
            inxcol.setName(r.getString(1));            
            uni.addColumn(inxcol);
        }});
        
        return uni;
    }
    
    protected Collection readViews() throws SQLException {
        
        return readList(_stmt_listviews, 
            new RowConstructor() { public Object getRow(ResultSet r) throws SQLException {
                View v = new View();
                v.setName(r.getString(1));
                v.setStatement(r.getString(2));
                return v;
            }});
    }      
    
    protected Collection readSequences() throws SQLException {
        
        return readList(_stmt_listsequences, 
            new RowConstructor() { public Object getRow(ResultSet r) throws SQLException {
                Sequence s = new Sequence();
                s.setName(r.getString(1));
                s.setStart(r.getInt(2));
                s.setIncrement(r.getInt(3));
                return s;
            }});
    }    
    
    protected Collection readTriggers() throws SQLException {
        
        return readList(_stmt_listtriggers, 
            new RowConstructor() { public Object getRow(ResultSet r) throws SQLException {
                Trigger t = new Trigger();
                t.setName(r.getString(1));
                t.setTable(r.getString(2));               
                t.setFiresCode(translateFires(r.getString(3)));
                t.setForeachCode(translateForeach(r.getString(3)));
                t.setInsert(translateIsInsert(r.getString(4)));
                t.setUpdate(translateIsUpdate(r.getString(4)));
                t.setDelete(translateIsDelete(r.getString(4)));
                t.setBody(translatePLSQLBody(r.getString(5)));
                return t;
            }});
    }   
    
    protected Collection readFunctions() throws SQLException {
        
        return readList(_stmt_listfunctions, 
            new RowConstructor() { public Object getRow(ResultSet r) throws SQLException {
                Function f = new Function();
                f.setName(r.getString(1));
                
                parseFunctionCode(f, readFunctionCode(f.getName()));

                return f;
            }});
    } 
    
    protected void parseFunctionCode(Function f, String functioncode) {
        
        Matcher mFunctionHeader = _pFunctionHeader.matcher(functioncode);

        if (mFunctionHeader.find()) {
            f.setTypeCode(translateParamType(mFunctionHeader.group(5)));
            f.setBody(translatePLSQLBody(functioncode.substring(mFunctionHeader.end(0))));           
            
            if (mFunctionHeader.group(3) != null) {
                StringTokenizer t = new StringTokenizer(mFunctionHeader.group(3), ",");
                while (t.hasMoreTokens()) {
                    Matcher mparam = _pFunctionParam.matcher(t.nextToken());
                    if (mparam.find()) {
                        Parameter p = new Parameter();
                        p.setName(mparam.group(1));
                        p.setModeCode(translateMode(mparam.group(3)));
                        p.setTypeCode(translateParamType(mparam.group(4)));
                        p.setDefaultValue(translateDefault(mparam.group(6), p.getTypeCode())); 
                        
                        f.addParameter(p);
                    } else {
                        System.out.println("Function parameter not readed for function : " + f.getName());
                    }
                    // System.out.println(t.nextToken());
                }
            }
        } else {
            System.out.println("Function header not readed for function : " + f.getName());
        }
    }
    
    protected String readFunctionCode(String function) throws SQLException {
        
        final StringBuffer code = new StringBuffer();
        
        _stmt_functioncode.setString(1, function);
        fillList(_stmt_functioncode, new RowFiller() { public void fillRow(ResultSet r) throws SQLException {
            code.append(r.getString(1));            
        }});
        
        return code.toString();
    }
    
    protected int translateMode(String value) {
        if ("IN".equalsIgnoreCase(value)) {
            return Parameter.MODE_IN;
        } else if ("OUT".equalsIgnoreCase(value)) {
            return Parameter.MODE_OUT;
        } else {
            return Parameter.MODE_NONE;
        }
    }
    
  
    protected int translateFires(String value) {
        return value.startsWith("BEFORE")
                ? Trigger.FIRES_BEFORE
                : Trigger.FIRES_AFTER;
    }
    
    protected int translateForeach(String value) {
        return value.endsWith("EACH ROW")
                ? Trigger.FOR_EACH_ROW
                : Trigger.FOR_EACH_STATEMENT;
    }
    
    protected boolean translateIsInsert(String value) {
        return value.contains("INSERT");
    }
    
    protected boolean translateIsUpdate(String value) {
        return value.contains("UPDATE");
    }
    
    protected boolean translateIsDelete(String value) {
        return value.contains("DELETE");
    }
    
    protected String translatePLSQLBody(String value) {
        String body = value.trim();
        if (body.startsWith("DECLARE")) {
            body = body.substring(7);
        }
        if (body.endsWith(";")) {
            body = body.substring(0, body.length() -1);
        }
        return body;
    }
    
    protected boolean translateUniqueness(String uniqueness) {
        
        return "UNIQUE".equalsIgnoreCase(uniqueness);
    }
    
    protected abstract int translateFKEvent(String fkevent);
    
    protected abstract String translateDefault(String value, int type);
    
    protected abstract boolean translateRequired(String required);

    protected abstract int translateParamType(String nativeType);
        
    protected abstract int translateColumnType(String nativeType);

    
    private List readList(PreparedStatement stmt, RowConstructor r) throws SQLException {
        
        List l = new ArrayList();
        ResultSet rs = null;
        
        try {
            rs = stmt.executeQuery();
            while (rs.next()) {
                l.add(r.getRow(rs));
            }
        } finally {
            if (rs != null) {
                rs.close();
            }
        }        
        return l;
    }
    
    private Object readRow(PreparedStatement stmt, RowConstructor r) throws SQLException {
        
        ResultSet rs = null;
        
        try {
            rs = stmt.executeQuery();
            if (rs.next()) {
                return r.getRow(rs);
            } else {
                return null;
            }
        } finally {
            if (rs != null) {
                rs.close();
            }
        }        
    }    
    
    private void fillRow(PreparedStatement stmt, RowFiller r) throws SQLException {
        
        ResultSet rs = null;
        
        try {
            rs = stmt.executeQuery();
            if (rs.next()) {
                r.fillRow(rs);
            }
        } finally {
            if (rs != null) {
                rs.close();
            }
        }        
    }  
    
    private void fillList(PreparedStatement stmt, RowFiller r) throws SQLException {
        
        ResultSet rs = null;
        
        try {
            rs = stmt.executeQuery();
            while (rs.next()) {
                r.fillRow(rs);
            }
        } finally {
            if (rs != null) {
                rs.close();
            }
        }        
    }       
}
