/*
 ************************************************************************************
 * Copyright (C) 2013 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.retail.posterminal.master;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.function.SQLFunction;
import org.hibernate.dialect.function.StandardSQLFunction;
import org.hibernate.impl.SessionFactoryImpl;
import org.hibernate.impl.SessionImpl;
import org.hibernate.type.StringType;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.client.kernel.ComponentProvider.Qualifier;
import org.openbravo.client.kernel.RequestContext;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.businessUtility.Preferences;
import org.openbravo.erpCommon.utility.PropertyException;
import org.openbravo.mobile.core.model.HQLProperty;
import org.openbravo.mobile.core.model.ModelExtension;
import org.openbravo.model.pricing.pricelist.ProductPrice;
import org.openbravo.retail.posterminal.OBPOSApplications;
import org.openbravo.retail.posterminal.POSUtils;

@Qualifier(Product.productPropertyExtension)
public class ProductProperties extends ModelExtension {

  public static final Logger log = Logger.getLogger(ProductProperties.class);

  @Override
  public List<HQLProperty> getHQLProperties(Object params) {

    // Calculate POS Precision
    String localPosPrecision = "";
    try {
      if (params != null) {
        @SuppressWarnings("unchecked")
        HashMap<String, Object> localParams = (HashMap<String, Object>) params;
        localPosPrecision = (String) localParams.get("posPrecision");
      }
    } catch (Exception e) {
      log.error("Error getting posPrecision: " + e.getMessage(), e);
    }
    final String posPrecision = localPosPrecision;

    // Build Product Tax Category select clause
    final Dialect dialect = ((SessionFactoryImpl) ((SessionImpl) OBDal.getInstance().getSession())
        .getSessionFactory()).getDialect();
    Map<String, SQLFunction> function = dialect.getFunctions();
    if (!function.containsKey("c_get_product_taxcategory")) {
      dialect.getFunctions().put("c_get_product_taxcategory",
          new StandardSQLFunction("c_get_product_taxcategory", new StringType()));
    }
    OBPOSApplications posDetail;
    posDetail = POSUtils.getTerminalById(RequestContext.get().getSessionAttribute("POSTerminal")
        .toString());
    if (posDetail == null) {
      throw new OBException("terminal id is not present in session ");
    }
    StringBuffer taxCategoryQry = new StringBuffer();
    taxCategoryQry.append("c_get_product_taxcategory(product.id, '");
    taxCategoryQry.append(posDetail.getOrganization().getId());
    // Date, shipfrom and shipto as null
    taxCategoryQry.append("', null, null, null)");
    final String strTaxCategoryQry = taxCategoryQry.toString();

    ArrayList<HQLProperty> list = new ArrayList<HQLProperty>() {
      private static final long serialVersionUID = 1L;
      {
        String trlName;
        if (OBContext.hasTranslationInstalled()) {
          trlName = "coalesce((select pt.name from ProductTrl AS pt where pt.language='"
              + OBContext.getOBContext().getLanguage().getLanguage()
              + "'  and pt.product=product), product.name)";
        } else {
          trlName = "product.name";
        }

        add(new HQLProperty("product.id", "id"));
        add(new HQLProperty("product.searchKey", "searchkey"));
        add(new HQLProperty(trlName, "_identifier"));
        add(new HQLProperty(strTaxCategoryQry, "taxCategory"));
        add(new HQLProperty("product.productCategory.id", "productCategory"));
        add(new HQLProperty("product.obposScale", "obposScale"));
        add(new HQLProperty("product.uOM.id", "uOM"));
        add(new HQLProperty("product.uOM.symbol", "uOMsymbol"));
        add(new HQLProperty("product.uPCEAN", "uPCEAN"));
        try {
          if ("Y".equals(Preferences.getPreferenceValue("OBPOS_retail.productImages", true,
              OBContext.getOBContext().getCurrentClient(), OBContext.getOBContext()
                  .getCurrentOrganization(), OBContext.getOBContext().getUser(), OBContext
                  .getOBContext().getRole(), null))) {
          } else {
            add(new HQLProperty("img.bindaryData", "img"));
          }
        } catch (PropertyException e) {
          add(new HQLProperty("img.bindaryData", "img"));
        }
        add(new HQLProperty("product.description", "description"));
        add(new HQLProperty("product.obposGroupedproduct", "groupProduct"));
        add(new HQLProperty("product.stocked", "stocked"));
        add(new HQLProperty("product.obposShowstock", "showstock"));
        add(new HQLProperty("product.isGeneric", "isGeneric"));
        add(new HQLProperty("product.genericProduct.id", "generic_product_id"));
        add(new HQLProperty("product.brand.id", "brand"));
        add(new HQLProperty("product.characteristicDescription", "characteristicDescription"));
        add(new HQLProperty("product.obposShowChDesc", "showchdesc"));
        add(new HQLProperty("pli.bestseller", "bestseller"));
        add(new HQLProperty("'false'", "ispack"));
        if (posPrecision != null && !"".equals(posPrecision)) {
          add(new HQLProperty("round(ppp.listPrice, " + posPrecision + ")", "listPrice"));
        } else {
          add(new HQLProperty("ppp.listPrice", "listPrice"));
        }
        if (posPrecision != null && !"".equals(posPrecision)) {
          add(new HQLProperty("round(ppp.standardPrice, " + posPrecision + ")", "standardPrice"));
        } else {
          add(new HQLProperty("ppp.standardPrice", "standardPrice"));
        }

        add(new HQLProperty("ppp.priceLimit", "priceLimit"));
        add(new HQLProperty("ppp.cost", "cost"));
        Entity ProductPrice = ModelProvider.getInstance().getEntity(ProductPrice.class);
        if (ProductPrice.hasProperty("algorithm") == true) {
          add(new HQLProperty("ppp.algorithm", "algorithm"));
        }
        add(new HQLProperty("product.active", "active"));
      }
    };
    return list;
  }
}
