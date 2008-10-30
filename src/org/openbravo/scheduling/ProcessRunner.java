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
package org.openbravo.scheduling;

import static org.openbravo.scheduling.Process.*;

import java.util.Date;

import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.scheduling.ProcessRequestData;
import org.openbravo.scheduling.ProcessRunData;
import org.openbravo.erpCommon.utility.SequenceIdData;

/**
 * @author awolski
 *
 */
public class ProcessRunner {
  
  static Logger log = Logger.getLogger(ProcessRunner.class);
  
  private ProcessBundle bundle;
  
  public ProcessRunner(ProcessBundle bundle) {
    this.bundle = bundle;
  }
  
  /**
   * @param process
   * @param vars
   * @param conn
   * @throws ServletException
   */
  public String execute(ConnectionProvider conn) throws ServletException {
    
    Process process = null;
    try {
      process = bundle.getProcessClass().newInstance();
      process.initialize(bundle);
      
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      throw new ServletException(e.getMessage(), e);
    } 
    String requestId = SequenceIdData.getUUID();
    String status = null;
    
    long startTime = System.currentTimeMillis();
    long endTime = startTime;
    
    try {
      log.debug("Calling execute on process " + requestId);
      process.execute(bundle);
      endTime = System.currentTimeMillis();
      status = SUCCESS;
    
    } catch (Exception e) {
      e.printStackTrace();
      endTime = System.currentTimeMillis();
      status = ERROR;
      log.info("Process " + requestId + " threw an Exception: ", e);
    }
    ProcessContext ctx = bundle.getContext();
    ProcessRequestData.insert(conn, ctx.getOrganization(), ctx.getClient(), 
        ctx.getUser(), ctx.getUser(), requestId, bundle.getProcessId(), ctx.getUser(), 
        status, "Direct", ctx.toString(), "",
        OBScheduler.format(new Date(startTime)), null, OBScheduler.format(new Date(startTime)));
    
    String duration = ProcessMonitor.getDuration(endTime - startTime);
    String executionId = SequenceIdData.getUUID();
    ProcessRunData.insert(conn, ctx.getOrganization(), ctx.getClient(), 
        ctx.getUser(), ctx.getUser(), ctx.getUser(), executionId, 
        status, OBScheduler.format(new Date(startTime)), duration, bundle.getLog(), requestId);
    
    return executionId;
  }
  
}
