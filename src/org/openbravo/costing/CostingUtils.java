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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.costing.CostingAlgorithm.CostDimension;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBDateUtils;
import org.openbravo.model.common.enterprise.Locator;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.common.plm.Product;
import org.openbravo.model.materialmgmt.cost.Costing;
import org.openbravo.model.materialmgmt.cost.TransactionCost;
import org.openbravo.model.materialmgmt.transaction.MaterialTransaction;

public class CostingUtils {
  protected static Logger log4j = Logger.getLogger(CostingUtils.class);

  /**
   * Calls {@link #getTransactionCost(MaterialTransaction, Date, boolean)} setting the calculateTrx
   * flag to false.
   */
  public static BigDecimal getTransactionCost(MaterialTransaction transaction, Date date) {
    return getTransactionCost(transaction, date, false);
  }

  /**
   * Calculates the total cost amount of a transaction including the cost adjustments done until the
   * given date.
   * 
   * @param transaction
   *          MaterialTransaction to get its cost.
   * @param date
   *          The Date it is desired to know the cost.
   * @param calculateTrx
   *          boolean flag to force the calculation of the transaction cost if it is not calculated.
   * @return The total cost amount.
   */
  public static BigDecimal getTransactionCost(MaterialTransaction transaction, Date date,
      boolean calculateTrx) {
    OBCriteria<TransactionCost> obcTrxCost = OBDal.getInstance().createCriteria(
        TransactionCost.class);
    obcTrxCost.add(Restrictions.eq(TransactionCost.PROPERTY_INVENTORYTRANSACTION, transaction));
    obcTrxCost.add(Restrictions.le(TransactionCost.PROPERTY_COSTDATE, date));
    // obcTrxCost.setProjection(Projections.sum(TransactionCost.PROPERTY_COST));

    if (obcTrxCost.count() == 0) {
      // Transaction hasn't been calculated yet.
      if (calculateTrx) {
        log4j.debug("Cost for transaction will be calculated." + transaction.getIdentifier());
        CostingServer transactionCost = new CostingServer(transaction);
        transactionCost.process();
        return transactionCost.getTransactionCost();
      }
      log4j.error("No cost found for transaction " + transaction.getIdentifier() + " with id "
          + transaction.getId() + " on date " + OBDateUtils.formatDate(date));
      throw new OBException("@NoCostFoundForTrxOnDate@ @Transaction@: "
          + transaction.getIdentifier() + " @Date@ " + OBDateUtils.formatDate(date));
    }
    BigDecimal cost = BigDecimal.ZERO;
    for (TransactionCost trxCost : obcTrxCost.list()) {
      cost = cost.add(trxCost.getCost());
    }
    return cost;
  }

  /**
   * Calls {@link #getStandardCost(Product, Date, HashMap, boolean)} setting the
   * recheckWithoutDimensions flag to true.
   */
  public static BigDecimal getStandardCost(Product product, Date date,
      HashMap<CostDimension, BaseOBObject> costDimensions) throws OBException {
    return getStandardCost(product, date, costDimensions, true);
  }

  /**
   * Calculates the standard cost of a product on the given date and cost dimensions.
   * 
   * @param product
   *          The Product to get its Standard Cost
   * @param date
   *          The Date to get the Standard Cost
   * @param costDimensions
   *          The cost dimensions to get the Standard Cost if it is defined by some of them.
   * @param recheckWithoutDimensions
   *          boolean flag to force a recall the method to get the Standard Cost at client level if
   *          no cost is found in the given cost dimensions.
   * @return the Standard Cost.
   * @throws OBException
   *           when no standard cost is found.
   */
  public static BigDecimal getStandardCost(Product product, Date date,
      HashMap<CostDimension, BaseOBObject> costDimensions, boolean recheckWithoutDimensions)
      throws OBException {
    // Get cost from M_Costing for given date.
    OBCriteria<Costing> obcCosting = OBDal.getInstance().createCriteria(Costing.class);
    obcCosting.add(Restrictions.eq(Costing.PROPERTY_PRODUCT, product));
    obcCosting.add(Restrictions.le(Costing.PROPERTY_STARTINGDATE, date));
    obcCosting.add(Restrictions.gt(Costing.PROPERTY_ENDINGDATE, date));
    obcCosting.add(Restrictions.eq(Costing.PROPERTY_COSTTYPE, "STA"));
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
      if (obcCosting.list().get(0).getCost() == null) {
        throw new OBException("@NoStandardCostDefined@ @Product@: " + product.getName()
            + ", @Date@: " + OBDateUtils.formatDate(date));
      }
      return obcCosting.list().get(0).getCost();
    } else if (recheckWithoutDimensions) {
      return getStandardCost(product, date, getEmptyDimensions(), false);
    }
    // If no standard cost is found throw an exception.
    throw new OBException("@NoStandardCostDefined@ @Product@: " + product.getName() + ", @Date@: "
        + OBDateUtils.formatDate(date));
  }

  /**
   * @return The costDimensions HashMap with null values for the dimensions.
   */
  public static HashMap<CostDimension, BaseOBObject> getEmptyDimensions() {
    HashMap<CostDimension, BaseOBObject> costDimensions = new HashMap<CostDimension, BaseOBObject>();
    costDimensions.put(CostDimension.Warehouse, null);
    costDimensions.put(CostDimension.LegalEntity, null);
    return costDimensions;
  }

  /**
   * Calculates the stock of the product on the given date and for the given cost dimensions. It
   * only takes transactions that have its cost calculated.
   */
  public static BigDecimal getCurrentStock(Product product, Date date,
      HashMap<CostDimension, BaseOBObject> costDimensions) {
    OBCriteria<MaterialTransaction> obcTrx = OBDal.getInstance().createCriteria(
        MaterialTransaction.class);
    obcTrx.createAlias(MaterialTransaction.PROPERTY_STORAGEBIN, "locator");
    obcTrx.setFilterOnReadableOrganization(false);
    obcTrx.add(Restrictions.eq(MaterialTransaction.PROPERTY_PRODUCT, product));
    obcTrx.add(Restrictions.le(MaterialTransaction.PROPERTY_TRANSACTIONPROCESSDATE, date));
    // Include only transactions that have its cost calculated
    obcTrx.add(Restrictions.isNotNull(MaterialTransaction.PROPERTY_TRANSACTIONCOST));
    if (costDimensions.get(CostDimension.Warehouse) != null) {
      obcTrx.add(Restrictions.eq("locator." + Locator.PROPERTY_WAREHOUSE,
          costDimensions.get(CostDimension.Warehouse)));
    }
    if (costDimensions.get(CostDimension.LegalEntity) != null) {
      // Get child tree of organizations.
      Set<String> orgs = OBContext
          .getOBContext()
          .getOrganizationStructureProvider()
          .getChildTree(((Organization) costDimensions.get(CostDimension.LegalEntity)).getId(),
              true);
      obcTrx.add(Restrictions.in(MaterialTransaction.PROPERTY_ORGANIZATION + ".id", orgs));
    }
    obcTrx.setProjection(Projections.sum(MaterialTransaction.PROPERTY_MOVEMENTQUANTITY));

    if (obcTrx.list() != null && obcTrx.list().size() > 0) {
      @SuppressWarnings("rawtypes")
      List o = obcTrx.list();
      Object resultSet = (Object) o.get(0);
      return (resultSet != null) ? (BigDecimal) resultSet : BigDecimal.ZERO;
    }

    return BigDecimal.ZERO;
  }
}
