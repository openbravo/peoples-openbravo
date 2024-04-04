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
import org.hibernate.criterion.Restrictions;
import org.junit.After;
import org.junit.Test;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.common.plm.AttributeSetInstance;
import org.openbravo.model.common.plm.Product;
import org.openbravo.model.materialmgmt.onhandquantity.ReferencedInventory;
import org.openbravo.model.materialmgmt.onhandquantity.StorageDetail;

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
    executedNestedReferencedInventoryTest();
  }

  @Test
  public void testAddProductInNestedReferencedInventory() throws Exception {
    executedAddProductInNestedReferencedInventoryTest();
  }

  @Test
  public void testAddProductAndNestedReferencedInventoryAtSameTime() throws Exception {
    executeAddProductAndNestedReferencedInventoryAtSameTimeTest();
  }

  @Test
  public void testNestedBox3LevelAddProduct() throws Exception {
    createNestedReferencedInventory3LevelAddProduct();
  }

  /**
   * Create Individual Box referenced inventory
   */

  private ReferencedInventory executeIndividualBoxTest() throws Exception {
    final String toBinId = BINS[0];

    final ReferencedInventory refInv = NestedReferencedInventoryTestUtils
        .createRI(ReferencedInventoryTestUtils.CONTENTRESTRICTION_BOTH_ITEMS_OR_HU);

    JSONArray selectedStorageDetailsJS = new JSONArray();

    final Product firstProduct = ReferencedInventoryTestUtils.cloneProduct(PRODUCTS[0][0]);
    selectedStorageDetailsJS.put(
        NestedReferencedInventoryTestUtils.addProductInBox(firstProduct, PRODUCTS[0][1]).get(0));

    final Product secondProduct = ReferencedInventoryTestUtils.cloneProduct(PRODUCTS[1][0]);
    selectedStorageDetailsJS.put(
        NestedReferencedInventoryTestUtils.addProductInBox(secondProduct, PRODUCTS[1][1]).get(0));

    NestedReferencedInventoryTestUtils.validateRIAfterBoxTransaction(refInv,
        selectedStorageDetailsJS, toBinId, 2, 2L, 0L);

    validateAttributeSetInstanceValue(refInv, firstProduct, BigDecimal.ONE,
        "[" + refInv.getSearchKey() + "]");
    validateAttributeSetInstanceValue(refInv, secondProduct, BigDecimal.ONE,
        "Yellow[" + refInv.getSearchKey() + "]");

    return refInv;
  }

  /**
   * Validate product and attribute set instance value in the referenced inventory
   */

  private void validateAttributeSetInstanceValue(final ReferencedInventory refInv,
      final Product product, final BigDecimal qtyOnHand,
      final String attributeSetInstanceDescription) {
    assertThat("Product with Attribute Set Instance does not exists in Referenced Inventory",
        storageDetailExists(refInv, product, qtyOnHand, attributeSetInstanceDescription),
        equalTo(true));
  }

  /**
   * Check whether storage detail exists for product and attribute set instance value in the
   * referenced inventory
   */

  private boolean storageDetailExists(final ReferencedInventory refInv, final Product product,
      final BigDecimal qtyOnHand, final String attributeSetInstanceDescription) {
    OBCriteria<StorageDetail> crit = OBDal.getInstance().createCriteria(StorageDetail.class);
    crit.createAlias(StorageDetail.PROPERTY_ATTRIBUTESETVALUE, "att");
    crit.add(Restrictions.eq(StorageDetail.PROPERTY_REFERENCEDINVENTORY, refInv));
    if (product != null) {
      crit.add(Restrictions.eq(StorageDetail.PROPERTY_PRODUCT, product));
    }
    if (qtyOnHand != null) {
      crit.add(Restrictions.eq(StorageDetail.PROPERTY_QUANTITYONHAND, qtyOnHand));
    }
    crit.add(Restrictions.eq("att." + AttributeSetInstance.PROPERTY_DESCRIPTION,
        attributeSetInstanceDescription));
    return !crit.list().isEmpty();
  }

  /**
   * Add product in existing referenced inventory
   */

  private void executeAddProductInIndividualBoxTest() throws Exception {

    final String toBinId = BINS[0];

    final ReferencedInventory refInv = NestedReferencedInventoryTestUtils
        .createRI(ReferencedInventoryTestUtils.CONTENTRESTRICTION_BOTH_ITEMS_OR_HU);

    JSONArray selectedStorageDetailsJS = new JSONArray();

    // Store first Product Storage Detail without Attribute set instance to be added later on
    final Product firstProduct = ReferencedInventoryTestUtils.cloneProduct(PRODUCTS[0][0]);
    JSONArray firstProductSD = new JSONArray();
    firstProductSD.put(
        NestedReferencedInventoryTestUtils.addProductInBox(firstProduct, PRODUCTS[0][1]).get(0));
    selectedStorageDetailsJS.put(firstProductSD.get(0));

    final Product secondProduct = ReferencedInventoryTestUtils.cloneProduct(PRODUCTS[1][0]);
    selectedStorageDetailsJS.put(
        NestedReferencedInventoryTestUtils.addProductInBox(secondProduct, PRODUCTS[1][1]).get(0));

    NestedReferencedInventoryTestUtils.validateRIAfterBoxTransaction(refInv,
        selectedStorageDetailsJS, toBinId, 2, 2L, 0L);

    validateAttributeSetInstanceValue(refInv, firstProduct, BigDecimal.ONE,
        "[" + refInv.getSearchKey() + "]");
    validateAttributeSetInstanceValue(refInv, secondProduct, BigDecimal.ONE,
        "Yellow[" + refInv.getSearchKey() + "]");

    // Add product in without attribute set instance which is already present in Box, added during
    // previous Box transaction in the referenced inventory
    selectedStorageDetailsJS = new JSONArray();
    selectedStorageDetailsJS.put(firstProductSD.get(0));
    NestedReferencedInventoryTestUtils.validateRIAfterBoxTransaction(refInv,
        selectedStorageDetailsJS, toBinId, 1, 2L, 0L);

    validateAttributeSetInstanceValue(refInv, firstProduct, new BigDecimal(2),
        "[" + refInv.getSearchKey() + "]");

  }

  /**
   * create nested referenced inventory
   */

  private void executedNestedReferencedInventoryTest() throws Exception {

    final String toBinId = BINS[0];
    final ReferencedInventory refInv = executeIndividualBoxTest();
    // Create a nested Referenced Inventory
    final ReferencedInventory refInvNested = ReferencedInventoryTestUtils.createReferencedInventory(
        ReferencedInventoryTestUtils.QA_SPAIN_ORG_ID, refInv.getReferencedInventoryType());

    final JSONArray storageDetailsForNestedRI = NestedReferencedInventoryTestUtils
        .getStorageDetailsforNestedRI(refInv, toBinId);
    NestedReferencedInventoryTestUtils.validateRIAfterBoxTransaction(refInvNested,
        storageDetailsForNestedRI, toBinId, 2, 2L, 1L);

    validateAttributeSetInstanceValue(refInv, null, null,
        "Yellow[" + refInv.getSearchKey() + "]" + "[" + refInvNested.getSearchKey() + "]");
    validateAttributeSetInstanceValue(refInv, null, null,
        "[" + refInv.getSearchKey() + "]" + "[" + refInvNested.getSearchKey() + "]");
  }

  /**
   * Adds product in existing nested referenced inventory
   */

  private void executedAddProductInNestedReferencedInventoryTest() throws Exception {

    final String toBinId = BINS[0];

    JSONArray selectedStorageDetailsJS = new JSONArray();

    final ReferencedInventory refInv = NestedReferencedInventoryTestUtils
        .createRI(ReferencedInventoryTestUtils.CONTENTRESTRICTION_BOTH_ITEMS_OR_HU);

    // Store first Product Storage Detail without Attribute set instance to be added later on
    final Product firstProduct = ReferencedInventoryTestUtils.cloneProduct(PRODUCTS[0][0]);
    JSONArray firstProductSD = new JSONArray();
    firstProductSD.put(
        NestedReferencedInventoryTestUtils.addProductInBox(firstProduct, PRODUCTS[0][1]).get(0));
    selectedStorageDetailsJS.put(firstProductSD.get(0));

    final Product secondProduct = ReferencedInventoryTestUtils.cloneProduct(PRODUCTS[1][0]);
    selectedStorageDetailsJS.put(
        NestedReferencedInventoryTestUtils.addProductInBox(secondProduct, PRODUCTS[1][1]).get(0));

    NestedReferencedInventoryTestUtils.validateRIAfterBoxTransaction(refInv,
        selectedStorageDetailsJS, toBinId, 2, 2L, 0L);

    // create nested referenced inventory

    final ReferencedInventory refInvNested = NestedReferencedInventoryTestUtils
        .createRI(ReferencedInventoryTestUtils.CONTENTRESTRICTION_BOTH_ITEMS_OR_HU);

    final JSONArray storageDetailsForNestedRI = NestedReferencedInventoryTestUtils
        .getStorageDetailsforNestedRI(refInv, toBinId);

    NestedReferencedInventoryTestUtils.validateRIAfterBoxTransaction(refInvNested,
        storageDetailsForNestedRI, toBinId, 2, 2L, 1L);

    validateAttributeSetInstanceValue(refInv, firstProduct, BigDecimal.ONE,
        "[" + refInv.getSearchKey() + "]" + "[" + refInvNested.getSearchKey() + "]");

    validateAttributeSetInstanceValue(refInv, secondProduct, BigDecimal.ONE,
        "Yellow[" + refInv.getSearchKey() + "]" + "[" + refInvNested.getSearchKey() + "]");

    // Add product without attribute set instance which is already present in Box, added during
    // previous Box transaction in nested referenced inventory
    selectedStorageDetailsJS = new JSONArray();
    selectedStorageDetailsJS.put(firstProductSD.get(0));
    NestedReferencedInventoryTestUtils.validateRIAfterBoxTransaction(refInvNested,
        selectedStorageDetailsJS, toBinId, 1, 2L, 1L);

    validateAttributeSetInstanceValue(refInvNested, firstProduct, BigDecimal.ONE,
        "[" + refInvNested.getSearchKey() + "]");

  }

  /**
   * Add Product & Nested Referenced Inventory at the same time
   */

  private void executeAddProductAndNestedReferencedInventoryAtSameTimeTest() throws Exception {

    final String toBinId = BINS[0];

    JSONArray selectedStorageDetailsJS = new JSONArray();

    final ReferencedInventory refInv = NestedReferencedInventoryTestUtils
        .createRI(ReferencedInventoryTestUtils.CONTENTRESTRICTION_BOTH_ITEMS_OR_HU);

    // Store first Product Storage Detail without Attribute set instance to be added later on
    final Product firstProduct = ReferencedInventoryTestUtils.cloneProduct(PRODUCTS[0][0]);
    JSONArray firstProductSD = new JSONArray();
    firstProductSD.put(
        NestedReferencedInventoryTestUtils.addProductInBox(firstProduct, PRODUCTS[0][1]).get(0));
    selectedStorageDetailsJS.put(firstProductSD.get(0));

    final Product secondProduct = ReferencedInventoryTestUtils.cloneProduct(PRODUCTS[1][0]);
    JSONArray secondProductSD = new JSONArray();
    secondProductSD.put(
        NestedReferencedInventoryTestUtils.addProductInBox(secondProduct, PRODUCTS[1][1]).get(0));
    selectedStorageDetailsJS.put(secondProductSD.get(0));

    NestedReferencedInventoryTestUtils.validateRIAfterBoxTransaction(refInv,
        selectedStorageDetailsJS, toBinId, 2, 2L, 0L);

    // create nested referenced inventory

    final ReferencedInventory refInvNested = NestedReferencedInventoryTestUtils
        .createRI(ReferencedInventoryTestUtils.CONTENTRESTRICTION_BOTH_ITEMS_OR_HU);

    final JSONArray storageDetailsForNestedRI = NestedReferencedInventoryTestUtils
        .getStorageDetailsforNestedRI(refInv, toBinId);

    // Add products with without attribute set instance which is already present in Box, added
    // during
    // previous Box transaction in nested referenced inventory
    selectedStorageDetailsJS = new JSONArray();
    selectedStorageDetailsJS.put(firstProductSD.get(0));
    selectedStorageDetailsJS.put(secondProductSD.get(0));
    NestedReferencedInventoryTestUtils.validateRIAfterBoxTransaction(refInvNested,
        storageDetailsForNestedRI, toBinId, 2, 2L, 1L);

  }

  /**
   * create referenced inventory and do boxing transaction
   */

  private void createNestedReferencedInventory3LevelAddProduct() throws Exception {

    final String toBinId = BINS[0];

    JSONArray selectedStorageDetailsJS = new JSONArray();

    // Small Box

    final ReferencedInventory refInv = NestedReferencedInventoryTestUtils
        .createRI(ReferencedInventoryTestUtils.CONTENTRESTRICTION_BOTH_ITEMS_OR_HU);

    // Store first Product Storage Detail without Attribute set instance to be added later on
    final Product firstProduct = ReferencedInventoryTestUtils.cloneProduct(PRODUCTS[0][0]);
    JSONArray firstProductSD = new JSONArray();
    firstProductSD.put(
        NestedReferencedInventoryTestUtils.addProductInBox(firstProduct, PRODUCTS[0][1]).get(0));
    selectedStorageDetailsJS.put(firstProductSD.get(0));

    final Product secondProduct = ReferencedInventoryTestUtils.cloneProduct(PRODUCTS[1][0]);
    JSONArray secondProductSD = new JSONArray();
    secondProductSD.put(
        NestedReferencedInventoryTestUtils.addProductInBox(secondProduct, PRODUCTS[1][1]).get(0));
    selectedStorageDetailsJS.put(secondProductSD.get(0));

    NestedReferencedInventoryTestUtils.validateRIAfterBoxTransaction(refInv,
        selectedStorageDetailsJS, toBinId, 2, 2L, 0L);

    // Medium Box

    final ReferencedInventory refInvNestedLevel2 = NestedReferencedInventoryTestUtils
        .createRI(ReferencedInventoryTestUtils.CONTENTRESTRICTION_BOTH_ITEMS_OR_HU);

    final JSONArray storageDetailsForNestedRI = NestedReferencedInventoryTestUtils
        .getStorageDetailsforNestedRI(refInv, toBinId);

    final Product thirdProduct = ReferencedInventoryTestUtils.cloneProduct(PRODUCTS[2][0]);
    JSONArray thirdProductSD = new JSONArray();
    thirdProductSD.put(
        NestedReferencedInventoryTestUtils.addProductInBox(thirdProduct, PRODUCTS[2][1]).get(0));
    storageDetailsForNestedRI.put(thirdProductSD.get(0));

    NestedReferencedInventoryTestUtils.validateRIAfterBoxTransaction(refInvNestedLevel2,
        storageDetailsForNestedRI, toBinId, 3, 3L, 1L);

    // Big Box

    final ReferencedInventory refInvNestedLevel3 = NestedReferencedInventoryTestUtils
        .createRI(ReferencedInventoryTestUtils.CONTENTRESTRICTION_BOTH_ITEMS_OR_HU);

    // Add products with and without attribute set instance which is already present in Box, added
    // during previous Box transaction in nested referenced inventory

    final JSONArray storageDetailsForNestedRILevel3 = NestedReferencedInventoryTestUtils
        .getStorageDetailsforNestedRI(refInvNestedLevel2, toBinId);
    storageDetailsForNestedRILevel3.put(firstProductSD.get(0));
    storageDetailsForNestedRILevel3.put(secondProductSD.get(0));
    NestedReferencedInventoryTestUtils.validateRIAfterBoxTransaction(refInvNestedLevel3,
        storageDetailsForNestedRILevel3, toBinId, 5, 3L, 2L);
  }

  @Override
  @After
  public void clearSession() {
    OBDal.getInstance().rollbackAndClose();
  }
}
