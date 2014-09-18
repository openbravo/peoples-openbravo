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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.hibernate.Query;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.session.OBPropertiesProvider;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.client.kernel.ComponentProvider;
import org.openbravo.costing.CostingAlgorithm.CostDimension;
import org.openbravo.costing.CostingServer.TrxType;
import org.openbravo.dal.core.DalUtil;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.security.OrganizationStructureProvider;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.erpCommon.utility.OBDateUtils;
import org.openbravo.financial.FinancialUtils;
import org.openbravo.model.common.currency.Currency;
import org.openbravo.model.common.enterprise.Locator;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.common.enterprise.Warehouse;
import org.openbravo.model.common.plm.Product;
import org.openbravo.model.materialmgmt.cost.CostAdjustmentLine;
import org.openbravo.model.materialmgmt.cost.Costing;
import org.openbravo.model.materialmgmt.cost.CostingRule;
import org.openbravo.model.materialmgmt.cost.TransactionCost;
import org.openbravo.model.materialmgmt.transaction.MaterialTransaction;
import org.openbravo.model.materialmgmt.transaction.ShipmentInOut;
import org.openbravo.model.materialmgmt.transaction.ShipmentInOutLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ComponentProvider.Qualifier("B069080A0AE149A79CF1FA0E24F16AB6")
public class AverageCostAdjustment extends CostingAlgorithmAdjustmentImp {
  private static final Logger log = LoggerFactory.getLogger(CostAdjustmentProcess.class);

  @Override
  protected void getRelatedTransactionsByAlgorithm() {
    // Search all transactions after the date of the adjusted line and recalculate the costs of them
    // to adjust differences
    MaterialTransaction basetrx = getTransaction();
    // Transactions of closing inventories are managed by generic CostAdjustmentProcess adjusting
    // the cost of the related opening inventory.
    if (basetrx.getPhysicalInventoryLine() != null
        && basetrx.getPhysicalInventoryLine().getRelatedInventory() != null) {
      return;
    }
    BigDecimal signMultiplier = new BigDecimal(basetrx.getMovementQuantity().signum());
    Date trxDate = basetrx.getTransactionProcessDate();
    boolean backdatedTrxSourcePending = false;

    BigDecimal adjustmentBalance = BigDecimal.ZERO;
    // Initialize adjustment balance looping through all cost adjustment lines of current
    // transaction.
    log.debug("Initialize adjustment balance");
    CostAdjustmentLine baseCAL = getCostAdjLine();
    for (CostAdjustmentLine costAdjLine : getTrxAdjustmentLines(basetrx)) {
      if (costAdjLine.isSource() && !costAdjLine.isRelatedTransactionAdjusted()) {
        if (!costAdjLine.getId().equals(strCostAdjLineId)) {
          searchRelatedTransactionCosts(costAdjLine);
          // OBDal.getInstance().refresh(costAdjLine);
        }

        backdatedTrxSourcePending |= costAdjLine.isBackdatedTrx()
            && basetrx.getMaterialMgmtCostingList().size() > 0;
      }
      costAdjLine.setRelatedTransactionAdjusted(Boolean.TRUE);
      if (!costAdjLine.getId().equals(strCostAdjLineId)) {
        costAdjLine.setParentCostAdjustmentLine(baseCAL);
      }
      OBDal.getInstance().save(costAdjLine);
      // If the cost adjustment line has Transaction Costs those adjustment amount are included
      // in the Current Value Amount and not in the Adjustment Balance
      if (!costAdjLine.getTransactionCostList().isEmpty()) {
        continue;
      }
      BigDecimal adjustmentAmt = costAdjLine.getAdjustmentAmount();
      if (!strCostCurrencyId.equals(costAdjLine.getCurrency().getId())) {
        adjustmentAmt = FinancialUtils.getConvertedAmount(adjustmentAmt, costAdjLine.getCurrency(),
            getCostCurrency(), costAdjLine.getAccountingDate(), getCostOrg(),
            FinancialUtils.PRECISION_STANDARD);
      }
      adjustmentBalance = adjustmentBalance.add(adjustmentAmt.multiply(signMultiplier));
    }

    // Initialize current stock qty and value amt.
    BigDecimal currentStock = CostingUtils.getCurrentStock(getCostOrg(), basetrx,
        getCostDimensions(), isManufacturingProduct);
    BigDecimal currentValueAmt = getCurrentValuedStock(basetrx);
    log.debug("Adjustment balance: " + adjustmentBalance.toPlainString()
        + ", current stock {}, current value {}", currentStock.toPlainString(),
        currentValueAmt.toPlainString());

    // Initialize current unit cost including the cost adjustments.
    Costing costing = AverageAlgorithm.getProductCost(trxDate, basetrx.getProduct(),
        getCostDimensions(), getCostOrg());
    BigDecimal cost = null;
    // If current stock is zero the cost is not modified until a related transaction that modifies
    // the stock is found.
    if (currentStock.signum() != 0) {
      cost = currentValueAmt.add(adjustmentBalance).divide(currentStock, costCurPrecission,
          RoundingMode.HALF_UP);
    }
    log.debug("Starting average cost {}", cost == null ? "not cost" : cost.toPlainString());
    if (AverageAlgorithm.modifiesAverage(trxType) && cost != null) {
      Costing curCosting = basetrx.getMaterialMgmtCostingList().get(0);
      if (curCosting.getCost().compareTo(cost) != 0) {
        // Update existing costing
        curCosting.setPermanent(Boolean.FALSE);
        OBDal.getInstance().save(curCosting);
        OBDal.getInstance().flush();
        if (curCosting.getOriginalCost() == null) {
          curCosting.setOriginalCost(curCosting.getCost());
        }
        curCosting.setCost(cost);
        curCosting.setPermanent(Boolean.TRUE);
        OBDal.getInstance().save(curCosting);
      }
    }

    // Modify isManufacturingProduct flag in case it has changed at some point.
    isManufacturingProduct = ((String) DalUtil.getId(costing.getOrganization())).equals("0");

    ScrollableResults trxs = getRelatedTransactions();
    String strCurrentCurId = strCostCurrencyId;
    try {
      while (trxs.next()) {
        MaterialTransaction trx = (MaterialTransaction) trxs.get()[0];
        log.debug("Process related transaction {}", trx.getIdentifier());
        BigDecimal trxSignMultiplier = new BigDecimal(trx.getMovementQuantity().signum());
        BigDecimal trxAdjAmt = BigDecimal.ZERO;
        if (backdatedTrxSourcePending) {
          // If there is a backdated source adjustment pending modify the dates of its m_costing
          Costing prevCosting = getPreviousCosting(trx);
          Costing sourceCosting = basetrx.getMaterialMgmtCostingList().get(0);
          sourceCosting.setPermanent(Boolean.FALSE);
          OBDal.getInstance().save(sourceCosting);
          // Fire trigger to allow to modify the average cost and starting date.
          OBDal.getInstance().flush();
          if (prevCosting != null) {
            sourceCosting.setEndingDate(prevCosting.getEndingDate());
          } else {
            // There isn't any previous costing.
            sourceCosting.setEndingDate(trx.getTransactionProcessDate());
          }
          sourceCosting.setStartingDate(trx.getTransactionProcessDate());
          sourceCosting.setPermanent(Boolean.TRUE);
          OBDal.getInstance().save(sourceCosting);
          if (prevCosting != null) {
            prevCosting.setEndingDate(trx.getTransactionProcessDate());
            OBDal.getInstance().save(prevCosting);
          }

          // This update is done only on the first related transaction.
          backdatedTrxSourcePending = false;
        }

        if (!strCurrentCurId.equals(trx.getCurrency().getId())) {
          Currency curCurrency = OBDal.getInstance().get(Currency.class, strCurrentCurId);
          Organization costOrg = getCostOrg();

          currentValueAmt = FinancialUtils.getConvertedAmount(currentValueAmt, curCurrency,
              trx.getCurrency(), trx.getMovementDate(), costOrg, FinancialUtils.PRECISION_STANDARD);
          if (cost != null) {
            cost = FinancialUtils.getConvertedAmount(cost, curCurrency, trx.getCurrency(),
                trx.getMovementDate(), costOrg, FinancialUtils.PRECISION_COSTING);
          }

          strCurrentCurId = trx.getCurrency().getId();
        }

        List<CostAdjustmentLine> existingAdjLines = getTrxAdjustmentLines(trx);
        for (CostAdjustmentLine existingCAL : existingAdjLines) {
          if (existingCAL.isSource() && !existingCAL.isRelatedTransactionAdjusted()) {
            searchRelatedTransactionCosts(existingCAL);
            // OBDal.getInstance().refresh(costAdjLine);

            backdatedTrxSourcePending |= existingCAL.isBackdatedTrx()
                && basetrx.getMaterialMgmtCostingList().size() > 0;
          }
          if (existingCAL.getTransactionCostList().isEmpty()
              && !existingCAL.isRelatedTransactionAdjusted()) {
            BigDecimal adjustmentAmt = existingCAL.getAdjustmentAmount();
            if (!strCurrentCurId.equals(existingCAL.getCurrency().getId())) {
              Currency curCurrency = OBDal.getInstance().get(Currency.class, strCurrentCurId);
              adjustmentAmt = FinancialUtils.getConvertedAmount(adjustmentAmt,
                  existingCAL.getCurrency(), curCurrency, existingCAL.getAccountingDate(),
                  getCostOrg(), FinancialUtils.PRECISION_STANDARD);
            }
            trxAdjAmt = trxAdjAmt.add(adjustmentAmt);
            adjustmentBalance = adjustmentBalance.add(adjustmentAmt.multiply(signMultiplier));
          }

          existingCAL.setRelatedTransactionAdjusted(Boolean.TRUE);
          existingCAL.setParentCostAdjustmentLine((CostAdjustmentLine) OBDal.getInstance()
              .getProxy(CostAdjustmentLine.ENTITY_NAME, strCostAdjLineId));

          OBDal.getInstance().save(existingCAL);
        }
        log.debug("Current trx adj amount of existing CALs {}", trxAdjAmt.toPlainString());

        BigDecimal trxCost = CostAdjustmentUtils.getTrxCost(trx, false,
            OBDal.getInstance().get(Currency.class, strCurrentCurId));
        currentValueAmt = currentValueAmt.add(trxCost.multiply(new BigDecimal(trx
            .getMovementQuantity().signum())));
        currentStock = currentStock.add(trx.getMovementQuantity());
        log.debug("Updated current stock {} and, current value {}", currentStock.toPlainString(),
            currentValueAmt.toPlainString());

        TrxType currentTrxType = TrxType.getTrxType(trx);
        if (isVoidedTrx(trx, currentTrxType)) {
          continue;
        }

        if (AverageAlgorithm.modifiesAverage(currentTrxType)) {
          // Recalculate average, if current stock is zero the average is not modified
          if (currentStock.signum() != 0) {
            cost = currentValueAmt.add(adjustmentBalance).divide(currentStock, costCurPrecission,
                RoundingMode.HALF_UP);
          }
          if (cost == null) {
            continue;
          }
          log.debug("New average cost: {}", cost.toPlainString());
          BigDecimal trxPrice = trxCost.add(trxAdjAmt).divide(trx.getMovementQuantity().abs(),
              costCurPrecission, RoundingMode.HALF_UP);

          if (currentStock.compareTo(trx.getMovementQuantity()) < 0
              && cost.compareTo(trxPrice) != 0) {
            // stock was negative and cost different than trx price then Negative Stock Correction
            // is added
            BigDecimal negCorrAmt = trxPrice.multiply(currentStock)
                .setScale(stdCurPrecission, RoundingMode.HALF_UP).subtract(currentValueAmt)
                .subtract(adjustmentBalance);
            adjustmentBalance = adjustmentBalance.add(negCorrAmt.multiply(trxSignMultiplier));
            trxAdjAmt = trxAdjAmt.add(negCorrAmt.multiply(trxSignMultiplier));
            // If there is a difference insert a cost adjustment line.
            CostAdjustmentLine newCAL = insertCostAdjustmentLine(trx, negCorrAmt, null);
            newCAL.setNegativeStockCorrection(true);
            newCAL.setRelatedTransactionAdjusted(true);
            OBDal.getInstance().save(newCAL);
            cost = trxPrice;
            log.debug("Negative stock correction. Amount: {}, new cost {}",
                negCorrAmt.toPlainString(), cost.toPlainString());
          }

          Costing curCosting = trx.getMaterialMgmtCostingList().get(0);
          if (curCosting.getCost().compareTo(cost) == 0) {
            // new cost hasn't changed, following transactions will have the same cost, so no more
            // related transactions are needed to include.
            log.debug("New cost matches existing cost. Adjustment finished.");
            return;
          } else {
            // Update existing costing
            curCosting.setPermanent(Boolean.FALSE);
            OBDal.getInstance().save(curCosting);
            OBDal.getInstance().flush();
            if (curCosting.getOriginalCost() == null) {
              curCosting.setOriginalCost(curCosting.getCost());
            }
            curCosting.setCost(cost);
            curCosting.setPermanent(Boolean.TRUE);
            OBDal.getInstance().save(curCosting);
          }
        } else if (!trx.isCostPermanent() && cost != null) {
          // Check current trx unit cost matches new expected cost
          BigDecimal expectedCost = cost.multiply(trx.getMovementQuantity().abs());
          BigDecimal unitCost = CostAdjustmentUtils.getTrxCost(trx, true,
              OBDal.getInstance().get(Currency.class, strCurrentCurId));
          log.debug("Is adjustment needed? Expected {} vs Current {}",
              expectedCost.toPlainString(), unitCost.toPlainString());
          if (expectedCost.compareTo(unitCost) != 0) {
            trxAdjAmt = trxAdjAmt.add(expectedCost.subtract(unitCost).multiply(trxSignMultiplier));
            adjustmentBalance = adjustmentBalance.add(expectedCost.subtract(unitCost).multiply(
                trxSignMultiplier));
            // If there is a difference insert a cost adjustment line.
            CostAdjustmentLine newCAL = insertCostAdjustmentLine(trx,
                expectedCost.subtract(unitCost), null);
            newCAL.setRelatedTransactionAdjusted(true);
            OBDal.getInstance().save(newCAL);
            log.debug("Adjustment added. Amount {}.", expectedCost.subtract(unitCost)
                .toPlainString());
          }
        }

        OBDal.getInstance().flush();
        OBDal.getInstance().getSession().clear();
      }
    } finally {
      trxs.close();
    }

    if (getCostingRule().getEndingDate() == null && cost != null) {
      // This is the current costing rule. Check if current average cost needs to be updated.
      Costing currentCosting = AverageAlgorithm.getProductCost(new Date(), basetrx.getProduct(),
          getCostDimensions(), getCostOrg());
      if (currentCosting.getCost().compareTo(cost) != 0) {
        basetrx = getTransaction();
        Date newDate = new Date();
        Date dateTo = currentCosting.getEndingDate();
        currentCosting.setEndingDate(newDate);
        OBDal.getInstance().save(currentCosting);
        Costing newCosting = OBProvider.getInstance().get(Costing.class);
        newCosting.setCost(cost);
        newCosting.setCurrency((Currency) OBDal.getInstance().getProxy(Currency.ENTITY_NAME,
            strCurrentCurId));
        newCosting.setStartingDate(newDate);
        newCosting.setEndingDate(dateTo);
        newCosting.setInventoryTransaction(null);
        newCosting.setProduct(basetrx.getProduct());
        if (isManufacturingProduct) {
          newCosting.setOrganization((Organization) OBDal.getInstance().getProxy(
              Organization.ENTITY_NAME, "0"));
        } else {
          newCosting.setOrganization((Organization) OBDal.getInstance().getProxy(
              Organization.ENTITY_NAME, strCostOrgId));
        }
        newCosting.setQuantity(null);
        newCosting.setTotalMovementQuantity(currentStock);
        newCosting.setPrice(null);
        newCosting.setCostType("AVA");
        newCosting.setManual(false);
        newCosting.setPermanent(true);
        newCosting.setProduction(trxType == TrxType.ManufacturingProduced);
        newCosting.setWarehouse((Warehouse) getCostDimensions().get(CostDimension.Warehouse));
        OBDal.getInstance().save(newCosting);
      }
    }
  }

  @Override
  protected void calculateBackdatedTrxAdjustment(CostAdjustmentLine costAdjLine) {
    MaterialTransaction trx = costAdjLine.getInventoryTransaction();
    TrxType calTrxType = TrxType.getTrxType(trx);
    if (AverageAlgorithm.modifiesAverage(calTrxType)) {
      // Move average to the movement date.
      Costing bdCosting = trx.getMaterialMgmtCostingList().get(0);
      Costing lastCosting = getLastCosting(bdCosting);
      lastCosting.setEndingDate(bdCosting.getEndingDate());
      OBDal.getInstance().save(lastCosting);
    }
    super.calculateBackdatedTrxAdjustment(costAdjLine);
  }

  @Override
  protected BigDecimal getOutgoingBackdatedTrxAdjAmt(CostAdjustmentLine costAdjLine) {
    // Calculate the average cost on the transaction's movement date and adjust the cost if needed.
    MaterialTransaction trx = costAdjLine.getInventoryTransaction();
    Costing costing = getTrxProductCost(trx, getCostDimensions(), getCostOrg());

    if (costing == null) {
      throw new OBException("@NoAvgCostDefined@ @Organization@: " + getCostOrg().getName()
          + ", @Product@: " + trx.getProduct().getName() + ", @Date@: "
          + OBDateUtils.formatDate(trx.getMovementDate()));
    }
    BigDecimal cost = costing.getCost();
    Currency costCurrency = getCostCurrency();
    if (costing.getCurrency() != costCurrency) {
      cost = FinancialUtils.getConvertedAmount(costing.getCost(), costing.getCurrency(),
          costCurrency, trx.getTransactionProcessDate(), getCostOrg(),
          FinancialUtils.PRECISION_COSTING);
    }
    BigDecimal expectedCostAmt = trx.getMovementQuantity().abs().multiply(cost);
    BigDecimal currentCost = trx.getTransactionCost();
    return expectedCostAmt.subtract(currentCost);
  }

  private ScrollableResults getRelatedTransactions() {
    CostingRule costingRule = getCostingRule();
    HashMap<CostDimension, BaseOBObject> costDimensions = getCostDimensions();
    OrganizationStructureProvider osp = OBContext.getOBContext().getOrganizationStructureProvider(
        costingRule.getClient().getId());
    Set<String> orgs = osp.getChildTree(strCostOrgId, true);
    if (isManufacturingProduct) {
      orgs = osp.getChildTree("0", false);
      costDimensions = CostingUtils.getEmptyDimensions();
    }
    Warehouse warehouse = (Warehouse) costDimensions.get(CostDimension.Warehouse);
    MaterialTransaction trx = getTransaction();

    StringBuffer where = new StringBuffer();
    where.append(" as trx");
    where.append(" join trx." + Product.PROPERTY_ORGANIZATION + " as org");
    where.append(" join trx." + Product.PROPERTY_STORAGEBIN + " as loc");
    where.append(" where trx." + MaterialTransaction.PROPERTY_ISCOSTCALCULATED + " = true");
    where.append("  and trx." + MaterialTransaction.PROPERTY_PRODUCT + " = :product");
    // Consider only transactions with movement date equal or later than the movement date of the
    // adjusted transaction. But for transactions with the same movement date only those with a
    // transaction date after the process date of the adjusted transaction.
    where.append("  and (trx." + MaterialTransaction.PROPERTY_MOVEMENTDATE + " > :mvtdate");
    where.append("    or (trx." + MaterialTransaction.PROPERTY_MOVEMENTDATE + " = :mvtdate");
    where.append("  and trx." + MaterialTransaction.PROPERTY_TRANSACTIONPROCESSDATE
        + " > :trxdate ))");
    where.append("  and org.id in (:orgs)");
    if (warehouse != null) {
      where.append("  and loc." + Locator.PROPERTY_WAREHOUSE + " = :warehouse");
    }
    if (costingRule.getEndingDate() != null) {
      where.append("  and trx." + MaterialTransaction.PROPERTY_TRANSACTIONPROCESSDATE
          + " <= :enddate");
    }
    where.append("  and trx." + MaterialTransaction.PROPERTY_TRANSACTIONPROCESSDATE
        + " > :startdate ");
    where.append(" order by trx." + MaterialTransaction.PROPERTY_MOVEMENTDATE);
    where.append("   , trx." + MaterialTransaction.PROPERTY_TRANSACTIONPROCESSDATE);
    where.append("   , trx." + MaterialTransaction.PROPERTY_MOVEMENTLINE);
    // This makes M- to go before M+. In Oracle it must go with desc as if not, M+ would go before
    // M-.
    if (OBPropertiesProvider.getInstance().getOpenbravoProperties().getProperty("bbdd.rdbms")
        .equalsIgnoreCase("oracle")) {
      where.append("   , trx." + MaterialTransaction.PROPERTY_MOVEMENTTYPE + " desc ");
    } else {
      where.append("   , trx." + MaterialTransaction.PROPERTY_MOVEMENTTYPE);
    }
    OBQuery<MaterialTransaction> trxQry = OBDal.getInstance().createQuery(
        MaterialTransaction.class, where.toString());
    trxQry.setFilterOnReadableOrganization(false);
    trxQry.setFilterOnReadableClients(false);
    trxQry.setNamedParameter("mvtdate", trx.getMovementDate());
    trxQry.setNamedParameter("trxdate", trx.getTransactionProcessDate());
    if (costingRule.getEndingDate() != null) {
      trxQry.setNamedParameter("enddate", costingRule.getEndingDate());
    }
    trxQry.setNamedParameter("startdate", costingRule.getStartingDate());
    trxQry.setNamedParameter("orgs", orgs);
    trxQry.setNamedParameter("product", trx.getProduct());
    if (warehouse != null) {
      trxQry.setNamedParameter("warehouse", warehouse);
    }

    return trxQry.scroll(ScrollMode.FORWARD_ONLY);
  }

  /**
   * Calculates the value of the stock of the product on the given date, for the given cost
   * dimensions and for the given currency. It only takes transactions that have its cost
   * calculated.
   */
  private BigDecimal getCurrentValuedStock(MaterialTransaction trx) {
    Organization org = getCostOrg();
    Currency currency = getCostCurrency();
    HashMap<CostDimension, BaseOBObject> costDimensions = getCostDimensions();

    // Get child tree of organizations.
    OrganizationStructureProvider osp = OBContext.getOBContext().getOrganizationStructureProvider(
        trx.getClient().getId());
    Set<String> orgs = osp.getChildTree(org.getId(), true);
    if (isManufacturingProduct) {
      orgs = osp.getChildTree("0", false);
      costDimensions = CostingUtils.getEmptyDimensions();
    }

    StringBuffer select = new StringBuffer();
    select.append(" select sum(case");
    select.append("     when trx." + MaterialTransaction.PROPERTY_MOVEMENTQUANTITY
        + " < 0 then -tc." + TransactionCost.PROPERTY_COST);
    select.append("     else tc." + TransactionCost.PROPERTY_COST + " end ) as cost");
    select.append(" , tc." + TransactionCost.PROPERTY_CURRENCY + ".id as currency");
    select.append(" , coalesce(sr." + ShipmentInOut.PROPERTY_ACCOUNTINGDATE + ", trx."
        + MaterialTransaction.PROPERTY_MOVEMENTDATE + ") as mdate");
    select.append(" , sum(trx." + MaterialTransaction.PROPERTY_MOVEMENTQUANTITY + ") as stock");

    select.append(" from " + TransactionCost.ENTITY_NAME + " as tc");
    select.append("  join tc." + TransactionCost.PROPERTY_INVENTORYTRANSACTION + " as trx");
    select.append("  join trx." + MaterialTransaction.PROPERTY_STORAGEBIN + " as locator");
    select.append("  left join trx." + MaterialTransaction.PROPERTY_GOODSSHIPMENTLINE + " as line");
    select.append("  left join line." + ShipmentInOutLine.PROPERTY_SHIPMENTRECEIPT + " as sr");

    select.append(" where trx." + MaterialTransaction.PROPERTY_PRODUCT + " = :product");
    // Include only transactions that have its cost calculated
    select.append("   and trx." + MaterialTransaction.PROPERTY_ISCOSTCALCULATED + " = true");
    // Consider only transactions with movement date equal or lower than the movement date of the
    // adjusted transaction. But for transactions with the same movement date only those with a
    // transaction date equal or before the process date of the adjusted transaction.
    select.append("   and (trx." + MaterialTransaction.PROPERTY_MOVEMENTDATE + " < :mvtdate");
    select.append("     or (trx." + MaterialTransaction.PROPERTY_MOVEMENTDATE + " = :mvtdate");
    select.append("   and trx." + MaterialTransaction.PROPERTY_TRANSACTIONPROCESSDATE
        + " <= :trxdate ))");
    if (costDimensions.get(CostDimension.Warehouse) != null) {
      select.append("  and locator." + Locator.PROPERTY_WAREHOUSE + ".id = :warehouse");
    }
    select.append("   and trx." + MaterialTransaction.PROPERTY_ORGANIZATION + ".id in (:orgs)");

    select.append(" group by tc." + TransactionCost.PROPERTY_CURRENCY);
    select.append("   , coalesce(sr." + ShipmentInOut.PROPERTY_ACCOUNTINGDATE + ", trx."
        + MaterialTransaction.PROPERTY_MOVEMENTDATE + ")");

    Query trxQry = OBDal.getInstance().getSession().createQuery(select.toString());
    trxQry.setParameter("product", trx.getProduct());
    trxQry.setParameter("mvtdate", trx.getMovementDate());
    trxQry.setParameter("trxdate", trx.getTransactionProcessDate());
    if (costDimensions.get(CostDimension.Warehouse) != null) {
      trxQry.setParameter("warehouse", costDimensions.get(CostDimension.Warehouse).getId());
    }
    trxQry.setParameterList("orgs", orgs);
    @SuppressWarnings("unchecked")
    List<Object[]> o = trxQry.list();
    BigDecimal costsum = BigDecimal.ZERO;
    for (Object[] resultSet : o) {
      BigDecimal origAmt = (BigDecimal) resultSet[0];
      Currency origCur = OBDal.getInstance().get(Currency.class, resultSet[1]);
      Date convDate = (Date) resultSet[2];

      if (origCur != currency) {
        costsum = costsum.add(FinancialUtils.getConvertedAmount(origAmt, origCur, currency,
            convDate, org, FinancialUtils.PRECISION_COSTING));
      } else {
        costsum = costsum.add(origAmt);
      }
    }
    return costsum;
  }

  private List<CostAdjustmentLine> getTrxAdjustmentLines(MaterialTransaction trx) {
    OBCriteria<CostAdjustmentLine> critLines = OBDal.getInstance().createCriteria(
        CostAdjustmentLine.class);
    critLines.add(Restrictions.eq(CostAdjustmentLine.PROPERTY_COSTADJUSTMENT, getCostAdj()));
    critLines.add(Restrictions.eq(CostAdjustmentLine.PROPERTY_INVENTORYTRANSACTION, trx));
    critLines.add(Restrictions.eq(CostAdjustmentLine.PROPERTY_ISRELATEDTRANSACTIONADJUSTED, false));

    return critLines.list();
  }

  @Override
  protected void calculateNegativeStockCorrectionAdjustmentAmount(CostAdjustmentLine costAdjLine) {
    MaterialTransaction basetrx = costAdjLine.getInventoryTransaction();
    BigDecimal currentStock = CostingUtils.getCurrentStock(getCostOrg(), basetrx,
        getCostDimensions(), isManufacturingProduct);
    BigDecimal currentValueAmt = getCurrentValuedStock(basetrx);
    int precission = getCostCurrency().getCostingPrecision().intValue();

    BigDecimal trxCost = CostAdjustmentUtils.getTrxCost(basetrx, false, getCostCurrency());
    BigDecimal trxUnitCost = trxCost.divide(basetrx.getMovementQuantity(), precission);
    // BigDecimal totalStock = currentStock.add(basetrx.getMovementQuantity());
    // BigDecimal adjustAmt = totalStock.multiply(trxUnitCost).subtract(trxCost)
    // .subtract(currentValueAmt);
    BigDecimal adjustAmt = currentStock.multiply(trxUnitCost).subtract(currentValueAmt);

    costAdjLine.setCurrency((Currency) OBDal.getInstance().getProxy(Currency.ENTITY_NAME,
        strCostCurrencyId));
    costAdjLine.setAdjustmentAmount(adjustAmt);
    OBDal.getInstance().save(costAdjLine);
  }

  /**
   * Calculates the value of the stock of the product on the given date, for the given cost
   * dimensions and for the given currency. It only takes transactions that have its cost
   * calculated.
   */
  protected static Costing getTrxProductCost(MaterialTransaction trx,
      HashMap<CostDimension, BaseOBObject> costDimensions, Organization costOrg) {

    // Get child tree of organizations.
    OrganizationStructureProvider osp = OBContext.getOBContext().getOrganizationStructureProvider(
        costOrg.getClient().getId());
    Set<String> orgs = osp.getChildTree(costOrg.getId(), true);

    StringBuffer select = new StringBuffer();
    select.append(" select c ");

    select.append(" from " + Costing.ENTITY_NAME + " as c");
    select.append("  join c." + TransactionCost.PROPERTY_INVENTORYTRANSACTION + " as trx");
    select.append("  join trx." + MaterialTransaction.PROPERTY_STORAGEBIN + " as locator");

    select.append(" where trx." + MaterialTransaction.PROPERTY_PRODUCT + ".id = :product");
    // Consider only transactions with movement date equal or lower than the movement date of the
    // adjusted transaction. But for transactions with the same movement date only those with a
    // transaction date equal or before the process date of the adjusted transaction.
    select.append("   and (trx." + MaterialTransaction.PROPERTY_MOVEMENTDATE + " < :mvtdate");
    select.append("     or (trx." + MaterialTransaction.PROPERTY_MOVEMENTDATE + " = :mvtdate");
    select.append("   and trx." + MaterialTransaction.PROPERTY_TRANSACTIONPROCESSDATE
        + " <= :trxdate ))");
    // Include only transactions that have its cost calculated
    select.append("   and trx." + MaterialTransaction.PROPERTY_ISCOSTCALCULATED + " = true");
    if (costDimensions.get(CostDimension.Warehouse) != null) {
      select.append("  and locator." + Locator.PROPERTY_WAREHOUSE + ".id = :warehouse");
    }
    select.append("   and trx." + MaterialTransaction.PROPERTY_ORGANIZATION + ".id in (:orgs)");
    select.append(" order by trx." + MaterialTransaction.PROPERTY_MOVEMENTDATE + ", trx."
        + MaterialTransaction.PROPERTY_TRANSACTIONPROCESSDATE);

    Query trxQry = OBDal.getInstance().getSession().createQuery(select.toString());
    trxQry.setParameter("product", trx.getProduct().getId());
    trxQry.setParameter("mvtdate", trx.getMovementDate());
    trxQry.setParameter("trxdate", trx.getTransactionProcessDate());

    if (costDimensions.get(CostDimension.Warehouse) != null) {
      trxQry.setParameter("warehouse", costDimensions.get(CostDimension.Warehouse).getId());
    }
    trxQry.setParameterList("orgs", orgs);
    @SuppressWarnings("unchecked")
    List<Costing> o = trxQry.list();
    if (o.size() == 0) {
      return null;
    }
    return o.get(0);
  }

  private Costing getLastCosting(Costing bdCosting) {
    StringBuffer where = new StringBuffer();
    where.append(" as c");
    where.append("  join c." + Costing.PROPERTY_INVENTORYTRANSACTION + " as trx");
    where.append(" where c." + Costing.PROPERTY_PRODUCT + " = :product");
    where.append("   and c." + Costing.PROPERTY_ORGANIZATION + " = :org");
    if (bdCosting.getWarehouse() == null) {
      where.append(" and c." + Costing.PROPERTY_WAREHOUSE + " is null");
    } else {
      where.append(" and c." + Costing.PROPERTY_WAREHOUSE + " = :warehouse");
    }
    where.append("   and c." + Costing.PROPERTY_ENDINGDATE + " = :endDate");
    where.append(" order by trx." + MaterialTransaction.PROPERTY_MOVEMENTDATE + " desc");
    where.append("   , trx." + MaterialTransaction.PROPERTY_TRANSACTIONPROCESSDATE + " desc");

    OBQuery<Costing> qryCosting = OBDal.getInstance().createQuery(Costing.class, where.toString());
    qryCosting.setNamedParameter("product", bdCosting.getProduct());
    qryCosting.setNamedParameter("org", bdCosting.getOrganization());
    if (bdCosting.getWarehouse() != null) {
      qryCosting.setNamedParameter("warehouse", bdCosting.getWarehouse());
    }
    qryCosting.setNamedParameter("endDate", bdCosting.getStartingDate());

    qryCosting.setMaxResult(1);

    return qryCosting.uniqueResult();
  }

  private Costing getPreviousCosting(MaterialTransaction trx) {
    HashMap<CostDimension, BaseOBObject> costDimensions = getCostDimensions();
    StringBuffer where = new StringBuffer();
    where.append(" as c");
    where.append(" where c." + Costing.PROPERTY_PRODUCT + " = :product");
    where.append("   and c." + Costing.PROPERTY_ORGANIZATION + " = :org");
    if (costDimensions.get(CostDimension.Warehouse) == null) {
      where.append(" and c." + Costing.PROPERTY_WAREHOUSE + " is null");
    } else {
      where.append(" and c." + Costing.PROPERTY_WAREHOUSE + " = :warehouse");
    }
    where.append("   and c." + Costing.PROPERTY_ENDINGDATE + " >= :trxdate");
    where.append("   and c." + Costing.PROPERTY_STARTINGDATE + " < :trxdate");
    where.append(" order by c." + Costing.PROPERTY_STARTINGDATE + " desc");

    OBQuery<Costing> qryCosting = OBDal.getInstance().createQuery(Costing.class, where.toString());
    qryCosting.setNamedParameter("product", trx.getProduct());
    qryCosting.setNamedParameter("org",
        isManufacturingProduct ? OBDal.getInstance().get(Organization.class, "0") : getCostOrg());
    if (costDimensions.get(CostDimension.Warehouse) != null) {
      qryCosting.setNamedParameter("warehouse", costDimensions.get(CostDimension.Warehouse));
    }
    qryCosting.setNamedParameter("trxdate", trx.getTransactionProcessDate());

    qryCosting.setMaxResult(1);

    return qryCosting.uniqueResult();
  }

  private boolean isVoidedTrx(MaterialTransaction trx, TrxType currentTrxType) {
    // Transactions of voided documents do not need adjustment
    switch (currentTrxType) {
    case ReceiptVoid:
    case ShipmentVoid:
    case InternalConsVoid:
      return true;
    case Receipt:
    case ReceiptNegative:
    case ReceiptReturn:
    case Shipment:
    case ShipmentNegative:
    case ShipmentReturn:
      if (trx.getGoodsShipmentLine().getShipmentReceipt().getDocumentStatus().equals("VO")) {
        return true;
      }
      break;
    case InternalCons:
    case InternalConsNegative:
      if (trx.getInternalConsumptionLine().getInternalConsumption().getStatus().equals("VO")) {
        return true;
      }
      break;
    default:
      break;
    }
    return false;
  }
}
