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
 * All portions are Copyright (C) 2009-2011 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.retail.posterminal;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.service.json.JsonConstants;
import org.openbravo.service.json.JsonUtils;
import org.openbravo.service.web.InvalidRequestException;
import org.openbravo.service.web.WebServiceUtil;

/**
 * A web service which provides POS terminal services.
 * 
 * @author adrianromero
 */
public class TerminalServlet extends WebServiceAuthenticatedServlet {

  private static final Logger log = Logger.getLogger(TerminalServlet.class);
  private static final long serialVersionUID = 1L;

  private static String SERVLET_PATH = "org.openbravo.service.retail.posterminal.jsonrest";

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
      } catch (JSONException e) {
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
      throws JSONException,
      ServletException {

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
}
