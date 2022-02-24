/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.1  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html 
 * Software distributed under the License  is  distributed  on  an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific  language  governing  rights  and  limitations
 * under the License. 
 * The Original Code is Openbravo ERP. 
 * The Initial Developer of the Original Code is Openbravo SLU 
 * All portions are Copyright (C) 2022 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.service.externalsystem.http;

import java.util.Map;

import org.openbravo.service.externalsystem.HttpExternalSystemData;

/**
 * Provides the information required to authenticate an HTTP request with an authorization method in
 * particular. Classes extending this class must be annotated with {@link HttpAuthorizationMethod}
 * to declare the authorization it implements.
 */
public abstract class HttpAuthorizationProvider {

  /**
   * Initializes the required information based on the provided configuration
   * 
   * @param configuration
   *          Provides the configuration data of an HTTP protocol based external system
   */
  protected void init(HttpExternalSystemData configuration) {
  }

  /**
   * Retrieves the HTTP headers to be included into the HTTP request to authenticate it with the
   * authorization method
   * 
   * @return a map with the security headers where for each entry the key is the header name and the
   *         values is the header value
   */
  public abstract Map<String, String> getHeaders();
}
