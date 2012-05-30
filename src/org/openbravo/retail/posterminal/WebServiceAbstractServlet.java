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
 * All portions are Copyright (C) 2008-2010 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.retail.posterminal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Writer;
import java.sql.BatchUpdateException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.exception.OBSecurityException;
import org.openbravo.base.util.Check;
import org.openbravo.dal.core.SessionHandler;
import org.openbravo.dal.service.OBDal;
import org.openbravo.service.json.JsonConstants;
import org.openbravo.service.web.InvalidContentException;
import org.openbravo.service.web.InvalidRequestException;
import org.openbravo.service.web.ResourceNotFoundException;

/**
 * This servlet has two responsibilities: 1) translate Exceptions into the correct Http response
 * code, 2) Provide json utilities
 * <p/>
 * 
 * @author mtaal, aro
 */

public abstract class WebServiceAbstractServlet extends HttpServlet {
  private static final Logger log = Logger.getLogger(WebServiceAbstractServlet.class);
  private static final long serialVersionUID = 1L;

  protected void authenticated(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
  }

  @Override
  protected final void service(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    try {
      authenticated(request, response);
      super.service(request, response);
      response.setStatus(200);
    } catch (final InvalidRequestException e) {
      SessionHandler.getInstance().setDoRollback(true);
      response.setStatus(400);
      log.error(e.getMessage(), e);
      writeResult(response, silentExceptionToJson(e));
    } catch (final InvalidContentException e) {
      SessionHandler.getInstance().setDoRollback(true);
      response.setStatus(409);
      log.error(e.getMessage(), e);
      writeResult(response, silentExceptionToJson(e));
    } catch (final ResourceNotFoundException e) {
      SessionHandler.getInstance().setDoRollback(true);
      response.setStatus(404);
      log.error(e.getMessage(), e);
      writeResult(response, silentExceptionToJson(e));
    } catch (final OBSecurityException e) {
      SessionHandler.getInstance().setDoRollback(true);
      response.setStatus(401);
      log.error(e.getMessage(), e);
      writeResult(response, silentExceptionToJson(e));
    } catch (final Throwable t) {
      SessionHandler.getInstance().setDoRollback(true);
      response.setStatus(500);
      log.error(t.getMessage(), t);
      writeResult(response, silentExceptionToJson(t));
    }
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    writeResult(response,
 silentExceptionToJson(new InvalidRequestException(
        "Method not supported: GET")));
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    writeResult(response,
 silentExceptionToJson(new InvalidRequestException(
        "Method not supported: POST")));
  }

  @Override
  public void doDelete(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    writeResult(response, silentExceptionToJson(new InvalidRequestException(
        "Method not supported: DELETE")));
  }

  @Override
  public void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    writeResult(response,
 silentExceptionToJson(new InvalidRequestException(
        "Method not supported: PUT")));
  }

  protected void writeResult(HttpServletResponse response, String result) throws IOException {
    response.setContentType("application/json;charset=UTF-8");
    response.setHeader("Content-Type", "application/json;charset=UTF-8");

    final Writer w = response.getWriter();
    w.write(result);
    w.close();
  }

  protected String getRequestContent(HttpServletRequest request) throws IOException {
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

  protected Object getContentAsJSON(String content) throws JSONException {
    Check.isNotNull(content, "Content must be set");
    if (content.trim().startsWith("[")) {
      return new JSONArray(content);
    } else {
      return new JSONObject(content);
    }
  }

  protected String getJSONResult(String result) throws JSONException {
    final JSONObject jsonResult = new JSONObject();
    jsonResult.put("result", result);
    return jsonResult.toString();
  }

  public static String silentExceptionToJson(Throwable throwable) throws IOException {

    Throwable localThrowable = throwable;
    if (throwable.getCause() instanceof BatchUpdateException) {
      final BatchUpdateException batchException = (BatchUpdateException) throwable.getCause();
      localThrowable = batchException.getNextException();
    }

    try {
      final JSONObject jsonResult = new JSONObject();
      final JSONObject jsonResponse = new JSONObject();
      jsonResponse.put(JsonConstants.RESPONSE_STATUS, JsonConstants.RPCREQUEST_STATUS_FAILURE);

      try {
        // get rid of the current transaction
        OBDal.getInstance().rollbackAndClose();
      } catch (Exception e) {
        // ignored on purpose
        log.error(e.getMessage(), e);
      }

      final JSONObject error = new JSONObject();
      error.put("message", localThrowable.getMessage());
      error.put("type", localThrowable.getClass().getName());
      jsonResponse.put(JsonConstants.RESPONSE_ERROR, error);

      jsonResponse.put(JsonConstants.RESPONSE_TOTALROWS, 0);
      jsonResult.put(JsonConstants.RESPONSE_RESPONSE, jsonResponse);
      return jsonResult.toString();
    } catch (JSONException e) {
      throw new IOException(e);
    }
  }
}
