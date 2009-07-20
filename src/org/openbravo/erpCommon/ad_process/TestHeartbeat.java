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
 * All portions are Copyright (C) 2008-2009 Openbravo SL 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.erpCommon.ad_process;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.criterion.Expression;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.ad.system.SystemInformation;
import org.openbravo.model.ad.ui.Process;
import org.openbravo.model.ad.ui.ProcessRequest;
import org.openbravo.model.ad.ui.ProcessRun;
import org.openbravo.scheduling.OBScheduler;
import org.openbravo.scheduling.ProcessBundle;
import org.openbravo.scheduling.ProcessContext;
import org.openbravo.scheduling.ProcessRunner;
import org.openbravo.scheduling.ProcessBundle.Channel;

public class TestHeartbeat extends HttpSecureAppServlet {

  private static final long serialVersionUID = 1L;
  private static final String HB_Process_ID = "1005800000";
  private static final String SystemInfomation_ID = "0";
  private static final String WEEKLY = "5";
  private static final String SCHEDULE = "S";

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    VariablesSecureApp vars = new VariablesSecureApp(request);
    final boolean isHearbeatEnabled = vars.getRequiredStringParameter("inptestproxy").equals("Y");

    if (isHearbeatEnabled) { // Disable
      try {

        // Getting the process
        final Process HBProcess = OBDal.getInstance().get(Process.class, HB_Process_ID);

        // Deactivating the process at SystemInfo
        final SystemInformation sysInfo = OBDal.getInstance().get(SystemInformation.class,
            SystemInfomation_ID);
        sysInfo.setEnableHeartbeat(false);
        sysInfo.setTestHeartbeat("N");
        OBDal.getInstance().save(sysInfo);

        // Un-scheduling the process
        final OBCriteria<ProcessRequest> prCriteria = OBDal.getInstance().createCriteria(
            ProcessRequest.class);
        prCriteria.add(Expression.eq(ProcessRequest.PROPERTY_PROCESS, HBProcess));
        prCriteria
            .add(Expression.eq(ProcessRequest.PROPERTY_CHANNEL, Channel.SCHEDULED.toString()));
        final List<ProcessRequest> requestList = prCriteria.list();

        if (requestList.size() != 0) {

          final ProcessRequest pr = requestList.get(0);

          OBDal.getInstance().save(pr);
          OBDal.getInstance().commitAndClose();

          final ProcessBundle bundle = ProcessBundle.request(pr.getId(), vars, this);

          OBScheduler.getInstance().unschedule(pr.getId(), bundle.getContext());
        }

        String msg = Utility.messageBD(this, "HB_SUCCESS", vars.getLanguage());
        advisePopUp(request, response, "SUCCESS", "Heartbeat Configuration", msg);

      } catch (Exception e) {
        log4j.error(e.getMessage(), e);
        advisePopUp(request, response, "ERROR", "Heartbeat Configuration", e.getMessage());
      }

    } else { // Enable

      try {

        // Activating the process
        final Process HBProcess = OBDal.getInstance().get(Process.class, HB_Process_ID);
        HBProcess.setActive(true);
        OBDal.getInstance().save(HBProcess);

        // Activating the process at SystemInfo
        final SystemInformation sysInfo = OBDal.getInstance().get(SystemInformation.class,
            SystemInfomation_ID);
        sysInfo.setEnableHeartbeat(true);
        sysInfo.setTestHeartbeat("Y");
        OBDal.getInstance().save(sysInfo);

        // Committing because sqlc uses a different connection
        OBDal.getInstance().commitAndClose();

        // Making the first beat
        ProcessBundle bundle = new ProcessBundle(HB_Process_ID, vars).init(this);
        final String beatExecutionId = new ProcessRunner(bundle).execute(this);

        // Getting beat result
        final OBCriteria<ProcessRun> runCriteria = OBDal.getInstance().createCriteria(
            ProcessRun.class);
        runCriteria.add(Expression.eq(ProcessRun.PROPERTY_ID, beatExecutionId));
        final List<ProcessRun> prl = runCriteria.list();
        final ProcessRun processRunResult = prl.get(0);

        if (processRunResult.getStatus().equals("ERR")) {
          advisePopUp(request, response, "ERROR", "Heartbeat Configuration");
          return;
        }

        // Scheduling the process
        final OBCriteria<ProcessRequest> prCriteria = OBDal.getInstance().createCriteria(
            ProcessRequest.class);
        prCriteria.add(Expression.eq(ProcessRequest.PROPERTY_PROCESS, HBProcess));
        prCriteria
            .add(Expression.eq(ProcessRequest.PROPERTY_CHANNEL, Channel.SCHEDULED.toString()));
        final List<ProcessRequest> requestList = prCriteria.list();

        ProcessRequest pr = null;

        if (requestList.size() == 0) {
          pr = OBProvider.getInstance().get(ProcessRequest.class);
          pr.setProcess(HBProcess);
          pr.setActive(true);
          pr.setSecurityBasedOnRole(true);
          pr.setFrequency(WEEKLY);
          pr.setFriday(true);
          pr.setTiming(SCHEDULE);
          final ProcessContext context = new ProcessContext(vars);
          pr.setOpenbravoContext(context.toString());

        } else {
          pr = requestList.get(0);
        }

        OBDal.getInstance().save(pr);

        OBDal.getInstance().commitAndClose();

        final ProcessBundle bundle2 = ProcessBundle.request(pr.getId(), vars, this);
        if (requestList.size() == 0) {
          OBScheduler.getInstance().schedule(pr.getId(), bundle2);
        } else {
          OBScheduler.getInstance().reschedule(pr.getId(), bundle2);
        }

        String msg = Utility.messageBD(this, "HB_SUCCESS", vars.getLanguage());
        advisePopUp(request, response, "SUCCESS", "Heartbeat Configuration", msg);

      } catch (Exception e) {
        log4j.error(e.getMessage(), e);
        advisePopUp(request, response, "ERROR", "Heartbeat Configuration", e.getMessage());
      }
    }
  }
}
