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
import org.openbravo.erpCommon.utility.SequenceUtil.CalculationMethod;
import org.openbravo.erpCommon.utility.SequenceUtil.ControlDigit;
import org.openbravo.erpCommon.utility.SequenceUtil.SequenceNumberLength;
import org.openbravo.model.ad.utility.Sequence;
import org.openbravo.model.common.enterprise.DocumentType;

public class SequenceUtilityTest extends SequenceTest {

  /**
   * Test with Sequence having CalculationMethod: Auto numbering, Control Digit : None,
   * NextAssignedNo: 1000000L, SequenceNumberLength: Variable,
   * 
   * Utility: AD_SEQUENCE_DOCTYPE, AD_SEQUENCE_DOC
   */
  @Test
  public void utilitySequenceTest_NextAssignedNo() {

    String tableName = SequenceTestUtils.TABLE_NAME + "_1";

    final Sequence sequence = SequenceTestUtils.createDocumentSequence("DocumentNo_" + tableName,
        CalculationMethod.AUTONUMERING, null, null, null, 1000000L, null, null, ControlDigit.NONE,
        SequenceNumberLength.VARIABLE, null, true);

    assertThat(
        "Sequence with CalculationMethod: Auto numbering, Control Digit : None, "
            + "NextAssignedNo: 1000000L, SequenceNumberLength: Variable,"
            + "SequenceLength: Null is not computed correctly using Utility - AD_SEQUENCE_DOC",
        "1000000",
        equalTo(SequenceTestUtils.getDocumentNo(sequence.getClient().getId(), tableName, false)));

    // Create a document Type
    final DocumentType docType = SequenceTestUtils.createDocumentType(SequenceTestUtils.DOC_TYPE_ID,
        sequence);

    assertThat(
        "Sequence with CalculationMethod: Auto numbering, Control Digit : None, "
            + "NextAssignedNo: 1000000L, SequenceNumberLength: Variable, "
            + "SequenceLength: Null is not computed correctly using Utility - AD_SEQUENCE_DOCTYPE",
        "1000000", equalTo(SequenceTestUtils.getDocumentNo("", SequenceTestUtils.TABLE_NAME, "",
            docType.getId(), false, false)));
  }

  /**
   * Test with Sequence having CalculationMethod: Auto numbering, Control Digit : None,
   * NextAssignedNo: 1000000L, SequenceNumberLength: Variable, Prefix: SUT/
   * 
   * Utility: AD_SEQUENCE_DOCTYPE, AD_SEQUENCE_DOC
   */

  @Test
  public void utilitySequenceTest_Prefix() {

    String tableName = SequenceTestUtils.TABLE_NAME + "_1";

    final Sequence sequence = SequenceTestUtils.createDocumentSequence("DocumentNo_" + tableName,
        CalculationMethod.AUTONUMERING, null, "SUT/", null, 1000000L, null, null, ControlDigit.NONE,
        SequenceNumberLength.VARIABLE, null, true);

    assertThat(
        "Sequence with CalculationMethod: Auto numbering, Control Digit : None, "
            + "Prefix: SUT/, NextAssignedNo: 1000000L, SequenceNumberLength: Variable,"
            + "SequenceLength: Null is not computed correctly using Utility - AD_SEQUENCE_DOC",
        "SUT/1000000",
        equalTo(SequenceTestUtils.getDocumentNo(sequence.getClient().getId(), tableName, false)));

    // Create a document Type
    final DocumentType docType = SequenceTestUtils.createDocumentType(SequenceTestUtils.DOC_TYPE_ID,
        sequence);

    assertThat(
        "Sequence with CalculationMethod: Auto numbering, Control Digit : None, "
            + "Prefix: SUT/, NextAssignedNo: 1000000L, SequenceNumberLength: Variable,"
            + "SequenceLength: Null is not computed correctly using Utility - AD_SEQUENCE_DOCTYPE",
        "SUT/1000000", equalTo(SequenceTestUtils.getDocumentNo("", SequenceTestUtils.TABLE_NAME, "",
            docType.getId(), false, false)));

  }

  /**
   * Test with Sequence having CalculationMethod: Auto numbering, Control Digit : None,
   * NextAssignedNo: 1000000L, SequenceNumberLength: Variable, Prefix: 6, Suffix: 000
   * 
   * Utility: AD_SEQUENCE_DOCTYPE, AD_SEQUENCE_DOC
   */

  @Test
  public void utilitySequenceTest_PrefixSuffix() {

    String tableName = SequenceTestUtils.TABLE_NAME + "_1";

    final Sequence sequence = SequenceTestUtils.createDocumentSequence("DocumentNo_" + tableName,
        CalculationMethod.AUTONUMERING, null, "6", null, 1000000L, null, "000",
        ControlDigit.MODULE10, SequenceNumberLength.VARIABLE, null, true);

    assertThat(
        "Sequence with CalculationMethod: Auto numbering, Control Digit : None, "
            + "Prefix: 6, Suffix: 000, NextAssignedNo: 1000000L, SequenceNumberLength: Variable,"
            + "SequenceLength: Null is not computed correctly using Utility - AD_SEQUENCE_DOC",
        "610000000001",
        equalTo(SequenceTestUtils.getDocumentNo(sequence.getClient().getId(), tableName, false)));

    // Create a document Type
    final DocumentType docType = SequenceTestUtils.createDocumentType(SequenceTestUtils.DOC_TYPE_ID,
        sequence);
    assertThat(
        "Sequence with CalculationMethod: Auto numbering, Control Digit : None, "
            + "Prefix: 6, Suffix: 000, NextAssignedNo: 1000000L, SequenceNumberLength: Variable,"
            + "SequenceLength: Null is not computed correctly using Utility - AD_SEQUENCE_DOCTYPE",
        "610000000001", equalTo(SequenceTestUtils.getDocumentNo("", SequenceTestUtils.TABLE_NAME,
            "", docType.getId(), false, false)));
  }

  /**
   * Test with Sequence having CalculationMethod: Auto numbering, Control Digit : None, No
   * NextAssignedNo, No Prefix, No Suffix, SequenceNumberLength: Variable.
   * 
   * Utility: AD_SEQUENCE_DOCTYPE, AD_SEQUENCE_DOC
   */

  @Test
  public void utilitySequenceTest_NoPrefixSuffixNextAssignedNo_ControlDigitNone() {

    String tableName = SequenceTestUtils.TABLE_NAME + "_1";

    final Sequence sequence = SequenceTestUtils.createDocumentSequence("DocumentNo_" + tableName,
        CalculationMethod.AUTONUMERING, null, null, null, null, null, null, ControlDigit.NONE,
        SequenceNumberLength.VARIABLE, null, true);

    assertThat(
        "Sequence with CalculationMethod: Auto numbering, Control Digit : None, "
            + "No Prefix, Suffix, NextAssignedNo, SequenceNumberLength: Variable,"
            + "SequenceLength: Null is not computed correctly using Utility - AD_SEQUENCE_DOC",
        "1",
        equalTo(SequenceTestUtils.getDocumentNo(sequence.getClient().getId(), tableName, false)));

    // Create a document Type
    final DocumentType docType = SequenceTestUtils.createDocumentType(SequenceTestUtils.DOC_TYPE_ID,
        sequence);
    assertThat(
        "Sequence with CalculationMethod: Auto numbering, Control Digit : None, "
            + "No Prefix, Suffix, NextAssignedNo, SequenceNumberLength: Variable,"
            + "SequenceLength: Null is not computed correctly using Utility - AD_SEQUENCE_DOCTYPE",
        "1", equalTo(SequenceTestUtils.getDocumentNo("", SequenceTestUtils.TABLE_NAME, "",
            docType.getId(), false, false)));

  }

  /**
   * Test with Sequence having CalculationMethod: Auto numbering, Control Digit : Module 10, No
   * NextAssignedNo, No Prefix, No Suffix, SequenceNumberLength: Variable.
   * 
   * Utility: AD_SEQUENCE_DOCTYPE, AD_SEQUENCE_DOC
   */

  @Test
  public void utilitySequenceTest_NoPrefixSuffixNextAssignedNo_ControlDigitModule10() {

    String tableName = SequenceTestUtils.TABLE_NAME + "_1";

    final Sequence sequence = SequenceTestUtils.createDocumentSequence("DocumentNo_" + tableName,
        CalculationMethod.AUTONUMERING, null, null, null, null, null, null, ControlDigit.MODULE10,
        SequenceNumberLength.VARIABLE, null, true);

    assertThat(
        "Sequence with CalculationMethod: Auto numbering, Control Digit : Module 10, "
            + "No Prefix, Suffix, NextAssignedNo, SequenceNumberLength: Variable,"
            + "SequenceLength: Null is not computed correctly using Utility - AD_SEQUENCE_DOC",
        "17",
        equalTo(SequenceTestUtils.getDocumentNo(sequence.getClient().getId(), tableName, false)));

    // Create a document Type
    final DocumentType docType = SequenceTestUtils.createDocumentType(SequenceTestUtils.DOC_TYPE_ID,
        sequence);

    assertThat(
        "Sequence with CalculationMethod: Auto numbering, Control Digit : Module 10, "
            + "No Prefix, Suffix, NextAssignedNo, SequenceNumberLength: Variable,"
            + "SequenceLength: Null is not computed correctly using Utility - AD_SEQUENCE_DOCTYPE",
        "17", equalTo(SequenceTestUtils.getDocumentNo("", SequenceTestUtils.TABLE_NAME, "",
            docType.getId(), false, false)));
  }

  /**
   * 
   * 2 Level
   * 
   * Test with Base Sequence having CalculationMethod: Auto numbering, Control Digit : Module10,
   * NextAssignedNo: 2821L, SequenceNumberLength: Fixed, Sequence Length: 5L, Prefix: 0110491.
   * 
   * Parent Sequence having CalculationMethod: Based On Sequence, Base Sequence as above, Control
   * Digit : Module10, SequenceNumberLength: Variable, Prefix: 6, Suffix: 000
   * 
   * Utility: AD_SEQUENCE_DOCTYPE, AD_SEQUENCE_DOC
   */

  @Test
  public void utilitySequenceTest_PrefixSuffixNextAssignedNo_ControlDigitModule10_Level2() {

    String tableName = SequenceTestUtils.TABLE_NAME + "_1";

    final Sequence baseSequence = SequenceTestUtils.createDocumentSequence(
        UUID.randomUUID().toString(), CalculationMethod.AUTONUMERING, null, "0110491", null, 2821L,
        null, null, ControlDigit.MODULE10, SequenceNumberLength.FIXED, 5L, true);

    final Sequence parentSequence = SequenceTestUtils.createDocumentSequence(
        "DocumentNo_" + tableName, CalculationMethod.SEQUENCE, baseSequence, "6", null, null, null,
        "000", ControlDigit.MODULE10, SequenceNumberLength.VARIABLE, null, true);

    assertThat(
        "Sequence with base sequence is not computed correctly using Utility - AD_SEQUENCE_DOC",
        "601104910282130002", equalTo(
            SequenceTestUtils.getDocumentNo(parentSequence.getClient().getId(), tableName, false)));

    // Create a document Type
    final DocumentType docType = SequenceTestUtils.createDocumentType(SequenceTestUtils.DOC_TYPE_ID,
        parentSequence);

    assertThat(
        "Sequence with base sequence is not computed correctly using Utility - AD_SEQUENCE_DOCTYPE",
        "601104910282130002", equalTo(SequenceTestUtils.getDocumentNo("",
            SequenceTestUtils.TABLE_NAME, "", docType.getId(), false, false)));
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
   * Utility: AD_SEQUENCE_DOCTYPE, AD_SEQUENCE_DOC
   */

  @Test
  public void utilitySequenceTest_PrefixSuffixNextAssignedNo_ControlDigitModule10_Level3() {

    String tableName = SequenceTestUtils.TABLE_NAME + "_1";

    final Sequence sequence = SequenceTestUtils.createDocumentSequence(UUID.randomUUID().toString(),
        CalculationMethod.AUTONUMERING, null, "3", null, 5000L, null, "7", ControlDigit.NONE,
        SequenceNumberLength.VARIABLE, null, true);

    final Sequence baseSequence = SequenceTestUtils.createDocumentSequence(
        UUID.randomUUID().toString(), CalculationMethod.SEQUENCE, sequence, "8", null, null, null,
        "2", ControlDigit.MODULE10, SequenceNumberLength.VARIABLE, null, true);

    final Sequence parentSequence = SequenceTestUtils.createDocumentSequence(
        "DocumentNo_" + tableName, CalculationMethod.SEQUENCE, baseSequence, "9", null, null, null,
        "1", ControlDigit.MODULE10, SequenceNumberLength.VARIABLE, null, true);

    assertThat(
        "Sequence with base sequence is not computed correctly using Utility - AD_SEQUENCE_DOC",
        "983500072510", equalTo(
            SequenceTestUtils.getDocumentNo(parentSequence.getClient().getId(), tableName, false)));

    // Create a document Type
    final DocumentType docType = SequenceTestUtils.createDocumentType(SequenceTestUtils.DOC_TYPE_ID,
        parentSequence);

    assertThat(
        "Sequence with base sequence is not computed correctly using Utility - AD_SEQUENCE_DOCTYPE",
        "983500072510", equalTo(SequenceTestUtils.getDocumentNo("", SequenceTestUtils.TABLE_NAME,
            "", docType.getId(), false, false)));
  }

}
