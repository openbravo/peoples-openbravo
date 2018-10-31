/*
 ************************************************************************************
 * Copyright (C) 2018 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.retail.posterminal;

import org.codehaus.jettison.json.JSONObject;
import org.openbravo.model.common.order.OrderLine;

/**
 * Classes implementing this interface will be executed before create order lines in OrderLoader
 * process. They can modify jsonorder parameter.
 * 
 */
public abstract class OrderLoaderCreateOrderlineHook {
  public abstract void exec(JSONObject jsonorderLine, OrderLine orderLine) throws Exception;
}
