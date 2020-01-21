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
 * All portions are Copyright (C) 2018 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.service.password;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.openbravo.authentication.ChangePasswordException;
import org.openbravo.dal.core.OBContext;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.ad.access.User;
import org.openbravo.service.db.DalConnectionProvider;

/**
 * Utility class used to check that passwords meets a minimum strength policy.
 *
 * Strong passwords should be at least 8 characters long and contain at least 3 out of the
 * following: Uppercase letters, lowercase letters, digits or special characters.
 * 
 * @author jarmendariz
 */
@ApplicationScoped
public class PasswordStrengthChecker {
  @Inject
  @Any
  Instance<PasswordPolicyRule> policyRules;

  /**
   * Verifies that the given password meets the minimum strength criteria
   *
   * @param password
   *          The password to evaluate
   * @return true if the password is strong enough, false otherwise
   */
  public boolean isStrongPassword(User user, String password) {
    String language = OBContext.getOBContext().getLanguage().getLanguage();

    boolean allRulesComply = true;
    StringBuilder sb = new StringBuilder();
    for (PasswordPolicyRule rule : policyRules) {
      sb.append(rule.compliesWithRule(user, password));
    }
    if (StringUtils.isNotBlank(sb.toString())) {
      allRulesComply = false;
      throwChangePasswordException(String.format(OBMessageUtils.messageBD("CPWeakPasswordTitle")),
          sb.toString(), language);
    }
    return allRulesComply;
  }

  private void throwChangePasswordException(String titleKey, String messageKey, String language)
      throws ChangePasswordException {
    ConnectionProvider conn = new DalConnectionProvider(false);
    OBError errorMsg = new OBError();
    errorMsg.setType("Error");
    errorMsg.setTitle(Utility.messageBD(conn, titleKey, language));
    errorMsg.setMessage(Utility.messageBD(conn, messageKey, language));
    throw new ChangePasswordException(errorMsg.getMessage());
  }

}
