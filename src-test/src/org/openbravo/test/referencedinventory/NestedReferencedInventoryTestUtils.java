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
import static org.hamcrest.Matchers.equalTo;

import java.math.BigDecimal;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.hibernate.ScrollableResults;
import org.openbravo.dal.service.OBDal;
import org.openbravo.materialmgmt.refinventory.BoxProcessor;
import org.openbravo.materialmgmt.refinventory.ReferencedInventoryProcessor.StorageDetailJS;
import org.openbravo.materialmgmt.refinventory.ReferencedInventoryUtil;
import org.openbravo.materialmgmt.refinventory.ReferencedInventoryUtil.SequenceType;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.common.plm.Product;
import org.openbravo.model.materialmgmt.onhandquantity.ReferencedInventory;
import org.openbravo.model.materialmgmt.onhandquantity.ReferencedInventoryType;
import org.openbravo.model.materialmgmt.onhandquantity.StorageDetail;
import org.openbravo.model.materialmgmt.transaction.InternalMovement;

/**
 * This is Utility class used in Nested Referenced Inventory Tests.
 *
 */

class NestedReferencedInventoryTestUtils {

  /**
   * validate referenced inventory after box transaction
   */

  static void validateRIAfterBoxTransaction(final ReferencedInventory refInv,
      JSONArray selectedStorageDetailsJS, String toBinId, Integer noOfLines, Long uniqueItemCount,
      Long nestedRefInvCount) throws JSONException, Exception {
    InternalMovement boxMovement = new BoxProcessor(refInv, selectedStorageDetailsJS, toBinId)
        .createAndProcessGoodsMovement();

    OBDal.getInstance().refresh(boxMovement);
    OBDal.getInstance().refresh(refInv);

    assertThat("Box movement must be processed", boxMovement.isProcessed(), equalTo(true));

    if (noOfLines != null) {
      assertThat("Box Movement does not have " + noOfLines + "lines",
          boxMovement.getMaterialMgmtInternalMovementLineList().size(), equalTo(noOfLines));
    }
    if (uniqueItemCount != null) {
      assertThat("Nested Referenced Inventory does not have Unique Items Count equal to "
          + uniqueItemCount.intValue(), refInv.getUniqueItemsCount(), equalTo(uniqueItemCount));
    }

    if (nestedRefInvCount != null) {
      assertThat(
          "Nested Referenced Inventory Count is not equal to " + nestedRefInvCount.intValue(),
          refInv.getNestedReferencedInventoriesCount(), equalTo(nestedRefInvCount));
    }
  }

  /**
   * get Storage details for Nested RI
   */

  static JSONArray getStorageDetailsforNestedRI(final ReferencedInventory refInv, String toBinId)
      throws JSONException {
    final JSONArray StorageDetailJS = new JSONArray();
    try (ScrollableResults sdScroll = ReferencedInventoryUtil.getStorageDetails(refInv.getId(),
        true)) {
      while (sdScroll.next()) {
        final StorageDetail sd = (StorageDetail) sdScroll.get(0);
        final StorageDetailJS sdJS = new StorageDetailJS(sd.getId(), sd.getQuantityOnHand(),
            toBinId);
        StorageDetailJS.put(sdJS.toJSONObject());
      }
    }
    return StorageDetailJS;
  }

  /**
   * Add items in referenced inventory during boxing transaction
   */

  static JSONArray addProductInBox(final Product product, final String attributeSetInstanceId)
      throws Exception {
    final BigDecimal receivedQty = new BigDecimal("10");
    ReferencedInventoryTestUtils.receiveProduct(product, receivedQty, attributeSetInstanceId);
    final StorageDetail storageDetail = ReferencedInventoryTestUtils
        .getUniqueStorageDetail(product);
    final JSONArray storageDetailsJS = ReferencedInventoryTestUtils
        .getStorageDetailsToBoxJSArray(storageDetail, BigDecimal.ONE);
    return storageDetailsJS;
  }

  /**
   * Creates a referenced inventory with referenced inventory type having contentRestriction
   */

  static ReferencedInventory createRI(String contentRestriction) {
    final ReferencedInventoryType refInvType = ReferencedInventoryTestUtils
        .createReferencedInventoryType(OBDal.getInstance().getProxy(Organization.class, "0"),
            SequenceType.NONE, null, contentRestriction);

    return ReferencedInventoryTestUtils
        .createReferencedInventory(ReferencedInventoryTestUtils.QA_SPAIN_ORG_ID, refInvType);

  }
}
