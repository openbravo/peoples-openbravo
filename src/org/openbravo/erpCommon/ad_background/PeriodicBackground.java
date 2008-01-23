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
 * All portions are Copyright (C) 2001-2008 Openbravo SL 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
*/
package org.openbravo.erpCommon.ad_background;

import java.sql.*;
import java.text.*;
import java.util.Date;
import javax.servlet.*;
import java.io.*;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.erpCommon.utility.SequenceIdData;
import org.openbravo.base.HttpBaseServlet;
import org.openbravo.database.ConnectionProvider;

public class PeriodicBackground implements Runnable {
  public ConnectionProvider conn;
  public HttpBaseServlet baseServlet;
  private Thread runner;
  private long seconds;
  private boolean isactive=true;
  private boolean isprocessing=false;
  private boolean cancelRequested=false;
  private boolean directLaunch=false;
  private boolean isFullTime=false;
  private boolean writingLog=false;
  public VariablesSecureApp vars=null;
  public String adProcessId="";
  public String adPinstanceId = "";
  public String adClientId = "";
  private StringBuffer lastLog=new StringBuffer();
  private StringBuffer message=new StringBuffer();
  private String pid;
  private String logFileString;
  private BackgroundProcess object;
  private boolean debugMode = false;


  public PeriodicBackground(HttpBaseServlet _base, long _seconds, String _logFileString, String _adProcessId, String _objectName) throws IOException {
    runner = new Thread(this);
    this.seconds = _seconds;
    this.logFileString = _logFileString;
    this.conn = _base;
    this.baseServlet = _base;
    this.adProcessId = _adProcessId;
    if (!startClass(_objectName)) {
      runner.interrupt();
      throw new IOException("Couldn't load class " + _objectName);
    } else if (!writePID()) {
      runner.interrupt();
      throw new IOException("Couldn't write file " + this.logFileString);
    }
  }

  private boolean startClass(String _objectName) {
    Class<?> objectClass = null;
    try {
      objectClass = Class.forName(_objectName);
    } catch (ClassNotFoundException clex) {
      clex.printStackTrace();
    }

    if (objectClass!=null) {
      try {
        object = (BackgroundProcess) objectClass.newInstance();
        if (object==null) return false;
        return true;
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    return false;
  }

  public void setSeconds(long _seconds) {
    this.seconds = _seconds;
  }

  public void setLogFilePath(String _logFileString) {
    this.logFileString = _logFileString;
  }

  public void setConnection(ConnectionProvider _conn) {
    this.conn = _conn;
  }

  public void setADProcessID(String _adProcessId) {
    this.adProcessId = _adProcessId;
  }

  public String getADProcessID() {
    return this.adProcessId;
  }

  public void start() {
    runner.start();
  }

  public boolean writePID() {
    try {
      SimpleDateFormat formatter = new SimpleDateFormat ("yyyy.MM.dd G 'at' hh:mm:ss:SSS a zzz");
      Date nowc = new Date();
      pid = formatter.format(nowc);

      BufferedWriter pidout = new BufferedWriter(new 
                                      FileWriter(logFileString + "pid" + adProcessId));
      pidout.write(pid);
      pidout.close();
    } catch(IOException ex) {
      ex.printStackTrace();
      return false;
    }
    return true;
  }

  public boolean isSamePID() {
    try {
      BufferedReader in = new BufferedReader(new 
                     FileReader(logFileString + "pid" + adProcessId));
      String curr_pid = in.readLine();
      in.close();
      return (curr_pid.equals(pid));
    } catch(IOException ex) {
      System.out.println("Can't read the file for pid info: " +
                logFileString + "pid" + adProcessId);
    }
    return false;
  }

  public void initializeParams() {
    cancelRequested=false;
    isprocessing=false;
    directLaunch=false;
    isFullTime=false;
    writingLog=false;
  }

  public void run() {
    boolean forever = true;

    while(forever) {
      try {
        if (!isSamePID()) return;
        Thread.sleep((seconds*1000));
        if (isActive()) {
          initializeParams();
          try {
            process();
          } catch(Exception ex) {
            ex.printStackTrace();
          }
          initializeParams();
        } else if (isDirectProcess()) {
          if (debugMode) debug("run()");
          if (cancelRequested) {
            initializeParams();
          } else {
            try {
              object.processPL(this, true);
            } catch(Exception ex) {
              ex.printStackTrace();
            }
            addLog("@DL_ENDED@", false);
            this.vars = null;
            initializeParams();
          }
        }
        setProcessing(false);
      } catch(InterruptedException e) {
        return;
      }
    }
  }

  private void process() throws Exception {
    PeriodicBackgroundData[] data = null;
    try {
      data = PeriodicBackgroundData.select(conn, "");
    } catch (ServletException ex) {
      ex.printStackTrace();
      return;
    }
    if (data==null) return;
    for (int j=0;j<data.length;j++) {
      if (!isSamePID()) return;
      else if (!isProcesable(false, data[j].adClientId)) {
        continue;
      }
      setProcessing(true);
      this.adClientId = data[j].adClientId;
      this.message.setLength(0);
      this.lastLog.setLength(0);
      object.processPL(this, false);
    }
  }

  public boolean isDirectProcess() {
    return(directLaunch);
  }

  public String saveLog(String adNoteId, String adClientId) {
    String strMessage="", strNewMessage="";
    try {
      if (adNoteId==null || adNoteId.equals("")) {
        adNoteId = SequenceIdData.getSequence(conn, "AD_Note", adClientId);
        PeriodicBackgroundData.insert(conn, adNoteId, adClientId, "");
      }
      if (this.message.length()>2000) {
        strMessage = this.message.toString().substring(0, 1990) + "...";
        strNewMessage = this.message.toString().substring(1990);
        this.message.setLength(0);
        this.message.append("...").append(strNewMessage);
      } else strMessage = this.message.toString();
      PeriodicBackgroundData.update(conn, strMessage, adNoteId);
      if (!strNewMessage.equals("")) adNoteId = saveLog("", adClientId);
    } catch (ServletException ex) {
      ex.printStackTrace();
    }
    return adNoteId;
  }

  public void addLog(String texto) {
    addLog(texto, true);
  }

  public void addLog(String texto, boolean generalLog) {
    writingLog=true;
    Timestamp tmp = new Timestamp(System.currentTimeMillis());
    if(this.isDirectProcess()){
    	lastLog.append("<span>").append(texto).append("</span><br>");
    }
    else {
    	if (generalLog) this.message.append(tmp.toString()).append(" - ").append(texto).append("<br>");
    	lastLog.append("<span>").append(tmp.toString()).append(" - ").append(texto).append("</span><br>");
    }
    writingLog=false;
  }

  public void clearLastLog() {
    lastLog.setLength(0);
  }

  public boolean isProcesable(boolean directProcess, String adClientId) {
    if (directLaunch!=directProcess) return false;
    if (!directProcess) {
      if (isActive() && isPlannedRange(adClientId)) {
        try {
          isFullTime = PeriodicBackgroundData.getFullTime(conn, adClientId);
        } catch (ServletException ex) {
          ex.printStackTrace();
          isFullTime=false;
        }
        return true;
      } else return false;
    } else if (isActive() || this.cancelRequested) return false;
    return true;
  }

  public boolean isPlannedRange(String AD_Client_ID) {
    try {
      return (PeriodicBackgroundData.checkPlannedRange(conn, AD_Client_ID, adProcessId));
    } catch (ServletException e) {
      e.printStackTrace();
    }
    return false;
  }

  public boolean isWritingLog() {
    return writingLog;
  }

  public String getLog() {
    return(message.toString());
  }

  public String getOut() {
    return(lastLog.toString());
  }

  public boolean isActive() {
    return (isactive);
  }

  public void setActive(boolean status) {
    if (!status || !isDirectProcess()) this.isactive = status;
  }

  public void cancelDirectProcess() {
    this.cancelRequested = true;
  }

  public boolean isCanceledDirectProcess() {
    return this.cancelRequested;
  }

  public boolean isProcessing() {
    return(this.isprocessing);
  }

  public void setProcessing(boolean status) {
    this.isprocessing = status;
  }

  private void debug(String processName) {
    System.out.println(processName + "***************************** adPinstanceId: " + this.adPinstanceId + " - directLaunch: " + this.directLaunch + " - cancelRequest: " + this.cancelRequested + " - adClientId: " + this.adClientId);
  }

  public boolean directLaunch(VariablesSecureApp vars, String stradPinstanceId) throws ServletException {
    this.directLaunch = true;
    String strClient = PeriodicBackgroundData.selectClientId(conn, stradPinstanceId);
    if (isProcesable(true, strClient)) {
      this.vars = vars;
      this.adPinstanceId = stradPinstanceId;
      this.directLaunch = true;
      this.cancelRequested=false;
      this.adClientId = strClient;
      this.clearLastLog();
      if (debugMode) debug("directLaunch(vars, " + stradPinstanceId + "): " + strClient);
      return true;
    } else {
      addLog("This task cannot be lauched while the background process is active");
      this.directLaunch = false;
      this.cancelRequested=false;
      setProcessing(false);
      return false;
    }
  }

  public void doPause() throws Exception {
    if (!isSamePID()) throw new Exception("End of thread");
    if (!isDirectProcess() || !isFullTime) Thread.sleep((seconds*1000));
  }

  public boolean canContinue(boolean directProcess, String client) {
    if (!isProcesable(directProcess, client)) {
      cancelRequested=false;
      setProcessing(false);
      return false;
    }
    return true;
  }

  public void destroy(int millis) throws SQLException {
    runner.interrupt();

    try {
      runner.join(millis); 
    } catch(InterruptedException e){} // ignore 
  }

  public void destroy() { 
    try {
      destroy(10000);
    } catch(SQLException e) {}
  }
}
