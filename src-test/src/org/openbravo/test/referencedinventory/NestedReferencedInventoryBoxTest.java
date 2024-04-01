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
import org.junit.After;
import org.junit.Test;
import org.openbravo.dal.service.OBDal;
import org.openbravo.materialmgmt.refinventory.BoxProcessor;
import org.openbravo.materialmgmt.refinventory.ReferencedInventoryUtil.SequenceType;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.common.plm.Product;
import org.openbravo.model.materialmgmt.onhandquantity.ReferencedInventory;
import org.openbravo.model.materialmgmt.onhandquantity.ReferencedInventoryType;
import org.openbravo.model.materialmgmt.onhandquantity.StorageDetail;
import org.openbravo.model.materialmgmt.transaction.InternalMovement;

/**
 * This is class to test Nested Referenced Inventory Functionalities.
 *
 */

public class NestedReferencedInventoryBoxTest extends ReferencedInventoryTest {

  @Test
  public void testBox() throws Exception {
    createReferencedInventory();
  }

  @Test
  public void testNestedBox() throws Exception {
    createNestedReferencedInventory();
  }

  /**
   * create referenced inventory and do boxing transaction
   */

  private void createReferencedInventory() throws Exception {
    final ReferencedInventoryType refInvType = ReferencedInventoryTestUtils
        .createReferencedInventoryType(OBDal.getInstance().getProxy(Organization.class, "0"),
            SequenceType.NONE, null,
            ReferencedInventoryTestUtils.CONTENTRESTRICTION_BOTH_ITEMS_OR_HU);
    final ReferencedInventory refInv = ReferencedInventoryTestUtils
        .createReferencedInventory(ReferencedInventoryTestUtils.QA_SPAIN_ORG_ID, refInvType);
    JSONArray selectedStorageDetailsJS = new JSONArray();
    String toBinId = BINS[0];

    final Product firstProduct = ReferencedInventoryTestUtils.cloneProduct(PRODUCTS[0][0]);
    final Product secondProduct = ReferencedInventoryTestUtils.cloneProduct(PRODUCTS[1][0]);
    selectedStorageDetailsJS.put(addProductInBox(refInv, firstProduct, PRODUCTS[0][1]).get(0));

    // Store second Product Storage Detail with Attribute set instance to be added later on
    JSONArray secondProductSD = new JSONArray();
    secondProductSD.put(addProductInBox(refInv, secondProduct, PRODUCTS[1][1]).get(0));
    selectedStorageDetailsJS.put(secondProductSD.get(0));

    InternalMovement boxMovement = new BoxProcessor(refInv, selectedStorageDetailsJS, toBinId)
        .createAndProcessGoodsMovement();

    OBDal.getInstance().refresh(boxMovement);
    OBDal.getInstance().refresh(refInv);

    assertsGoodsMovementIsProcessed(boxMovement);
    assertsGoodsMovementNumberOfLines(boxMovement, 2);
    assertThat("Nested Referenced Inventory does not have Unique Items Count equal to 2",
        refInv.getUniqueItemsCount(), equalTo(2L));

    // Add product in with attribute set instance which is already present in Box, added during
    // previous Box transaction
    selectedStorageDetailsJS = new JSONArray();
    selectedStorageDetailsJS.put(secondProductSD.get(0));
    boxMovement = new BoxProcessor(refInv, selectedStorageDetailsJS, toBinId)
        .createAndProcessGoodsMovement();
    OBDal.getInstance().refresh(boxMovement);
    OBDal.getInstance().refresh(refInv);

    assertsGoodsMovementIsProcessed(boxMovement);
    assertThat("Nested Referenced Inventory does not have Unique Items Count equal to 2",
        refInv.getUniqueItemsCount(), equalTo(2L));

  }

  /**
   * create nested referenced inventory
   */

  private void createNestedReferencedInventory() throws Exception {
    final ReferencedInventoryType refInvType = ReferencedInventoryTestUtils
        .createReferencedInventoryType(OBDal.getInstance().getProxy(Organization.class, "0"),
            SequenceType.NONE, null,
            ReferencedInventoryTestUtils.CONTENTRESTRICTION_BOTH_ITEMS_OR_HU);
    final ReferencedInventory refInv = ReferencedInventoryTestUtils
        .createReferencedInventory(ReferencedInventoryTestUtils.QA_SPAIN_ORG_ID, refInvType);
    JSONArray selectedStorageDetailsJS = new JSONArray();
    String toBinId = BINS[0];

    final Product firstProduct = ReferencedInventoryTestUtils.cloneProduct(PRODUCTS[0][0]);
    final Product secondProduct = ReferencedInventoryTestUtils.cloneProduct(PRODUCTS[1][0]);
    selectedStorageDetailsJS.put(addProductInBox(refInv, firstProduct, PRODUCTS[0][1]).get(0));
    selectedStorageDetailsJS.put(addProductInBox(refInv, secondProduct, PRODUCTS[1][1]).get(0));

    InternalMovement boxMovement = new BoxProcessor(refInv, selectedStorageDetailsJS, toBinId)
        .createAndProcessGoodsMovement();

    OBDal.getInstance().refresh(boxMovement);
    OBDal.getInstance().refresh(refInv);

    assertsGoodsMovementIsProcessed(boxMovement);
    assertsGoodsMovementNumberOfLines(boxMovement, 2);
    assertThat("Nested Referenced Inventory does not have Unique Items Count equal to 2",
        refInv.getUniqueItemsCount(), equalTo(2L));

    // Create a nested Referenced Inventory
    final StorageDetail storageDetail = refInv.getMaterialMgmtStorageDetailList().get(0);

    final ReferencedInventory refInvNested = ReferencedInventoryTestUtils.createReferencedInventory(
        ReferencedInventoryTestUtils.QA_SPAIN_ORG_ID, refInv.getReferencedInventoryType());
    final JSONArray storageDetailsJS = ReferencedInventoryTestUtils
        .getStorageDetailsToBoxJSArray(storageDetail, BigDecimal.ONE);

    new BoxProcessor(refInvNested, storageDetailsJS, storageDetail.getStorageBin().getId())
        .createAndProcessGoodsMovement();

  }

  /**
   * Add items in referenced inventory during boxing transaction
   */

  private JSONArray addProductInBox(final ReferencedInventory refInv, final Product product,
      final String attributeSetInstanceId) throws Exception {
    final BigDecimal receivedQty = new BigDecimal("10");
    ReferencedInventoryTestUtils.receiveProduct(product, receivedQty, attributeSetInstanceId);
    final StorageDetail storageDetail = ReferencedInventoryTestUtils
        .getUniqueStorageDetail(product);
    final JSONArray storageDetailsJS = ReferencedInventoryTestUtils
        .getStorageDetailsToBoxJSArray(storageDetail, BigDecimal.ONE);
    return storageDetailsJS;
  }

  @Override
  @After
  public void clearSession() {
    OBDal.getInstance().rollbackAndClose();
  }
}
