/*************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.0  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html
 * Software distributed under the License  is  distributed  on  an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific  language  governing  rights  and  limitations
 * under the License.
 * The Original Code is Openbravo ERP.
 * The Initial Developer of the Original Code is Openbravo SLU
 * All portions are Copyright (C) 2017 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 *************************************************************************
 */

package org.openbravo.common.hooks;

import javax.enterprise.context.ApplicationScoped;

import org.apache.commons.lang.StringUtils;
import org.openbravo.base.exception.OBException;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.materialmgmt.ReservationUtils;
import org.openbravo.model.common.enterprise.Locator;
import org.openbravo.model.materialmgmt.onhandquantity.InventoryStatus;
import org.openbravo.model.materialmgmt.onhandquantity.StorageDetail;

@ApplicationScoped
public class InventoryStatusValidationHookReservations implements InventoryStatusValidationHook {

  @Override
  public void exec(Locator locator, InventoryStatus newStatus) throws OBException {
    String errorMessage = "";
    for (StorageDetail storageDetail : locator.getMaterialMgmtStorageDetailList()) {
      if (!newStatus.isAvailable() && ReservationUtils.existsReservationForStock(storageDetail)) {
        errorMessage = errorMessage.concat(
            String.format(OBMessageUtils.messageBD("InventoryStatusChangeReservation"),
                storageDetail.getProduct().getIdentifier(), (StringUtils.equals("0", storageDetail
                    .getAttributeSetValue().getId()) ? OBMessageUtils.messageBD("Empty")
                    : storageDetail.getAttributeSetValue().getIdentifier()), storageDetail.getUOM()
                    .getIdentifier())).concat("<br/>");
      }
    }
    if (!StringUtils.isEmpty(errorMessage)) {
      throw new OBException("WARNING".concat(errorMessage));
    }
  }

}
