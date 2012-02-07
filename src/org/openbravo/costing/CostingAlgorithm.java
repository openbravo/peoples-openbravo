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
  protected TrxType trxType;
  protected static Logger log4j = Logger.getLogger(CostingAlgorithm.class);

  /**
   * Initializes the instance of the CostingAlgorith with the MaterailTransaction that is being to
   * be calculated and the cost dimensions values in case they have to be used.
   * 
   * It initializes several values: <list><li>Organization, it's used the Legal Entity dimension. If
   * this is null Asterisk organization is used. <li>Currency, it takes the currency defined for the
   * Organization. If this is null it uses the currency defined for the Client. <li>Transaction
   * Type, it calculates its type. </list>
   * 
   * @param _transaction
   *          MaterialTransaction to calculate its cost.
   * @param _costDimensions
   *          Dimension values to calculate the cost based on them. A null value means that the
   *          dimension is not used.
   */
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
    this.trxType = TrxType.getTrxType(this.transaction);
  }

  /**
   * Based on the transaction type, calls the corresponding method to calculate and return the total
   * cost amount of the transaction.
   * 
   * @return the total cost amount of the transaction.
   * @throws OBException
   *           when the transaction type is unknown.
   */
  public BigDecimal getTransactionCost() throws OBException {
    log4j.debug("Get transactions cost.");
    if (transaction.getMovementQuantity().compareTo(BigDecimal.ZERO) == 0
        && getZeroMovementQtyCost() != null) {
      return getZeroMovementQtyCost();
    }
    switch (trxType) {
    case Shipment:
      return getOutgoingTransactionCost();
    case ShipmentReturn:
      return getShipmentReturnAmount();
    case ShipmentVoid:
      return getShipmentVoidAmount();
    case Receipt:
      return getReceiptAmount();
    case ReceiptReturn:
      return getReceiptReturnAmount();
    case ReceiptVoid:
      return getReceiptVoidAmount();
    case InventoryDecrease:
      return getOutgoingTransactionCost();
    case InventoryIncrease:
      return getIncomingPhysicalInventoryCost();
    case IntMovementFrom:
      return getOutgoingTransactionCost();
    case IntMovementTo:
      return getInternalMovementToCost();
    case InternalCons:
      return getOutgoingTransactionCost();
    case BOMPart:
      return getOutgoingTransactionCost();
    case BOMProduct:
      return getBOMProductionCost();
    case Manufacturing:
      // Manufacturing transaction are not implemented.
      return BigDecimal.ZERO;
    case Unknown:
      throw new OBException("@UnknownTrxType@: " + transaction.getIdentifier());
    default:
      throw new OBException("@UnknownTrxType@: " + transaction.getIdentifier());
    }
  }

  /**
   * Calculates the total cost amount of an outgoing transaction.
   */
  abstract protected BigDecimal getOutgoingTransactionCost();

  /**
   * Auxiliary method for transactions with 0 Movement quantity. It can be overwritten by Costing
   * Algorithms if further actions are needed.
   */
  protected BigDecimal getZeroMovementQtyCost() {
    return BigDecimal.ZERO;
  }

  /**
   * Method to calculate cost of Returned Shipments. By default the cost is calculated as a regular
   * receipt, based on the price of the related Return Sales Order.
   */
  protected BigDecimal getShipmentReturnAmount() {
    // Shipment return's cost is calculated as a regular receipt.
    return getReceiptAmount();
  }

  /**
   * Method to calculate the cost of Voided Shipments. By default the cost is calculated getting the
   * cost of the original payment.
   */
  protected BigDecimal getShipmentVoidAmount() {
    // Voided shipment gets cost from original shipment line.
    return getOriginalInOutLineAmount();
  }

  /*
   * Default method to get Receipts Transaction amount based on receipt's related order price.
   */
  protected BigDecimal getReceiptAmount() {
    BigDecimal trxCost = BigDecimal.ZERO;
    BigDecimal addQty = BigDecimal.ZERO;
    ShipmentInOutLine receiptline = transaction.getGoodsShipmentLine();
    if (receiptline.getSalesOrderLine() == null) {
      // Receipt without order. Returns cost based on price list.
      // FIXME: return Price List price
      return transaction.getMovementQuantity().multiply(
          CostingUtils.getStandardCost(transaction.getProduct(), transaction.getCreationDate(),
              costDimensions));
    }
    for (POInvoiceMatch matchPO : receiptline.getProcurementPOInvoiceMatchList()) {
      addQty = addQty.add(matchPO.getQuantity());
      BigDecimal orderAmt = matchPO.getQuantity().multiply(
          matchPO.getSalesOrderLine().getUnitPrice());
      // TODO: Check if conversion is done correctly.
      trxCost = trxCost.add(new BigDecimal(AcctServer.getConvertedAmt(orderAmt.toString(), matchPO
          .getSalesOrderLine().getSalesOrder().getCurrency().getId(), costCurrency.getId(),
          Utility.formatDate(transaction.getCreationDate()), "", transaction.getClient().getId(),
          costOrg.getId(), new DalConnectionProvider())));
    }
    return trxCost.setScale(costCurrency.getCostingPrecision().intValue(), RoundingMode.HALF_UP);
  }

  /**
   * Method to calculate cost of Returned Receipts. By default the cost is calculated as a regular
   * receipt, based on the price of the related Return Sales Order.
   */
  protected BigDecimal getReceiptReturnAmount() {
    // Return's cost is calculated as outgoing transactions.
    return getOutgoingTransactionCost();
  }

  /**
   * Method to calculate the cost of Voided Receipts. By default the cost is calculated getting the
   * cost of the original payment.
   */
  protected BigDecimal getReceiptVoidAmount() {
    // Voided receipt gets cost from original receipt line.
    return getOriginalInOutLineAmount();
  }

  /**
   * Returns the cost of the canceled Shipment/Receipt line on the date it is canceled.
   */
  protected BigDecimal getOriginalInOutLineAmount() {
    if (transaction.getGoodsShipmentLine().getCanceledInoutLine() == null) {
      log4j.error("No canceled line found for transaction: " + transaction.getId());
      throw new OBException("@NoCanceledLineFoundForTrx@ @Transaction@: "
          + transaction.getIdentifier());
    }
    MaterialTransaction origInOutLineTrx = transaction.getGoodsShipmentLine()
        .getCanceledInoutLine().getMaterialMgmtMaterialTransactionList().get(0);

    return CostingUtils.getTransactionCost(origInOutLineTrx, transaction.getCreationDate());
  }

  /**
   * Calculates the total cost amount of a physical inventory that results on an increment of stock.
   * Default behavior is to calculate the cost based on the Standard Cost defined for the product.
   */
  protected BigDecimal getIncomingPhysicalInventoryCost() {
    BigDecimal standardCost = CostingUtils.getStandardCost(transaction.getProduct(),
        transaction.getCreationDate(), costDimensions);
    return transaction.getMovementQuantity().multiply(standardCost);
  }

  /**
   * Calculates the total cost amount of an incoming internal movement. The cost amount is the same
   * than the related outgoing transaction. The outgoing transaction cost is calculated if it has
   * not been yet.
   */
  protected BigDecimal getInternalMovementToCost() {
    // Get transaction of From movement to retrieve it's cost.
    for (MaterialTransaction movementTransaction : transaction.getMovementLine()
        .getMaterialMgmtMaterialTransactionList()) {
      if (movementTransaction.getId().equals(transaction.getId())) {
        continue;
      }
      // Calculate transaction cost if it is not calculated yet.
      return CostingUtils.getTransactionCost(movementTransaction, transaction.getCreationDate(),
          true);
    }
    // If no transaction is found throw an exception.
    throw new OBException("@NoInternalMovementTransactionFound@ @Transaction@: "
        + transaction.getIdentifier());
  }

  /**
   * Calculates the cost of a produced BOM product. Its cost is the sum of the used products
   * transactions costs. If these has not been calculated yet they are calculated.
   */
  protected BigDecimal getBOMProductionCost() {
    List<ProductionLine> productionLines = transaction.getProductionLine().getProductionPlan()
        .getManufacturingProductionLineList();
    // Remove produced BOM line.
    productionLines.remove(transaction.getProductionLine());
    BigDecimal totalCost = BigDecimal.ZERO;
    for (ProductionLine prodLine : productionLines) {
      MaterialTransaction partTransaction = prodLine.getMaterialMgmtMaterialTransactionList()
          .get(0);
      // Calculate transaction cost if it is not calculated yet.
      totalCost = totalCost.add(CostingUtils.getTransactionCost(partTransaction,
          transaction.getCreationDate(), true));
    }

    return totalCost;
  }

  /**
   * @return the base currency used to calculate all the costs.
   */
  public Currency getCostCurrency() {
    return costCurrency;
  }

  /**
   * Dimensions available to manage the cost on an entity.
   */
  public enum CostDimension {
    Warehouse, LegalEntity
  }

  /**
   * Transaction types implemented on the cost engine.
   */
  public enum TrxType {
    Shipment, ShipmentReturn, ShipmentVoid, Receipt, ReceiptReturn, ReceiptVoid, InventoryIncrease, InventoryDecrease, IntMovementFrom, IntMovementTo, InternalCons, BOMPart, BOMProduct, Manufacturing, Unknown;
    /**
     * Given a Material Management transaction returns its type.
     */
    private static TrxType getTrxType(MaterialTransaction transaction) {
      if (transaction.getGoodsShipmentLine() != null) {
        // Receipt / Shipment
        ShipmentInOut inout = transaction.getGoodsShipmentLine().getShipmentReceipt();
        if (inout.isSalesTransaction()) {
          // Shipment
          if (inout.getDocumentType().isReversal()) {
            log4j.debug("Reversal shipment: " + transaction.getGoodsShipmentLine().getIdentifier());
            return ShipmentReturn;
          } else if (inout.getDocumentStatus().equals("VO")
              && transaction.getGoodsShipmentLine().getMovementQuantity()
                  .compareTo(BigDecimal.ZERO) < 0) {
            log4j.debug("Void shipment: " + transaction.getGoodsShipmentLine().getIdentifier());
            return ShipmentVoid;
          } else {
            log4j.debug("Shipment: " + transaction.getGoodsShipmentLine().getIdentifier());
            return Shipment;
          }
        } else {
          // Receipt
          if (inout.getDocumentType().isReversal()) {
            log4j.debug("Reversal Receipt: " + transaction.getGoodsShipmentLine().getIdentifier());
            return ReceiptReturn;
          } else if (inout.getDocumentStatus().equals("VO")
              && transaction.getGoodsShipmentLine().getMovementQuantity()
                  .compareTo(BigDecimal.ZERO) < 0) {
            log4j.debug("Void receipt: " + transaction.getGoodsShipmentLine().getIdentifier());
            return ReceiptVoid;
          } else {
            log4j.debug("Receipt: " + transaction.getGoodsShipmentLine().getIdentifier());
            return Receipt;
          }
        }
      } else if (transaction.getPhysicalInventoryLine() != null) {
        // Physical Inventory
        if (transaction.getMovementQuantity().compareTo(BigDecimal.ZERO) > 0) {
          log4j.debug("Physical inventory, increments stock: "
              + transaction.getPhysicalInventoryLine().getIdentifier());
          return InventoryIncrease;
        } else {
          log4j.debug("Physical inventory, decreases stock "
              + transaction.getPhysicalInventoryLine().getIdentifier());
          return InventoryDecrease;
        }
      } else if (transaction.getMovementLine() != null) {
        // Internal movement
        if (transaction.getMovementQuantity().compareTo(BigDecimal.ZERO) > 0) {
          log4j.debug("Internal Movement to: " + transaction.getMovementLine().getIdentifier());
          return IntMovementTo;
        } else {
          log4j.debug("Internal Movement from: " + transaction.getMovementLine().getIdentifier());
          return IntMovementFrom;
        }
      } else if (transaction.getInternalConsumptionLine() != null) {
        log4j.debug("Internal Consumption: "
            + transaction.getInternalConsumptionLine().getIdentifier());
        return InternalCons;
      } else if (transaction.getProductionLine() != null) {
        // Production Line
        if (transaction.getProductionLine().getProductionPlan().getProduction()
            .isSalesTransaction()) {
          // BOM Production
          if (transaction.getMovementQuantity().compareTo(BigDecimal.ZERO) > 0) {
            log4j.debug("Produced BOM product: " + transaction.getProductionLine().getIdentifier());
            return BOMProduct;
          } else {
            log4j.debug("Used BOM Part: " + transaction.getProductionLine().getIdentifier());
            // Used parts
            return BOMPart;
          }
        } else {
          log4j.debug("Manufacturing Product");
          // Work Effort
          // TODO: Pending to implement manufacturing cost management.
          return Manufacturing;
        }
      }
      return Unknown;
    }

  }

}
