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
 * All portions are Copyright (C) 2014 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 *************************************************************************
 */
package org.openbravo.costing;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;

import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.costing.CostingAlgorithm.CostDimension;
import org.openbravo.costing.CostingServer.TrxType;
import org.openbravo.dal.core.DalUtil;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.common.businesspartner.BusinessPartner;
import org.openbravo.model.common.currency.Currency;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.common.enterprise.Warehouse;
import org.openbravo.model.common.order.OrderLine;
import org.openbravo.model.materialmgmt.cost.CostAdjustment;
import org.openbravo.model.materialmgmt.cost.CostAdjustmentLine;
import org.openbravo.model.materialmgmt.cost.CostingRule;
import org.openbravo.model.materialmgmt.transaction.InternalConsumptionLine;
import org.openbravo.model.materialmgmt.transaction.InventoryCountLine;
import org.openbravo.model.materialmgmt.transaction.MaterialTransaction;
import org.openbravo.model.materialmgmt.transaction.ProductionLine;
import org.openbravo.model.materialmgmt.transaction.ShipmentInOutLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public abstract class CostingAlgorithmAdjustmentImp {
  protected static Logger log4j = LoggerFactory.getLogger(CostingAlgorithmAdjustmentImp.class);
  protected String strCostAdjLineId;
  protected String strCostAdjId;
  protected String strTransactionId;
  protected String strCostOrgId;
  protected String strCostCurrencyId;
  protected int costCurPrecission;
  protected int stdCurPrecission;
  protected TrxType trxType;
  protected String strCostingRuleId;
  protected Date startingDate;
  protected String strClientId;
  protected boolean isManufacturingProduct;
  protected HashMap<CostDimension, String> costDimensionIds = new HashMap<CostDimension, String>();

  /**
   * Initializes class variables to perform the cost adjustment process. Variables are stored by the
   * ids instead of the BaseOBObject to be safe of session clearing.
   * 
   * @param costAdjLine
   *          The Cost Adjustment Line that it is processed.
   */
  protected void init(CostAdjustmentLine costAdjLine) {
    strCostAdjLineId = costAdjLine.getId();
    strCostAdjId = (String) DalUtil.getId(costAdjLine.getCostAdjustment());
    MaterialTransaction transaction = costAdjLine.getInventoryTransaction();
    strTransactionId = transaction.getId();
    isManufacturingProduct = transaction.getProduct().isProduction();
    CostingServer costingServer = new CostingServer(transaction);
    strCostOrgId = costingServer.getOrganization().getId();
    strCostCurrencyId = transaction.getCurrency().getId();
    costCurPrecission = transaction.getCurrency().getCostingPrecision().intValue();
    stdCurPrecission = transaction.getCurrency().getStandardPrecision().intValue();
    trxType = CostingServer.TrxType.getTrxType(transaction);
    CostingRule costingRule = costingServer.getCostingRule();
    strCostingRuleId = costingRule.getId();
    startingDate = costingRule.getStartingDate();
    strClientId = costingRule.getClient().getId();

    HashMap<CostDimension, BaseOBObject> costDimensions = CostingUtils.getEmptyDimensions();
    // Production products cannot be calculated by warehouse dimension.
    if (costingRule.isWarehouseDimension()) {
      costDimensions.put(CostDimension.Warehouse, transaction.getStorageBin().getWarehouse());
    }
    for (CostDimension costDimension : costDimensions.keySet()) {
      String value = null;
      if (costDimensions.get(costDimension) != null) {
        value = (String) costDimensions.get(costDimension).getId();
      }
      costDimensionIds.put(costDimension, value);
    }
  }

  /**
   * Process to include in the Cost Adjustment the required lines of transactions whose cost needs
   * to be adjusted as a consequence of other lines already included.
   */
  protected void searchRelatedTransactionCosts(CostAdjustmentLine _costAdjLine) {
    boolean searchRelatedTransactions = true;
    CostAdjustmentLine costAdjLine;
    if (_costAdjLine != null) {
      costAdjLine = _costAdjLine;
      searchRelatedTransactions = false;
    } else {
      costAdjLine = getCostAdjLine();
    }

    // Backdated transactions are inserted with a null adjustment amount.
    if (costAdjLine.isBackdatedTrx()) {
      calculateBackdatedTrxAdjustment(costAdjLine);
    }

    // Negative stock correction are inserted with a null adjustment amount.
    if (costAdjLine.isNegativeStockCorrection()) {
      calculateNegativeStockCorrectionAdjustmentAmount(costAdjLine);
    }

    if (costAdjLine.isSource()) {
      addCostDependingTrx(null);
    }

    if (searchRelatedTransactions) {
      getRelatedTransactionsByAlgorithm();
    }
  }

  protected void addCostDependingTrx(CostAdjustmentLine costAdjLine) {
    // Some transaction costs are directly related to other transaction costs. These relationships
    // must be kept when the original transaction cost is adjusted adjusting as well the dependent
    // transactions.
    TrxType _trxType = trxType;
    if (costAdjLine != null) {
      _trxType = TrxType.getTrxType(costAdjLine.getInventoryTransaction());
    }
    switch (_trxType) {
    case Shipment:
      searchReturnShipments(costAdjLine);
    case Receipt:
      searchVoidInOut(costAdjLine);
      break;
    case IntMovementFrom:
      searchIntMovementTo(costAdjLine);
      break;
    case InternalCons:
      searchVoidInternalConsumption(costAdjLine);
      break;
    case BOMPart:
      searchBOMProducts(costAdjLine);
      break;
    case ManufacturingConsumed:
      searchManufacturingProduced(costAdjLine);
      break;
    case InventoryDecrease:
    case InventoryIncrease:
      searchOpeningInventory(costAdjLine);
    default:
      break;
    }
  }

  protected CostAdjustmentLine insertCostAdjustmentLine(MaterialTransaction trx,
      BigDecimal adjustmentamt, CostAdjustmentLine _parentLine) {
    Date dateAcct = trx.getMovementDate();

    CostAdjustmentLine parentLine;
    if (_parentLine == null) {
      parentLine = getCostAdjLine();
    } else {
      parentLine = _parentLine;
    }
    Date parentAcctDate = parentLine.getAccountingDate();
    if (parentAcctDate == null) {
      parentAcctDate = parentLine.getInventoryTransaction().getMovementDate();
    }

    if (dateAcct.before(parentAcctDate)) {
      dateAcct = parentAcctDate;
    }

    CostAdjustmentLine newCAL = CostAdjustmentUtils.insertCostAdjustmentLine(trx,
        (CostAdjustment) OBDal.getInstance().getProxy(CostAdjustment.ENTITY_NAME, strCostAdjId),
        adjustmentamt, false, trx.getTransactionProcessDate(), dateAcct);
    newCAL.setRelatedTransactionAdjusted(false);
    newCAL.setParentCostAdjustmentLine(parentLine);

    OBDal.getInstance().save(newCAL);

    addCostDependingTrx(newCAL);
    return newCAL;
  }

  /*
   * When the cost of a Closing Inventory is adjusted it is needed to adjust with the same amount
   * the related Opening Inventory.
   */
  protected void searchOpeningInventory(CostAdjustmentLine _costAdjLine) {
    CostAdjustmentLine costAdjLine;
    if (_costAdjLine != null) {
      costAdjLine = _costAdjLine;
    } else {
      costAdjLine = getCostAdjLine();
    }
    InventoryCountLine invline = costAdjLine.getInventoryTransaction().getPhysicalInventoryLine()
        .getRelatedInventory();
    if (invline == null) {
      return;
    }
    MaterialTransaction deptrx = invline.getMaterialMgmtMaterialTransactionList().get(0);
    insertCostAdjustmentLine(deptrx, costAdjLine.getAdjustmentAmount(), _costAdjLine);
  }

  protected void searchManufacturingProduced(CostAdjustmentLine _costAdjLine) {
    CostAdjustmentLine lastAdjLine = null;
    CostAdjustmentLine costAdjLine;
    if (_costAdjLine != null) {
      costAdjLine = _costAdjLine;
    } else {
      costAdjLine = getCostAdjLine();
    }
    BigDecimal pendingAmt = costAdjLine.getAdjustmentAmount();
    List<ProductionLine> productionLines = costAdjLine.getInventoryTransaction()
        .getProductionLine().getProductionPlan().getManufacturingProductionLineList();
    Collections.sort(productionLines, new Comparator<ProductionLine>() {
      public int compare(ProductionLine pline1, ProductionLine pline2) {
        return pline1.getComponentCost().compareTo(pline2.getComponentCost());
      }
    });
    for (ProductionLine pline : productionLines) {
      if (!pline.getProductionType().equals("P+")) {
        continue;
      }
      MaterialTransaction prodtrx = pline.getMaterialMgmtMaterialTransactionList().get(0);
      BigDecimal adjAmt = costAdjLine.getAdjustmentAmount().multiply(pline.getComponentCost());
      CostAdjustmentLine newCAL = insertCostAdjustmentLine(prodtrx, adjAmt, _costAdjLine);

      pendingAmt = pendingAmt.subtract(adjAmt);
      lastAdjLine = newCAL;
    }
    // If there is more than one P+ product there can be some amount left to assign due to rounding.
    if (pendingAmt.signum() != 0 && lastAdjLine != null) {
      lastAdjLine.setAdjustmentAmount(lastAdjLine.getAdjustmentAmount().add(pendingAmt));
      OBDal.getInstance().save(lastAdjLine);
    }
  }

  protected void searchBOMProducts(CostAdjustmentLine _costAdjLine) {
    CostAdjustmentLine costAdjLine;
    if (_costAdjLine != null) {
      costAdjLine = _costAdjLine;
    } else {
      costAdjLine = getCostAdjLine();
    }
    for (ProductionLine pline : costAdjLine.getInventoryTransaction().getProductionLine()
        .getProductionPlan().getManufacturingProductionLineList()) {
      if (pline.getMovementQuantity().signum() <= 0) {
        continue;
      }
      MaterialTransaction prodtrx = pline.getMaterialMgmtMaterialTransactionList().get(0);
      insertCostAdjustmentLine(prodtrx, costAdjLine.getAdjustmentAmount(), _costAdjLine);
    }
  }

  protected void searchVoidInternalConsumption(CostAdjustmentLine _costAdjLine) {
    CostAdjustmentLine costAdjLine;
    if (_costAdjLine != null) {
      costAdjLine = _costAdjLine;
    } else {
      costAdjLine = getCostAdjLine();
    }
    InternalConsumptionLine intCons = costAdjLine.getInventoryTransaction()
        .getInternalConsumptionLine()
        .getMaterialMgmtInternalConsumptionLineVoidedInternalConsumptionLineList().get(0);
    MaterialTransaction voidedTrx = intCons.getMaterialMgmtMaterialTransactionList().get(0);
    insertCostAdjustmentLine(voidedTrx, costAdjLine.getAdjustmentAmount(), _costAdjLine);
  }

  protected void searchIntMovementTo(CostAdjustmentLine _costAdjLine) {
    CostAdjustmentLine costAdjLine;
    if (_costAdjLine != null) {
      costAdjLine = _costAdjLine;
    } else {
      costAdjLine = getCostAdjLine();
    }
    MaterialTransaction transaction = costAdjLine.getInventoryTransaction();
    for (MaterialTransaction movementTransaction : transaction.getMovementLine()
        .getMaterialMgmtMaterialTransactionList()) {
      if (movementTransaction.getId().equals(transaction.getId())) {
        continue;
      }
      insertCostAdjustmentLine(movementTransaction, costAdjLine.getAdjustmentAmount(), _costAdjLine);
    }
  }

  protected void searchVoidInOut(CostAdjustmentLine _costAdjLine) {
    CostAdjustmentLine costAdjLine;
    if (_costAdjLine != null) {
      costAdjLine = _costAdjLine;
    } else {
      costAdjLine = getCostAdjLine();
    }
    ShipmentInOutLine voidedinoutline = costAdjLine.getInventoryTransaction()
        .getGoodsShipmentLine().getCanceledInoutLine();
    if (voidedinoutline == null) {
      return;
    }
    for (MaterialTransaction trx : voidedinoutline.getMaterialMgmtMaterialTransactionList()) {
      // TODO: Generate cost adjustment line. Check if adjustment amount is properly calculated
      insertCostAdjustmentLine(trx, costAdjLine.getAdjustmentAmount(), _costAdjLine);
    }
  }

  protected void searchReturnShipments(CostAdjustmentLine _costAdjLine) {
    CostAdjustmentLine costAdjLine;
    if (_costAdjLine != null) {
      costAdjLine = _costAdjLine;
    } else {
      costAdjLine = getCostAdjLine();
    }
    ShipmentInOutLine inoutline = costAdjLine.getInventoryTransaction().getGoodsShipmentLine();
    BigDecimal costAdjAmt = costAdjLine.getAdjustmentAmount().negate();
    int precission = getCostCurrency().getStandardPrecision().intValue();
    // FIXME: PERFORMANCE: change loops to OBQuery
    for (OrderLine returnorderline : inoutline.getOrderLineList()) {
      for (ShipmentInOutLine retinoutline : returnorderline.getMaterialMgmtShipmentInOutLineList()) {
        for (MaterialTransaction rettrx : retinoutline.getMaterialMgmtMaterialTransactionList()) {
          // TODO: Generate cost adjustment line
          BigDecimal adjAmt = costAdjAmt.multiply(rettrx.getMovementQuantity().abs()).divide(
              inoutline.getMovementQuantity().abs(), precission, RoundingMode.HALF_UP);
          insertCostAdjustmentLine(rettrx, adjAmt, _costAdjLine);
        }
      }
    }

  }

  protected abstract void calculateNegativeStockCorrectionAdjustmentAmount(
      CostAdjustmentLine costAdjLine);

  protected abstract void getRelatedTransactionsByAlgorithm();

  protected void calculateBackdatedTrxAdjustment(CostAdjustmentLine costAdjLine) {
    BigDecimal adjAmt = BigDecimal.ZERO;
    TrxType calTrxType = TrxType.getTrxType(costAdjLine.getInventoryTransaction());

    // Incoming transactions does not modify the calculated cost
    switch (calTrxType) {
    case ShipmentVoid:
    case ReceiptVoid:
    case IntMovementTo:
    case InternalConsVoid:
    case BOMProduct:
    case ManufacturingProduced:
      // The cost of these transaction types does not depend on the date it is calculated.
      break;

    case Receipt:
      if (hasOrder(costAdjLine)) {
        // If the receipt has a related order the cost amount does not depend on the date.
        break;
      }
      // Check receipt default on backdated date.
      adjAmt = getDefaultCostDifference(calTrxType, costAdjLine);
      break;
    case ShipmentReturn:
      if (hasReturnedReceipt(costAdjLine)) {
        // If the return receipt has a original receipt the cost amount does not depend on the date.
        break;
      }
    case ShipmentNegative:
      // These transaction types are calculated using the default cost. Check if there is a
      // difference.
      adjAmt = getDefaultCostDifference(calTrxType, costAdjLine);
      break;
    case InventoryIncrease:
    case InventoryOpening:
      if (inventoryHasCost(costAdjLine)) {
        // If the inventory line defines a unit cost it does not depend on the date.
        break;
      }
    case InternalConsNegative:
      // These transaction types are calculated using the default cost. Check if there is a
      // difference.
      adjAmt = getDefaultCostDifference(calTrxType, costAdjLine);
      break;

    case Shipment:
    case ReceiptReturn:
    case ReceiptNegative:
    case InventoryDecrease:
    case InventoryClosing:
    case IntMovementFrom:
    case InternalCons:
    case BOMPart:
    case ManufacturingConsumed:
      // These transactions are calculated as regular outgoing transactions. The adjustment amount
      // needs to be calculated by the algorithm.
      adjAmt = getOutgoingBackdatedTrxAdjAmt(costAdjLine);
    default:
      break;
    }
    costAdjLine.setCurrency((Currency) OBDal.getInstance().getProxy(Currency.ENTITY_NAME,
        strCostCurrencyId));
    costAdjLine.setAdjustmentAmount(adjAmt);
    OBDal.getInstance().save(costAdjLine);

  }

  protected abstract BigDecimal getOutgoingBackdatedTrxAdjAmt(CostAdjustmentLine costAdjLine);

  private BigDecimal getDefaultCostDifference(TrxType calTrxType, CostAdjustmentLine costAdjLine) {
    MaterialTransaction trx = costAdjLine.getInventoryTransaction();
    BusinessPartner bp = CostingUtils.getTrxBusinessPartner(trx, calTrxType);

    BigDecimal defaultCost = CostingUtils.getDefaultCost(trx.getProduct(),
        trx.getMovementQuantity(), getCostOrg(), getCostAdjLine().getTransactionDate(),
        trx.getMovementDate(), bp, getCostCurrency(), getCostDimensions());
    // FIXME: Review if previous adjustment cost need to be considered.
    BigDecimal trxCalculatedCost = trx.getTransactionCost();
    return trxCalculatedCost.subtract(defaultCost);
  }

  /**
   * Checks if the goods receipt line of the adjustment line has a related purchase order line.
   * 
   * @param costAdjLine
   *          the adjustment line to check.
   * @return true if there is a related order line.
   */
  private boolean hasOrder(CostAdjustmentLine costAdjLine) {
    return costAdjLine.getInventoryTransaction().getGoodsShipmentLine().getSalesOrderLine() != null;
  }

  /**
   * Checks if the inventory line has a unit cost defined.
   * 
   * @param costAdjLine
   *          the adjustment line to check.
   * @return true if there is a unit cost.
   */
  private boolean inventoryHasCost(CostAdjustmentLine costAdjLine) {
    return costAdjLine.getInventoryTransaction().getPhysicalInventoryLine().getCost() != null;
  }

  /**
   * Checks if the returned receipt line has a related original shipment line.
   * 
   * @param costAdjLine
   *          the adjustment line to check.
   * @return true if there is a original shipment line.
   */
  private boolean hasReturnedReceipt(CostAdjustmentLine costAdjLine) {
    OrderLine shipmentLine = costAdjLine.getInventoryTransaction().getGoodsShipmentLine()
        .getSalesOrderLine();
    return shipmentLine != null && shipmentLine.getGoodsShipmentLine() != null;
  }

  public CostAdjustmentLine getCostAdjLine() {
    return OBDal.getInstance().get(CostAdjustmentLine.class, strCostAdjLineId);
  }

  public CostAdjustment getCostAdj() {
    return OBDal.getInstance().get(CostAdjustment.class, strCostAdjId);
  }

  public MaterialTransaction getTransaction() {
    return OBDal.getInstance().get(MaterialTransaction.class, strTransactionId);
  }

  public Organization getCostOrg() {
    return OBDal.getInstance().get(Organization.class, strCostOrgId);
  }

  public Currency getCostCurrency() {
    return OBDal.getInstance().get(Currency.class, strCostCurrencyId);
  }

  public CostingRule getCostingRule() {
    return OBDal.getInstance().get(CostingRule.class, strCostingRuleId);
  }

  public HashMap<CostDimension, BaseOBObject> getCostDimensions() {
    HashMap<CostDimension, BaseOBObject> costDimensions = new HashMap<CostDimension, BaseOBObject>();
    for (CostDimension costDimension : costDimensionIds.keySet()) {
      switch (costDimension) {
      case Warehouse:
        Warehouse warehouse = null;
        if (costDimensionIds.get(costDimension) != null) {
          warehouse = OBDal.getInstance().get(Warehouse.class, costDimensionIds.get(costDimension));
        }
        costDimensions.put(costDimension, warehouse);
        break;
      default:
        break;
      }
    }

    return costDimensions;
  }
}
