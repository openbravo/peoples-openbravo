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
 * All portions are Copyright (C) 2001-2006 Openbravo SL 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
*/
package org.openbravo.erpCommon.utility;

import org.openbravo.database.ConnectionProvider;
import org.apache.log4j.Logger;
import org.openbravo.base.secureApp.VariablesSecureApp;

/**
 * @author Fernando Iriazabal
 *
 * Abstract Class Handler for the Error management in the application.
 */
public abstract class ErrorTextParser {
  static Logger log4j = Logger.getLogger(ErrorTextParser.class);
  private ConnectionProvider conn;
  private String language = "";
  private String message = "";
  private VariablesSecureApp vars;

  /**
   * Constructor.
   */
  public ErrorTextParser() {
  }

  /**
   * Constructor
   * 
   * @param _data: Object with the database connection handler.
   */
  public void setConnection(ConnectionProvider _data) {
    this.conn = _data;
  }

  /**
   * Getter for the database connection handler.
   * 
   * @return Object with the database connection handler.
   */
  public ConnectionProvider getConnection() {
    return this.conn;
  }

  /**
   * Setter for the language.
   * 
   * @param _data: String with the language.
   */
  public void setLanguage(String _data) {
    if (_data==null) _data = "";
    this.language = _data;
  }

  /**
   * Getter for the language.
   * 
   * @return String with the language.
   */
  public String getLanguage() {
    return ((this.language==null)?"":this.language);
  }

  /**
   * Setter for the message text.
   * 
   * @param _data: String with the new message text.
   */
  public void setMessage(String _data) {
    if (_data==null) _data = "";
    this.message = _data;
  }

  /**
   * Getter for the message text.
   * 
   * @return String with the message text.
   */
  public String getMessage() {
    return ((this.message==null)?"":this.message);
  }

  /**
   * Setter for the session info handler.
   * 
   * @param _data: Object with the session info handler.
   */
  public void setVars(VariablesSecureApp _data) {
    this.vars = _data;
  }

  /**
   * Getter for the session info handler.
   * 
   * @return Object with the session info handler.
   */
  public VariablesSecureApp getVars() {
    return this.vars;
  }

  /**
   * Abstract method to implement the specific error parsing for each database type.
   * 
   * @return Object with the error message parsed.
   * @throws Exception
   */
  public abstract OBError parse() throws Exception;
}
