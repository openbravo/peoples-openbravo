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
import org.openbravo.retail.posterminal.OBPOSApplications;
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
      final String clientId = OBContext.getOBContext().getCurrentClient().getId();
      final String orgId = OBContext.getOBContext().getCurrentOrganization().getId();
      final String posId = jsonsent.getString("pos");
      final OBPOSApplications pos = POSUtils.getTerminalById(posId);
      final boolean isCrossStoreEnabled = POSUtils.isCrossStoreEnabled(pos);
      final String productListId = POSUtils.getProductListByPosterminalId(posId).getId();
      final List<String> orgIds = POSUtils.getOrgListCrossStore(posId, isCrossStoreEnabled);
      final Set<String> productListIds = POSUtils.getProductListCrossStore(posId,
          isCrossStoreEnabled);

      final Map<String, Object> paramValues = new HashMap<>();
      if (OBContext.hasTranslationInstalled()) {
        paramValues.put("languageId", OBContext.getOBContext().getLanguage().getId());
      }
      paramValues.put("clientId", clientId);
      paramValues.put("orgId", orgId);
      paramValues.put("orgIds", orgIds);
      paramValues.put("startingDate", now.getTime());
      paramValues.put("endingDate", now.getTime());
      paramValues.put("productListId", productListId);
      paramValues.put("productListIds", productListIds);
      paramValues.put("productCategoryTableId", CategoryTree.productCategoryTableId);
      return paramValues;
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  @Override
  protected List<String> getQuery(JSONObject jsonsent) throws JSONException {
    final boolean isCrossStoreEnabled = POSUtils
        .isCrossStoreEnabled(POSUtils.getTerminalById(jsonsent.getString("pos")));
    final Long lastUpdated = getLastUpdated(jsonsent);
    final HQLPropertyList regularProductsCategoriesHQLProperties = ModelExtensionUtils
        .getPropertyExtensions(extensions);

    final List<String> hqlQueries = new ArrayList<>();
    hqlQueries.add(getRegularProductCategoryHqlString(regularProductsCategoriesHQLProperties,
        lastUpdated, false));
    hqlQueries.add(getSummaryProductCategoryHqlString(regularProductsCategoriesHQLProperties,
        lastUpdated, false));
    if (isCrossStoreEnabled) {
      hqlQueries.add(getRegularProductCategoryHqlString(regularProductsCategoriesHQLProperties,
          lastUpdated, true));
      hqlQueries.add(getSummaryProductCategoryHqlString(regularProductsCategoriesHQLProperties,
          lastUpdated, true));
    }
    hqlQueries.add(getPackProductCategoryHqlString());

    return hqlQueries;
  }

  private String getRegularProductCategoryHqlString(
      final HQLPropertyList regularProductsCategoriesHQLProperties, final Long lastUpdated,
      final boolean isCrossStore) {
    final String assortmentFilter = isCrossStore ? ":productListIds" : ":productListId";
    final String lastUpdatedFilter = lastUpdated == null ? "and" : "or";

    final StringBuilder query = new StringBuilder();
    query.append(" select");
    query.append(regularProductsCategoriesHQLProperties.getHqlSelect());
    query.append(" , " + isCrossStore + " as crossStore");
    query.append(" from ProductCategory pCat");
    query.append(" left join pCat.image img");
    query.append(" where pCat.$readableSimpleClientCriteria");
    query.append(" and exists (");
    query.append("   select 1");
    query.append("   from OBRETCO_Productcategory aCat");
    query.append("   where aCat.productCategory.id = pCat.id");
    query.append("   and aCat.obretcoProductlist.id in " + assortmentFilter);
    query.append("   and (aCat.$incrementalUpdateCriteria");
    query.append("   " + lastUpdatedFilter + " pCat.$incrementalUpdateCriteria)");
    query.append(" )");
    if (isCrossStore) {
      query.append(" and not exists (");
      query.append("   select 1");
      query.append("   from OBRETCO_Productcategory aCat2");
      query.append("   where aCat2.productCategory.id = pCat.id");
      query.append("   and aCat2.obretcoProductlist.id = :productListId");
      query.append(" )");
    }
    query.append(" order by pCat.name, pCat.id");
    return query.toString();
  }

  private String getSummaryProductCategoryHqlString(
      final HQLPropertyList regularProductsCategoriesHQLProperties, final Long lastUpdated,
      final boolean isCrossStore) {
    final String storeFilter = isCrossStore ? ":orgIds" : ":orgId";

    final StringBuilder query = new StringBuilder();
    query.append(" select");
    query.append(regularProductsCategoriesHQLProperties.getHqlSelect());
    query.append(" , " + isCrossStore + " as crossStore");
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
    query.append("   where o.id in " + storeFilter);
    query.append("   and ad_org_isinnaturaltree(pCat.organization.id, o.id, pCat.client.id) = 'Y'");
    query.append(" )");
    query.append(" and (tn.$incrementalUpdateCriteria");
    query.append(" " + (lastUpdated == null ? "and" : "or") + " pCat.$incrementalUpdateCriteria)");
    if (isCrossStore) {
      query.append(" and not exists (");
      query.append("   select 1");
      query.append("   from ProductCategory pCat2");
      query.append("   where pCat2.id = pCat.id");
      query.append(
          "   and ad_org_isinnaturaltree(pCat2.organization.id, :orgId, pCat2.client.id) = 'Y'");
      query.append(" )");
    }
    query.append(" order by pCat.name, pCat.id");
    return query.toString();
  }

  private String getPackProductCategoryHqlString() {
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
    query.append(" , 'N' as realCategory");
    query.append(" , (");
    query.append("   select bindaryData");
    query.append("   from ADImage ai");
    query.append("   where ai = pt.obposImage");
    query.append(" ) as img");
    query.append(" , case when exists (");
    query.append("   select 1");
    query.append("   from PricingAdjustment p");
    query.append("   where p.discountType.id = pt.id");
    query.append("   and p.active = true");
    query.append("   and " + Product.getPackProductWhereClause());
    query.append(" ) then true else false end as active");
    query.append(" , false as crossStore");
    query.append(" from PromotionType pt");
    query.append(" join pt.pricingAdjustmentList p");
    query.append(" where pt.active = true");
    query.append(" and pt.obposIsCategory = true");
    query.append(" and pt.$readableSimpleClientCriteria");
    query.append(" and p.$incrementalUpdateCriteria");
    query.append(" and " + Product.getPackProductWhereClause());
    query.append(" group by pt.id, pt.commercialName, pt.obposImage");
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
