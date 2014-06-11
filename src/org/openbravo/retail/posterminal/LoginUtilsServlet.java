/*
 ************************************************************************************
 * Copyright (C) 2012-2013 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.retail.posterminal;

import java.sql.SQLException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.Query;
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.secureApp.LoginUtils;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.client.kernel.RequestContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.erpCommon.businessUtility.Preferences;
import org.openbravo.erpCommon.utility.PropertyException;
import org.openbravo.mobile.core.login.MobileCoreLoginUtilsServlet;
import org.openbravo.model.ad.access.FormAccess;
import org.openbravo.model.ad.access.User;
import org.openbravo.model.ad.access.UserRoles;
import org.openbravo.service.db.DalConnectionProvider;

public class LoginUtilsServlet extends MobileCoreLoginUtilsServlet {

  private static final Logger log = Logger.getLogger(LoginUtilsServlet.class);

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

  private boolean hasADFormAccess(UserRoles userRole) {
    for (FormAccess form : userRole.getRole().getADFormAccessList()) {
      if (form.getSpecialForm().getId().equals(POSUtils.WEB_POS_FORM_ID)) {
        return true;
      }
    }
    return false;
  }

  @Override
  protected JSONObject getCompanyLogo(HttpServletRequest request) throws JSONException {
    JSONObject result = new JSONObject();
    final String terminalName = request.getParameter("terminalName");
    String clientId = getClientOrgIds(terminalName)[0];
    if ("none".equals(clientId)) {
      clientId = "0";
    }

    result.put("logoUrl", getClientLogoData(clientId));
    return result;
  }

  @Override
  protected JSONObject getUserImages(HttpServletRequest request) throws JSONException {
    JSONObject result = new JSONObject();
    JSONArray data = new JSONArray();
    JSONArray approvalType = new JSONArray();
    final String terminalName = request.getParameter("terminalName");
    if (request.getParameter("approvalType") != null) {
      approvalType = new JSONArray(request.getParameter("approvalType"));
    }

    String hqlUser = "select distinct user.name, user.username, user.id "
        + "from ADUser user, ADUserRoles userRoles, ADRole role, "
        + "ADFormAccess formAccess, OBPOS_Applications terminal "
        + "where user.active = true and "
        + "userRoles.active = true and "
        + "role.active = true and "
        + "formAccess.active = true and "
        + "user.username is not null and "
        + "user.password is not null and "
        + "exists (from ADRoleOrganization ro where ro.role = role and ro.organization = terminal.organization) and "
        + "(not exists(from OBPOS_TerminalAccess ta where ta.userContact = user) or exists(from OBPOS_TerminalAccess ta where ta.userContact = user and ta.pOSTerminal=terminal)) and "
        + "terminal.searchKey = :theTerminalSearchKey and "
        + "user.id = userRoles.userContact.id and " + "userRoles.role.id = role.id and "
        + "userRoles.role.id = formAccess.role.id and "
        + "formAccess.specialForm.id = :webPOSFormId ";

    if (approvalType.length() != 0) {
      // checking supervisor users for sent approval type
      for (int i = 0; i < approvalType.length(); i++) {
        String iter = approvalType.getString(i);
        hqlUser += "and exists (from ADPreference as p" + //
            " where property = '" + iter + //
            "'   and active = true" + //
            "   and to_char(searchKey) = 'Y'" + //
            "   and (userContact = user" + //
            "        or exists (from ADUserRoles r" + //
            "                  where r.role = p.visibleAtRole" + //
            "                    and r.userContact = user))) ";
      }

    }

    hqlUser += "order by user.name";

    Query qryUser = OBDal.getInstance().getSession().createQuery(hqlUser);
    qryUser.setParameter("theTerminalSearchKey", terminalName);
    qryUser.setParameter("webPOSFormId", "B7B7675269CD4D44B628A2C6CF01244F");

    for (Object qryUserObject : qryUser.list()) {
      final Object[] qryUserObjectItem = (Object[]) qryUserObject;

      JSONObject item = new JSONObject();
      item.put("name", qryUserObjectItem[0]);
      item.put("userName", qryUserObjectItem[1]);

      // Get the image for the current user
      String hqlImage = "select image.mimetype, image.bindaryData "
          + "from ADImage image, ADUser user "
          + "where user.image = image.id and user.id = :theUserId";
      Query qryImage = OBDal.getInstance().getSession().createQuery(hqlImage);
      qryImage.setParameter("theUserId", qryUserObjectItem[2].toString());
      String imageData = "none";
      for (Object qryImageObject : qryImage.list()) {
        final Object[] qryImageObjectItem = (Object[]) qryImageObject;
        imageData = "data:"
            + qryImageObjectItem[0].toString()
            + ";base64,"
            + org.apache.commons.codec.binary.Base64
                .encodeBase64String((byte[]) qryImageObjectItem[1]);
      }
      item.put("image", imageData);

      // Get the session status for the current user
      String hqlSession = "select distinct session.username, session.sessionActive "
          + "from ADSession session "
          + "where session.username = :theUsername and session.sessionActive = 'Y' and "
          + "session.loginStatus = 'OBPOS_POS'";
      Query qrySession = OBDal.getInstance().getSession().createQuery(hqlSession);
      qrySession.setParameter("theUsername", qryUserObjectItem[1].toString());
      qrySession.setMaxResults(1);
      String sessionData = "false";
      if (qrySession.uniqueResult() != null) {
        sessionData = "true";
      }
      item.put("connected", sessionData);

      data.put(item);
    }
    result.put("data", data);
    return result;
  }

  @Override
  protected JSONObject getPrerrenderData(HttpServletRequest request) throws JSONException {

    JSONObject result = super.getPrerrenderData(request);

    if (RequestContext.get().getSessionAttribute("POSTerminal") == null) {
      final VariablesSecureApp vars = new VariablesSecureApp(request);
      final String terminalSearchKey = vars.getStringParameter("terminalName");
      OBCriteria<OBPOSApplications> qApp = OBDal.getInstance().createCriteria(
          OBPOSApplications.class);
      qApp.add(Restrictions.eq(OBPOSApplications.PROPERTY_SEARCHKEY, terminalSearchKey));
      qApp.setFilterOnReadableOrganization(false);
      qApp.setFilterOnReadableClients(false);
      List<OBPOSApplications> apps = qApp.list();
      if (apps.size() == 1) {
        OBPOSApplications terminal = apps.get(0);
        RequestContext.get().setSessionAttribute("POSTerminal", terminal.getId());

        result.put("appCaption", terminal.getIdentifier() + " - "
            + terminal.getOrganization().getIdentifier());
      }
    }
    return result;
  }

  @Override
  protected JSONObject preLogin(HttpServletRequest request) throws JSONException {
    String userId = "";
    boolean success = false;
    boolean hasAccess = false;
    JSONObject result = super.preLogin(request);
    Object params = request.getParameter("params");
    JSONObject obj = new JSONObject((String) params);
    String terminalKeyIdentifier = obj.getString("terminalKeyIdentifier");
    String username = obj.getString("username");
    String password = obj.getString("password");

    OBCriteria<OBPOSApplications> qApp = OBDal.getInstance()
        .createCriteria(OBPOSApplications.class);
    qApp.add(Restrictions.eq(OBPOSApplications.PROPERTY_TERMINALKEY, terminalKeyIdentifier));
    qApp.setFilterOnReadableOrganization(false);
    qApp.setFilterOnReadableClients(false);
    List<OBPOSApplications> apps = qApp.list();
    if (apps.size() == 1) {
      OBPOSApplications terminal = ((OBPOSApplications) apps.get(0));
      if (terminal.isLinked()) {
        result.put("exception", "OBPOS_TerminalAlreadyLinked");
        return result;
      }
      userId = LoginUtils.checkUserPassword(new DalConnectionProvider(false), username, password);
      if (userId != null) {
        // Terminal access will be checked to ensure that the user has access to the terminal
        OBQuery<TerminalAccess> accessCrit = OBDal.getInstance().createQuery(TerminalAccess.class,
            "where userContact.id='" + userId + "'");
        accessCrit.setFilterOnReadableClients(false);
        accessCrit.setFilterOnReadableOrganization(false);
        List<TerminalAccess> accessList = accessCrit.list();

        if (accessList.size() != 0) {
          for (TerminalAccess access : accessList) {
            if (access.getPOSTerminal().getSearchKey().equals(terminal.getSearchKey())) {
              hasAccess = true;
              break;
            }
          }
          if (!hasAccess) {
            result.put("exception", "OBPOS_USER_NO_ACCESS_TO_TERMINAL_TITLE");
            return result;
          }
        }
        OBCriteria<User> userQ = OBDal.getInstance().createCriteria(User.class);
        userQ.add(Restrictions.eq(OBPOSApplications.PROPERTY_ID, userId));
        userQ.setFilterOnReadableOrganization(false);
        userQ.setFilterOnReadableClients(false);
        List<User> userList = userQ.list();
        if (userList.size() == 1) {
          User user = ((User) userList.get(0));
          for (UserRoles userRole : user.getADUserRolesList()) {
            if (this.hasADFormAccess(userRole)) {
              success = true;
              break;
            }
          }
        }
        if (success) {
          RequestContext.get().setSessionAttribute("POSTerminal", terminal.getId());
          result.put("terminalName", terminal.getSearchKey());
          result.put("terminalKeyIdentifier", terminal.getTerminalKey());
          result.put("appCaption", terminal.getIdentifier() + " - "
              + terminal.getOrganization().getIdentifier());

          terminal.setLinked(true);
          OBDal.getInstance().save(terminal);
          try {
            OBDal.getInstance().getConnection().commit();
          } catch (SQLException e) {
            throw new JSONException(e);
          }
        } else {
          result.put("exception", "OBPOS_USERS_ROLE_NO_ACCESS_WEB_POS");
          return result;
        }

      } else {
        result.put("exception", "OBPOS_InvalidUserPassword");
        return result;
      }
    } else {
      result.put("exception", "OBPOS_WrongTerminalKeyIdentifier");
      return result;
    }

    return result;
  }

  @Override
  protected JSONObject initActions(HttpServletRequest request) throws JSONException {
    JSONObject result = super.initActions(request);
    String value;
    try {
      value = Preferences.getPreferenceValue("OBPOS_TerminalAuthentication", true, null, null,
          null, null, (String) null);
    } catch (PropertyException e) {
      result.put("terminalAuthentication", "N");
      return result;
    }
    result.put("terminalAuthentication", value);
    return result;
  }

  @Override
  protected String getModuleId() {
    return POSConstants.MODULE_ID;
  }
}