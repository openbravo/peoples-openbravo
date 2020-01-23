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

import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.model.ad.access.User;
import org.openbravo.model.ad.system.SystemInformation;

@ApplicationScoped
public class MinLenghtPasswordPolicyRule implements PasswordPolicyRule {
  private static int MINIMUM_LENGTH;

  /**
   * This method check that the password has the minimun lenght defined in the system preferences
   */
  @Override
  public String compliesWithRule(User user, String password) {
    String error = "";
    MINIMUM_LENGTH = getPswMinLength();
    if (!hasMinimumLength(password)) {
      error = String.format(OBMessageUtils.messageBD("ADErrorPasswordMinLength") + "<br/>",
          MINIMUM_LENGTH);
    }
    return error;
  }

  public boolean hasMinimumLength(String password) {
    return password.length() >= MINIMUM_LENGTH;
  }

  /**
   * Get the minimun lenght of the system info
   * 
   * @return Password Minimun Length number
   */
  private int getPswMinLength() {
    int minLength = 0;
    SystemInformation systemInf = (SystemInformation) OBDal.getInstance()
        .createCriteria(SystemInformation.class)
        .uniqueResult();
    if (systemInf != null && systemInf.getPasswordMinimunLength() != null) {
      minLength = Math.toIntExact(systemInf.getPasswordMinimunLength());
    }
    return minLength;
  }
}
