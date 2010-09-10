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

package org.openbravo.erpCommon.utility;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.CRC32;

import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.criterion.Order;
import org.json.JSONArray;
import org.openbravo.base.session.OBPropertiesProvider;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.data.FieldProvider;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.obps.ActivationKey;
import org.openbravo.model.ad.module.Module;

public class SystemInfo {

  private static final Logger log4j = Logger.getLogger(SystemInfo.class);
  private static Map<Item, String> systemInfo;
  private static Date firstLogin;
  private static Date lastLogin;
  private static Long numberOfLogins;
  private static SimpleDateFormat sd;

  static {
    systemInfo = new HashMap<Item, String>();
    sd = new SimpleDateFormat("dd-MM-yyyy");
  }

  /**
   * Loads system information but ID
   * 
   * @param conn
   * @throws ServletException
   */
  public static void load(ConnectionProvider conn) throws ServletException {
    loadLoginInfo();
    for (Item i : Item.values()) {
      if (!i.isIdInfo()) {
        load(i, conn);
      }
    }
  }

  /**
   * Loads ID information
   * 
   * @param conn
   * @throws ServletException
   */
  public static void loadId(ConnectionProvider conn) throws ServletException {
    for (Item i : Item.values()) {
      if (i.isIdInfo()) {
        load(i, conn);
      }
    }
  }

  private static void load(Item i, ConnectionProvider conn) throws ServletException {

    switch (i) {
    case SYSTEM_IDENTIFIER:
      systemInfo.put(i, getSystemIdentifier(conn));
      break;
    case MAC_IDENTIFIER:
      systemInfo.put(i, getMacAddress());
      break;
    case DB_IDENTIFIER:
      systemInfo.put(i, getDBIdentifier(conn));
      break;
    case DATABASE:
      systemInfo.put(i, conn.getRDBMS());
      break;
    case DATABASE_VERSION:
      systemInfo.put(i, getDatabaseVersion(conn));
      break;
    case WEBSERVER:
      systemInfo.put(i, getWebserver(conn)[0]);
      break;
    case WEBSERVER_VERSION:
      systemInfo.put(i, getWebserver(conn)[1]);
      break;
    case SERVLET_CONTAINER:
      systemInfo.put(i, SystemInfoData.selectServletContainer(conn));
      break;
    case SERVLET_CONTAINER_VERSION:
      systemInfo.put(i, SystemInfoData.selectServletContainerVersion(conn));
      break;
    case ANT_VERSION:
      systemInfo.put(i, getVersion(SystemInfoData.selectAntVersion(conn)));
      break;
    case OB_VERSION:
      OBVersion version = OBVersion.getInstance();
      systemInfo.put(i, version.getVersionNumber() + version.getMP());
      break;
    case OB_INSTALL_MODE:
      systemInfo.put(i, SystemInfoData.selectObInstallMode(conn));
      break;
    case CODE_REVISION:
      systemInfo.put(i, SystemInfoData.selectCodeRevision(conn));
      break;
    case NUM_REGISTERED_USERS:
      systemInfo.put(i, SystemInfoData.selectNumRegisteredUsers(conn));
      break;
    case ISHEARTBEATACTIVE:
      systemInfo.put(i, SystemInfoData.selectIsheartbeatactive(conn));
      break;
    case ISPROXYREQUIRED:
      systemInfo.put(i, SystemInfoData.selectIsproxyrequired(conn));
      break;
    case PROXY_SERVER:
      systemInfo.put(i, SystemInfoData.selectProxyServer(conn));
      break;
    case PROXY_PORT:
      systemInfo.put(i, SystemInfoData.selectProxyPort(conn));
      break;
    case ACTIVITY_RATE:
      systemInfo.put(i, getActivityRate(conn));
      break;
    case COMPLEXITY_RATE:
      systemInfo.put(i, getComplexityRate(conn));
      break;
    case OPERATING_SYSTEM:
      systemInfo.put(i, System.getProperty("os.name"));
      break;
    case OPERATING_SYSTEM_VERSION:
      systemInfo.put(i, System.getProperty("os.version"));
      break;
    case JAVA_VERSION:
      systemInfo.put(i, System.getProperty("java.version"));
      break;
    case MODULES:
      systemInfo.put(i, getModules());
      break;
    case OBPS_INSTANCE:
      systemInfo.put(i, getOBPSInstance());
      break;
    case FIRT_LOGIN:
      systemInfo.put(i, sd.format(firstLogin));
      break;
    case LAST_LOGIN:
      systemInfo.put(i, sd.format(lastLogin));
      break;
    case TOTAL_LOGINS:
      systemInfo.put(i, numberOfLogins.toString());
    }
  }

  private final static String getSystemIdentifier(ConnectionProvider conn) throws ServletException {
    validateConnection(conn);
    String systemIdentifier = SystemInfoData.selectSystemIdentifier(conn);
    if (systemIdentifier == null || systemIdentifier.equals("")) {
      systemIdentifier = UUID.randomUUID().toString();
      SystemInfoData.updateSystemIdentifier(conn, systemIdentifier);
    }
    return systemIdentifier;
  }

  /**
   * Obtains mac address a CRC of the byte[] array for the obtained mac address.
   * 
   * In case multiple interfaces are present, it is taken the first one with mac address of the list
   * sorted in this way: loopbacks are sorted at the end, the rest of interfaces are sorted by name.
   * 
   * @return
   */
  private final static String getMacAddress() {
    List<NetworkInterface> interfaces = new ArrayList<NetworkInterface>();
    try {
      interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());

      Collections.sort(interfaces, new Comparator<NetworkInterface>() {
        @Override
        public int compare(NetworkInterface o1, NetworkInterface o2) {
          try {
            if (o1.isLoopback() && !o2.isLoopback()) {
              return 1;
            }
            if (!o1.isLoopback() && o2.isLoopback()) {
              return -1;
            }
          } catch (SocketException e) {
            log4j.error("Error sorting network interfaces", e);
            return 0;
          }
          return o1.getName().compareTo(o2.getName());
        }
      });

      for (NetworkInterface iface : interfaces) {
        if (iface.getHardwareAddress() != null) {
          // get the first not null hw address and CRC it
          CRC32 crc = new CRC32();
          crc.update(iface.getHardwareAddress());
          return Long.toHexString(crc.getValue());
        }
      }

      if (interfaces.isEmpty()) {
        log4j.warn("Not found mac adress");
      }
      return "";
    } catch (SocketException e) {
      log4j.error("Error getting mac address", e);
      return "";
    }
  }

  /**
   * Obtains a unique identifier for database
   */
  private final static String getDBIdentifier(ConnectionProvider conn) {
    Properties obProps = OBPropertiesProvider.getInstance().getOpenbravoProperties();
    if ("ORACLE".equals(conn.getRDBMS())) {
      Connection con = null;
      Statement st = null;
      try {
        // Obtain a direct jdbc connection instead of using DAL nor ConnectionProvider. This query
        // is needed to be executed with DBA privileges, which standard user might not have.

        con = DriverManager.getConnection(obProps.getProperty("bbdd.url"), obProps
            .getProperty("bbdd.systemUser"), obProps.getProperty("bbdd.systemPassword"));
        st = con.createStatement();
        st.execute("select dbid from v$database");
        st.getResultSet().next();
        String id = st.getResultSet().getString(1);
        return id;
      } catch (SQLException e) {
        log4j.error("Error obtaining Oracle's DB identifier");
        return "";
      } finally {
        try {
          st.getResultSet().close();
          st.close();
          con.close();
        } catch (SQLException e) {
          log4j.error("Error closing connection for setting db id", e);
        }
      }
    } else { // PG
      Vector<String> param = new Vector<String>();
      param.add(obProps.getProperty("bbdd.sid"));
      try {
        // Executing query in this way instead of with sqlc not to have to create pg_stat_database
        // view in Oracle
        ExecuteQuery q = new ExecuteQuery(conn,
            "select datid from pg_stat_database where datname=?", param);
        FieldProvider[] results = q.select();
        if (results.length != 0) {
          return results[0].getField("datid");
        }
      } catch (Exception e) {
        log4j.error("Error getting PG DB ID", e);
      }
      log4j.warn("Not found DB id");
      return "";
    }
  }

  private final static String getDatabaseVersion(ConnectionProvider conn) throws ServletException {
    validateConnection(conn);
    if (systemInfo.get(Item.DATABASE) == null) {
      load(Item.DATABASE, conn);
    }
    String database = systemInfo.get(Item.DATABASE);
    String databaseVersion = null;
    if ("ORACLE".equals(database)) {
      databaseVersion = getVersion(SystemInfoData.selectOracleVersion(conn));
    } else {
      databaseVersion = SystemInfoData.selectPostregresVersion(conn);
    }
    return databaseVersion;
  }

  /**
   * Runs a native command to try and locate the user's web server version. Tests all combinations
   * of paths + commands.
   * 
   * Currently only checks for Apache.
   * 
   * @param conn
   * @throws ServletException
   */
  private final static String[] getWebserver(ConnectionProvider conn) {
    List<String> commands = new ArrayList<String>();
    String[] paths = { "/usr/local/sbin", "/usr/local/bin", "/usr/sbin", "/usr/bin", "/sbin",
        "/bin" };
    String[] execs = { "httpd", "apache", "apache2" };
    for (String path : paths) {
      for (String exec : execs) {
        commands.add(path + "/" + exec);
      }
    }
    commands.addAll(Arrays.asList(execs));
    for (String command : commands) {
      try {
        Process process = new ProcessBuilder(command, "-v").start();
        InputStream is = process.getInputStream();
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);

        StringBuilder sb = new StringBuilder();
        String line;

        while ((line = br.readLine()) != null) {
          sb.append(line);
        }
        Pattern pattern = Pattern.compile("Apache/((\\d+\\.)+)\\d+");
        Matcher matcher = pattern.matcher(sb.toString());
        if (matcher.find()) {
          String s = matcher.group();
          return s.split("/");
        }
      } catch (IOException e) {
        // OK. We'll probably get a lot of these.
      }
    }
    return new String[] { "", "" };
  }

  /**
   * Returns the activity rate of the system. Range: 0..............1.- Inactive 1-100..........2.-
   * Low 101-500........3.- Medium 500-1000.......4.- High 1001 or more...5.- Very High
   * 
   * @param data
   * @return
   * @throws ServletException
   */
  private final static String getActivityRate(ConnectionProvider conn) throws ServletException {
    String result = null;
    int activityRate = Integer.valueOf(SystemInfoData.selectActivityRate(conn));
    if (activityRate == 0) {
      result = "1";
    } else if (activityRate > 0 && activityRate < 101) {
      result = "2";
    } else if (activityRate > 100 && activityRate < 501) {
      result = "3";
    } else if (activityRate > 500 && activityRate < 1001) {
      result = "4";
    } else if (activityRate > 1001) {
      result = "5";
    }
    return result;
  }

  /**
   * Returns the complexity rate of the system Range: 0-2 ............1.- Low 3-6.............2.-
   * Medium 7 or more.......3.- High
   * 
   * @param data
   * @return
   * @throws ServletException
   */
  private final static String getComplexityRate(ConnectionProvider conn) throws ServletException {
    String result = null;
    int complexityRate = Integer.valueOf(SystemInfoData.selectComplexityRate(conn));
    if (complexityRate > 0 && complexityRate < 3) {
      result = "1";
    } else if (complexityRate > 2 && complexityRate < 7) {
      result = "2";
    } else {
      result = "3";
    }
    return result;
  }

  /**
   * Obtain all the modules installed in the instance.
   * 
   * @return
   */
  private final static String getModules() {
    try {
      OBContext.setAdminMode();
      OBCriteria<Module> qMods = OBDal.getInstance().createCriteria(Module.class);
      qMods.addOrder(Order.asc(Module.PROPERTY_JAVAPACKAGE));
      JSONArray mods = new JSONArray();
      for (Module mod : qMods.list()) {
        ArrayList<String> modInfo = new ArrayList<String>();
        modInfo.add(mod.getId());
        modInfo.add(mod.getVersion());
        modInfo.add(mod.isEnabled() ? "Y" : "N");
        modInfo.add(mod.getName());
        mods.put(modInfo);
      }
      return mods.toString();
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  private static boolean validateConnection(ConnectionProvider conn) throws ServletException {
    if (conn == null) {
      throw new ServletException("Invalid database connection provided.");
    }
    return true;
  }

  /**
   * @return the all systemInfo properties
   */
  public static Properties getSystemInfo() {
    Properties props = new Properties();
    if (systemInfo == null) {
      return props;
    }
    for (Map.Entry<Item, String> entry : systemInfo.entrySet()) {
      String key = entry.getKey().getLabel();
      String value = entry.getValue();
      props.setProperty(key, value);
    }
    return props;
  }

  /**
   * Returns the string representation of a numerical version from a longer string. For example,
   * given the string: 'Apache Ant version 1.7.0 compiled on August 29 2007' getVersion() will
   * return '1.7.0'
   * 
   * @param str
   * @return the string representation of a numerical version from a longer string.
   */
  private static String getVersion(String str) {
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

  /**
   * In case it is an OBPS instance, it returns the CRC of the activation key
   * 
   * @return
   */
  private static String getOBPSInstance() {
    if (ActivationKey.getInstance().isOPSInstance()) {
      return ActivationKey.getInstance().getOpsLogId();
    } else {
      return "";
    }
  }

  private static void loadLoginInfo() {
    StringBuilder hql = new StringBuilder();
    hql.append("select min(s.creationDate) as firstLogin, ");
    hql.append("       max(s.creationDate) as lastLogin, ");
    hql.append("       count(*) as totalLogins");
    hql.append("  from ADSession s");
    Query q = OBDal.getInstance().getSession().createQuery(hql.toString());
    if (q.list().size() != 0) {
      Object[] logInfo = (Object[]) q.list().get(0);
      firstLogin = (Date) logInfo[0];
      lastLogin = (Date) logInfo[1];
      numberOfLogins = (Long) logInfo[2];
    }
  }

  /**
   * @param item
   * @return the systemInfo of the passed item
   */
  public static String get(Item item) {
    return systemInfo.get(item);
  }

  public enum Item {
    SYSTEM_IDENTIFIER("systemIdentifier", true), MAC_IDENTIFIER("macId", true), DB_IDENTIFIER(
        "dbIdentifier", true), OPERATING_SYSTEM("os", false), OPERATING_SYSTEM_VERSION("osVersion",
        false), DATABASE("db", false), DATABASE_VERSION("dbVersion", false), WEBSERVER("webserver",
        false), WEBSERVER_VERSION("webserverVersion", false), SERVLET_CONTAINER("servletContainer",
        false), SERVLET_CONTAINER_VERSION("servletContainerVersion", false), ANT_VERSION(
        "antVersion", false), OB_VERSION("obVersion", false), OB_INSTALL_MODE("obInstallMode",
        false), CODE_REVISION("codeRevision", false), NUM_REGISTERED_USERS("numRegisteredUsers",
        false), ISHEARTBEATACTIVE("isheartbeatactive", false), ISPROXYREQUIRED("isproxyrequired",
        false), PROXY_SERVER("proxyServer", false), PROXY_PORT("proxyPort", false), ACTIVITY_RATE(
        "activityRate", false), COMPLEXITY_RATE("complexityRate", false), JAVA_VERSION(
        "javaVersion", false), MODULES("modules", false), OBPS_INSTANCE("obpsId", false), FIRT_LOGIN(
        "firstLogin", false), LAST_LOGIN("lastLogin", false), TOTAL_LOGINS("totalLogins", false);

    private String label;
    private boolean isIdInfo;

    private Item(String label, boolean isIdInfo) {
      this.label = label;
      this.isIdInfo = isIdInfo;
    }

    public String getLabel() {
      return label;
    }

    public boolean isIdInfo() {
      return isIdInfo;
    }
  }

  /**
   * Parses a date represented by a String with the format used for the date properties into a Date
   */
  public static Date parseDate(String date) throws ParseException {
    return sd.parse(date);
  }

}
