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
package org.openbravo.client.application;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jettison.json.JSONObject;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Startup;
import org.openbravo.client.kernel.ActionHandlerRegistry;
import org.openbravo.client.kernel.BaseActionHandler;
import org.openbravo.client.kernel.KernelServlet;
import org.openbravo.client.kernel.StaticResourceComponent;

/**
 * Is responsible for logging out from the application.
 * 
 * @author mtaal
 * @see StaticResourceComponent
 */
@Name("org.openbravo.client.application.LogOutActionHandler")
@Scope(ScopeType.APPLICATION)
@Startup
@AutoCreate
public class LogOutActionHandler extends BaseActionHandler {

  @Create
  public void initialize() {
    ActionHandlerRegistry.getInstance().registerActionHandler(this);
  }

  public String getName() {
    return "org.openbravo.client.application.LogOutActionHandler";
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.openbravo.client.kernel.BaseActionHandler#execute(javax.servlet.http.HttpServletRequest,
   * javax.servlet.http.HttpServletResponse)
   */
  public void execute(HttpServletRequest request, HttpServletResponse response) {
    KernelServlet.logoutFromApplication(request, response);
  }

  protected JSONObject execute(Map<String, Object> parameters, String data) {
    throw new UnsupportedOperationException();
  }
}
