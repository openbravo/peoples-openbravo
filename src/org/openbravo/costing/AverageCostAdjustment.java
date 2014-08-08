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
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.session.OBPropertiesProvider;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.client.kernel.ComponentProvider;
import org.openbravo.costing.CostingAlgorithm.CostDimension;
import org.openbravo.costing.CostingServer.TrxType;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.security.OrganizationStructureProvider;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.financial.FinancialUtils;
import org.openbravo.model.common.currency.Currency;
import org.openbravo.model.common.enterprise.Locator;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.common.enterprise.Warehouse;
import org.openbravo.model.common.plm.Product;
import org.openbravo.model.materialmgmt.cost.CostAdjustment;
import org.openbravo.model.materialmgmt.cost.CostAdjustmentLine;
import org.openbravo.model.materialmgmt.cost.Costing;
import org.openbravo.model.materialmgmt.cost.CostingRule;
import org.openbravo.model.materialmgmt.cost.TransactionCost;
import org.openbravo.model.materialmgmt.transaction.MaterialTransaction;
import org.openbravo.model.materialmgmt.transaction.ShipmentInOut;
import org.openbravo.model.materialmgmt.transaction.ShipmentInOutLine;

@ComponentProvider.Qualifier("B069080A0AE149A79CF1FA0E24F16AB6")
public class AverageCostAdjustment extends CostingAlgorithmAdjustmentImp {

  @Override
  void getRelatedTransactionsByAlgorithm() {
    // Search all transactions after the date of the adjusted line and recalculate the costs of them
    // to adjust differences
    MaterialTransaction basetrx = getTransaction();
    // Transactions of closing inventories are managed by generic CostAdjustmentProcess adjusting
    // the cost of the related opening inventory.
    if (basetrx.getPhysicalInventoryLine() != null
        && basetrx.getPhysicalInventoryLine().getRelatedInventory() != null) {
      return;
    }
    BigDecimal adjustmentBalance = BigDecimal.ZERO;
    CostAdjustmentLine costAdjLine = getCostAdjLine();
    if (costAdjLine.getTransactionCostList().isEmpty()) {
      adjustmentBalance = costAdjLine.getAdjustmentAmount();
    }
    Date trxDate = costAdjLine.getTransactionDate();
    if (trxDate == null) {
      trxDate = basetrx.getTransactionProcessDate();
    }
    Costing costing = AverageAlgorithm.getProductCost(trxDate, basetrx.getProduct(),
        getCostDimensions(), getCostOrg());
    BigDecimal cost = costing.getCost();
    int precission = getCostCurrency().getCostingPrecision().intValue();

    BigDecimal currentStock = getCurrentStock();
    BigDecimal currentValueAmt = getCurrentValuedStock();
    if (currentStock.signum() != 0) {
      cost = currentValueAmt.add(adjustmentBalance).divide(currentStock, precission,
          RoundingMode.HALF_UP);
    }
    ScrollableResults trxs = getRelatedTransactions();
    try {
      while (trxs.next()) {
        MaterialTransaction trx = (MaterialTransaction) trxs.get()[0];
        CostAdjustmentLine adjustedLine = getAdjustmentLine(trx);
        if (adjustedLine != null) {
          adjustedLine.setRelatedTransactionAdjusted(true);
          adjustedLine.setParentCostAdjustmentLine((CostAdjustmentLine) OBDal.getInstance()
              .getProxy(CostAdjustmentLine.ENTITY_NAME, strCostAdjLineId));
        }
        BigDecimal trxCost = getTrxCost(trx, false);
        currentValueAmt = currentValueAmt.add(trxCost.multiply(new BigDecimal(trx
            .getMovementQuantity().signum())));
        currentStock = currentStock.add(trx.getMovementQuantity());

        TrxType currentTrxType = TrxType.getTrxType(trx);
        // Transactions of voided documents do not need adjustment
        switch (currentTrxType) {
        case ReceiptVoid:
        case ShipmentVoid:
        case InternalConsVoid:
          continue;
        case Receipt:
        case ReceiptNegative:
        case ReceiptReturn:
        case Shipment:
        case ShipmentNegative:
        case ShipmentReturn:
          if (trx.getGoodsShipmentLine().getShipmentReceipt().getDocumentStatus().equals("VO")) {
            continue;
          }
          break;
        case InternalCons:
        case InternalConsNegative:
          if (trx.getInternalConsumptionLine().getInternalConsumption().getStatus().equals("VO")) {
            continue;
          }
          break;
        default:
          break;
        }

        // FIXME: Also check decreasing inventories with custom cost.
        if (AverageAlgorithm.modifiesAverage(currentTrxType)) {
          // Recalculate average, if current stock is zero the average is not modified
          if (currentStock.signum() != 0) {
            cost = currentValueAmt.add(adjustmentBalance).divide(currentStock, precission,
                RoundingMode.HALF_UP);
          }
          Costing curCosting = trx.getMaterialMgmtCostingList().get(0);
          if (curCosting.getCost().compareTo(cost) == 0) {
            // new cost hasn't changed, following transactions will have the same cost, so no more
            // related transactions are needed to include.
            return;
          }
        } else if (!trx.isManualcostadjustment() && adjustedLine == null) {
          // Check current trx unit cost matches new expected cost
          BigDecimal expectedCost = cost.multiply(trx.getMovementQuantity().abs());
          BigDecimal unitCost = getTrxCost(trx, true);
          if (expectedCost.compareTo(unitCost) != 0) {
            adjustmentBalance = adjustmentBalance.add(expectedCost.subtract(unitCost).multiply(
                new BigDecimal(trx.getMovementQuantity().signum())));
            // If there is a difference insert a cost adjustment line.
            CostAdjustmentLine newCAL = insertCostAdjustmentLine(trx,
                expectedCost.subtract(unitCost), getCostAdjLine());
            newCAL.setRelatedTransactionAdjusted(true);
          }
        }

        OBDal.getInstance().flush();
        OBDal.getInstance().getSession().clear();
      }
    } finally {
      trxs.close();
    }

    if (getCostingRule().getEndingDate() == null) {
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
            strCostCurrencyId));
        newCosting.setStartingDate(newDate);
        newCosting.setEndingDate(dateTo);
        newCosting.setInventoryTransaction(null);
        newCosting.setProduct(basetrx.getProduct());
        if (basetrx.getProduct().isProduction()) {
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

  private BigDecimal getTrxCost(MaterialTransaction trx, boolean justUnitCost) {
    StringBuffer select = new StringBuffer();
    select.append("select sum(cal." + CostAdjustmentLine.PROPERTY_ADJUSTMENTAMOUNT + ") as cost");
    select.append(" from " + CostAdjustmentLine.ENTITY_NAME + " as cal");
    select.append("   join cal." + CostAdjustmentLine.PROPERTY_COSTADJUSTMENT + " as ca");
    select.append(" where cal." + CostAdjustmentLine.PROPERTY_INVENTORYTRANSACTION + ".id = :trx");
    if (justUnitCost) {
      select.append("  and cal." + CostAdjustmentLine.PROPERTY_UNITCOST + " = true");
    }
    // Get amounts of processed adjustments and the adjustment that it is being processed.
    select.append("   and (ca = :ca");
    select.append("     or ca." + CostAdjustment.PROPERTY_PROCESSED + " = true)");
    // TODO: Add filter to consider only costs based on the algorithm (avoid landed cost)

    Query qryCost = OBDal.getInstance().getSession().createQuery(select.toString());
    qryCost.setParameter("trx", trx.getId());
    qryCost.setParameter("ca", getCostAdjLine().getCostAdjustment());

    Object adjCost = qryCost.uniqueResult();
    BigDecimal cost = trx.getTransactionCost();
    if (adjCost != null) {
      cost = cost.add((BigDecimal) adjCost);
    }
    return cost;
  }

  @Override
  BigDecimal getOutgoingBackdatedTrxAdjAmt() {
    // Calculate the average cost on the transaction's movement date and adjust the cost if needed.
    MaterialTransaction trx = getTransaction();
    CostAdjustmentLine costAdjLine = getCostAdjLine();
    Costing costing = AverageAlgorithm.getProductCost(costAdjLine.getTransactionDate(),
        trx.getProduct(), getCostDimensions(), getCostOrg());
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
    OrganizationStructureProvider osp = OBContext.getOBContext().getOrganizationStructureProvider(
        costingRule.getClient().getId());
    Set<String> orgs = osp.getChildTree(strCostOrgId, true);
    HashMap<CostDimension, BaseOBObject> costDimensions = getCostDimensions();
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
    if (costingRule.getEndingDate() != null) {
      where.append("  and trx." + MaterialTransaction.PROPERTY_TRANSACTIONPROCESSDATE
          + " <= :enddate");
    }
    where.append("  and org.id in (:orgs)");
    if (warehouse != null) {
      where.append("  and loc." + Locator.PROPERTY_WAREHOUSE + " = :warehouse");
    }
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
    trxQry.setNamedParameter("mvtdate", trx.getMovementDate());
    trxQry.setNamedParameter("trxdate", trx.getTransactionProcessDate());
    if (costingRule.getEndingDate() != null) {
      trxQry.setNamedParameter("enddate", costingRule.getEndingDate());
    }
    trxQry.setNamedParameter("orgs", orgs);
    trxQry.setNamedParameter("product", trx.getProduct());
    if (warehouse != null) {
      trxQry.setNamedParameter("warehouse", warehouse);
    }

    return trxQry.scroll(ScrollMode.FORWARD_ONLY);
  }

  /**
   * Calculates the stock of the product on the given date and for the given cost dimensions. It
   * only takes transactions that have its cost calculated.
   */
  private BigDecimal getCurrentStock() {
    // Get child tree of organizations.
    Set<String> orgs = OBContext.getOBContext().getOrganizationStructureProvider()
        .getChildTree(strCostOrgId, true);
    HashMap<CostDimension, BaseOBObject> costDimensions = getCostDimensions();
    MaterialTransaction trx = getTransaction();

    StringBuffer select = new StringBuffer();
    select
        .append(" select sum(trx." + MaterialTransaction.PROPERTY_MOVEMENTQUANTITY + ") as stock");
    select.append(" from " + MaterialTransaction.ENTITY_NAME + " as trx");
    select.append("   join trx." + MaterialTransaction.PROPERTY_STORAGEBIN + " as locator");
    select.append(" where trx." + MaterialTransaction.PROPERTY_PRODUCT + " = :product");
    // Include only transactions that have its cost calculated. Should be all.
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
    Query trxQry = OBDal.getInstance().getSession().createQuery(select.toString());
    trxQry.setParameter("product", trx.getProduct());
    trxQry.setParameter("mvtdate", trx.getMovementDate());
    trxQry.setParameter("trxdate", trx.getTransactionProcessDate());
    if (costDimensions.get(CostDimension.Warehouse) != null) {
      trxQry.setParameter("warehouse", costDimensions.get(CostDimension.Warehouse).getId());
    }
    trxQry.setParameterList("orgs", orgs);
    Object stock = trxQry.uniqueResult();
    if (stock != null) {
      return (BigDecimal) stock;
    }
    return BigDecimal.ZERO;
  }

  /**
   * Calculates the value of the stock of the product on the given date, for the given cost
   * dimensions and for the given currency. It only takes transactions that have its cost
   * calculated.
   */
  private BigDecimal getCurrentValuedStock() {
    Organization org = getCostOrg();
    Currency currency = getCostCurrency();
    MaterialTransaction trx = getTransaction();
    HashMap<CostDimension, BaseOBObject> costDimensions = getCostDimensions();

    // Get child tree of organizations.
    Set<String> orgs = OBContext.getOBContext().getOrganizationStructureProvider()
        .getChildTree(org.getId(), true);

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

  private CostAdjustmentLine getAdjustmentLine(MaterialTransaction trx) {
    OBCriteria<CostAdjustmentLine> critCAL = OBDal.getInstance().createCriteria(
        CostAdjustmentLine.class);
    critCAL.add(Restrictions.eq(CostAdjustmentLine.PROPERTY_INVENTORYTRANSACTION, trx));
    critCAL.add(Restrictions.eq(CostAdjustmentLine.PROPERTY_PARENTCOSTADJUSTMENTLINE, OBDal
        .getInstance().getProxy(CostAdjustmentLine.ENTITY_NAME, strCostAdjLineId)));
    return (CostAdjustmentLine) critCAL.uniqueResult();
  }
}
