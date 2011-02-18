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
 * All portions are Copyright (C) 2009 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.userinterface.smartclient;

import org.openbravo.base.model.Entity;
import org.openbravo.base.model.Property;
import org.openbravo.service.json.JsonConstants;

/**
 * Contains utility methods used in this module.
 * 
 * @author mtaal
 */
public class SmartClientUtils {

  /**
   * Translates a so-called property path to a property. The passed entity is the starting entity.
   * For example the property: organization.name and entity: Product will result in the
   * Organization.name property to be returned.
   * 
   * @param entity
   *          the start entity for the property path
   * @param propertyPath
   *          the property path, dot-separated property names
   * @return the found property
   */
  public static Property getProperty(Entity entity, String propertyPath) {
    final String[] parts = propertyPath.split("\\.");
    Entity currentEntity = entity;
    Property result = null;
    for (String part : parts) {
      // only consider it as an identifier if it is called an identifier and
      // the entity does not accidentally have an identifier property
      // && !currentEntity.hasProperty(part)
      // NOTE disabled for now, there is one special case: AD_Column.IDENTIFIER
      // which is NOT HANDLED
      if (part.equals(JsonConstants.IDENTIFIER)) {
        // pick the first identifier property
        if (currentEntity.getIdentifierProperties().isEmpty()) {
          return null;
        }
        return currentEntity.getIdentifierProperties().get(0);
      }
      if (!currentEntity.hasProperty(part)) {
        return null;
      }
      result = currentEntity.getProperty(part);
      if (result.getTargetEntity() != null) {
        currentEntity = result.getTargetEntity();
      }
    }
    return result;
  }
}
