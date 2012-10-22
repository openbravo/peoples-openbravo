/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.1  (the  "License"),  being   the  Mozilla   Public  License
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
 ************************************************************************
 */
package org.openbravo.erpCommon.ad_process;

import java.math.BigDecimal;
import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.TreeUtility;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.financialmgmt.accounting.BudgetLine;
import org.openbravo.scheduling.ProcessBundle;
import org.openbravo.service.db.DalBaseProcess;

public class UpdateActuals extends DalBaseProcess {
  private static Logger log4j = Logger.getLogger(UpdateActuals.class);

  @Override
  protected void doExecute(ProcessBundle bundle) throws Exception {
    final OBError msg = new OBError();

    try {
      // retrieve the parameters from the bundle
      final String cBudgetId = (String) bundle.getParams().get("C_Budget_ID");

      String activity = null;
      String accountingSchema = null;
      String asset = null;
      String businessPartner = null;
      String businessPartnerCategory = null;
      String costcenter = null;
      String account = null;
      String accountSign = null;
      String period = null;
      String product = null;
      String productCategory = null;
      String project = null;
      String salesCampaign = null;
      String salesRegion = null;
      String user1 = null;
      String user2 = null;

      // Gets the budget lines
      OBQuery<BudgetLine> budgetLines = OBDal.getInstance().createQuery(BudgetLine.class,
          "budget='" + cBudgetId + "'");
      if (budgetLines.count() > 0) {
        for (BudgetLine budgetLine : budgetLines.list()) {
          activity = (budgetLine.getActivity() != null) ? budgetLine.getActivity().getId() : null;
          accountingSchema = (budgetLine.getAccountingSchema() != null) ? budgetLine
              .getAccountingSchema().getId() : null;
          asset = (budgetLine.getAsset() != null) ? budgetLine.getAsset().getId() : null;
          businessPartner = (budgetLine.getBusinessPartner() != null) ? budgetLine
              .getBusinessPartner().getId() : null;
          businessPartnerCategory = (budgetLine.getBusinessPartnerCategory() != null) ? budgetLine
              .getBusinessPartnerCategory().getId() : null;
          costcenter = (budgetLine.getCostcenter() != null) ? budgetLine.getCostcenter().getId()
              : null;
          account = (budgetLine.getAccountElement() != null) ? budgetLine.getAccountElement()
              .getId() : null;
          accountSign = (budgetLine.getAccountElement() != null) ? budgetLine.getAccountElement()
              .getAccountSign() : null;
          period = (budgetLine.getPeriod() != null) ? budgetLine.getPeriod().getId() : null;
          product = (budgetLine.getProduct() != null) ? budgetLine.getProduct().getId() : null;
          productCategory = (budgetLine.getProductCategory() != null) ? budgetLine
              .getProductCategory().getId() : null;
          project = (budgetLine.getProject() != null) ? budgetLine.getProject().getId() : null;
          salesCampaign = (budgetLine.getSalesCampaign() != null) ? budgetLine.getSalesCampaign()
              .getId() : null;
          salesRegion = (budgetLine.getSalesRegion() != null) ? budgetLine.getSalesRegion().getId()
              : null;
          user1 = (budgetLine.getStDimension() != null) ? budgetLine.getStDimension().getId()
              : null;
          user2 = (budgetLine.getNdDimension() != null) ? budgetLine.getNdDimension().getId()
              : null;

          // get the natural tree
          TreeUtility treeUtility = new TreeUtility();
          String activityNaturalTree = activity != null ? Utility.arrayListToString(
              (new ArrayList<String>(treeUtility.getNaturalTree(activity, "AY"))), true) : activity;
          String productCategoryNaturalTree = productCategory != null ? Utility.arrayListToString(
              (new ArrayList<String>(treeUtility.getNaturalTree(productCategory, "PC"))), true)
              : productCategory;
          String assetNaturalTree = asset != null ? Utility.arrayListToString(
              (new ArrayList<String>(treeUtility.getNaturalTree(asset, "AS"))), true) : asset;
          String costcenterNaturalTree = costcenter != null ? Utility.arrayListToString(
              (new ArrayList<String>(treeUtility.getNaturalTree(costcenter, "CC"))), true)
              : costcenter;
          String accountNaturalTree = account != null ? Utility.arrayListToString(
              (new ArrayList<String>(treeUtility.getNaturalTree(account, "EV"))), true) : account;
          String projectNaturalTree = project != null ? Utility.arrayListToString(
              new ArrayList<String>(treeUtility.getNaturalTree(project, "PJ")), true) : project;
          String campaignNaturalTree = salesCampaign != null ? Utility.arrayListToString(
              new ArrayList<String>(treeUtility.getNaturalTree(salesCampaign, "MC")), true)
              : salesCampaign;
          String regionNaturalTree = salesRegion != null ? Utility.arrayListToString(
              new ArrayList<String>(treeUtility.getNaturalTree(salesRegion, "SR")), true)
              : salesRegion;
          String user1NaturalTree = user1 != null ? Utility.arrayListToString(
              (new ArrayList<String>(treeUtility.getNaturalTree(user1, "U1"))), true) : user1;
          String user2NaturalTree = user2 != null ? Utility.arrayListToString(
              (new ArrayList<String>(treeUtility.getNaturalTree(user2, "U2"))), true) : user2;

          StringBuilder queryString = new StringBuilder();
          queryString.append("select SUM(e.credit) as credit,");
          queryString.append(" SUM(e.debit) as debit");
          queryString.append(" from FinancialMgmtAccountingFact e where");
          queryString.append(" e.activity.id"
              + ((activity != null) ? " in (" + activityNaturalTree + ")" : " is NULL"));
          queryString.append(" and e.accountingSchema.id=:accountingSchema");
          queryString.append(" and e.asset.id"
              + ((asset != null) ? " in (" + assetNaturalTree + ")" : " is NULL"));
          queryString
              .append(" and e.businessPartner.id"
                  + ((businessPartner != null) ? " = :businessPartner"
                      : ((businessPartnerCategory != null) ? " in (select id from BusinessPartner where businessPartnerCategory.id=:businessPartnerCategory)"
                          : " is NULL")));
          queryString.append((costcenter != null) ? " and e.costcenter.id in ("
              + costcenterNaturalTree + ")" : " and e.costcenter is NULL");
          queryString.append(" and e.account.id in (" + accountNaturalTree + ")");
          queryString.append(" and e.period.id" + ((period != null) ? "=:period" : " is NULL"));
          queryString
              .append(" and e.product.id"
                  + ((product != null) ? "=:product"
                      : (productCategory != null) ? " in (select id from Product where productCategory.id in ("
                          + productCategoryNaturalTree + "))"
                          : " is NULL"));
          queryString.append(" and e.project.id"
              + ((project != null) ? " in (" + projectNaturalTree + ")" : " is NULL"));
          queryString.append(" and e.salesCampaign.id"
              + ((salesCampaign != null) ? " in (" + campaignNaturalTree + ")" : " is NULL"));
          queryString.append(" and e.salesRegion.id"
              + ((salesRegion != null) ? " in (" + regionNaturalTree + ")" : " is NULL"));
          queryString.append(" and e.stDimension.id"
              + ((user1 != null) ? " in (" + user1NaturalTree + ")" : " is NULL"));
          queryString.append(" and e.ndDimension.id"
              + ((user2 != null) ? " in (" + user2NaturalTree + ")" : " is NULL"));

          Query query = OBDal.getInstance().getSession().createQuery(queryString.toString());
          query.setReadOnly(true);
          query.setString("accountingSchema", accountingSchema);
          if (businessPartner != null)
            query.setString("businessPartner", businessPartner);
          else if (businessPartnerCategory != null)
            query.setString("businessPartnerCategory", businessPartnerCategory);
          if (period != null)
            query.setString("period", period);
          if (product != null)
            query.setString("product", product);

          log4j.debug("Query String" + query.getQueryString());
          log4j.debug("Query size:" + query.list().size());

          BigDecimal credit = new BigDecimal(0);
          BigDecimal debit = new BigDecimal(0);
          for (Object obj : query.list()) {
            if (obj != null) {
              Object[] row = (Object[]) obj;
              credit = (BigDecimal) ((row[0] != null) ? row[0] : new BigDecimal(0));
              debit = (BigDecimal) ((row[1] != null) ? row[1] : new BigDecimal(0));
            }
          }

          if (("N").equals(accountSign)) {
            budgetLine.setActualAmount(debit.subtract(credit));
          } else {
            budgetLine.setActualAmount(credit.subtract(debit));
          }

          msg.setType("Success");
          msg.setTitle("Success");
          msg.setMessage("Actual Amount = " + budgetLine.getActualAmount());
          bundle.setResult(msg);
        }
      }
    } catch (Exception e) {
      msg.setType("Error");
      msg.setTitle("Error");
      msg.setMessage(e.toString());

      bundle.setResult(msg);
      OBDal.getInstance().rollbackAndClose();
    } finally {
      OBDal.getInstance().commitAndClose();
    }
  }
}
