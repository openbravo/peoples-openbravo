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

/**
 * Classes implementing this interface will be executed before loading orders. They can modify
 * jsonorder parameter.
 * 
 */
public abstract class OrderLoaderModifiedPreProcessHook {
  public abstract void exec(JSONObject jsonorder) throws Exception;
}
