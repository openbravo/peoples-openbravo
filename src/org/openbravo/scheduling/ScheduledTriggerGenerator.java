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

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

import java.text.ParseException;
import java.util.Calendar;

import org.apache.commons.lang.StringUtils;
import org.quartz.CronTrigger;
import org.quartz.TriggerBuilder;

/**
 * A generator of Quartz's Triggers with an scheduled frequency. Classes extending this one should
 * implement the scheduled frequency in particular for the generated Triggers.
 */
abstract class ScheduledTriggerGenerator extends TriggerGenerator {

  private static final String FINISHES = "Y";

  abstract TriggerBuilder<?> getScheduledBuilder(TriggerData data) throws ParseException;

  @Override
  public TriggerBuilder<?> getBuilder(TriggerData data) throws ParseException {
    TriggerBuilder<?> triggerBuilder = getScheduledBuilder(data);
    if (StringUtils.isEmpty(data.nextFireTime)) {
      triggerBuilder.startAt(getStartDate(data).getTime());
    } else {
      triggerBuilder.startAt(getNextFireDate(data).getTime());
    }

    if (FINISHES.equals(data.finishes)) {
      triggerBuilder.endAt(getFinishDate(data).getTime());
    }

    return triggerBuilder;
  }

  private Calendar getStartDate(TriggerData data) throws ParseException {
    return timestamp(data.startDate, data.startTime);
  }

  private Calendar getFinishDate(TriggerData data) throws ParseException {
    return timestamp(data.finishesDate, data.finishesTime);
  }

  private Calendar getNextFireDate(TriggerData data) throws ParseException {
    return timestamp(data.nextFireTime, data.nextFireTime);
  }

  protected String getCronTime(TriggerData data) throws ParseException {
    Calendar start = getStartDate(data);

    int second = start.get(Calendar.SECOND);
    int minute = start.get(Calendar.MINUTE);
    int hour = start.get(Calendar.HOUR_OF_DAY);

    return second + " " + minute + " " + hour;
  }

  protected TriggerBuilder<CronTrigger> cronScheduledTriggerBuilder(String cron) {
    return newTrigger().withSchedule(cronSchedule(cron).withMisfireHandlingInstructionDoNothing());
  }
}
