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
 * All portions are Copyright (C) 2009 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.base.seam.remote;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jboss.seam.Component;
import org.jboss.seam.core.Manager;
import org.jboss.seam.servlet.ContextualHttpServletRequest;
import org.openbravo.service.web.WebServiceUtil;

/**
 * Makes sure that a call to a request handler is done within a properly setup conversation
 * environment.
 * 
 * @author mtaal
 */
public class ContextualRESTRequest extends ContextualHttpServletRequest {

  private static final String GET_CONVERSATION_ID = "getConversationId";

  private HttpServletRequest request;
  private HttpServletResponse response;
  final String path;

  public ContextualRESTRequest(String path, HttpServletRequest request, HttpServletResponse response) {
    super(request);
    this.request = request;
    this.response = response;
    this.path = path;
  }

  @Override
  public void process() throws Exception {
    try {
      internalProcess();
    } catch (Throwable t) {
      t.printStackTrace(System.err);
    }
  }

  protected void internalProcess() throws Exception {
    if (path.endsWith(GET_CONVERSATION_ID)) {
      final String convId = createConversation();
      final String jsonString = getDataToJsonConverter().convertToJsonString(convId);
      getRequestUtils().writeJsonResult(response, jsonString);
      return;
    } else if (!Manager.instance().isLongRunningConversation()) {
      // create a conversation and use that
      // is a bit suboptimal...
      createConversation();
    }

    final RESTRequestHandler requestHandler = determineRequestHandler();
    final String[] segments = getPathStripFirst();

    final String method = request.getMethod().toUpperCase();
    if (method.equals("POST")) {
      requestHandler.doPost(segments, request, response);
    } else if (method.equals("PUT")) {
      requestHandler.doPut(segments, request, response);
    } else if (method.equals("GET")) {
      requestHandler.doGet(segments, request, response);
    } else if (method.equals("DELETE")) {
      requestHandler.doDelete(segments, request, response);
    } else {
      throw new IllegalStateException("Request method " + method + " is not supported here");
    }
  }

  private String[] getPathStripFirst() {
    final String[] segments = WebServiceUtil.getInstance().getSegments(path);
    if (segments.length == 1) {
      return new String[0];
    }
    final String[] result = new String[segments.length - 1];
    for (int i = 1; i < segments.length; i++) {
      result[i - 1] = segments[i];
    }
    return result;
  }

  protected String createConversation() {
    Manager.instance().beginConversation();
    Manager.instance().setLongRunningConversation(true);
    Manager.instance().switchConversation(Manager.instance().getCurrentConversationId());
    return Manager.instance().getCurrentConversationId();
  }

  private RESTRequestHandler determineRequestHandler() {
    final String segment = WebServiceUtil.getInstance().getFirstSegment(path);
    if (segment == null) {
      throw new IllegalArgumentException("The path " + path
          + " can not be handled, the OBSeamHandler needs to be specified in the path");
    }
    final RESTRequestHandler requestHandler = (RESTRequestHandler) Component.getInstance(segment);
    if (requestHandler == null) {
      throw new IllegalArgumentException("Name " + segment
          + " is not the name of a registered component");
    }
    return requestHandler;
  }

  private DataToJsonConverter getDataToJsonConverter() {
    return (DataToJsonConverter) Component.getInstance(DataToJsonConverter.class);
  }

  private RequestUtils getRequestUtils() {
    return (RequestUtils) Component.getInstance(RequestUtils.class);
  }

}
