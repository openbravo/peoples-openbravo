package org.openbravo.scheduling.quartz;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.quartz.impl.jdbcjobstore.CronTriggerPersistenceDelegate;
import org.quartz.impl.jdbcjobstore.PostgreSQLDelegate;
import org.quartz.impl.jdbcjobstore.SimpleTriggerPersistenceDelegate;

import static org.openbravo.scheduling.quartz.OpenbravoJDBCPersistenceSupport.getBooleanValue;
import static org.openbravo.scheduling.quartz.OpenbravoJDBCPersistenceSupport.setBooleanValue;

public class OpenbravoJDBCDelegate extends PostgreSQLDelegate {

	@Override
    protected void addDefaultTriggerPersistenceDelegates() {
        addTriggerPersistenceDelegate(new SimpleTriggerPersistenceDelegate());
        addTriggerPersistenceDelegate(new CronTriggerPersistenceDelegate());
        // Handling of Bool fields is not extensible in TriggerPersistenceDelegates that use
        // extended properties so those classes are replaced with Openbravo specific ones
        addTriggerPersistenceDelegate(new OpenbravoDailyTimeIntervalTriggerPersistenceDelegate());
        addTriggerPersistenceDelegate(new OpenbravoCalendarIntervalTriggerPersistenceDelegate());
    }
	
    /**
     * Use 'Y' and 'N' Varchar fields instead of Bool to be consistent with Openbravo
     * standards and to allow DBSourceManager to manage the data structures
     */
	@Override
    protected void setBoolean(PreparedStatement ps, int index, boolean val) throws SQLException {
        setBooleanValue(ps, index, val);
    }

    /**
     * Use 'Y' and 'N' Varchar fields instead of Bool to be consistent with Openbravo
     * standards and to allow DBSourceManager to manage the data structures
     */
	@Override
    protected boolean getBoolean(ResultSet rs, String columnName) throws SQLException {
        return getBooleanValue(rs, columnName);
    }
    
    /**
     * Use 'Y' and 'N' Varchar fields instead of Bool to be consistent with Openbravo
     * standards and to allow DBSourceManager to manage the data structures
     */
	@Override
    protected boolean getBoolean(ResultSet rs, int columnIndex) throws SQLException {
        return getBooleanValue(rs, columnIndex);
    }
	
}
