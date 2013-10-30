package org.openbravo.utility.cleanup.log;

import java.util.Set;

import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;

import org.hibernate.criterion.Restrictions;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.base.weld.WeldUtils;
import org.openbravo.client.kernel.ComponentProvider;
import org.openbravo.dal.core.DalUtil;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.ad.system.Client;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.scheduling.ProcessBundle;
import org.openbravo.scheduling.ProcessLogger;
import org.openbravo.service.db.DalBaseProcess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogCleanUpProcess extends DalBaseProcess {
  private static final Logger log = LoggerFactory.getLogger(LogCleanUpConfig.class);

  @Override
  protected void doExecute(ProcessBundle bundle) throws Exception {
    ProcessLogger bgLogger = bundle.getLogger();

    BeanManager bm = WeldUtils.getStaticInstanceBeanManager();
    final Set<Bean<?>> beans = bm.getBeans(CleanEntity.class, new ComponentProvider.Selector(
        "Default"));
    Bean<?> bean = beans.iterator().next();
    CleanEntity defaultCleaner = (CleanEntity) bm.getReference(bean, CleanEntity.class,
        bm.createCreationalContext(bean));

    OBContext.setAdminMode(false);

    try {
      VariablesSecureApp vars = bundle.getContext().toVars();

      Client client = OBDal.getInstance().get(Client.class, vars.getClient());
      Organization org = OBDal.getInstance().get(Organization.class, vars.getOrg());

      String logMsg = "Starting log clean up process for Client:" + client.getName()
          + " - Organization:" + org.getName();
      bgLogger.log(logMsg + "\n\n");
      log.debug(logMsg);

      OBCriteria<LogCleanUpConfig> qConfig = OBDal.getInstance().createCriteria(
          LogCleanUpConfig.class);
      qConfig.add(Restrictions.eq(LogCleanUpConfig.PROPERTY_ACTIVE, true));
      for (LogCleanUpConfig config : qConfig.list()) {
        long t = System.currentTimeMillis();
        Entity entity = ModelProvider.getInstance().getEntityByTableId(
            (String) DalUtil.getId(config.getTable()));
        logMsg = "Cleaning up entity " + entity.getName();
        bgLogger.log(logMsg + "\n");
        log.debug(logMsg);

        boolean useDefault = true;

        bm.getBeans(CleanEntity.class, new ComponentProvider.Selector(entity.getName()));
        for (Bean<?> beanCleaner : bm.getBeans(CleanEntity.class, new ComponentProvider.Selector(
            entity.getName()))) {
          useDefault = false;
          CleanEntity cleaner = (CleanEntity) bm.getReference(beanCleaner, CleanEntity.class,
              bm.createCreationalContext(beanCleaner));
          log.debug("Using {} to clean up entity", cleaner, entity);
          cleaner.clean(config, client, org, bgLogger);
        }

        if (useDefault) {
          log.debug("Using default cleaner for entity", entity);
          defaultCleaner.clean(config, client, org, bgLogger);
        }
        logMsg = "Entity " + entity.getName() + " cleaned up in "
            + (System.currentTimeMillis() - t) + "ms";
        bgLogger.log(logMsg + "\n\n");
        log.debug(logMsg);
      }
    } finally {
      OBContext.restorePreviousMode();
    }
  }
}
