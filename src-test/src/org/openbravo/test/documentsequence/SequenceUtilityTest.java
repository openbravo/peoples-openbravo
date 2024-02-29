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

  private static final String DOC_TYPE_ID = "FF8080812C2ABFC6012C2B3BDF4D005A";
  private static final String TABLE_NAME = "C_Order";

  @Test
  public void utilitySequenceTest_a() {
    final Sequence sequence = SequenceTestUtils.createDocumentSequence(UUID.randomUUID().toString(),
        CalculationMethod.AUTONUMERING, null, null, null, 1000000L, null, null, ControlDigit.NONE,
        SequenceNumberLength.VARIABLE, null, true);
    // Create a document Type
    final DocumentType docType = SequenceTestUtils.createDocumentType(DOC_TYPE_ID, sequence);
    assertThat(
        "Sequence is not computed correctly using Utility - AD_SEQUENCE_DOCTYPE for AutoNumbering calculation method, next Assigned No. and Control Digit - None",
        "1000000", equalTo(
            SequenceTestUtils.getDocumentNo("", TABLE_NAME, "", docType.getId(), false, false)));
  }

  @Test
  public void utilitySequenceTest_aa() {
    String tableName = TABLE_NAME + "_1";
    final Sequence sequence = SequenceTestUtils.createDocumentSequence("DocumentNo_" + tableName,
        CalculationMethod.AUTONUMERING, null, null, null, 1000000L, null, null, ControlDigit.NONE,
        SequenceNumberLength.VARIABLE, null, true);
    assertThat(
        "Sequence is not computed correctly using Utility - AD_SEQUENCE_DOC for AutoNumbering calculation method, prefix, suffix, next Assigned No and Control Digit - Module 10",
        "1000000",
        equalTo(SequenceTestUtils.getDocumentNo(sequence.getClient().getId(), tableName, false)));
  }

  @Test
  public void utilitySequenceTest_b() {
    final Sequence sequence = SequenceTestUtils.createDocumentSequence(UUID.randomUUID().toString(),
        CalculationMethod.AUTONUMERING, null, "SUT/", null, 1000000L, null, null, ControlDigit.NONE,
        SequenceNumberLength.VARIABLE, null, true);
    // Create a document Type
    final DocumentType docType = SequenceTestUtils.createDocumentType(DOC_TYPE_ID, sequence);
    assertThat(
        "Sequence is not computed correctly using Utility - AD_SEQUENCE_DOCTYPE for AutoNumbering calculation method, prefix, next Assigned No and Control Digit - None",
        "SUT/1000000", equalTo(
            SequenceTestUtils.getDocumentNo("", TABLE_NAME, "", docType.getId(), false, false)));
  }

  @Test
  public void utilitySequenceTest_bb() {
    String tableName = TABLE_NAME + "_1";
    final Sequence sequence = SequenceTestUtils.createDocumentSequence("DocumentNo_" + tableName,
        CalculationMethod.AUTONUMERING, null, "SUT/", null, 1000000L, null, null, ControlDigit.NONE,
        SequenceNumberLength.VARIABLE, null, true);
    assertThat(
        "Sequence is not computed correctly using Utility - AD_SEQUENCE_DOC for AutoNumbering calculation method, prefix, suffix, next Assigned No and Control Digit - Module 10",
        "SUT/1000000",
        equalTo(SequenceTestUtils.getDocumentNo(sequence.getClient().getId(), tableName, false)));
  }

  @Test
  public void utilitySequenceTest_c() {
    final Sequence sequence = SequenceTestUtils.createDocumentSequence(UUID.randomUUID().toString(),
        CalculationMethod.AUTONUMERING, null, null, null, 1000000L, null, "000", ControlDigit.NONE,
        SequenceNumberLength.VARIABLE, null, true);
    // Create a document Type
    final DocumentType docType = SequenceTestUtils.createDocumentType(DOC_TYPE_ID, sequence);
    assertThat(
        "Sequence is not computed correctly using Utility - AD_SEQUENCE_DOCTYPE for AutoNumbering calculation method, suffix, next Assigned No and Control Digit - None",
        "1000000000", equalTo(
            SequenceTestUtils.getDocumentNo("", TABLE_NAME, "", docType.getId(), false, false)));
  }

  @Test
  public void utilitySequenceTest_cc() {
    String tableName = TABLE_NAME + "_1";
    final Sequence sequence = SequenceTestUtils.createDocumentSequence("DocumentNo_" + tableName,
        CalculationMethod.AUTONUMERING, null, null, null, 1000000L, null, "000", ControlDigit.NONE,
        SequenceNumberLength.VARIABLE, null, true);
    assertThat(
        "Sequence is not computed correctly using Utility - AD_SEQUENCE_DOC for AutoNumbering calculation method, prefix, suffix, next Assigned No and Control Digit - Module 10",
        "1000000000",
        equalTo(SequenceTestUtils.getDocumentNo(sequence.getClient().getId(), tableName, false)));
  }

  @Test
  public void utilitySequenceTest_d() {
    final Sequence sequence = SequenceTestUtils.createDocumentSequence(UUID.randomUUID().toString(),
        CalculationMethod.AUTONUMERING, null, "SUT/", null, 1000000L, null, "000",
        ControlDigit.NONE, SequenceNumberLength.VARIABLE, null, true);
    // Create a document Type
    final DocumentType docType = SequenceTestUtils.createDocumentType(DOC_TYPE_ID, sequence);
    assertThat(
        "Sequence is not computed correctly using Utility - AD_SEQUENCE_DOCTYPE for AutoNumbering calculation method, prefix, suffix, next Assigned No and Control Digit - None",
        "SUT/1000000000", equalTo(
            SequenceTestUtils.getDocumentNo("", TABLE_NAME, "", docType.getId(), false, false)));
  }

  @Test
  public void utilitySequenceTest_dd() {
    String tableName = TABLE_NAME + "_1";
    final Sequence sequence = SequenceTestUtils.createDocumentSequence("DocumentNo_" + tableName,
        CalculationMethod.AUTONUMERING, null, "SUT/", null, 1000000L, null, "000",
        ControlDigit.NONE, SequenceNumberLength.VARIABLE, null, true);
    assertThat(
        "Sequence is not computed correctly using Utility - AD_SEQUENCE_DOC for AutoNumbering calculation method, prefix, suffix, next Assigned No and Control Digit - Module 10",
        "SUT/1000000000",
        equalTo(SequenceTestUtils.getDocumentNo(sequence.getClient().getId(), tableName, false)));
  }

  @Test
  public void utilitySequenceTest_e() {
    final Sequence sequence = SequenceTestUtils.createDocumentSequence(UUID.randomUUID().toString(),
        CalculationMethod.AUTONUMERING, null, "6", null, 1000000L, null, "000",
        ControlDigit.MODULE10, SequenceNumberLength.VARIABLE, null, true);
    // Create a document Type
    final DocumentType docType = SequenceTestUtils.createDocumentType(DOC_TYPE_ID, sequence);
    assertThat(
        "Sequence is not computed correctly using Utility - AD_SEQUENCE_DOCTYPE for AutoNumbering calculation method, prefix, suffix, next Assigned No and Control Digit - Module 10",
        "610000000001", equalTo(
            SequenceTestUtils.getDocumentNo("", TABLE_NAME, "", docType.getId(), false, false)));
  }

  @Test
  public void utilitySequenceTest_ee() {
    String tableName = TABLE_NAME + "_1";
    final Sequence sequence = SequenceTestUtils.createDocumentSequence("DocumentNo_" + tableName,
        CalculationMethod.AUTONUMERING, null, "6", null, 1000000L, null, "000",
        ControlDigit.MODULE10, SequenceNumberLength.VARIABLE, null, true);
    assertThat(
        "Sequence is not computed correctly using Utility - AD_SEQUENCE_DOC for AutoNumbering calculation method, prefix, suffix, next Assigned No and Control Digit - Module 10",
        "610000000001",
        equalTo(SequenceTestUtils.getDocumentNo(sequence.getClient().getId(), tableName, false)));
  }

  @Test
  public void utilitySequenceTest_f() {
    final Sequence sequence = SequenceTestUtils.createDocumentSequence(UUID.randomUUID().toString(),
        CalculationMethod.AUTONUMERING, null, null, null, null, null, null, ControlDigit.NONE,
        SequenceNumberLength.VARIABLE, null, true);
    // Create a document Type
    final DocumentType docType = SequenceTestUtils.createDocumentType(DOC_TYPE_ID, sequence);
    assertThat(
        "Sequence is not computed correctly using Utility - AD_SEQUENCE_DOCTYPE for AutoNumbering calculation method, prefix, suffix, next Assigned No and Control Digit - None",
        "1", equalTo(
            SequenceTestUtils.getDocumentNo("", TABLE_NAME, "", docType.getId(), false, false)));

  }

  @Test
  public void utilitySequenceTest_ff() {
    String tableName = TABLE_NAME + "_1";
    final Sequence sequence = SequenceTestUtils.createDocumentSequence("DocumentNo_" + tableName,
        CalculationMethod.AUTONUMERING, null, null, null, null, null, null, ControlDigit.NONE,
        SequenceNumberLength.VARIABLE, null, true);
    assertThat(
        "Sequence is not computed correctly using Utility - AD_SEQUENCE_DOC for AutoNumbering calculation method, prefix, suffix, next Assigned No and Control Digit - Module 10",
        "1",
        equalTo(SequenceTestUtils.getDocumentNo(sequence.getClient().getId(), tableName, false)));
  }

  @Test
  public void utilitySequenceTest_g() {
    final Sequence sequence = SequenceTestUtils.createDocumentSequence(UUID.randomUUID().toString(),
        CalculationMethod.AUTONUMERING, null, null, null, null, null, null, ControlDigit.MODULE10,
        SequenceNumberLength.VARIABLE, null, true);
    // Create a document Type
    final DocumentType docType = SequenceTestUtils.createDocumentType(DOC_TYPE_ID, sequence);
    assertThat(
        "Sequence is not computed correctly using Utility - AD_SEQUENCE_DOCTYPE for AutoNumbering calculation method, prefix, suffix, next Assigned No and Control Digit - Module 10",
        "17", equalTo(
            SequenceTestUtils.getDocumentNo("", TABLE_NAME, "", docType.getId(), false, false)));
  }

  @Test
  public void utilitySequenceTest_gg() {
    String tableName = TABLE_NAME + "_1";
    final Sequence sequence = SequenceTestUtils.createDocumentSequence("DocumentNo_" + tableName,
        CalculationMethod.AUTONUMERING, null, null, null, null, null, null, ControlDigit.MODULE10,
        SequenceNumberLength.VARIABLE, null, true);
    assertThat(
        "Sequence is not computed correctly using Utility - AD_SEQUENCE_DOC for AutoNumbering calculation method, prefix, suffix, next Assigned No and Control Digit - Module 10",
        "17",
        equalTo(SequenceTestUtils.getDocumentNo(sequence.getClient().getId(), tableName, false)));
  }
}
