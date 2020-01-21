/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.service.password;

import org.openbravo.model.ad.access.User;

public interface PasswordPolicyRule {

  public String compliesWithRule(User user, String password);

}
