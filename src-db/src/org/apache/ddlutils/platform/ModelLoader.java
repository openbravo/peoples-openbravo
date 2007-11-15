/*
 * ModelLoader.java
 *
 * Created on 13 de noviembre de 2007, 12:03
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.apache.ddlutils.platform;

import java.sql.Connection;
import java.sql.SQLException;
import org.apache.ddlutils.model.Database;

/**
 *
 * @author adrian
 */
public interface ModelLoader {
    
    public Database getDatabase(Connection connection) throws SQLException;    
}
