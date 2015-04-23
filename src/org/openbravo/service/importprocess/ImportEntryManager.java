/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.1  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html 
 * Software distributed under the License  is  distributed  on  an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific  language  governing  rights  and  limitations
 * under the License. 
 * The Original Code is Openbravo ERP. 
 * The Initial Developer of the Original Code is Openbravo SLU 
 * All portions are Copyright (C) 2015 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.service.importprocess;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.core.SessionHandler;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;

/**
 * This class is the main manager for performing multi-threaded and parallel import of data from the
 * {@link ImportEntry} entity/table. The {@link ImportEntryManager} is a
 * singleton/ApplicationScoped.
 * 
 * {@link ImportEntry} records are created by for example data synchronization processes. For
 * creating a new {@link ImportEntry} preferably the
 * {@link #createImportEntry(String, String, String)} method should be used. This method also takes
 * care of calling all the relevant {@link ImportEntryPreProcessor} instances. As the
 * {@link ImportEntryManager} is a singleton/applicationscoped class it should preferably be
 * obtained through Weld.
 * 
 * After creating a new {@link ImportEntry} and committing the transaction the creator of the
 * {@link ImportEntry} should preferably call the method {@link #notifyNewImportEntryCreated()}.
 * This to wake up the {@link ImportEntryManagerThread} to process the new entry.
 * 
 * The {@link ImportEntryManager} runs a thread (the {@link ImportEntryManagerThread}) which
 * periodically queries if there are {@link ImportEntry} records in state 'Initial'. Any
 * {@link ImportEntry} with status 'Initial' is to be processed. The
 * {@link ImportEntryManagerThread} is started when the application starts and is shutdown when the
 * Tomcat application stops, see the {@link #start()} and {@link #shutdown()} methods which are
 * called from the {@link ImportProcessContextListener}.
 * 
 * As mentioned above, the {@link ImportEntryManagerThread} periodically checks if there are
 * {@link ImportEntry} records in state 'Initial'. This thread is also notified when a new
 * {@link ImportEntry} is created. If there are no notifications or {@link ImportEntry} records in
 * state 'Initial', then the thread waits for a preset amount of time before querying the
 * {@link ImportEntry} table again. This notification and waiting is managed through the
 * {@link #notifyNewImportEntryCreated()} and {@link ImportEntryManagerThread#doNotify()} and
 * {@link ImportEntryManagerThread#doWait()} methods. This mechanism uses a monitor object. See here
 * for more information:
 * http://javarevisited.blogspot.nl/2011/05/wait-notify-and-notifyall-in-java.html
 * 
 * When the {@link ImportEntryManagerThread} retrieves an {@link ImportEntry} instance in state
 * 'Initial' then it tries to find an {@link ImportEntryProcessor} which can handle this instance.
 * The right {@link ImportEntryProcessor} is found by using the {@link ImportEntryQualifier} and
 * Weld selections.
 * 
 * The {@link ImportEntryProcessor#handleImportEntry(ImportEntry)} method gets the
 * {@link ImportEntry} and processes it.
 * 
 * As the {@link ImportEntryManagerThread} runs periodically and the processing of
 * {@link ImportEntry} instances can take a long it is possible that an ImportEntry is again
 * 'offered' to the {@link ImportEntryProcessor} for processing. The {@link ImportEntryProcessor}
 * should handle this case robustly.
 * 
 * For more information see the {@link ImportEntryProcessor}.
 * 
 * This class also provides methods for error handling and result processing:
 * {@link #setImportEntryProcessed(String)}, {@link #setImportEntryError(String, Throwable)},
 * {@link #setImportEntryErrorIndependent(String, Throwable)}.
 * 
 * @author mtaal
 *
 */
@ApplicationScoped
public class ImportEntryManager {

  private static final Logger log = Logger.getLogger(ImportEntryManager.class);

  private static ImportEntryManager instance;

  public static ImportEntryManager getInstance() {
    return instance;
  }

  @Inject
  @Any
  private Instance<ImportEntryPreProcessor> entryPreProcessors;

  @Inject
  @Any
  private Instance<ImportEntryProcessor> entryProcessors;

  private ImportEntryManagerThread managerThread;
  private ExecutorService executorService;

  private Map<String, ImportEntryProcessor> importEntryProcessors = new HashMap<String, ImportEntryProcessor>();

  private Map<String, ImportStatistics> stats = new HashMap<String, ImportEntryManager.ImportStatistics>();

  // TODO: make this a preference
  private long managerWaitTime = 60000;

  public ImportEntryManager() {
    instance = this;
  }

  public void start() {
    log.debug("Starting Import Entry Framework");
    executorService = Executors.newSingleThreadExecutor();
    // passing ourselves as we have the Weld injected code
    managerThread = new ImportEntryManagerThread(this);
    executorService.execute(managerThread);
  }

  /**
   * Shutdown all the threads being by the import framework
   */
  public void shutdown() {
    log.debug("Shutting down Import Entry Framework");
    executorService.shutdown();
    for (ImportEntryProcessor importEntryProcessor : importEntryProcessors.values()) {
      importEntryProcessor.shutdown();
    }
  }

  /**
   * Creates and saves the import entry, calls the
   * {@link ImportQueueEntryProcessor#beforeCreate(ImportEntry)} on the
   * {@link ImportQueueEntryProcessor} instances.
   * 
   * Note will commit the session/connection using {@link OBDal#commitAndClose()}
   * 
   * @param json
   */
  public void createImportEntry(String id, String typeOfData, String data) {
    OBDal.getInstance().flush();
    OBContext.setAdminMode(false);
    try {
      // check if it is not there already
      final Query qry = SessionHandler.getInstance().getSession()
          .createQuery("select id from " + ImportEntry.ENTITY_NAME + " where id=:id");
      qry.setParameter("id", id);
      if (!qry.list().isEmpty()) {
        // already exists, ignore
        return;
      }

      ImportEntry importEntry = OBProvider.getInstance().get(ImportEntry.class);
      importEntry.setId(id);
      importEntry.setNewOBObject(true);
      importEntry.setImportStatus("Initial");
      importEntry.setStored(new Date());
      importEntry.setImported(null);
      importEntry.setTypeofdata(typeOfData);
      importEntry.setData(data);

      for (Iterator<? extends Object> procIter = entryPreProcessors.iterator(); procIter.hasNext();) {
        ImportEntryPreProcessor processor = (ImportEntryPreProcessor) procIter.next();
        processor.beforeCreate(importEntry);
      }
      OBDal.getInstance().save(importEntry);
      OBDal.getInstance().commitAndClose();

      notifyNewImportEntryCreated();

    } finally {
      OBContext.restorePreviousMode();
    }
  }

  public void reportStats(String typeOfData, long timeForEntry) {
    ImportStatistics importStatistics = stats.get(typeOfData);
    if (importStatistics == null) {
      createStatsEntry(typeOfData);
      importStatistics = stats.get(typeOfData);
    }
    importStatistics.addTiming(timeForEntry);
    if ((importStatistics.getCnt() % 100) == 0) {
      importStatistics.log();
    }
  }

  private void createStatsEntry(String typeOfData) {
    if (stats.containsKey(typeOfData)) {
      return;
    }
    ImportStatistics importStatistics = new ImportStatistics();
    importStatistics.setTypeOfData(typeOfData);
    stats.put(typeOfData, importStatistics);
  }

  /**
   * Is used to tell the import entry manager that a new entry was created in the import entry
   * table, so it can go process it immediately.
   */
  public void notifyNewImportEntryCreated() {
    managerThread.doNotify();
  }

  private void handleImportEntry(ImportEntry importEntry) {

    try {
      ImportEntryProcessor entryProcessor = getImportEntryProcessor(importEntry.getTypeofdata());
      if (entryProcessor == null) {
        log.warn("No import entry processor defined for type of data "
            + importEntry.getTypeofdata() + " with json " + importEntry.getData() + " imported on "
            + importEntry.getImported() + " by " + importEntry.getCreatedBy());
      } else {
        entryProcessor.handleImportEntry(importEntry);
      }
    } catch (Throwable t) {
      handleImportError(importEntry, t);
    }
  }

  // somehow cache the import entry processors, Weld seems to create many instances
  // caching is probably also faster
  private ImportEntryProcessor getImportEntryProcessor(String qualifier) {
    ImportEntryProcessor importEntryProcessor = importEntryProcessors.get(qualifier);
    if (importEntryProcessor == null) {
      importEntryProcessor = entryProcessors.select(new ImportEntryProcessorSelector(qualifier))
          .get();
      if (importEntryProcessor != null) {
        importEntryProcessors.put(qualifier, importEntryProcessor);
      } else {
        // caller should handle it
        return null;
      }
    }
    return importEntryProcessor;
  }

  public void handleImportError(ImportEntry importEntry, Throwable t) {
    importEntry.setImportStatus("Error");
    importEntry.setErrorinfo(ImportProcessUtils.getErrorMessage(t));
    OBDal.getInstance().save(importEntry);
  }

  /**
   * Set the ImportEntry to status Processed in the same transaction as the caller.
   */
  public void setImportEntryProcessed(String importEntryId) {
    ImportEntry importEntry = OBDal.getInstance().get(ImportEntry.class, importEntryId);
    if (importEntry != null && !"Processed".equals(importEntry.getImportStatus())) {
      importEntry.setImportStatus("Processed");
      importEntry.setImported(new Date());
      OBDal.getInstance().save(importEntry);
    }
  }

  /**
   * Set the ImportEntry to status Error in the same transaction as the caller.
   */
  public void setImportEntryError(String importEntryId, Throwable t) {
    ImportEntry importEntry = OBDal.getInstance().get(ImportEntry.class, importEntryId);
    if (importEntry != null && !"Processed".equals(importEntry.getImportStatus())) {
      importEntry.setImportStatus("Error");
      importEntry.setErrorinfo(ImportProcessUtils.getErrorMessage(t));
      OBDal.getInstance().save(importEntry);
    }
  }

  /**
   * Sets an {@link ImportEntry} in status Error but does this in its own transaction so not
   * together with the original data. This is relevant when the previous transaction which tried to
   * import the data fails.
   */
  public void setImportEntryErrorIndependent(String importEntryId, Throwable t) {
    OBDal.getInstance().rollbackAndClose();
    OBContext.setOBContext("0", "0", "0", "0");
    try {
      OBContext.setAdminMode();
      ImportEntry importEntry = OBDal.getInstance().get(ImportEntry.class, importEntryId);
      if (importEntry != null && !"Processed".equals(importEntry.getImportStatus())) {
        importEntry.setImportStatus("Error");
        importEntry.setErrorinfo(ImportProcessUtils.getErrorMessage(t));
        OBDal.getInstance().save(importEntry);
        OBDal.getInstance().commitAndClose();
      }
    } finally {
      OBContext.restorePreviousMode();
      OBContext.setOBContext((OBContext) null);
    }
  }

  private static class ImportEntryManagerThread implements Runnable {

    private final ImportEntryManager manager;

    private Object monitorObject = new Object();
    private boolean wasPingedInParallel = false;

    ImportEntryManagerThread(ImportEntryManager manager) {
      this.manager = manager;
    }

    // http://javarevisited.blogspot.nl/2011/05/wait-notify-and-notifyall-in-java.html
    // note the doNotify and doWait methods should not be synchronized themselves
    // the synchronization should happen on the monitorObject
    private void doNotify() {
      synchronized (monitorObject) {
        wasPingedInParallel = true;
        monitorObject.notify();
      }
    }

    private void doWait() {
      synchronized (monitorObject) {
        try {
          if (!wasPingedInParallel) {
            log.debug("Waiting for next cycle or new import entries");
            monitorObject.wait(10 * manager.managerWaitTime);
            log.debug("Woken");
          }
          wasPingedInParallel = false;
        } catch (InterruptedException ignore) {
        }
      }
      // thread can be woken by new import entries
      // wait 5 seconds for more importentries to arrive
      // before processing them
      try {
        Thread.sleep(5000);
      } catch (InterruptedException ignore) {
      }
    }

    @Override
    public void run() {

      // don't start right away at startup, give the system time to
      // really start
      log.debug("Started, first sleep " + manager.managerWaitTime);
      try {
        Thread.sleep(manager.managerWaitTime);
      } catch (Exception ignored) {
      }
      log.debug("Run loop started");

      // make ourselves an admin
      OBContext.setOBContext("0", "0", "0", "0");
      while (true) {
        try {
          boolean dataProcessed = false;
          try {
            OBQuery<ImportEntry> entriesQry = OBDal.getInstance().createQuery(
                ImportEntry.class,
                ImportEntry.PROPERTY_IMPORTSTATUS + "='Initial' order by "
                    + ImportEntry.PROPERTY_STORED);
            entriesQry.setFilterOnReadableClients(false);
            entriesQry.setFilterOnReadableOrganization(false);

            // do a try catch block here
            try {
              final List<ImportEntry> entries = entriesQry.list();
              log.debug("Found " + entries.size() + " import entries");
              for (ImportEntry importEntry : entries) {
                dataProcessed = true;
                manager.handleImportEntry(importEntry);
              }
            } catch (Throwable t) {
              log.error(t.getMessage(), t);
            }
          } finally {
            OBDal.getInstance().commitAndClose();
          }

          // always wait 1 cycle if data was processed, so that
          // the data can be processed in parallel and the system
          // does not continuously read/take the same import entries
          // which are already being processed
          if (dataProcessed) {
            Thread.sleep(manager.managerWaitTime);
          }

          // now wait for new ones to arrive or check after a certain
          // amount of time
          doWait();

        } catch (Throwable t) {
          log.error(t.getMessage(), t);

          // wait otherwise the loop goes wild
          try {
            Thread.sleep(5 * manager.managerWaitTime);
          } catch (Exception ignored) {
          }
        }
      }
    }
  }

  @javax.inject.Qualifier
  @Retention(RetentionPolicy.RUNTIME)
  @Target({ ElementType.TYPE })
  public @interface ImportEntryQualifier {
    String entity();
  }

  @SuppressWarnings("all")
  public static class ImportEntryProcessorSelector extends AnnotationLiteral<ImportEntryQualifier>
      implements ImportEntryQualifier {
    private static final long serialVersionUID = 1L;

    final String entity;

    public ImportEntryProcessorSelector(String entity) {
      this.entity = entity;
    }

    public String entity() {
      return entity;
    }
  }

  private static class ImportStatistics {
    private String typeOfData;
    private long cnt;
    private long totalTime;

    public void setTypeOfData(String typeOfData) {
      this.typeOfData = typeOfData;
    }

    public long getCnt() {
      return cnt;
    }

    public synchronized void addTiming(long timeForEntry) {
      cnt++;
      totalTime += timeForEntry;
    }

    public synchronized void log() {
      log.info("Timings for " + typeOfData + " cnt: " + cnt + " avg millis: " + (totalTime / cnt));
      System.err.println("Timings for " + typeOfData + " cnt: " + cnt + " avg millis: "
          + (totalTime / cnt));
    }
  }
}
