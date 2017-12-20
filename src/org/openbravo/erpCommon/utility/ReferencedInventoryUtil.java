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
 * All portions are Copyright (C) 2017 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.erpCommon.utility;

import org.openbravo.advpaymentmngt.utility.FIN_Utility;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.ad.utility.Sequence;
import org.openbravo.model.materialmgmt.onhandquantity.ReferencedInventoryType;

/**
 * Utility class for Referenced Inventory feature
 *
 */
public class ReferencedInventoryUtil {

  /**
   * If the given referenced inventory type id is associated to a sequence, it then return the next
   * value in that sequence. Otherwise returns null.
   * 
   * @param referencedInventoryTypeId
   *          Referenced Inventory Type Id used to get its sequence
   * @param updateNext
   *          if true updates the sequence's next value in database
   */
  public static String getProposedValueFromSequenceOrNull(final String referencedInventoryTypeId,
      final boolean updateNext) {
    return FIN_Utility.getDocumentNo(updateNext, getSequence(referencedInventoryTypeId));
  }

  /**
   * Returns the sequence associated to the given referenced inventory type id or null if not found
   */
  private static Sequence getSequence(final String referencedInventoryTypeId) {
    return OBDal.getInstance().get(ReferencedInventoryType.class, referencedInventoryTypeId)
        .getSequence();
  }

}
