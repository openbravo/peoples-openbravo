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
 * All portions are Copyright (C) 2023 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.authentication;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.enterprise.context.ApplicationScoped;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.openbravo.base.secureApp.LoginHandler;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.client.kernel.RequestContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.SequenceIdData;

/**
 * Keeps in session the available login provider configurations linked to a random UUID. In the case
 * of OAuth 2.0, this UUID is used for preventing cross-site request forgery attacks by comparing
 * that UUID with the value received in the state parameter of the authorization requests.
 */
@ApplicationScoped
public class LoginStateHandler {
  public static final String LOGIN_STATE = "#LOGIN_STATE";

  /**
   * Saves the given configuration ID in the session, linked to a random generated UUID.
   *
   * @param configId
   *          The ID of the record where the configuration is kept
   *
   * @return the generated UUID
   */
  public String addNewConfiguration(String configId) {
    Map<String, String> loginState = getLoginState();
    String key = SequenceIdData.getUUID();
    loginState.put(key, configId);
    RequestContext.get().setSessionAttribute(LOGIN_STATE, loginState);
    return key;
  }

  /**
   * Validates if the given key is valid in the current session.
   *
   * @param key
   *          The key to be validated
   *
   * @return true if the key is valid or false in any other case.
   */
  public boolean isValidKey(String key) {
    return getLoginState().containsKey(key);
  }

  /**
   * Gets the configuration linked to the given key.
   *
   * @param clz
   *          The BaseOBObject class of the record where the configuration is kept
   * @param key
   *          The key linked to the configuration
   *
   * @return true if the key is valid or false in any other case.
   */
  public <T extends BaseOBObject> T getConfiguration(Class<T> clz, String key) {
    String configId = getLoginState().get(key);
    return OBDal.getInstance().get(clz, configId);
  }

  /**
   * Invalidates the session of the provided request and generates a new one on the fly, thus
   * generating a new JSSESSIONID cookie but preserving the login state information in the session.
   *
   * @see LoginHandler#doPost(HttpServletRequest, javax.servlet.http.HttpServletResponse)
   *
   * @param request
   *          The request whose session should be invalidate
   */
  public void resetCookieId(HttpServletRequest request) {
    HttpSession httpSession = request.getSession(false);
    Object loginState = null;
    if (httpSession != null && !httpSession.isNew()) {
      loginState = httpSession.getAttribute(LOGIN_STATE);
      httpSession.invalidate();
    }
    httpSession = request.getSession(true);
    if (loginState != null) {
      // keep the login state for the new session, used by the external authentication providers
      httpSession.setAttribute(LOGIN_STATE, loginState);
    }
  }

  private Map<String, String> getLoginState() {
    @SuppressWarnings("unchecked")
    Map<String, String> loginState = RequestContext.get().getSessionAttribute(LOGIN_STATE) != null
        ? (Map<String, String>) RequestContext.get().getSessionAttribute(LOGIN_STATE)
        : new ConcurrentHashMap<>();
    return loginState;
  }
}
