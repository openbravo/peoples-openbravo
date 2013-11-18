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

package org.openbravo.client.kernel.reference;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.exception.OBException;
import org.openbravo.client.kernel.RequestContext;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.data.Sqlc;
import org.openbravo.model.ad.ui.Field;
import org.openbravo.model.common.plm.Product;
import org.openbravo.model.common.plm.ProductCharacteristicValue;

public class CharacteristicsUIDefinition extends TextUIDefinition {

  @Override
  public String getFormEditorType() {
    return "OBCharacteristicsItem";
  }

  @Override
  public String getFilterEditorType() {
    return "OBCharacteristicsFilterItem";
  }

  @Override
  public String getGridEditorType() {
    return "OBCharacteristicsGridItem";
  }

  @Override
  public String getFieldProperties(Field field, boolean getValueFromSession) {
    String result = super.getFieldProperties(field, getValueFromSession);
    OBContext.setAdminMode(true);
    try {
      JSONObject jsnobject = new JSONObject(result);

      RequestContext rq = RequestContext.get();
      String columnValue = rq.getRequestParameter("inp"
          + Sqlc.TransformaNombreColumna(field.getColumn().getDBColumnName()));
      if (StringUtils.isEmpty(columnValue)) {
        return result;
      }
      String productId = rq.getRequestParameter("inpmProductId");
      Product product = null;
      if (!StringUtils.isEmpty(productId)) {
        product = OBDal.getInstance().get(Product.class, productId);
      }
      if (product == null) {
        return result;
      }

      JSONObject value = new JSONObject();
      value.put("dbValue", columnValue);

      JSONObject jsonValue = new JSONObject();
      for (ProductCharacteristicValue charValue : product.getProductCharacteristicValueList()) {
        jsonValue.put(charValue.getCharacteristic().getIdentifier(), charValue
            .getCharacteristicValue().getIdentifier());
      }

      value.put("characteristics", jsonValue);

      jsnobject.put("value", value);
      jsnobject.put("classicValue", columnValue);
      return jsnobject.toString();
    } catch (JSONException e) {
      throw new OBException("Exception when parsing date ", e);
    } finally {
      OBContext.restorePreviousMode();
    }
  }

}
