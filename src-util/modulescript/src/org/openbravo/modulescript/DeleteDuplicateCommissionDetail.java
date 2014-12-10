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
 * All portions are Copyright (C) 2014 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.modulescript;

import org.openbravo.database.ConnectionProvider;

public class DeleteDuplicateCommissionDetail extends ModuleScript {

  @Override
  public void execute() {
    try {
      ConnectionProvider cp = getConnectionProvider();
      boolean isDeleteDuplicateCommissionDetailExecuted = DeleteDuplicateCommissionDetailData.isDeleteDuplicateCommissionDetailExecuted(cp);
      
      if (!isDeleteDuplicateCommissionDetailExecuted){
        DeleteDuplicateCommissionDetailData.deleteDuplicateCommissionDetail(cp);
        DeleteDuplicateCommissionDetailData.updateCommissionQty(cp);
        
        DeleteDuplicateCommissionDetailData[] data = DeleteDuplicateCommissionDetailData.getCommissionPyments(cp);
        for (DeleteDuplicateCommissionDetailData commissionPayment : data){
          DeleteDuplicateCommissionDetailData.updateCommissionAmount(cp, commissionPayment.amtresult, commissionPayment.cCommissionamtId);
          DeleteDuplicateCommissionDetailData.updateCommissionPayment(cp, commissionPayment.amtresult, commissionPayment.cCommissionrunId);
        }
        
        DeleteDuplicateCommissionDetailData.createPreference(cp);
      }
    } catch (Exception e) {
      handleError(e);
    }
  }
}
