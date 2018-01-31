package org.openbravo.test.referencedinventory;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.openbravo.base.weld.test.ParameterCdiTest;
import org.openbravo.base.weld.test.ParameterCdiTestRule;
import org.openbravo.model.materialmgmt.onhandquantity.StorageDetail;

public class ReferencedInventoryPartialUnboxPartialReservation extends
    ReferencedInventoryUnboxReservationTest {

  @Rule
  public ParameterCdiTestRule<ParamsUnboxReservationTest> parameterValuesRule = new ParameterCdiTestRule<ParamsUnboxReservationTest>(
      Arrays
          .asList(new ParamsUnboxReservationTest[] {
              new ParamsUnboxReservationTest(
                  "Unbox more quantity than reserved one. Part of the reservation is still in box",
                  "10", "7", "4"),
              new ParamsUnboxReservationTest(
                  "Unbox less quantity than reserved one. Part of the reservation is still in box",
                  "10", "3", "8"),
              new ParamsUnboxReservationTest(
                  "Unbox less quantity than reserved one. Reservation is fully in box", "10", "3",
                  "1"),
              new ParamsUnboxReservationTest(
                  "Unbox less quantity than reserved one. Reservation is fully in box", "10", "1",
                  "9"), }));

  protected @ParameterCdiTest ParamsUnboxReservationTest params;

  @Test
  public void allTests() throws Exception {
    for (boolean isAllocated : ISALLOCATED) {
      for (String[] product : PRODUCTS) {
        for (String toBinId : BINS) {
          final TestUnboxOutputParams outParams = testUnboxReservation(toBinId, product[0],
              product[1], params.qtyToBox, params.qtyToUnbox, params.reservationQty, isAllocated);
          assertsReferenceInventoryIsNotEmpty(outParams.refInv,
              params.qtyToBox.subtract(params.qtyToUnbox));
          assertStorageDetailInBox(params.qtyToBox, params.qtyToUnbox, params.reservationQty,
              outParams);
        }
      }
    }
  }

  private void assertStorageDetailInBox(final BigDecimal qtyToBox, final BigDecimal qtyToUnbox,
      final BigDecimal reservationQty, final TestUnboxOutputParams outParams) {
    final List<StorageDetail> storageDetailsInBox = outParams.refInv
        .getMaterialMgmtStorageDetailList();
    assertThat("One Storage Detail must be in the referenced inventory",
        storageDetailsInBox.size(), equalTo(1));
    final StorageDetail storageDetailInBox = storageDetailsInBox.get(0);
    assertThat("Qty in box is the expected one", qtyToBox.subtract(qtyToUnbox),
        equalTo(storageDetailInBox.getQuantityOnHand()));

    if (hasUnboxedReservedQty(qtyToUnbox, reservationQty, new BigDecimal("10"))) {
      assertThat("Storage Detail in box is totally reserved", storageDetailInBox.getReservedQty(),
          equalTo(storageDetailInBox.getQuantityOnHand().min(reservationQty)));
    } else {
      assertThat(
          "Qty in box reserved is expected one",
          storageDetailInBox.getReservedQty(),
          equalTo((qtyToBox.subtract(qtyToUnbox)).compareTo(reservationQty) < 0 ? reservationQty
              .subtract(qtyToBox.subtract(qtyToUnbox)) : reservationQty));
    }
  }

  private boolean hasUnboxedReservedQty(final BigDecimal qtyToUnbox,
      final BigDecimal reservationQty, final BigDecimal receivedQty) {
    return reservationQty != null
    // If available qty not reserved is lower than qty to unbox then I need to unbox reserved qty
        && (receivedQty.subtract(reservationQty)).compareTo(qtyToUnbox) < 0;
  }
}
