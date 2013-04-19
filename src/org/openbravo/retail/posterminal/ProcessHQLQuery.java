/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.retail.posterminal;

public abstract class ProcessHQLQuery extends org.openbravo.mobile.core.process.ProcessHQLQuery {
  final String WEBPOS_FORM_ID = "B7B7675269CD4D44B628A2C6CF01244F";

  @Override
  protected String getFormId() {
    return WEBPOS_FORM_ID;
  }
}
