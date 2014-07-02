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

import java.util.Date;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.model.materialmgmt.cost.CostAdjustment;
import org.openbravo.model.materialmgmt.cost.CostAdjustmentLine;
import org.openbravo.model.materialmgmt.cost.TransactionCost;
import org.openbravo.model.materialmgmt.transaction.MaterialTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CostAdjustmentProcess {
  private static final Logger log = LoggerFactory.getLogger(CostAdjustmentProcessHandler.class);

  /**
   * Method to process a cost adjustment.
   * 
   * @param costAdj
   *          the cost adjustment to be processed.
   * @return the message to be shown to the user properly formatted and translated to the user
   *         language.
   * @throws OBException
   *           when there is an error that prevents the cost adjustment to be processed.
   * @throws JSONException
   */
  public static JSONObject processCostAdjustment(CostAdjustment costAdjustment) throws OBException,
      JSONException {
    JSONObject message = new JSONObject();
    message.put("severity", "success");
    message.put("title", "");
    message.put("text", OBMessageUtils.messageBD("Success"));
    doChecks(costAdjustment, message);
    calculateAdjustmentAmount(costAdjustment, message);
    generateTransactionCosts(costAdjustment);
    return message;
  }

  private static void doChecks(CostAdjustment costAdjustment, JSONObject message) {

  }

  private static void calculateAdjustmentAmount(CostAdjustment costAdjustment, JSONObject message) {
    OBCriteria<CostAdjustmentLine> critLines = OBDal.getInstance().createCriteria(
        CostAdjustmentLine.class);
    critLines.createAlias(CostAdjustmentLine.PROPERTY_INVENTORYTRANSACTION, "trx");
    critLines.add(Restrictions.eq(CostAdjustmentLine.PROPERTY_COSTADJUSTMENT, costAdjustment));
    critLines.addOrderBy("trx." + MaterialTransaction.PROPERTY_MOVEMENTDATE, true);
    critLines.addOrderBy("trx." + MaterialTransaction.PROPERTY_TRANSACTIONPROCESSDATE, true);

  }

  private static void generateTransactionCosts(CostAdjustment costAdjustment) {
    OBCriteria<CostAdjustmentLine> critLines = OBDal.getInstance().createCriteria(
        CostAdjustmentLine.class);
    Date referenceDate = costAdjustment.getReferenceDate();
    critLines.add(Restrictions.eq(CostAdjustmentLine.PROPERTY_COSTADJUSTMENT, costAdjustment));
    ScrollableResults lines = critLines.scroll(ScrollMode.FORWARD_ONLY);
    while (lines.next()) {
      CostAdjustmentLine line = (CostAdjustmentLine) lines.get(0);
      TransactionCost trxCost = OBProvider.getInstance().get(TransactionCost.class);
      trxCost.setInventoryTransaction(line.getInventoryTransaction());
      trxCost.setCost(line.getAdjustmentAmount());
      trxCost.setCostDate(referenceDate);
      // FIXME: Set proper currency!!!
      trxCost.setCurrency(null);

      OBDal.getInstance().save(trxCost);
      OBDal.getInstance().flush();
      OBDal.getInstance().getSession().clear();
    }
    lines.close();
  }
}
