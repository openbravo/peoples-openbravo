/*
 ************************************************************************************
 * Copyright (C) 2012-2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.retail.posterminal;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.criterion.Restrictions;
import org.hibernate.query.Query;
import org.openbravo.authentication.AuthenticationException;
import org.openbravo.authentication.AuthenticationManager;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.client.kernel.RequestContext;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.businessUtility.Preferences;
import org.openbravo.erpCommon.businessUtility.Preferences.QueryFilter;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.erpCommon.utility.PropertyException;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.mobile.core.MobileServerDefinition;
import org.openbravo.mobile.core.MobileServerOrganization;
import org.openbravo.mobile.core.login.MobileCoreLoginUtilsServlet;
import org.openbravo.mobile.core.servercontroller.MobileServerUtils;
import org.openbravo.model.ad.access.FormAccess;
import org.openbravo.model.ad.access.RoleOrganization;
import org.openbravo.model.ad.access.User;
import org.openbravo.model.ad.access.UserRoles;
import org.openbravo.model.ad.system.Client;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.retail.posterminal.utility.OBPOSPrintTemplateReader;
import org.openbravo.service.db.DalConnectionProvider;

public class LoginUtilsServlet extends MobileCoreLoginUtilsServlet {
  public static final Logger log = LogManager.getLogger();
  private static final long serialVersionUID = 1L;

  private String[] getClientOrgIds(String terminalName) {
    final String hqlOrg = "select terminal.organization.client.id, terminal.organization.id "
        + "from OBPOS_Applications terminal " + "where terminal.searchKey = :theTerminalSearchKey";
    Query<Object[]> qryOrg = OBDal.getInstance().getSession().createQuery(hqlOrg, Object[].class);
    qryOrg.setParameter("theTerminalSearchKey", terminalName);
    qryOrg.setMaxResults(1);

    String strClient = "none";
    String strOrg = "none";

    if (qryOrg.uniqueResult() != null) {
      final Object[] orgResult = qryOrg.uniqueResult();
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

    OBCriteria<OBPOSApplications> terminalCriteria = OBDal.getInstance()
        .createCriteria(OBPOSApplications.class);
    terminalCriteria.add(Restrictions.eq(OBPOSApplications.PROPERTY_SEARCHKEY, terminalName));
    terminalCriteria.setFilterOnReadableOrganization(false);
    terminalCriteria.setFilterOnReadableClients(false);

    List<OBPOSApplications> terminalList = terminalCriteria.list();
    if (terminalList.size() != 0) {
      OBPOSApplications terminalObj = terminalList.get(0);

      List<String> naturalTreeOrgList = new ArrayList<String>(OBContext.getOBContext()
          .getOrganizationStructureProvider(terminalObj.getClient().getId())
          .getNaturalTree(terminalObj.getOrganization().getId()));

      final String extraFilter = !doFilterUserOnlyByTerminalAccessPreference(
          approvalType.length() > 0 ? true : false)
              ? "(not exists(from OBPOS_TerminalAccess ta2 where ta2.userContact = user)) or "
              : "";

      String hqlUser = "select distinct user.name, user.username, user.id, user.firstName, user.lastName "
          + "from ADUser user, ADUserRoles userRoles, ADRole role, ADFormAccess formAccess, "
          + "OBPOS_Applications terminal, ADRoleOrganization ro, OBPOS_TerminalAccess ta "
          + (approvalType.length() > 0 ? ", ADPreference p " : "")
          + "where user.active = true and user.username is not null and user.password is not null and "
          + "userRoles.active = true and role.active = true and formAccess.active = true and "
          + "terminal.searchKey = :theTerminalSearchKey and "
          + "user.id = userRoles.userContact.id and userRoles.role.id = role.id and "
          + "role.id = formAccess.role.id and role.forPortalUsers = false and "
          + "ro.role = role and ro.organization = terminal.organization and "
          + "formAccess.specialForm.id = :webPOSFormId and "
          + "((user.organization.id in (:orgList)) or (terminal.organization.id in (:orgList))) and "
          + "(" + extraFilter + " (ta.userContact = user and ta.pOSTerminal=terminal)) ";

      // checking supervisor users for sent approval type
      Map<String, String> iterParameter = new HashMap<>();
      if (approvalType.length() > 0) {
        hqlUser += "and (";
        for (int i = 0; i < approvalType.length(); i++) {
          hqlUser += "(p.property = :iter" + i + " "
              + "and p.active = true and to_char(p.searchKey) = 'Y' "
              + "and (p.userContact = user or p.visibleAtRole = role) "
              + "and (p.visibleAtOrganization = terminal.organization "
              + "or p.visibleAtOrganization.id in (:orgList) "
              + "or p.visibleAtOrganization is null)) or ";
          iterParameter.put("iter" + i, approvalType.getString(i));
        }
        hqlUser += "(1 = 0)) ";
      }
      hqlUser += "order by user.name";

      Query<Object[]> qryUser = OBDal.getInstance()
          .getSession()
          .createQuery(hqlUser, Object[].class);
      qryUser.setParameter("theTerminalSearchKey", terminalName);
      qryUser.setParameter("webPOSFormId", "B7B7675269CD4D44B628A2C6CF01244F");
      qryUser.setParameterList("orgList", naturalTreeOrgList);
      qryUser.setProperties(iterParameter);

      for (Object[] qryUserObjectItem : qryUser.list()) {
        JSONObject item = new JSONObject();
        item.put("name", qryUserObjectItem[0]);
        item.put("userName", qryUserObjectItem[1]);
        item.put("userId", qryUserObjectItem[2]);
        item.put("firstName", qryUserObjectItem[3]);
        item.put("lastName", qryUserObjectItem[4]);

        // Get the image for the current user
        String hqlImage = "select image.mimetype, image.bindaryData "
            + "from ADImage image, ADUser user "
            + "where user.image = image.id and user.id = :theUserId";
        Query<Object[]> qryImage = OBDal.getInstance()
            .getSession()
            .createQuery(hqlImage, Object[].class);
        qryImage.setParameter("theUserId", qryUserObjectItem[2].toString());
        String imageData = "none";

        for (Object[] qryImageObjectItem : qryImage.list()) {
          imageData = "data:" + qryImageObjectItem[0].toString() + ";base64,"
              + org.apache.commons.codec.binary.Base64
                  .encodeBase64String((byte[]) qryImageObjectItem[1]);
        }
        item.put("image", imageData);

        data.put(item);
      }
    }
    result.put("data", data);
    return result;
  }

  public static boolean doFilterUserOnlyByTerminalAccessPreference() {
    return doFilterUserOnlyByTerminalAccessPreference(true);
  }

  public static boolean doFilterUserOnlyByTerminalAccessPreference(boolean checkContext) {
    try {
      OBContext.setAdminMode(false);
      String value;
      if (checkContext) {
        value = Preferences.getPreferenceValue("OBPOS_FILTER_USER_ONLY_BY_TERMINAL_ACCESS", true,
            OBContext.getOBContext().getCurrentClient(),
            OBContext.getOBContext().getCurrentOrganization(), OBContext.getOBContext().getUser(),
            OBContext.getOBContext().getRole(), null);
      } else {
        Map<QueryFilter, Boolean> terminalAccessQueryFilters = new HashMap<>();
        terminalAccessQueryFilters.put(QueryFilter.ACTIVE, true);
        terminalAccessQueryFilters.put(QueryFilter.CLIENT, false);
        terminalAccessQueryFilters.put(QueryFilter.ORGANIZATION, false);
        value = Preferences.getPreferenceValue("OBPOS_FILTER_USER_ONLY_BY_TERMINAL_ACCESS", true,
            null, null, null, null, (String) null, terminalAccessQueryFilters);
      }
      return "Y".equals(value);
    } catch (Exception e) {
      return false;
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  @Override
  protected JSONObject getPrerrenderData(HttpServletRequest request) throws JSONException {

    JSONObject result = super.getPrerrenderData(request);

    if (OBContext.getOBContext().getUser().getId().equals("0")) {
      final VariablesSecureApp vars = new VariablesSecureApp(request);
      final String terminalSearchKey = vars.getStringParameter("terminalName");
      OBCriteria<OBPOSApplications> qApp = OBDal.getInstance()
          .createCriteria(OBPOSApplications.class);
      qApp.add(Restrictions.eq(OBPOSApplications.PROPERTY_SEARCHKEY, terminalSearchKey));
      qApp.setFilterOnReadableOrganization(false);
      qApp.setFilterOnReadableClients(false);
      List<OBPOSApplications> apps = qApp.list();
      if (apps.size() == 1) {
        OBPOSApplications terminal = apps.get(0);
        RequestContext.get().setSessionAttribute("POSTerminal", terminal.getId());

        result.put("appCaption",
            terminal.getIdentifier() + " - " + terminal.getOrganization().getIdentifier());
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
    String cacheSessionId = obj.getString("cacheSessionId");

    OBCriteria<OBPOSApplications> qApp = OBDal.getInstance()
        .createCriteria(OBPOSApplications.class);
    qApp.add(Restrictions.eq(OBPOSApplications.PROPERTY_TERMINALKEY, terminalKeyIdentifier));
    qApp.setFilterOnReadableOrganization(false);
    qApp.setFilterOnReadableClients(false);
    List<OBPOSApplications> apps = qApp.list();
    if (apps.size() == 1) {
      OBPOSApplications terminal = ((OBPOSApplications) apps.get(0));
      if (terminal.isLinked() && (!(terminal.getCurrentCacheSession().equals(cacheSessionId)
          && terminal.getTerminalKey().equals(terminalKeyIdentifier)))) {
        result.put("exception", "OBPOS_TerminalAlreadyLinked");
        return result;
      }

      try {
        AuthenticationManager authManager = AuthenticationManager.getAuthenticationManager(this);
        HttpServletResponse response = RequestContext.get().getResponse();
        userId = authManager.authenticate(request, response);
        terminal = OBDal.getInstance().get(OBPOSApplications.class, terminal.getId());
      } catch (AuthenticationException ae) {
        ConnectionProvider cp = new DalConnectionProvider(false);
        Client systemClient = OBDal.getInstance().get(Client.class, "0");
        throw new AuthenticationException(
            Utility.messageBD(cp, ae.getMessage(), systemClient.getLanguage().getLanguage()));
      } catch (Exception e) {
        throw new AuthenticationException(e.getMessage());
      }

      if (userId != null && !userId.isEmpty()) {
        // Terminal access will be checked to ensure that the user has access to the terminal
        OBQuery<TerminalAccess> accessCrit = OBDal.getInstance()
            .createQuery(TerminalAccess.class, "where userContact.id = :userId");
        accessCrit.setFilterOnReadableClients(false);
        accessCrit.setFilterOnReadableOrganization(false);
        accessCrit.setNamedParameter("userId", userId);
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

        OBQuery<OBPOSApplications> appQry = OBDal.getInstance()
            .createQuery(OBPOSApplications.class,
                " as e where e.id = :terminalId and ((ad_isorgincluded("
                    + "(select organization from ADUser where id= :userId)"
                    + ", e.organization, e.client.id) <> -1) or "
                    + "(ad_isorgincluded(e.organization, "
                    + "(select organization from ADUser where id= :userId)"
                    + ", e.client.id) <> -1)) ");
        appQry.setFilterOnReadableClients(false);
        appQry.setFilterOnReadableOrganization(false);
        appQry.setNamedParameter("terminalId", terminal.getId());
        appQry.setNamedParameter("userId", userId);
        List<OBPOSApplications> appList = appQry.list();
        if (appList.isEmpty()) {
          result.put("exception", "OBPOS_USER_NO_ACCESS_TO_TERMINAL_TITLE");
          return result;
        }

        OBCriteria<User> userQ = OBDal.getInstance().createCriteria(User.class);
        userQ.add(Restrictions.eq(OBPOSApplications.PROPERTY_ID, userId));
        userQ.setFilterOnReadableOrganization(false);
        userQ.setFilterOnReadableClients(false);
        List<User> userList = userQ.list();
        if (userList.size() == 1) {
          User user = ((User) userList.get(0));

          boolean haveOrgAccess = false;
          for (UserRoles userRole : user.getADUserRolesList()) {
            if (!userRole.isActive() || !userRole.getRole().isActive()) {
              continue;
            }
            for (RoleOrganization roleOrg : userRole.getRole().getADRoleOrganizationList()) {
              if (roleOrg.isActive()
                  && roleOrg.getOrganization().equals(terminal.getOrganization())) {
                haveOrgAccess = true;
                break;
              }
            }
          }
          if (!haveOrgAccess) {
            result.put("exception", "OBPOS_USER_NO_ACCESS_TO_TERMINAL_TITLE");
            return result;
          }

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
          result.put("appCaption",
              terminal.getIdentifier() + " - " + terminal.getOrganization().getIdentifier());
          result.put("servers", getServers(terminal));
          result.put("safeBoxInfo", getTerminalSafeBoxes(terminal));
          result.put("services", getServices());
          result.put("processes", getProcesses());
          terminal.setLinked(true);
          terminal.setCurrentCacheSession(cacheSessionId);

          OBDal.getInstance().save(terminal);

          try {
            OBDal.getInstance().getConnection().commit();
            log.info("[termAuth] Terminal " + terminal.getIdentifier() + "("
                + terminal.getCurrentCacheSession() + ") has been linked");
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

    HttpSession session = request.getSession(false);
    if (session != null) {
      // finally invalidate the session (this event will be caught by the session listener
      session.invalidate();
    }

    return result;
  }

  private JSONObject getTerminalSafeBoxes(OBPOSApplications terminal) throws JSONException {
    JSONObject result = new JSONObject();

    if (terminal.getObposTerminaltype().isSafebox()) {
      result.put("isSafeBox", true);
      // Get all safeBoxes defined and their payment methods
      OBCriteria<OBPOSSafeBox> safeBoxCrt = OBDal.getInstance().createCriteria(OBPOSSafeBox.class);
      safeBoxCrt.setFilterOnReadableOrganization(false);
      safeBoxCrt.setFilterOnReadableClients(false);
      if (safeBoxCrt.count() > 0) {
        JSONArray safeBoxArray = new JSONArray();
        for (OBPOSSafeBox safeBox : safeBoxCrt.list()) {
          final JSONObject jsonSafeBoxObject = new JSONObject();
          jsonSafeBoxObject.put("name", safeBox.getCommercialName());
          jsonSafeBoxObject.put("searchKey", safeBox.getSearchKey());
          if (safeBox.getUser() != null) {
            jsonSafeBoxObject.put("userId", safeBox.getUser().getId());
            jsonSafeBoxObject.put("userName", safeBox.getUser().getName());
          }
          JSONArray safeBoxPaymentMethodsArray = new JSONArray();
          for (OBPOSSafeBoxPaymentMethod safeBoxPaymentMethod : safeBox
              .getOBPOSSafeBoxPaymentMethodList()) {
            // Get SafeBoxes payment methods
            final JSONObject jsonSafeBoxPaymentMethodObject = new JSONObject();
            jsonSafeBoxPaymentMethodObject.put("paymentMethodId",
                safeBoxPaymentMethod.getPaymentMethod().getId());
            jsonSafeBoxPaymentMethodObject.put("paymentMethodName",
                safeBoxPaymentMethod.getPaymentMethod().getName());
            jsonSafeBoxPaymentMethodObject.put("financialAccountId",
                safeBoxPaymentMethod.getFINFinancialaccount().getId());
            jsonSafeBoxPaymentMethodObject.put("financialAccountName",
                safeBoxPaymentMethod.getFINFinancialaccount().getName());
            jsonSafeBoxPaymentMethodObject.put("keepFixedAmount",
                safeBoxPaymentMethod.isKeepFixedAmount());
            if (safeBoxPaymentMethod.isKeepFixedAmount()) {
              jsonSafeBoxPaymentMethodObject.put("amountToKeep", safeBoxPaymentMethod.getAmount());
            }
            jsonSafeBoxPaymentMethodObject.put("allowVariableAmount",
                safeBoxPaymentMethod.isAllowVariableAmount());
            jsonSafeBoxPaymentMethodObject.put("allowNotToMove",
                safeBoxPaymentMethod.isAllowNotToMove());
            jsonSafeBoxPaymentMethodObject.put("allowMoveEverything",
                safeBoxPaymentMethod.isAllowMoveEverything());
            jsonSafeBoxPaymentMethodObject.put("countDifferenceLimit",
                safeBoxPaymentMethod.getCountDifferenceLimit());
            safeBoxPaymentMethodsArray.put(jsonSafeBoxPaymentMethodObject);
          }
          jsonSafeBoxObject.put("paymentMethods", safeBoxPaymentMethodsArray);
          safeBoxArray.put(jsonSafeBoxObject);
        }
        result.put("safeBoxes", safeBoxArray);
      } else {
        result.put("exception", "OBPOS_NoSafeBoxesDefined");
        return result;
      }
    } else {
      result.put("isSafeBox", false);
    }

    return result;
  }

  @Override
  protected JSONObject initActions(HttpServletRequest request) throws JSONException {
    JSONObject result = super.initActions(request);

    final String terminalName = request.getParameter("terminalName");
    JSONObject properties = (JSONObject) result.get("properties");
    OBPOSApplications terminal = null;
    if (terminalName != null) {
      OBCriteria<OBPOSApplications> qApp = OBDal.getInstance()
          .createCriteria(OBPOSApplications.class);
      qApp.add(Restrictions.eq(OBPOSApplications.PROPERTY_SEARCHKEY, terminalName));
      qApp.setFilterOnReadableOrganization(false);
      qApp.setFilterOnReadableClients(false);
      List<OBPOSApplications> apps = qApp.list();
      if (apps.size() == 1) {
        terminal = ((OBPOSApplications) apps.get(0));
        properties.put("servers", getServers(terminal));
        result.put("safeBoxInfo", getTerminalSafeBoxes(terminal));
      }
    }
    properties.put("templateVersion",
        OBPOSPrintTemplateReader.getInstance().getPrintTemplatesIdentifier());

    String terminalAuthenticationValue = "";
    String maxAllowedTimeInOfflineValue = "";
    String offlineSessionTimeExpirationValue = "";
    String currentPropertyToLaunchError = "";

    try {
      // Get terminal terminalAuthentication
      currentPropertyToLaunchError = "errorReadingTerminalAuthentication";
      Map<QueryFilter, Boolean> terminalAuthenticationQueryFilters = new HashMap<>();
      terminalAuthenticationQueryFilters.put(QueryFilter.ACTIVE, true);
      terminalAuthenticationQueryFilters.put(QueryFilter.CLIENT, false);
      terminalAuthenticationQueryFilters.put(QueryFilter.ORGANIZATION, false);
      terminalAuthenticationValue = Preferences.getPreferenceValue("OBPOS_TerminalAuthentication",
          true, null, null, null, null, (String) null, terminalAuthenticationQueryFilters);
      result.put("terminalAuthentication", terminalAuthenticationValue);
    } catch (PropertyException e) {
      result.put("terminalAuthentication", "Y");
      result.put(currentPropertyToLaunchError,
          OBMessageUtils.messageBD("OBPOS_errorWhileReadingTerminalAuthenticationPreference"));
    }

    try {
      // Get maxTimeInOffline preference
      currentPropertyToLaunchError = "errorReadingMaxAllowedTimeInOffline";
      Map<QueryFilter, Boolean> maxAllowedTimeInOfflineQueryFilters = new HashMap<>();
      maxAllowedTimeInOfflineQueryFilters.put(QueryFilter.ACTIVE, true);
      maxAllowedTimeInOfflineQueryFilters.put(QueryFilter.CLIENT, false);
      maxAllowedTimeInOfflineQueryFilters.put(QueryFilter.ORGANIZATION, false);
      maxAllowedTimeInOfflineValue = Preferences.getPreferenceValue("OBPOS_MaxTimeInOffline", true,
          null, null, null, null, (String) null, maxAllowedTimeInOfflineQueryFilters);
      result.put("maxTimeInOffline", maxAllowedTimeInOfflineValue);
    } catch (PropertyException e) {
      // Preference is not defined, max time in offline will not be set
      result.put(currentPropertyToLaunchError,
          OBMessageUtils.messageBD("OBPOS_errorWhileReadingMaxAllowedTimeInOfflinePreference"));
    }
    try {
      // Get offlineSessionTimeExpiration preference
      currentPropertyToLaunchError = "errorReadingOfflineSessionTimeExpiration";
      Map<QueryFilter, Boolean> offlineSessionTimeExpirationQueryFilters = new HashMap<>();
      offlineSessionTimeExpirationQueryFilters.put(QueryFilter.ACTIVE, true);
      offlineSessionTimeExpirationQueryFilters.put(QueryFilter.CLIENT, false);
      offlineSessionTimeExpirationQueryFilters.put(QueryFilter.ORGANIZATION, false);
      offlineSessionTimeExpirationValue = Preferences.getPreferenceValue(
          "OBPOS_offlineSessionTimeExpiration", true, null, null, null, null, (String) null,
          offlineSessionTimeExpirationQueryFilters);
      result.put("offlineSessionTimeExpiration", offlineSessionTimeExpirationValue);
    } catch (PropertyException e) {
      result.put("offlineSessionTimeExpiration", 60);
      result.put(currentPropertyToLaunchError, OBMessageUtils
          .messageBD("OBPOS_errorWhileReadingOfflineSessionTimeExpirationPreference"));
    }

    return result;
  }

  protected JSONArray getServers(OBPOSApplications terminal) throws JSONException {
    JSONArray respArray = new JSONArray();

    if (!MobileServerUtils.isMultiServerEnabled()) {
      return respArray;
    }

    OBQuery<MobileServerDefinition> servers = OBDal.getInstance()
        .createQuery(MobileServerDefinition.class,
            "client.id=:clientId order by " + MobileServerDefinition.PROPERTY_PRIORITY);
    servers.setFilterOnReadableClients(false);
    servers.setFilterOnReadableOrganization(false);
    servers.setNamedParameter("clientId", terminal.getClient().getId());

    List<MobileServerDefinition> serversList = servers.list();
    for (MobileServerDefinition server : serversList) {
      if (server.isAllorgs()) {
        respArray.put(createServerJSON(server));
      } else {
        String hql = "select mso from " + MobileServerOrganization.ENTITY_NAME + " as mso " //
            + "where mso." + MobileServerOrganization.PROPERTY_OBMOBCSERVERDEFINITION
            + " = :serverDefinition " //
            + "and mso." + MobileServerOrganization.PROPERTY_SERVERORG + " = :org";
        Query<Object> query = OBDal.getInstance().getSession().createQuery(hql, Object.class);

        query.setParameter("serverDefinition", server);
        query.setParameter("org", terminal.getOrganization());
        if (!query.list().isEmpty()) {
          respArray.put(createServerJSON(server));
        }
      }
    }

    return respArray;
  }

  @Override
  public String getDefaultDecimalSymbol() {
    String decimalSymbol = (String) POSUtils.getPropertyInOrgTree(
        OBContext.getOBContext().getCurrentOrganization(),
        Organization.PROPERTY_OBPOSFORMATDECIMAL);
    if (StringUtils.isEmpty(decimalSymbol)) {
      return super.getDefaultDecimalSymbol();
    } else {
      return StringEscapeUtils.escapeJavaScript(decimalSymbol);
    }
  }

  @Override
  public String getDefaultGroupingSymbol() {
    String groupSymbol = (String) POSUtils.getPropertyInOrgTree(
        OBContext.getOBContext().getCurrentOrganization(), Organization.PROPERTY_OBPOSFORMATGROUP);
    if (StringUtils.isEmpty(groupSymbol)) {
      return super.getDefaultGroupingSymbol();
    } else {
      return StringEscapeUtils.escapeJavaScript(groupSymbol);
    }
  }

  @Override
  public String getDateFormat() {
    String dateFormat = (String) POSUtils.getPropertyInOrgTree(
        OBContext.getOBContext().getCurrentOrganization(), Organization.PROPERTY_OBPOSDATEFORMAT);
    if (StringUtils.isEmpty(dateFormat)) {
      return super.getDateFormat();
    } else {
      return dateFormat;
    }
  }

  @Override
  protected String getModuleId() {
    return POSConstants.MODULE_ID;
  }
}
