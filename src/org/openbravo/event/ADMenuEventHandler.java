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
 * All portions are Copyright (C) 2020 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.event;

import javax.enterprise.event.Observes;
import javax.servlet.ServletException;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.model.Property;
import org.openbravo.client.kernel.event.EntityPersistenceEventObserver;
import org.openbravo.client.kernel.event.EntityUpdateEvent;
import org.openbravo.dal.service.OBDal;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.businessUtility.TreeData;
import org.openbravo.model.ad.ui.Menu;
import org.openbravo.service.db.DalConnectionProvider;

/**
 * Event Handler for AD_MENU.
 * 
 * It updates the "Included In Reduced Translation" flag for any child menu entry.
 */
class ADMenuEventHandler extends EntityPersistenceEventObserver {
  private static final Logger log = LogManager.getLogger();
  private static final Entity[] ENTITIES = {
      ModelProvider.getInstance().getEntity(Menu.ENTITY_NAME) };
  private static final String MENU_TREE_ID = "10";

  @Override
  protected Entity[] getObservedEntities() {
    return ENTITIES;
  }

  public void onUpdate(@Observes EntityUpdateEvent event) {
    if (!isValidEvent(event)) {
      return;
    }
    updateIncludedInReducedTranslationFlagForChildEntries(event);
  }

  private void updateIncludedInReducedTranslationFlagForChildEntries(EntityUpdateEvent event) {
    ConnectionProvider conn = new DalConnectionProvider(false);
    String menuId = (String) event.getTargetInstance().getId();
    final Entity menuEntity = ModelProvider.getInstance().getEntity(Menu.ENTITY_NAME);
    final Property availableForTrlProperty = menuEntity
        .getProperty(Menu.PROPERTY_AVAILABLEFORTRANSLATION);
    final Boolean currentValueAvailableForTrl = (Boolean) event
        .getCurrentState(availableForTrlProperty);
    final Boolean previousValueAvailableForTrl = (Boolean) event
        .getPreviousState(availableForTrlProperty);
    if (previousValueAvailableForTrl != null && currentValueAvailableForTrl != null
        && !previousValueAvailableForTrl.equals(currentValueAvailableForTrl)) {
      try {
        TreeData[] data = TreeData.select(conn, MENU_TREE_ID, menuId);
        for (int i = 0; i < data.length; i++) {
          if (!StringUtils.equals(menuId, data[i].id)) {
            final Menu menu = OBDal.getInstance().get(Menu.class, data[i].id);
            menu.setAvailableForTranslation(currentValueAvailableForTrl);
          }
        }
      } catch (ServletException e) {
        log.error("Error while updating AvailableForTranslation flag in Menu", e);
      }
    }
  }
}
