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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.openbravo.advpaymentmngt.utility.FIN_Utility;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.costing.CostingAlgorithm.CostDimension;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.security.OrganizationStructureProvider;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.financial.FinancialUtils;
import org.openbravo.model.common.currency.Currency;
import org.openbravo.model.common.enterprise.DocumentType;
import org.openbravo.model.common.enterprise.Locator;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.common.plm.Product;
import org.openbravo.model.materialmgmt.cost.CostAdjustment;
import org.openbravo.model.materialmgmt.cost.CostAdjustmentLine;
import org.openbravo.model.materialmgmt.cost.TransactionCost;
import org.openbravo.model.materialmgmt.transaction.MaterialTransaction;
import org.openbravo.model.materialmgmt.transaction.ShipmentInOut;
import org.openbravo.model.materialmgmt.transaction.ShipmentInOutLine;

public class CostAdjustmentUtils {
  private static final Logger log4j = Logger.getLogger(CostAdjustmentUtils.class);
  final static String strCategoryCostAdj = "CAD";
  final static String strTableCostAdj = "M_CostAdjustment";

  /**
   * @param organization
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
   * @param transaction
   *          transaction to apply the cost adjustment
   * 
   * @param costAdjustmentHeader
   *          header of line
   * 
   * @param costAdjusted
   *          amount to adjust in the cost
   * 
   * @param transactionDate
   *          date to do the transaction
   * 
   * @param isSource
   */
  public static CostAdjustmentLine insertCostAdjustmentLine(MaterialTransaction transaction,
      CostAdjustment costAdjustmentHeader, BigDecimal costAdjusted, boolean isSource,
      Date transactionDate, Date accountingDate) {

    CostAdjustmentLine costAdjustmentLine = OBProvider.getInstance().get(CostAdjustmentLine.class);
    costAdjustmentLine.setOrganization(costAdjustmentHeader.getOrganization());
    costAdjustmentLine.setCostAdjustment(costAdjustmentHeader);
    costAdjustmentLine.setAdjustmentAmount(costAdjusted);
    costAdjustmentLine.setCurrency(transaction.getCurrency());
    costAdjustmentLine.setInventoryTransaction(transaction);
    costAdjustmentLine.setSource(isSource);
    costAdjustmentLine.setTransactionDate(transactionDate);
    costAdjustmentLine.setAccountingDate(accountingDate);
    costAdjustmentLine.setLineNo(getNewLineNo(costAdjustmentHeader));

    OBDal.getInstance().save(costAdjustmentLine);

    return costAdjustmentLine;
  }

  /**
   * @param transaction
   *          transaction to check if cost adjustment should be applied
   * 
   * @param costDimensions
   *          dimensions used in costs
   */
  public static boolean isNeededCostAdjustmentByBackDateTrx(MaterialTransaction transaction,
      boolean includeWarehouseDimension) {

    final String orgLegalId = OBContext.getOBContext()
        .getOrganizationStructureProvider(transaction.getClient().getId())
        .getLegalEntity(transaction.getOrganization()).getId();
    // Get child tree of organizations.
    Set<String> orgs = OBContext.getOBContext().getOrganizationStructureProvider()
        .getChildTree(orgLegalId, true);

    StringBuffer where = new StringBuffer();
    where.append(" as trx");
    where.append("   join trx." + MaterialTransaction.PROPERTY_STORAGEBIN + " as locator");
    where.append(" where trx." + MaterialTransaction.PROPERTY_TRANSACTIONPROCESSDATE
        + " < :transactionProcessDate");
    where.append("   and trx." + MaterialTransaction.PROPERTY_MOVEMENTDATE
        + " > :transactionMovementDate");
    where.append("   and trx." + MaterialTransaction.PROPERTY_PRODUCT
        + ".id = :transactionProductId");
    where.append("   and trx." + MaterialTransaction.PROPERTY_ORGANIZATION + ".id in (:orgs)");

    if (includeWarehouseDimension) {
      where.append("  and locator." + Locator.PROPERTY_WAREHOUSE + ".id = :warehouse");
    }

    OBQuery<MaterialTransaction> trxQry = OBDal.getInstance().createQuery(
        MaterialTransaction.class, where.toString());

    trxQry.setNamedParameter("transactionProcessDate", transaction.getTransactionProcessDate());
    trxQry.setNamedParameter("transactionMovementDate", transaction.getMovementDate());
    trxQry.setNamedParameter("transactionProductId", transaction.getProduct().getId());
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
    where.append(" as cal");
    where.append(" where cal." + CostAdjustmentLine.PROPERTY_COSTADJUSTMENT
        + ".id = :costAdjustment");
    where.append(" order by cal." + CostAdjustmentLine.PROPERTY_LINENO + " desc");
    OBQuery<CostAdjustmentLine> calQry = OBDal.getInstance().createQuery(CostAdjustmentLine.class,
        where.toString());
    calQry.setNamedParameter("costAdjustment", cadj.getId());
    calQry.setMaxResult(1);

    if (calQry.uniqueResult() != null) {
      return calQry.uniqueResult().getLineNo() + 10L;
    }
    return 10L;
  }

  public static BigDecimal getTrxCost(MaterialTransaction trx, boolean justUnitCost,
      Currency currency) {
    // log4j.debug("Get Transaction Cost");
    if (!trx.isCostCalculated()) {
      // Transaction hasn't been calculated yet.
      log4j.error("  *** No cost found for transaction " + trx.getIdentifier() + " with id "
          + trx.getId());
      throw new OBException("@NoCostFoundForTrxOnDate@ @Transaction@: " + trx.getIdentifier());
    }
    BigDecimal cost = BigDecimal.ZERO;
    for (TransactionCost trxCost : trx.getTransactionCostList()) {
      if (!justUnitCost || trxCost.isUnitCost()) {
        if (trxCost.getCurrency().getId().equals(currency.getId())) {
          cost = cost.add(trxCost.getCost());
        } else {
          cost = cost.add(FinancialUtils.getConvertedAmount(trxCost.getCost(),
              trxCost.getCurrency(), currency, trxCost.getCostDate(), trxCost.getOrganization(),
              FinancialUtils.PRECISION_COSTING));
        }
      }
    }
    return cost;
  }

  /**
   * Calculates the stock of the product on the given date and for the given cost dimensions. It
   * only takes transactions that have its cost calculated.
   */
  public static BigDecimal getCurrentStock(Product product, Organization org, Date _date,
      HashMap<CostDimension, BaseOBObject> costDimensions, boolean backdatedTransactionsFixed) {
    // Get child tree of organizations.
    Date date = _date;
    Set<String> orgs = OBContext.getOBContext().getOrganizationStructureProvider()
        .getChildTree(org.getId(), true);

    StringBuffer select = new StringBuffer();
    StringBuffer subSelect = new StringBuffer();
    select
        .append(" select sum(trx." + MaterialTransaction.PROPERTY_MOVEMENTQUANTITY + ") as stock");
    select.append(" from " + MaterialTransaction.ENTITY_NAME + " as trx");
    select.append("   join trx." + MaterialTransaction.PROPERTY_STORAGEBIN + " as locator");
    select.append(" where trx." + MaterialTransaction.PROPERTY_PRODUCT + ".id = :product");
    if (backdatedTransactionsFixed) {
      select.append("   and trx." + MaterialTransaction.PROPERTY_MOVEMENTDATE + " < :date");
    } else {
      subSelect.append("select min(trx." + MaterialTransaction.PROPERTY_TRANSACTIONPROCESSDATE
          + ")");
      subSelect.append(" from " + MaterialTransaction.ENTITY_NAME + " as trx");
      subSelect.append(" where trx." + MaterialTransaction.PROPERTY_MOVEMENTDATE + " >= :date");
      Query trxsubQry = OBDal.getInstance().getSession().createQuery(subSelect.toString());
      trxsubQry.setParameter("date", date);
      Object trxprocessDate = trxsubQry.uniqueResult();
      if (trxprocessDate != null) {
        date = (Date) trxprocessDate;
        select.append("   and trx." + MaterialTransaction.PROPERTY_TRANSACTIONPROCESSDATE
            + " <= :date");
      } else {
        select.append("   and trx." + MaterialTransaction.PROPERTY_MOVEMENTDATE + " < :date");
      }
    }

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
   * Calculates the value of the stock of the product on the given date, for the given cost
   * dimensions and for the given currency. It only takes transactions that have its cost
   * calculated.
   */
  public static BigDecimal getCurrentValuedStock(Product product, Organization org, Date _date,
      HashMap<CostDimension, BaseOBObject> _costDimensions, Currency currency,
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

    StringBuffer select = new StringBuffer();
    StringBuffer subSelect = new StringBuffer();
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
    if (backdatedTransactionsFixed) {
      select.append("   and trx." + MaterialTransaction.PROPERTY_MOVEMENTDATE + " < :date");
    } else {
      subSelect.append("select min(trx." + MaterialTransaction.PROPERTY_TRANSACTIONPROCESSDATE
          + ")");
      subSelect.append(" from " + MaterialTransaction.ENTITY_NAME + " as trx");
      subSelect.append(" where trx." + MaterialTransaction.PROPERTY_MOVEMENTDATE + " >= :date");
      Query trxsubQry = OBDal.getInstance().getSession().createQuery(subSelect.toString());
      trxsubQry.setParameter("date", date);
      Object trxprocessDate = trxsubQry.uniqueResult();
      if (trxprocessDate != null) {
        date = (Date) trxprocessDate;
        select.append("   and trx." + MaterialTransaction.PROPERTY_TRANSACTIONPROCESSDATE
            + " <= :date");
      } else {
        select.append("   and trx." + MaterialTransaction.PROPERTY_MOVEMENTDATE + " < :date");
      }
    }
    if (costDimensions.get(CostDimension.Warehouse) != null) {
      select.append("  and locator." + Locator.PROPERTY_WAREHOUSE + ".id = :warehouse");
    }
    select.append("   and trx." + MaterialTransaction.PROPERTY_ORGANIZATION + ".id in (:orgs)");

    select.append(" group by tc." + TransactionCost.PROPERTY_CURRENCY);
    select.append("   , coalesce(sr." + ShipmentInOut.PROPERTY_ACCOUNTINGDATE + ", trx."
        + MaterialTransaction.PROPERTY_MOVEMENTDATE + ")");

    Query trxQry = OBDal.getInstance().getSession().createQuery(select.toString());
    trxQry.setParameter("product", product);
    trxQry.setParameter("date", date);
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
}
