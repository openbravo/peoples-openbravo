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

import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

import java.math.BigDecimal;
import java.util.List;

import javax.servlet.ServletException;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.openbravo.dal.service.OBDal;
import org.openbravo.exception.NoConnectionAvailableException;
import org.openbravo.materialmgmt.refinventory.ReferencedInventoryUtil;
import org.openbravo.materialmgmt.refinventory.UnboxProcessor;
import org.openbravo.model.common.plm.Product;
import org.openbravo.model.materialmgmt.onhandquantity.ReferencedInventory;
import org.openbravo.model.materialmgmt.onhandquantity.StorageDetail;
import org.openbravo.model.materialmgmt.transaction.InternalMovement;

/**
 * Tests related to unboxing into referenced inventory
 */
public class ReferencedInventoryUnboxTest extends ReferencedInventoryTest {

  @Test
  public void testFullUnboxWithoutAttributes() throws Exception {
    testFullUnbox(null, ReferencedInventoryTestUtils.PRODUCT_TSHIRT_ID, null);
  }

  @Test
  public void testPartialUnboxWithoutAttributes() throws Exception {
    testPartialUnbox(null, ReferencedInventoryTestUtils.PRODUCT_TSHIRT_ID, null);
  }

  @Test
  public void testFullUnboxWithAttributes() throws Exception {
    testFullUnbox(null, ReferencedInventoryTestUtils.PRODUCT_BALL_COLORATTRIBUTE,
        ReferencedInventoryTestUtils.ATTRIBUTE_COLOR_YELLOW);
  }

  @Test
  public void testPartialUnboxWithAttributes() throws Exception {
    testPartialUnbox(null, ReferencedInventoryTestUtils.PRODUCT_BALL_COLORATTRIBUTE,
        ReferencedInventoryTestUtils.ATTRIBUTE_COLOR_YELLOW);
  }

  @Test
  public void testFullUnboxWithSerialNo() throws Exception {
    testFullUnbox(null, ReferencedInventoryTestUtils.PRODUCT_LAPTOP_SERIALATTRIBUTE,
        ReferencedInventoryTestUtils.ATTRIBUTE_LAPTOP_SERIALNO);
  }

  @Test
  public void testPartialUnboxWithSerialNo() throws Exception {
    testPartialUnbox(null, ReferencedInventoryTestUtils.PRODUCT_LAPTOP_SERIALATTRIBUTE,
        ReferencedInventoryTestUtils.ATTRIBUTE_LAPTOP_SERIALNO);
  }

  @Test
  public void testFullUnboxWithoutAttributesDifferentBin() throws Exception {
    testFullUnbox(ReferencedInventoryTestUtils.BIN_SPAIN_L02,
        ReferencedInventoryTestUtils.PRODUCT_TSHIRT_ID, null);
  }

  @Test
  public void testPartialUnboxWithoutAttributesDifferentBin() throws Exception {
    testPartialUnbox(ReferencedInventoryTestUtils.BIN_SPAIN_L02,
        ReferencedInventoryTestUtils.PRODUCT_TSHIRT_ID, null);
  }

  @Test
  public void testFullUnboxWithAttributesDifferentBin() throws Exception {
    testFullUnbox(ReferencedInventoryTestUtils.BIN_SPAIN_L02,
        ReferencedInventoryTestUtils.PRODUCT_BALL_COLORATTRIBUTE,
        ReferencedInventoryTestUtils.ATTRIBUTE_COLOR_YELLOW);
  }

  @Test
  public void testPartialUnboxWithAttributesDifferentBin() throws Exception {
    testPartialUnbox(ReferencedInventoryTestUtils.BIN_SPAIN_L02,
        ReferencedInventoryTestUtils.PRODUCT_BALL_COLORATTRIBUTE,
        ReferencedInventoryTestUtils.ATTRIBUTE_COLOR_YELLOW);
  }

  @Test
  public void testFullUnboxWithSerialNoDifferentBin() throws Exception {
    testFullUnbox(ReferencedInventoryTestUtils.BIN_SPAIN_L02,
        ReferencedInventoryTestUtils.PRODUCT_LAPTOP_SERIALATTRIBUTE,
        ReferencedInventoryTestUtils.ATTRIBUTE_LAPTOP_SERIALNO);
  }

  @Test
  public void testPartialUnboxWithSerialNoDifferentBin() throws Exception {
    testPartialUnbox(ReferencedInventoryTestUtils.BIN_SPAIN_L02,
        ReferencedInventoryTestUtils.PRODUCT_LAPTOP_SERIALATTRIBUTE,
        ReferencedInventoryTestUtils.ATTRIBUTE_LAPTOP_SERIALNO);
  }

  private TestUnboxOutputParams testUnbox(final String _toBinId, final String productId,
      final String attributeSetInstanceId, final BigDecimal qtyToUnbox) throws Exception {
    ReferencedInventory refInv = testBox(null, productId, attributeSetInstanceId, new BigDecimal(
        "10"));
    final List<StorageDetail> storageDetails = refInv.getMaterialMgmtStorageDetailList();
    final Product originalProduct = storageDetails.get(0).getProduct();
    final String originalAttributeSet = ReferencedInventoryUtil.getParentAttributeSetInstance(
        storageDetails.get(0)).getId();

    final String toBinId = StringUtils.isBlank(_toBinId) ? storageDetails.get(0).getStorageBin()
        .getId() : _toBinId;
    final InternalMovement unBoxMovement = new UnboxProcessor(refInv,
        ReferencedInventoryTestUtils.getUnboxStorageDetailsJSArray(storageDetails.get(0),
            qtyToUnbox == null ? storageDetails.get(0).getQuantityOnHand() : qtyToUnbox, toBinId))
        .createAndProcessGoodsMovement();

    OBDal.getInstance().refresh(unBoxMovement);
    OBDal.getInstance().getSession().evict(refInv); // Hack to avoid problems in Hibernate when the
                                                    // unbox process is executed
    refInv = OBDal.getInstance().get(ReferencedInventory.class, refInv.getId());

    assertsGoodsMovementIsProcessed(unBoxMovement);
    assertsGoodsMovementNumberOfLines(unBoxMovement, 1);

    return new TestUnboxOutputParams(refInv, originalProduct, originalAttributeSet, toBinId);
  }

  private class TestUnboxOutputParams {
    private ReferencedInventory refInv;
    private Product originalProduct;
    private String originalAttributeSetId;
    private String toBinId;

    private TestUnboxOutputParams(ReferencedInventory refInv, Product originalProduct,
        String originalAttributeSetId, String toBinId) {
      this.refInv = refInv;
      this.originalProduct = originalProduct;
      this.originalAttributeSetId = originalAttributeSetId;
      this.toBinId = toBinId;
    }
  }

  private void testFullUnbox(final String _toBinId, final String productId,
      final String attributeSetInstanceId) throws Exception {
    final TestUnboxOutputParams params = testUnbox(_toBinId, productId, attributeSetInstanceId,
        null);
    assertsReferenceInventoryIsEmpty(params.refInv);
    assertsAttributeSetIsProperlyRestored(params.refInv, params.originalAttributeSetId,
        params.originalProduct);

    final StorageDetail outStorageDetail = ReferencedInventoryTestUtils.getStorageDetails(
        params.originalProduct).get(0);
    assertsUnboxedStorageDetailIsInRightBin(outStorageDetail, params.toBinId);
  }

  private void testPartialUnbox(final String _toBinId, final String productId,
      final String attributeSetInstanceId) throws Exception {
    final TestUnboxOutputParams params = testUnbox(_toBinId, productId, attributeSetInstanceId,
        BigDecimal.ONE);
    assertsReferenceInventoryIsNotEmpty(params.refInv, new BigDecimal("9"));
    assertsAttributeSet(params.refInv, params.originalAttributeSetId);

    final List<StorageDetail> afterUnboxStorageDetails = ReferencedInventoryTestUtils
        .getStorageDetails(params.originalProduct);
    ReferencedInventoryTestUtils.sortStorageDetailsByQtyOnHand(afterUnboxStorageDetails);
    final StorageDetail outStorageDetail = afterUnboxStorageDetails.get(0);
    assertsUnboxedStorageDetailIsInRightBin(outStorageDetail, params.toBinId);
  }

  private void assertsReferenceInventoryIsEmpty(final ReferencedInventory refInv) throws Exception {
    for (final StorageDetail storageDetail : refInv.getMaterialMgmtStorageDetailList()) {
      assertThat("Storage detail found in referenced inventory must not have qty on hand",
          storageDetail.getQuantityOnHand(), equalTo(BigDecimal.ZERO));
    }
  }

  private void assertsReferenceInventoryIsNotEmpty(final ReferencedInventory refInv,
      final BigDecimal expectedQtyInRefInv) throws Exception {
    final List<StorageDetail> storageDetails = refInv.getMaterialMgmtStorageDetailList();
    assertThat("Referenced inventory must still be linked to a storage detail",
        storageDetails.size(), equalTo(1));
    assertThat("Storage detail must have expected qty on hand", storageDetails.get(0)
        .getQuantityOnHand(), equalTo(expectedQtyInRefInv));
    assertThat("Attribute Set description does contain info about referenced inventory",
        storageDetails.get(0).getAttributeSetValue().getDescription(),
        endsWith(ReferencedInventoryUtil.REFERENCEDINVENTORYPREFIX + refInv.getSearchKey()
            + ReferencedInventoryUtil.REFERENCEDINVENTORYSUFFIX));
  }

  private void assertsAttributeSetIsProperlyRestored(ReferencedInventory refInv,
      String originalAttributeSet, Product product) throws Exception {
    for (final StorageDetail storageDetail : ReferencedInventoryTestUtils
        .getStorageDetails(product)) {
      if (storageDetail.getQuantityOnHand().compareTo(BigDecimal.ZERO) > 0) {
        assertThat("Current storage detail has restored its original attribute set", storageDetail
            .getAttributeSetValue().getId(), equalTo(originalAttributeSet));
        assertThat("Attribute Set description doesn't contain info about referenced inventory",
            storageDetail.getAttributeSetValue().getDescription(),
            not(endsWith(ReferencedInventoryUtil.REFERENCEDINVENTORYPREFIX + refInv.getSearchKey()
                + ReferencedInventoryUtil.REFERENCEDINVENTORYSUFFIX)));
      }
    }
  }

  private void assertsUnboxedStorageDetailIsInRightBin(final StorageDetail unboxedStorageDetail,
      final String toBinId) throws ServletException, NoConnectionAvailableException {
    assertThat("Unboxed storage detail is in the expected bin", unboxedStorageDetail
        .getStorageBin().getId(), equalTo(toBinId));
  }

}
