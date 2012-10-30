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
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.TreeUtility;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.financialmgmt.accounting.Budget;
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
      // OBQuery<BudgetLine> budgetLines = OBDal.getInstance().createQuery(BudgetLine.class,
      // "budget='" + cBudgetId + "'");
      Budget myBudget = OBDal.getInstance().get(Budget.class, cBudgetId);
      for (BudgetLine budgetLine : myBudget.getFinancialMgmtBudgetLineList()) {
        activity = (budgetLine.getActivity() != null) ? budgetLine.getActivity().getId() : "";
        accountingSchema = (budgetLine.getAccountingSchema() != null) ? budgetLine
            .getAccountingSchema().getId() : "";
        asset = (budgetLine.getAsset() != null) ? budgetLine.getAsset().getId() : "";
        businessPartner = (budgetLine.getBusinessPartner() != null) ? budgetLine
            .getBusinessPartner().getId() : "";
        businessPartnerCategory = (budgetLine.getBusinessPartnerCategory() != null) ? budgetLine
            .getBusinessPartnerCategory().getId() : "";
        costcenter = (budgetLine.getCostcenter() != null) ? budgetLine.getCostcenter().getId() : "";
        account = (budgetLine.getAccountElement() != null) ? budgetLine.getAccountElement().getId()
            : "";
        accountSign = (budgetLine.getAccountElement() != null) ? budgetLine.getAccountElement()
            .getAccountSign() : "";
        period = (budgetLine.getPeriod() != null) ? budgetLine.getPeriod().getId() : "";
        product = (budgetLine.getProduct() != null) ? budgetLine.getProduct().getId() : "";
        productCategory = (budgetLine.getProductCategory() != null) ? budgetLine
            .getProductCategory().getId() : "";
        project = (budgetLine.getProject() != null) ? budgetLine.getProject().getId() : "";
        salesCampaign = (budgetLine.getSalesCampaign() != null) ? budgetLine.getSalesCampaign()
            .getId() : "";
        salesRegion = (budgetLine.getSalesRegion() != null) ? budgetLine.getSalesRegion().getId()
            : "";
        user1 = (budgetLine.getStDimension() != null) ? budgetLine.getStDimension().getId() : "";
        user2 = (budgetLine.getNdDimension() != null) ? budgetLine.getNdDimension().getId() : "";

        // get the natural tree
        TreeUtility treeUtility = new TreeUtility();
        String activityNaturalTree = activity != null ? Utility.arrayListToString(
            (new ArrayList<String>(treeUtility.getNaturalTree(activity, "AY"))), true) : activity;
        String productCategoryNaturalTree = productCategory != null ? Utility.arrayListToString(
            (new ArrayList<String>(treeUtility.getNaturalTree(productCategory, "PC"))), true)
            : productCategory;
        String assetNaturalTree = asset != null ? Utility.arrayListToString((new ArrayList<String>(
            treeUtility.getNaturalTree(asset, "AS"))), true) : asset;
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
        String user1NaturalTree = user1 != null ? Utility.arrayListToString((new ArrayList<String>(
            treeUtility.getNaturalTree(user1, "U1"))), true) : user1;
        String user2NaturalTree = user2 != null ? Utility.arrayListToString((new ArrayList<String>(
            treeUtility.getNaturalTree(user2, "U2"))), true) : user2;

        StringBuilder queryString = new StringBuilder();
        queryString.append("select SUM(e.credit) as credit,");
        queryString.append(" SUM(e.debit) as debit");
        queryString.append(" from FinancialMgmtAccountingFact e where");
        queryString.append(" e.client.id='").append(myBudget.getClient().getId()).append("'");

        if (!"".equals(activity)) {
          queryString.append(" and e.activity.id in (" + activityNaturalTree + ")");
        }
        queryString.append(" and e.accountingSchema.id=:accountingSchema");
        if (!"".equals(asset)) {
          queryString.append(" and e.asset.id in (" + assetNaturalTree + ")");
        }
        if (!"".equals(businessPartner)) {
          queryString.append(" and e.businessPartner.id = :businessPartner");
        } else if (!"".equals(businessPartnerCategory)) {
          queryString
              .append(" and exists (select 1 from BusinessPartner bp where e.businessPartner.id = bp.id and businessPartnerCategory.id=:businessPartnerCategory)");
        }
        if (!"".equals(costcenter)) {
          queryString.append(" and e.costcenter.id in (" + costcenterNaturalTree + ")");
        }
        queryString.append(" and e.account.id in (" + accountNaturalTree + ")");
        if (!"".equals(period)) {
          queryString.append(" and e.period.id=:period");
        }
        if (!"".equals(product)) {
          queryString.append(" and e.product.id=:product");
        } else if (!"".equals(productCategory)) {
          queryString
              .append(" and exists (select 1 from Product p where e.product.id = p.id and productCategory.id in ("
                  + productCategoryNaturalTree + ")");
        }
        if (!"".equals(project)) {
          queryString.append(" and e.project.id in (" + projectNaturalTree + ")");
        }
        if (!"".equals(salesCampaign)) {
          queryString.append(" and e.salesCampaign.id in (" + campaignNaturalTree + ")");
        }
        if (!"".equals(salesRegion)) {
          queryString.append(" and e.salesRegion.id in (" + regionNaturalTree + ")");
        }
        if (!"".equals(user1)) {
          queryString.append(" and e.stDimension.id in (" + user1NaturalTree + ")");
        }
        if (!"".equals(user1)) {
          queryString.append(" and e.ndDimension.id in (" + user2NaturalTree + ")");
        }
        Query query = OBDal.getInstance().getSession().createQuery(queryString.toString());
        query.setReadOnly(true);
        query.setString("accountingSchema", accountingSchema);
        if (!"".equals(businessPartner))
          query.setString("businessPartner", businessPartner);
        else if (!"".equals(businessPartnerCategory))
          query.setString("businessPartnerCategory", businessPartnerCategory);
        if (!"".equals(period))
          query.setString("period", period);
        if (!"".equals(product))
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

        if (("D").equals(accountSign)) {
          budgetLine.setActualAmount(debit.subtract(credit));
        } else {
          budgetLine.setActualAmount(credit.subtract(debit));
        }

        msg.setType("Success");
        msg.setTitle("Success");
        msg.setMessage("Actual Amount = " + budgetLine.getActualAmount());
        bundle.setResult(msg);
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
