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
import java.net.HttpURLConnection;
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
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.utility.Alert;
import org.openbravo.erpCommon.utility.HttpsUtils;

public class PeriodicHeartbeat implements BackgroundProcess {
  
  static Logger log4j = Logger.getLogger(PeriodicHeartbeat.class);
  
  public static final long PAUSE_TIME = 86400; //  1 day.
  
  public static final String PROTOCOL = "https";
  public static final String HOST = "butler.openbravo.com";
  public static final int PORT = 443;
  public static final String PATH = "/heartbeat-server/heartbeat";
  
  public void processPL(PeriodicBackground periodicBG, boolean directProcess) throws Exception {
    
    try {
      String isheartbeatactive = PeriodicHeartbeatData.selectIsheartbeatactive(periodicBG.conn);
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
  
  public static boolean isInternetAvailable(ConnectionProvider conn) 
      throws ServletException {
    log4j.info("Checking for internet connection...");
    String isproxyrequired = PeriodicHeartbeatData.selectIsproxyrequired(conn);
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
   * Collects system information from the database and system properties
   * and returns the key value pairs in a Properties object.
   * @param conn
   * @return
   * @throws ServletException
   */
  public Properties getSystemInfo(ConnectionProvider conn) 
      throws ServletException {
    log4j.info("Gathering system information...");
    Properties systemInfo = new Properties();
    
    // Get required data from AD_SYSTEM_INFO
    PeriodicHeartbeatData[] data = PeriodicHeartbeatData.selectSystemProperties(conn);
    if (data.length > 0) {
      
      // Check to see if system has a system identifier. If not, set one.
      String systemIdentifier = data[0].systemIdentifier;
      if (systemIdentifier == null || systemIdentifier.equals("")) {
        systemIdentifier = UUID.randomUUID().toString();
        PeriodicHeartbeatData.updateSystemIdentifier(conn, systemIdentifier);
      }
      systemInfo.put("systemIdentifier", systemIdentifier);
      
      String db = conn.getRDBMS();
      if (db != null && db.equals("ORACLE")) {
        String dbVersion = PeriodicHeartbeatData.selectOracleVersion(conn);
        systemInfo.put("dbVersion", getVersion(dbVersion));
      } else if (db != null && db.equals("POSTGRE")) {
        systemInfo.put("dbVersion", PeriodicHeartbeatData.selectPostregresVersion(conn));
      }
      systemInfo.put("db", db);
      
      String webserver = data[0].webserver;
      if (webserver == null || webserver.equals("")) {
        try {
          URL url = new URL("http://openbravo.com");
          HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
          httpConn.connect();
          if (httpConn.getResponseCode() == 200) {
            String server = httpConn.getHeaderField("Server");
            
            webserver = server.split("/")[0];
            String webserverVersion = server.split("/")[1];
            data[0].webserver = webserver;
            data[0].webserverVersion = webserverVersion;
            PeriodicHeartbeatData.updateWebserver(conn, webserver, webserverVersion);
          } else {
            throw new Exception();
          }
        } catch (Exception e) {
          log4j.error("Unable to get Web Server and version");
        }
        
      }
      
      systemInfo.put("servletContainer", data[0].servletContainer);
      systemInfo.put("servletContainerVersion", data[0].servletContainerVersion);
      systemInfo.put("antVersion", getVersion(data[0].antVersion));
      systemInfo.put("obVersion", data[0].obVersion);
      systemInfo.put("obInstallMode", data[0].obInstallmode);
      systemInfo.put("codeRevision", data[0].codeRevision);
      systemInfo.put("webserver", data[0].webserver);
      systemInfo.put("webserverVersion", data[0].webserverVersion);
      systemInfo.put("numRegisteredUsers", PeriodicHeartbeatData.selectNumRegisteredUsers(conn));
      
      
      systemInfo.put("isheartbeatactive", data[0].isheartbeatactive);
      systemInfo.put("isproxyrequired", data[0].isproxyrequired);
      systemInfo.put("proxyServer", data[0].proxyServer);
      systemInfo.put("proxyPort", data[0].proxyPort);
      
      /* activityRate mapping
       * Range:  0..............1.- Inactive
         1-100..........2.- Low
         101-500........3.- Medium
         500-1000.......4.- High
         1001 or more...5.- Very High
      */
      int activityRate = Integer.valueOf(PeriodicHeartbeatData.selectActivityRate(conn));
      if (activityRate == 0) { systemInfo.put("activityRate", "1"); }
      else if (activityRate > 0 && activityRate < 101) { systemInfo.put("activityRate", "2"); }
      else if (activityRate > 100 && activityRate < 501) { systemInfo.put("activityRate", "3"); }
      else if (activityRate > 500 && activityRate < 1001) { systemInfo.put("activityRate", "4"); }
      else if (activityRate > 1001) { systemInfo.put("activityRate", "5"); }
      
      /* complexityRate mapping
       * Range:
         0-2 ............1.- Low
         3-6.............2.- Medium
         7 or more.......3.- High
       */
      int complexityRate = Integer.valueOf(PeriodicHeartbeatData.selectComplexityRate(conn));
      if (complexityRate > 0 && complexityRate < 3) { systemInfo.put("complexityRate", "1"); }
      else if (complexityRate > 2 && complexityRate < 7) { systemInfo.put("complexityRate", "2"); }
      else if (complexityRate > 7) { systemInfo.put("complexityRate", "3"); }
    }
    
    // Get required data from System properties
    Properties props = System.getProperties();
    if     (props != null) {
      systemInfo.put("os", props.getProperty("os.name"));
      systemInfo.put("osVersion", props.getProperty("os.version"));
      systemInfo.put("javaVersion", props.getProperty("java.version"));
    }
    
    return systemInfo;
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
    return HttpsUtils.sendSecure(url, queryStr, "changeit"); 
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
    log4j.info("Generating update alerts from heartbeat response...");
    if (response == null)
      return null;
    String[] updates = response.split("::");
    List<Alert> alerts = new ArrayList<Alert>();
    for (String update : updates) {
      Alert alert = new Alert(1005400000);
      alert.setDescription(update);
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
  
  /**
   * Returns the string representation of a numerical version from a
   * longer string. For example, given the string:
   * 'Apache Ant version 1.7.0 compiled on August 29 2007' getVersion() will
   * return '1.7.0'
   * @param str
   * @return
   */
  public String getVersion(String str) {
    String version = "";
    if (str == null)
      return "";
    Pattern pattern = Pattern.compile("((\\d+\\.)+)\\d+");
    Matcher matcher = pattern.matcher(str);
    if (matcher.find()) {
      version = matcher.group();
    }
    return version;
  }
}
