package org.openbravo.client.application;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.criterion.Expression;
import org.openbravo.dal.core.DalInitializingTask;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBDao;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.model.ad.module.Module;
import org.openbravo.model.ad.ui.Window;

public class WindowsInClassicModeTask extends DalInitializingTask {
  private static final Logger log = Logger.getLogger(WindowsInClassicModeTask.class);

  @Override
  protected void doExecute() {
    OBQuery<Module> modules = OBDal.getInstance().createQuery(Module.class, "");
    for (Module module : modules.list()) {
      List<String> classicWindowMessages = new ArrayList<String>();
      OBCriteria<Window> windowsOfModule = OBDao.getFilteredCriteria(Window.class, Expression.eq(
          Window.PROPERTY_MODULE, module));
      for (Window window : windowsOfModule.list()) {
        ApplicationUtils.showWindowInClassicMode(window, classicWindowMessages);
      }
      if (classicWindowMessages.size() > 0) {
        log.info("Module: " + module.getName());
        log.info("The following windows will be shown in classic mode:");
        for (String message : classicWindowMessages) {
          log.info("  " + message);
        }
      }
    }
    log.info("The rest of the windows will be shown in new mode.");
  }
}
