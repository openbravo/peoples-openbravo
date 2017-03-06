/*
 ************************************************************************************
 * Copyright (C) 2018 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.retail.posterminal.utility;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

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
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.SequenceIdData;
import org.openbravo.materialmgmt.StockUtils;
import org.openbravo.mobile.core.process.JSONPropertyToEntity;
import org.openbravo.mobile.core.utils.OBMOBCUtils;
import org.openbravo.model.common.businesspartner.Location;
import org.openbravo.model.common.enterprise.DocumentType;
import org.openbravo.model.common.enterprise.Locator;
import org.openbravo.model.common.enterprise.Warehouse;
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

public class ShipmentInOut_Utils {

  private final Logger log = Logger.getLogger(ShipmentInOut_Utils.class);

  private Locator binForRetuns = null;

  HashMap<String, DocumentType> shipmentDocTypes = new HashMap<String, DocumentType>();

  // DocumentNo Handlers are used to collect all needed document numbers and create and set
  // them as late in the process as possible
  private static ThreadLocal<List<DocumentNoHandler>> documentNoHandlers = new ThreadLocal<List<DocumentNoHandler>>();

  private static final BigDecimal NEGATIVE_ONE = new BigDecimal(-1);

  @Inject
  @Any
  private Instance<OrderLoaderPreAddShipmentLineHook> preAddShipmentLine;

  private static void addDocumentNoHandler(BaseOBObject bob, Entity entity,
      DocumentType docTypeTarget, DocumentType docType) {
    documentNoHandlers.get().add(new DocumentNoHandler(bob, entity, docTypeTarget, docType));
  }

  public void createShipment(ShipmentInOut shipment, Order order, JSONObject jsonorder,
      boolean useOrderDocumentNoForRelatedDocs) throws JSONException {
    Entity shpEntity = ModelProvider.getInstance().getEntity(ShipmentInOut.class);
    JSONPropertyToEntity.fillBobFromJSON(shpEntity, shipment, jsonorder,
        jsonorder.getLong("timezoneOffset"));
    if (jsonorder.has("id")) {
      ShipmentInOut oldShipment = OBDal.getInstance().get(ShipmentInOut.class,
          jsonorder.getString("id"));
      if (oldShipment == null) {
        shipment.setId(jsonorder.getString("id"));
      } else {
        shipment.setId(SequenceIdData.getUUID());
      }
      shipment.setNewOBObject(true);
    }
    shipment.setDocumentType(getShipmentDocumentType(order.getDocumentType().getId()));

    if (useOrderDocumentNoForRelatedDocs) {
      String docNum = order.getDocumentNo();
      if (order.getMaterialMgmtShipmentInOutList().size() > 0) {
        docNum += "-" + order.getMaterialMgmtShipmentInOutList().size();
      }
      shipment.setDocumentNo(docNum);
    } else {
      addDocumentNoHandler(shipment, shpEntity, null, shipment.getDocumentType());
    }

    if (shipment.getMovementDate() == null) {
      shipment.setMovementDate(order.getOrderDate());
    }
    if (shipment.getAccountingDate() == null) {
      shipment.setAccountingDate(order.getOrderDate());
    }

    shipment.setPartnerAddress(OBDal.getInstance().getProxy(Location.class,
        jsonorder.getJSONObject("bp").getString("shipLocId")));
    shipment.setSalesTransaction(true);
    shipment.setDocumentStatus("CO");
    shipment.setDocumentAction("--");
    shipment.setMovementType("C-");
    shipment.setProcessNow(false);
    shipment.setProcessed(true);
    shipment.setSalesOrder(order);
    shipment.setProcessGoodsJava("--");
  }

  public void createShipmentLines(ShipmentInOut shipment, Order order, JSONObject jsonorder,
      JSONArray orderlines, ArrayList<OrderLine> lineReferences, List<Locator> locatorList)
      throws JSONException {
    int lineNo = 0;
    Locator foundSingleBin = null;
    Entity shplineentity = ModelProvider.getInstance().getEntity(ShipmentInOutLine.class);

    if (locatorList.size() == 1) {
      foundSingleBin = locatorList.get(0);
    }
    for (int i = 0; i < orderlines.length(); i++) {

      OrderLine orderLine = lineReferences.get(i);

      if (!orderLine.isObposIspaid()) {
        continue;
      }

      BigDecimal pendingQty = orderLine.getDeliveredQuantity().abs();
      if (orderlines.getJSONObject(i).has("deliveredQuantity")
          && orderlines.getJSONObject(i).get("deliveredQuantity") != JSONObject.NULL) {
        pendingQty = pendingQty.subtract(new BigDecimal(orderlines.getJSONObject(i).getLong(
            "deliveredQuantity")).abs());
      }
      if (pendingQty.compareTo(BigDecimal.ZERO) != 0) {
        boolean negativeLine = orderLine.getOrderedQuantity().compareTo(BigDecimal.ZERO) < 0;

        final Warehouse warehouse = (orderLine.getWarehouse() != null ? orderLine.getWarehouse()
            : order.getWarehouse());
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
            log.error("An error happened executing hook OrderLoaderPreAddShipmentLineHook for Return action "
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
          addShipmentline(shipment, shplineentity, orderlines.getJSONObject(i), orderLine,
              jsonorder, lineNo, pendingQty.negate(), binForReturn, null, i);
        } else if (useSingleBin && pendingQty.compareTo(BigDecimal.ZERO) > 0) {
          OrderLoaderPreAddShipmentLineHook_Response singleBinHookResponse = null;
          lineNo += 10;

          try {
            singleBinHookResponse = executeOrderLoaderPreAddShipmentLineHook(preAddShipmentLine,
                OrderLoaderPreAddShipmentLineHook_Actions.ACTION_SINGLEBIN,
                orderlines.getJSONObject(i), orderLine, jsonorder, order, foundSingleBin);
          } catch (Exception e) {
            log.error("An error happened executing hook OrderLoaderPreAddShipmentLineHook for SingleBin action"
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
          addShipmentline(shipment, shplineentity, orderlines.getJSONObject(i), orderLine,
              jsonorder, lineNo, pendingQty, foundSingleBin, null, i);
        } else {
          HashMap<String, ShipmentInOutLine> usedBins = new HashMap<String, ShipmentInOutLine>();
          if (pendingQty.compareTo(BigDecimal.ZERO) > 0) {

            String id = callProcessGetStock(orderLine.getId(), orderLine.getClient().getId(),
                orderLine.getOrganization().getId(), orderLine.getProduct().getId(), orderLine
                    .getUOM().getId(), warehouse.getId(),
                orderLine.getAttributeSetValue() != null ? orderLine.getAttributeSetValue().getId()
                    : null, pendingQty, orderLine.getWarehouseRule() != null ? orderLine
                    .getWarehouseRule().getId() : null, null);

            OBCriteria<StockProposed> stockProposed = OBDal.getInstance().createCriteria(
                StockProposed.class);
            stockProposed.add(Restrictions.eq(StockProposed.PROPERTY_PROCESSINSTANCE, id));
            stockProposed.addOrderBy(StockProposed.PROPERTY_PRIORITY, true);

            ScrollableResults bins = stockProposed.scroll(ScrollMode.FORWARD_ONLY);

            try {
              while (pendingQty.compareTo(BigDecimal.ZERO) > 0 && bins.next()) {
                // TODO: Can we safely clear session here?
                StockProposed stock = (StockProposed) bins.get(0);
                BigDecimal qty;
                OrderLoaderPreAddShipmentLineHook_Response standardSaleBinHookResponse = null;

                try {
                  standardSaleBinHookResponse = executeOrderLoaderPreAddShipmentLineHook(
                      preAddShipmentLine,
                      OrderLoaderPreAddShipmentLineHook_Actions.ACTION_STANDARD_SALE,
                      orderlines.getJSONObject(i), orderLine, jsonorder, order, stock
                          .getStorageDetail().getStorageBin());
                } catch (Exception e) {
                  log.error("An error happened executing hook OrderLoaderPreAddShipmentLineHook for SimpleSale action "
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
                    orderlines.getJSONObject(i), orderLine, jsonorder, lineNo, qty, stock
                        .getStorageDetail().getStorageBin(), stock.getStorageDetail()
                        .getAttributeSetValue(), i);

                usedBins.put(stock.getStorageDetail().getStorageBin().getId(), objShipmentLine);

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
              loc = locatorList.get(0);
            }

            try {
              lastAttemptBinHookResponse = executeOrderLoaderPreAddShipmentLineHook(
                  preAddShipmentLine,
                  OrderLoaderPreAddShipmentLineHook_Actions.ACTION_LAST_ATTEMPT,
                  orderlines.getJSONObject(i), orderLine, jsonorder, order, loc);
            } catch (Exception e) {
              log.error("An error happened executing hook OrderLoaderPreAddShipmentLineHook for SimpleSaleLastAttempt action "
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
              objShipmentInOutLine.setMovementQuantity(objShipmentInOutLine.getMovementQuantity()
                  .add(pendingQty));
              OBDal.getInstance().save(objShipmentInOutLine);
            } else {
              addShipmentline(shipment, shplineentity, orderlines.getJSONObject(i), orderLine,
                  jsonorder, lineNo, pendingQty, loc, null, i);
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
      OBPOSApplications posTerminal = OBDal.getInstance().get(OBPOSApplications.class,
          posTerminalId);
      binForRetuns = POSUtils.getBinForReturns(posTerminal);
    }
    return binForRetuns;
  }

  private ShipmentInOutLine addShipmentline(ShipmentInOut shipment, Entity shplineentity,
      JSONObject jsonOrderLine, OrderLine orderLine, JSONObject jsonorder, long lineNo,
      BigDecimal qty, Locator bin, AttributeSetInstance attributeSetInstance, int i)
      throws JSONException {
    String orderOrganizationId = jsonorder.getString("organization");

    ShipmentInOutLine line = OBProvider.getInstance().get(ShipmentInOutLine.class);
    JSONPropertyToEntity.fillBobFromJSON(shplineentity, line, jsonOrderLine,
        jsonorder.getLong("timezoneOffset"));
    JSONPropertyToEntity.fillBobFromJSON(
        ModelProvider.getInstance().getEntity(ShipmentInOutLine.class), line, jsonorder,
        jsonorder.getLong("timezoneOffset"));
    line.setLineNo(lineNo);
    line.setShipmentReceipt(shipment);
    line.setSalesOrderLine(orderLine);

    orderLine.getMaterialMgmtShipmentInOutLineList().add(line);

    line.setMovementQuantity(qty);
    line.setStorageBin(bin);
    if (OBMOBCUtils.isJsonObjectPropertyStringPresentNotNullAndNotEmptyString(jsonOrderLine,
        "attSetInstanceDesc")) {
      line.setAttributeSetValue(AttributesUtils.fetchAttributeSetValue(
          jsonOrderLine.get("attSetInstanceDesc").toString(), jsonOrderLine
              .getJSONObject("product").get("id").toString(), orderOrganizationId));
    } else if (OBMOBCUtils.isJsonObjectPropertyStringPresentNotNullAndNotEmptyString(jsonOrderLine,
        "attributeValue")) {
      line.setAttributeSetValue(AttributesUtils.fetchAttributeSetValue(
          jsonOrderLine.get("attributeValue").toString(), jsonOrderLine.getJSONObject("product")
              .get("id").toString(), orderOrganizationId));
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

  public void handleStock(ShipmentInOut shipment, CallableStatement updateStockStatement) {
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
      updateStockStatement.setString(6, transaction.getAttributeSetValue() != null ? transaction
          .getAttributeSetValue().getId() : null);
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
      updateStockStatement.setBigDecimal(13, transaction.getOrderQuantity() != null ? transaction
          .getOrderQuantity().multiply(NEGATIVE_ONE) : null);

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
        if (hookResponse == null) {
          return null;
        }
        return hookResponse;
      }
    }
    return null;
  }

}
