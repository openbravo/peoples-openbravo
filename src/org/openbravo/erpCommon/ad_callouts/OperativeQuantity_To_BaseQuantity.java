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
 * All portions are Copyright (C) 2016-2017 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.erpCommon.ad_callouts;

import java.math.BigDecimal;

import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.openbravo.base.exception.OBException;
import org.openbravo.dal.core.OBContext;
import org.openbravo.materialmgmt.UOMUtil;

/**
 * 
 * Callout to convert from alternate quantity to base quantity
 *
 */
public class OperativeQuantity_To_BaseQuantity extends SimpleCallout {

  private static final Logger logger = Logger.getLogger(OperativeQuantity_To_BaseQuantity.class);

  private static final String ADWINDOW_SalesOrder = "143";
  private static final String ADWINDOW_PurchaseOrder = "181";
  private static final String ADWINDOW_GoodsShipment = "169";
  private static final String ADWINDOW_GoodsReceipt = "184";
  private static final String ADWINDOW_GoodsMovements = "170";
  private static final String ADWINDOW_SalesInvoice = "167";
  private static final String ADWINDOW_PurchaseInvoice = "183";
  private static final String ADWINDOW_Requisition = "800092";
  private static final String ADWINDOW_ManageRequisition = "1004400000";
  private static final String ADWINDOW_SalesQuotation = "6CB5B67ED33F47DFA334079D3EA2340E";
  private static final String ADWINDOW_ReceiveDistributionOrder = "E5F3A81364F6485EA1C6960409C6BCA5";
  private static final String ADWINDOW_IssueDistributionOrder = "F3BBB20F4BA1436CB49ADA517E0CC1E1";

  /**
   * Converts a quantity from an alternate unit of measure to the base unit of the product
   */

  @Override
  protected void execute(CalloutInfo info) throws ServletException {

    String strWindowId = info.getWindowId();
    BigDecimal qty = info.getBigDecimalParameter("inpaumqty");
    String strOperativeUOM = info.getStringParameter("inpcAum");
    String strBaseUOM = info.getStringParameter("inpcUomId");
    String mProductId = info.getStringParameter("inpmProductId");
    try {
      OBContext.setAdminMode();
      if (UOMUtil.isUomManagementEnabled()) {
        if (strOperativeUOM == null || strOperativeUOM.isEmpty()) {
          qty = null;
        } else if (!strOperativeUOM.equals(strBaseUOM)) {
          qty = UOMUtil.getConvertedQty(mProductId, qty, strOperativeUOM);
        }
      }
    } catch (OBException e) {
      logger.error("Error while converting UOM. ", e);
      info.showError(e.getMessage());
      qty = null;
    } finally {
      OBContext.restorePreviousMode();
      if (strWindowId.equals(ADWINDOW_SalesOrder) || strWindowId.equals(ADWINDOW_PurchaseOrder)
          || strWindowId.equals(ADWINDOW_SalesQuotation)) {
        info.addResult("inpqtyordered", qty);
      } else if (strWindowId.equals(ADWINDOW_GoodsShipment)
          || strWindowId.equals(ADWINDOW_GoodsReceipt)
          || strWindowId.equals(ADWINDOW_IssueDistributionOrder)
          || strWindowId.equals(ADWINDOW_ReceiveDistributionOrder)
          || strWindowId.equals(ADWINDOW_GoodsMovements)) {
        info.addResult("inpmovementqty", qty);
      } else if (strWindowId.equals(ADWINDOW_SalesInvoice)
          || strWindowId.equals(ADWINDOW_PurchaseInvoice)) {
        info.addResult("inpqtyinvoiced", qty);
      } else if (strWindowId.equals(ADWINDOW_Requisition)) {
        info.addResult("inpqty", qty);
      } else if (strWindowId.equals(ADWINDOW_ManageRequisition)) {
        info.addResult("inpqty", qty);
      }
    }
  }
}
