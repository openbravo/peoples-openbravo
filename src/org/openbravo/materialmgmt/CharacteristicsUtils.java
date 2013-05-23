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
 * All portions are Copyright (C) 2012 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 *************************************************************************
 */

package org.openbravo.materialmgmt;

import org.hibernate.criterion.Restrictions;
import org.openbravo.base.exception.OBException;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.common.plm.Characteristic;
import org.openbravo.model.common.plm.CharacteristicValue;
import org.openbravo.model.common.plm.Product;
import org.openbravo.model.common.plm.ProductCharacteristicValue;

public class CharacteristicsUtils {

  public static CharacteristicValue getCharacteristicValue(Product product,
      Characteristic characteristic) throws OBException {
    OBCriteria<ProductCharacteristicValue> obCriteria = OBDal.getInstance().createCriteria(
        ProductCharacteristicValue.class);
    obCriteria.add(Restrictions.eq(ProductCharacteristicValue.PROPERTY_PRODUCT, product));
    obCriteria.add(Restrictions.eq(ProductCharacteristicValue.PROPERTY_CHARACTERISTIC,
        characteristic));
    obCriteria.setMaxResults(1);
    if (obCriteria.count() > 0) {
      return ((ProductCharacteristicValue) obCriteria.uniqueResult()).getCharacteristicValue();
    } else {
      return null;
    }
  }
}