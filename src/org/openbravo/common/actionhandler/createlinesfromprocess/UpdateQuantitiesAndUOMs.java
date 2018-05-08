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

package org.openbravo.common.actionhandler.createlinesfromprocess;

import java.math.BigDecimal;

import javax.enterprise.context.Dependent;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.client.kernel.ComponentProvider.Qualifier;
import org.openbravo.common.actionhandler.createlinesfromprocess.util.CreateLinesFromUtil;
import org.openbravo.dal.service.OBDal;
import org.openbravo.materialmgmt.UOMUtil;
import org.openbravo.model.common.invoice.Invoice;
import org.openbravo.model.common.invoice.InvoiceLine;
import org.openbravo.model.common.order.OrderLine;
import org.openbravo.model.common.plm.Product;
import org.openbravo.model.common.plm.ProductUOM;
import org.openbravo.model.common.uom.UOM;
import org.openbravo.model.materialmgmt.transaction.ShipmentInOutLine;

@Dependent
@Qualifier(CreateLinesFromProcessImplementationInterface.CREATE_LINES_FROM_PROCESS_HOOK_QUALIFIER)
class UpdateQuantitiesAndUOMs implements CreateLinesFromProcessImplementationInterface {
  private Invoice processingInvoice;
  private BaseOBObject copiedLine;
  private boolean isOrderLine;
  private JSONObject pickExecLineValues;

  @Override
  public int getOrder() {
    return -30;
  }

  /**
   * Calculation of quantities and UOM-AUM Support
   */
  @Override
  public void exec(final Invoice currentInvoice, final JSONObject pickExecuteLineValues,
      final BaseOBObject selectedLine, InvoiceLine newInvoiceLine) {
    this.copiedLine = selectedLine;
    this.processingInvoice = currentInvoice;
    this.pickExecLineValues = pickExecuteLineValues;
    this.isOrderLine = CreateLinesFromUtil.isOrderLine(selectedLine);

    BigDecimal orderedQuantity = CreateLinesFromUtil
        .getOrderedQuantityInPickEdit(pickExecLineValues);
    BigDecimal operativeQuantity = CreateLinesFromUtil
        .getOperativeQuantityInPickEdit(pickExecLineValues);
    BigDecimal orderQuantity = CreateLinesFromUtil.getOrderQuantityInPickEdit(pickExecLineValues);
    UOM operativeUOM = (UOM) copiedLine.get(isOrderLine ? OrderLine.PROPERTY_OPERATIVEUOM
        : ShipmentInOutLine.PROPERTY_OPERATIVEUOM);
    UOM uOM = (UOM) copiedLine.get(isOrderLine ? OrderLine.PROPERTY_UOM
        : ShipmentInOutLine.PROPERTY_UOM);
    ProductUOM orderUOM = (ProductUOM) copiedLine.get(isOrderLine ? OrderLine.PROPERTY_ORDERUOM
        : ShipmentInOutLine.PROPERTY_ORDERUOM);
    Product product = (Product) copiedLine.get(isOrderLine ? OrderLine.PROPERTY_PRODUCT
        : ShipmentInOutLine.PROPERTY_PRODUCT);

    if (uomManagementIsEnabledAndAUMAndOrderUOMAreEmpty()) {
      String defaultAum = UOMUtil.getDefaultAUMForDocument(product.getId(), processingInvoice
          .getTransactionDocument().getId());
      operativeQuantity = orderedQuantity;
      operativeUOM = OBDal.getInstance().getProxy(UOM.class, defaultAum);
      orderUOM = null;
      if (aUMIsDifferentThanUOM(defaultAum)) {
        operativeQuantity = UOMUtil.getConvertedAumQty(product.getId(), orderedQuantity,
            operativeUOM.getId());
      }
    }

    newInvoiceLine.setInvoicedQuantity(orderedQuantity);
    newInvoiceLine.setUOM(uOM);
    if (UOMUtil.isUomManagementEnabled()) {
      newInvoiceLine.setOperativeQuantity(operativeQuantity);
      newInvoiceLine.setOperativeUOM(operativeUOM);
    }
    newInvoiceLine.setOrderQuantity(orderQuantity);
    newInvoiceLine.setOrderUOM(orderUOM);
  }

  private boolean uomManagementIsEnabledAndAUMAndOrderUOMAreEmpty() {
    boolean isUomManagementEnabled = UOMUtil.isUomManagementEnabled();
    BigDecimal operativeQuantity = CreateLinesFromUtil
        .getOperativeQuantityInPickEdit(pickExecLineValues);
    UOM operativeUOM = (UOM) copiedLine.get(isOrderLine ? OrderLine.PROPERTY_OPERATIVEUOM
        : ShipmentInOutLine.PROPERTY_OPERATIVEUOM);
    ProductUOM orderUOM = (ProductUOM) copiedLine.get(isOrderLine ? OrderLine.PROPERTY_ORDERUOM
        : ShipmentInOutLine.PROPERTY_ORDERUOM);

    return isUomManagementEnabled && orderUOM == null && operativeUOM == null
        && operativeQuantity == null;
  }

  private boolean aUMIsDifferentThanUOM(final String defaultAum) {
    UOM uOM = (UOM) copiedLine.get(isOrderLine ? OrderLine.PROPERTY_UOM
        : ShipmentInOutLine.PROPERTY_UOM);
    return !StringUtils.equals(defaultAum, uOM.getId());
  }

}
