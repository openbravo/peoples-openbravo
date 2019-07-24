package org.openbravo.scheduling.quartz;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class OpenbravoJDBCPersistenceSupport {

	public static final String TRUE_STRING = "Y";
	public static final String FALSE_STRING = "N";

    protected static void setBooleanValue(PreparedStatement ps, int index, boolean val) throws SQLException {
        ps.setString(index, val?TRUE_STRING:FALSE_STRING);
    }

    protected static boolean getBooleanValue(ResultSet rs, String columnName) throws SQLException {
        return TRUE_STRING.equals(rs.getString(columnName));
    }
    
    protected static boolean getBooleanValue(ResultSet rs, int columnIndex) throws SQLException {
        return TRUE_STRING.equals(rs.getString(columnIndex));
    }

}
