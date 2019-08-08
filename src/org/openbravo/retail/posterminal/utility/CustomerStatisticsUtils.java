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
      cal.add(Calendar.DATE, -(timingUnit.intValue())*7);
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

}
