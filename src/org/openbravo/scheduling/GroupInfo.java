package org.openbravo.scheduling;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.model.ad.ui.ProcessGroup;
import org.openbravo.model.ad.ui.ProcessGroupList;
import org.openbravo.model.ad.ui.ProcessRequest;
import org.openbravo.model.ad.ui.ProcessRun;
import org.openbravo.scheduling.ProcessBundle.Channel;
import org.quartz.SchedulerException;

public class GroupInfo {

  /**
   * String constant id for the Process Group process.
   */
  public static final String processGroupId = "5BD4D2B3313E4C708F0AE29095AF16AD";

  public static final String END = "END";

  private org.openbravo.model.ad.ui.ProcessGroup group;

  private ProcessRequest request;

  private ProcessRun processRun;

  private List<ProcessGroupList> groupList;

  private int actualposition = 0;

  private VariablesSecureApp vars;

  private ConnectionProvider conn;

  private StringBuilder groupLog;

  private Date startGroupTime;

  private Date endGroupTime;

  Logger log4j = Logger.getLogger(GroupInfo.class);

  public GroupInfo(ProcessGroup group, ProcessRequest request, ProcessRun processRun,
      List<ProcessGroupList> groupList, VariablesSecureApp vars, ConnectionProvider conn) {
    super();
    this.group = group;
    this.request = request;
    this.groupList = groupList;
    this.vars = vars;
    this.conn = conn;
    this.processRun = processRun;
  }

  public ProcessRequest getRequest() {
    return request;
  }

  public ProcessRun getProcessRun() {
    return processRun;
  }

  public StringBuilder getLogger() {
    return groupLog;
  }

  public String getLog() {
    String groupLogMessage = this.groupLog.toString();
    groupLogMessage = groupLogMessage + "\n END Process Group: " + group.getName();
    return groupLogMessage;
  }

  public String executeNextProcess() throws SchedulerException, ServletException {
    if (actualposition == 0) {
      groupLog = new StringBuilder();
      groupLog.append(now() + "Process Group: " + group.getName() + " started. \n\n");
      startGroupTime = new Date();
    }
    if (actualposition < groupList.size()) {
      ProcessGroupList processList = groupList.get(actualposition);
      String actualProcessId = processList.getProcess().getId();
      actualposition++;
      groupLog.append(now() + processList.getSequenceNumber() + " Process : "
          + processList.getProcess().getName() + " started succesfully. \n");
      final ProcessBundle firstProcessBundle = new ProcessBundle(actualProcessId, vars,
          Channel.SCHEDULED, request.getClient().getId(), request.getOrganization().getId(),
          request.isSecurityBasedOnRole(), this).init(conn);
      OBScheduler.getInstance().schedule(firstProcessBundle);
      return actualProcessId;
    } else {
      endGroupTime = new Date();
      return END;
    }
  }

  public void logProcess(String result) {
    String resultMessage = "";
    ProcessGroupList processList = groupList.get(actualposition - 1);
    if (result.equals(Process.SUCCESS)) {
      resultMessage = " processed successfully.";
    } else if (result.equals(Process.ERROR)) {
      resultMessage = " FAILED!!!.";
    }
    groupLog.append(now() + processList.getSequenceNumber() + " Process : "
        + processList.getProcess().getName() + resultMessage + "\n");
    groupLog.append("-------- \n");
  }

  public long getDuration() {
    return endGroupTime.getTime() - startGroupTime.getTime();
  }

  private String now() {
    return new Timestamp(System.currentTimeMillis()).toString() + " - ";
  }
}
