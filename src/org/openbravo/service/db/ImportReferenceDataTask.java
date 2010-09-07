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
 * The Initial Developer of the Original Code is Openbravo SLU 
 * All portions are Copyright (C) 2008 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.service.db;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;
import java.util.Vector;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.ddlutils.Platform;
import org.apache.ddlutils.PlatformFactory;
import org.apache.ddlutils.model.Database;
import org.apache.log4j.Logger;
import org.openbravo.base.exception.OBException;
import org.openbravo.dal.service.OBDal;
import org.openbravo.ddlutils.task.DatabaseUtils;
import org.openbravo.ddlutils.util.DBSMOBUtil;

/**
 * Imports client data for the clients defined in the clients parameter from the
 * {@link ReferenceDataTask#REFERENCE_DATA_DIRECTORY} directory.
 */
public class ImportReferenceDataTask extends ReferenceDataTask {
  private static final Logger log = Logger.getLogger(ImportReferenceDataTask.class);
  private Platform platform;
  private Database xmlModel;

  @Override
  public void execute() {
    try {
      disableConstraints();
    } catch (Exception e) {
      log.info("Error disabling check constraint");
    }
    super.execute();
    enableConstraints();
  }

  @Override
  protected void doExecute() {
    final File importDir = getReferenceDataDir();

    for (final File importFile : importDir.listFiles()) {
      if (importFile.isDirectory() || !importFile.getName().endsWith(".xml")) {
        continue;
      }
      log.info("Importing from file " + importFile.getAbsolutePath());

      final ClientImportProcessor importProcessor = new ClientImportProcessor();
      importProcessor.setNewName(null);
      try {
        final InputStreamReader inputStreamReader = new InputStreamReader(new FileInputStream(
            importFile), "UTF-8");
        final ImportResult ir = DataImportService.getInstance().importClientData(importProcessor,
            false, inputStreamReader);
        inputStreamReader.close();

        if (ir.hasErrorOccured()) {
          if (ir.getException() != null) {
            throw new OBException(ir.getException());
          }
          if (ir.getErrorMessages() != null) {
            throw new OBException(ir.getErrorMessages());
          }
        }
      } catch (Exception e) {
        throw new OBException("Exception (" + e.getMessage() + ") while importing from file "
            + importFile, e);
      }
    }
    OBDal.getInstance().commitAndClose();
  }

  private void disableConstraints() throws FileNotFoundException, IOException {
    String obDir = getProject().getBaseDir().toString() + "/../";
    Properties obProp = new Properties();
    obProp.load(new FileInputStream(new File(obDir, "config/Openbravo.properties")));
    // We disable check constraints before inserting reference data
    String driver = obProp.getProperty("bbdd.driver");
    String url = obProp.getProperty("bbdd.rdbms").equals("POSTGRE") ? obProp
        .getProperty("bbdd.url")
        + "/" + obProp.getProperty("bbdd.sid") : obProp.getProperty("bbdd.url");
    String user = obProp.getProperty("bbdd.user");
    String password = obProp.getProperty("bbdd.password");
    BasicDataSource datasource = DBSMOBUtil.getDataSource(driver, url, user, password);

    platform = PlatformFactory.createNewPlatformInstance(datasource);

    Vector<File> dirs = new Vector<File>();
    dirs.add(new File(obDir, "/src-db/database/model/"));
    File modules = new File(obDir, "/modules");

    for (int j = 0; j < modules.listFiles().length; j++) {
      final File dirF = new File(modules.listFiles()[j], "/src-db/database/model/");
      if (dirF.exists()) {
        dirs.add(dirF);
      }
    }
    File[] fileArray = new File[dirs.size()];
    for (int i = 0; i < dirs.size(); i++) {
      fileArray[i] = dirs.get(i);
    }
    xmlModel = DatabaseUtils.readDatabase(fileArray);

    platform.disableCheckConstraints(xmlModel);
  }

  private void enableConstraints() {
    platform.enableCheckConstraints(xmlModel);
  }
}
