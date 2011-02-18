/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.0  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html 
 * Software distributed under the License  is  distributed  on  an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific  language  governing  rights  and  limitations
 * under the License. 
 * The Original Code is Openbravo ERP. 
 * The Initial Developer of the Original Code is Openbravo SLU 
 * All portions are Copyright (C) 2009-2010 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.client.kernel;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.jboss.seam.servlet.ContextualHttpServletRequest;
import org.jfree.util.Log;
import org.openbravo.authentication.AuthenticationException;
import org.openbravo.authentication.AuthenticationManager;
import org.openbravo.authentication.basic.DefaultAuthenticationManager;
import org.openbravo.base.ConfigParameters;
import org.openbravo.base.HttpBaseUtils;
import org.openbravo.dal.core.OBContext;
import org.openbravo.service.web.WebServiceUtil;

/**
 * The main servlet responsible for handling all the requests for components from the system.
 * 
 * @author mtaal
 */
public class KernelServlet extends AuthenticatingServlet {
  // private static final Logger log = Logger.getLogger(DataSourceServlet.class);

  // this is needed to support logout deep in the code...
  // TODO: make it easier to get to the authentication manager from
  // the
  public static final String KERNEL_SERVLET = "kernelServletInstance";

  private static final String RESPONSE_HEADER_ETAG = "ETag";
  private static final String RESPONSE_HEADER_LASTMODIFIED = "Last-Modified";
  private static final String REQUEST_HEADER_IFMODIFIEDSINCE = "If-Modified-Since";
  private static final String REQUEST_HEADER_IFNONEMATCH = "If-None-Match";
  private static final String RESPONSE_HEADER_CONTENTTYPE = "Content-Type";
  private static final String RESPONSE_HEADER_CACHE_CONTROL = "Cache-Control";
  private static final String RESPONSE_NO_CACHE = "no-cache";

  private static final long serialVersionUID = 1L;

  private static String servletPathPart = "org.openbravo.client.kernel";

  // are used to compute the relative path
  private static ConfigParameters globalParameters;

  private static AuthenticationManager authenticationManager = null;

  /**
   * @return the parameters as they are defined in the servlet context.
   */
  public static ConfigParameters getGlobalParameters() {
    return globalParameters;
  }

  public static String getServletPathPart() {
    return servletPathPart;
  }

  public static AuthenticationManager getGlobalAuthenticationManager() {
    return authenticationManager;
  }

  public static void logoutFromApplication(HttpServletRequest request, HttpServletResponse response) {
    try {

      // TODO: copied from logout.java, needs to be centralized some more
      final HttpSession session = request.getSession(false);
      if (session != null) {
        org.jboss.seam.web.Session.instance().invalidate();
      }

      // clear the context
      // TODO: this should be done in the authentication manager...
      OBContext.setOBContext((OBContext) null);

      // the authentication manager will do sendredirect
      // the real redirect to the login page is done in the AuthenticatingServlet
      KernelServlet.getGlobalAuthenticationManager().logout(request, response);
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }

  public void init(ServletConfig config) {
    super.init(config);
    globalParameters = ConfigParameters.retrieveFrom(config.getServletContext());

    // Code copied from HttpSecureAppServlet
    // TODO: from MP20 on use the getAuthenticationManager from the super class
    String sAuthManagerClass = globalParameters.getOBProperty("authentication.class");
    if (sAuthManagerClass == null || sAuthManagerClass.equals("")) {
      // If not defined, load default
      sAuthManagerClass = "org.openbravo.authentication.basic.DefaultAuthenticationManager";
    }

    try {
      authenticationManager = (AuthenticationManager) Class.forName(sAuthManagerClass)
          .newInstance();
    } catch (final Exception e) {
      log4j.error("Authentication manager not defined", e);
      authenticationManager = new DefaultAuthenticationManager();
    }

    try {
      authenticationManager.init(this);
    } catch (final AuthenticationException e) {
      log4j.error("Unable to initialize authentication manager", e);
    }
  }

  public void service(final HttpServletRequest request, final HttpServletResponse response)
      throws ServletException, IOException {
    try {
      // run everything in a seam context
      new ContextualHttpServletRequest(request) {
        @Override
        public void process() throws Exception {
          callServiceInSuper(request, response);
        }
      }.run();
      response.setStatus(200);
    } catch (final Throwable t) {
      throw new ServletException(t);
    }
  }

  protected void callServiceInSuper(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    super.service(request, response);
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    if (!request.getRequestURI().contains("/" + servletPathPart)) {
      throw new UnsupportedOperationException("Invalid url " + request.getRequestURI());
    }

    final int nameIndex = request.getRequestURI().indexOf(servletPathPart);
    final String servicePart = request.getRequestURI().substring(nameIndex);
    final String[] pathParts = WebServiceUtil.getInstance().getSegments(servicePart);
    if (pathParts.length < 2) {
      throw new UnsupportedOperationException("No service name present in url "
          + request.getRequestURI());
    }

    final String componentProviderName = pathParts[1];
    final ComponentProvider componentProvider = ComponentProviderRegistry.getInstance()
        .getComponentProvider(componentProviderName);
    final String componentId;
    if (pathParts.length > 2) {
      componentId = pathParts[2];
    } else {
      componentId = null;
    }

    final Map<String, Object> parameters = getParameterMap(request);
    final Component component = componentProvider.getComponent(componentId, parameters);
    OBContext.setAdminMode();
    String eTag;
    try {
      eTag = component.getETag();
    } finally {
      OBContext.restorePreviousMode();
    }
    final String requestETag = request.getHeader(REQUEST_HEADER_IFNONEMATCH);

    if (requestETag != null && eTag.equals(requestETag)) {
      response.sendError(HttpServletResponse.SC_NOT_MODIFIED);
      response.setDateHeader(RESPONSE_HEADER_LASTMODIFIED, request
          .getDateHeader(REQUEST_HEADER_IFMODIFIEDSINCE));
      return;
    }

    try {
      final String result = ComponentGenerator.getInstance().generate(component);

      response.setHeader(RESPONSE_HEADER_ETAG, eTag);
      response.setDateHeader(RESPONSE_HEADER_LASTMODIFIED, component.getLastModified().getTime());
      response.setContentType(component.getContentType());
      response.setHeader(RESPONSE_HEADER_CONTENTTYPE, component.getContentType());
      response.setHeader(RESPONSE_HEADER_CACHE_CONTROL, RESPONSE_NO_CACHE);

      final PrintWriter pw = response.getWriter();
      pw.write(result);
      pw.close();
    } catch (Exception e) {
      Log.error(e.getMessage(), e);
      if (!response.isCommitted()) {
        response.setContentType(KernelConstants.JAVASCRIPT_CONTENTTYPE);
        response.setHeader(RESPONSE_HEADER_CONTENTTYPE, KernelConstants.JAVASCRIPT_CONTENTTYPE);
        response.setHeader(RESPONSE_HEADER_CACHE_CONTROL, RESPONSE_NO_CACHE);
        response.getWriter().write(KernelUtils.getInstance().createErrorJavaScript(e));
      }
    }
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    final String action = request.getParameter(KernelConstants.ACTION_PARAMETER);

    response.setContentType(KernelConstants.JAVASCRIPT_CONTENTTYPE);
    response.setHeader(RESPONSE_HEADER_CONTENTTYPE, KernelConstants.JAVASCRIPT_CONTENTTYPE);
    response.setHeader(RESPONSE_HEADER_CACHE_CONTROL, RESPONSE_NO_CACHE);

    try {
      ActionHandlerRegistry.getInstance().getActionHandler(action).execute(request, response);
    } catch (Exception e) {
      Log.error(e.getMessage(), e);
      if (!response.isCommitted()) {
        response.setContentType(KernelConstants.JAVASCRIPT_CONTENTTYPE);
        response.setHeader(RESPONSE_HEADER_CONTENTTYPE, KernelConstants.JAVASCRIPT_CONTENTTYPE);
        response.setHeader(RESPONSE_HEADER_CACHE_CONTROL, RESPONSE_NO_CACHE);
        response.getWriter().write(KernelUtils.getInstance().createErrorJavaScript(e));
      }
    }
  }

  @Override
  public void doDelete(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    throw new UnsupportedOperationException("Only GET is supported");
  }

  @Override
  public void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    throw new UnsupportedOperationException("Only GET is supported");
  }

  @SuppressWarnings("unchecked")
  private Map<String, Object> getParameterMap(HttpServletRequest request) {
    final Map<String, Object> parameterMap = new HashMap<String, Object>();
    for (Enumeration keys = request.getParameterNames(); keys.hasMoreElements();) {
      final String key = (String) keys.nextElement();
      parameterMap.put(key, request.getParameter(key));
    }

    if (!parameterMap.containsKey(KernelConstants.CONTEXT_URL)) {
      parameterMap.put(KernelConstants.CONTEXT_URL, computeContextURL(request));
    }

    return parameterMap;
  }

  private String computeContextURL(HttpServletRequest request) {
    return HttpBaseUtils.getLocalAddress(request);
  }
}
