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

package org.openbravo.wad.validation;

import org.openbravo.data.FieldProvider;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.wad.validation.WADValidationResult.WADValidationType;

/**
 * Performs a series of validations for WAD tabs
 * 
 */
public class WADValidator {
  private FieldProvider[] tabs;
  private ConnectionProvider conn;

  /**
   * Constructor
   * 
   * @param conn
   *          Database ConnectionProvider
   * @param tabs
   *          Tabs to check
   */
  public WADValidator(ConnectionProvider conn, FieldProvider[] tabs) {
    this.tabs = tabs;
    this.conn = conn;
  }

  /**
   * Performs the validations on the assigned tabs
   * 
   * @return the result of the validations
   */
  public WADValidationResult validate() {
    WADValidationResult result = new WADValidationResult();
    if (tabs != null && tabs.length > 0) {
      validateIdentifier(result);
      validateKey(result);
    }
    return result;
  }

  private void validateIdentifier(WADValidationResult result) {
    try {
      WADValidatorData data[] = WADValidatorData.checkIdentifier(conn, getTabIDs());
      for (WADValidatorData issue : data) {
        result.addError(WADValidationType.MISSING_IDENTIFIER, issue.windowname + " > "
            + issue.tabname + ": table " + issue.tablename + " has not identifier.");
      }
    } catch (Exception e) {
      result.addWarning(WADValidationType.SQL,
          "Error when executing query for validating identifiers: " + e.getMessage());
    }
  }

  private void validateKey(WADValidationResult result) {
    try {
      WADValidatorData data[] = WADValidatorData.checkKey(conn, getTabIDs());
      for (WADValidatorData issue : data) {
        result.addError(WADValidationType.MISSING_KEY, issue.windowname + " > " + issue.tabname
            + ": table " + issue.tablename + " has not primary key.");
      }
    } catch (Exception e) {
      result.addWarning(WADValidationType.SQL,
          "Error when executing query for validating identifiers: " + e.getMessage());
    }
  }

  private String getTabIDs() {
    StringBuffer result = new StringBuffer();
    for (FieldProvider tab : tabs) {
      if (result.length() > 0) {
        result.append(", ");
      }
      result.append("'").append(tab.getField("tabId")).append("'");
    }
    return result.toString();
  }
}
