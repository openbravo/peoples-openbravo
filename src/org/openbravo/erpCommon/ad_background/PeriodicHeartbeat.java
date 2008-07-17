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
 * All portions are Copyright (C) 2008 Openbravo SL 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
*/

package org.openbravo.erpCommon.ad_background;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.utility.Alert;
import org.openbravo.erpCommon.utility.HttpsUtils;
import org.openbravo.erpCommon.utility.SystemInfo;

public class PeriodicHeartbeat implements BackgroundProcess {
  
  static Logger log4j = Logger.getLogger(PeriodicHeartbeat.class);
  
  public static final long PAUSE_TIME = 86400; //  1 day.
  
  public static final String PROTOCOL = "https";
  public static final String HOST = "butler.openbravo.com";
  public static final int PORT = 443;
  public static final String PATH = "/heartbeat-server/heartbeat";
  public static final String CERT_ALIAS = "openbravo-butler";
  
  public void processPL(PeriodicBackground periodicBG, boolean directProcess) throws Exception {
    
    try {
      String isheartbeatactive = SystemInfo.get(SystemInfo.Item.ISHEARTBEATACTIVE);
      if (isheartbeatactive != null && isheartbeatactive.equals("Y") &&
          isInternetAvailable(periodicBG.conn)) {
        periodicBG.addLog("Starting Heartbeat Background Process...");
        String lastBeatStr = PeriodicHeartbeatData.selectLastHeartbeat(periodicBG.conn);
        periodicBG.addLog("Last heartbeat " + lastBeatStr);
        if (lastBeatStr == null || lastBeatStr.equals("")) {
          beat(periodicBG);
        } else {
          Date date = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss").parse(lastBeatStr);
          Calendar cal = Calendar.getInstance();
          cal.setTime(date);
          cal.add(Calendar.DAY_OF_MONTH, 3);
          
          if (cal.getTime().before(new Date())) {
            periodicBG.addLog("Gathering and sending heartbeat information...");
            beat(periodicBG);
          } else {
            periodicBG.addLog("Next scheduled heartbeat on: " + cal.getTime());
          }
        }
      }
    } catch (Exception e) {
      log4j.error(e.getMessage(), e);
    }
    periodicBG.doPause(PAUSE_TIME);
  }
  
  public void beat(PeriodicBackground periodicBG) throws Exception {
    beat(periodicBG.conn);
  }
  
  public void beat(ConnectionProvider conn) throws ServletException, 
      IOException, GeneralSecurityException {
    Properties systemInfo = getSystemInfo(conn);
    String queryStr = createQueryStr(systemInfo);
    String response = sendInfo(queryStr);
    logSystemInfo(conn, systemInfo);
    List<Alert> updates = parseUpdates(response);
    saveUpdateAlerts(conn, updates);
  }
  
  /**
   * @param conn
   * @return
   * @throws ServletException
   */
  public static boolean isInternetAvailable(ConnectionProvider conn) 
      throws ServletException {
    log4j.info("Checking for internet connection...");
    String isproxyrequired = SystemInfo.get(SystemInfo.Item.ISPROXYREQUIRED);
    if (isproxyrequired != null && isproxyrequired.equals("Y")) {
      String proxyServer = PeriodicHeartbeatData.selectProxyServer(conn);
      String proxyPort = PeriodicHeartbeatData.selectProxyPort(conn);
      int port = 80;
      try {
        port = Integer.parseInt(proxyPort);
      } catch (NumberFormatException e) {}
      return HttpsUtils.isInternetAvailable(proxyServer, port);
    } else {
      return HttpsUtils.isInternetAvailable();
    }
  }
  
  /**
   * @param conn
   * @return
   * @throws ServletException
   */
  public Properties getSystemInfo(ConnectionProvider conn) throws ServletException {
    SystemInfo.load(conn);
    return SystemInfo.getSystemInfo();
  }
  
  /**
   * Converts properties into a UTF-8 encoded query string.
   * @param props
   * @return
   */
  public String createQueryStr(Properties props) {
    log4j.info("Generating query string from system information...");
    if (props == null)
      return null;
    StringBuilder sb = new StringBuilder();
    Enumeration e = props.propertyNames();
    while (e.hasMoreElements()) {
      String elem = (String) e.nextElement();
      String value = props.getProperty(elem);
      sb.append(elem + "=" + (value == null ? "" : value) + "&");
    }
    
    return HttpsUtils.encode(sb.toString(), "UTF-8");
  }
  
  /**
   * Sends a query string to the heartbeat server.
   * Returns the https response as a string.
   * @param queryStr
   * @return
   * @throws IOException 
   * @throws GeneralSecurityException 
   */
  public String sendInfo(String queryStr) throws GeneralSecurityException, 
      IOException {
    log4j.info("Sending heartbeat info to " + HOST);
    URL url = null;
    try {
      url = new URL(PROTOCOL, HOST, PORT, PATH);
    } catch (MalformedURLException e) { // Won't happen
      log4j.error(e.getMessage(), e);
    }
    log4j.info("Heartbeat sending: '" + queryStr + "'");
    return HttpsUtils.sendSecure(url, queryStr, CERT_ALIAS, "changeit"); 
  }
  
  public void logSystemInfo(ConnectionProvider conn, Properties systemInfo) throws ServletException {
    log4j.info("Logging system information to AD_HEARTBEAT_LOG...");
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
    
    PeriodicHeartbeatData.insertHeartbeatLog(conn, "0", "0", systemIdentifier, isHeartbeatActive, isProxyRequired, proxyServer, proxyPort, activityRate, complexityRate, os, osVersion, db, dbVersion, servletContainer, servletContainerVersion, webserver, webserverVersion, obVersion, obInstallMode, codeRevision, numRegisteredUsers, javaVersion, antVersion);
  }
  
  /**
   * @param response
   * @return
   */
  public List<Alert> parseUpdates(String response) {
    log4j.info("Generating update alerts from heartbeat response: " + response);
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
  public void saveUpdateAlerts(ConnectionProvider conn, List<Alert> updates) {
    if (updates == null)
      return;
    log4j.info("Saving updates...");
    for (Alert update : updates) {
      update.save(conn);
    }
  }
  
}
