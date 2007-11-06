/*
************************************************************************************
* Copyright (C) 2001-2006 Openbravo S.L.
* Licensed under the Apache Software License version 2.0
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
* Unless required by applicable law or agreed to  in writing,  software  distributed
* under the License is distributed  on  an  "AS IS"  BASIS,  WITHOUT  WARRANTIES  OR
* CONDITIONS OF ANY KIND, either  express  or  implied.  See  the  License  for  the
* specific language governing permissions and limitations under the License.
************************************************************************************
*/

package org.openbravo.ddlutils.task;

import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import javax.sql.DataSource;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.ddlutils.Platform;
import org.apache.ddlutils.PlatformFactory;
import org.apache.ddlutils.io.DatabaseFilter;
import org.apache.ddlutils.io.DatabaseIO;
import org.apache.ddlutils.io.DynamicDatabaseFilter;
import org.apache.ddlutils.model.Column;
import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.model.Table;
import org.apache.tools.ant.BuildException;

/**
 *
 * @author adrian
 */
public class DatabaseUtils {
    
    /** Creates a new instance of DatabaseUtils */
    private DatabaseUtils() {
    }
    
    public static Database readDatabase(File f) {
        
        Database d = readDatabase_noChecks(f);
        d.initialize();
        return d;
    } 
    
    public static File[] readFileArray(File f) {
        
        if (f.isDirectory()) {
            
            ArrayList<File> fileslist = new ArrayList<File>();
            
            File[] directoryfiles = f.listFiles(new XMLFiles());
            for (File file : directoryfiles) {
                File[] ff = readFileArray(file);
                for (File fileint : ff) {
                    fileslist.add(fileint);
                }                
            }
            
            return fileslist.toArray(new File[fileslist.size()]);
        } else {
            return new File[] { f };
        }
    }
    
    private static Database readDatabase_noChecks(File f) {
            
        if (f.isDirectory()) {
            
            // create an empty database
            Database d = new Database();
            d.setName(f.getName());
            
            // gets the list and sort
            File[] filelist = f.listFiles(new XMLFiles());
            Arrays.sort(filelist, new FilesComparator());
            
            for (File file : filelist) {
                d.mergeWith(readDatabase_noChecks(file));                
            }
            
            return d;
        } else {
            DatabaseIO dbIO = new DatabaseIO();
            dbIO.setValidateXml(false); 
            return dbIO.readplain(f); 
        }
    } 
    
    private static void executeUpdate(DataSource ds, String query) throws SQLException {
        
        Connection c = ds.getConnection();
        
        Statement s = c.createStatement();
        s.execute(query);
        s.close();
        
        c.close();
    }    
    
    public static void manageDatabase(DataSource ds) throws SQLException {
        
        // prepare table
        Platform platform = PlatformFactory.createNewPlatformInstance(ds);
        // platform.setDelimitedIdentifierModeOn(true);
        
        Database db = new Database();
        db.setName("AD_SYSTEM_MODEL");
        Table t = new Table();
        t.setName("AD_SYSTEM_MODEL");
        Column c = new Column();
        c.setName("MODEL");
        c.setPrimaryKey(false);
        c.setRequired(false);
        c.setAutoIncrement(false);
        c.setTypeCode(Types.LONGVARBINARY);
        t.addColumn(c);
        db.addTable(t);
        
        platform.createTables(db, false, true);
        
        // insert an empty database
        db = new Database();
        db.setName("unnamed");            
        saveCurrentDatabase(ds, db);
    }
    
    public static void unmanageDatabase(DataSource ds) throws SQLException {
        executeUpdate(ds, "DROP TABLE AD_SYSTEM_MODEL");
    }
    
    public static void saveCurrentDatabase(DataSource ds, Database model) throws SQLException {
        
        executeUpdate(ds, "DELETE FROM AD_SYSTEM_MODEL");

        Connection c = ds.getConnection();
        
        PreparedStatement s = c.prepareStatement("INSERT INTO AD_SYSTEM_MODEL (MODEL) VALUES (?)");

        s.setBytes(1, serializeDatabase(model));
        s.executeUpdate();
        s.close();
        
        c.close();        
    }
    
    public static Database loadCurrentDatabase(DataSource ds) throws SQLException {
        
        Connection c = ds.getConnection();
        
        PreparedStatement s = c.prepareStatement("SELECT MODEL FROM AD_SYSTEM_MODEL");
        ResultSet rs = s.executeQuery();
        rs.next();
        byte[] model = rs.getBytes(1);
        rs.close();
        s.close();
        
        c.close();
        
        return deserializeDatabase(model);
    }
    
    public static String readFile(File f) throws IOException {

        StringBuffer s = new StringBuffer();
        BufferedReader br = new BufferedReader(new FileReader(f));

        String line;
        while ((line = br.readLine()) != null) {
            s.append(line);
            s.append('\n');
        }
        br.close();
        return s.toString();        
    }
    
    private static byte[] serializeDatabase(Database model) {
        
        try {        
            ByteArrayOutputStream out = new ByteArrayOutputStream();     

            Writer w =  new OutputStreamWriter(new GZIPOutputStream(out), "UTF-8");
            new DatabaseIO().write(model, w);
            w.flush();
            w.close();
            
            return out.toByteArray();        
        } catch (IOException e) {
            return null;
        }        
    }
    
    private static Database deserializeDatabase(byte[] model) {
        
        try {        
            ByteArrayInputStream in = new ByteArrayInputStream(model);
            Reader r = new InputStreamReader(new GZIPInputStream(in), "UTF-8");
            
            DatabaseIO dbIO = new DatabaseIO();
            dbIO.setValidateXml(false);
            return dbIO.read(r);
        } catch (IOException e) {
            return null;
        }
    }
    
    public static DatabaseFilter getDynamicDatabaseFilter(String filter, Database database) {
        try {
            DynamicDatabaseFilter dbfilter = (DynamicDatabaseFilter) Class.forName(filter).newInstance();
            dbfilter.init(database);
            return dbfilter;
        } catch (InstantiationException ex) {
            throw new BuildException(ex);
        } catch (IllegalAccessException ex) {
            throw new BuildException(ex);
        } catch (ClassNotFoundException ex) {
            throw new BuildException(ex);
        }
    }
    
    private static class XMLFiles implements FileFilter {
         public boolean accept(File pathname) {
             return pathname.isDirectory() || (pathname.isFile() && pathname.getName().endsWith(".xml"));
         }
    }   
    
    private static class FilesComparator implements Comparator<File> {
        public int compare(File a, File b) {

            if (a.isDirectory() && !b.isDirectory()) {
                return -1;
            } else if (!a.isDirectory() && b.isDirectory()) {
                return 1;
            } else {
                return a.getName().compareTo(b.getName());
            }
        }
    }
       
}
