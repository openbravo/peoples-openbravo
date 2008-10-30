/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.0  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html 
 * Software distributed under the License  is  distributed  on  an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific  language  governing  rights  and  limitations
 * under the License. 
 * The Original Code is Openbravo ERP. 
 * The Initial Developer of the Original Code is Openbravo SL 
 * All portions are Copyright (C) 2008 Openbravo SL 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
*/
package org.openbravo.scheduling;

import static org.openbravo.scheduling.Process.*;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.ServletException;

import org.openbravo.base.ConfigParameters;
import org.openbravo.base.ConnectionProviderContextListener;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.scheduling.ProcessRequestData;
import org.openbravo.scheduling.ProcessRunData;
import org.openbravo.erpCommon.utility.SequenceIdData;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobListener;
import org.quartz.SchedulerContext;
import org.quartz.SchedulerException;
import org.quartz.SchedulerListener;
import org.quartz.Trigger;
import org.quartz.TriggerListener;

/**
 * @author awolski
 *
 */
public class ProcessMonitor implements SchedulerListener, JobListener, TriggerListener {

  public static final String KEY = "org.openbravo.scheduling.ProcessMonitor.KEY";
  
  private String name;
  
  private SchedulerContext context;
  
  public ProcessMonitor(String name, SchedulerContext context) {
    this.name = name;
    this.context = context;
  }
  
  public void jobScheduled(Trigger trigger) {
    ProcessBundle bundle = (ProcessBundle) trigger.getJobDataMap().get(ProcessBundle.KEY);
    ProcessContext ctx = bundle.getContext();
    try {
      ProcessRequestData.update(getConnection(), ctx.getUser(), ctx.getUser(), 
          SCHEDULED, bundle.getChannel().toString(), format(trigger.getNextFireTime()), 
          format(trigger.getPreviousFireTime()), format(trigger.getFinalFireTime()), 
          ctx.toString(), trigger.getName());
    
    } catch (ServletException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  public void jobUnscheduled(String triggerName, String triggerGroup) {
    try {
      ProcessRequestData.update(getConnection(), UNSCHEDULED, null, null, triggerName);
    
    } catch (ServletException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  public void jobsPaused(String jobName, String jobGroup) {
    // TODO Auto-generated method stub
    
  }

  public void jobsResumed(String jobName, String jobGroup) {
    // TODO Auto-generated method stub
    
  }

  public void schedulerError(String msg, SchedulerException e) {
    // TODO Auto-generated method stub
    
  }

  public void schedulerShutdown() {
    // TODO Auto-generated method stub
    
  }

  public void triggerFinalized(Trigger trigger) {
    try {
      ProcessRequestData.update(getConnection(), COMPLETE, trigger.getName());
    
    } catch (ServletException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  public void triggersPaused(String triggerName, String triggerGroup) {
    // TODO Auto-generated method stub
    
  }

  public void triggersResumed(String triggerName, String triggerGroup) {
    // TODO Auto-generated method stub
    
  }

  public void jobExecutionVetoed(JobExecutionContext jec) {
    // TODO Auto-generated method stub
    
  }

  public void jobToBeExecuted(JobExecutionContext jec) {
    String executionId = SequenceIdData.getUUID();
    ProcessBundle bundle = (ProcessBundle) jec.getMergedJobDataMap().get(ProcessBundle.KEY);
    ProcessContext ctx = bundle.getContext();
    try {
      ProcessRunData.insert(getConnection(), ctx.getOrganization(), ctx.getClient(), 
          ctx.getUser(), ctx.getUser(), ctx.getUser(), executionId, 
          PROCESSING, format(jec.getFireTime()), null, null, 
          jec.getJobDetail().getName());
      
          jec.put(EXECUTION_ID, executionId);
          
          bundle.setConnection(getConnection());
          bundle.setConfig(getConfigParameters());
    
    } catch (ServletException e){
      // TODO Auto-generated method stub
      e.printStackTrace();
    }
  }

  public void jobWasExecuted(JobExecutionContext jec, JobExecutionException jee) {
    ProcessBundle bundle = (ProcessBundle) jec.getMergedJobDataMap().get(ProcessBundle.KEY);
    ProcessContext ctx = bundle.getContext();
    try {
      String executionId = (String) jec.get(EXECUTION_ID);
      if (jee == null) {
        ProcessRunData.update(getConnection(), ctx.getUser(), SUCCESS, 
            getDuration(jec.getJobRunTime()), bundle.getLog().toString(), executionId);
      } else {
        ProcessRunData.update(getConnection(), ctx.getUser(), ERROR, 
            getDuration(jec.getJobRunTime()), bundle.getLog().toString(), executionId);
      }
    
    } catch (ServletException e) {
      // TODO Auto-generated method stub
      e.printStackTrace();
    }
  }

  public void triggerComplete(Trigger trigger, JobExecutionContext jec, 
      int triggerInstructionCode) {
    // TODO Auto-generated method stub
    
  }

  public void triggerFired(Trigger trigger, JobExecutionContext jec) {
    ProcessBundle bundle = (ProcessBundle) jec.getMergedJobDataMap().get(ProcessBundle.KEY);
    ProcessContext ctx = bundle.getContext();
    try {
      ProcessRequestData.update(getConnection(), ctx.getUser(), ctx.getUser(), 
          SCHEDULED, bundle.getChannel().toString(), 
          format(trigger.getPreviousFireTime()), format(trigger.getNextFireTime()), 
          format(trigger.getFinalFireTime()), ctx.toString(), trigger.getName());
    
    } catch (ServletException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  public void triggerMisfired(Trigger trigger) {
    try {
      ProcessRequestData.update(getConnection(), MISFIRED, trigger.getName());
    
    } catch (ServletException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    
  }

  public boolean vetoJobExecution(Trigger trigger, JobExecutionContext jec) {
    // TODO Auto-generated method stub
    return false;
  }

  /**
   * @return
   */
  public ConnectionProvider getConnection() {
    return (ConnectionProvider) context.get(ConnectionProviderContextListener.POOL_ATTRIBUTE);
  }
  
  /**
   * @return
   */
  public ConfigParameters getConfigParameters() {
    return (ConfigParameters) context.get(ConfigParameters.CONFIG_ATTRIBUTE);
  }
  
  /**
   * @param date
   * @return
   */
  public final String format(Date date) {
    String dateTimeFormat = getConfigParameters().getJavaDateTimeFormat();
    return date == null ? null : new SimpleDateFormat(dateTimeFormat).format(date);
  }
  
  /**
   * @param duration
   * @return
   */
  public static String getDuration(long duration) {
    
    int milliseconds = (int) (duration % 1000);
    int seconds = (int) ((duration / 1000) % 60);
    int minutes = (int) ((duration / 60000) % 60);
    int hours = (int) ((duration / 3600000) % 24);
    
    String m = (milliseconds < 10 ? "00" : (milliseconds<100 ? "0" : "")) + milliseconds;
    String sec = (seconds < 10 ? "0" : "") + seconds;
    String min = (minutes < 10 ? "0" : "") + minutes;
    String hr = (hours < 10 ? "0" : "") + hours;

    return hr + ":" + min + ":" + sec + "." + m;
  }

  /* (non-Javadoc)
   * @see org.quartz.JobListener#getName()
   */
  public String getName() {
    return name;
  }
}
