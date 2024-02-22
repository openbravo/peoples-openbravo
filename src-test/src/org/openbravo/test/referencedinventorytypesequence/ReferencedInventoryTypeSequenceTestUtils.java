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

import java.util.UUID;

import org.openbravo.base.provider.OBProvider;
import org.openbravo.dal.core.DalUtil;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.SequenceUtil.CalculationMethod;
import org.openbravo.erpCommon.utility.SequenceUtil.ControlDigit;
import org.openbravo.erpCommon.utility.SequenceUtil.SequenceNumberLength;
import org.openbravo.materialmgmt.refinventory.ReferencedInventoryUtil.SequenceType;
import org.openbravo.model.ad.utility.Sequence;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.materialmgmt.onhandquantity.ReferencedInventoryType;
import org.openbravo.model.materialmgmt.onhandquantity.ReferencedInventoryTypeOrgSequence;

class ReferencedInventoryTypeSequenceTestUtils {
  static final String QA_SPAIN_ORG_ID = "357947E87C284935AD1D783CF6F099A1";

  /*
   * Creates Document Sequence
   */

  static Sequence createDocumentSequence(CalculationMethod calculationMethod, Sequence baseSequence,
      String prefix, Long startingNo, Long nextAssignedNumber, Long incrementBy, String suffix,
      ControlDigit controlDigit, SequenceNumberLength sequenceNoLength, Long sequenceLength) {
    final Sequence anyExistingSequence = OBDal.getInstance()
        .getProxy(Sequence.class, "FF8080812C2ABFC6012C2B3BE4970094");
    // Create a Sequence
    final Sequence sequence = (Sequence) DalUtil.copy(anyExistingSequence);
    sequence.setOrganization(OBDal.getInstance().getProxy(Organization.class, QA_SPAIN_ORG_ID));
    sequence.setName(UUID.randomUUID().toString());
    sequence.setControlDigit(controlDigit.value);
    sequence.setCalculationMethod(calculationMethod.value);
    sequence.setPrefix(prefix);
    sequence.setStartingNo(startingNo == null ? 1L : startingNo);
    sequence.setNextAssignedNumber(nextAssignedNumber == null ? 1L : nextAssignedNumber);
    sequence.setIncrementBy(incrementBy == null ? 1L : incrementBy);
    sequence.setSuffix(suffix);
    sequence.setBaseSequence(baseSequence);
    sequence.setSequenceNumberLength(sequenceNoLength.value);
    sequence.setSequenceLength(sequenceLength);
    return sequence;
  }

  /*
   * Creates Reference Inventory Type
   */

  static ReferencedInventoryType createReferencedInventoryType(SequenceType sequenceType,
      Sequence sequence) {
    final ReferencedInventoryType refInvType = OBProvider.getInstance()
        .get(ReferencedInventoryType.class);
    refInvType.setClient(OBContext.getOBContext().getCurrentClient());
    refInvType.setOrganization(OBDal.getInstance().getProxy(Organization.class, "0"));
    refInvType.setSequenceType(sequenceType.value);
    refInvType.setSequence(sequence);
    refInvType.setName(UUID.randomUUID().toString());
    refInvType.setShared(true);
    return refInvType;
  }

  /*
   * Creates Reference Inventory Type Organization Sequence when Sequence Type is Per Organization
   */

  static ReferencedInventoryTypeOrgSequence createReferencedInventoryTypeOrgSeq(
      ReferencedInventoryType refInvType, Organization org, Sequence parentSequence) {
    final ReferencedInventoryTypeOrgSequence refInvTypeOrgSeq = OBProvider.getInstance()
        .get(ReferencedInventoryTypeOrgSequence.class);
    refInvTypeOrgSeq.setClient(OBContext.getOBContext().getCurrentClient());
    refInvTypeOrgSeq.setOrganization(org);
    refInvTypeOrgSeq.setReferencedInventoryType(refInvType);
    refInvTypeOrgSeq.setSequence(parentSequence);
    return refInvTypeOrgSeq;
  }

}
