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
import java.io.Writer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.weld.WeldUtils;
import org.openbravo.mobile.core.process.JSONProcess;
import org.openbravo.mobile.core.process.JSONRowConverter;
import org.openbravo.mobile.core.process.ProcessProcedure;
import org.openbravo.mobile.core.process.WebServiceAuthenticatedServlet;
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

  private static String SERVLET_PATH = "org.openbravo.retail.posterminal.service.jsonrest";

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    doGetOrPost(request, response, null);
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    doGetOrPost(request, response, getRequestContent(request));
  }

  @SuppressWarnings("unchecked")
  private void doGetOrPost(HttpServletRequest request, HttpServletResponse response, String content)
      throws IOException, ServletException {

    String[] pathparts = checkSetParameters(request, response);
    if (pathparts == null) {
      return;
    }
    try {
      final Object jsonsent = getContentAsJSON((content == null && pathparts.length == 3) ? java.net.URLDecoder
          .decode(pathparts[2], "UTF-8") : content);

      response.setContentType("application/json");
      response.setCharacterEncoding("UTF-8");

      if (pathparts.length == 1 || "hql".equals(pathparts[1])) {
        final Writer w = response.getWriter();
        execThingArray(w, jsonsent);
        w.close();
      } else {
        // Command it is a class name
        try {
          if (jsonsent instanceof JSONObject) {
            final Class<JSONProcess> clazz = (Class<JSONProcess>) Class.forName(pathparts[1]);
            final Writer w = response.getWriter();
            w.write("{\"response\":{");
            execClassName(w, clazz, (JSONObject) jsonsent);
            w.write("}}");
            w.close();
          } else {
            writeResult(response, JsonUtils.convertExceptionToJson(new InvalidRequestException(
                "Content is not a JSON object.")));
          }
        } catch (ClassNotFoundException e) {
          log.error(e.getMessage(), e);
          writeResult(response, JsonUtils.convertExceptionToJson(new InvalidRequestException(
              "Command class not found: " + pathparts[1])));
        }
      }
    } catch (JSONException e) {
      writeResult(response, JsonUtils.convertExceptionToJson(e));
    }
  }

  private void execThingArray(Writer w, Object jsonContent) throws IOException, ServletException {

    if (jsonContent instanceof JSONArray) {
      JSONArray jsonArray = (JSONArray) jsonContent;
      w.write("{\"response\":{");
      try {
        w.write("\"responses\":[");
        for (int i = 0; i < jsonArray.length(); i++) {
          if (i > 0) {
            w.write(',');
          }
          w.write('{');
          execThing(w, jsonArray.getJSONObject(i));
          w.write('}');
        }
        w.write("]");
      } catch (JSONException e) {
        w.write("],");
        JSONRowConverter.addJSONExceptionFields(w, e);
      }
      w.write("}}");
    } else if (jsonContent instanceof JSONObject) {
      w.write("{\"response\":{");
      execThing(w, (JSONObject) jsonContent);
      w.write("}}");
    } else {
      w.write(JsonUtils.convertExceptionToJson(new JSONException("Expected JSON object or array.")));
    }
  }

  @SuppressWarnings("unchecked")
  private void execThing(Writer w, JSONObject jsonsent) throws IOException, ServletException {

    // this is the response object
    // JSONProcess writes the properties of the response object.

    if (jsonsent.has("className")) {
      try {
        execClassName(w, (Class<JSONProcess>) Class.forName(jsonsent.getString("className")),
            jsonsent);
      } catch (JSONException e) {
        JSONRowConverter.addJSONExceptionFields(w, e);
      } catch (ClassNotFoundException e) {
        JSONRowConverter.addJSONExceptionFields(w, e);
      }
    } else if (jsonsent.has("process")) { // It is a Process
      execClassName(w, ProcessProcedure.class, jsonsent);
    } else {
      JSONRowConverter.addJSONExceptionFields(w, new JSONException(
          "Expected one of the following properties: \"className\", \"query\" or \"process\"."));
    }
  }

  private void execClassName(Writer w, Class<? extends JSONProcess> process, JSONObject jsonsent)
      throws IOException, ServletException {

    JSONProcess proc = WeldUtils.getInstanceFromStaticBeanManager(process);
    proc.exec(w, jsonsent);
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
