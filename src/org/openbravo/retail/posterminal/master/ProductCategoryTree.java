/*
 ************************************************************************************
 * Copyright (C) 2018 Openbravo S.L.U.
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
import java.util.stream.Collectors;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.client.kernel.ComponentProvider.Qualifier;
import org.openbravo.dal.core.OBContext;
import org.openbravo.erpCommon.businessUtility.Preferences;
import org.openbravo.erpCommon.utility.PropertyException;
import org.openbravo.mobile.core.master.MasterDataProcessHQLQuery;
import org.openbravo.mobile.core.master.MasterDataProcessHQLQuery.MasterDataModel;
import org.openbravo.mobile.core.model.HQLProperty;
import org.openbravo.mobile.core.model.HQLPropertyList;
import org.openbravo.mobile.core.model.ModelExtension;
import org.openbravo.mobile.core.model.ModelExtensionUtils;
import org.openbravo.mobile.core.utils.OBMOBCUtils;
import org.openbravo.model.pricing.pricelist.PriceListVersion;
import org.openbravo.retail.config.OBRETCOProductList;
import org.openbravo.retail.posterminal.OBPOSApplications;
import org.openbravo.retail.posterminal.POSUtils;

@MasterDataModel("ProductCategoryTree")
public class ProductCategoryTree extends MasterDataProcessHQLQuery {
  public static final String productCategoryTreePropertyExtension = "OBPOS_ProductCategoryTreeExtension";
  public static final String productCategoryTableId = "209";
  public static final Logger log = LogManager.getLogger();

  @Inject
  @Any
  @Qualifier(productCategoryTreePropertyExtension)
  private Instance<ModelExtension> extensions;

  @Override
  protected Map<String, Object> getParameterValues(JSONObject jsonsent) throws JSONException {

    try {
      OBContext.setAdminMode(true);
      String clientId = OBContext.getOBContext().getCurrentClient().getId();
      String orgId = OBContext.getOBContext().getCurrentOrganization().getId();
      final OBRETCOProductList productList = POSUtils
          .getProductListByPosterminalId(jsonsent.getString("pos"));
      final String posId = jsonsent.getString("pos");
      final OBPOSApplications pos = POSUtils.getTerminalById(posId);
      final boolean isCrossStoreEnabled = POSUtils.isCrossStoreEnabled(pos);
      final Set<String> productListIds = POSUtils.getProductListCrossStore(posId,
          isCrossStoreEnabled);
      final List<String> orgIds = POSUtils.getOrgListCrossStore(posId, isCrossStoreEnabled);

      boolean isRemote = false;
      try {
        OBContext.setAdminMode(false);
        isRemote = "Y".equals(Preferences.getPreferenceValue("OBPOS_remote.product", true,
            OBContext.getOBContext().getCurrentClient(),
            OBContext.getOBContext().getCurrentOrganization(), OBContext.getOBContext().getUser(),
            OBContext.getOBContext().getRole(), null));
      } catch (PropertyException e) {
        log.error("Error getting preference OBPOS_remote.product " + e.getMessage(), e);
      } finally {
        OBContext.restorePreviousMode();
      }

      Map<String, Object> paramValues = new HashMap<String, Object>();
      paramValues.put("productListId", productList.getId());
      paramValues.put("productListIds", productListIds);
      paramValues.put("productCategoryTableId", productCategoryTableId);
      if (isRemote) {
        paramValues.put("productCategoryTableId", productCategoryTableId);
      }
      if (!isRemote) {
        final Date terminalDate = OBMOBCUtils.calculateServerDate(
            jsonsent.getJSONObject("parameters").getString("terminalTime"),
            jsonsent.getJSONObject("parameters")
                .getJSONObject("terminalTimeOffset")
                .getLong("value"));

        final PriceListVersion priceListVersion = POSUtils.getPriceListVersionByOrgId(orgId,
            terminalDate);
        paramValues.put("priceListVersionId", priceListVersion.getId());
      }
      if (OBContext.hasTranslationInstalled()) {
        paramValues.put("languageId", OBContext.getOBContext().getLanguage().getId());
      }
      paramValues.put("productCategoryTableId", productCategoryTableId);
      if (isRemote) {
        paramValues.put("productCategoryTableId", productCategoryTableId);
      }
      Calendar now = Calendar.getInstance();
      paramValues.put("endingDate", now.getTime());
      paramValues.put("startingDate", now.getTime());
      paramValues.put("clientId", clientId);
      paramValues.put("orgId", orgId);
      paramValues.put("orgIds", orgIds);
      return paramValues;
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  @Override
  protected List<String> getQuery(JSONObject jsonsent) throws JSONException {
    final boolean isCrossStoreEnabled = POSUtils
        .isCrossStoreEnabled(POSUtils.getTerminalById(jsonsent.getString("pos")));
    List<String> hqlQueries = new ArrayList<String>();

    HQLPropertyList regularProductsCategoriesAndTreesHQLProperties = ModelExtensionUtils
        .getPropertyExtensions(extensions);
    Long lastUpdated = null;
    if (jsonsent != null) {
      lastUpdated = jsonsent.has("lastUpdated") && !jsonsent.get("lastUpdated").equals("undefined")
          && !jsonsent.get("lastUpdated").equals("null") ? jsonsent.getLong("lastUpdated") : null;
    }

    hqlQueries.add(getRegularProductCategoryHqlString(
        regularProductsCategoriesAndTreesHQLProperties, lastUpdated, false));
    hqlQueries.add(getSummaryProductCategoryHqlString(
        regularProductsCategoriesAndTreesHQLProperties, lastUpdated, false));
    if (isCrossStoreEnabled) {
      hqlQueries.add(getRegularProductCategoryHqlString(
          regularProductsCategoriesAndTreesHQLProperties, lastUpdated, true));
      hqlQueries.add(getSummaryProductCategoryHqlString(
          regularProductsCategoriesAndTreesHQLProperties, lastUpdated, true));
    }
    hqlQueries.add(getPackProductCategoryHqlString());

    return hqlQueries;

  }

  private String getRegularProductCategoryHqlString(
      final HQLPropertyList regularProductsCategoriesAndTreesHQLProperties, final Long lastUpdated,
      final boolean isCrossStore) {
    final String assortmentFilter = isCrossStore ? ":productListIds" : ":productListId";
    final String lastUpdatedFilter = lastUpdated == null ? "and" : "or";
    // @formatter:off
    String hqlQuery = "select" + regularProductsCategoriesAndTreesHQLProperties.getHqlSelect() //
        + " , " +isCrossStore + " as crossStore" 
        + " from OBRETCO_Productcategory aCat "
        + " left outer join aCat.productCategory as pCat "
        + " left outer join pCat.image as img, ADTreeNode as tn "
        + " where ( aCat.obretcoProductlist.id in  " + assortmentFilter + " ) "
        + " and tn.node = aCat.productCategory.id "
        + " and tn.tree.table.id = :productCategoryTableId"
        + " and (aCat.$incrementalUpdateCriteria " + lastUpdatedFilter   + " pCat.$incrementalUpdateCriteria) " 
        + " and aCat.active = true "
        + " and aCat.$readableSimpleClientCriteria "
        + " and tn.$incrementalUpdateCriteria " 
        + " and tn.active = true "
        + " and tn.$readableSimpleClientCriteria ";
    if (isCrossStore) {
      hqlQuery = hqlQuery
          + " and not exists (select 1 "
          + "                 from OBRETCO_Productcategory aCat2 "
          + "                 where aCat2.productCategory.id = pCat.id  "
          + "                 and aCat2.obretcoProductlist.id = :productListId) ";
    }
    hqlQuery = hqlQuery + " order by pCat.name, pCat.id ";
    // @formatter:on
    return hqlQuery;
  }

  private String getSummaryProductCategoryHqlString(
      final HQLPropertyList regularProductsCategoriesAndTreesHQLProperties, final Long lastUpdated,
      final boolean isCrossStore) {
    final String storeFilter = isCrossStore ? ":orgIds" : ":orgId";
    final String lastUpdatedFilter = lastUpdated == null ? "and" : "or";
    // @formatter:off
    String hqlQuery = "select" + regularProductsCategoriesAndTreesHQLProperties.getHqlSelect() //
        + " , " + isCrossStore + " as crossStore"
        + " from ADTreeNode tn, ProductCategory pCat "
        + " left outer join pCat.image as img "
        + " where tn.$incrementalUpdateCriteria "
        + " and pCat.active = true "
        + " and pCat.summaryLevel = 'Y'"     
        + " and tn.$readableSimpleClientCriteria "
        + " and tn.node = pCat.id "
        + " and tn.tree.table.id = :productCategoryTableId "
        + " and exists (select 1 from Organization o "
        + "             where o.id in " + storeFilter
        + "             and ad_org_isinnaturaltree(pCat.organization.id, o.id, pCat.client.id) = 'Y' ) "
        + " and (tn.$incrementalUpdateCriteria "+ lastUpdatedFilter + " pCat.$incrementalUpdateCriteria) "
        + " and not exists (select pc.id from OBRETCO_Productcategory pc where tn.node = pc.productCategory.id) ";
        if (isCrossStore) {
          hqlQuery = hqlQuery + " and not exists ( select 1 "
              + "                                  from ProductCategory pCat2 "
              + "                                  where pCat2.id = pCat.id "
              + "                                  and ad_org_isinnaturaltree(pCat2.organization.id, :orgId, pCat2.client.id) = 'Y' ) ";
        }
        hqlQuery = hqlQuery +  " order by tn.sequenceNumber, tn.id ";
     // @formatter:on
    return hqlQuery;
  }

  private String getPackProductCategoryHqlString() {
    String promoNameTrl;
    // @formatter:off
    if (OBContext.hasTranslationInstalled()) {
      promoNameTrl = "coalesce ((select t.commercialName from PromotionTypeTrl t where t.discountPromotionType=pt and t.language.id= :languageId), pt.commercialName) ";
    } else {
      promoNameTrl = "pt.commercialName ";
    }
    // Discounts marked as category
    String hqlQuery= "select pt.id as id, pt.id as categoryId, " 
        + promoNameTrl + " as searchKey, " 
        + promoNameTrl + " as name, " 
        + promoNameTrl + " as _identifier, '0' as parentId, 999999999 as seqNo, "
        + " (select bindaryData from ADImage ai where ai = pt.obposImage) as img, "
        + " case when exists ( select 1 "
        + "                    from PricingAdjustment p "
        + "                    where p.discountType.id = pt.id and p.active = true and " + Product.getPackProductWhereClause() + " ) "
        + "      then true else false end as active, "
        + " false as crossStore, 'N' as realCategory , '' as treeNodeId,  '' as categoryId  " //
        + " from PromotionType pt "
        + " join pt.pricingAdjustmentList p "
        + " where pt.active = true "
        + " and pt.obposIsCategory = true "//
        + " and pt.$readableSimpleClientCriteria "//
        + " and (p.$incrementalUpdateCriteria) " //
        + " and " + Product.getPackProductWhereClause()//
        + " group by pt.id, pt.commercialName, pt.obposImage ";
    return hqlQuery;
    // @formatter:on
  }

  @Override
  protected boolean bypassPreferenceCheck() {
    return true;
  }

  @Override
  public List<String> getMasterDataModelProperties() {
    return ModelExtensionUtils.getPropertyExtensions(extensions)
        .getProperties()
        .stream()
        .map(HQLProperty::getHqlProperty)
        .collect(Collectors.toList());
  }
}
