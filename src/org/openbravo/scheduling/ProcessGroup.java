package org.openbravo.scheduling;

import java.util.List;

import org.hibernate.Hibernate;
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.model.ad.ui.ProcessGroupList;
import org.openbravo.model.ad.ui.ProcessRequest;
import org.openbravo.service.db.DalBaseProcess;

public class ProcessGroup extends DalBaseProcess {

  @Override
  protected void doExecute(ProcessBundle bundle) throws Exception {

    ProcessLogger log = bundle.getLogger();
    ConnectionProvider conn = bundle.getConnection();
    final VariablesSecureApp vars = bundle.getContext().toVars();
    final ProcessRequest processRequest = OBDal.getInstance().get(ProcessRequest.class,
        bundle.getProcessRequestId());
    final org.openbravo.model.ad.ui.ProcessGroup group = processRequest.getProcessGroup();
    log.logln("Process Group: " + group.getName());

    // Execute first job
    if (group.getProcessGroupListList().size() == 0) {
      log.logln("No processes on the group: " + group.getName());
    } else {
      OBCriteria<ProcessGroupList> processListcri = OBDal.getInstance().createCriteria(
          ProcessGroupList.class);
      processListcri.add(Restrictions.eq(ProcessGroupList.PROPERTY_PROCESSGROUP, group));
      processListcri.addOrderBy(ProcessGroupList.PROPERTY_SEQUENCENUMBER, true);
      List<ProcessGroupList> processList = processListcri.list();

      // Since Hibernate lazyloads objects and the subprocesses may access these processes as well,
      // the following is necessary to pre-load the objects and "solidify" them
      for (ProcessGroupList prolist : processList) {
        Hibernate.initialize(prolist);
        Hibernate.initialize(prolist.getProcess());
      }

      GroupInfo groupInfo = new GroupInfo(group, processRequest, processList, vars, conn);
      groupInfo.executeNextProcess();

    }

  }

}
