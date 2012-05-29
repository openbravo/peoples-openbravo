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
 * All portions are Copyright (C) 2012 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.retail.posterminal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Writer;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.exception.OBSecurityException;
import org.openbravo.base.util.Check;
import org.openbravo.dal.core.SessionHandler;
import org.openbravo.service.json.JsonConstants;
import org.openbravo.service.json.JsonUtils;
import org.openbravo.service.web.BaseWebServiceServlet;
import org.openbravo.service.web.InvalidContentException;
import org.openbravo.service.web.InvalidRequestException;
import org.openbravo.service.web.ResourceNotFoundException;
import org.openbravo.service.web.WebServiceUtil;

/**
 * A web service which provides POS terminal services.
 * 
 * @author adrianromero
 */
public class LoginUtils extends BaseWebServiceServlet {

  private static final Logger log = Logger.getLogger(LoginUtils.class);
  private static final long serialVersionUID = 1L;

  private static String SERVLET_PATH = "org.openbravo.service.retail.posterminal.loginutils";

  @Override
  public void init(ServletConfig config) throws ServletException {
    // if (config.getInitParameter(JsonConstants.JSON_REST_URL_NAME_PARAM) != null) {
    // servletPathPart = config.getInitParameter(JsonConstants.JSON_REST_URL_NAME_PARAM);
    // }
    super.init(config);
  }

  protected void doService(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    try {
      callServiceInSuper(request, response);
      response.setStatus(200);
    } catch (final InvalidRequestException e) {
      SessionHandler.getInstance().setDoRollback(true);
      response.setStatus(400);
      log.error(e.getMessage(), e);
      writeResult(response, JsonUtils.convertExceptionToJson(e));
    } catch (final InvalidContentException e) {
      SessionHandler.getInstance().setDoRollback(true);
      response.setStatus(409);
      log.error(e.getMessage(), e);
      writeResult(response, JsonUtils.convertExceptionToJson(e));
    } catch (final ResourceNotFoundException e) {
      SessionHandler.getInstance().setDoRollback(true);
      response.setStatus(404);
      log.error(e.getMessage(), e);
      writeResult(response, JsonUtils.convertExceptionToJson(e));
    } catch (final OBSecurityException e) {
      SessionHandler.getInstance().setDoRollback(true);
      response.setStatus(401);
      log.error(e.getMessage(), e);
      writeResult(response, JsonUtils.convertExceptionToJson(e));
    } catch (final Throwable t) {
      SessionHandler.getInstance().setDoRollback(true);
      response.setStatus(500);
      log.error(t.getMessage(), t);
      writeResult(response, JsonUtils.convertExceptionToJson(t));
    }
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    doGetOrPost(request, response, request.getParameter("content"));
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    doGetOrPost(request, response, getRequestContent(request));
  }

  @Override
  public void doDelete(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    writeResult(response, JsonUtils.convertExceptionToJson(new InvalidRequestException(
        "Method not supported: DELETE")));
  }

  @Override
  public void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    writeResult(response,
        JsonUtils.convertExceptionToJson(new InvalidRequestException("Method not supported: PUT")));
  }

  private void doGetOrPost(HttpServletRequest request, HttpServletResponse response, String content)
      throws IOException, ServletException {

    String[] pathparts = checkSetParameters(request, response);
    if (pathparts == null) {
      return;
    }

    if (pathparts.length == 1 || "hql".equals(pathparts[1])) {
      try {
        JSONObject jsonResult = execThingArray(request, response, getContentAsJSON(content));
        writeResult(response, jsonResult.toString());
      } catch (Exception e) {
        log.error(e.getMessage(), e);
        writeResult(response, JsonUtils.convertExceptionToJson(e));
      }
    } else if (pathparts.length == 2 || "logout".equals(pathparts[1])) {
      request.getSession(true).invalidate();
      try {
        writeResult(response, getJSONResult("success"));
      } catch (Exception e) {
        log.error(e.getMessage(), e);
        writeResult(response, JsonUtils.convertExceptionToJson(e));
      }
    } else {
      writeResult(
          response,
          JsonUtils.convertExceptionToJson(new InvalidRequestException("Command not found: "
              + pathparts[1])));
    }
  }

  private JSONObject execThingArray(HttpServletRequest request, HttpServletResponse response,
      Object jsonContent) throws JSONException, ServletException {

    // get all sentences to execute.
    final JSONObject jsonResult = new JSONObject();

    if (jsonContent instanceof JSONArray) {
      final JSONArray jsonArray = (JSONArray) jsonContent;
      final JSONArray jsonResponses = new JSONArray();
      for (int i = 0; i < jsonArray.length(); i++) {
        jsonResponses.put(execThing(jsonArray.getJSONObject(i)));
      }
      jsonResult.put(JsonConstants.RESPONSE_RESPONSE, jsonResponses);
    } else if (jsonContent instanceof JSONObject) {
      jsonResult.put(JsonConstants.RESPONSE_RESPONSE, execThing((JSONObject) jsonContent));
    } else {
      throw new JSONException("Expected JSON object or array.");
    }

    return jsonResult;
  }

  @SuppressWarnings("unchecked")
  private JSONObject execThing(JSONObject jsonsent) throws JSONException, ServletException {

    if (jsonsent.has("className")) {
      try {
        return execClassName((Class<JSONProcess>) Class.forName(jsonsent.getString("className")),
            jsonsent);
      } catch (ClassNotFoundException e) {
        throw new JSONException(e);
      }
    } else if (jsonsent.has("query")) { // It is an HQL Query
      return execClassName(ProcessHQLQuery.class, jsonsent);
    } else if (jsonsent.has("process")) { // It is a Process
      return execClassName(ProcessProcedure.class, jsonsent);
    } else {
      throw new JSONException("Expected one of the following properties: \"query\" or \"process\".");
    }
  }

  private JSONObject execClassName(Class<? extends JSONProcess> process, JSONObject jsonsent)
      throws JSONException, ServletException {

    try {
      JSONProcess proc = process.newInstance();
      return proc.exec(jsonsent);
    } catch (InstantiationException e) {
      throw new JSONException(e);
    } catch (IllegalAccessException e) {
      throw new JSONException(e);
    }
  }

  private String[] checkSetParameters(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    if (!request.getRequestURI().contains("/" + SERVLET_PATH)) {
      writeResult(response, JsonUtils.convertExceptionToJson(new InvalidRequestException(
          "Invalid url, the path should contain the service name: " + SERVLET_PATH)));
      return null;
    }

    final int nameIndex = request.getRequestURI().indexOf(SERVLET_PATH);
    final String servicePart = request.getRequestURI().substring(nameIndex);
    final String[] pathParts = WebServiceUtil.getInstance().getSegments(servicePart);
    if (pathParts.length == 0 || !pathParts[0].equals(SERVLET_PATH)) {
      writeResult(
          response,
          JsonUtils.convertExceptionToJson(new InvalidRequestException("Invalid url: "
              + request.getRequestURI())));
      return null;
    }

    return pathParts;
  }

  private void writeResult(HttpServletResponse response, String result) throws IOException {
    response.setContentType("application/json;charset=UTF-8");
    response.setHeader("Content-Type", "application/json;charset=UTF-8");

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
    log.debug(sb.toString());
    return sb.toString();
  }

  private Object getContentAsJSON(String content) throws JSONException {
    Check.isNotNull(content, "Content must be set");
    if (content.trim().startsWith("[")) {
      return new JSONArray(content);
    } else {
      return new JSONObject(content);
    }
  }

  private String getJSONResult(String result) throws JSONException {
    final JSONObject jsonResult = new JSONObject();
    jsonResult.put("result", result);
    return jsonResult.toString();
  }
}
