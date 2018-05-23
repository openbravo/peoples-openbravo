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

package org.openbravo.common.actionhandler.createlinesfromprocess.util;

import java.math.BigDecimal;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.client.application.ApplicationConstants;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.model.common.invoice.Invoice;
import org.openbravo.model.common.order.OrderLine;
import org.openbravo.model.common.uom.UOM;
import org.openbravo.model.materialmgmt.transaction.ShipmentInOutLine;
import org.openbravo.service.db.DbUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CreateLinesFromUtil {
  private static final Logger log = LoggerFactory.getLogger(CreateLinesFromUtil.class);

  public static final String MESSAGE = "message";
  private static final String MESSAGE_SEVERITY = "severity";
  private static final String MESSAGE_TEXT = "text";
  private static final String MESSAGE_TITLE = "title";
  private static final String MESSAGE_SUCCESS = "success";
  private static final String MESSAGE_ERROR = "error";

  public static JSONObject getSuccessMessage() throws JSONException {
    JSONObject errorMessage = new JSONObject();
    errorMessage.put(MESSAGE_SEVERITY, MESSAGE_SUCCESS);
    errorMessage.put(MESSAGE_TITLE, "Success");
    errorMessage.put(MESSAGE_TEXT, OBMessageUtils.messageBD(MESSAGE_SUCCESS));
    return errorMessage;
  }

  public static JSONObject getErrorMessage(final Exception e) throws JSONException {
    Throwable ex = DbUtility.getUnderlyingSQLException(e);
    String message = OBMessageUtils.translateError(ex.getMessage()).getMessage();
    JSONObject errorMessage = new JSONObject();
    errorMessage.put(MESSAGE_SEVERITY, MESSAGE_ERROR);
    errorMessage.put(MESSAGE_TITLE, "Error");
    errorMessage.put(MESSAGE_TEXT, message);
    return errorMessage;
  }

  public static boolean isOrderLine(BaseOBObject line) {
    return line instanceof OrderLine;
  }

  public static boolean isShipmentReceiptLine(BaseOBObject line) {
    return line instanceof ShipmentInOutLine;
  }

  public static Invoice getCurrentInvoice(JSONObject jsonRequest) {
    Invoice invoice = null;
    try {
      String invoiceId = jsonRequest.getString("inpcInvoiceId");
      invoice = OBDal.getInstance().get(Invoice.class, invoiceId);
    } catch (JSONException e) {
      log.error("Error getting the invoice.", e);
      throw new OBException(e);
    }
    return invoice;
  }

  public static String getRequestedAction(final JSONObject jsonRequest) throws JSONException {
    return jsonRequest.getString(ApplicationConstants.BUTTON_VALUE);
  }

  public static JSONArray getSelectedLines(final JSONObject jsonRequest) throws JSONException {
    return jsonRequest.getJSONObject("_params").getJSONObject("grid").getJSONArray("_selection");
  }

  public static boolean requestedActionIsDoneAndThereAreSelectedOrderLines(
      final String requestedAction, final JSONArray selectedOrderLines) {
    return StringUtils.equals(requestedAction, "DONE") && selectedOrderLines.length() > 0;
  }

  public static BigDecimal getOrderedQuantity(BaseOBObject line, JSONObject selectedPEValuesInLine) {
    BigDecimal orderedQuantity = null;
    if (isOrderLine(line) && ((OrderLine) line).getGoodsShipmentLine() != null) {
      orderedQuantity = ((OrderLine) line).getGoodsShipmentLine().getMovementQuantity();
    } else {
      orderedQuantity = getOrderedQuantity(selectedPEValuesInLine);
    }
    return orderedQuantity;
  }

  private static BigDecimal getOrderedQuantity(JSONObject selectedPEValuesInLine) {
    try {
      return new BigDecimal(selectedPEValuesInLine.getString(selectedPEValuesInLine
          .has("orderedQuantity") ? "orderedQuantity" : "movementQuantity"));
    } catch (JSONException e) {
      log.error("Error getting the Ordered Quantity.", e);
      throw new OBException(e);
    }
  }

  public static BigDecimal getOperativeQuantity(BaseOBObject line, JSONObject selectedPEValuesInLine) {
    BigDecimal operativeQuantity = null;
    if (isOrderLine(line) && ((OrderLine) line).getGoodsShipmentLine() != null) {
      operativeQuantity = ((OrderLine) line).getGoodsShipmentLine().getOperativeQuantity();
    } else {
      operativeQuantity = getOperativeQuantity(selectedPEValuesInLine);
    }
    return operativeQuantity;
  }

  private static BigDecimal getOperativeQuantity(JSONObject selectedPEValuesInLine) {
    try {
      return StringUtils.isEmpty(selectedPEValuesInLine.getString("operativeQuantity")) ? null
          : new BigDecimal(selectedPEValuesInLine.getString("operativeQuantity"));
    } catch (JSONException e) {
      log.error("Error getting the Operative Quantity.", e);
      throw new OBException(e);
    }
  }

  public static BigDecimal getOrderQuantity(JSONObject selectedPEValuesInLine) {
    try {
      return StringUtils.isEmpty(selectedPEValuesInLine.getString("orderQuantity")) ? null
          : new BigDecimal(selectedPEValuesInLine.getString("orderQuantity"));
    } catch (JSONException e) {
      log.error("Error getting the Order Quantity.", e);
      throw new OBException(e);
    }
  }

  public static ShipmentInOutLine getShipmentInOutLine(JSONObject selectedPEValuesInLine) {
    ShipmentInOutLine inOutLine = null;
    try {
      String inOutLineId = selectedPEValuesInLine.getString("shipmentInOutLine");
      if (StringUtils.isNotEmpty(inOutLineId)) {
        inOutLine = OBDal.getInstance().get(ShipmentInOutLine.class, inOutLineId);
      }
    } catch (JSONException e) {
      log.error("Error getting the Shipment/Receipt.", e);
      throw new OBException(e);
    }
    return inOutLine;
  }

  public static UOM getAUM(JSONObject selectedPEValuesInLine) {
    UOM aum = null;
    try {
      String aumId = selectedPEValuesInLine.getString("operativeUOM");
      if (StringUtils.isNotEmpty(aumId)) {
        aum = OBDal.getInstance().get(UOM.class, aumId);
      }
    } catch (JSONException e) {
      log.error("Error getting the AUM.", e);
      throw new OBException(e);
    }
    return aum;
  }

  public static boolean isOrderLineWithRelatedShipmentReceiptLines(BaseOBObject line,
      JSONObject selectedPEValuesInLine) {
    try {
      return isOrderLine(line)
          && !((OrderLine) line).getMaterialMgmtShipmentInOutLineList().isEmpty()
          && StringUtils.isEmpty(selectedPEValuesInLine.getString("shipmentInOutLine"));
    } catch (JSONException e) {
      log.error("Error getting is an order line and has related shipment/receipt.", e);
      throw new OBException(e);
    }
  }

  public static boolean isOrderLineOrHasRelatedOrderLine(final boolean isOrderLine,
      final BaseOBObject copiedLine) {
    return isOrderLine || ((ShipmentInOutLine) copiedLine).getSalesOrderLine() != null;
  }
}
