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
 * All portions are Copyright (C) 2012 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 *************************************************************************
 */
package org.openbravo.costing;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.session.OBPropertiesProvider;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.ad_forms.AcctServer;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.common.currency.Currency;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.materialmgmt.transaction.MaterialTransaction;
import org.openbravo.model.materialmgmt.transaction.ProductionLine;
import org.openbravo.model.materialmgmt.transaction.ShipmentInOut;
import org.openbravo.model.materialmgmt.transaction.ShipmentInOutLine;
import org.openbravo.model.procurement.POInvoiceMatch;
import org.openbravo.service.db.DalConnectionProvider;

public abstract class CostingAlgorithm {
  protected MaterialTransaction transaction;
  protected HashMap<CostDimension, BaseOBObject> costDimensions;
  protected Organization costOrg;
  protected Currency costCurrency;

  public Currency getCostCurrency() {
    return costCurrency;
  }

  protected static Logger log4j = Logger.getLogger(CostingAlgorithm.class);

  public void init(MaterialTransaction _transaction,
      HashMap<CostDimension, BaseOBObject> _costDimensions) {
    this.transaction = _transaction;
    this.costDimensions = _costDimensions;
    this.costOrg = (Organization) costDimensions.get(CostDimension.LegalEntity);
    if (costOrg == null) {
      // Cost calculated at client level. Records stored in asterisk organization.
      costOrg = OBDal.getInstance().get(Organization.class, "0");
      costCurrency = OBContext.getOBContext().getCurrentClient().getCurrency();
    } else {
      costCurrency = costOrg.getCurrency();
    }
    if (costCurrency == null) {
      costCurrency = transaction.getClient().getCurrency();
    }
  }

  public BigDecimal getTransactionCost() {
    log4j.debug("Get transactions cost.");
    BigDecimal transactionCost = null;
    if (transaction.getMovementQuantity().compareTo(BigDecimal.ZERO) == 0
        && getZeroMovementQtyCost() != null) {
      return getZeroMovementQtyCost();
    }
    if (transaction.getGoodsShipmentLine() != null) {
      // Receipt / Shipment
      ShipmentInOut inout = transaction.getGoodsShipmentLine().getShipmentReceipt();
      if (inout.isSalesTransaction()) {
        // Shipment
        if (inout.getDocumentType().isReversal()) {
          log4j.debug("Reversal shipment: " + transaction.getGoodsShipmentLine().getIdentifier());
          // Reversal shipment. Get cost from original shipment.
          return getShipmentReturnAmount();
        } else {
          log4j.debug("Shipment: " + transaction.getGoodsShipmentLine().getIdentifier());
          return getOutgoingTransactionCost();
        }
      } else {
        // Receipt
        if (inout.getDocumentType().isReversal()) {
          log4j.debug("Reversal Receipt: " + transaction.getGoodsShipmentLine().getIdentifier());
          // Reversal receipt. Create adjustments for original receipt.
          // TODO: pending to implement reversal receipts.
        } else {
          log4j.debug("Receipt: " + transaction.getGoodsShipmentLine().getIdentifier());
          return getReceiptAmount();
        }
      }
    } else if (transaction.getPhysicalInventoryLine() != null) {
      // Physical Inventory
      if (transaction.getMovementQuantity().compareTo(BigDecimal.ZERO) > 0) {
        log4j.debug("Physical inventory, increments stock: "
            + transaction.getPhysicalInventoryLine().getIdentifier());
        // Calculate cost of new stock.
        return getPhysicalInventoryCost();
      } else {
        log4j.debug("Physical inventory, decreases stock "
            + transaction.getPhysicalInventoryLine().getIdentifier());
        return getOutgoingTransactionCost();
      }
    } else if (transaction.getMovementLine() != null) {
      // Internal movement
      if (transaction.getMovementQuantity().compareTo(BigDecimal.ZERO) > 0) {
        log4j.debug("Internal Movement dest: " + transaction.getMovementLine().getIdentifier());
        // Destination movement.
        return getInternalMovementDestinationCost();
      } else {
        log4j.debug("Internal Movement origin " + transaction.getMovementLine().getIdentifier());
        // Origin movement.
        return getOutgoingTransactionCost();
      }
    } else if (transaction.getInternalConsumptionLine() != null) {
      log4j.debug("Internal Consumption: "
          + transaction.getInternalConsumptionLine().getIdentifier());
      // Internal Consumption
      return getOutgoingTransactionCost();
    } else if (transaction.getProductionLine() != null) {
      // Production Line
      if (transaction.getProductionLine().getProductionPlan().getProduction().isSalesTransaction()) {
        // BOM Production
        if (transaction.getMovementQuantity().compareTo(BigDecimal.ZERO) > 0) {
          log4j.debug("Produced BOM product: " + transaction.getProductionLine().getIdentifier());
          // Produced BOM
          return getBOMProductionCost();
        } else {
          log4j.debug("Used BOM Part: " + transaction.getProductionLine().getIdentifier());
          // Used parts
          return getOutgoingTransactionCost();
        }
      } else {
        log4j.debug("Manufacturing Product");
        // Work Effort
        // TODO: Pending to implement manufacturing cost management.
      }
    }

    return transactionCost;
  }

  /**
   * Auxiliary method for transactions with 0 Movement qty. It can be overwritten by Costing
   * Algorithms if further actions are needed.
   * 
   * @return Transaction total cost amount.
   */
  private BigDecimal getZeroMovementQtyCost() {
    return BigDecimal.ZERO;
  }

  /**
   * Calculates the total cost amount of an outgoing transaction.
   * 
   * @return Transaction total cost amount.
   */
  abstract protected BigDecimal getOutgoingTransactionCost();

  /*
   * Default method to get Receipts Transaction amount based on receipt's related order price.
   */
  private BigDecimal getReceiptAmount() {
    BigDecimal trxCost = BigDecimal.ZERO;
    BigDecimal addQty = BigDecimal.ZERO;
    ShipmentInOutLine receiptline = transaction.getGoodsShipmentLine();
    if (receiptline.getShipmentReceipt().getDocumentStatus().equals("VO")
        && transaction.getMovementQuantity().compareTo(BigDecimal.ZERO) == -1) {
      // Receipt that voids another receipt.
      // FIXME: Pending to implement receipt cancellations.
      return null;
    }
    if (receiptline.getSalesOrderLine() == null) {
      // Receipt without order. Returns standard cost.
      // TODO: Review if standard cost is the correct approach.
      return transaction.getMovementQuantity().multiply(
          CostingUtils.getStandardCost(transaction.getProduct(), transaction.getCreationDate(),
              costDimensions));
    }
    for (POInvoiceMatch matchPO : receiptline.getProcurementPOInvoiceMatchList()) {
      addQty = addQty.add(matchPO.getQuantity());
      BigDecimal orderAmt = matchPO.getQuantity().multiply(
          matchPO.getSalesOrderLine().getUnitPrice());
      String pattern = OBPropertiesProvider.getInstance().getOpenbravoProperties()
          .getProperty("dateFormat.java");
      // TODO: Check if conversion is done correctly.
      trxCost = trxCost.add(new BigDecimal(AcctServer.getConvertedAmt(orderAmt.toString(), matchPO
          .getSalesOrderLine().getSalesOrder().getCurrency().getId(), costCurrency.getId(), Utility
          .formatDate(transaction.getCreationDate(), pattern), "", transaction.getClient().getId(),
          costOrg.getId(), new DalConnectionProvider())));
    }
    if (addQty.compareTo(transaction.getMovementQuantity()) != 0) {
      // Quantity related to orders does not match receipts quantity.
      return null;
    }
    return trxCost.setScale(costCurrency.getCostingPrecision().intValue(), RoundingMode.HALF_UP);
  }

  private BigDecimal getShipmentReturnAmount() {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * Calculates the total cost amount of a physical inventory that results on an increment of stock.
   * Default behavior is to calculate the cost based on the Standard Cost defined for the product.
   * 
   * This method can be overridden by Costing Algorithms.
   * 
   * @return
   */
  private BigDecimal getPhysicalInventoryCost() {
    // TODO Auto-generated method stub
    return BigDecimal.ZERO;
  }

  private BigDecimal getInternalMovementDestinationCost() {
    // Get transaction of Origin movement to retrieve it's cost.
    for (MaterialTransaction movementTransaction : transaction.getMovementLine()
        .getMaterialMgmtMaterialTransactionList()) {
      if (movementTransaction.getId().equals(transaction.getId())) {
        continue;
      }
      return CostingUtils.getTransactionCost(movementTransaction, transaction.getCreationDate());
    }
    return null;
  }

  private BigDecimal getBOMProductionCost() {
    List<ProductionLine> productionLines = transaction.getProductionLine().getProductionPlan()
        .getManufacturingProductionLineList();
    // Remove produced BOM line.
    productionLines.remove(transaction.getProductionLine());
    BigDecimal totalCost = BigDecimal.ZERO;
    for (ProductionLine prodLine : productionLines) {
      MaterialTransaction partTransaction = prodLine.getMaterialMgmtMaterialTransactionList()
          .get(0);
      // FIXME: review check to know if cost has been calculated
      if (CostingUtils.getTransactionCost(partTransaction, transaction.getCreationDate()) == null) {
        CostingServer transactionCost = new CostingServer(transaction);
        try {
          transactionCost.process();
          totalCost = totalCost.add(transactionCost.getTransactionCost());
        } catch (OBException e) {
          log4j.error(e.getMessage(), e);
        }
      } else {
        // FIXME: using cost stored in M_Transaction
        totalCost = totalCost.add(CostingUtils.getTransactionCost(partTransaction,
            transaction.getCreationDate()));
      }
    }

    return totalCost;
  }

  public enum CostDimension {
    Warehouse, LegalEntity
  }

}
