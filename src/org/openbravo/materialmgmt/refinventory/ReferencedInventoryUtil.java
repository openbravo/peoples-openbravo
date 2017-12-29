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
 * All portions are Copyright (C) 2017 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.materialmgmt.refinventory;

import org.apache.commons.lang.StringUtils;
import org.openbravo.advpaymentmngt.utility.FIN_Utility;
import org.openbravo.dal.core.DalUtil;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.ad.utility.Sequence;
import org.openbravo.model.common.plm.AttributeSetInstance;
import org.openbravo.model.materialmgmt.onhandquantity.ReferencedInventory;
import org.openbravo.model.materialmgmt.onhandquantity.ReferencedInventoryType;
import org.openbravo.model.materialmgmt.onhandquantity.StorageDetail;

/**
 * Utility class for Referenced Inventory feature
 *
 */
public class ReferencedInventoryUtil {
  public static final String REFERENCEDINVENTORYPREFIX = "RI";

  /**
   * Create and return a new AttributeSetInstance from the given originalAttributeSetInstance and
   * link it to the given referencedInventory
   */
  public static final AttributeSetInstance cloneAttributeSetInstance(
      final AttributeSetInstance originalAttributeSetInstance,
      final ReferencedInventory referencedInventory) {
    final AttributeSetInstance newAttributeSetInstance = (AttributeSetInstance) DalUtil.copy(
        originalAttributeSetInstance, false);
    newAttributeSetInstance.setClient(referencedInventory.getClient());
    newAttributeSetInstance.setOrganization(originalAttributeSetInstance.getOrganization());
    newAttributeSetInstance.setParentAttributeSetInstance(originalAttributeSetInstance);
    newAttributeSetInstance.setReferencedInventory(referencedInventory);
    newAttributeSetInstance.setDescription(StringUtils.left(
        (StringUtils.isBlank(newAttributeSetInstance.getDescription()) ? ""
            : newAttributeSetInstance.getDescription())
            + REFERENCEDINVENTORYPREFIX
            + referencedInventory.getSearchKey(), 255));
    OBDal.getInstance().save(newAttributeSetInstance);
    return newAttributeSetInstance;
  }

  /**
   * Returns the parent attribute set instance for the given storage detail. If not found it returns
   * null
   */
  public static final AttributeSetInstance getParentAttributeSetInstance(
      final StorageDetail storageDetail) {
    try {
      return storageDetail.getAttributeSetValue().getParentAttributeSetInstance();
    } catch (NullPointerException noParentFound) {
      return null;
    }
  }

  /**
   * Returns the parent reference inventory for the given storage detail. If not found it returns
   * null
   */
  public static final ReferencedInventory getParentReferenceInventory(
      final StorageDetail storageDetail) {
    try {
      return getParentAttributeSetInstance(storageDetail).getReferencedInventory();
    } catch (NullPointerException noParentFound) {
      return null;
    }
  }

  /**
   * If the given referenced inventory type id is associated to a sequence, it then return the next
   * value in that sequence. Otherwise returns null.
   * 
   * @param referencedInventoryTypeId
   *          Referenced Inventory Type Id used to get its sequence
   * @param updateNext
   *          if true updates the sequence's next value in database
   */
  public static String getProposedValueFromSequenceOrNull(final String referencedInventoryTypeId,
      final boolean updateNext) {
    return FIN_Utility.getDocumentNo(updateNext, getSequence(referencedInventoryTypeId));
  }

  /**
   * Returns the sequence associated to the given referenced inventory type id or null if not found
   */
  private static Sequence getSequence(final String referencedInventoryTypeId) {
    return OBDal.getInstance().get(ReferencedInventoryType.class, referencedInventoryTypeId)
        .getSequence();
  }

}
