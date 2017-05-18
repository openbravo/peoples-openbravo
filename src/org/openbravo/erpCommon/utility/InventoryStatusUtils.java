/*
 *************************************************************************
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

package org.openbravo.erpCommon.utility;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.weld.WeldUtils;
import org.openbravo.common.hooks.InventoryStatusHookManager;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.common.enterprise.Locator;
import org.openbravo.model.materialmgmt.onhandquantity.InventoryStatus;
import org.openbravo.model.materialmgmt.onhandquantity.StorageDetail;

public class InventoryStatusUtils {

  private static final Logger log4j = Logger.getLogger(InventoryStatusUtils.class);

  /**
   * Changes the Inventory Status of the given Storage Bin
   * 
   * @param storageBinID
   *          ID of the Storage Bin that is going to change it's Inventory Status
   * @param inventoryStatusID
   *          ID of the new Inventory Status that is going to be set to the Storage Bin
   */
  public static void changeStatusOfStorageBin(String storageBinID, String inventoryStatusID) {
    Locator storageBin = OBDal.getInstance().get(Locator.class, storageBinID);
    // No change required needed
    if (StringUtils.equals(storageBin.getInventoryStatus().getId(), inventoryStatusID)) {
      return;
    }
    String errorMessage = "";
    if (storageBin.isVirtual()) {
      throw new OBException(OBMessageUtils.messageBD("M_VirtualBinCanNotChangeInvStatus").concat(
          "<br/>"));
    }
    for (StorageDetail storageDetail : storageBin.getMaterialMgmtStorageDetailList()) {
      try {
        // Hook to perform validations over the Storage Detail
        WeldUtils.getInstanceFromStaticBeanManager(InventoryStatusHookManager.class)
            .executeValidationHooks(storageDetail,
                OBDal.getInstance().get(InventoryStatus.class, inventoryStatusID));
      } catch (Exception e) {
        errorMessage = errorMessage.concat(e.getMessage()).concat("<br/>");
      }
    }
    if (!StringUtils.isEmpty(errorMessage)) {
      if (StringUtils.startsWith(errorMessage, "WARNING")) {
        storageBin.setInventoryStatus(OBDal.getInstance().get(InventoryStatus.class,
            inventoryStatusID));
        OBDal.getInstance().flush();
        throw new OBException(errorMessage);
      } else {
        log4j.error(errorMessage);
        throw new OBException(errorMessage);
      }
    } else {
      storageBin.setInventoryStatus(OBDal.getInstance().get(InventoryStatus.class,
          inventoryStatusID));
      OBDal.getInstance().flush();
    }
  }

  /**
   * Changes the Inventory Status of the given Storage Bin
   * 
   * @param storageBin
   *          Storage Bin that is going to change it's Inventory Status
   * @param inventoryStatusID
   *          ID of the new Inventory Status that is going to be set to the Storage Bin
   */
  public static void changeStatusOfStorageBin(Locator storageBin, String inventoryStatusID) {
    changeStatusOfStorageBin(storageBin.getId(), inventoryStatusID);
  }

  /**
   * Returns the number of Virtual Bins that are associated to the given Storage Bin
   */
  public static int getNumberOfVirtualBins(Locator storageBin) {
    OBCriteria<Locator> obc = OBDal.getInstance().createCriteria(Locator.class);
    obc.add(Restrictions.eq(Locator.PROPERTY_ISVIRTUAL, true));
    obc.add(Restrictions.eq(Locator.PROPERTY_PARENTLOCATOR, storageBin));
    return obc.list().size();
  }

}
