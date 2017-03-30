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
 * All portions are Copyright (C) 2017 Openbravo SLU 
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.test.base.mock;

import java.util.HashMap;
import java.util.Map;

import org.openbravo.base.secureApp.OrgTree;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.client.kernel.RequestContext;
import org.openbravo.dal.core.OBContext;
import org.openbravo.service.db.DalConnectionProvider;
import org.openbravo.test.base.HiddenObjectHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A mocked VariablesSecureApp to be used to create test requests.
 * 
 * @author alostale
 *
 */
public class VariablesSecureAppMock extends VariablesSecureApp {
  private static final Logger log = LoggerFactory.getLogger(VariablesSecureAppMock.class);
  private Map<String, String> mockedParams;

  /** Creates a mock based on current session settings. */
  public VariablesSecureAppMock() {
    this(new HashMap<String, String>());
  }

  /**
   * Creates a mock based on current session settings, {@code mockedParams} will be use to return
   * parameter values through {@code #getStringParameter(String)} method.
   */
  public VariablesSecureAppMock(Map<String, String> mockedParams) {
    super(OBContext.getOBContext().getUser().getId(), OBContext.getOBContext().getCurrentClient()
        .getId(), OBContext.getOBContext().getCurrentOrganization().getId());

    this.mockedParams = mockedParams;
    OBContext ctx = OBContext.getOBContext();
    DalConnectionProvider con = new DalConnectionProvider(false);
    OrgTree tree = new OrgTree(con, ctx.getCurrentClient().getId());
    setSessionObject("#CompleteOrgTree", tree);
    setSessionValue("#User_Client", "'" + ctx.getCurrentClient().getId() + "'");

    try {
      HiddenObjectHelper.set(RequestContext.get(), "variablesSecureApp", this);
    } catch (Exception e) {
      log.error("Error setting mock vars in context", e);
    }
  }

  @Override
  public String getStringParameter(String parameter) {
    if (mockedParams.containsKey(parameter)) {
      return mockedParams.get(parameter);
    }
    return super.getStringParameter(parameter);
  }

  /** Sets an instance of VariablesSecureApp in request context so it can be used as a mock. */
  public static void setVariablesInRequestContext(VariablesSecureApp vars) {
    try {
      HiddenObjectHelper.set(RequestContext.get(), "variablesSecureApp", vars);
    } catch (Exception e) {
      log.error("Error setting mock vars in context", e);
    }
  }

  /** Creates a new instance of VariablesSecureApp and sets it in request context. */
  public static void setVariablesInRequestContext() {
    setVariablesInRequestContext(new VariablesSecureAppMock());
  }
}
