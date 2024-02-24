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
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThrows;

import java.math.BigDecimal;
import java.util.List;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Test;
import org.openbravo.base.exception.OBException;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.materialmgmt.refinventory.BoxProcessor;
import org.openbravo.model.common.plm.Product;
import org.openbravo.model.materialmgmt.onhandquantity.ReferencedInventory;
import org.openbravo.model.materialmgmt.onhandquantity.StorageDetail;

/**
 * Tests exceptions are thrown
 */
public class ReferencedInventoryExceptionTest extends ReferencedInventoryBoxTest {

  /**
   * testBox with Referenced Inventory having Referenced Inventory Type of Sequence Type - None
   */
  protected ReferencedInventory testBox(final String _toBinId, final String productId,
      final String attributeSetInstanceId, final BigDecimal qtyInBox) throws Exception {
    return testBox(_toBinId, productId, attributeSetInstanceId, qtyInBox, null, false, false,
        false);
  }

  /**
   * testBox with Referenced Inventory having Referenced Inventory Type of Sequence Type - Global
   */
  protected ReferencedInventory testBox_a(final String _toBinId, final String productId,
      final String attributeSetInstanceId, final BigDecimal qtyInBox) throws Exception {
    return testBox_a(_toBinId, productId, attributeSetInstanceId, qtyInBox, null, false, false,
        false);
  }

  /**
   * testBox with Referenced Inventory having Referenced Inventory Type of Sequence Type - Per
   * Organization
   */
  protected ReferencedInventory testBox_b(final String _toBinId, final String productId,
      final String attributeSetInstanceId, final BigDecimal qtyInBox) throws Exception {
    return testBox_b(_toBinId, productId, attributeSetInstanceId, qtyInBox, null, false, false,
        false);
  }

  /**
   * test with Referenced Inventory having Referenced Inventory Type of Sequence Type - None
   */

  @Test
  public void testNoStorageDetailsProvidedToBox() throws Exception {

    final ReferencedInventory refInv = ReferencedInventoryTestUtils.createReferencedInventory(
        ReferencedInventoryTestUtils.QA_SPAIN_ORG_ID,
        ReferencedInventoryTestUtils.createRefInvTypeSequenceTypeNone());
    assertNoStorageDetailsProvidedToBox(refInv);
  }

  /**
   * test with Referenced Inventory having Referenced Inventory Type of Sequence Type - Global
   */
  @Test
  public void testNoStorageDetailsProvidedToBox_a() throws Exception {

    final ReferencedInventory refInv = ReferencedInventoryTestUtils.createReferencedInventory(
        ReferencedInventoryTestUtils.QA_SPAIN_ORG_ID,
        ReferencedInventoryTestUtils.createRefInvTypeSequenceTypeGlobal(SEQUENCE));
    assertNoStorageDetailsProvidedToBox(refInv);
  }

  /**
   * test with Referenced Inventory having Referenced Inventory Type of Sequence Type - Per
   * Organization
   */

  @Test
  public void testNoStorageDetailsProvidedToBox_b() throws Exception {
    final ReferencedInventory refInv = ReferencedInventoryTestUtils.createReferencedInventory(
        ReferencedInventoryTestUtils.QA_SPAIN_ORG_ID,
        ReferencedInventoryTestUtils.createRefInvTypeSequenceTypePerOrganization(SEQUENCE));
    assertNoStorageDetailsProvidedToBox(refInv);
  }

  private void assertNoStorageDetailsProvidedToBox(ReferencedInventory refInv) {
    OBException thrown = assertThrows(OBException.class, () -> {
      new BoxProcessor(refInv, null, null).createAndProcessGoodsMovement();
    });
    assertThat(thrown.getMessage(), containsString(OBMessageUtils.messageBD("NotSelected")));
  }

  /**
   * test with Referenced Inventory having Referenced Inventory Type of Sequence Type - None
   */

  @Test
  public void testNegativeStorageDetailProvidedToBox() throws Exception {
    final ReferencedInventory refInv = ReferencedInventoryTestUtils.createReferencedInventory(
        ReferencedInventoryTestUtils.QA_SPAIN_ORG_ID,
        ReferencedInventoryTestUtils.createRefInvTypeSequenceTypeNone());
    assertNegativeStorageDetailProvidedToBox(refInv);
  }

  /**
   * test with Referenced Inventory having Referenced Inventory Type of Sequence Type - Global
   */

  @Test
  public void testNegativeStorageDetailProvidedToBox_a() throws Exception {
    final ReferencedInventory refInv = ReferencedInventoryTestUtils.createReferencedInventory(
        ReferencedInventoryTestUtils.QA_SPAIN_ORG_ID,
        ReferencedInventoryTestUtils.createRefInvTypeSequenceTypeGlobal(SEQUENCE));
    assertNegativeStorageDetailProvidedToBox(refInv);
  }

  /**
   * test with Referenced Inventory having Referenced Inventory Type of Sequence Type - Per
   * Organization
   */
  @Test
  public void testNegativeStorageDetailProvidedToBox_b() throws Exception {
    final ReferencedInventory refInv = ReferencedInventoryTestUtils.createReferencedInventory(
        ReferencedInventoryTestUtils.QA_SPAIN_ORG_ID,
        ReferencedInventoryTestUtils.createRefInvTypeSequenceTypePerOrganization(SEQUENCE));
    assertNegativeStorageDetailProvidedToBox(refInv);
  }

  private void assertNegativeStorageDetailProvidedToBox(ReferencedInventory refInv)
      throws Exception {
    final JSONObject storageDetailJS = new JSONObject();
    storageDetailJS.put("id", ReferencedInventoryTestUtils.getAnyStorageDetail());
    storageDetailJS.put("quantityOnHand", BigDecimal.ONE.negate());
    final JSONArray storageDetailsJS = new JSONArray();
    storageDetailsJS.put(storageDetailJS);

    OBException thrown = assertThrows(OBException.class, () -> {
      new BoxProcessor(refInv, storageDetailsJS, null).createAndProcessGoodsMovement();
    });
    assertThat(thrown.getMessage(),
        containsString(String.format(OBMessageUtils.messageBD("RefInv_NegativeQty"), "")));
  }

  /**
   * test with Referenced Inventory having Referenced Inventory Type of Sequence Type - None
   */

  @Test
  public void testZeroQtyStorageDetailProvidedToBox() throws Exception {
    final ReferencedInventory refInv = ReferencedInventoryTestUtils.createReferencedInventory(
        ReferencedInventoryTestUtils.QA_SPAIN_ORG_ID,
        ReferencedInventoryTestUtils.createRefInvTypeSequenceTypeNone());
    assertZeroQtyStorageDetailProvidedToBox(refInv);
  }

  /**
   * test with Referenced Inventory having Referenced Inventory Type of Sequence Type - Global
   */

  @Test
  public void testZeroQtyStorageDetailProvidedToBox_a() throws Exception {
    final ReferencedInventory refInv = ReferencedInventoryTestUtils.createReferencedInventory(
        ReferencedInventoryTestUtils.QA_SPAIN_ORG_ID,
        ReferencedInventoryTestUtils.createRefInvTypeSequenceTypeGlobal(SEQUENCE));
    assertZeroQtyStorageDetailProvidedToBox(refInv);
  }

  /**
   * test with Referenced Inventory having Referenced Inventory Type of Sequence Type - Per
   * Organization
   */

  @Test
  public void testZeroQtyStorageDetailProvidedToBox_b() throws Exception {
    final ReferencedInventory refInv = ReferencedInventoryTestUtils.createReferencedInventory(
        ReferencedInventoryTestUtils.QA_SPAIN_ORG_ID,
        ReferencedInventoryTestUtils.createRefInvTypeSequenceTypePerOrganization(SEQUENCE));
    assertZeroQtyStorageDetailProvidedToBox(refInv);
  }

  private void assertZeroQtyStorageDetailProvidedToBox(ReferencedInventory refInv)
      throws Exception {
    final JSONObject storageDetailJS = new JSONObject();
    storageDetailJS.put("id", ReferencedInventoryTestUtils.getAnyStorageDetail());
    storageDetailJS.put("quantityOnHand", BigDecimal.ZERO);
    final JSONArray storageDetailsJS = new JSONArray();
    storageDetailsJS.put(storageDetailJS);

    OBException thrown = assertThrows(OBException.class, () -> {
      new BoxProcessor(refInv, storageDetailsJS, null).createAndProcessGoodsMovement();
    });
    assertThat(thrown.getMessage(),
        containsString(String.format(OBMessageUtils.messageBD("RefInv_NegativeQty"), "")));
  }

  /**
   * test with Referenced Inventory having Referenced Inventory Type of Sequence Type - None
   */
  @Test
  public void testBoxQtyGreaterThanQtyOnHand() throws Exception {
    final BigDecimal TWO_HUNDRED = new BigDecimal("200");

    OBException thrown = assertThrows(OBException.class, () -> {
      testBox(null, ReferencedInventoryTestUtils.PRODUCT_TSHIRT_ID, null, TWO_HUNDRED);
    });
    assertThat(thrown.getMessage(), containsString("(" + TWO_HUNDRED + ")"));
  }

  /**
   * test with Referenced Inventory having Referenced Inventory Type of Sequence Type - Global
   */
  @Test
  public void testBoxQtyGreaterThanQtyOnHand_a() throws Exception {
    final BigDecimal TWO_HUNDRED = new BigDecimal("200");

    OBException thrown = assertThrows(OBException.class, () -> {
      testBox_a(null, ReferencedInventoryTestUtils.PRODUCT_TSHIRT_ID, null, TWO_HUNDRED);
    });
    assertThat(thrown.getMessage(), containsString("(" + TWO_HUNDRED + ")"));
  }

  /**
   * test with Referenced Inventory having Referenced Inventory Type of Sequence Type - Per
   * Organization
   */

  @Test
  public void testBoxQtyGreaterThanQtyOnHand_b() throws Exception {
    final BigDecimal TWO_HUNDRED = new BigDecimal("200");

    OBException thrown = assertThrows(OBException.class, () -> {
      testBox_b(null, ReferencedInventoryTestUtils.PRODUCT_TSHIRT_ID, null, TWO_HUNDRED);
    });
    assertThat(thrown.getMessage(), containsString("(" + TWO_HUNDRED + ")"));
  }

  /**
   * test with Referenced Inventory having Referenced Inventory Type of Sequence Type - None
   */

  @Test
  public void testCascadeReferencedInventoryNotPossible() throws Exception {
    final ReferencedInventory refInv1 = testBox(null,
        ReferencedInventoryTestUtils.PRODUCT_TSHIRT_ID, null, null);
    assertCascadeReferencedInventoryNotPossible(refInv1);
  }

  /**
   * test with Referenced Inventory having Reference Inventory Type of Sequence Type - Global
   */
  @Test
  public void testCascadeReferencedInventoryNotPossible_a() throws Exception {
    final ReferencedInventory refInv1 = testBox_a(null,
        ReferencedInventoryTestUtils.PRODUCT_TSHIRT_ID, null, null);
    assertCascadeReferencedInventoryNotPossible(refInv1);
  }

  /**
   * test with Referenced Inventory having Referenced Inventory Type of Sequence Type - Per
   * Organization
   */
  @Test
  public void testCascadeReferencedInventoryNotPossible_b() throws Exception {
    final ReferencedInventory refInv1 = testBox_b(null,
        ReferencedInventoryTestUtils.PRODUCT_TSHIRT_ID, null, null);
    assertCascadeReferencedInventoryNotPossible(refInv1);
  }

  private void assertCascadeReferencedInventoryNotPossible(ReferencedInventory refInv1)
      throws Exception {
    final StorageDetail storageDetail = refInv1.getMaterialMgmtStorageDetailList().get(0);
    final ReferencedInventory refInv2 = ReferencedInventoryTestUtils.createReferencedInventory(
        ReferencedInventoryTestUtils.QA_SPAIN_ORG_ID, refInv1.getReferencedInventoryType());
    final JSONArray storageDetailsJS = ReferencedInventoryTestUtils
        .getStorageDetailsToBoxJSArray(storageDetail, BigDecimal.ONE);
    OBException thrown = assertThrows(OBException.class, () -> {
      new BoxProcessor(refInv2, storageDetailsJS, null).createAndProcessGoodsMovement();
    });
    assertThat(thrown.getMessage(), containsString(" is already linked to the handling unit "));
  }

  /**
   * test with Referenced Inventory having Referenced Inventory Type of Sequence Type - None
   */

  @Test
  public void testSameReferencedInventoryInDifferentBinsNotPossible() throws Exception {
    final ReferencedInventory refInv = testBox(ReferencedInventoryTestUtils.BIN_SPAIN_L02,
        ReferencedInventoryTestUtils.PRODUCT_TSHIRT_ID, null, BigDecimal.ONE);
    assertSameReferencedInventoryInDifferentBinsNotPossible(refInv);
  }

  /**
   * test with Referenced Inventory having Reference Inventory Type of Sequence Type - Global
   */
  @Test
  public void testSameReferencedInventoryInDifferentBinsNotPossible_a() throws Exception {
    final ReferencedInventory refInv = testBox_a(ReferencedInventoryTestUtils.BIN_SPAIN_L02,
        ReferencedInventoryTestUtils.PRODUCT_TSHIRT_ID, null, BigDecimal.ONE);
    assertSameReferencedInventoryInDifferentBinsNotPossible(refInv);
  }

  /**
   * test with Referenced Inventory having Referenced Inventory Type of Sequence Type - Per
   * Organization
   */
  @Test
  public void testSameReferencedInventoryInDifferentBinsNotPossible_b() throws Exception {
    final ReferencedInventory refInv = testBox_b(ReferencedInventoryTestUtils.BIN_SPAIN_L02,
        ReferencedInventoryTestUtils.PRODUCT_TSHIRT_ID, null, BigDecimal.ONE);
    assertSameReferencedInventoryInDifferentBinsNotPossible(refInv);
  }

  private void assertSameReferencedInventoryInDifferentBinsNotPossible(ReferencedInventory refInv)
      throws Exception {
    final List<StorageDetail> storageDetails = ReferencedInventoryTestUtils
        .getAvailableStorageDetailsOrderByQtyOnHand(
            refInv.getMaterialMgmtStorageDetailList().get(0).getProduct());
    final JSONArray storageDetailsJS = ReferencedInventoryTestUtils
        .getStorageDetailsToBoxJSArray(storageDetails.get(1), new BigDecimal("3"));

    OBException thrown = assertThrows(OBException.class, () -> {
      new BoxProcessor(refInv, storageDetailsJS, ReferencedInventoryTestUtils.BIN_SPAIN_L03)
          .createAndProcessGoodsMovement();
    });
    assertThat(thrown.getMessage(), containsString(" handling unit is also located in bin: "));
  }

  /**
   * test with Referenced Inventory having Referenced Inventory Type of Sequence Type - None
   */

  @Test
  public void testBoxMandatoryNewStorageBinParameter() throws Exception {
    final ReferencedInventory refInv = ReferencedInventoryTestUtils.createReferencedInventory(
        ReferencedInventoryTestUtils.QA_SPAIN_ORG_ID,
        ReferencedInventoryTestUtils.createRefInvTypeSequenceTypeNone());
    assertBoxMandatoryNewStorageBinParameter(refInv);
  }

  /**
   * test with Referenced Inventory having Referenced Inventory Type of Sequence Type - Global
   */
  @Test
  public void testBoxMandatoryNewStorageBinParameter_a() throws Exception {
    final ReferencedInventory refInv = ReferencedInventoryTestUtils.createReferencedInventory(
        ReferencedInventoryTestUtils.QA_SPAIN_ORG_ID,
        ReferencedInventoryTestUtils.createRefInvTypeSequenceTypeGlobal(SEQUENCE));
    assertBoxMandatoryNewStorageBinParameter(refInv);
  }

  /**
   * test with Referenced Inventory having Referenced Inventory Type of Sequence Type - Per
   * Organization
   */
  @Test
  public void testBoxMandatoryNewStorageBinParameter_b() throws Exception {
    final ReferencedInventory refInv = ReferencedInventoryTestUtils.createReferencedInventory(
        ReferencedInventoryTestUtils.QA_SPAIN_ORG_ID,
        ReferencedInventoryTestUtils.createRefInvTypeSequenceTypePerOrganization(SEQUENCE));
    assertBoxMandatoryNewStorageBinParameter(refInv);
  }

  private void assertBoxMandatoryNewStorageBinParameter(ReferencedInventory refInv) {
    final Product product = ReferencedInventoryTestUtils
        .cloneProduct(ReferencedInventoryTestUtils.PRODUCT_TSHIRT_ID);
    final BigDecimal receivedQty = new BigDecimal("10");
    ReferencedInventoryTestUtils.receiveProduct(product, receivedQty, null);

    final StorageDetail storageDetail = ReferencedInventoryTestUtils
        .getUniqueStorageDetail(product);

    OBException thrown = assertThrows(OBException.class, () -> {
      new BoxProcessor(refInv,
          ReferencedInventoryTestUtils.getStorageDetailsToBoxJSArray(storageDetail, BigDecimal.ONE),
          null).createAndProcessGoodsMovement();
    });
    assertThat(thrown.getMessage(),
        containsString(OBMessageUtils.messageBD("NewStorageBinParameterMandatory")));
  }
}
