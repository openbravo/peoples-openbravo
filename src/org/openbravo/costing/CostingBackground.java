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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.session.OBPropertiesProvider;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.common.plm.Product;
import org.openbravo.model.materialmgmt.cost.CostingRule;
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
  public static final String AD_PROCESS_ID = "3F2B4AAC707B4CE7B98D2005CF7310B5";
  private ProcessLogger logger;
  private int maxTransactions = 0;

  @Override
  protected void doExecute(ProcessBundle bundle) throws Exception {

    logger = bundle.getLogger();
    OBError result = new OBError();
    List<String> orgsWithRule = new ArrayList<String>();
    try {
      OBContext.setAdminMode(false);
      result.setType("Success");
      result.setTitle(OBMessageUtils.messageBD("Success"));

      // Get organizations with costing rules.
      StringBuffer where = new StringBuffer();
      where.append(" as o");
      where.append(" where exists (");
      where.append("    select 1 from " + CostingRule.ENTITY_NAME + " as cr");
      where.append("    where ad_isorgincluded(o.id, cr." + CostingRule.PROPERTY_ORGANIZATION
          + ".id, " + CostingRule.PROPERTY_CLIENT + ".id) <> -1 ");
      where.append("      and cr." + CostingRule.PROPERTY_VALIDATED + " is true");
      where.append(" )");
      where.append("    and ad_isorgincluded(o.id, '" + bundle.getContext().getOrganization()
          + "', '" + bundle.getContext().getClient() + "') <> -1 ");
      OBQuery<Organization> orgQry = OBDal.getInstance().createQuery(Organization.class,
          where.toString());
      List<Organization> orgs = orgQry.list();
      if (orgs.size() == 0) {
        log4j.debug("No organizations with Costing Rule defined");
        logger.logln(OBMessageUtils.messageBD("Success"));
        bundle.setResult(result);
        return;
      }
      for (Organization org : orgs) {
        orgsWithRule.add(org.getId());
      }

      // Fix the Not Processed flag for those Transactions with Cost Not Calculated
      setNotProcessedWhenNotCalculatedTransactions(orgsWithRule);

      ScrollableResults trxs = getTransactions(orgsWithRule);
      int counter = 0;
      try {
        while (trxs.next()) {
          MaterialTransaction transaction = (MaterialTransaction) trxs.get()[0];
          counter++;
          if ("S".equals(transaction.getCostingStatus())) {
            // Do not calculate trx in skip status.
            continue;
          }
          log4j.debug("Start transaction process: " + transaction.getId());
          OBDal.getInstance().refresh(transaction);
          CostingServer transactionCost = new CostingServer(transaction);
          transactionCost.process();
          log4j.debug("Transaction processed: " + counter + "/" + maxTransactions);
          // If cost has been calculated successfully do a commit.
          OBDal.getInstance().getConnection(true).commit();
          if (counter % 1 == 0) {
            OBDal.getInstance().getSession().clear();
          }
        }
      } finally {
        try {
          trxs.close();
        } catch (Exception ignore) {
        }
      }

      logger.logln(OBMessageUtils.messageBD("Success"));
      bundle.setResult(result);
    } catch (OBException e) {
      OBDal.getInstance().rollbackAndClose();
      String message = OBMessageUtils.parseTranslation(bundle.getConnection(), bundle.getContext()
          .toVars(), OBContext.getOBContext().getLanguage().getLanguage(), e.getMessage());
      result.setMessage(message);
      result.setType("Error");
      log4j.error(message, e);
      logger.logln(message);
      bundle.setResult(result);
      return;
    } catch (Exception e) {
      result = OBMessageUtils.translateError(bundle.getConnection(), bundle.getContext().toVars(),
          OBContext.getOBContext().getLanguage().getLanguage(), e.getMessage());
      result.setType("Error");
      result.setTitle(OBMessageUtils.messageBD("Error"));
      log4j.error(result.getMessage(), e);
      logger.logln(result.getMessage());
      bundle.setResult(result);
      return;
    } finally {
      // Set the processed flag to true to those transactions whose cost has been calculated.
      if (!orgsWithRule.isEmpty()) {
        setCalculatedTransactionsAsProcessed(orgsWithRule);
      }
      OBContext.restorePreviousMode();
    }
  }

  private void setNotProcessedWhenNotCalculatedTransactions(List<String> orgsWithRule) {
    ScrollableResults trxs = getTransactionsNotCalculated(orgsWithRule);
    while (trxs.next()) {
      MaterialTransaction transaction = (MaterialTransaction) trxs.get(0);
      transaction.setProcessed(Boolean.FALSE);
      OBDal.getInstance().save(transaction);
    }
    OBDal.getInstance().flush();
  }

  private void setCalculatedTransactionsAsProcessed(List<String> orgsWithRule) {
    ScrollableResults trxs = getTransactionsCalculated(orgsWithRule);
    while (trxs.next()) {
      MaterialTransaction transaction = (MaterialTransaction) trxs.get(0);
      transaction.setProcessed(Boolean.TRUE);
      OBDal.getInstance().save(transaction);
    }
    OBDal.getInstance().flush();
  }

  private ScrollableResults getTransactions(List<String> orgsWithRule) {
    StringBuffer where = new StringBuffer();
    where.append(" as trx");
    where.append(" join trx." + MaterialTransaction.PROPERTY_PRODUCT + " as p");
    where.append(" where trx." + MaterialTransaction.PROPERTY_ISPROCESSED + " = false");
    where.append("   and p." + Product.PROPERTY_PRODUCTTYPE + " = 'I'");
    where.append("   and p." + Product.PROPERTY_STOCKED + " = true");
    where.append("   and trx." + MaterialTransaction.PROPERTY_TRANSACTIONPROCESSDATE + " <= :now");
    where.append("   and trx." + MaterialTransaction.PROPERTY_ORGANIZATION + ".id in (:orgs)");
    where.append(" order by trx." + MaterialTransaction.PROPERTY_TRANSACTIONPROCESSDATE);
    where.append("   , trx." + MaterialTransaction.PROPERTY_MOVEMENTLINE);
    // This makes M- to go before M+. In Oracle it must go with desc as if not, M+ would go before
    // M-.
    if (OBPropertiesProvider.getInstance().getOpenbravoProperties().getProperty("bbdd.rdbms")
        .equalsIgnoreCase("oracle")) {
      where.append("   , trx." + MaterialTransaction.PROPERTY_MOVEMENTTYPE + " desc ");
    } else {
      where.append("   , trx." + MaterialTransaction.PROPERTY_MOVEMENTTYPE);
      where.append(" , trx." + MaterialTransaction.PROPERTY_ID);
    }
    OBQuery<MaterialTransaction> trxQry = OBDal.getInstance().createQuery(
        MaterialTransaction.class, where.toString());

    trxQry.setNamedParameter("now", new Date());
    trxQry.setFilterOnReadableOrganization(false);
    trxQry.setNamedParameter("orgs", orgsWithRule);

    if (maxTransactions == 0) {
      maxTransactions = trxQry.count();
    }
    try {
      OBDal.getInstance().getConnection().setHoldability(ResultSet.HOLD_CURSORS_OVER_COMMIT);
    } catch (SQLException e) {
      log4j.error("error: " + e.getMessage(), e);
      throw new OBException(e.getMessage());
    }

    return trxQry.scroll(ScrollMode.FORWARD_ONLY);
  }

  /**
   * Get Transactions with Processed flag = 'Y' but it's cost is Not Calculated
   */
  private ScrollableResults getTransactionsNotCalculated(List<String> orgsWithRule) {
    StringBuffer where = new StringBuffer();
    where.append(" as trx");
    where.append(" where trx." + MaterialTransaction.PROPERTY_ISPROCESSED + " = true");
    where.append("   and trx." + MaterialTransaction.PROPERTY_ISCOSTCALCULATED + " = false");
    where.append("   and trx." + MaterialTransaction.PROPERTY_ORGANIZATION + ".id in (:orgs)");
    OBQuery<MaterialTransaction> trxQry = OBDal.getInstance().createQuery(
        MaterialTransaction.class, where.toString());
    trxQry.setFilterOnReadableOrganization(false);
    trxQry.setNamedParameter("orgs", orgsWithRule);

    return trxQry.scroll(ScrollMode.FORWARD_ONLY);
  }

  /**
   * Get Transactions with Processed flag = 'N' but it's cost is Calculated
   */
  private ScrollableResults getTransactionsCalculated(List<String> orgsWithRule) {
    StringBuffer where = new StringBuffer();
    where.append(" as trx");
    where.append(" where trx." + MaterialTransaction.PROPERTY_ISPROCESSED + " = false");
    where.append("   and trx." + MaterialTransaction.PROPERTY_ISCOSTCALCULATED + " = true");
    where.append("   and trx." + MaterialTransaction.PROPERTY_ORGANIZATION + ".id in (:orgs)");
    OBQuery<MaterialTransaction> trxQry = OBDal.getInstance().createQuery(
        MaterialTransaction.class, where.toString());
    trxQry.setFilterOnReadableOrganization(false);
    trxQry.setNamedParameter("orgs", orgsWithRule);

    return trxQry.scroll(ScrollMode.FORWARD_ONLY);
  }
}
