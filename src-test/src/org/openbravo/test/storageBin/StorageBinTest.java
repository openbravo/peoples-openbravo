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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openbravo.base.weld.test.WeldBaseTest;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.common.enterprise.Locator;
import org.openbravo.model.common.plm.Product;

public class StorageBinTest extends WeldBaseTest {

  final static private Logger log = LogManager.getLogger();

  @Before
  public void initialize() {
    log.info("Initializing Storage Bin Test ...");
    OBContext.setOBContext(StorageBinTestUtils.USER_ID, StorageBinTestUtils.ROLE_ID,
        StorageBinTestUtils.CLIENT_ID, StorageBinTestUtils.ORG_ID,
        StorageBinTestUtils.LANGUAGE_CODE);
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
    Exception thrown = assertThrows(Exception.class, () -> OBDal.getInstance().flush());
    assertThat(thrown.getMessage(), containsString(StorageBinTestUtils.ERROR_MESSAGE));

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

  @After
  public void cleanUp() {
    OBDal.getInstance().rollbackAndClose();
  }
}
