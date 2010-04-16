/*
 ************************************************************************************
 * Copyright (C) 2010 Openbravo S.L.U.

 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at
  http://www.openbravo.com/legal/obcl.html
  <http://www.openbravo.com/legal/obcl.html>
 ************************************************************************************
 */
package org.openbravo.erpCommon.ad_forms;

import java.sql.Connection;

import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.database.ConnectionProvider;

public abstract class DocFINPaymentTemplate {
  private static final long serialVersionUID = 1L;
  static Logger log4jDocAccDefPlan = Logger.getLogger(DocFINPayment.class);

  /**
   * Constructor
   * 
   */
  public DocFINPaymentTemplate() {
  }

  /**
   * Create Facts (the accounting logic) for
   * 
   * @param as
   *          accounting schema
   * @return Fact
   */
  public abstract Fact createFact(DocFINPayment docAccDefPlan, AcctSchema as,
      ConnectionProvider conn, Connection con, VariablesSecureApp vars) throws ServletException;

  public String getServletInfo() {
    return "Servlet for the accounting";
  } // end of getServletInfo() method
}
