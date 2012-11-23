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

import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.openbravo.base.exception.OBException;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.core.SessionHandler;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.model.common.plm.Product;
import org.openbravo.model.materialmgmt.transaction.MaterialTransaction;
import org.openbravo.scheduling.ProcessBundle;
import org.openbravo.scheduling.ProcessLogger;
import org.openbravo.service.db.DalBaseProcess;

/**
 * @author gorkaion
 * 
 */
public class CostingBackground extends DalBaseProcess {
  private static final Logger log4j = Logger.getLogger(CostingBackground.class);
  private ProcessLogger logger;

  @Override
  protected void doExecute(ProcessBundle bundle) throws Exception {
    logger = bundle.getLogger();
    OBError result = new OBError();
    result.setType("Success");
    result.setTitle(OBMessageUtils.messageBD("Success"));

    List<MaterialTransaction> trxs = getTransactionsBatch();
    int counter = 0, total = trxs.size(), batch = 0;
    boolean pendingTrx = areTransactionsPending();
    while (pendingTrx) {
      batch++;
      for (MaterialTransaction transaction : trxs) {
        counter++;
        try {
          log4j.debug("Start transaction process: " + transaction.getId());
          CostingServer transactionCost = new CostingServer(transaction);
          transactionCost.process();
          log4j.debug("Transaction processed: " + counter + "/" + total + " batch: " + batch);
        } catch (OBException e) {
          String resultMsg = OBMessageUtils.parseTranslation(e.getMessage());
          log4j.error(e);
          logger.logln(resultMsg);
          result.setType("Error");
          result.setTitle(OBMessageUtils.messageBD("Error"));
          result.setMessage(resultMsg);
          bundle.setResult(result);
          return;
        } catch (Exception e) {
          result = OBMessageUtils.translateError(bundle.getConnection(), bundle.getContext()
              .toVars(), OBContext.getOBContext().getLanguage().getLanguage(), e.getMessage());
          log4j.error(result.getMessage(), e);
          logger.logln(result.getMessage());
          bundle.setResult(result);
          return;
        }

        // If cost has been calculated successfully do a commit.
        SessionHandler.getInstance().commitAndStart();
      }
      OBDal.getInstance().getSession().clear();
      trxs = getTransactionsBatch();
      pendingTrx = areTransactionsPending();
      total += trxs.size();
    }
    logger.logln(OBMessageUtils.messageBD("Success"));
    bundle.setResult(result);
  }

  private List<MaterialTransaction> getTransactionsBatch() {
    StringBuffer where = new StringBuffer();
    where.append(" as trx");
    where.append(" join trx." + MaterialTransaction.PROPERTY_PRODUCT + " as p");
    where.append(" where trx." + MaterialTransaction.PROPERTY_ISCOSTCALCULATED + " = false");
    where.append("   and trx." + MaterialTransaction.PROPERTY_COSTINGSTATUS + " <> 'S'");
    where.append("   and p." + Product.PROPERTY_PRODUCTTYPE + " = 'I'");
    where.append("   and p." + Product.PROPERTY_STOCKED + " = true");
    where.append("   and trx." + MaterialTransaction.PROPERTY_TRANSACTIONPROCESSDATE + " <= :now");
    where.append(" order by trx." + MaterialTransaction.PROPERTY_TRANSACTIONPROCESSDATE);
    OBQuery<MaterialTransaction> trxQry = OBDal.getInstance().createQuery(
        MaterialTransaction.class, where.toString());
    trxQry.setMaxResult(1000);
    trxQry.setNamedParameter("now", new Date());

    return trxQry.list();
  }

  private boolean areTransactionsPending() {
    StringBuffer where = new StringBuffer();
    where.append(" as trx");
    where.append(" join trx." + MaterialTransaction.PROPERTY_PRODUCT + " as p");
    where.append(" where trx." + MaterialTransaction.PROPERTY_ISCOSTCALCULATED + " = false");
    where.append("   and trx." + MaterialTransaction.PROPERTY_COSTINGSTATUS + " = 'NC'");
    where.append("   and p." + Product.PROPERTY_PRODUCTTYPE + " = 'I'");
    where.append("   and p." + Product.PROPERTY_STOCKED + " = true");
    where.append("   and trx." + MaterialTransaction.PROPERTY_TRANSACTIONPROCESSDATE + " <= :now");
    where.append(" order by trx." + MaterialTransaction.PROPERTY_TRANSACTIONPROCESSDATE);
    OBQuery<MaterialTransaction> trxQry = OBDal.getInstance().createQuery(
        MaterialTransaction.class, where.toString());
    trxQry.setMaxResult(1);
    trxQry.setNamedParameter("now", new Date());

    return trxQry.list().size() > 0;
  }
}
