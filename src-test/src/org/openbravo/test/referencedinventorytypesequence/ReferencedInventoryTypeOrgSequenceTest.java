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

import org.junit.Test;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.SequenceUtil.CalculationMethod;
import org.openbravo.erpCommon.utility.SequenceUtil.ControlDigit;
import org.openbravo.erpCommon.utility.SequenceUtil.SequenceNumberLength;
import org.openbravo.materialmgmt.refinventory.ReferencedInventoryUtil.SequenceType;
import org.openbravo.model.ad.utility.Sequence;
import org.openbravo.model.materialmgmt.onhandquantity.ReferencedInventoryType;
import org.openbravo.model.materialmgmt.onhandquantity.ReferencedInventoryTypeOrgSequence;

public class ReferencedInventoryTypeOrgSequenceTest extends ReferencedInventoryTypeSequenceTest {

  /**
   * test unique sequence defined in Organization Sequence Tab for Referenced Inventory Type with
   * Sequence Type as Per Organization
   */
  @Test
  public void testReferencedInventoryTypeOrgSequenceTest() {
    final Sequence baseSequence = ReferencedInventoryTypeSequenceTestUtils.createDocumentSequence(
        CalculationMethod.AUTONUMERING, null, null, null, null, null, "100", ControlDigit.NONE,
        SequenceNumberLength.VARIABLE, null);
    OBDal.getInstance().save(baseSequence);
    final Sequence parentSequence = ReferencedInventoryTypeSequenceTestUtils.createDocumentSequence(
        CalculationMethod.SEQUENCE, baseSequence, "06", null, null, null, null,
        ControlDigit.MODULE10, SequenceNumberLength.VARIABLE, null);
    OBDal.getInstance().save(parentSequence);

    // Create Referenced Inventory Type with Sequence Type as Per Organization
    final ReferencedInventoryType refInvType = ReferencedInventoryTypeSequenceTestUtils
        .createReferencedInventoryType(SequenceType.PER_ORGANIZATION, parentSequence);
    OBDal.getInstance().save(refInvType);

    // Create Referenced Inventory Type Organization Sequence with Parent Sequence created Above.
    ReferencedInventoryTypeOrgSequence referencedInventoryTypeOrgSequence = ReferencedInventoryTypeSequenceTestUtils
        .createReferencedInventoryTypeOrgSeq(refInvType, parentSequence);
    OBDal.getInstance().save(referencedInventoryTypeOrgSequence);
    OBDal.getInstance().flush();

    referencedInventoryTypeOrgSequence = ReferencedInventoryTypeSequenceTestUtils
        .createReferencedInventoryTypeOrgSeq(refInvType, baseSequence);
    OBDal.getInstance().save(referencedInventoryTypeOrgSequence);

    Exception exception = assertThrows(Exception.class, () -> OBDal.getInstance().flush());
    assertThat(exception.getMessage(), containsString("ConstraintViolationException"));
  }
}
