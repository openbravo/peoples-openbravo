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
 * All portions are Copyright (C) 2009 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.client.application.window;

import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.exception.OBException;
import org.openbravo.client.application.MenuManager;
import org.openbravo.client.application.MenuManager.MenuOption;
import org.openbravo.client.kernel.BaseActionHandler;
import org.openbravo.client.kernel.RequestContext;
import org.openbravo.client.kernel.StaticResourceComponent;
import org.openbravo.dal.core.OBContext;
import org.openbravo.model.ad.ui.Window;
import org.openbravo.service.json.JsonConstants;

/**
 * Is used to compute which records to select in a set of tabs of a certain window.
 * 
 * @author mtaal
 * @see StaticResourceComponent
 */
@ApplicationScoped
public class ComputeSelectedRecordActionHandler extends BaseActionHandler {

  private static final String WINDOW_ID = "windowId";
  private static final String TARGET_RECORD_ID = "targetRecordId";
  private static final String TARGET_ENTITY = "targetEntity";

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
            result = processWindow(menuOption.getMenu().getWindow(), targetRecordId, targetEntity);
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

  private JSONObject processWindow(Window window, String recordId, String tabId) {
    final JSONObject result = new JSONObject();

    return result;
  }

  protected JSONObject execute(Map<String, Object> parameters, String data) {
    throw new UnsupportedOperationException();
  }
}
