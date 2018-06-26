/*
 ************************************************************************************
 * Copyright (C) 2018 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.retail.posterminal.utility;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.Query;
import org.hibernate.criterion.Restrictions;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.ad.access.User;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.retail.posterminal.ApprovalCheckHook;
import org.openbravo.retail.posterminal.ApprovalPreCheckHook;
import org.openbravo.retail.posterminal.OBPOSApplications;
import org.openbravo.utils.FormatUtilities;

public class CheckApproval extends HttpServlet {

  private static final long serialVersionUID = 1L;

  @Inject
  @Any
  private Instance<ApprovalCheckHook> approvalCheckProcesses;

  @Inject
  @Any
  private Instance<ApprovalPreCheckHook> approvalPreCheckProcesses;

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {

    OBContext.setAdminMode(false);
    try {
      JSONObject attributes = new JSONObject();
      JSONArray approvalType = new JSONArray();
      String terminal = request.getParameter("t");
      String username = request.getParameter("u");
      String password = request.getParameter("p");
      if (request.getParameter("a") != null) {
        attributes = new JSONObject(request.getParameter("a"));
      }
      Organization store = getTerminalStore(terminal);
      final String organization = store.getId();
      final String client = store.getClient().getId();

      if (request.getParameter("approvalType") != null) {
        approvalType = new JSONArray(request.getParameter("approvalType"));
      }
      executeApprovalPreCheckHook(username, password, terminal, approvalType, attributes);
      JSONObject result = new JSONObject();

      OBCriteria<User> qUser = OBDal.getInstance().createCriteria(User.class);
      qUser.add(Restrictions.eq(User.PROPERTY_USERNAME, username));
      qUser.add(Restrictions.eq(User.PROPERTY_PASSWORD, FormatUtilities.sha1Base64(password)));
      qUser.setFilterOnReadableOrganization(false);
      qUser.setFilterOnReadableClients(false);
      List<User> qUserList = qUser.list();

      if (qUserList.size() == 0) {
        result.put("status", 1);
        JSONObject jsonError = new JSONObject();
        jsonError.put("message", OBMessageUtils.getI18NMessage("OBPOS_InvalidUserPassword", null));
        result.put("error", jsonError);
      } else {
        String approvals = "'" + approvalType.getString(0) + "'";
        for (int i = 1; i < approvalType.length(); i++) {
          approvals = approvals + ",'" + approvalType.getString(i) + "'";
        }

        String naturalTreeOrgList = Utility.getInStrSet(OBContext.getOBContext()
            .getOrganizationStructureProvider(client).getNaturalTree(organization));

        String hqlQuery = "select p.property from ADPreference as p"
            + " where property IS NOT NULL "
            + "   and active = true" //
            + "   and (case when length(searchKey)<>1 then 'X' else to_char(searchKey) end) = 'Y'" //
            + "   and (userContact.id = :user" //
            + "        or exists (from ADUserRoles r"
            + "                  where r.role = p.visibleAtRole"
            + "                    and r.userContact.id = :user"
            + "                    and r.active=true))"
            + "   and (p.visibleAtOrganization.id = :org " //
            + "   or p.visibleAtOrganization.id in (:orgList) "
            + "   or p.visibleAtOrganization is null) group by p.property";
        Query preferenceQuery = OBDal.getInstance().getSession().createQuery(hqlQuery);
        preferenceQuery.setParameter("user", qUserList.get(0).getId());
        preferenceQuery.setParameter("org", organization);
        preferenceQuery.setParameter("orgList", naturalTreeOrgList);

        // List preferenceList = preferenceQuery.list();
        if (preferenceQuery.list().size() == 0) {
          result.put("status", 1);
          JSONObject jsonError = new JSONObject();
          jsonError.put("message",
              OBMessageUtils.getI18NMessage("OBPOS_UserCannotApprove", new String[] { username }));
          result.put("error", jsonError);
        } else {
          result.put("status", 0);
          JSONObject jsonData = new JSONObject();
          JSONObject jsonPreference = new JSONObject();
          Integer c = 0;
          for (Object preference : preferenceQuery.list()) {
            jsonPreference.put((String) preference, (String) preference);
            if (approvals.contains((String) preference)) {
              c++;
            }
          }
          jsonData.put("userId", qUserList.get(0).getId());
          jsonData.put("canApprove", c >= approvalType.length());
          jsonData.put("preference", jsonPreference);
          result.put("data", jsonData);
        }
        executeApprovalCheckHook(username, password, terminal, approvalType, attributes);
        if (attributes.has("msg")) {
          result.put("status", 1);
          JSONObject jsonError = new JSONObject();
          jsonError.put("message", attributes.getString("msg"));
          result.put("error", jsonError);
        }
      }
      PrintWriter out = response.getWriter();
      response.setContentType("application/json");
      response.setCharacterEncoding("UTF-8");
      out.print(result.toString());
      out.flush();
    } catch (JSONException e) {
      e.printStackTrace();
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  private void executeApprovalCheckHook(String username, String password, String terminal,
      JSONArray approvalType, JSONObject attributes) {
    for (Iterator<ApprovalCheckHook> processIterator = approvalCheckProcesses.iterator(); processIterator
        .hasNext();) {
      ApprovalCheckHook process = processIterator.next();
      try {
        process.exec(username, password, terminal, approvalType, attributes);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  private void executeApprovalPreCheckHook(String username, String password, String terminal,
      JSONArray approvalType, JSONObject attributes) {
    for (Iterator<ApprovalPreCheckHook> processIterator = approvalPreCheckProcesses.iterator(); processIterator
        .hasNext();) {
      ApprovalPreCheckHook process = processIterator.next();
      try {
        process.exec(username, password, terminal, approvalType, attributes);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  private Organization getTerminalStore(String posTerminal) {
    OBCriteria<OBPOSApplications> terminalCriteria = OBDal.getInstance().createCriteria(
        OBPOSApplications.class);
    terminalCriteria.setFilterOnReadableClients(false);
    terminalCriteria.setFilterOnReadableOrganization(false);
    terminalCriteria.add(Restrictions.eq(OBPOSApplications.PROPERTY_SEARCHKEY, posTerminal));
    OBPOSApplications terminal = (OBPOSApplications) terminalCriteria.uniqueResult();
    return terminal.getOrganization();
  }
}
