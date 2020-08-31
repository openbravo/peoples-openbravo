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
 * All portions are Copyright (C) 2019-2020 Openbravo SLU
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.scheduling.trigger;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.base.session.OBPropertiesProvider;
import org.openbravo.erpCommon.utility.SequenceIdData;
import org.openbravo.scheduling.Frequency;
import org.openbravo.scheduling.JobDetailProvider;
import org.openbravo.scheduling.ProcessBundle;
import org.openbravo.scheduling.ProcessLogger;
import org.openbravo.scheduling.TimingOption;
import org.openbravo.service.db.DalBaseProcess;
import org.openbravo.service.db.DalConnectionProvider;
import org.openbravo.test.base.Issue;
import org.openbravo.test.base.OBBaseTest;
import org.openbravo.test.base.TestConstants.Clients;
import org.openbravo.test.base.TestConstants.Orgs;
import org.openbravo.test.base.TestConstants.Roles;
import org.openbravo.test.base.TestConstants.Users;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobListener;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;

/**
 * Test cases to cover the expected behavior of the misfire policy applied to the background
 * processes.
 */
public class MisfirePolicyTest extends OBBaseTest {
  private static final DateTimeFormatter DEFAULT_FORMATTER = DateTimeFormatter
      .ofPattern("dd-MM-yyyy HH:mm:ss");

  private Scheduler scheduler;
  private TestProcessMonitor monitor;

  private static Integer misfireThreshold;

  @Before
  public void startScheduler() throws SchedulerException {
    scheduler = new StdSchedulerFactory().getScheduler();
    monitor = new TestProcessMonitor();
    scheduler.getListenerManager().addJobListener(monitor);
    scheduler.start();
  }

  @After
  public void stopScheduler() throws SchedulerException {
    scheduler.clear();
    scheduler.shutdown();
  }

  /**
   * Check that the misfire policy is fulfilled: don't execute on misfire and wait for next regular
   * execution time.
   */
  @Test
  @Issue("23767")
  public void checkMisfirePolicy() throws SchedulerException, InterruptedException {
    TriggerData data = new TriggerData();
    data.timingOption = TimingOption.SCHEDULED.getLabel();
    data.frequency = Frequency.WEEKLY.getLabel();
    data.startDate = "23-09-2019";
    data.startTime = "00:00:00";
    data.dayMon = "Y";
    data.dayTue = "N";
    data.dayWed = "N";
    data.dayThu = "N";
    data.dayFri = "N";
    data.daySat = "N";
    data.daySun = "N";

    Date startDate = dateOf("23-09-2019 00:00:00");
    Date nextExecutionDate = dateOf("30-09-2019 00:00:00");

    String name = SequenceIdData.getUUID();
    ProcessBundle bundle = getProcessBundle();
    Trigger trigger = TriggerProvider.getInstance().createTrigger(name, bundle, data);
    scheduleJob(name, trigger, bundle);

    // give some little time to ensure that job is not executed
    Thread.sleep(500);

    assertThat("Job not executed on misfire", monitor.getJobExecutions(name), equalTo(0));
    assertThat("Next regular execution time", trigger.getFireTimeAfter(startDate),
        is(nextExecutionDate));
  }

  @Test
  public void checkMisfirePolicyWithSecondlySchedule()
      throws SchedulerException, InterruptedException {
    TriggerData data = new TriggerData();
    data.timingOption = TimingOption.SCHEDULED.getLabel();
    data.frequency = Frequency.SECONDLY.getLabel();
    data.startDate = "23-09-2019";
    data.startTime = "00:00:00";
    data.secondlyInterval = "1";
    data.secondlyRepetitions = "2";

    String name = SequenceIdData.getUUID();
    scheduleJob(name, data);

    // wait for the job executions
    Thread.sleep(getMisfireThreshold() + 3000L);

    assertThat("Expected number of job executions", monitor.getJobExecutions(name), equalTo(2));
  }

  /**
   * Check that jobs are not executed on misfire in the "every n days" daily execution. We are
   * explicitly testing this kind of schedule because of its particular implementation.
   */
  @Test
  public void everyNDaysNotExecutedOnMisfire() throws SchedulerException, InterruptedException {
    TriggerData data = new TriggerData();
    data.timingOption = TimingOption.SCHEDULED.getLabel();
    data.frequency = Frequency.DAILY.getLabel();
    data.startDate = "23-09-2019";
    data.startTime = "00:00:00";
    data.dailyInterval = "1";
    data.dailyOption = "N";

    String name = SequenceIdData.getUUID();
    scheduleJob(name, data);

    // give some little time to ensure that job is not executed
    Thread.sleep(500);

    assertThat("Job not executed on misfire", monitor.getJobExecutions(name), equalTo(0));
  }

  private void scheduleJob(String name, TriggerData data) throws SchedulerException {
    ProcessBundle bundle = getProcessBundle();
    Trigger trigger = TriggerProvider.getInstance().createTrigger(name, bundle, data);
    scheduleJob(name, trigger, bundle);
  }

  private void scheduleJob(String name, Trigger trigger, ProcessBundle bundle)
      throws SchedulerException {
    JobDetail jd = JobDetailProvider.getInstance().createJobDetail(name, bundle);
    scheduler.scheduleJob(jd, trigger);
  }

  private ProcessBundle getProcessBundle() {
    DalConnectionProvider conn = new DalConnectionProvider();
    VariablesSecureApp vars = new VariablesSecureApp(Users.OPENBRAVO, Clients.SYSTEM, Orgs.MAIN,
        Roles.SYS_ADMIN);
    ProcessBundle bundle = new ProcessBundle(null, vars);

    bundle.setProcessClass(EmptyProcess.class);
    bundle.setParams(Collections.emptyMap());
    bundle.setConnection(conn);
    bundle.setLog(new ProcessLogger(conn));

    return bundle;
  }

  private Date dateOf(String executionDate) {
    return Date.from(LocalDateTime.parse(executionDate, DEFAULT_FORMATTER)
        .atZone(ZoneId.systemDefault())
        .toInstant());
  }

  private class TestProcessMonitor implements JobListener {

    private static final String NAME = "TestProcessMonitor";
    private Map<String, Integer> jobExecutions;

    public TestProcessMonitor() {
      jobExecutions = new HashMap<>();
    }

    @Override
    public String getName() {
      return NAME;
    }

    @Override
    public void jobToBeExecuted(JobExecutionContext context) {
      context.put(ProcessBundle.CONNECTION, new DalConnectionProvider());
    }

    @Override
    public void jobExecutionVetoed(JobExecutionContext context) {
      // NOOP
    }

    @Override
    public void jobWasExecuted(JobExecutionContext context, JobExecutionException jobException) {
      String key = context.getJobDetail().getKey().getName();
      jobExecutions.putIfAbsent(key, 0);
      jobExecutions.compute(key, (k, v) -> v + 1);
    }

    public int getJobExecutions(String name) {
      return jobExecutions.getOrDefault(name, 0);
    }
  }

  /** Empty process used to determine whether jobs are executed by the scheduler */
  public static class EmptyProcess extends DalBaseProcess {
    @Override
    protected void doExecute(ProcessBundle bundle) throws Exception {
      // Do nothing
    }
  }

  /**
   * Returns org.quartz.jobStore.misfireThreshold property to allow jobs to wait certain time before
   * misfire happens
   * 
   * @return misfireThreshold property from config/quartz.properties file
   */
  private static Integer getMisfireThreshold() {
    if (misfireThreshold != null) {
      return misfireThreshold;
    }
    String pathToConfig = OBPropertiesProvider.getInstance()
        .getOpenbravoProperties()
        .getProperty("source.path") + File.separator + "config" + File.separator
        + "quartz.properties";
    try (FileInputStream file = new FileInputStream(pathToConfig)) {
      Properties p = new Properties();
      p.load(file);
      String misfireProperty = p.getProperty("org.quartz.jobStore.misfireThreshold");
      misfireThreshold = Integer.parseInt(misfireProperty);
      return misfireThreshold;
    } catch (IOException e) {
      // File has not been found, return default MisfireThreshold of 60 seconds
      return 60000;
    }

  }
}
