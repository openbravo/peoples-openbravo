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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assume.assumeThat;

import java.math.BigDecimal;

import org.apache.commons.math3.random.RandomDataGenerator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.base.weld.test.WeldBaseTest;
import org.openbravo.client.kernel.KernelUtils;
import org.openbravo.client.kernel.RequestContext;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.SequenceUtil.CalculationMethod;
import org.openbravo.erpCommon.utility.SequenceUtil.ControlDigit;
import org.openbravo.erpCommon.utility.SequenceUtil.SequenceNumberLength;
import org.openbravo.model.ad.utility.Sequence;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.materialmgmt.transaction.InternalMovement;

/**
 * Helper class to develop Referenced Inventory related tests
 */
public abstract class ReferencedInventoryTest extends WeldBaseTest {
  protected static final Logger log = LogManager.getLogger();

  // All tests creates a new product with 10 units
  protected static final BigDecimal RECEIVEDQTY_10 = new BigDecimal("10");

  protected static Sequence SEQUENCE = null;

  protected final String[] BINS = { ReferencedInventoryTestUtils.BIN_SPAIN_L01,
      ReferencedInventoryTestUtils.BIN_SPAIN_L02 };
  protected final String[][] PRODUCTS = { { ReferencedInventoryTestUtils.PRODUCT_TSHIRT_ID, null },
      { ReferencedInventoryTestUtils.PRODUCT_BALL_COLORATTRIBUTE,
          ReferencedInventoryTestUtils.ATTRIBUTE_COLOR_YELLOW },
      { ReferencedInventoryTestUtils.PRODUCT_LAPTOP_SERIALATTRIBUTE,
          ReferencedInventoryTestUtils.ATTRIBUTE_LAPTOP_SERIALNO } };
  protected final boolean[] ISALLOCATED = { false, true };

  private boolean isAwoInstalled() {
    try {
      OBContext.setAdminMode(true);
      return KernelUtils.getInstance()
          .isModulePresent("org.openbravo.warehouse.advancedwarehouseoperations");
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  @Before
  public void initialize() {
    boolean awoIsInstalled = isAwoInstalled();
    assumeThat("Auto-Disabled test case as incompatible with AWO (found to be installed) ",
        awoIsInstalled, is(false));

    setUserContext(QA_TEST_ADMIN_USER_ID);
    VariablesSecureApp vsa = new VariablesSecureApp(OBContext.getOBContext().getUser().getId(),
        OBContext.getOBContext().getCurrentClient().getId(),
        OBContext.getOBContext().getCurrentOrganization().getId());
    RequestContext.get().setVariableSecureApp(vsa);
    ReferencedInventoryTestUtils.initializeReservationsPreferenceIfDoesnotExist();

    // create single document sequence to be used throughout the tests to avoid duplicate document
    // numbers
    SEQUENCE = createSequenceForRefInvType();
  }

  void assertsGoodsMovementIsProcessed(final InternalMovement boxMovement) {
    assertThat("Box movement must be processed", boxMovement.isProcessed(), equalTo(true));
  }

  void assertsGoodsMovementNumberOfLines(final InternalMovement boxMovement,
      final int expectedNumberOfLines) {
    assertThat("Box Movement has one line",
        boxMovement.getMaterialMgmtInternalMovementLineList().size(),
        equalTo(expectedNumberOfLines));
  }

  /**
   * Define Sequence for Referenced Inventory Type
   * 
   * @return Sequence to be defined for Referenced Inventory Type
   */

  protected static Sequence createSequenceForRefInvType() {
    Organization org = OBDal.getInstance()
        .getProxy(Organization.class, ReferencedInventoryTestUtils.QA_SPAIN_ORG_ID);
    Sequence childSequence = ReferencedInventoryTestUtils.setUpChildSequence(org,
        ControlDigit.MODULE10, CalculationMethod.AUTONUMERING, "0110491", 1L,
        new RandomDataGenerator().nextLong(1L, 999999999L), 1L, null, SequenceNumberLength.VARIABLE,
        null, false);
    return ReferencedInventoryTestUtils.createDocumentSequence(org, ControlDigit.MODULE10,
        CalculationMethod.SEQUENCE, "6", null, null, null, "000", childSequence,
        SequenceNumberLength.VARIABLE, null);
  }

  @After
  public void clearSession() {
    OBDal.getInstance().getSession().clear();
  }

}
