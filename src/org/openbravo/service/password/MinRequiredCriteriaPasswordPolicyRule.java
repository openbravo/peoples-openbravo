/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.service.password;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;

import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.model.ad.access.User;
import org.openbravo.model.ad.system.SystemInformation;

@ApplicationScoped
public class MinRequiredCriteriaPasswordPolicyRule implements PasswordPolicyRule {

  private static int MIN_REQUIRED_CRITERIA;
  private List<PasswordStrengthCriterion> strengthCriteria;

  /**
   * This method check that the password has the character groups defined in the system preferences
   */
  @Override
  public String compliesWithRule(User user, String password) {
    String error = "";
    MIN_REQUIRED_CRITERIA = getPswCharGroups();
    init();
    if (!(getCriteriaScore(password) >= MIN_REQUIRED_CRITERIA)) {
      error = String.format(OBMessageUtils.messageBD("ADErrorPasswordMinCriteria") + "<br/>",
          MIN_REQUIRED_CRITERIA);
    }
    return error;
  }

  @PostConstruct
  private void init() {
    strengthCriteria = new ArrayList<>(4);
    strengthCriteria.add(getUppercaseCriterion());
    strengthCriteria.add(getLowercaseCriterion());
    strengthCriteria.add(getDigitsCriterion());
    strengthCriteria.add(getSpecialCharactersCriterion());
  }

  private int getCriteriaScore(String password) {
    int score = 0;

    for (PasswordStrengthCriterion criterion : strengthCriteria) {
      if (criterion.match(password)) {
        score += 1;
      }
    }

    return score;
  }

  private interface PasswordStrengthCriterion {
    boolean match(String password);
  }

  private PasswordStrengthCriterion getUppercaseCriterion() {
    return new PasswordStrengthCriterion() {
      @Override
      public boolean match(String password) {
        return password.matches(".*[A-Z].*");
      }
    };
  }

  private PasswordStrengthCriterion getLowercaseCriterion() {
    return new PasswordStrengthCriterion() {
      @Override
      public boolean match(String password) {
        return password.matches(".*[a-z].*");
      }
    };
  }

  private PasswordStrengthCriterion getDigitsCriterion() {
    return new PasswordStrengthCriterion() {
      @Override
      public boolean match(String password) {
        return password.matches(".*[0-9].*");
      }
    };
  }

  private PasswordStrengthCriterion getSpecialCharactersCriterion() {
    return new PasswordStrengthCriterion() {
      @Override
      public boolean match(String password) {
        return password.matches(".*[`~!@#$%€^&*()_\\-+={}\\[\\]|:;\"' <>,.?/].*");
      }
    };
  }

  /**
   * Get the number the password char groups of the system info
   * 
   * @return Password Character Groups number
   */
  private int getPswCharGroups() {
    int charGroups = 0;
    SystemInformation systemInf = (SystemInformation) OBDal.getInstance()
        .createCriteria(SystemInformation.class)
        .uniqueResult();
    if (systemInf != null && systemInf.getPasswordCharacterGroups() != null) {
      charGroups = Math.toIntExact(systemInf.getPasswordCharacterGroups());
    }
    return charGroups;
  }

}
