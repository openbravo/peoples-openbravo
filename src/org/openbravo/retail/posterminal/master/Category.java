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
import java.util.Date;
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
import org.openbravo.mobile.core.utils.OBMOBCUtils;
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
      final Date terminalDate = OBMOBCUtils.calculateServerDate(
          jsonsent.getJSONObject("parameters").getString("terminalTime"),
          jsonsent.getJSONObject("parameters")
              .getJSONObject("terminalTimeOffset")
              .getLong("value"));
      final String clientId = OBContext.getOBContext().getCurrentClient().getId();
      final String orgId = OBContext.getOBContext().getCurrentOrganization().getId();
      final String posId = jsonsent.getString("pos");
      final List<String> crossStoreOrgIds = POSUtils.getOrgListCrossStore(posId);
      final String assortmentId = POSUtils.getProductListByPosterminalId(posId).getId();
      final Set<String> crossStoreAssortmentIds = POSUtils.getProductListCrossStore(posId);

      Map<String, Object> paramValues = new HashMap<>();

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
      paramValues.put("terminalDate", terminalDate);
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

    hqlQueries.add(getRegularProductCategoryHqlString(regularProductsCategoriesHQLProperties));
    hqlQueries.add(getPackProductCategoryHqlString(isCrossStoreEnabled));

    return hqlQueries;
  }

  private String getRegularProductCategoryHqlString(
      final HQLPropertyList regularProductsCategoriesHQLProperties) {
    final StringBuilder query = new StringBuilder();
    query.append(" select");
    query.append(regularProductsCategoriesHQLProperties.getHqlSelect());
    query.append(" from ProductCategory pCat");
    query.append(" left join pCat.image img");
    query.append(" left join pCat.oBRETCOProductcategoryList aCat");
    query.append(" with aCat.obretcoProductlist.id = :assortmentId");
    query.append(" where pCat.$incrementalUpdateCriteria");
    query.append(" and (exists (");
    query.append("   select 1");
    query.append("   from OBRETCO_Productcategory aCat2");
    query.append("   where aCat2.obretcoProductlist.id in :crossStoreAssortmentIds");
    query.append("   and aCat2.productCategory.id = pCat.id");
    query.append(" )");
    query.append(" or (pCat.summaryLevel = true");
    query.append(" and pCat.$readableSimpleClientCriteria");
    query.append(" and pCat.$naturalOrgCriteria");
    query.append(" ))");
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
