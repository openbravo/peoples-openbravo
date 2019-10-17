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
 * All portions are Copyright (C) 2019 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.scheduling.trigger;

import static org.openbravo.scheduling.GroupInfo.processGroupId;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.scheduling.Frequency;
import org.openbravo.scheduling.ProcessBundle;
import org.openbravo.scheduling.TimingOption;
import org.quartz.Trigger;

/**
 * Provides Quartz's Trigger instances configured with the timing options and schedules defined with
 * a process request.
 */
public class TriggerProvider {
  private static final Logger log = LogManager.getLogger();
  private static final TriggerProvider INSTANCE = new TriggerProvider();

  private Map<String, TriggerGenerator> triggerGenerators;

  private TriggerProvider() {
    triggerGenerators = getGenerators();
  }

  private Map<String, TriggerGenerator> getGenerators() {
    Map<String, TriggerGenerator> generators = new HashMap<>(9);
    generators.put("I", new ImmediateTriggerGenerator());
    generators.put("L", new LaterTriggerGenerator());
    generators.put("S1", new SecondlyTriggerGenerator());
    generators.put("S2", new MinutelyTriggerGenerator());
    generators.put("S3", new HourlyTriggerGenerator());
    generators.put("S4", new DailyTriggerGenerator());
    generators.put("S5", new WeeklyTriggerGenerator());
    generators.put("S6", new MonthlyTriggerGenerator());
    generators.put("S7", new CronTriggerGenerator());
    return generators;
  }

  /**
   * @return the TriggerProvider singleton instance
   */
  public static TriggerProvider getInstance() {
    return INSTANCE;
  }

  /**
   * Loads the trigger details from AD_PROCESS_REQUEST and converts them into a schedulable Quartz
   * Trigger instance.
   * 
   * @param name
   *          The name element for the Trigger's TriggerKey. In general this will be the the ID of
   *          the AD_PROCESS_REQUEST.
   * @param bundle
   *          the ProcessBundle to be included into the Trigger's job data map
   * @param conn
   *          the ConnectionProvider used to load the AD_PROCESS_REQUEST information
   * @return a Trigger instance configured according to the AD_PROCESS_REQUEST information retrieved
   *         from database
   */
  public Trigger createTrigger(String name, ProcessBundle bundle, ConnectionProvider conn)
      throws TriggerGenerationException {
    TriggerData data = null;
    try {
      data = getTriggerData(name, bundle, conn);
      Trigger trigger = getTriggerGenerator(data, bundle, conn).generate(name, data);
      trigger.getJobDataMap().put(ProcessBundle.KEY, bundle);

      log.debug("Created quartz trigger for process {} {}. Start time: {}.", data.processName,
          data.processGroupName, trigger.getStartTime());

      return trigger;
    } catch (ParseException ex) {
      log.error("Couldn't create quartz trigger for process {} {}", data.processName,
          data.processGroupName, ex);
      throw new TriggerGenerationException(getErrorMessage(conn, bundle, ex.getMessage()));
    }
  }

  private TriggerData getTriggerData(String requestId, ProcessBundle bundle,
      ConnectionProvider conn) throws TriggerGenerationException {
    try {
      return bundle.isGroup() ? TriggerData.selectGroup(conn, requestId, processGroupId)
          : TriggerData.select(conn, requestId);
    } catch (ServletException ex) {
      throw new TriggerGenerationException("Error retrieving Trigger data: " + ex.getMessage());
    }
  }

  private TriggerGenerator getTriggerGenerator(TriggerData data, ProcessBundle bundle,
      ConnectionProvider conn) throws TriggerGenerationException {
    String timing = getTiming(data);
    if (!triggerGenerators.containsKey(timing)) {
      log.error("Couldn't get a trigger generator for timing option {}", timing);
      throw new TriggerGenerationException(
          getErrorMessage(conn, bundle, "Unrecognized timing option " + timing));
    }
    return triggerGenerators.get(timing);
  }

  private String getTiming(TriggerData data) {
    TimingOption timingOption = getTimingOption(data);
    if (timingOption == TimingOption.SCHEDULED) {
      String frequency = Frequency.of(data.frequency).map(Frequency::getLabel).orElse("");
      return timingOption.getLabel() + frequency;
    } else {
      return timingOption.getLabel();
    }
  }

  private TimingOption getTimingOption(TriggerData data) {
    if (data == null) {
      return TimingOption.IMMEDIATE;
    } else {
      return TimingOption.of(data.timingOption).orElse(TimingOption.IMMEDIATE);
    }
  }

  private String getErrorMessage(ConnectionProvider conn, ProcessBundle bundle, String msg) {
    String language = bundle.getContext().getLanguage();
    return Utility.messageBD(conn, "TRIG_INVALID_DATA", language) + " " + msg;
  }
}
