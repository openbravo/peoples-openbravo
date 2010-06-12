package org.openbravo.erpCommon.ad_process;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.hibernate.criterion.Expression;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.utility.Alert;
import org.openbravo.erpCommon.utility.HttpsUtils;
import org.openbravo.erpCommon.utility.SequenceIdData;
import org.openbravo.erpCommon.utility.SystemInfo;
import org.openbravo.erpCommon.utility.Utility;
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

    SystemInfo.load(bundle.getConnection());

    String msg = null;
    if (this.channel == Channel.SCHEDULED && !isHeartbeatActive()) {
      msg = Utility.messageBD(connection, "HB_INACTIVE", ctx.getLanguage());
      logger.logln(msg);
      return;
    }

    if (!isInternetAvailable(connection)) {
      msg = Utility.messageBD(connection, "HB_INTERNET_UNAVAILABLE", ctx.getLanguage());
      logger.logln(msg);
      throw new Exception(msg);
    }

    logger.logln("Hearbeat process starting...");
    try {
      Properties systemInfo = getSystemInfo(connection);

      String beatType = UNKNOWN_BEAT;

      if (this.channel == Channel.SCHEDULED) {
        beatType = SCHEDULED_BEAT;
      } else {
        final String active = SystemInfoData.isHeartbeatActive(connection);
        if (active.equals("") || active.equals("N")) {
          beatType = ENABLING_BEAT;
        } else {
          beatType = DISABLING_BEAT;
        }
      }

      String queryStr = createQueryStr(systemInfo, beatType);
      String response = sendInfo(queryStr);
      logSystemInfo(connection, systemInfo, beatType);
      List<Alert> updates = parseUpdates(response);
      saveUpdateAlerts(connection, updates);
      updateHeartbeatStatus();

    } catch (Exception e) {
      logger.logln(e.getMessage());
      log.error(e.getMessage(), e);
      throw new Exception(e.getMessage());
    }
  }

  private void updateHeartbeatStatus() throws Exception {

    if (this.channel == Channel.SCHEDULED) {
      // Don't update status when is a scheduled beat
      return;
    }

    String active = SystemInfoData.isHeartbeatActive(connection);
    if (active.equals("") || active.equals("N")) {
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
   * @param connection
   * @return true if there is a connection to the internet, false otherwise
   * @throws ServletException
   */
  private static boolean isInternetAvailable(ConnectionProvider connection) throws ServletException {
    log.info("Checking for internet connection...");
    String isproxyrequired = SystemInfo.get(SystemInfo.Item.ISPROXYREQUIRED);
    if (isproxyrequired != null && isproxyrequired.equals("Y")) {
      String proxyServer = HeartbeatProcessData.selectProxyServer(connection);
      String proxyPort = HeartbeatProcessData.selectProxyPort(connection);
      int port = 80;
      try {
        port = Integer.parseInt(proxyPort);
      } catch (NumberFormatException e) {
      }
      return HttpsUtils.isInternetAvailable(proxyServer, port);
    } else {
      return HttpsUtils.isInternetAvailable();
    }
  }

  /**
   * @param con
   * @return the system info as properties
   * @throws ServletException
   */
  private Properties getSystemInfo(ConnectionProvider con) throws ServletException {
    logger.logln(logger.messageDb("HB_GATHER", ctx.getLanguage()));
    return SystemInfo.getSystemInfo();
  }

  /**
   * Converts properties into a UTF-8 encoded query string.
   * 
   * @param props
   * @return the UTF-8 encoded query string
   */
  private String createQueryStr(Properties props, String beatType) {
    logger.logln(logger.messageDb("HB_QUERY", ctx.getLanguage()));
    if (props == null)
      return null;
    StringBuilder sb = new StringBuilder();
    Enumeration e = props.propertyNames();
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

  private void logSystemInfo(ConnectionProvider conn, Properties systemInfo, String beatType)
      throws ServletException {
    logger.logln(logger.messageDb("HB_LOG", ctx.getLanguage()));
    String id = SequenceIdData.getUUID();
    String systemIdentifier = systemInfo.getProperty("systemIdentifier");
    String servletContainer = systemInfo.getProperty("servletContainer");
    String servletContainerVersion = systemInfo.getProperty("servletContainerVersion");
    String antVersion = systemInfo.getProperty("antVersion");
    String obVersion = systemInfo.getProperty("obVersion");
    String obInstallMode = systemInfo.getProperty("obInstallMode");
    String codeRevision = systemInfo.getProperty("codeRevision");
    String webserver = systemInfo.getProperty("webserver");
    String webserverVersion = systemInfo.getProperty("webserverVersion");
    String os = systemInfo.getProperty("os");
    String osVersion = systemInfo.getProperty("osVersion");
    String db = systemInfo.getProperty("db");
    String dbVersion = systemInfo.getProperty("dbVersion");
    String javaVersion = systemInfo.getProperty("javaVersion");
    String activityRate = systemInfo.getProperty("activityRate");
    String complexityRate = systemInfo.getProperty("complexityRate");
    String isHeartbeatActive = systemInfo.getProperty("isheartbeatactive");
    String isProxyRequired = systemInfo.getProperty("isproxyrequired");
    String proxyServer = systemInfo.getProperty("proxyServer");
    String proxyPort = systemInfo.getProperty("proxyPort");
    String numRegisteredUsers = systemInfo.getProperty("numRegisteredUsers");

    HeartbeatProcessData.insertHeartbeatLog(conn, id, "0", "0", systemIdentifier,
        isHeartbeatActive, isProxyRequired, proxyServer, proxyPort, activityRate, complexityRate,
        os, osVersion, db, dbVersion, servletContainer, servletContainerVersion, webserver,
        webserverVersion, obVersion, obInstallMode, codeRevision, numRegisteredUsers, javaVersion,
        antVersion, beatType);
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
