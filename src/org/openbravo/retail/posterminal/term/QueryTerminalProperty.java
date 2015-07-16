/*
 ************************************************************************************
 * Copyright (C) 2015 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.retail.posterminal.term;

import org.openbravo.retail.posterminal.ProcessHQLQuery;

public abstract class QueryTerminalProperty extends ProcessHQLQuery {

  public abstract String getProperty();

  public abstract boolean returnList();
}
