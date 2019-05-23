/*
 ************************************************************************************
 * Copyright (C) 2016-2019 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.retail.posterminal.master;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.client.kernel.ComponentProvider.Qualifier;
import org.openbravo.dal.core.OBContext;
import org.openbravo.mobile.core.model.HQLPropertyList;
import org.openbravo.mobile.core.model.ModelExtension;
import org.openbravo.mobile.core.model.ModelExtensionUtils;
import org.openbravo.retail.posterminal.OBPOSApplications;
import org.openbravo.retail.posterminal.POSUtils;
import org.openbravo.retail.posterminal.ProcessHQLQuery;

public class CategoryTree extends ProcessHQLQuery {
  public static final String productCategoryTreePropertyExtension = "OBPOS_ProductCategoryTreeExtension";
  public static final String productCategoryTableId = "209";
  public static final Logger log = LogManager.getLogger();

  @Inject
  @Any
  @Qualifier(productCategoryTreePropertyExtension)
  private Instance<ModelExtension> extensions;

  @Override
  protected Map<String, Object> getParameterValues(final JSONObject jsonsent) throws JSONException {
    OBContext.setAdminMode(true);
    try {
      final Calendar now = Calendar.getInstance();
      final String clientId = OBContext.getOBContext().getCurrentClient().getId();
      final String orgId = OBContext.getOBContext().getCurrentOrganization().getId();
      final String posId = jsonsent.getString("pos");
      final OBPOSApplications pos = POSUtils.getTerminalById(posId);
      final boolean isCrossStoreEnabled = POSUtils.isCrossStoreEnabled(pos);
      final List<String> orgIds = POSUtils.getOrgListCrossStore(posId, isCrossStoreEnabled);
      final String productListId = POSUtils.getProductListByPosterminalId(posId).getId();

      final Map<String, Object> paramValues = new HashMap<>();
      paramValues.put("clientId", clientId);
      paramValues.put("orgId", orgId);
      paramValues.put("orgIds", orgIds);
      paramValues.put("startingDate", now.getTime());
      paramValues.put("endingDate", now.getTime());
      paramValues.put("productListId", productListId);
      paramValues.put("productCategoryTableId", CategoryTree.productCategoryTableId);
      return paramValues;
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  @Override
  protected List<String> getQuery(final JSONObject jsonsent) throws JSONException {
    final Long lastUpdated = getLastUpdated(jsonsent);
    final HQLPropertyList regularProductsCategoriesTreeHQLProperties = ModelExtensionUtils
        .getPropertyExtensions(extensions);

    final List<String> hqlQueries = new ArrayList<>();
    hqlQueries.add(getRegularProductCategoryTreeHqlString(
        regularProductsCategoriesTreeHQLProperties, lastUpdated));
    hqlQueries.add(getSummaryProductCategoryTreeHqlString(
        regularProductsCategoriesTreeHQLProperties, lastUpdated));
    hqlQueries.add(getPackProductCategoryTreeHqlString());
    return hqlQueries;
  }

  private String getRegularProductCategoryTreeHqlString(
      final HQLPropertyList regularProductsCategoriesTreeHQLProperties, final Long lastUpdated) {
    final StringBuilder query = new StringBuilder();
    query.append(" select distinct");
    query.append(regularProductsCategoriesTreeHQLProperties.getHqlSelect());
    query.append(" from OBRETCO_Productcategory pCat");
    query.append(" join pCat.productCategory pc");
    query.append(" , ADTreeNode tn");
    query.append(" where tn.$readableSimpleClientCriteria");
    query.append(" and tn.node = pc.id");
    query.append(" and tn.tree.table.id = :productCategoryTableId");
    query.append(" and (tn.$incrementalUpdateCriteria");
    query.append(" " + (lastUpdated == null ? "and" : "or") + " pc.$incrementalUpdateCriteria)");
    query.append(" and exists (");
    query.append("   select 1");
    query.append("   from Organization o");
    query.append("   where o.id in :orgIds");
    query.append("   and ad_org_isinnaturaltree(tn.organization.id, o.id, tn.client.id) = 'Y'");
    query.append(" )");
    return query.toString();
  }

  private String getSummaryProductCategoryTreeHqlString(
      final HQLPropertyList regularProductsCategoriesTreeHQLProperties, final Long lastUpdated) {
    final StringBuilder query = new StringBuilder();
    query.append(" select");
    query.append(regularProductsCategoriesTreeHQLProperties.getHqlSelect());
    query.append(" from ProductCategory pc");
    query.append(" , ADTreeNode tn");
    query.append(" where tn.$readableSimpleClientCriteria");
    query.append(" and tn.node = pc.id");
    query.append(" and tn.tree.table.id = :productCategoryTableId");
    query.append(" and pc.summaryLevel = true");
    query.append(" and (tn.$incrementalUpdateCriteria");
    query.append(" " + (lastUpdated == null ? "and" : "or") + " pc.$incrementalUpdateCriteria)");
    query.append(" and exists (");
    query.append("   select 1");
    query.append("   from Organization o");
    query.append("   where o.id in :orgIds");
    query.append("   and ad_org_isinnaturaltree(tn.organization.id, o.id, tn.client.id) = 'Y'");
    query.append(" )");
    return query.toString();
  }

  private String getPackProductCategoryTreeHqlString() {
    final StringBuilder query = new StringBuilder();
    query.append(" select pt.id as id");
    query.append(" , pt.id as categoryId");
    query.append(" , '0' as parentId");
    query.append(" , 999999999 as seqNo");
    query.append(" , case when exists (");
    query.append("   select 1");
    query.append("   from PricingAdjustment p");
    query.append("   where p.discountType.id = pt.id");
    query.append("   and p.active = true");
    query.append("   and " + Product.getPackProductWhereClause());
    query.append(" ) then true else false end as active");
    query.append(" from PromotionType pt");
    query.append(" join pt.pricingAdjustmentList p");
    query.append(" where pt.active = true");
    query.append(" and pt.obposIsCategory = true");
    query.append(" and pt.$readableSimpleClientCriteria");
    query.append(" and p.$incrementalUpdateCriteria");
    query.append(" and " + Product.getPackProductWhereClause());
    query.append(" group by pt.id");
    return query.toString();
  }

  private Long getLastUpdated(final JSONObject jsonsent) throws JSONException {
    return jsonsent.has("lastUpdated") && !jsonsent.get("lastUpdated").equals("undefined")
        && !jsonsent.get("lastUpdated").equals("null") ? jsonsent.getLong("lastUpdated") : null;
  }

  @Override
  protected boolean bypassPreferenceCheck() {
    return true;
  }
}
