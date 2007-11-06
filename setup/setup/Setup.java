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
  static String bbddTns = "";
  static String actualPath = ".";

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
    File fileSource = new File(actualPath);
    if (!fileSource.exists()) throw new Exception("Unknown directory: " + actualPath);
    
    replaceBuildFile(fileSource);
    if (systemPassword!=null && !systemPassword.equals("")) replaceBuildDatabaseFile(fileSource);
    replaceDBCon5File(fileSource);
    replaceXMLPoolFile(fileSource);
    replaceUserConfigFile(fileSource);
    replaceDeployWsddFile(fileSource);
  }

  public static void replaceBuildFile(File path) {
    try {
      File file = new File(path, "build.xml");
      if (!file.exists()) {
        System.out.println("Unknown file: " + path + "/build.xml");
        return;
      }
      BufferedReader fileBuffer = new BufferedReader(new FileReader(file));
      StringBuffer total = new StringBuffer();

      String nextLine = fileBuffer.readLine();
      while (nextLine != null) {
        int aux = -1;
        if ((aux=nextLine.indexOf("<property name=\"base.app\""))!=-1) {
          nextLine = replacePropertyValue(nextLine, baseApp, aux);
        } else if ((aux=nextLine.indexOf("<property name=\"base.source\""))!=-1) {
          nextLine = replacePropertyValue(nextLine, baseSource, aux);
        } else if ((aux=nextLine.indexOf("<property name=\"base.temp\""))!=-1) {
          nextLine = replacePropertyValue(nextLine, baseTemp, aux);
        } else if ((aux=nextLine.indexOf("<property name=\"attach.path\""))!=-1) {
          nextLine = replacePropertyValue(nextLine, attachPath, aux);
        } else if ((aux=nextLine.indexOf("<property name=\"web.url\""))!=-1) {
          nextLine = replacePropertyValue(nextLine, webUrl, aux);
        } else if ((aux=nextLine.indexOf("<property name=\"context.url\""))!=-1) {
          nextLine = replacePropertyValue(nextLine, webUrl.replace("/web",""), aux);
        } else if ((aux=nextLine.indexOf("<property name=\"context.name\""))!=-1) {
          nextLine = replacePropertyValue(nextLine, jakartaContextName, aux);
        } else if ((aux=nextLine.indexOf("<property name=\"jakarta.context\""))!=-1) {
          nextLine = replacePropertyValue(nextLine, "${jakarta.home}${separator}webapps${separator}" + jakartaContextName, aux);
	} 
 
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

  public static void replaceDBCon5File(File path) {
    try {
      File file = new File(path, "config/dbCon5.xml");
      if (!file.exists()) {
        System.out.println("Unknown file: " + path + "/config/dbCon5.xml");
        return;
      }
      StringBuffer strBuf = new StringBuffer();
      strBuf.append("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n");
      strBuf.append("<!--\n");
      strBuf.append(" *************************************************************************\n");
      strBuf.append(" * The contents of this file are subject to the Openbravo  Public  License\n");
      strBuf.append(" * Version  1.0  (the  \"License\"),  being   the  Mozilla   Public  License\n");
      strBuf.append(" * Version 1.1  with a permitted attribution clause; you may not  use this\n");
      strBuf.append(" * file except in compliance with the License. You  may  obtain  a copy of\n");
      strBuf.append(" * the License at http://www.openbravo.com/legal/license.html \n");
      strBuf.append(" * Software distributed under the License  is  distributed  on  an \"AS IS\"\n");
      strBuf.append(" * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the\n");
      strBuf.append(" * License for the specific  language  governing  rights  and  limitations\n");
      strBuf.append(" * under the License. \n");
      strBuf.append(" * The Original Code is Openbravo ERP. \n");
      strBuf.append(" * The Initial Developer of the Original Code is Openbravo SL \n");
      strBuf.append(" * All portions are Copyright (C) 2001-2006 Openbravo SL \n");
      strBuf.append(" * All Rights Reserved. \n");
      strBuf.append(" * Contributor(s):  ______________________________________.\n");
      strBuf.append(" ************************************************************************\n");
      strBuf.append("-->\n");
      strBuf.append("\n");
      strBuf.append("\n");
      strBuf.append("<data-base>\n");
      strBuf.append("  <connection name=\"myPool\" type=\"pool\" driver = \"oracle.jdbc.OracleDriver\" URL = \"jdbc:oracle:thin:").append(bbddUser).append("/").append(bbddPassword).append("@").append(bbddHost).append(":").append(bbddPort).append(":").append(bbddSid).append("\">\n");
      strBuf.append("  </connection>\n");
      strBuf.append("</data-base>\n");


      FileWriter fileWriter = new FileWriter(file);
      PrintWriter printWriter = new PrintWriter(fileWriter);
      printWriter.print(strBuf.toString());
      printWriter.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static void replaceBuildDatabaseFile(File path) {
    try {
      File file = new File(path, "/database/build.xml");
      if (!file.exists()) {
        System.out.println("Unknown file: " + path + "/database/build.xml");
        return;
      }
      BufferedReader fileBuffer = new BufferedReader(new FileReader(file));
      StringBuffer total = new StringBuffer();

      String nextLine = fileBuffer.readLine();
      while (nextLine != null) {
        int aux = -1;
        if ((aux=nextLine.indexOf("<property name=\"bbdd.host\""))!=-1) {
          nextLine = replacePropertyValue(nextLine, bbddHost, aux);
        } else if ((aux=nextLine.indexOf("<property name=\"bbdd.port\""))!=-1) {
          nextLine = replacePropertyValue(nextLine, bbddPort, aux);
        } else if ((aux=nextLine.indexOf("<property name=\"bbdd.sid\""))!=-1) {
          nextLine = replacePropertyValue(nextLine, bbddSid, aux);
        } else if ((aux=nextLine.indexOf("<property name=\"systemPassword\""))!=-1) {
          nextLine = replacePropertyValue(nextLine, systemPassword, aux);
        } else if ((aux=nextLine.indexOf("<property name=\"bbdd.tns\""))!=-1) {
          nextLine = replacePropertyValue(nextLine, bbddTns, aux);
        } else if ((aux=nextLine.indexOf("<property name=\"bbdd.user\""))!=-1) {
          nextLine = replacePropertyValue(nextLine, bbddUser, aux);
        } else if ((aux=nextLine.indexOf("<property name=\"bbdd.password\""))!=-1) {
          nextLine = replacePropertyValue(nextLine, bbddPassword, aux);
        }

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


  public static void replaceXMLPoolFile(File path) {
    try {
      File file = new File(path, "config/XmlPool.xml");
      if (!file.exists()) {
        System.out.println("Unknown file: " + path + "/config/XmlPool.xml");
        return;
      }
      StringBuffer strBuf = new StringBuffer();
      strBuf.append("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n");
      strBuf.append("<!--\n");
      strBuf.append(" *************************************************************************\n");
      strBuf.append(" * The contents of this file are subject to the Openbravo  Public  License\n");
      strBuf.append(" * Version  1.0  (the  \"License\"),  being   the  Mozilla   Public  License\n");
      strBuf.append(" * Version 1.1  with a permitted attribution clause; you may not  use this\n");
      strBuf.append(" * file except in compliance with the License. You  may  obtain  a copy of\n");
      strBuf.append(" * the License at http://www.openbravo.com/legal/license.html \n");
      strBuf.append(" * Software distributed under the License  is  distributed  on  an \"AS IS\"\n");
      strBuf.append(" * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the\n");
      strBuf.append(" * License for the specific  language  governing  rights  and  limitations\n");
      strBuf.append(" * under the License. \n");
      strBuf.append(" * The Original Code is Openbravo ERP. \n");
      strBuf.append(" * The Initial Developer of the Original Code is Openbravo SL \n");
      strBuf.append(" * All portions are Copyright (C) 2001-2006 Openbravo SL \n");
      strBuf.append(" * All Rights Reserved. \n");
      strBuf.append(" * Contributor(s):  ______________________________________.\n");
      strBuf.append(" ************************************************************************\n");
      strBuf.append("-->\n");
      strBuf.append("\n");
      strBuf.append("\n");
      strBuf.append("<!DOCTYPE aplication SYSTEM \"pool.dtd\">\n");
      strBuf.append("<aplication>\n");
      strBuf.append("  <pool name=\"myPool\">\n");
      strBuf.append("    <dbDriver>oracle.jdbc.OracleDriver</dbDriver>\n");
      strBuf.append("    <dbServer>jdbc:oracle:thin:@").append(bbddHost).append(":").append(bbddPort).append(":").append(bbddSid).append("</dbServer>\n");
      strBuf.append("    <dbLogin>").append(bbddUser).append("</dbLogin>\n");
      strBuf.append("    <dbPassword>").append(bbddPassword).append("</dbPassword>\n");
      strBuf.append("    <minConns>1</minConns>\n");
      strBuf.append("    <maxConns>10</maxConns>\n");
      strBuf.append("    <maxConnTime>0.5</maxConnTime>\n");
      strBuf.append("    <dbSessionConfig>ALTER SESSION SET NLS_LANGUAGE='SPANISH' NLS_DATE_FORMAT='DD-MM-YYYY' NLS_NUMERIC_CHARACTERS='.,'</dbSessionConfig>\n");
      strBuf.append("    <rdbms>ORACLE</rdbms>\n");
      strBuf.append("  </pool>\n");
      strBuf.append("</aplication>\n");


      FileWriter fileWriter = new FileWriter(file);
      PrintWriter printWriter = new PrintWriter(fileWriter);
      printWriter.print(strBuf.toString());
      printWriter.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static String replacePropertyValue(String line, String value, int pos) {
    String result = line;
    pos = line.indexOf("value=\"", pos+1);
    if (pos==-1) return line;
    pos=line.indexOf("\"", pos+1);
    if (pos!=-1) {
      result = line.substring(0, pos+1);
      result += value;
      pos=line.indexOf("\"", pos+1);
      if (pos==-1) return line;
      result += line.substring(pos);
    }
    return result;
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
      while (nextLine != null) {
        if (!found && (nextLine.indexOf("fontBaseDir"))!=-1) {
          found=true;
        } else if (found) {
          nextLine = "<value>" + fontBase + "</value>";
        }
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

  public static void replaceDeployWsddFile(File path) {
    try {
      File file = new File(path, "src/deploy.wsdd");
      if (!file.exists()) {
        System.out.println("Unknown file: " + path + "/src/deploy.wsdd");
        return;
      }
      BufferedReader fileBuffer = new BufferedReader(new FileReader(file));
      StringBuffer total = new StringBuffer();

      String nextLine = fileBuffer.readLine();
      while (nextLine != null) {
        if ((nextLine.indexOf("xmlns:myNS="))!=-1) {
          total.append(nextLine.replace(nextLine.substring(nextLine.indexOf("http://"), nextLine.indexOf("/services/")), webUrl.replace("/web",""))).append("\n");
        }
        else
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
