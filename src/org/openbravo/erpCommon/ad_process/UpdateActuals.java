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
 * All portions are Copyright (C) 2012-2019 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.erpCommon.ad_process;

import static org.openbravo.erpCommon.utility.StringCollectionUtils.commaSeparated;

import java.math.BigDecimal;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.query.Query;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.TreeUtility;
import org.openbravo.model.financialmgmt.accounting.Budget;
import org.openbravo.model.financialmgmt.accounting.BudgetLine;
import org.openbravo.scheduling.ProcessBundle;
import org.openbravo.service.db.DalBaseProcess;

public class UpdateActuals extends DalBaseProcess {
  private static final Logger log4j = LogManager.getLogger();

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
      Budget myBudget = OBDal.getInstance().get(Budget.class, cBudgetId);
      for (BudgetLine budgetLine : myBudget.getFinancialMgmtBudgetLineList()) {
        activity = (budgetLine.getActivity() != null) ? budgetLine.getActivity().getId() : "";
        accountingSchema = (budgetLine.getAccountingSchema() != null)
            ? budgetLine.getAccountingSchema().getId()
            : "";
        asset = (budgetLine.getAsset() != null) ? budgetLine.getAsset().getId() : "";
        businessPartner = (budgetLine.getBusinessPartner() != null)
            ? budgetLine.getBusinessPartner().getId()
            : "";
        businessPartnerCategory = (budgetLine.getBusinessPartnerCategory() != null)
            ? budgetLine.getBusinessPartnerCategory().getId()
            : "";
        costcenter = (budgetLine.getCostcenter() != null) ? budgetLine.getCostcenter().getId() : "";
        account = (budgetLine.getAccountElement() != null) ? budgetLine.getAccountElement().getId()
            : "";
        accountSign = (budgetLine.getAccountElement() != null)
            ? budgetLine.getAccountElement().getAccountSign()
            : "";
        period = (budgetLine.getPeriod() != null) ? budgetLine.getPeriod().getId() : "";
        product = (budgetLine.getProduct() != null) ? budgetLine.getProduct().getId() : "";
        productCategory = (budgetLine.getProductCategory() != null)
            ? budgetLine.getProductCategory().getId()
            : "";
        project = (budgetLine.getProject() != null) ? budgetLine.getProject().getId() : "";
        salesCampaign = (budgetLine.getSalesCampaign() != null)
            ? budgetLine.getSalesCampaign().getId()
            : "";
        salesRegion = (budgetLine.getSalesRegion() != null) ? budgetLine.getSalesRegion().getId()
            : "";
        user1 = (budgetLine.getStDimension() != null) ? budgetLine.getStDimension().getId() : "";
        user2 = (budgetLine.getNdDimension() != null) ? budgetLine.getNdDimension().getId() : "";

        // get the natural tree
        TreeUtility treeUtility = new TreeUtility();
        String activityTree = activity != null
            ? commaSeparated(treeUtility.getChildTree(activity, "AY"))
            : activity;
        String productCategoryTree = productCategory != null
            ? commaSeparated(treeUtility.getChildTree(productCategory, "PC"))
            : productCategory;
        String assetTree = asset != null ? commaSeparated(treeUtility.getChildTree(asset, "AS"))
            : asset;
        String costcenterTree = costcenter != null
            ? commaSeparated(treeUtility.getChildTree(costcenter, "CC"))
            : costcenter;
        String accountTree = account != null
            ? commaSeparated(treeUtility.getChildTree(account, "EV"))
            : account;
        String projectTree = project != null
            ? commaSeparated(treeUtility.getChildTree(project, "PJ"))
            : project;
        String campaignTree = salesCampaign != null
            ? commaSeparated(treeUtility.getChildTree(salesCampaign, "MC"))
            : salesCampaign;
        String regionTree = salesRegion != null
            ? commaSeparated(treeUtility.getChildTree(salesRegion, "SR"))
            : salesRegion;
        String user1Tree = user1 != null ? commaSeparated(treeUtility.getChildTree(user1, "U1"))
            : user1;
        String user2Tree = user2 != null ? commaSeparated(treeUtility.getChildTree(user2, "U2"))
            : user2;

        final String orgId = myBudget.getOrganization().getId();

        String OrgTreeList = commaSeparated(
            OBContext.getOBContext().getOrganizationStructureProvider().getChildTree(orgId, true));

        //@formatter:off
        String queryString = "select SUM(e.credit) as credit,"
                       + "      SUM(e.debit) as debit"
                       + "    from FinancialMgmtAccountingFact e "
                       + "    where e.client.id="
                       + "'" + myBudget.getClient().getId() + "'"
                       + "      and e.organization.id in "
                       + "(" + OrgTreeList + ")"
                       + "      and e.period.year.id="
                       + "'" + myBudget.getYear().getId() + "'";

        if (!"".equals(activity)) {
          queryString += "      and e.activity.id in "
                      + "(" + activityTree + ")";
        }
        queryString   += "      and e.accountingSchema.id=:accountingSchema";
        if (!"".equals(asset)) {
          queryString += "      and e.asset.id in (" + assetTree + ")";
        }
        if (!"".equals(businessPartner)) {
          queryString += "      and e.businessPartner.id = :businessPartner";
        }
        if (StringUtils.isNotEmpty(businessPartnerCategory)) {
          queryString += "      and e.businessPartner.businessPartnerCategory.id=:businessPartnerCategory";
        }
        if (!"".equals(costcenter)) {
          queryString += "      and e.costcenter.id in "
                      +  "(" + costcenterTree + ")";
        }
        queryString   += "      and e.account.id in "
                      +  "(" + accountTree + ")";
        if (!"".equals(period)) {
          queryString += "      and e.period.id=:period";
        }
        if (!"".equals(product)) {
          queryString += "      and e.product.id=:product";
        }
        if (StringUtils.isNotEmpty(productCategory)) {
          queryString += "      and e.product.productCategory.id in (" + productCategoryTree +")";
        }
        if (!"".equals(project)) {
          queryString += "      and e.project.id in (" + projectTree + ")";
        }
        if (!"".equals(salesCampaign)) {
          queryString += "      and e.salesCampaign.id in (" + campaignTree + ")";
        }
        if (!"".equals(salesRegion)) {
          queryString += "      and e.salesRegion.id in (" + regionTree + ")";
        }
        if (!"".equals(user1)) {
          queryString += "      and e.stDimension.id in (" + user1Tree + ")";
        }
        if (!"".equals(user1)) {
          queryString += "      and e.ndDimension.id in (" + user2Tree + ")";
        }
        Query<Object[]> query = OBDal.getInstance()
            .getSession()
            .createQuery(queryString, Object[].class);
        query.setReadOnly(true);
        query.setParameter("accountingSchema", accountingSchema);
        if (!"".equals(businessPartner)) {
          query.setParameter("businessPartner", businessPartner);
        }
        if (StringUtils.isNotEmpty(businessPartnerCategory)) {
          query.setParameter("businessPartnerCategory", businessPartnerCategory);
        }
        if (!"".equals(period)) {
          query.setParameter("period", period);
        }
        if (!"".equals(product)) {
          query.setParameter("product", product);
        }

        log4j.debug("Query String" + query.getQueryString());

        BigDecimal credit = BigDecimal.ZERO;
        BigDecimal debit = BigDecimal.ZERO;
        for (Object[] row : query.list()) {
          if (row != null) {
            credit = (row[0] != null) ? (BigDecimal) row[0] : new BigDecimal(0);
            debit = (row[1] != null) ? (BigDecimal) row[1] : new BigDecimal(0);
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
