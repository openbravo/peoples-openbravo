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
package org.openbravo.userinterface.selector.reference;

import java.util.HashMap;
import java.util.Map;

import org.openbravo.base.model.Property;
import org.openbravo.base.util.Check;
import org.openbravo.base.weld.WeldUtils;
import org.openbravo.client.kernel.reference.ForeignKeyUIDefinition;
import org.openbravo.dal.core.DalUtil;
import org.openbravo.model.ad.domain.Reference;
import org.openbravo.model.ad.ui.Field;
import org.openbravo.userinterface.selector.Selector;
import org.openbravo.userinterface.selector.SelectorComponent;
import org.openbravo.userinterface.selector.SelectorConstants;

/**
 * Implementation of the foreign key ui definition which uses a selector for its input/filter types.
 * 
 * @author mtaal
 */
public class FKSelectorUIDefinition extends ForeignKeyUIDefinition {

  @Override
  public String getFormEditorType() {
    return "OBSelectorItem";
  }

  public String getFieldProperties(Field field) {
    final Selector selector = getSelector(field);
    final String tableName = field.getColumn().getTable().getDBTableName();
    final String columnName = field.getColumn().getDBColumnName();

    final Property property = DalUtil.getProperty(tableName, columnName);

    final SelectorComponent selectorComponent = WeldUtils
        .getInstanceFromStaticBeanManager(SelectorComponent.class);
    final Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put(SelectorConstants.PARAM_TAB_ID, field.getTab().getId());
    parameters.put(SelectorConstants.PARAM_COLUMN_NAME, field.getColumn().getDBColumnName());
    parameters.put(SelectorComponent.SELECTOR_ITEM_PARAMETER, "true");
    parameters.put(SelectorConstants.PARAM_TARGET_PROPERTY_NAME, property.getName());
    selectorComponent.setId(selector.getId());
    selectorComponent.setParameters(parameters);
    return selectorComponent.generate();
  }

  private Selector getSelector(Field field) {
    final Reference reference = field.getColumn().getReferenceSearchKey();
    Check.isNotNull(reference, "Field " + field + " does not have a reference value set");
    for (Selector selector : reference.getOBUISELSelectorList()) {
      if (selector.isActive()) {
        return selector;
      }
    }
    Check.fail("No valid selector for field " + field);
    return null;
  }
}
