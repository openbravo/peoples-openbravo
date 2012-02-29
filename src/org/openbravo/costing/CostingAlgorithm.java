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
import org.openbravo.costing.CostingServer.TrxType;
import org.openbravo.dal.core.DalUtil;
import org.openbravo.dal.core.OBContext;
import org.openbravo.erpCommon.ad_forms.AcctServer;
import org.openbravo.erpCommon.utility.OBDateUtils;
import org.openbravo.financial.FinancialUtils;
import org.openbravo.model.common.currency.Currency;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.common.order.OrderLine;
import org.openbravo.model.materialmgmt.cost.CostingRule;
import org.openbravo.model.materialmgmt.transaction.MaterialTransaction;
import org.openbravo.model.materialmgmt.transaction.ProductionLine;
import org.openbravo.model.materialmgmt.transaction.ShipmentInOutLine;
import org.openbravo.model.pricing.pricelist.PriceList;
import org.openbravo.model.pricing.pricelist.ProductPrice;
import org.openbravo.model.procurement.POInvoiceMatch;
import org.openbravo.service.db.DalConnectionProvider;

public abstract class CostingAlgorithm {
  protected MaterialTransaction transaction;
  protected HashMap<CostDimension, BaseOBObject> costDimensions = new HashMap<CostDimension, BaseOBObject>();
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
   * @param costingRule
   * @param currency
   * @param organization
   * @param _costDimensions
   *          Dimension values to calculate the cost based on them. A null value means that the
   *          dimension is not used.
   */
  public void init(CostingServer costingServer) {
    transaction = costingServer.getTransaction();
    costOrg = costingServer.getOrganization();
    costCurrency = costingServer.getCostCurrency();
    trxType = TrxType.getTrxType(this.transaction);

    CostingRule costingRule = costingServer.getCostingRule();
    costDimensions = CostingUtils.getEmptyDimensions();
    if (costingRule.isOrganizationDimension()) {
      costDimensions.put(CostDimension.LegalEntity, OBContext.getOBContext()
          .getOrganizationStructureProvider().getLegalEntity(transaction.getOrganization()));
    }
    if (costingRule.isWarehouseDimension()) {
      costDimensions.put(CostDimension.Warehouse, transaction.getStorageBin().getWarehouse());
    }

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
      return getShipmentReturnCost();
    case ShipmentVoid:
      return getShipmentVoidCost();
    case ShipmentNegative:
      return getShipmentNegativeCost();
    case Receipt:
      return getReceiptCost();
    case ReceiptReturn:
      return getReceiptReturnCost();
    case ReceiptVoid:
      return getReceiptVoidCost();
    case ReceiptNegative:
      return getReceiptNegativeCost();
    case InventoryDecrease:
      return getOutgoingTransactionCost();
    case InventoryIncrease:
      return getIncomingInventoryCost();
    case IntMovementFrom:
      return getOutgoingTransactionCost();
    case IntMovementTo:
      return getInternalMovementToCost();
    case InternalCons:
      return getOutgoingTransactionCost();
    case InternalConsNegative:
      return getInternalConsNegativeCost();
    case InternalConsVoid:
      return getInternalConsVoidCost();
    case BOMPart:
      return getOutgoingTransactionCost();
    case BOMProduct:
      return getBOMProductionCost();
    case Manufacturing:
      // Manufacturing transactions are not implemented.
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
   * Method to calculate cost of Returned Shipments. Cost is calculated based on the proportional
   * cost of the original receipt. If no original receipt is found the cost is calculated based on
   * the standard cost.
   */
  protected BigDecimal getShipmentReturnCost() {
    // Shipment return's cost is calculated based on the proportional cost of the original shipment.
    try {
      return getReturnedInOutLineCost();
    } catch (OBException e) {
      // if no original trx is found use standard cost.
      return getTransactionStandardCost();
    }
  }

  /**
   * Method to calculate the cost of Voided Shipments. By default the cost is calculated getting the
   * cost of the original payment.
   */
  protected BigDecimal getShipmentVoidCost() {
    // Voided shipment gets cost from original shipment line.
    return getOriginalInOutLineCost();
  }

  protected BigDecimal getShipmentNegativeCost() {
    // Shipment with negative quantity. If the product is purchased get cost from its purchase price
    // list. If no price list is found or it is not purchased Standard Cost is used.
    if (transaction.getProduct().isPurchase()) {
      return getPriceListCost();
    }
    return getTransactionStandardCost();
  }

  protected BigDecimal getShipmentCost() {
    ShipmentInOutLine shipmentline = transaction.getGoodsShipmentLine();
    OrderLine salesOrderLine = shipmentline.getSalesOrderLine();
    if (salesOrderLine == null) {
      // Shipment without order. Returns cost based on price list.
      // FIXME: return Price List price
      return getTransactionStandardCost();
    }

    BigDecimal orderAmt = transaction.getMovementQuantity().multiply(salesOrderLine.getUnitPrice());
    // TODO: Check if conversion is done correctly.

    BigDecimal trxCost = new BigDecimal(AcctServer.getConvertedAmt(orderAmt.toString(),
        salesOrderLine.getSalesOrder().getCurrency().getId(), costCurrency.getId(), OBDateUtils
            .formatDate(transaction.getTransactionProcessDate()), "", transaction.getClient()
            .getId(), costOrg.getId(), new DalConnectionProvider(false)));

    return trxCost;
  }

  /*
   * Default method to get Receipts Transaction amount based on receipt's related order price.
   */
  protected BigDecimal getReceiptCost() {
    BigDecimal trxCost = BigDecimal.ZERO;
    BigDecimal addQty = BigDecimal.ZERO;
    ShipmentInOutLine receiptline = transaction.getGoodsShipmentLine();
    if (receiptline.getSalesOrderLine() == null) {
      // Receipt without order. Returns cost based on price list.
      // FIXME: return Price List price
      return getTransactionStandardCost();
    }
    for (POInvoiceMatch matchPO : receiptline.getProcurementPOInvoiceMatchList()) {
      addQty = addQty.add(matchPO.getQuantity());
      BigDecimal orderAmt = matchPO.getQuantity().multiply(
          matchPO.getSalesOrderLine().getUnitPrice());
      // TODO: Check if conversion is done correctly.
      trxCost = trxCost.add(new BigDecimal(AcctServer.getConvertedAmt(orderAmt.toString(), matchPO
          .getSalesOrderLine().getSalesOrder().getCurrency().getId(), costCurrency.getId(),
          OBDateUtils.formatDate(transaction.getTransactionProcessDate()), "", transaction
              .getClient().getId(), costOrg.getId(), new DalConnectionProvider(false))));
    }
    return trxCost;
  }

  /**
   * Method to calculate cost of Returned Receipts. Cost is calculated based on the proportional
   * cost of the original receipt. If no original receipt is found the cost is calculated as a
   * regular outgoing transaction.
   */
  protected BigDecimal getReceiptReturnCost() throws OBException {
    // Receipt return's cost is calculated based on the proportional cost of the original shipment.
    try {
      return getReturnedInOutLineCost();
    } catch (OBException e) {
      // if no original trx is found use standard outgoing trx.
      return getOutgoingTransactionCost();
    }
  }

  /**
   * Method to calculate the cost of Voided Receipts. By default the cost is calculated getting the
   * cost of the original payment.
   */
  protected BigDecimal getReceiptVoidCost() {
    // Voided receipt gets cost from original receipt line.
    return getOriginalInOutLineCost();
  }

  protected BigDecimal getReceiptNegativeCost() {
    // Receipt with negative quantity. Calculate cost as a regular outgoing transaction.
    return getOutgoingTransactionCost();
  }

  /**
   * Returns the cost of the canceled Shipment/Receipt line on the date it is canceled.
   */
  protected BigDecimal getOriginalInOutLineCost() {
    if (transaction.getGoodsShipmentLine().getCanceledInoutLine() == null) {
      log4j.error("No canceled line found for transaction: " + transaction.getId());
      throw new OBException("@NoCanceledLineFoundForTrx@ @Transaction@: "
          + transaction.getIdentifier());
    }
    MaterialTransaction origInOutLineTrx = transaction.getGoodsShipmentLine()
        .getCanceledInoutLine().getMaterialMgmtMaterialTransactionList().get(0);

    return CostingUtils.getTransactionCost(origInOutLineTrx,
        transaction.getTransactionProcessDate());
  }

  /**
   * Gets the returned in out line and returns the proportional cost amount based on the original
   * movement quantity and the returned movement qty.
   * 
   * @throws OBException
   *           when no original in out line is found.
   */
  protected BigDecimal getReturnedInOutLineCost() throws OBException {
    MaterialTransaction originalTrx = null;
    try {
      originalTrx = transaction.getGoodsShipmentLine().getSalesOrderLine().getGoodsShipmentLine()
          .getMaterialMgmtMaterialTransactionList().get(0);
    } catch (Exception e) {
      throw new OBException("@NoReturnedLineFoundForTrx@ @Transaction@: "
          + transaction.getIdentifier());
    }
    BigDecimal originalCost = CostingUtils.getTransactionCost(originalTrx,
        transaction.getTransactionProcessDate());
    return originalCost.multiply(transaction.getMovementQuantity().abs()).divide(
        originalTrx.getMovementQuantity().abs(), costCurrency.getStandardPrecision().intValue(),
        RoundingMode.HALF_UP);
  }

  /**
   * Calculates the total cost amount of a physical inventory that results on an increment of stock.
   * Default behavior is to calculate the cost based on the Standard Cost defined for the product.
   */
  protected BigDecimal getIncomingInventoryCost() {
    return getTransactionStandardCost();
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
      return CostingUtils.getTransactionCost(movementTransaction,
          transaction.getTransactionProcessDate(), true);
    }
    // If no transaction is found throw an exception.
    throw new OBException("@NoInternalMovementTransactionFound@ @Transaction@: "
        + transaction.getIdentifier());
  }

  /**
   * Calculates the cost of a negative internal consumption. It uses product's Standard Cost to
   * calculate its price.
   */
  protected BigDecimal getInternalConsNegativeCost() {
    return getTransactionStandardCost();
  }

  /**
   * Returns the cost of the original internal consumption.
   */
  protected BigDecimal getInternalConsVoidCost() {
    return CostingUtils.getTransactionCost(transaction.getInternalConsumptionLine()
        .getVoidedInternalConsumptionLine().getMaterialMgmtMaterialTransactionList().get(0),
        transaction.getTransactionProcessDate(), true);
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
          transaction.getTransactionProcessDate(), true));
    }
    return totalCost;
  }

  /**
   * Calculates the transaction cost based on the Standard Cost of the product on the Transaction
   * Process Date.
   */
  protected BigDecimal getTransactionStandardCost() {
    BigDecimal standardCost = CostingUtils.getStandardCost(transaction.getProduct(),
        transaction.getTransactionProcessDate(), costDimensions);
    return transaction.getMovementQuantity().abs().multiply(standardCost);
  }

  /**
   * Calculates the transaction cost based on the purchase price list of the product. It searches
   * first for a default price list and if none exists takes one.
   * 
   * @return BigDecimal object representing the total cost amount of the transaction.
   * @throws OBException
   *           when no PriceList is found for the product.
   */
  protected BigDecimal getPriceListCost() {
    org.openbravo.model.common.businesspartner.BusinessPartner bp = CostingUtils
        .getTrxBusinessPartner(transaction, trxType);
    PriceList pricelist = null;
    if (bp != null) {
      pricelist = bp.getPurchasePricelist();
    }
    ProductPrice pp = FinancialUtils.getProductPrice(transaction.getProduct(),
        transaction.getTransactionProcessDate(), false, pricelist);
    BigDecimal cost = pp.getStandardPrice().multiply(transaction.getMovementQuantity().abs());
    if (DalUtil.getId(pp.getPriceListVersion().getPriceList().getCurrency()).equals(
        costCurrency.getId())) {
      // no conversion needed
      return cost;
    }
    return FinancialUtils.getConvertedAmount(cost, pp.getPriceListVersion().getPriceList()
        .getCurrency(), costCurrency, transaction.getTransactionProcessDate(), costOrg,
        FinancialUtils.PRECISION_STANDARD);
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

}
