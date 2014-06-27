/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.retail.posterminal;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.model.Property;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.client.kernel.RequestContext;
import org.openbravo.dal.core.DalUtil;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.erpCommon.utility.SequenceIdData;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.ad.access.InvoiceLineTax;
import org.openbravo.model.ad.access.OrderLineTax;
import org.openbravo.model.common.businesspartner.BusinessPartner;
import org.openbravo.model.common.enterprise.DocumentType;
import org.openbravo.model.common.invoice.Invoice;
import org.openbravo.model.common.invoice.InvoiceLine;
import org.openbravo.model.common.invoice.InvoiceLineOffer;
import org.openbravo.model.common.invoice.InvoiceTax;
import org.openbravo.model.common.order.Order;
import org.openbravo.model.common.order.OrderLine;
import org.openbravo.model.common.order.OrderLineOffer;
import org.openbravo.model.financialmgmt.payment.FIN_OrigPaymentScheduleDetail;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentSchedule;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentScheduleDetail;
import org.openbravo.model.financialmgmt.payment.Fin_OrigPaymentSchedule;
import org.openbravo.model.materialmgmt.transaction.ShipmentInOutLine;
import org.openbravo.service.db.DalConnectionProvider;
import org.openbravo.service.json.JsonConstants;

public class OrderGroupingProcessor {

  private static final Logger log = Logger.getLogger(OrderGroupingProcessor.class);

  public JSONObject groupOrders(OBPOSApplications posTerminal, String cashUpId, Date cashUpDate)
      throws JSONException, SQLException {
    // Obtaining order lines that have been created in current terminal and have not already been
    // reconciled. This query must be kept in sync with the one in CashCloseReport

    String hqlWhereClause = "as line"
        + " where line.salesOrder.obposApplications = :terminal and line.salesOrder.obposAppCashup=:cashUpId and line.deliveredQuantity=line.orderedQuantity and line.orderedQuantity <> 0"
        + " and line.salesOrder.documentType.id in ('"
        + posTerminal.getObposTerminaltype().getDocumentType().getId()
        + "', '"
        + posTerminal.getObposTerminaltype().getDocumentTypeForReturns().getId()
        + "') and not exists (select 1 from OrderLine as ord where invoicedQuantity<>0 and ord.salesOrder = line.salesOrder)"
        + " order by line.businessPartner.id, line.salesOrder.id";

    OBQuery<OrderLine> query = OBDal.getInstance().createQuery(OrderLine.class, hqlWhereClause);
    query.setNamedParameter("terminal", posTerminal);
    query.setNamedParameter("cashUpId", cashUpId);

    long t1 = System.currentTimeMillis();
    ScrollableResults orderLines = query.scroll(ScrollMode.FORWARD_ONLY);
    Invoice invoice = null;
    FIN_PaymentSchedule paymentSchedule = null;
    Fin_OrigPaymentSchedule origPaymentSchedule = null;
    String currentOrderId = "";
    Order currentOrder = null;
    String currentbpId = "";
    BusinessPartner currentBp = null;
    HashMap<String, InvoiceTax> invoiceTaxes = null;
    BigDecimal totalNetAmount = BigDecimal.ZERO;
    List<String> processedOrders = new ArrayList<String>();
    boolean isMultiShipmentLine;
    long lineno = 10;
    long taxLineNo = 0;
    try {
      while (orderLines.next()) {
        long t = System.currentTimeMillis();
        isMultiShipmentLine = false;
        OrderLine orderLine = (OrderLine) orderLines.get(0);
        log.debug("Line id:" + orderLine.getId());

        String orderId = (String) DalUtil.getId(orderLine.getSalesOrder());
        if (!orderId.equals(currentOrderId)
            && !posTerminal.getObposTerminaltype().isGroupingOrders()) {
          // New Order. We need to finish current invoice, and create a new one
          finishInvoice(invoice, totalNetAmount, invoiceTaxes, paymentSchedule,
              origPaymentSchedule, cashUpDate);
          currentOrderId = orderId;
          Order order = OBDal.getInstance().get(Order.class, orderId);
          currentOrder = OBDal.getInstance().get(Order.class, orderId);
          invoice = createNewInvoice(posTerminal, currentOrder, orderLine, cashUpDate);
          paymentSchedule = createNewPaymentSchedule(invoice, cashUpDate);
          if (!posTerminal.getObposTerminaltype().isGroupingOrders()) {

            String language = RequestContext.get().getVariablesSecureApp().getLanguage();
            String description = Utility.messageBD(new DalConnectionProvider(false),
                "OrderDocumentno", language) + ": " + order.getDocumentNo() + "\n";
            invoice.setDescription(description);
          }
          origPaymentSchedule = createOriginalPaymentSchedule(invoice, paymentSchedule);
          invoiceTaxes = new HashMap<String, InvoiceTax>();
          totalNetAmount = BigDecimal.ZERO;
          taxLineNo = 10;
          lineno = 10;
          OBDal.getInstance().save(invoice);
          OBDal.getInstance().save(paymentSchedule);
          OBDal.getInstance().save(origPaymentSchedule);
          OBDal.getInstance().flush();
        }

        String bpId = (String) DalUtil.getId(orderLine.getBusinessPartner());
        if (bpId == null) {
          bpId = (String) DalUtil.getId(orderLine.getSalesOrder().getBusinessPartner());
        }
        if (!bpId.equals(currentbpId) && posTerminal.getObposTerminaltype().isGroupingOrders()) {
          // New business partner. We need to finish current invoice, and create a new one
          finishInvoice(invoice, totalNetAmount, invoiceTaxes, paymentSchedule,
              origPaymentSchedule, cashUpDate);
          currentbpId = bpId;
          currentBp = OBDal.getInstance().get(BusinessPartner.class, bpId);
          invoice = createNewInvoice(posTerminal, currentBp, orderLine, cashUpDate);
          paymentSchedule = createNewPaymentSchedule(invoice, cashUpDate);
          origPaymentSchedule = createOriginalPaymentSchedule(invoice, paymentSchedule);
          invoiceTaxes = new HashMap<String, InvoiceTax>();
          totalNetAmount = BigDecimal.ZERO;
          taxLineNo = 10;
          lineno = 10;
          OBDal.getInstance().save(invoice);
          OBDal.getInstance().save(paymentSchedule);
          OBDal.getInstance().save(origPaymentSchedule);
          OBDal.getInstance().flush();
        }
        List<FIN_PaymentSchedule> finPaymentScheduleList = orderLine.getSalesOrder()
            .getFINPaymentScheduleList();
        if (!processedOrders.contains((String) DalUtil.getId(orderLine.getSalesOrder()))
            && !finPaymentScheduleList.isEmpty()
            && finPaymentScheduleList.get(0).getFINPaymentScheduleDetailOrderPaymentScheduleList()
                .size() > 0) {
          boolean success = processPaymentsFromOrder(invoice, orderLine.getSalesOrder(),
              paymentSchedule, origPaymentSchedule);
          if (!success) {
            continue;
          }
          processedOrders.add((String) DalUtil.getId(orderLine.getSalesOrder()));
          log.debug("processed payment");
        }

        // the line is split in goods shipment lines
        OrderLine[] orderLinesSplittedByShipmentLine = splitOrderLineByShipmentLine(orderLine);
        if (orderLinesSplittedByShipmentLine.length > 1) {
          isMultiShipmentLine = true;
        }
        for (int i = 0; i < orderLinesSplittedByShipmentLine.length; i++) {
          OrderLine olSplitted = orderLinesSplittedByShipmentLine[i];
          InvoiceLine invoiceLine = createInvoiceLine(olSplitted, orderLine, isMultiShipmentLine);
          invoiceLine.setLineNo(lineno);
          lineno += 10;
          invoiceLine.setInvoice(invoice);
          OBDal.getInstance().save(invoiceLine);
          totalNetAmount = totalNetAmount.add(invoiceLine.getLineNetAmount());

          List<InvoiceLineTax> lineTaxes = createInvoiceLineTaxes(olSplitted);
          for (InvoiceLineTax tax : lineTaxes) {
            String taxId = (String) DalUtil.getId(tax.getTax());
            InvoiceTax invoiceTax = null;
            if (invoiceTaxes.containsKey(taxId)) {
              invoiceTax = invoiceTaxes.get(taxId);
            } else {
              invoiceTax = OBProvider.getInstance().get(InvoiceTax.class);
              invoiceTax.setTax(tax.getTax());
              invoiceTax.setTaxableAmount(BigDecimal.ZERO);
              invoiceTax.setTaxAmount(BigDecimal.ZERO);
              invoiceTax.setLineNo(taxLineNo);
              taxLineNo += 10;
              invoiceTaxes.put(taxId, invoiceTax);
            }
            invoiceTax.setTaxableAmount(invoiceTax.getTaxableAmount().add(tax.getTaxableAmount()));
            invoiceTax.setTaxAmount(invoiceTax.getTaxAmount().add(tax.getTaxAmount()));

            tax.setInvoiceLine(invoiceLine);
            tax.setInvoice(invoice);
            invoiceLine.getInvoiceLineTaxList().add(tax);
            invoice.getInvoiceLineTaxList().add(tax);
            OBDal.getInstance().save(tax);
            invoiceLine.setTaxableAmount(invoiceLine.getTaxableAmount() == null ? BigDecimal.ZERO
                : invoiceLine.getTaxableAmount().add(tax.getTaxableAmount()));
          }
          log.debug("Line time: " + (System.currentTimeMillis() - t));
          if (lineno % 500 == 0) {
            OBDal.getInstance().flush();
            OBDal.getInstance().getSession().clear();
            paymentSchedule = OBDal.getInstance().get(FIN_PaymentSchedule.class,
                paymentSchedule.getId());
            origPaymentSchedule = OBDal.getInstance().get(Fin_OrigPaymentSchedule.class,
                origPaymentSchedule.getId());
          }

          // if isMultiShipmentLine then the order line ficticious are deleted
          if (isMultiShipmentLine) {
            OBDal.getInstance().getSession().evict(olSplitted);
          }
        }
      }

    } finally {
      orderLines.close();
    }
    finishInvoice(invoice, totalNetAmount, invoiceTaxes, paymentSchedule, origPaymentSchedule,
        cashUpDate);
    // The commit will be done in ProcessCashClose.java (flush), Transactional process.
    // OBDal.getInstance().getConnection().commit();

    log.info("Total time: " + (System.currentTimeMillis() - t1));
    JSONObject jsonResponse = new JSONObject();
    jsonResponse.put(JsonConstants.RESPONSE_STATUS, JsonConstants.RPCREQUEST_STATUS_SUCCESS);
    return jsonResponse;
  }

  protected FIN_PaymentSchedule createNewPaymentSchedule(Invoice invoice, Date cashUpDate) {
    FIN_PaymentSchedule paymentScheduleInvoice = OBProvider.getInstance().get(
        FIN_PaymentSchedule.class);
    paymentScheduleInvoice.setCurrency(invoice.getCurrency());
    paymentScheduleInvoice.setInvoice(invoice);
    paymentScheduleInvoice.setOrganization(invoice.getOrganization());
    paymentScheduleInvoice.setFinPaymentmethod(invoice.getPaymentMethod());
    paymentScheduleInvoice.setAmount(BigDecimal.ZERO);
    paymentScheduleInvoice.setOutstandingAmount(BigDecimal.ZERO);
    paymentScheduleInvoice.setDueDate(cashUpDate);
    paymentScheduleInvoice.setExpectedDate(cashUpDate);
    if (ModelProvider.getInstance().getEntity(FIN_PaymentSchedule.class).hasProperty("origDueDate")) {
      // This property is checked and set this way to force compatibility with both MP13, MP14
      // and
      // later releases of Openbravo. This property is mandatory and must be set. Check issue
      paymentScheduleInvoice.set("origDueDate", paymentScheduleInvoice.getDueDate());
    }
    paymentScheduleInvoice.setFINPaymentPriority(invoice.getFINPaymentPriority());
    return paymentScheduleInvoice;
  }

  protected Fin_OrigPaymentSchedule createOriginalPaymentSchedule(Invoice invoice,
      FIN_PaymentSchedule paymentScheduleInvoice) {

    Fin_OrigPaymentSchedule origPaymentSchedule = OBProvider.getInstance().get(
        Fin_OrigPaymentSchedule.class);
    origPaymentSchedule.setCurrency(invoice.getCurrency());
    origPaymentSchedule.setInvoice(invoice);
    origPaymentSchedule.setOrganization(invoice.getOrganization());
    origPaymentSchedule.setPaymentMethod(invoice.getPaymentMethod());
    origPaymentSchedule.setAmount(BigDecimal.ZERO);
    origPaymentSchedule.setDueDate(invoice.getOrderDate());
    origPaymentSchedule.setPaymentPriority(paymentScheduleInvoice.getFINPaymentPriority());
    return origPaymentSchedule;

  }

  protected boolean processPaymentsFromOrder(Invoice invoice, Order order,
      FIN_PaymentSchedule paymentScheduleInvoice, Fin_OrigPaymentSchedule originalPaymentSchedule) {
    FIN_PaymentSchedule orderPaymentSchedule = null;
    // In case order is payed using different payment methods, payment schedule list size will be >1
    for (FIN_PaymentSchedule sched : order.getFINPaymentScheduleList()) {
      orderPaymentSchedule = sched;

      FIN_PaymentScheduleDetail paymentScheduleDetail = null;
      for (FIN_PaymentScheduleDetail detail : sched
          .getFINPaymentScheduleDetailOrderPaymentScheduleList()) {
        paymentScheduleDetail = detail;

        paymentScheduleInvoice.getFINPaymentScheduleDetailInvoicePaymentScheduleList().add(
            paymentScheduleDetail);
        paymentScheduleDetail.setInvoicePaymentSchedule(paymentScheduleInvoice);

        paymentScheduleInvoice.setAmount(paymentScheduleInvoice.getAmount().add(
            paymentScheduleDetail.getAmount()));

        FIN_OrigPaymentScheduleDetail origDetail = OBProvider.getInstance().get(
            FIN_OrigPaymentScheduleDetail.class);
        origDetail.setArchivedPaymentPlan(originalPaymentSchedule);
        origDetail.setPaymentScheduleDetail(paymentScheduleDetail);
        origDetail.setAmount(paymentScheduleDetail.getAmount());
        origDetail.setWriteoffAmount(paymentScheduleDetail.getWriteoffAmount());

        OBDal.getInstance().save(origDetail);
      }

      if (paymentScheduleDetail == null) {
        log.error("Couldn't find payment schedule detail for order : " + order.getDocumentNo()
            + ". Ignoring order");
        return false;
      }
    }

    if (orderPaymentSchedule == null) {
      log.error("Couldn't find payment schedule for order: " + order.getDocumentNo()
          + ". Ignoring order");
      return false;
    } else {
      return true;
    }
  }

  protected List<InvoiceLineTax> createInvoiceLineTaxes(OrderLine orderLine) {
    List<InvoiceLineTax> taxes = new ArrayList<InvoiceLineTax>();
    for (OrderLineTax orgTax : orderLine.getOrderLineTaxList()) {
      InvoiceLineTax tax = OBProvider.getInstance().get(InvoiceLineTax.class);
      tax.setTax(orgTax.getTax());
      tax.setTaxableAmount(orgTax.getTaxableAmount());
      tax.setTaxAmount(orgTax.getTaxAmount());
      tax.setRecalculate(true);
      taxes.add(tax);
    }
    return taxes;
  }

  protected InvoiceLine createInvoiceLine(OrderLine orderLine, OrderLine origOrderLine,
      boolean isMultiShipmentLine) {
    InvoiceLine invoiceLine = OBProvider.getInstance().get(InvoiceLine.class);
    copyObject(orderLine, invoiceLine);
    invoiceLine.setTaxableAmount(BigDecimal.ZERO);
    invoiceLine.setInvoicedQuantity(orderLine.getOrderedQuantity());
    invoiceLine.setGrossAmount(orderLine.getLineGrossAmount());
    invoiceLine.setSalesOrderLine(origOrderLine);
    OBDal.getInstance().refresh(origOrderLine);
    origOrderLine.setInvoicedQuantity(origOrderLine.getOrderedQuantity());
    OBDal.getInstance().save(origOrderLine);
    // if isMultiShipmentLine = true then evict is done to delete fictitious orderLine and
    // the setInvoiceQuantity is not saved
    if (isMultiShipmentLine) {
      OBDal.getInstance().flush();
    }

    if (orderLine.getGoodsShipmentLine() != null) {
      invoiceLine.setGoodsShipmentLine(orderLine.getGoodsShipmentLine());
    } else {
      invoiceLine.setGoodsShipmentLine(getShipmentLine(orderLine));
    }

    // Promotions. Loading all together as there shoudn't be many promotions per line
    List<OrderLineOffer> promotions = orderLine.getOrderLineOfferList();
    for (OrderLineOffer orderLinePromotion : promotions) {
      InvoiceLineOffer promotion = OBProvider.getInstance().get(InvoiceLineOffer.class);
      copyObject(orderLinePromotion, promotion);

      promotion.setInvoiceLine(invoiceLine);
      invoiceLine.getInvoiceLineOfferList().add(promotion);
    }

    return invoiceLine;
  }

  private ShipmentInOutLine getShipmentLine(OrderLine orderLine) {
    String hqlWhereClause = "as line where line.salesOrderLine = :orderLine ";
    OBQuery<ShipmentInOutLine> query = OBDal.getInstance().createQuery(ShipmentInOutLine.class,
        hqlWhereClause);
    query.setNamedParameter("orderLine", orderLine);
    query.setMaxResult(1); // it should be a 1:1 relationship
    List<ShipmentInOutLine> result = query.list();
    if (result.size() == 0) {
      return null;
    } else {
      return result.get(0);
    }
  }

  private void copyObject(BaseOBObject sourceObj, BaseOBObject targetObj) {
    Entity sourceEntity = sourceObj.getEntity();
    Entity targetEntity = targetObj.getEntity();
    for (Property p : sourceEntity.getProperties()) {
      if (targetEntity.hasProperty(p.getName()) && !p.isOneToMany() && !p.isId()
          && !p.getName().equals(Entity.COMPUTED_COLUMNS_PROXY_PROPERTY) && !p.isComputedColumn()) {
        targetObj.set(p.getName(), sourceObj.get(p.getName()));
      }
    }

  }

  protected String getInvoiceDocumentNo(DocumentType doctypeTarget, DocumentType doctype) {
    return Utility.getDocumentNo(OBDal.getInstance().getConnection(false),
        new DalConnectionProvider(false), RequestContext.get().getVariablesSecureApp(), "",
        "C_Invoice", doctypeTarget == null ? "" : doctypeTarget.getId(), doctype == null ? ""
            : doctype.getId(), false, true);
  }

  protected Invoice createNewInvoice(OBPOSApplications terminal, BusinessPartner bp,
      OrderLine firstLine, Date cashUpDate) {
    Invoice invoice = OBProvider.getInstance().get(Invoice.class);
    invoice.setBusinessPartner(bp);
    if (bp.getBusinessPartnerLocationList().size() == 0) {
      throw new OBException("No addresses defined for the business partner " + bp.getName());
    }
    invoice.setPartnerAddress(bp.getBusinessPartnerLocationList().get(0));
    invoice.setCurrency(firstLine.getCurrency());
    invoice.setOrganization(terminal.getOrganization());
    invoice.setSalesTransaction(true);
    invoice.setDocumentStatus("CO");
    invoice.setDocumentAction("RE");
    invoice.setAPRMProcessinvoice("RE");
    invoice.setProcessed(true);
    invoice.setPaymentMethod(bp.getPaymentMethod());
    invoice.setPaymentTerms(bp.getPaymentTerms());
    invoice.setDocumentType(terminal.getObposTerminaltype().getDocumentType()
        .getDocumentTypeForInvoice());
    invoice.setTransactionDocument(terminal.getObposTerminaltype().getDocumentType()
        .getDocumentTypeForInvoice());
    invoice.setDocumentNo(getInvoiceDocumentNo(invoice.getTransactionDocument(),
        invoice.getDocumentType()));
    invoice.setAccountingDate(cashUpDate);
    invoice.setInvoiceDate(cashUpDate);
    invoice.setPriceList(firstLine.getSalesOrder().getPriceList());
    return invoice;
  }

  protected Invoice createNewInvoice(OBPOSApplications terminal, Order order, OrderLine firstLine,
      Date cashUpDate) {
    Invoice invoice = OBProvider.getInstance().get(Invoice.class);
    invoice.setBusinessPartner(order.getBusinessPartner());
    if (order.getBusinessPartner().getBusinessPartnerLocationList().size() == 0) {
      throw new OBException("No addresses defined for the business partner "
          + order.getBusinessPartner().getName());
    }
    invoice.setPartnerAddress(order.getBusinessPartner().getBusinessPartnerLocationList().get(0));
    invoice.setCurrency(firstLine.getCurrency());
    invoice.setSalesTransaction(true);
    invoice.setOrganization(terminal.getOrganization());
    invoice.setDocumentStatus("CO");
    invoice.setDocumentAction("RE");
    invoice.setAPRMProcessinvoice("RE");
    invoice.setProcessed(true);
    invoice.setPaymentMethod(order.getBusinessPartner().getPaymentMethod());
    invoice.setPaymentTerms(order.getBusinessPartner().getPaymentTerms());
    invoice.setDocumentType(terminal.getObposTerminaltype().getDocumentType()
        .getDocumentTypeForInvoice());
    invoice.setTransactionDocument(terminal.getObposTerminaltype().getDocumentType()
        .getDocumentTypeForInvoice());
    invoice.setDocumentNo(getInvoiceDocumentNo(invoice.getTransactionDocument(),
        invoice.getDocumentType()));
    invoice.setAccountingDate(cashUpDate);
    invoice.setInvoiceDate(cashUpDate);
    invoice.setPriceList(firstLine.getSalesOrder().getPriceList());
    invoice.setSalesOrder(order);
    return invoice;
  }

  protected void finishInvoice(Invoice oriInvoice, BigDecimal totalNetAmount,
      HashMap<String, InvoiceTax> invoiceTaxes, FIN_PaymentSchedule paymentSchedule,
      Fin_OrigPaymentSchedule origPaymentSchedule, Date cashUpDate) throws SQLException {
    if (oriInvoice == null) {
      return;
    }
    long tf = System.currentTimeMillis();
    Invoice invoice = OBDal.getInstance().get(Invoice.class, oriInvoice.getId());

    OBDal.getInstance().save(invoice);
    BigDecimal grossamount = totalNetAmount;
    for (String taxId : invoiceTaxes.keySet()) {
      InvoiceTax tax = invoiceTaxes.get(taxId);
      tax.setRecalculate(true);
      tax.setInvoice(invoice);
      invoice.getInvoiceTaxList().add(tax);
      OBDal.getInstance().save(tax);
      grossamount = grossamount.add(tax.getTaxAmount());
    }

    BigDecimal totalPaid = BigDecimal.ZERO;
    for (FIN_PaymentScheduleDetail psd : paymentSchedule
        .getFINPaymentScheduleDetailInvoicePaymentScheduleList()) {
      totalPaid = totalPaid.add(psd.getAmount());
    }

    // if the total paid is distinct that grossamount, we should create a new sched detail with the
    // difference
    if (grossamount.compareTo(totalPaid) != 0 && grossamount.compareTo(BigDecimal.ZERO) != 0) {
      FIN_PaymentScheduleDetail newDetail = OBProvider.getInstance().get(
          FIN_PaymentScheduleDetail.class);
      newDetail.setAmount(grossamount.subtract(totalPaid));
      newDetail.setInvoicePaymentSchedule(paymentSchedule);
      paymentSchedule.getFINPaymentScheduleDetailInvoicePaymentScheduleList().add(newDetail);
      paymentSchedule.setOutstandingAmount(grossamount.subtract(totalPaid));

    }
    if (grossamount.compareTo(BigDecimal.ZERO) != 0) {
      totalPaid = BigDecimal.ZERO;
    }

    invoice.setGrandTotalAmount(grossamount);
    invoice.setSummedLineAmount(totalNetAmount);
    invoice.setPaymentComplete(grossamount.compareTo(totalPaid) == 0);
    invoice.setTotalPaid(totalPaid);
    invoice.setPercentageOverdue(new Long(0));
    invoice.setFinalSettlementDate(grossamount.compareTo(totalPaid) == 0 ? cashUpDate : null);
    invoice.setDaysSalesOutstanding(new Long(0));
    invoice.setOutstandingAmount(grossamount.subtract(totalPaid));

    paymentSchedule.setAmount(grossamount);
    paymentSchedule.setPaidAmount(totalPaid);
    origPaymentSchedule.setAmount(grossamount);

    if (grossamount.compareTo(BigDecimal.ZERO) == 0) {
      paymentSchedule.setActive(false);
      OBDal.getInstance().remove(paymentSchedule);
      OBDal.getInstance().remove(origPaymentSchedule);
    }
    // Update customer credit

    BigDecimal total = invoice.getGrandTotalAmount();

    if (!invoice.getCurrency().equals(invoice.getBusinessPartner().getPriceList().getCurrency())) {
      // We need to convert the total taking into account the currency difference
      total = OrderLoader.convertCurrencyInvoice(invoice);
    }

    OBDal.getInstance().flush();
    log.debug("Finishing invoice: " + (System.currentTimeMillis() - tf));
  }

  OrderLine[] splitOrderLineByShipmentLine(OrderLine ol) {
    BigDecimal qtyTotal = ol.getOrderedQuantity();
    // if qtyOrdered is ZERO then the line can not be splitted
    if (qtyTotal.equals(BigDecimal.ZERO)) {
      return new OrderLine[] { ol };
    }

    String hqlWhereClause = "as line where line.salesOrderLine = :orderLine ";
    OBQuery<ShipmentInOutLine> query = OBDal.getInstance().createQuery(ShipmentInOutLine.class,
        hqlWhereClause);
    query.setNamedParameter("orderLine", ol);
    List<ShipmentInOutLine> shipmentLines = query.list();
    int stdPrecision = ol.getSalesOrder().getCurrency().getStandardPrecision().intValue();
    long lineNo = 0;

    // if there is one or none then only one record is returned with the original orderline
    if (shipmentLines.size() < 2) {
      return new OrderLine[] { ol };
    } else {
      OrderLine[] arrayOlSplit = new OrderLine[shipmentLines.size()];
      for (int i = 0; i < shipmentLines.size(); i++) {
        lineNo += 10;
        BigDecimal ratio = shipmentLines.get(i).getMovementQuantity()
            .divide(qtyTotal, 32, RoundingMode.HALF_UP);
        OrderLine olSplit = OBProvider.getInstance().get(OrderLine.class);
        olSplit = (OrderLine) DalUtil.copy(ol);

        olSplit.setId(SequenceIdData.getUUID());
        olSplit.setOrderedQuantity(shipmentLines.get(i).getMovementQuantity());
        olSplit.setDeliveredQuantity(shipmentLines.get(i).getMovementQuantity());
        olSplit.setGoodsShipmentLine(shipmentLines.get(i));
        olSplit.setInvoicedQuantity(shipmentLines.get(i).getMovementQuantity());
        olSplit.setLineGrossAmount(ol.getGrossUnitPrice().multiply(olSplit.getOrderedQuantity())
            .setScale(stdPrecision, RoundingMode.HALF_UP));
        olSplit.setLineNetAmount(ol.getUnitPrice().multiply(olSplit.getOrderedQuantity())
            .setScale(stdPrecision, RoundingMode.HALF_UP));
        olSplit.setTaxableAmount(ol.getUnitPrice().multiply(olSplit.getOrderedQuantity())
            .setScale(stdPrecision, RoundingMode.HALF_UP));

        olSplit.setLineNo(lineNo);

        for (int j = 0; j < olSplit.getOrderLineTaxList().size(); j++) {
          OrderLineTax olt = olSplit.getOrderLineTaxList().get(j);
          olt.setTaxAmount(olt.getTaxAmount().multiply(ratio)
              .setScale(stdPrecision, RoundingMode.HALF_UP));
          olt.setTaxableAmount(olt.getTaxableAmount().multiply(ratio)
              .setScale(stdPrecision, RoundingMode.HALF_UP));
        }

        List<OrderLineOffer> promotions = olSplit.getOrderLineOfferList();
        for (OrderLineOffer olPromotion : promotions) {
          olPromotion.setAdjustedPrice(olPromotion.getAdjustedPrice().multiply(ratio));
          olPromotion.setBaseGrossUnitPrice(olPromotion.getBaseGrossUnitPrice().multiply(ratio));
          olPromotion.setPriceAdjustmentAmt(olPromotion.getPriceAdjustmentAmt().multiply(ratio));
          olPromotion.setTotalAmount(olPromotion.getTotalAmount().multiply(ratio));
        }

        arrayOlSplit[i] = olSplit;
      }
      return arrayOlSplit;
    }
  }

}
