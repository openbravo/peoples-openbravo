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

public class SequenceUtilControlDigitModule10Test extends SequenceTest {

  /**
   * Test documentNo from sequence with module 10 control digit, no prefix, no suffix, no
   * nextAssignedNo, sequence number length variable
   */
  @Test
  public void sequenceUtilTest_AutoNumbering() {
    final Sequence sequence = SequenceTestUtils.createDocumentSequence(UUID.randomUUID().toString(),
        CalculationMethod.AUTONUMERING, null, null, null, null, null, null, ControlDigit.MODULE10,
        SequenceNumberLength.VARIABLE, null, true);
    assertThat("Sequence is not computed correctly using SequenceUtil for AutoNumbering "
        + "calculation method, no prefix, no suffix, no nextAssignedNo and Control Digit - Module 10",
        "17", equalTo(SequenceUtil.getDocumentNo(false, sequence)));
  }

  /**
   * Test documentNo from sequence with module 10 control digit, no prefix, no suffix,
   * nextAssignedNo:1000000L, sequence number length variable
   */
  @Test
  public void sequenceUtilTest_AutoNumbering_NextAssignedNo() {
    final Sequence sequence = SequenceTestUtils.createDocumentSequence(UUID.randomUUID().toString(),
        CalculationMethod.AUTONUMERING, null, null, null, 1000000L, null, null,
        ControlDigit.MODULE10, SequenceNumberLength.VARIABLE, null, true);
    assertThat(
        "Sequence is not computed correctly using SequenceUtil for AutoNumbering calculation method, "
            + "valid nextAssignedNo, no prefix, no suffix and Control Digit - Module 10",
        "10000007", equalTo(SequenceUtil.getDocumentNo(false, sequence)));
  }

  /**
   * Test documentNo from sequence with module 10 control digit and prefix: 6, no suffix,
   * nextAssignedNo:1000000L, sequence number length variable
   */
  @Test
  public void sequenceUtilTest_AutoNumbering_NextAssignedNo_Prefix() {
    final Sequence sequence = SequenceTestUtils.createDocumentSequence(UUID.randomUUID().toString(),
        CalculationMethod.AUTONUMERING, null, "6", null, 1000000L, null, null,
        ControlDigit.MODULE10, SequenceNumberLength.VARIABLE, null, true);
    assertThat(
        "Sequence is not computed correctly using SequenceUtil for AutoNumbering calculation method, "
            + "valid prefix, nextAssignedNo, no suffix and Control Digit - Module 10",
        "610000001", equalTo(SequenceUtil.getDocumentNo(false, sequence)));
  }

  /**
   * Test documentNo from sequence with module 10 control digit and no prefix, suffix 000,
   * nextAssignedNo:1000000L, sequence number length variable
   */
  @Test
  public void sequenceUtilTest_AutoNumbering_NextAssignedNo_Suffix() {
    final Sequence sequence = SequenceTestUtils.createDocumentSequence(UUID.randomUUID().toString(),
        CalculationMethod.AUTONUMERING, null, null, null, 1000000L, null, "000",
        ControlDigit.MODULE10, SequenceNumberLength.VARIABLE, null, true);
    assertThat(
        "Sequence is not computed correctly using SequenceUtil for AutoNumbering calculation method, "
            + "valid suffix, nextAssignedNo, no prefix, Control Digit - MOdule 10",
        "10000000009", equalTo(SequenceUtil.getDocumentNo(false, sequence)));
  }

  /**
   * Test documentNo from sequence with module 10 control digit, prefix 6, suffix 000,
   * nextAssignedNo:1000000L, sequence number length variable
   */
  @Test
  public void sequenceUtilTest_AutoNumbering_NextAssignedNo_PrefixSuffix() {
    final Sequence sequence = SequenceTestUtils.createDocumentSequence(UUID.randomUUID().toString(),
        CalculationMethod.AUTONUMERING, null, "6", null, 1000000L, null, "000",
        ControlDigit.MODULE10, SequenceNumberLength.VARIABLE, null, true);
    assertThat(
        "Sequence is not computed correctly using SequenceUtil for AutoNumbering calculation method,"
            + "valid prefix, suffix, nextAssignedNo and Control Digit - Module 10",
        "610000000001", equalTo(SequenceUtil.getDocumentNo(false, sequence)));
  }

  /**
   * Test documentno from sequence with module 10 control digit, prefix 7, suffix 000,
   * nextAssignedNo:1023045L, sequence number length fixed, sequence length 10L
   */
  @Test
  public void sequenceUtilTest_AutoNumbering_NextAssignedNo_PrefixSuffix_FixedLength_GreaterThan_ComputedSequenceLength() {
    final Sequence sequence = SequenceTestUtils.createDocumentSequence(UUID.randomUUID().toString(),
        CalculationMethod.AUTONUMERING, null, "7", null, 1023045L, null, "000",
        ControlDigit.MODULE10, SequenceNumberLength.FIXED, 10L, true);
    assertThat(
        "Sequence is not computed correctly using SequenceUtil for AutoNumbering calculation method, "
            + "valid prefix, suffix, nextAssignedNo, Control Digit - Module 10, Fixed Length 10L",
        "700010230450004", equalTo(SequenceUtil.getDocumentNo(false, sequence)));
  }

  /**
   * Test documentno from sequence with module 10 control digit, prefix 8, suffix 000,
   * nextAssignedNo:1025045L, sequence number length fixed, sequence length 5L
   */
  @Test
  public void sequenceUtilTest_AutoNumbering_NextAssignedNo_PrefixSuffix_FixedLength_LessThan_ComputedSequenceLength() {
    final Sequence sequence = SequenceTestUtils.createDocumentSequence(UUID.randomUUID().toString(),
        CalculationMethod.AUTONUMERING, null, "8", null, 1025045L, null, "000",
        ControlDigit.MODULE10, SequenceNumberLength.FIXED, 5L, true);
    assertThat(
        "Sequence is not computed correctly using SequenceUtil for AutoNumbering calculation method, "
            + "valid prefix, suffix, nextAssignedNo, Control Digit - Module 10, Fixed Length 5L",
        "810250450001", equalTo(SequenceUtil.getDocumentNo(false, sequence)));
  }
}
