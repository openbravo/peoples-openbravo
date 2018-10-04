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
 * All portions are Copyright (C) 2018 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 *************************************************************************
 */
package org.openbravo.materialmgmt;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.weld.WeldUtils;
import org.openbravo.client.kernel.RequestContext;
import org.openbravo.common.actionhandler.createlinesfromprocess.CreateInvoiceLinesFromProcess;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.common.enterprise.DocumentType;
import org.openbravo.model.common.invoice.Invoice;
import org.openbravo.model.common.invoice.InvoiceLine;
import org.openbravo.model.common.order.Order;
import org.openbravo.model.common.order.OrderLine;
import org.openbravo.model.materialmgmt.transaction.ShipmentInOut;
import org.openbravo.model.materialmgmt.transaction.ShipmentInOutLine;
import org.openbravo.service.db.CallStoredProcedure;
import org.openbravo.service.db.DalConnectionProvider;
import org.openbravo.service.db.DbUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to process Goods Shipments
 *
 */
public class ShipmentProcessor {

  private static final String C_INVOICE_TABLE_ID = "318";
  private static final Logger log = LoggerFactory.getLogger(ShipmentProcessor.class);
  private ShipmentInOut shipment;
  Invoice invoice;
  private CreateInvoiceLinesFromProcess createInvoiceLineProcess;

  private static final String AFTER_ORDER_DELIVERY = "O";
  private static final String AFTER_DELIVERY = "D";
  private static final String IMMEDIATE = "I";

  public ShipmentProcessor(String shipmentId) {
    this.shipment = OBDal.getInstance().get(ShipmentInOut.class, shipmentId);
    this.createInvoiceLineProcess = WeldUtils
        .getInstanceFromStaticBeanManager(CreateInvoiceLinesFromProcess.class);
  }

  /**
   * Creates and process an Invoice from Goods Shipment, considering the invoice terms of orders
   * linked to shipment lines.
   * 
   * @return The invoice created
   */
  public Invoice createAndProcessInvoiceConsideringInvoiceTerms() {
    try {
      createInvoiceConsideringInvoiceTerms();
      if (invoice != null) {
        processInvoice();
      }

    } catch (OBException e) {
      executeRollBack();
      throw new OBException(e.getMessage());
    } catch (Exception e1) {
      executeRollBack();
      Throwable e3 = DbUtility.getUnderlyingSQLException(e1);
      throw new OBException(e3);
    }

    return invoice;
  }

  private Invoice createInvoiceConsideringInvoiceTerms() {
    HashSet<String> ordersAlreadyInvoiced = new HashSet<>();
    try (ScrollableResults scrollShipmentLines = getShipmentLines()) {
      while (scrollShipmentLines.next()) {
        final ShipmentInOutLine shipmentLine = OBDal.getInstance().get(ShipmentInOutLine.class,
            scrollShipmentLines.get()[0]);

        final OrderLine orderLine = shipmentLine.getSalesOrderLine();
        boolean shipmentLineIsLinkedToSalesOrderLine = orderLine != null;
        final Order order = shipmentLineIsLinkedToSalesOrderLine ? orderLine.getSalesOrder() : null;
        final String invoiceTerms = order != null ? order.getInvoiceTerms() : null;
        final Long deliveryStatus = order != null ? order.getDeliveryStatus() : null;

        if (AFTER_DELIVERY.equals(invoiceTerms) || IMMEDIATE.equals(invoiceTerms)
            || !shipmentLineIsLinkedToSalesOrderLine) {
          invoiceShimpentLineIfNotYetInvoiced(shipmentLine);

        } else if (AFTER_ORDER_DELIVERY.equals(invoiceTerms) && deliveryStatus == 100
            && !ordersAlreadyInvoiced.contains(order.getId())) {

          inovoiceAllShipmentLinesNotFullyInvoicedLinkedToOrder(order);
          ordersAlreadyInvoiced.add(order.getId());
        }
      }
      OBDal.getInstance().flush();
    }
    return invoice;
  }

  private ScrollableResults getShipmentLines() {
    final String shipmentLinesHQLQuery = "select iol.id " //
        + "from " + ShipmentInOutLine.ENTITY_NAME + " iol " //
        + "where " + ShipmentInOutLine.PROPERTY_SHIPMENTRECEIPT + ".id = :shipmentId ";

    final Session session = OBDal.getInstance().getSession();
    final Query<String> query = session.createQuery(shipmentLinesHQLQuery, String.class);
    query.setParameter("shipmentId", shipment.getId());

    return query.scroll(ScrollMode.FORWARD_ONLY);
  }

  protected void invoiceShimpentLineIfNotYetInvoiced(final ShipmentInOutLine shipmentLine) {
    final BigDecimal totalInvoiced = getTotalInvoicedForShipmentLine(shipmentLine);
    if (totalInvoiced.compareTo(shipmentLine.getMovementQuantity()) != 0) {
      invoiceShipmentLine(shipmentLine);
    }
  }

  private BigDecimal getTotalInvoicedForShipmentLine(final ShipmentInOutLine iol) {
    final String invoiceLinesHqlQuery = "select coalesce(sum(il. "
        + InvoiceLine.PROPERTY_INVOICEDQUANTITY + "), 0) " //
        + "from " + InvoiceLine.ENTITY_NAME + " il " //
        + "where il." + InvoiceLine.PROPERTY_GOODSSHIPMENTLINE + ".id = :shipmentLineId ";

    final Session sessionInvoiceLines = OBDal.getInstance().getSession();
    final Query<BigDecimal> queryInvoiceLines = sessionInvoiceLines.createQuery(
        invoiceLinesHqlQuery, BigDecimal.class);
    queryInvoiceLines.setParameter("shipmentLineId", iol.getId());

    return queryInvoiceLines.uniqueResult();
  }

  private void invoiceShipmentLine(final ShipmentInOutLine shipmentLine) {
    createInvoiceShipmentLine(shipmentLine, shipmentLine.getMovementQuantity());

  }

  private void createInvoiceShipmentLine(final ShipmentInOutLine shipmentLine,
      final BigDecimal invoicedQuantity) {
    createInvoiceLineProcess.createInvoiceLinesFromDocumentLines(
        getShipmentLineToBeInvoiced(shipmentLine, invoicedQuantity), getInvoiceHeader(),
        ShipmentInOutLine.class);
  }

  private JSONArray getShipmentLineToBeInvoiced(final ShipmentInOutLine shipmentInOutLine,
      final BigDecimal invoicedQuantity) {

    final JSONArray lines = new JSONArray();
    try {
      final JSONObject line = new JSONObject();
      line.put("uOM", shipmentInOutLine.getUOM().getId());
      line.put("uOM$_identifier", shipmentInOutLine.getUOM().getIdentifier());
      line.put("product", shipmentInOutLine.getProduct().getId());
      line.put("product$_identifier", shipmentInOutLine.getProduct().getIdentifier());
      line.put("lineNo", shipmentInOutLine.getLineNo());
      line.put("movementQuantity", invoicedQuantity.toString());
      line.put("operativeQuantity",
          shipmentInOutLine.getOperativeQuantity() == null ? shipmentInOutLine
              .getMovementQuantity().toString() : shipmentInOutLine.getOperativeQuantity()
              .toString());
      line.put("id", shipmentInOutLine.getId());
      line.put("operativeUOM", shipmentInOutLine.getOperativeUOM() == null ? shipmentInOutLine
          .getUOM().getId() : shipmentInOutLine.getOperativeUOM().getId());
      line.put("operativeUOM$_identifier",
          shipmentInOutLine.getOperativeUOM() == null ? shipmentInOutLine.getUOM().getIdentifier()
              : shipmentInOutLine.getOperativeUOM().getIdentifier());
      line.put("orderQuantity", "");
      lines.put(line);
    } catch (JSONException e) {
      log.error(e.getMessage());
    }
    return lines;
  }

  private void inovoiceAllShipmentLinesNotFullyInvoicedLinkedToOrder(final Order order) {
    try (ScrollableResults scrollOrderShipmentLines = getShipmentLinesLinkedToASalesOrder(order)) {
      while (scrollOrderShipmentLines.next()) {

        final ShipmentInOutLine iol = OBDal.getInstance().get(ShipmentInOutLine.class,
            (String) scrollOrderShipmentLines.get()[0]);
        final BigDecimal invoicedQuantity = getTotalInvoicedForShipmentLine(iol);
        if (invoicedQuantity.compareTo(iol.getMovementQuantity()) != 0) {
          createInvoiceShipmentLine(iol, iol.getMovementQuantity().subtract(invoicedQuantity));
        }
      }
    }
  }

  private ScrollableResults getShipmentLinesLinkedToASalesOrder(final Order order) {
    final String orderLinesHqlQuery = "select iol.id " //
        + "from " + ShipmentInOutLine.ENTITY_NAME + " iol " //
        + "join iol." + ShipmentInOutLine.PROPERTY_SALESORDERLINE + " ol " //
        + "where ol." + OrderLine.PROPERTY_SALESORDER + ".id = :orderId ";

    final Session sessionOrderLines = OBDal.getInstance().getSession();
    final Query<String> queryOrderLines = sessionOrderLines.createQuery(orderLinesHqlQuery,
        String.class);
    queryOrderLines.setParameter("orderId", order.getId());

    return queryOrderLines.scroll(ScrollMode.FORWARD_ONLY);
  }

  private Invoice getInvoiceHeader() {
    if (invoice == null) {
      invoice = createInvoiceHeader();
    }
    return invoice;
  }

  private Invoice createInvoiceHeader() {
    final Entity invoiceEntity = ModelProvider.getInstance().getEntity(Invoice.class);
    final Invoice newInvoice = OBProvider.getInstance().get(Invoice.class);

    newInvoice.setClient(shipment.getClient());
    newInvoice.setOrganization(shipment.getOrganization());
    final DocumentType invoiceDocumentType = getInvoiceDocumentType();
    newInvoice.setDocumentType(invoiceDocumentType);
    newInvoice.setTransactionDocument(invoiceDocumentType);
    String documentNo = Utility.getDocumentNo(OBDal.getInstance().getConnection(false),
        new DalConnectionProvider(false), RequestContext.get().getVariablesSecureApp(), "",
        invoiceEntity.getTableName(), newInvoice.getTransactionDocument() == null ? "" : newInvoice
            .getTransactionDocument().getId(), newInvoice.getDocumentType() == null ? ""
            : newInvoice.getDocumentType().getId(), false, true);
    newInvoice.setDocumentNo(documentNo);
    newInvoice.setDocumentAction("CO");
    newInvoice.setDocumentStatus("DR");
    newInvoice.setAccountingDate(shipment.getAccountingDate());
    newInvoice.setInvoiceDate(shipment.getMovementDate());
    newInvoice.setTaxDate(shipment.getMovementDate());
    newInvoice.setSalesTransaction(true);
    newInvoice.setBusinessPartner(shipment.getBusinessPartner());
    newInvoice.setPartnerAddress(shipment.getPartnerAddress());
    newInvoice.setPriceList(shipment.getBusinessPartner().getPriceList());
    newInvoice.setCurrency((shipment.getBusinessPartner().getPriceList() == null) ? null : shipment
        .getBusinessPartner().getPriceList().getCurrency());
    newInvoice.setSummedLineAmount(BigDecimal.ZERO);
    newInvoice.setGrandTotalAmount(BigDecimal.ZERO);
    newInvoice.setWithholdingamount(BigDecimal.ZERO);
    newInvoice.setPaymentMethod(shipment.getBusinessPartner().getPaymentMethod());
    newInvoice.setPaymentTerms(shipment.getBusinessPartner().getPaymentTerms());
    OBDal.getInstance().save(newInvoice);
    return newInvoice;
  }

  private DocumentType getInvoiceDocumentType() {
    String hql = "from DocumentType dt where dt.salesTransaction = true" //
        + " and dt.default = true and dt.table.id = :cInvoiceTableId" //
        + " and Ad_Isorgincluded(:invoiceOrgId, dt.organization.id, :clientId) <> -1";

    final Query<DocumentType> query = OBDal.getInstance().getSession()
        .createQuery(hql.toString(), DocumentType.class);
    query.setParameter("cInvoiceTableId", C_INVOICE_TABLE_ID);
    query.setParameter("invoiceOrgId", this.shipment.getOrganization().getId());
    query.setParameter("clientId", this.shipment.getClient().getId());
    query.setMaxResults(1);

    final DocumentType invoiceDocumentType = query.uniqueResult();

    if (invoiceDocumentType == null) {
      throw new OBException("There is no Document type for Sales Invoice defined");
    }
    return invoiceDocumentType;
  }

  private void processInvoice() throws Exception {
    if (invoice != null) {
      final List<Object> parameters = new ArrayList<>();
      parameters.add(null); // Process Instance parameter
      parameters.add(invoice.getId());
      CallStoredProcedure.getInstance().call("C_INVOICE_POST", parameters, null, false, false);
    }
  }

  protected void executeRollBack() {
    try {
      log.error("Error executing creating Invoice");
      OBDal.getInstance().rollbackAndClose();
    } catch (Exception e2) {
      log.error("An error happened when rollback was executed.", e2);
    }
  }
}
