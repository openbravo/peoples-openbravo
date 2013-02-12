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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.openbravo.authentication.AuthenticationManager;
import org.openbravo.base.exception.OBSecurityException;
import org.openbravo.dal.core.OBContext;
import org.openbravo.mobile.core.process.WebServiceAbstractServlet;
import org.openbravo.service.json.JsonUtils;
import org.openbravo.service.web.InvalidContentException;
import org.openbravo.service.web.UserContextCache;

public class WebServiceLoginServlet extends WebServiceAbstractServlet {

  private static final Logger log = Logger.getLogger(WebServiceLoginServlet.class);

  private static final long serialVersionUID = 1L;

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {

    if (OBContext.getOBContext() != null) {
      log.warn("Already logged in.");
      throw new InvalidContentException("Already logged in.");
    }

    // do the login action
    AuthenticationManager authManager = AuthenticationManager.getAuthenticationManager(this);
    String userId = authManager.webServiceAuthenticate(request);
    if (userId == null) {
      log.warn("Authentication failed.");
      throw new OBSecurityException("Authentication failed.");
    }

    // Set the session
    OBContext.setOBContext(UserContextCache.getInstance().getCreateOBContext(userId));
    OBContext.setOBContextInSession(request, OBContext.getOBContext());

    try {
      writeResult(response, getJSONResult("success"));
    } catch (JSONException e) {
      log.error(e.getMessage(), e);
      writeResult(response, JsonUtils.convertExceptionToJson(e));
    }
  }
}
