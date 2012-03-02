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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.hibernate.criterion.Restrictions;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBDateUtils;
import org.openbravo.model.common.enterprise.Warehouse;
import org.openbravo.model.common.plm.Product;
import org.openbravo.model.materialmgmt.cost.Costing;

public class AverageAlgorithm extends CostingAlgorithm {

  public BigDecimal getTransactionCost() {
    BigDecimal trxCost = super.getTransactionCost();
    // If it is a transaction whose cost has not been calculated based on current average cost
    // calculate calculate new average cost.
    switch (trxType) {
    case Receipt:
    case ReceiptVoid:
    case ReceiptReturn:
    case ShipmentVoid:
    case ShipmentReturn:
    case ShipmentNegative:
    case InventoryIncrease:
    case IntMovementTo:
    case InternalConsNegative:
    case InternalConsVoid:
    case BOMProduct:
      Costing currentCosting = getProductCost();
      BigDecimal trxCostWithSign = (transaction.getMovementQuantity().signum() == -1) ? trxCost
          .negate() : trxCost;
      BigDecimal newCost = null;
      BigDecimal currentStock = CostingUtils.getCurrentStock(transaction.getProduct(),
          transaction.getTransactionProcessDate(), costDimensions);
      if (currentCosting == null) {
        if (transaction.getMovementQuantity().signum() == 0) {
          newCost = BigDecimal.ZERO;
        } else {
          newCost = trxCostWithSign.divide(transaction.getMovementQuantity(), costCurrency
              .getCostingPrecision().intValue(), RoundingMode.HALF_UP);
        }
      } else {
        BigDecimal newCostAmt = currentCosting.getCost().multiply(currentStock)
            .add(trxCostWithSign);
        BigDecimal newStock = currentStock.add(transaction.getMovementQuantity());
        if (newStock.signum() == 0) {
          // If stock is zero keep current cost.
          // FIXME: Cost adjustment is needed if newCostAmt is not zero.
          newCost = currentCosting.getCost();
        } else {
          newCost = newCostAmt.divide(newStock, costCurrency.getCostingPrecision().intValue(),
              RoundingMode.HALF_UP);
        }
      }
      insertCost(currentCosting, newCost, currentStock, trxCostWithSign);
    default:
      break;
    }
    return trxCost;
  }

  @Override
  protected BigDecimal getOutgoingTransactionCost() {
    Costing currentCosting = getProductCost();
    if (currentCosting == null) {
      throw new OBException("@NoAvgCostDefined@ @Product@: " + transaction.getProduct().getName()
          + ", @Date@: " + OBDateUtils.formatDate(transaction.getTransactionProcessDate()));
    }
    BigDecimal cost = currentCosting.getCost();
    return transaction.getMovementQuantity().abs().multiply(cost);
  }

  /**
   * Cost of incoming physical inventory is calculated based on current average cost. If there is no
   * average cost it uses the default method..
   */
  @Override
  protected BigDecimal getInventoryIncreaseCost() {
    try {
      return getOutgoingTransactionCost();
    } catch (OBException e) {
      return super.getInventoryIncreaseCost();
    }
  }

  private void insertCost(Costing currentCosting, BigDecimal newCost, BigDecimal currentStock,
      BigDecimal trxCost) {
    Date dateTo = getLastDate();
    if (currentCosting != null) {
      dateTo = currentCosting.getEndingDate();
      currentCosting.setEndingDate(transaction.getTransactionProcessDate());
      OBDal.getInstance().save(currentCosting);
    }
    Costing cost = OBProvider.getInstance().get(Costing.class);
    cost.setCost(newCost);
    cost.setStartingDate(transaction.getTransactionProcessDate());
    cost.setEndingDate(dateTo);
    cost.setInventoryTransaction(transaction);
    cost.setProduct(transaction.getProduct());
    cost.setOrganization(costOrg);
    cost.setQuantity(transaction.getMovementQuantity());
    cost.setTotalMovementQuantity(currentStock.add(transaction.getMovementQuantity()));
    cost.setPrice(trxCost.divide(transaction.getMovementQuantity(), costCurrency
        .getCostingPrecision().intValue()));
    cost.setCostType("AVA");
    cost.setManual(false);
    cost.setPermanent(false);
    cost.setProduction(false);
    cost.setWarehouse((Warehouse) costDimensions.get(CostDimension.Warehouse));
    OBDal.getInstance().save(cost);
  }

  private Costing getProductCost() {
    Product product = transaction.getProduct();
    Date date = transaction.getTransactionProcessDate();
    OBCriteria<Costing> obcCosting = OBDal.getInstance().createCriteria(Costing.class);
    obcCosting.add(Restrictions.eq(Costing.PROPERTY_PRODUCT, product));
    obcCosting.add(Restrictions.le(Costing.PROPERTY_STARTINGDATE, date));
    obcCosting.add(Restrictions.gt(Costing.PROPERTY_ENDINGDATE, date));
    obcCosting.add(Restrictions.eq(Costing.PROPERTY_COSTTYPE, "AVA"));
    if (costDimensions.get(CostDimension.Warehouse) != null) {
      obcCosting.add(Restrictions.eq(Costing.PROPERTY_WAREHOUSE,
          costDimensions.get(CostDimension.Warehouse)));
    }
    if (costDimensions.get(CostDimension.LegalEntity) != null) {
      obcCosting.add(Restrictions.eq(Costing.PROPERTY_ORGANIZATION,
          costDimensions.get(CostDimension.LegalEntity)));
    } else {
      obcCosting.setFilterOnReadableOrganization(false);
    }
    if (obcCosting.count() > 0) {
      if (obcCosting.count() > 1) {
        log4j.warn("More than one cost found for same date: " + OBDateUtils.formatDate(date)
            + " for product: " + product.getName() + " (" + product.getId() + ")");
      }
      return obcCosting.list().get(0);
    }
    // If no average cost is found return null.
    return null;
  }

  private Date getLastDate() {
    SimpleDateFormat outputFormat = new SimpleDateFormat("dd-MM-yyyy");
    try {
      return outputFormat.parse("31-12-9999");
    } catch (ParseException e) {
      // Error parsing the date.
      log4j.error("Error parsing the date.", e);
      return null;
    }
  }

}
