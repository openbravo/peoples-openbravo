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
 * All portions are Copyright (C) 2010-2011 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.client.application.window;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.model.Property;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.client.application.MenuManager;
import org.openbravo.client.application.MenuManager.MenuOption;
import org.openbravo.client.kernel.BaseActionHandler;
import org.openbravo.client.kernel.RequestContext;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.ad.ui.Tab;
import org.openbravo.model.ad.ui.Window;
import org.openbravo.service.json.JsonConstants;

/**
 * Is used to compute which records to select in a set of tabs of a certain window. It receives the
 * id and entity of a record and then finds the tab and all the parent tab and records which need to
 * be selected. This is then returned to the client to build the ui.
 * 
 * @author mtaal
 */
@ApplicationScoped
public class ComputeSelectedRecordActionHandler extends BaseActionHandler {

  private static final String WINDOW_ID = "windowId";
  private static final String TARGET_RECORD_ID = "targetRecordId";
  private static final String TARGET_ENTITY = "targetEntity";
  private static final String TARGET_TAB_ID = "targetTabId";
  private static final String RESULT = "result";

  @Inject
  private MenuManager menuManager;

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.openbravo.client.kernel.BaseActionHandler#execute(javax.servlet.http.HttpServletRequest,
   * javax.servlet.http.HttpServletResponse)
   */
  @Override
  public void execute() {
    OBContext.setAdminMode();
    try {
      final HttpServletRequest request = RequestContext.get().getRequest();
      final String windowId = request.getParameter(WINDOW_ID);
      final String targetRecordId = request.getParameter(TARGET_RECORD_ID);
      final String targetEntity = request.getParameter(TARGET_ENTITY);

      JSONObject result = null;
      final List<MenuOption> menuOptions = menuManager.getSelectableMenuOptions();
      for (MenuOption menuOption : menuOptions) {
        if (menuOption.getType() == MenuManager.MenuEntryType.Window) {
          if (windowId.equals(menuOption.getMenu().getWindow().getId())) {
            // found the window process it
            final Window menuWindow = OBDal.getInstance().get(Window.class,
                menuOption.getMenu().getWindow().getId());
            result = processWindow(menuWindow, targetRecordId, targetEntity);
            break;
          }
        }
      }
      // return an empty result
      if (result == null) {
        result = new JSONObject();
      }
      final HttpServletResponse response = RequestContext.get().getResponse();
      response.setContentType(JsonConstants.JSON_CONTENT_TYPE);
      response.setHeader("Content-Type", JsonConstants.JSON_CONTENT_TYPE);
      response.getWriter().write(result.toString());

    } catch (Exception e) {
      throw new OBException(e);
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  private JSONObject processWindow(Window window, String recordId, String entityName)
      throws Exception {
    // create the initial TabInfo
    final BaseOBObject bob = OBDal.getInstance().get(entityName, recordId);

    // special case, no bob
    // find the root tab and return only that
    if (bob == null) {
      for (Tab tab : window.getADTabList()) {
        if (tab.isActive() && tab.getModule().isEnabled() && tab.getTabLevel() == 0) {
          final TabInfo tabInfo = new TabInfo();
          tabInfo.setTab(tab);
          final List<JSONObject> resultList = new ArrayList<JSONObject>();
          resultList.add(tabInfo.getJSONObject());
          final JSONObject result = new JSONObject();
          result.put(RESULT, new JSONArray(resultList));
          return result;
        }
      }
      throw new OBException("No root tab found in window " + window);
    }

    final Tab tab = getTab(window, entityName);
    final TabInfo tabInfo = new TabInfo();
    tabInfo.setRecord(bob);
    tabInfo.setTab(tab);
    final List<JSONObject> resultList = new ArrayList<JSONObject>();
    resultList.add(tabInfo.getJSONObject());
    TabInfo currentTabInfo = tabInfo;
    while (currentTabInfo != null) {
      currentTabInfo = getParentTabInfo(currentTabInfo.getRecord(), window);
      if (currentTabInfo != null) {
        resultList.add(0, currentTabInfo.getJSONObject());
      }
    }
    final JSONObject result = new JSONObject();
    result.put(RESULT, new JSONArray(resultList));
    return result;
  }

  private TabInfo getParentTabInfo(BaseOBObject childEntity, Window window) {
    for (Property property : childEntity.getEntity().getProperties()) {
      if (property.isParent()) {
        final Tab tab = getTab(window, property.getTargetEntity().getName());
        if (tab != null && childEntity.get(property.getName()) != null) {
          final BaseOBObject parent = (BaseOBObject) childEntity.get(property.getName());
          final TabInfo tabInfo = new TabInfo();
          tabInfo.setRecord(parent);
          tabInfo.setTab(tab);
          return tabInfo;
        }
      }
    }
    return null;
  }

  private Tab getTab(Window window, String entityName) {
    for (Tab tab : window.getADTabList()) {
      if (tab.isActive() && tab.getModule().isEnabled()
          && tab.getTable().getName().equals(entityName)) {
        return tab;
      }
    }
    throw new OBException("No tab found using entity " + entityName + " window: " + window);
  }

  protected JSONObject execute(Map<String, Object> parameters, String data) {
    throw new UnsupportedOperationException();
  }

  private class TabInfo {
    private BaseOBObject record;
    private Tab tab;

    public Tab getTab() {
      return tab;
    }

    public void setTab(Tab tab) {
      this.tab = tab;
    }

    public BaseOBObject getRecord() {
      return record;
    }

    public void setRecord(BaseOBObject record) {
      this.record = record;
    }

    public JSONObject getJSONObject() throws Exception {
      final JSONObject result = new JSONObject();
      if (getRecord() != null) {
        result.put(TARGET_RECORD_ID, getRecord().getId());
      }
      result.put(TARGET_TAB_ID, getTab().getId());
      return result;
    }
  }
}
