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

import org.apache.log4j.Logger;
import org.openbravo.client.kernel.ComponentProvider.Qualifier;
import org.openbravo.dal.core.OBContext;
import org.openbravo.erpCommon.businessUtility.Preferences;
import org.openbravo.erpCommon.utility.PropertyException;
import org.openbravo.mobile.core.model.HQLProperty;
import org.openbravo.mobile.core.model.ModelExtension;

@Qualifier(Product.productDiscPropertyExtension)
public class ProductDiscProperties extends ModelExtension {

  public static final Logger log = Logger.getLogger(ProductDiscProperties.class);

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

    ArrayList<HQLProperty> list;
    try {
      list = new ArrayList<HQLProperty>() {
        private static final long serialVersionUID = 1L;
        {
          boolean isHgvol = "Y".equals(Preferences.getPreferenceValue("OBPOS_highVolume.customer",
              true, OBContext.getOBContext().getCurrentClient(), OBContext.getOBContext()
                  .getCurrentOrganization(), OBContext.getOBContext().getUser(), OBContext
                  .getOBContext().getRole(), null));
          String discountNameTrl;
          if (OBContext.hasTranslationInstalled() && !isHgvol) {
            discountNameTrl = "coalesce ((select pt.name from PricingAdjustmentTrl pt where pt.promotionDiscount=p and pt.language='"
                + OBContext.getOBContext().getLanguage().getLanguage() + "'), p.name) ";
          } else {
            discountNameTrl = "p.name";
          }

          add(new HQLProperty("p.id", "id"));
          add(new HQLProperty(discountNameTrl, "searchkey"));
          add(new HQLProperty(discountNameTrl, "_identifier"));
          add(new HQLProperty("round(p.obdiscPrice, " + posPrecision + ")", "listPrice"));
          add(new HQLProperty("round(p.obdiscPrice, " + posPrecision + ")", "standardPrice"));
          add(new HQLProperty("p.obdiscUpc", "uPCEAN"));
          add(new HQLProperty("img.bindaryData", "img"));
          add(new HQLProperty("'[[null]]'", "generic_product_id"));
          add(new HQLProperty("'false'", "showchdesc"));
          add(new HQLProperty("'true'", "ispack"));
          add(new HQLProperty("'false'", "isGeneric"));
          add(new HQLProperty("'false'", "stocked"));
        }
      };
      return list;
    } catch (PropertyException e) {
      log.error("Error getting preference: " + e.getMessage(), e);
    }
    return null;
  }
}
