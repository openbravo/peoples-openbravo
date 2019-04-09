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
import java.util.HashSet;
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
import org.openbravo.erpCommon.businessUtility.Preferences;
import org.openbravo.erpCommon.utility.PropertyException;
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
      final String clientId = OBContext.getOBContext().getCurrentClient().getId();
      final String orgId = OBContext.getOBContext().getCurrentOrganization().getId();
      final String posId = jsonsent.getString("pos");

      Set<String> productListIds = new HashSet<>();
      productListIds = POSUtils.getProductListCrossStore(posId);
      final List<String> allCrossStoreOrgIds = POSUtils.getOrgListCrossStore(posId);
      final List<String> crossStoreOrgIds = new ArrayList<>(allCrossStoreOrgIds);
      crossStoreOrgIds.remove(orgId);
      Set<String> crossStoreNaturalTree = OBContext.getOBContext()
          .getOrganizationStructureProvider(OBContext.getOBContext().getCurrentClient().getId())
          .getNaturalTree(orgId);
      crossStoreNaturalTree.addAll(crossStoreOrgIds);

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

      if (isRemote) {
        paramValues.put("productCategoryTableId", CategoryTree.productCategoryTableId);
      }
      final Date terminalDate = OBMOBCUtils.calculateServerDate(
          jsonsent.getJSONObject("parameters").getString("terminalTime"),
          jsonsent.getJSONObject("parameters")
              .getJSONObject("terminalTimeOffset")
              .getLong("value"));

      if (OBContext.hasTranslationInstalled()) {
        paramValues.put("languageId", OBContext.getOBContext().getLanguage().getId());
      }
      Calendar now = Calendar.getInstance();
      paramValues.put("endingDate", now.getTime());
      paramValues.put("startingDate", now.getTime());
      paramValues.put("clientId", clientId);
      paramValues.put("orgId", orgId);
      paramValues.put("allCrossStoreOrgIds", allCrossStoreOrgIds);
      paramValues.put("crossStoreNaturalTree", crossStoreNaturalTree);
      paramValues.put("terminalDate", terminalDate);
      paramValues.put("productListIds", productListIds);
      return paramValues;
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  @Override
  protected List<String> getQuery(JSONObject jsonsent) throws JSONException {

    List<String> hqlQueries = new ArrayList<String>();

    HQLPropertyList regularProductsCategoriesHQLProperties = ModelExtensionUtils
        .getPropertyExtensions(extensions);

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

    final Long lastUpdated = jsonsent != null && jsonsent.has("lastUpdated")
        && !jsonsent.get("lastUpdated").equals("undefined")
        && !jsonsent.get("lastUpdated").equals("null") ? jsonsent.getLong("lastUpdated") : null;

    if (isRemote) {
      StringBuilder query = new StringBuilder();
      query.append(" select");
      query.append(regularProductsCategoriesHQLProperties.getHqlSelect());
      query.append(" from ProductCategory as pCat");
      query.append(" left outer join pCat.image as img");
      query.append(" where exists (");
      query.append("   select 1");
      query.append("   from OBRETCO_Productcategory aCat");
      query.append("   where aCat.productCategory = pCat");
      query.append("   and aCat.$readableSimpleClientCriteria");
      query.append("   and aCat.organization.id in :crossStoreNaturalTree");
      query.append("   and aCat.obretcoProductlist.id in :productListIds");
      query.append("   and (aCat.$incrementalUpdateCriteria ");
      query.append(lastUpdated == null ? "and" : "or");
      query.append("   pCat.$incrementalUpdateCriteria)");
      query.append("   )");
      query.append(" order by pCat.name, pCat.id");

      hqlQueries.add(query.toString());

      hqlQueries.add("select" + regularProductsCategoriesHQLProperties.getHqlSelect() //
          + "from ADTreeNode tn, ProductCategory pCat left outer join pCat.image as img "
          + "where tn.$readableSimpleClientCriteria and tn.organization.id in :crossStoreNaturalTree "
          + " and tn.node = pCat.id and tn.tree.table.id = :productCategoryTableId "
          + " and pCat.summaryLevel = 'Y' "
          + " and not exists (select pc.id from OBRETCO_Productcategory pc where tn.node = pc.productCategory.id) "
          + " and (tn.$incrementalUpdateCriteria "//
          + (lastUpdated == null ? "and" : "or") //
          + " pCat.$incrementalUpdateCriteria) "//
          + " order by tn.sequenceNumber, tn.id");
    } else {
      StringBuilder query = new StringBuilder();
      query.append(" select");
      query.append(regularProductsCategoriesHQLProperties.getHqlSelect());
      query.append(" from ProductCategory as pCat");
      query.append(" left outer join pCat.image as img");
      query.append(" where (exists (");
      // Product exists in the assortment and price list of alls cross store
      query.append("     select 1");
      query.append("     from Organization o");
      query.append("     where o.id in :allCrossStoreOrgIds");
      query.append("     and exists (");
      query.append("       select 1");
      query.append("       from OBRETCO_Prol_Product pli");
      query.append("       where pli.product.productCategory.id = pCat.id");
      query.append("       and pli.obretcoProductlist.id = o.obretcoProductlist.id");
      query.append("     )");
      query.append("     and exists (");
      query.append("       select 1");
      query.append("       from PricingProductPrice ppp");
      query.append("       join ppp.priceListVersion plv");
      query.append("       where ppp.product.productCategory.id = pCat.id");
      query.append("       and plv.priceList.id = o.obretcoPricelist.id");
      query.append("       and plv.validFromDate <= :terminalDate");
      query.append("       )");
      query.append("     )");
      query.append("   or (");
      query.append("     pCat.summaryLevel = 'Y'");
      query.append("     and pCat.organization.id in :crossStoreNaturalTree");
      query.append("     and pCat.$readableSimpleClientCriteria");
      query.append("     )");
      query.append("   )");
      query.append(" and pCat.$incrementalUpdateCriteria");
      query.append(" order by pCat.name, pCat.id");

      hqlQueries.add(query.toString());
    }
    String promoNameTrl;
    if (OBContext.hasTranslationInstalled()) {
      promoNameTrl = "coalesce ((select t.commercialName from PromotionTypeTrl t where t.discountPromotionType=pt and t.language.id= :languageId), pt.commercialName)";
    } else {
      promoNameTrl = "pt.commercialName";
    }

    // Discounts marked as category
    hqlQueries.add("select pt.id as id, " + promoNameTrl + " as searchKey, " + promoNameTrl
        + " as name, " + promoNameTrl + " as _identifier, "
        + "(select bindaryData from ADImage ai where ai = pt.obposImage) as img, "
        + "(case when (count(p.name) > 0 and exists (select 1 from PricingAdjustment p "
        + "where p.discountType = pt and p.active = true and " + getDiscountWhereClause() + ")) "
        + "then true else false end) as active, " + "'N' as realCategory " //
        + "from PromotionType pt inner join pt.pricingAdjustmentList p "
        + "where pt.active = true and pt.obposIsCategory = true "//
        + "and pt.$readableSimpleClientCriteria "//
        + "and (p.$incrementalUpdateCriteria) " //
        + "and " + getDiscountWhereClause()//
        + "group by pt.id, pt.commercialName, pt.obposImage");
    return hqlQueries;
  }

  private String getDiscountWhereClause() {
    StringBuilder whereClause = new StringBuilder();
    whereClause.append(" p.client.id = :clientId");
    whereClause.append(" and p.startingDate <= :startingDate");
    whereClause.append(" and (p.endingDate is null or p.endingDate >= :endingDate)");
    // assortment products
    whereClause.append(" and (");
    whereClause.append("   (");
    whereClause.append("    p.includedProducts = 'N'");
    whereClause.append("    and not exists (");
    whereClause.append("      select 1");
    whereClause.append("      from PricingAdjustmentProduct pap");
    whereClause.append("      where pap.active = true");
    whereClause.append("      and pap.priceAdjustment = p");
    whereClause.append("      and pap.product.sale = true");
    whereClause.append("      and pap.product");
    whereClause.append("      not in (");
    whereClause.append("        select ppl.product.id");
    whereClause.append("        from OBRETCO_Prol_Product ppl");
    whereClause.append("        where ppl.obretcoProductlist.id in :productListIds");
    whereClause.append("        and ppl.active = true");
    whereClause.append("      )");
    whereClause.append("    )");
    whereClause.append("   )");
    whereClause.append("   or (");
    whereClause.append("       p.includedProducts = 'Y'");
    whereClause.append("       and not exists (");
    whereClause.append("         select 1");
    whereClause.append("         from PricingAdjustmentProduct pap,");
    whereClause.append("         OBRETCO_Prol_Product ppl");
    whereClause.append("         where pap.active = true");
    whereClause.append("         and pap.priceAdjustment = p");
    whereClause.append("         and pap.product.id = ppl.product.id");
    whereClause.append("         and ppl.obretcoProductlist.id in :productListIds");
    whereClause.append("       )");
    whereClause.append("   )");
    whereClause.append(" )");
    // organization
    whereClause.append(" and (");
    whereClause.append("   (");
    whereClause.append("    p.includedOrganizations='Y'");
    whereClause.append("    and not exists (");
    whereClause.append("      select 1");
    whereClause.append("      from PricingAdjustmentOrganization o");
    whereClause.append("      where active = true");
    whereClause.append("      and o.priceAdjustment = p");
    whereClause.append("      and o.organization.id in :allCrossStoreOrgIds");
    whereClause.append("    )");
    whereClause.append("   )");
    whereClause.append("   or (");
    whereClause.append("       p.includedOrganizations='N'");
    whereClause.append("       and  exists (");
    whereClause.append("         select 1");
    whereClause.append("         from PricingAdjustmentOrganization o");
    whereClause.append("         where active = true");
    whereClause.append("         and o.priceAdjustment = p");
    whereClause.append("      and o.organization.id in :allCrossStoreOrgIds");
    whereClause.append("       )");
    whereClause.append("      )");
    whereClause.append(" )");

    return whereClause.toString();
  }

  @Override
  protected boolean bypassPreferenceCheck() {
    return true;
  }
}
