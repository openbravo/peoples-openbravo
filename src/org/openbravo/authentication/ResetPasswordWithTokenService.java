/*
 ************************************************************************************
 * Copyright (C) 2024 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at https://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.authentication;

import java.io.IOException;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.dal.core.OBContext;
import org.openbravo.mobile.core.process.WebServiceAbstractServlet;

@SuppressWarnings("serial")
public class ResetPasswordWithTokenService extends WebServiceAbstractServlet {
  private static final Logger log = LogManager.getLogger();

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    JSONObject result = new JSONObject();
    try {
      OBContext.setAdminMode(true);
      JSONObject body = new JSONObject(
          request.getReader().lines().collect(Collectors.joining(System.lineSeparator())));
      String token = body.optString("token");
      String newPwd = body.optString("newPassword");
      result.put("token", token);
      result.put("newPwd", newPwd);

    } catch (JSONException ex) {
      log.error("Error parsing JSON", ex);
      result = new JSONObject(Map.of("error", ex.getMessage()));
    } finally {
      OBContext.restorePreviousMode();
      writeResult(response, new JSONObject(Map.of("response", result)).toString());
    }
  }
}
