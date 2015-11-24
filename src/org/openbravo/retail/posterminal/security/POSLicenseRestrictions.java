/*
 ************************************************************************************
 * Copyright (C) 2015 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.retail.posterminal.security;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import javax.enterprise.context.ApplicationScoped;

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
import org.openbravo.xmlEngine.XmlDocument;
import org.openbravo.xmlEngine.XmlEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

/**
 * Implements license restrictions for Retail based on defining a maximum number of active POS
 * Terminals per instance.
 * 
 * @author alostale
 *
 */
@ApplicationScoped
public class POSLicenseRestrictions implements ModuleLicenseRestrictions {

  /** keeping count in cache to prevent DB queries each time it is accessed */
  private LoadingCache<String, Integer> countCache;

  private static final String CACHE_KEY = "count";
  private static final Logger log = LoggerFactory.getLogger(POSLicenseRestrictions.class);

  public POSLicenseRestrictions() {
    // initializing cache, keep last count for 10 minutes
    countCache = CacheBuilder.newBuilder().expireAfterWrite(10L, TimeUnit.MINUTES)
        .build(new CacheLoader<String, Integer>() {
          @Override
          public Integer load(String key) throws Exception {
            return countNumberOfActiveTerminals();
          }
        });
  }

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
  public ActivationMsg getActivationMessage(ActivationKey activationKey, String lang) {
    String msg = getPOSTerminalRestrictionMsg(activationKey, lang, 0);

    if (StringUtils.isNotEmpty(msg)) {
      return new ActivationMsg(MsgSeverity.ERROR, msg);
    }

    msg = getPOSTerminalsWarningMsg(activationKey, lang);

    if (StringUtils.isNotEmpty(msg)) {
      return new ActivationMsg(MsgSeverity.WARN, msg);
    }
    return null;
  }

  @Override
  public String getInstanceActivationExtraActionsHtml(XmlEngine xmlEngine) {
    if (checkRestrictions(ActivationKey.getInstance(), null) != LicenseRestriction.POS_TERMINALS_EXCEEDED) {
      return "";
    }

    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/retail/posterminal/security/DeactivateAllTerminalsButton")
        .createXmlDocument();
    return xmlDocument.print().replace("<HTML><BODY><TABLE>", "")
        .replace("</TABLE></BODY></HTML>", "");
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
    if (actualNumberOfTerminals >= warnLimit) {
      return OBMessageUtils
          .messageBD(new DalConnectionProvider(false), "OBPOS_TerminalLimitWarn", lang)
          .replace("%0", Integer.toString(actualNumberOfTerminals))
          .replace("%1", totalAllowed.toString());
    }
    return "";
  }

  /** resets count cache by reading actual count from DB */
  void resetNumberOfTerminals() {
    countCache.invalidateAll();
  }

  private String getPOSTerminalRestrictionMsg(ActivationKey activationKey, String lang,
      int addingTerminals) {
    Long allowedNumberOfTerminals = activationKey.getAllowedPosTerminals();
    if (allowedNumberOfTerminals == ActivationKey.NO_LIMIT || allowedNumberOfTerminals == null) {
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

  /** gets current number of active terminals from cache */
  private int getNumberOfActiveTerminals() {
    long t = System.currentTimeMillis();
    try {
      return countCache.get(CACHE_KEY);
    } catch (ExecutionException e) {
      log.error("Error reading cache", e);
      return countNumberOfActiveTerminals();
    } finally {
      log.debug("read active terminals from cache, took {} ms", System.currentTimeMillis() - t);
    }
  }

  /** counts current number of active terminals from DB */
  private int countNumberOfActiveTerminals() {
    long t = System.currentTimeMillis();
    // admin mode without client check in order to count all terminals in the system
    OBContext.setAdminMode(false);
    try {
      OBCriteria<OBPOSApplications> q = OBDal.getInstance().createCriteria(OBPOSApplications.class);
      q.setFilterOnReadableClients(false);
      q.setFilterOnReadableOrganization(false);
      q.setFilterOnActive(true);
      return q.count();
    } finally {
      log.debug("count active terminals from DB, took {} ms", System.currentTimeMillis() - t);
      OBContext.restorePreviousMode();
    }
  }
}
