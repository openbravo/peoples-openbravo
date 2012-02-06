package org.openbravo.costing;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.hibernate.criterion.Restrictions;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.common.enterprise.Warehouse;
import org.openbravo.model.common.plm.Product;
import org.openbravo.model.materialmgmt.cost.Costing;

public class AverageAlgorithm extends CostingAlgorithm {

  @Override
  protected BigDecimal getOutgoingTransactionCost() {
    Costing currentCosting = getProductCost();
    if (currentCosting == null) {
      throw new OBException("@NoAvgCostDefined@ @Product@: " + transaction.getProduct().getName()
          + ", @date@: " + Utility.formatDate(transaction.getTransactionProcessDate()));
    }
    BigDecimal cost = currentCosting.getCost();
    return transaction.getMovementQuantity().abs().multiply(cost);
  }

  public BigDecimal getTransactionCost() {
    BigDecimal trxCost = super.getTransactionCost();
    // If it is an incoming transaction update cost.
    if (transaction.getMovementQuantity().compareTo(BigDecimal.ZERO) == 1) {
      Costing currentCosting = getProductCost();
      BigDecimal newCost = null;
      BigDecimal currentStock = CostingUtils.getCurrentStock(transaction.getProduct(),
          transaction.getTransactionProcessDate(), costDimensions);
      if (currentCosting == null) {
        newCost = trxCost.divide(transaction.getMovementQuantity(), costCurrency
            .getCostingPrecision().intValue());
      } else {
        BigDecimal newCostAmt = currentCosting.getCost().multiply(currentStock).add(trxCost);
        newCost = newCostAmt.divide(currentStock.add(transaction.getMovementQuantity()),
            costCurrency.getCostingPrecision().intValue());
      }
      insertCost(currentCosting, newCost, currentStock, trxCost);

    }
    return trxCost;
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
    cost.setManual(false);
    cost.setPermanent(false);
    cost.setProduct(transaction.getProduct());
    cost.setOrganization(costOrg);
    cost.setQuantity(transaction.getMovementQuantity());
    cost.setTotalMovementQuantity(currentStock.add(transaction.getMovementQuantity()));
    cost.setPrice(trxCost.divide(transaction.getMovementQuantity(), costCurrency
        .getCostingPrecision().intValue()));
    cost.setCostType("AV");
    cost.setProduction(false);
    cost.setWarehouse((Warehouse) costDimensions.get(CostDimension.Warehouse));
    // TODO: set inoutline id, what about other transaction types ?
    OBDal.getInstance().save(cost);
  }

  private Costing getProductCost() {
    Product product = transaction.getProduct();
    Date date = transaction.getTransactionProcessDate();
    OBCriteria<Costing> obcCosting = OBDal.getInstance().createCriteria(Costing.class);
    obcCosting.add(Restrictions.eq(Costing.PROPERTY_PRODUCT, product));
    obcCosting.add(Restrictions.le(Costing.PROPERTY_STARTINGDATE, date));
    obcCosting.add(Restrictions.gt(Costing.PROPERTY_ENDINGDATE, date));
    obcCosting.add(Restrictions.eq(Costing.PROPERTY_COSTTYPE, "AV"));
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
        log4j.warn("More than one cost found for same date: " + Utility.formatDate(date)
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
