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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.client.kernel.ComponentProvider;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.model.common.invoice.Invoice;
import org.openbravo.model.common.invoice.InvoiceLine;
import org.openbravo.model.common.order.OrderLine;
import org.openbravo.model.materialmgmt.transaction.ShipmentInOutLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CreateLinesFromProcess {
  @Inject
  @Any
  private Instance<CreateLinesFromProcessImplementationInterface> createLinesFromProcessHooks;

  private static final Logger log = LoggerFactory.getLogger(CreateLinesFromProcess.class);

  // The class of the objects from the invoice lines will be created
  private Class<? extends BaseOBObject> linesClz;
  private Invoice processingInvoice;
  // Last Line number of the Processing Line
  private Long lastLineNo = 0L;
  private JSONArray selectedLines;

  /**
   * This process copies the selected Lines into the Invoice that is being processed by this same
   * Process
   * <ul>
   * <li>1. Update Invoice and Invoice Line related information</li>
   * <li>2. Copy product and attributes</li>
   * <li>3. Calculate amounts and UOM's</li>
   * <li>4. Calculate Prices based on price list</li>
   * <li>5. Calculate Acc and Def Plan from Product</li>
   * <li>6. Recalculate Taxes</li>
   * </ul>
   * 
   * @param selectedLinesParam
   *          Order/Shipment/Receipt Lines from which the lines are going to be copied
   * @param selectedLinesClz
   *          The class of the lines being copied (Order/Shipment/Receipt)
   * @param currentInvoice
   * @return The number of the lines properly copied
   */
  public int createInvoiceLinesFromDocumentLines(final JSONArray selectedLinesParam,
      Invoice currentInvoice, final Class<? extends BaseOBObject> selectedLinesClz) {

    this.selectedLines = selectedLinesParam;
    // Validate the object class is supported in the process.
    this.linesClz = selectedLinesClz;
    validateLinesClz();

    OBContext.setAdminMode(true);
    try {
      long startTime = System.currentTimeMillis();
      processingInvoice = currentInvoice;
      // Copy all the selected lines
      int createdInvoiceLinesCount = createLinesFromSelectedLines();

      long endTime = System.currentTimeMillis();
      log.debug(String.format("CreateLinesFromProcess: Time taken to complete the process: %d ms",
          (endTime - startTime)));
      return createdInvoiceLinesCount;
    } catch (Exception e) {
      log.error(OBMessageUtils.messageBD("CreateLinesFromError"),
          "Error in CreateLinesFromProcess: ", e);
      throw new OBException(e);
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  private int createLinesFromSelectedLines() {
    // Initialize the line number with the last one in the processing invoice.
    lastLineNo = getLastLineNoOfCurrentInvoice();
    int createdInvoiceLinesCount = 0;
    for (int index = 0; index < selectedLines.length(); index++) {
      JSONObject selectedLine = getPickEditLineValuesInPosition(index);
      BaseOBObject copiedLine = getSelectedLineObject(selectedLine);
      if (CreateLinesFromUtil.isOrderLineWithRelatedShipmentReceiptLines(copiedLine, selectedLine)) {
        processCopiedLineShipmentInOut(copiedLine);
      } else {
        InvoiceLine newInvoiceLine = createLineFromSelectedLineAndRunHooks(copiedLine, selectedLine);
        processingInvoice.getInvoiceLineList().add(newInvoiceLine);
        OBDal.getInstance().save(newInvoiceLine);
        OBDal.getInstance().save(processingInvoice);
        createdInvoiceLinesCount++;
        OBDal.getInstance().flush();
      }
    }
    return createdInvoiceLinesCount;
  }

  /**
   * Return an object instance for the line passed as JSONObject
   * 
   * @param selectedLine
   *          The JSONOBject representing the selected object in the PE
   * @return The object representing the line
   */
  private BaseOBObject getSelectedLineObject(final JSONObject selectedLine) {
    try {
      return OBDal.getInstance().get(linesClz, selectedLine.getString("id"));
    } catch (JSONException e) {
      log.error(OBMessageUtils.messageBD("CreateLinesFromOrderError"),
          "Error in CreateLinesFromProcess when reading a JSONObject", e);
      throw new OBException(e);
    }
  }

  private JSONObject getPickEditLineValuesInPosition(final int index) {
    try {
      return selectedLines.getJSONObject(index);
    } catch (JSONException e) {
      log.error(OBMessageUtils.messageBD("CreateLinesFromOrderError"),
          "Error in CreateLinesFromProcess when reading a JSONObject", e);
      throw new OBException(e);
    }
  }

  /**
   * Validate that the line class is supported by the process. If not then an exception is thrown
   */
  private void validateLinesClz() {
    if (!linesClz.getName().equals(OrderLine.class.getName())
        && !linesClz.getName().equals(ShipmentInOutLine.class.getName())) {
      throw new OBException("CreateLinesFromProccessInvalidDocumentType");
    }
  }

  /**
   * Creates a new invoice line from an existing line
   * 
   * @param copiedLine
   *          The BaseOBObject representing the selected object in the PE
   * @param selectedLine
   *          The JSONOBject representing the selected object in the PE
   * @return The created invoice line
   */
  private InvoiceLine createLineFromSelectedLineAndRunHooks(BaseOBObject copiedLine,
      final JSONObject pickExecuteLineValues) {
    long startTime = System.currentTimeMillis();
    InvoiceLine newInvoiceLine = OBProvider.getInstance().get(InvoiceLine.class);

    // Always increment the lineNo when adding a new invoice line
    newInvoiceLine.setLineNo(nextLineNo());

    // Execute Hooks to perform operations
    executeHooks(pickExecuteLineValues, copiedLine, newInvoiceLine);

    long endTime = System.currentTimeMillis();
    log.debug(String.format(
        "CreateLinesFromProcess: Time taken to copy a line from the previous one: %d ms",
        (endTime - startTime)));

    return newInvoiceLine;
  }

  private Long nextLineNo() {
    lastLineNo = lastLineNo + 10L;
    return lastLineNo;
  }

  private void executeHooks(JSONObject pickExecuteLineValues, final BaseOBObject line,
      InvoiceLine newInvoiceLine) {
    try {
      if (createLinesFromProcessHooks != null) {
        final List<CreateLinesFromProcessImplementationInterface> hooks = new ArrayList<>();
        for (CreateLinesFromProcessImplementationInterface hook : createLinesFromProcessHooks
            .select(new ComponentProvider.Selector(
                CreateLinesFromProcessImplementationInterface.CREATE_LINES_FROM_PROCESS_HOOK_QUALIFIER))) {
          if (hook != null) {
            hooks.add(hook);
          }
        }

        Collections.sort(hooks, new CreateLinesFromHookComparator());
        for (CreateLinesFromProcessImplementationInterface hook : hooks) {
          hook.exec(processingInvoice, pickExecuteLineValues, line, newInvoiceLine);
        }
      }
    } catch (Exception e) {
      log.error("Error in CreateLinesFromProcess executing hooks.", e);
      throw new OBException(e);
    }
  }

  private class CreateLinesFromHookComparator implements
      Comparator<CreateLinesFromProcessImplementationInterface> {
    @Override
    public int compare(CreateLinesFromProcessImplementationInterface a,
        CreateLinesFromProcessImplementationInterface b) {
      return a.getOrder() < b.getOrder() ? -1 : a.getOrder() == b.getOrder() ? 0 : 1;
    }
  }

  /**
   * Returns the max invoice line number defined in the invoice to which the lines are going to be
   * added
   *
   * @return The max invoice line number
   */
  private Long getLastLineNoOfCurrentInvoice() {
    OBCriteria<InvoiceLine> obc = OBDal.getInstance().createCriteria(InvoiceLine.class);
    obc.add(Restrictions.eq(InvoiceLine.PROPERTY_INVOICE, processingInvoice));
    obc.setProjection(Projections.max(InvoiceLine.PROPERTY_LINENO));
    Long lineNumber = 0L;
    obc.setMaxResults(1);
    Object o = obc.uniqueResult();
    if (o != null) {
      lineNumber = (Long) o;
    }
    return lineNumber;
  }

  private void processCopiedLineShipmentInOut(BaseOBObject copiedLine) {

    StringBuilder shipmentHQLQuery = new StringBuilder(" as il");
    shipmentHQLQuery.append(" join il.shipmentReceipt sh");
    shipmentHQLQuery.append(" where il.salesOrderLine.id = :orderLineId");
    shipmentHQLQuery.append("  and sh.processed = :processed");
    shipmentHQLQuery.append("  and sh.documentStatus in ('CO', 'CL')");
    shipmentHQLQuery.append("  and sh.completelyInvoiced = 'N'");

    OBQuery<ShipmentInOutLine> shipmentQuery = OBDal.getInstance().createQuery(
        ShipmentInOutLine.class, shipmentHQLQuery.toString());

    shipmentQuery.setNamedParameter("orderLineId", ((OrderLine) copiedLine).getId());
    shipmentQuery.setNamedParameter("processed", true);

    List<ShipmentInOutLine> shipmentInOutLines = shipmentQuery.list();

    for (ShipmentInOutLine shipmentInOutLine : shipmentInOutLines) {
      selectedLines.put(getShipmentInOutJson((OrderLine) copiedLine, shipmentInOutLine));
    }
  }

  private JSONObject getShipmentInOutJson(OrderLine orderLine, ShipmentInOutLine shipmentInOut) {
    JSONObject line = new JSONObject();

    try {
      line.put("uOM", orderLine.getUOM().getId());
      line.put("uOM$_identifier", orderLine.getUOM().getIdentifier());
      line.put("product", orderLine.getProduct().getId());
      line.put("product$_identifier", orderLine.getProduct().getIdentifier());
      line.put("lineNo", orderLine.getLineNo());
      line.put("orderedQuantity", shipmentInOut.getMovementQuantity().toString());
      line.put("operativeQuantity", shipmentInOut.getOperativeQuantity() == null ? shipmentInOut
          .getMovementQuantity().toString() : shipmentInOut.getOperativeQuantity().toString());
      line.put("id", orderLine.getId());
      line.put("salesOrder", orderLine.getSalesOrder().getId());
      line.put("operativeUOM", shipmentInOut.getOperativeUOM() == null ? shipmentInOut.getUOM()
          .getId() : shipmentInOut.getOperativeUOM().getId());
      line.put("operativeUOM$_identifier", shipmentInOut.getOperativeUOM() == null ? shipmentInOut
          .getUOM().getIdentifier() : shipmentInOut.getOperativeUOM().getIdentifier());
      line.put("orderQuantity", "");

      line.put("shipmentInOutLine", shipmentInOut.getId());
      return line;
    } catch (JSONException e) {
      throw new OBException(e);
    }
  }
}