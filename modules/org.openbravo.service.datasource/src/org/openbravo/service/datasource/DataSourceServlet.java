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
 * All portions are Copyright (C) 2009 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.service.datasource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Writer;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.exception.OBSecurityException;
import org.openbravo.base.session.OBPropertiesProvider;
import org.openbravo.client.kernel.BaseKernelServlet;
import org.openbravo.client.kernel.KernelUtils;
import org.openbravo.dal.core.SessionHandler;
import org.openbravo.dal.service.OBDal;
import org.openbravo.database.SessionInfo;
import org.openbravo.erpCommon.security.UsageAudit;
import org.openbravo.service.json.JsonConstants;
import org.openbravo.service.json.JsonUtils;
import org.openbravo.service.web.InvalidContentException;
import org.openbravo.service.web.InvalidRequestException;
import org.openbravo.service.web.ResourceNotFoundException;
import org.openbravo.service.web.WebServiceUtil;

/**
 * A web service which provides a JSON REST service using the {@link DataSourceService}
 * implementation. Retrieves the data source using the {@link DataSourceServiceProvider}.
 * 
 * @author mtaal
 */
public class DataSourceServlet extends BaseKernelServlet {
  private static final Logger log = Logger.getLogger(DataSourceServlet.class);

  private static final long serialVersionUID = 1L;

  private static String servletPathPart = "org.openbravo.service.datasource";

  public static String getServletPathPart() {
    return servletPathPart;
  }

  @Inject
  private DataSourceServiceProvider dataSourceServiceProvider;

  @Override
  public void init(ServletConfig config) {
    if (config.getInitParameter(DataSourceConstants.URL_NAME_PARAM) != null) {
      servletPathPart = config.getInitParameter(DataSourceConstants.URL_NAME_PARAM);
    }

    super.init(config);
  }

  public void service(final HttpServletRequest request, final HttpServletResponse response)
      throws ServletException, IOException {

    try {
      super.service(request, response);

    } catch (final InvalidRequestException e) {
      if (SessionHandler.isSessionHandlerPresent()) {
        SessionHandler.getInstance().setDoRollback(true);
      }
      response.setStatus(400);
      log.error(e.getMessage(), e);
      writeResult(response, JsonUtils.convertExceptionToJson(e));
    } catch (final InvalidContentException e) {
      if (SessionHandler.isSessionHandlerPresent()) {
        SessionHandler.getInstance().setDoRollback(true);
      }
      response.setStatus(409);
      log.error(e.getMessage(), e);
      writeResult(response, JsonUtils.convertExceptionToJson(e));
    } catch (final ResourceNotFoundException e) {
      if (SessionHandler.isSessionHandlerPresent()) {
        SessionHandler.getInstance().setDoRollback(true);
      }
      response.setStatus(404);
      log.error(e.getMessage(), e);
      writeResult(response, JsonUtils.convertExceptionToJson(e));
    } catch (final OBSecurityException e) {
      if (SessionHandler.isSessionHandlerPresent()) {
        SessionHandler.getInstance().setDoRollback(true);
      }
      response.setStatus(401);
      log.error(e.getMessage(), e);
      writeResult(response, JsonUtils.convertExceptionToJson(e));
    } catch (final Throwable t) {
      t.printStackTrace(System.err);
      if (SessionHandler.isSessionHandlerPresent()) {
        SessionHandler.getInstance().setDoRollback(true);
      }
      response.setStatus(500);
      log.error(t.getMessage(), t);
      writeResult(response, JsonUtils.convertExceptionToJson(t));
    }
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    final Map<String, String> parameters = getParameterMap(request);
    UsageAudit.auditAction(request, parameters);
    doFetch(request, response, parameters);
  }

  private void doFetch(HttpServletRequest request, HttpServletResponse response,
      Map<String, String> parameters) throws IOException, ServletException {
    // checks and set parameters, if not valid then go away
    if (!checkSetParameters(request, response, parameters)) {
      return;
    }

    if (log.isDebugEnabled()) {
      getRequestContent(request);
    }
    try {
      String filterClass = parameters.get(DataSourceConstants.DS_FILTERCLASS_PARAM);
      if (filterClass != null) {
        try {
          DataSourceFilter filter = (DataSourceFilter) Class.forName(filterClass).newInstance();
          filter.doFilter(parameters, request);
        } catch (Exception e) {
          log.error("Error trying to apply datasource filter with class: " + filterClass, e);
        }
      }

      // now do the action
      String result = getDataSource(request).fetch(parameters);
      writeResult(response, result);
    } catch (Exception e) {
      log4j.error(e.getMessage(), e);
      if (!response.isCommitted()) {
        final JSONObject jsonResult = new JSONObject();
        final JSONObject jsonResponse = new JSONObject();
        String result = "";
        try {
          jsonResponse.put(JsonConstants.RESPONSE_STATUS,
              JsonConstants.RPCREQUEST_STATUS_VALIDATION_ERROR);
          jsonResponse.put("error", KernelUtils.getInstance().createErrorJSON(e));
          jsonResult.put(JsonConstants.RESPONSE_RESPONSE, jsonResponse);
          result = jsonResult.toString();
        } catch (JSONException e1) {
          log.error("Error genearating JSON error", e1);
        }
        writeResult(response, result);
      }
    }
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    final Map<String, String> parameters = getParameterMap(request);
    UsageAudit.auditAction(request, parameters);

    if (DataSourceConstants.FETCH_OPERATION.equals(parameters
        .get(DataSourceConstants.OPERATION_TYPE_PARAM))) {
      doFetch(request, response, parameters);
      return;
    }

    // note if clause updates parameter map
    if (checkSetIDDataSourceName(request, response, parameters)) {
      final String result = getDataSource(request).add(parameters, getRequestContent(request));
      writeResult(response, result);
    }
  }

  @Override
  public void doDelete(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    final Map<String, String> parameters = getParameterMap(request);
    UsageAudit.auditAction(request, parameters);
    setSessionInfo(request, parameters);

    // checks and set parameters, if not valid then go away
    if (!checkSetParameters(request, response, parameters)) {
      return;
    }

    final String id = parameters.get(JsonConstants.ID);
    if (id == null) {
      throw new InvalidRequestException("No id parameter");
    }

    final String result = getDataSource(request).remove(parameters);
    writeResult(response, result);
  }

  private String getDataSourceNameFromRequest(HttpServletRequest request) {
    final String url = request.getRequestURI();
    if (url.indexOf(getServletPathPart()) == -1) {
      throw new OBException("Request url " + url + " is not valid");
    }
    final int startIndex = 1 + url.indexOf(getServletPathPart()) + getServletPathPart().length();
    final int endIndex = url.indexOf("/", startIndex + 1);
    final String dsName = (endIndex == -1 ? url.substring(startIndex) : url.substring(startIndex,
        endIndex));

    if (dsName.length() == 0) {
      throw new ResourceNotFoundException("Data source not found using url " + url);
    }
    return dsName;
  }

  @Override
  public void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    final Map<String, String> parameters = getParameterMap(request);
    setSessionInfo(request, parameters);
    UsageAudit.auditAction(request, parameters);

    // note if clause updates parameter map
    if (checkSetIDDataSourceName(request, response, parameters)) {
      final String result = getDataSource(request).update(parameters, getRequestContent(request));
      writeResult(response, result);
    }
  }

  private void setSessionInfo(HttpServletRequest request, Map<String, String> parameters) {
    SessionInfo.setModuleId(parameters.get("moduleId"));
    SessionInfo.setProcessType("W");
    SessionInfo.setProcessId(parameters.get("tabId"));
    // Session ID and user needn't be set as they were done in the service method.

    // SessionInfo.setUserId(OBContext.getOBContext().getUser().getId());
    // SessionInfo.setSessionId((String) request.getAttribute("#AD_SESSION_ID"));

    // FIXME: Because of issue #15331 connection is initialized with temporary audit table before
    // setting session info
    // Reset Session Info in DB manually as it was set in the service but actual information is not
    // available till now.
    SessionInfo.setDBSessionInfo(OBDal.getInstance().getConnection(), OBPropertiesProvider
        .getInstance().getOpenbravoProperties().getProperty("bbdd.rdbms"));
  }

  private boolean checkSetParameters(HttpServletRequest request, HttpServletResponse response,
      Map<String, String> parameters) throws IOException {
    if (!request.getRequestURI().contains("/" + servletPathPart)) {
      writeResult(response, JsonUtils.convertExceptionToJson(new InvalidRequestException(
          "Invalid url, the path should contain the service name: " + servletPathPart)));
      return false;
    }
    final int nameIndex = request.getRequestURI().indexOf(servletPathPart);
    final String servicePart = request.getRequestURI().substring(nameIndex);
    final String[] pathParts = WebServiceUtil.getInstance().getSegments(servicePart);
    if (pathParts.length == 0 || !pathParts[0].equals(servletPathPart)) {
      writeResult(response, JsonUtils.convertExceptionToJson(new InvalidRequestException(
          "Invalid url: " + request.getRequestURI())));
      return false;
    }
    if (pathParts.length == 1) {
      writeResult(response, JsonUtils.convertExceptionToJson(new InvalidRequestException(
          "Invalid url, no datasource name: " + request.getRequestURI())));
      return false;
    }
    final String dsName = pathParts[1];
    parameters.put(DataSourceConstants.DS_NAME_PARAM, dsName);
    if (pathParts.length > 2) {
      // search on the exact id
      parameters.put(JsonConstants.ID, pathParts[2]);
      if (!parameters.containsKey(JsonConstants.TEXTMATCH_PARAMETER)) {
        parameters.put(JsonConstants.TEXTMATCH_PARAMETER, JsonConstants.TEXTMATCH_EXACT);
        parameters.put(JsonConstants.TEXTMATCH_PARAMETER_OVERRIDE, JsonConstants.TEXTMATCH_EXACT);
      }
    }
    return true;
  }

  @SuppressWarnings("unchecked")
  private Map<String, String> getParameterMap(HttpServletRequest request) {
    final Map<String, String> parameterMap = new HashMap<String, String>();
    for (Enumeration keys = request.getParameterNames(); keys.hasMoreElements();) {
      final String key = (String) keys.nextElement();

      // do simple conversion of array of values to a string
      // TODO: replace when advancedcriteria are supported
      final String[] values = request.getParameterValues(key);
      if (values.length == 1) {
        parameterMap.put(key, values[0]);
      } else {
        final StringBuilder sb = new StringBuilder();
        for (String value : values) {
          if (sb.length() > 0) {
            sb.append(JsonConstants.IN_PARAMETER_SEPARATOR);
          }
          sb.append(value);
        }
        parameterMap.put(key, sb.toString());
      }
    }
    return parameterMap;
  }

  // NOTE: parameters parameter is updated inside this method
  private boolean checkSetIDDataSourceName(HttpServletRequest request,
      HttpServletResponse response, Map<String, String> parameters) throws IOException {
    if (!request.getRequestURI().contains("/" + servletPathPart)) {
      writeResult(response, JsonUtils.convertExceptionToJson(new InvalidRequestException(
          "Invalid url, the path should contain the service name: " + servletPathPart)));
      return false;
    }
    final int nameIndex = request.getRequestURI().indexOf(servletPathPart);
    final String servicePart = request.getRequestURI().substring(nameIndex);
    final String[] pathParts = WebServiceUtil.getInstance().getSegments(servicePart);
    if (pathParts.length == 0 || !pathParts[0].equals(servletPathPart)) {
      writeResult(response, JsonUtils.convertExceptionToJson(new InvalidRequestException(
          "Invalid url: " + request.getRequestURI())));
      return false;
    }
    if (pathParts.length == 1) {
      return true;
    }

    final String dsName = pathParts[1];
    parameters.put(DataSourceConstants.DS_NAME_PARAM, dsName);

    if (pathParts.length > 2) {
      // search on the exact id
      parameters.put(JsonConstants.ID, pathParts[2]);
      if (!parameters.containsKey(JsonConstants.TEXTMATCH_PARAMETER)) {
        parameters.put(JsonConstants.TEXTMATCH_PARAMETER, JsonConstants.TEXTMATCH_EXACT);
        parameters.put(JsonConstants.TEXTMATCH_PARAMETER_OVERRIDE, JsonConstants.TEXTMATCH_EXACT);
      }
    }
    return true;
  }

  private DataSourceService getDataSource(HttpServletRequest request) {
    final String dsName = getDataSourceNameFromRequest(request);
    final DataSourceService dataSource = dataSourceServiceProvider.getDataSource(dsName);
    return dataSource;
  }

  private void writeResult(HttpServletResponse response, String result) throws IOException {
    response.setContentType(JsonConstants.JSON_CONTENT_TYPE);
    response.setHeader("Content-Type", JsonConstants.JSON_CONTENT_TYPE);

    final Writer w = response.getWriter();
    w.write(result);
    w.close();
  }

  private String getRequestContent(HttpServletRequest request) throws IOException {
    final BufferedReader reader = request.getReader();
    if (reader == null) {
      return "";
    }
    String line;
    final StringBuilder sb = new StringBuilder();
    while ((line = reader.readLine()) != null) {
      if (sb.length() > 0) {
        sb.append("\n");
      }
      sb.append(line);
    }
    log.debug("REQUEST CONTENT>>>>");
    for (Enumeration<?> enumeration = request.getParameterNames(); enumeration.hasMoreElements();) {
      final Object key = enumeration.nextElement();
      log.debug(key + ": " + request.getParameter((String) key));
    }
    return sb.toString();
  }

}
