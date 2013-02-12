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

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.openbravo.mobile.core.process.WebServiceAbstractServlet;
import org.openbravo.service.json.JsonUtils;
import org.openbravo.service.web.InvalidRequestException;

/**
 * A web service which provides POS terminal services.
 * 
 * @author adrianromero
 */
public class LoginServlet extends WebServiceAbstractServlet {

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
  }
}
