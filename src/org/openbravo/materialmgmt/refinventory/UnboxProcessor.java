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
 * All portions are Copyright (C) 2017-2024 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.materialmgmt.refinventory;

import java.util.Date;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.util.Check;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBDateUtils;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.model.common.plm.AttributeSetInstance;
import org.openbravo.model.materialmgmt.onhandquantity.ReferencedInventory;
import org.openbravo.model.materialmgmt.onhandquantity.StorageDetail;

/**
 * Process of unboxing storage details or referenced inventories out of a referenced inventory
 */
public class UnboxProcessor extends ReferencedInventoryProcessor {

  private boolean unboxToIndividualItems;

  @Deprecated
  public UnboxProcessor(final ReferencedInventory referencedInventory,
      final JSONArray selectedStorageDetails) throws JSONException {
    this(referencedInventory, selectedStorageDetails, true);
  }

  public UnboxProcessor(final ReferencedInventory referencedInventory,
      final JSONArray selectedStorageDetails, boolean unboxToIndividualItems) throws JSONException {
    super(referencedInventory, selectedStorageDetails);
    checkStorageDetailsHaveReferencedInventory(selectedStorageDetails);
    this.unboxToIndividualItems = unboxToIndividualItems;
  }

  private void checkStorageDetailsHaveReferencedInventory(final JSONArray selectedStorageDetails)
      throws JSONException {
    for (int i = 0; i < selectedStorageDetails.length(); i++) {
      final JSONObject storageDetailJS = selectedStorageDetails.getJSONObject(i);
      final StorageDetail storageDetail = getStorageDetail(storageDetailJS);
      Check.isNotNull(storageDetail.getReferencedInventory(),
          String.format(OBMessageUtils.messageBD("StorageDetailNotLinkedToReferencedInventory"),
              storageDetail.getIdentifier()));
    }
  }

  @Override
  protected AttributeSetInstance getAttributeSetInstanceTo(StorageDetail storageDetail) {
    final AttributeSetInstance storageDetailAttributeSetInstance = storageDetail
        .getAttributeSetValue();
    final AttributeSetInstance innerMostAttributeSetInstance = ReferencedInventoryUtil
        .getInnerMostAttributeSetInstance(storageDetailAttributeSetInstance);
    final boolean isAlreadyTheInnerMost = storageDetailAttributeSetInstance.getId()
        .equals(innerMostAttributeSetInstance.getId());

    if (unboxToIndividualItems || isAlreadyTheInnerMost) {
      // Unbox from all the boxes
      return innerMostAttributeSetInstance.getParentAttributeSetInstance();
    } else {
      // Unbox from the selected box, but keep the stock in any inner box
      return ReferencedInventoryUtil.getInnerAttributeSetInstanceLinkedToRefInventory(
          storageDetailAttributeSetInstance, getReferencedInventory());
    }
  }

  @Override
  protected String generateInternalMovementName() {
    return OBDateUtils.formatDateTime(new Date()) + "_" + OBMessageUtils.messageBD("UNBOX");
  }

  @Override
  protected String getNewStorageBinId(JSONObject storageDetailJS) {
    try {
      return storageDetailJS.getString(GridJS.STORAGEBIN_ID);
    } catch (JSONException e) {
      throw new OBException("Error getting new storage bin for storage detail: " + storageDetailJS,
          e);
    }
  }

  @Override
  protected int updateParentReferenceInventory() {
    return unboxToIndividualItems ? removeParentRefInventoryIfEmpty()
        : removeParentRefInventoryForThisRIAndAnyImmediateRIWhenOutermost();
  }

  /**
   * Remove the parent reference inventory, i.e. move the reference inventory outside of the
   * outermost reference inventory, if this reference inventory has no stock remaining after the
   * unbox process
   */
  private int removeParentRefInventoryIfEmpty() {
    //@formatter:off
    final String hql = "update MaterialMgmtReferencedInventory ri "
                     + "set ri.parentRefInventory.id = null "
                       // Always this box
                     + " where ri.id = :thisRefInventoryId "
                       // if no stock remaining
                     + " and not exists (select 1 "
                     + "                 from MaterialMgmtStorageDetail sd "
                     + "                 where sd.referencedInventory.id = ri.id) ";
    //@formatter:on
    return OBDal.getInstance()
        .getSession()
        .createQuery(hql)
        .setParameter("thisRefInventoryId", this.getReferencedInventory().getId())
        .executeUpdate();
  }

  /**
   * Remove always the parent reference inventory for this RI.
   * 
   * Besides, if this RI is the outermost one, remove the parent reference inventory of the
   * immediate inner RIs, i.e. the former immediate inner RIs are now the outermost RIs.
   */
  private int removeParentRefInventoryForThisRIAndAnyImmediateRIWhenOutermost() {
    //@formatter:off
    final String hql = "update MaterialMgmtReferencedInventory ri "
                     + "set ri.parentRefInventory.id = null "
                       // Always this box
                     + "where ri.id = :thisRefInventoryId "
                       // Immediate inner boxes if this box is the outermost
                     + "or (ri.parentRefInventory.id = :thisRefInventoryId "
                     + "    and exists (select 1 from MaterialMgmtReferencedInventory rip "
                     + "                where rip.id = ri.parentRefInventory.id "
                     + "                and rip.parentRefInventory.id is null) "
                     + "    )";
    //@formatter:on
    return OBDal.getInstance()
        .getSession()
        .createQuery(hql)
        .setParameter("thisRefInventoryId", this.getReferencedInventory().getId())
        .executeUpdate();
  }

}
