/*
 ************************************************************************************
 * Copyright (C) 2012-2015 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.retail.posterminal;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.servlet.ServletException;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.Property;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.client.kernel.RequestContext;
import org.openbravo.dal.core.DalUtil;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.erpCommon.utility.SequenceIdData;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.ad.access.InvoiceLineTax;
import org.openbravo.model.ad.access.OrderLineTax;
import org.openbravo.model.common.enterprise.DocumentType;
import org.openbravo.model.common.invoice.Invoice;
import org.openbravo.model.common.invoice.InvoiceLine;
import org.openbravo.model.common.invoice.InvoiceLineOffer;
import org.openbravo.model.common.invoice.InvoiceTax;
import org.openbravo.model.common.order.Order;
import org.openbravo.model.common.order.OrderLine;
import org.openbravo.model.common.order.OrderLineOffer;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentSchedule;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentScheduleDetail;
import org.openbravo.model.materialmgmt.transaction.ShipmentInOutLine;
import org.openbravo.service.db.DalConnectionProvider;
import org.openbravo.service.json.JsonConstants;

public class OrderGroupingProcessor {

  @Inject
  @Any
  private Instance<FinishInvoiceHook> invoiceProcesses;

  private static final Logger log = Logger.getLogger(OrderGroupingProcessor.class);

  /**
   * Creates invoices for the order lines which are part of a cashup. Groups order lines of the same
   * bp in one invoice, depending on the create invoices for order setting in the (
   * {@link TerminalType#isGroupingOrders}).
   * 
   */
  public JSONObject groupOrders(OBPOSApplications posTerminal, String cashUpId, Date currentDate)
      throws JSONException, SQLException, ServletException {
    // Obtaining order lines that have been created in current terminal and have not already been
    // reconciled. This query must be kept in sync with the one in CashCloseReport

    ConnectionProvider conn = new DalConnectionProvider(false);

    long t0 = System.currentTimeMillis();
    long t1, t2, t3, t4, t5, t6, t7, t8, t9, t10;
    final SimpleDateFormat dateFormatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
    final String strUserId = OBContext.getOBContext().getUser().getId();
    final String strCurrentDate = dateFormatter.format(currentDate);
    final String strLang = RequestContext.get().getVariablesSecureApp().getLanguage();
    final OBPOSAppCashup cashUp = OBDal.getInstance().get(OBPOSAppCashup.class, cashUpId);
    final String strInvDescription = String.format(
        OBMessageUtils.messageBD("OBPOS_InvoiceCashupDescription"), cashUp.getIdentifier());

    // Validate Order Business Partner
    OrderGroupingProcessorData[] orderBusinesspartner = OrderGroupingProcessorData
        .selectOrderBusinessPartner(conn, strLang, cashUp.getId());
    if (orderBusinesspartner.length > 0) {
      final List<String> customerErrorList = new ArrayList<String>();
      final String strBPValidation = OBMessageUtils.messageBD("OBPOS_BPValidationOnCashup");
      final String strPaymentMethod = OBMessageUtils.messageBD("OBPOS_LblPaymentMethod");
      final String strPaymentTerm = OBMessageUtils.messageBD("OBPOS_LblPaymentTerm");
      for (OrderGroupingProcessorData businessPartner : orderBusinesspartner) {
        if (StringUtils.isEmpty(businessPartner.cPaymenttermId)) {
          customerErrorList.add(String.format(strBPValidation, strPaymentTerm,
              businessPartner.bpname));
        }
        if (StringUtils.isEmpty(businessPartner.finPaymentmethodId)) {
          customerErrorList.add(String.format(strBPValidation, strPaymentMethod,
              businessPartner.bpname));
        }
      }
      if (customerErrorList.size() > 0) {
        throw new OBException(StringUtils.join(
            customerErrorList.toArray(new String[customerErrorList.size()]), "\n        "));
      }
    }

    // Validate Order Document Type
    OrderGroupingProcessorData[] orderDocumentType = OrderGroupingProcessorData
        .selectOrderDocumentType(conn, cashUp.getId());
    if (orderDocumentType.length > 0) {
      int i = 0;
      final String strBPValidation = OBMessageUtils.messageBD("OBPOS_DocTypeInvValidationOnCashup");
      String[] docType = new String[orderDocumentType.length];
      for (OrderGroupingProcessorData documentType : orderDocumentType) {
        docType[i++] = documentType.doctype;
      }
      throw new OBException(String.format(strBPValidation, StringUtils.join(docType, ", ")));
    }

    // random string is created as random numeric between 0 and 1000000
    Random rnd = new Random();
    final String strExecutionId = "WebPOS_CashUp_" + String.valueOf(rnd.nextInt(1000000));

    if (posTerminal.getObposTerminaltype().isGroupingOrders()) {
      // Extend sql to separate invoices for sales and returns
      final String strSeparateInvoiceForReturnsHeaderParameter1 = posTerminal
          .getObposTerminaltype().isSeparateinvoiceforreturns() ? "tt.c_doctype_id, tt.c_doctyperet_id"
          : "tt.c_doctype_id";
      final String strSeparateInvoiceForReturnsHeaderParameter2 = posTerminal
          .getObposTerminaltype().isSeparateinvoiceforreturns() ? "and o.c_doctype_id = dt.c_doctype_id"
          : "";
      final String strSeparateInvoiceForReturnsLines = posTerminal.getObposTerminaltype()
          .isSeparateinvoiceforreturns() ? "and o.c_doctype_id = dt.c_doctype_id and dt.c_doctypeinvoice_id = i.c_doctype_id"
          : "and tt.c_doctype_id = dt.c_doctype_id";

      // insert invoice headers
      OrderGroupingProcessorData.insertHeaderGrouping(conn, strUserId, strExecutionId,
          strInvDescription, strCurrentDate, cashUpId,
          strSeparateInvoiceForReturnsHeaderParameter1,
          strSeparateInvoiceForReturnsHeaderParameter2);
      t1 = System.currentTimeMillis();
      // insert invoice lines
      OrderGroupingProcessorData.insertLinesGrouping(conn, cashUpId, strExecutionId,
          strSeparateInvoiceForReturnsLines);
      t2 = System.currentTimeMillis();
      OrderGroupingProcessorData.updateQtyOrderLinesGrouping(conn, strExecutionId);
      // insert invoice lines Tax
      OrderGroupingProcessorData.insertTaxLinesGrouping(conn, strExecutionId);
      t3 = System.currentTimeMillis();
      // insert offer lines
      OrderGroupingProcessorData.insertOfferLinesGrouping(conn, strExecutionId);
      t4 = System.currentTimeMillis();
      // insert invoice tax
      OrderGroupingProcessorData.insertInvoiceTaxGrouping(conn, strExecutionId);
    } else {
      // insert invoice headers
      OrderGroupingProcessorData.insertHeaderNoGrouping(conn, strUserId, strExecutionId,
          strInvDescription, strLang, strCurrentDate, cashUpId);
      t1 = System.currentTimeMillis();
      // insert invoice lines
      OrderGroupingProcessorData.insertLinesNoGrouping(conn, cashUpId, strExecutionId);
      t2 = System.currentTimeMillis();
      OrderGroupingProcessorData.updateQtyOrderLinesNoGrouping(conn, strExecutionId);
      // insert invoice lines Tax
      OrderGroupingProcessorData.insertTaxLinesNoGrouping(conn, strExecutionId);
      t3 = System.currentTimeMillis();
      // insert offer lines
      OrderGroupingProcessorData.insertOfferLinesNoGrouping(conn, strExecutionId);
      t4 = System.currentTimeMillis();
      // insert invoice tax
      OrderGroupingProcessorData.insertInvoiceTaxNoGrouping(conn, strExecutionId);
    }

    t5 = System.currentTimeMillis();

    // check if there are orderlines splitted by inoutlines
    OrderGroupingProcessorData[] orderLinesToSplit = OrderGroupingProcessorData
        .selectSplitOrderLines(conn, cashUpId, strInvDescription);

    for (OrderGroupingProcessorData orderLineToSplit : orderLinesToSplit) {
      OrderLine orderLine = OBDal.getInstance().get(OrderLine.class, orderLineToSplit.cOrderlineId);

      OrderLine[] orderLinesSplittedByShipmentLine = splitOrderLineByShipmentLine(orderLine);
      if (orderLinesSplittedByShipmentLine.length > 1) {

        Invoice invoice = null;
        for (int i = 0; i < orderLine.getInvoiceLineList().size(); i++) {
          InvoiceLine oldInvoiceLine = orderLine.getInvoiceLineList().get(i);
          invoice = oldInvoiceLine.getInvoice();
          OBDal.getInstance().remove(oldInvoiceLine);
        }

        Long lineno = (long) 10;
        for (int i = 0; i < orderLinesSplittedByShipmentLine.length; i++) {
          OrderLine olSplitted = orderLinesSplittedByShipmentLine[i];

          InvoiceLine invoiceLine = createInvoiceLine(olSplitted, orderLine);

          OBCriteria<InvoiceLine> obc = OBDal.getInstance().createCriteria(InvoiceLine.class);
          obc.add(Restrictions.eq(InvoiceLine.PROPERTY_INVOICE, invoice));
          obc.setProjection(Projections.max(InvoiceLine.PROPERTY_LINENO));
          Long maxInvoiceLineNo = 0L;
          Object o = obc.uniqueResult();
          if (o != null) {
            maxInvoiceLineNo = (Long) o;
          }

          if (i != 0) {
            maxInvoiceLineNo += 10L;
            lineno = maxInvoiceLineNo;
          }

          invoiceLine.setLineNo(lineno);
          invoiceLine.setInvoice(invoice);

          List<InvoiceLineTax> lineTaxes = createInvoiceLineTaxes(olSplitted);
          for (InvoiceLineTax tax : lineTaxes) {
            tax.setInvoiceLine(invoiceLine);
            tax.setInvoice(invoice);
            invoiceLine.getInvoiceLineTaxList().add(tax);
            invoice.getInvoiceLineTaxList().add(tax);
            invoiceLine.setTaxableAmount(invoiceLine.getTaxableAmount() == null ? BigDecimal.ZERO
                : invoiceLine.getTaxableAmount().add(tax.getTaxableAmount()));
          }
          OBDal.getInstance().save(invoiceLine);
        }
      }
      OBDal.getInstance().flush();
    }

    t6 = System.currentTimeMillis();

    // insert payment schedule
    OrderGroupingProcessorData.insertPaymentSchedule(conn, strCurrentDate, strExecutionId);

    t7 = System.currentTimeMillis();

    // update payment schedule of orders
    OrderGroupingProcessorData[] arrayOrderAndInvoiceId = OrderGroupingProcessorData
        .selectOrderAndInvoiceId(conn, strExecutionId);

    for (OrderGroupingProcessorData orderAndInvoiceId : arrayOrderAndInvoiceId) {
      Order order = OBDal.getInstance().get(Order.class, orderAndInvoiceId.cOrderId);
      Invoice invoice = OBDal.getInstance().get(Invoice.class, orderAndInvoiceId.cInvoiceId);

      List<FIN_PaymentSchedule> finPaymentScheduleList = order.getFINPaymentScheduleList();
      if (!finPaymentScheduleList.isEmpty()
          && finPaymentScheduleList.get(0).getFINPaymentScheduleDetailOrderPaymentScheduleList()
              .size() > 0) {
        boolean success = processPaymentsFromOrder(order, invoice);
        if (!success) {
          continue;
        }
        log.debug("processed payment");
      }
    }

    t8 = System.currentTimeMillis();

    OrderGroupingProcessorData[] arrayInvoicesId = OrderGroupingProcessorData.selectInvoiceId(conn,
        strExecutionId);

    Invoice invoice = null;
    for (OrderGroupingProcessorData invoiceId : arrayInvoicesId) {
      invoice = OBDal.getInstance().get(Invoice.class, invoiceId.cInvoiceId);
      invoice.setDocumentNo(getInvoiceDocumentNo(invoice.getTransactionDocument(),
          invoice.getDocumentType()));
      finishInvoice(invoice, currentDate);
      executeHooks(invoice, cashUpId);
    }

    t9 = System.currentTimeMillis();

    OBDal.getInstance().flush();

    t10 = System.currentTimeMillis();

    log.debug("time execution query Headers: " + (t1 - t0));
    log.debug("time execution query Lines: " + (t2 - t1));
    log.debug("time execution query Lines Tax: " + (t3 - t2));
    log.debug("time execution query Lines Offers: " + (t4 - t3));
    log.debug("time execution query Tax: " + (t5 - t4));
    log.debug("time execution splitOrderLines: " + (t6 - t5));
    log.debug("time execution query Paym Sched: " + (t7 - t6));
    log.debug("time execution query Upd PS: " + (t8 - t7));
    log.debug("time execution documentNo: " + (t9 - t8));
    log.debug("time execution flush : " + (t10 - t9));
    log.debug("time execution total: " + (t10 - t0));

    log.info("Cash up " + cashUp.getIdentifier() + ": Invoice genarated. Total time: " + (t10 - t0));

    JSONObject jsonResponse = new JSONObject();
    jsonResponse.put(JsonConstants.RESPONSE_STATUS, JsonConstants.RPCREQUEST_STATUS_SUCCESS);
    return jsonResponse;
  }

  private void executeHooks(Invoice invoice, String cashUpId) {
    for (Iterator<FinishInvoiceHook> processIterator = invoiceProcesses.iterator(); processIterator
        .hasNext();) {
      FinishInvoiceHook process = processIterator.next();
      process.exec(invoice, cashUpId);
    }
  }

  private List<InvoiceLineTax> createInvoiceLineTaxes(OrderLine orderLine) {
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

  private InvoiceLine createInvoiceLine(OrderLine orderLine, OrderLine origOrderLine) {
    InvoiceLine invoiceLine = OBProvider.getInstance().get(InvoiceLine.class);
    copyObject(orderLine, invoiceLine);
    invoiceLine.setTaxableAmount(BigDecimal.ZERO);
    invoiceLine.setInvoicedQuantity(orderLine.getOrderedQuantity());
    if (orderLine.getSalesOrder().getPriceList().isPriceIncludesTax()) {
      invoiceLine.setGrossAmount(orderLine.getLineGrossAmount());
    }
    invoiceLine.setSalesOrderLine(origOrderLine);
    origOrderLine.getInvoiceLineList().add(invoiceLine);
    origOrderLine.setInvoicedQuantity(origOrderLine.getOrderedQuantity());

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
      promotion.setCreatedBy(OBContext.getOBContext().getUser());
      promotion.setUpdatedBy(OBContext.getOBContext().getUser());
      promotion.setCreationDate(new Date());
      promotion.setUpdated(new Date());
      invoiceLine.getInvoiceLineOfferList().add(promotion);
    }

    invoiceLine.setCreatedBy(OBContext.getOBContext().getUser());
    invoiceLine.setUpdatedBy(OBContext.getOBContext().getUser());
    invoiceLine.setCreationDate(new Date());
    invoiceLine.setUpdated(new Date());
    return invoiceLine;
  }

  private ShipmentInOutLine getShipmentLine(OrderLine orderLine) {
    List<ShipmentInOutLine> result = orderLine.getMaterialMgmtShipmentInOutLineList();
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

  private String getInvoiceDocumentNo(DocumentType doctypeTarget, DocumentType doctype) {
    return Utility.getDocumentNo(OBDal.getInstance().getConnection(false),
        new DalConnectionProvider(false), RequestContext.get().getVariablesSecureApp(), "",
        "C_Invoice", doctypeTarget == null ? "" : doctypeTarget.getId(), doctype == null ? ""
            : doctype.getId(), false, true);
  }

  private void finishInvoice(Invoice oriInvoice, Date currentDate) throws SQLException {
    if (oriInvoice == null) {
      return;
    }
    Invoice invoice = OBDal.getInstance().get(Invoice.class, oriInvoice.getId());

    OBDal.getInstance().save(invoice);
    BigDecimal grossamount = invoice.getGrandTotalAmount();
    InvoiceTax taxCandidate = null;
    BigDecimal summedLineGross = invoice.getSummedLineAmount();
    for (InvoiceTax tax : invoice.getInvoiceTaxList()) {
      if (taxCandidate == null
          || tax.getTaxAmount().abs().compareTo(taxCandidate.getTaxAmount().abs()) == 1) {
        taxCandidate = tax;
      }
      summedLineGross = summedLineGross.add(tax.getTaxAmount());
    }
    if (summedLineGross.compareTo(invoice.getGrandTotalAmount()) != 0) {
      BigDecimal difference = invoice.getGrandTotalAmount().subtract(summedLineGross);
      taxCandidate.setTaxAmount(taxCandidate.getTaxAmount().add(difference));
      OBDal.getInstance().save(taxCandidate);
    }

    BigDecimal totalPaid = BigDecimal.ZERO;
    FIN_PaymentSchedule ps = invoice.getFINPaymentScheduleList().get(0);
    for (FIN_PaymentScheduleDetail psd : ps.getFINPaymentScheduleDetailInvoicePaymentScheduleList()) {
      totalPaid = totalPaid.add(psd.getAmount());
    }

    // if the total paid is distinct that grossamount, we should create a new sched detail with the
    // difference
    if (grossamount.compareTo(totalPaid) != 0 && grossamount.compareTo(BigDecimal.ZERO) != 0) {
      FIN_PaymentScheduleDetail newDetail = OBProvider.getInstance().get(
          FIN_PaymentScheduleDetail.class);
      newDetail.setAmount(grossamount.subtract(totalPaid));
      newDetail.setInvoicePaymentSchedule(invoice.getFINPaymentScheduleList().get(0));
      invoice.getFINPaymentScheduleList().get(0)
          .getFINPaymentScheduleDetailInvoicePaymentScheduleList().add(newDetail);
      invoice.getFINPaymentScheduleList().get(0)
          .setOutstandingAmount(grossamount.subtract(totalPaid));
    }
    OBDal.getInstance().save(ps);

    if (grossamount.compareTo(BigDecimal.ZERO) == 0) {
      totalPaid = BigDecimal.ZERO;
    }

    invoice.setGrandTotalAmount(grossamount);
    invoice.setPaymentComplete(grossamount.compareTo(totalPaid) == 0);
    invoice.setTotalPaid(totalPaid);
    invoice.setPercentageOverdue(new Long(0));
    invoice.setFinalSettlementDate(grossamount.compareTo(totalPaid) == 0 ? currentDate : null);
    invoice.setDaysSalesOutstanding(new Long(0));
    invoice.setOutstandingAmount(grossamount.subtract(totalPaid));

    ps.setAmount(grossamount);
    ps.setPaidAmount(totalPaid);

    if (grossamount.compareTo(BigDecimal.ZERO) == 0) {
      for (FIN_PaymentScheduleDetail detail : ps
          .getFINPaymentScheduleDetailInvoicePaymentScheduleList()) {
        detail.setInvoicePaymentSchedule(null);
      }
      ps.getFINPaymentScheduleDetailInvoicePaymentScheduleList().clear();
      ps.setActive(false);
      invoice.getFINPaymentScheduleList().clear();
      OBDal.getInstance().remove(ps);
    }
  }

  private OrderLine[] splitOrderLineByShipmentLine(OrderLine ol) {
    BigDecimal qtyTotal = ol.getOrderedQuantity();
    // if qtyOrdered is ZERO then the line can not be splitted
    if (qtyTotal.equals(BigDecimal.ZERO)) {
      return new OrderLine[] { ol };
    }

    List<ShipmentInOutLine> shipmentLines = ol.getMaterialMgmtShipmentInOutLineList();

    int pricePrecision = ol.getSalesOrder().getCurrency().getObposPosprecision() == null ? ol
        .getSalesOrder().getCurrency().getPricePrecision().intValue() : ol.getSalesOrder()
        .getCurrency().getObposPosprecision().intValue();
    long lineNo = 0;

    // if there is one or none then only one record is returned with the original orderline
    if (shipmentLines.size() < 2) {
      return new OrderLine[] { ol };
    } else {
      BigDecimal partialGrossAmount = BigDecimal.ZERO;
      BigDecimal partialLineNetAmount = BigDecimal.ZERO;

      BigDecimal[] partialTaxableAmount = new BigDecimal[ol.getOrderLineTaxList().size()];
      BigDecimal[] partialTaxAmount = new BigDecimal[ol.getOrderLineTaxList().size()];
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
        olSplit.setTaxableAmount(ol.getUnitPrice().multiply(olSplit.getOrderedQuantity())
            .setScale(pricePrecision, RoundingMode.HALF_UP));

        if (shipmentLines.size() > i + 1) {
          olSplit.setLineGrossAmount(ol.getGrossUnitPrice().multiply(olSplit.getOrderedQuantity())
              .setScale(pricePrecision, RoundingMode.HALF_UP));
          olSplit.setLineNetAmount(ol.getUnitPrice().multiply(olSplit.getOrderedQuantity())
              .setScale(pricePrecision, RoundingMode.HALF_UP));
          partialGrossAmount = partialGrossAmount.add(ol.getGrossUnitPrice()
              .multiply(olSplit.getOrderedQuantity())
              .setScale(pricePrecision, RoundingMode.HALF_UP));
          partialLineNetAmount = partialLineNetAmount.add(ol.getUnitPrice()
              .multiply(olSplit.getOrderedQuantity())
              .setScale(pricePrecision, RoundingMode.HALF_UP));
        } else {
          olSplit.setLineNetAmount(ol.getLineNetAmount().subtract(partialLineNetAmount)
              .setScale(pricePrecision, RoundingMode.HALF_UP));
          olSplit.setLineGrossAmount(ol.getLineGrossAmount().subtract(partialGrossAmount)
              .setScale(pricePrecision, RoundingMode.HALF_UP));
        }

        olSplit.setLineNo(lineNo);

        if (shipmentLines.size() > i + 1) {
          for (int j = 0; j < olSplit.getOrderLineTaxList().size(); j++) {
            OrderLineTax olt = olSplit.getOrderLineTaxList().get(j);
            olt.setTaxAmount(olt.getTaxAmount().multiply(ratio)
                .setScale(pricePrecision, RoundingMode.HALF_UP));
            olt.setTaxableAmount(olt.getTaxableAmount().multiply(ratio)
                .setScale(pricePrecision, RoundingMode.HALF_UP));
            // in partialTaxableAmount is added the taxable amount set in the splited lines
            if (partialTaxableAmount[j] == null) {
              partialTaxableAmount[j] = olt.getTaxableAmount();
            } else {
              partialTaxableAmount[j] = partialTaxableAmount[j].add(olt.getTaxableAmount());
            }
            // in partialTaxAmount is added the taxable amount set in the splited lines
            if (partialTaxAmount[j] == null) {
              partialTaxAmount[j] = olt.getTaxAmount();
            } else {
              partialTaxAmount[j] = partialTaxAmount[j].add(olt.getTaxAmount());
            }
          }
        } else {
          // in the last line of splited lines, is set to the pending tax amount and taxable amount
          for (int j = 0; j < olSplit.getOrderLineTaxList().size(); j++) {
            OrderLineTax olt = olSplit.getOrderLineTaxList().get(j);
            olt.setTaxAmount(olt.getTaxAmount().subtract(partialTaxAmount[j])
                .setScale(pricePrecision, RoundingMode.HALF_UP));
            olt.setTaxableAmount(olt.getTaxableAmount().subtract(partialTaxableAmount[j])
                .setScale(pricePrecision, RoundingMode.HALF_UP));
          }
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

  private boolean processPaymentsFromOrder(Order order, Invoice invoice) {
    FIN_PaymentSchedule orderPaymentSchedule = null;
    FIN_PaymentSchedule paymentScheduleInvoice = null;
    // In case order is payed using different payment methods, payment schedule list size will be >1
    for (FIN_PaymentSchedule sched : order.getFINPaymentScheduleList()) {
      paymentScheduleInvoice = invoice.getFINPaymentScheduleList().get(0);

      FIN_PaymentScheduleDetail paymentScheduleDetail = null;
      for (FIN_PaymentScheduleDetail detail : sched
          .getFINPaymentScheduleDetailOrderPaymentScheduleList()) {
        orderPaymentSchedule = sched;
        paymentScheduleDetail = detail;

        paymentScheduleInvoice.getFINPaymentScheduleDetailInvoicePaymentScheduleList().add(
            paymentScheduleDetail);
        paymentScheduleDetail.setInvoicePaymentSchedule(paymentScheduleInvoice);
        paymentScheduleDetail.setInvoicePaid(Boolean.TRUE);

        paymentScheduleInvoice.setAmount(paymentScheduleInvoice.getAmount().add(
            paymentScheduleDetail.getAmount()));
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
}
