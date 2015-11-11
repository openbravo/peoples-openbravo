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
 * All portions are Copyright (C) 2010-2014 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.retail.posterminal;

import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.client.kernel.RequestContext;
import org.openbravo.dal.core.OBContext;
import org.openbravo.test.base.OBBaseTest;

/**
 * Base unit test for retail
 * 
 * @author mtaal
 */

public class OBBaseRetailTest extends OBBaseTest {

  /**
   * Record ID of Client "White Valley"
   */
  protected static final String TEST_RETAIL_CLIENT_ID = "39363B0921BB4293B48383844325E84C";

  /**
   * Record ID of Organization "Vall Blanca Store"
   */
  protected static final String TEST_RETAIL_ORG_ID = "D270A5AC50874F8BA67A88EE977F8E3B";

  /**
   * Record ID of Warehouse "Vall Blanca Store"
   */
  protected static final String TEST_RETAIL_WAREHOUSE_ID = "A154EC30A296479BB078B0AFFD74CA22";

  /**
   * Record ID of Role "White Valley Group Admin"
   */
  protected static final String TEST_RETAIL_WV_ADMIN_ROLE_ID = "E717F902C44C455793463450495FF36B";

  /**
   * Record ID of Role "Vall Blanca User"
   */
  protected static final String TEST_RETAIL_VB_USER_ROLE_ID = "5FA11B3DD8F04C0986C774624809C31E";

  /**
   * Record ID of User "Openbravo"
   */
  protected static final String TEST_RETAIL_OB_USER_ID = "100";

  /**
   * Record ID of User "Vall Blance" - Any user with less privileges than {@link #TEST_USER_ID}
   */
  protected static final String TEST_RETAIL_VB_USER_ID = "3073EDF96A3C42CC86C7069E379522D2";

  /**
   * Record ID of POS Terminal "VBS-1"
   */
  protected static final String TEST_POS_TERMINAL_ID = "9104513C2D0741D4850AE8493998A7C8";

  /**
   * Sets the current user to the {@link #TEST_RETAIL_VB_USER_ID} user.
   * 
   * NOTE: also creates and sets a {@link VariablesSecureApp} in the
   * {@link RequestContext#setVariableSecureApp(VariablesSecureApp)}.
   */
  protected void setTestUserContext() {
    OBContext.setOBContext(TEST_RETAIL_VB_USER_ID, TEST_RETAIL_VB_USER_ROLE_ID,
        TEST_RETAIL_CLIENT_ID, TEST_RETAIL_ORG_ID);
    OBContext obContext = OBContext.getOBContext();
    final VariablesSecureApp vars = new VariablesSecureApp(obContext.getUser().getId(), obContext
        .getCurrentClient().getId(), obContext.getCurrentOrganization().getId(), obContext
        .getRole().getId(), obContext.getLanguage().getLanguage()) {
      // overloaded to prevent error messages in the log as there is no session
      public void setSessionValue(String attribute, String value) {
      }
    };

    RequestContext.get().setVariableSecureApp(vars);
  }
}
