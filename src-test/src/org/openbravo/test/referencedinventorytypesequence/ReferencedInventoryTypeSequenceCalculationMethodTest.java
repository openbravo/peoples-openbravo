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
package org.openbravo.test.referencedinventorytypesequence;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.SequenceUtil.CalculationMethod;
import org.openbravo.erpCommon.utility.SequenceUtil.ControlDigit;
import org.openbravo.erpCommon.utility.SequenceUtil.SequenceNumberLength;
import org.openbravo.model.ad.utility.Sequence;

public class ReferencedInventoryTypeSequenceCalculationMethodTest
    extends ReferencedInventoryTypeSequenceTest {

  /**
   * test Sequence with Calculation Method as Sequence and null Base Sequence
   */
  @Test
  public void sequenceWithCalculationMethod_Sequence() {
    final Sequence sequence = createBaseSequence(CalculationMethod.SEQUENCE, "0110491",
        SequenceNumberLength.FIXED, 7L);
    OBDal.getInstance().save(sequence);
    Exception exception = assertThrows(Exception.class, () -> OBDal.getInstance().flush());
    assertThat(exception.getMessage(), containsString("ConstraintViolationException"));
  }

  /**
   * test Sequence with Calculation Method as Auto Numbering and null Base Sequence
   */
  @Test
  public void sequenceWithCalculationMethod_AutoNumbering() {
    final Sequence sequence = createBaseSequence(CalculationMethod.AUTONUMERING, "0110491",
        SequenceNumberLength.FIXED, 7L);
    OBDal.getInstance().save(sequence);
    OBDal.getInstance().flush();
    assertTrue("Sequence with calculation method - AutoNumbering is not created", sequence != null);
  }

  /**
   * test Sequence with Calculation Method as DocumentNo_TablName and null Base Sequence
   */

  @Test
  public void sequenceWithCalculationMethod_DocumentNoTableName() {
    final Sequence sequence = createBaseSequence(CalculationMethod.DOCUMENTNO_TABLENAME, "0110491",
        SequenceNumberLength.FIXED, 7L);
    OBDal.getInstance().save(sequence);
    OBDal.getInstance().flush();
    assertTrue("Sequence with calculation method - DocumentNo_TableName is not created",
        sequence != null);
  }

  /**
   * test Sequence with Calculation Method as Sequence and update Base Sequence as empty/null.
   */
  @Test
  public void sequenceWithCalculationMethod_Sequence_a() {
    final Sequence baseSequence = createBaseSequence(CalculationMethod.AUTONUMERING, "100",
        SequenceNumberLength.VARIABLE, null);
    OBDal.getInstance().save(baseSequence);
    final Sequence parentSequence = createParentSequence(baseSequence);
    OBDal.getInstance().save(parentSequence);
    OBDal.getInstance().flush();

    parentSequence.setBaseSequence(null);
    OBDal.getInstance().save(parentSequence);
    Exception exception = assertThrows(Exception.class, () -> OBDal.getInstance().flush());
    assertThat(exception.getMessage(), containsString("ConstraintViolationException"));
  }

  /**
   * test Sequence with Calculation Method as Sequence with Sequence and its valid Base Sequence
   */
  @Test
  public void sequenceWithCalculationMethod_Sequence_b() {
    final Sequence baseSequence = createBaseSequence(CalculationMethod.AUTONUMERING, "100",
        SequenceNumberLength.VARIABLE, null);
    OBDal.getInstance().save(baseSequence);
    final Sequence parentSequence = createParentSequence(baseSequence);
    OBDal.getInstance().save(parentSequence);
    OBDal.getInstance().flush();
    assertTrue(
        "Sequence with calculation method - Sequence with Sequence and its valid base sequence is not created",
        parentSequence != null && parentSequence.getBaseSequence() != null);

  }

  /**
   * Create sequence with calculation method, prefix, sequence number length and sequence length
   * 
   * @return Sequence to be used for defined Referenced Inventory Type
   */

  private Sequence createBaseSequence(CalculationMethod calculationMethod, String prefix,
      SequenceNumberLength sequenceNumberLength, Long sequenceLength) {
    return ReferencedInventoryTypeSequenceTestUtils.createDocumentSequence(calculationMethod, null,
        prefix, null, null, null, null, ControlDigit.NONE, sequenceNumberLength, sequenceLength);
  }

  /**
   * Create sequence with base sequence
   * 
   * @return Sequence to be used for defined Referenced Inventory Type
   */

  private Sequence createParentSequence(Sequence baseSequence) {
    return ReferencedInventoryTypeSequenceTestUtils.createDocumentSequence(
        CalculationMethod.SEQUENCE, baseSequence, "06", null, null, null, null,
        ControlDigit.MODULE10, SequenceNumberLength.VARIABLE, null);
  }

}
