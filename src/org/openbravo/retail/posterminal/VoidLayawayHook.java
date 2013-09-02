/*
 ************************************************************************************
 * Copyright (C) 2013 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.retail.posterminal;

import org.codehaus.jettison.json.JSONObject;
import org.openbravo.model.common.order.Order;

public interface VoidLayawayHook {
  public void exec(JSONObject jsonorder, Order order) throws Exception;
}

// Example of a hook:
// import javax.enterprise.context.ApplicationScoped;
//
// import org.openbravo.model.common.order.Order;
//
// @ApplicationScoped
// public class VoidLayawayHookTest implements VoidLayawayHook {
//
// @Override
// public void exec(JSONObject jsonorder, Order order)
// throws Exception {
// // TODO Auto-generated method stub
// System.out.println("somebody is calling me");
// }
//
// }