/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.0  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html 
 * Software distributed under the License  is  distributed  on  an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific  language  governing  rights  and  limitations
 * under the License. 
 * The Original Code is Openbravo ERP. 
 * The Initial Developer of the Original Code is Openbravo SL 
 * All portions are Copyright (C) 2009 Openbravo SL 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.service.system;

import org.openbravo.database.ConnectionProvider;
import org.openbravo.service.system.WADValidationResult.WADValidationType;

/**
 * Performs a series of validations for WAD tabs. It does not use DAL but sqlc not to have to init
 * DAL for each compilation.
 * 
 */
public class WADValidator {
  private String modules;
  private ConnectionProvider conn;
  private String checkAll;

  /**
   * Constructor
   * 
   * @param conn
   *          Database ConnectionProvider
   * @param moduleId
   *          Module to check
   */
  public WADValidator(ConnectionProvider conn, String modules) {
    checkAll = (modules == null || modules.equals("%") || modules.equals("")) ? "Y" : "N";
    this.modules = checkAll.equals("Y") ? "" : modules;
    this.conn = conn;
  }

  /**
   * Performs the validations on the assigned tabs
   * 
   * @return the result of the validations
   */
  public WADValidationResult validate() {
    WADValidationResult result = new WADValidationResult();
    validateIdentifier(result);
    validateKey(result);
    return result;
  }

  private void validateIdentifier(WADValidationResult result) {
    try {
      WADValidatorData data[] = WADValidatorData.checkIdentifier(conn, modules, checkAll);
      for (WADValidatorData issue : data) {
        result.addError(WADValidationType.MISSING_IDENTIFIER, "Table " + issue.tablename
            + " has not identifier.");
      }
    } catch (Exception e) {
      result.addWarning(WADValidationType.SQL,
          "Error when executing query for validating identifiers: " + e.getMessage());
    }
  }

  private void validateKey(WADValidationResult result) {
    try {
      WADValidatorData data[] = WADValidatorData.checkKey(conn, modules, checkAll);
      for (WADValidatorData issue : data) {
        result.addError(WADValidationType.MISSING_KEY, "Table " + issue.tablename
            + " has not primary key.");
      }
    } catch (Exception e) {
      result.addWarning(WADValidationType.SQL,
          "Error when executing query for validating identifiers: " + e.getMessage());
    }
  }
}
