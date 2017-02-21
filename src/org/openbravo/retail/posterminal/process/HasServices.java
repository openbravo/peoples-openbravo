/*
 ************************************************************************************
 * Copyright (C) 2015-2017 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.retail.posterminal.process;

import java.util.Date;
import java.util.List;

import javax.servlet.ServletException;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.Query;
import org.openbravo.dal.core.DalUtil;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.mobile.core.process.SimpleQueryBuilder;
import org.openbravo.mobile.core.utils.OBMOBCUtils;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.pricing.pricelist.PriceListVersion;
import org.openbravo.retail.posterminal.JSONProcessSimple;
import org.openbravo.retail.posterminal.OBPOSApplications;
import org.openbravo.retail.posterminal.POSUtils;
import org.openbravo.service.json.JsonConstants;

public class HasServices extends JSONProcessSimple {
  @Override
  public JSONObject exec(JSONObject jsonData) throws JSONException, ServletException {
    OBContext.setAdminMode(true);
    JSONObject result = new JSONObject();
    JSONObject data = new JSONObject();

    try {
      String productId = jsonData.getString("product");
      String productCategoryId = jsonData.getString("productCategory");
      Organization terminalOrganization = (OBDal.getInstance().get(OBPOSApplications.class,
          jsonData.getString("pos"))).getOrganization();

      Date terminalDate = OBMOBCUtils.calculateServerDate(jsonData.getJSONObject("parameters")
          .getString("terminalTime"),
          jsonData.getJSONObject("parameters").getLong("terminalTimeOffset"));

      PriceListVersion priceListVersion = POSUtils.getPriceListVersionByOrgId(
          terminalOrganization.getId(), terminalDate);

      SimpleQueryBuilder querybuilder = new SimpleQueryBuilder(getProductServicesQuery(productId,
          productCategoryId, terminalOrganization, priceListVersion), OBContext.getOBContext()
          .getCurrentClient().getId(), (String) DalUtil.getId(terminalOrganization), null, null,
          null);

      final Query query = querybuilder.getDalQuery();
      query.setParameter("obretcoProductlistId",
          POSUtils.getProductListByOrgId(terminalOrganization.getId()).getId());
      if (productId != null && !("null".equals(productId))) {
        query.setParameter("productId", productId);
        query.setParameter("priceListVersionId", priceListVersion.getId());
        query.setParameter("productCategoryId", productCategoryId);
      }

      data.put("hasservices", false);
      result.put("data", data);

      List<?> services = query.list();
      if (services.size() > 0) {
        for (Object resultObject : services) {
          Object[] serviceLine = (Object[]) resultObject;
          if ("MP".equals(serviceLine[1])) {
            data.put("hasservices", true);
            data.put("hasmandatoryservices", true);
            result.put("data", data);
            break;
          } else if ("OP".equals(serviceLine[1])) {
            data.put("hasservices", true);
            result.put("data", data);
          }
        }
      }
      result.put("status", JsonConstants.RPCREQUEST_STATUS_SUCCESS);

    } catch (Exception e) {
      result.put("status", JsonConstants.RPCREQUEST_STATUS_FAILURE);
    } finally {
      OBContext.restorePreviousMode();
    }
    return result;
  }

  private String getProductServicesQuery(String productId, String productCategoryId,
      Organization terminalOrganization, PriceListVersion priceListVersion) {

    if (productId == null || "null".equals(productId)) {
      return getCategoryServicesQuery(productCategoryId);
    }

    final StringBuilder hqlString = new StringBuilder();

    hqlString.append("select count(*), s.obposProposalType ");
    hqlString.append("from OBRETCO_Prol_Product as assort left outer join assort.product as s ");
    hqlString.append("where s.productType = 'S'  and s.linkedToProduct = true ");
    hqlString.append("and s.$orgCriteria and s.$activeCriteria ");
    hqlString
        .append("and assort.obretcoProductlist.id = '"
            + DalUtil.getId(POSUtils.getProductListByOrgId((String) DalUtil
                .getId(terminalOrganization))) + "' ");
    hqlString
        .append("and exists (select 1 from PricingProductPrice as ppp where ppp.product.id = '"
            + productId + "' and ppp.priceListVersion.id= '" + DalUtil.getId(priceListVersion)
            + "' and ppp.$activeCriteria ) ");
    hqlString.append("and ((s.includedProducts = 'Y' and ");
    hqlString
        .append("not exists (select 1 from ServiceProduct sp where s = sp.product and sp.$activeCriteria  and sp.relatedProduct.id = '"
            + productId + "')) ");
    hqlString
        .append("or (s.includedProducts = 'N' and exists (select 1 from ServiceProduct sp where s = sp.product and sp.$activeCriteria and sp.relatedProduct.id = '"
            + productId + "')) ");
    hqlString.append("or s.includedProducts is null) ");
    hqlString.append("and ((s.includedProductCategories = 'Y' and ");
    hqlString
        .append("not exists (select 1 from ServiceProductCategory spc where s = spc.product and spc.$activeCriteria and spc.productCategory.id = '"
            + productCategoryId + "')) ");
    hqlString
        .append("or (s.includedProductCategories = 'N' and exists (select 1 from ServiceProductCategory spc where s = spc.product and spc.$activeCriteria and spc.productCategory.id = '"
            + productCategoryId + "')) ");
    hqlString.append("or s.includedProductCategories is null) ");
    hqlString.append("group by s.obposProposalType ");
    return hqlString.toString();
  }

  private String getCategoryServicesQuery(String productCategoryId) {
    final StringBuilder hqlString = new StringBuilder();

    hqlString.append("select count(*), s.obposProposalType ");
    hqlString.append("from OBRETCO_Prol_Product as assort left outer join assort.product as s ");
    hqlString.append("where s.productType = 'S'  and s.linkedToProduct = true ");
    hqlString.append("and s.$orgCriteria and s.$activeCriteria ");
    hqlString.append("and assort.obretcoProductlist.id =  :obretcoProductlistId ");
    hqlString
        .append("and ((s.includedProducts = 'Y' and not exists (select 1 from ServiceProduct sp where s = sp.product and sp.$activeCriteria))  or s.includedProducts is null)");
    hqlString
        .append("and ((s.includedProductCategories = 'N' and exists (select 1 from ServiceProductCategory spc where s = spc.product and spc.$activeCriteria and spc.productCategory.id = '"
            + productCategoryId + "'))) ");
    hqlString.append("group by s.obposProposalType ");
    return hqlString.toString();
  }

}