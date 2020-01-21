/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.service.password;

import javax.enterprise.context.ApplicationScoped;

import org.openbravo.authentication.hashing.PasswordHash;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.model.ad.access.User;

@ApplicationScoped
public class DifferentPasswordPolicyRule implements PasswordPolicyRule {

  /**
   * This method verifies that the password isn't the same as the current
   */
  @Override
  public String compliesWithRule(User user, String password) {
    String error = "";
    if (PasswordHash.matches(password, user.getPassword())) {
      error = String.format(OBMessageUtils.messageBD("CPSamePasswordThanOld") + "<br/>");
    }
    return error;
  }
}
