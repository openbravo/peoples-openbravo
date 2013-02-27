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
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.Query;
import org.openbravo.base.session.OBPropertiesProvider;
import org.openbravo.client.kernel.KernelConstants;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.service.json.JsonUtils;

public class LoginUtilsServlet extends WebServiceAbstractServlet {

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
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {

    OBContext.setAdminMode(false);

    final String terminalName = request.getParameter("terminalName");
    final String command = request.getParameter("command");

    JSONObject result = new JSONObject();
    JSONObject resp = new JSONObject();
    JSONArray data = new JSONArray();
    JSONObject item = null;
    try {
      if (command.equals("preRenderActions")) {
        int queryCount = 0;

        final String strClient = getClientOrgIds(terminalName)[0];

        item = new JSONObject();
        item.put("strClient", strClient);
        queryCount++;
        data.put(item);

        queryCount++;
        data.put(LabelsComponent.getLabels(null));

        item = new JSONObject();
        final Properties props = OBPropertiesProvider.getInstance().getOpenbravoProperties();
        item.put("date", props.getProperty(KernelConstants.DATE_FORMAT_PROPERTY, "dd-MM-yyyy"));
        item.put("dateTime",
            props.getProperty(KernelConstants.DATETIME_FORMAT_PROPERTY, "dd-MM-yyyy HH:mm:ss"));
        queryCount++;
        data.put(item);

        resp.put("startRow", 0);
        resp.put("endRow", (queryCount == 0 ? 0 : queryCount - 1));
        resp.put("totalRows", queryCount);
        resp.put("data", data);

        result.append("response", resp);
        writeResult(response, result.toString());
      } else if (command.equals("companyLogo")) {
        int queryCount = 1;

        String strClient = getClientOrgIds(terminalName)[0];
        if ("none".equals(strClient)) {
          strClient = "0";
        }

        String hqlCompanyImage = "select image.mimetype, image.bindaryData "
            + "from ADImage image, ClientInformation clientInfo "
            + "where clientInfo.obposCompanyLoginImage = image.id and clientInfo.client.id = :theClientId";
        Query qryCompanyImage = OBDal.getInstance().getSession().createQuery(hqlCompanyImage);
        qryCompanyImage.setParameter("theClientId", strClient);
        String companyImageData = "../../utility/ShowImageLogo?logo=yourcompanylogin";
        for (Object qryCompanyImageObject : qryCompanyImage.list()) {
          final Object[] qryCompanyImageObjectItem = (Object[]) qryCompanyImageObject;
          companyImageData = "data:"
              + qryCompanyImageObjectItem[0].toString()
              + ";base64,"
              + org.apache.commons.codec.binary.Base64
                  .encodeBase64String((byte[]) qryCompanyImageObjectItem[1]);
        }
        item = new JSONObject();
        item.put("logoUrl", companyImageData);
        data.put(item);

        resp.put("startRow", 0);
        resp.put("endRow", (queryCount == 0 ? 0 : queryCount - 1));
        resp.put("totalRows", queryCount);
        resp.put("data", data);

        result.append("response", resp);
        writeResult(response, result.toString());
      } else if (command.equals("userImages")) {

        String strClient = getClientOrgIds(terminalName)[0];
        String strOrg = getClientOrgIds(terminalName)[1];
        if ("none".equals(strClient)) {
          strClient = "0";
        }
        if ("none".equals(strOrg)) {
          strOrg = "0";
        }

        // Get the user name and username list with the following criteria
        // * Belongs to a "Role" which has the "Web POS" form as an allowed one
        // * Has access to the organization the terminal is defined in
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
        int queryCount = 0;

        for (Object qryUserObject : qryUser.list()) {
          queryCount++;
          final Object[] qryUserObjectItem = (Object[]) qryUserObject;

          item = new JSONObject();
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