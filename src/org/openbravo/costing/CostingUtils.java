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

import org.apache.log4j.Logger;
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.costing.CostingAlgorithm.CostDimension;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.common.plm.Product;
import org.openbravo.model.materialmgmt.cost.Costing;
import org.openbravo.model.materialmgmt.cost.TransactionCost;
import org.openbravo.model.materialmgmt.transaction.MaterialTransaction;

public class CostingUtils {
  protected static Logger log4j = Logger.getLogger(CostingUtils.class);

  public static BigDecimal getTransactionCost(MaterialTransaction transaction, Date date) {
    return getTransactionCost(transaction, date, false);
  }

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
        CostingServer transactionCost = new CostingServer(transaction);
        transactionCost.process();
        return transactionCost.getTransactionCost();
      }
      log4j.error("No cost found for transaction " + transaction.getIdentifier() + " with id "
          + transaction.getId() + " on date " + Utility.formatDate(date));
      throw new OBException("@NoCostFoundForTrxOnDate@ " + transaction.getIdentifier() + " @date@ "
          + Utility.formatDate(date));
    }
    BigDecimal cost = BigDecimal.ZERO;
    for (TransactionCost trxCost : obcTrxCost.list()) {
      cost = cost.add(trxCost.getCost());
    }
    return cost;
  }

  public static BigDecimal getStandardCost(Product product, Date date,
      HashMap<CostDimension, BaseOBObject> costDimensions) throws OBException {
    return getStandardCost(product, date, costDimensions, true);
  }

  public static BigDecimal getStandardCost(Product product, Date date,
      HashMap<CostDimension, BaseOBObject> costDimensions, boolean recheckWithoutDimensions)
      throws OBException {
    // Get cost from M_Costing for given date.
    OBCriteria<Costing> obcCosting = OBDal.getInstance().createCriteria(Costing.class);
    obcCosting.add(Restrictions.eq(Costing.PROPERTY_PRODUCT, product));
    obcCosting.add(Restrictions.le(Costing.PROPERTY_STARTINGDATE, date));
    obcCosting.add(Restrictions.gt(Costing.PROPERTY_ENDINGDATE, date));
    obcCosting.add(Restrictions.eq(Costing.PROPERTY_COSTTYPE, "ST"));
    if (costDimensions.get(CostDimension.Warehouse) != null) {
      // TODO: Add warehouse column on m_costing table to filter.
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
      if (obcCosting.list().get(0).getCost() == null) {
        throw new OBException("@NoStandardCostDefined@ @Product@: " + product.getName()
            + ", @date@: " + Utility.formatDate(date));
      }
      return obcCosting.list().get(0).getCost();
    } else if (recheckWithoutDimensions) {
      return getStandardCost(product, date, getEmptyDimensions(), false);
    }
    // If no standard cost is found throw an exception.
    throw new OBException("@NoStandardCostDefined@ @Product@: " + product.getName() + ", @date@: "
        + Utility.formatDate(date));
  }

  public static HashMap<CostDimension, BaseOBObject> getEmptyDimensions() {
    HashMap<CostDimension, BaseOBObject> costDimensions = new HashMap<CostDimension, BaseOBObject>();
    costDimensions.put(CostDimension.Warehouse, null);
    costDimensions.put(CostDimension.LegalEntity, null);
    return costDimensions;
  }
}
