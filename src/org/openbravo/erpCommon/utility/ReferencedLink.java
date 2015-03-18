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
 * All portions are Copyright (C) 2001-2015 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.erpCommon.utility;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.dal.core.DalUtil;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.data.Sqlc;
import org.openbravo.erpCommon.businessUtility.Preferences;
import org.openbravo.model.ad.datamodel.Table;
import org.openbravo.model.ad.domain.TableNavigation;
import org.openbravo.model.ad.system.Language;
import org.openbravo.model.ad.ui.Tab;
import org.openbravo.model.ad.ui.Window;
import org.openbravo.model.ad.ui.WindowTrl;

public class ReferencedLink extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void init(ServletConfig config) {
    super.init(config);
    boolHist = false;
  }

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    try {
      OBContext.setAdminMode(true);
      VariablesSecureApp vars = new VariablesSecureApp(request);

      if (vars.commandIn("DEFAULT")) {

        StringBuffer servletURL = new StringBuffer();
        String tabId = getTabId(vars);
        String strKeyReferenceId = vars.getStringParameter("inpKeyReferenceId");

        servletURL.append(Utility.getTabURL(tabId, "E", true));
        servletURL.append("?Command=")
            .append((strKeyReferenceId.equals("") ? "DEFAULT" : "DIRECT")).append("&");
        servletURL.append("inpDirectKey").append("=").append(strKeyReferenceId);

        if (log4j.isDebugEnabled()) {
          log4j.debug(servletURL.toString());
        }

        response.sendRedirect(servletURL.toString());

      } else if (vars.commandIn("JSON")) {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        out.print(getJSON(vars));
        out.close();
      } else
        throw new ServletException();
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  private String getJSON(VariablesSecureApp vars) throws ServletException {
    String tabId = getTabId(vars);
    String recordId = vars.getStringParameter("inpKeyReferenceId");
    JSONObject json = null;

    try {

      json = new JSONObject();

      Tab tab = OBDal.getInstance().get(Tab.class, tabId);

      json.put("tabId", tabId);
      json.put("windowId", tab.getWindow().getId());

      final Entity entity = ModelProvider.getInstance().getEntity(tab.getTable().getName());

      // Special case, find the real recordId for the language case
      if (entity.getName().equals(Language.ENTITY_NAME)) {
        final OBQuery<Language> languages = OBDal.getInstance().createQuery(Language.class,
            Language.PROPERTY_LANGUAGE + "=?");
        languages.setParameters(Collections.singletonList((Object) recordId));
        json.put("recordId", languages.list().get(0).getId());
      } else {
        json.put("recordId", recordId);
      }

      final String userLanguageId = OBContext.getOBContext().getLanguage().getId();
      String tabTitle = null;
      for (WindowTrl windowTrl : tab.getWindow().getADWindowTrlList()) {
        final String trlLanguageId = (String) DalUtil.getId(windowTrl.getLanguage());
        if (trlLanguageId.equals(userLanguageId)) {
          tabTitle = windowTrl.getName();
        }
      }
      if (tabTitle == null) {
        tabTitle = tab.getWindow().getName();
      }

      json.put("keyParameter",
          "inp" + Sqlc.TransformaNombreColumna(entity.getIdProperties().get(0).getColumnName()));
      json.put("tabTitle", tabTitle);

      // Find the model object mapping
      json.put("mappingName", Utility.getTabURL(tabId, "E", false));
    } catch (Exception e) {
      try {
        json.put("error", e.getMessage());
      } catch (JSONException jex) {
        log4j.error("Error trying to generate message: " + jex.getMessage(), jex);
      }
    }

    return json.toString();
  }

  private String getTabId(VariablesSecureApp vars) throws ServletException {
    String strKeyReferenceColumnName = vars.getRequiredStringParameter("inpKeyReferenceColumnName");
    String strTableReferenceId;
    Entity obEntity;
    if (vars.hasParameter("inpEntityName")) {
      String entityName = vars.getStringParameter("inpEntityName");
      obEntity = ModelProvider.getInstance().getEntity(entityName);
      strTableReferenceId = obEntity.getTableId();
    } else {
      strTableReferenceId = vars.getRequiredStringParameter("inpTableReferenceId");
      obEntity = ModelProvider.getInstance().getEntityByTableId(strTableReferenceId);
    }
    String strNavigationTabId = vars.getStringParameter("inpNavigationTabId");
    String strKeyReferenceId = vars.getStringParameter("inpKeyReferenceId");
    String strWindowId = vars.getStringParameter("inpwindowId");
    String strTableName = ReferencedLinkData.selectTableName(this, strTableReferenceId);
    log4j.debug("strKeyReferenceColumnName:" + strKeyReferenceColumnName + " strTableReferenceId:"
        + strTableReferenceId + " strKeyReferenceId:" + strKeyReferenceId + " strWindowId:"
        + strWindowId + " strTableName:" + strTableName);

    boolean hasKeyReferenceId = StringUtils.isNotEmpty(strKeyReferenceId);
    // 1st Check - Forced Links
    try {
      strWindowId = Preferences.getPreferenceValue("ForcedLinkWindow" + strTableName, false,
          vars.getClient(), vars.getOrg(), vars.getUser(), vars.getRole(), strWindowId);
      return getTabIdFromWindow(strWindowId, strTableReferenceId, hasKeyReferenceId);
    } catch (PropertyException ignore) {
    }
    try {
      // 2nd Check - NavigationTab
      if (!"".equals(strNavigationTabId)) {
        if (!hasKeyReferenceId) {
          Tab currentTab = OBDal.getInstance().get(Tab.class, strNavigationTabId);
          Window currentWindow = currentTab.getWindow();
          if (currentWindow != null) {
            String currentWindowId = currentWindow.getId();
            if (StringUtils.isNotEmpty(currentWindowId)) {
              ReferencedLinkData[] data = ReferencedLinkData.selectParent(this, currentWindowId);
              if (data == null || data.length == 0) {
                throw new ServletException("Window parent not found: " + strWindowId);
              }
              return data[0].adTabId;
            } else {
              throw new ServletException("Window not found");
            }
          } else {
            throw new ServletException("Window not found");
          }
        } else {
          return strNavigationTabId;
        }
      }
      // 3rd Check - Navigation Rules
      if (hasKeyReferenceId) {
        OBCriteria<TableNavigation> tableNavigationCriteria = OBDal.getInstance().createCriteria(
            TableNavigation.class);
        tableNavigationCriteria.add(Restrictions.eq("table.id", strTableReferenceId));
        tableNavigationCriteria.addOrderBy(TableNavigation.PROPERTY_SEQUENCENUMBER, true);
        List<TableNavigation> tableNavigationList = tableNavigationCriteria.list();
        for (TableNavigation tableNavigation : tableNavigationList) {
          String hqlWhere = "AS e WHERE e.id = :strKeyReferenceId AND ( "
              + tableNavigation.getHqllogic() + " )";

          final OBQuery<BaseOBObject> query = OBDal.getInstance().createQuery(obEntity.getName(),
              hqlWhere);
          query.setNamedParameter("strKeyReferenceId", strKeyReferenceId);

          query.setMaxResult(1);
          if (query.uniqueResult() != null) {
            return tableNavigation.getTab().getId();
          }
        }
      }
    } catch (Exception e2) {
      throw new OBException("Error retrieving destination tab: ", e2);
    }

    // 4th Check - Standard case, select window based on table definition and isSOTrx
    Table table = OBDal.getInstance().get(Table.class, strTableReferenceId);
    if (table.getWindow() == null) {
      throw new ServletException("Window not found");
    }
    Window window = table.getWindow();
    // Only in case an adWindowId is returned
    if (window != null) {
      String windowId = window.getId();
      if (StringUtils.isNotEmpty(windowId)) {
        strWindowId = windowId;
      }
    }

    Window poWindow = table.getPOWindow();
    if (poWindow != null) {
      boolean isSOTrx = getISSOTrx(strTableReferenceId, strKeyReferenceColumnName,
          strKeyReferenceId, vars, strWindowId);
      String poWindowId = poWindow.getId();
      if (!isSOTrx && StringUtils.isNotEmpty(poWindowId)) {
        strWindowId = poWindowId;
      }
    }

    // End of advanced navigation feature
    return getTabIdFromWindow(strWindowId, strTableReferenceId, !hasKeyReferenceId);
  }

  public String getServletInfo() {
    return "Servlet that presents the referenced links";
  } // end of getServletInfo() method

  private String getTabIdFromWindow(String strWindowId, String strTableReferenceId,
      boolean returnParent) throws ServletException {
    ReferencedLinkData[] data = ReferencedLinkData.select(this, strWindowId, strTableReferenceId);
    if (data == null || data.length == 0) {
      throw new ServletException("Window not found: " + strWindowId);
    }
    if (returnParent) {
      data = ReferencedLinkData.selectParent(this, strWindowId);
      if (data == null || data.length == 0) {
        throw new ServletException("Window parent not found: " + strWindowId);
      }
      return data[0].adTabId;
    }
    return data[0].adTabId;
  }

  private boolean getISSOTrx(String strTableReferenceId, String strKeyReferenceColumnName,
      String strKeyReferenceId, VariablesSecureApp vars, String strWindowId)
      throws ServletException {
    boolean isSOTrx = true;
    ReferencedTables ref = new ReferencedTables(this, strTableReferenceId,
        strKeyReferenceColumnName, strKeyReferenceId);
    if (!ref.hasSOTrx()) {
      isSOTrx = (Utility.getContext(this, vars, "IsSOTrx", strWindowId).equals("N") ? false : true);
    } else {
      isSOTrx = ref.isSOTrx();
    }
    return isSOTrx;
  }
}
