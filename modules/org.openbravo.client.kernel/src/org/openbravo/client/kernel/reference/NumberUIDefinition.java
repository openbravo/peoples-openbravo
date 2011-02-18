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
 * All portions are Copyright (C) 2010 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.client.kernel.reference;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.model.domaintype.PrimitiveDomainType;
import org.openbravo.client.kernel.reference.UIDefinitionController.FormatDefinition;
import org.openbravo.model.ad.ui.Field;

/**
 * Implementation of the ui definition for numbers.
 * 
 * @author mtaal
 */
public abstract class NumberUIDefinition extends UIDefinition {

  @Override
  public String getFormEditorType() {
    return "OBNumberItem";
  }

  public String getFilterEditorProperties(Field field) {
    final String superProps = super.getFilterEditorProperties(field);
    final PrimitiveDomainType primitiveDomainType = (PrimitiveDomainType) getDomainType();
    if (primitiveDomainType.getFormatId() != null) {
      final String formatId = primitiveDomainType.getFormatId();
      final FormatDefinition inputFormat = UIDefinitionController.getInstance()
          .getFormatDefinition(formatId, UIDefinitionController.SHORTFORMAT_QUALIFIER);
      if (inputFormat != null) {
        return superProps + ", 'maskNumeric': '" + inputFormat.getFormat() + "', "
            + "'decSeparator': '" + inputFormat.getDecimalSymbol() + "'," + "'groupSeparator': '"
            + inputFormat.getGroupingSymbol() + "'";
      }
    }
    return superProps;
  }

  @Override
  public String getTypeProperties() {
    final PrimitiveDomainType primitiveDomainType = (PrimitiveDomainType) getDomainType();
    if (primitiveDomainType.getFormatId() != null) {
      final String formatId = primitiveDomainType.getFormatId();
      final FormatDefinition shortFormat = UIDefinitionController.getInstance()
          .getFormatDefinition(formatId, UIDefinitionController.SHORTFORMAT_QUALIFIER);
      final FormatDefinition normalFormat = UIDefinitionController.getInstance()
          .getFormatDefinition(formatId, UIDefinitionController.NORMALFORMAT_QUALIFIER);
      final StringBuilder sb = new StringBuilder();
      if (shortFormat != null) {
        sb.append("shortDisplayFormatter: function(value, field, component, record) {"
            + "return OB.Utilities.Number.OBPlainToOBMasked(value," + "'" + shortFormat.getFormat()
            + "'," + "'" + shortFormat.getDecimalSymbol() + "'," + "'"
            + shortFormat.getGroupingSymbol() + "', OB.Format.defaultGroupingSize);" + "},");
      }
      if (normalFormat != null) {
        sb.append("normalDisplayFormatter: function(value, field, component, record) {"
            + "return OB.Utilities.Number.OBPlainToOBMasked(value," + "'"
            + normalFormat.getFormat() + "'," + "'" + normalFormat.getDecimalSymbol() + "'," + "'"
            + normalFormat.getGroupingSymbol() + "', OB.Format.defaultGroupingSize);" + "},");

      }
      return sb.toString();
    }
    return "";
  }

  @Override
  public String getFieldProperties(Field field) {
    final PrimitiveDomainType primitiveDomainType = (PrimitiveDomainType) getDomainType();
    if (primitiveDomainType.getFormatId() != null) {
      final String formatId = primitiveDomainType.getFormatId();
      final FormatDefinition inputFormat = UIDefinitionController.getInstance()
          .getFormatDefinition(formatId, UIDefinitionController.SHORTFORMAT_QUALIFIER);
      if (inputFormat != null) {
        try {
          final JSONObject jsonObject = new JSONObject();
          jsonObject.put("maskNumeric", inputFormat.getFormat());
          jsonObject.put("decSeparator", inputFormat.getDecimalSymbol());
          jsonObject.put("groupSeparator", inputFormat.getGroupingSymbol());
          return jsonObject.toString();
        } catch (JSONException e) {
          throw new OBException(e);
        }
      }
    }
    return "";
  }

  public static class DecimalUIDefinition extends NumberUIDefinition {
    public String getParentType() {
      return "float";
    }
  }

  public static class IntegerUIDefinition extends NumberUIDefinition {
    public String getParentType() {
      return "integer";
    }
  }
}
