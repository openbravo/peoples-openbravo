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

import static org.openbravo.scheduling.OBScheduler.OB_GROUP;
import static org.quartz.CalendarIntervalScheduleBuilder.calendarIntervalSchedule;
import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.servlet.ServletException;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.utility.StringCollectionUtils;
import org.openbravo.erpCommon.utility.Utility;
import org.quartz.DateBuilder.IntervalUnit;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;

/**
 * Provides {@link Trigger} instances configured with the timing options and schedules defined with
 * a process request.
 */
class TriggerProvider {
  private static final Logger log = LogManager.getLogger();

  private static final TriggerProvider INSTANCE = new TriggerProvider();

  private static final String FINISHES = "Y";

  private static final String WEEKDAYS = "D";

  private static final String WEEKENDS = "E";

  private static final String EVERY_N_DAYS = "N";

  private static final String MONTH_OPTION_FIRST = "1";

  private static final String MONTH_OPTION_SECOND = "2";

  private static final String MONTH_OPTION_THIRD = "3";

  private static final String MONTH_OPTION_FOURTH = "4";

  private static final String MONTH_OPTION_LAST = "L";

  private static final String MONTH_OPTION_SPECIFIC = "S";

  private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd-MM-yyyy");

  private TriggerProvider() {
  }

  static TriggerProvider getInstance() {
    return INSTANCE;
  }

  /**
   * Loads the trigger details from AD_PROCESS_REQUEST and converts them into a schedulable Quartz
   * Trigger instance.
   * 
   * @param requestId
   *          the ID of the AD_PROCESS_REQUEST. It is also used as the name element for the
   *          Trigger's TriggerKey
   * @param bundle
   *          the ProcessBundle to be included into the Trigger's job data map
   * @param conn
   *          the ConnectionProvider used to load the AD_PROCESS_REQUEST information
   * @return a Trigger instance configured according to the AD_PROCESS_REQUEST information retrieved
   *         from database
   */
  Trigger createTrigger(String requestId, ProcessBundle bundle, ConnectionProvider conn)
      throws ServletException {
    TriggerData data = getTriggerData(requestId, bundle, conn);

    Trigger trigger = getTriggerBuilder(data, bundle, conn).withIdentity(requestId, OB_GROUP)
        .build();
    trigger.getJobDataMap().put(ProcessBundle.KEY, bundle);

    log.debug("Scheduled process {} {}. Start time: {}.",
        () -> data != null ? data.processName : "unknown",
        () -> data != null ? data.processGroupName : "", trigger::getStartTime);

    return trigger;
  }

  private TriggerData getTriggerData(String requestId, ProcessBundle bundle,
      ConnectionProvider conn) throws ServletException {
    if (bundle.isGroup()) {
      return TriggerData.selectGroup(conn, requestId, GroupInfo.processGroupId);
    } else {
      return TriggerData.select(conn, requestId);
    }
  }

  private TriggerBuilder<?> getTriggerBuilder(TriggerData data, ProcessBundle bundle,
      ConnectionProvider conn) throws ServletException {
    if (data == null) {
      return newTrigger().startNow();
    }

    try {
      TimingOption timingOption = getTimingOption(data);
      TriggerBuilder<?> triggerBuilder;

      switch (timingOption) {
        case IMMEDIATE:
          triggerBuilder = newTrigger().startNow();
          break;
        case LATER:
          triggerBuilder = newTrigger().startAt(getStartDate(data).getTime());
          break;
        case SCHEDULED:
          triggerBuilder = scheduledTriggerBuilder(data);
          break;
        default:
          log.error("Error scheduling process {}", data.processName + " " + data.processGroupName);
          throw new ServletException(getErrorMessage(conn, bundle, "Unrecognized timing option"));
      }

      return triggerBuilder
          .usingJobData(Process.PREVENT_CONCURRENT_EXECUTIONS, "Y".equals(data.preventconcurrent))
          .usingJobData(Process.PROCESS_NAME, data.processName + " " + data.processGroupName)
          .usingJobData(Process.PROCESS_ID, data.adProcessId);

    } catch (ParseException e) {
      log.error("Error scheduling process {}", data.processName + " " + data.processGroupName, e);
      throw new ServletException(getErrorMessage(conn, bundle, e.getMessage()));
    }
  }

  private String getErrorMessage(ConnectionProvider conn, ProcessBundle bundle, String msg) {
    String language = bundle.getContext().getLanguage();
    return Utility.messageBD(conn, "TRIG_INVALID_DATA", language) + " " + msg;
  }

  private TimingOption getTimingOption(TriggerData data) {
    String timingOption = data.timingOption;
    if ("".equals(timingOption)) {
      return TimingOption.IMMEDIATE;
    } else {
      return TimingOption.of(timingOption);
    }
  }

  private TriggerBuilder<?> scheduledTriggerBuilder(TriggerData data)
      throws ServletException, ParseException {

    TriggerBuilder<?> triggerBuilder;

    Frequency frequency = Frequency.of(data.frequency);
    switch (frequency) {
      case SECONDLY:
        triggerBuilder = secondlyScheduledTriggerBuilder(data);
        break;
      case MINUTELY:
        triggerBuilder = minutelyScheduledTriggerBuilder(data);
        break;
      case HOURLY:
        triggerBuilder = hourlyScheduledTriggerBuilder(data);
        break;
      case DAILY:
        triggerBuilder = dailyScheduledTriggerBuilder(data);
        break;
      case WEEKLY:
        triggerBuilder = weekylScheduledTriggerBuilder(data);
        break;
      case MONTHLY:
        triggerBuilder = monthlyScheduledTriggerBuilder(data);
        break;
      case CRON:
        triggerBuilder = cronScheduledTriggerBuilder(data.cron);
        break;
      default:
        throw new ServletException("Invalid option: " + data.frequency);
    }

    if (StringUtils.isEmpty(data.nextFireTime)) {
      triggerBuilder.startAt(getStartDate(data).getTime());
    } else {
      Calendar nextTriggerTime = timestamp(data.nextFireTime, data.nextFireTime);
      triggerBuilder.startAt(nextTriggerTime.getTime());
    }

    if (FINISHES.equals(data.finishes)) {
      triggerBuilder.endAt(getFinishDate(data).getTime());
    }

    return triggerBuilder;
  }

  private TriggerBuilder<SimpleTrigger> secondlyScheduledTriggerBuilder(TriggerData data) {
    if (StringUtils.isBlank(data.secondlyRepetitions)) {
      return newTrigger().withSchedule(
          SimpleScheduleBuilder.repeatSecondlyForever(Integer.parseInt(data.secondlyInterval)));
    } else {
      return newTrigger().withSchedule(SimpleScheduleBuilder.repeatSecondlyForTotalCount(
          Integer.parseInt(data.secondlyRepetitions), Integer.parseInt(data.secondlyInterval)));
    }
  }

  private TriggerBuilder<SimpleTrigger> minutelyScheduledTriggerBuilder(TriggerData data) {
    if (StringUtils.isBlank(data.minutelyRepetitions)) {
      return newTrigger().withSchedule(
          SimpleScheduleBuilder.repeatMinutelyForever(Integer.parseInt(data.minutelyInterval)));
    } else {
      return newTrigger().withSchedule(SimpleScheduleBuilder.repeatMinutelyForTotalCount(
          Integer.parseInt(data.minutelyRepetitions), Integer.parseInt(data.minutelyInterval)));
    }
  }

  private TriggerBuilder<SimpleTrigger> hourlyScheduledTriggerBuilder(TriggerData data) {
    if (StringUtils.isBlank(data.hourlyRepetitions)) {
      return newTrigger().withSchedule(
          SimpleScheduleBuilder.repeatHourlyForever(Integer.parseInt(data.hourlyInterval)));
    } else {
      return newTrigger().withSchedule(SimpleScheduleBuilder.repeatHourlyForTotalCount(
          Integer.parseInt(data.hourlyRepetitions), Integer.parseInt(data.hourlyInterval)));
    }
  }

  private TriggerBuilder<?> dailyScheduledTriggerBuilder(TriggerData data) throws ParseException {

    if (StringUtils.isEmpty(data.dailyOption)) {
      String cronExpression = getCronTime(data) + " ? * *";
      return cronScheduledTriggerBuilder(cronExpression);
    } else if (data.dailyOption.equals(EVERY_N_DAYS)) {
      try {
        int interval = Integer.parseInt(data.dailyInterval);
        return newTrigger()
            .withSchedule(calendarIntervalSchedule().withInterval(interval, IntervalUnit.DAY));

      } catch (NumberFormatException e) {
        throw new ParseException("Invalid daily interval specified.", -1);
      }
    } else if (data.dailyOption.equals(WEEKDAYS)) {
      String cronExpression = getCronTime(data) + " ? * MON-FRI";
      return cronScheduledTriggerBuilder(cronExpression);
    } else if (data.dailyOption.equals(WEEKENDS)) {
      String cronExpression = getCronTime(data) + " ? * SAT,SUN";
      return cronScheduledTriggerBuilder(cronExpression);
    } else {
      throw new ParseException("At least one option must be selected.", -1);
    }
  }

  private TriggerBuilder<?> weekylScheduledTriggerBuilder(TriggerData data) throws ParseException {
    List<String> days = new ArrayList<>();
    if (data.daySun.equals("Y")) {
      days.add("SUN");
    }
    if (data.dayMon.equals("Y")) {
      days.add("MON");
    }
    if (data.dayTue.equals("Y")) {
      days.add("TUE");
    }
    if (data.dayWed.equals("Y")) {
      days.add("WED");
    }
    if (data.dayThu.equals("Y")) {
      days.add("THU");
    }
    if (data.dayFri.equals("Y")) {
      days.add("FRI");
    }
    if (data.daySat.equals("Y")) {
      days.add("SAT");
    }

    if (!days.isEmpty()) {
      StringBuilder sb = new StringBuilder();
      sb.append(StringCollectionUtils.commaSeparated(days, false));
      sb.insert(0, getCronTime(data) + " ? * ");
      return cronScheduledTriggerBuilder(sb.toString());
    } else {
      throw new ParseException("At least one day must be selected.", -1);
    }
  }

  private TriggerBuilder<?> monthlyScheduledTriggerBuilder(TriggerData data) throws ParseException {
    StringBuilder sb = new StringBuilder();
    sb.append(getCronTime(data) + " ");

    if (data.monthlyOption.equals(MONTH_OPTION_FIRST)
        || data.monthlyOption.equals(MONTH_OPTION_SECOND)
        || data.monthlyOption.equals(MONTH_OPTION_THIRD)
        || data.monthlyOption.equals(MONTH_OPTION_FOURTH)) {
      int day = Integer.parseInt(data.monthlyDayOfWeek) + 1;
      sb.append("? * " + (day > 7 ? 1 : day) + "#" + data.monthlyOption);
    } else if (data.monthlyOption.equals(MONTH_OPTION_LAST)) {
      sb.append("L * ?");
    } else if (data.monthlyOption.equals(MONTH_OPTION_SPECIFIC)) {
      sb.append(Integer.parseInt(data.monthlySpecificDay) + " * ?");
    } else {
      throw new ParseException("At least one month option be selected.", -1);
    }
    return cronScheduledTriggerBuilder(sb.toString());
  }

  private TriggerBuilder<?> cronScheduledTriggerBuilder(String cron) {
    return newTrigger().withSchedule(cronSchedule(cron).withMisfireHandlingInstructionDoNothing());
  }

  private Calendar getStartDate(TriggerData data) throws ParseException {
    return timestamp(data.startDate, data.startTime);
  }

  private Calendar getFinishDate(TriggerData data) throws ParseException {
    return timestamp(data.finishesDate, data.finishesTime);
  }

  private String getCronTime(TriggerData data) throws ParseException {
    Calendar start = getStartDate(data);

    int second = start.get(Calendar.SECOND);
    int minute = start.get(Calendar.MINUTE);
    int hour = start.get(Calendar.HOUR_OF_DAY);

    return second + " " + minute + " " + hour;
  }

  /**
   * Utility method to parse a start date string and a start time string into a date.
   * 
   * Expected format for dates: 'dd-MM-yyyy' Expected format for times: 'HH24:MI:SS'
   * 
   * @throws ParseException
   */
  private Calendar timestamp(String date, String time) throws ParseException {
    Calendar cal = null;

    if (date == null || date.equals("")) {
      cal = Calendar.getInstance();
    } else {
      cal = Calendar.getInstance();
      cal.setTime(DATE_FORMAT.parse(date));
    }

    if (time != null && !time.equals("")) {
      int hour = Integer.parseInt(time.substring(time.indexOf(' ') + 1, time.indexOf(':')));
      int minute = Integer.parseInt(time.substring(time.indexOf(':') + 1, time.lastIndexOf(':')));
      int second = Integer.parseInt(time.substring(time.lastIndexOf(':') + 1, time.length()));

      cal.set(Calendar.HOUR_OF_DAY, hour);
      cal.set(Calendar.MINUTE, minute);
      cal.set(Calendar.SECOND, second);
    }

    return cal;
  }
}
