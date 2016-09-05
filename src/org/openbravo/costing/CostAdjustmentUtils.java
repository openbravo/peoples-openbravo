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
 * All portions are Copyright (C) 2014-2016 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 *************************************************************************
 */
package org.openbravo.costing;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.hibernate.Query;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.criterion.Restrictions;
import org.openbravo.advpaymentmngt.utility.FIN_Utility;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.costing.CostingAlgorithm.CostDimension;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.security.OrganizationStructureProvider;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.financial.FinancialUtils;
import org.openbravo.model.common.currency.Currency;
import org.openbravo.model.common.enterprise.DocumentType;
import org.openbravo.model.common.enterprise.Locator;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.common.enterprise.Warehouse;
import org.openbravo.model.common.plm.AttributeSetInstance;
import org.openbravo.model.common.plm.Product;
import org.openbravo.model.materialmgmt.cost.CostAdjustment;
import org.openbravo.model.materialmgmt.cost.CostAdjustmentLine;
import org.openbravo.model.materialmgmt.cost.Costing;
import org.openbravo.model.materialmgmt.cost.CostingRule;
import org.openbravo.model.materialmgmt.cost.TransactionCost;
import org.openbravo.model.materialmgmt.transaction.InventoryCount;
import org.openbravo.model.materialmgmt.transaction.InventoryCountLine;
import org.openbravo.model.materialmgmt.transaction.MaterialTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CostAdjustmentUtils {
  private static final Logger log4j = LoggerFactory.getLogger(CostAdjustmentUtils.class);
  public static final String strCategoryCostAdj = "CAD";
  public static final String strTableCostAdj = "M_CostAdjustment";
  public static final String propADListPriority = org.openbravo.model.ad.domain.List.PROPERTY_SEQUENCENUMBER;
  public static final String propADListReference = org.openbravo.model.ad.domain.List.PROPERTY_REFERENCE;
  public static final String propADListValue = org.openbravo.model.ad.domain.List.PROPERTY_SEARCHKEY;
  public static final String MovementTypeRefID = "189";
  public static final String ENABLE_AUTO_PRICE_CORRECTION_PREF = "enableAutomaticPriceCorrectionTrxs";
  public static final String ENABLE_NEGATIVE_STOCK_CORRECTION_PREF = "enableNegativeStockCorrections";

  /**
   * Returns a new header for a Cost Adjustment
   * 
   * @param org
   *          organization set in record
   * 
   * @param sourceProcess
   *          the process that origin the Cost Adjustment: - MCC: Manual Cost Correction - IAU:
   *          Inventory Amount Update - PDC: Price Difference Correction - LC: Landed Cost - BDT:
   *          Backdated Transaction
   */
  public static CostAdjustment insertCostAdjustmentHeader(Organization org, String sourceProcess) {

    final DocumentType docType = FIN_Utility.getDocumentType(org, strCategoryCostAdj);
    final String docNo = FIN_Utility.getDocumentNo(docType, strTableCostAdj);

    CostAdjustment costAdjustment = OBProvider.getInstance().get(CostAdjustment.class);
    costAdjustment.setOrganization(org);
    costAdjustment.setDocumentType(docType);
    costAdjustment.setDocumentNo(docNo);
    costAdjustment.setReferenceDate(new Date());
    costAdjustment.setSourceProcess(sourceProcess);
    costAdjustment.setProcessed(Boolean.FALSE);
    OBDal.getInstance().save(costAdjustment);

    return costAdjustment;
  }

  /**
   * Creates a new Cost Adjustment Line and returns it.
   * 
   * @param transaction
   *          transaction to apply the cost adjustment
   * 
   * @param costAdjustmentHeader
   *          header of line
   * 
   * @param costAdjusted
   *          amount to adjust in the cost
   * 
   * @param isSource
   */
  public static CostAdjustmentLine insertCostAdjustmentLine(MaterialTransaction transaction,
      CostAdjustment costAdjustmentHeader, BigDecimal costAdjusted, boolean isSource,
      Date accountingDate) {
    Long stdPrecission = transaction.getCurrency().getStandardPrecision();
    CostAdjustmentLine costAdjustmentLine = OBProvider.getInstance().get(CostAdjustmentLine.class);
    costAdjustmentLine.setOrganization(costAdjustmentHeader.getOrganization());
    costAdjustmentLine.setCostAdjustment(costAdjustmentHeader);
    if (costAdjusted == null) {
      costAdjustmentLine.setAdjustmentAmount(null);
    } else {
      costAdjustmentLine.setAdjustmentAmount(costAdjusted.setScale(stdPrecission.intValue(),
          RoundingMode.HALF_UP));
    }
    costAdjustmentLine.setCurrency(transaction.getCurrency());
    costAdjustmentLine.setInventoryTransaction(transaction);
    costAdjustmentLine.setSource(isSource);
    costAdjustmentLine.setAccountingDate(accountingDate);
    costAdjustmentLine.setLineNo(getNewLineNo(costAdjustmentHeader));

    OBDal.getInstance().save(costAdjustmentLine);

    return costAdjustmentLine;
  }

  /**
   * Calculates if the given Material Transaction is a beckdated transaction or not.
   */
  public static boolean isNeededBackdatedCostAdjustment(MaterialTransaction transaction,
      boolean includeWarehouseDimension, Date startingDate) {

    final String orgLegalId = OBContext.getOBContext()
        .getOrganizationStructureProvider(transaction.getClient().getId())
        .getLegalEntity(transaction.getOrganization()).getId();
    // Get child tree of organizations.
    Set<String> orgs = OBContext.getOBContext().getOrganizationStructureProvider()
        .getChildTree(orgLegalId, true);

    StringBuffer where = new StringBuffer();
    where.append(" as trx");
    if (includeWarehouseDimension) {
      where.append("\n join trx." + MaterialTransaction.PROPERTY_STORAGEBIN + " as locator");
    }
    where.append("\n , " + org.openbravo.model.ad.domain.List.ENTITY_NAME + " as trxtype");
    where.append("\n where trxtype." + propADListReference + ".id = :refid");
    where.append("  and trxtype." + propADListValue + " = trx."
        + MaterialTransaction.PROPERTY_MOVEMENTTYPE);
    where.append("   and trx." + MaterialTransaction.PROPERTY_TRANSACTIONPROCESSDATE
        + " > :crStartDate");
    where.append("   and trx." + MaterialTransaction.PROPERTY_ISCOSTCALCULATED + " = 'Y' ");
    // If there are more than one trx on the same trx process date filter out those types with less
    // priority and / or higher quantity.
    where.append(" and (");
    where.append("  trx." + MaterialTransaction.PROPERTY_TRANSACTIONPROCESSDATE + " < :trxDate");
    where.append("  or (");
    where.append("   trx." + MaterialTransaction.PROPERTY_TRANSACTIONPROCESSDATE + " = :trxDate");
    where.append("   and trxtype." + propADListPriority + " < :trxtypeprio");
    where.append("  )");
    where.append("  or (");
    where.append("   trx." + MaterialTransaction.PROPERTY_TRANSACTIONPROCESSDATE + " = :trxDate");
    where.append("   and trxtype." + propADListPriority + " = :trxtypeprio");
    where.append("   and trx." + MaterialTransaction.PROPERTY_MOVEMENTQUANTITY + " >= :trxqty");
    where.append(" ))");

    where.append("   and trunc(trx." + MaterialTransaction.PROPERTY_MOVEMENTDATE
        + ") > :movementDate");
    where.append("   and trx." + MaterialTransaction.PROPERTY_PRODUCT + ".id = :productId");
    where.append("   and trx." + MaterialTransaction.PROPERTY_ORGANIZATION + ".id in (:orgs)");

    if (includeWarehouseDimension) {
      where.append("  and locator." + Locator.PROPERTY_WAREHOUSE + ".id = :warehouse");
    }

    OBQuery<MaterialTransaction> trxQry = OBDal.getInstance().createQuery(
        MaterialTransaction.class, where.toString());

    trxQry.setNamedParameter("refid", MovementTypeRefID);
    trxQry.setNamedParameter("crStartDate", startingDate);
    trxQry.setNamedParameter("trxDate", transaction.getTransactionProcessDate());
    trxQry.setNamedParameter("movementDate", transaction.getMovementDate());
    trxQry.setNamedParameter("productId", transaction.getProduct().getId());
    trxQry.setNamedParameter("trxtypeprio",
        CostAdjustmentUtils.getTrxTypePrio(transaction.getMovementType()));
    trxQry.setNamedParameter("trxqty", transaction.getMovementQuantity());
    trxQry.setNamedParameter("orgs", orgs);
    if (includeWarehouseDimension) {
      trxQry.setNamedParameter("warehouse", transaction.getStorageBin().getWarehouse().getId());
    }
    trxQry.setMaxResult(1);
    Object res = trxQry.uniqueResult();

    return res != null;
  }

  private static Long getNewLineNo(CostAdjustment cadj) {
    StringBuffer where = new StringBuffer();
    where.append(" select " + CostAdjustmentLine.PROPERTY_LINENO);
    where.append(" from " + CostAdjustmentLine.ENTITY_NAME + " as cal");
    where.append(" where cal." + CostAdjustmentLine.PROPERTY_COSTADJUSTMENT
        + ".id = :costAdjustment");
    where.append(" order by cal." + CostAdjustmentLine.PROPERTY_LINENO + " desc");
    Query calQry = OBDal.getInstance().getSession().createQuery(where.toString());
    calQry.setParameter("costAdjustment", cadj.getId());
    calQry.setMaxResults(1);

    Long lineNo = (Long) calQry.uniqueResult();
    if (lineNo != null) {
      return lineNo + 10L;
    }
    return 10L;
  }

  public static BigDecimal getTrxCost(MaterialTransaction trx, boolean justUnitCost,
      Currency currency) {
    if (!trx.isCostCalculated()) {
      // Transaction hasn't been calculated yet.
      log4j.error("  *** No cost found for transaction {} with id {}", trx.getIdentifier(),
          trx.getId());
      throw new OBException("@NoCostFoundForTrxOnDate@ @Transaction@: " + trx.getIdentifier());
    }

    StringBuffer select = new StringBuffer();
    select.append(" select sum(tc." + TransactionCost.PROPERTY_COST + ") as cost");
    select.append(" , tc." + TransactionCost.PROPERTY_CURRENCY + ".id as currency");
    select.append(" , tc." + TransactionCost.PROPERTY_COSTDATE + " as date");
    select.append(" from " + TransactionCost.ENTITY_NAME + " as tc");
    select.append(" where tc." + TransactionCost.PROPERTY_INVENTORYTRANSACTION + ".id = :trxId");
    if (justUnitCost) {
      select.append(" and tc." + TransactionCost.PROPERTY_UNITCOST + " = true");
    }
    select.append(" group by tc." + TransactionCost.PROPERTY_CURRENCY);
    select.append(" , tc." + TransactionCost.PROPERTY_COSTDATE);

    Query qry = OBDal.getInstance().getSession().createQuery(select.toString());
    qry.setParameter("trxId", trx.getId());
    ScrollableResults scroll = qry.scroll(ScrollMode.FORWARD_ONLY);

    BigDecimal cost = BigDecimal.ZERO;
    try {
      while (scroll.next()) {
        Object[] resultSet = scroll.get();
        BigDecimal costAmt = (BigDecimal) resultSet[0];
        String origCurId = (String) resultSet[1];

        if (StringUtils.equals(origCurId, currency.getId())) {
          cost = cost.add(costAmt);
        } else {
          Currency origCur = OBDal.getInstance().get(Currency.class, origCurId);
          Date convDate = (Date) resultSet[2];
          cost = cost.add(FinancialUtils.getConvertedAmount(costAmt, origCur, currency, convDate,
              trx.getOrganization(), FinancialUtils.PRECISION_COSTING));
        }
      }
    } finally {
      scroll.close();
    }
    return cost;
  }

  /**
   * Calculates the stock of the product on the given date and for the given cost dimensions. It
   * only takes transactions that have its cost calculated.
   */
  public static BigDecimal getStockOnMovementDate(Product product, Organization org, Date _date,
      HashMap<CostDimension, BaseOBObject> costDimensions, boolean backdatedTransactionsFixed) {
    // Get child tree of organizations.
    Date date = _date;
    Set<String> orgs = OBContext.getOBContext().getOrganizationStructureProvider()
        .getChildTree(org.getId(), true);

    StringBuffer subSelect = new StringBuffer();
    subSelect.append(" select min(case when coalesce(i." + InventoryCount.PROPERTY_INVENTORYTYPE
        + ", 'N') <> 'N' then trx." + MaterialTransaction.PROPERTY_MOVEMENTDATE + " else trx."
        + MaterialTransaction.PROPERTY_TRANSACTIONPROCESSDATE + " end)");
    subSelect.append(" from " + MaterialTransaction.ENTITY_NAME + " as trx");
    if (costDimensions.get(CostDimension.Warehouse) != null) {
      subSelect.append("   join trx." + MaterialTransaction.PROPERTY_STORAGEBIN + " as locator");
    }
    subSelect.append("   left join trx." + MaterialTransaction.PROPERTY_PHYSICALINVENTORYLINE
        + " as il");
    subSelect.append("   left join il." + InventoryCountLine.PROPERTY_PHYSINVENTORY + " as i");
    subSelect.append(" where trx." + MaterialTransaction.PROPERTY_PRODUCT + ".id = :product");
    subSelect.append(" and trx." + MaterialTransaction.PROPERTY_MOVEMENTDATE + " > :date");
    // Include only transactions that have its cost calculated
    subSelect.append("   and trx." + MaterialTransaction.PROPERTY_ISCOSTCALCULATED + " = true");
    if (costDimensions.get(CostDimension.Warehouse) != null) {
      subSelect.append("  and locator." + Locator.PROPERTY_WAREHOUSE + ".id = :warehouse");
    }
    subSelect.append("   and trx." + MaterialTransaction.PROPERTY_ORGANIZATION + ".id in (:orgs)");

    Query trxsubQry = OBDal.getInstance().getSession().createQuery(subSelect.toString());
    trxsubQry.setParameter("date", date);
    trxsubQry.setParameter("product", product.getId());
    if (costDimensions.get(CostDimension.Warehouse) != null) {
      trxsubQry.setParameter("warehouse", costDimensions.get(CostDimension.Warehouse).getId());
    }
    trxsubQry.setParameterList("orgs", orgs);
    Object trxprocessDate = trxsubQry.uniqueResult();

    StringBuffer select = new StringBuffer();
    select
        .append(" select sum(trx." + MaterialTransaction.PROPERTY_MOVEMENTQUANTITY + ") as stock");
    select.append(" from " + MaterialTransaction.ENTITY_NAME + " as trx");
    if (costDimensions.get(CostDimension.Warehouse) != null) {
      select.append("   join trx." + MaterialTransaction.PROPERTY_STORAGEBIN + " as locator");
    }

    Date backdatedTrxFrom = null;
    if (backdatedTransactionsFixed) {
      CostingRule costRule = CostingUtils.getCostDimensionRule(org, date);
      backdatedTrxFrom = CostingUtils.getCostingRuleFixBackdatedFrom(costRule);
    }

    if (trxprocessDate != null
        && (!backdatedTransactionsFixed || ((Date) trxprocessDate).before(backdatedTrxFrom))) {
      date = (Date) trxprocessDate;
      select.append(" left join trx." + MaterialTransaction.PROPERTY_PHYSICALINVENTORYLINE
          + " as il");
      select.append(" left join il." + InventoryCountLine.PROPERTY_PHYSINVENTORY + " as i");
      select.append(" where case when coalesce(i." + InventoryCount.PROPERTY_INVENTORYTYPE
          + ", 'N') <> 'N' then trx." + MaterialTransaction.PROPERTY_MOVEMENTDATE + " else trx."
          + MaterialTransaction.PROPERTY_TRANSACTIONPROCESSDATE + " end < :date");
    } else {
      select.append(" where trx." + MaterialTransaction.PROPERTY_MOVEMENTDATE + " <= :date");
    }

    select.append(" and trx." + MaterialTransaction.PROPERTY_PRODUCT + ".id = :product");
    // Include only transactions that have its cost calculated
    select.append("   and trx." + MaterialTransaction.PROPERTY_ISCOSTCALCULATED + " = true");
    if (costDimensions.get(CostDimension.Warehouse) != null) {
      select.append("  and locator." + Locator.PROPERTY_WAREHOUSE + ".id = :warehouse");
    }
    select.append("   and trx." + MaterialTransaction.PROPERTY_ORGANIZATION + ".id in (:orgs)");
    Query trxQry = OBDal.getInstance().getSession().createQuery(select.toString());
    trxQry.setParameter("product", product.getId());
    trxQry.setParameter("date", date);
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
   * Calculates the stock of the product on the given date and for the given cost dimensions. It
   * only takes transactions that have its cost calculated.
   */
  public static BigDecimal getStockOnTransactionDate(Organization costorg, MaterialTransaction trx,
      HashMap<CostDimension, BaseOBObject> _costDimensions, boolean isManufacturingProduct,
      boolean areBackdatedTrxFixed) {
    Costing costing = AverageAlgorithm.getProductCost(trx.getTransactionProcessDate(),
        trx.getProduct(), _costDimensions, costorg);
    return getStockOnTransactionDate(costorg, trx, _costDimensions, isManufacturingProduct,
        areBackdatedTrxFixed, costing);
  }

  /**
   * Calculates the stock of the product on the given date and for the given cost dimensions. It
   * only takes transactions that have its cost calculated.
   */
  public static BigDecimal getStockOnTransactionDate(Organization costorg, MaterialTransaction trx,
      HashMap<CostDimension, BaseOBObject> _costDimensions, boolean isManufacturingProduct,
      boolean areBackdatedTrxFixed, Costing costing) {

    // Get child tree of organizations.
    OrganizationStructureProvider osp = OBContext.getOBContext().getOrganizationStructureProvider(
        trx.getClient().getId());
    Set<String> orgs = osp.getChildTree(costorg.getId(), true);
    HashMap<CostDimension, BaseOBObject> costDimensions = _costDimensions;
    if (isManufacturingProduct) {
      orgs = osp.getChildTree("0", false);
      costDimensions = CostingUtils.getEmptyDimensions();
    }
    CostingRule costingRule = CostingUtils.getCostDimensionRule(costorg,
        trx.getTransactionProcessDate());

    boolean existsCumulatedStockOnTrxDate = costing != null
        && costing.getTotalMovementQuantity() != null
        && costing.getInventoryTransaction() != null
        && (!costingRule.isBackdatedTransactionsFixed() || trx.getMovementDate().after(
            costing.getInventoryTransaction().getMovementDate()));

    StringBuffer select = new StringBuffer();
    select
        .append(" select sum(trx." + MaterialTransaction.PROPERTY_MOVEMENTQUANTITY + ") as stock");
    select.append("\n from " + MaterialTransaction.ENTITY_NAME + " as trx");
    if (costDimensions.get(CostDimension.Warehouse) != null) {
      select.append("\n join trx." + MaterialTransaction.PROPERTY_STORAGEBIN + " as locator");
    }
    select.append("\n , " + org.openbravo.model.ad.domain.List.ENTITY_NAME + " as trxtype");
    select.append("\n where trxtype." + propADListReference + ".id = :refid");
    select.append("  and trxtype." + propADListValue + " = trx."
        + MaterialTransaction.PROPERTY_MOVEMENTTYPE);
    select.append("   and trx." + MaterialTransaction.PROPERTY_PRODUCT + " = :product");
    // Include only transactions that have its cost calculated. Should be all.
    select.append("   and trx." + MaterialTransaction.PROPERTY_ISCOSTCALCULATED + " = true");
    if (existsCumulatedStockOnTrxDate) {
      select.append(" and (trx." + MaterialTransaction.PROPERTY_TRANSACTIONPROCESSDATE
          + " > :dateFrom");
      select.append(" or (trx." + MaterialTransaction.PROPERTY_TRANSACTIONPROCESSDATE
          + " = :dateFrom");
      select.append(" and (trxtype." + CostAdjustmentUtils.propADListPriority + " > :trxtypeprio");
      select.append(" or (trxtype." + CostAdjustmentUtils.propADListPriority + " = :trxtypeprio");
      select.append(" and (trx." + MaterialTransaction.PROPERTY_MOVEMENTQUANTITY + " < :trxqty");
      select.append(" or (trx." + MaterialTransaction.PROPERTY_MOVEMENTQUANTITY + " = :trxqty");
      select.append(" and trx." + MaterialTransaction.PROPERTY_ID + " > :trxid");
      select.append(" ))))))");
    }
    select.append("  and ( ");

    if (costingRule.isBackdatedTransactionsFixed()) {
      select.append("  (");
      select.append("   trx." + MaterialTransaction.PROPERTY_TRANSACTIONPROCESSDATE + " < :fixbdt");
      select.append("   and  (");
    }

    // If there are more than one trx on the same trx process date filter out those types with less
    // priority and / or higher quantity.
    select.append("    trx." + MaterialTransaction.PROPERTY_TRANSACTIONPROCESSDATE + " < :trxdate");
    select.append("    or (");
    select
        .append("     trx." + MaterialTransaction.PROPERTY_TRANSACTIONPROCESSDATE + " = :trxdate");
    select.append("     and (");
    select.append("      trxtype." + propADListPriority + " < :trxtypeprio");
    select.append("      or (");
    select.append("       trxtype." + propADListPriority + " = :trxtypeprio");
    select.append("       and trx." + MaterialTransaction.PROPERTY_MOVEMENTQUANTITY + " > :trxqty");
    select.append("        or (");
    select.append("         trx." + MaterialTransaction.PROPERTY_MOVEMENTQUANTITY + " = :trxqty");
    select.append("         and trx." + MaterialTransaction.PROPERTY_ID + " <= :trxid");
    select.append("   ))))");
    if (costingRule.isBackdatedTransactionsFixed()) {
      select.append(" )) or (");

      select.append("  trx." + MaterialTransaction.PROPERTY_TRANSACTIONPROCESSDATE + " >= :fixbdt");
      select.append("  and (");
      select.append("   trx." + MaterialTransaction.PROPERTY_MOVEMENTDATE + " < :mvtdate");
      select.append("   or (");
      select.append("    trx." + MaterialTransaction.PROPERTY_MOVEMENTDATE + " = :mvtdate");
      // If there are more than one trx on the same trx process date filter out those types with
      // less
      // priority and / or higher quantity.
      select.append("    and (");
      select.append("     trx." + MaterialTransaction.PROPERTY_TRANSACTIONPROCESSDATE
          + " < :trxdate");
      select.append("     or (");
      select.append("      trx." + MaterialTransaction.PROPERTY_TRANSACTIONPROCESSDATE
          + " = :trxdate");
      select.append("      and (");
      select.append("       trxtype." + propADListPriority + " < :trxtypeprio");
      select.append("       or (");
      select.append("        trxtype." + propADListPriority + " = :trxtypeprio");
      select.append("        and trx." + MaterialTransaction.PROPERTY_MOVEMENTQUANTITY
          + " > :trxqty");
      select.append("        or (");
      select.append("         trx." + MaterialTransaction.PROPERTY_MOVEMENTQUANTITY + " = :trxqty");
      select.append("         and trx." + MaterialTransaction.PROPERTY_ID + " <= :trxid");
      select.append("    ))))");
      select.append("   ))))");
    }
    select.append("  )");
    if (costDimensions.get(CostDimension.Warehouse) != null) {
      select.append("  and locator." + Locator.PROPERTY_WAREHOUSE + ".id = :warehouse");
    }
    select.append("   and trx." + MaterialTransaction.PROPERTY_ORGANIZATION + ".id in (:orgs)");
    Query trxQry = OBDal.getInstance().getSession().createQuery(select.toString());
    trxQry.setParameter("refid", MovementTypeRefID);
    trxQry.setParameter("product", trx.getProduct());
    trxQry.setParameter("trxdate", trx.getTransactionProcessDate());
    trxQry.setParameter("trxtypeprio", getTrxTypePrio(trx.getMovementType()));
    trxQry.setParameter("trxqty", trx.getMovementQuantity());
    trxQry.setParameter("trxid", trx.getId());

    if (existsCumulatedStockOnTrxDate) {
      trxQry.setParameter("dateFrom", costing.getStartingDate());
    }

    if (costingRule.isBackdatedTransactionsFixed()) {
      trxQry.setParameter("mvtdate", trx.getMovementDate());
      trxQry.setParameter("fixbdt", CostingUtils.getCostingRuleFixBackdatedFrom(costingRule));
    }

    if (costDimensions.get(CostDimension.Warehouse) != null) {
      trxQry.setParameter("warehouse", costDimensions.get(CostDimension.Warehouse).getId());
    }
    trxQry.setParameterList("orgs", orgs);
    BigDecimal stock = (BigDecimal) trxQry.uniqueResult();
    if (stock == null) {
      stock = BigDecimal.ZERO;
    }
    if (existsCumulatedStockOnTrxDate) {
      stock = stock.add(costing.getTotalMovementQuantity());
    }
    return stock;
  }

  /**
   * Calculates the value of the stock of the product on the given date, for the given cost
   * dimensions and for the given currency. It only takes transactions that have its cost
   * calculated.
   */
  public static BigDecimal getValuedStockOnMovementDate(Product product, Organization org,
      Date _date, HashMap<CostDimension, BaseOBObject> _costDimensions, Currency currency,
      boolean backdatedTransactionsFixed) {
    return getValuedStockOnMovementDateByAttrAndLocator(product, org, _date, _costDimensions, null,
        null, currency, backdatedTransactionsFixed);
  }

  /**
   * Calculates the value of the stock of the product on the given date, for the given cost
   * dimensions and for the given currency. It only takes transactions that have its cost
   * calculated.
   */
  public static BigDecimal getValuedStockOnMovementDateByAttrAndLocator(Product product,
      Organization org, Date _date, HashMap<CostDimension, BaseOBObject> _costDimensions,
      Locator locator, AttributeSetInstance asi, Currency currency,
      boolean backdatedTransactionsFixed) {
    Date date = _date;
    HashMap<CostDimension, BaseOBObject> costDimensions = _costDimensions;

    // Get child tree of organizations.
    OrganizationStructureProvider osp = OBContext.getOBContext().getOrganizationStructureProvider(
        org.getClient().getId());
    Set<String> orgs = osp.getChildTree(org.getId(), true);
    if (product.isProduction()) {
      orgs = osp.getChildTree("0", false);
      costDimensions = CostingUtils.getEmptyDimensions();
    }

    StringBuffer subSelect = new StringBuffer();
    subSelect.append(" select min(case when coalesce(i." + InventoryCount.PROPERTY_INVENTORYTYPE
        + ", 'N') <> 'N' then trx." + MaterialTransaction.PROPERTY_MOVEMENTDATE + " else trx."
        + MaterialTransaction.PROPERTY_TRANSACTIONPROCESSDATE + " end)");
    subSelect.append(" from " + MaterialTransaction.ENTITY_NAME + " as trx");
    if (costDimensions.get(CostDimension.Warehouse) != null) {
      subSelect.append("   join trx." + MaterialTransaction.PROPERTY_STORAGEBIN + " as locator");
    }
    subSelect.append("   left join trx." + MaterialTransaction.PROPERTY_PHYSICALINVENTORYLINE
        + " as il");
    subSelect.append("   left join il." + InventoryCountLine.PROPERTY_PHYSINVENTORY + " as i");
    subSelect.append(" where trx." + MaterialTransaction.PROPERTY_PRODUCT + ".id = :product");
    subSelect.append(" and trx." + MaterialTransaction.PROPERTY_MOVEMENTDATE + " > :date");
    // Include only transactions that have its cost calculated
    subSelect.append("   and trx." + MaterialTransaction.PROPERTY_ISCOSTCALCULATED + " = true");
    if (costDimensions.get(CostDimension.Warehouse) != null) {
      subSelect.append("  and locator." + Locator.PROPERTY_WAREHOUSE + ".id = :warehouse");
    }
    subSelect.append("   and trx." + MaterialTransaction.PROPERTY_ORGANIZATION + ".id in (:orgs)");

    Query trxsubQry = OBDal.getInstance().getSession().createQuery(subSelect.toString());
    trxsubQry.setParameter("date", date);
    trxsubQry.setParameter("product", product.getId());
    if (costDimensions.get(CostDimension.Warehouse) != null) {
      trxsubQry.setParameter("warehouse", costDimensions.get(CostDimension.Warehouse).getId());
    }
    trxsubQry.setParameterList("orgs", orgs);
    Object trxprocessDate = trxsubQry.uniqueResult();

    StringBuffer select = new StringBuffer();
    select.append(" select sum(case");
    select.append("     when trx." + MaterialTransaction.PROPERTY_MOVEMENTQUANTITY
        + " < 0 then -tc." + TransactionCost.PROPERTY_COST);
    select.append("     else tc." + TransactionCost.PROPERTY_COST + " end ) as cost");
    select.append(" , tc." + TransactionCost.PROPERTY_CURRENCY + ".id as currency");
    select.append(" , tc." + TransactionCost.PROPERTY_ACCOUNTINGDATE + " as mdate");
    select.append(" , sum(trx." + MaterialTransaction.PROPERTY_MOVEMENTQUANTITY + ") as stock");

    select.append(" from " + TransactionCost.ENTITY_NAME + " as tc");
    select.append("  join tc." + TransactionCost.PROPERTY_INVENTORYTRANSACTION + " as trx");
    if (costDimensions.get(CostDimension.Warehouse) != null) {
      select.append("  join trx." + MaterialTransaction.PROPERTY_STORAGEBIN + " as locator");
    }

    Date backdatedTrxFrom = null;
    if (backdatedTransactionsFixed) {
      CostingRule costRule = CostingUtils.getCostDimensionRule(org, date);
      backdatedTrxFrom = CostingUtils.getCostingRuleFixBackdatedFrom(costRule);
    }

    if (trxprocessDate != null
        && (!backdatedTransactionsFixed || ((Date) trxprocessDate).before(backdatedTrxFrom))) {
      date = (Date) trxprocessDate;
      select.append(" left join trx." + MaterialTransaction.PROPERTY_PHYSICALINVENTORYLINE
          + " as il");
      select.append(" left join il." + InventoryCountLine.PROPERTY_PHYSINVENTORY + " as i");
      select.append(" where case when coalesce(i." + InventoryCount.PROPERTY_INVENTORYTYPE
          + ", 'N') <> 'N' then trx." + MaterialTransaction.PROPERTY_MOVEMENTDATE + " else trx."
          + MaterialTransaction.PROPERTY_TRANSACTIONPROCESSDATE + " end < :date");
    } else {
      select.append(" where trx." + MaterialTransaction.PROPERTY_MOVEMENTDATE + " <= :date");
    }

    select.append(" and trx." + MaterialTransaction.PROPERTY_PRODUCT + " = :product");
    // Include only transactions that have its cost calculated
    select.append("   and trx." + MaterialTransaction.PROPERTY_ISCOSTCALCULATED + " = true");

    if (costDimensions.get(CostDimension.Warehouse) != null) {
      select.append("  and locator." + Locator.PROPERTY_WAREHOUSE + ".id = :warehouse");
    }
    if (locator != null) {
      select.append("   and trx." + MaterialTransaction.PROPERTY_STORAGEBIN + " = :locator");
    }
    if (asi != null) {
      select.append("   and trx." + MaterialTransaction.PROPERTY_ATTRIBUTESETVALUE + " = :asi");
    }
    select.append("   and trx." + MaterialTransaction.PROPERTY_ORGANIZATION + ".id in (:orgs)");

    select.append(" group by tc." + TransactionCost.PROPERTY_CURRENCY);
    select.append("   , tc." + TransactionCost.PROPERTY_ACCOUNTINGDATE);

    Query trxQry = OBDal.getInstance().getSession().createQuery(select.toString());
    trxQry.setParameter("product", product);
    trxQry.setParameter("date", date);
    if (costDimensions.get(CostDimension.Warehouse) != null) {
      trxQry.setParameter("warehouse", costDimensions.get(CostDimension.Warehouse).getId());
    }
    if (locator != null) {
      trxQry.setParameter("locator", locator);
    }
    if (asi != null) {
      trxQry.setParameter("asi", asi);
    }
    trxQry.setParameterList("orgs", orgs);

    ScrollableResults scroll = trxQry.scroll(ScrollMode.FORWARD_ONLY);
    BigDecimal sum = BigDecimal.ZERO;
    try {
      while (scroll.next()) {
        Object[] resultSet = scroll.get();
        BigDecimal origAmt = (BigDecimal) resultSet[0];
        String origCurId = (String) resultSet[1];

        if (StringUtils.equals(origCurId, currency.getId())) {
          sum = sum.add(origAmt);
        } else {
          Currency origCur = OBDal.getInstance().get(Currency.class, origCurId);
          Date convDate = (Date) resultSet[2];
          sum = sum.add(FinancialUtils.getConvertedAmount(origAmt, origCur, currency, convDate,
              org, FinancialUtils.PRECISION_COSTING));
        }
      }
    } finally {
      scroll.close();
    }
    return sum;
  }

  /**
   * Calculates the value of the stock of the product on the given date, for the given cost
   * dimensions and for the given currency. It only takes transactions that have its cost
   * calculated.
   */
  public static BigDecimal getValuedStockOnTransactionDate(Organization costorg,
      MaterialTransaction trx, HashMap<CostDimension, BaseOBObject> _costDimensions,
      boolean isManufacturingProduct, boolean areBackdatedTrxFixed, Currency currency) {
    Costing costing = AverageAlgorithm.getProductCost(trx.getTransactionProcessDate(),
        trx.getProduct(), _costDimensions, costorg);
    return getValuedStockOnTransactionDate(costorg, trx, _costDimensions, isManufacturingProduct,
        areBackdatedTrxFixed, currency, costing);
  }

  /**
   * Calculates the value of the stock of the product on the given date, for the given cost
   * dimensions and for the given currency. It only takes transactions that have its cost
   * calculated.
   */
  public static BigDecimal getValuedStockOnTransactionDate(Organization costorg,
      MaterialTransaction trx, HashMap<CostDimension, BaseOBObject> _costDimensions,
      boolean isManufacturingProduct, boolean areBackdatedTrxFixed, Currency currency,
      Costing costing) {
    HashMap<CostDimension, BaseOBObject> costDimensions = _costDimensions;

    // Get child tree of organizations.
    OrganizationStructureProvider osp = OBContext.getOBContext().getOrganizationStructureProvider(
        trx.getClient().getId());
    Set<String> orgs = osp.getChildTree(costorg.getId(), true);
    if (isManufacturingProduct) {
      orgs = osp.getChildTree("0", false);
      costDimensions = CostingUtils.getEmptyDimensions();
    }
    CostingRule costingRule = CostingUtils.getCostDimensionRule(costorg,
        trx.getTransactionProcessDate());

    boolean existsCumulatedValuationOnTrxDate = costing != null
        && costing.getTotalStockValuation() != null
        && costing.getInventoryTransaction() != null
        && (!costingRule.isBackdatedTransactionsFixed() || trx.getMovementDate().after(
            costing.getInventoryTransaction().getMovementDate()));

    StringBuffer select = new StringBuffer();
    select.append(" select sum(case");
    select.append("     when trx." + MaterialTransaction.PROPERTY_MOVEMENTQUANTITY
        + " < 0 then -tc." + TransactionCost.PROPERTY_COST);
    select.append("     else tc." + TransactionCost.PROPERTY_COST + " end ) as cost");
    select.append(" , tc." + TransactionCost.PROPERTY_CURRENCY + ".id as currency");
    select.append(" , tc." + TransactionCost.PROPERTY_ACCOUNTINGDATE + " as mdate");
    select.append(" , sum(trx." + MaterialTransaction.PROPERTY_MOVEMENTQUANTITY + ") as stock");

    select.append("\n from " + TransactionCost.ENTITY_NAME + " as tc");
    select.append("\n  join tc." + TransactionCost.PROPERTY_INVENTORYTRANSACTION + " as trx");
    if (costDimensions.get(CostDimension.Warehouse) != null) {
      select.append("\n  join trx." + MaterialTransaction.PROPERTY_STORAGEBIN + " as locator");
    }
    select.append("\n , " + org.openbravo.model.ad.domain.List.ENTITY_NAME + " as trxtype");
    select.append("\n where trxtype." + propADListReference + ".id = :refid");
    select.append("  and trxtype." + propADListValue + " = trx."
        + MaterialTransaction.PROPERTY_MOVEMENTTYPE);

    select.append("  and trx." + MaterialTransaction.PROPERTY_PRODUCT + " = :product");
    // Include only transactions that have its cost calculated
    select.append("  and trx." + MaterialTransaction.PROPERTY_ISCOSTCALCULATED + " = true");
    if (existsCumulatedValuationOnTrxDate) {
      select.append(" and (trx." + MaterialTransaction.PROPERTY_TRANSACTIONPROCESSDATE
          + " > :dateFrom");
      select.append(" or (trx." + MaterialTransaction.PROPERTY_TRANSACTIONPROCESSDATE
          + " = :dateFrom");
      select.append(" and (trxtype." + CostAdjustmentUtils.propADListPriority + " > :trxtypeprio");
      select.append(" or (trxtype." + CostAdjustmentUtils.propADListPriority + " = :trxtypeprio");
      select.append(" and (trx." + MaterialTransaction.PROPERTY_MOVEMENTQUANTITY + " < :trxqty");
      select.append(" or (trx." + MaterialTransaction.PROPERTY_MOVEMENTQUANTITY + " = :trxqty");
      select.append(" and trx." + MaterialTransaction.PROPERTY_ID + " > :trxid");
      select.append(" ))))))");
    }
    select.append("  and (");

    if (costingRule.isBackdatedTransactionsFixed()) {
      select.append("   ( trx." + MaterialTransaction.PROPERTY_TRANSACTIONPROCESSDATE
          + " < :fixbdt");
      select.append(" and (");
    }
    // If there are more than one trx on the same trx process date filter out those types with less
    // priority and / or higher quantity.
    select.append("  trx." + MaterialTransaction.PROPERTY_TRANSACTIONPROCESSDATE + " < :trxdate");
    select.append("  or (");
    select.append("   trx." + MaterialTransaction.PROPERTY_TRANSACTIONPROCESSDATE + " = :trxdate");
    select.append("   and (");
    select.append("    trxtype." + propADListPriority + " < :trxtypeprio");
    select.append("    or (");
    select.append("     trxtype." + propADListPriority + " = :trxtypeprio");
    select.append("     and trx." + MaterialTransaction.PROPERTY_MOVEMENTQUANTITY + " > :trxqty");
    select.append("        or (");
    select.append("         trx." + MaterialTransaction.PROPERTY_MOVEMENTQUANTITY + " = :trxqty");
    select.append("         and trx." + MaterialTransaction.PROPERTY_ID + " <= :trxid");
    select.append(" ))))");

    if (costingRule.isBackdatedTransactionsFixed()) {
      select.append(" )) or (");
      select
          .append("   trx." + MaterialTransaction.PROPERTY_TRANSACTIONPROCESSDATE + " >= :fixbdt");

      select.append("   and (trx." + MaterialTransaction.PROPERTY_MOVEMENTDATE + " < :mvtdate");
      select.append("   or (");
      select.append("    trx." + MaterialTransaction.PROPERTY_MOVEMENTDATE + " = :mvtdate");
      // If there are more than one trx on the same trx process date filter out those types with
      // less
      // priority and / or higher quantity.
      select.append(" and (");
      select.append("  trx." + MaterialTransaction.PROPERTY_TRANSACTIONPROCESSDATE + " < :trxdate");
      select.append("  or (");
      select
          .append("   trx." + MaterialTransaction.PROPERTY_TRANSACTIONPROCESSDATE + " = :trxdate");
      select.append("   and (");
      select.append("    trxtype." + propADListPriority + " < :trxtypeprio");
      select.append("    or (");
      select.append("     trxtype." + propADListPriority + " = :trxtypeprio");
      select.append("     and trx." + MaterialTransaction.PROPERTY_MOVEMENTQUANTITY + " > :trxqty");
      select.append("        or (");
      select.append("         trx." + MaterialTransaction.PROPERTY_MOVEMENTQUANTITY + " = :trxqty");
      select.append("         and trx." + MaterialTransaction.PROPERTY_ID + " <= :trxid");
      select.append("  ))))))");
      select.append(" ))");
    }
    select.append(" )");

    if (costDimensions.get(CostDimension.Warehouse) != null) {
      select.append("  and locator." + Locator.PROPERTY_WAREHOUSE + ".id = :warehouse");
    }
    select.append("   and trx." + MaterialTransaction.PROPERTY_ORGANIZATION + ".id in (:orgs)");

    select.append(" group by tc." + TransactionCost.PROPERTY_CURRENCY);
    select.append("   , tc." + TransactionCost.PROPERTY_ACCOUNTINGDATE);

    Query trxQry = OBDal.getInstance().getSession().createQuery(select.toString());
    trxQry.setParameter("refid", MovementTypeRefID);
    trxQry.setParameter("product", trx.getProduct());
    if (existsCumulatedValuationOnTrxDate) {
      trxQry.setParameter("dateFrom", costing.getStartingDate());
    }
    if (costDimensions.get(CostDimension.Warehouse) != null) {
      trxQry.setParameter("warehouse", costDimensions.get(CostDimension.Warehouse).getId());
    }
    trxQry.setParameter("trxdate", trx.getTransactionProcessDate());
    trxQry.setParameter("trxtypeprio", getTrxTypePrio(trx.getMovementType()));
    trxQry.setParameter("trxqty", trx.getMovementQuantity());
    trxQry.setParameter("trxid", trx.getId());
    if (costingRule.isBackdatedTransactionsFixed()) {
      trxQry.setParameter("mvtdate", trx.getMovementDate());
      trxQry.setParameter("fixbdt", CostingUtils.getCostingRuleFixBackdatedFrom(costingRule));
    }
    trxQry.setParameterList("orgs", orgs);

    ScrollableResults scroll = trxQry.scroll(ScrollMode.FORWARD_ONLY);
    BigDecimal sum = BigDecimal.ZERO;
    try {
      while (scroll.next()) {
        Object[] resultSet = scroll.get();
        BigDecimal origAmt = (BigDecimal) resultSet[0];
        String origCurId = (String) resultSet[1];

        if (StringUtils.equals(origCurId, currency.getId())) {
          sum = sum.add(origAmt);
        } else {
          Currency origCur = OBDal.getInstance().get(Currency.class, origCurId);
          Date convDate = (Date) resultSet[2];
          sum = sum.add(FinancialUtils.getConvertedAmount(origAmt, origCur, currency, convDate,
              costorg, FinancialUtils.PRECISION_COSTING));
        }
      }
    } finally {
      scroll.close();
    }

    if (existsCumulatedValuationOnTrxDate) {
      BigDecimal costingValuedStock = costing.getTotalStockValuation();
      if (!StringUtils.equals(costing.getCurrency().getId(), currency.getId())) {
        costingValuedStock = FinancialUtils.getConvertedAmount(costingValuedStock,
            costing.getCurrency(), currency, costing.getStartingDate(), costorg,
            FinancialUtils.PRECISION_COSTING);
      }
      sum = sum.add(costingValuedStock);
    }
    return sum;
  }

  /**
   * Returns the last transaction process date of a non backdated transactions for the given
   * movement date or previous date.
   */
  public static Date getLastTrxDateOfMvmntDate(Date refDate, Product product, Organization org,
      HashMap<CostDimension, BaseOBObject> costDimensions) {
    OrganizationStructureProvider osp = OBContext.getOBContext().getOrganizationStructureProvider(
        org.getClient().getId());
    Set<String> orgs = osp.getChildTree(org.getId(), true);
    Warehouse wh = (Warehouse) costDimensions.get(CostDimension.Warehouse);

    // Calculate the transaction process date of the first transaction with a movement date
    // after the given date. Any transaction with a transaction process date after this min date on
    // the given date or before is a backdated transaction.
    StringBuffer select = new StringBuffer();
    select.append(" select min(trx." + MaterialTransaction.PROPERTY_TRANSACTIONPROCESSDATE
        + ") as date");
    select.append(" from " + MaterialTransaction.ENTITY_NAME + " as trx");
    if (wh != null) {
      select.append("    join trx." + MaterialTransaction.PROPERTY_STORAGEBIN + " as loc");
    }
    select.append(" where trx." + MaterialTransaction.PROPERTY_ISCOSTCALCULATED + " = true");
    select.append("   and trx." + MaterialTransaction.PROPERTY_ORGANIZATION + ".id in (:orgs)");
    select.append("   and trx." + MaterialTransaction.PROPERTY_PRODUCT + " = :product");
    select.append("   and trx." + MaterialTransaction.PROPERTY_MOVEMENTDATE + " > :mvntdate");
    if (wh != null) {
      select.append("   and loc." + Locator.PROPERTY_WAREHOUSE + " = :warehouse");
    }
    Query qryMinDate = OBDal.getInstance().getSession().createQuery(select.toString());
    qryMinDate.setParameterList("orgs", orgs);
    qryMinDate.setParameter("product", product);
    qryMinDate.setParameter("mvntdate", refDate);
    if (wh != null) {
      qryMinDate.setParameter("warehouse", wh);
    }
    Object objMinDate = qryMinDate.uniqueResult();
    if (objMinDate == null) {
      return null;
    }

    // Get the last transaction process date of transactions with movement date equal or before the
    // given date and a transaction process date before the previously calculated min date.
    Date minNextDate = (Date) objMinDate;
    select = new StringBuffer();
    select.append(" select max(trx." + MaterialTransaction.PROPERTY_TRANSACTIONPROCESSDATE
        + ") as date");
    select.append(" from " + MaterialTransaction.ENTITY_NAME + " as trx");
    if (wh != null) {
      select.append("    join trx." + MaterialTransaction.PROPERTY_STORAGEBIN + " as loc");
    }
    select.append(" where trx." + MaterialTransaction.PROPERTY_ISCOSTCALCULATED + " = true");
    select.append("   and trx." + MaterialTransaction.PROPERTY_ORGANIZATION + ".id in (:orgs)");
    select.append("   and trx." + MaterialTransaction.PROPERTY_PRODUCT + " = :product");
    select.append("   and trx." + MaterialTransaction.PROPERTY_MOVEMENTDATE + " <= :mvntdate");
    select.append("   and trx." + MaterialTransaction.PROPERTY_TRANSACTIONPROCESSDATE
        + " < :trxdate");
    if (wh != null) {
      select.append("   and loc." + Locator.PROPERTY_WAREHOUSE + " = :warehouse");
    }
    Query qryMaxDate = OBDal.getInstance().getSession().createQuery(select.toString());
    qryMaxDate.setParameterList("orgs", orgs);
    qryMaxDate.setParameter("product", product);
    qryMaxDate.setParameter("mvntdate", refDate);
    qryMaxDate.setParameter("trxdate", minNextDate);
    if (wh != null) {
      qryMaxDate.setParameter("warehouse", wh);
    }

    Object objMaxDate = qryMaxDate.uniqueResult();
    if (objMaxDate == null) {
      return null;
    }

    return (Date) objMaxDate;
  }

  /**
   * Returns the priority of the given movementType.
   */
  public static long getTrxTypePrio(String mvmntType) {
    OBCriteria<org.openbravo.model.ad.domain.List> crList = OBDal.getInstance().createCriteria(
        org.openbravo.model.ad.domain.List.class);
    crList.createAlias(propADListReference, "ref");
    crList.add(Restrictions.eq("ref.id", MovementTypeRefID));
    crList.add(Restrictions.eq(propADListValue, mvmntType));
    return ((org.openbravo.model.ad.domain.List) crList.uniqueResult()).getSequenceNumber();
  }
}
