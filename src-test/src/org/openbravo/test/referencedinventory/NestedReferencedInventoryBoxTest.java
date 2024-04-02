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
  public void testBoxAddProduct() throws Exception {
    createReferencedInventoryAddProduct();
  }

  @Test
  public void testNestedBox() throws Exception {
    createNestedReferencedInventory();
  }

  @Test
  public void testNestedBoxAddProduct() throws Exception {
    createNestedReferencedInventoryAddProduct();
  }

  /**
   * create referenced inventory and do boxing transaction
   */

  private ReferencedInventory createReferencedInventory() throws Exception {
    final String toBinId = BINS[0];

    final ReferencedInventory refInv = createRI(
        ReferencedInventoryTestUtils.CONTENTRESTRICTION_BOTH_ITEMS_OR_HU);

    JSONArray selectedStorageDetailsJS = new JSONArray();

    final Product firstProduct = ReferencedInventoryTestUtils.cloneProduct(PRODUCTS[0][0]);
    selectedStorageDetailsJS.put(addProductInBox(refInv, firstProduct, PRODUCTS[0][1]).get(0));

    final Product secondProduct = ReferencedInventoryTestUtils.cloneProduct(PRODUCTS[1][0]);
    selectedStorageDetailsJS.put(addProductInBox(refInv, secondProduct, PRODUCTS[1][1]).get(0));

    validateRIAfterBoxTransaction(refInv, selectedStorageDetailsJS, toBinId, 2, 2L);
    return refInv;
  }

  /**
   * validate referenced inventory after box transaction
   */

  private void validateRIAfterBoxTransaction(final ReferencedInventory refInv,
      JSONArray selectedStorageDetailsJS, String toBinId, Integer noOfLines, Long uniqueItemCount)
      throws JSONException, Exception {
    InternalMovement boxMovement = new BoxProcessor(refInv, selectedStorageDetailsJS, toBinId)
        .createAndProcessGoodsMovement();

    OBDal.getInstance().refresh(boxMovement);
    OBDal.getInstance().refresh(refInv);

    assertsGoodsMovementIsProcessed(boxMovement);
    if (noOfLines != null) {
      assertsGoodsMovementNumberOfLines(boxMovement, noOfLines);
    }
    if (uniqueItemCount != null) {
      assertThat("Nested Referenced Inventory does not have Unique Items Count equal to 2",
          refInv.getUniqueItemsCount(), equalTo(uniqueItemCount));
    }
  }

  /**
   * create referenced inventory and do boxing transaction
   */

  private void createReferencedInventoryAddProduct() throws Exception {

    final String toBinId = BINS[0];

    final ReferencedInventory refInv = createRI(
        ReferencedInventoryTestUtils.CONTENTRESTRICTION_BOTH_ITEMS_OR_HU);

    JSONArray selectedStorageDetailsJS = new JSONArray();

    // Store first Product Storage Detail without Attribute set instance to be added later on
    final Product firstProduct = ReferencedInventoryTestUtils.cloneProduct(PRODUCTS[0][0]);
    JSONArray firstProductSD = new JSONArray();
    firstProductSD.put(addProductInBox(refInv, firstProduct, PRODUCTS[0][1]).get(0));
    selectedStorageDetailsJS.put(firstProductSD.get(0));

    final Product secondProduct = ReferencedInventoryTestUtils.cloneProduct(PRODUCTS[1][0]);
    selectedStorageDetailsJS.put(addProductInBox(refInv, secondProduct, PRODUCTS[1][1]).get(0));

    validateRIAfterBoxTransaction(refInv, selectedStorageDetailsJS, toBinId, 2, 2L);

    // Add product in without attribute set instance which is already present in Box, added during
    // previous Box transaction in the referenced inventory
    selectedStorageDetailsJS = new JSONArray();
    selectedStorageDetailsJS.put(firstProductSD.get(0));
    validateRIAfterBoxTransaction(refInv, selectedStorageDetailsJS, toBinId, 1, 2L);

  }

  /**
   * create nested referenced inventory
   */

  private void createNestedReferencedInventory() throws Exception {

    final String toBinId = BINS[0];
    final ReferencedInventory refInv = createReferencedInventory();
    // Create a nested Referenced Inventory
    final ReferencedInventory refInvNested = ReferencedInventoryTestUtils.createReferencedInventory(
        ReferencedInventoryTestUtils.QA_SPAIN_ORG_ID, refInv.getReferencedInventoryType());

    final JSONArray storageDetailsForNestedRI = new JSONArray();
    for (StorageDetail sd : refInv.getMaterialMgmtStorageDetailList()) {
      storageDetailsForNestedRI.put(
          ReferencedInventoryTestUtils.getStorageDetailsToBoxJSArray(sd, BigDecimal.ONE).get(0));
    }
    validateRIAfterBoxTransaction(refInvNested, storageDetailsForNestedRI, toBinId, 2, 2L);
  }

  /**
   * create referenced inventory and do boxing transaction
   */

  private void createNestedReferencedInventoryAddProduct() throws Exception {

    final String toBinId = BINS[0];

    JSONArray selectedStorageDetailsJS = new JSONArray();

    final ReferencedInventory refInv = createRI(
        ReferencedInventoryTestUtils.CONTENTRESTRICTION_BOTH_ITEMS_OR_HU);

    // Store first Product Storage Detail without Attribute set instance to be added later on
    final Product firstProduct = ReferencedInventoryTestUtils.cloneProduct(PRODUCTS[0][0]);
    JSONArray firstProductSD = new JSONArray();
    firstProductSD.put(addProductInBox(refInv, firstProduct, PRODUCTS[0][1]).get(0));
    selectedStorageDetailsJS.put(firstProductSD.get(0));

    final Product secondProduct = ReferencedInventoryTestUtils.cloneProduct(PRODUCTS[1][0]);
    selectedStorageDetailsJS.put(addProductInBox(refInv, secondProduct, PRODUCTS[1][1]).get(0));

    validateRIAfterBoxTransaction(refInv, selectedStorageDetailsJS, toBinId, 2, 2L);

    // create nested referenced inventory

    final ReferencedInventory refInvNested = createRI(
        ReferencedInventoryTestUtils.CONTENTRESTRICTION_BOTH_ITEMS_OR_HU);

    final JSONArray storageDetailsForNestedRI = new JSONArray();
    for (StorageDetail sd : refInv.getMaterialMgmtStorageDetailList()) {
      storageDetailsForNestedRI.put(
          ReferencedInventoryTestUtils.getStorageDetailsToBoxJSArray(sd, BigDecimal.ONE).get(0));
    }

    validateRIAfterBoxTransaction(refInvNested, storageDetailsForNestedRI, toBinId, 2, 2L);

    // Add product without attribute set instance which is already present in Box, added during
    // previous Box transaction in nested referenced inventory
    selectedStorageDetailsJS = new JSONArray();
    selectedStorageDetailsJS.put(firstProductSD.get(0));
    validateRIAfterBoxTransaction(refInvNested, selectedStorageDetailsJS, toBinId, 1, 2L);

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

  /**
   * Creates a referenced inventory with referenced inventory type having contentRestriction
   */

  private ReferencedInventory createRI(String contentRestriction) {
    final ReferencedInventoryType refInvType = ReferencedInventoryTestUtils
        .createReferencedInventoryType(OBDal.getInstance().getProxy(Organization.class, "0"),
            SequenceType.NONE, null, contentRestriction);

    return ReferencedInventoryTestUtils
        .createReferencedInventory(ReferencedInventoryTestUtils.QA_SPAIN_ORG_ID, refInvType);

  }

  @Override
  @After
  public void clearSession() {
    OBDal.getInstance().rollbackAndClose();
  }
}
