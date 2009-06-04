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

package org.openbravo.erpCommon.ops;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Properties;
import java.util.zip.CRC32;

import javax.crypto.Cipher;

import org.apache.log4j.Appender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.hibernate.criterion.Expression;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.ad.access.Session;

public class ActivationKey {

  private boolean isActive = false;
  private boolean hasActivationKey = false;
  private String errorMessage = "";
  private Properties instanceProperties;
  private static final Logger log = Logger.getLogger(ActivationKey.class);
  private String strPublicKey;
  private static boolean opsLog = false;
  private static String opsLogId;

  public enum LicenseRestriction {
    NO_RESTRICTION, OPS_INSTANCE_NOT_ACTIVE, NUMBER_OF_SOFT_USERS_REACHED, NUMBER_OF_CONCURRENT_USERS_REACHED
  }

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
      Cipher cipher = Cipher.getInstance("RSA");

      ByteArrayInputStream bis = new ByteArrayInputStream(org.apache.commons.codec.binary.Base64
          .decodeBase64(activationKey.getBytes()));
      ByteArrayOutputStream bos = new ByteArrayOutputStream();

      // Encryptation only accepts 128B size, it must be chuncked
      final byte[] buf = new byte[128];
      while ((bis.read(buf)) > 0) {
        cipher.init(Cipher.DECRYPT_MODE, pk);
        bos.write(cipher.doFinal(buf));
      }
      byte[] props = bos.toByteArray();

      ByteArrayInputStream isProps = new ByteArrayInputStream(props);
      instanceProperties = new Properties();

      instanceProperties.load(isProps);
    } catch (Exception e) {
      isActive = false;
      errorMessage = "@NotAValidKey@";
      e.printStackTrace();
      setLogger();
      return;
    }

    // Check for dates to know if the instance is active
    SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
    Date startDate = null;
    Date endDate = null;
    try {
      startDate = sd.parse(getProperty("startdate"));

      if (getProperty("enddate") != null)
        endDate = sd.parse(getProperty("enddate"));

    } catch (ParseException e) {
      errorMessage = "@ErrorReadingDates@";
      isActive = false;
      log.error(e);
      setLogger();
      return;
    }

    Date now = new Date();
    if (startDate == null || now.before(startDate)) {
      isActive = false;
      errorMessage = "@OPSNotActiveTill@ " + startDate;
      setLogger();
      return;
    }
    if (endDate != null && now.after(endDate)) {
      isActive = false;
      errorMessage = "@OPSActivationExpired@ " + endDate;
      setLogger();
      return;
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
      boolean adminMode = OBContext.getOBContext().isInAdministratorMode();
      OBContext.getOBContext().setInAdministratorMode(true);

      OBCriteria<Session> obCriteria = OBDal.getInstance().createCriteria(Session.class);
      obCriteria.add(Expression.eq(Session.PROPERTY_SESSIONACTIVE, true));
      int currentSessions = obCriteria.list().size();
      OBContext.getOBContext().setInAdministratorMode(adminMode);

      Long softUsers = null;
      if (getProperty("limituserswarn") != null) {
        softUsers = new Long(getProperty("limituserswarn"));
      }

      Long maxUsers = new Long(getProperty("limitusers"));

      if (currentSessions >= maxUsers) {
        return LicenseRestriction.NUMBER_OF_CONCURRENT_USERS_REACHED;
      }

      if (softUsers != null && currentSessions >= softUsers) {
        return LicenseRestriction.NUMBER_OF_SOFT_USERS_REACHED;
      }
    }
    return LicenseRestriction.NO_RESTRICTION;
  }

  public String toString(ConnectionProvider conn, String lang) {
    StringBuffer sb = new StringBuffer();
    if (instanceProperties != null) {
      sb.append(Utility.messageBD(conn, "OPSCustomer", lang)).append(": ").append(
          getProperty("customer")).append("\n");
      sb.append(Utility.messageBD(conn, "OPSInstanceNo", lang)).append(": ").append(
          getProperty("instanceno")).append("\n");
      sb.append(Utility.messageBD(conn, "OPSLicenseType", lang)).append(": ").append(
          Utility.getListValueName("OPSLicenseType", getProperty("lincensetype"), lang)).append(
          "\n");
      sb.append(Utility.messageBD(conn, "OPSInstancePurpose", lang)).append(": ").append(
          Utility.getListValueName("InstancePurpose", getProperty("purpose"), lang)).append("\n");
      sb.append(Utility.messageBD(conn, "OPSStartDate", lang)).append(": ").append(
          getProperty("startdate")).append("\n");
      if (getProperty("enddate") != null) {
        sb.append(Utility.messageBD(conn, "OPSEndDate", lang)).append(": ").append(
            getProperty("enddate")).append("\n");
      }
      sb.append(Utility.messageBD(conn, "OPSConcurrentUsers", lang)).append(": ").append(
          getProperty("limitusers")).append("\n");
      if (getProperty("limituserswarn") != null) {
        sb.append(Utility.messageBD(conn, "OPSConcurrentUsersWarn", lang)).append(": ").append(
            getProperty("limituserswarn")).append("\n");
      }
    } else {
      sb.append(Utility.messageBD(conn, "OPSNonActiveInstance", lang));
    }
    return sb.toString();
  }

  public String getProperty(String propName) {
    return instanceProperties.getProperty(propName);
  }

}
