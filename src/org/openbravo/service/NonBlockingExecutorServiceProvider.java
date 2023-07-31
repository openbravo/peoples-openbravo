package org.openbravo.service;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openbravo.dal.core.OBContext;
import org.openbravo.erpCommon.businessUtility.Preferences;
import org.openbravo.erpCommon.utility.PropertyException;
import org.openbravo.erpCommon.utility.PropertyNotFoundException;
import org.openbravo.service.importprocess.ImportEntryManager;


/**
 * Singleton that provides an ExecutorService for non-blocking tasks and utility methods for
 * execution
 */
@ApplicationScoped
public class NonBlockingExecutorServiceProvider {
  private static ExecutorService executorService = null;

  private static final Integer DEFAULT_AMOUNT_OF_NON_BLOCKING_THREADS = 10;
  private static final String AMOUNT_OF_NON_BLOCKING_THREADS_PREFERENCE = "amountOfNonBlockingThreads";
  private static final Logger log = LogManager.getLogger();

  static void initializeExecutorService() {
    if (executorService == null) {
      int amountOfThreads = DEFAULT_AMOUNT_OF_NON_BLOCKING_THREADS;
      try {
        // TODO: Maybe client/organization/user/role not required, this seems a system preference
        String amountOfThreadsFromPref = Preferences.getPreferenceValue(
            AMOUNT_OF_NON_BLOCKING_THREADS_PREFERENCE, false,
            OBContext.getOBContext().getCurrentClient(),
            OBContext.getOBContext().getCurrentOrganization(), OBContext.getOBContext().getUser(),
            OBContext.getOBContext().getRole(), null);
        amountOfThreads = Integer.parseInt(amountOfThreadsFromPref);
      } catch (PropertyNotFoundException e) {
        log.info("Preference {} not found, defaulting to {}",
            AMOUNT_OF_NON_BLOCKING_THREADS_PREFERENCE, DEFAULT_AMOUNT_OF_NON_BLOCKING_THREADS);
      } catch (PropertyException e) {
        log.error("Could not retrieve preference {}", AMOUNT_OF_NON_BLOCKING_THREADS_PREFERENCE, e);
      }
      executorService = Executors.newFixedThreadPool(amountOfThreads,
          new ImportEntryManager.DaemonThreadFactory("NonBlocking"));
    }
  }

  public static ExecutorService getExecutorService() {
    if (executorService == null){
      initializeExecutorService();
    }
    return executorService;
  }
}
