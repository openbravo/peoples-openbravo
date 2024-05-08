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

import java.math.BigDecimal;

import org.codehaus.jettison.json.JSONArray;
import org.junit.After;
import org.junit.Test;
import org.openbravo.dal.service.OBDal;
import org.openbravo.materialmgmt.refinventory.ContentRestriction;
import org.openbravo.model.common.plm.Product;
import org.openbravo.model.materialmgmt.onhandquantity.ReferencedInventory;

/**
 * This is class to test Nested Referenced Inventory Functionalities.
 *
 */

public class NestedReferencedInventoryBoxTest extends ReferencedInventoryTest {

  @Test
  public void testIndividualBox() throws Exception {
    executeIndividualBoxTest();
  }

  @Test
  public void testAddProductInIndividualBox() throws Exception {
    executeAddProductInIndividualBoxTest();
  }

  @Test
  public void testNestedReferencedInventory() throws Exception {
    executeNestedReferencedInventoryTest();
  }

  @Test
  public void testAddProductInNestedReferencedInventory() throws Exception {
    executedAddProductInNestedReferencedInventoryTest();
  }

  @Test
  public void testAddProductAndNestedReferencedInventoryAtSameTime() throws Exception {
    executeAddProductAndNestedReferencedInventoryAtSameTimeTest();
  }

  /**
   * Create Individual Box referenced inventory
   */

  private ReferencedInventory executeIndividualBoxTest() throws Exception {
    final String toBinId = BINS[0];

    final ReferencedInventory refInv = NestedReferencedInventoryTestUtils.createReferencedInventory(
        ReferencedInventoryTestUtils.QA_SPAIN_ORG_ID,
        ContentRestriction.BOTH_ITEMS_OR_REFINVENTORIES);

    JSONArray selectedStorageDetailsJS = new JSONArray();

    final Product firstProduct = ReferencedInventoryTestUtils.cloneProduct(PRODUCTS[0][0]);
    selectedStorageDetailsJS.put(NestedReferencedInventoryTestUtils
        .addProductInBox(firstProduct, BigDecimal.TEN, PRODUCTS[0][1])
        .get(0));

    final Product secondProduct = ReferencedInventoryTestUtils.cloneProduct(PRODUCTS[1][0]);
    selectedStorageDetailsJS.put(NestedReferencedInventoryTestUtils
        .addProductInBox(secondProduct, BigDecimal.TEN, PRODUCTS[1][1])
        .get(0));

    NestedReferencedInventoryTestUtils.validateRIAfterBoxTransaction(refInv,
        selectedStorageDetailsJS, toBinId, 2, 2L, 0L);

    NestedReferencedInventoryTestUtils.validateAttributeSetInstanceValue(refInv, firstProduct,
        BigDecimal.ONE, "[" + refInv.getSearchKey() + "]");
    NestedReferencedInventoryTestUtils.validateAttributeSetInstanceValue(refInv, secondProduct,
        BigDecimal.ONE, "Yellow[" + refInv.getSearchKey() + "]");

    return refInv;
  }

  /**
   * Add product in existing referenced inventory
   */

  private void executeAddProductInIndividualBoxTest() throws Exception {

    final String toBinId = BINS[0];

    final ReferencedInventory refInv = NestedReferencedInventoryTestUtils.createReferencedInventory(
        ReferencedInventoryTestUtils.QA_SPAIN_ORG_ID,
        ContentRestriction.BOTH_ITEMS_OR_REFINVENTORIES);

    JSONArray selectedStorageDetailsJS = new JSONArray();

    // Store first Product Storage Detail without Attribute set instance to be added later on
    final Product firstProduct = ReferencedInventoryTestUtils.cloneProduct(PRODUCTS[0][0]);
    JSONArray firstProductSD = new JSONArray();
    firstProductSD.put(NestedReferencedInventoryTestUtils
        .addProductInBox(firstProduct, BigDecimal.TEN, PRODUCTS[0][1])
        .get(0));
    selectedStorageDetailsJS.put(firstProductSD.get(0));

    final Product secondProduct = ReferencedInventoryTestUtils.cloneProduct(PRODUCTS[1][0]);
    selectedStorageDetailsJS.put(NestedReferencedInventoryTestUtils
        .addProductInBox(secondProduct, BigDecimal.TEN, PRODUCTS[1][1])
        .get(0));

    NestedReferencedInventoryTestUtils.validateRIAfterBoxTransaction(refInv,
        selectedStorageDetailsJS, toBinId, 2, 2L, 0L);

    NestedReferencedInventoryTestUtils.validateAttributeSetInstanceValue(refInv, firstProduct,
        BigDecimal.ONE, "[" + refInv.getSearchKey() + "]");
    NestedReferencedInventoryTestUtils.validateAttributeSetInstanceValue(refInv, secondProduct,
        BigDecimal.ONE, "Yellow[" + refInv.getSearchKey() + "]");

    // Add product in without attribute set instance which is already present in Box, added during
    // previous Box transaction in the referenced inventory
    selectedStorageDetailsJS = new JSONArray();
    selectedStorageDetailsJS.put(firstProductSD.get(0));
    NestedReferencedInventoryTestUtils.validateRIAfterBoxTransaction(refInv,
        selectedStorageDetailsJS, toBinId, 1, 2L, 0L);

    NestedReferencedInventoryTestUtils.validateAttributeSetInstanceValue(refInv, firstProduct,
        new BigDecimal(2), "[" + refInv.getSearchKey() + "]");

  }

  /**
   * create nested referenced inventory
   */

  private void executeNestedReferencedInventoryTest() throws Exception {

    final String toBinId = BINS[0];
    final ReferencedInventory refInv = executeIndividualBoxTest();
    // Create a nested Referenced Inventory
    final ReferencedInventory refInvNested = ReferencedInventoryTestUtils.createReferencedInventory(
        ReferencedInventoryTestUtils.QA_SPAIN_ORG_ID, refInv.getReferencedInventoryType());

    final JSONArray storageDetailsForNestedRI = NestedReferencedInventoryTestUtils
        .getStorageDetailsforNestedRI(refInv, toBinId);
    NestedReferencedInventoryTestUtils.validateRIAfterBoxTransaction(refInvNested,
        storageDetailsForNestedRI, toBinId, 2, 2L, 1L);

    NestedReferencedInventoryTestUtils.validateAttributeSetInstanceValue(refInv, null, null,
        "Yellow[" + refInv.getSearchKey() + "]" + "[" + refInvNested.getSearchKey() + "]");
    NestedReferencedInventoryTestUtils.validateAttributeSetInstanceValue(refInv, null, null,
        "[" + refInv.getSearchKey() + "]" + "[" + refInvNested.getSearchKey() + "]");
  }

  /**
   * Adds product in existing nested referenced inventory
   */

  private void executedAddProductInNestedReferencedInventoryTest() throws Exception {

    final String toBinId = BINS[0];

    JSONArray selectedStorageDetailsJS = new JSONArray();

    final ReferencedInventory refInv = NestedReferencedInventoryTestUtils.createReferencedInventory(
        ReferencedInventoryTestUtils.QA_SPAIN_ORG_ID,
        ContentRestriction.BOTH_ITEMS_OR_REFINVENTORIES);

    // Store first Product Storage Detail without Attribute set instance to be added later on
    final Product firstProduct = ReferencedInventoryTestUtils.cloneProduct(PRODUCTS[0][0]);
    JSONArray firstProductSD = new JSONArray();
    firstProductSD.put(NestedReferencedInventoryTestUtils
        .addProductInBox(firstProduct, BigDecimal.TEN, PRODUCTS[0][1])
        .get(0));
    selectedStorageDetailsJS.put(firstProductSD.get(0));

    final Product secondProduct = ReferencedInventoryTestUtils.cloneProduct(PRODUCTS[1][0]);
    selectedStorageDetailsJS.put(NestedReferencedInventoryTestUtils
        .addProductInBox(secondProduct, BigDecimal.TEN, PRODUCTS[1][1])
        .get(0));

    NestedReferencedInventoryTestUtils.validateRIAfterBoxTransaction(refInv,
        selectedStorageDetailsJS, toBinId, 2, 2L, 0L);

    // create nested referenced inventory

    final ReferencedInventory refInvNested = NestedReferencedInventoryTestUtils
        .createReferencedInventory(ReferencedInventoryTestUtils.QA_SPAIN_ORG_ID,
            ContentRestriction.BOTH_ITEMS_OR_REFINVENTORIES);

    final JSONArray storageDetailsForNestedRI = NestedReferencedInventoryTestUtils
        .getStorageDetailsforNestedRI(refInv, toBinId);

    NestedReferencedInventoryTestUtils.validateRIAfterBoxTransaction(refInvNested,
        storageDetailsForNestedRI, toBinId, 2, 2L, 1L);

    NestedReferencedInventoryTestUtils.validateAttributeSetInstanceValue(refInv, firstProduct,
        BigDecimal.ONE, "[" + refInv.getSearchKey() + "][" + refInvNested.getSearchKey() + "]");

    NestedReferencedInventoryTestUtils.validateAttributeSetInstanceValue(refInv, secondProduct,
        BigDecimal.ONE,
        "Yellow[" + refInv.getSearchKey() + "][" + refInvNested.getSearchKey() + "]");

    // Add product without attribute set instance which is already present in Box, added during
    // previous Box transaction in nested referenced inventory
    selectedStorageDetailsJS = new JSONArray();
    selectedStorageDetailsJS.put(firstProductSD.get(0));
    NestedReferencedInventoryTestUtils.validateRIAfterBoxTransaction(refInvNested,
        selectedStorageDetailsJS, toBinId, 1, 2L, 1L);

    NestedReferencedInventoryTestUtils.validateAttributeSetInstanceValue(refInvNested, firstProduct,
        BigDecimal.ONE, "[" + refInvNested.getSearchKey() + "]");

  }

  /**
   * Add Product & Nested Referenced Inventory at the same time
   */

  private void executeAddProductAndNestedReferencedInventoryAtSameTimeTest() throws Exception {

    final String toBinId = BINS[0];

    JSONArray storageDetailsFromSmallBox = new JSONArray();

    final ReferencedInventory smallBox = NestedReferencedInventoryTestUtils
        .createReferencedInventory(ReferencedInventoryTestUtils.QA_SPAIN_ORG_ID,
            ContentRestriction.BOTH_ITEMS_OR_REFINVENTORIES);

    // Store first Product Storage Detail without Attribute set instance to be added later on
    final Product firstProduct = ReferencedInventoryTestUtils.cloneProduct(PRODUCTS[0][0]);
    JSONArray firstProductSD = new JSONArray();
    firstProductSD.put(NestedReferencedInventoryTestUtils
        .addProductInBox(firstProduct, BigDecimal.TEN, PRODUCTS[0][1])
        .get(0));
    storageDetailsFromSmallBox.put(firstProductSD.get(0));

    final Product secondProduct = ReferencedInventoryTestUtils.cloneProduct(PRODUCTS[1][0]);
    JSONArray secondProductSD = new JSONArray();
    secondProductSD.put(NestedReferencedInventoryTestUtils
        .addProductInBox(secondProduct, BigDecimal.TEN, PRODUCTS[1][1])
        .get(0));
    storageDetailsFromSmallBox.put(secondProductSD.get(0));

    NestedReferencedInventoryTestUtils.validateRIAfterBoxTransaction(smallBox,
        storageDetailsFromSmallBox, toBinId, 2, 2L, 0L);

    // Box Transaction: small box inside medium box

    final ReferencedInventory mediumBox = NestedReferencedInventoryTestUtils
        .createReferencedInventory(ReferencedInventoryTestUtils.QA_SPAIN_ORG_ID,
            ContentRestriction.BOTH_ITEMS_OR_REFINVENTORIES);

    JSONArray storageDetailsForMediumBox = NestedReferencedInventoryTestUtils
        .getStorageDetailsforNestedRI(smallBox, toBinId);

    final Product thirdProduct = ReferencedInventoryTestUtils.cloneProduct(PRODUCTS[2][0]);
    JSONArray thirdProductSD = new JSONArray();
    thirdProductSD.put(NestedReferencedInventoryTestUtils
        .addProductInBox(thirdProduct, BigDecimal.TEN, PRODUCTS[2][1])
        .get(0));
    storageDetailsForMediumBox.put(thirdProductSD.get(0));

    NestedReferencedInventoryTestUtils.validateRIAfterBoxTransaction(mediumBox,
        storageDetailsForMediumBox, toBinId, 3, 3L, 1L);

    // Box Transaction - Medium Box inside Pallet

    final ReferencedInventory pallet = NestedReferencedInventoryTestUtils.createReferencedInventory(
        ReferencedInventoryTestUtils.QA_SPAIN_ORG_ID,
        ContentRestriction.BOTH_ITEMS_OR_REFINVENTORIES);

    // Add products with and without attribute set instance which is already present in Box, added
    // during previous Box transaction in nested referenced inventory

    final JSONArray storageDetailsForPallet = NestedReferencedInventoryTestUtils
        .getStorageDetailsforNestedRI(mediumBox, toBinId);
    storageDetailsForPallet.put(firstProductSD.get(0));
    storageDetailsForPallet.put(secondProductSD.get(0));
    NestedReferencedInventoryTestUtils.validateRIAfterBoxTransaction(pallet,
        storageDetailsForPallet, toBinId, 5, 3L, 2L);

    NestedReferencedInventoryTestUtils.validateAttributeSetInstanceValue(pallet, firstProduct,
        BigDecimal.ONE, "[" + pallet.getSearchKey() + "]");

    NestedReferencedInventoryTestUtils.validateAttributeSetInstanceValue(pallet, secondProduct,
        BigDecimal.ONE, "Yellow[" + pallet.getSearchKey() + "]");

    NestedReferencedInventoryTestUtils.validateAttributeSetInstanceValue(mediumBox, thirdProduct,
        BigDecimal.ONE, "#015[" + mediumBox.getSearchKey() + "][" + pallet.getSearchKey() + "]");

    NestedReferencedInventoryTestUtils.validateAttributeSetInstanceValue(smallBox, firstProduct,
        BigDecimal.ONE, "[" + smallBox.getSearchKey() + "][" + mediumBox.getSearchKey() + "]["
            + pallet.getSearchKey() + "]");

    NestedReferencedInventoryTestUtils.validateAttributeSetInstanceValue(smallBox, secondProduct,
        BigDecimal.ONE, "Yellow[" + smallBox.getSearchKey() + "][" + mediumBox.getSearchKey() + "]["
            + pallet.getSearchKey() + "]");

  }

  @Override
  @After
  public void clearSession() {
    OBDal.getInstance().rollbackAndClose();
  }
}
