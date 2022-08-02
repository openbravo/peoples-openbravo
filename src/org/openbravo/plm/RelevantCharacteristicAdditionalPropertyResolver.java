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
 * All portions are Copyright (C) 2022 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.plm;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openbravo.base.model.Entity;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.client.kernel.reference.UIDefinitionController;
import org.openbravo.dal.core.DalUtil;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.ad.domain.Reference;
import org.openbravo.model.common.plm.CharacteristicValue;
import org.openbravo.service.datasource.DataSourceProperty;
import org.openbravo.service.json.AdditionalPropertyResolver;
import org.openbravo.service.json.JsonConstants;

/**
 * Resolves additional properties that reference to relevant characteristics and provides the data
 * source properties required to filter and sort by the characteristic values linked to the relevant
 * characteristic in the client side.
 */
public class RelevantCharacteristicAdditionalPropertyResolver
    implements AdditionalPropertyResolver {

  private static final String RELEVANT_CHARACTERISTIC_REFERENCE_ID = "243FB7EE87FD477E9DF1F14E098C645F";

  @Override
  public Map<String, Object> resolve(BaseOBObject bob, String additionalProperty) {
    return RelevantCharacteristicProperty.from(bob.getEntity(), additionalProperty).map(o -> {
      Map<String, Object> result = new HashMap<>();
      CharacteristicValue chv = o.getCharacteristicValue(bob);
      result.put(additionalProperty, chv != null ? chv.getId() : null);
      result.put(additionalProperty + DalUtil.DOT + JsonConstants.IDENTIFIER,
          chv != null ? chv.getIdentifier() : null);
      return result;
    }).orElse(Collections.emptyMap());
  }

  @Override
  public List<DataSourceProperty> getDataSourceProperties(Entity entity,
      String additionalProperty) {
    return RelevantCharacteristicProperty.from(entity, additionalProperty)
        .map(o -> getRelevantCharacteristicDataSourceProperties(additionalProperty))
        .orElse(Collections.emptyList());
  }

  private List<DataSourceProperty> getRelevantCharacteristicDataSourceProperties(
      String additionalProperty) {
    DataSourceProperty dsProperty = new DataSourceProperty();
    dsProperty.setName(additionalProperty.replace(DalUtil.DOT, DalUtil.FIELDSEPARATOR));
    dsProperty.setId(false);
    dsProperty.setMandatory(false);
    dsProperty.setAuditInfo(false);
    dsProperty.setUpdatable(false);
    dsProperty.setBoolean(false);
    dsProperty.setAllowedValues(null);
    dsProperty.setPrimitive(true);
    dsProperty.setFieldLength(100);
    dsProperty.setUIDefinition(UIDefinitionController.getInstance()
        .getUIDefinition(
            OBDal.getInstance().getProxy(Reference.class, RELEVANT_CHARACTERISTIC_REFERENCE_ID)));
    dsProperty.setPrimitiveObjectType(String.class);
    dsProperty.setNumericType(false);
    dsProperty.setAdditional(true);
    return List.of(dsProperty);
  }
}
