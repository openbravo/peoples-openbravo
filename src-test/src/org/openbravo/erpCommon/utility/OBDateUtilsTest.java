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
 * All portions are Copyright (C) 2022 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
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
   * Test truncate method with specified unit.
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

  /**
   * Tests getCurrentClientDate method
   * 
   * @throws ParseException
   */
  @Test
  public void getCurrentClientDate() throws ParseException {

    String strDate = "2022-10-02T22:00:00";
    String strDateResult = "Mon Oct 03 00:00:00 CEST 2022";

    Date actualDate = OBDateUtils.getCurrentClientDate(strDate);

    assertThat(actualDate.toString(), equalTo(strDateResult.toString()));

  }

}
