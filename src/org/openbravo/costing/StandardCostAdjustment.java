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
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;

import org.apache.commons.lang.time.DateUtils;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.openbravo.base.session.OBPropertiesProvider;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.client.kernel.ComponentProvider;
import org.openbravo.costing.CostingAlgorithm.CostDimension;
import org.openbravo.costing.CostingServer.TrxType;
import org.openbravo.dal.core.DalUtil;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.security.OrganizationStructureProvider;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.financial.FinancialUtils;
import org.openbravo.model.common.enterprise.Locator;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.common.enterprise.Warehouse;
import org.openbravo.model.common.plm.Product;
import org.openbravo.model.materialmgmt.cost.CostAdjustmentLine;
import org.openbravo.model.materialmgmt.cost.Costing;
import org.openbravo.model.materialmgmt.transaction.MaterialTransaction;

@ComponentProvider.Qualifier("6A39D8B46CD94FE682D48758D3B7726B")
public class StandardCostAdjustment extends CostingAlgorithmAdjustmentImp {

  @Override
  void getRelatedTransactionsByAlgorithm() {
    // Inventory opening transactions are the only transaction types that can generates a new
    // Standard Cost. Having a new Standard COst on a backdated transaction forces to adjust the
    // cost off all the transactions from the backdated transaction to the next defined standard
    // cost.
    if (trxType == TrxType.InventoryOpening) {
      // If it is a backdated transaction of an inventory opening, insert a new costing if the
      // standard cost is different than the unit cost of the inventory.
      MaterialTransaction trx = getTransaction();
      BigDecimal unitCost = trx.getPhysicalInventoryLine().getCost()
          .setScale(costCurPrecission, RoundingMode.HALF_UP);

      // Cost is effective on the beginning of the following date.
      Date startingDate = DateUtils.truncate(
          DateUtils.addDays(getCostAdjLine().getTransactionDate(), 1), Calendar.DATE);

      Costing stdCost = CostingUtils.getStandardCostDefinition(trx.getProduct(), getCostOrg(),
          startingDate, getCostDimensions());
      // Modify isManufacturingProduct flag in case it has changed at some point.
      isManufacturingProduct = ((String) DalUtil.getId(stdCost.getOrganization())).equals("0");

      BigDecimal baseCurrentCost = stdCost.getCost();
      if (!stdCost.getCurrency().equals(strCostCurrencyId)) {
        baseCurrentCost = FinancialUtils.getConvertedAmount(baseCurrentCost, stdCost.getCurrency(),
            getCostCurrency(), startingDate, getCostOrg(), "C");
      }
      if (baseCurrentCost.compareTo(unitCost) == 0) {
        // If current cost is the same than the unit cost there is no need to create a new costing
        // so it is not needed to adjust any other transaction.
        return;
      }
      Costing newCosting = insertCost(stdCost, unitCost);
      // Adjust all transactions calculated with the previous costing.
      ScrollableResults trxs = getRelatedTransactions(newCosting.getStartingDate(),
          newCosting.getEndingDate());
      int i = 1;
      while (trxs.next()) {
        MaterialTransaction relTrx = (MaterialTransaction) trxs.get()[0];
        BigDecimal currentCost = CostAdjustmentUtils.getTrxCost(getCostAdjLine()
            .getCostAdjustment(), relTrx, true);
        BigDecimal expectedCost = relTrx.getMovementQuantity().abs().multiply(unitCost)
            .setScale(stdCurPrecission, RoundingMode.HALF_UP);
        if (expectedCost.compareTo(currentCost) != 0) {
          CostAdjustmentLine newCAL = insertCostAdjustmentLine(relTrx,
              expectedCost.subtract(currentCost), getCostAdjLine());
          newCAL.setRelatedTransactionAdjusted(true);
        }

        if (i % 100 == 0) {
          OBDal.getInstance().flush();
          OBDal.getInstance().getSession().clear();
        }
        i++;
      }
    }
  }

  private ScrollableResults getRelatedTransactions(Date startingDate, Date endingDate) {
    OrganizationStructureProvider osp = OBContext.getOBContext().getOrganizationStructureProvider(
        (String) DalUtil.getId(getCostOrg().getClient()));
    HashMap<CostDimension, BaseOBObject> costDimensions = getCostDimensions();
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
    where.append("  and trx." + MaterialTransaction.PROPERTY_MOVEMENTDATE + " >= :mvtdate");
    where.append("  and trx." + MaterialTransaction.PROPERTY_MOVEMENTDATE + " <= :enddate");
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
    trxQry.setNamedParameter("mvtdate", startingDate);
    trxQry.setNamedParameter("enddate", endingDate);
    trxQry.setNamedParameter("orgs", orgs);
    trxQry.setNamedParameter("product", trx.getProduct());
    if (warehouse != null) {
      trxQry.setNamedParameter("warehouse", warehouse);
    }

    return trxQry.scroll(ScrollMode.FORWARD_ONLY);

  }

  private Costing insertCost(Costing currentCosting, BigDecimal newCost) {
    MaterialTransaction transaction = getTransaction();
    currentCosting.setEndingDate(transaction.getTransactionProcessDate());
    OBDal.getInstance().save(currentCosting);

    Costing cost = (Costing) DalUtil.copy(currentCosting, false);
    cost.setCost(newCost);
    cost.setStartingDate(transaction.getTransactionProcessDate());
    cost.setCurrency(getCostCurrency());
    cost.setInventoryTransaction(transaction);
    if (isManufacturingProduct) {
      cost.setOrganization(OBDal.getInstance().get(Organization.class, "0"));
    } else {
      cost.setOrganization(getCostOrg());
    }
    cost.setCostType("STA");
    cost.setManual(false);
    cost.setPermanent(true);

    OBDal.getInstance().save(cost);
    return cost;
  }

  @Override
  BigDecimal getOutgoingBackdatedTrxAdjAmt() {
    // Calculate the standard cost on the transaction's movement date and adjust the cost if needed.
    MaterialTransaction trx = getTransaction();
    CostAdjustmentLine costAdjLine = getCostAdjLine();

    BigDecimal cost = CostingUtils.getStandardCost(trx.getProduct(), getCostOrg(),
        costAdjLine.getTransactionDate(), getCostDimensions(), getCostCurrency());

    BigDecimal expectedCostAmt = trx.getMovementQuantity().abs().multiply(cost);
    BigDecimal currentCost = trx.getTransactionCost();
    return expectedCostAmt.subtract(currentCost);
  }

  @Override
  void calculateNegativeStockCorrectionAdjustmentAmount() {
  }

  @Override
  protected void addCostDependingTrx(CostAdjustmentLine costAdjLine) {
    // Do nothing.
    // All transactions are calculated using the current standard cost so there is no need to
    // specifically search for dependent transactions.
  }
}
