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

import org.quartz.CronTrigger;
import org.quartz.TriggerBuilder;

/**
 * A generator of Quartz's Triggers with monthly frequency.
 */
class MonthlyTriggerGenerator extends ScheduledTriggerGenerator {

  private static final String MONTH_OPTION_FIRST = "1";
  private static final String MONTH_OPTION_SECOND = "2";
  private static final String MONTH_OPTION_THIRD = "3";
  private static final String MONTH_OPTION_FOURTH = "4";
  private static final String MONTH_OPTION_LAST = "L";
  private static final String MONTH_OPTION_SPECIFIC = "S";

  @Override
  TriggerBuilder<CronTrigger> getScheduledBuilder(TriggerData data) throws ParseException {
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

}
