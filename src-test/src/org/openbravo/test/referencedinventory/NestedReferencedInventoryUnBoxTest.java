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

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.junit.After;
import org.junit.Test;
import org.openbravo.dal.service.OBDal;
import org.openbravo.materialmgmt.refinventory.UnboxProcessor;
import org.openbravo.model.common.plm.Product;
import org.openbravo.model.materialmgmt.onhandquantity.ReferencedInventory;
import org.openbravo.model.materialmgmt.transaction.InternalMovement;

/**
 * This is class to test Nested Referenced Inventory Box, UnBox Functionalities.
 *
 */

public class NestedReferencedInventoryUnBoxTest extends ReferencedInventoryTest {

  @Test
  public void testUnBoxNestedRI() throws Exception {

    final String toBinId = BINS[0];

    JSONArray storageDetailsForSmallBox = new JSONArray();

    // Small Box

    ReferencedInventory smallBoxRefInv = NestedReferencedInventoryTestUtils
        .createRI(ReferencedInventoryTestUtils.CONTENTRESTRICTION_BOTH_ITEMS_OR_HU);

    // Store first Product Storage Detail without Attribute set instance to be added later on
    final Product firstProduct = ReferencedInventoryTestUtils.cloneProduct(PRODUCTS[0][0]);
    JSONArray firstProductSD = new JSONArray();
    firstProductSD.put(
        NestedReferencedInventoryTestUtils.addProductInBox(firstProduct, PRODUCTS[0][1]).get(0));
    storageDetailsForSmallBox.put(firstProductSD.get(0));

    final Product secondProduct = ReferencedInventoryTestUtils.cloneProduct(PRODUCTS[1][0]);
    JSONArray secondProductSD = new JSONArray();
    secondProductSD.put(
        NestedReferencedInventoryTestUtils.addProductInBox(secondProduct, PRODUCTS[1][1]).get(0));
    storageDetailsForSmallBox.put(secondProductSD.get(0));

    NestedReferencedInventoryTestUtils.validateRIAfterBoxTransaction(smallBoxRefInv,
        storageDetailsForSmallBox, toBinId, 2, 2L, 0L);

    // Medium Box

    ReferencedInventory mediumBoxRefInv = NestedReferencedInventoryTestUtils
        .createRI(ReferencedInventoryTestUtils.CONTENTRESTRICTION_BOTH_ITEMS_OR_HU);

    final JSONArray storageDetailsForMediumBox = NestedReferencedInventoryTestUtils
        .getStorageDetailsforNestedRI(smallBoxRefInv, toBinId);

    final Product thirdProduct = ReferencedInventoryTestUtils.cloneProduct(PRODUCTS[2][0]);
    JSONArray thirdProductSD = new JSONArray();
    thirdProductSD.put(
        NestedReferencedInventoryTestUtils.addProductInBox(thirdProduct, PRODUCTS[2][1]).get(0));
    storageDetailsForMediumBox.put(thirdProductSD.get(0));

    NestedReferencedInventoryTestUtils.validateRIAfterBoxTransaction(mediumBoxRefInv,
        storageDetailsForMediumBox, toBinId, 3, 3L, 1L);

    // Pallet

    ReferencedInventory palletRefInv = NestedReferencedInventoryTestUtils
        .createRI(ReferencedInventoryTestUtils.CONTENTRESTRICTION_BOTH_ITEMS_OR_HU);

    // Add products with and without attribute set instance which is already present in Box, added
    // during previous Box transaction in nested referenced inventory

    JSONArray storageDetailsForPallet = NestedReferencedInventoryTestUtils
        .getStorageDetailsforNestedRI(mediumBoxRefInv, toBinId);
    storageDetailsForPallet.put(firstProductSD.get(0));
    storageDetailsForPallet.put(secondProductSD.get(0));
    NestedReferencedInventoryTestUtils.validateRIAfterBoxTransaction(palletRefInv,
        storageDetailsForPallet, toBinId, 5, 3L, 2L);

    // It is important to re-initialize referenced inventory objects when unboxing so that parent
    // referenced inventory is properly set
    mediumBoxRefInv = reInitializeRefInv(mediumBoxRefInv);
    smallBoxRefInv = reInitializeRefInv(smallBoxRefInv);

    // Unbox medium box inside pallet, unbox to individual items as No
    unBoxNestedRI(palletRefInv, mediumBoxRefInv, null, toBinId, false);

    // Re-Box
    final JSONArray storageDetailsForNestedRILevel3ReBox = NestedReferencedInventoryTestUtils
        .getStorageDetailsforNestedRI(mediumBoxRefInv, toBinId);
    NestedReferencedInventoryTestUtils.validateRIAfterBoxTransaction(palletRefInv,
        storageDetailsForNestedRILevel3ReBox, toBinId, 3, 3L, 2L);

    // Unbox medium box inside pallet, unbox to individual items as Yes
    unBoxNestedRI(palletRefInv, mediumBoxRefInv, null, toBinId, true);

    // Re-Box - Small Box
    // It is important to re-initialize referenced inventory objects when unboxing so that parent
    // referenced inventory is properly set
    smallBoxRefInv = reInitializeRefInv(smallBoxRefInv);

    storageDetailsForSmallBox = new JSONArray();
    storageDetailsForSmallBox.put(firstProductSD.get(0));
    storageDetailsForSmallBox.put(secondProductSD.get(0));
    NestedReferencedInventoryTestUtils.validateRIAfterBoxTransaction(smallBoxRefInv,
        storageDetailsForSmallBox, toBinId, 2, 2L, 0L);

    // Re-Box - Small Box into Pallet
    storageDetailsForPallet = NestedReferencedInventoryTestUtils
        .getStorageDetailsforNestedRI(smallBoxRefInv, toBinId);
    storageDetailsForPallet.put(thirdProductSD.get(0));
    NestedReferencedInventoryTestUtils.validateRIAfterBoxTransaction(palletRefInv,
        storageDetailsForPallet, toBinId, 3, 3L, null);

    // Box Pallet into Big Pallet
    ReferencedInventory bigPalletRefInv = NestedReferencedInventoryTestUtils
        .createRI(ReferencedInventoryTestUtils.CONTENTRESTRICTION_BOTH_ITEMS_OR_HU);
    JSONArray storageDetailsForBigPallet = NestedReferencedInventoryTestUtils
        .getStorageDetailsforNestedRI(palletRefInv, toBinId);
    NestedReferencedInventoryTestUtils.validateRIAfterBoxTransaction(bigPalletRefInv,
        storageDetailsForBigPallet, toBinId, 5, 3L, 2L);

    // Partial Unbox - Select any one item from small box, unbox to individual items
    // as Yes
    storageDetailsForSmallBox = NestedReferencedInventoryTestUtils
        .getStorageDetailsforNestedRI(smallBoxRefInv, toBinId);
    JSONArray storageDetailsFromSmallBoxToUnBox = new JSONArray();
    if (storageDetailsForSmallBox.length() > 0) {
      storageDetailsFromSmallBoxToUnBox.put(storageDetailsForSmallBox.get(0));
    }
    unBoxNestedRI(bigPalletRefInv, null, storageDetailsFromSmallBoxToUnBox, toBinId, true);

    // Total Unbox - Select all one item from Big Pallet, unbox to individual items
    // as Yes
    unBoxNestedRI(bigPalletRefInv, null,
        NestedReferencedInventoryTestUtils.getStorageDetailsforNestedRI(bigPalletRefInv, toBinId),
        toBinId, true);

  }

  @Test
  public void testUnBoxOuterMostParentRIWithoutUnBoxIndividualItems() throws Exception {

    final String toBinId = BINS[0];

    JSONArray storageDetailsForSmallBox = new JSONArray();

    // Small Box

    ReferencedInventory smallBoxRefInv = NestedReferencedInventoryTestUtils
        .createRI(ReferencedInventoryTestUtils.CONTENTRESTRICTION_BOTH_ITEMS_OR_HU);

    final Product smallBoxProduct = ReferencedInventoryTestUtils.cloneProduct(PRODUCTS[0][0]);
    JSONArray smallBoxProductSD = new JSONArray();
    smallBoxProductSD.put(
        NestedReferencedInventoryTestUtils.addProductInBox(smallBoxProduct, PRODUCTS[0][1]).get(0));
    storageDetailsForSmallBox.put(smallBoxProductSD.get(0));

    NestedReferencedInventoryTestUtils.validateRIAfterBoxTransaction(smallBoxRefInv,
        storageDetailsForSmallBox, toBinId, null, null, null);

    // Medium Box 1
    ReferencedInventory mediumBox1RefInv = NestedReferencedInventoryTestUtils
        .createRI(ReferencedInventoryTestUtils.CONTENTRESTRICTION_BOTH_ITEMS_OR_HU);

    final JSONArray storageDetailsForMediumBox1 = NestedReferencedInventoryTestUtils
        .getStorageDetailsforNestedRI(smallBoxRefInv, toBinId);

    final Product mediumBox1Product = ReferencedInventoryTestUtils.cloneProduct(PRODUCTS[1][0]);
    JSONArray mediumBox1ProductSD = new JSONArray();
    mediumBox1ProductSD
        .put(NestedReferencedInventoryTestUtils.addProductInBox(mediumBox1Product, PRODUCTS[1][1])
            .get(0));
    storageDetailsForMediumBox1.put(mediumBox1ProductSD.get(0));

    NestedReferencedInventoryTestUtils.validateRIAfterBoxTransaction(mediumBox1RefInv,
        storageDetailsForMediumBox1, toBinId, null, null, null);

    // Medium Box 2
    ReferencedInventory mediumBox2RefInv = NestedReferencedInventoryTestUtils
        .createRI(ReferencedInventoryTestUtils.CONTENTRESTRICTION_BOTH_ITEMS_OR_HU);

    final Product mediumBox2Product = ReferencedInventoryTestUtils.cloneProduct(PRODUCTS[2][0]);
    JSONArray mediumBox2ProductSD = new JSONArray();
    mediumBox2ProductSD
        .put(NestedReferencedInventoryTestUtils.addProductInBox(mediumBox2Product, PRODUCTS[2][1])
            .get(0));

    NestedReferencedInventoryTestUtils.validateRIAfterBoxTransaction(mediumBox2RefInv,
        mediumBox2ProductSD, toBinId, null, null, null);

    // Pallet - Medium Box 1 and Medium Box 2

    ReferencedInventory palletRefInv = NestedReferencedInventoryTestUtils
        .createRI(ReferencedInventoryTestUtils.CONTENTRESTRICTION_BOTH_ITEMS_OR_HU);

    JSONArray storageDetailsForPallet = NestedReferencedInventoryTestUtils
        .getStorageDetailsforNestedRI(mediumBox1RefInv, toBinId);

    JSONArray storageDetailsForPalletFromMediumBox2 = NestedReferencedInventoryTestUtils
        .getStorageDetailsforNestedRI(mediumBox2RefInv, toBinId);

    for (int i = 0; i < storageDetailsForPalletFromMediumBox2.length(); i++) {
      storageDetailsForPallet.put(storageDetailsForPalletFromMediumBox2.get(i));
    }

    storageDetailsForPallet.put(smallBoxProductSD.get(0));

    NestedReferencedInventoryTestUtils.validateRIAfterBoxTransaction(palletRefInv,
        storageDetailsForPallet, toBinId, null, null, null);

    // It is important to re-initialize referenced inventory objects when unboxing so that parent
    // referenced inventory is properly set
    palletRefInv = reInitializeRefInv(palletRefInv);

    // Unbox pallet, unbox to individual items as No
    unBoxNestedRI(palletRefInv, palletRefInv, null, toBinId, false);

  }

  /**
   * Re-initialize referenced inventory object to to avoid problems in Hibernate when the sunbox
   * process is executed
   */

  private ReferencedInventory reInitializeRefInv(final ReferencedInventory refInv) {
    OBDal.getInstance().getSession().evict(refInv); // Hack to avoid problems in Hibernate when the
                                                    // unbox process is executed
    return OBDal.getInstance().get(ReferencedInventory.class, refInv.getId());

  }

  /**
   * UnBox nested referenced inventory
   */
  private void unBoxNestedRI(final ReferencedInventory refInvToUnBox,
      final ReferencedInventory selectedRefInvToUnbox, final JSONArray storageDetailsForUnBox,
      String toBinId, boolean unBoxToIndividualItems) throws Exception {
    final JSONArray selectedRefInventoriesToUnbox = new JSONArray();
    final JSONObject refInventoryJSToUnbox = new JSONObject();
    if (selectedRefInvToUnbox != null) {
      refInventoryJSToUnbox.put("id", selectedRefInvToUnbox.getId());
      selectedRefInventoriesToUnbox.put(refInventoryJSToUnbox);
    }

    final JSONArray storageDetailsForUnBoxNestedRI = selectedRefInvToUnbox != null
        ? NestedReferencedInventoryTestUtils.getStorageDetailsforNestedRI(selectedRefInvToUnbox,
            toBinId)
        : new JSONArray();
    if (storageDetailsForUnBox != null && storageDetailsForUnBox.length() > 0) {
      for (int i = 0; i < storageDetailsForUnBox.length(); i++) {
        storageDetailsForUnBoxNestedRI.put(storageDetailsForUnBox.get(i));
      }
    }

    final InternalMovement unBoxMovement = new UnboxProcessor(refInvToUnBox,
        storageDetailsForUnBoxNestedRI, selectedRefInventoriesToUnbox, unBoxToIndividualItems)
            .createAndProcessGoodsMovement();
    OBDal.getInstance().refresh(unBoxMovement);
  }

  @Override
  @After
  public void clearSession() {
    OBDal.getInstance().rollbackAndClose();
  }
}
