/*
 * OracleModelReader.java
 *
 * Created on 13 de noviembre de 2007, 9:59
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.apache.ddlutils.platform.oracle;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import org.apache.ddlutils.model.Database;

/**
 *
 * @author adrian
 */
public class OracleModelLoader {
    
    private Connection _connection;
    
    /** Creates a new instance of OracleModelReader */
    public OracleModelLoader() {
    }
    
    
    public Database getDatabase(Connection connection) throws SQLException {
        Database db = new Database();
        
        db.addTables(readTables());
        
        return db;
        
    }
    
    public Collection readTables() {
        return null;
    }
}
