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

import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.model.ad.access.User;

import com.openbravo.norauto.passwordpolicy.util.PasswordPolicyUtility;

@ApplicationScoped
public class MinLenghtPasswordPolicyRule implements PasswordPolicyRule {
  private static int MINIMUM_LENGTH;

  /**
   * This method check that the password has the minimun lenght defined in the system preferences
   */
  @Override
  public String compliesWithRule(User user, String password) {
    String error = "";
    MINIMUM_LENGTH = PasswordPolicyUtility.getPswMinLength();
    if (!hasMinimumLength(password)) {
      error = String.format(OBMessageUtils.messageBD("ADErrorPasswordMinLength") + "<br/>",
          MINIMUM_LENGTH);
    }
    return error;
  }

  public boolean hasMinimumLength(String password) {
    return password.length() >= MINIMUM_LENGTH;
  }
}
