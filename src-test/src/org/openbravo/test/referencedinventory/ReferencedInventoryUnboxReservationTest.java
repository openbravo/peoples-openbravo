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

import java.math.BigDecimal;

/**
 * Abstract class to test unboxing related to reservations
 */
public abstract class ReferencedInventoryUnboxReservationTest extends ReferencedInventoryUnboxTest {

  protected TestUnboxOutputParams testUnboxReservation(final String toBinId,
      final String productId, final String attributeSetInstanceId, final BigDecimal qtyToBox,
      final BigDecimal qtyToUnbox, final BigDecimal reservationQty, final boolean isAllocated)
      throws Exception {
    final TestUnboxOutputParams outParams = super.testUnbox(toBinId, productId,
        attributeSetInstanceId, qtyToUnbox, reservationQty, isAllocated);

    return outParams;
  }

  protected class ParamsUnboxReservationTest extends ParamsBoxReservationTest {
    BigDecimal qtyToUnbox;

    ParamsUnboxReservationTest(String testDesc, String qtyToBox, String qtyToUnbox,
        String reservationQty) {
      super(testDesc, qtyToBox, reservationQty);
      this.qtyToUnbox = new BigDecimal(qtyToUnbox);
    }
  }

}
