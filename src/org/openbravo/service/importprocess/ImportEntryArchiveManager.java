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

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.model.Property;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;

/**
 * Class responsible for moving {@link ImportEntry} objects to the {@link ImportEntryArchive} table.
 * 
 * @author mtaal
 */
@ApplicationScoped
public class ImportEntryArchiveManager {

  private static final Logger log = Logger.getLogger(ImportEntryArchiveManager.class);

  // TODO: make this a preference
  // Archive hourly for now
  private static final long ARCHIVE_INTERVAL = 60000;

  private static ImportEntryArchiveManager instance;

  public static ImportEntryArchiveManager getInstance() {
    return instance;
  }

  @Inject
  @Any
  private Instance<ImportEntryArchivePreProcessor> archiveEntryPreProcessors;

  private ImportEntryArchiveThread archiveThread;
  private ExecutorService executorService;

  public ImportEntryArchiveManager() {
    instance = this;
  }

  public void start() {
    executorService = Executors.newSingleThreadExecutor();
    archiveThread = new ImportEntryArchiveThread(this);
    executorService.execute(archiveThread);
  }

  public void shutdown() {
    log.debug("Shutting down Import Entry Archive Framework");
    executorService.shutdown();
  }

  private static class ImportEntryArchiveThread implements Runnable {

    private final ImportEntryArchiveManager manager;

    ImportEntryArchiveThread(ImportEntryArchiveManager manager) {
      this.manager = manager;
    }

    @Override
    public void run() {

      // make ourselves an admin
      OBContext.setOBContext("0", "0", "0", "0");
      Date lastStored = null;
      while (true) {
        try {
          boolean dataProcessed = false;
          try {
            // stored is used in the whereclause to make sure that the system will continue
            // processing next records if there is one failing or giving issues, so the failing
            // is skipped in the next cycle
            String additionalClause = "";
            if (lastStored != null) {
              additionalClause = " AND " + ImportEntry.PROPERTY_STORED + ">:stored";
            }
            OBQuery<ImportEntry> entriesQry = OBDal.getInstance().createQuery(
                ImportEntry.class,
                ImportEntry.PROPERTY_IMPORTSTATUS + "='Processed' " + additionalClause
                    + " order by " + ImportEntry.PROPERTY_STORED);
            entriesQry.setFilterOnReadableClients(false);
            entriesQry.setFilterOnReadableOrganization(false);
            if (lastStored != null) {
              entriesQry.setNamedParameter("stored", lastStored);
            }
            entriesQry.setMaxResult(1000);

            log.debug("Querying for entries to archive");

            // do a try catch block here
            final List<ImportEntry> entries = entriesQry.list();
            log.debug("Found " + entries.size() + " import entries");
            for (ImportEntry importEntry : entries) {
              dataProcessed = true;
              lastStored = importEntry.getStored();

              ImportEntryArchive archiveEntry = createArchiveEntry(importEntry);

              for (Iterator<? extends Object> procIter = manager.archiveEntryPreProcessors
                  .iterator(); procIter.hasNext();) {
                ImportEntryArchivePreProcessor processor = (ImportEntryArchivePreProcessor) procIter
                    .next();
                processor.beforeArchive(importEntry, archiveEntry);

              }

              log.debug("Processed one entry");

              OBDal.getInstance().remove(importEntry);
              OBDal.getInstance().save(archiveEntry);
            }
            // commit in batches of 1000 records
            OBDal.getInstance().commitAndClose();
          } catch (Throwable t) {
            ImportProcessUtils.logError(log, t);
          }

          // nothing to do in last cycle wait one hour
          if (!dataProcessed) {
            log.debug("waiting");
            lastStored = null;
            try {
              Thread.sleep(ARCHIVE_INTERVAL);
            } catch (Exception ignored) {
            }
          }
        } catch (Throwable t) {
          log.error(t.getMessage(), t);
        }
      }
    }

    private ImportEntryArchive createArchiveEntry(ImportEntry importEntry) {
      ImportEntryArchive archiveEntry = OBProvider.getInstance().get(ImportEntryArchive.class);
      Entity entryEntity = ModelProvider.getInstance().getEntity(ImportEntry.ENTITY_NAME);
      Entity archiveEntity = ModelProvider.getInstance().getEntity(ImportEntryArchive.ENTITY_NAME);
      // copy properties with the same name
      for (Property sourceProperty : entryEntity.getProperties()) {
        // ignore these ones
        if (sourceProperty.isOneToMany() || sourceProperty.isId()
            || !archiveEntity.hasProperty(sourceProperty.getName())) {
          continue;
        }
        Property targetProperty = archiveEntity.getProperty(sourceProperty.getName());
        // should be the same type
        if (targetProperty.getDomainType().getClass() != sourceProperty.getDomainType().getClass()) {
          continue;
        }
        archiveEntry.set(targetProperty.getName(), importEntry.getValue(sourceProperty.getName()));
      }
      return archiveEntry;
    }
  }
}
