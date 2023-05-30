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
 * All portions are Copyright (C) 2022-2023 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.service.externalsystem.http;

import org.openbravo.service.externalsystem.HttpExternalSystemData;

/**
 * Provides the information required to authenticate an HTTP request with an authorization method in
 * particular. Classes extending this class must be annotated with {@link HttpAuthorizationMethod}
 * to declare the authorization it implements.
 */
public interface HttpAuthorizationProvider {

  /**
   * Initializes the required information based on the provided configuration
   * 
   * @param configuration
   *          Provides the configuration data of an HTTP protocol based external system
   */
  public void init(HttpExternalSystemData configuration);

  /**
   * This method allows to execute additional actions required by the authorization method before
   * retrying a request for which an error response is received. It also allows to skip the retry if
   * needed by using the returned value: if true is returned then the retry is done and if false is
   * returned then no retry is attempted.
   * 
   * @param statusCode
   *          The status code of the external system response
   * @return true if it is necessary to retry the connection with the External System or false to
   *         skip the retry
   */
  public default boolean handleRequestRetry(int statusCode) {
    return false;
  }
}
