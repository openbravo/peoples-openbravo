/*
 ************************************************************************************
 * Copyright (C) 2018-2019 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.retail.posterminal.utility;

import java.util.Calendar;
import java.util.Date;

import org.openbravo.erpCommon.utility.OBMessageUtils;

public class CustomerStatisticsUtils {

  public static Date getStartDateFromTimingUnit(String timing, Long timingUnit) {
    Date startDate = null;
    Calendar cal = Calendar.getInstance();

    if (timing.equals("H")) {
      cal.add(Calendar.HOUR, -timingUnit.intValue());
      startDate = cal.getTime();
    } else if (timing.equals("D")) {
      cal.add(Calendar.DATE, -timingUnit.intValue());
      startDate = cal.getTime();
    } else if (timing.equals("W")) {
      cal.add(Calendar.DATE, -(timingUnit.intValue()) * 7);
      startDate = cal.getTime();
    } else if (timing.equals("M")) {
      cal.add(Calendar.MONTH, -timingUnit.intValue());
      startDate = cal.getTime();
    } else if (timing.equals("Y")) {
      cal.add(Calendar.YEAR, -timingUnit.intValue());
      startDate = cal.getTime();
    }
    return startDate;
  }

  public static String getTimingText(Long timingUnit, String timing) {
    String timingText = "";
    if (timingUnit.compareTo(1L) > 0) {
      if (timing.equalsIgnoreCase("H")) {
        timingText = OBMessageUtils.messageBD("OBPOS_Hours");
      } else if (timing.equalsIgnoreCase("D")) {
        timingText = OBMessageUtils.messageBD("OBPOS_Days");
      } else if (timing.equalsIgnoreCase("W")) {
        timingText = OBMessageUtils.messageBD("OBPOS_Weeks");
      } else if (timing.equalsIgnoreCase("M")) {
        timingText = OBMessageUtils.messageBD("OBPOS_Months");
      } else if (timing.equalsIgnoreCase("Y")) {
        timingText = OBMessageUtils.messageBD("OBPOS_Years");
      }
    } else {
      if (timing.equalsIgnoreCase("H")) {
        timingText = OBMessageUtils.messageBD("OBPOS_Hour");
      } else if (timing.equalsIgnoreCase("D")) {
        timingText = OBMessageUtils.messageBD("OBPOS_Day");
      } else if (timing.equalsIgnoreCase("W")) {
        timingText = OBMessageUtils.messageBD("OBPOS_Week");
      } else if (timing.equalsIgnoreCase("M")) {
        timingText = OBMessageUtils.messageBD("OBPOS_Month");
      } else if (timing.equalsIgnoreCase("Y")) {
        timingText = OBMessageUtils.messageBD("OBPOS_Year");
      }
    }
    return timingText;
  }

}
