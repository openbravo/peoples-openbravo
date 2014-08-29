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
 * All portions are Copyright (C) 2012-2014 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 *************************************************************************
 */
package org.openbravo.costing;

import java.util.Date;
import java.util.Set;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.Query;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.openbravo.base.exception.OBException;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.security.OrganizationStructureProvider;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBDateUtils;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.materialmgmt.cost.CostAdjustment;
import org.openbravo.model.materialmgmt.cost.CostAdjustmentLine;
import org.openbravo.model.materialmgmt.cost.CostingRule;
import org.openbravo.model.materialmgmt.transaction.MaterialTransaction;
import org.openbravo.scheduling.Process;
import org.openbravo.scheduling.ProcessBundle;
import org.openbravo.scheduling.ProcessLogger;
import org.openbravo.service.db.DbUtility;

public class FixBackdatedTransactionsProcess implements Process {
  private ProcessLogger logger;
  private static final Logger log4j = Logger.getLogger(FixBackdatedTransactionsProcess.class);
  private static CostAdjustment costAdjHeader = null;

  @Override
  public void execute(ProcessBundle bundle) throws Exception {
    costAdjHeader = null;
    logger = bundle.getLogger();
    OBError msg = new OBError();
    final String ruleId = (String) bundle.getParams().get("M_Costing_Rule_ID");
    CostingRule rule = OBDal.getInstance().get(CostingRule.class, ruleId);

    try {
      OBContext.setAdminMode(false);
      OrganizationStructureProvider osp = OBContext.getOBContext()
          .getOrganizationStructureProvider(rule.getClient().getId());
      final Set<String> childOrgs = osp.getChildTree(rule.getOrganization().getId(), true);

      ScrollableResults transactions = getTransactions(childOrgs, rule.getStartingDate(),
          rule.getEndingDate());
      int i = 0;
      try {
        while (transactions.next()) {
          MaterialTransaction trx = (MaterialTransaction) transactions.get()[0];
          if (CostAdjustmentUtils.isNeededCostAdjustmentByBackDateTrx(trx,
              rule.isWarehouseDimension())) {
            createCostAdjustmenHeader(rule.getOrganization());
            CostAdjustmentLine cal = CostAdjustmentUtils.insertCostAdjustmentLine(trx,
                costAdjHeader, null, Boolean.TRUE, OBDateUtils.getEndOfDay(trx.getMovementDate()),
                trx.getMovementDate());
            cal.setBackdatedTrx(Boolean.TRUE);
            OBDal.getInstance().save(cal);
            i++;
            OBDal.getInstance().flush();
            if ((i % 100) == 0) {
              OBDal.getInstance().getSession().clear();
              // Reload rule after clear session.
              rule = OBDal.getInstance().get(CostingRule.class, ruleId);
            }
          }
        }
      } finally {
        transactions.close();
      }

    } catch (final Exception e) {
      OBDal.getInstance().rollbackAndClose();
      String message = DbUtility.getUnderlyingSQLException(e).getMessage();
      logger.log(message);
      log4j.error(message, e);
      msg.setType("Error");
      msg.setTitle(OBMessageUtils.messageBD("Error"));
      msg.setMessage(message);
      bundle.setResult(msg);
      return;
    } finally {
      OBContext.restorePreviousMode();
    }

    if (costAdjHeader != null) {
      try {
        JSONObject message = CostAdjustmentProcess.doProcessCostAdjustment(costAdjHeader);

        if (message.get("severity") != "success") {
          throw new OBException(OBMessageUtils.parseTranslation("@ErrorProcessingCostAdj@") + ": "
              + costAdjHeader.getDocumentNo() + " - " + message.getString("text"));
        }

        msg.setType((String) message.get("severity"));
        msg.setTitle((String) message.get("title"));
        msg.setMessage((String) message.get("text"));
      } catch (JSONException e) {
        throw new OBException(OBMessageUtils.parseTranslation("@ErrorProcessingCostAdj@"));
      } catch (Exception e) {
        OBDal.getInstance().rollbackAndClose();
        String message = DbUtility.getUnderlyingSQLException(e).getMessage();
        logger.log(message);
        log4j.error(message, e);
        msg.setType("Error");
        msg.setTitle(OBMessageUtils.messageBD("Error"));
        msg.setMessage(message);
        bundle.setResult(msg);
        return;
      }
    } else {
      msg.setType("Success");
      msg.setTitle(OBMessageUtils.messageBD("Success"));
    }

    rule.setBackdatedTransactionsFixed(Boolean.TRUE);
    OBDal.getInstance().save(rule);

    bundle.setResult(msg);
  }

  private ScrollableResults getTransactions(Set<String> childOrgs, Date startDate, Date endDate) {
    StringBuffer select = new StringBuffer();
    select.append("select trx as trx");
    select.append(" from " + MaterialTransaction.ENTITY_NAME + " as trx");
    select.append(" where trx." + MaterialTransaction.PROPERTY_ORGANIZATION + ".id in (:orgs)");
    select.append(" and trx." + MaterialTransaction.PROPERTY_MOVEMENTDATE + " >= (:startDate)");
    if (endDate != null) {
      select.append(" and trx." + MaterialTransaction.PROPERTY_MOVEMENTDATE + " <= (:endDate)");
    }
    select.append(" order by trx." + MaterialTransaction.PROPERTY_MOVEMENTDATE);

    Query stockLinesQry = OBDal.getInstance().getSession().createQuery(select.toString());
    stockLinesQry.setParameterList("orgs", childOrgs);
    stockLinesQry.setTimestamp("startDate", startDate);
    if (endDate != null) {
      stockLinesQry.setTimestamp("endDate", endDate);
    }

    stockLinesQry.setFetchSize(1000);
    ScrollableResults stockLines = stockLinesQry.scroll(ScrollMode.FORWARD_ONLY);
    return stockLines;
  }

  private static void createCostAdjustmenHeader(Organization org) {
    if (costAdjHeader == null) {
      costAdjHeader = CostAdjustmentUtils.insertCostAdjustmentHeader(org, "BDT");
    }
  }
}
