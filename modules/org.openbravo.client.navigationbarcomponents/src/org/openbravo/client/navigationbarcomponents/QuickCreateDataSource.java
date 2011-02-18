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
package org.openbravo.client.navigationbarcomponents;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.seam.Component;
import org.openbravo.client.application.MenuManager;
import org.openbravo.client.application.MenuManager.MenuOption;
import org.openbravo.dal.core.OBContext;
import org.openbravo.service.datasource.ReadOnlyDataSourceService;
import org.openbravo.service.json.JsonConstants;

/**
 * Reads the entities which the user is allowed to create and the tabs which can be used to create
 * the entity.
 * 
 * @author mtaal
 */
public class QuickCreateDataSource extends ReadOnlyDataSourceService {

  private MenuManager menuManager;

  /**
   * Returns the count of objects based on the passed parameters.
   * 
   * @param parameters
   *          the parameters passed in from the request
   * @return the total number of objects
   */
  protected int getCount(Map<String, String> parameters) {
    return getData(parameters, 0, Integer.MAX_VALUE).size();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.openbravo.service.datasource.ReadOnlyDataSourceService#getData(java.util.Map, int,
   * int)
   */
  protected List<Map<String, Object>> getData(Map<String, String> parameters, int startRow,
      int endRow) {
    OBContext.setAdminMode();
    try {
      final List<MenuOption> menuOptions = getMenuManager().getSelectableMenuOptions();
      final List<MenuOption> filteredMenuOptions = new ArrayList<MenuOption>();
      String filterOn = parameters.get(JsonConstants.IDENTIFIER);
      if (filterOn != null) {
        filterOn = filterOn.toLowerCase().trim();
      }
      for (MenuOption menuOption : menuOptions) {
        if (menuOption.getType() == MenuManager.MenuEntryType.Window) {
          if (filterOn == null || menuOption.getLabel().toLowerCase().contains(filterOn)) {
            filteredMenuOptions.add(menuOption);
          }
        }
      }
      List<MenuOption> returnList = filteredMenuOptions;
      if (startRow > -1 && endRow > -1) {
        if (startRow >= returnList.size()) {
          returnList.clear();
        } else if (endRow >= returnList.size()) {
          returnList = returnList.subList(startRow, returnList.size());
        } else {
          returnList = returnList.subList(startRow, endRow);
        }
      }
      final List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
      for (MenuOption menuOption : returnList) {
        final Map<String, Object> data = new HashMap<String, Object>();
        data.put(JsonConstants.IDENTIFIER, menuOption.getLabel());
        data.put(JsonConstants.ID, menuOption.getId());
        data.put(QuickLaunchDataSource.OPTION_TYPE, QuickLaunchDataSource.OPTION_TYPE_TAB);
        if (menuOption.getMenu() != null && menuOption.getMenu().getWindow() != null) {
          data.put(QuickLaunchDataSource.WINDOW_ID, menuOption.getMenu().getWindow().getId());
        }
        result.add(data);
      }
      sort(JsonConstants.IDENTIFIER, result);
      return result;
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  public MenuManager getMenuManager() {
    if (menuManager == null) {
      menuManager = (MenuManager) Component.getInstance(MenuManager.class);
    }
    return menuManager;
  }

  public void setMenuManager(MenuManager menuManager) {
    this.menuManager = menuManager;
  }
}
