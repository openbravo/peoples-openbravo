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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.enterprise.context.ApplicationScoped;

import org.apache.commons.lang.StringUtils;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;

/**
 * A generator of Quartz's Triggers. Each class extending this one should provide a
 * {@code TriggerBuilder} with the settings used to generate the Trigger.
 */
@ApplicationScoped
abstract class TriggerGenerator {

  private SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");

  /**
   * Provides the TriggerBuilder used by the {@link #generate(String, TriggerData)} method to create
   * the Trigger.
   * 
   * @param data
   *          the trigger details loaded from an AD_PROCESS_REQUEST.
   * 
   * @return the {@link TriggerBuilder} instance that will be used to create the Trigger.
   * 
   * @throws ParseException
   *           if there is an error creating the {@link TriggerBuilder} instance.
   */
  protected abstract TriggerBuilder<?> getBuilder(TriggerData data) throws ParseException;

  /**
   * Generates a Trigger with the provided name and details. Note that this method uses as group
   * element for the Trigger's TriggerKey the value of {@link OBScheduler#OB_GROUP}.
   * 
   * @param name
   *          the name element for the Trigger's TriggerKey. In general this will be the ID of the
   *          AD_PROCESS_REQUEST.
   * @param data
   *          the trigger details loaded from the corresponding AD_PROCESS_REQUEST.
   * 
   * @return a {@link Trigger} instance configured according to the provided information.
   * 
   * @throws ParseException
   *           if there is an error creating the {@link Trigger} instance.
   */
  Trigger generate(String name, TriggerData data) throws ParseException {
    TriggerBuilder<?> builder = getBuilder(data).withIdentity(name, OB_GROUP);
    if (data != null) {
      builder
          .usingJobData(Process.PREVENT_CONCURRENT_EXECUTIONS, "Y".equals(data.preventconcurrent))
          .usingJobData(Process.PROCESS_NAME, data.processName + " " + data.processGroupName)
          .usingJobData(Process.PROCESS_ID, data.adProcessId);
    }
    return builder.build();
  }

  /**
   * Utility method to parse a start date string and a start time string into a date.
   * 
   * @param date
   *          A date as a String. Expected format: 'dd-MM-yyyy'
   * 
   * @param time
   *          A time as a String. Expected format: 'HH24:MI:SS'
   * 
   * @return a {@link Calendar} with the provided date and time.
   * 
   * @throws ParseException
   *           if the provided date and time can not be parsed to create the {@link Calendar}
   *           instance.
   */
  protected Calendar timestamp(String date, String time) throws ParseException {
    Calendar cal = Calendar.getInstance();

    if (StringUtils.isNotBlank(date)) {
      synchronized (dateFormat) {
        cal.setTime(dateFormat.parse(date));
      }
    }

    if (StringUtils.isNotBlank(time)) {
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
