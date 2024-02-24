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
import org.openbravo.base.exception.OBException;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.erpCommon.utility.SequenceUtil.CalculationMethod;
import org.openbravo.erpCommon.utility.SequenceUtil.ControlDigit;
import org.openbravo.erpCommon.utility.SequenceUtil.SequenceNumberLength;
import org.openbravo.model.ad.utility.Sequence;

public class ReferencedInventoryTypeSequenceControlDigitPrefixSuffixTest
    extends ReferencedInventoryTypeSequenceTest {
  /**
   * Empty prefix and suffix, None control digit
   */
  @Test
  public void testReferencedInventoryTypeSequence_ControlDigit_None() {
    final Sequence sequence = createSequence(CalculationMethod.AUTONUMERING, null, null, null,
        ControlDigit.NONE);
    OBDal.getInstance().save(sequence);
    assertTrue("Sequence with control digit None and empty prefix/suffix is not created.",
        sequence != null);
  }

  /** Numeric prefix */
  @Test
  public void testReferencedInventoryTypeSequence_ControlDigit_Module10_a() {
    final Sequence sequence = createSequence(CalculationMethod.AUTONUMERING, null, "102", null,
        ControlDigit.MODULE10);
    OBDal.getInstance().save(sequence);
    assertTrue("Sequence with control digit Module 10 and numeric prefix is not created.",
        sequence != null);
  }

  /** Alphanumeric prefix */
  @Test
  public void testReferencedInventoryTypeSequence_ControlDigit_Module10_b() {
    final Sequence sequence = createSequence(CalculationMethod.AUTONUMERING, null, "1A2", null,
        ControlDigit.MODULE10);
    OBException exception = assertThrows(OBException.class,
        () -> OBDal.getInstance().save(sequence));
    assertThat(exception.getMessage(),
        containsString(OBMessageUtils.messageBD("ValidateSequence")));
  }

  /** Numeric suffix */
  @Test
  public void testReferencedInventoryTypeSequence_ControlDigit_Module10_c() {
    final Sequence sequence = createSequence(CalculationMethod.AUTONUMERING, null, null, "101",
        ControlDigit.MODULE10);
    OBDal.getInstance().save(sequence);
    assertTrue("Sequence with control digit Module 10 and numeric suffix is not created.",
        sequence != null);
  }

  /** Alphanumeric suffix */
  @Test
  public void testReferencedInventoryTypeSequence_ControlDigit_Module10_d() {
    final Sequence sequence = createSequence(CalculationMethod.AUTONUMERING, null, null, "1A2",
        ControlDigit.MODULE10);
    OBException exception = assertThrows(OBException.class,
        () -> OBDal.getInstance().save(sequence));
    assertThat(exception.getMessage(),
        containsString(OBMessageUtils.messageBD("ValidateSequence")));
  }

  /** Use Base sequence with alphanumeric prefix */
  @Test
  public void testReferencedInventoryTypeSequence_ControlDigit_Module10_e() {
    final Sequence baseSequence = createSequence(CalculationMethod.AUTONUMERING, null, "1A2", null,
        ControlDigit.NONE);
    OBDal.getInstance().save(baseSequence);
    final Sequence parentSequence = createSequence(CalculationMethod.SEQUENCE, baseSequence, "06",
        null, ControlDigit.MODULE10);
    OBException exception = assertThrows(OBException.class,
        () -> OBDal.getInstance().save(parentSequence));
    assertThat(exception.getMessage(),
        containsString(OBMessageUtils.messageBD("ValidateBaseSequence")));
  }

  /** Use Base sequence with alphanumeric suffix */
  @Test
  public void testReferencedInventoryTypeSequence_ControlDigit_Module10_f() {
    final Sequence baseSequence = createSequence(CalculationMethod.AUTONUMERING, null, null, "1A2",
        ControlDigit.NONE);
    OBDal.getInstance().save(baseSequence);
    final Sequence parentSequence = createSequence(CalculationMethod.SEQUENCE, baseSequence, "06",
        null, ControlDigit.MODULE10);
    OBException exception = assertThrows(OBException.class,
        () -> OBDal.getInstance().save(parentSequence));
    assertThat(exception.getMessage(),
        containsString(OBMessageUtils.messageBD("ValidateBaseSequence")));
  }

  /** Update Base sequence with alphanumeric prefix */
  @Test
  public void testReferencedInventoryTypeSequence_ControlDigit_Module10_g() {
    final Sequence baseSequence = createSequence(CalculationMethod.AUTONUMERING, null, null, "100",
        ControlDigit.NONE);
    OBDal.getInstance().save(baseSequence);
    final Sequence parentSequence = createSequence(CalculationMethod.SEQUENCE, baseSequence, "06",
        null, ControlDigit.MODULE10);
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

  /** Update Base sequence with numeric prefix */
  @Test
  public void testReferencedInventoryTypeSequence_ControlDigit_Module10_h() {
    final Sequence baseSequence = createSequence(CalculationMethod.AUTONUMERING, null, null, "100",
        ControlDigit.NONE);
    OBDal.getInstance().save(baseSequence);
    final Sequence parentSequence = createSequence(CalculationMethod.SEQUENCE, baseSequence, "06",
        null, ControlDigit.MODULE10);
    OBDal.getInstance().save(parentSequence);
    assertTrue(
        "Sequence with control digit Module 10 and base sequence with numeric prefix is not created.",
        parentSequence != null);
    baseSequence.setPrefix("102");
    OBDal.getInstance().save(baseSequence);
    assertTrue(
        "Sequence with control digit Module 10 and base sequence with numeric prefix is not updated.",
        baseSequence.getPrefix() == "102");
  }

  /** Update Base sequence with alphanumeric suffix */
  @Test
  public void testReferencedInventoryTypeSequence_ControlDigit_Module10_i() {
    final Sequence baseSequence = createSequence(CalculationMethod.AUTONUMERING, null, null, "100",
        ControlDigit.NONE);
    OBDal.getInstance().save(baseSequence);
    final Sequence parentSequence = createSequence(CalculationMethod.SEQUENCE, baseSequence, "06",
        null, ControlDigit.MODULE10);
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

  /** Update Base sequence with numeric suffix */
  @Test
  public void testReferencedInventoryTypeSequence_ControlDigit_Module10_j() {
    final Sequence baseSequence = createSequence(CalculationMethod.AUTONUMERING, null, null, "100",
        ControlDigit.NONE);
    OBDal.getInstance().save(baseSequence);
    final Sequence parentSequence = createSequence(CalculationMethod.SEQUENCE, baseSequence, "06",
        null, ControlDigit.MODULE10);
    OBDal.getInstance().save(parentSequence);
    assertTrue(
        "Sequence with control digit Module 10 and base sequence with numeric suffix is not created.",
        parentSequence != null);

    baseSequence.setSuffix("102");
    OBDal.getInstance().save(baseSequence);
    assertTrue(
        "Sequence with control digit Module 10 and base sequence with numeric prefix is not updated.",
        baseSequence.getSuffix() == "102");
  }

  /**
   * Update Parent Sequence having Module 10 control digit with new Base sequence with alphanumeric
   * suffix or prefix.
   */
  @Test
  public void testReferencedInventoryTypeSequence_ControlDigit_Module10_k() {
    final Sequence baseSequence = createSequence(CalculationMethod.AUTONUMERING, null, null, "100",
        ControlDigit.NONE);
    OBDal.getInstance().save(baseSequence);
    final Sequence parentSequence = createSequence(CalculationMethod.SEQUENCE, baseSequence, "06",
        null, ControlDigit.MODULE10);
    OBDal.getInstance().save(parentSequence);
    OBDal.getInstance().flush();

    // new base sequence with alphanumeric suffix
    final Sequence newBaseSequenceAlphanumericSuffix = createSequence(
        CalculationMethod.AUTONUMERING, null, null, "1A2", ControlDigit.NONE);
    OBDal.getInstance().save(newBaseSequenceAlphanumericSuffix);

    // set parent sequence having Module10 control digit with new base sequence with alphanumeric
    // suffix.
    parentSequence.setBaseSequence(newBaseSequenceAlphanumericSuffix);
    OBDal.getInstance().save(parentSequence);

    Exception exception = assertThrows(Exception.class, () -> OBDal.getInstance().flush());
    assertThat(exception.getMessage(),
        containsString(OBMessageUtils.messageBD("ValidateBaseSequence")));

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
        containsString(OBMessageUtils.messageBD("ValidateBaseSequence")));

  }

  /**
   * Update Parent or Base Sequence having None control digit to Module 10 with Parent or Base
   * sequence with alphanumeric suffix or prefix.
   */
  @Test
  public void testReferencedInventoryTypeSequence_ControlDigit_Module10_l() {
    final Sequence baseSequence = createSequence(CalculationMethod.AUTONUMERING, null, null, "1A2",
        ControlDigit.NONE);
    OBDal.getInstance().save(baseSequence);
    final Sequence parentSequence = createSequence(CalculationMethod.SEQUENCE, baseSequence, "100",
        null, ControlDigit.NONE);
    OBDal.getInstance().save(parentSequence);
    OBDal.getInstance().flush();

    parentSequence.setControlDigit(ControlDigit.MODULE10.value);
    OBDal.getInstance().save(parentSequence);

    OBException exception = assertThrows(OBException.class, () -> OBDal.getInstance().flush());
    assertThat(exception.getMessage(),
        containsString(OBMessageUtils.messageBD("ValidateBaseSequence")));

    baseSequence.setControlDigit(ControlDigit.MODULE10.value);
    OBDal.getInstance().save(parentSequence);

    exception = assertThrows(OBException.class, () -> OBDal.getInstance().flush());
    assertThat(exception.getMessage(),
        containsString(OBMessageUtils.messageBD("ValidateSequence")));
  }

  /**
   * Create sequence with calculation method, baseSequence, controlDigit, prefix and suffix
   * 
   * @return Sequence to be used for validate prefix/suffix/control digit combination
   */
  private Sequence createSequence(CalculationMethod calculationMethod, Sequence baseSequence,
      String prefix, String suffix, ControlDigit controlDigit) {
    return ReferencedInventoryTypeSequenceTestUtils.createDocumentSequence(calculationMethod,
        baseSequence, prefix, null, null, null, suffix, controlDigit, SequenceNumberLength.VARIABLE,
        null);
  }
}
