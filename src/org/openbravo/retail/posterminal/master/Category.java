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
import java.util.Collections;
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
    try {
      OBContext.setAdminMode(true);

      final Calendar now = Calendar.getInstance();
      final String clientId = OBContext.getOBContext().getCurrentClient().getId();
      final String orgId = OBContext.getOBContext().getCurrentOrganization().getId();
      final String posId = jsonsent.getString("pos");
      final String assortmentId = POSUtils.getProductListByPosterminalId(posId).getId();
      final Set<String> crossStoreAssortmentIds = POSUtils.getProductListCrossStore(posId);
      crossStoreAssortmentIds.add(assortmentId);

      Map<String, Object> paramValues = new HashMap<>();

      if (OBContext.hasTranslationInstalled()) {
        paramValues.put("languageId", OBContext.getOBContext().getLanguage().getId());
      }

      paramValues.put("endingDate", now.getTime());
      paramValues.put("startingDate", now.getTime());
      paramValues.put("clientId", clientId);
      paramValues.put("orgId", orgId);
      paramValues.put("assortmentId", assortmentId);
      paramValues.put("assortmentIds",
          POSUtils.isCrossStoreSearch(jsonsent) ? crossStoreAssortmentIds
              : Collections.singleton(assortmentId));
      return paramValues;
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  @Override
  protected List<String> getQuery(JSONObject jsonsent) throws JSONException {
    final List<String> hqlQueries = new ArrayList<>();
    final HQLPropertyList regularProductsCategoriesHQLProperties = ModelExtensionUtils
        .getPropertyExtensions(extensions);
    final Long lastUpdated = jsonsent != null && jsonsent.has("lastUpdated")
        && !jsonsent.get("lastUpdated").equals("undefined")
        && !jsonsent.get("lastUpdated").equals("null") ? jsonsent.getLong("lastUpdated") : null;

    hqlQueries.add(
        getRegularProductCategoryHqlString(regularProductsCategoriesHQLProperties, lastUpdated));
    hqlQueries.add(getSummaryProductCategoryHqlString(regularProductsCategoriesHQLProperties));
    hqlQueries.add(getDiscountProductCategoryHqlString());

    return hqlQueries;
  }

  private String getRegularProductCategoryHqlString(
      final HQLPropertyList regularProductsCategoriesHQLProperties, final Long lastUpdated) {
    // TODO: Check if lastUpdated check is needed
    final StringBuilder query = new StringBuilder();
    query.append(" select");
    query.append(regularProductsCategoriesHQLProperties.getHqlSelect());
    query.append(" from OBRETCO_Productcategory aCat");
    query.append(" join aCat.productCategory as pCat");
    query.append(" left join pCat.image as img");
    query.append(" where aCat.$readableSimpleClientCriteria");
    query.append(" and aCat.$naturalOrgCriteria");
    query.append(" and aCat.obretcoProductlist.id in :assortmentIds");
    query.append(" and (aCat.$incrementalUpdateCriteria");
    query.append(lastUpdated == null ? "and" : "or");
    query.append(" pCat.$incrementalUpdateCriteria)");
    query.append(" order by pCat.name, pCat.id");
    return query.toString();
  }

  private String getSummaryProductCategoryHqlString(
      final HQLPropertyList regularProductsCategoriesHQLProperties) {
    // TODO: Check if it works with Cross Store
    final StringBuilder query = new StringBuilder();
    query.append(" select");
    query.append(regularProductsCategoriesHQLProperties.getHqlSelect());
    query.append(" from ProductCategory pCat");
    query.append(" left join pCat.image as img");
    query.append(" where pCat.summaryLevel = 'Y'");
    query.append(" and pCat.$readableSimpleClientCriteria");
    query.append(" and pCat.$naturalOrgCriteria");
    query.append(" and pCat.$incrementalUpdateCriteria");
    query.append(" order by pCat.name, pCat.id");
    return query.toString();
  }

  private String getDiscountProductCategoryHqlString() {
    // TODO: Check if it works with Cross Store
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
    query.append(" , (select bindaryData from ADImage ai where ai = pt.obposImage) as img");
    query.append(" , (case when (count(p.name) > 0 and exists (select 1 from PricingAdjustment p "
        + "where p.discountType = pt and p.active = true and " + getDiscountWhereClause() + ")) "
        + "then true else false end) as active");
    query.append(" , 'N' as realCategory");
    query.append(" from PromotionType pt");
    query.append(" join pt.pricingAdjustmentList p");
    query.append(" where pt.active = true");
    query.append(" and pt.obposIsCategory = true");
    query.append(" and pt.$readableSimpleClientCriteria");
    query.append(" and p.$incrementalUpdateCriteria");
    query.append(" and " + getDiscountWhereClause());
    query.append(" group by pt.id, pt.commercialName, pt.obposImage");
    return query.toString();
  }

  private String getDiscountWhereClause() {
    // TODO: Fix for Cross Store
    final StringBuilder whereClause = new StringBuilder();
    whereClause.append(" p.client.id = :clientId");
    whereClause.append(" and p.startingDate <= :startingDate");
    whereClause.append(" and (p.endingDate is null or p.endingDate >= :endingDate)");
    // assortment products
    whereClause.append(" and ((p.includedProducts = 'N'");
    whereClause.append(" and not exists (");
    whereClause.append("   select 1");
    whereClause.append("   from PricingAdjustmentProduct pap");
    whereClause.append("   where pap.active = true");
    whereClause.append("   and pap.priceAdjustment.id = p.id");
    whereClause.append("   and pap.product.sale = true");
    whereClause.append("   and pap.product");
    whereClause.append("   not in (");
    whereClause.append("     select ppl.product.id");
    whereClause.append("     from OBRETCO_Prol_Product ppl");
    whereClause.append("     where ppl.obretcoProductlist.id = :assortmentId");
    whereClause.append("     and ppl.active = true");
    whereClause.append("   )");
    whereClause.append(" ))");
    whereClause.append(" or (p.includedProducts = 'Y'");
    whereClause.append(" and not exists (");
    whereClause.append("   select 1");
    whereClause.append("   from PricingAdjustmentProduct pap,");
    whereClause.append("   OBRETCO_Prol_Product ppl");
    whereClause.append("   where pap.active = true");
    whereClause.append("   and pap.priceAdjustment = p");
    whereClause.append("   and pap.product.id = ppl.product.id");
    whereClause.append("   and ppl.obretcoProductlist.id = :assortmentId");
    whereClause.append(" )))");
    // organization
    whereClause.append(" and ((p.includedOrganizations='Y'");
    whereClause.append(" and not exists (");
    whereClause.append("   select 1");
    whereClause.append("   from PricingAdjustmentOrganization o");
    whereClause.append("   where active = true");
    whereClause.append("   and o.priceAdjustment = p");
    whereClause.append("   and o.organization.id = :orgId");
    whereClause.append(" ))");
    whereClause.append(" or (p.includedOrganizations='N'");
    whereClause.append(" and  exists (");
    whereClause.append("   select 1");
    whereClause.append("   from PricingAdjustmentOrganization o");
    whereClause.append("   where active = true");
    whereClause.append("   and o.priceAdjustment = p");
    whereClause.append("   and o.organization.id = :orgId");
    whereClause.append(" )))");

    return whereClause.toString();
  }

  @Override
  protected boolean bypassPreferenceCheck() {
    return true;
  }
}
