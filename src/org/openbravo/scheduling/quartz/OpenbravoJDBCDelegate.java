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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.quartz.impl.jdbcjobstore.CronTriggerPersistenceDelegate;
import org.quartz.impl.jdbcjobstore.PostgreSQLDelegate;
import org.quartz.impl.jdbcjobstore.SimpleTriggerPersistenceDelegate;

import static org.openbravo.scheduling.quartz.OpenbravoJDBCPersistenceSupport.getBooleanValue;
import static org.openbravo.scheduling.quartz.OpenbravoJDBCPersistenceSupport.setBooleanValue;

public class OpenbravoJDBCDelegate extends PostgreSQLDelegate {

  private static final String COL_SCHEDULER_STATUS = "STATUS";

  static String SCHEDULER_STATUS_STANDBY = "STANDBY";
  static String SCHEDULER_STATUS_STARTED = "STARTED";

  String UPDATE_SCHEDULER_STATE_EXTENDED = "UPDATE " + TABLE_PREFIX_SUBST + TABLE_SCHEDULER_STATE
      + " SET " + COL_LAST_CHECKIN_TIME + " = ?, " + COL_SCHEDULER_STATUS + " = ? " + " WHERE "
      + COL_SCHEDULER_NAME + " = " + SCHED_NAME_SUBST + " AND " + COL_INSTANCE_NAME + " = ?";

  static String COUNT_STARTED_SCHEDULER_INSTANCES = "SELECT count(*) " + " FROM "
      + TABLE_PREFIX_SUBST + TABLE_SCHEDULER_STATE + " WHERE " + COL_SCHEDULER_NAME + " = "
      + SCHED_NAME_SUBST + " AND " + COL_SCHEDULER_STATUS + " = ?";

  @Override
  protected void addDefaultTriggerPersistenceDelegates() {
    addTriggerPersistenceDelegate(new SimpleTriggerPersistenceDelegate());
    addTriggerPersistenceDelegate(new CronTriggerPersistenceDelegate());
    // Handling of Bool fields is not extensible in TriggerPersistenceDelegates that use
    // extended properties so those classes are replaced with Openbravo specific ones
    addTriggerPersistenceDelegate(new OpenbravoDailyTimeIntervalTriggerPersistenceDelegate());
    addTriggerPersistenceDelegate(new OpenbravoCalendarIntervalTriggerPersistenceDelegate());
  }

  public int updateSchedulerStatus(Connection conn, String theInstanceId, long checkInTime,
      String status) throws SQLException {
    PreparedStatement ps = null;
    try {
      ps = conn.prepareStatement(rtp(UPDATE_SCHEDULER_STATE_EXTENDED));
      ps.setLong(1, checkInTime);
      ps.setString(2, status);
      ps.setString(3, theInstanceId);

      return ps.executeUpdate();
    } finally {
      closeStatement(ps);
    }
  }

  public boolean schedulersStarted(Connection conn) throws SQLException {
    PreparedStatement ps = null;
    ResultSet rs = null;

    try {
      ps = conn.prepareStatement(rtp(COUNT_STARTED_SCHEDULER_INSTANCES));
      ps.setString(1, SCHEDULER_STATUS_STARTED);
      rs = ps.executeQuery();

      if (rs.next()) {
        return rs.getInt(1) > 0;
      }

      throw new SQLException("No started instances count returned.");
    } finally {
      closeResultSet(rs);
      closeStatement(ps);
    }
  }

  /**
   * Use 'Y' and 'N' Varchar fields instead of Bool to be consistent with Openbravo standards and to
   * allow DBSourceManager to manage the data structures
   */
  @Override
  protected void setBoolean(PreparedStatement ps, int index, boolean val) throws SQLException {
    setBooleanValue(ps, index, val);
  }

  /**
   * Use 'Y' and 'N' Varchar fields instead of Bool to be consistent with Openbravo standards and to
   * allow DBSourceManager to manage the data structures
   */
  @Override
  protected boolean getBoolean(ResultSet rs, String columnName) throws SQLException {
    return getBooleanValue(rs, columnName);
  }

  /**
   * Use 'Y' and 'N' Varchar fields instead of Bool to be consistent with Openbravo standards and to
   * allow DBSourceManager to manage the data structures
   */
  @Override
  protected boolean getBoolean(ResultSet rs, int columnIndex) throws SQLException {
    return getBooleanValue(rs, columnIndex);
  }

}
