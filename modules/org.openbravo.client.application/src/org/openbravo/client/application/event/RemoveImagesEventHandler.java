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
 * All portions are Copyright (C) 2014 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.client.application.event;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.event.Observes;

import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.model.Property;
import org.openbravo.client.kernel.event.EntityDeleteEvent;
import org.openbravo.client.kernel.event.EntityPersistenceEventObserver;
import org.openbravo.client.kernel.event.EntityUpdateEvent;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.ad.utility.Image;

public class RemoveImagesEventHandler extends EntityPersistenceEventObserver {

  private static Entity[] entities = getImageEntities();

  @Override
  protected Entity[] getObservedEntities() {
    return entities;
  }

  public void onDelete(@Observes
  EntityDeleteEvent event) {
    if (!isValidEvent(event)) {
      return;
    }

    // Iterate image properties of the entity
    for (String property : getImageProperties(event.getTargetInstance().getEntity())) {

      Property imageProperty = event.getTargetInstance().getEntity().getProperty(property);

      // Remove image if it exists
      if (event.getCurrentState(imageProperty) != null) {

        Image bob = (Image) event.getCurrentState(imageProperty);

        if (bob != null) {
          OBContext.setAdminMode(true);
          try {
            OBDal.getInstance().remove(bob);
          } finally {
            OBContext.restorePreviousMode();
          }
        }
      }
    }
  }

  public void onUpdate(@Observes
  EntityUpdateEvent event) {
    if (!isValidEvent(event)) {
      return;
    }

    // Iterate image properties of the entity
    for (String property : getImageProperties(event.getTargetInstance().getEntity())) {

      Property imageProperty = event.getTargetInstance().getEntity().getProperty(property);

      // If the old image is different than the new one remove the old image if exists
      if (event.getPreviousState(imageProperty) != null
          && event.getCurrentState(imageProperty) != event.getPreviousState(imageProperty)) {

        Image bob = (Image) event.getPreviousState(imageProperty);

        if (bob != null) {
          OBContext.setAdminMode(true);
          try {
            OBDal.getInstance().remove(bob);
          } finally {
            OBContext.restorePreviousMode();
          }
        }
      }
    }
  }

  private static Entity[] getImageEntities() {
    ArrayList<Entity> entityArray = new ArrayList<Entity>();

    // Create the observed entities from ModelProvider
    for (Entity entity : ModelProvider.getInstance().getEntityWithImage().keySet()) {
      entityArray.add(entity);
    }
    return (Entity[]) entityArray.toArray(new Entity[entityArray.size()]);
  }

  private static List<String> getImageProperties(Entity entity) {
    // Get EntitiesWithImages from ModelProvider
    return ModelProvider.getInstance().getEntityWithImage().get(entity);
  }
}
