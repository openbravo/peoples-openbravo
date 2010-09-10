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
 * The Initial Developer of the Original Code is Openbravo SLU 
 * All portions are Copyright (C) 2008-2010 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.erpCommon.ad_process;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.hibernate.criterion.Expression;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.businessUtility.HeartbeatData;
import org.openbravo.erpCommon.businessUtility.RegistrationData;
import org.openbravo.erpCommon.utility.Alert;
import org.openbravo.erpCommon.utility.HttpsUtils;
import org.openbravo.erpCommon.utility.SystemInfo;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.ad.system.HeartbeatLog;
import org.openbravo.model.ad.system.SystemInformation;
import org.openbravo.model.ad.ui.ProcessRequest;
import org.openbravo.scheduling.Process;
import org.openbravo.scheduling.ProcessBundle;
import org.openbravo.scheduling.ProcessContext;
import org.openbravo.scheduling.ProcessLogger;
import org.openbravo.scheduling.ProcessBundle.Channel;

public class HeartbeatProcess implements Process {

  private static Logger log = Logger.getLogger(HeartbeatProcess.class);

  private static final String HEARTBEAT_URL = "https://butler.openbravo.com:443/heartbeat-server/heartbeat";

  private static final String CERT_ALIAS = "openbravo-butler";

  private static final String ENABLING_BEAT = "E";
  private static final String SCHEDULED_BEAT = "S";
  private static final String DISABLING_BEAT = "D";
  private static final String DECLINING_BEAT = "DEC";
  private static final String DEFERRING_BEAT = "DEF";

  private static final String UNKNOWN_BEAT = "U";
  public static final String HB_PROCESS_ID = "1005800000";
  public static final String STATUS_SCHEDULED = "SCH";
  public static final String STATUS_UNSCHEDULED = "UNS";

  private ProcessContext ctx;

  private ConnectionProvider connection;
  private ProcessLogger logger;
  private Channel channel;

  public void execute(ProcessBundle bundle) throws Exception {

    connection = bundle.getConnection();
    logger = bundle.getLogger();
    channel = bundle.getChannel();

    this.ctx = bundle.getContext();

    SystemInfo.loadId(connection);

    String msg = null;
    if (this.channel == Channel.SCHEDULED && !isHeartbeatActive()) {
      msg = Utility.messageBD(connection, "HB_INACTIVE", ctx.getLanguage());
      logger.logln(msg);
      return;
    }

    if (!HttpsUtils.isInternetAvailable()) {
      msg = Utility.messageBD(connection, "HB_INTERNET_UNAVAILABLE", ctx.getLanguage());
      logger.logln(msg);
      throw new Exception(msg);
    }

    logger.logln("Hearbeat process starting...");
    try {
      String beatType = UNKNOWN_BEAT;

      if (this.channel == Channel.SCHEDULED) {
        beatType = SCHEDULED_BEAT;
      } else {
        final String active = SystemInfoData.isHeartbeatActive(connection);
        if (active.equals("") || active.equals("N")) {
          String action = bundle.getParams().get("action") == null ? "" : ((String) bundle
              .getParams().get("action"));
          if ("DECLINE".equals(action)) {
            beatType = DECLINING_BEAT;
          } else if ("DEFER".equals(action)) {
            beatType = DEFERRING_BEAT;
          } else {
            beatType = ENABLING_BEAT;
          }
        } else {
          beatType = DISABLING_BEAT;
        }
      }

      String queryStr = createQueryStr(beatType);
      String response = sendInfo(queryStr);
      logSystemInfo(beatType);
      List<Alert> updates = parseUpdates(response);
      saveUpdateAlerts(connection, updates);
      updateHeartbeatStatus(beatType);

    } catch (Exception e) {
      logger.logln(e.getMessage());
      log.error(e.getMessage(), e);
      throw new Exception(e.getMessage());
    }
  }

  private void updateHeartbeatStatus(String beatType) throws Exception {

    if (this.channel == Channel.SCHEDULED || DEFERRING_BEAT.equals(beatType)) {
      // Don't update status when is a scheduled beat or deferring beat
      return;
    }

    String active = "";

    if (ENABLING_BEAT.equals(beatType)) {
      active = "Y";
    } else {
      active = "N";
    }

    SystemInfoData.updateHeartbeatActive(connection, active);
  }

  /**
   * @return true if heart beat is enabled, false otherwise
   */
  private boolean isHeartbeatActive() {
    String isheartbeatactive = SystemInfo.get(SystemInfo.Item.ISHEARTBEATACTIVE);
    return (isheartbeatactive != null && !isheartbeatactive.equals("") && !isheartbeatactive
        .equals("N"));
  }

  /**
   * @return the system info as properties
   * @throws ServletException
   */
  private Properties getSystemInfo() {
    logger.logln(logger.messageDb("HB_GATHER", ctx.getLanguage()));
    return SystemInfo.getSystemInfo();
  }

  /**
   * Converts properties into a UTF-8 encoded query string.
   * 
   * @param props
   * @return the UTF-8 encoded query string
   */
  private String createQueryStr(String beatType) {
    logger.logln(logger.messageDb("HB_QUERY", ctx.getLanguage()));

    StringBuilder sb = new StringBuilder();
    if (!(DECLINING_BEAT.equals(beatType) || DEFERRING_BEAT.equals(beatType))) {
      // Complete beat with all available instance info
      try {
        SystemInfo.load(connection);
      } catch (ServletException e1) {
        log.error("Error reading system info", e1);
      }
    }

    Properties props = null;
    props = getSystemInfo();
    if (props == null) {
      return null;
    }

    Enumeration<?> e = props.propertyNames();
    while (e.hasMoreElements()) {
      String elem = (String) e.nextElement();
      String value = props.getProperty(elem);
      sb.append(elem + "=" + (value == null ? "" : value) + "&");
    }
    sb.append("beatType=" + beatType);

    return HttpsUtils.encode(sb.toString(), "UTF-8");
  }

  /**
   * Sends a query string to the heartbeat server. Returns the https response as a string.
   * 
   * @param queryStr
   * @return the result of sending the info
   * @throws IOException
   * @throws GeneralSecurityException
   */
  private String sendInfo(String queryStr) throws GeneralSecurityException, IOException {
    logger.logln(logger.messageDb("HB_SEND", ctx.getLanguage()));
    URL url = null;
    try {
      url = new URL(HEARTBEAT_URL);
    } catch (MalformedURLException e) { // Won't happen
      log.error(e.getMessage(), e);
    }
    log.info("Heartbeat sending: '" + queryStr + "'");
    logger.logln(queryStr);
    return HttpsUtils.sendSecure(url, queryStr, CERT_ALIAS, "changeit");
  }

  private void logSystemInfo(String beatType) {
    logger.logln(logger.messageDb("HB_LOG", ctx.getLanguage()));

    try {
      Properties systemInfo = SystemInfo.getSystemInfo();
      OBContext.setAdminMode();
      HeartbeatLog hbLog = OBProvider.getInstance().get(HeartbeatLog.class);
      hbLog.setSystemIdentifier(systemInfo.getProperty("systemIdentifier"));
      hbLog.setDatabaseIdentifier(systemInfo.getProperty(SystemInfo.Item.DB_IDENTIFIER.getLabel()));
      hbLog.setMacIdentifier(systemInfo.getProperty(SystemInfo.Item.MAC_IDENTIFIER.getLabel()));
      hbLog.setBeatType(beatType);

      if (!(DECLINING_BEAT.equals(beatType) || DEFERRING_BEAT.equals(beatType))) {
        hbLog.setServletContainer(systemInfo.getProperty("servletContainer"));
        hbLog.setServletContainerVersion(systemInfo.getProperty("servletContainerVersion"));
        hbLog.setAntVersion(systemInfo.getProperty("antVersion"));
        hbLog.setOpenbravoVersion(systemInfo.getProperty("obVersion"));
        hbLog.setOpenbravoInstallMode(systemInfo.getProperty("obInstallMode"));
        hbLog.setCodeRevision(systemInfo.getProperty("codeRevision"));
        hbLog.setWebServer(systemInfo.getProperty("webserver"));
        hbLog.setWebServerVersion(systemInfo.getProperty("webserverVersion"));
        hbLog.setOperatingSystem(systemInfo.getProperty("os"));
        hbLog.setOperatingSystemVersion(systemInfo.getProperty("osVersion"));
        hbLog.setDatabase(systemInfo.getProperty("db"));
        hbLog.setDatabaseVersion(systemInfo.getProperty("dbVersion"));
        hbLog.setJavaVersion(systemInfo.getProperty("javaVersion"));
        try {
          hbLog.setActivityRate(new BigDecimal(systemInfo.getProperty("activityRate")));
        } catch (NumberFormatException e) {
          log.warn("Incorrect activity rate: " + systemInfo.getProperty("activityRate"));
        }
        try {
          hbLog.setComplexityRate(new BigDecimal(systemInfo.getProperty("complexityRate")));
        } catch (NumberFormatException e) {
          log.warn("Incorrect complexity rate: " + systemInfo.getProperty("complexityRate"));
        }
        hbLog.setActive("Y".equals(systemInfo.getProperty("isheartbeatactive")));
        hbLog.setProxyRequired("Y".equals(systemInfo.getProperty("isproxyrequired")));
        hbLog.setProxyServer(systemInfo.getProperty("proxyServer"));
        try {
          hbLog.setProxyPort(Long.parseLong(systemInfo.getProperty("proxyPort")));
        } catch (NumberFormatException e) {
          log.warn("Incorrect port: " + systemInfo.getProperty("proxyPort"));
        }
        try {
          hbLog.setNumberOfRegisteredUsers(Long.parseLong(systemInfo
              .getProperty("numRegisteredUsers")));
        } catch (NumberFormatException e) {
          log.warn("Incorrect number of registered users: "
              + systemInfo.getProperty("numRegisteredUsers"));
        }
        hbLog.setInstalledModules(systemInfo.getProperty(SystemInfo.Item.MODULES.getLabel()));
        hbLog.setActivationKeyIdentifier(systemInfo.getProperty(SystemInfo.Item.OBPS_INSTANCE
            .getLabel()));
        try {
          hbLog.setFirstLogin(SystemInfo.parseDate(systemInfo
              .getProperty(SystemInfo.Item.FIRT_LOGIN.getLabel())));
        } catch (ParseException e) {
          log.warn("Incorrect date of first login: "
              + systemInfo.getProperty(systemInfo
                  .getProperty(SystemInfo.Item.FIRT_LOGIN.getLabel())));
        }
        try {
          hbLog.setLastLogin(SystemInfo.parseDate(systemInfo.getProperty(SystemInfo.Item.LAST_LOGIN
              .getLabel())));
        } catch (ParseException e) {
          log.warn("Incorrect date of last login: "
              + systemInfo.getProperty(systemInfo
                  .getProperty(SystemInfo.Item.LAST_LOGIN.getLabel())));
        }
        try {
          hbLog.setTotalLogins(Long.parseLong(systemInfo.getProperty(SystemInfo.Item.TOTAL_LOGINS
              .getLabel())));
        } catch (NumberFormatException e) {
          log.warn("Incorrect number of total logins: "
              + systemInfo.getProperty(SystemInfo.Item.TOTAL_LOGINS.getLabel()));
        }
      }
      OBDal.getInstance().save(hbLog);
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  /**
   * @param response
   * @return the list of updates
   */
  private List<Alert> parseUpdates(String response) {
    logger.logln(logger.messageDb("HB_UPDATES", ctx.getLanguage()));
    if (response == null)
      return null;
    String[] updates = response.split("::");
    List<Alert> alerts = new ArrayList<Alert>();
    Pattern pattern = Pattern.compile("\\[recordId=\\d+\\]");
    for (String update : updates) {
      String recordId = null;
      String description = update;
      Matcher matcher = pattern.matcher(update);
      if (matcher.find()) {
        String s = matcher.group();
        recordId = s.substring(s.indexOf('=') + 1, s.indexOf(']'));
        description = update.substring(update.indexOf(']') + 1);
      }
      Alert alert = new Alert(1005400000, recordId);
      alert.setDescription(description);
      alerts.add(alert);
    }
    return alerts;
  }

  /**
   * @param conn
   * @param updates
   */
  private void saveUpdateAlerts(ConnectionProvider conn, List<Alert> updates) {
    if (updates == null) {
      logger.logln("No Updates found...");
      return;
    }
    // info("  ");
    for (Alert update : updates) {
      update.save(conn);
    }
  }

  public enum HeartBeatOrRegistration {
    HeartBeat, Registration, None;
  }

  public static HeartBeatOrRegistration showHeartBeatOrRegistration(VariablesSecureApp vars,
      ConnectionProvider connectionProvider) throws ServletException {

    if (vars.getRole() != null && vars.getRole().equals("0")) {
      // Check if the heartbeat popup needs to be displayed
      final HeartbeatData[] hbData = HeartbeatData.selectSystemProperties(connectionProvider);
      if (hbData.length > 0) {
        final String isheartbeatactive = hbData[0].isheartbeatactive;
        final String postponeDate = hbData[0].postponeDate;
        if (isheartbeatactive == null || isheartbeatactive.equals("")) {
          if (postponeDate == null || postponeDate.equals("")) {
            return HeartBeatOrRegistration.HeartBeat;
          } else {
            Date date = null;
            try {
              date = new SimpleDateFormat(vars.getJavaDateFormat()).parse(postponeDate);
              if (date.before(new Date())) {
                return HeartBeatOrRegistration.HeartBeat;
              }
            } catch (final ParseException e) {
              e.printStackTrace();
            }
          }
        }
      }

      // If the heartbeat doesn't need to be displayed, check the
      // registration popup
      final RegistrationData[] rData = RegistrationData.select(connectionProvider);
      if (rData.length > 0) {
        final String isregistrationactive = rData[0].isregistrationactive;
        final String rPostponeDate = rData[0].postponeDate;
        if (isregistrationactive == null || isregistrationactive.equals("")) {
          if (rPostponeDate == null || rPostponeDate.equals("")) {
            return HeartBeatOrRegistration.Registration;
          } else {
            Date date = null;
            try {
              date = new SimpleDateFormat(vars.getJavaDateFormat()).parse(rPostponeDate);
              if (date.before(new Date())) {
                return HeartBeatOrRegistration.Registration;
              }
            } catch (final ParseException e) {
              e.printStackTrace();
            }
          }
        }
      }
    }
    return HeartBeatOrRegistration.None;
  }

  public static boolean isHeartbeatEnabled() {
    SystemInformation sys = OBDal.getInstance().get(SystemInformation.class, "0");

    final org.openbravo.model.ad.ui.Process HBProcess = OBDal.getInstance().get(
        org.openbravo.model.ad.ui.Process.class, HB_PROCESS_ID);

    final OBCriteria<ProcessRequest> prCriteria = OBDal.getInstance().createCriteria(
        ProcessRequest.class);
    prCriteria.add(Expression.and(Expression.eq(ProcessRequest.PROPERTY_PROCESS, HBProcess),
        Expression.or(Expression.eq(ProcessRequest.PROPERTY_STATUS,
            org.openbravo.scheduling.Process.SCHEDULED), Expression.eq(
            ProcessRequest.PROPERTY_STATUS, org.openbravo.scheduling.Process.MISFIRED))));
    final List<ProcessRequest> prRequestList = prCriteria.list();

    if (prRequestList.size() == 0) { // Resetting state to disabled
      sys.setEnableHeartbeat(false);
      OBDal.getInstance().save(sys);
      OBDal.getInstance().flush();
    }

    // Must exist a scheduled process request for HB and must be enable at SystemInfo level
    return prRequestList.size() > 0 && sys.isEnableHeartbeat();
  }
}
