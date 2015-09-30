/*
 ************************************************************************************
 * Copyright (C) 2015 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.retail.posterminal.security;

import org.apache.commons.lang.StringUtils;
import org.openbravo.base.exception.OBException;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.obps.ActivationKey;
import org.openbravo.erpCommon.obps.ActivationKey.LicenseRestriction;
import org.openbravo.erpCommon.obps.ModuleLicenseRestrictions;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.retail.posterminal.OBPOSApplications;
import org.openbravo.service.db.DalConnectionProvider;

/**
 * Implements license restrictions for Retail based on defining a maximum number of active POS
 * Terminals per instance.
 * 
 * @author alostale
 *
 */
public final class POSLicenseRestrictions implements ModuleLicenseRestrictions {

  @Override
  public LicenseRestriction checkRestrictions(ActivationKey activationKey, String currentSession) {
    Long allowedNumberOfTerminals = activationKey.getAllowedPosTerminals();

    if (allowedNumberOfTerminals != ActivationKey.NO_LIMIT
        && getNumberOfActiveTerminals() > allowedNumberOfTerminals) {
      return LicenseRestriction.POS_TERMINALS_EXCEEDED;
    } else {
      return LicenseRestriction.NO_RESTRICTION;
    }
  }

  @Override
  public String getLoginPageMessage(ActivationKey activationKey, String lang) {
    return getPOSTerminalRestrictionMsg(activationKey, lang, 0);
  }

  /**
   * Checks whether a new POS terminal can be added to this instance according to its license. In
   * case it is not possible an {@link OBException} is thrown.
   */
  public void checkRestrictionForNewTerminal() {
    ActivationKey activationKey = ActivationKey.getInstance();
    String msg = getPOSTerminalRestrictionMsg(activationKey, OBContext.getOBContext().getLanguage()
        .getLanguage(), 1);
    if (StringUtils.isNotBlank(msg)) {
      throw new OBException(msg);
    }
  }

  /** Returns a message if the warn limit has been reached, if not empty string */
  public String getPOSTerminalsWarningMsg(ActivationKey activationKey, String lang) {
    Long totalAllowed = activationKey.getAllowedPosTerminals();
    Long warnLimit = activationKey.getPosTerminalsWarn();
    if (warnLimit == null || totalAllowed == ActivationKey.NO_LIMIT) {
      return "";
    }

    int actualNumberOfTerminals = getNumberOfActiveTerminals();
    if (warnLimit == 0) {
      return OBMessageUtils.messageBD(new DalConnectionProvider(false), "OBPOS_TerminalNotAllowed",
          lang);
    } else if (actualNumberOfTerminals > warnLimit) {
      return OBMessageUtils
          .messageBD(new DalConnectionProvider(false), "OBPOS_TerminalLimitWarn", lang)
          .replace("%0", Integer.toString(actualNumberOfTerminals))
          .replace("%1", totalAllowed.toString());
    }
    return "";
  }

  private String getPOSTerminalRestrictionMsg(ActivationKey activationKey, String lang,
      int addingTerminals) {
    Long allowedNumberOfTerminals = activationKey.getAllowedPosTerminals();
    if (allowedNumberOfTerminals == ActivationKey.NO_LIMIT) {
      return "";
    }

    int actualNumberOfTerminals = getNumberOfActiveTerminals() + addingTerminals;
    if (allowedNumberOfTerminals == 0 && actualNumberOfTerminals > 0) {
      return OBMessageUtils.messageBD(new DalConnectionProvider(false), "OBPOS_TerminalNotAllowed",
          lang);
    } else if (actualNumberOfTerminals > allowedNumberOfTerminals) {
      return OBMessageUtils.messageBD(new DalConnectionProvider(false),
          "OBPOS_TerminalLimitExceeded", lang).replace("%0", allowedNumberOfTerminals.toString());
    }
    return "";
  }

  private int getNumberOfActiveTerminals() {
    // admin mode without client check in order to count all terminals in the system
    OBContext.setAdminMode(false);
    try {
      OBCriteria<OBPOSApplications> q = OBDal.getInstance().createCriteria(OBPOSApplications.class);
      q.setFilterOnReadableClients(false);
      q.setFilterOnReadableOrganization(false);
      q.setFilterOnActive(true);
      return q.count();
    } finally {
      OBContext.restorePreviousMode();
    }
  }

}
