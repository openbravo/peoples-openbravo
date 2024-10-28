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
 * All portions are Copyright (C) 2024 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  
 *************************************************************************
 */
package org.openbravo.advpaymentmngt;

import org.openbravo.base.Prioritizable;

/**
 * Interface to be used to extend the Fund Transfer Record functionality.
 * 
 */
public interface FundTransferRecordPostProcessHook extends Prioritizable {

  /**
   * Method to implement in extension classes. This method will be called to extend functionality.
   * 
   * @param fundTransferRecord
   *          The fund transfer record, which include information about the transactions
   */
  public void exec(APRM_FundTransferRec fundTransferRecord) throws Exception;

}
