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

  private enum DailyOption {
    WEEKDAYS("D"), WEEKENDS("E"), EVERY_N_DAYS("N");

    private String label;

    private DailyOption(String label) {
      this.label = label;
    }

    static DailyOption of(String label) {
      for (DailyOption dailyOption : values()) {
        if (dailyOption.label.equals(label)) {
          return dailyOption;
        }
      }
      return null;
    }
  }

  @Override
  TriggerBuilder<?> getScheduledBuilder(TriggerData data) throws ParseException {
    if (StringUtils.isEmpty(data.dailyOption)) {
      return cronScheduledTriggerBuilder(getCronTime(data) + " ? * *");
    }

    DailyOption dailyOption = DailyOption.of(data.dailyOption);
    if (dailyOption == null) {
      throw new ParseException("At least one daily option must be selected.", -1);
    }

    switch (dailyOption) {
      case EVERY_N_DAYS:
        try {
          return newTrigger().withSchedule(calendarIntervalSchedule()
              .withInterval(Integer.parseInt(data.dailyInterval), IntervalUnit.DAY));

        } catch (NumberFormatException e) {
          throw new ParseException("Invalid daily interval specified.", -1);
        }
      case WEEKDAYS:
        return cronScheduledTriggerBuilder(getCronTime(data) + " ? * MON-FRI");
      case WEEKENDS:
        return cronScheduledTriggerBuilder(getCronTime(data) + " ? * SAT,SUN");
      default:
        throw new ParseException("At least one daily option must be selected.", -1);
    }
  }

}
