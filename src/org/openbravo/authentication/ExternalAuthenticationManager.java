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
 * All portions are Copyright (C) 2023 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.authentication;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.base.Prioritizable;

/**
 * Provides authentication using an external authentication provider. Classes extending this one
 * must be annotated with the {@link AuthenticationType} annotation so they can be properly selected
 * before starting with the authentication process.
 */
public abstract class ExternalAuthenticationManager extends AuthenticationManager
    implements Prioritizable {

  @Override
  public String doAuthenticate(HttpServletRequest request, HttpServletResponse response) {
    AuthenticatedUser user = doExternalAuthentication(request, response);
    if (user.getUserName() != null) {
      loginName.set(user.getUserName());
    }
    return user.getId();
  }

  /**
   * To be implemented with the logic of the external authentication
   *
   * @param request
   *          HTTP request object to handle parameters and session attributes
   * @param response
   *          HTTP response object to handle possible redirects
   * @return the information of the successfully authenticated user
   */
  public abstract AuthenticatedUser doExternalAuthentication(HttpServletRequest request,
      HttpServletResponse response);
}
