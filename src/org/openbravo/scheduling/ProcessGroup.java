package org.openbravo.scheduling;

import org.openbravo.service.db.DalBaseProcess;

public class ProcessGroup extends DalBaseProcess {

  @Override
  protected void doExecute(ProcessBundle bundle) throws Exception {

    ProcessLogger log = bundle.getLogger();

    log.logln("Process for groups");

  }

}
