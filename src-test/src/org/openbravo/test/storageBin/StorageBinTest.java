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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThrows;
import static org.junit.Assume.assumeThat;

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

/**
 * This class implements tests for Storage Bin configuration: Allow Storing Items, Included Handling
 * Unit Types and Handling Unit Type defined.
 */

public class StorageBinTest extends WeldBaseTest {

  private static final Logger log = LogManager.getLogger();

  // Error messages
  private static final String ERROR_MESSAGE_NOT_EDITABLE = "NotEditableAllowStoringItemsHUType";
  private static final String ERROR_MESSAGE_NON_EMPTY_HU = "NonEmptyHUForTransactionAttributeSetInst";
  private static final String ERROR_MESSAGE_Not_Valid_HU_TYPE = "Handling Unit Type is not valid for this storage bin";
  private static final String ERROR_MESSAGE_No_Duplicate_Locator_HU_TYPE = "duplicate key value violates unique constraint";

  @Before
  public void initialize() {
    boolean awoIsInstalled = ReferencedInventoryTestUtils.isAwoInstalled();
    assumeThat("Auto-Disabled test case as incompatible with AWO (found to be installed) ",
        awoIsInstalled, is(false));

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
    Product product = StorageBinTestUtils.getNewProductForTest("SB028");

    // Create stock for product
    StorageBinTestUtils.createStockForProductInBinForTest(StorageBinTestUtils.GOODS_RECEIPT_ID,
        "SB029", product, storageBin, BigDecimal.ONE);

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

    Locator storageBin = StorageBinTestUtils.getNewStorageBinForTest("SB030");
    Product product = StorageBinTestUtils.getNewProductForTest("SB031");

    // Create stock for product
    StorageBinTestUtils.createStockForProductInBinForTest(StorageBinTestUtils.GOODS_RECEIPT_ID,
        "SB032", product, storageBin, BigDecimal.ONE);

    // Consume stock for product
    StorageBinTestUtils.createStockForProductInBinForTest(StorageBinTestUtils.GOODS_SHIPMENT_ID,
        "SB033", product, storageBin, BigDecimal.ONE);

    // when there is no stock available in Storage Bin, Allow Storing Items flag or HU Type
    // Selection could be changed
    storageBin.setAllowStoringItems(!storageBin.isAllowStoringItems());
    OBDal.getInstance().save(storageBin);
    OBDal.getInstance().flush();

    storageBin.setIncludedHandlingUnitTypes(StorageBinTestUtils.ALL_EXCLUDING_DEFINED);
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
    newStorageBin.setIncludedHandlingUnitTypes(StorageBinTestUtils.ONLY_THOSE_DEFINED);
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
  public void testStorageBin_AllowStoringItemsNo() {
    runAllowStoringItemTest(false, true, "Storagebin with allowed storing items no");
  }

  @Test
  public void testStorageBin_AllowStoringItemsYes() {
    runAllowStoringItemTest(true, false, "Storagebin with allowed storing items yes");
  }

  @Test
  public void testStorageBin_AllowStoringItemsNo_IncludedHandlingUnitTypesAll() {
    runHandlingUnitInStorageBinTest(false, StorageBinTestUtils.ALL_SELECTION_MODE, true, false,
        "StorageBin with AllowStoringItems No, IncludedHandlingUnitTypes All, HandlingUnitTypeTab is empty");
  }

  @Test
  public void testStorageBin_AllowStoringItemsNo_IncludedHandlingUnitTypesOnlyThoseDefined_EmptyHandlingUnitTypeTab() {
    runHandlingUnitInStorageBinTest(false, StorageBinTestUtils.ONLY_THOSE_DEFINED, true, true,
        "StorageBin with AllowStoringItems No, IncludedHandlingUnitTypes OnlyThoseDefined, HandlingUnitTypeTab is empty");
  }

  @Test
  public void testStorageBin_AllowStoringItemsNo_IncludedHandlingUnitTypesOnlyThoseDefined() {
    runHandlingUnitInStorageBinTest(false, StorageBinTestUtils.ONLY_THOSE_DEFINED, false, false,
        "StorageBin with AllowStoringItems No, IncludedHandlingUnitTypes OnlyThoseDefined, HandlingUnitTypeTab is not empty");
  }

  @Test
  public void testStorageBin_AllowStoringItemsNo_IncludedHandlingUnitTypesAllExcludingDefined_EmptyHandlingUnitTypeTab() {
    runHandlingUnitInStorageBinTest(false, StorageBinTestUtils.ALL_EXCLUDING_DEFINED, true, false,
        "StorageBin with AllowStoringItems No, IncludedHandlingUnitTypes AllExcludingDefined, HandlingUnitTypeTab is empty");
  }

  @Test
  public void testStorageBin_AllowStoringItemsNo_IncludedHandlingUnitTypesAllExcludingDefined() {
    runHandlingUnitInStorageBinTest(false, StorageBinTestUtils.ALL_EXCLUDING_DEFINED, false, true,
        "StorageBin with AllowStoringItems No, IncludedHandlingUnitTypes AllExcludingDefined, HandlingUnitTypeTab is not empty");
  }

  @Test
  public void testStorageBin_AllowStoringItems_Yes_IncludedHandlingUnitTypes_OnlyThoseDefined() {
    runHandlingUnitInStorageBinTest(true, StorageBinTestUtils.ONLY_THOSE_DEFINED, false, false,
        "StorageBin with AllowStoringItems No, IncludedHandlingUnitTypes OnlyThoseDefined, HandlingUnitTypeTab is not empty");
  }

  @Test
  public void testStorageBin_AllowStoringItems_Yes_IncludedHandlingUnitTypes_All() {
    runHandlingUnitInStorageBinTest(true, StorageBinTestUtils.ALL_SELECTION_MODE, false, false,
        "StorageBin with AllowStoringItems Yes, IncludedHandlingUnitTypes All, HandlingUnitTypeTab is not empty");
  }

  @Test
  public void testStorageBin_AllowStoringItems_Yes_IncludedHandlingUnitTypes_AllExcludingDefined() {
    runHandlingUnitInStorageBinTest(true, StorageBinTestUtils.ALL_EXCLUDING_DEFINED, false, false,
        "StorageBin with AllowStoringItems Yes, IncludedHandlingUnitTypes AllExcludingDefined, HandlingUnitTypeTab is not empty");
  }

  /**
   * Runs a test if a specific handling unit is allowed in a storage bin
   */
  private void runHandlingUnitInStorageBinTest(boolean allowStoringItems,
      String includedHandlingUnitTypes, boolean isHandlingUnitTypeTabEmpty, boolean throwsException,
      String testName) {

    Locator storageBin = StorageBinTestUtils.getNewStorageBinForTest("SB023");

    Product product = StorageBinTestUtils.getNewProductForTest("SB024");

    // Create stock for product
    StorageBinTestUtils.createStockForProductInBinForTest(StorageBinTestUtils.GOODS_RECEIPT_ID,
        "SB025", product, storageBin, BigDecimal.ONE);

    Locator newStorageBin = StorageBinTestUtils.getNewStorageBinForTest("SB026");
    newStorageBin.setAllowStoringItems(allowStoringItems);
    newStorageBin.setIncludedHandlingUnitTypes(includedHandlingUnitTypes);
    OBDal.getInstance().save(newStorageBin);
    OBDal.getInstance().flush();

    final ReferencedInventoryType huType = ReferencedInventoryTestUtils
        .createReferencedInventoryType(
            OBDal.getInstance().getProxy(Organization.class, StorageBinTestUtils.ORG_ID),
            SequenceType.NONE, null);

    if (!isHandlingUnitTypeTabEmpty) {
      StorageBinTestUtils.createStorageBinHUType(newStorageBin, huType);
    }

    final StorageDetail storageDetail = ReferencedInventoryTestUtils
        .getUniqueStorageDetail(product);

    final ReferencedInventory refInv = ReferencedInventoryTestUtils
        .createReferencedInventory(StorageBinTestUtils.ORG_ID, huType);

    if (throwsException) {
      OBException thrown = assertThrows(OBException.class,
          () -> new BoxProcessor(refInv,
              ReferencedInventoryTestUtils.getStorageDetailsToBoxJSArray(storageDetail,
                  storageDetail.getQuantityOnHand()),
              newStorageBin.getId()).createAndProcessGoodsMovement());

      assertThat(thrown.getMessage(), containsString(ERROR_MESSAGE_Not_Valid_HU_TYPE));
    } else {
      try {
        new BoxProcessor(refInv,
            ReferencedInventoryTestUtils.getStorageDetailsToBoxJSArray(storageDetail,
                storageDetail.getQuantityOnHand()),
            newStorageBin.getId()).createAndProcessGoodsMovement();
      } catch (JSONException e) {
        log.error("JSONException while creating box transaction in test " + testName, e,
            e.getMessage());
      } catch (Exception e) {
        log.error("Exception while creating box transaction in test " + testName, e,
            e.getMessage());
      }
    }
  }

  /**
   * Runs a test for allowing items or not
   */
  private void runAllowStoringItemTest(boolean allowStoringItems, boolean throwsException,
      String testName) {
    Locator newStorageBin = StorageBinTestUtils.getNewStorageBinForTest("SB026");
    newStorageBin.setAllowStoringItems(allowStoringItems);
    OBDal.getInstance().save(newStorageBin);
    OBDal.getInstance().flush();

    Product product = StorageBinTestUtils.getNewProductForTest("SB024");
    if (throwsException) {
      Exception thrown = assertThrows(Exception.class,
          () -> StorageBinTestUtils.createStockForProductInBinForTest(
              StorageBinTestUtils.GOODS_RECEIPT_ID, "SB025", product, newStorageBin,
              BigDecimal.ONE));
      assertThat(thrown.getMessage(), containsString(ERROR_MESSAGE_NON_EMPTY_HU));
    } else {
      StorageBinTestUtils.createStockForProductInBinForTest(StorageBinTestUtils.GOODS_RECEIPT_ID,
          "SB025", product, newStorageBin, BigDecimal.ONE);
    }
  }

  @After
  public void cleanUp() {
    OBDal.getInstance().rollbackAndClose();
  }
}
