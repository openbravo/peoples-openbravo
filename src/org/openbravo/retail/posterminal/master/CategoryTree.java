/*
 ************************************************************************************
 * Copyright (C) 2015 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.retail.posterminal.master;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.client.kernel.ComponentProvider.Qualifier;
import org.openbravo.dal.core.OBContext;
import org.openbravo.mobile.core.model.HQLPropertyList;
import org.openbravo.mobile.core.model.ModelExtension;
import org.openbravo.mobile.core.model.ModelExtensionUtils;
import org.openbravo.retail.posterminal.ProcessHQLQuery;

public class CategoryTree extends ProcessHQLQuery {
  public static final String productCategoryTreePropertyExtension = "OBPOS_ProductCategoryTreeExtension";
  public static final String productCategoryTableId = "209";
  public static final Logger log = Logger.getLogger(CategoryTree.class);

  @Inject
  @Any
  @Qualifier(productCategoryTreePropertyExtension)
  private Instance<ModelExtension> extensions;

  @Override
  protected List<String> getQuery(JSONObject jsonsent) throws JSONException {

    String orgId = OBContext.getOBContext().getCurrentOrganization().getId();

    SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd");
    Calendar now = Calendar.getInstance();

    List<String> hqlQueries = new ArrayList<String>();

    HQLPropertyList regularProductsCategoriesTreeHQLProperties = ModelExtensionUtils
        .getPropertyExtensions(extensions);

    hqlQueries
        .add("select"
            + regularProductsCategoriesTreeHQLProperties.getHqlSelect() //
            + "from ADTreeNode tn, ProductCategory pc "
            + "where tn.$incrementalUpdateCriteria and tn.$naturalOrgCriteria and tn.$readableSimpleClientCriteria "
            + " and tn.node = pc.id and tn.tree.table.id = '" + productCategoryTableId + "' "
            + "order by tn.sequenceNumber");

    // Discounts marked as category
    hqlQueries.add("select pt.id as id, pt.id as categoryId, '0' as parentId, 999999999 as seqNo "
        + "from PromotionType as pt left outer join pt.obposImage img " //
        + "where pt.obposIsCategory = true "//
        + "  and pt.$readableSimpleClientCriteria" //
        + "  and (pt.$incrementalUpdateCriteria)"//
        + "  and exists (select 1"//
        + "                from PricingAdjustment p " //
        + "               where p.discountType.active = true " //
        + "                 and p.active = true"//
        + "                 and p.discountType = pt"//
        + "                 and (p.endingDate is null or p.endingDate >= TO_DATE('"
        + format.format(now.getTime())
        + "','yyyy/MM/dd'))" //
        + "                 and p.startingDate <= TO_DATE('"
        + format.format(now.getTime())
        + "', 'yyyy/MM/dd')"
        // organization
        + "and ((p.includedOrganizations='Y' "
        + "  and not exists (select 1 "
        + "         from PricingAdjustmentOrganization o"
        + "        where active = true"
        + "          and o.priceAdjustment = p"
        + "          and o.organization.id ='"
        + orgId
        + "')) "
        + "   or (p.includedOrganizations='N' "
        + "  and  exists (select 1 "
        + "         from PricingAdjustmentOrganization o"
        + "        where active = true"
        + "          and o.priceAdjustment = p"
        + "          and o.organization.id ='"
        + orgId
        + "')) " //
        + "    ) "//
        + ")");

    return hqlQueries;
  }

  @Override
  protected boolean bypassPreferenceCheck() {
    return true;
  }
}