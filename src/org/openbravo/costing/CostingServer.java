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
import java.util.List;

import org.apache.log4j.Logger;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.util.OBClassLoader;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.erpCommon.utility.OBDateUtils;
import org.openbravo.model.common.currency.Currency;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.materialmgmt.cost.CostingRule;
import org.openbravo.model.materialmgmt.cost.TransactionCost;
import org.openbravo.model.materialmgmt.transaction.MaterialTransaction;

/**
 * @author gorkaion
 * 
 */
public class CostingServer {
  private MaterialTransaction transaction;
  private BigDecimal trxCost;
  protected static Logger log4j = Logger.getLogger(CostingServer.class);
  private CostingRule costingRule;
  private Currency currency;
  private Organization organization;

  public CostingServer(MaterialTransaction transaction) {
    this.transaction = transaction;
    init();
  }

  private void init() {
    organization = getOrganization();
    costingRule = getCostDimensionRule();
    currency = getCostCurrency();
    trxCost = transaction.getTransactionCost();
  }

  /**
   * Calculates and stores in the database the cost of the transaction.
   * 
   */
  public void process() throws OBException {
    if (trxCost != null) {
      // Transaction cost has already been calculated. Nothing to do.
      return;
    }
    log4j.debug("Process cost");
    try {
      OBContext.setAdminMode(false);
      // Get needed algorithm. And set it in the M_Transaction.
      CostingAlgorithm costingAlgorithm = getCostingAlgorithm();
      costingAlgorithm.init(this);
      log4j.debug("  *** Algorithm initializated: " + costingAlgorithm.getClass());

      trxCost = costingAlgorithm.getTransactionCost();
      if (trxCost == null && !transaction.getCostingStatus().equals("P")) {
        throw new OBException("@NoCostCalculated@: " + transaction.getIdentifier());
      }
      if (transaction.getCostingStatus().equals("P")) {
        return;
      }

      trxCost = trxCost.setScale(costingAlgorithm.getCostCurrency().getStandardPrecision()
          .intValue(), RoundingMode.HALF_UP);
      log4j.debug("  *** Transaction cost amount: " + trxCost.toString());
      // Save calculated cost on M_Transaction.
      transaction.setTransactionCost(trxCost);
      transaction.setCurrency(currency);
      transaction.setCostCalculated(true);
      transaction.setCostingStatus("CC");
      // insert on m_transaction_cost
      createTransactionCost();
      OBDal.getInstance().save(transaction);
    } finally {
      OBContext.restorePreviousMode();
    }
    return;
  }

  private CostingAlgorithm getCostingAlgorithm() {
    // Algorithm class is retrieved from costDimensionRule
    org.openbravo.model.materialmgmt.cost.CostingAlgorithm costAlgorithm = costingRule
        .getCostingAlgorithm();
    // FIXME: remove when manufacturing costs are fully migrated
    // In case the product is Manufacturing type it is forced to use Average Algorithm
    if (transaction.getProduct().isProduction()) {
      OBQuery<org.openbravo.model.materialmgmt.cost.CostingAlgorithm> caQry = OBDal.getInstance()
          .createQuery(
              org.openbravo.model.materialmgmt.cost.CostingAlgorithm.class,
              org.openbravo.model.materialmgmt.cost.CostingAlgorithm.PROPERTY_JAVACLASSNAME
                  + " = 'org.openbravo.costing.AverageAlgorithm'");
      caQry.setFilterOnReadableClients(false);
      caQry.setFilterOnReadableOrganization(false);
      costAlgorithm = caQry.uniqueResult();
    }
    transaction.setCostingAlgorithm(costAlgorithm);

    try {
      final Class<?> clz = OBClassLoader.getInstance().loadClass(costAlgorithm.getJavaClassName());
      return (CostingAlgorithm) clz.newInstance();
    } catch (Exception e) {
      log4j.error("Exception loading Algorithm class: " + costAlgorithm.getJavaClassName()
          + " algorithm: " + costAlgorithm.getIdentifier());
      throw new OBException("@AlgorithmClassNotLoaded@: " + costAlgorithm.getName(), e);
    }
  }

  private void createTransactionCost() {
    TransactionCost transactionCost = OBProvider.getInstance().get(TransactionCost.class);
    transactionCost.setInventoryTransaction(transaction);
    transactionCost.setOrganization(transaction.getOrganization());
    transactionCost.setCost(trxCost);
    transactionCost.setCurrency(currency);
    transactionCost.setCostDate(transaction.getTransactionProcessDate());
    OBDal.getInstance().save(transactionCost);
  }

  public BigDecimal getTransactionCost() {
    return trxCost;
  }

  private CostingRule getCostDimensionRule() {
    StringBuffer where = new StringBuffer();
    where.append(CostingRule.PROPERTY_ORGANIZATION + " = :organization");
    where.append(" and (" + CostingRule.PROPERTY_STARTINGDATE + " is null ");
    where.append("   or " + CostingRule.PROPERTY_STARTINGDATE + " <= :startdate)");
    where.append(" and (" + CostingRule.PROPERTY_ENDINGDATE + " is null");
    where.append("   or " + CostingRule.PROPERTY_ENDINGDATE + " > :enddate )");
    where.append(" order by case when " + CostingRule.PROPERTY_STARTINGDATE
        + " is null then 1 else 0 end, " + CostingRule.PROPERTY_STARTINGDATE + " desc");
    where.append(" and " + CostingRule.PROPERTY_VALIDATED + " = true");
    OBQuery<CostingRule> crQry = OBDal.getInstance().createQuery(CostingRule.class,
        where.toString());
    crQry.setFilterOnReadableOrganization(false);
    crQry.setNamedParameter("organization", organization);
    crQry.setNamedParameter("startdate", transaction.getTransactionProcessDate());
    crQry.setNamedParameter("enddate", transaction.getTransactionProcessDate());
    crQry.setMaxResult(1);
    List<CostingRule> costRules = crQry.list();
    if (costRules.size() == 0) {
      throw new OBException("@NoCostingRuleFoundForOrganizationAndDate@ @Organization@: "
          + organization.getName() + ", @Date@: "
          + OBDateUtils.formatDate(transaction.getTransactionProcessDate()));
    }
    return costRules.get(0);
  }

  public Currency getCostCurrency() {
    if (currency != null) {
      return currency;
    }
    // FIXME: remove when manufacturing costs are fully migrated
    // Production product costs are calculated in clients currency
    if (transaction.getProduct().isProduction()) {
      return transaction.getClient().getCurrency();
    }
    if (organization != null) {
      if (organization.getCurrency() != null) {
        return organization.getCurrency();
      }
      return organization.getClient().getCurrency();
    } else {
      init();
      return getCostCurrency();
    }
  }

  public Organization getOrganization() {
    if (organization != null) {
      return organization;
    }
    Organization org = OBContext.getOBContext()
        .getOrganizationStructureProvider(transaction.getClient().getId())
        .getLegalEntity(transaction.getOrganization());
    if (org == null) {
      throw new OBException("@WrongCostOrganization@" + transaction.getIdentifier());
    }
    return org;
  }

  public CostingRule getCostingRule() {
    return costingRule;
  }

  public MaterialTransaction getTransaction() {
    return transaction;
  }

  /**
   * Transaction types implemented on the cost engine.
   */
  public enum TrxType {
    Shipment, ShipmentReturn, ShipmentVoid, ShipmentNegative, Receipt, ReceiptReturn, ReceiptVoid, ReceiptNegative, InventoryIncrease, InventoryDecrease, IntMovementFrom, IntMovementTo, InternalCons, InternalConsNegative, InternalConsVoid, BOMPart, BOMProduct, ManufacturingConsumed, ManufacturingProduced, Unknown;
    /**
     * Given a Material Management transaction returns its type.
     */
    static TrxType getTrxType(MaterialTransaction transaction) {
      if (transaction.getGoodsShipmentLine() != null) {
        // Receipt / Shipment
        org.openbravo.model.materialmgmt.transaction.ShipmentInOut inout = transaction
            .getGoodsShipmentLine().getShipmentReceipt();
        if (inout.isSalesTransaction()) {
          // Shipment
          if (inout.getDocumentStatus().equals("VO")
              && transaction.getGoodsShipmentLine().getCanceledInoutLine() != null) {
            log4j.debug("Void shipment: " + transaction.getGoodsShipmentLine().getIdentifier());
            return ShipmentVoid;
          } else if (inout.getDocumentType().isReturn()) {
            log4j.debug("Reversal shipment: " + transaction.getGoodsShipmentLine().getIdentifier());
            return ShipmentReturn;
          } else if (transaction.getGoodsShipmentLine().getMovementQuantity()
              .compareTo(BigDecimal.ZERO) < 0) {
            log4j.debug("Negative Shipment: " + transaction.getGoodsShipmentLine().getIdentifier());
            return ShipmentNegative;
          } else {
            log4j.debug("Shipment: " + transaction.getGoodsShipmentLine().getIdentifier());
            return Shipment;
          }
        } else {
          // Receipt
          if (inout.getDocumentStatus().equals("VO")
              && transaction.getGoodsShipmentLine().getCanceledInoutLine() != null) {
            log4j.debug("Void receipt: " + transaction.getGoodsShipmentLine().getIdentifier());
            return ReceiptVoid;
          } else if (inout.getDocumentType().isReturn()) {
            log4j.debug("Reversal Receipt: " + transaction.getGoodsShipmentLine().getIdentifier());
            return ReceiptReturn;
          } else if (transaction.getGoodsShipmentLine().getMovementQuantity()
              .compareTo(BigDecimal.ZERO) < 0) {
            log4j.debug("Negative Receipt: " + transaction.getGoodsShipmentLine().getIdentifier());
            return ReceiptNegative;
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
        if (transaction.getInternalConsumptionLine().getVoidedInternalConsumptionLine() != null) {
          return InternalConsVoid;
        } else if (transaction.getMovementQuantity().compareTo(BigDecimal.ZERO) > 0) {
          log4j.debug("Negative Internal Consumption: "
              + transaction.getInternalConsumptionLine().getIdentifier());
          return InternalConsNegative;
        } else {
          log4j.debug("Internal Consumption: "
              + transaction.getInternalConsumptionLine().getIdentifier());
          return InternalCons;
        }
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
          if ("+".equals(transaction.getProductionLine().getProductionType())) {
            return ManufacturingProduced;
          } else {
            return ManufacturingConsumed;
          }
        }
      }
      return Unknown;
    }

  }
}
