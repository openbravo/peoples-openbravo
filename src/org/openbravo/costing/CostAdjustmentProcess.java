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

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.weld.WeldUtils;
import org.openbravo.client.kernel.ComponentProvider;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.financial.FinancialUtils;
import org.openbravo.model.materialmgmt.cost.CostAdjustment;
import org.openbravo.model.materialmgmt.cost.CostAdjustmentLine;
import org.openbravo.model.materialmgmt.cost.TransactionCost;
import org.openbravo.model.materialmgmt.transaction.MaterialTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CostAdjustmentProcess {
  private static final Logger log = LoggerFactory.getLogger(CostAdjustmentProcessHandler.class);
  @Inject
  @Any
  private Instance<CostingAlgorithmAdjustmentImp> costAdjustmentAlgorithms;
  @Inject
  @Any
  private Instance<CostAdjusmentProcessCheck> costAdjustmentProcessChecks;

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
  public JSONObject processCostAdjustment(CostAdjustment _costAdjustment) throws OBException,
      JSONException {
    CostAdjustment costAdjustment = _costAdjustment;
    JSONObject message = new JSONObject();
    message.put("severity", "success");
    message.put("title", "");
    message.put("text", OBMessageUtils.messageBD("Success"));
    OBContext.setAdminMode(true);
    try {
      doChecks(costAdjustment, message);
      initializeLines(costAdjustment);
      calculateAdjustmentAmount(costAdjustment.getId(), message);

      costAdjustment = OBDal.getInstance().get(CostAdjustment.class, costAdjustment.getId());
      costAdjustment.setProcessed(true);
      costAdjustment.setDocumentStatus("CO");
      OBDal.getInstance().save(costAdjustment);
    } finally {
      OBContext.restorePreviousMode();
    }
    return message;
  }

  private void doChecks(CostAdjustment costAdjustment, JSONObject message) {
    // Execute checks added implementing costAdjustmentProcess interface.

    for (CostAdjusmentProcessCheck checksInstance : costAdjustmentProcessChecks) {
      checksInstance.doCheck(costAdjustment, message);
    }
  }

  private void initializeLines(CostAdjustment costAdjustment) {
    // initialize is related transaction adjusted flag to false
    OBCriteria<CostAdjustmentLine> critLines = OBDal.getInstance().createCriteria(
        CostAdjustmentLine.class);
    critLines.add(Restrictions.eq(CostAdjustmentLine.PROPERTY_COSTADJUSTMENT, costAdjustment));
    critLines.add(Restrictions.eq(CostAdjustmentLine.PROPERTY_ISRELATEDTRANSACTIONADJUSTED, true));
    ScrollableResults lines = critLines.scroll(ScrollMode.FORWARD_ONLY);
    long count = 1L;
    try {
      while (lines.next()) {
        CostAdjustmentLine line = (CostAdjustmentLine) lines.get(0);
        line.setRelatedTransactionAdjusted(false);
        OBDal.getInstance().save(line);

        if (count % 1000 == 0) {
          OBDal.getInstance().flush();
          OBDal.getInstance().getSession().clear();
        }
        count++;
      }
      OBDal.getInstance().flush();
      OBDal.getInstance().getSession().clear();
    } finally {
      lines.close();
    }
  }

  private void calculateAdjustmentAmount(String strCostAdjustmentId, JSONObject message) {
    boolean hasNewLines = false;
    OBCriteria<CostAdjustmentLine> critLines = OBDal.getInstance().createCriteria(
        CostAdjustmentLine.class);
    critLines.createAlias(CostAdjustmentLine.PROPERTY_INVENTORYTRANSACTION, "trx");
    critLines.createAlias(CostAdjustmentLine.PROPERTY_COSTADJUSTMENT, "ca");
    critLines.add(Restrictions.eq("ca.id", strCostAdjustmentId));
    critLines.add(Restrictions.eq(CostAdjustmentLine.PROPERTY_ISRELATEDTRANSACTIONADJUSTED, false));
    critLines.addOrder(Order.asc("trx." + MaterialTransaction.PROPERTY_MOVEMENTDATE));
    critLines.addOrder(Order.asc("trx." + MaterialTransaction.PROPERTY_TRANSACTIONPROCESSDATE));
    ScrollableResults lines = critLines.scroll(ScrollMode.FORWARD_ONLY);
    try {
      while (lines.next()) {
        hasNewLines = true;
        CostAdjustmentLine line = (CostAdjustmentLine) lines.get(0);
        final String strCostAdjLineId = line.getId();
        MaterialTransaction trx = line.getInventoryTransaction();
        if (trx.getCostingAlgorithm() == null) {
          throw new OBException("Cannot adjust transactions calculated with legacy cost engine.");
        }

        // Add transactions that depend on the transaction being adjusted.
        CostingAlgorithmAdjustmentImp costAdjImp = getAlgorithmAdjustmentImp(trx
            .getCostingAlgorithm().getId());

        if (costAdjImp == null) {
          throw new OBException(
              "The algorithm used to calculate the cost of the transaction does not implement cost adjustments.");
        }
        costAdjImp.init(line);
        costAdjImp.searchRelatedTransactionCosts();
        // Reload cost adjustment object in case the costing algorithm has cleared the session.
        line = OBDal.getInstance().get(CostAdjustmentLine.class, strCostAdjLineId);
        line.setRelatedTransactionAdjusted(true);
        OBDal.getInstance().save(line);
        OBDal.getInstance().flush();
        OBDal.getInstance().getSession().clear();
      }
    } finally {
      lines.close();
    }
    generateTransactionCosts(OBDal.getInstance().get(CostAdjustment.class, strCostAdjustmentId));
    if (hasNewLines) {
      calculateAdjustmentAmount(strCostAdjustmentId, message);
    }
  }

  private void generateTransactionCosts(CostAdjustment costAdjustment) {
    OBCriteria<CostAdjustmentLine> critLines = OBDal.getInstance().createCriteria(
        CostAdjustmentLine.class);
    Date referenceDate = costAdjustment.getReferenceDate();
    critLines.add(Restrictions.eq(CostAdjustmentLine.PROPERTY_COSTADJUSTMENT, costAdjustment));
    ScrollableResults lines = critLines.scroll(ScrollMode.FORWARD_ONLY);

    while (lines.next()) {
      CostAdjustmentLine line = (CostAdjustmentLine) lines.get(0);
      if (!line.getTransactionCostList().isEmpty()) {
        continue;
      }
      TransactionCost trxCost = OBProvider.getInstance().get(TransactionCost.class);
      MaterialTransaction trx = line.getInventoryTransaction();
      trxCost.setInventoryTransaction(trx);
      trxCost.setOrganization(trx.getOrganization());
      trxCost.setCostDate(referenceDate);
      trxCost.setCostAdjustmentLine(line);
      trxCost.setUnitCost(line.isUnitCost());
      Date accountingDate = line.getAccountingDate();
      if (accountingDate == null) {
        accountingDate = trx.getMovementDate();
      }
      trxCost.setAccountingDate(accountingDate);
      BigDecimal convertedAmt = line.getAdjustmentAmount();
      if (!line.getCurrency().getId().equals(trx.getCurrency().getId())) {
        convertedAmt = FinancialUtils.getConvertedAmount(convertedAmt, line.getCurrency(),
            trx.getCurrency(), accountingDate, trx.getOrganization(), "C");
      }
      trxCost.setCost(convertedAmt);
      trxCost.setCurrency(line.getCurrency());

      OBDal.getInstance().save(trxCost);
      OBDal.getInstance().flush();
      OBDal.getInstance().getSession().clear();
    }
    lines.close();
  }

  private CostingAlgorithmAdjustmentImp getAlgorithmAdjustmentImp(String strCostingAlgorithmId) {
    CostingAlgorithmAdjustmentImp implementor = null;
    for (CostingAlgorithmAdjustmentImp nextImplementor : costAdjustmentAlgorithms
        .select(new ComponentProvider.Selector(strCostingAlgorithmId))) {
      if (implementor == null) {
        implementor = nextImplementor;
      } else {
        log.warn("More than one class found implementing cost adjustment for algorithm with id {}",
            strCostingAlgorithmId);
      }
    }
    return implementor;
  }

  public static JSONObject doProcessCostAdjustment(CostAdjustment costAdjustment)
      throws OBException, JSONException {
    CostAdjustmentProcess cap = WeldUtils
        .getInstanceFromStaticBeanManager(CostAdjustmentProcess.class);
    JSONObject message = cap.processCostAdjustment(costAdjustment);
    return message;
  }
}
