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

package org.openbravo.common.actionhandler.createlinesfromorderprocess;

import java.math.BigDecimal;

import javax.enterprise.context.Dependent;

import org.apache.commons.lang.StringUtils;
import org.openbravo.client.kernel.ComponentProvider.Qualifier;
import org.openbravo.dal.service.OBDal;
import org.openbravo.materialmgmt.UOMUtil;
import org.openbravo.model.common.invoice.Invoice;
import org.openbravo.model.common.invoice.InvoiceLine;
import org.openbravo.model.common.order.OrderLine;
import org.openbravo.model.common.plm.ProductUOM;
import org.openbravo.model.common.uom.UOM;

@Dependent
@Qualifier(CreateLinesFromOrderProcessImplementationInterface.CREATE_LINES_FROM_PROCESS_HOOK_QUALIFIER)
class UpdateQuantitiesAndUOMs implements CreateLinesFromOrderProcessImplementationInterface {
  // Order Line that is being copied
  private OrderLine orderLine;

  @Override
  public int getOrder() {
    return -30;
  }

  /**
   * Calculation of quantities and UOM-AUM Support
   */
  @Override
  public void exec(final Invoice processingInvoice, final OrderLine copiedOrderLine,
      InvoiceLine newInvoiceLine) {
    this.orderLine = copiedOrderLine;

    BigDecimal orderedQuantity = orderLine.getOrderedQuantity();
    BigDecimal operativeQuantity = orderLine.getOperativeQuantity();
    UOM operativeUOM = orderLine.getOperativeUOM();
    ProductUOM orderUOM = orderLine.getOrderUOM();

    if (uomManagementIsEnabledAndAUMAndOrderUOMAreEmpty()) {
      String defaultAum = UOMUtil.getDefaultAUMForDocument(orderLine.getProduct().getId(),
          processingInvoice.getTransactionDocument().getId());
      operativeQuantity = orderLine.getOrderedQuantity();
      operativeUOM = OBDal.getInstance().getProxy(UOM.class, defaultAum);
      orderUOM = null;
      if (aUMIsDifferentThanUOM(defaultAum)) {
        operativeQuantity = UOMUtil.getConvertedAumQty(orderLine.getProduct().getId(),
            orderedQuantity, operativeUOM.getId());
      }
    }

    newInvoiceLine.setInvoicedQuantity(orderedQuantity);
    newInvoiceLine.setUOM(orderLine.getUOM());
    newInvoiceLine.setOperativeQuantity(operativeQuantity);
    newInvoiceLine.setOperativeUOM(operativeUOM);
    newInvoiceLine.setOrderQuantity(orderLine.getOrderQuantity());
    newInvoiceLine.setOrderUOM(orderUOM);
  }

  private boolean uomManagementIsEnabledAndAUMAndOrderUOMAreEmpty() {
    boolean isUomManagementEnabled = UOMUtil.isUomManagementEnabled();
    return isUomManagementEnabled && orderLine.getOrderUOM() == null
        && orderLine.getOperativeUOM() == null && orderLine.getOperativeQuantity() == null;
  }

  private boolean aUMIsDifferentThanUOM(final String defaultAum) {
    return !StringUtils.equals(defaultAum, orderLine.getUOM().getId());
  }

}
