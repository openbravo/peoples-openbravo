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
 * The Initial Developer of the Original Code is Openbravo SL 
 * All portions are Copyright (C) 2008-2009 Openbravo SL 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.erpCommon.modules;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.servlet.ServletException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.ddlutils.io.DataReader;
import org.apache.ddlutils.io.DataToArraySink;
import org.apache.ddlutils.io.DatabaseDataIO;
import org.apache.ddlutils.model.Database;
import org.apache.log4j.Logger;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.ddlutils.task.DatabaseUtils;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.erpCommon.utility.Zip;
import org.openbravo.services.webservice.Module;
import org.openbravo.services.webservice.ModuleDependency;
import org.openbravo.services.webservice.ModuleInstallDetail;
import org.openbravo.services.webservice.SimpleModule;
import org.openbravo.services.webservice.WebServiceImpl;
import org.openbravo.services.webservice.WebServiceImplServiceLocator;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * ImportModule is able to install modules.
 * 
 * This is done in two steps: -First check if it is possible to install and receive the latest
 * installable modules -Second install them
 * 
 * These two processes are callable independently in order to do it from UI and show messages and
 * wait for confirmation after first one.
 * 
 * It is possible to do the whole installation remotely pulling for the modules to install from the
 * central repository or locally, installing everything from the obx file (which can be passed as an
 * InputStream or as a String file name)
 * 
 */
public class ImportModule {
  static ConnectionProvider pool;
  static Logger log4j = Logger.getLogger(ImportModule.class);
  private String obDir;
  private Database db;
  private boolean installLocally = true;
  private boolean antInstall = false;
  private boolean force = false; // force installation though dependencies are
  // not satisfied
  private Module[] modulesToInstall = null;
  private Module[] modulesToUpdate = null;
  private StringBuffer log = new StringBuffer();
  private int logLevel = 0;
  VariablesSecureApp vars;

  public static final int MSG_SUCCESS = 0;
  public static final int MSG_WARN = 1;
  public static final int MSG_ERROR = 2;

  OBError errors = null;
  Vector<DynaBean> dynModulesToInstall = new Vector<DynaBean>();
  Vector<DynaBean> dynModulesToUpdate = new Vector<DynaBean>();
  Vector<DynaBean> dependencies = new Vector<DynaBean>();
  Vector<DynaBean> dbprefix = new Vector<DynaBean>();

  boolean checked;

  /**
   * Initializes a new ImportModule object, it reads from obdir directory the database model to be
   * able to read the xml information within the obx file.
   * 
   * @param obdir
   *          base directory for the application
   * @param _vars
   *          VariablesSecureApp that will be used to parse messages, if null they will not be
   *          parsed.
   */
  public ImportModule(ConnectionProvider conn, String obdir, VariablesSecureApp _vars) {
    vars = _vars;
    obDir = obdir;
    pool = conn;
    final File[] f = new File[3];
    f[0] = new File(obDir + "/src-db/database/model/tables/AD_MODULE.xml");
    f[1] = new File(obDir + "/src-db/database/model/tables/AD_MODULE_DEPENDENCY.xml");
    f[2] = new File(obDir + "/src-db/database/model/tables/AD_MODULE_DBPREFIX.xml");

    db = DatabaseUtils.readDatabaseNoInit(f);
  }

  /**
   * Check the dependencies for a file name. See {@link #checkDependenciesId(String[], String[])}.
   */
  public boolean checkDependenciesFileName(String fileName) throws Exception {
    final File file = new File(fileName);
    if (!file.exists())
      throw new Exception("File " + fileName + " do not exist!");
    return checkDependenciesFile(new FileInputStream(file));
  }

  /**
   * Checks whether the given .obx InputStream contains an update to an already installed version.
   * 
   * @param is
   *          an InputStream to the module .obx file
   * @return true if the .obx represents an update to the module
   * @throws Exception
   *           if an error occurs performing the comparison
   */
  public boolean isModuleUpdate(InputStream is) throws Exception {

    boolean isUpdate = false;
    final Vector<DynaBean> modulesInObx = new Vector<DynaBean>();
    getModulesFromObx(modulesInObx, dependencies, dbprefix, is);

    for (final DynaBean module : modulesInObx) {

      String moduleId = (String) module.get("AD_MODULE_ID");
      String moduleName = (String) module.get("NAME");
      String version = (String) module.get("VERSION");

      if (ImportModuleData.moduleInstalled(pool, moduleId)) {
        String installedVersion = ImportModuleData.selectVersion(pool, moduleId);
        VersionUtility.VersionComparator comparator = new VersionUtility.VersionComparator();
        if (comparator.compare(version, installedVersion) > 0) {
          isUpdate = true;
        } else {
          addLog(moduleName + " " + version + " is not an update to "
              + " already installed version " + installedVersion, MSG_WARN);
        }
      } else {
        return true;
      }
    }
    return isUpdate;
  }

  /**
   * Check the dependencies for a file. See {@link #checkDependenciesId(String[], String[])}.
   */
  public boolean checkDependenciesFile(InputStream file) throws Exception {
    if (installLocally) {
      final Vector<DynaBean> modulesInObx = new Vector<DynaBean>();
      getModulesFromObx(modulesInObx, dependencies, dbprefix, file);

      for (final DynaBean module : modulesInObx) {

        String moduleId = (String) module.get("AD_MODULE_ID");
        String version = (String) module.get("VERSION");

        if (ImportModuleData.moduleInstalled(pool, moduleId)) {
          String installedVersion = ImportModuleData.selectVersion(pool, moduleId);
          VersionUtility.VersionComparator comparator = new VersionUtility.VersionComparator();
          if (comparator.compare(version, installedVersion) > 0) {
            dynModulesToUpdate.add(module);
          }
        } else {
          dynModulesToInstall.add(module);
        }
      }

      modulesToInstall = dyanaBeanToModules(dynModulesToInstall, dependencies);
      modulesToUpdate = dyanaBeanToModules(dynModulesToUpdate, dependencies);
      errors = new OBError();
      checked = VersionUtility.checkLocal(vars, modulesToInstall, errors);
    } else {
      // if it is a remote installation for a file, just take the first
      // module and
      // pull the rest of them

      getModulesFromObx(dynModulesToInstall, dependencies, new Vector<DynaBean>(), file);
      final String[] installableModules = new String[1];
      installableModules[0] = (String) dynModulesToInstall.get(0).get("AD_MODULE_ID");
      checkDependenciesId(installableModules, new String[0]);
    }
    if (antInstall) {
      printAntDependenciesLog();
    }
    if (!checked) {
      try {
        ImportModuleData.insertLog(pool, (vars == null ? "0" : vars.getUser()), "", "", "",
            "Cannot perform installation correctly: " + errors.getMessage()
                + (force ? ". Forced anyway" : ""), "E");
      } catch (final ServletException ex) {
        ex.printStackTrace();
      }
    }
    return checked;
  }

  /**
   * Check the dependencies for a id. After checking dependencies modulesToInstall and
   * modulesToUpdate arrays of modules are completed, thus it is possible to know wich are the
   * modules that are needed to install and/or update in order to complete the installation.
   * 
   */
  public boolean checkDependenciesId(String[] installableModules, String[] updateableModules)
      throws Exception {
    // just for remote usage
    errors = new OBError();
    VersionUtility.setPool(pool);
    final ModuleInstallDetail mid = VersionUtility.checkRemote(vars, installableModules,
        updateableModules, errors);
    modulesToInstall = mid.getModulesToInstall();
    modulesToUpdate = mid.getModulesToUpdate();
    checked = mid.isValidConfiguration();

    return checked;
  }

  /**
   * Executes the modules installation, first one of the checkDependencies method should have been
   * called in order to set the installable and updateable modules.
   * 
   * This method receives a filename
   * 
   */
  public void execute(String fileName) throws Exception {
    final File file = new File(fileName);
    if (!file.exists())
      throw new Exception("File " + fileName + " do not exist!");
    execute(new FileInputStream(fileName));
  }

  /**
   * Deprecated, use instead ImportModule.execute(InputStream file)
   * 
   * @param file
   * @param file2
   */
  @Deprecated
  public void execute(InputStream file, InputStream file2) {
    execute(file);
  }

  /**
   * Executes the modules installation, first one of the checkDependencies method should have been
   * called in order to set the installable and updateable modules.
   * 
   * This method receives a InputStream of the obx file
   * 
   */
  public void execute(InputStream file) {
    try {
      if (checked || force) {
        if (installLocally) {
          initInstallation();

          final String moduleToInstallID = (modulesToInstall != null && modulesToInstall.length > 0) ? modulesToInstall[0]
              .getModuleID()
              : modulesToUpdate[0].getModuleID();

          final Vector<DynaBean> dynMod = new Vector<DynaBean>();
          final Vector<DynaBean> dynDep = new Vector<DynaBean>();
          final Vector<DynaBean> dynDbPrefix = new Vector<DynaBean>();

          installModule(file, moduleToInstallID, dynMod, dynDep, dynDbPrefix);

          if (moduleToInstallID.equals("0"))
            Utility.mergeOpenbravoProperties(obDir + "/config/Openbravo.properties", obDir
                + "/config/Openbravo.properties.template");

          final Vector<DynaBean> allModules = new Vector<DynaBean>(); // all
          // modules
          // include
          // install
          // and
          // update
          allModules.addAll(dynModulesToInstall);
          allModules.addAll(dynModulesToUpdate);
          insertDynaModulesInDB(allModules, dependencies, dbprefix);
          insertDBLog();
          addDynaClasspathEntries(dynModulesToInstall);
        } else { // install remotely
          execute();
        }
      }
    } catch (final Exception e) {
      e.printStackTrace();
      addLog(e.toString(), MSG_ERROR);
      try {
        ImportModuleData.insertLog(pool, (vars == null ? "0" : vars.getUser()), "", "", "", e
            .toString(), "E");
      } catch (final ServletException ex) {
        ex.printStackTrace();
      }
      rollback();
    }
  }

  /**
   * Executes the modules installation, first one of the checkDependencies method should have been
   * called in order to set the installable and updateable modules.
   * 
   */
  public void execute() {
    // just for remote installation, modules to install and update must be
    // initialized
    WebServiceImplServiceLocator loc;
    WebServiceImpl ws = null;
    try {
      loc = new WebServiceImplServiceLocator();
      ws = loc.getWebService();
    } catch (final Exception e) {
      e.printStackTrace();
      addLog("@CouldntConnectToWS@", MSG_ERROR);
      try {
        ImportModuleData.insertLog(pool, (vars == null ? "0" : vars.getUser()), "", "", "",
            "Couldn't contact with webservice server", "E");
      } catch (final ServletException ex) {
        ex.printStackTrace();
      }
      return;
    }
    if (checked || force) {
      initInstallation();
      if ((modulesToInstall == null || modulesToInstall.length == 0)
          && (modulesToUpdate == null || modulesToUpdate.length == 0)) {
        addLog("@ErrorNoModulesToInstall@", MSG_ERROR);
        return;
      }

      if (modulesToInstall != null) {
        for (int i = 0; i < modulesToInstall.length; i++) {
          try {
            // get remote module obx
            InputStream obx = ModuleUtiltiy.getRemoteModule(this, modulesToInstall[i]
                .getModuleVersionID());
            if (obx == null) {
              return;
            }

            final Vector<DynaBean> dynMod = new Vector<DynaBean>();
            final Vector<DynaBean> dynDep = new Vector<DynaBean>();
            final Vector<DynaBean> dynDbPrefix = new Vector<DynaBean>();
            installModule(obx, modulesToInstall[i].getModuleID(), dynMod, dynDep, dynDbPrefix);

            // Add entries in .classpath for eclipse users
            insertDynaModulesInDB(dynMod, dynDep, dynDbPrefix);
            addDynaClasspathEntries(dynMod);
          } catch (final Exception e) {
            log4j.error(e.getMessage(), e);
            if (!(e instanceof PermissionException)) {
              addLog("@ErrorGettingModule@", MSG_ERROR);
            }
            rollback();
            try {
              ImportModuleData.insertLog(pool, (vars == null ? "0" : vars.getUser()), "", "", "",
                  "Error getting module " + modulesToInstall[i].getName() + " - "
                      + modulesToInstall[i].getVersionNo(), "E");
            } catch (final ServletException ex) {
              ex.printStackTrace();
            }
            return;
          }
        }
      }

      if (modulesToUpdate != null) {
        for (int i = 0; i < modulesToUpdate.length; i++) {
          try {
            // get remote module obx
            InputStream obx = ModuleUtiltiy.getRemoteModule(this, modulesToUpdate[i]
                .getModuleVersionID());
            if (obx == null) {
              return;
            }

            final Vector<DynaBean> dynMod = new Vector<DynaBean>();
            final Vector<DynaBean> dynDep = new Vector<DynaBean>();
            final Vector<DynaBean> dynDBPrefix = new Vector<DynaBean>();
            installModule(obx, modulesToUpdate[i].getModuleID(), dynMod, dynDep, dynDBPrefix);

            insertDynaModulesInDB(dynMod, dynDep, dynDBPrefix);

            if (modulesToUpdate[i].getModuleID().equals("0"))
              Utility.mergeOpenbravoProperties(obDir + "/config/Openbravo.properties", obDir
                  + "/config/Openbravo.properties.template");

            // Entries for .classpath should be there, do not try to
            // insert them
          } catch (final Exception e) {
            e.printStackTrace();
            addLog("@ErrorGettingModule@", MSG_ERROR);
            rollback();
            try {
              ImportModuleData.insertLog(pool, (vars == null ? "0" : vars.getUser()), "", "", "",
                  "Error getting module " + modulesToUpdate[i].getName() + " - "
                      + modulesToUpdate[i].getVersionNo(), "E");
            } catch (final ServletException ex) {
              ex.printStackTrace();
            }
            return;
          }
        }
      }
      insertDBLog();
    }
  }

  /**
   * Prapares a backup for rollback of updates and removes the files within the directories to
   * update, making thus the update like an installation from scratch.
   * 
   */
  private void initInstallation() {

    if (modulesToUpdate == null || modulesToUpdate.length == 0)
      return;
    final File dir = new File(obDir + "/backup_install");
    if (!dir.exists())
      dir.mkdirs();
    for (int i = 0; i < modulesToUpdate.length; i++) {
      // Prepare backup for updates
      if (modulesToUpdate[i].getModuleID().equals("0")) { // Updating core
        // set directories to zip
        final File core[] = getCore();

        log4j.info("Zipping core...");
        try {
          Zip.zip(core, obDir + "/backup_install/" + modulesToUpdate[i].getPackageName() + "-"
              + modulesToUpdate[i].getVersionNo() + ".zip", obDir);
        } catch (final Exception e) {
          e.printStackTrace();
        }
        log4j.info("deleting files...");
        Utility.deleteDir(core);
      } else { // updating a module different than core

        // take the info from module in db instead from modulesToUpdate because it can be
        // different
        ImportModuleData moduleInDB = null;
        try {
          moduleInDB = ImportModuleData.getModule(pool, modulesToUpdate[i].getModuleID());
        } catch (Exception e) {
          log4j.error(e);
        }

        if (moduleInDB != null) {
          try {
            Zip.zip(obDir + "/modules/" + moduleInDB.javapackage, obDir + "/backup_install/"
                + moduleInDB.javapackage + "-" + moduleInDB.version + ".zip");
            // Delete directory to be updated
            log4j.info("deleting files...");
            Utility.deleteDir(new File(obDir + "/modules/" + moduleInDB.javapackage));
          } catch (final Exception e) {
            log4j.error(e);
          }
        } else {
          log4j.error("module " + modulesToUpdate[i].getName()
              + " not found in DB. Backup and old package skipped!");
        }

      }
    }

  }

  /**
   * Returns the list of modules to update. This list is set by one of the checkDependencies
   * methods.
   */
  public Module[] getModulesToUpdate() {
    return modulesToUpdate;
  }

  /**
   * Returns the list of modules to install. This list is set by one of the checkDependencies
   * methods.
   */
  public Module[] getModulesToInstall() {
    return modulesToInstall;
  }

  /**
   * Returns the list of errors. This list is set by one of the checkDependencies methods. A list of
   * errors is returned in case the selected modules cannot be installed because dependencies are
   * not satisfied.
   * 
   */
  public OBError getCheckError() {
    return errors;
  }

  /**
   * Set the install locally variable, install locally means that no pull is going to be done for
   * the contents of the obx, it will be installed directly from the obx file regardless better
   * versions are available.
   * 
   */
  public void setInstallLocal(boolean v) {
    installLocally = v;
  }

  /**
   * Returns an OBError instance based on the log for the current ImportModule instance
   * 
   */
  public OBError getOBError(ConnectionProvider conn) {
    if (log.length() != 0) {

      final OBError rt = new OBError();
      switch (logLevel) {
      case MSG_ERROR:
        rt.setType("Error");
        break;
      case MSG_WARN:
        rt.setType("Warning");
        break;
      default:
        rt.setType("Success");
        break;
      }

      if (vars != null) {
        final String lang = vars.getLanguage();
        rt.setMessage(Utility.parseTranslation(conn, vars, lang, log.toString()));
        rt.setTitle(Utility.messageBD(conn, rt.getType(), lang));
      } else {
        rt.setMessage(log.toString());
        rt.setTitle(rt.getType());
      }
      return rt;
    } else
      return null;
  }

  /**
   * Rolls back current transaction deleting the already installed modules and recovering the backup
   * for the modules to update.
   * 
   */
  private void rollback() {
    // Modules to install
    addLog("@RollbackInstallation@", MSG_ERROR);
    try {
      ImportModuleData.insertLog(pool, (vars == null ? "0" : vars.getUser()), "", "", "",
          "Rollback installation", "E");
    } catch (final ServletException ex) {
      ex.printStackTrace();
    }
    if (modulesToInstall != null && modulesToInstall.length > 0) {
      for (int i = 0; i < modulesToInstall.length; i++) {
        try {
          // remove module from db (in case it is already there)
          ImportModuleData.setInDevelopment(pool, modulesToInstall[i].getModuleID());
          ImportModuleData.deleteDependencies(pool, modulesToInstall[i].getModuleID());
          ImportModuleData.deleteDBPrefix(pool, modulesToInstall[i].getModuleID());
          ImportModuleData.deleteModule(pool, modulesToInstall[i].getModuleID());
        } catch (final Exception e) {
          e.printStackTrace();
          addLog("Error deleting module " + modulesToInstall[i].getName() + " from db. "
              + e.getMessage(), MSG_ERROR);
        }

        final File f = new File(obDir + "/modules/" + modulesToInstall[i].getPackageName());
        if (f.exists()) {
          if (Utility.deleteDir(f))
            addLog("@DeletedDirectory@ " + f.getAbsolutePath(), MSG_ERROR);
          else
            addLog("@CouldntDeleteDirectory@ " + f.getAbsolutePath(), MSG_ERROR);
        }
      }
    }

    if (modulesToUpdate != null && modulesToUpdate.length > 0) {
      for (int i = 0; i < modulesToUpdate.length; i++) {
        if (modulesToUpdate[i].getModuleID().equals("0")) { // restore
          // core
          final File core[] = getCore();
          Utility.deleteDir(core);
          try {
            Zip.unzip(obDir + "/backup_install/" + modulesToUpdate[i].getPackageName() + "-"
                + modulesToUpdate[i].getVersionNo() + ".zip", obDir);
          } catch (final Exception e) {
            e.printStackTrace();
          }
        } else { // restore regular modules
          try {
            Utility.deleteDir(new File(obDir + "/modules/" + modulesToUpdate[i].getPackageName()));
            Zip.unzip(obDir + "/backup_install/" + modulesToUpdate[i].getPackageName() + "-"
                + modulesToUpdate[i].getVersionNo() + ".zip", obDir + "/modules/"
                + modulesToUpdate[i].getPackageName());
          } catch (final Exception e) {
            e.printStackTrace();
          }
        }
      }
    }
  }

  /**
   * Prints an ant log for the dependencies
   */
  private void printAntDependenciesLog() {
    if (!checked) {
      log4j.error("This module does not satisfy dependencies");
      log4j.warn("Needed dependencies:");
      log4j.warn("  " + errors.getMessage());
    } else {
      if (modulesToInstall != null && modulesToInstall.length > 0) {
        log4j.info("Modules to install:");
        for (int i = 0; i < modulesToInstall.length; i++) {
          log4j.info(modulesToInstall[i].getName() + " " + modulesToInstall[i].getVersionNo());
        }
      }
      if (modulesToUpdate != null && modulesToUpdate.length > 0) {
        log4j.info("Modules to update:");
        for (int i = 0; i < modulesToUpdate.length; i++) {
          log4j.info(modulesToUpdate[i].getName() + " " + modulesToUpdate[i].getVersionNo());
        }
      }
    }
  }

  /**
   * Adds a message with a log level to the current instance log.
   * 
   */
  void addLog(String m, int level) {
    log4j.info(m);
    if (level > logLevel) {
      logLevel = level;
      log = new StringBuffer(m);
    } else if (level == logLevel)
      log.append(m + "\n");
  }

  /**
   * Receives a Vector<DynaBean> and tranforms it to a Module[]
   */
  private Module[] dyanaBeanToModules(Vector<DynaBean> dModulesToInstall,
      Vector<DynaBean> dynDependencies) {
    final Module[] rt = new Module[dModulesToInstall.size()];
    int i = 0;
    for (final DynaBean dynModule : dModulesToInstall) {
      rt[i] = new Module();
      rt[i].setModuleID((String) dynModule.get("AD_MODULE_ID"));
      rt[i].setVersionNo((String) dynModule.get("VERSION"));
      rt[i].setName((String) dynModule.get("NAME"));
      rt[i].setLicenseAgreement((String) dynModule.get("LICENSE"));
      rt[i].setLicenseType((String) dynModule.get("LICENSETYPE"));
      rt[i].setPackageName((String) dynModule.get("JAVAPACKAGE"));
      rt[i].setType((String) dynModule.get("TYPE"));
      rt[i].setDescription((String) dynModule.get("DESCRIPTION"));
      rt[i].setHelp((String) dynModule.get("HELP"));
      rt[i].setDependencies(dyanaBeanToDependencies(dynDependencies, rt[i].getModuleID()));
      // old modules don't have iscommercial column
      Object isCommercial = dynModule.get("ISCOMMERCIAL");
      rt[i].setIsCommercial(isCommercial != null && ((String) isCommercial).equals("Y"));
      rt[i].setModuleVersionID((String) dynModule.get("AD_MODULE_ID")); // To
      // show
      // details
      // in
      // local
      // ad_module_id
      // is
      // used
      i++;
    }
    return rt;
  }

  /**
   * Returns the dependencies in Vector<DynaBean> dynDependencies for the ad_module_id module as a
   * ModuleDependency[], used by dyanaBeanToModules method
   * 
   */
  private ModuleDependency[] dyanaBeanToDependencies(Vector<DynaBean> dynDependencies,
      String ad_module_id) {
    final ArrayList<ModuleDependency> dep = new ArrayList<ModuleDependency>();
    for (final DynaBean dynModule : dynDependencies) {
      if (((String) dynModule.get("AD_MODULE_ID")).equals(ad_module_id)) {
        final ModuleDependency md = new ModuleDependency();
        md.setModuleID((String) dynModule.get("AD_DEPENDENT_MODULE_ID"));
        md.setVersionStart((String) dynModule.get("STARTVERSION"));
        md.setVersionEnd((String) dynModule.get("ENDVERSION"));
        dep.add(md);
      }
    }
    final ModuleDependency rt[] = new ModuleDependency[dep.size()];
    for (int i = 0; i < rt.length; i++) {
      rt[i] = dep.get(i);
    }
    return rt;
  }

  /**
   * Adds the classpath entries to .classpath file from the modules in the Vector<DynaBean>
   * 
   */
  private void addDynaClasspathEntries(Vector<DynaBean> dModulesToInstall) throws Exception {
    if (!(new File(obDir + "/.classpath").exists())) {
      log4j.info("No " + obDir + "/.classpath file");
      return;
    }
    log4j.info("Adding .claspath entries");
    final DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
    final DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
    final Document doc = docBuilder.parse(obDir + "/.classpath");
    for (final DynaBean module : dModulesToInstall) {
      final String dir = "modules/" + (String) module.get("JAVAPACKAGE") + "/src";
      if (new File(obDir + "/" + dir).exists()) {
        addClassPathEntry(doc, dir);
      } else {
        log4j.info(dir + " does not exist, no claspath entry added");
      }
    }

    // Save the modified xml file to .classpath file
    final Transformer transformer = TransformerFactory.newInstance().newTransformer();
    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
    final FileOutputStream fout = new FileOutputStream(obDir + "/.classpath");
    final StreamResult result = new StreamResult(fout);
    final DOMSource source = new DOMSource(doc);
    transformer.transform(source, result);
    fout.close();
  }

  /**
   * Adds a single classpath entry to the xml file
   */
  private void addClassPathEntry(Document doc, String dir) throws Exception {
    log4j.info("adding entry for directory" + dir);
    final Node root = doc.getFirstChild();
    final Node classpath = doc.createElement("classpathentry");
    final NamedNodeMap cpAttributes = classpath.getAttributes();

    Attr attr = doc.createAttribute("kind");
    attr.setValue("src");
    cpAttributes.setNamedItem(attr);

    attr = doc.createAttribute("path");
    attr.setValue(dir);
    cpAttributes.setNamedItem(attr);

    root.appendChild(classpath);

  }

  /**
   * Reads an ZipInputStream and returns it as a ByteArrayInputStream
   * 
   * @param obxInputStream
   * @return
   * @throws Exception
   */
  private ByteArrayInputStream getCurrentEntryStream(ZipInputStream obxInputStream)
      throws Exception {
    final ByteArrayOutputStream fout = new ByteArrayOutputStream();
    for (int c = obxInputStream.read(); c != -1; c = obxInputStream.read()) {
      fout.write(c);
    }
    fout.close();
    final ByteArrayInputStream ba = new ByteArrayInputStream(fout.toByteArray());
    return ba;
  }

  private byte[] getBytesCurrentEntryStream(ZipInputStream obxInputStream) throws Exception {
    final ByteArrayOutputStream fout = new ByteArrayOutputStream();
    final byte[] buf = new byte[1024];
    int len;
    while ((len = obxInputStream.read(buf)) > 0) {
      fout.write(buf, 0, len);
    }

    fout.close();
    return fout.toByteArray();
  }

  /**
   * Inserts in database the Vector<DynaBean> with its dependencies
   * 
   * @param dModulesToInstall
   * @param dependencies1
   * @throws Exception
   */
  private void insertDynaModulesInDB(Vector<DynaBean> dModulesToInstall,
      Vector<DynaBean> dependencies1, Vector<DynaBean> dbPrefix) throws Exception {
    final Properties obProperties = new Properties();
    obProperties.load(new FileInputStream(obDir + "/config/Openbravo.properties"));

    final String url = obProperties.getProperty("bbdd.url")
        + (obProperties.getProperty("bbdd.rdbms").equals("POSTGRE") ? "/"
            + obProperties.getProperty("bbdd.sid") : "");

    final BasicDataSource ds = new BasicDataSource();
    ds.setDriverClassName(obProperties.getProperty("bbdd.driver"));
    ds.setUrl(url);
    ds.setUsername(obProperties.getProperty("bbdd.user"));
    ds.setPassword(obProperties.getProperty("bbdd.password"));

    final Connection conn = ds.getConnection();

    Integer seqNo = new Integer(ImportModuleData.selectSeqNo(pool));

    for (final DynaBean module : dModulesToInstall) {
      seqNo += 10;
      module.set("ISINDEVELOPMENT", "Y");
      module.set("ISDEFAULT", "N");
      module.set("STATUS", "I");
      module.set("SEQNO", seqNo);
      module.set("UPDATE_AVAILABLE", null);
      log4j.info("Inserting in DB info for module: " + module.get("NAME"));

      String moduleId = (String) module.get("AD_MODULE_ID");

      // Clean temporary tables
      ImportModuleData.cleanModuleInstall(pool, moduleId);
      ImportModuleData.cleanModuleDBPrefixInstall(pool, moduleId);
      ImportModuleData.cleanModuleDependencyInstall(pool, moduleId);

      // Insert data in temporary tables
      ImportModuleData.insertModuleInstall(pool, moduleId, (String) module.get("NAME"),
          (String) module.get("VERSION"), (String) module.get("DESCRIPTION"), (String) module
              .get("HELP"), (String) module.get("URL"), (String) module.get("TYPE"),
          (String) module.get("LICENSE"), (String) module.get("ISINDEVELOPMENT"), (String) module
              .get("ISDEFAULT"), seqNo.toString(), (String) module.get("JAVAPACKAGE"),
          (String) module.get("LICENSETYPE"), (String) module.get("AUTHOR"), (String) module
              .get("STATUS"), (String) module.get("UPDATE_AVAILABLE"), (String) module
              .get("ISTRANSLATIONREQUIRED"), (String) module.get("AD_LANGUAGE"), (String) module
              .get("HASCHARTOFACCOUNTS"), (String) module.get("ISTRANSLATIONMODULE"),
          (String) module.get("HASREFERENCEDATA"), (String) module.get("ISREGISTERED"),
          (String) module.get("UPDATEINFO"), (String) module.get("UPDATE_VER_ID"), (String) module
              .get("REFERENCEDATAINFO"));

      // Set installed for modules being updated
      ImportModuleData.setModuleUpdated(pool, (String) module.get("AD_MODULE_ID"));

      addLog("@ModuleInstalled@ " + module.get("NAME") + " - " + module.get("VERSION"), MSG_SUCCESS);
    }
    for (final DynaBean module : dependencies1) {
      ImportModuleData.insertModuleDependencyInstall(pool, (String) module
          .get("AD_MODULE_DEPENDENCY_ID"), (String) module.get("AD_MODULE_ID"), (String) module
          .get("AD_DEPENDENT_MODULE_ID"), (String) module.get("STARTVERSION"), (String) module
          .get("ENDVERSION"), (String) module.get("ISINCLUDED"), (String) module
          .get("DEPENDANT_MODULE_NAME"));
    }
    for (final DynaBean module : dbPrefix) {
      ImportModuleData.insertModuleDBPrefixInstall(pool, (String) module
          .get("AD_MODULE_DBPREFIX_ID"), (String) module.get("AD_MODULE_ID"), (String) module
          .get("NAME"));
    }

    conn.close();
  }

  /**
   * Returns all the modules and dependencies described within the obx file (as InputStream)
   * 
   * Used to check dependencies in local intallation
   * 
   * @param dModulesToInstall
   * @param dDependencies
   * @param obx
   * @throws Exception
   */
  private void getModulesFromObx(Vector<DynaBean> dModulesToInstall,
      Vector<DynaBean> dDependencies, Vector<DynaBean> dDBprefix, InputStream obx) throws Exception {
    final ZipInputStream obxInputStream = new ZipInputStream(obx);
    ZipEntry entry = null;
    boolean foundAll = false;
    boolean foundModule = false;
    boolean foundDependency = false;
    boolean foundPrefix = false;
    while (((entry = obxInputStream.getNextEntry()) != null) && !foundAll) {

      if (entry.getName().endsWith(".obx")) { // If it is a new module
        // install it
        final ByteArrayInputStream ba = getCurrentEntryStream(obxInputStream);
        obxInputStream.closeEntry();
        getModulesFromObx(dModulesToInstall, dDependencies, dDBprefix, ba);
      } else if (entry.getName().replace("\\", "/").endsWith(
          "src-db/database/sourcedata/AD_MODULE.xml")) {
        final Vector<DynaBean> module = getEntryDynaBeans(getBytesCurrentEntryStream(obxInputStream));
        boolean isPackage = false;
        if (module != null && module.size() > 0) {
          isPackage = !((String) module.get(0).get("TYPE")).equals("M");
        }
        dModulesToInstall.addAll(module);
        obxInputStream.closeEntry();
        foundModule = true && !isPackage;
      } else if (entry.getName().replace("\\", "/").endsWith(
          "src-db/database/sourcedata/AD_MODULE_DEPENDENCY.xml")) {
        dDependencies.addAll(getEntryDynaBeans(getBytesCurrentEntryStream(obxInputStream)));
        obxInputStream.closeEntry();
        foundDependency = true;
      } else if (entry.getName().replace("\\", "/").endsWith(
          "src-db/database/sourcedata/AD_MODULE_DBPREFIX.xml")) {
        dDBprefix.addAll(getEntryDynaBeans(getBytesCurrentEntryStream(obxInputStream)));
        obxInputStream.closeEntry();
        foundPrefix = true;
      } else
        obxInputStream.closeEntry();
      foundAll = foundModule && foundDependency && foundPrefix;
    }
    obxInputStream.close();
  }

  /**
   * Reads a ZipInputStream and returns a Vector<DynaBean> with its modules
   * 
   * @param obxInputStream
   * @return
   * @throws Exception
   */
  private Vector<DynaBean> getEntryDynaBeans(byte[] obxEntryBytes) throws Exception {
    final ByteArrayInputStream ba = new ByteArrayInputStream(obxEntryBytes);

    final DatabaseDataIO io = new DatabaseDataIO();
    final DataReader dr = io.getConfiguredCompareDataReader(db);
    dr.getSink().start();
    dr.parse(ba);
    return ((DataToArraySink) dr.getSink()).getVector();
  }

  /**
   * Installs or updates the modules in the obx file
   * 
   * @param obx
   * @param moduleID
   *          The ID for the current module to install
   * @throws Exception
   */
  private void installModule(InputStream obx, String moduleID, Vector<DynaBean> dModulesToInstall,
      Vector<DynaBean> dDependencies, Vector<DynaBean> dDBprefix) throws Exception {
    if (!(new File(obDir + "/modules").canWrite())) {
      addLog("@CannotWriteDirectory@ " + obDir + "/modules. ", MSG_ERROR);
      throw new PermissionException("Cannot write on directory: " + obDir + "/modules");
    }

    final ZipInputStream obxInputStream = new ZipInputStream(obx);
    ZipEntry entry = null;
    while ((entry = obxInputStream.getNextEntry()) != null) {
      if (entry.getName().endsWith(".obx")) { // If it is a new module
        // install it
        if (installLocally) {
          final ByteArrayInputStream ba = new ByteArrayInputStream(
              getBytesCurrentEntryStream(obxInputStream));

          installModule(ba, moduleID, dModulesToInstall, dDependencies, dDBprefix);
        } // If install remotely it is no necessary to install the .obx
        // because it will be get from CR
        obxInputStream.closeEntry();
      } else {
        // Unzip the contents
        final String fileName = obDir + (moduleID.equals("0") ? "/" : "/modules/")
            + entry.getName().replace("\\", "/");
        final File entryFile = new File(fileName);
        // Check whether the directory exists, if not create

        File dir = null;
        if (entryFile.getParent() != null)
          dir = new File(entryFile.getParent());
        if (entry.isDirectory())
          dir = entryFile;

        if (entry.isDirectory() || entryFile.getParent() != null) {
          if (!dir.exists()) {
            log4j.info("Created dir: " + dir.getAbsolutePath());
            dir.mkdirs();
          }
        }

        if (!entry.isDirectory()) {
          // It is a file
          byte[] entryBytes = null;
          boolean found = false;
          // Read the xml file to obtain module info
          if (entry.getName().replace("\\", "/").endsWith(
              "src-db/database/sourcedata/AD_MODULE.xml")) {
            entryBytes = getBytesCurrentEntryStream(obxInputStream);
            final Vector<DynaBean> module = getEntryDynaBeans(entryBytes);
            dModulesToInstall.addAll(module);
            moduleID = (String) module.get(0).get("AD_MODULE_ID");
            obxInputStream.closeEntry();
            found = true;
          } else if (entry.getName().replace("\\", "/").endsWith(
              "src-db/database/sourcedata/AD_MODULE_DEPENDENCY.xml")) {
            entryBytes = getBytesCurrentEntryStream(obxInputStream);
            dDependencies.addAll(getEntryDynaBeans(entryBytes));
            obxInputStream.closeEntry();
            found = true;
          } else if (entry.getName().replace("\\", "/").endsWith(
              "src-db/database/sourcedata/AD_MODULE_DBPREFIX.xml")) {
            entryBytes = getBytesCurrentEntryStream(obxInputStream);
            dDBprefix.addAll(getEntryDynaBeans(entryBytes));
            obxInputStream.closeEntry();
            found = true;
          }

          // Unzip the file
          log4j.info("Installing " + fileName);

          final FileOutputStream fout = new FileOutputStream(entryFile);

          if (found) {
            // the entry is already read as a byte[]
            fout.write(entryBytes);
          } else {
            final byte[] buf = new byte[4096];
            int len;
            while ((len = obxInputStream.read(buf)) > 0) {
              fout.write(buf, 0, len);
            }
          }

          fout.close();
        }

        obxInputStream.closeEntry();
      }
    }
    obxInputStream.close();
  }

  public boolean getIsLocal() {
    return installLocally;
  }

  /**
   * Inserts log in ad_module_log table
   * 
   */
  private void insertDBLog() {
    try {
      final String user = vars == null ? "0" : vars.getUser();
      if (modulesToInstall != null && modulesToInstall.length > 0) {
        for (int i = 0; i < modulesToInstall.length; i++) {
          ImportModuleData.insertLog(pool, user, modulesToInstall[i].getModuleID(),
              modulesToInstall[i].getModuleVersionID(), modulesToInstall[i].getName(),
              "Installed module " + modulesToInstall[i].getName() + " - "
                  + modulesToInstall[i].getVersionNo(), "I");
        }
      }
      if (modulesToUpdate != null && modulesToUpdate.length > 0) {
        for (int i = 0; i < modulesToUpdate.length; i++) {
          ImportModuleData.insertLog(pool, user, modulesToUpdate[i].getModuleID(),
              modulesToUpdate[i].getModuleVersionID(), modulesToUpdate[i].getName(),
              "Updated module " + modulesToUpdate[i].getName() + " to version "
                  + modulesToUpdate[i].getVersionNo(), "U");
        }
      }
    } catch (final ServletException e) {
      e.printStackTrace();
    }
  }

  /**
   * Scans for updates for the existent modules and sets and returs the list of modules that have
   * updates available
   * 
   * @param conn
   * @param vars
   * @return the list of updates keyed by module id
   */
  public static HashMap<String, String> scanForUpdates(ConnectionProvider conn,
      VariablesSecureApp vars) {
    try {
      final HashMap<String, String> updateModules = new HashMap<String, String>();
      final String user = vars == null ? "0" : vars.getUser();
      ImportModuleData.insertLog(conn, user, "", "", "", "Scanning For Updates", "S");
      WebServiceImplServiceLocator loc;
      WebServiceImpl ws = null;
      SimpleModule[] updates;
      try {
        loc = new WebServiceImplServiceLocator();
        ws = loc.getWebService();
        final HashMap<String, String> currentlyInstalledModules = getInstalledModules(conn);
        updates = ws.moduleScanForUpdates(currentlyInstalledModules);
      } catch (final Exception e) {
        // do nothing just log the error
        e.printStackTrace();
        try {
          ImportModuleData.insertLog(conn, user, "", "", "",
              "Scan for updates: Couldn't contact with webservice server", "E");
        } catch (final ServletException ex) {
          ex.printStackTrace();
        }
        return updateModules; // return empty hashmap
      }

      if (updates != null && updates.length > 0) {
        for (int i = 0; i < updates.length; i++) {
          if (!ImportModuleData.existsVersion(conn, updates[i].getVersionNo())) {
            ImportModuleData.updateNewVersionAvailable(conn, updates[i].getVersionNo(), updates[i]
                .getModuleVersionID(), updates[i].getModuleID());
            ImportModuleData.insertLog(conn, user, updates[i].getModuleID(), updates[i]
                .getModuleVersionID(), updates[i].getName(), "Found new version "
                + updates[i].getVersionNo() + " for module " + updates[i].getName(), "S");
            updateModules.put(updates[i].getModuleID(), "U");
          }
        }
        addParentUpdates(updateModules, conn);
      }
      try {
        ImportModuleData.insertLog(conn, (vars == null ? "0" : vars.getUser()), "", "", "",
            "Total: found " + updateModules.size() + " updates", "S");
      } catch (final ServletException ex) {
        ex.printStackTrace();
      }
      return updateModules;
    } catch (final Exception e) {
      e.printStackTrace();
      try {
        ImportModuleData.insertLog(conn, (vars == null ? "0" : vars.getUser()), "", "", "",
            "Scan for updates: Error: " + e.toString(), "E");
      } catch (final ServletException ex) {
        ex.printStackTrace();
      }
      return new HashMap<String, String>();
    }
  }

  private static void addParentUpdates(HashMap<String, String> updates, ConnectionProvider conn) {
    final HashMap<String, String> iniUpdates = (HashMap<String, String>) updates.clone();
    for (final String node : iniUpdates.keySet()) {
      addParentNode(node, updates, iniUpdates, conn);
    }
  }

  private static void addParentNode(String node, HashMap<String, String> updates,
      HashMap<String, String> iniUpdates, ConnectionProvider conn) {
    String parentId;
    try {
      parentId = ImportModuleData.getParentNode(conn, node);
    } catch (final ServletException e) {
      // do nothing just stop adding elements
      e.printStackTrace();
      return;
    }
    if (parentId == null || parentId.equals(""))
      return;
    if (updates.get(parentId) == null && iniUpdates.get(parentId) == null)
      updates.put(parentId, "P");
    addParentNode(parentId, updates, iniUpdates, conn);
  }

  /**
   * Returns the current installed modules with its version
   * 
   * @param conn
   *          ConnectionProvider needed as it is a static method
   * @return HashMap<String,String> -> <ModuleId, VersionNo>
   */
  public static HashMap<String, String> getInstalledModules(ConnectionProvider conn) {
    final HashMap<String, String> rt = new HashMap<String, String>();
    ImportModuleData data[] = null;
    try {
      data = ImportModuleData.selectInstalled(conn);
    } catch (final Exception e) {
      e.printStackTrace();
    }
    if (data != null) {
      for (int i = 0; i < data.length; i++) {
        rt.put(data[i].adModuleId, data[i].version);
      }
    }
    return rt;
  }

  /**
   * Returns a File array with the directories that are part of core
   * 
   * @return
   */
  private File[] getCore() {
    final File[] file = new File[12];
    file[0] = new File(obDir + "/legal");
    file[1] = new File(obDir + "/lib");
    file[2] = new File(obDir + "/WebContent");
    file[3] = new File(obDir + "/src-core");
    file[4] = new File(obDir + "/src-db");
    file[5] = new File(obDir + "/src-gen");
    file[6] = new File(obDir + "/src-trl");
    file[7] = new File(obDir + "/src-wad");
    file[8] = new File(obDir + "/src");
    file[9] = new File(obDir + "/web");
    file[10] = new File(obDir + "/src-test");
    file[11] = new File(obDir + "/src-diagnostics");
    return file;
  }

  /**
   * Returns the module with the ID that is in the module to install or update.
   * 
   * @param moduleID
   * @return the module with the moduleID, or null if not found
   */
  public Module getModule(String moduleID) {
    for (int i = 0; i < modulesToInstall.length; i++) {
      if (modulesToInstall[i].getModuleID().equals(moduleID))
        return modulesToInstall[i];
    }
    for (int i = 0; i < modulesToUpdate.length; i++) {
      if (modulesToUpdate[i].getModuleID().equals(moduleID))
        return modulesToUpdate[i];
    }
    return null;
  }

  private class PermissionException extends Exception {
    public PermissionException() {
      super();
    }

    public PermissionException(String msg) {
      super(msg);
    }
  }

}
