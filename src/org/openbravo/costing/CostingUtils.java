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
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.costing.CostingAlgorithm.CostDimension;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.common.plm.Product;
import org.openbravo.model.materialmgmt.cost.Costing;
import org.openbravo.model.materialmgmt.cost.TransactionCost;
import org.openbravo.model.materialmgmt.transaction.MaterialTransaction;

public class CostingUtils {
  protected static Logger log4j = Logger.getLogger(CostingUtils.class);

  public static BigDecimal getTransactionCost(MaterialTransaction transaction, Date date) {
    OBCriteria<TransactionCost> obcTrxCost = OBDal.getInstance().createCriteria(
        TransactionCost.class);
    obcTrxCost.add(Restrictions.eq(TransactionCost.PROPERTY_INVENTORYTRANSACTION, transaction));
    obcTrxCost.add(Restrictions.le(TransactionCost.PROPERTY_COSTDATE, date));
    // obcTrxCost.setProjection(Projections.sum(TransactionCost.PROPERTY_COST));

    if (obcTrxCost.count() > 0) {
      BigDecimal cost = BigDecimal.ZERO;
      for (TransactionCost trxCost : obcTrxCost.list()) {
        cost = cost.add(trxCost.getCost());
      }
      return cost;
    }
    return null;
  }

  public static BigDecimal getStandardCost(Product product, Date date,
      HashMap<CostDimension, BaseOBObject> costDimensions) {
    // Get cost from M_Costing for given date.
    OBCriteria<Costing> obcCosting = OBDal.getInstance().createCriteria(Costing.class);
    obcCosting.add(Restrictions.eq(Costing.PROPERTY_PRODUCT, product));
    obcCosting.add(Restrictions.ge(Costing.PROPERTY_STARTINGDATE, date));
    obcCosting.add(Restrictions.lt(Costing.PROPERTY_ENDINGDATE, date));
    obcCosting.add(Restrictions.eq(Costing.PROPERTY_COSTTYPE, "ST"));
    if (costDimensions.get(CostDimension.Warehouse) != null) {
      // TODO: Add warehouse column on m_costing table to filter.
    }
    if (costDimensions.get(CostDimension.LegalEntity) != null) {
      obcCosting.add(Restrictions.eq(Costing.PROPERTY_ORGANIZATION,
          costDimensions.get(CostDimension.LegalEntity)));
    }
    if (obcCosting.count() > 0) {
      if (obcCosting.count() > 1) {
        // TODO: Add product and date to the log.
        log4j.warn("More than one cost found for same date");
      }
      return obcCosting.list().get(0).getCost();
    }
    return null;
  }

  public static HashMap<CostDimension, BaseOBObject> getEmptyDimensions() {
    HashMap<CostDimension, BaseOBObject> costDimensions = new HashMap<CostDimension, BaseOBObject>();
    costDimensions.put(CostDimension.Warehouse, null);
    costDimensions.put(CostDimension.LegalEntity, null);
    return costDimensions;
  }
}
