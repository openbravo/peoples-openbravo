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

import java.util.UUID;

import org.junit.Test;
import org.openbravo.erpCommon.utility.SequenceUtil;
import org.openbravo.erpCommon.utility.SequenceUtil.CalculationMethod;
import org.openbravo.erpCommon.utility.SequenceUtil.ControlDigit;
import org.openbravo.erpCommon.utility.SequenceUtil.SequenceNumberLength;
import org.openbravo.model.ad.utility.Sequence;

public class SequenceUtilControlDigitNoneTest extends SequenceTest {

  /**
   * Test documentNo from sequence with none control digit and no prefix, no suffix, no
   * nextAssignedNo, sequence number length variable
   */
  @Test
  public void sequenceUtilTest_AutoNumbering() {
    final Sequence sequence = SequenceTestUtils.createDocumentSequence(UUID.randomUUID().toString(),
        CalculationMethod.AUTONUMERING, null, null, null, null, null, null, ControlDigit.NONE,
        SequenceNumberLength.VARIABLE, null, true);
    assertThat(
        "Sequence is not computed correctly using SequenceUtil for AutoNumbering calculation method, "
            + "no prefix, no suffix, no nextAssignedNo, ControlDigit - None",
        "1", equalTo(SequenceUtil.getDocumentNo(false, sequence)));
  }

  /**
   * Test documentNo from sequence with none control digit and no prefix, no suffix, nextAssignedNo:
   * 1000000L, sequence number length variable
   */
  @Test
  public void sequenceUtilTest_AutoNumbering_NextAssignedNo() {
    final Sequence sequence = SequenceTestUtils.createDocumentSequence(UUID.randomUUID().toString(),
        CalculationMethod.AUTONUMERING, null, null, null, 1000000L, null, null, ControlDigit.NONE,
        SequenceNumberLength.VARIABLE, null, true);
    assertThat(
        "Sequence is not computed correctly using SequenceUtil for AutoNumbering calculation method, "
            + " valid nextAssignedNo, ControlDigit - None, no Prefix/Suffix",
        "1000000", equalTo(SequenceUtil.getDocumentNo(false, sequence)));
  }

  /**
   * Test documentNo from sequence with none control digit and prefix: SUT/, no suffix,
   * nextAssignedNo: 1000000L, sequence number length variable
   */
  @Test
  public void sequenceUtilTest_AutoNumbering_NextAssignedNo_Prefix() {
    final Sequence sequence = SequenceTestUtils.createDocumentSequence(UUID.randomUUID().toString(),
        CalculationMethod.AUTONUMERING, null, "SUT/", null, 1000000L, null, null, ControlDigit.NONE,
        SequenceNumberLength.VARIABLE, null, true);
    assertThat(
        "Sequence is not computed correctly using SequenceUtil for AutoNumbering calculation method, "
            + "valid Prefix, valid nextAssignedNo, no Suffix, ControlDigit - None",
        "SUT/1000000", equalTo(SequenceUtil.getDocumentNo(false, sequence)));
  }

  /**
   * Test documentNo from sequence with none control digit, no prefix, suffix: 000, nextAssignedNo:
   * 1000000L, sequence number length variable
   */
  @Test
  public void sequenceUtilTest_AutoNumbering_NextAssignedNo_Suffix() {
    final Sequence sequence = SequenceTestUtils.createDocumentSequence(UUID.randomUUID().toString(),
        CalculationMethod.AUTONUMERING, null, null, null, 1000000L, null, "000", ControlDigit.NONE,
        SequenceNumberLength.VARIABLE, null, true);
    assertThat(
        "Sequence is not computed correctly using SequenceUtil for AutoNumbering calculation method, "
            + "valid Suffix, valid nextAssignedNo, no Suffix, ControlDigit - None",
        "1000000000", equalTo(SequenceUtil.getDocumentNo(false, sequence)));
  }

  /**
   * Test documentNo from sequence with none control digit, prefix: SUT/, suffix: 000,
   * nextAssignedNo: 1000000L, sequence number length variable
   */
  @Test
  public void sequenceUtilTest_AutoNumbering_NextAssignedNo_PrefixSuffix() {
    final Sequence sequence = SequenceTestUtils.createDocumentSequence(UUID.randomUUID().toString(),
        CalculationMethod.AUTONUMERING, null, "SUT/", null, 1000000L, null, "000",
        ControlDigit.NONE, SequenceNumberLength.VARIABLE, null, true);
    assertThat(
        "Sequence is not computed correctly using SequenceUtil for AutoNumbering calculation method, "
            + "valid Prefix, valid Suffix, valid nextAssignedNo, ControlDigit - None",
        "SUT/1000000000", equalTo(SequenceUtil.getDocumentNo(false, sequence)));
  }

  /**
   * Test documentNo from sequence with none control digit, prefix: SUT/, suffix: 000,
   * nextAssignedNo: 1000000L, sequence number length fixed: 10L
   */
  @Test
  public void sequenceUtilTest_AutoNumbering_NextAssignedNo_PrefixSuffix_FixedLength_GreaterThan_ComputedSequenceLength() {
    final Sequence sequence = SequenceTestUtils.createDocumentSequence(UUID.randomUUID().toString(),
        CalculationMethod.AUTONUMERING, null, "SUT/", null, 1000000L, null, "000",
        ControlDigit.NONE, SequenceNumberLength.FIXED, 10L, true);
    assertThat(
        "Sequence is not computed correctly using SequenceUtil for AutoNumbering calculation method, "
            + "valid Prefix, valid Suffix, valid nextAssignedNo, ControlDigit - None, Fixed Length 10L",
        "SUT/0001000000000", equalTo(SequenceUtil.getDocumentNo(false, sequence)));
  }

  /**
   * Test documentNo from sequence with none control digit, prefix: SUT/, suffix: 000,
   * nextAssignedNo: 1000000L, sequence number length fixed: 5L
   */
  @Test
  public void sequenceUtilTest_AutoNumbering_NextAssignedNo_PrefixSuffix_FixedLength_LessThan_ComputedSequenceLength() {
    final Sequence sequence = SequenceTestUtils.createDocumentSequence(UUID.randomUUID().toString(),
        CalculationMethod.AUTONUMERING, null, "SUT/", null, 1000000L, null, "000",
        ControlDigit.NONE, SequenceNumberLength.FIXED, 5L, true);
    assertThat(
        "Sequence is not computed correctly using SequenceUtil for AutoNumbering calculation method, "
            + "valid Prefix, valid Suffix, valid nextAssignedNo, ControlDigit - None, Fixed Length 5L",
        "SUT/1000000000", equalTo(SequenceUtil.getDocumentNo(false, sequence)));
  }
}
