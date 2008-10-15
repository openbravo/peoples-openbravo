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
package org.openbravo.erpCommon.security;

import javax.servlet.*;
import javax.servlet.http.*;
import java.net.*;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.utility.SequenceIdData;

import org.apache.log4j.Logger ;

public class SessionLogin {
  static Logger log4j = Logger.getLogger(SessionLogin.class);
  protected String sessionID;
  protected String client;
  protected String org;
  protected String isactive = "Y";
  protected String user;
  protected String websession;
  protected String remoteAddr;
  protected String remoteHost;
  protected String processed = "N";

  public SessionLogin(String ad_client_id, String ad_org_id, String ad_user_id) throws ServletException {
    if (ad_client_id==null || ad_client_id.equals("")) throw new ServletException("SessionLogin load - client is null");
    else if (ad_org_id==null || ad_org_id.equals("")) throw new ServletException("SessionLogin load - organization is null");
    else if (ad_user_id==null || ad_user_id.equals("")) throw new ServletException("SessionLogin load - user is null");
    setClient(ad_client_id);
    setOrg(ad_org_id);
    setUser(ad_user_id);
    defaultParameters();
  }

  public SessionLogin(HttpServletRequest request, String ad_client_id, String ad_org_id, String ad_user_id) throws ServletException {
    if (ad_client_id==null || ad_client_id.equals("")) throw new ServletException("SessionLogin load - client is null");
    else if (ad_org_id==null || ad_org_id.equals("")) throw new ServletException("SessionLogin load - organization is null");
    else if (ad_user_id==null || ad_user_id.equals("")) throw new ServletException("SessionLogin load - user is null");
    setClient(ad_client_id);
    setOrg(ad_org_id);
    setUser(ad_user_id);
    defaultParameters(request);
  }

  public void defaultParameters() {
    try {
      InetAddress lh = InetAddress.getLocalHost();
      setRemoteAddr(lh.getHostAddress());
      setRemoteHost(lh.getHostName());
    } catch (UnknownHostException e) {
      log4j.error("SessionLogin.defaultParameters() - No local host. " + e);
    }
    if (log4j.isDebugEnabled()) log4j.debug("SessionLogin.defaultParameters() - Remote Address: " + getRemoteAddr() + " - Remote Host: " + getRemoteHost());
  }

  public void defaultParameters(HttpServletRequest request) {
    setRemoteAddr(request.getRemoteAddr());
    setRemoteHost(request.getRemoteHost());
    if (log4j.isDebugEnabled()) log4j.debug("SessionLogin.defaultParameters(request) - Remote Address: " + getRemoteAddr() + " - Remote Host: " + getRemoteHost());
  }

  public int save(ConnectionProvider conn) throws ServletException {
    if (getSessionID().equals("")) {
      String key = SequenceIdData.getUUID();
      if (key==null || key.equals("")) throw new ServletException("SessionLogin.save() - key creation failed");
      setSessionID(key);
      return SessionLoginData.insert(conn, getSessionID(), getClient(), getOrg(), getIsActive(), getUser(), getWebSession(), getRemoteAddr(), getRemoteHost(), getProcessed());
    } else return SessionLoginData.update(conn, getIsActive(), getUser(), getWebSession(), getRemoteAddr(), getRemoteHost(), getProcessed(), getSessionID());
  }

  public void setSessionID(String newValue) {
    this.sessionID = (newValue==null)?"":newValue;
  }

  public String getSessionID() {
    return ((this.sessionID==null)?"":this.sessionID);
  }

  public void setClient(String newValue) {
    this.client = (newValue==null)?"":newValue;
  }

  public String getClient() {
    return ((this.client==null)?"":this.client);
  }

  public void setOrg(String newValue) {
    this.org = (newValue==null)?"":newValue;
  }

  public String getOrg() {
    return ((this.org==null)?"":this.org);
  }

  public void setIsActive(String newValue) {
    this.isactive = (newValue==null)?"Y":newValue;
  }

  public String getIsActive() {
    return (this.isactive);
  }

  public void setUser(String newValue) {
    this.user = (newValue==null)?"":newValue;
  }

  public String getUser() {
    return ((this.user==null)?"":this.user);
  }

  public void setWebSession(String newValue) {
    this.websession = (newValue==null)?"":newValue;
  }

  public String getWebSession() {
    return ((this.websession==null)?"":this.websession);
  }

  public void setRemoteAddr(String newValue) {
    this.remoteAddr = (newValue==null)?"":newValue;
  }

  public String getRemoteAddr() {
    return ((this.remoteAddr==null)?"":this.remoteAddr);
  }

  public void setRemoteHost(String newValue) {
    this.remoteHost = (newValue==null)?"":newValue;
  }

  public String getRemoteHost() {
    return ((this.remoteHost==null)?"":this.remoteHost);
  }

  public void setProcessed(String newValue) {
    this.processed = (newValue==null)?"":newValue;
  }

  public String getProcessed() {
    return ((this.processed==null)?"":this.processed);
  }
}
