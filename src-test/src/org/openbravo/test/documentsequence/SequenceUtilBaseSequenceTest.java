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

public class SequenceUtilBaseSequenceTest extends SequenceTest {

  @Test

  /**
   * 2 Level Sequences
   * 
   * Test with Base Sequence having CalculationMethod: Auto numbering, Control Digit : None, No
   * NextAssignedNo, SequenceNumberLength: Variable, No Prefix, No Suffix
   * 
   * Parent Sequence having CalculationMethod: Based On Sequence, Base Sequence as above, Control
   * Digit : Module10, SequenceNumberLength: Variable, No Prefix, No Suffix
   * 
   * Compute documentNo using SequenceUtil getDocumentNo
   */
  public void sequenceUtilTest_BaseSequence_AutoNumbering_Level2() {

    final Sequence baseSequence = SequenceTestUtils.createDocumentSequence(
        UUID.randomUUID().toString(), CalculationMethod.AUTONUMERING, null, null, null, null, null,
        null, ControlDigit.NONE, SequenceNumberLength.VARIABLE, null, true);

    final Sequence parentSequence = SequenceTestUtils.createDocumentSequence(
        UUID.randomUUID().toString(), CalculationMethod.SEQUENCE, baseSequence, null, null, null,
        null, null, ControlDigit.MODULE10, SequenceNumberLength.VARIABLE, null, true);

    assertThat("Sequence is not computed correctly using SequenceUtil 2 Level Sequences", "17",
        equalTo(SequenceUtil.getDocumentNo(false, parentSequence)));
  }

  /**
   * 2 Level Sequences
   * 
   * Test with Base Sequence having CalculationMethod: Auto numbering, Control Digit : Module10,
   * NextAssignedNo: 2821L, SequenceNumberLength: Fixed, Sequence Length: 5L, Prefix: 0110491.
   * 
   * Parent Sequence having CalculationMethod: Based On Sequence, Base Sequence as above, Control
   * Digit : Module10, SequenceNumberLength: Variable, Prefix: 6, Suffix: 000
   * 
   * Compute documentNo using SequenceUtil getDocumentNo
   */

  @Test
  public void sequenceUtilTest_BaseSequence_AutoNumbering_NextAssignedNo_Level2() {

    final Sequence baseSequence = SequenceTestUtils.createDocumentSequence(
        UUID.randomUUID().toString(), CalculationMethod.AUTONUMERING, null, "0110491", null, 2821L,
        null, null, ControlDigit.MODULE10, SequenceNumberLength.FIXED, 5L, true);

    final Sequence parentSequence = SequenceTestUtils.createDocumentSequence(
        UUID.randomUUID().toString(), CalculationMethod.SEQUENCE, baseSequence, "6", null, null,
        null, "000", ControlDigit.MODULE10, SequenceNumberLength.VARIABLE, null, true);

    assertThat("Sequence is not computed correctly using SequenceUtil 2 Level Sequences",
        "601104910282130002", equalTo(SequenceUtil.getDocumentNo(false, parentSequence)));
  }

  /**
   * 2 Level Sequences
   * 
   * Test with Base Sequence having CalculationMethod: Auto numbering, Control Digit : Module10,
   * NextAssignedNo: 2821L, SequenceNumberLength: Fixed, Sequence Length: 5L, Prefix: 0110491,
   * Suffix: 01
   * 
   * Parent Sequence having CalculationMethod: Based On Sequence, Base Sequence as above, Control
   * Digit : Module10, SequenceNumberLength: Variable, Prefix: 6, Suffix: 000
   * 
   * Compute documentNo using SequenceUtil getDocumentNo, AD_SEQUENCE_DOCUMENTNO
   */
  @Test
  public void sequenceUtilTest_BaseSequence_AutoNumbering_NextAssignedNo_PrefixSuffix_Level2() {

    final Sequence baseSequence = SequenceTestUtils.createDocumentSequence(
        UUID.randomUUID().toString(), CalculationMethod.AUTONUMERING, null, "0110491", null, 2821L,
        null, "01", ControlDigit.MODULE10, SequenceNumberLength.FIXED, 5L, true);

    final Sequence parentSequence = SequenceTestUtils.createDocumentSequence(
        UUID.randomUUID().toString(), CalculationMethod.SEQUENCE, baseSequence, "6", null, null,
        null, "000", ControlDigit.MODULE10, SequenceNumberLength.VARIABLE, null, true);

    assertThat("Sequence is not computed correctly using SequenceUtil 2 Level Sequences",
        "60110491028210100002", equalTo(SequenceUtil.getDocumentNo(false, parentSequence)));

    assertThat("Sequence is not computed correctly using DB function for 2 level sequences",
        "60110491028210100002", equalTo(SequenceTestUtils.getDocumentNo(parentSequence.getId(),
            false, "AD_SEQUENCE_DOCUMENTNO")));
  }

  /**
   * 
   * 3 Level Sequences
   * 
   * Test with Sequence having CalculationMethod: Auto numbering, Control Digit : Module10,
   * NextAssignedNo: 5000L, SequenceNumberLength: Variable, Prefix: 3, Suffix: 7
   * 
   * Base Sequence having CalculationMethod: Based On Sequence, Sequence as above, Control Digit :
   * Module10, SequenceNumberLength: Variable, Prefix: 8, Suffix: 2
   * 
   * Parent Sequence having CalculationMethod: Based On Sequence, Base Sequence as above, Control
   * Digit : Module10, SequenceNumberLength: Variable, Prefix: 9, Suffix: 1
   * 
   * Compute documentNo using SequenceUtil getDocumentNo, AD_SEQUENCE_DOCUMENTNO
   */

  @Test
  public void sequenceUtilTest_BaseSequence_AutoNumbering_NextAssignedNo_PrefixSuffix_Level3() {

    final Sequence sequence = SequenceTestUtils.createDocumentSequence(UUID.randomUUID().toString(),
        CalculationMethod.AUTONUMERING, null, "3", null, 5000L, null, "7", ControlDigit.NONE,
        SequenceNumberLength.VARIABLE, null, true);

    final Sequence baseSequence = SequenceTestUtils.createDocumentSequence(
        UUID.randomUUID().toString(), CalculationMethod.SEQUENCE, sequence, "8", null, null, null,
        "2", ControlDigit.MODULE10, SequenceNumberLength.VARIABLE, null, true);

    final Sequence parentSequence = SequenceTestUtils.createDocumentSequence(
        UUID.randomUUID().toString(), CalculationMethod.SEQUENCE, baseSequence, "9", null, null,
        null, "1", ControlDigit.MODULE10, SequenceNumberLength.VARIABLE, null, true);

    assertThat("Sequence is not computed correctly using SequenceUtil for3 level sequences",
        "983500072510", equalTo(SequenceUtil.getDocumentNo(false, parentSequence)));

    assertThat("Sequence is not computed correctly using DB function for 3 level sequences",
        "983500072510", equalTo(SequenceTestUtils.getDocumentNo(parentSequence.getId(), false,
            "AD_SEQUENCE_DOCUMENTNO")));
  }

  /**
   * 2 Level Sequences
   * 
   * Test with Base Sequence having CalculationMethod: Auto numbering, Control Digit : Module10,
   * NextAssignedNo: 1000000L, SequenceNumberLength: Fixed, Sequence Length: 10L, Prefix: 0110491,
   * Suffix: 01
   * 
   * Parent Sequence having CalculationMethod: Based On Sequence, Base Sequence as above, Control
   * Digit : Module10, SequenceNumberLength: Variable, Prefix: 6, Suffix: 000
   * 
   * Compute documentNo using SequenceUtil getDocumentNo, AD_SEQUENCE_DOCUMENTNO
   */

  @Test
  public void sequenceUtilTest_BaseSequence_AutoNumbering_NextAssignedNo_PrefixSuffix_FixedLength_10L_Level2() {

    final Sequence baseSequence = SequenceTestUtils.createDocumentSequence(
        UUID.randomUUID().toString(), CalculationMethod.AUTONUMERING, null, "SUT/", null, 1000000L,
        null, "000", ControlDigit.NONE, SequenceNumberLength.FIXED, 10L, true);

    final Sequence parentSequence = SequenceTestUtils.createDocumentSequence(
        UUID.randomUUID().toString(), CalculationMethod.SEQUENCE, baseSequence, null, null, null,
        null, null, ControlDigit.NONE, SequenceNumberLength.VARIABLE, null, true);

    assertThat("Sequence is not computed correctly using SequenceUtil 2 Level Sequences",
        "SUT/0001000000000", equalTo(SequenceUtil.getDocumentNo(false, parentSequence)));

    assertThat("Sequence is not computed correctly using DB function for 2 level sequences",
        "SUT/0001000000000", equalTo(SequenceTestUtils.getDocumentNo(parentSequence.getId(), false,
            "AD_SEQUENCE_DOCUMENTNO")));
  }

  /**
   * 2 Level Sequences
   * 
   * Test with Base Sequence having CalculationMethod: Auto numbering, Control Digit : None,
   * NextAssignedNo: 1000000L, SequenceNumberLength: Fixed, Sequence Length: 5L, Prefix: "SUT/",
   * Suffix: 000
   * 
   * Parent Sequence having CalculationMethod: Based On Sequence, Base Sequence as above, Control
   * Digit : None, SequenceNumberLength: Variable, No Prefix, No Suffix
   * 
   * Compute documentNo using SequenceUtil getDocumentNo, AD_SEQUENCE_DOCUMENTNO
   */

  @Test
  public void sequenceUtilTest_BaseSequence_AutoNumbering_NextAssignedNo_PrefixSuffix_FixedLength_5L_Level2() {

    final Sequence baseSequence = SequenceTestUtils.createDocumentSequence(
        UUID.randomUUID().toString(), CalculationMethod.AUTONUMERING, null, "SUT/", null, 1000000L,
        null, "000", ControlDigit.NONE, SequenceNumberLength.FIXED, 5L, true);

    final Sequence parentSequence = SequenceTestUtils.createDocumentSequence(
        UUID.randomUUID().toString(), CalculationMethod.SEQUENCE, baseSequence, null, null, null,
        null, null, ControlDigit.NONE, SequenceNumberLength.VARIABLE, null, true);

    assertThat("Sequence is not computed correctly using SequenceUtil 2 Level Sequences",
        "SUT/1000000000", equalTo(SequenceUtil.getDocumentNo(false, parentSequence)));

    assertThat("Sequence is not computed correctly using DB function for 2 level sequences",
        "SUT/1000000000", equalTo(SequenceTestUtils.getDocumentNo(parentSequence.getId(), false,
            "AD_SEQUENCE_DOCUMENTNO")));
  }
}
