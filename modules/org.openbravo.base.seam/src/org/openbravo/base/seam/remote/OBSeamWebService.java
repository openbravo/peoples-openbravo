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

import org.openbravo.service.web.WebService;

/**
 * The main seam web service which creates a conversation or if there is a conversation calls the
 * component addressed in the request.
 * 
 * This webservice is also capable of creating a new conversation. The conversationId is returned as
 * a json array.
 * 
 * @author mtaal
 */
public class OBSeamWebService implements WebService {

  @Override
  public void doDelete(String path, HttpServletRequest request, HttpServletResponse response)
      throws Exception {
    try {
      new ContextualRESTRequest(path, request, response).run();
    } catch (Throwable t) {
      // send a jsonized exception back
      handleException(t, response);
    }
  }

  @Override
  public void doGet(String path, HttpServletRequest request, HttpServletResponse response)
      throws Exception {
    try {
      new ContextualRESTRequest(path, request, response).run();
    } catch (Throwable t) {
      // send a jsonized exception back
      handleException(t, response);
    }
  }

  @Override
  public void doPost(String path, HttpServletRequest request, HttpServletResponse response)
      throws Exception {
    try {
      new ContextualRESTRequest(path, request, response).run();
    } catch (Throwable t) {
      // send a jsonized exception back
      handleException(t, response);
    }
  }

  @Override
  public void doPut(String path, HttpServletRequest request, HttpServletResponse response)
      throws Exception {
    try {
      new ContextualRESTRequest(path, request, response).run();
    } catch (Throwable t) {
      // send a jsonized exception back
      handleException(t, response);
    }
  }

  private void handleException(Throwable t, HttpServletResponse response) {
    t.printStackTrace(System.err);
  }
}
