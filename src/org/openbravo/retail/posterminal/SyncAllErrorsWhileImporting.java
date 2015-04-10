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
 * All portions are Copyright (C) 2015 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 *************************************************************************
 */
package org.openbravo.retail.posterminal;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.weld.WeldUtils;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.mobile.core.process.DataSynchronizationProcess;
import org.openbravo.scheduling.ProcessBundle;
import org.openbravo.scheduling.ProcessLogger;
import org.openbravo.service.db.DalBaseProcess;
import org.openbravo.service.json.JsonConstants;

/**
 * @author malsasua
 * 
 */
public class SyncAllErrorsWhileImporting extends DalBaseProcess {
  private static final Logger log4j = Logger.getLogger(SyncAllErrorsWhileImporting.class);
  private ProcessLogger logger;

  @Inject
  @Any
  private Instance<POSDataSynchronizationProcess> syncProcesses;

  @Override
  protected void doExecute(ProcessBundle bundle) throws Exception {
    logger = bundle.getLogger();
    try {
      OBCriteria<OBPOSErrors> queryListErrors = OBDal.getInstance().createCriteria(
          OBPOSErrors.class);
      queryListErrors.add(Restrictions.eq(OBPOSErrors.PROPERTY_ORDERSTATUS, "N"));
      queryListErrors.add(Restrictions.le(OBPOSErrors.PROPERTY_ATTEMPTS, new Long("3")));
      queryListErrors.addOrderBy(OBPOSErrors.PROPERTY_CREATIONDATE, true);

      List<OBPOSErrors> listErrors = queryListErrors.list();
      Collections.sort(listErrors, new ErrorComparator());

      for (OBPOSErrors error : listErrors) {
        String errorId = error.getId();
        String type = error.getTypeofdata();

        // RequestContext.get().setSessionAttribute("#AD_ORG_ID", "1000000");
        POSDataSynchronizationProcess syncProcess = null;
        BeanManager beanManager = WeldUtils.getStaticInstanceBeanManager();
        Set<Bean<?>> beans = beanManager.getBeans(POSDataSynchronizationProcess.class,
            new DataSynchronizationProcess.Selector(type));

        for (Bean<?> bean : beans) {
          syncProcess = (POSDataSynchronizationProcess) beanManager.getReference(bean,
              POSDataSynchronizationProcess.class, beanManager.createCreationalContext(bean));
        }
        JSONObject record = new JSONObject(error.getJsoninfo());
        record.put("posErrorId", errorId);
        JSONObject data = new JSONObject();
        data.put("data", record);
        JSONObject result = syncProcess.exec(data, true);
        try {
          OBContext.setAdminMode(true);
          error = OBDal.getInstance().get(OBPOSErrors.class, errorId);
          if (result.get(JsonConstants.RESPONSE_STATUS).equals(
              JsonConstants.RPCREQUEST_STATUS_FAILURE)) {
            logger.logln("Record has not been synced: " + error.getIdentifier());
          } else {
            error.setOrderstatus("Y");
            logger.logln("Record has been synced successfully: " + error.getIdentifier());
          }
          error.setAttempts(error.getAttempts() + 1);
          OBDal.getInstance().save(error);
          OBDal.getInstance().flush();
          OBDal.getInstance().commitAndClose();
        } finally {
          OBContext.restorePreviousMode();
        }
      }
      logger.logln(OBMessageUtils.messageBD("Success"));
    } catch (Exception e) {// won't' happen
      logger.logln(OBMessageUtils.messageBD("Error"));
    }
  }
}
