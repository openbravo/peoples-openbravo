/*
 ************************************************************************************
 * Copyright (C) 2016 Openbravo S.L.U.
 * Licensed under the Apache Software License version 2.0
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to  in writing,  software  distributed
 * under the License is distributed  on  an  "AS IS"  BASIS,  WITHOUT  WARRANTIES  OR
 * CONDITIONS OF ANY KIND, either  express  or  implied.  See  the  License  for  the
 * specific language governing permissions and limitations under the License.
 ************************************************************************************
 */

package org.openbravo.authentication;

import org.openbravo.erpCommon.utility.OBError;

/**
 * This exception is used in case password for user has expired. Exception is launched in case that
 * last update password date for user plus validity days defined for client has been reached
 * 
 */
public class AuthenticationExpirationPasswordException extends AuthenticationException {
  private static final long serialVersionUID = 1L;
  private OBError error;
  private boolean passwordExpiration;

  public AuthenticationExpirationPasswordException(String msg) {
    super(msg);
    this.error = null;
  }

  public AuthenticationExpirationPasswordException(String msg, Throwable cause) {
    super(msg, cause);
    this.error = null;
  }

  public AuthenticationExpirationPasswordException(String msg, OBError error,
      boolean passwordExpiration) {
    super(msg, false);
    this.error = error;
    this.passwordExpiration = passwordExpiration;
    this.getLogger().error(error.getTitle());

  }

  public OBError getOBError() {
    return error;
  }

  public boolean isPasswordExpiration() {
    return passwordExpiration;
  }
}