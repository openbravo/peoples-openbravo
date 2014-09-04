/*
 ******************************************************************************
 * The contents of this file are subject to the   Compiere License  Version 1.1
 * ("License"); You may not use this file except in compliance with the License
 * You may obtain a copy of the License at http://www.compiere.org/license.html
 * Software distributed under the License is distributed on an  "AS IS"  basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific language governing rights and limitations under the License.
 * The Original Code is                  Compiere  ERP & CRM  Business Solution
 * The Initial Developer of the Original Code is Jorg Janke  and ComPiere, Inc.
 * Portions created by Jorg Janke are Copyright (C) 1999-2001 Jorg Janke, parts
 * created by ComPiere are Copyright (C) ComPiere, Inc.;   All Rights Reserved.
 * Contributor(s): Openbravo SLU
 * Contributions are Copyright (C) 2001-2014 Openbravo S.L.U.
 ******************************************************************************
 */
package org.openbravo.erpCommon.ad_forms;

import org.apache.log4j.Logger;

public class DocLine_LCCost extends DocLine {
  static Logger log4jDocLine_LCCost = Logger.getLogger(DocLine_LCCost.class);

  public DocLine_LCCost(String DocumentType, String TrxHeader_ID, String TrxLine_ID) {
    super(DocumentType, TrxHeader_ID, TrxLine_ID);
  }

  private String trxAmt;
  private String warehouseId;
  private String landedCostTypeId;
  private String lcCostId;
  private String isMatchingAdjusted;

  public void setAmount(String amt) {
    trxAmt = amt;
  } // setAmounts

  public String getAmount() {
    return trxAmt;
  } // setAmounts

  public void setWarehouseId(String warehouse) {
    warehouseId = warehouse;
  }

  public String getWarehouseId() {
    return warehouseId;
  }

  public void setLandedCostTypeId(String landedCostType) {
    landedCostTypeId = landedCostType;
  }

  public String getLandedCostTypeId() {
    return landedCostTypeId;
  }

  public void setLcCostId(String lcCost) {
    lcCostId = lcCost;
  }

  public String getLcCostId() {
    return lcCostId;
  }

  public void setIsMatchingAdjusted(String p_isMatchingAdjusted) {
    isMatchingAdjusted = p_isMatchingAdjusted;
  }

  public String getIsMatchingAdjusted() {
    return isMatchingAdjusted;
  }

  public String getServletInfo() {
    return "Servlet for the accounting";
  } // end of getServletInfo() method
}
