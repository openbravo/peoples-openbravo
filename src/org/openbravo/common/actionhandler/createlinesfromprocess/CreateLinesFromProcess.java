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
import org.openbravo.model.common.order.Order;
import org.openbravo.model.common.order.OrderLine;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentSchedule;
import org.openbravo.model.materialmgmt.transaction.ShipmentInOutLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CreateLinesFromProcess {
  @Inject
  @Any
  private Instance<CreateLinesFromProcessImplementationInterface> CreateLinesFromProcessHooks;

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

      // Update invoice prepayment from order lines
      updateInvoicePrepayment();

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
      InvoiceLine newInvoiceLine = createLineFromSelectedLineAndRunHooks(index);
      processingInvoice.getInvoiceLineList().add(newInvoiceLine);
      OBDal.getInstance().save(newInvoiceLine);
      OBDal.getInstance().save(processingInvoice);
      createdInvoiceLinesCount++;
    }
    OBDal.getInstance().flush();
    return createdInvoiceLinesCount;
  }

  /**
   * Return an object instance for the line in an specified index of the selection
   * 
   * @param index
   *          The ID of the selected object in the PE
   * @return The object representing the line
   */
  private BaseOBObject getSelectedLineInPosition(final int index) {
    try {
      String selectedLineId = selectedLines.getJSONObject(index).getString("id");
      return OBDal.getInstance().get(linesClz, selectedLineId);
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
   * @param index
   *          The index of the order/shipment/receipt line to be copied in the selected lines
   * @return The created invoice line
   */
  private InvoiceLine createLineFromSelectedLineAndRunHooks(final int index) {
    long startTime = System.currentTimeMillis();
    BaseOBObject copiedLine = getSelectedLineInPosition(index);
    JSONObject pickExecuteLineValues = getPickEditLineValuesInPosition(index);

    InvoiceLine newInvoiceLine = OBProvider.getInstance().get(InvoiceLine.class);

    // Always increment the lineNo when adding a new order line
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
      if (CreateLinesFromProcessHooks != null) {
        final List<CreateLinesFromProcessImplementationInterface> hooks = new ArrayList<>();
        for (CreateLinesFromProcessImplementationInterface hook : CreateLinesFromProcessHooks
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
    obc.setProjection(Projections.max(OrderLine.PROPERTY_LINENO));
    Long lineNumber = 0L;
    obc.setMaxResults(1);
    Object o = obc.uniqueResult();
    if (o != null) {
      lineNumber = (Long) o;
    }
    return lineNumber;
  }

  /**
   * Update the invoice prepayment with the sum of all the prepayment order amounts
   */
  private void updateInvoicePrepayment() {
    processingInvoice.setPrepaymentamt(getOrdersPrepaymentAmt());
    OBDal.getInstance().save(processingInvoice);
  }

  /**
   * Get the sum of the orders prepayment amount of all the orders related with the invoice
   * 
   * @return The sum of the prepayment amount of related orders
   */
  private BigDecimal getOrdersPrepaymentAmt() {
    final OBCriteria<FIN_PaymentSchedule> obc = OBDal.getInstance().createCriteria(
        FIN_PaymentSchedule.class);

    List<Order> relatedOrdersToInvoiceLines = getRelatedOrdersToInvoiceLines();
    if (relatedOrdersToInvoiceLines.isEmpty()) {
      return BigDecimal.ZERO;
    }
    obc.add(Restrictions.in(FIN_PaymentSchedule.PROPERTY_ORDER, relatedOrdersToInvoiceLines));
    obc.setProjection(Projections.sum(FIN_PaymentSchedule.PROPERTY_PAIDAMOUNT));
    obc.setMaxResults(1);
    BigDecimal paidAmt = (BigDecimal) obc.uniqueResult();
    return paidAmt;
  }

  private List<Order> getRelatedOrdersToInvoiceLines() {
    StringBuffer where = new StringBuffer();
    where.append(" as o");
    where.append(" where exists (");
    where.append("   select 1 from " + InvoiceLine.ENTITY_NAME + " as il");
    where.append("     join il." + InvoiceLine.PROPERTY_SALESORDERLINE + " as ol");
    where.append("   where ol." + OrderLine.PROPERTY_SALESORDER + " = o");
    where.append("     and il." + InvoiceLine.PROPERTY_INVOICE + ".id = :invoiceId)");
    OBQuery<Order> qryOrder = OBDal.getInstance().createQuery(Order.class, where.toString());
    qryOrder.setNamedParameter("invoiceId", processingInvoice.getId());
    return qryOrder.list();
  }
}