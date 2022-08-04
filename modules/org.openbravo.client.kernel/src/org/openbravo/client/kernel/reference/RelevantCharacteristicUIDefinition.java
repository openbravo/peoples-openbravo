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
package org.openbravo.client.kernel.reference;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.exception.OBException;
import org.openbravo.common.plm.RelevantCharacteristicProperty;
import org.openbravo.dal.core.DalUtil;
import org.openbravo.model.ad.ui.Field;
import org.openbravo.service.json.JsonConstants;

/**
 * UI definition for displaying the values of the characteristics which are linked to a relevant
 * characteristic
 */
public class RelevantCharacteristicUIDefinition extends UIDefinition {
  @Override
  public String getFormEditorType() {
    return "OBTextItem";
  }

  @Override
  public String getFilterEditorType() {
    return "OBCharacteristicValueFilterItem";
  }

  @Override
  public String getGridFieldProperties(Field field) {
    String gridFieldProperties = super.getGridFieldProperties(field);
    gridFieldProperties += ", displayField: '" + getDisplayField(field) + "'";
    gridFieldProperties += ", valueField: '" + getProperty(field) + "'";
    gridFieldProperties += ", length: 100";
    gridFieldProperties += ", displaylength: 100";
    return gridFieldProperties;
  }

  @Override
  public String getGridEditorFieldProperties(Field field) {
    String gridFieldEditorProperties = "displayField: '" + getDisplayField(field) + "'";
    gridFieldEditorProperties += ", valueField: '" + getProperty(field) + "'";
    return gridFieldEditorProperties;
  }

  @Override
  public String getFilterEditorPropertiesProperty(Field field) {
    // Ignore grid configuration settings like "allowFkFilterByIdentifier" or "disableFkDropdown"
    // because this reference always displays all the values of the relevant characteristic and
    // only allows to select the filtering criteria by selecting values in the drop-down
    return RelevantCharacteristicProperty.from(field)
        .map(p -> ", filterOnChange: false, characteristicId: '" + p.getCharacteristicId() + "'")
        .orElse("");
  }

  @Override
  public String getFieldProperties(Field field) {
    String parentProperties = super.getFieldProperties(field);
    if (field == null) {
      return parentProperties;
    }
    try {
      JSONObject fieldProperties = new JSONObject(
          parentProperties != null && parentProperties.startsWith("{") ? parentProperties : "{}");
      fieldProperties.put("displayField", getDisplayField(field));
      return fieldProperties.toString();
    } catch (JSONException ex) {
      throw new OBException("Exception when generating field properties for " + field, ex);
    }
  }

  @Override
  public String getTypeProperties() {
    return "sortNormalizer: function (item, field, context){ return OB.Utilities.bySeqNoSortNormalizer(item, field, context); },";
  }

  private String getDisplayField(Field field) {
    return getProperty(field) + DalUtil.FIELDSEPARATOR + JsonConstants.IDENTIFIER;
  }

  private String getProperty(Field field) {
    return field.getProperty().replace(DalUtil.DOT, DalUtil.FIELDSEPARATOR);
  }
}
