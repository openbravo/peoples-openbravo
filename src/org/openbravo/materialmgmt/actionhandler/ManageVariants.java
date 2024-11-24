/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.0  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html
 * Software distributed under the License  is  distributed  on  an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific  language  governing  rights  and  limitations
 * under the License.
 * The Original Code is Openbravo ERP.
 * The Initial Developer of the Original Code is Openbravo SLU
 * All portions are Copyright (C) 2013-2024 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 *************************************************************************
 */
package org.openbravo.materialmgmt.actionhandler;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.client.application.process.BaseProcessActionHandler;
import org.openbravo.dal.core.DalUtil;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBDao;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.materialmgmt.VariantChDescUpdateProcess;
import org.openbravo.materialmgmt.utility.EANUtility;
import org.openbravo.model.ad.utility.Image;
import org.openbravo.model.common.plm.Characteristic;
import org.openbravo.model.common.plm.CharacteristicValue;
import org.openbravo.model.common.plm.Product;
import org.openbravo.model.common.plm.ProductAccounts;
import org.openbravo.model.common.plm.ProductCharacteristic;
import org.openbravo.model.common.plm.ProductCharacteristicConf;
import org.openbravo.model.common.plm.ProductCharacteristicValue;
import org.openbravo.model.pricing.pricelist.ProductPrice;
import org.openbravo.service.db.DbUtility;

public class ManageVariants extends BaseProcessActionHandler {
  final static private Logger log = LogManager.getLogger();
  private static final String SALES_PRICELIST = "SALES";
  private static final String PURCHASE_PRICELIST = "PURCHASE";

  @Override
  protected JSONObject doExecute(Map<String, Object> parameters, String content) {
    JSONObject jsonRequest = null;
    List<String> warnProducts = new ArrayList<String>();
    String severity = "success";
    String title = OBMessageUtils.messageBD("Success");
    OBContext.setAdminMode(true);
    try {
      jsonRequest = new JSONObject(content);
      JSONArray selection = jsonRequest.getJSONObject("_params")
          .getJSONObject("grid")
          .getJSONArray("_selection");
      String strProductId = jsonRequest.getString("M_Product_ID");
      final Product generic = OBDal.getInstance().get(Product.class, strProductId);
      log.debug("{}", jsonRequest);

      for (int i = 0; i < selection.length(); i++) {
        JSONObject row = selection.getJSONObject(i);
        boolean isVariantCreated = row.getBoolean("variantCreated");
        if (!isVariantCreated) {
          createVariant(row, generic);
        } else {
          Optional.ofNullable(updateVariant(row)).ifPresent(warnProducts::add);
        }
      }

      Map<String, String> map = new HashMap<String, String>();
      map.put("productNumer", Integer.toString(0));
      String text = OBMessageUtils.parseTranslation(title, map);

      if (!warnProducts.isEmpty()) {
        String prodList = "";
        severity = "warning";
        title = OBMessageUtils.messageBD("Warning");

        for (String prod : warnProducts) {
          prodList += prod + ", ";
        }
        prodList = prodList.substring(0, prodList.length() - 2);
        text = OBMessageUtils.getI18NMessage("ProductVariantEanAlreadyDefined",
            new String[] { prodList });
      }

      JSONObject msg = new JSONObject();
      msg.put("severity", severity);
      msg.put("title", title);
      msg.put("text", text);
      jsonRequest.put("message", msg);

    } catch (

    Exception e) {
      log.error("Error in Manage Variants Action Handler", e);

      try {
        OBDal.getInstance().rollbackAndClose();
        jsonRequest = new JSONObject();
        Throwable ex = DbUtility.getUnderlyingSQLException(e);
        String message = OBMessageUtils.translateError(ex.getMessage()).getMessage();
        JSONObject errorMessage = new JSONObject();
        errorMessage.put("severity", "error");
        errorMessage.put("text", message);
        jsonRequest.put("message", errorMessage);
      } catch (Exception e2) {
        log.error(e.getMessage(), e2);
      }
    } finally {
      OBContext.restorePreviousMode();
    }
    return jsonRequest;
  }

  private void createVariant(JSONObject variantProperties, Product generic) throws JSONException {
    Product variant = (Product) DalUtil.copy(generic);
    if (generic.getClient().isMultilingualDocuments()) {
      variant.getProductTrlList().clear();
    }
    variant.setCreationDate(new Date());
    variant.setGenericProduct(generic);
    variant.setProductAccountsList(Collections.<ProductAccounts> emptyList());
    variant.setGeneric(false);
    for (ProductCharacteristic prCh : variant.getProductCharacteristicList()) {
      prCh.setProductCharacteristicConfList(Collections.<ProductCharacteristicConf> emptyList());
    }
    variant.setName(variantProperties.getString("name"));
    variant.setSearchKey(variantProperties.getString("searchKey"));
    if (generic.isGenerateVariantEAN()) {
      variant.setUPCEAN(EANUtility.generateEAN(generic.getSequence()));
    }

    JSONArray variantValues = variantProperties.getJSONArray("characteristicArray");
    OBDal.getInstance().save(variant);
    OBDal.getInstance().flush();
    for (int i = 0; i < variantValues.length(); i++) {
      JSONObject chValue = variantValues.getJSONObject(i);
      ProductCharacteristicValue newPrChValue = OBProvider.getInstance()
          .get(ProductCharacteristicValue.class);
      newPrChValue.setCharacteristic((Characteristic) OBDal.getInstance()
          .getProxy(Characteristic.ENTITY_NAME, chValue.getString("characteristic")));
      newPrChValue.setCharacteristicValue((CharacteristicValue) OBDal.getInstance()
          .getProxy(CharacteristicValue.ENTITY_NAME, chValue.getString("characteristicValue")));
      newPrChValue.setProduct(variant);
      OBDal.getInstance().save(newPrChValue);
      ProductCharacteristicConf prChConf = OBDal.getInstance()
          .get(ProductCharacteristicConf.class, chValue.getString("characteristicConf"));
      if (prChConf.getCharacteristicOfProduct().isDefinesPrice()
          && prChConf.getNetUnitPrice() != null) {
        setPrice(variant, prChConf.getNetUnitPrice(),
            prChConf.getCharacteristicOfProduct().getPriceListType());
      }
      if (prChConf.getCharacteristicOfProduct().isDefinesImage() && prChConf.getImage() != null) {
        Image newImage = (Image) DalUtil.copy(prChConf.getImage(), false);
        OBDal.getInstance().save(newImage);
        variant.setImage(newImage);
      }
    }
    OBDal.getInstance().save(variant);
    OBDal.getInstance().flush();
    new VariantChDescUpdateProcess().update(variant.getId(), null);

  }

  private String updateVariant(JSONObject variantProperties) throws JSONException {
    String warnProduct = null;
    final String strProductId = variantProperties.getString("variantId");
    Product variant = OBDal.getInstance().get(Product.class, strProductId);
    variant.setName(variantProperties.getString("name"));
    variant.setSearchKey(variantProperties.getString("searchKey"));
    Product generic = variant.getGenericProduct();
    if (generic.isGenerateVariantEAN()) {
      if (variant.getUPCEAN() == null || StringUtils.isEmpty(variant.getUPCEAN())) {
        variant.setUPCEAN(EANUtility.generateEAN(generic.getSequence()));
      } else {
        warnProduct = variant.getSearchKey();
      }
    }
    OBDal.getInstance().save(variant);

    return warnProduct;
  }

  private void setPrice(Product variant, BigDecimal price, String strPriceListType) {
    List<ProductPrice> prodPrices = OBDao.getActiveOBObjectList(variant,
        Product.PROPERTY_PRICINGPRODUCTPRICELIST);
    for (ProductPrice prodPrice : prodPrices) {
      boolean isSOPriceList = prodPrice.getPriceListVersion().getPriceList().isSalesPriceList();
      if (SALES_PRICELIST.equals(strPriceListType) && !isSOPriceList) {
        continue;
      } else if (PURCHASE_PRICELIST.equals(strPriceListType) && isSOPriceList) {
        continue;
      }
      prodPrice.setStandardPrice(price);
      prodPrice.setListPrice(price);
      prodPrice.setPriceLimit(price);
      OBDal.getInstance().save(prodPrice);
    }
  }
}
