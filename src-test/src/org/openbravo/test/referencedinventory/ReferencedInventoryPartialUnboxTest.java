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
import static org.junit.Assert.assertThat;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.openbravo.base.weld.test.ParameterCdiTest;
import org.openbravo.base.weld.test.ParameterCdiTestRule;
import org.openbravo.materialmgmt.refinventory.ReferencedInventoryUtil;
import org.openbravo.model.materialmgmt.onhandquantity.ReferencedInventory;
import org.openbravo.model.materialmgmt.onhandquantity.StorageDetail;

/**
 * Partial unbox (1 unit of 10 units) of a storage detail without reservations
 */
public class ReferencedInventoryPartialUnboxTest extends ReferencedInventoryUnboxTest {

  @Rule
  public ParameterCdiTestRule<ParamsUnboxTest> parameterValuesRule = new ParameterCdiTestRule<ParamsUnboxTest>(
      Arrays
          .asList(new ParamsUnboxTest[] { new ParamsUnboxTest(
              "Partial unbox (1 unit of 10 units) of a storage detail without reservations", "10",
              "1") }));

  private @ParameterCdiTest ParamsUnboxTest params;

  @Test
  public void allTests() throws Exception {
    for (String[] product : PRODUCTS) {
      for (String toBinId : BINS) {
        testPartialUnbox(toBinId, product[0], product[1]);
      }
    }
  }

  private void testPartialUnbox(final String _toBinId, final String productId,
      final String attributeSetInstanceId) throws Exception {
    final TestUnboxOutputParams outParams = testUnbox(_toBinId, productId, attributeSetInstanceId,
        BigDecimal.ONE);
    assertsReferenceInventoryIsNotEmpty(outParams.refInv, new BigDecimal("9"));
    assertsAttributeSetIsValid(outParams.refInv, outParams.originalAttributeSetId);

    final List<StorageDetail> afterUnboxStorageDetails = ReferencedInventoryTestUtils
        .getStorageDetails(outParams.originalProduct);
    ReferencedInventoryTestUtils.sortStorageDetailsByQtyOnHand(afterUnboxStorageDetails);
    final StorageDetail outStorageDetail = afterUnboxStorageDetails.get(0);
    assertsUnboxedStorageDetailIsInRightBin(outStorageDetail, outParams.toBinId);
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

}
