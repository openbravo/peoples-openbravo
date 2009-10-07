/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.0  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html 
 * Software distributed under the License  is  distributed  on  an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific  language  governing  rights  and  limitations
 * under the License. 
 * The Original Code is Openbravo ERP. 
 * The Initial Developer of the Original Code is Openbravo SL 
 * All portions are Copyright (C) 2009 Openbravo SL 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.erpCommon.obps;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Properties;
import java.util.zip.CRC32;

import javax.crypto.Cipher;

import org.apache.log4j.Appender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.hibernate.criterion.Expression;
import org.openbravo.base.session.OBPropertiesProvider;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.ad.access.Session;

public class ActivationKey {

  private final static String OB_PUBLIC_KEY = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCPwCM5RfisLvWhujHajnLEjEpLC7DOXLySuJmHBqcQ8AQ63yZjlcv3JMkHMsPqvoHF3s2ztxRcxBRLc9C2T3uXQg0PTH5IAxsV4tv05S+tNXMIajwTeYh1LCoQyeidiid7FwuhtQNQST9/FqffK1oVFBnWUfgZKLMO2ZSHoEAORwIDAQAB";

  private boolean isActive = false;
  private boolean hasActivationKey = false;
  private String errorMessage = "";
  private String messageType = "Error";
  private Properties instanceProperties;
  private static final Logger log = Logger.getLogger(ActivationKey.class);
  private String strPublicKey;
  private static boolean opsLog = false;
  private static String opsLogId;
  private Long pendingTime;
  private boolean hasExpired = false;

  private boolean notActiveYet = false;

  public enum LicenseRestriction {
    NO_RESTRICTION, OPS_INSTANCE_NOT_ACTIVE, NUMBER_OF_SOFT_USERS_REACHED, NUMBER_OF_CONCURRENT_USERS_REACHED
  }

  public enum CommercialModuleStatus {
    NO_SUBSCRIBED, ACTIVE, EXPIRED, NO_ACTIVE_YET
  }

  private static final int MILLSECS_PER_DAY = 24 * 60 * 60 * 1000;

  public ActivationKey() {
    org.openbravo.model.ad.system.System sys = OBDal.getInstance().get(
        org.openbravo.model.ad.system.System.class, "0");
    strPublicKey = sys.getInstanceKey();
    String activationKey = sys.getActivationKey();

    if (strPublicKey == null || activationKey == null || strPublicKey.equals("")
        || activationKey.equals("")) {
      hasActivationKey = false;
      setLogger();
      return;
    }

    PublicKey pk = getPublicKey(strPublicKey);
    if (pk == null) {
      hasActivationKey = true;
      errorMessage = "@NotAValidKey@";
      setLogger();
      return;
    }
    hasActivationKey = true;
    try {
      PublicKey obPk = getPublicKey(OB_PUBLIC_KEY); // get OB public key to check signature
      Signature signer = Signature.getInstance("MD5withRSA");
      signer.initVerify(obPk);

      Cipher cipher = Cipher.getInstance("RSA");

      ByteArrayInputStream bis = new ByteArrayInputStream(org.apache.commons.codec.binary.Base64
          .decodeBase64(activationKey.getBytes()));
      ByteArrayOutputStream bos = new ByteArrayOutputStream();

      // Encryptation only accepts 128B size, it must be chuncked
      final byte[] buf = new byte[128];
      final byte[] signature = new byte[128];

      // read the signature
      if (!(bis.read(signature) > 0)) {
        isActive = false;
        errorMessage = "@NotSigned@";
        setLogger();
        return;
      }

      // decrypt
      while ((bis.read(buf)) > 0) {
        cipher.init(Cipher.DECRYPT_MODE, pk);
        bos.write(cipher.doFinal(buf));
      }

      // verify signature
      signer.update(bos.toByteArray());
      boolean signed = signer.verify(signature);
      log.debug("signature length:" + buf.length);
      log.debug("singature:" + (new BigInteger(signature).toString(16).toUpperCase()));
      log.debug("signed:" + signed);
      if (!signed) {
        isActive = false;
        errorMessage = "@NotSigned@";
        setLogger();
        return;
      }

      byte[] props = bos.toByteArray();

      ByteArrayInputStream isProps = new ByteArrayInputStream(props);
      InputStreamReader reader = new InputStreamReader(isProps, "UTF-8");
      instanceProperties = new Properties();

      instanceProperties.load(reader);
    } catch (Exception e) {
      isActive = false;
      errorMessage = "@NotAValidKey@";
      e.printStackTrace();
      setLogger();
      return;
    }

    // Check for dates to know if the instance is active
    SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd");
    Date startDate = null;
    Date endDate = null;

    try {
      startDate = sd.parse(getProperty("startdate"));

      if (getProperty("enddate") != null)
        endDate = sd.parse(getProperty("enddate"));

    } catch (Exception e) {
      errorMessage = "@ErrorReadingDates@";
      isActive = false;
      log.error(e);
      setLogger();
      return;
    }
    String dateFormat = OBPropertiesProvider.getInstance().getOpenbravoProperties().getProperty(
        "dateFormat.java");
    SimpleDateFormat outputFormat = new SimpleDateFormat(dateFormat);
    Date now = new Date();
    if (startDate == null || now.before(startDate)) {
      isActive = false;
      notActiveYet = true;
      errorMessage = "@OPSNotActiveTill@ " + outputFormat.format(startDate);
      messageType = "Warning";
      setLogger();
      return;
    }
    if (endDate != null) {
      pendingTime = ((endDate.getTime() - now.getTime()) / MILLSECS_PER_DAY) + 1;
      if (now.after(endDate)) {
        isActive = false;
        hasExpired = true;

        errorMessage = "@OPSActivationExpired@ " + outputFormat.format(endDate);

        setLogger();
        return;
      }
    }
    isActive = true;
    setLogger();
  }

  @SuppressWarnings( { "static-access", "unchecked" })
  private void setLogger() {
    if (isActive() && !opsLog) {
      // add instance id to logger
      Enumeration<Appender> appenders = log.getRoot().getAllAppenders();
      while (appenders.hasMoreElements()) {
        Appender appender = appenders.nextElement();
        if (appender.getLayout() instanceof PatternLayout) {
          PatternLayout l = (PatternLayout) appender.getLayout();
          opsLogId = getOpsLogId();
          l.setConversionPattern(opsLogId + l.getConversionPattern());
        }
      }
      opsLog = true;
    }

    if (!isActive() && opsLog) {

      // remove instance id from logger
      Enumeration<Appender> appenders = log.getRoot().getAllAppenders();
      while (appenders.hasMoreElements()) {
        Appender appender = appenders.nextElement();
        if (appender.getLayout() instanceof PatternLayout) {
          PatternLayout l = (PatternLayout) appender.getLayout();
          String pattern = l.getConversionPattern();
          if (pattern.startsWith(opsLogId)) {
            l.setConversionPattern(l.getConversionPattern().substring(opsLogId.length()));
          }
        }
      }
      opsLog = false;
    }

  }

  private String getOpsLogId() {
    CRC32 crc = new CRC32();
    crc.update(getPublicKey().getBytes());
    return Long.toHexString(crc.getValue()) + " ";
  }

  private PublicKey getPublicKey(String strPublickey) {
    try {
      KeyFactory keyFactory = KeyFactory.getInstance("RSA");
      byte[] rawPublicKey = org.apache.commons.codec.binary.Base64.decodeBase64(strPublickey
          .getBytes());

      X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(rawPublicKey);
      return keyFactory.generatePublic(publicKeySpec);
    } catch (Exception e) {
      log.error(e);
      return null;
    }
  }

  public String getPublicKey() {
    return strPublicKey;
  }

  public boolean hasActivationKey() {
    return hasActivationKey;
  }

  public boolean isActive() {
    return isActive;
  }

  public static boolean isActiveInstance() {
    return opsLog;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  public String getMessageType() {
    return messageType;
  }

  public boolean isOPSInstance() {
    return instanceProperties != null;
  }

  /**
   * Checks the current activation key
   * 
   * @return {@link LicenseRestriction} with the status of the restrictions
   */
  public LicenseRestriction checkOPSLimitations() {
    if (!isOPSInstance())
      return LicenseRestriction.NO_RESTRICTION;

    if (!isActive)
      return LicenseRestriction.OPS_INSTANCE_NOT_ACTIVE;

    if (getProperty("lincensetype").equals("USR")) {
      Long softUsers = null;
      if (getProperty("limituserswarn") != null) {
        softUsers = new Long(getProperty("limituserswarn"));
      }

      Long maxUsers = new Long(getProperty("limitusers"));

      if (maxUsers != 0) {

        boolean adminMode = OBContext.getOBContext().isInAdministratorMode();
        OBContext.getOBContext().setInAdministratorMode(true);

        OBCriteria<Session> obCriteria = OBDal.getInstance().createCriteria(Session.class);
        obCriteria.add(Expression.eq(Session.PROPERTY_SESSIONACTIVE, true));
        int currentSessions = obCriteria.list().size();
        OBContext.getOBContext().setInAdministratorMode(adminMode);

        if (currentSessions >= maxUsers) {
          return LicenseRestriction.NUMBER_OF_CONCURRENT_USERS_REACHED;
        }

        if (softUsers != null && currentSessions >= softUsers) {
          return LicenseRestriction.NUMBER_OF_SOFT_USERS_REACHED;
        }
      }
    }
    return LicenseRestriction.NO_RESTRICTION;
  }

  public String toString(ConnectionProvider conn, String lang) {
    String dateFormat = OBPropertiesProvider.getInstance().getOpenbravoProperties().getProperty(
        "dateFormat.java");
    SimpleDateFormat outputFormat = new SimpleDateFormat(dateFormat);

    SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd");
    Date startDate = null;
    Date endDate = null;
    try {
      startDate = sd.parse(getProperty("startdate"));
      if (getProperty("enddate") != null)
        endDate = sd.parse(getProperty("enddate"));
    } catch (ParseException e) {
      log.error("Error parsing date", e);
    }
    StringBuffer sb = new StringBuffer();
    if (instanceProperties != null) {
      sb.append("<tr><td>").append(Utility.messageBD(conn, "OPSCustomer", lang))
          .append("</td><td>").append(getProperty("customer")).append("</td></tr>");
      sb.append("<tr><td>").append(Utility.messageBD(conn, "OPSLicenseType", lang)).append(
          "</td><td>").append(
          Utility.getListValueName("OPSLicenseType", getProperty("lincensetype"), lang)).append(
          "</td></tr>");
      sb.append("<tr><td>").append(Utility.messageBD(conn, "OPSStartDate", lang)).append(
          "</td><td>").append(outputFormat.format(startDate)).append("</td></tr>");

      sb.append("<tr><td>").append(Utility.messageBD(conn, "OPSEndDate", lang)).append("</td><td>")
          .append(
              (getProperty("enddate") == null ? Utility.messageBD(conn, "OPSNoEndDate", lang)
                  : outputFormat.format(endDate))).append("</td></tr>");

      sb.append("<tr><td>").append(Utility.messageBD(conn, "OPSConcurrentUsers", lang)).append(
          "</td><td>").append(
          (getProperty("limitusers") == null || getProperty("limitusers").equals("0")) ? Utility
              .messageBD(conn, "OPSUnlimitedUsers", lang) : getProperty("limitusers")).append(
          "</td></tr>");
      if (getProperty("limituserswarn") != null) {
        sb.append("<tr><td>").append(Utility.messageBD(conn, "OPSConcurrentUsersWarn", lang))
            .append("</td><td>").append(getProperty("limituserswarn")).append("</td></tr>");
      }

      sb.append("<tr><td>").append(Utility.messageBD(conn, "OPSInstanceNo", lang)).append(
          "</td><td>").append(getProperty("instanceno")).append("\n");

      sb.append("<tr><td>").append(Utility.messageBD(conn, "OPSInstancePurpose", lang)).append(
          "</td><td>").append(
          Utility.getListValueName("InstancePurpose", getProperty("purpose"), lang)).append(
          "</td></tr>");

    } else {
      sb.append(Utility.messageBD(conn, "OPSNonActiveInstance", lang));
    }
    return sb.toString();
  }

  public String getPurpose(String lang) {
    return Utility.getListValueName("InstancePurpose", getProperty("purpose"), lang);
  }

  public String getLicenseExplanation(ConnectionProvider conn, String lang) {
    if (getProperty("lincensetype").equals("USR")) {
      return getProperty("limitusers") + " " + Utility.messageBD(conn, "OPSConcurrentUsers", lang);

    } else {
      return Utility.getListValueName("OPSLicenseType", getProperty("lincensetype"), lang);
    }
  }

  public boolean hasExpirationDate() {
    return isOPSInstance() && (getProperty("enddate") != null);
  }

  public String getProperty(String propName) {
    return instanceProperties.getProperty(propName);
  }

  public Long getPendingDays() {
    return pendingTime;
  }

  public boolean hasExpired() {
    return hasExpired;
  }

  public boolean isNotActiveYet() {
    return notActiveYet;
  }

  /**
   * Obtains a list for modules ID the instance is subscribed to and their statuses
   * 
   * @return HashMap<String, CommercialModuleStatus> containing the subscribed modules
   */
  public HashMap<String, CommercialModuleStatus> getSubscribedModules() {
    HashMap<String, CommercialModuleStatus> moduleList = new HashMap<String, CommercialModuleStatus>();

    SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd");

    String allModules = getProperty("modules");
    if (allModules == null || allModules.equals(""))
      return moduleList;
    String modulesInfo[] = allModules.split(",");
    Date now = new Date();
    for (String moduleInfo : modulesInfo) {
      String moduleData[] = moduleInfo.split("\\|");

      Date validFrom = null;
      Date validTo = null;
      try {
        validFrom = sd.parse(moduleData[1]);
        if (moduleData.length > 2) {
          validTo = sd.parse(moduleData[2]);
        }
        if (validFrom.before(now) && (validTo == null || validTo.after(now))) {
          moduleList.put(moduleData[0], CommercialModuleStatus.ACTIVE);
        } else if (validFrom.after(now)) {
          moduleList.put(moduleData[0], CommercialModuleStatus.NO_ACTIVE_YET);
        } else if (validTo != null && validTo.before(now)) {
          moduleList.put(moduleData[0], CommercialModuleStatus.EXPIRED);
        }
      } catch (Exception e) {
        log.error("Error reading module's dates module:" + moduleData[0], e);
      }

    }
    return moduleList;
  }

  /**
   * Returns the status for the commercial module passed as parameter
   * 
   * @param moduleId
   * @return the status for the commercial module passed as parameter
   */
  public CommercialModuleStatus isModuleSubscribed(String moduleId) {
    HashMap<String, CommercialModuleStatus> moduleList = getSubscribedModules();

    if (!moduleList.containsKey(moduleId)) {
      return CommercialModuleStatus.NO_SUBSCRIBED;
    }

    return moduleList.get(moduleId);
  }

}
