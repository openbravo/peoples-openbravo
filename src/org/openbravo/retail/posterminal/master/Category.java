/*
 ************************************************************************************
 * Copyright (C) 2012-2019 Openbravo S.L.U.
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
import java.util.Set;

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
import org.openbravo.retail.posterminal.POSUtils;
import org.openbravo.retail.posterminal.ProcessHQLQuery;

public class Category extends ProcessHQLQuery {
  public static final String productCategoryPropertyExtension = "OBPOS_ProductCategoryExtension";
  public static final Logger log = LogManager.getLogger();

  @Inject
  @Any
  @Qualifier(productCategoryPropertyExtension)
  private Instance<ModelExtension> extensions;

  @Override
  protected Map<String, Object> getParameterValues(JSONObject jsonsent) throws JSONException {
    OBContext.setAdminMode(true);
    try {
      final Calendar now = Calendar.getInstance();
      final String posId = jsonsent.getString("pos");
      final String clientId = OBContext.getOBContext().getCurrentClient().getId();
      final String orgId = OBContext.getOBContext().getCurrentOrganization().getId();
      final List<String> crossStoreOrgIds = POSUtils.getOrgListCrossStore(posId);
      final String assortmentId = POSUtils.getProductListByPosterminalId(posId).getId();
      final Set<String> crossStoreAssortmentIds = POSUtils.getProductListCrossStore(posId);

      final Map<String, Object> paramValues = new HashMap<>();
      if (OBContext.hasTranslationInstalled()) {
        paramValues.put("languageId", OBContext.getOBContext().getLanguage().getId());
      }
      paramValues.put("clientId", clientId);
      paramValues.put("orgId", orgId);
      paramValues.put("crossStoreOrgIds", crossStoreOrgIds);
      paramValues.put("startingDate", now.getTime());
      paramValues.put("endingDate", now.getTime());
      paramValues.put("assortmentId", assortmentId);
      paramValues.put("crossStoreAssortmentIds", crossStoreAssortmentIds);
      paramValues.put("productCategoryTableId", CategoryTree.productCategoryTableId);
      return paramValues;
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  @Override
  protected List<String> getQuery(JSONObject jsonsent) throws JSONException {
    final List<String> hqlQueries = new ArrayList<>();
    final boolean isCrossStoreEnabled = POSUtils
        .isCrossStoreEnabled(POSUtils.getTerminalById(jsonsent.getString("pos")));
    final HQLPropertyList regularProductsCategoriesHQLProperties = ModelExtensionUtils
        .getPropertyExtensions(extensions);

    final Long lastUpdated = jsonsent.has("lastUpdated")
        && !jsonsent.get("lastUpdated").equals("undefined")
        && !jsonsent.get("lastUpdated").equals("null") ? jsonsent.getLong("lastUpdated") : null;

    hqlQueries.add(
        getRegularProductCategoryHqlString(regularProductsCategoriesHQLProperties, lastUpdated));
    hqlQueries.add(
        getSummaryProductCategoryHqlString(regularProductsCategoriesHQLProperties, lastUpdated));
    hqlQueries.add(getPackProductCategoryHqlString(isCrossStoreEnabled));

    return hqlQueries;
  }

  private String getRegularProductCategoryHqlString(
      final HQLPropertyList regularProductsCategoriesHQLProperties, final Long lastUpdated) {
    final StringBuilder query = new StringBuilder();
    query.append(" select");
    query.append(regularProductsCategoriesHQLProperties.getHqlSelect());
    query.append(
        " , min(case when aCat.obretcoProductlist.id = :assortmentId then 'N' else 'Y' end) as crossStore");
    query.append(" from OBRETCO_Productcategory aCat");
    query.append(" join aCat.productCategory pCat");
    query.append(" left join pCat.image img");
    query.append(" where aCat.$readableSimpleClientCriteria");
    query.append(" and aCat.obretcoProductlist.id in :crossStoreAssortmentIds");
    query.append(" and (aCat.$incrementalUpdateCriteria");
    query.append(" " + (lastUpdated == null ? "and" : "or") + " pCat.$incrementalUpdateCriteria)");
    query.append(" group by");
    query.append(regularProductsCategoriesHQLProperties.getHqlGroupBy());
    query.append(" order by pCat.name, pCat.id");
    return query.toString();
  }

  private String getSummaryProductCategoryHqlString(
      final HQLPropertyList regularProductsCategoriesHQLProperties, final Long lastUpdated) {
    final StringBuilder query = new StringBuilder();
    query.append(" select");
    query.append(regularProductsCategoriesHQLProperties.getHqlSelect());
    query.append(
        " , min(case when ad_org_isinnaturaltree(pCat.organization.id, :orgId, pCat.client.id) = 'Y' then 'N' else 'Y' end) as crossStore");
    query.append(" from ProductCategory pCat");
    query.append(" left join pCat.image img");
    query.append(" , ADTreeNode tn");
    query.append(" where pCat.$readableSimpleClientCriteria");
    query.append(" and tn.node = pCat.id");
    query.append(" and tn.tree.table.id = :productCategoryTableId");
    query.append(" and pCat.summaryLevel = true");
    query.append(" and exists (");
    query.append("   select 1");
    query.append("   from Organization o");
    query.append("   where o.id in :crossStoreOrgIds");
    query.append("   and ad_org_isinnaturaltree(pCat.organization.id, o.id, o.client.id) = 'Y'");
    query.append(" )");
    query.append(" and (tn.$incrementalUpdateCriteria");
    query.append(" " + (lastUpdated == null ? "and" : "or") + " pCat.$incrementalUpdateCriteria)");
    query.append(" group by");
    query.append(regularProductsCategoriesHQLProperties.getHqlGroupBy());
    query.append(" order by pCat.name, pCat.id");
    return query.toString();
  }

  private String getPackProductCategoryHqlString(final boolean isCrossStoreEnabled) {
    String promoNameTrl;
    if (OBContext.hasTranslationInstalled()) {
      promoNameTrl = "coalesce ((select t.commercialName from PromotionTypeTrl t where t.discountPromotionType=pt and t.language.id= :languageId), pt.commercialName)";
    } else {
      promoNameTrl = "pt.commercialName";
    }

    final StringBuilder query = new StringBuilder();
    query.append(" select pt.id as id");
    query.append(" , " + promoNameTrl + " as searchKey");
    query.append(" , " + promoNameTrl + " as name");
    query.append(" , " + promoNameTrl + " as _identifier");
    query.append(" , false as realCategory");
    query.append(" , (");
    query.append("   select bindaryData");
    query.append("   from ADImage ai");
    query.append("   where ai = pt.obposImage");
    query.append(" ) as img");
    query.append(" , case when exists (");
    query.append("   select 1");
    query.append("   from PricingAdjustment p2");
    query.append("   where p2.discountType.id = pt.id");
    query.append("   and p2.active = true");
    query.append("   and " + Product.getPackProductWhereClause("p2", isCrossStoreEnabled));
    query.append(" ) then true else false end as active");
    if (isCrossStoreEnabled) {
      query.append(" , case when exists (");
      query.append("   select 1");
      query.append("   from PricingAdjustment p2");
      query.append("   where p2.discountType.id = pt.id");
      query.append("   and p2.active = true");
      query.append("   and " + Product.getPackProductWhereClause("p2", false));
      query.append(" ) then false else true end as crossStore");
    } else {
      query.append(" , false as crossStore");
    }
    query.append(" from PromotionType pt");
    query.append(" join pt.pricingAdjustmentList p");
    query.append(" where pt.active = true");
    query.append(" and pt.obposIsCategory = true");
    query.append(" and pt.$readableSimpleClientCriteria");
    query.append(" and p.$incrementalUpdateCriteria");
    query.append(" and " + Product.getPackProductWhereClause("p", isCrossStoreEnabled));
    query.append(" group by pt.id, pt.commercialName, pt.obposImage");
    return query.toString();
  }

  @Override
  protected boolean bypassPreferenceCheck() {
    return true;
  }
}
