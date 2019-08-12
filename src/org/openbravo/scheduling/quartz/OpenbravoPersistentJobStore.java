package org.openbravo.scheduling.quartz;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Map;

import org.quartz.JobPersistenceException;
import org.quartz.SchedulerException;
import org.quartz.impl.jdbcjobstore.JobStoreTX;
import org.quartz.spi.JobStore;

public class OpenbravoPersistentJobStore extends JobStoreTX {

  private static Map<String, JobStore> clusterJobStores = new Hashtable<String, JobStore>();

  @Override
  public void setInstanceName(String instanceName) {
    super.setInstanceName(instanceName);
    clusterJobStores.put(instanceName, this);
  }

  @Override
  public void schedulerPaused() {
    try {
      super.schedulerPaused();
      updateSchedulerStatus(OpenbravoJDBCDelegate.SCHEDULER_STATUS_STANDBY);
    } catch (JobPersistenceException | SQLException e) {
      getLog().error("Scheduler state could not be updated. " + e.getMessage());
    }
  }

  @Override
  public void schedulerStarted() {
    try {
      super.schedulerStarted();
      updateSchedulerStatus(OpenbravoJDBCDelegate.SCHEDULER_STATUS_STARTED);
    } catch (SchedulerException | SQLException e) {
      getLog().error("Scheduler state could not be updated. " + e.getMessage());
    }
  }

  @Override
  public void schedulerResumed() {
    try {
      super.schedulerResumed();
      /*
       * The status intends to inform about the ability of the scheduler to execute processes, it
       * makes no difference if the scheduler was just started or put in standby and then resumed.
       */
      updateSchedulerStatus(OpenbravoJDBCDelegate.SCHEDULER_STATUS_STARTED);
    } catch (JobPersistenceException | SQLException e) {
      getLog().error("Scheduler state could not be updated. " + e.getMessage());
    }
  }

  private void updateSchedulerStatus(String status) throws JobPersistenceException, SQLException {
    Connection conn = getNonManagedTXConnection();
    try {
      ((OpenbravoJDBCDelegate) getDelegate()).updateSchedulerStatus(conn, getInstanceId(),
          lastCheckin, status);
    } catch (ClassCastException e) {
      getDelegate().updateSchedulerState(conn, getInstanceId(), lastCheckin);
      commitConnection(conn);
    } finally {
      cleanupConnection(conn);
    }
  }

  public boolean isSchedulingAllowed() {
    boolean schedulingAllowed = false;
    Connection conn;
    try {
      conn = getNonManagedTXConnection();
      schedulingAllowed = ((OpenbravoJDBCDelegate) getDelegate()).schedulersStarted(conn);
    } catch (ClassCastException | JobPersistenceException | SQLException e) {
      getLog().error("Failed to look for started scheduler instances. " + e.getMessage());
    }
    return schedulingAllowed;
  }

  public static boolean isSchedulingAllowedInCluster(String instanceName) {
    boolean schedulingAllowed = false;
    JobStore jobStore = clusterJobStores.get(instanceName);
    if (jobStore == null || !(jobStore instanceof OpenbravoPersistentJobStore)) {
      return schedulingAllowed;
    }
    schedulingAllowed = ((OpenbravoPersistentJobStore) jobStore).isSchedulingAllowed();
    return schedulingAllowed;
  }

}
