/*
 ************************************************************************************
 * Copyright (C) 2022 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.erpCommon.utility;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import java.text.ParseException;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import org.junit.Test;
import org.openbravo.service.json.JsonUtils;
import org.openbravo.test.base.OBBaseTest;

/**
 * Tests the {@link OBDateUtils} class
 */
public class OBDateUtilsTest extends OBBaseTest {

  /**
   * Test truncate method without specified unit.
   * 
   * @throws ParseException
   */
  @Test
  public void truncateTimeInDate() throws ParseException {

    String strDateWithTime = "2022-07-01T19:55:55+0200";
    String strDateWithoutTime = "2022-07-01";

    Date dateWithTime = JsonUtils.createDateTimeFormat().parse(strDateWithTime);
    Date dateWithoutTime = JsonUtils.createDateFormat().parse(strDateWithoutTime);
    Date dateTrucanted = OBDateUtils.truncate(dateWithTime);

    assertThat(dateTrucanted, equalTo(dateWithoutTime));
  }

  /**
   * Test truncate method wit specified unit.
   * 
   * @throws ParseException
   */
  @Test
  public void truncateMinutesInDate() throws ParseException {

    String strDateWithMinutes = "2022-07-01T19:55:55+0200";
    String strDateWithoutMinutes = "2022-07-01T19:00:00+0200";

    Date dateWithMinutes = JsonUtils.createDateTimeFormat().parse(strDateWithMinutes);
    Date dateWithoutMinutes = JsonUtils.createDateTimeFormat().parse(strDateWithoutMinutes);
    Date dateTrucanted = OBDateUtils.truncate(dateWithMinutes, ChronoUnit.HOURS);

    assertThat(dateTrucanted, equalTo(dateWithoutMinutes));
  }

}
