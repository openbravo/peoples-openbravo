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
 * All portions are Copyright (C) 2015 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.client.application.event;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.event.Observes;

import org.hibernate.criterion.Restrictions;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.model.Property;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.client.kernel.event.EntityDeleteEvent;
import org.openbravo.client.kernel.event.EntityPersistenceEventObserver;
import org.openbravo.client.kernel.event.EntityUpdateEvent;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.ad.utility.ADFile;

public class RemoveFilesEventHandler extends EntityPersistenceEventObserver {

  private static Entity[] entities = getFileEntities();
  private static final String DUMMY_FILE_NAME = "DummyFileForDeletedRows";

  @Override
  protected Entity[] getObservedEntities() {
    return entities;
  }

  public void onDelete(@Observes EntityDeleteEvent event) {
    if (!isValidEvent(event)) {
      return;
    }

    ADFile dummyFile = getDummyFile();
    // Iterate file properties of the entity
    for (String property : getFileProperties(event.getTargetInstance().getEntity())) {

      Property fileProperty = event.getTargetInstance().getEntity().getProperty(property);

      // Remove file if it exists
      if (event.getCurrentState(fileProperty) != null) {

        ADFile bob = (ADFile) event.getCurrentState(fileProperty);
        // Replace the current file with a dummy one, just in case the file column is mandatory
        // See issue https://issues.openbravo.com/view.php?id=30571 that describes the same
        // situation for Image BLOB references
        event.setCurrentState(fileProperty, dummyFile);
        if (bob != null) {
          OBDal.getInstance().remove(bob);
        }
      }
    }
  }

  public void onUpdate(@Observes EntityUpdateEvent event) {
    if (!isValidEvent(event)) {
      return;
    }

    // Iterate file properties of the entity
    for (String property : getFileProperties(event.getTargetInstance().getEntity())) {

      Property fileProperty = event.getTargetInstance().getEntity().getProperty(property);

      // If the old file is different than the new one remove the old file if exists
      if (event.getPreviousState(fileProperty) != null
          && event.getCurrentState(fileProperty) != event.getPreviousState(fileProperty)) {

        ADFile bob = (ADFile) event.getPreviousState(fileProperty);
        if (bob != null) {
          OBDal.getInstance().remove(bob);
        }
      }
    }
  }

  /**
   * Returns a dummy File (ADFile instance) that will be named DUMMY_FILE_NAME and will not have
   * binary data
   *
   * @return a dummy image
   */
  private ADFile getDummyFile() {
    OBCriteria<ADFile> dummyImageCriteria = OBDal.getInstance().createCriteria(ADFile.class);
    dummyImageCriteria.add(Restrictions.eq(ADFile.PROPERTY_NAME, DUMMY_FILE_NAME));
    dummyImageCriteria.add(Restrictions.isNull(ADFile.PROPERTY_BINDARYDATA));
    ADFile dummyImage = (ADFile) dummyImageCriteria.uniqueResult();
    // If it is not already created, do it
    if (dummyImage == null) {
      dummyImage = createDummyFile();
    }
    return dummyImage;
  }

  /**
   * Creates a dummy image, that will be called DUMMY_FILE_NAME and will not have binary data
   * 
   * @return the dummy file
   */
  private ADFile createDummyFile() {
    ADFile dummyFile = OBProvider.getInstance().get(ADFile.class);
    dummyFile.setName(DUMMY_FILE_NAME);
    OBDal.getInstance().save(dummyFile);
    return dummyFile;
  }

  private static Entity[] getFileEntities() {
    ArrayList<Entity> entityArray = new ArrayList<Entity>();

    // Create the observed entities from ModelProvider
    for (Entity entity : ModelProvider.getInstance().getEntityWithFile().keySet()) {
      entityArray.add(entity);
    }
    return entityArray.toArray(new Entity[entityArray.size()]);
  }

  private static List<String> getFileProperties(Entity entity) {
    // Get EntitiesWithFile from ModelProvider
    return ModelProvider.getInstance().getEntityWithFile().get(entity);
  }
}
