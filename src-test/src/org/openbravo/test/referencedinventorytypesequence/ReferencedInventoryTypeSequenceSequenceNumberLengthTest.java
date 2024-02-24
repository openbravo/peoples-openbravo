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

public class ReferencedInventoryTypeSequenceSequenceNumberLengthTest
    extends ReferencedInventoryTypeSequenceTest {

  /**
   * test sequence with Variable sequence number length and empty sequence length
   */
  @Test
  public void sequenceWithSequenceNumberLength_Variable() {
    final Sequence sequence = createSequence(SequenceNumberLength.VARIABLE, null);
    OBDal.getInstance().save(sequence);
    OBDal.getInstance().flush();
    assertTrue("Sequence with Sequence Number Length - Variable is not created", sequence != null);
  }

  /**
   * test sequence with Fixed sequence number length and non empty non zero long sequence length
   */

  @Test
  public void sequenceWithSequenceNumberLength_Fixed() {
    final Sequence sequence = createSequence(SequenceNumberLength.FIXED, 7L);
    OBDal.getInstance().save(sequence);
    OBDal.getInstance().flush();
    assertTrue("Sequence with Sequence Number Length - Fixed is not created", sequence != null);
  }

  /**
   * test sequence with Fixed sequence number length and zero sequence length
   */

  @Test
  public void sequenceWithSequenceNumberLength_Fixed_SequenceLength_a() {
    final Sequence sequence = createSequence(SequenceNumberLength.FIXED, 0L);
    OBDal.getInstance().save(sequence);
    Exception exception = assertThrows(Exception.class, () -> OBDal.getInstance().flush());
    assertThat(exception.getMessage(), containsString("ConstraintViolationException"));
  }

  /**
   * test sequence with Fixed sequence number length and empty sequence length
   */

  @Test
  public void sequenceWithSequenceNumberLength_Fixed_SequenceLength_b() {
    final Sequence sequence = createSequence(SequenceNumberLength.FIXED, null);
    OBDal.getInstance().save(sequence);
    Exception exception = assertThrows(Exception.class, () -> OBDal.getInstance().flush());
    assertThat(exception.getMessage(), containsString("ConstraintViolationException"));
  }

  /**
   * test sequence with Fixed sequence number length and non empty sequence length, but when saving
   * such sequence, sequence length is set as null
   */

  @Test
  public void sequenceWithSequenceNumberLength_Variable_SequenceLength() {
    final Sequence sequence = createSequence(SequenceNumberLength.VARIABLE, 10L);
    OBDal.getInstance().save(sequence);
    OBDal.getInstance().flush();
    assertTrue("Sequence with Sequence Number Length - Variable is with Sequence Length",
        sequence.getSequenceLength() == null);
  }

  /**
   * Create sequence with calculation method auto numbering, Fixed or Variable sequence number
   * length, sequence length
   * 
   * @return Sequence for various sequence number length and sequence length combinations
   */
  private Sequence createSequence(SequenceNumberLength sequenceNumberLength, Long sequenceLength) {
    return ReferencedInventoryTypeSequenceTestUtils.createDocumentSequence(
        CalculationMethod.AUTONUMERING, null, "0110491", null, null, null, null, ControlDigit.NONE,
        sequenceNumberLength, sequenceLength);
  }
}
