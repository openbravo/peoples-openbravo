/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.1  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html 
 * Software distributed under the License  is  distributed  on  an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific  language  governing  rights  and  limitations
 * under the License. 
 * The Original Code is Openbravo ERP. 
 * The Initial Developer of the Original Code is Openbravo SLU 
 * All portions are Copyright (C) 2013 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.materialmgmt.actionhandler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.client.application.process.BaseProcessActionHandler;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.materialmgmt.CharacteristicsUtils;
import org.openbravo.model.common.plm.Characteristic;
import org.openbravo.model.common.plm.CharacteristicValue;
import org.openbravo.model.common.plm.Product;
import org.openbravo.model.common.plm.ProductCharacteristic;
import org.openbravo.model.common.plm.ProductCharacteristicValue;
import org.openbravo.service.db.DbUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AddProductsToChValue extends BaseProcessActionHandler {
  final static private Logger log = LoggerFactory.getLogger(AddProductsToChValue.class);

  @Override
  protected JSONObject doExecute(Map<String, Object> parameters, String content) {
    JSONObject jsonRequest = null;
    OBContext.setAdminMode(true);
    try {
      jsonRequest = new JSONObject(content);
      JSONObject params = jsonRequest.getJSONObject("_params");
      log.debug("{}", jsonRequest);
      final String strChValueId = jsonRequest.getString("inpmChValueId");
      CharacteristicValue chValue = OBDal.getInstance()
          .get(CharacteristicValue.class, strChValueId);

      JSONArray productIds = params.getJSONArray("M_Product_ID");
      int count = addProducts(chValue, productIds);

      Map<String, String> map = new HashMap<String, String>();
      map.put("productNumer", Integer.toString(count));
      map.put("chValueName", chValue.getName());

      String messageText = OBMessageUtils.messageBD("AddProductsResult");
      JSONObject msg = new JSONObject();
      msg.put("severity", "success");
      msg.put("text", OBMessageUtils.parseTranslation(messageText, map));
      jsonRequest.put("message", msg);

    } catch (Exception e) {
      log.error("Error in Add Products to Ch Value Action Handler", e);

      try {
        jsonRequest = new JSONObject();
        Throwable ex = DbUtility.getUnderlyingSQLException(e);
        String message = OBMessageUtils.translateError(ex.getMessage()).getMessage();
        JSONObject errorMessage = new JSONObject();
        errorMessage.put("severity", "error");
        errorMessage.put("text", message);
        jsonRequest.put("message", errorMessage);
      } catch (Exception e2) {
        log.error(e.getMessage(), e2);
        // do nothing, give up
      }
    } finally {
      OBContext.restorePreviousMode();
    }
    return jsonRequest;
  }

  private int addProducts(CharacteristicValue chValue, JSONArray productIds) throws JSONException {
    int count = 0;
    for (int i = 0; i < productIds.length(); i++) {
      final String strProductId = productIds.getString(i);
      final Product product = OBDal.getInstance().get(Product.class, strProductId);
      if (CharacteristicsUtils.getCharacteristicValue(product, chValue.getCharacteristic()) != null) {
        // The product already has a value the characteristic, skip it.
        continue;
      }
      ProductCharacteristicValue newPrChValue = OBProvider.getInstance().get(
          ProductCharacteristicValue.class);
      newPrChValue.setCharacteristic(chValue.getCharacteristic());
      newPrChValue.setCharacteristicValue(chValue);
      newPrChValue.setProduct(product);
      newPrChValue.setOrganization(product.getOrganization());
      OBDal.getInstance().save(newPrChValue);

      if (doesNotHaveCharacteristic(product, chValue.getCharacteristic())) {
        ProductCharacteristic newPrCh = OBProvider.getInstance().get(ProductCharacteristic.class);
        newPrCh.setProduct(product);
        newPrCh.setCharacteristic(chValue.getCharacteristic());
        newPrCh.setOrganization(product.getOrganization());
        newPrCh.setVariant(false);
        newPrCh.setSequenceNumber((product.getProductCharacteristicList().size() + 1) * 10L);
        OBDal.getInstance().save(newPrCh);
      }

      if (product.isGeneric()) {
        List<Product> variants = product.getProductGenericProductList();
        JSONArray variantsArray = new JSONArray();
        for (Product variant : variants) {
          variantsArray.put(variant.getId());
        }
        count += addProducts(chValue, variantsArray);
      }

      count++;
    }
    return count;
  }

  private boolean doesNotHaveCharacteristic(Product product, Characteristic characteristic) {
    OBCriteria<ProductCharacteristic> prChCrit = OBDal.getInstance().createCriteria(
        ProductCharacteristic.class);
    prChCrit.add(Restrictions.eq(ProductCharacteristic.PROPERTY_CHARACTERISTIC, characteristic));
    prChCrit.add(Restrictions.eq(ProductCharacteristic.PROPERTY_PRODUCT, product));
    prChCrit.setFilterOnReadableOrganization(false);
    prChCrit.setFilterOnActive(false);
    return prChCrit.count() == 0;
  }
}
