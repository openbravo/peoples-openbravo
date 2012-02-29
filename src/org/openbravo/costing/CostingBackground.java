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

import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.exception.OBException;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.core.SessionHandler;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.model.materialmgmt.transaction.MaterialTransaction;
import org.openbravo.scheduling.ProcessBundle;
import org.openbravo.scheduling.ProcessLogger;
import org.openbravo.service.db.DalBaseProcess;

/**
 * @author gorkaion
 * 
 */
public class CostingBackground extends DalBaseProcess {
  static Logger log4j = Logger.getLogger(CostingBackground.class);
  private ProcessLogger logger;

  // private ConnectionProvider connection;

  /*
   * (non-Javadoc)
   * 
   * @see org.openbravo.service.db.DalBaseProcess#doExecute(org.openbravo.scheduling.ProcessBundle)
   */
  @Override
  protected void doExecute(ProcessBundle bundle) throws Exception {
    logger = bundle.getLogger();
    OBError result = new OBError();
    result.setType("Success");
    result.setTitle(OBMessageUtils.messageBD("Success"));

    OBCriteria<MaterialTransaction> obcTrx = OBDal.getInstance().createCriteria(
        MaterialTransaction.class);
    obcTrx.add(Restrictions.isNull(MaterialTransaction.PROPERTY_TRANSACTIONCOST));
    obcTrx.addOrderBy(MaterialTransaction.PROPERTY_TRANSACTIONPROCESSDATE, true);
    List<MaterialTransaction> trxs = obcTrx.list();
    int counter = 0, total = trxs.size();

    for (MaterialTransaction transaction : trxs) {
      counter++;
      try {
        log4j.debug("Start transaction process: " + transaction.getId());
        CostingServer transactionCost = new CostingServer(transaction);
        transactionCost.process();
        log4j.debug("Transaction processed: " + counter + "/" + total);
      } catch (OBException e) {
        log4j.error(e.getMessage(), e);
        logger.logln(e.getMessage());
        result.setType("Error");
        result.setTitle(OBMessageUtils.messageBD("Error"));
        result.setMessage(OBMessageUtils.parseTranslation(e.getMessage()));
        bundle.setResult(result);
        return;
      } catch (Exception e) {
        result = OBMessageUtils.translateError(bundle.getConnection(),
            bundle.getContext().toVars(), OBContext.getOBContext().getLanguage().getLanguage(),
            e.getMessage());
        log4j.error(result.getMessage(), e);
        logger.logln(result.getMessage());
        bundle.setResult(result);
        return;
      }
      // If cost has been calculated successfully do a commit.
      SessionHandler.getInstance().commitAndStart();
    }
  }
}
