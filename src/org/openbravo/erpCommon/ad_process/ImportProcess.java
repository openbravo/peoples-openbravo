/*
 ******************************************************************************
 * The contents of this file are subject to the   Compiere License  Version 1.1
 * ("License"); You may not use this file except in compliance with the License
 * You may obtain a copy of the License at http://www.compiere.org/license.html
 * Software distributed under the License is distributed on an  "AS IS"  basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific language governing rights and limitations under the License.
 * The Original Code is                  Compiere  ERP & CRM  Business Solution
 * The Initial Developer of the Original Code is Jorg Janke  and ComPiere, Inc.
 * Portions created by Jorg Janke are Copyright (C) 1999-2001 Jorg Janke, parts
 * created by ComPiere are Copyright (C) ComPiere, Inc.;   All Rights Reserved.
 * Contributor(s): Openbravo SL
 * Contributions are Copyright (C) 2001-2006 Openbravo S.L.
 ******************************************************************************
 */
package org.openbravo.erpCommon.ad_process;

import java.util.Vector;

import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.SequenceIdData;
import org.openbravo.erpCommon.utility.Utility;

public abstract class ImportProcess {
  static Logger log4j = Logger.getLogger(ImportProcess.class);

  private String m_AD_PInstance_ID = "";
  private OBError m_error = new OBError();
  private StringBuffer m_msg = new StringBuffer();
  private String m_AD_User_ID = "";
  private String m_AD_Client_ID = "";
  private String m_AD_Org_ID = "";
  private Vector<Object> m_params = new Vector<Object>();
  private ConnectionProvider m_conn;
  protected static String MSG_SaveErrorRowNotFound = "@SaveErrorRowNotFound@";
  protected static String MSG_InvalidArguments = "@InvalidArguments@";

  public ImportProcess(ConnectionProvider conn) {
    // PropertyConfigurator.configure("log4j.lcf");
    m_conn = conn;
  }

  public final OBError startProcess(VariablesSecureApp vars) throws ServletException {
    createInstance(vars);
    createInstanceParams(vars);
    lock();
    process(vars);
    unlock();
    return m_error;
  }

  protected void process(VariablesSecureApp vars) {
    String msg = null;
    try {
      fillInstanceParams();
      m_error = doIt(vars);
    } catch (ServletException e) {
      msg = e.getMessage();
      if (msg == null)
        msg = e.toString();
      if (e.getCause() != null)
        log4j.error("process - " + msg, e.getCause());
      else
        log4j.error("process - " + msg);
      m_error.setType("Error");
      m_error.setTitle(Utility.messageBD(m_conn, "Error", vars.getLanguage()));
      m_error.setMessage(Utility.messageBD(m_conn, "ProcessRunError", vars.getLanguage()));
    }
  }

  protected abstract OBError doIt(VariablesSecureApp vars) throws ServletException;

  protected abstract void createInstanceParams(VariablesSecureApp vars) throws ServletException;

  protected abstract String getAD_Process_ID();

  protected abstract String getRecord_ID();

  protected ConnectionProvider getConnection() {
    return m_conn;
  }

  public String getAD_User_ID() {
    return m_AD_User_ID;
  }

  public void setAD_User_ID(String adUserId) {
    m_AD_User_ID = adUserId;
  }

  public String getAD_Client_ID() {
    return m_AD_Client_ID;
  }

  public void setAD_Client_ID(String adClientId) {
    m_AD_Client_ID = adClientId;
  }

  public String getAD_Org_ID() {
    return m_AD_Org_ID;
  }

  public void setAD_Org_ID(String adOrgId) {
    m_AD_Org_ID = adOrgId;
  }

  protected String getAD_PInstance_ID() {
    return m_AD_PInstance_ID;
  }

  public void addLog(String msgtext) {
    m_msg.append(msgtext);
  }

  public String getLog() {
    return m_msg.toString();
  }

  public OBError getError() {
    return m_error;
  }

  public String getParamValue(String paramName) {
    return getParamData(paramName, 1);
  }

  public String getParamValueTo(String paramName) {
    return getParamData(paramName, 2);
  }

  public String getParamData(String paramName, int index) {
    if (m_params == null)
      return "";
    for (int i = 0; i < m_params.size(); i++) {
      String[] param = (String[]) (m_params.elementAt(i));
      if (param[0].equals(paramName))
        return param[index];
    }
    return "";
  }

  private void createInstance(VariablesSecureApp vars) throws ServletException {
    try {
      setAD_User_ID(vars.getUser());
      setAD_Client_ID(vars.getClient());
      setAD_Org_ID(vars.getOrg());
      m_AD_PInstance_ID = SequenceIdData.getUUID();

      ImportProcessData.insertInstance(getConnection(), m_AD_PInstance_ID, getAD_Process_ID(),
          getRecord_ID(), getAD_User_ID(), getAD_Client_ID(), getAD_Org_ID());
    } catch (ServletException ex) {
      log4j.error("Unable to create instance - " + ex);
      throw new ServletException("Unable to create instance - " + ex.getMessage());
    }
  }

  public void addParameterString(String parametername, String seqno, String value)
      throws ServletException {
    ImportProcessData.insertPInstanceParam(getConnection(), getAD_PInstance_ID(), seqno,
        parametername, value, getAD_Client_ID(), getAD_Org_ID(), getAD_User_ID());
  }

  public void addParameterNumber(String parametername, String seqno, String value)
      throws ServletException {
    ImportProcessData.insertPInstanceParamNumber(getConnection(), getAD_PInstance_ID(), seqno,
        parametername, value, getAD_Client_ID(), getAD_Org_ID(), getAD_User_ID());
  }

  public void addParameterDate(String parametername, String seqno, String value)
      throws ServletException {
    ImportProcessData.insertPInstanceParamDate(getConnection(), getAD_PInstance_ID(), seqno,
        parametername, value, getAD_Client_ID(), getAD_Org_ID(), getAD_User_ID());
  }

  public void addParameterInfo(String parametername, String seqno, String value)
      throws ServletException {
    ImportProcessData.insertPInstanceParamInfo(getConnection(), getAD_PInstance_ID(), seqno,
        parametername, value, getAD_Client_ID(), getAD_Org_ID(), getAD_User_ID());
  }

  private void lock() throws ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("lock");
    try {
      ImportProcessData.lockInstance(getConnection(), getAD_PInstance_ID());
    } catch (ServletException ex) {
      log4j.error("Unable to lock instance - " + ex);
      throw new ServletException("Unable to lock instance - " + ex.getMessage());
    }
  }

  private void unlock() throws ServletException {
    try {
      ImportProcessData.unlockInstance(getConnection(), (m_error.getType().equals("Error") ? "0"
          : "1"), m_msg.toString(), getAD_PInstance_ID());
    } catch (ServletException ex) {
      log4j.error("Unable to unlock instance - " + ex);
      throw new ServletException("Unable to unlock instance - " + ex.getMessage());
    }
    if (log4j.isDebugEnabled())
      log4j.debug("unlock - Error=" + m_error.toString() + " - " + m_msg.toString());
  }

  private void fillInstanceParams() throws ServletException {
    ImportProcessData[] data = ImportProcessData.select(getConnection(), getAD_PInstance_ID());
    if (data == null || data.length == 0)
      return;
    for (int i = 0; i < data.length; i++) {
      String[] param = { data[i].parametername, "", "" };
      if (data[i].pString != null && !data[i].pString.equals("")) {
        param[1] = new String(data[i].pString);
        param[2] = new String(data[i].pStringTo);
      } else if (data[i].pNumber != null && !data[i].pNumber.equals("")) {
        param[1] = new String(data[i].pNumber);
        param[2] = new String(data[i].pNumberTo);
      } else if (data[i].pDate != null && !data[i].pDate.equals("")) {
        param[1] = new String(data[i].pDate);
        param[2] = new String(data[i].pDateTo);
      } else if (data[i].info != null && !data[i].info.equals("")) {
        param[1] = new String(data[i].info);
        param[2] = new String(data[i].infoTo);
      }
      m_params.addElement(param);
    }
  }

}
