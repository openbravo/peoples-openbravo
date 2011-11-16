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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Writer;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.exception.OBSecurityException;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.base.util.Check;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.core.SessionHandler;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.ad.process.ProcessInstance;
import org.openbravo.service.db.CallProcess;
import org.openbravo.service.db.DalConnectionProvider;
import org.openbravo.service.json.JsonConstants;
import org.openbravo.service.json.JsonToDataConverter;
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
public class TerminalServlet extends BaseWebServiceServlet {

  private static final Logger log = Logger.getLogger(TerminalServlet.class);
  private static final long serialVersionUID = 1L;

  private static String SERVLET_PATH = "org.openbravo.service.retail.posterminal.jsonrest";

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
        JSONObject jsonResult = execHQLQueryArray(request, response, getContentAsJSON(content));
        writeResult(response, jsonResult.toString());
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

  private JSONObject execHQLQueryArray(HttpServletRequest request, HttpServletResponse response,
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

  private JSONObject execThing(JSONObject jsonsent) throws JSONException, ServletException {

    if (jsonsent.has("query")) { // It is an HQL Query
      return execHQLQuery(jsonsent);
    } else if (jsonsent.has("process")) { // It is a Process
      return execProcess(jsonsent);
    } else {
      throw new JSONException("Expected one of the following properties: \"query\" or \"process\".");
    }
  }

  private JSONObject execHQLQuery(JSONObject jsonsent) throws JSONException {

    final JSONObject jsonResponse = new JSONObject();
    final JSONArray jsonData = new JSONArray();

    final int startRow = 0;

    SimpleQueryBuilder querybuilder = new SimpleQueryBuilder(jsonsent.getString("query"));

    final Session session = OBDal.getInstance().getSession();
    final Query query = session.createQuery(querybuilder.getHQLQuery());

    if (jsonsent.has("parameters")) {
      JSONObject jsonparams = jsonsent.getJSONObject("parameters");
      Iterator<?> it = jsonparams.keys();
      while (it.hasNext()) {
        String key = (String) it.next();
        Object value = jsonparams.get(key);
        if (value instanceof JSONObject) {
          JSONObject jsonvalue = (JSONObject) value;
          query.setParameter(
              key,
              JsonToDataConverter.convertJsonToPropertyValue(
                  PropertyByType.get(jsonvalue.getString("type")), jsonvalue.get("value")));
        } else {
          query.setParameter(key,
              JsonToDataConverter.convertJsonToPropertyValue(PropertyByType.infer(value), value));

        }
      }
    }

    JSONRowConverter converter = new JSONRowConverter(query.getReturnAliases());

    List<?> listdata = query.list();
    for (Object o : listdata) {
      jsonData.put(converter.convert(o));
    }

    jsonResponse.put(JsonConstants.RESPONSE_STARTROW, startRow);
    jsonResponse.put(JsonConstants.RESPONSE_ENDROW, (jsonData.length() > 0 ? jsonData.length()
        + startRow - 1 : 0));

    if (jsonData.length() == 0) {
      jsonResponse.put(JsonConstants.RESPONSE_TOTALROWS, 0);
    }

    jsonResponse.put(JsonConstants.RESPONSE_DATA, jsonData);
    jsonResponse.put(JsonConstants.RESPONSE_STATUS, JsonConstants.RPCREQUEST_STATUS_SUCCESS);

    return jsonResponse;
  }

  private JSONObject execProcess(JSONObject jsonsent) throws JSONException, ServletException {
    
    ConnectionProvider dalconn = new DalConnectionProvider();
    OBContext cnx = OBContext.getOBContext();
    String user = cnx.getUser().getId();
    String client = cnx.getCurrentClient().getId();
    String organization = cnx.getCurrentOrganization().getId();
    String role = cnx.getRole().getId();
    VariablesSecureApp vars = new VariablesSecureApp(user, client,organization, role);
    
    OBCriteria<org.openbravo.model.ad.ui.Process> crProcess = OBDal.getInstance().createCriteria(
        org.openbravo.model.ad.ui.Process.class);
    crProcess.add(Restrictions.eq("searchKey", jsonsent.getString("process")));
    crProcess.setFilterOnReadableClients(true);
    crProcess.setFilterOnReadableOrganization(true);
    crProcess.setFilterOnActive(true);
    List<org.openbravo.model.ad.ui.Process> processes = crProcess.list();
    if (processes.size() != 1) {
      throw new JSONException(MessageFormat.format("Process \"{0}\" does not exist.",
          jsonsent.getString("process")));
    }
    org.openbravo.model.ad.ui.Process process = processes.get(0);

    // Calls the process
    final ProcessInstance pinstance = CallProcess.getInstance().callProcess(process,
        jsonsent.optString("record"), new HashMap<String, Object>());

    final JSONObject jsonResponse = new JSONObject();

    String message = pinstance.getErrorMsg();
    int i = message.indexOf("@ERROR=");
    if (i >= 0) {
      throw new JSONException(message.substring(i + 7));
    } else {
      jsonResponse.put(JsonConstants.RESPONSE_STATUS, JsonConstants.RPCREQUEST_STATUS_SUCCESS);
      jsonResponse.put("result", pinstance.getResult());
      jsonResponse.put("record", pinstance.getRecordID());
      jsonResponse.put("message",
          Utility.parseTranslation(dalconn, vars, vars.getLanguage(), message));
    }

    return jsonResponse;
  }

  // private JSONObject execJavaProcess(JSONObject jsonsent) throws JSONException, ServletException
  // {
  //
  // OBContext cnx = OBContext.getOBContext();
  // String user = cnx.getUser().getId();
  // String client = cnx.getCurrentClient().getId();
  // String organization = cnx.getCurrentOrganization().getId();
  // String role = cnx.getRole().getId();
  //
  // OBError myMessage = null;
  // ProcessBundle pb = new ProcessBundle(process.getId(), new VariablesSecureApp(user, client,
  // organization, role)).init(dalconn);
  //
  // // HashMap<String, Object> params= new HashMap<String, Object>();
  // // params.put("FIN_Bankstatement_ID", strFIN_Bankstatement_ID);
  // // params.put("adOrgId", vars.getStringParameter("inpadOrgId"));
  // // params.put("adClientId", vars.getStringParameter("inpadClientId"));
  // // params.put("tabId", tabId);
  // // pb.setParams(params);
  //
  // new ProcessRunner(pb).execute(dalconn);
  // myMessage = (OBError) pb.getResult();
  //
  // final JSONObject jsonResponse = new JSONObject();
  // jsonResponse.put(JsonConstants.RESPONSE_STATUS, JsonConstants.RPCREQUEST_STATUS_SUCCESS);
  //
  // final JSONObject jsonResult = new JSONObject();
  // jsonResult.put("title", myMessage.getTitle());
  // jsonResult.put("message", myMessage.getMessage());
  // jsonResponse.put("result", jsonResult);
  //
  // final JSONObject jsonResultLoc = new JSONObject();
  // jsonResultLoc.put("title",
  // Utility.parseTranslation(dalconn, vars, vars.getLanguage(), myMessage.getTitle()));
  // jsonResultLoc.put("message",
  // Utility.parseTranslation(dalconn, vars, vars.getLanguage(), myMessage.getMessage()));
  // jsonResponse.put("resultLocalized", jsonResultLoc);
  //
  // return jsonResponse;
  // }

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
}
