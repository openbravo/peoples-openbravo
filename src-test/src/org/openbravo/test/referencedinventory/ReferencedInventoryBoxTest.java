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
 * All portions are Copyright (C) 2018 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.test.referencedinventory;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Test;
import org.openbravo.base.exception.OBException;
import org.openbravo.dal.core.DalUtil;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.materialmgmt.refinventory.BoxProcessor;
import org.openbravo.model.ad.utility.Sequence;
import org.openbravo.model.common.plm.Product;
import org.openbravo.model.materialmgmt.onhandquantity.ReferencedInventory;
import org.openbravo.model.materialmgmt.onhandquantity.ReferencedInventoryType;
import org.openbravo.model.materialmgmt.onhandquantity.StorageDetail;
import org.openbravo.model.materialmgmt.transaction.InternalMovement;

/**
 * Tests related to boxing into referenced inventory
 */
public class ReferencedInventoryBoxTest extends ReferencedInventoryTest {

  @Test
  public void testBoxWithoutAttributeInTheSameBinFullBox() throws Exception {
    testBox(null, ReferencedInventoryTestUtils.PRODUCT_TSHIRT_ID, null, null);
  }

  @Test
  public void testBoxWithoutAttributeInDifferentBinFullBox() throws Exception {
    testBox(ReferencedInventoryTestUtils.BIN_SPAIN_L02,
        ReferencedInventoryTestUtils.PRODUCT_TSHIRT_ID, null, null);
  }

  @Test
  public void testBoxWithAttributeInTheSameBinFullBox() throws Exception {
    testBox(null, ReferencedInventoryTestUtils.PRODUCT_BALL_COLORATTRIBUTE,
        ReferencedInventoryTestUtils.ATTRIBUTE_COLOR_YELLOW, null);
  }

  @Test
  public void testBoxWithAttributeInDifferentBinFullBox() throws Exception {
    testBox(ReferencedInventoryTestUtils.BIN_SPAIN_L02,
        ReferencedInventoryTestUtils.PRODUCT_BALL_COLORATTRIBUTE,
        ReferencedInventoryTestUtils.ATTRIBUTE_COLOR_YELLOW, null);
  }

  @Test
  public void testBoxWithSerialNoInTheSameBinFullBox() throws Exception {
    testBox(null, ReferencedInventoryTestUtils.PRODUCT_LAPTOP_SERIALATTRIBUTE,
        ReferencedInventoryTestUtils.ATTRIBUTE_LAPTOP_SERIALNO, null);
  }

  @Test
  public void testBoxWithSerialNoInDifferentBinFullBox() throws Exception {
    testBox(ReferencedInventoryTestUtils.BIN_SPAIN_L02,
        ReferencedInventoryTestUtils.PRODUCT_LAPTOP_SERIALATTRIBUTE,
        ReferencedInventoryTestUtils.ATTRIBUTE_LAPTOP_SERIALNO, null);
  }

  @Test
  public void testBoxWithLotNoInTheSameBinFullBox() throws Exception {
    testBox(null, ReferencedInventoryTestUtils.PRODUCT_LAPTOP_SERIALATTRIBUTE,
        ReferencedInventoryTestUtils.ATTRIBUTE_LAPTOP_SERIALNO, null);
  }

  @Test
  public void testBoxWithLotNoInDifferentBinFullBox() throws Exception {
    testBox(ReferencedInventoryTestUtils.BIN_SPAIN_L02,
        ReferencedInventoryTestUtils.PRODUCT_LAPTOP_SERIALATTRIBUTE,
        ReferencedInventoryTestUtils.ATTRIBUTE_LAPTOP_SERIALNO, null);
  }

  @Test
  public void testBoxWithoutAttributeInTheSameBinPartialBox() throws Exception {
    testBox(null, ReferencedInventoryTestUtils.PRODUCT_TSHIRT_ID, null, BigDecimal.ONE);
  }

  @Test
  public void testBoxWithoutAttributeInDifferentBinPartialBox() throws Exception {
    testBox(ReferencedInventoryTestUtils.BIN_SPAIN_L02,
        ReferencedInventoryTestUtils.PRODUCT_TSHIRT_ID, null, BigDecimal.ONE);
  }

  @Test
  public void testBoxWithAttributeInTheSameBinPartialBox() throws Exception {
    testBox(null, ReferencedInventoryTestUtils.PRODUCT_BALL_COLORATTRIBUTE,
        ReferencedInventoryTestUtils.ATTRIBUTE_COLOR_YELLOW, BigDecimal.ONE);
  }

  @Test
  public void testBoxWithAttributeInDifferentBinPartialBox() throws Exception {
    testBox(ReferencedInventoryTestUtils.BIN_SPAIN_L02,
        ReferencedInventoryTestUtils.PRODUCT_BALL_COLORATTRIBUTE,
        ReferencedInventoryTestUtils.ATTRIBUTE_COLOR_YELLOW, BigDecimal.ONE);
  }

  @Test
  public void testBoxWithSerialNoInTheSameBinPartialBox() throws Exception {
    testBox(null, ReferencedInventoryTestUtils.PRODUCT_LAPTOP_SERIALATTRIBUTE,
        ReferencedInventoryTestUtils.ATTRIBUTE_LAPTOP_SERIALNO, BigDecimal.ONE);
  }

  @Test
  public void testBoxWithSerialNoInDifferentBinPartialBox() throws Exception {
    testBox(ReferencedInventoryTestUtils.BIN_SPAIN_L02,
        ReferencedInventoryTestUtils.PRODUCT_LAPTOP_SERIALATTRIBUTE,
        ReferencedInventoryTestUtils.ATTRIBUTE_LAPTOP_SERIALNO, BigDecimal.ONE);
  }

  @Test
  public void testBoxWithLotNoInTheSameBinPartialBox() throws Exception {
    testBox(null, ReferencedInventoryTestUtils.PRODUCT_LAPTOP_SERIALATTRIBUTE,
        ReferencedInventoryTestUtils.ATTRIBUTE_LAPTOP_SERIALNO, BigDecimal.ONE);
  }

  @Test
  public void testBoxWithLotNoInDifferentBinPartialBox() throws Exception {
    testBox(ReferencedInventoryTestUtils.BIN_SPAIN_L02,
        ReferencedInventoryTestUtils.PRODUCT_LAPTOP_SERIALATTRIBUTE,
        ReferencedInventoryTestUtils.ATTRIBUTE_LAPTOP_SERIALNO, BigDecimal.ONE);
  }

  @Test
  public void testNoStorageDetailsProvidedToBox() throws Exception {
    final ReferencedInventoryType refInvType = ReferencedInventoryTestUtils
        .createReferencedInventoryType();
    final ReferencedInventory refInv = ReferencedInventoryTestUtils.createReferencedInventory(
        ReferencedInventoryTestUtils.QA_SPAIN_ORG_ID, refInvType);

    thrown.expect(OBException.class);
    thrown.expectMessage(equalTo(OBMessageUtils.messageBD("NotSelected")));
    new BoxProcessor(refInv, null, null).createAndProcessGoodsMovement();
  }

  @Test
  public void testNegativeStorageDetailProvidedToBox() throws Exception {
    final ReferencedInventoryType refInvType = ReferencedInventoryTestUtils
        .createReferencedInventoryType();
    final ReferencedInventory refInv = ReferencedInventoryTestUtils.createReferencedInventory(
        ReferencedInventoryTestUtils.QA_SPAIN_ORG_ID, refInvType);

    final JSONObject storageDetailJS = new JSONObject();
    storageDetailJS.put("id", ReferencedInventoryTestUtils.getAnyStorageDetail());
    storageDetailJS.put("quantityOnHand", BigDecimal.ONE.negate());
    final JSONArray storageDetailsJS = new JSONArray();
    storageDetailsJS.put(storageDetailJS);

    thrown.expect(OBException.class);
    thrown.expectMessage(containsString(String.format(
        OBMessageUtils.messageBD("RefInv_NegativeQty"), "")));
    new BoxProcessor(refInv, storageDetailsJS, null).createAndProcessGoodsMovement();
  }

  @Test
  public void testZeroQtyStorageDetailProvidedToBox() throws Exception {
    final ReferencedInventoryType refInvType = ReferencedInventoryTestUtils
        .createReferencedInventoryType();
    final ReferencedInventory refInv = ReferencedInventoryTestUtils.createReferencedInventory(
        ReferencedInventoryTestUtils.QA_SPAIN_ORG_ID, refInvType);

    final JSONObject storageDetailJS = new JSONObject();
    storageDetailJS.put("id", ReferencedInventoryTestUtils.getAnyStorageDetail());
    storageDetailJS.put("quantityOnHand", BigDecimal.ZERO);
    final JSONArray storageDetailsJS = new JSONArray();
    storageDetailsJS.put(storageDetailJS);

    thrown.expect(OBException.class);
    thrown.expectMessage(containsString(String.format(
        OBMessageUtils.messageBD("RefInv_NegativeQty"), "")));
    new BoxProcessor(refInv, storageDetailsJS, null).createAndProcessGoodsMovement();
  }

  @Test
  public void testBoxQtyGreaterThanQtyOnHand() throws Exception {
    final BigDecimal TWO_HUNDRED = new BigDecimal("200");
    thrown.expect(OBException.class);
    thrown.expectMessage(containsString("(" + TWO_HUNDRED + ")"));
    testBox(null, ReferencedInventoryTestUtils.PRODUCT_TSHIRT_ID, null, TWO_HUNDRED);
  }

  @Test
  public void testCascadeReferencedInventoryNotPossible() throws Exception {
    final ReferencedInventory refInv1 = testBox(null,
        ReferencedInventoryTestUtils.PRODUCT_TSHIRT_ID, null, null);
    final StorageDetail storageDetail = refInv1.getMaterialMgmtStorageDetailList().get(0);

    final ReferencedInventory refInv2 = ReferencedInventoryTestUtils.createReferencedInventory(
        ReferencedInventoryTestUtils.QA_SPAIN_ORG_ID, refInv1.getReferencedInventoryType());
    final JSONArray storageDetailsJS = ReferencedInventoryTestUtils.getStorageDetailsToBoxJSArray(
        storageDetail, BigDecimal.ONE);

    thrown.expect(OBException.class);
    thrown.expectMessage(containsString(" is already linked to the referenced inventory "));
    new BoxProcessor(refInv2, storageDetailsJS, null).createAndProcessGoodsMovement();
  }

  @Test
  public void testSameReferencedInventoryInDifferentBinsNotPossible() throws Exception {
    final ReferencedInventory refInv = testBox(ReferencedInventoryTestUtils.BIN_SPAIN_L02,
        ReferencedInventoryTestUtils.PRODUCT_TSHIRT_ID, null, BigDecimal.ONE);
    final List<StorageDetail> storageDetails = ReferencedInventoryTestUtils
        .getStorageDetails(refInv.getMaterialMgmtStorageDetailList().get(0).getProduct());
    ReferencedInventoryTestUtils.sortStorageDetailsByQtyOnHand(storageDetails);
    final JSONArray storageDetailsJS = ReferencedInventoryTestUtils.getStorageDetailsToBoxJSArray(
        storageDetails.get(1), new BigDecimal("3"));

    thrown.expect(OBException.class);
    thrown.expectMessage(containsString(" is also located in bin "));
    new BoxProcessor(refInv, storageDetailsJS, ReferencedInventoryTestUtils.BIN_SPAIN_L03)
        .createAndProcessGoodsMovement();
  }

  @Test
  public void testReferencedInventorySequenceIsUsed() {
    final Sequence sequence = (Sequence) DalUtil.copy(OBDal.getInstance().getProxy(Sequence.class,
        "FF8080812C2ABFC6012C2B3BE4970094"));
    sequence.setName(UUID.randomUUID().toString());
    OBDal.getInstance().save(sequence);
    OBDal.getInstance().flush(); // Required to lock sequence at db level later on

    final ReferencedInventoryType refInvType = ReferencedInventoryTestUtils
        .createReferencedInventoryType();
    refInvType.setSequence(sequence);
    OBDal.getInstance().save(refInvType);
    Long currentSequenceNumber = sequence.getNextAssignedNumber();

    final ReferencedInventory refInv = ReferencedInventoryTestUtils.createReferencedInventory(
        ReferencedInventoryTestUtils.QA_SPAIN_ORG_ID, refInvType);
    assertThat("Referenced Inventory Search Key is taken from sequence", refInv.getSearchKey(),
        equalTo(Long.toString(currentSequenceNumber)));

    final ReferencedInventory refInv2 = ReferencedInventoryTestUtils.createReferencedInventory(
        ReferencedInventoryTestUtils.QA_SPAIN_ORG_ID, refInvType);
    assertThat("Referenced Inventory Search Key is updated from sequence", refInv2.getSearchKey(),
        equalTo(Long.toString(currentSequenceNumber + 1)));
  }

  @Test
  public void testBoxSeveralStorageDetailsInSameMovement() throws Exception {
    final ReferencedInventoryType refInvType = ReferencedInventoryTestUtils
        .createReferencedInventoryType();
    final ReferencedInventory refInv = ReferencedInventoryTestUtils.createReferencedInventory(
        ReferencedInventoryTestUtils.QA_SPAIN_ORG_ID, refInvType);

    final Product product1 = ReferencedInventoryTestUtils
        .cloneProduct(ReferencedInventoryTestUtils.PRODUCT_TSHIRT_ID);
    final BigDecimal receivedQty = new BigDecimal("10");
    ReferencedInventoryTestUtils.receiveProduct(product1, receivedQty, null);
    final StorageDetail storageDetail1 = ReferencedInventoryTestUtils
        .getUniqueStorageDetail(product1);
    final JSONArray storageDetails1JS = ReferencedInventoryTestUtils.getStorageDetailsToBoxJSArray(
        storageDetail1, receivedQty);

    final Product product2 = ReferencedInventoryTestUtils
        .cloneProduct(ReferencedInventoryTestUtils.PRODUCT_TSHIRT_ID);
    ReferencedInventoryTestUtils.receiveProduct(product2, receivedQty, null);
    final StorageDetail storageDetail2 = ReferencedInventoryTestUtils
        .getUniqueStorageDetail(product2);
    final JSONArray storageDetails2JS = ReferencedInventoryTestUtils.getStorageDetailsToBoxJSArray(
        storageDetail2, receivedQty);

    final JSONArray selectedStorageDetailsJS = storageDetails1JS;
    selectedStorageDetailsJS.put(storageDetails2JS.get(0));

    final InternalMovement boxMovement = new BoxProcessor(refInv, selectedStorageDetailsJS,
        storageDetail1.getStorageBin().getId()).createAndProcessGoodsMovement();

    OBDal.getInstance().refresh(boxMovement);
    OBDal.getInstance().refresh(refInv);

    assertsGoodsMovementIsProcessed(boxMovement);
    assertsGoodsMovementNumberOfLines(boxMovement, 2);
    assertThat("Referenced Inventory has two different storage details", refInv
        .getMaterialMgmtStorageDetailList().size(), equalTo(2));
  }

  @Test
  public void testBoxSeveralStorageDetailsInTwoMovements() throws Exception {
    final ReferencedInventory refInv = testBox(null,
        ReferencedInventoryTestUtils.PRODUCT_TSHIRT_ID, null, null);
    final Product product2 = ReferencedInventoryTestUtils
        .cloneProduct(ReferencedInventoryTestUtils.PRODUCT_TSHIRT_ID);
    final BigDecimal receivedQty = new BigDecimal("10");
    ReferencedInventoryTestUtils.receiveProduct(product2, receivedQty, null);
    final StorageDetail storageDetail2 = ReferencedInventoryTestUtils
        .getUniqueStorageDetail(product2);
    final JSONArray storageDetails2JS = ReferencedInventoryTestUtils.getStorageDetailsToBoxJSArray(
        storageDetail2, receivedQty);

    final InternalMovement boxMovement = new BoxProcessor(refInv, storageDetails2JS, storageDetail2
        .getStorageBin().getId()).createAndProcessGoodsMovement();

    OBDal.getInstance().refresh(boxMovement);
    OBDal.getInstance().refresh(refInv);

    assertsGoodsMovementIsProcessed(boxMovement);
    assertsGoodsMovementNumberOfLines(boxMovement, 1);
    assertThat("Referenced Inventory has two different storage details", refInv
        .getMaterialMgmtStorageDetailList().size(), equalTo(2));
  }

  @Test
  public void testBoxMandatoryNewStorageBinParameter() throws Exception {
    final ReferencedInventoryType refInvType = ReferencedInventoryTestUtils
        .createReferencedInventoryType();
    final ReferencedInventory refInv = ReferencedInventoryTestUtils.createReferencedInventory(
        ReferencedInventoryTestUtils.QA_SPAIN_ORG_ID, refInvType);

    final Product product = ReferencedInventoryTestUtils
        .cloneProduct(ReferencedInventoryTestUtils.PRODUCT_TSHIRT_ID);
    final BigDecimal receivedQty = new BigDecimal("10");
    ReferencedInventoryTestUtils.receiveProduct(product, receivedQty, null);

    final StorageDetail storageDetail = ReferencedInventoryTestUtils
        .getUniqueStorageDetail(product);

    thrown.expect(OBException.class);
    thrown.expectMessage(equalTo(OBMessageUtils.messageBD("NewStorageBinParameterMandatory")));
    new BoxProcessor(refInv, ReferencedInventoryTestUtils.getStorageDetailsToBoxJSArray(
        storageDetail, BigDecimal.ONE), null).createAndProcessGoodsMovement();
  }

}
