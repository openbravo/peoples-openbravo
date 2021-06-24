/*
 ************************************************************************************
 * Copyright (C) 2019-2021 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.retail.posterminal.process;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.Dependent;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.ScrollableResults;
import org.openbravo.advpaymentmngt.utility.FIN_Utility;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.util.Check;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.businessUtility.Preferences;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.erpCommon.utility.PropertyException;
import org.openbravo.materialmgmt.InvoiceGeneratorFromGoodsShipment;
import org.openbravo.materialmgmt.StockUtils;
import org.openbravo.model.ad.process.ProcessInstance;
import org.openbravo.model.common.businesspartner.BusinessPartner;
import org.openbravo.model.common.enterprise.Locator;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.common.enterprise.Warehouse;
import org.openbravo.model.common.invoice.Invoice;
import org.openbravo.model.common.order.Order;
import org.openbravo.model.common.order.OrderLine;
import org.openbravo.model.common.plm.Product;
import org.openbravo.model.materialmgmt.onhandquantity.StockProposed;
import org.openbravo.model.materialmgmt.transaction.ShipmentInOut;
import org.openbravo.model.materialmgmt.transaction.ShipmentInOutLine;
import org.openbravo.retail.posterminal.OBPOSApplications;
import org.openbravo.retail.posterminal.POSUtils;
import org.openbravo.service.db.CallProcess;

/**
 * Helper class to generate, process and invoice Goods Shipment
 */

@Dependent
class GoodsShipmentGenerator {

  private static final String PROCESS_M_MINOUT_POST = "109";
  private static final String DOCBASETYPE_MATERIAL_SHIPMENT = "MMS";
  private static final Logger log = LogManager.getLogger();

  private ShipmentInOut shipment;
  private long lineNo = 10L;

  /**
   * Creates a new Shipment header
   * 
   * @param organization
   *          The organization
   * @param warehouse
   *          The warehouse
   * @param businessPartner
   *          The Business Partner
   * @param salesOrder
   *          The Sales Order the shipping address will taken from
   * 
   * @return the shipment header
   */
  ShipmentInOut createNewGoodsShipment(final Organization organization, final Warehouse warehouse,
      final BusinessPartner businessPartner, Order salesOrder) {
    this.shipment = OBProvider.getInstance().get(ShipmentInOut.class);
    this.shipment.setNewOBObject(true);
    this.shipment.setClient(organization.getClient());
    this.shipment.setOrganization(organization);
    this.shipment.setTrxOrganization(salesOrder.getTrxOrganization());
    this.shipment.setSalesTransaction(true);
    this.shipment
        .setDocumentType(FIN_Utility.getDocumentType(organization, DOCBASETYPE_MATERIAL_SHIPMENT));
    this.shipment.setDocumentNo(FIN_Utility.getDocumentNo(this.shipment.getDocumentType(),
        this.shipment.getDocumentType().getTable() != null
            ? "DocumentNo_" + this.shipment.getDocumentType().getTable().getDBTableName()
            : ""));
    this.shipment.setWarehouse(warehouse);
    this.shipment.setBusinessPartner(businessPartner);
    this.shipment.setPartnerAddress(salesOrder.getPartnerAddress());
    this.shipment.setMovementDate(Date.valueOf(LocalDate.now()));
    this.shipment.setAccountingDate(Date.valueOf(LocalDate.now()));

    OBDal.getInstance().save(this.shipment);

    return this.shipment;
  }

  /**
   * Creates as many shipment lines as required linked to the shipment header. Generates as many
   * lines as number of bins required to fulfill the quantity to deliver. Those bins are being
   * proposed by StockUtils.getStockProposed
   * 
   * @param product
   *          The product
   * @param qtyToDeliver
   *          The movement quantity
   * @param salesOrderLine
   *          The sales order line
   * @return the shipment line created
   */
  List<ShipmentInOutLine> createShipmentLines(final Product product, final BigDecimal qtyToDeliver,
      final OrderLine salesOrderLine) {
    List<ShipmentInOutLine> result = new ArrayList<>();
    BigDecimal quantityPending = qtyToDeliver;
    ScrollableResults proposedBins = null;
    try {
      boolean allowShipmentWithoutStock = false;
      try {
        allowShipmentWithoutStock = "Y".equals(Preferences.getPreferenceValue(
            "OBPOS_AllowShipmentWithoutStock", true, OBContext.getOBContext().getCurrentClient(),
            OBContext.getOBContext().getCurrentOrganization(), OBContext.getOBContext().getUser(),
            OBContext.getOBContext().getRole(), null));
      } catch (PropertyException e) {
        log.error("Error while getting Preference : " + e.getMessage(), e);
      }

      if (product.isStocked()) {
        proposedBins = StockUtils.getStockProposed(salesOrderLine, qtyToDeliver,
            this.shipment.getWarehouse());
        while (quantityPending.compareTo(BigDecimal.ZERO) > 0 && proposedBins.next()) {
          StockProposed stockProposed = (StockProposed) proposedBins.get(0);
          BigDecimal shipmentlineQty;
          BigDecimal stockProposedQty = stockProposed.getQuantity();
          if (quantityPending.compareTo(stockProposedQty) > 0) {
            shipmentlineQty = stockProposedQty;
            quantityPending = quantityPending.subtract(shipmentlineQty);
          } else {
            shipmentlineQty = quantityPending;
            quantityPending = BigDecimal.ZERO;
          }
          result.add(createShipmentLine(product, shipmentlineQty, salesOrderLine,
              stockProposed.getStorageDetail().getStorageBin()));
        }
        if (salesOrderLine != null && allowShipmentWithoutStock && result.size() == 0
            && quantityPending.compareTo(BigDecimal.ZERO) > 0) {
          final Warehouse warehouse = salesOrderLine.getWarehouse() != null
              ? salesOrderLine.getWarehouse()
              : salesOrderLine.getSalesOrder().getWarehouse();
          final String hqlQuery = " as sb left outer join sb.materialMgmtStorageDetailList ms "
              + "on ms.product.id = :productId where sb.warehouse.id = :warehouseId and sb.active = 'Y' "
              + "and sb.isvirtual = 'N' and (ms.reservedQty is null or ms.reservedQty = 0) "
              + "order by sb.relativePriority";
          Locator locator = OBDal.getInstance()
              .createQuery(Locator.class, hqlQuery)
              .setNamedParameter("warehouseId", warehouse.getId())
              .setNamedParameter("productId", product.getId())
              .setMaxResult(1)
              .uniqueResult();
          if (locator != null) {
            result.add(createShipmentLine(product, qtyToDeliver, salesOrderLine, locator));
          }
        }
      } else {
        result.add(createShipmentLine(product, qtyToDeliver, salesOrderLine, null));
      }
    } finally {
      if (proposedBins != null) {
        proposedBins.close();
      }
    }
    if (result.isEmpty()) {
      throw new OBException(
          String.format(OBMessageUtils.messageBD("OBRDM_UnableToGetStock"), product.getName()));
    }
    return result;
  }

  /**
   * Creates a new shipment line linked to the shipment header
   * 
   * @param product
   *          The product
   * @param quantity
   *          The movement quantity
   * @param salesOrderLine
   *          The sales order line
   * @param bin
   *          The storage bin the item is delivered from
   * @return the shipment line created
   */
  ShipmentInOutLine createShipmentLine(final Product product, final BigDecimal quantity,
      final OrderLine salesOrderLine, Locator bin) {
    Check.isNotNull(this.shipment, "Shipment should not be null");
    Check.isNotNull(product, "Product should not be null");
    Check.isNotNull(salesOrderLine, "Sales Order Line should not be null");
    BigDecimal movementQuantity = quantity;
    if (movementQuantity == null) {
      movementQuantity = BigDecimal.ZERO;
    }
    final ShipmentInOutLine shipmentLine = OBProvider.getInstance().get(ShipmentInOutLine.class);
    shipmentLine.setNewOBObject(true);
    shipmentLine.setLineNo(lineNo);
    shipmentLine.setShipmentReceipt(this.shipment);
    shipmentLine.setOrganization(this.shipment.getOrganization());
    shipmentLine.setClient(this.shipment.getClient());
    shipmentLine.setProduct(product);
    shipmentLine.setMovementQuantity(movementQuantity);
    shipmentLine.setUOM(product.getUOM());
    shipmentLine.setStorageBin(bin);
    shipmentLine.setSalesOrderLine(salesOrderLine);
    shipmentLine.setAttributeSetValue(salesOrderLine.getAttributeSetValue());

    OBDal.getInstance().save(shipmentLine);
    this.shipment.getMaterialMgmtShipmentInOutLineList().add(shipmentLine);
    lineNo += 10;
    return shipmentLine;
  }

  /**
   * Automatically generate invoice from Goods Shipment, if possible
   * 
   * @throws JSONException
   */
  Invoice invoiceShipmentIfPossible(final JSONObject json) throws JSONException {
    if (!json.has("invoiceDocumentNo")) {
      return null;
    }

    OBDal.getInstance().refresh(this.shipment);
    InvoiceGeneratorFromGoodsShipment invoiceGenerator = new InvoiceGeneratorFromGoodsShipment(
        this.shipment.getId(), null, null, json.getString("invoiceDocumentNo"));
    invoiceGenerator.setAllowInvoicePOSOrder(true);
    final Invoice invoice = invoiceGenerator.createInvoiceConsideringInvoiceTerms(true);

    if (invoice != null) {
      final String invoiceSequenceName = json.optString("invoiceSequenceName");
      final Long invoiceSequenceNumber = json.optLong("invoiceSequenceNumber");
      invoice.setObposSequencename(invoiceSequenceName);
      invoice.setObposSequencenumber(invoiceSequenceNumber);
      final OBPOSApplications terminal = OBDal.getInstance()
          .get(OBPOSApplications.class, json.getString("posTerminal"));
      POSUtils.updateTerminalDocumentSequence(terminal, invoiceSequenceName, invoiceSequenceNumber);
    }

    return invoice;
  }

  /**
   * Process the Goods Shipment
   */
  void processShipment() {
    final org.openbravo.model.ad.ui.Process process = OBDal.getInstance()
        .get(org.openbravo.model.ad.ui.Process.class, PROCESS_M_MINOUT_POST);
    final ProcessInstance pinstance = CallProcess.getInstance()
        .call(process, this.shipment.getId(), null);
    final OBError result = OBMessageUtils.getProcessInstanceMessage(pinstance);
    if (StringUtils.equals("Error", result.getType())) {
      throw new OBException(result.getMessage());
    }
  }
}
