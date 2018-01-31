package org.openbravo.test.referencedinventory;

import java.util.Arrays;

import org.junit.Rule;
import org.junit.Test;
import org.openbravo.base.weld.test.ParameterCdiTest;
import org.openbravo.base.weld.test.ParameterCdiTestRule;

public class ReferencedInventoryFullUnboxPartialReservation extends
    ReferencedInventoryUnboxReservationTest {

  @Rule
  public ParameterCdiTestRule<ParamsUnboxReservationTest> parameterValuesRule2 = new ParameterCdiTestRule<ParamsUnboxReservationTest>(
      Arrays
          .asList(new ParamsUnboxReservationTest[] { new ParamsUnboxReservationTest(
              "Full unbox of a partial reservation. Storage detail should be reserved and out of the box",
              "10", "10", "4") }));

  protected @ParameterCdiTest ParamsUnboxReservationTest params;

  @Test
  public void allTests() throws Exception {
    for (boolean isAllocated : ISALLOCATED) {
      for (String[] product : PRODUCTS) {
        for (String toBinId : BINS) {
          final TestUnboxOutputParams outParams = testUnboxReservation(toBinId, product[0],
              product[1], params.qtyToBox, params.qtyToUnbox, params.reservationQty, isAllocated);
          assertsReferenceInventoryIsEmpty(outParams.refInv);
          // TODO
        }
      }
    }
  }

}
