/*
 * PostgreSqlModelLoader.java
 *
 * Created on 26 de noviembre de 2007, 17:31
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.apache.ddlutils.platform.postgresql;

import java.sql.Connection;
import java.sql.SQLException;
import org.apache.ddlutils.Platform;
import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.platform.ModelLoader;

/**
 *
 * @author adrian
 */
public class PostgreSqlModelLoader implements ModelLoader {
    
    /** Creates a new instance of PostgreSqlModelLoader */
    public PostgreSqlModelLoader(Platform p) {
    }
    
    public Database getDatabase(Connection connection) throws SQLException {
        return null; // Not implemented
    }   
}
