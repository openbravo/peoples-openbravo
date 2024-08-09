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
 * All portions are Copyright (C) 2008-2024 Openbravo SLU
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.service.web;

import java.io.IOException;
import java.io.Writer;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openbravo.authentication.AuthenticationException;
import org.openbravo.authentication.AuthenticationManager;
import org.openbravo.authentication.ExternalAuthenticationManager;
import org.openbravo.authentication.oauth2.OAuth2TokenAuthenticationManager;
import org.openbravo.authentication.oauth2.OAuth2TokenDataProvider;
import org.openbravo.base.exception.OBSecurityException;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.secureApp.AllowedCrossDomainsHandler;
import org.openbravo.base.session.OBPropertiesProvider;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.core.SessionHandler;
import org.openbravo.database.SessionInfo;

/**
 * This servlet has two main responsibilities: 1) authenticate, 2) set the correct {@link OBContext}
 * , and 3) translate Exceptions into the correct Http response code.
 * <p>
 * In regard to authentication: there is support for basic-authentication as well as url parameter
 * based authentication.
 * 
 * @author mtaal
 */

public class BaseWebServiceServlet extends HttpServlet {
  private static final Logger log = LogManager.getLogger();

  public static final String LOGIN_PARAM = "l";
  public static final String PASSWORD_PARAM = "p";

  private static final long serialVersionUID = 1L;

  private static Integer wsInactiveInterval = null;
  private static final int DEFAULT_WS_INACTIVE_INTERVAL = 60;

  @Inject
  private OAuth2TokenDataProvider oauth2TokenDataProvider;

  @Override
  protected final void service(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    final boolean sessionExists = request.getSession(false) != null;

    AllowedCrossDomainsHandler.getInstance().setCORSHeaders(request, response);

    // don't process any further requests otherwise sessions are created for OPTIONS
    // requests, the cors headers have already been set so can return
    if (request.getMethod().equals("OPTIONS")) {
      return;
    }

    // if a stateless webservice then set the stateless flag
    try {
      if (AuthenticationManager.isStatelessRequest(request)) {
        request.setAttribute(AuthenticationManager.STATELESS_REQUEST_PARAMETER, "true");
      } else {
        final WebService webservice = getWebService(request);
        if (webservice != null && AuthenticationManager.isStatelessService(webservice.getClass())) {
          request.setAttribute(AuthenticationManager.STATELESS_REQUEST_PARAMETER, "true");
        }
      }
    } catch (Throwable ignore) {
      // ignore on purpose as subclasses may manage the resolving of webservices in a different
      // way
      // ignore also for backward compatibility
    }

    String userId = null;
    try {
      AuthenticationManager authManager = getAuthorizationManager();
      userId = authManager.webServiceAuthenticate(request);
    } catch (AuthenticationException e) {
      final boolean sessionCreated = !sessionExists && null != request.getSession(false);
      if (sessionCreated && AuthenticationManager.isStatelessRequest(request)) {
        log.warn("Stateless request, still a session was created " + request.getRequestURL() + " "
            + request.getQueryString());
      }

      response.setStatus(HttpServletResponse.SC_FORBIDDEN);
      response.setContentType("text/plain;charset=UTF-8");
      final Writer w = response.getWriter();
      w.write(e.getMessage());
      w.close();
      return;
    }

    if (userId != null) {
      log.debug("WS accessed by userId " + userId);
      OBContext.setOBContext(UserContextCache.getInstance().getCreateOBContext(userId));
      OBContext.setOBContextInSession(request, OBContext.getOBContext());
      SessionInfo.setUserId(userId);
      SessionInfo.setProcessType("WS");
      SessionInfo.setProcessId("DAL");
      try {
        doService(request, response);
      } finally {
        final boolean sessionCreated = !sessionExists && null != request.getSession(false);
        if (sessionCreated && AuthenticationManager.isStatelessRequest(request)) {
          log.warn("Stateless request, still a session was created " + request.getRequestURL() + " "
              + request.getQueryString());
        }

        HttpSession session = request.getSession(false);
        if (sessionCreated && session != null) {
          // HttpSession for WS should typically expire fast
          // only update the expire interval if this session was created as a consequence of the ws
          // request. otherwise we would be updating the expire intervals of standard session, see
          // https://issues.openbravo.com/view.php?id=50872
          int maxExpireInterval = getWSInactiveInterval();
          if (maxExpireInterval == 0) {
            session.invalidate();
          } else {
            session.setMaxInactiveInterval(maxExpireInterval);
          }
        }
      }

    } else {
      log.debug("WS accessed by unauthenticated user, requesting authentication");
      // not logged in
      if (!"false".equals(request.getParameter("auth"))) {
        response.setHeader("WWW-Authenticate", "Basic realm=\"Openbravo\"");
      }
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }
  }

  private int getWSInactiveInterval() {
    if (wsInactiveInterval == null) {
      try {
        wsInactiveInterval = Integer.parseInt(OBPropertiesProvider.getInstance()
            .getOpenbravoProperties()
            .getProperty("ws.maxInactiveInterval", Integer.toString(DEFAULT_WS_INACTIVE_INTERVAL)));
      } catch (Exception e) {
        wsInactiveInterval = DEFAULT_WS_INACTIVE_INTERVAL;
      }
      log.info("Sessions for WS calls expire after " + wsInactiveInterval
          + " seconds. This can be configured with ws.maxInactiveInterval property.");
    }

    return wsInactiveInterval;
  }

  protected WebService getWebService(HttpServletRequest request) {
    final Object o = OBProvider.getInstance().getMostSpecificService(request.getPathInfo());
    if (o instanceof WebService) {
      return (WebService) o;
    }
    return null;
  }

  protected void callServiceInSuper(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    super.service(request, response);
  }

  protected void doService(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    try {
      if (OBContext.getOBContext() != null) {
        if (OBContext.getOBContext().isPortalRole()) {
          // Portal users are not granted to direct web services
          log.error("Portal user " + OBContext.getOBContext().getUser() + " with role "
              + OBContext.getOBContext().getRole()
              + " is trying to access to non granted web service " + request.getRequestURL());
          throw new OBSecurityException("Web Services are not granted to Portal roles");
        } else if (!OBContext.getOBContext().isWebServiceEnabled()) {
          log.error("User " + OBContext.getOBContext().getUser() + " with role "
              + OBContext.getOBContext().getRole()
              + " is trying to access to non granted web service " + request.getRequestURL());
          throw new OBSecurityException(
              "Web Services are not granted to " + OBContext.getOBContext().getRole() + " role");
        }
      }
      super.service(request, response);
      response.setStatus(200);
    } catch (final InvalidRequestException e) {
      SessionHandler.getInstance().setDoRollback(true);
      response.setStatus(400);
      final Writer w = response.getWriter();
      w.write(WebServiceUtil.getInstance().createErrorXML(e));
      w.close();
    } catch (final InvalidContentException e) {
      SessionHandler.getInstance().setDoRollback(true);
      response.setStatus(409);
      final Writer w = response.getWriter();
      w.write(WebServiceUtil.getInstance().createErrorXML(e));
      w.close();
    } catch (final ResourceNotFoundException e) {
      SessionHandler.getInstance().setDoRollback(true);
      response.setStatus(404);
      final Writer w = response.getWriter();
      w.write(WebServiceUtil.getInstance().createErrorXML(e));
      w.close();
    } catch (final OBSecurityException e) {
      SessionHandler.getInstance().setDoRollback(true);
      response.setStatus(401);
      final Writer w = response.getWriter();
      w.write(WebServiceUtil.getInstance().createErrorXML(e));
      w.close();
    } catch (final Throwable t) {
      log.error(t.getMessage(), t);
      SessionHandler.getInstance().setDoRollback(true);
      response.setStatus(500);
      final Writer w = response.getWriter();
      w.write(WebServiceUtil.getInstance().createErrorXML(t));
      w.close();
    }
  }

  /**
   * Retrieve the authentication manager to be on authentication while processing the servelet
   * request. Depending on the configuration done in the authentication providers window the
   * specific manager for openbravo will be used.
   * 
   * In case there is defined a provider with type, OAuth 2.0 Token, the
   * {@link OAuth2TokenAuthenticationManager} will be used to manage authentication, otherwise, the
   * one defined in openbravo properties file will be used instead
   * 
   * @return authentication manager instance to be used on authentication
   */
  protected AuthenticationManager getAuthorizationManager() {
    return oauth2TokenDataProvider.existsOAuth2TokenConfig()
        ? (AuthenticationManager) ExternalAuthenticationManager.newInstance("OAUTH2TOKEN")
            .map(m -> {
              m.init(this);
              return m;
            })
            .orElseThrow(() -> new AuthenticationException(
                "Could not find an ApiOAuth2TokenAuthenticationManager"))
        : AuthenticationManager.getAuthenticationManager(this);
  }
}
