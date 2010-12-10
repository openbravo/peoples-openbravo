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
package org.openbravo.client.application.window;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.openbravo.client.application.MenuManager;
import org.openbravo.client.application.MenuManager.MenuOption;
import org.openbravo.client.kernel.BaseTemplateComponent;
import org.openbravo.client.kernel.Template;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.ad.ui.Tab;

/**
 * Component for creating mappings from entities to windows/tabs. It also computes the parent tabs
 * and parent properties from an entity to its parents and grand parents in the same window. This is
 * used to support correct opening of tabs/forms when a child tab is opened directly.
 * 
 * @author mtaal
 */
public class EntityWindowMappingComponent extends BaseTemplateComponent {

  private static final String TEMPLATE_ID = "7130F3674BCC434C8F939DFC2F58002B";
  public static final String COMPONENT_ID = "EntityWindowMappingComponent";

  @Inject
  private MenuManager menuManager;

  /*
   * (non-Javadoc)
   * 
   * @see org.openbravo.client.kernel.BaseTemplateComponent#getComponentTemplate()
   */
  @Override
  protected Template getComponentTemplate() {
    return OBDal.getInstance().get(Template.class, TEMPLATE_ID);
  }

  protected String getModulePackageName() {
    return "org.openbravo.client.application";
  }

  public List<EntityWindowMapping> getEntityWindowMappings() {
    final List<MenuOption> menuOptions = menuManager.getSelectableMenuOptions();
    final List<EntityWindowMapping> mappings = new ArrayList<EntityWindowMapping>();
    final List<String> mappedEntity = new ArrayList<String>();
    for (MenuOption menuOption : menuOptions) {
      if (menuOption.getType() == MenuManager.MenuEntryType.Window) {
        for (Tab tab : menuOption.getMenu().getWindow().getADTabList()) {
          if (mappedEntity.contains(tab.getTable().getName())) {
            continue;
          }
          final EntityWindowMapping mapping = new EntityWindowMapping();
          mapping.setEntityName(tab.getTable().getName());
          mapping.setTabTitle(menuOption.getLabel());
          mapping.setWindowId(menuOption.getMenu().getWindow().getId());
          mapping.setTabId(menuOption.getTab().getId());
          mappings.add(mapping);
          mappedEntity.add(mapping.getEntityName());
        }
      }
    }
    return mappings;
  }

  public String getETag() {
    final String eTag = super.getETag();
    return OBContext.getOBContext().getRole().getId() + eTag;
  }

  public static class EntityWindowMapping {
    private String entityName;
    private String windowId;
    private String tabId;
    private String tabTitle;

    public String getEntityName() {
      return entityName;
    }

    public void setEntityName(String entityName) {
      this.entityName = entityName;
    }

    public String getWindowId() {
      return windowId;
    }

    public void setWindowId(String windowId) {
      this.windowId = windowId;
    }

    public String getTabId() {
      return tabId;
    }

    public void setTabId(String tabId) {
      this.tabId = tabId;
    }

    public String getTabTitle() {
      return tabTitle;
    }

    public void setTabTitle(String tabTitle) {
      this.tabTitle = tabTitle;
    }

  }
}
