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
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.openbravo.base.exception.OBException;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.erpCommon.utility.SequenceUtil.CalculationMethod;
import org.openbravo.erpCommon.utility.SequenceUtil.ControlDigit;
import org.openbravo.erpCommon.utility.SequenceUtil.SequenceNumberLength;
import org.openbravo.model.ad.utility.Sequence;

public class SequenceExceptionTest extends SequenceTest {

  /**
   * test Sequence with Calculation Method as Sequence and null Base Sequence
   */
  @Test
  public void testSequenceExceptionWithCalculationMethod_Sequence() {
    final Sequence sequence = SequenceTestUtils.createBaseSequence(CalculationMethod.SEQUENCE,
        "0110491", SequenceNumberLength.FIXED, 7L);
    OBDal.getInstance().save(sequence);
    Exception exception = assertThrows(Exception.class, () -> OBDal.getInstance().flush());
    assertThat(exception.getMessage(), containsString("ConstraintViolationException"));
  }

  /**
   * test Sequence with Calculation Method as Sequence and update Base Sequence as empty/null.
   */
  @Test
  public void testSequenceExceptionWithCalculationMethod_AutoNumbering() {
    final Sequence baseSequence = SequenceTestUtils.createBaseSequence(
        CalculationMethod.AUTONUMERING, "100", SequenceNumberLength.VARIABLE, null);
    OBDal.getInstance().save(baseSequence);
    final Sequence parentSequence = SequenceTestUtils.createParentSequence(baseSequence);
    OBDal.getInstance().save(parentSequence);
    OBDal.getInstance().flush();

    parentSequence.setBaseSequence(null);
    OBDal.getInstance().save(parentSequence);
    Exception exception = assertThrows(Exception.class, () -> OBDal.getInstance().flush());
    assertThat(exception.getMessage(), containsString("ConstraintViolationException"));
  }

  /**
   * test sequence with Fixed sequence number length and zero sequence length
   */

  @Test
  public void testSequenceExceptionWithSequenceNumberLength_Fixed_SequenceLength_0L() {
    final Sequence sequence = SequenceTestUtils.createSequence(SequenceNumberLength.FIXED, 0L);
    OBDal.getInstance().save(sequence);
    Exception exception = assertThrows(Exception.class, () -> OBDal.getInstance().flush());
    assertThat(exception.getMessage(), containsString("ConstraintViolationException"));
  }

  /**
   * test sequence with Fixed sequence number length and empty sequence length
   */

  @Test
  public void testSequenceExceptionWithSequenceNumberLength_Fixed_SequenceLength_Empty() {
    final Sequence sequence = SequenceTestUtils.createSequence(SequenceNumberLength.FIXED, null);
    OBDal.getInstance().save(sequence);
    Exception exception = assertThrows(Exception.class, () -> OBDal.getInstance().flush());
    assertThat(exception.getMessage(), containsString("ConstraintViolationException"));
  }

  /** Alphanumeric prefix */
  @Test
  public void testSequenceExceptionWithControlDigit_Module10_AlphanumericPrefix() {
    final Sequence sequence = SequenceTestUtils.createSequence(CalculationMethod.AUTONUMERING, null,
        "1A2", null, ControlDigit.MODULE10);
    OBException exception = assertThrows(OBException.class,
        () -> OBDal.getInstance().save(sequence));
    assertThat(exception.getMessage(),
        containsString(OBMessageUtils.messageBD("ValidateSequence")));
  }

  /** Alphanumeric suffix */
  @Test
  public void testSequenceExceptionWithControlDigit_Module10_AlphanumericSuffix() {
    final Sequence sequence = SequenceTestUtils.createSequence(CalculationMethod.AUTONUMERING, null,
        null, "1A2", ControlDigit.MODULE10);
    OBException exception = assertThrows(OBException.class,
        () -> OBDal.getInstance().save(sequence));
    assertThat(exception.getMessage(),
        containsString(OBMessageUtils.messageBD("ValidateSequence")));
  }

  /** Use Base sequence with alphanumeric prefix */
  @Test
  public void testSequenceExceptionWithControlDigit_Module10_BaseSequenceAlphanumericPrefix() {
    final Sequence baseSequence = SequenceTestUtils.createSequence(CalculationMethod.AUTONUMERING,
        null, "1A2", null, ControlDigit.NONE);
    OBDal.getInstance().save(baseSequence);
    final Sequence parentSequence = SequenceTestUtils.createSequence(CalculationMethod.SEQUENCE,
        baseSequence, "06", null, ControlDigit.MODULE10);
    OBException exception = assertThrows(OBException.class,
        () -> OBDal.getInstance().save(parentSequence));
    assertThat(exception.getMessage(),
        containsString(OBMessageUtils.messageBD("ValidateSequence")));
  }

  /** Use Base sequence with alphanumeric suffix */
  @Test
  public void testSequenceExceptionWithControlDigit_Module10_BaseSequenceAlphanumericSuffix() {
    final Sequence baseSequence = SequenceTestUtils.createSequence(CalculationMethod.AUTONUMERING,
        null, null, "1A2", ControlDigit.NONE);
    OBDal.getInstance().save(baseSequence);
    final Sequence parentSequence = SequenceTestUtils.createSequence(CalculationMethod.SEQUENCE,
        baseSequence, "06", null, ControlDigit.MODULE10);
    OBException exception = assertThrows(OBException.class,
        () -> OBDal.getInstance().save(parentSequence));
    assertThat(exception.getMessage(),
        containsString(OBMessageUtils.messageBD("ValidateSequence")));
  }

  /** Update Base sequence with alphanumeric suffix */
  @Test
  public void testSequenceExceptionWithControlDigit_Module10_UpdateBaseSequenceAlphanumericSuffix() {
    final Sequence baseSequence = SequenceTestUtils.createSequence(CalculationMethod.AUTONUMERING,
        null, null, "100", ControlDigit.NONE);
    OBDal.getInstance().save(baseSequence);
    final Sequence parentSequence = SequenceTestUtils.createSequence(CalculationMethod.SEQUENCE,
        baseSequence, "06", null, ControlDigit.MODULE10);
    OBDal.getInstance().save(parentSequence);
    OBDal.getInstance().flush();
    assertTrue(
        "Sequence with control digit Module 10 and base sequence with numeric suffix is not created.",
        parentSequence != null);

    baseSequence.setSuffix("1A2");
    OBDal.getInstance().save(baseSequence);
    OBException exception = assertThrows(OBException.class, () -> OBDal.getInstance().flush());
    assertThat(exception.getMessage(),
        containsString(OBMessageUtils.messageBD("ValidateBaseSequence")));
  }

  /** Update Base sequence with alphanumeric prefix */
  @Test
  public void testSequenceExceptionWithControlDigit_Module10_UpdateBaseSequenceAlphanumericPrefix() {
    final Sequence baseSequence = SequenceTestUtils.createSequence(CalculationMethod.AUTONUMERING,
        null, null, "100", ControlDigit.NONE);
    OBDal.getInstance().save(baseSequence);
    final Sequence parentSequence = SequenceTestUtils.createSequence(CalculationMethod.SEQUENCE,
        baseSequence, "06", null, ControlDigit.MODULE10);
    OBDal.getInstance().save(parentSequence);
    OBDal.getInstance().flush();
    assertTrue(
        "Sequence with control digit Module 10 and base sequence with numeric prefix is not created.",
        parentSequence != null);
    baseSequence.setPrefix("1A2");
    OBDal.getInstance().save(baseSequence);
    Exception exception = assertThrows(Exception.class, () -> OBDal.getInstance().flush());
    assertThat(exception.getMessage(),
        containsString(OBMessageUtils.messageBD("ValidateBaseSequence")));
  }

  /**
   * Update Parent or Base Sequence having None control digit to Module 10 with Parent or Base
   * sequence with alphanumeric suffix or prefix.
   */
  @Test
  public void testSequenceException_UpdateControlDigitModule10() {
    final Sequence baseSequence = SequenceTestUtils.createSequence(CalculationMethod.AUTONUMERING,
        null, null, "1A2", ControlDigit.NONE);
    OBDal.getInstance().save(baseSequence);
    final Sequence parentSequence = SequenceTestUtils.createSequence(CalculationMethod.SEQUENCE,
        baseSequence, "100", null, ControlDigit.NONE);
    OBDal.getInstance().save(parentSequence);
    OBDal.getInstance().flush();

    parentSequence.setControlDigit(ControlDigit.MODULE10.value);
    OBDal.getInstance().save(parentSequence);

    OBException exception = assertThrows(OBException.class, () -> OBDal.getInstance().flush());
    assertThat(exception.getMessage(),
        containsString(OBMessageUtils.messageBD("ValidateSequence")));

    baseSequence.setControlDigit(ControlDigit.MODULE10.value);
    OBDal.getInstance().save(baseSequence);

    exception = assertThrows(OBException.class, () -> OBDal.getInstance().flush());
    assertThat(exception.getMessage(),
        containsString(OBMessageUtils.messageBD("ValidateSequence")));
  }

  /**
   * Update Parent Sequence having Module 10 control digit with new Base sequence with alphanumeric
   * suffix or prefix.
   */
  @Test
  public void testSequenceException_ControlDigitModule10_UpdateBaseSequenceWithAlphanumericPrefixSuffix() {
    final Sequence baseSequence = SequenceTestUtils.createSequence(CalculationMethod.AUTONUMERING,
        null, null, "100", ControlDigit.NONE);
    OBDal.getInstance().save(baseSequence);
    final Sequence parentSequence = SequenceTestUtils.createSequence(CalculationMethod.SEQUENCE,
        baseSequence, "06", null, ControlDigit.MODULE10);
    OBDal.getInstance().save(parentSequence);
    OBDal.getInstance().flush();

    // new base sequence with alphanumeric suffix
    final Sequence newBaseSequenceAlphanumericSuffix = SequenceTestUtils
        .createSequence(CalculationMethod.AUTONUMERING, null, null, "1A2", ControlDigit.NONE);
    OBDal.getInstance().save(newBaseSequenceAlphanumericSuffix);

    // set parent sequence having Module10 control digit with new base sequence with alphanumeric
    // suffix.
    parentSequence.setBaseSequence(newBaseSequenceAlphanumericSuffix);
    OBDal.getInstance().save(parentSequence);

    Exception exception = assertThrows(Exception.class, () -> OBDal.getInstance().flush());
    assertThat(exception.getMessage(),
        containsString(OBMessageUtils.messageBD("ValidateSequence")));

    // new base sequence with alphanumeric prefix
    newBaseSequenceAlphanumericSuffix.setSuffix("102");
    newBaseSequenceAlphanumericSuffix.setPrefix("1A2");
    OBDal.getInstance().save(newBaseSequenceAlphanumericSuffix);

    // set parent sequence having Module10 control digit with new base sequence with alphanumeric
    // prefix.
    parentSequence.setBaseSequence(newBaseSequenceAlphanumericSuffix);
    OBDal.getInstance().save(parentSequence);

    exception = assertThrows(Exception.class, () -> OBDal.getInstance().flush());
    assertThat(exception.getMessage(),
        containsString(OBMessageUtils.messageBD("ValidateSequence")));

  }

  /**
   * Add Level 3 Sequences verification
   */
  @Test
  public void testSequenceException_ControlDigitModule10_3Level() {
    final Sequence sequence1 = SequenceTestUtils.createSequence(CalculationMethod.AUTONUMERING,
        null, "A", null, ControlDigit.NONE);
    OBDal.getInstance().save(sequence1);

    final Sequence Sequence2 = SequenceTestUtils.createSequence(CalculationMethod.SEQUENCE,
        sequence1, null, null, ControlDigit.NONE);
    OBDal.getInstance().save(Sequence2);

    final Sequence Sequence3 = SequenceTestUtils.createSequence(CalculationMethod.SEQUENCE,
        Sequence2, null, null, ControlDigit.MODULE10);

    Exception exception = assertThrows(Exception.class, () -> OBDal.getInstance().save(Sequence3));
    assertThat(exception.getMessage(),
        containsString(OBMessageUtils.messageBD("ValidateSequence")));

  }
}
