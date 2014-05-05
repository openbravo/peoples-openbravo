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

    String propertyName = getPropertyName(event.getTargetInstance().getEntity());
    Property imageProperty = event
        .getTargetInstance()
        .getEntity()
        .getProperty(
            propertyName.substring(0, propertyName.length()
                - event.getTargetInstance().getEntityName().length()));
    if (event.getCurrentState(imageProperty) != null) {

      if (event.getCurrentState(imageProperty) instanceof Image) {
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

    String propertyName = getPropertyName(event.getTargetInstance().getEntity());
    Property imageProperty = event
        .getTargetInstance()
        .getEntity()
        .getProperty(
            propertyName.substring(0, propertyName.length()
                - event.getTargetInstance().getEntityName().length()));

    if (event.getPreviousState(imageProperty) != null
        && event.getCurrentState(imageProperty) != event.getPreviousState(imageProperty)) {

      if (event.getPreviousState(imageProperty) instanceof Image) {
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
    for (Entity entity : ModelProvider.getInstance().getEntityWithImage().values()) {
      entityArray.add(entity);
    }
    return (Entity[]) entityArray.toArray(new Entity[entityArray.size()]);
  }

  private static String getPropertyName(Entity entity) {
    String property = new String();
    for (String key : ModelProvider.getInstance().getEntityWithImage().keySet()) {
      if (ModelProvider.getInstance().getEntityWithImage().get(key).equals(entity)) {
        property = key;
        break;
      }
    }
    return property;
  }
}
