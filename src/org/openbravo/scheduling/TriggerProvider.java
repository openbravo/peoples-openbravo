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
package org.openbravo.scheduling;

import java.text.ParseException;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.servlet.ServletException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.utility.Utility;
import org.quartz.Trigger;

/**
 * Provides Quartz's Trigger instances configured with the timing options and schedules defined with
 * a process request.
 */
class TriggerProvider {
  private static final Logger log = LogManager.getLogger();
  private static final TriggerProvider INSTANCE = new TriggerProvider();

  private Map<String, TriggerGenerator> triggerGenerators;

  private TriggerProvider() {
    triggerGenerators = getGenerators();
  }

  private Map<String, TriggerGenerator> getGenerators() {
    return Stream
        .of(new SimpleEntry<>("I", new ImmediateTriggerGenerator()),
            new SimpleEntry<>("L", new LaterTriggerGenerator()),
            new SimpleEntry<>("S1", new SecondlyTriggerGenerator()),
            new SimpleEntry<>("S2", new MinutelyTriggerGenerator()),
            new SimpleEntry<>("S3", new HourlyTriggerGenerator()),
            new SimpleEntry<>("S4", new DailyTriggerGenerator()),
            new SimpleEntry<>("S5", new WeeklyTriggerGenerator()),
            new SimpleEntry<>("S6", new MonthlyTriggerGenerator()),
            new SimpleEntry<>("S7", new CronTriggerGenerator()))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  /**
   * @return the TriggerProvider singleton instance
   */
  static TriggerProvider getInstance() {
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
  Trigger createTrigger(String name, ProcessBundle bundle, ConnectionProvider conn)
      throws ServletException {
    TriggerData data = getTriggerData(name, bundle, conn);
    try {
      Trigger trigger = getTriggerGenerator(data, bundle, conn).generate(name, data);
      trigger.getJobDataMap().put(ProcessBundle.KEY, bundle);

      log.debug("Scheduled process {} {}. Start time: {}.",
          () -> data != null ? data.processName : "",
          () -> data != null ? data.processGroupName : "", trigger::getStartTime);

      return trigger;
    } catch (ParseException e) {
      log.error("Error scheduling process {} {}", () -> data != null ? data.processName : "",
          () -> data != null ? data.processGroupName : "", () -> e);
      throw new ServletException(getErrorMessage(conn, bundle, e.getMessage()));
    }
  }

  private TriggerData getTriggerData(String requestId, ProcessBundle bundle,
      ConnectionProvider conn) throws ServletException {
    if (bundle.isGroup()) {
      return TriggerData.selectGroup(conn, requestId, GroupInfo.processGroupId);
    } else {
      return TriggerData.select(conn, requestId);
    }
  }

  private TriggerGenerator getTriggerGenerator(TriggerData data, ProcessBundle bundle,
      ConnectionProvider conn) throws ServletException {
    String timing = getTiming(data);
    if (!triggerGenerators.containsKey(timing)) {
      log.error("Couldn't get a trigger generator for timing option {}", timing);
      throw new ServletException(
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
