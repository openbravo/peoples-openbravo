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
package org.openbravo.test.storageBin;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThrows;

import java.math.BigDecimal;

import javax.persistence.PersistenceException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.base.weld.test.WeldBaseTest;
import org.openbravo.client.kernel.RequestContext;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.materialmgmt.refinventory.BoxProcessor;
import org.openbravo.materialmgmt.refinventory.ReferencedInventoryUtil.SequenceType;
import org.openbravo.model.common.enterprise.Locator;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.common.plm.Product;
import org.openbravo.model.materialmgmt.onhandquantity.ReferencedInventory;
import org.openbravo.model.materialmgmt.onhandquantity.ReferencedInventoryType;
import org.openbravo.model.materialmgmt.onhandquantity.StorageDetail;
import org.openbravo.service.db.DbUtility;
import org.openbravo.test.referencedinventory.ReferencedInventoryTestUtils;

public class StorageBinTest extends WeldBaseTest {

  private static final Logger log = LogManager.getLogger();
  // Error messages
  private static final String ERROR_MESSAGE_NOT_EDITABLE = "NotEditableAllowStoringItemsHUType";
  private static final String ERROR_MESSAGE_Not_Valid_HU_TYPE = "Handling Unit Type is not valid for the locator";
  private static final String ERROR_MESSAGE_No_Duplicate_Locator_HU_TYPE = "duplicate key value violates unique constraint";

  @Before
  public void initialize() {
    log.info("Initializing Storage Bin Test ...");
    OBContext.setOBContext(StorageBinTestUtils.USER_ID, StorageBinTestUtils.ROLE_ID,
        StorageBinTestUtils.CLIENT_ID, StorageBinTestUtils.ORG_ID,
        StorageBinTestUtils.LANGUAGE_CODE);
    VariablesSecureApp vsa = new VariablesSecureApp(OBContext.getOBContext().getUser().getId(),
        OBContext.getOBContext().getCurrentClient().getId(),
        OBContext.getOBContext().getCurrentOrganization().getId());
    RequestContext.get().setVariableSecureApp(vsa);
  }

  @Test
  public void testAllowStoringItemsHUTypeSelectionWithStock() {

    Locator storageBin = StorageBinTestUtils.getNewStorageBinForTest("SB001");
    Product product = StorageBinTestUtils.getNewProductForTest("SB001");

    // Create stock for product
    StorageBinTestUtils.createStockForProductInBinForTest(StorageBinTestUtils.GOODS_RECEIPT_ID,
        "SB001", product, storageBin, BigDecimal.ONE);

    // Expect to throw an exception when changing Allow Storing Items flag or HU Type Selection
    storageBin.setAllowStoringItems(!storageBin.isAllowStoringItems());
    OBDal.getInstance().save(storageBin);
    PersistenceException thrown = assertThrows(PersistenceException.class,
        () -> OBDal.getInstance().flush());
    assertThat(DbUtility.getUnderlyingSQLException(thrown).getMessage(),
        containsString(ERROR_MESSAGE_NOT_EDITABLE));

  }

  @Test
  public void testAllowStoringItemsHUTypeSelectionWithoutStock() {

    Locator storageBin = StorageBinTestUtils.getNewStorageBinForTest("SB001");
    Product product = StorageBinTestUtils.getNewProductForTest("SB001");

    // Create stock for product
    StorageBinTestUtils.createStockForProductInBinForTest(StorageBinTestUtils.GOODS_RECEIPT_ID,
        "SB002", product, storageBin, BigDecimal.ONE);

    // Consume stock for product
    StorageBinTestUtils.createStockForProductInBinForTest(StorageBinTestUtils.GOODS_SHIPMENT_ID,
        "SB003", product, storageBin, BigDecimal.ONE);

    // when there is no stock available in Storage Bin, Allow Storing Items flag or HU Type
    // Selection could be changed
    storageBin.setAllowStoringItems(!storageBin.isAllowStoringItems());
    OBDal.getInstance().save(storageBin);
    OBDal.getInstance().flush();

    storageBin.setHandlingUnitTypeSelection("E");
    OBDal.getInstance().save(storageBin);
    OBDal.getInstance().flush();
  }

  @Test
  public void testNoDuplicateStorageBinHUType() {

    Locator storageBin = StorageBinTestUtils.getNewStorageBinForTest("SB004");

    Product product = StorageBinTestUtils.getNewProductForTest("SB005");

    // Create stock for product
    StorageBinTestUtils.createStockForProductInBinForTest(StorageBinTestUtils.GOODS_RECEIPT_ID,
        "SB006", product, storageBin, BigDecimal.ONE);

    Locator newStorageBin = StorageBinTestUtils.getNewStorageBinForTest("SB007");
    newStorageBin.setAllowStoringItems(false);
    newStorageBin.setHandlingUnitTypeSelection("I");
    OBDal.getInstance().save(newStorageBin);
    OBDal.getInstance().flush();

    final ReferencedInventoryType huType = ReferencedInventoryTestUtils
        .createReferencedInventoryType(
            OBDal.getInstance().getProxy(Organization.class, StorageBinTestUtils.ORG_ID),
            SequenceType.NONE, null);

    StorageBinTestUtils.createStorageBinHUType(newStorageBin, huType);

    PersistenceException thrown = assertThrows(PersistenceException.class, () -> {
      StorageBinTestUtils.createStorageBinHUType(newStorageBin, huType);
      OBDal.getInstance().flush();
    });

    assertThat(DbUtility.getUnderlyingSQLException(thrown).getMessage(),
        containsString(ERROR_MESSAGE_No_Duplicate_Locator_HU_TYPE));

  }

  @Test
  public void testStorageBinHUTypeSelection_OnlyThoseDefined() {

    Locator storageBin = StorageBinTestUtils.getNewStorageBinForTest("SB004");

    Product product = StorageBinTestUtils.getNewProductForTest("SB005");

    // Create stock for product
    StorageBinTestUtils.createStockForProductInBinForTest(StorageBinTestUtils.GOODS_RECEIPT_ID,
        "SB006", product, storageBin, BigDecimal.ONE);

    Locator newStorageBin = StorageBinTestUtils.getNewStorageBinForTest("SB007");
    newStorageBin.setAllowStoringItems(false);
    newStorageBin.setHandlingUnitTypeSelection("I");
    OBDal.getInstance().save(newStorageBin);
    OBDal.getInstance().flush();

    final ReferencedInventoryType huType = ReferencedInventoryTestUtils
        .createReferencedInventoryType(
            OBDal.getInstance().getProxy(Organization.class, StorageBinTestUtils.ORG_ID),
            SequenceType.NONE, null);

    StorageBinTestUtils.createStorageBinHUType(newStorageBin, huType);

    final StorageDetail storageDetail = ReferencedInventoryTestUtils
        .getUniqueStorageDetail(product);

    final ReferencedInventory refInv = ReferencedInventoryTestUtils
        .createReferencedInventory(StorageBinTestUtils.ORG_ID, huType);

    try {
      new BoxProcessor(refInv,
          ReferencedInventoryTestUtils.getStorageDetailsToBoxJSArray(storageDetail,
              storageDetail.getQuantityOnHand()),
          newStorageBin.getId()).createAndProcessGoodsMovement();
    } catch (JSONException e) {
      log.error(
          "JSONException while creating box transaction in testStorageBinHUTypeSelection_OnlyThoseDefined ",
          e, e.getMessage());
    } catch (Exception e) {
      log.error(
          "Exception while creating box transaction in testStorageBinHUTypeSelection_OnlyThoseDefined ",
          e, e.getMessage());
    }
  }

  @Test
  public void testStorageBinHUTypeSelection_AllExcludingDefined() {

    final ReferencedInventoryType huType = ReferencedInventoryTestUtils
        .createReferencedInventoryType(
            OBDal.getInstance().getProxy(Organization.class, StorageBinTestUtils.ORG_ID),
            SequenceType.NONE, null);

    Locator storageBin = StorageBinTestUtils.getNewStorageBinForTest("SB008");

    Product product = StorageBinTestUtils.getNewProductForTest("SB009");

    // Create stock for product
    StorageBinTestUtils.createStockForProductInBinForTest(StorageBinTestUtils.GOODS_RECEIPT_ID,
        "SB010", product, storageBin, BigDecimal.ONE);

    Locator newStorageBin = StorageBinTestUtils.getNewStorageBinForTest("SB011");
    newStorageBin.setAllowStoringItems(false);
    newStorageBin.setHandlingUnitTypeSelection("I");
    OBDal.getInstance().save(newStorageBin);
    OBDal.getInstance().flush();

    StorageBinTestUtils.createStorageBinHUType(storageBin, huType);

    final ReferencedInventory refInv = ReferencedInventoryTestUtils
        .createReferencedInventory(StorageBinTestUtils.ORG_ID, huType);

    final StorageDetail storageDetail = ReferencedInventoryTestUtils
        .getUniqueStorageDetail(product);

    OBException thrown = assertThrows(OBException.class,
        () -> new BoxProcessor(refInv,
            ReferencedInventoryTestUtils.getStorageDetailsToBoxJSArray(storageDetail,
                storageDetail.getQuantityOnHand()),
            newStorageBin.getId()).createAndProcessGoodsMovement());

    assertThat(thrown.getMessage(), containsString(ERROR_MESSAGE_Not_Valid_HU_TYPE));

  }

  @After
  public void cleanUp() {
    OBDal.getInstance().rollbackAndClose();
  }
}
