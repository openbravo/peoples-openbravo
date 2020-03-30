/*
 ************************************************************************************
 * Copyright (C) 2013-2019 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.retail.posterminal.master;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.client.kernel.ComponentProvider.Qualifier;
import org.openbravo.dal.core.OBContext;
import org.openbravo.erpCommon.businessUtility.Preferences;
import org.openbravo.erpCommon.utility.PropertyException;
import org.openbravo.mobile.core.model.HQLProperty;
import org.openbravo.mobile.core.model.ModelExtension;
import org.openbravo.model.pricing.pricelist.ProductPrice;

@Qualifier(Product.productPropertyExtension)
public class ProductProperties extends ModelExtension {

  private static final Logger log = LogManager.getLogger();

  @Override
  public List<HQLProperty> getHQLProperties(final Object params) {
    final List<HQLProperty> list = ProductProperties.getMainProductHQLProperties(params);
    final boolean crossStore = (params == null) ? false : (boolean) getParam(params, "crossStore");

    list.addAll(new ArrayList<HQLProperty>() {
      private static final long serialVersionUID = 1L;
      {
        try {
          if ("Y".equals(Preferences.getPreferenceValue("OBPOS_retail.productImages", true,
              OBContext.getOBContext().getCurrentClient(),
              OBContext.getOBContext().getCurrentOrganization(), OBContext.getOBContext().getUser(),
              OBContext.getOBContext().getRole(), null))) {
            add(new HQLProperty("product.image.id", "imgId"));
          } else {
            add(new HQLProperty("img.bindaryData", "img"));
            add(new HQLProperty("img.id", "imgId"));
            add(new HQLProperty("img.mimetype", "mimetype"));
          }
        } catch (PropertyException e) {
          add(new HQLProperty("img.bindaryData", "img"));
          add(new HQLProperty("img.id", "imgId"));
          add(new HQLProperty("img.mimetype", "mimetype"));
        }
        if (crossStore) {
          add(new HQLProperty("false", "bestseller"));
          add(new HQLProperty("product.active", "active"));
        } else {
          add(new HQLProperty("pli.bestseller", "bestseller"));
          add(new HQLProperty("pli.productStatus.id", "productAssortmentStatus"));
          add(new HQLProperty("ppp.listPrice", "listPrice"));
          add(new HQLProperty("ppp.standardPrice", "standardPrice"));
          add(new HQLProperty("ppp.priceLimit", "priceLimit"));
          add(new HQLProperty("ppp.cost", "cost"));
          Entity productPrice = ModelProvider.getInstance().getEntity(ProductPrice.class);
          if (productPrice.hasProperty("algorithm")) {
            add(new HQLProperty("ppp.algorithm", "algorithm"));
          }
          add(new HQLProperty(
              "case when product.active = 'Y' and pli.active is not null then pli.active else product.active end",
              "active"));
        }
        add(new HQLProperty("product.obposEditablePrice", "obposEditablePrice"));
        add(new HQLProperty("'false'", "ispack"));
        add(new HQLProperty("case when product.attributeSet is not null then true else false end",
            "hasAttributes"));
        add(new HQLProperty("case when attrset.serialNo = 'Y' then true else false end",
            "isSerialNo"));
      }
    });

    return list;
  }

  public static List<HQLProperty> getMainProductHQLProperties(final Object params) {

    final boolean multiPriceList = (params == null) ? false
        : (boolean) getParam(params, "multiPriceList");
    final boolean crossStore = (params == null) ? false : (boolean) getParam(params, "crossStore");
    final boolean isRemote = (params == null) ? false
        : (boolean) getParam(params, "isRemoteSearch");

    ArrayList<HQLProperty> list = null;
    list = new ArrayList<HQLProperty>() {
      private static final long serialVersionUID = 1L;
      {
        String trlName;
        if (OBContext.hasTranslationInstalled() && !isRemote) {
          trlName = "coalesce((select pt.name from ProductTrl AS pt where pt.language='"
              + OBContext.getOBContext().getLanguage().getLanguage()
              + "'  and pt.product=product), product.name)";
        } else {
          trlName = "product.name";
        }
        add(new HQLProperty("product.id", "id"));
        add(new HQLProperty("product.organization.id", "organization"));
        add(new HQLProperty("product.searchKey", "searchkey"));
        add(new HQLProperty(trlName, "_identifier"));
        add(new HQLProperty("product.productCategory.id", "productCategory"));
        add(new HQLProperty("product.obposScale", "obposScale"));
        add(new HQLProperty("product.uOM.id", "uOM"));
        add(new HQLProperty("product.uOM.symbol", "uOMsymbol"));
        add(new HQLProperty("coalesce(product.uOM.standardPrecision)", "uOMstandardPrecision"));
        if (isRemote) {
          add(new HQLProperty("product.uPCEAN", "uPCEAN"));
        } else {
          add(new HQLProperty("upper(product.uPCEAN)", "uPCEAN"));
        }
        add(new HQLProperty("product.description", "description"));
        add(new HQLProperty("product.obposGroupedproduct", "groupProduct"));
        add(new HQLProperty("product.stocked", "stocked"));
        add(new HQLProperty("product.obposShowstock", "showstock"));
        add(new HQLProperty("product.isGeneric", "isGeneric"));
        add(new HQLProperty("product.productStatus.id", "productStatus"));
        add(new HQLProperty("product.genericProduct.id", "generic_product_id"));
        add(new HQLProperty("product.characteristicDescription", "characteristicDescription"));
        add(new HQLProperty("product.characteristicDescription||','",
            "characteristicDescriptionSearch"));
        add(new HQLProperty("product.obposShowChDesc", "showchdesc"));
        add(new HQLProperty("product.productType", "productType"));
        add(new HQLProperty("product.includedProductCategories", "includeProductCategories"));
        add(new HQLProperty("product.includedProducts", "includeProducts"));
        add(new HQLProperty("product.printDescription", "printDescription"));
        add(new HQLProperty("product.oBPOSAllowAnonymousSale", "oBPOSAllowAnonymousSale"));
        add(new HQLProperty("product.returnable", "returnable"));
        add(new HQLProperty("product.overdueReturnDays", "overdueReturnDays"));
        add(new HQLProperty("product.ispricerulebased", "isPriceRuleBased"));
        add(new HQLProperty("product.obposProposalType", "proposalType"));
        add(new HQLProperty("product.obposIsmultiselectable", "availableForMultiline"));
        add(new HQLProperty("product.linkedToProduct", "isLinkedToProduct"));
        add(new HQLProperty("product.modifyTax", "modifyTax"));
        add(new HQLProperty("product.allowDeferredSell", "allowDeferredSell"));
        add(new HQLProperty("product.deferredSellMaxDays", "deferredSellMaxDays"));
        add(new HQLProperty("product.quantityRule", "quantityRule"));
        add(new HQLProperty("product.obposPrintservices", "isPrintServices"));
        add(new HQLProperty("product.weight", "weight"));
        if (multiPriceList && !crossStore) {
          add(new HQLProperty("pp.standardPrice", "currentStandardPrice"));
        }
        add(new HQLProperty(String.valueOf(crossStore), "crossStore"));
        add(new HQLProperty("product.obposMaxpriceassocprod", "obposMaxpriceassocprod"));
        add(new HQLProperty("product.obposMinpriceassocprod", "obposMinpriceassocprod"));
      }
    };

    list.add(new HQLProperty("product.taxCategory.id", "taxCategory", 0));

    return list;
  }

  protected static Object getParam(final Object params, final String paramName) {
    try {
      if (params != null) {
        @SuppressWarnings("unchecked")
        final HashMap<String, Object> localParams = (HashMap<String, Object>) params;
        return localParams.containsKey(paramName) ? localParams.get(paramName) : false;
      }
    } catch (Exception e) {
      log.error("Error getting params: " + e.getMessage(), e);
    }
    return null;
  }
}
