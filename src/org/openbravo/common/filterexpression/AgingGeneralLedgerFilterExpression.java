/*
 ************************************************************************************
 * Copyright (C) 2017 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.common.filterexpression;

import java.util.Map;

import javax.script.ScriptException;

import org.openbravo.client.application.FilterExpression;
import org.openbravo.client.application.ParameterUtils;
import org.openbravo.client.kernel.RequestContext;
import org.openbravo.erpCommon.utility.OBLedgerUtils;

public class AgingGeneralLedgerFilterExpression implements FilterExpression {

  @Override
  public String getExpression(Map<String, String> requestMap) {

    try {
      String defaultOrg = (String)ParameterUtils
          .getJSExpressionResult(requestMap, RequestContext.get().getSession(),
              "OB.getFilterExpression('org.openbravo.common.filterexpression.AgingOrganizationFilterExpression')");
      return OBLedgerUtils.getOrgLedger(defaultOrg);
    } catch (ScriptException e) {
      e.printStackTrace();
    }
    return null;
  }

}
