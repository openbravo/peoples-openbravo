/*
 ************************************************************************************
 * Copyright (C) 2018-2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.retail.posterminal.utility;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.CallableStatement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.dal.core.DalUtil;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.SequenceIdData;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.materialmgmt.StockUtils;
import org.openbravo.mobile.core.process.JSONPropertyToEntity;
import org.openbravo.mobile.core.utils.OBMOBCUtils;
import org.openbravo.model.ad.access.InvoiceLineTax;
import org.openbravo.model.common.businesspartner.Location;
import org.openbravo.model.common.enterprise.DocumentType;
import org.openbravo.model.common.enterprise.Locator;
import org.openbravo.model.common.enterprise.OrgWarehouse;
import org.openbravo.model.common.enterprise.Warehouse;
import org.openbravo.model.common.invoice.Invoice;
import org.openbravo.model.common.invoice.InvoiceLine;
import org.openbravo.model.common.invoice.InvoiceLineOffer;
import org.openbravo.model.common.order.Order;
import org.openbravo.model.common.order.OrderLine;
import org.openbravo.model.common.plm.AttributeSetInstance;
import org.openbravo.model.materialmgmt.onhandquantity.StockProposed;
import org.openbravo.model.materialmgmt.transaction.MaterialTransaction;
import org.openbravo.model.materialmgmt.transaction.ShipmentInOut;
import org.openbravo.model.materialmgmt.transaction.ShipmentInOutLine;
import org.openbravo.retail.posterminal.OBPOSApplications;
import org.openbravo.retail.posterminal.OrderLoaderPreAddShipmentLineHook;
import org.openbravo.retail.posterminal.OrderLoaderPreAddShipmentLineHook.OrderLoaderPreAddShipmentLineHook_Actions;
import org.openbravo.retail.posterminal.OrderLoaderPreAddShipmentLineHook_Response;
import org.openbravo.retail.posterminal.POSUtils;
import org.openbravo.retail.posterminal.TicketPropertyMapping;
import org.openbravo.service.db.DalConnectionProvider;

public class ShipmentUtils implements TicketPropertyMapping {

  private final Logger log = Logger.getLogger(ShipmentUtils.class);

  private Locator binForRetuns = null;

  HashMap<String, DocumentType> shipmentDocTypes = new HashMap<String, DocumentType>();

  private static final BigDecimal NEGATIVE_ONE = BigDecimal.valueOf(-1);

  @Inject
  @Any
  private Instance<OrderLoaderPreAddShipmentLineHook> preAddShipmentLine;

  public ShipmentInOut createNewShipment(Order order, JSONObject jsonorder,
      ArrayList<OrderLine> lineReferences, List<DocumentNoHandler> docNoHandlers) {
    List<Locator> locatorList = getLocatorList(order.getWarehouse());

    if (locatorList.isEmpty()) {
      throw new OBException(Utility.messageBD(new DalConnectionProvider(false),
          "OBPOS_WarehouseNotStorageBin", OBContext.getOBContext().getLanguage().getLanguage()));
    }

    final ShipmentInOut shipment = OBProvider.getInstance().get(ShipmentInOut.class);
    try {
      final JSONArray orderlines = jsonorder.getJSONArray("lines");
      createShipmentAndLines(jsonorder, shipment, order, orderlines, lineReferences, locatorList,
          docNoHandlers);

      // Stock manipulation
      org.openbravo.database.ConnectionProvider cp = new DalConnectionProvider(false);
      CallableStatement updateStockStatement = cp.getConnection()
          .prepareCall("{call M_UPDATE_INVENTORY (?,?,?,?,?,?,?,?,?,?,?,?,?)}");
      try {
        // Stock manipulation
        handleStock(shipment, updateStockStatement);
      } finally {
        updateStockStatement.close();
      }
    } catch (Exception e) {
      throw new OBException("Error when creating the shipment: ", e);
    }
    return shipment;
  }

  private List<Locator> getLocatorList(Warehouse warehouse) {
    final OBCriteria<Locator> locators = OBDal.getInstance().createCriteria(Locator.class);
    locators.add(Restrictions.eq(Locator.PROPERTY_WAREHOUSE, warehouse));
    locators.add(Restrictions.eqOrIsNull(Locator.PROPERTY_ISVIRTUAL, false));
    locators.addOrderBy(Locator.PROPERTY_RELATIVEPRIORITY, true);
    locators.setFilterOnReadableOrganization(false);
    locators.setMaxResults(2);
    return locators.list();
  }

  private void addDocumentNoHandler(BaseOBObject bob, Entity entity, DocumentType docTypeTarget,
      DocumentType docType, List<DocumentNoHandler> documentNoHandlers) {
    documentNoHandlers.add(new DocumentNoHandler(bob, entity, docTypeTarget, docType));
  }

  private void createShipmentAndLines(final JSONObject jsonorder, final ShipmentInOut shipment,
      final Order order, final JSONArray orderlines, final ArrayList<OrderLine> lineReferences,
      final List<Locator> locatorList, final List<DocumentNoHandler> docNoHandlers)
      throws JSONException {
    createShipment(shipment, order, jsonorder, docNoHandlers);
    OBDal.getInstance().save(shipment);
    createShipmentLines(shipment, order, jsonorder, orderlines, lineReferences, locatorList);

    if (POSUtils.isCrossStore(order, order.getObposApplications())) {
      OBContext.setCrossOrgReferenceAdminMode();
      try {
        OBDal.getInstance().flush();
      } finally {
        OBContext.restorePreviousCrossOrgReferenceMode();
      }
    }
  }

  private void createShipment(ShipmentInOut shipment, Order order, JSONObject jsonorder,
      List<DocumentNoHandler> docNoHandlers) throws JSONException {
    Entity shpEntity = ModelProvider.getInstance().getEntity(ShipmentInOut.class);
    JSONPropertyToEntity.fillBobFromJSON(shpEntity, shipment, jsonorder,
        jsonorder.getLong("timezoneOffset"));
    if (jsonorder.has("id")) {
      ShipmentInOut oldShipment = OBDal.getInstance()
          .get(ShipmentInOut.class, jsonorder.getString("id"));
      if (oldShipment == null) {
        shipment.setId(jsonorder.getString("id"));
      } else {
        shipment.setId(SequenceIdData.getUUID());
      }
      shipment.setNewOBObject(true);
    }
    shipment.setOrganization(order.getOrganization());
    shipment.setTrxOrganization(order.getTrxOrganization());
    shipment.setDocumentType(getShipmentDocumentType(order.getDocumentType().getId()));

    addDocumentNoHandler(shipment, shpEntity, null, shipment.getDocumentType(), docNoHandlers);

    if (shipment.getMovementDate() == null) {
      shipment.setMovementDate(order.getOrderDate());
    }
    if (shipment.getAccountingDate() == null) {
      shipment.setAccountingDate(order.getOrderDate());
    }

    shipment.setPartnerAddress(OBDal.getInstance()
        .getProxy(Location.class, getBusinessPartner(jsonorder).getString("shipLocId")));
    shipment.setSalesTransaction(true);
    shipment.setDocumentStatus("CO");
    shipment.setDocumentAction("--");
    shipment.setMovementType("C-");
    shipment.setProcessNow(false);
    shipment.setProcessed(true);
    shipment.setSalesOrder(order);
    shipment.setProcessGoodsJava("--");
  }

  private void createShipmentLines(ShipmentInOut shipment, Order order, JSONObject jsonorder,
      JSONArray orderlines, ArrayList<OrderLine> lineReferences, List<Locator> locatorList)
      throws JSONException {
    int lineNo = 0;
    Locator foundSingleBin = null;
    Entity shplineentity = ModelProvider.getInstance().getEntity(ShipmentInOutLine.class);

    for (int i = 0; i < orderlines.length(); i++) {

      OrderLine orderLine = lineReferences.get(i);

      if (!orderLine.isObposIspaid()) {
        continue;
      }

      final Warehouse warehouse = orderLine.getWarehouse() != null ? orderLine.getWarehouse()
          : order.getWarehouse();
      List<Locator> lineLocatorList = warehouse.getId().equals(order.getWarehouse().getId())
          ? locatorList
          : getLocatorList(warehouse);
      List<OrgWarehouse> activeOrgWarehouse = order.getOrganization()
          .getOrganizationWarehouseList()
          .stream()
          .filter(s -> s.isActive())
          .collect(Collectors.toList());
      if (activeOrgWarehouse.size() == 1 && lineLocatorList.size() == 1) {
        foundSingleBin = lineLocatorList.get(0);
      }

      final List<ShipmentInOutLine> createdShipmentLines = new ArrayList<>();

      BigDecimal pendingQty = orderLine.getDeliveredQuantity().abs();
      if (orderlines.getJSONObject(i).has("deliveredQuantity")
          && orderlines.getJSONObject(i).get("deliveredQuantity") != JSONObject.NULL) {
        pendingQty = pendingQty.subtract(
            new BigDecimal(orderlines.getJSONObject(i).getLong("deliveredQuantity")).abs());
      }
      if (pendingQty.compareTo(BigDecimal.ZERO) != 0) {
        boolean negativeLine = orderLine.getOrderedQuantity().compareTo(BigDecimal.ZERO) < 0;

        if (!warehouse.equals(shipment.getWarehouse())) {
          shipment.setWarehouse(warehouse);
        }

        boolean useSingleBin = foundSingleBin != null && orderLine.getAttributeSetValue() == null
            && orderLine.getWarehouseRule() == null
            && (order.getWarehouse().getId().equals(warehouse.getId()))
            && orderLine.getProduct().getAttributeSet() == null;

        if (negativeLine && pendingQty.compareTo(BigDecimal.ZERO) > 0) {
          OrderLoaderPreAddShipmentLineHook_Response returnBinHookResponse;
          lineNo += 10;
          Locator binForReturn = null;
          if (orderLine.getWarehouse() != null
              && orderLine.getWarehouse().getReturnlocator() != null) {
            binForReturn = orderLine.getWarehouse().getReturnlocator();
          } else {
            binForReturn = getBinForReturns(jsonorder.getString("posTerminal"));
          }

          try {
            returnBinHookResponse = executeOrderLoaderPreAddShipmentLineHook(preAddShipmentLine,
                OrderLoaderPreAddShipmentLineHook_Actions.ACTION_RETURN,
                orderlines.getJSONObject(i), orderLine, jsonorder, order, binForReturn);
          } catch (Exception e) {
            log.error(
                "An error happened executing hook OrderLoaderPreAddShipmentLineHook for Return action "
                    + e.getMessage());
            returnBinHookResponse = null;
          }
          if (returnBinHookResponse != null) {
            if (!returnBinHookResponse.isValid()) {
              throw new OBException(returnBinHookResponse.getMsg());
            } else if (returnBinHookResponse.getNewLocator() != null) {
              binForReturn = returnBinHookResponse.getNewLocator();
            }
          }
          createdShipmentLines
              .add(addShipmentline(shipment, shplineentity, orderlines.getJSONObject(i), orderLine,
                  jsonorder, lineNo, pendingQty.negate(), binForReturn, null, i));
        } else if (useSingleBin && pendingQty.compareTo(BigDecimal.ZERO) > 0) {
          OrderLoaderPreAddShipmentLineHook_Response singleBinHookResponse = null;
          lineNo += 10;

          try {
            singleBinHookResponse = executeOrderLoaderPreAddShipmentLineHook(preAddShipmentLine,
                OrderLoaderPreAddShipmentLineHook_Actions.ACTION_SINGLEBIN,
                orderlines.getJSONObject(i), orderLine, jsonorder, order, foundSingleBin);
          } catch (Exception e) {
            log.error(
                "An error happened executing hook OrderLoaderPreAddShipmentLineHook for SingleBin action"
                    + e.getMessage());
            singleBinHookResponse = null;
          }
          if (singleBinHookResponse != null) {
            if (!singleBinHookResponse.isValid()) {
              throw new OBException(singleBinHookResponse.getMsg());
            } else if (singleBinHookResponse.getNewLocator() != null) {
              foundSingleBin = singleBinHookResponse.getNewLocator();
            }
          }
          createdShipmentLines
              .add(addShipmentline(shipment, shplineentity, orderlines.getJSONObject(i), orderLine,
                  jsonorder, lineNo, pendingQty, foundSingleBin, null, i));
        } else {
          HashMap<String, ShipmentInOutLine> usedBins = new HashMap<String, ShipmentInOutLine>();
          if (pendingQty.compareTo(BigDecimal.ZERO) > 0) {

            String id = callProcessGetStock(orderLine.getId(), orderLine.getClient().getId(),
                orderLine.getOrganization().getId(), orderLine.getProduct().getId(),
                orderLine.getUOM().getId(), warehouse.getId(),
                orderLine.getAttributeSetValue() != null ? orderLine.getAttributeSetValue().getId()
                    : null,
                pendingQty,
                orderLine.getWarehouseRule() != null ? orderLine.getWarehouseRule().getId() : null,
                null);

            OBCriteria<StockProposed> stockProposed = OBDal.getInstance()
                .createCriteria(StockProposed.class);
            stockProposed.setFilterOnReadableOrganization(false);
            stockProposed.add(Restrictions.eq(StockProposed.PROPERTY_PROCESSINSTANCE, id));
            stockProposed.addOrderBy(StockProposed.PROPERTY_PRIORITY, true);

            ScrollableResults bins = stockProposed.scroll(ScrollMode.FORWARD_ONLY);

            try {
              while (pendingQty.compareTo(BigDecimal.ZERO) > 0 && bins.next()) {
                // TODO: Can we safely clear session here?
                StockProposed stock = (StockProposed) bins.get(0);
                if (!stock.getStorageDetail().getStorageBin().getWarehouse().isActive()) {
                  continue;
                }

                BigDecimal qty;
                OrderLoaderPreAddShipmentLineHook_Response standardSaleBinHookResponse = null;

                try {
                  standardSaleBinHookResponse = executeOrderLoaderPreAddShipmentLineHook(
                      preAddShipmentLine,
                      OrderLoaderPreAddShipmentLineHook_Actions.ACTION_STANDARD_SALE,
                      orderlines.getJSONObject(i), orderLine, jsonorder, order,
                      stock.getStorageDetail().getStorageBin());
                } catch (Exception e) {
                  log.error(
                      "An error happened executing hook OrderLoaderPreAddShipmentLineHook for SimpleSale action "
                          + e.getMessage());
                  standardSaleBinHookResponse = null;
                }
                if (standardSaleBinHookResponse != null) {
                  if (!standardSaleBinHookResponse.isValid()) {
                    if (standardSaleBinHookResponse.isCancelExecution()) {
                      throw new OBException(standardSaleBinHookResponse.getMsg());
                    }
                    continue;
                  }
                }

                Object stockQty = stock.get("quantity");
                if (stockQty instanceof Long) {
                  stockQty = new BigDecimal((Long) stockQty);
                }
                if (pendingQty.compareTo((BigDecimal) stockQty) > 0) {
                  qty = (BigDecimal) stockQty;
                  pendingQty = pendingQty.subtract(qty);
                } else {
                  qty = pendingQty;
                  pendingQty = BigDecimal.ZERO;
                }
                lineNo += 10;
                if (negativeLine) {
                  qty = qty.negate();
                }
                ShipmentInOutLine objShipmentLine = addShipmentline(shipment, shplineentity,
                    orderlines.getJSONObject(i), orderLine, jsonorder, lineNo, qty,
                    stock.getStorageDetail().getStorageBin(),
                    stock.getStorageDetail().getAttributeSetValue(), i);
                createdShipmentLines.add(objShipmentLine);

                usedBins.put(stock.getStorageDetail().getStorageBin().getId(), objShipmentLine);

                if (lineNo == 10 && !stock.getStorageDetail()
                    .getStorageBin()
                    .getWarehouse()
                    .equals(shipment.getWarehouse())) {
                  shipment.setWarehouse(stock.getStorageDetail().getStorageBin().getWarehouse());
                }
              }
            } finally {
              bins.close();
            }
          }

          if (pendingQty.compareTo(BigDecimal.ZERO) != 0) {
            // still qty to ship or return: let's use the bin with highest prio
            OrderLoaderPreAddShipmentLineHook_Response lastAttemptBinHookResponse = null;
            JSONObject jsonorderline = orderlines.getJSONObject(i);
            Locator loc = null;
            if (jsonorderline.has("overissueStoreBin")) {
              loc = OBDal.getInstance().get(Locator.class, jsonorderline.get("overissueStoreBin"));
            } else {
              loc = lineLocatorList.get(0);
            }

            try {
              lastAttemptBinHookResponse = executeOrderLoaderPreAddShipmentLineHook(
                  preAddShipmentLine, OrderLoaderPreAddShipmentLineHook_Actions.ACTION_LAST_ATTEMPT,
                  orderlines.getJSONObject(i), orderLine, jsonorder, order, loc);
            } catch (Exception e) {
              log.error(
                  "An error happened executing hook OrderLoaderPreAddShipmentLineHook for SimpleSaleLastAttempt action "
                      + e.getMessage());
              lastAttemptBinHookResponse = null;
            }
            if (lastAttemptBinHookResponse != null) {
              if (!lastAttemptBinHookResponse.isValid()) {
                throw new OBException(lastAttemptBinHookResponse.getMsg());
              } else if (lastAttemptBinHookResponse.getNewLocator() != null) {
                loc = lastAttemptBinHookResponse.getNewLocator();
              }
            }

            lineNo += 10;
            if (jsonorder.getLong("orderType") == 1) {
              pendingQty = pendingQty.negate();
            }
            ShipmentInOutLine objShipmentInOutLine = usedBins.get(loc.getId());
            if (objShipmentInOutLine != null) {
              objShipmentInOutLine
                  .setMovementQuantity(objShipmentInOutLine.getMovementQuantity().add(pendingQty));
              OBDal.getInstance().save(objShipmentInOutLine);
            } else {
              createdShipmentLines
                  .add(addShipmentline(shipment, shplineentity, orderlines.getJSONObject(i),
                      orderLine, jsonorder, lineNo, pendingQty, loc, null, i));
            }
          }
        }
      }
      if (createdShipmentLines.size() != 0) {
        // Check if there's already an invoice line that is not related to any shipment
        final OBCriteria<InvoiceLine> invoiceLineCriteria = OBDal.getInstance()
            .createCriteria(InvoiceLine.class);
        invoiceLineCriteria.add(Restrictions.eq(InvoiceLine.PROPERTY_SALESORDERLINE, orderLine));
        invoiceLineCriteria.add(Restrictions.isNull(InvoiceLine.PROPERTY_GOODSSHIPMENTLINE));
        final List<InvoiceLine> invoiceLineList = invoiceLineCriteria.list();
        if (invoiceLineList.size() != 0) {
          final int pricePrecision = order.getCurrency().getObposPosprecision() == null
              ? order.getCurrency().getPricePrecision().intValue()
              : order.getCurrency().getObposPosprecision().intValue();
          final Invoice invoice = invoiceLineList.get(0).getInvoice();
          for (final ShipmentInOutLine shipmentLine : createdShipmentLines) {
            BigDecimal qtyToShip = shipmentLine.getMovementQuantity();
            for (final InvoiceLine invoiceLine : invoiceLineList) {
              if (qtyToShip.compareTo(BigDecimal.ZERO) != 1) {
                break;
              }
              final BigDecimal invoicedQty = invoiceLine.getInvoicedQuantity();
              if (qtyToShip.compareTo(invoicedQty) != -1) {
                invoiceLine.setGoodsShipmentLine(shipmentLine);
                OBDal.getInstance().save(invoiceLine);
                qtyToShip = qtyToShip.subtract(invoicedQty);
              } else {
                // The invoice line must be splitted
                final InvoiceLine newInvoiceLine = (InvoiceLine) DalUtil.copy(invoiceLine, false,
                    true);
                invoice.getInvoiceLineList().add(newInvoiceLine);
                invoiceLine.setInvoicedQuantity(qtyToShip);
                invoiceLine.setLineNetAmount(orderLine.getUnitPrice()
                    .multiply(qtyToShip)
                    .setScale(pricePrecision, RoundingMode.HALF_UP));
                invoiceLine.setGrossAmount(orderLine.getGrossUnitPrice()
                    .multiply(qtyToShip)
                    .setScale(pricePrecision, RoundingMode.HALF_UP));
                newInvoiceLine.setLineNo(invoice.getInvoiceLineList().size() * 10L);
                newInvoiceLine.setInvoicedQuantity(invoicedQty.subtract(qtyToShip));
                newInvoiceLine.setLineNetAmount(newInvoiceLine.getLineNetAmount()
                    .subtract(invoiceLine.getLineNetAmount())
                    .setScale(pricePrecision, RoundingMode.HALF_UP));
                newInvoiceLine.setGrossAmount(newInvoiceLine.getGrossAmount()
                    .subtract(invoiceLine.getGrossAmount())
                    .setScale(pricePrecision, RoundingMode.HALF_UP));
                OBDal.getInstance().save(invoiceLine);
                OBDal.getInstance().save(newInvoiceLine);
                // Split the taxes
                for (final InvoiceLineTax invoiceLineTax : invoiceLine.getInvoiceLineTaxList()) {
                  final InvoiceLineTax newInvoiceLineTax = (InvoiceLineTax) DalUtil
                      .copy(invoiceLineTax, false, true);
                  newInvoiceLineTax.setInvoiceLine(newInvoiceLine);
                  newInvoiceLine.getInvoiceLineTaxList().add(newInvoiceLineTax);
                  invoiceLineTax.setTaxableAmount(invoiceLineTax.getTaxableAmount()
                      .divide(invoicedQty, 32, RoundingMode.HALF_UP)
                      .multiply(qtyToShip)
                      .setScale(pricePrecision, RoundingMode.HALF_UP));
                  invoiceLineTax.setTaxAmount(invoiceLineTax.getTaxAmount()
                      .divide(invoicedQty, 32, RoundingMode.HALF_UP)
                      .multiply(qtyToShip)
                      .setScale(pricePrecision, RoundingMode.HALF_UP));
                  newInvoiceLineTax.setTaxableAmount(newInvoiceLineTax.getTaxableAmount()
                      .subtract(invoiceLineTax.getTaxableAmount())
                      .setScale(pricePrecision, RoundingMode.HALF_UP));
                  newInvoiceLineTax.setTaxAmount(newInvoiceLineTax.getTaxAmount()
                      .subtract(invoiceLineTax.getTaxAmount())
                      .setScale(pricePrecision, RoundingMode.HALF_UP));
                  OBDal.getInstance().save(invoiceLineTax);
                  OBDal.getInstance().save(newInvoiceLineTax);
                }
                // Split the discounts
                for (final InvoiceLineOffer invoiceLineOffer : invoiceLine
                    .getInvoiceLineOfferList()) {
                  final InvoiceLineOffer newInvoiceLineOffer = (InvoiceLineOffer) DalUtil
                      .copy(invoiceLineOffer, false, true);
                  newInvoiceLineOffer.setInvoiceLine(newInvoiceLine);
                  newInvoiceLine.getInvoiceLineOfferList().add(newInvoiceLineOffer);
                  invoiceLineOffer.setTotalAmount(invoiceLineOffer.getBaseGrossUnitPrice()
                      .multiply(qtyToShip)
                      .setScale(pricePrecision, RoundingMode.HALF_UP));
                  newInvoiceLineOffer.setTotalAmount(newInvoiceLineOffer.getTotalAmount()
                      .subtract(invoiceLineOffer.getTotalAmount())
                      .setScale(pricePrecision, RoundingMode.HALF_UP));
                  OBDal.getInstance().save(invoiceLineOffer);
                  OBDal.getInstance().save(newInvoiceLineOffer);
                }
                qtyToShip = qtyToShip.subtract(invoicedQty);
              }
            }
          }
        }
      }
    }
  }

  private DocumentType getShipmentDocumentType(String documentTypeId) {
    if (shipmentDocTypes.get(documentTypeId) != null) {
      return shipmentDocTypes.get(documentTypeId);
    }
    DocumentType orderDocType = OBDal.getInstance().get(DocumentType.class, documentTypeId);
    final DocumentType docType = orderDocType.getDocumentTypeForShipment();
    shipmentDocTypes.put(documentTypeId, docType);
    if (docType == null) {
      throw new OBException(
          "There is no 'Document type for Shipment' defined for the specified Document Type. The document type for shipments can be configured in the Document Type window, and it should be configured for the document type: "
              + orderDocType.getName());
    }
    return docType;
  }

  private Locator getBinForReturns(String posTerminalId) {
    if (binForRetuns == null) {
      OBPOSApplications posTerminal = OBDal.getInstance()
          .get(OBPOSApplications.class, posTerminalId);
      binForRetuns = POSUtils.getBinForReturns(posTerminal);
    }
    return binForRetuns;
  }

  private ShipmentInOutLine addShipmentline(ShipmentInOut shipment, Entity shplineentity,
      JSONObject jsonOrderLine, OrderLine orderLine, JSONObject jsonorder, long lineNo,
      BigDecimal qty, Locator bin, AttributeSetInstance attributeSetInstance, int i)
      throws JSONException {

    if (jsonOrderLine.has("description")
        && StringUtils.length(jsonOrderLine.getString("description")) > 255) {
      jsonOrderLine.put("description",
          StringUtils.substring(jsonOrderLine.getString("description"), 0, 255));
    }

    ShipmentInOutLine line = OBProvider.getInstance().get(ShipmentInOutLine.class);
    JSONPropertyToEntity.fillBobFromJSON(shplineentity, line, jsonOrderLine,
        jsonorder.getLong("timezoneOffset"));
    JSONPropertyToEntity.fillBobFromJSON(
        ModelProvider.getInstance().getEntity(ShipmentInOutLine.class), line, jsonorder,
        jsonorder.getLong("timezoneOffset"));
    line.setOrganization(orderLine.getOrganization());
    line.setLineNo(lineNo);
    line.setShipmentReceipt(shipment);
    line.setSalesOrderLine(orderLine);

    orderLine.getMaterialMgmtShipmentInOutLineList().add(line);

    line.setMovementQuantity(qty);
    line.setStorageBin(bin);
    if (OBMOBCUtils.isJsonObjectPropertyStringPresentNotNullAndNotEmptyString(jsonOrderLine,
        "attSetInstanceDesc")) {
      line.setAttributeSetValue(
          AttributesUtils.fetchAttributeSetValue(jsonOrderLine.get("attSetInstanceDesc").toString(),
              jsonOrderLine.getJSONObject("product").get("id").toString(),
              orderLine.getOrganization().getId()));
    } else if (OBMOBCUtils.isJsonObjectPropertyStringPresentNotNullAndNotEmptyString(jsonOrderLine,
        "attributeValue")) {
      line.setAttributeSetValue(
          AttributesUtils.fetchAttributeSetValue(jsonOrderLine.get("attributeValue").toString(),
              jsonOrderLine.getJSONObject("product").get("id").toString(),
              orderLine.getOrganization().getId()));
    } else {
      line.setAttributeSetValue(attributeSetInstance);
    }
    shipment.getMaterialMgmtShipmentInOutLineList().add(line);
    OBDal.getInstance().save(line);
    return line;
  }

  private String callProcessGetStock(String recordID, String clientId, String orgId,
      String productId, String uomId, String warehouseId, String attributesetinstanceId,
      BigDecimal quantity, String warehouseRuleId, String reservationId) {
    String processId = SequenceIdData.getUUID();
    OBContext.setAdminMode(false);
    try {
      if (log.isDebugEnabled()) {
        log.debug("Parameters : '" + processId + "', '" + recordID + "', " + quantity + ", '"
            + productId + "', null, '" + warehouseId + "', null, '" + orgId + "', '"
            + attributesetinstanceId + "', '" + OBContext.getOBContext().getUser().getId() + "', '"
            + clientId + "', '" + warehouseRuleId + "', '" + uomId
            + "', null, null, null, null, null, '" + reservationId + "', 'N'");
      }
      long initGetStockProcedureCall = System.currentTimeMillis();
      StockUtils.getStock(processId, recordID, quantity, productId, null, null, warehouseId, orgId,
          attributesetinstanceId, OBContext.getOBContext().getUser().getId(), clientId,
          warehouseRuleId, uomId, null, null, null, null, null, reservationId, "N");
      long elapsedGetStockProcedureCall = (System.currentTimeMillis() - initGetStockProcedureCall);
      if (log.isDebugEnabled()) {
        log.debug("Partial time to execute callGetStock Procedure Call() : "
            + elapsedGetStockProcedureCall);
      }
      return processId;
    } catch (Exception ex) {
      throw new OBException("Error in OrderLoader when getting stock for product " + productId
          + " order line " + recordID, ex);
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  private void handleStock(ShipmentInOut shipment, CallableStatement updateStockStatement) {
    for (ShipmentInOutLine line : shipment.getMaterialMgmtShipmentInOutLineList()) {
      if (line.getProduct().getProductType().equals("I") && line.getProduct().isStocked()) {
        // Stock is changed only for stocked products of type "Item"
        MaterialTransaction transaction = OBProvider.getInstance().get(MaterialTransaction.class);
        transaction.setOrganization(line.getOrganization());
        transaction.setMovementType(shipment.getMovementType());
        transaction.setProduct(line.getProduct());
        transaction.setStorageBin(line.getStorageBin());
        transaction.setOrderUOM(line.getOrderUOM());
        transaction.setUOM(line.getUOM());
        transaction.setOrderQuantity(line.getOrderQuantity());
        transaction.setMovementQuantity(line.getMovementQuantity().multiply(NEGATIVE_ONE));
        transaction.setMovementDate(shipment.getMovementDate());
        transaction.setGoodsShipmentLine(line);
        transaction.setAttributeSetValue(line.getAttributeSetValue());
        transaction.setId(line.getId());
        transaction.setNewOBObject(true);

        updateInventory(transaction, updateStockStatement);

        OBDal.getInstance().save(transaction);
      }
    }
  }

  private void updateInventory(MaterialTransaction transaction,
      CallableStatement updateStockStatement) {
    try {
      // client
      updateStockStatement.setString(1, OBContext.getOBContext().getCurrentClient().getId());
      // org
      updateStockStatement.setString(2, OBContext.getOBContext().getCurrentOrganization().getId());
      // user
      updateStockStatement.setString(3, OBContext.getOBContext().getUser().getId());
      // product
      updateStockStatement.setString(4, transaction.getProduct().getId());
      // locator
      updateStockStatement.setString(5, transaction.getStorageBin().getId());
      // attributesetinstance
      updateStockStatement.setString(6,
          transaction.getAttributeSetValue() != null ? transaction.getAttributeSetValue().getId()
              : null);
      // uom
      updateStockStatement.setString(7, transaction.getUOM().getId());
      // product uom
      updateStockStatement.setString(8, null);
      // p_qty
      updateStockStatement.setBigDecimal(9,
          transaction.getMovementQuantity() != null ? transaction.getMovementQuantity() : null);
      // p_qtyorder
      updateStockStatement.setBigDecimal(10,
          transaction.getOrderQuantity() != null ? transaction.getOrderQuantity() : null);
      // p_dateLastInventory --- **
      updateStockStatement.setDate(11, null);
      // p_preqty
      updateStockStatement.setBigDecimal(12, BigDecimal.ZERO);
      // p_preqtyorder
      updateStockStatement.setBigDecimal(13,
          transaction.getOrderQuantity() != null
              ? transaction.getOrderQuantity().multiply(NEGATIVE_ONE)
              : null);

      updateStockStatement.execute();

    } catch (Exception e) {
      throw new OBException(e.getMessage(), e);
    }
  }

  private OrderLoaderPreAddShipmentLineHook_Response executeOrderLoaderPreAddShipmentLineHook(
      Instance<? extends Object> hooks, OrderLoaderPreAddShipmentLineHook_Actions action,
      JSONObject jsonorderline, OrderLine orderline, JSONObject jsonorder, Order order, Locator bin)
      throws Exception {
    for (Iterator<? extends Object> procIter = hooks.iterator(); procIter.hasNext();) {
      Object proc = procIter.next();
      if (proc instanceof OrderLoaderPreAddShipmentLineHook) {
        OrderLoaderPreAddShipmentLineHook_Response hookResponse = ((OrderLoaderPreAddShipmentLineHook) proc)
            .exec(action, jsonorderline, orderline, jsonorder, order, bin);
        if (hookResponse != null) {
          return hookResponse;
        }
      }
    }
    return null;
  }

}
