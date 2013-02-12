/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.retail.posterminal;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.Query;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.dal.core.DalUtil;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.mobile.core.process.WebServiceAbstractServlet;
import org.openbravo.model.ad.system.Language;
import org.openbravo.service.json.JsonConstants;
import org.openbravo.service.json.JsonUtils;

public class ProfileUtilsServlet extends WebServiceAbstractServlet {

  private static final Logger log = Logger.getLogger(ProfileUtilsServlet.class);

  private static final long serialVersionUID = 1L;

  private String[] getClientOrgIds(String terminalName) {
    final String hqlOrg = "select terminal.organization.client.id, terminal.organization.id "
        + "from OBPOS_Applications terminal " + "where terminal.searchKey = :theTerminalSearchKey";
    Query qryOrg = OBDal.getInstance().getSession().createQuery(hqlOrg);
    qryOrg.setParameter("theTerminalSearchKey", terminalName);
    qryOrg.setMaxResults(1);

    String strClient = "none";
    String strOrg = "none";

    if (qryOrg.uniqueResult() != null) {
      final Object[] orgResult = (Object[]) qryOrg.uniqueResult();
      strClient = orgResult[0].toString();
      strOrg = orgResult[1].toString();
    }

    final String result[] = { strClient, strOrg };
    return result;
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {

    OBContext.setAdminMode(false);

    final String terminalName = request.getParameter("terminalName");
    final String userId = request.getParameter("userId");
    final String command = request.getParameter("command");

    JSONObject result = new JSONObject();
    JSONObject resp = new JSONObject();
    JSONArray data = new JSONArray();
    JSONObject item = null;
    try {
      if (command.equals("availableRoles")) {

        String strClient = getClientOrgIds(terminalName)[0];
        String strOrg = getClientOrgIds(terminalName)[1];
        if ("none".equals(strClient)) {
          strClient = "0";
        }
        if ("none".equals(strOrg)) {
          strOrg = "0";
        }

        // Get the role and role name list with the following criteria
        // * The "Role" is available for the current organization
        // * The "Role" is available for the current user
        // * The "Role" has the "Web POS" form as an allowed one

        final String hqlRole = "select distinct role.id, role.name "
            + "from ADRole role, ADRoleOrganization roleOrg, ADUserRoles userRoles, ADFormAccess formAccess "
            + "where role.active = true and " + "roleOrg.active = true and "
            + "userRoles.active = true and " + "formAccess.active = true and "
            + "roleOrg.organization.id = :strOrg and " + "role.id = roleOrg.role.id and "
            + "role.id = userRoles.role.id and " + "role.id = formAccess.role.id and "
            + "userRoles.userContact.id = :userId and "
            + "formAccess.specialForm.id = :webPOSFormId " + "order by role.name";
        Query qryRole = OBDal.getInstance().getSession().createQuery(hqlRole);
        qryRole.setParameter("strOrg", strOrg);
        qryRole.setParameter("userId", userId);
        qryRole.setParameter("webPOSFormId", "B7B7675269CD4D44B628A2C6CF01244F");
        int queryCount = 0;

        for (Object qryRoleObject : qryRole.list()) {
          queryCount++;
          final Object[] qryRoleObjectItem = (Object[]) qryRoleObject;

          item = new JSONObject();
          item.put("id", qryRoleObjectItem[0]);
          item.put("_identifier", qryRoleObjectItem[1]);

          data.put(item);
        }

        resp.put("startRow", 0);
        resp.put("endRow", (queryCount == 0 ? 0 : queryCount - 1));
        resp.put("totalRows", queryCount);
        resp.put("data", data);
        result.append("response", resp);
        writeResult(response, result.toString());

      } else if (command.equals("availableLanguages")) {
        int queryCount = 0;

        final OBQuery<Language> languages = OBDal.getInstance().createQuery(
            Language.class,
            "(" + Language.PROPERTY_SYSTEMLANGUAGE + "=true or " + Language.PROPERTY_BASELANGUAGE
                + "=true)");
        languages.setFilterOnReadableClients(false);
        languages.setFilterOnReadableOrganization(false);

        DalUtil.sortByIdentifier(languages.list());

        for (BaseOBObject bob : languages.list()) {
          queryCount++;
          final JSONObject jsonArrayItem = new JSONObject();
          jsonArrayItem.put(JsonConstants.ID, (String) bob.getId());
          jsonArrayItem.put(JsonConstants.IDENTIFIER, (String) bob.getIdentifier());
          data.put(jsonArrayItem);
        }

        resp.put("startRow", 0);
        resp.put("endRow", (queryCount == 0 ? 0 : queryCount - 1));
        resp.put("totalRows", queryCount);
        resp.put("data", data);
        result.append("response", resp);
        writeResult(response, result.toString());
      }
    } catch (JSONException e) {
      log.error(e.getMessage(), e);
      writeResult(response, JsonUtils.convertExceptionToJson(e));
    } finally {
      OBContext.restorePreviousMode();
      OBContext.setOBContext((OBContext) null);
    }
  }
}