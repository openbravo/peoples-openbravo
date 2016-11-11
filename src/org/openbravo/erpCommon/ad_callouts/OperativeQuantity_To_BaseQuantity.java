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
 * All portions are Copyright (C) 2016 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.erpCommon.ad_callouts;

import java.math.BigDecimal;

import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.core.OBContext;
import org.openbravo.erpCommon.businessUtility.Preferences;
import org.openbravo.erpCommon.utility.PropertyException;
import org.openbravo.materialmgmt.CentralBroker;

public class OperativeQuantity_To_BaseQuantity extends SimpleCallout {
  private static final Logger log4j = Logger.getLogger(OperativeQuantity_To_BaseQuantity.class);

  private static final long serialVersionUID = 1L;
  private static final String ADWINDOW_SalesOrder = "143";
  private static final String ADWINDOW_PurchaseOrder = "181";
  private static final String ADWINDOW_GoodsShipment = "169";
  private static final String ADWINDOW_GoodsReceipt = "184";
  private static final String ADWINDOW_SalesInvoice = "167";
  private static final String ADWINDOW_PurchaseInvoice = "183";
  private static final String ADWINDOW_Requisition = "800092";
  private static final String ADWINDOW_SalesQuotation = "6CB5B67ED33F47DFA334079D3EA2340E";

  /**
   * Converts a quantity from an alternate unit of measure to the base unit of the product
   */

  @Override
  protected void execute(CalloutInfo info) throws ServletException {

    String strWindowId = info.getWindowId();
    VariablesSecureApp vars = info.vars;
    BigDecimal qty = (vars.getNumericParameter("inpaumqty").isEmpty()) ? null : new BigDecimal(
        vars.getNumericParameter("inpaumqty"));
    String strOperativeUOM = vars.getStringParameter("inpcAum");
    String strBaseUOM = vars.getStringParameter("inpcUomId");
    String mProductId = vars.getStringParameter("inpmProductId");

    try {
      String propertyValue = Preferences.getPreferenceValue("UomManagement", true, OBContext
          .getOBContext().getCurrentClient(), OBContext.getOBContext().getCurrentOrganization(),
          OBContext.getOBContext().getUser(), OBContext.getOBContext().getRole(), null);
      if (propertyValue.equalsIgnoreCase("Y")) {

        if (!strOperativeUOM.equals(strBaseUOM) && !strOperativeUOM.isEmpty()) {
          qty = CentralBroker.getInstance().getConvertedQty(mProductId, qty, strOperativeUOM);
        } else if (strOperativeUOM.isEmpty()) {
          qty = null;
        }
      }
    } catch (PropertyException e) {
      log4j.error("Error while converting UOM. ", e);
      e.printStackTrace();
      qty = null;
    } catch (OBException e) {
      log4j.error("Error while converting UOM. ", e);
      info.showError(e.getMessage());
      qty = null;
    } finally {
      if (strWindowId.equals(ADWINDOW_SalesOrder) || strWindowId.equals(ADWINDOW_PurchaseOrder)
          || strWindowId.equals(ADWINDOW_SalesQuotation)) {
        info.addResult("inpqtyordered", qty);
      } else if (strWindowId.equals(ADWINDOW_GoodsShipment)
          || strWindowId.equals(ADWINDOW_GoodsReceipt)) {
        info.addResult("inpmovementqty", qty);
      } else if (strWindowId.equals(ADWINDOW_SalesInvoice)
          || strWindowId.equals(ADWINDOW_PurchaseInvoice)) {
        info.addResult("inpqtyinvoiced", qty);
      } else if (strWindowId.equals(ADWINDOW_Requisition)) {
        info.addResult("inpqty", qty);
      }
    }
  }
}
