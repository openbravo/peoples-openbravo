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
 * All portions are Copyright (C) 2024 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.test.documentsequence;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertTrue;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.openbravo.erpCommon.utility.SequenceUtil;
import org.openbravo.erpCommon.utility.SequenceUtil.CalculationMethod;
import org.openbravo.erpCommon.utility.SequenceUtil.ControlDigit;
import org.openbravo.erpCommon.utility.SequenceUtil.SequenceNumberLength;
import org.openbravo.model.ad.utility.Sequence;

public class SequenceUtilSequenceTest extends SequenceTest {

  @Test
  public void sequenceUtilTest_a() {
    final Sequence sequence = SequenceTestUtils.createDocumentSequence(
        CalculationMethod.AUTONUMERING, null, null, null, 1000000L, null, null, ControlDigit.NONE,
        SequenceNumberLength.VARIABLE, null, true);
    assertTrue(
        "Sequence is not computed correctly using SequenceUtil for AutoNumbering calculation method, next Assigned No. and Control Digit - None",
        StringUtils.equals("1000000", SequenceUtil.getDocumentNo(false, sequence)));
  }

  @Test
  public void sequenceUtilTest_b() {
    final Sequence sequence = SequenceTestUtils.createDocumentSequence(
        CalculationMethod.AUTONUMERING, null, "SUT/", null, 1000000L, null, null, ControlDigit.NONE,
        SequenceNumberLength.VARIABLE, null, true);
    assertThat(
        "Sequence is not computed correctly using SequenceUtil for AutoNumbering calculation method, prefix, next Assigned No and Control Digit - None",
        "SUT/1000000", equalTo(SequenceUtil.getDocumentNo(false, sequence)));
  }

  @Test
  public void sequenceUtilTest_c() {
    final Sequence sequence = SequenceTestUtils.createDocumentSequence(
        CalculationMethod.AUTONUMERING, null, null, null, 1000000L, null, "000", ControlDigit.NONE,
        SequenceNumberLength.VARIABLE, null, true);
    assertThat(
        "Sequence is not computed correctly using SequenceUtil for AutoNumbering calculation method, suffix, next Assigned No and Control Digit - None",
        "1000000000", equalTo(SequenceUtil.getDocumentNo(false, sequence)));
  }

  @Test
  public void sequenceUtilTest_d() {
    final Sequence sequence = SequenceTestUtils.createDocumentSequence(
        CalculationMethod.AUTONUMERING, null, "SUT/", null, 1000000L, null, "000",
        ControlDigit.NONE, SequenceNumberLength.VARIABLE, null, true);
    assertThat(
        "Sequence is not computed correctly using SequenceUtil for AutoNumbering calculation method, prefix, suffix, next Assigned No and Control Digit - None",
        "SUT/1000000000", equalTo(SequenceUtil.getDocumentNo(false, sequence)));
  }

  @Test
  public void sequenceUtilTest_e() {
    final Sequence sequence = SequenceTestUtils.createDocumentSequence(
        CalculationMethod.AUTONUMERING, null, "6", null, 1000000L, null, "000",
        ControlDigit.MODULE10, SequenceNumberLength.VARIABLE, null, true);
    assertThat(
        "Sequence is not computed correctly using SequenceUtil for AutoNumbering calculation method, prefix, suffix, next Assigned No and Control Digit - Module 10",
        "610000000001", equalTo(SequenceUtil.getDocumentNo(false, sequence)));
  }

  @Test
  public void sequenceUtilTest_f() {
    final Sequence sequence = SequenceTestUtils.createDocumentSequence(
        CalculationMethod.AUTONUMERING, null, null, null, null, null, null, ControlDigit.NONE,
        SequenceNumberLength.VARIABLE, null, true);
    assertThat(
        "Sequence is not computed correctly using SequenceUtil for AutoNumbering calculation method, prefix, suffix, next Assigned No and Control Digit - None",
        "1", equalTo(SequenceUtil.getDocumentNo(false, sequence)));
  }

  @Test
  public void sequenceUtilTest_g() {
    final Sequence sequence = SequenceTestUtils.createDocumentSequence(
        CalculationMethod.AUTONUMERING, null, null, null, null, null, null, ControlDigit.MODULE10,
        SequenceNumberLength.VARIABLE, null, true);
    assertThat(
        "Sequence is not computed correctly using SequenceUtil for AutoNumbering calculation method, prefix, suffix, next Assigned No and Control Digit - Module 10",
        "17", equalTo(SequenceUtil.getDocumentNo(false, sequence)));
  }
}
