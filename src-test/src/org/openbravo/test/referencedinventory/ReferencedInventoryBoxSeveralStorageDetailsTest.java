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
 * All portions are Copyright (C) 2018-2024 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.test.referencedinventory;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.math.BigDecimal;

import org.codehaus.jettison.json.JSONArray;
import org.junit.Test;
import org.openbravo.dal.service.OBDal;
import org.openbravo.materialmgmt.refinventory.BoxProcessor;
import org.openbravo.model.common.plm.Product;
import org.openbravo.model.materialmgmt.onhandquantity.ReferencedInventory;
import org.openbravo.model.materialmgmt.onhandquantity.StorageDetail;
import org.openbravo.model.materialmgmt.transaction.InternalMovement;

/**
 * Tests related to boxing several different storage details into the same referenced inventory
 * without reservations
 */
public class ReferencedInventoryBoxSeveralStorageDetailsTest extends ReferencedInventoryBoxTest {

  /** testBox with Referenced Inventory having Referenced Inventory Type of Sequence Type - None */
  protected ReferencedInventory testBox(final String _toBinId, final String productId,
      final String attributeSetInstanceId) throws Exception {
    return testBox(_toBinId, productId, attributeSetInstanceId, new BigDecimal("10"), null, false,
        false, false);
  }

  /**
   * testBox with Referenced Inventory having Referenced Inventory Type of Sequence Type - Global
   */
  protected ReferencedInventory testBox_a(final String _toBinId, final String productId,
      final String attributeSetInstanceId) throws Exception {
    return testBox_a(_toBinId, productId, attributeSetInstanceId, new BigDecimal("10"), null, false,
        false, false);
  }

  /**
   * testBox with Referenced Inventory having Referenced Inventory Type of Sequence Type - Per
   * Organization
   */
  protected ReferencedInventory testBox_b(final String _toBinId, final String productId,
      final String attributeSetInstanceId) throws Exception {
    return testBox_b(_toBinId, productId, attributeSetInstanceId, new BigDecimal("10"), null, false,
        false, false);
  }

  @Test
  public void allTestsBoxSeveralStorageDetailsInSameMovement() throws Exception {
    for (String[] product : PRODUCTS) {
      for (String toBinId : BINS) {

        // Test with Referenced Inventory having Referenced Inventory Type of Sequence Type - None
        final ReferencedInventory refInv = ReferencedInventoryTestUtils.createReferencedInventory(
            ReferencedInventoryTestUtils.QA_SPAIN_ORG_ID,
            ReferencedInventoryTestUtils.createRefInvTypeSequenceTypeNone());
        assertBoxSeveralStorageDetailsInSameMovement(refInv, product[0], product[1], toBinId);

        // Test with Referenced Inventory having Referenced Inventory Type of Sequence Type - Global
        final ReferencedInventory refInv_a = ReferencedInventoryTestUtils.createReferencedInventory(
            ReferencedInventoryTestUtils.QA_SPAIN_ORG_ID,
            ReferencedInventoryTestUtils.createRefInvTypeSequenceTypeGlobal(SEQUENCE));
        assertBoxSeveralStorageDetailsInSameMovement(refInv_a, product[0], product[1], toBinId);

        // Test with Referenced Inventory having Referenced Inventory Type of Sequence Type - Per
        // Organization
        final ReferencedInventory refInv_b = ReferencedInventoryTestUtils.createReferencedInventory(
            ReferencedInventoryTestUtils.QA_SPAIN_ORG_ID,
            ReferencedInventoryTestUtils.createRefInvTypeSequenceTypePerOrganization(SEQUENCE));
        assertBoxSeveralStorageDetailsInSameMovement(refInv_b, product[0], product[1], toBinId);

        OBDal.getInstance().getSession().clear();
      }
    }
  }

  @Test
  public void allTestsBoxSeveralStorageDetailsInTwoMovements() throws Exception {
    for (String[] product1 : PRODUCTS) {
      for (String toBinId : BINS) {
        for (String[] product2 : PRODUCTS) {
          if (!product1[0].equals(product2[0])) {

            // Test with Referenced Inventory having Referenced Inventory Type of Sequence Type -
            // None
            final ReferencedInventory refInv = testBox(toBinId, product1[0], product1[1]);
            assertBoxSeveralStorageDetailsInTwoMovements(refInv, product2[0], product2[1], toBinId);

            // Test with Referenced Inventory having Referenced Inventory Type of Sequence Type -
            // Global
            final ReferencedInventory refInv_a = testBox_a(toBinId, product1[0], product1[1]);
            assertBoxSeveralStorageDetailsInTwoMovements(refInv_a, product2[0], product2[1],
                toBinId);

            // Test with Referenced Inventory having Referenced Inventory Type of Sequence Type -
            // Per
            // Organization
            final ReferencedInventory refInv_b = testBox_b(toBinId, product1[0], product1[1]);
            assertBoxSeveralStorageDetailsInTwoMovements(refInv_b, product2[0], product2[1],
                toBinId);

            OBDal.getInstance().getSession().clear();
          }
        }
      }
    }
  }

  private void assertBoxSeveralStorageDetailsInSameMovement(final ReferencedInventory refInv,
      final String productId, final String attributeSetInstanceId, final String toBinId)
      throws Exception {
    final Product product1 = ReferencedInventoryTestUtils.cloneProduct(productId);
    final BigDecimal receivedQty = new BigDecimal("10");
    ReferencedInventoryTestUtils.receiveProduct(product1, receivedQty, attributeSetInstanceId);
    final StorageDetail storageDetail1 = ReferencedInventoryTestUtils
        .getUniqueStorageDetail(product1);
    final JSONArray storageDetails1JS = ReferencedInventoryTestUtils
        .getStorageDetailsToBoxJSArray(storageDetail1, receivedQty);

    final Product product2 = ReferencedInventoryTestUtils.cloneProduct(productId);
    ReferencedInventoryTestUtils.receiveProduct(product2, receivedQty, attributeSetInstanceId);
    final StorageDetail storageDetail2 = ReferencedInventoryTestUtils
        .getUniqueStorageDetail(product2);
    final JSONArray storageDetails2JS = ReferencedInventoryTestUtils
        .getStorageDetailsToBoxJSArray(storageDetail2, receivedQty);

    final JSONArray selectedStorageDetailsJS = storageDetails1JS;
    selectedStorageDetailsJS.put(storageDetails2JS.get(0));

    final InternalMovement boxMovement = new BoxProcessor(refInv, selectedStorageDetailsJS, toBinId)
        .createAndProcessGoodsMovement();

    OBDal.getInstance().refresh(boxMovement);
    OBDal.getInstance().refresh(refInv);

    assertsGoodsMovementIsProcessed(boxMovement);
    assertsGoodsMovementNumberOfLines(boxMovement, 2);
    assertThat("Referenced Inventory has two different storage details",
        refInv.getMaterialMgmtStorageDetailList().size(), equalTo(2));
  }

  private void assertBoxSeveralStorageDetailsInTwoMovements(ReferencedInventory refInv,
      final String productId2, final String attributeSetInstanceId2, final String toBinId)
      throws Exception {
    final Product product2 = ReferencedInventoryTestUtils.cloneProduct(productId2);
    final BigDecimal receivedQty = new BigDecimal("10");
    ReferencedInventoryTestUtils.receiveProduct(product2, receivedQty, attributeSetInstanceId2);
    final StorageDetail storageDetail2 = ReferencedInventoryTestUtils
        .getUniqueStorageDetail(product2);
    final JSONArray storageDetails2JS = ReferencedInventoryTestUtils
        .getStorageDetailsToBoxJSArray(storageDetail2, receivedQty);

    final InternalMovement boxMovement = new BoxProcessor(refInv, storageDetails2JS, toBinId)
        .createAndProcessGoodsMovement();

    OBDal.getInstance().refresh(boxMovement);
    OBDal.getInstance().refresh(refInv);

    assertsGoodsMovementIsProcessed(boxMovement);
    assertsGoodsMovementNumberOfLines(boxMovement, 1);
    assertThat("Referenced Inventory has two different storage details",
        refInv.getMaterialMgmtStorageDetailList().size(), equalTo(2));
  }
}
