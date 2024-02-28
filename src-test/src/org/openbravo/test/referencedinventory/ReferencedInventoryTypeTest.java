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
package org.openbravo.test.referencedinventory;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.SequenceUtil.CalculationMethod;
import org.openbravo.erpCommon.utility.SequenceUtil.ControlDigit;
import org.openbravo.erpCommon.utility.SequenceUtil.SequenceNumberLength;
import org.openbravo.materialmgmt.refinventory.ReferencedInventoryUtil.SequenceType;
import org.openbravo.model.ad.utility.Sequence;
import org.openbravo.model.materialmgmt.onhandquantity.ReferencedInventoryType;

public class ReferencedInventoryTypeTest extends ReferencedInventoryTest {

  /**
   * test Referenced Inventory Type with Sequence Type : None
   */
  @Test
  public void testReferencedInventoryType_None() {
    final ReferencedInventoryType referencedInventoryType = ReferencedInventoryTestUtils
        .createReferencedInventoryType(SequenceType.NONE, null);
    OBDal.getInstance().save(referencedInventoryType);
    OBDal.getInstance().flush();
    assertTrue("Referenced Inventory Type with Sequence Type - None is not created",
        referencedInventoryType != null);
  }

  /**
   * test Referenced Inventory Type with Sequence Type : Global
   */

  @Test
  public void testReferencedInventoryType_Global() {
    final ReferencedInventoryType referencedInventoryType = ReferencedInventoryTestUtils
        .createReferencedInventoryType(SequenceType.GLOBAL, null);
    OBDal.getInstance().save(referencedInventoryType);
    Exception exception = assertThrows(Exception.class, () -> OBDal.getInstance().flush());
    assertThat(exception.getMessage(), containsString("ConstraintViolationException"));
  }

  /**
   * test Referenced Inventory Type with Sequence Type : Per Organization
   */
  @Test
  public void testReferencedInventoryType_PerOrganization() {
    final ReferencedInventoryType referencedInventoryType = ReferencedInventoryTestUtils
        .createReferencedInventoryType(SequenceType.PER_ORGANIZATION, null);
    OBDal.getInstance().save(referencedInventoryType);
    OBDal.getInstance().flush();
    assertTrue("Referenced Inventory Type with Sequence Type - None is not created",
        referencedInventoryType != null);
  }

  /**
   * test Referenced Inventory Type with Sequence Type : None and Sequence not empty
   */

  @Test
  public void testReferencedInventoryType_None_Sequence() {
    final ReferencedInventoryType referencedInventoryType = ReferencedInventoryTestUtils
        .createReferencedInventoryType(SequenceType.NONE, createSequence());
    OBDal.getInstance().save(referencedInventoryType);
    OBDal.getInstance().flush();
    assertTrue("Referenced Inventory Type with Sequence Type - None is set with Sequence",
        referencedInventoryType.getSequence() == null);
  }

  /**
   * test Referenced Inventory Type with Sequence Type : Per Organization and Sequence not empty
   */

  @Test
  public void testReferencedInventoryType_PerOrganization_Sequence() {
    final ReferencedInventoryType referencedInventoryType = ReferencedInventoryTestUtils
        .createReferencedInventoryType(SequenceType.PER_ORGANIZATION, createSequence());
    OBDal.getInstance().save(referencedInventoryType);
    OBDal.getInstance().flush();
    assertTrue(
        "Referenced Inventory Type with Sequence Type - Per Organization is set with Sequence",
        referencedInventoryType.getSequence() == null);
  }

  /**
   * test Referenced Inventory Type with Sequence Type : Global and Sequence not empty
   */

  @Test
  public void testReferencedInventoryType_Global_Sequence_a() {
    final ReferencedInventoryType referencedInventoryType = ReferencedInventoryTestUtils
        .createReferencedInventoryType(SequenceType.GLOBAL, createSequence());
    OBDal.getInstance().save(referencedInventoryType);
    OBDal.getInstance().flush();
    assertTrue("Referenced Inventory Type with Sequence Type - Global is not set with Sequence",
        referencedInventoryType.getSequence() != null);
  }

  /**
   * test Referenced Inventory Type with Sequence Type : Global and Update Sequence as empty
   */

  @Test
  public void testReferencedInventoryType_Global_Sequence_b() {
    final ReferencedInventoryType referencedInventoryType = ReferencedInventoryTestUtils
        .createReferencedInventoryType(SequenceType.GLOBAL, createSequence());
    OBDal.getInstance().save(referencedInventoryType);
    OBDal.getInstance().flush();

    referencedInventoryType.setSequence(null);
    OBDal.getInstance().save(referencedInventoryType);
    Exception exception = assertThrows(Exception.class, () -> OBDal.getInstance().flush());
    assertThat(exception.getMessage(), containsString("ConstraintViolationException"));
  }

  /**
   * Create sequence with calculation method auto numbering
   * 
   * @return Sequence to be used for defined Referenced Inventory Type
   */
  private Sequence createSequence() {
    final Sequence sequence = ReferencedInventoryTestUtils.createDocumentSequence(
        CalculationMethod.AUTONUMERING, null, "0110491", null, null, null, null, ControlDigit.NONE,
        SequenceNumberLength.VARIABLE, null);
    OBDal.getInstance().save(sequence);
    return sequence;
  }
}
