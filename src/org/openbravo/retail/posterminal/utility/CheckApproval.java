/*
 ************************************************************************************
 * Copyright (C) 2013-2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.retail.posterminal.utility;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.criterion.Restrictions;
import org.openbravo.authentication.hashing.PasswordHash;
import org.openbravo.base.secureApp.AllowedCrossDomainsHandler;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.model.ad.access.User;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.retail.posterminal.ApprovalCheckHook;
import org.openbravo.retail.posterminal.ApprovalPreCheckHook;
import org.openbravo.retail.posterminal.OBPOSApplications;

public class CheckApproval extends HttpServlet {

  private static final long serialVersionUID = 1L;

  private static final Logger log = LogManager.getLogger();

  @Inject
  @Any
  private Instance<ApprovalCheckHook> approvalCheckProcesses;

  @Inject
  @Any
  private Instance<ApprovalPreCheckHook> approvalPreCheckProcesses;

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    try {
      JSONObject jsonError = new JSONObject();
      jsonError.put("message", "Method not supported: GET");

      JSONObject result = new JSONObject();
      result.put("status", 1);
      result.put("error", jsonError);
      response.setContentType("application/json");
      response.setCharacterEncoding("UTF-8");
      PrintWriter out = response.getWriter();
      out.print(result.toString());
      out.flush();
    } catch (Exception e) {
      log.error("Error in CheckApproval: ", e);
    }
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    // always set the cors headers
    AllowedCrossDomainsHandler.getInstance().setCORSHeaders(request, response);

    OBContext.setAdminMode(false);
    try {
      JSONObject attributes = new JSONObject();
      JSONArray approvalType = new JSONArray();
      String terminal = request.getParameter("terminal");
      String username = request.getParameter("user");
      String password = request.getParameter("password");
      if (request.getParameter("attributes") != null) {
        attributes = new JSONObject(request.getParameter("attributes"));
      }
      Organization store = getTerminalStore(terminal);
      final String organization = store.getId();
      final String client = store.getClient().getId();

      if (request.getParameter("approvalType") != null) {
        approvalType = new JSONArray(request.getParameter("approvalType"));
      }
      executeApprovalPreCheckHook(username, password, terminal, approvalType, attributes);
      JSONObject result = new JSONObject();

      Optional<User> supervisor = PasswordHash.getUserWithPassword(username, password);
      if (!supervisor.isPresent()) {
        result.put("status", 1);
        JSONObject jsonError = new JSONObject();
        jsonError.put("message", OBMessageUtils.getI18NMessage("OBPOS_InvalidUserPassword", null));
        result.put("error", jsonError);
      } else {
        String supervisorId = supervisor.get().getId();

        List<String> approvals = new ArrayList<>(approvalType.length());
        for (int i = 0; i < approvalType.length(); i++) {
          approvals.add(approvalType.getString(i));
        }

        Set<String> naturalTreeOrgList = OBContext.getOBContext()
            .getOrganizationStructureProvider(client)
            .getNaturalTree(organization);

        // @formatter:off
        String hqlQuery =
              "select p.property"
            + "  from ADPreference as p"
            + " where active = true"
            + "   and (case when length(searchKey)<>1 then 'X' else to_char(searchKey) end) = 'Y'"
            + "   and (userContact.id = :user"
            + "        or exists (from ADUserRoles r"
            + "                  where r.role = p.visibleAtRole"
            + "                    and r.userContact.id = :user"
            + "                    and r.active=true))"
            + "   and (p.visibleAtOrganization.id = :org "
            + "        or p.visibleAtOrganization.id in (:orgList) "
            + "        or p.visibleAtOrganization is null)"
            + "   and property in (:approvals)"
            + " group by p.property";
        // @formatter:on

        List<String> preferenceList = OBDal.getInstance()
            .getSession()
            .createQuery(hqlQuery, String.class)
            .setParameter("user", supervisorId)
            .setParameter("org", organization)
            .setParameterList("orgList", naturalTreeOrgList)
            .setParameterList("approvals", approvals)
            .list();

        result.put("status", 0);
        JSONObject jsonData = new JSONObject();
        jsonData.put("userId", supervisorId);
        jsonData.put("canApprove", preferenceList.size() == approvalType.length());
        // we need to send the types that can be approved because of the case of n approvals
        // required but only some of them accepted
        jsonData.put("approvableTypes", new JSONArray(preferenceList));
        result.put("data", jsonData);

        executeApprovalCheckHook(username, password, terminal, approvalType, attributes);
        if (attributes.has("msg")) {
          result.put("status", 1);
          JSONObject jsonError = new JSONObject();
          jsonError.put("message", attributes.getString("msg"));
          result.put("error", jsonError);
        }
      }
      response.setContentType("application/json");
      response.setCharacterEncoding("UTF-8");
      PrintWriter out = response.getWriter();
      out.print(result.toString());
      out.flush();
    } catch (JSONException e) {
      log.error("Error while checking user can approve and executing CheckApproval hooks: "
          + e.getMessage(), e);
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  private void executeApprovalCheckHook(String username, String password, String terminal,
      JSONArray approvalType, JSONObject attributes) {
    for (Iterator<ApprovalCheckHook> processIterator = approvalCheckProcesses
        .iterator(); processIterator.hasNext();) {
      ApprovalCheckHook process = processIterator.next();
      try {
        process.exec(username, password, terminal, approvalType, attributes);
      } catch (Exception e) {
        log.error("Error while executing post approval check processes: " + e.getMessage(), e);
      }
    }
  }

  private void executeApprovalPreCheckHook(String username, String password, String terminal,
      JSONArray approvalType, JSONObject attributes) {
    for (Iterator<ApprovalPreCheckHook> processIterator = approvalPreCheckProcesses
        .iterator(); processIterator.hasNext();) {
      ApprovalPreCheckHook process = processIterator.next();
      try {
        process.exec(username, password, terminal, approvalType, attributes);
      } catch (Exception e) {
        log.error("Error while executing pre approval check processes: " + e.getMessage(), e);
      }
    }
  }

  private Organization getTerminalStore(String posTerminal) {
    OBCriteria<OBPOSApplications> terminalCriteria = OBDal.getInstance()
        .createCriteria(OBPOSApplications.class);
    terminalCriteria.setFilterOnReadableClients(false);
    terminalCriteria.setFilterOnReadableOrganization(false);
    terminalCriteria.add(Restrictions.eq(OBPOSApplications.PROPERTY_SEARCHKEY, posTerminal));
    OBPOSApplications terminal = (OBPOSApplications) terminalCriteria.uniqueResult();
    return terminal.getOrganization();
  }
}
