package org.openbravo.scheduling;

import java.util.List;

import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.model.ad.ui.ProcessGroup;
import org.openbravo.model.ad.ui.ProcessGroupList;
import org.openbravo.model.ad.ui.ProcessRequest;
import org.openbravo.scheduling.ProcessBundle.Channel;
import org.quartz.SchedulerException;

public class GroupInfo {

  private org.openbravo.model.ad.ui.ProcessGroup group;

  private ProcessRequest request;

  private List<ProcessGroupList> groupList;

  private VariablesSecureApp vars;

  private int actualposition = 0;

  private ConnectionProvider conn;

  Logger log4j = Logger.getLogger(GroupInfo.class);

  public GroupInfo(ProcessGroup group, ProcessRequest request, List<ProcessGroupList> groupList,
      VariablesSecureApp vars, ConnectionProvider conn) {
    super();
    this.group = group;
    this.request = request;
    this.groupList = groupList;
    this.vars = vars;
    this.conn = conn;
  }

  public ProcessRequest getRequest() {
    return request;
  }

  public String executeNextProcess() throws SchedulerException, ServletException {
    if (actualposition < groupList.size()) {
      String actualProcessId = groupList.get(actualposition).getProcess().getId();
      actualposition++;
      final ProcessBundle firstProcessBundle = new ProcessBundle(actualProcessId, vars,
          Channel.SCHEDULED, request.getClient().getId(), request.getOrganization().getId(),
          request.isSecurityBasedOnRole(), this).init(conn);
      OBScheduler.getInstance().schedule(firstProcessBundle);
      return "OK";
    } else {
      return "END";
    }

  }

}
