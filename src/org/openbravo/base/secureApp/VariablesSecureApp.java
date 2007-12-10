/*
 ************************************************************************************
 * Copyright (C) 2001-2006 Openbravo S.L.
 * Licensed under the Apache Software License version 2.0
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to  in writing,  software  distributed
 * under the License is distributed  on  an  "AS IS"  BASIS,  WITHOUT  WARRANTIES  OR
 * CONDITIONS OF ANY KIND, either  express  or  implied.  See  the  License  for  the
 * specific language governing permissions and limitations under the License.
 ************************************************************************************
*/
package org.openbravo.base.secureApp;

import org.openbravo.base.VariablesBase;
import javax.servlet.http.*;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.data.FieldProvider;

public class VariablesSecureApp extends VariablesBase {
  private String user;
  private String role;
  private String language;
  private String theme;
  private String client;
  private String organization;
  private String warehouse;
  private String command;
  private String userClient;
  private String userOrganization;
  private String dbSessionID;
  private String javaDateFormat;
  private String jsDateFormat;
  private String sqlDateFormat;

  public VariablesSecureApp(String strUser, String strClient, String strOrganization) {
    this.user = strUser;
    this.role = "";
    this.language = System.getProperty("user.language") + "_" + System.getProperty("user.country");
    this.theme = "";
    this.client = strClient;
    this.organization = strOrganization;
    this.userClient = "";
    this.userOrganization = "";
    this.warehouse = "";
    this.dbSessionID = "";
    this.command = "DEFAULT";
    this.javaDateFormat = "";
    this.jsDateFormat = "";
    this.sqlDateFormat = "";
  }

  public VariablesSecureApp(HttpServletRequest request) {
    super(request);
    this.user = getSessionValue("#AD_User_ID");
    this.role = getSessionValue("#AD_Role_ID");
    this.language = getSessionValue("#AD_Language");
    this.theme = getSessionValue("#Theme");
    this.client = getSessionValue("#AD_Client_ID");
    this.organization = getSessionValue("#AD_Org_ID");
    this.userClient = getSessionValue("#User_Client");
    this.userOrganization = getSessionValue("#User_Org");
    this.warehouse = getSessionValue("#M_Warehouse_ID");
    this.dbSessionID = getSessionValue("#AD_Session_ID");
    this.command = getStringParameter("Command", "DEFAULT");
    this.javaDateFormat = getSessionValue("#AD_JavaDateFormat");
    this.jsDateFormat = getSessionValue("#AD_JsDateFormat");
    this.sqlDateFormat = getSessionValue("#AD_SqlDateFormat");
  }
  
  public VariablesSecureApp(HttpServletRequest request, boolean f) {
    super(request,f);
    this.user = getSessionValue("#AD_User_ID");
    this.role = getSessionValue("#AD_Role_ID");
    this.language = getSessionValue("#AD_Language");
    this.theme = getSessionValue("#Theme");
    this.client = getSessionValue("#AD_Client_ID");
    this.organization = getSessionValue("#AD_Org_ID");
    this.userClient = getSessionValue("#User_Client");
    this.userOrganization = getSessionValue("#User_Org");
    this.warehouse = getSessionValue("#M_Warehouse_ID");
    this.dbSessionID = getSessionValue("#AD_Session_ID");
    this.command = getStringParameter("Command", "DEFAULT");
    this.javaDateFormat = getSessionValue("#AD_JavaDateFormat");
    this.jsDateFormat = getSessionValue("#AD_JsDateFormat");
    this.sqlDateFormat = getSessionValue("#AD_SqlDateFormat");
  }

  public String getUser() {
    return user;
  }

  public String getRole() {
    return role;
  }

  public String getLanguage() {
    return language;
  }

  public String getTheme() {
    return theme;
  }

  public String getClient() {
    return client;
  }

  public String getOrg() {
    return organization;
  }

  public String getUserClient() {
    return userClient;
  }

  public String getUserOrg() {
    return userOrganization;
  }

  public String getWarehouse() {
    return warehouse;
  }

  public String getDBSession() {
    return dbSessionID;
  }

  public String getCommand() {
    return command;
  }

  public boolean commandIn(String inKey1) {
    if (command.equals(inKey1))
      return true;
    else
      return false;
  }

  public boolean commandIn(String inKey1, String inKey2) {
    if (command.equals(inKey1) || command.equals(inKey2))
      return true;
    else
      return false;
  }

  public boolean commandIn(String inKey1, String inKey2, String inKey3) {
    if (command.equals(inKey1) || command.equals(inKey2) || command.equals(inKey3))
      return true;
    else
      return false;
  }

  public boolean commandIn(String inKey1, String inKey2, String inKey3, String inKey4) {
    if (command.equals(inKey1) || command.equals(inKey2) || command.equals(inKey3) || command.equals(inKey4))
      return true;
    else
      return false;
  }
  
  public boolean commandIn(String inKey1, String inKey2, String inKey3, String inKey4, String inKey5) {
    if (command.equals(inKey1) || command.equals(inKey2) || command.equals(inKey3) || command.equals(inKey4) || command.equals(inKey5))
      return true;
    else
      return false;
  }

  public String getJavaDateFormat() {
    return javaDateFormat;
  }
  public String getJsDateFormat() {
    return jsDateFormat;
  }
  public String getSqlDateFormat() {
    return sqlDateFormat;
  }

  public OBError getMessage(String AD_Tab_ID) {
    return ((OBError)getSessionObject(AD_Tab_ID + "|message"));
  }

  public void setMessage(String AD_Tab_ID, OBError error) {
    setSessionObject(AD_Tab_ID + "|message", error);
  }

  public void removeMessage(String AD_Tab_ID) {
    removeSessionValue(AD_Tab_ID + "|message");
  }
  
  public FieldProvider getEditionData(String AD_Tab_ID) {
    return ((FieldProvider)getSessionObject(AD_Tab_ID + "|editionData"));
  }

  public void setEditionData(String AD_Tab_ID, FieldProvider data) {
    setSessionObject(AD_Tab_ID + "|editionData", data);
  }

  public void removeEditionData(String AD_Tab_ID) {
    removeSessionValue(AD_Tab_ID + "|editionData");
  }
}
