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

import static org.quartz.CalendarIntervalScheduleBuilder.calendarIntervalSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

import java.text.ParseException;

import org.apache.commons.lang.StringUtils;
import org.openbravo.scheduling.TriggerProvider.Timing;
import org.quartz.DateBuilder.IntervalUnit;
import org.quartz.TriggerBuilder;

/**
 * A generator of Quartz's Triggers with daily frequency.
 */
@Timing("S4")
class DailyTriggerGenerator extends ScheduledTriggerGenerator {

  private static final String WEEKDAYS = "D";
  private static final String WEEKENDS = "E";
  private static final String EVERY_N_DAYS = "N";

  @Override
  TriggerBuilder<?> getScheduledBuilder(TriggerData data) throws ParseException {
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

}
