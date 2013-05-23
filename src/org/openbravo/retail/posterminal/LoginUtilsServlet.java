/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.retail.posterminal;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.Query;
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.client.kernel.RequestContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.mobile.core.login.MobileCoreLoginUtilsServlet;

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
    final String terminalName = request.getParameter("terminalName");

    final String hqlUser = "select distinct user.name, user.username, user.id "
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
        + "formAccess.specialForm.id = :webPOSFormId " + "order by user.name";
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
    if (RequestContext.get().getSessionAttribute("POSTerminal") == null) {
      final VariablesSecureApp vars = new VariablesSecureApp(request);
      final String terminalSearchKey = vars.getStringParameter("terminalName");
      OBCriteria<OBPOSApplications> qApp = OBDal.getInstance().createCriteria(
          OBPOSApplications.class);
      qApp.add(Restrictions.eq(OBPOSApplications.PROPERTY_SEARCHKEY, terminalSearchKey));
      qApp.setFilterOnReadableOrganization(false);
      qApp.setFilterOnReadableClients(false);
      List<OBPOSApplications> apps = qApp.list();
      OBPOSApplications terminal = apps.get(0);
      RequestContext.get().setSessionAttribute("POSTerminal", terminal.getId());
    }
    return super.getPrerrenderData(request);
  }

  @Override
  protected String getModuleId() {
    return POSConstants.MODULE_ID;
  }
}