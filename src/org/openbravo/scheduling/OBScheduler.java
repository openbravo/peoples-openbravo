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
package org.openbravo.scheduling;

import static org.openbravo.scheduling.Process.SCHEDULED;
import static org.openbravo.scheduling.Process.UNSCHEDULED;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.JobKey.jobKey;
import static org.quartz.TriggerKey.triggerKey;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.ServletException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openbravo.base.ConfigParameters;
import org.openbravo.base.ConnectionProviderContextListener;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.base.session.OBPropertiesProvider;
import org.openbravo.base.weld.WeldUtils;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.utility.SequenceIdData;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerContext;
import org.quartz.SchedulerException;
import org.quartz.Trigger;

/**
 * Provides the ability of schedule and unschedule background processes.
 * 
 * @author awolski
 */
public class OBScheduler {
  private static final OBScheduler INSTANCE = new OBScheduler();

  private static final Logger log = LogManager.getLogger();

  protected static final String OB_GROUP = "OB_QUARTZ_GROUP";

  public static final String KEY = "org.openbravo.scheduling.OBSchedulingContext.KEY";

  private Scheduler sched;

  private SchedulerContext ctx;

  private String dateTimeFormat;

  private String sqlDateTimeFormat;

  private TriggerProvider triggerProvider = WeldUtils
      .getInstanceFromStaticBeanManager(TriggerProvider.class);

  private static final String BACKGROUND_POLICY = "background.policy";
  private static final String NO_EXECUTE_POLICY = "no-execute";

  private OBScheduler() {
  }

  /**
   * @return the singleton instance of this class
   */
  public static final OBScheduler getInstance() {
    return INSTANCE;
  }

  /**
   * @return The Quartz Scheduler instance used by OBScheduler.
   */
  public Scheduler getScheduler() {
    return sched;
  }

  /**
   * Retrieves the Openbravo ConnectionProvider from the Scheduler Context.
   * 
   * @return A ConnectionProvider
   */
  public ConnectionProvider getConnection() {
    return (ConnectionProvider) ctx.get(ConnectionProviderContextListener.POOL_ATTRIBUTE);
  }

  /**
   * Retrieves the Openbravo ConfigParameters from the Scheduler context.
   * 
   * @return Openbravo ConfigParameters
   */
  public ConfigParameters getConfigParameters() {
    return (ConfigParameters) ctx.get(ConfigParameters.CONFIG_ATTRIBUTE);
  }

  /**
   * @return The sqlDateTimeFormat of the OBScheduler.
   */
  String getSqlDateTimeFormat() {
    return sqlDateTimeFormat;
  }

  /**
   * Schedule a new process (bundle) to run immediately in the background, using a random name for
   * the Quartz's JobDetail.
   * 
   * This will create a new record in AD_PROCESS_REQUEST. This method throws a
   * {@link ServletException} if there is an error creating the AD_PROCESS_REQUEST information.
   * 
   * @see #schedule(String, ProcessBundle)
   */
  public void schedule(ProcessBundle bundle) throws SchedulerException, ServletException {
    if (bundle == null) {
      throw new SchedulerException("Process bundle cannot be null.");
    }
    final String requestId = SequenceIdData.getUUID();

    final String processId = bundle.getProcessId();
    final String channel = bundle.getChannel().toString();
    final ProcessContext context = bundle.getContext();

    ProcessRequestData.insert(getConnection(), context.getOrganization(), context.getClient(),
        context.getUser(), context.getUser(), requestId, processId, context.getUser(), SCHEDULED,
        channel, context.toString(), bundle.getParamsDeflated(), null, null, null, null);

    if (bundle.getGroupInfo() != null) {
      // Is Part of a Group, update the info
      ProcessRequestData.updateGroup(getConnection(), bundle.getGroupInfo().getRequest().getId(),
          requestId);
    }
    schedule(requestId, bundle);
  }

  /**
   * Schedule a process (bundle) with the specified request id. The request id is used in Quartz as
   * the JobDetail's name. The details must be saved to AD_PROCESS_REQUEST before reaching this
   * method.
   * 
   * @param requestId
   *          the id of the process request used as the Quartz jobDetail name
   * @param bundle
   *          The bundle with all of the process' details
   * 
   * @throws SchedulerException
   *           If something goes wrong with the process scheduling.
   * @throws ServletException
   *           If something goes wrong with the trigger creation.
   */
  public void schedule(String requestId, ProcessBundle bundle)
      throws SchedulerException, ServletException {
    if (isNoExecuteBackgroundPolicy()) {
      log.info("Not scheduling process because current context background policy is 'no-execute'");
      return;
    }
    if (requestId == null) {
      throw new SchedulerException("Request Id cannot be null.");
    }
    if (bundle == null) {
      throw new SchedulerException("Process bundle cannot be null.");
    }
    final JobDetail jobDetail = JobDetailProvider.newInstance(requestId, bundle);
    final Trigger trigger = triggerProvider.createTrigger(requestId, bundle, getConnection());

    sched.scheduleJob(jobDetail, trigger);
  }

  /**
   * Returns whether current node is set with no-execute background policy, which should prevent any
   * process scheduling.
   */
  public static boolean isNoExecuteBackgroundPolicy() {
    String policy = OBPropertiesProvider.getInstance()
        .getOpenbravoProperties()
        .getProperty(BACKGROUND_POLICY, "default");
    return NO_EXECUTE_POLICY.equals(policy);
  }

  /**
   * @param requestId
   * @param bundle
   * @throws SchedulerException
   * @throws ServletException
   */
  public void reschedule(String requestId, ProcessBundle bundle)
      throws SchedulerException, ServletException {
    try {
      sched.unscheduleJob(triggerKey(requestId, OB_GROUP));
      sched.deleteJob(jobKey(requestId, OB_GROUP));

    } catch (final SchedulerException e) {
      log.error("An error occurred rescheduling process " + bundle.toString(), e);
    }
    schedule(requestId, bundle);
  }

  public void unschedule(String requestId, ProcessContext context) throws SchedulerException {
    try {
      sched.unscheduleJob(triggerKey(requestId, OB_GROUP));
      sched.deleteJob(jobKey(requestId, OB_GROUP));
      ProcessRequestData.update(getConnection(), UNSCHEDULED, null, sqlDateTimeFormat,
          format(new Date()), context.getUser(), requestId);
    } catch (final Exception e) {
      log.error("An error occurred unscheduling process " + requestId, e);
    }
  }

  /**
   * @param date
   * @return the date as a formatted string
   */
  public static final String format(Date date) {
    return date == null ? null : new SimpleDateFormat(getInstance().dateTimeFormat).format(date);
  }

  /**
   * @param schdlr
   * @throws SchedulerException
   */
  public void initialize(Scheduler schdlr) throws SchedulerException {
    this.ctx = schdlr.getContext();
    this.sched = schdlr;

    final ProcessMonitor monitor = new ProcessMonitor("Monitor." + OB_GROUP, this.ctx);
    schdlr.getListenerManager().addSchedulerListener(monitor);
    schdlr.getListenerManager().addJobListener(monitor);
    schdlr.getListenerManager().addTriggerListener(monitor);

    dateTimeFormat = getConfigParameters().getJavaDateTimeFormat();
    sqlDateTimeFormat = getConfigParameters().getSqlDateTimeFormat();

    try {
      for (ProcessRequestData request : ProcessRequestData.selectByStatus(getConnection(),
          SCHEDULED)) {
        final String requestId = request.id;
        final VariablesSecureApp vars = ProcessContext.newInstance(request.obContext).toVars();

        if ("Direct".equals(request.channel)
            || TimingOption.of(request.timingOption) == TimingOption.IMMEDIATE) {
          // do not re-schedule immediate and direct requests that were in execution last time
          // Tomcat stopped
          ProcessRequestData.update(getConnection(), Process.SYSTEM_RESTART, vars.getUser(),
              requestId);
          log.debug(request.channel + " run of process id " + request.processId
              + " was scheduled, marked as 'System Restart'");
          continue;
        }

        scheduleProcess(requestId, vars);
      }
    } catch (final ServletException e) {
      log.error("An error occurred retrieving scheduled process data: " + e.getMessage(), e);
    }

  }

  private void scheduleProcess(String requestId, VariablesSecureApp vars)
      throws SchedulerException {
    try {
      final ProcessBundle bundle = ProcessBundle.request(requestId, vars, getConnection());
      schedule(requestId, bundle);
    } catch (final ServletException | ParameterSerializationException e) {
      log.error("Error scheduling process request: " + requestId, e);
    }
  }

  /**
   * @author awolski
   * 
   */
  private static class JobDetailProvider {

    /**
     * Creates a new JobDetail with the specified name and job class. Inserts the process bundle
     * into the JobDetail's jobDataMap for retrieval when the job is executed.
     * 
     * @param name
     *          The name of the JobDetail
     * @param bundle
     *          The Openbravo process bundle.
     * @throws SchedulerException
     */
    private static JobDetail newInstance(String name, ProcessBundle bundle)
        throws SchedulerException {
      if (bundle == null) {
        throw new SchedulerException("Process bundle cannot be null.");
      }
      final JobDetail jobDetail = newJob(DefaultJob.class).withIdentity(name, OB_GROUP).build();
      jobDetail.getJobDataMap().put(ProcessBundle.KEY, bundle);

      return jobDetail;
    }
  }
}
