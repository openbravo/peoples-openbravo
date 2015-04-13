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
import org.openbravo.erpCommon.businessUtility.Preferences;
import org.openbravo.model.ad.datamodel.Table;
import org.openbravo.model.ad.domain.TableNavigation;
import org.openbravo.model.ad.system.Language;
import org.openbravo.model.ad.ui.Field;
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

      }
      // Standard flow
      else if (vars.commandIn("JSON")) {
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
    JSONObject json = new JSONObject();

    try {

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

      json.put("tabTitle", tabTitle);
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
    String fieldId = vars.getStringParameter("inpFieldId");
    String strKeyReferenceId = vars.getStringParameter("inpKeyReferenceId");
    String strWindowId = vars.getStringParameter("inpwindowId");
    String strTableName = ReferencedLinkData.selectTableName(this, strTableReferenceId);
    log4j.debug("strKeyReferenceColumnName:" + strKeyReferenceColumnName + " strTableReferenceId:"
        + strTableReferenceId + " strKeyReferenceId:" + strKeyReferenceId + " strWindowId:"
        + strWindowId + " strTableName:" + strTableName);

    boolean hasKeyReferenceId = !StringUtils.equals("null", strKeyReferenceId);
    // 1st Check - Forced Links - Rules defined as a preference
    try {
      strWindowId = Preferences.getPreferenceValue("ForcedLinkWindow" + strTableName, false,
          vars.getClient(), vars.getOrg(), vars.getUser(), vars.getRole(), strWindowId);
      return getTabIdFromWindow(strWindowId, strTableReferenceId, hasKeyReferenceId);
    }
    // The normal flow throws a propertyException because there are not any forced link, this
    // exception will be ignored
    catch (PropertyException ignore) {
    }
    try {
      // 2nd Check - NavigationTab - Rules defined at field level
      // If the field from which navigates is empty, no id is sent, so hasKeyReferenceId is false
      if (hasKeyReferenceId) {
        Field field = OBDal.getInstance().get(Field.class, fieldId);
        OBCriteria<TableNavigation> tableNavigationCriteria = OBDal.getInstance().createCriteria(
            TableNavigation.class);
        tableNavigationCriteria.add(Restrictions.eq("table.id", strTableReferenceId));
        tableNavigationCriteria.add(Restrictions.eq(TableNavigation.PROPERTY_FIELD, field));
        tableNavigationCriteria.addOrderBy(TableNavigation.PROPERTY_SEQUENCENUMBER, true);
        List<TableNavigation> tableNavigationList = tableNavigationCriteria.list();
        // Iterate the navigation rules with a field
        for (TableNavigation tableNavigation : tableNavigationList) {
          String hqlWhere = "AS e WHERE e.id = :strKeyReferenceId AND ( "
              + tableNavigation.getHQLLogic() + " )";

          final OBQuery<BaseOBObject> query = OBDal.getInstance().createQuery(obEntity.getName(),
              hqlWhere);
          query.setNamedParameter("strKeyReferenceId", strKeyReferenceId);

          query.setMaxResult(1);
          // If the query returns at least 1 result the rule has to be applied
          if (query.uniqueResult() != null) {
            return tableNavigation.getTab().getId();
          }
        }
        // 3rd Check - Navigation Rules - Rules defined at table level
        tableNavigationCriteria = OBDal.getInstance().createCriteria(TableNavigation.class);
        tableNavigationCriteria.add(Restrictions.eq("table.id", strTableReferenceId));
        tableNavigationCriteria.add(Restrictions.isNull(TableNavigation.PROPERTY_FIELD));
        tableNavigationCriteria.addOrderBy(TableNavigation.PROPERTY_SEQUENCENUMBER, true);
        tableNavigationList = tableNavigationCriteria.list();
        // Iterate the navigation rules of the table without field
        for (TableNavigation tableNavigation : tableNavigationList) {
          String hqlWhere = "AS e WHERE e.id = :strKeyReferenceId AND ( "
              + tableNavigation.getHQLLogic() + " )";

          final OBQuery<BaseOBObject> query = OBDal.getInstance().createQuery(obEntity.getName(),
              hqlWhere);
          query.setNamedParameter("strKeyReferenceId", strKeyReferenceId);

          query.setMaxResult(1);
          // If the query returns at least 1 result the rule has to be applied
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
    Window window = table.getWindow();
    if (window == null) {
      throw new ServletException("Window not found");
    }
    // Only in case an adWindowId is returned
    strWindowId = DalUtil.getId(window).toString();

    boolean isSOTrx = isSOTrx(strTableReferenceId, strKeyReferenceColumnName, strKeyReferenceId,
        vars, strWindowId);
    if (!isSOTrx) {
      Window poWindow = table.getPOWindow();
      if (poWindow != null) {
        strWindowId = DalUtil.getId(poWindow).toString();
      }
    }

    return getTabIdFromWindow(strWindowId, strTableReferenceId, !hasKeyReferenceId);
  }

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

  private boolean isSOTrx(String strTableReferenceId, String strKeyReferenceColumnName,
      String strKeyReferenceId, VariablesSecureApp vars, String strWindowId)
      throws ServletException {
    boolean isSOTrx;
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
