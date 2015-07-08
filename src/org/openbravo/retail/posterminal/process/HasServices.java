/*
 ************************************************************************************
 * Copyright (C) 2015 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.retail.posterminal.process;

import java.io.StringWriter;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletException;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.Query;
import org.hibernate.Session;
import org.openbravo.base.weld.WeldUtils;
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
      JSONObject request = createRequestForProduct(jsonData, "Services_Filter");
      String productId = request.getString("product");
      String productCategoryId = request.getString("productCategory");
      StringWriter queryWriter = new StringWriter();
      Organization terminalOrganization = (OBDal.getInstance().get(OBPOSApplications.class,
          request.getString("pos"))).getOrganization();

      Date terminalDate = OBMOBCUtils.calculateServerDate(
          request.getJSONObject("parameters").getString("terminalTime"),
          request.getJSONObject("parameters").getJSONObject("terminalTimeOffset").getLong("value"));

      PriceListVersion priceListVersion = POSUtils
          .getPriceListVersionByOrgId((String) DalUtil.getId(terminalOrganization), terminalDate);

      final StringBuilder hqlString = new StringBuilder();

      hqlString.append("select count(*), s.obposProposalType ");
      hqlString.append("from OBRETCO_Prol_Product as assort left outer join assort.product as s ");
      hqlString.append("where s.productType = 'S'  and s.linkedToProduct = true ");
      hqlString.append("and s.$orgCriteria and s.$activeCriteria ");
      hqlString
          .append("and assort.obretcoProductlist.id = '"
              + DalUtil.getId(
                  POSUtils.getProductListByOrgId((String) DalUtil.getId(terminalOrganization)))
          + "' ");
      hqlString
          .append("and exists (select 1 from PricingProductPrice as ppp where ppp.product.id = '"
              + productId + "' and ppp.priceListVersion.id= '" + DalUtil.getId(priceListVersion)
              + "' and ppp.$activeCriteria ) ");
      hqlString.append("and ((s.includedProducts = 'Y' and ");
      hqlString.append(
          "not exists (select 1 from ServiceProduct sp where s = sp.product and sp.$activeCriteria  and sp.relatedProduct.id = '"
              + productId + "')) ");
      hqlString.append(
          "or (s.includedProducts = 'N' and exists (select 1 from ServiceProduct sp where s = sp.product and sp.$activeCriteria and sp.relatedProduct.id = '"
              + productId + "')) ");
      hqlString.append("or s.includedProducts is null) ");
      hqlString.append("and ((s.includedProductCategories = 'Y' and ");
      hqlString.append(
          "not exists (select 1 from ServiceProductCategory spc where s = spc.product and spc.$activeCriteria and spc.productCategory.id = '"
              + productCategoryId + "')) ");
      hqlString.append(
          "or (s.includedProductCategories = 'N' and exists (select 1 from ServiceProductCategory spc where s = spc.product and spc.$activeCriteria and spc.productCategory.id = '"
              + productCategoryId + "')) ");
      hqlString.append("or s.includedProductCategories is null) ");
      hqlString.append("group by s.obposProposalType ");

      SimpleQueryBuilder querybuilder = new SimpleQueryBuilder(hqlString.toString(),
          OBContext.getOBContext().getCurrentClient().getId(),
          (String) DalUtil.getId(terminalOrganization), null, null, null);

      final Session session = OBDal.getInstance().getSession();
      final Query query = session.createQuery(querybuilder.getHQLQuery());

      List<?> services = query.list();
      if (services.size() > 0) {
        for (Object resultObject : services) {
          Object[] serviceLine = (Object[]) resultObject;
          if ("MP".equals(serviceLine[1])) {
            org.openbravo.retail.posterminal.master.Product product = WeldUtils
                .getInstanceFromStaticBeanManager(
                    org.openbravo.retail.posterminal.master.Product.class);
            product.exec(queryWriter, request);
            data.put("mandatoryservices",
                new JSONObject("{" + queryWriter.toString() + "}").get("data"));
            data.put("hasservices", true);
            result.put("data", data);
            break;
          } else {
            data.put("hasservices", true);
            result.put("data", data);
          }
        }
      } else {
        data.put("hasservices", false);
        result.put("data", data);
      }
      result.put("status", JsonConstants.RPCREQUEST_STATUS_SUCCESS);

    } catch (Exception e) {
      result.put("status", JsonConstants.RPCREQUEST_STATUS_FAILURE);
      // throw new OBException();
    } finally {
      OBContext.restorePreviousMode();
    }
    return result;
  }

  private JSONObject createRequestForProduct(JSONObject requesObject, String filterName) {
    try {
      JSONArray arrayParams = new JSONArray();
      arrayParams.put(requesObject.getString("product"));
      arrayParams.put(requesObject.getString("productCategory"));

      JSONObject timeParameters = requesObject.getJSONObject("parameters");
      JSONObject terminalTimeOffset = new JSONObject();
      Long offset = timeParameters.getLong("terminalTimeOffset");
      terminalTimeOffset.put("value", offset);
      terminalTimeOffset.put("type", "long");
      timeParameters.put("terminalTimeOffset", terminalTimeOffset);
      requesObject.put("parameters", timeParameters);

      JSONObject filter = new JSONObject();
      filter.put("columns", new JSONArray());
      filter.put("operator", "filter");
      filter.put("value", filterName);
      filter.put("params", arrayParams);

      JSONArray hgVolFilters = new JSONArray();
      hgVolFilters.put(filter);
      requesObject.put("hgVolFilters", hgVolFilters);

      requesObject.put("_limit", 101);
    } catch (JSONException e) {
    }
    return requesObject;
  }
}