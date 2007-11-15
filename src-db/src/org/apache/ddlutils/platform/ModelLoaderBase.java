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
import org.apache.ddlutils.Platform;
import org.apache.ddlutils.model.Check;
import org.apache.ddlutils.model.Column;
import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.model.ForeignKey;
import org.apache.ddlutils.model.Reference;
import org.apache.ddlutils.model.Table;
import org.apache.ddlutils.model.Index;
import org.apache.ddlutils.model.IndexColumn;
import org.apache.ddlutils.model.Sequence;
import org.apache.ddlutils.model.Unique;
import org.apache.ddlutils.model.View;
import org.apache.ddlutils.util.ExtTypes;

/**
 *
 * @author adrian
 */
public class ModelLoaderBase implements ModelLoader {
    
    
    private Connection _connection;
    
       
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
    
    /** Creates a new instance of BasicModelLoader */
    public ModelLoaderBase(Platform p) {
    }
    
    
    public Database getDatabase(Connection connection) throws SQLException {
        
        try {
            _connection = connection;

            _stmt_listtables = _connection.prepareStatement("SELECT TABLE_NAME FROM USER_TABLES WHERE TABLE_NAME <> 'AD_SYSTEM_MODEL' ORDER BY TABLE_NAME");
            _stmt_pkname = _connection.prepareStatement("SELECT CONSTRAINT_NAME FROM USER_CONSTRAINTS WHERE CONSTRAINT_TYPE = 'P' AND TABLE_NAME = ?");
            _stmt_listcolumns = _connection.prepareStatement("SELECT COLUMN_NAME, DATA_TYPE, CHAR_COL_DECL_LENGTH, DATA_PRECISION, DATA_SCALE, NULLABLE FROM USER_TAB_COLUMNS WHERE TABLE_NAME = ? ORDER BY COLUMN_ID");
            _stmt_pkcolumns = _connection.prepareStatement("SELECT COLUMN_NAME FROM USER_CONS_COLUMNS WHERE CONSTRAINT_NAME = ? ORDER BY POSITION");
            _stmt_listchecks = _connection.prepareStatement("SELECT CONSTRAINT_NAME, SEARCH_CONDITION FROM USER_CONSTRAINTS WHERE CONSTRAINT_TYPE = 'C' AND GENERATED = 'USER NAME' AND TABLE_NAME = ?");
            _stmt_listfks = _connection.prepareStatement("SELECT C.CONSTRAINT_NAME, C2.TABLE_NAME, C.DELETE_RULE, 'NO ACTION' FROM USER_CONSTRAINTS C, USER_CONSTRAINTS C2 WHERE C.R_CONSTRAINT_NAME = C2.CONSTRAINT_NAME AND C.CONSTRAINT_TYPE = 'R' AND C.TABLE_NAME = ?");
            _stmt_fkcolumns = _connection.prepareStatement("SELECT C.COLUMN_NAME, C2.COLUMN_NAME FROM USER_CONS_COLUMNS C, USER_CONSTRAINTS K, USER_CONS_COLUMNS C2, USER_CONSTRAINTS K2 WHERE C.CONSTRAINT_NAME = K.CONSTRAINT_NAME AND C2.CONSTRAINT_NAME = K2.CONSTRAINT_NAME AND K.R_CONSTRAINT_NAME = K2.CONSTRAINT_NAME AND C.CONSTRAINT_NAME = ? ORDER BY C.POSITION");

            _stmt_listindexes = _connection.prepareStatement("SELECT INDEX_NAME, UNIQUENESS FROM USER_INDEXES WHERE TABLE_NAME = ? AND INDEX_NAME NOT IN (SELECT INDEX_NAME FROM USER_CONSTRAINTS WHERE CONSTRAINT_TYPE = 'U' OR CONSTRAINT_TYPE = 'P')");
            _stmt_indexcolumns = _connection.prepareStatement("SELECT COLUMN_NAME FROM USER_IND_COLUMNS WHERE INDEX_NAME = ? ORDER BY COLUMN_POSITION");

            _stmt_listuniques = _connection.prepareStatement("SELECT CONSTRAINT_NAME FROM USER_CONSTRAINTS WHERE CONSTRAINT_TYPE = 'U' AND TABLE_NAME = ?");
            _stmt_uniquecolumns = _connection.prepareStatement("SELECT COLUMN_NAME FROM USER_CONS_COLUMNS WHERE CONSTRAINT_NAME = ? ORDER BY POSITION");

            _stmt_listviews = _connection.prepareStatement("SELECT VIEW_NAME, TEXT FROM USER_VIEWS");
            
            _stmt_listsequences = _connection.prepareStatement("SELECT SEQUENCE_NAME, MIN_VALUE, INCREMENT_BY FROM USER_SEQUENCES");

            return readDatabase();
            
        } finally {
        
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
        }       
    }
    
    protected Database readDatabase()  throws SQLException{

        Database db = new Database();
        
        db.addTables(readTables());
        db.addViews(readViews());
        db.addSequences(readSequences());
        
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
        if (t.getPrimaryKey() == null || t.getPrimaryKey().equals("")) {
            _stmt_pkcolumns.setString(1, tablename);
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
        c.setTypeCode(translateType(rs.getString(2)));
        
        if (c.getTypeCode() == Types.DECIMAL) {
            int size = rs.getInt(4);
            if (size == 0) {
                c.setSize(null);
            } else {                
                c.setSizeAndScale(rs.getInt(4), rs.getInt(5));
            }
        } else if (c.getTypeCode() == Types.CHAR || c.getTypeCode() == Types.VARCHAR || c.getTypeCode() == Types.NCHAR || c.getTypeCode() == Types.NVARCHAR) {
            c.setSizeAndScale(rs.getInt(3), null);
        } else if (c.getTypeCode() == Types.TIMESTAMP) {
            c.setSizeAndScale(7, null);
        } else if (c.getTypeCode() == Types.CLOB || c.getTypeCode() == Types.BLOB) {
            c.setSizeAndScale(rs.getInt(3), null);
        }
        c.setRequired("N".equals(rs.getString(6)));        
        
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
    
    protected boolean translateUniqueness(String uniqueness) {
        
        return "UNIQUE".equals(uniqueness);
    }
    
    protected int translateFKEvent(String fkevent) {
        if ("CASCADE".equals(fkevent)) {
            return DatabaseMetaData.importedKeyCascade;
        } else if ("SET NULL".equals(fkevent)) {
            return DatabaseMetaData.importedKeySetNull;
        } else if ("RESTRICT".equals(fkevent)) {
            return DatabaseMetaData.importedKeyRestrict;
        } else {
            return DatabaseMetaData.importedKeyNoAction;
        }
    }
    
    protected int translateType(String nativeType) {
        if ("CHAR".equals(nativeType)) {
            return Types.CHAR;
        } else if ("VARCHAR2".equals(nativeType)) {
            return Types.VARCHAR;
        } else if ("NCHAR".equals(nativeType)) {
            return ExtTypes.NCHAR;
        } else if ("NVARCHAR2".equals(nativeType)) {
            return ExtTypes.NVARCHAR;
        } else if ("NUMBER".equals(nativeType)) {
            return Types.DECIMAL;
        } else if ("DATE".equals(nativeType)) {
            return Types.TIMESTAMP;
        } else if ("CLOB".equals(nativeType)) {
            return Types.LONGVARCHAR;
        } else if ("BLOB".equals(nativeType)) {
            return Types.LONGVARBINARY;
        } else {
            return Types.VARCHAR;
        }
    }
    
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
