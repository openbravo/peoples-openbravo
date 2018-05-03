/*
 ************************************************************************************
 * Copyright (C) 2013-2018 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.retail.posterminal.master;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.client.kernel.ComponentProvider.Qualifier;
import org.openbravo.dal.core.OBContext;
import org.openbravo.erpCommon.businessUtility.Preferences;
import org.openbravo.erpCommon.utility.PropertyException;
import org.openbravo.erpCommon.utility.PropertyNotFoundException;
import org.openbravo.mobile.core.model.HQLProperty;
import org.openbravo.mobile.core.model.ModelExtension;
import org.openbravo.model.pricing.pricelist.ProductPrice;
import org.openbravo.retail.posterminal.OBPOSApplications;
import org.openbravo.retail.posterminal.POSUtils;

@Qualifier(Product.productPropertyExtension)
public class ProductProperties extends ModelExtension {

  private static final Logger log = Logger.getLogger(ProductProperties.class);

  @Override
  public List<HQLProperty> getHQLProperties(Object params) {

    List<HQLProperty> list = ProductProperties.getMainProductHQLProperties(params);

    list.addAll(new ArrayList<HQLProperty>() {
      private static final long serialVersionUID = 1L;
      {
        try {
          if ("Y".equals(Preferences.getPreferenceValue("OBPOS_retail.productImages", true,
              OBContext.getOBContext().getCurrentClient(), OBContext.getOBContext()
                  .getCurrentOrganization(), OBContext.getOBContext().getUser(), OBContext
                  .getOBContext().getRole(), null))) {
            add(new HQLProperty("product.image.id", "imgId"));
          } else {
            add(new HQLProperty("img.bindaryData", "img"));
            add(new HQLProperty("img.id", "imgId"));
          }
        } catch (PropertyException e) {
          add(new HQLProperty("img.bindaryData", "img"));
          add(new HQLProperty("img.id", "imgId"));
        }
        add(new HQLProperty("pli.bestseller", "bestseller"));
        add(new HQLProperty("'false'", "ispack"));
        add(new HQLProperty("ppp.listPrice", "listPrice"));
        add(new HQLProperty("ppp.standardPrice", "standardPrice"));
        add(new HQLProperty("ppp.priceLimit", "priceLimit"));
        add(new HQLProperty("ppp.cost", "cost"));
        Entity ProductPrice = ModelProvider.getInstance().getEntity(ProductPrice.class);
        if (ProductPrice.hasProperty("algorithm") == true) {
          add(new HQLProperty("ppp.algorithm", "algorithm"));
        }
        add(new HQLProperty(
            "case when product.active = 'Y' and pli.active is not null then pli.active else product.active end",
            "active"));
        add(new HQLProperty(
            "(select case when atri.id is not null then true else false end from Product as prod left join prod.attributeSet as atri where prod.id = product.id)",
            "hasAttributes"));
        add(new HQLProperty(
            "(select case when atri.serialNo = 'Y' then true else false end from Product as prod left join prod.attributeSet as atri where prod.id = product.id)",
            "isSerialNo"));
      }
    });

    return list;

  }

  public static List<HQLProperty> getMainProductHQLProperties(Object params) {

    OBPOSApplications posDetail = null;
    Boolean localmultiPriceList = false;
    try {
      if (params != null) {
        @SuppressWarnings("unchecked")
        HashMap<String, Object> localParams = (HashMap<String, Object>) params;
        localmultiPriceList = (Boolean) localParams.get("multiPriceList");
        posDetail = POSUtils.getTerminalById((String) localParams.get("terminalId"));
      }
    } catch (Exception e) {
      log.error("Error getting params: " + e.getMessage(), e);
    }
    final boolean multiPriceList = localmultiPriceList;

    if (posDetail == null) {
      throw new OBException("terminal id is not present in session ");
    }

    ArrayList<HQLProperty> list = null;
    try {
      list = new ArrayList<HQLProperty>() {
        private static final long serialVersionUID = 1L;
        {
          String trlName;
          try {
            boolean isRemote = "Y".equals(Preferences.getPreferenceValue("OBPOS_remote.product",
                true, OBContext.getOBContext().getCurrentClient(), OBContext.getOBContext()
                    .getCurrentOrganization(), OBContext.getOBContext().getUser(), OBContext
                    .getOBContext().getRole(), null));

            if (OBContext.hasTranslationInstalled() && !isRemote) {
              trlName = "coalesce((select pt.name from ProductTrl AS pt where pt.language='"
                  + OBContext.getOBContext().getLanguage().getLanguage()
                  + "'  and pt.product=product), product.name)";
            } else {
              trlName = "product.name";
            }
            add(new HQLProperty("product.id", "id"));
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
            add(new HQLProperty("product.genericProduct.id", "generic_product_id"));
            add(new HQLProperty("product.brand.id", "brand"));
            add(new HQLProperty("product.characteristicDescription", "characteristicDescription"));
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
            add(new HQLProperty("product.allowDeferredSell", "allowDeferredSell"));
            add(new HQLProperty("product.deferredSellMaxDays", "deferredSellMaxDays"));
            add(new HQLProperty("product.quantityRule", "quantityRule"));
            add(new HQLProperty("product.obposPrintservices", "isPrintServices"));
            if (multiPriceList) {
              add(new HQLProperty("pp.standardPrice", "currentStandardPrice"));
            }
          } catch (PropertyNotFoundException e) {
            if (OBContext.hasTranslationInstalled()) {
              trlName = "coalesce((select pt.name from ProductTrl AS pt where pt.language='"
                  + OBContext.getOBContext().getLanguage().getLanguage()
                  + "'  and pt.product=product), product.name)";
            } else {
              trlName = "product.name";
            }
          }
        }
      };

    } catch (PropertyException e) {
      log.error("Error getting preference: " + e.getMessage(), e);
    }

    // If preference for Product Tax Category exists, taxCategory will be fetched from
    // ProductProperties extension, otherwise we will just read the tax category from the product
    try {
      Preferences.getPreferenceValue("GETPRODUCTTAXCATEGORY", true, OBContext.getOBContext()
          .getCurrentClient(), OBContext.getOBContext().getCurrentOrganization(), OBContext
          .getOBContext().getUser(), OBContext.getOBContext().getRole(), null);
    } catch (PropertyException e) {
      list.add(new HQLProperty("product.taxCategory.id", "taxCategory"));
    }

    return list;
  }
}
