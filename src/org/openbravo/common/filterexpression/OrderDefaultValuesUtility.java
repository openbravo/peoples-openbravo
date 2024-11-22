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
 * All portions are Copyright (C) 2024 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.common.filterexpression;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 * Utility class returns order Id from context
 */

public class OrderDefaultValuesUtility {
  private static final String INPC_ORDER_ID_PARAM = "inpcOrderId";
  private static final String C_ORDER_ID_PARAM = "C_ORDER_ID";

  /**
   * Get Order Id from context
   */

  public static String getOrderIdFromContext(final JSONObject context) throws JSONException {
    if (contextHascOrderIdParam(context)) {
      return context.getString(C_ORDER_ID_PARAM);
    } else if (contextHasInpcOrderIdParam(context)) {
      return context.getString(INPC_ORDER_ID_PARAM);
    } else {
      return null;
    }
  }

  /**
   * Check whether context has parameter INPC_ORDER_ID_PARAM
   */
  private static boolean contextHasInpcOrderIdParam(final JSONObject context) throws JSONException {
    return context.has(INPC_ORDER_ID_PARAM) && context.get(INPC_ORDER_ID_PARAM) != JSONObject.NULL
        && StringUtils.isNotEmpty(context.getString(INPC_ORDER_ID_PARAM));
  }

  /**
   * Check whether context has parameter C_ORDER_ID_PARAM
   */

  private static boolean contextHascOrderIdParam(final JSONObject context) throws JSONException {
    return context.has(C_ORDER_ID_PARAM) && context.get(C_ORDER_ID_PARAM) != JSONObject.NULL
        && StringUtils.isNotEmpty(context.getString(C_ORDER_ID_PARAM));
  }
}
