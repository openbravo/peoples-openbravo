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
 * This is class to test Nested Referenced Inventory UnBox Functionalities.
 *
 */

public class NestedReferencedInventoryUnBoxTest extends ReferencedInventoryTest {

  @Test
  public void testUnBoxNestedRI3Level() throws Exception {
    createNestedReferencedInventory3LevelForUnBox();
  }

  /**
   * create referenced inventory and do boxing transaction
   */

  private void createNestedReferencedInventory3LevelForUnBox() throws Exception {

    final String toBinId = BINS[0];

    JSONArray selectedStorageDetailsJS = new JSONArray();

    // Small Box

    ReferencedInventory refInv = NestedReferencedInventoryTestUtils
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

    ReferencedInventory refInvNestedLevel2 = NestedReferencedInventoryTestUtils
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

    ReferencedInventory refInvNestedLevel3 = NestedReferencedInventoryTestUtils
        .createRI(ReferencedInventoryTestUtils.CONTENTRESTRICTION_BOTH_ITEMS_OR_HU);

    // Add products with and without attribute set instance which is already present in Box, added
    // during previous Box transaction in nested referenced inventory

    final JSONArray storageDetailsForNestedRILevel3 = NestedReferencedInventoryTestUtils
        .getStorageDetailsforNestedRI(refInvNestedLevel2, toBinId);
    storageDetailsForNestedRILevel3.put(firstProductSD.get(0));
    storageDetailsForNestedRILevel3.put(secondProductSD.get(0));
    NestedReferencedInventoryTestUtils.validateRIAfterBoxTransaction(refInvNestedLevel3,
        storageDetailsForNestedRILevel3, toBinId, 5, 3L, 2L);

    OBDal.getInstance().getSession().evict(refInvNestedLevel2); // Hack to avoid problems in
                                                                // Hibernate when the unbox process
                                                                // is executed
    refInvNestedLevel2 = OBDal.getInstance()
        .get(ReferencedInventory.class, refInvNestedLevel2.getId());

    OBDal.getInstance().getSession().evict(refInv); // Hack to avoid problems in Hibernate when the
                                                    // unbox process is executed
    refInv = OBDal.getInstance().get(ReferencedInventory.class, refInv.getId());

    // Unbox

    final JSONArray selectedRefInventoriesToUnbox = new JSONArray();
    final JSONObject refInventoryJSToUnbox = new JSONObject();
    refInventoryJSToUnbox.put("id", refInvNestedLevel2.getId());
    selectedRefInventoriesToUnbox.put(refInventoryJSToUnbox);

    final JSONArray storageDetailsForUnBoxNestedRI = NestedReferencedInventoryTestUtils
        .getStorageDetailsforNestedRI(refInvNestedLevel2, toBinId);

    final InternalMovement unBoxMovement = new UnboxProcessor(refInvNestedLevel3,
        storageDetailsForUnBoxNestedRI, selectedRefInventoriesToUnbox, false)
            .createAndProcessGoodsMovement();
    OBDal.getInstance().refresh(unBoxMovement);

    // Re-Box

    final JSONArray storageDetailsForNestedRILevel3ReBox = NestedReferencedInventoryTestUtils
        .getStorageDetailsforNestedRI(refInvNestedLevel2, toBinId);
    NestedReferencedInventoryTestUtils.validateRIAfterBoxTransaction(refInvNestedLevel3,
        storageDetailsForNestedRILevel3ReBox, toBinId, 3, 3L, 2L);

  }

  @Override
  @After
  public void clearSession() {
    OBDal.getInstance().rollbackAndClose();
  }
}
