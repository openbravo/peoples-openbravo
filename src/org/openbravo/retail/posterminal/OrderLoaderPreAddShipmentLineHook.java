/*
 ************************************************************************************
 * Copyright (C) 2017 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.retail.posterminal;

import org.codehaus.jettison.json.JSONObject;
import org.openbravo.model.common.enterprise.Locator;
import org.openbravo.model.common.order.Order;
import org.openbravo.model.common.order.OrderLine;

public abstract class OrderLoaderPreAddShipmentLineHook {
  public static enum OrderLoaderPreAddShipmentLineHook_Actions {
    ACTION_SINGLEBIN, ACTION_LAST_ATTEMPT, ACTION_STANDARD_SALE, ACTION_RETURN
  }

  final static public String CONST_ACTION_SINGLEBIN = "SingleBin";
  final static public String CONST_ACTION_RETURN = "Return";
  final static public String CONST_ACTION_STANDARD_SALE = "StandardSale_Iterate_Stock_Propossed";
  final static public String CONST_ACTION_LAST_ATTEMPT = "StandardSale_Last_Attempt";

  public static String getActionString(OrderLoaderPreAddShipmentLineHook_Actions _action) {
    switch (_action) {
    case ACTION_SINGLEBIN:
      return CONST_ACTION_SINGLEBIN;
    case ACTION_RETURN:
      return CONST_ACTION_RETURN;
    case ACTION_STANDARD_SALE:
      return CONST_ACTION_STANDARD_SALE;
    case ACTION_LAST_ATTEMPT:
      return CONST_ACTION_LAST_ATTEMPT;
    default:
      return "";
    }
  }

  // Return true if everything goes well
  public abstract OrderLoaderPreAddShipmentLineHook_Response exec(
      OrderLoaderPreAddShipmentLineHook_Actions action, JSONObject jsonorderline,
      OrderLine orderline, JSONObject jsonorder, Order order, Locator bin) throws Exception;
}