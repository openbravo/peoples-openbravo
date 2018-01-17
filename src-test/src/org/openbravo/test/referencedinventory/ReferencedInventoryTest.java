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

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.math.BigDecimal;
import java.util.List;

import javax.servlet.ServletException;

import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.base.weld.test.WeldBaseTest;
import org.openbravo.client.kernel.RequestContext;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.exception.NoConnectionAvailableException;
import org.openbravo.materialmgmt.refinventory.BoxProcessor;
import org.openbravo.materialmgmt.refinventory.ReferencedInventoryUtil;
import org.openbravo.model.common.plm.Product;
import org.openbravo.model.materialmgmt.onhandquantity.ReferencedInventory;
import org.openbravo.model.materialmgmt.onhandquantity.ReferencedInventoryType;
import org.openbravo.model.materialmgmt.onhandquantity.StorageDetail;
import org.openbravo.model.materialmgmt.transaction.InternalMovement;

/**
 * Helper class to develop Referenced Inventory related tests
 */
public abstract class ReferencedInventoryTest extends WeldBaseTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Before
  public void initialize() {
    setUserContext(QA_TEST_ADMIN_USER_ID);
    VariablesSecureApp vsa = new VariablesSecureApp(OBContext.getOBContext().getUser().getId(),
        OBContext.getOBContext().getCurrentClient().getId(), OBContext.getOBContext()
            .getCurrentOrganization().getId());
    RequestContext.get().setVariableSecureApp(vsa);
  }

  /**
   * Runs a box process and verifies it.
   * 
   * @param _toBinId
   *          new bin where the referenced inventory is left. If null it is the same as the current
   *          storage detail
   * @param productId
   *          Mandatory. A new product will be created as a clone of this one
   * @param attributeSetInstanceId
   *          Mandatory only when do product does require it.
   * @param qtyInBox
   *          quantity of the storage detail to be included in the reference inventory. Note that
   *          there is maximum availability of 10 units of the cloned product, so this quantity
   *          should be lower or equal than 10
   * @return the created referenced inventory
   */
  protected ReferencedInventory testBox(final String _toBinId, final String productId,
      final String attributeSetInstanceId, final BigDecimal qtyInBox) throws Exception {
    final ReferencedInventoryType refInvType = ReferencedInventoryTestUtils
        .createReferencedInventoryType();
    final ReferencedInventory refInv = ReferencedInventoryTestUtils.createReferencedInventory(
        ReferencedInventoryTestUtils.QA_SPAIN_ORG_ID, refInvType);

    final Product product = ReferencedInventoryTestUtils.cloneProduct(productId);
    final BigDecimal receivedQty = new BigDecimal("10");
    ReferencedInventoryTestUtils.receiveProduct(product, receivedQty, attributeSetInstanceId);

    final StorageDetail storageDetail = ReferencedInventoryTestUtils
        .getUniqueStorageDetail(product);
    final String originalAttributeId = storageDetail.getAttributeSetValue().getId();
    final String originalStorageBinId = storageDetail.getStorageBin().getId();

    final String toBinId = StringUtils.isBlank(_toBinId) ? storageDetail.getStorageBin().getId()
        : _toBinId;

    final InternalMovement boxMovement = new BoxProcessor(refInv,
        ReferencedInventoryTestUtils.getStorageDetailsToBoxJSArray(storageDetail, qtyInBox),
        toBinId).createAndProcessGoodsMovement();

    OBDal.getInstance().refresh(boxMovement);
    OBDal.getInstance().refresh(refInv);

    assertsGoodsMovementIsProcessed(boxMovement);
    assertsGoodsMovementNumberOfLines(boxMovement, 1);
    assertsReferencedInventory(toBinId, qtyInBox, refInv, product, receivedQty, storageDetail);
    assertsStorageDetail(refInv);
    assertsAttributeSet(refInv, originalAttributeId);
    assertsMultipleStorageDetailsInPartialBoxing(toBinId, qtyInBox, product, receivedQty,
        storageDetail, originalStorageBinId);

    return refInv;
  }

  protected void assertsGoodsMovementIsProcessed(final InternalMovement boxMovement) {
    assertThat("Box movement must be processed", boxMovement.isProcessed(), equalTo(true));
  }

  protected void assertsGoodsMovementNumberOfLines(final InternalMovement boxMovement,
      final int expectedNumberOfLines) {
    assertThat("Box Movement has one line", boxMovement.getMaterialMgmtInternalMovementLineList()
        .size(), equalTo(expectedNumberOfLines));
  }

  protected void assertsReferencedInventory(final String toBinId, final BigDecimal qtyInBox,
      final ReferencedInventory refInv, final Product product, final BigDecimal receivedQty,
      final StorageDetail originalStorageDetail) {
    assertThat("Referenced Inventory is not empty", refInv.getMaterialMgmtStorageDetailList(),
        not(empty()));
    assertThat("Referenced Inventory has one record", refInv.getMaterialMgmtStorageDetailList()
        .size(), equalTo(1));
    assertThat("Referenced Inventory has right product", refInv.getMaterialMgmtStorageDetailList()
        .get(0).getProduct().getId(), equalTo(product.getId()));
    assertThat("Referenced Inventory has right quantity", refInv.getMaterialMgmtStorageDetailList()
        .get(0).getQuantityOnHand(), equalTo(qtyInBox == null ? receivedQty : qtyInBox));
    assertThat("Referenced Inventory is in the right bin", refInv
        .getMaterialMgmtStorageDetailList().get(0).getStorageBin().getId(),
        equalTo(StringUtils.isBlank(toBinId) ? originalStorageDetail.getStorageBin().getId()
            : toBinId));
  }

  protected void assertsStorageDetail(final ReferencedInventory refInv) {
    assertThat("Storage Detail is linked to Referenced Inventory", refInv
        .getMaterialMgmtStorageDetailList().get(0).getReferencedInventory().getId(),
        equalTo(refInv.getId()));
    assertThat("Storage Detail attribute set is not null", refInv
        .getMaterialMgmtStorageDetailList().get(0).getAttributeSetValue(), notNullValue());
    assertThat("Storage Detail attribute set is not zero", refInv
        .getMaterialMgmtStorageDetailList().get(0).getAttributeSetValue().getId(),
        not(equalTo("0")));
  }

  protected void assertsAttributeSet(final ReferencedInventory refInv,
      final String originalAttributeId) {
    assertThat("New attribute set is related to the referenced inventory", refInv
        .getMaterialMgmtStorageDetailList().get(0).getAttributeSetValue().getReferencedInventory()
        .getId(), equalTo(refInv.getId()));
    assertThat("New attribute set is related to previous one", ReferencedInventoryUtil
        .getParentAttributeSetInstance(refInv.getMaterialMgmtStorageDetailList().get(0)).getId(),
        equalTo(originalAttributeId));
    assertThat("New attribute set description contains referenced inventory string", refInv
        .getMaterialMgmtStorageDetailList().get(0).getAttributeSetValue().getDescription(),
        endsWith(ReferencedInventoryUtil.REFERENCEDINVENTORYPREFIX + refInv.getSearchKey()
            + ReferencedInventoryUtil.REFERENCEDINVENTORYSUFFIX));
  }

  protected void assertsMultipleStorageDetailsInPartialBoxing(final String toBinId,
      final BigDecimal qtyInBox, final Product product, final BigDecimal receivedQty,
      final StorageDetail storageDetail, final String originalStorageBinId)
      throws ServletException, NoConnectionAvailableException {
    if (qtyInBox != null && qtyInBox.compareTo(receivedQty) < 0) {
      OBDal.getInstance().refresh(storageDetail);
      final List<StorageDetail> storageDetails = ReferencedInventoryTestUtils
          .getStorageDetails(product);
      ReferencedInventoryTestUtils.sortStorageDetailsByQtyOnHand(storageDetails);
      assertThat("Two storage details were found", storageDetails.size(), equalTo(2));
      assertThat("First storage detail qty is the boxed qty", qtyInBox,
          equalTo(storageDetails.get(0).getQuantityOnHand()));
      assertThat("First storage detail is in new bin",
          StringUtils.isBlank(toBinId) ? originalStorageBinId : toBinId, equalTo(storageDetails
              .get(0).getStorageBin().getId()));
      assertThat("Second storage detail qty is original - boxed", receivedQty.subtract(qtyInBox),
          equalTo(storageDetails.get(1).getQuantityOnHand()));
      assertThat("Second storage detail is in old bin", originalStorageBinId,
          equalTo(storageDetails.get(1).getStorageBin().getId()));
    }
  }

}
