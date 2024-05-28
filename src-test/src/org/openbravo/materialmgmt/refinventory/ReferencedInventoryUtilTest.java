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
package org.openbravo.materialmgmt.refinventory;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.openbravo.materialmgmt.refinventory.ReferencedInventoryTestUtils.createHandlingUnit;
import static org.openbravo.materialmgmt.refinventory.ReferencedInventoryTestUtils.createHandlingUnitType;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.materialmgmt.onhandquantity.ReferencedInventory;
import org.openbravo.model.materialmgmt.onhandquantity.ReferencedInventoryType;
import org.openbravo.test.base.OBBaseTest;

/**
 * Tests the utilities provided by {@link ReferencedInventoryUtil}
 */
public class ReferencedInventoryUtilTest extends OBBaseTest {

  private ReferencedInventory container;
  private ReferencedInventory pallet;
  private ReferencedInventory box;

  @Before
  public void prepareHandlingUnits() {
    ReferencedInventoryType containerType = createHandlingUnitType("Container");
    ReferencedInventoryType palletType = createHandlingUnitType("Pallet");
    ReferencedInventoryType boxType = createHandlingUnitType("Box");

    container = createHandlingUnit("C1", containerType);
    pallet = createHandlingUnit("P1", palletType);
    pallet.setParentRefInventory(container);
    box = createHandlingUnit("B1", boxType);
    box.setParentRefInventory(pallet);
    ReferencedInventory box2 = createHandlingUnit("B2", boxType);
    box2.setParentRefInventory(pallet);
    OBDal.getInstance().flush();
  }

  @After
  public void cleanUp() {
    rollback();
  }

  @Test
  public void getDirectChildReferencedInventories() {
    List<String> containerChildren = ReferencedInventoryUtil
        .getDirectChildReferencedInventories(container)
        .map(ReferencedInventory::getSearchKey)
        .collect(Collectors.toList());
    assertThat(containerChildren, hasItems("P1"));

    List<String> palletChildren = ReferencedInventoryUtil
        .getDirectChildReferencedInventories(pallet)
        .map(ReferencedInventory::getSearchKey)
        .collect(Collectors.toList());
    assertThat(palletChildren, hasItems("B1", "B2"));

    List<String> boxChildren = ReferencedInventoryUtil.getDirectChildReferencedInventories(box)
        .map(ReferencedInventory::getSearchKey)
        .collect(Collectors.toList());
    assertThat(boxChildren, empty());
  }
}
