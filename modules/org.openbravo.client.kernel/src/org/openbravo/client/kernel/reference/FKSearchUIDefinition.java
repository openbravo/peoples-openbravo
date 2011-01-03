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
 * All portions are Copyright (C) 2010 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.client.kernel.reference;

import org.openbravo.base.model.Property;
import org.openbravo.client.kernel.KernelUtils;
import org.openbravo.dal.service.OBDal;
import org.openbravo.data.Sqlc;
import org.openbravo.model.ad.domain.ModelImplementation;
import org.openbravo.model.ad.domain.ModelImplementationMapping;
import org.openbravo.model.ad.domain.Reference;
import org.openbravo.model.ad.domain.Selector;
import org.openbravo.model.ad.domain.SelectorColumn;
import org.openbravo.model.ad.ui.Field;

/**
 * Implementation of the foreign key ui definition which handles the classic search references.
 * 
 * @author mtaal
 */
public class FKSearchUIDefinition extends ForeignKeyUIDefinition {

  @Override
  public String getFormEditorType() {
    return "OBSearchItem";
  }

  @Override
  public String getFieldProperties(Field field) {
    if (field == null) {
      return "";
    }
    final StringBuilder props = new StringBuilder();
    final Property prop = KernelUtils.getInstance().getPropertyFromColumn(field.getColumn());
    final Reference reference = OBDal.getInstance().get(Reference.class,
        prop.getDomainType().getReference().getId());
    ModelImplementation modelImplementation = null;
    for (ModelImplementation localModelImplementation : reference.getADModelImplementationList()) {
      if (localModelImplementation.isActive()) {
        modelImplementation = localModelImplementation;
        break;
      }
    }
    if (modelImplementation == null) {
      // TODO: warn
      return props.toString();
    }
    ModelImplementationMapping modelImplementationMapping = null;
    for (ModelImplementationMapping localModelImplementationMapping : modelImplementation
        .getADModelImplementationMappingList()) {
      if (localModelImplementationMapping.isActive()) {
        if (modelImplementationMapping == null) {
          modelImplementationMapping = localModelImplementationMapping;
        } else if (localModelImplementationMapping.isDefault()) {
          modelImplementationMapping = localModelImplementationMapping;
          break;
        }
      }
    }
    if (modelImplementationMapping == null) {
      // TODO: warn
      return getJsonObjectString(props.toString());
    }
    props.append("searchUrl: '" + modelImplementationMapping.getMappingName() + "'");

    Selector selector = null;
    for (Selector localSelector : reference.getADSelectorList()) {
      if (localSelector.isActive()) {
        selector = localSelector;
        break;
      }
    }
    if (selector == null) {
      // TODO: warn
      return getJsonObjectString(props.toString());
    }
    final StringBuilder inFields = new StringBuilder();
    final StringBuilder outFields = new StringBuilder();
    for (SelectorColumn selectorColumn : selector.getADSelectorColumnList()) {
      if (selectorColumn.isActive()) {
        String columnName = selectorColumn.getDBColumnName()
            + (selectorColumn.getSuffix() != null ? selectorColumn.getSuffix() : "");
        columnName = "inp" + Sqlc.TransformaNombreColumna(columnName);
        if (selectorColumn.getColumnType().equals("I")) {
          if (inFields.length() > 0) {
            inFields.append(",");
          }
          inFields.append("'" + columnName + "'");
        } else {
          if (outFields.length() > 0) {
            outFields.append(",");
          }
          outFields.append("'" + columnName + "'");
        }
      }
    }
    props.append(", inFields: [" + inFields.toString() + "]");
    props.append(", outFields: [" + outFields.toString() + "]");

    return getJsonObjectString(props.toString());
  }

  private String getJsonObjectString(String value) {
    return "{" + value.trim() + "}";
  }
}
