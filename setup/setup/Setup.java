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
 * All portions are Copyright (C) 2001-2006 Openbravo SL 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
*/
import java.io.*;
import java.util.Properties;

public class Setup {
  static String baseApp;
  static String baseSource;
  static String baseTemp;
  static String attachPath;
  static String fontBase;
  static String webUrl;
  static String contextUrl;
  static String bbddHost;
  static String bbddUser;
  static String bbddPassword;
  static String bbddPort;
  static String bbddSid;
  static String jakartaContextName;
  static String subversionBase;
  static String systemPassword = "";
  static String systemUser = "";
  static String bbddTns = "";
  static String actualPath = ".";
  static String rdbms = "";
  
  public static void main(String[] args) throws Exception {
    if (args.length<12) throw new Exception("Incorrect number of arguments");
    baseApp = args[0];
    baseSource = args[1];
    baseTemp = args[2];
    attachPath = args[3];
    fontBase = args[4];
    webUrl = args[5];
    bbddHost = args[6];
    bbddUser = args[7];
    bbddPassword = args[8];
    bbddSid = args[9];
    bbddPort = args[10];
    jakartaContextName = args[11];
    if (args.length>12) systemPassword = args[12];
    if (args.length>13) bbddTns = args[13];
    if (args.length>14) actualPath = args[14];
    if (args.length>15) rdbms = args[15];
    if (args.length>16) systemUser = args[16];
    
    File fileSource = new File(actualPath);
    if (!fileSource.exists()) throw new Exception("Unknown directory: " + actualPath);
    
    replacePropertiesFile(fileSource);
    replaceUserConfigFile(fileSource);
  }
  public static void replacePropertiesFile(File path) {
    try {
      File file = new File(path, "config/Openbravo.properties");
      if (!file.exists()) {
        System.out.println("Unknown file: " + path + "/Openbravo.properties");
        return;
      }
      
      /*
       * DB properties
       */
      Properties properties = new Properties();
      properties.load(new FileInputStream(file));
      properties.setProperty("bbdd.sid", bbddSid);
      properties.setProperty("bbdd.user", bbddUser);
      properties.setProperty("bbdd.password", bbddPassword);
      properties.setProperty("bbdd.rdbms",rdbms);
      
      if (systemPassword!=null && !systemPassword.equals("")) {
        properties.setProperty("bbdd.systemUser", systemUser);
        properties.setProperty("bbdd.systemPassword", systemPassword);
      }
        
      if (rdbms.equals("ORACLE")) { 
        properties.setProperty("bbdd.driver", "oracle.jdbc.OracleDriver");
        properties.setProperty("bbdd.url", "jdbc:oracle:thin:"+bbddUser+"/"+bbddPassword+"@"+bbddHost+":"+bbddPort+":"+bbddSid);
        properties.setProperty("bbdd.sessionConfig","ALTER SESSION SET NLS_LANGUAGE='SPANISH' NLS_DATE_FORMAT='DD-MM-YYYY' NLS_NUMERIC_CHARACTERS='.,'");
      } else { //postgres
        properties.setProperty("bbdd.driver", "org.postgresql.Driver");
        properties.setProperty("bbdd.url","jdbc:postgresql://"+bbddHost+":"+bbddPort);
      }
      
      /*
       * build.xml properties
       */
      properties.setProperty("base.app", baseApp);
      properties.setProperty("base.source", baseSource);
      properties.setProperty("base.temp", baseTemp);
      properties.setProperty("attach.path", attachPath);
      properties.setProperty("web.url", webUrl);
      properties.setProperty("context.url", webUrl.replace("/web",""));
      properties.setProperty("context.name", jakartaContextName);
      
      String strHeader = 
         "# *************************************************************************\n"
        +"# * The contents of this file are subject to the Openbravo  Public  License\n"
        +"# * Version  1.0  (the  \"License\"),  being   the  Mozilla   Public  License\n"
        +"# * Version 1.1  with a permitted attribution clause; you may not  use this\n"
        +"# * file except in compliance with the License. You  may  obtain  a copy of\n"
        +"# * the License at http://www.openbravo.com/legal/license.html \n"
        +"# * Software distributed under the License  is  distributed  on  an \"AS IS\"\n"
        +"# * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the\n"
        +"# * License for the specific  language  governing  rights  and  limitations\n"
        +"# * under the License. \n"
        +"# * The Original Code is Openbravo ERP. \n"
        +"# * The Initial Developer of the Original Code is Openbravo SL\n" 
        +"# * All portions are Copyright (C) 2007 Openbravo SL\n" 
        +"# * All Rights Reserved. \n"
        +"# * Contributor(s):  ______________________________________.\n"
        +"# ************************************************************************\n";
        
      properties.store(new FileOutputStream(file), strHeader);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  

  public static void replaceUserConfigFile(File path) {
    try {
      File file = new File(path, "config/userconfig.xml");
      if (!file.exists()) {
        System.out.println("Unknown file: " + path + "/config/userconfig.xml");
        return;
      }
      BufferedReader fileBuffer = new BufferedReader(new FileReader(file));
      StringBuffer total = new StringBuffer();

      String nextLine = fileBuffer.readLine();
      boolean found = false;
      boolean replaced = false;
      while (nextLine != null) {
        if (!found && (nextLine.indexOf("fontBaseDir")!=-1)) found=true;
        if (found) System.out.println("found:"+nextLine);
        if (found && (nextLine.indexOf("<!--")!=-1)) 
          nextLine = "";
        if (found && (nextLine.indexOf("<value>")!=-1)) { 
          nextLine = "<value>" + fontBase + "</value>";
          replaced = true;
        }
        if (replaced && (nextLine.indexOf("-->")!=-1))
          nextLine = "";
        
        total.append(nextLine).append("\n");
        nextLine = fileBuffer.readLine();
      }
      fileBuffer.close();


      FileWriter fileWriter = new FileWriter(file);
      PrintWriter printWriter = new PrintWriter(fileWriter);
      printWriter.print(total.toString());
      printWriter.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

}
