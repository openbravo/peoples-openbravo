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
 * All portions are Copyright (C) 2018-2024 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.test.referencedinventory;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.util.UUID;

import org.junit.Test;
import org.openbravo.dal.core.DalUtil;
import org.openbravo.dal.service.OBDal;
import org.openbravo.materialmgmt.refinventory.ReferencedInventoryUtil;
import org.openbravo.model.ad.utility.Sequence;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.materialmgmt.onhandquantity.ReferencedInventory;
import org.openbravo.model.materialmgmt.onhandquantity.ReferencedInventoryType;

/**
 * Test referenced inventory type sequence is properly used
 */
public class ReferencedInventorySequenceTest extends ReferencedInventoryTest {
  private static final String ANY_EXISTING_SEQUENCE_ID = "FF8080812C2ABFC6012C2B3BE4970094";

  @Test
  public void testReferencedInventorySequenceIsUsed() {
    final Sequence sequence = (Sequence) DalUtil
        .copy(OBDal.getInstance().getProxy(Sequence.class, ANY_EXISTING_SEQUENCE_ID));
    sequence.setName(UUID.randomUUID().toString());
    OBDal.getInstance().save(sequence);
    OBDal.getInstance().flush(); // Required to lock sequence at db level later on

    final ReferencedInventoryType refInvType = ReferencedInventoryTestUtils
        .createReferencedInventoryType();
    refInvType.setSequence(sequence);
    OBDal.getInstance().save(refInvType);
    Long currentSequenceNumber = sequence.getNextAssignedNumber();

    final ReferencedInventory refInv = ReferencedInventoryTestUtils
        .createReferencedInventory(ReferencedInventoryTestUtils.QA_SPAIN_ORG_ID, refInvType);
    assertThat("Referenced Inventory Search Key is taken from sequence", refInv.getSearchKey(),
        equalTo(Long.toString(currentSequenceNumber)));

    final ReferencedInventory refInv2 = ReferencedInventoryTestUtils
        .createReferencedInventory(ReferencedInventoryTestUtils.QA_SPAIN_ORG_ID, refInvType);
    assertThat("Referenced Inventory Search Key is updated from sequence", refInv2.getSearchKey(),
        equalTo(Long.toString(currentSequenceNumber + 1)));
  }

  /**
   * Test sequence computation in Referenced Inventory using Module 10 algorithm
   */

  @Test
  public void testReferenceInventorySequenceUsingModule10() {

    Organization org = OBDal.getInstance()
        .getProxy(Organization.class, ReferencedInventoryTestUtils.QA_SPAIN_ORG_ID);
    String orgCode = org.getSearchKey();

    // Set numeric search key for Organization
    setOrganizationCode(org, "10491");

    // Create a Child Sequence
    final Sequence childSequence = (Sequence) DalUtil
        .copy(OBDal.getInstance().getProxy(Sequence.class, ANY_EXISTING_SEQUENCE_ID));
    childSequence.setOrganization(org);
    childSequence.setName(UUID.randomUUID().toString());
    childSequence.setCalculationMethod("A");
    childSequence.setIncrementBy(1L);
    childSequence.setNextAssignedNumber(2821L);
    childSequence.setStartingNo(2820L);
    childSequence.setPrefix("01");
    childSequence.setSuffix(null);
    childSequence.setControlDigit("M10");
    childSequence.setSequenceLengthType("V");
    childSequence.setBaseSequence(null);
    childSequence.setSequenceLength(null);
    OBDal.getInstance().save(childSequence);
    OBDal.getInstance().flush(); // Required to lock sequence at db level later on

    // Create Parent Sequence with Child Sequence created above as its Base Sequence
    final Sequence parentSequence = (Sequence) DalUtil
        .copy(OBDal.getInstance().getProxy(Sequence.class, ANY_EXISTING_SEQUENCE_ID));
    parentSequence.setOrganization(OBDal.getInstance()
        .getProxy(Organization.class, ReferencedInventoryTestUtils.QA_SPAIN_ORG_ID));
    parentSequence.setName(UUID.randomUUID().toString());
    parentSequence.setCalculationMethod("S");
    parentSequence.setIncrementBy(1L);
    parentSequence.setNextAssignedNumber(1000000L);
    parentSequence.setStartingNo(1000000L);
    parentSequence.setPrefix("6");
    parentSequence.setSuffix("000");
    parentSequence.setControlDigit("M10");
    parentSequence.setSequenceLengthType("V");
    parentSequence.setBaseSequence(childSequence);
    parentSequence.setSequenceLength(null);
    OBDal.getInstance().save(parentSequence);
    OBDal.getInstance().flush(); // Required to lock sequence at db level later on

    // Create Referenced Inventory Type with Sequence Type as Per Organization
    final ReferencedInventoryType refInvType = ReferencedInventoryTestUtils
        .createReferencedInventoryType();
    refInvType.setOrganization(OBDal.getInstance()
        .getProxy(Organization.class, ReferencedInventoryTestUtils.QA_SPAIN_ORG_ID));
    refInvType.setSequenceType("P");
    OBDal.getInstance().save(refInvType);
    OBDal.getInstance().flush(); // Required to lock sequence at db level later on

    // Create Referenced Inventory Type Organization Sequence with Parent Sequence created Above.
    ReferencedInventoryTestUtils.createReferencedInventoryTypeOrgSeq(refInvType, parentSequence);

    String proposedSequence = ReferencedInventoryUtil.getProposedValueFromSequenceOrNull(
        refInvType.getId(), ReferencedInventoryTestUtils.QA_SPAIN_ORG_ID, false);
    assertThat("Referenced Inventory Search Key is computed from sequence", proposedSequence,
        equalTo("<60110491282110004>"));

    // Reset search key of Organization
    setOrganizationCode(org, orgCode);
  }

  /**
   * Sets numeric search key to the organization used in the test
   *
   * @param org
   *          Organization used to create Sequence, Handling Unit Type, Handling Unit
   * @param searchKey
   *          Search Key of the Organization
   */

  private void setOrganizationCode(Organization org, String searchKey) {
    org.setSearchKey(searchKey);
    OBDal.getInstance().save(org);
    OBDal.getInstance().flush();
  }
}
