/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.1  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html
 * Software distributed under the License  is  distributed  on  an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific  language  governing  rights  and  limitations
 * under the License.
 * The Original Code is Openbravo ERP.
 * The Initial Developer of the Original Code is Openbravo SLU 
 * All portions are Copyright (C) 2008-2019 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
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
