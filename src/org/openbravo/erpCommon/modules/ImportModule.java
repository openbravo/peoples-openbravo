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
 * All portions are Copyright (C) 2008 Openbravo SL 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
*/
package org.openbravo.erpCommon.modules;

import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.ddlutils.Platform;
import org.apache.ddlutils.PlatformFactory;
import org.apache.ddlutils.io.DataReader;
import org.apache.ddlutils.io.DataToArraySink;
import org.apache.ddlutils.io.DatabaseDataIO;
import org.apache.ddlutils.model.Database;

import org.apache.log4j.Logger;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.database.CPStandAlone;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.ddlutils.task.DatabaseUtils;
import org.openbravo.services.webservice.*;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.erpCommon.utility.Zip;

import org.w3c.dom.*;

import java.io.*;
import java.util.*;
import java.util.zip.*;

import javax.servlet.ServletException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;


/**
 * ImportModule is able to install modules.
 * 
 *  This is done in two steps:
 *    -First check if it is possible to install and receive the latest installable
 *     modules
 *    -Second install them
 *  
 *  These two processes are callable independently in order to do it from UI and 
 *  show messages and wait for confirmation after first one.
 *  
 *  It is possible to do the whole installation remotely pulling for the modules
 *  to install from the central repository or locally, installing everything from 
 *  the obx file (which can be passed as an InputStream or as a String file name)
 *  
 */ 
public class ImportModule {
  private static ConnectionProvider pool;
  static Logger log4j = Logger.getLogger(ExtractModule.class);
  private String obDir;
  private Database db;
  private boolean installLocally = true;
  private boolean antInstall = false;
  private boolean force= false; // force installation though dependencies are not satisfied
  private Module[] modulesToInstall = null;
  private Module[] modulesToUpdate = null;
  private StringBuffer log = new StringBuffer();
  private int logLevel=0;
  private VariablesSecureApp vars;
  
  public static final int MSG_SUCCESS = 0;
  public static final int MSG_WARN = 1;
  public static final int MSG_ERROR = 2;
  
  OBError errors=null; 
  Vector<DynaBean> dynModulesToInstall = new Vector<DynaBean>();
  Vector<DynaBean> dynModulesToUpdate = new Vector<DynaBean>();
  Vector<DynaBean> dependencies = new Vector<DynaBean>();
  
  boolean checked;
 
  /**
   * Initializes a new ImportModule object, it reads from obdir directory the
   * database model to be able to read the xml information within the obx file. 
   * 
   * @param obdir base directory for the application
   * @param _vars VariablesSecureApp that will be used to parse messages, if null
   *              they will not be parsed.
   */ 
  public ImportModule(ConnectionProvider conn, String obdir, VariablesSecureApp _vars){
    vars = _vars;
    obDir = obdir;
    pool = conn;
    File [] f= new File[3];
    f[0] = new File(obDir+"/src-db/database/model/tables/AD_MODULE.xml");
    f[1] = new File(obDir+"/src-db/database/model/tables/AD_MODULE_DEPENDENCY.xml");
    f[2] = new File(obDir+"/src-db/database/model/tables/AD_MODULE_DBPREFIX.xml");
    
    db = DatabaseUtils.readDatabaseNoInit(f);
  }
  
  /**
   * Check the dependencies for a file name. See {@link checkDependenciesId}
   */ 
  public boolean checkDependenciesFileName(String fileName) throws Exception{
    File file = new File(fileName);
    if (!file.exists()) throw new Exception("File "+fileName+" do not exist!");
    return checkDependenciesFile(new FileInputStream(file));
  }
  
  /**
   * Check the dependencies for a file. See {@link checkDependenciesId}
   */
  public boolean checkDependenciesFile(InputStream file) throws Exception{
    if (installLocally){
      getModulesFromObxLocal(file); 
      modulesToInstall = dyanaBeanToModules(dynModulesToInstall, dependencies);
      modulesToUpdate = dyanaBeanToModules(dynModulesToUpdate, dependencies);
      errors = new OBError();
      checked = VersionUtility.checkLocal(vars, modulesToInstall, errors);
    } else {
      //if it is a remote installation for a file, just take the first module and
      //pull the rest of them
      
      getModulesFromObx(dynModulesToInstall, dependencies, new Vector<DynaBean>(), file);
      String[] installableModules = new String[1];
      installableModules[0] = (String)dynModulesToInstall.get(0).get("AD_MODULE_ID");
      checkDependenciesId(installableModules, new String[0]);
    }
    if (antInstall){
      printAntDependenciesLog();
    }
    if (!checked) {
      try {
        ImportModuleData.insertLog(pool, (vars==null?"0":vars.getUser()), "", "", "",  "Cannot perform installation correctly: "+errors.getMessage()+(force?". Forced anyway":""), "E");
      } catch(ServletException ex) {ex.printStackTrace();}
    }
    return checked;
  }
  
  /**
   * Check the dependencies for a id. After checking dependencies modulesToInstall and 
   * modulesToUpdate arrays of modules are completed, thus it is possible to know wich
   * are the modules that are needed to install and/or update in order to complete the
   * installation.
   * 
   */
  public boolean checkDependenciesId(String[] installableModules, String[] updateableModules) throws Exception{
    //just for remote usage
    errors = new OBError();
    VersionUtility.setPool(pool);
    ModuleInstallDetail mid = VersionUtility.checkRemote(vars, installableModules, updateableModules, errors);
    modulesToInstall = mid.getModulesToInstall();
    modulesToUpdate = mid.getModulesToUpdate();
    checked = mid.isValidConfiguration();
    return checked;
  }
  
  /**
   * Executes the modules installation, first one of the checkDependencies method 
   * should have been called in order to set the installable and updateable modules.
   * 
   * This method receives a filename
   * 
   */
  public void execute(String fileName) throws Exception{
    File file = new File(fileName);
    if (!file.exists()) throw new Exception("File "+fileName+" do not exist!");
    execute(new FileInputStream(fileName), new FileInputStream(fileName));
  }
  
  /**
   * Executes the modules installation, first one of the checkDependencies method 
   * should have been called in order to set the installable and updateable modules.
   * 
   * This method receives a InputStream of the obx file
   * 
   */
  public void execute(InputStream file, InputStream file2){
    try {
      if (checked|| force) {
        if (installLocally) {
          initInstallation();
          
          //Obtain the first ID for the module to be installed (this is done for core)
          Vector<DynaBean> obxModule = new Vector<DynaBean>();
          Vector<DynaBean> dbprefix = new Vector<DynaBean>();     // not used       
          
          getModulesFromObx(obxModule, new Vector<DynaBean>(), dbprefix, file2);
          
          
          installModule(file, (String) obxModule.get(0).get("AD_MODULE_ID"));
          insertDynaModulesInDB(dynModulesToInstall, dependencies, dbprefix);
          insertDBLog();
          addDynaClasspathEntries(dynModulesToInstall);
        }else{ //install remotely
          execute();
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
      addLog(e.toString(), MSG_ERROR);
      try {
        ImportModuleData.insertLog(pool, (vars==null?"0":vars.getUser()), "", "", "",  e.toString(), "E");
      } catch(ServletException ex) {ex.printStackTrace();}
      rollback();
    }
  }
  
  /**
   * Executes the modules installation, first one of the checkDependencies method 
   * should have been called in order to set the installable and updateable modules.
   * 
   */
  public void execute(){
    // just for remote installation, modules to install and update must be initialized
    WebServiceImplServiceLocator loc;
    WebServiceImpl ws=null;
    try {
      loc = new WebServiceImplServiceLocator();
      ws = (WebServiceImpl) loc.getWebService();
    } catch (Exception e) {
      e.printStackTrace();
      addLog("@CouldntConnectToWS@", MSG_ERROR);
      try {
        ImportModuleData.insertLog(pool, (vars==null?"0":vars.getUser()), "", "", "",  "Couldn't contact with webservice server", "E");
      } catch(ServletException ex) {ex.printStackTrace();}
      return;
    }
    if (checked || force) {
      initInstallation();
      if ((modulesToInstall==null || modulesToInstall.length==0) && (modulesToUpdate==null || modulesToUpdate.length==0)){
        addLog("@ErrorNoModulesToInstall@", MSG_ERROR);
        return;
      }
      
      if (modulesToInstall != null) {
        for (int i=0; i<modulesToInstall.length;i++){
          try {
            byte[] getMod = ws.getModule(modulesToInstall[i].getModuleVersionID());
            ByteArrayInputStream obx= new ByteArrayInputStream(getMod);
            
            installModule(obx, modulesToInstall[i].getModuleID());
            //Add entries in .classpath for eclipse users
            Vector<DynaBean> dynMod = new Vector<DynaBean>();
            Vector<DynaBean> dynDep = new Vector<DynaBean>();
            Vector<DynaBean> dynDbPrefix = new Vector<DynaBean>();
            obx= new ByteArrayInputStream(getMod);
            getModulesFromObx(dynMod, dynDep, dynDbPrefix, obx);
            insertDynaModulesInDB(dynMod, dynDep, dynDbPrefix);
            addDynaClasspathEntries(dynMod);
          } catch (Exception e) {
            e.printStackTrace();
            addLog("@ErrorGettingModule@", MSG_ERROR);
            rollback();
            try {
              ImportModuleData.insertLog(pool, (vars==null?"0":vars.getUser()), "", 
                  "", "",  "Error getting module "+modulesToInstall[i].getName()+" - "+modulesToInstall[i].getVersionNo(), "E");
            } catch(ServletException ex) {ex.printStackTrace();}
            return;
          }
        }
      }
      
      if (modulesToUpdate!=null) {
        for (int i=0; i<modulesToUpdate.length; i++){
          try {
            byte[] getMod = ws.getModule(modulesToUpdate[i].getModuleVersionID());
            installModule(new ByteArrayInputStream(getMod), modulesToUpdate[i].getModuleID());
            
            Vector<DynaBean> dynMod = new Vector<DynaBean>();
            Vector<DynaBean> dynDep = new Vector<DynaBean>();
            Vector<DynaBean> dynDBPrefix = new Vector<DynaBean>();
            
            getModulesFromObx(dynMod, dynDep, dynDBPrefix, new ByteArrayInputStream(getMod));
            insertDynaModulesInDB(dynMod, dynDep, dynDBPrefix);
            
            //Entries for .classpath should be there, do not try to insert them
          } catch (Exception e) {
            e.printStackTrace();
            addLog("@ErrorGettingModule@", MSG_ERROR);
            rollback();
            try {
              ImportModuleData.insertLog(pool, (vars==null?"0":vars.getUser()), "", 
                  "", "",  "Error getting module "+modulesToUpdate[i].getName()+" - "+modulesToUpdate[i].getVersionNo(), "E");
            } catch(ServletException ex) {ex.printStackTrace();}
            return;
          }
        }
      }
      insertDBLog();
    }
  }
  
  /**
   * Prapares a backup for rollback of updates and removes the files within the directories to update, 
   * making thus the update like an installation from scratch.
   * 
   */
  private void initInstallation() {
    
    if (modulesToUpdate==null || modulesToUpdate.length==0) return;
    File dir = new File(obDir+"/backup_install");
    if (!dir.exists()) dir.mkdirs();
    for (int i=0; i<modulesToUpdate.length; i++){
      //Prepare backup for updates
      if (modulesToUpdate[i].getModuleID().equals("0")){ //Updating core
        //set directories to zip
        File core[]=getCore();
        
        log4j.info("Zipping core...");
        try {
          Zip.zip(core, obDir+"/backup_install/"+modulesToUpdate[i].getPackageName()+"-"+modulesToUpdate[i].getVersionNo()+".zip", obDir);
        } catch (Exception e) {
          e.printStackTrace();
        }
        Utility.deleteDir(core);
      } else { //updating a module different than core
        try {
          Zip.zip(obDir+"/modules/"+modulesToUpdate[i].getPackageName(), 
              obDir+"/backup_install/"+modulesToUpdate[i].getPackageName()+"-"+modulesToUpdate[i].getVersionNo()+".zip");
        } catch (Exception e) {
          e.printStackTrace();
        }
        //Delete directory to be updated
        Utility.deleteDir(new File(obDir+"/modules/"+modulesToUpdate[i].getPackageName()));
      }
    }
    
  }

  /**
   * Returns the list of modules to update. This list is set by one of
   * the checkDependencies methods.
   */
  public Module[] getModulesToUpdate() {
    return modulesToUpdate;
  }
  
  /**
   * Returns the list of modules to install. This list is set by one of
   * the checkDependencies methods.
   */
  public Module[] getModulesToInstall(){
    return modulesToInstall;
  }
  
  /**
   * Returns the list of errors. This list is set by one of the checkDependencies 
   * methods. A list of errors is returned in case the selected modules cannot be
   * installed because dependencies are not satisfied.
   * 
   */
  public OBError getCheckError(){
    return errors;
  }
  
  /**
   * Set the install locally variable, install locally means that no pull is going
   * to be done for the contents of the obx, it will be installed directly from
   * the obx file regardless better versions are available.
   * 
   */
  public void setInstallLocal(boolean v){
    installLocally = v;
  }
  
  /**
   * Returns an OBError instance based on the log for the current ImportModule instance
   * 
   */
  public OBError getOBError(ConnectionProvider conn){
    if (log.length()!=0) {
      String lang = vars.getLanguage();
      OBError rt = new OBError();
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
      rt.setMessage(Utility.parseTranslation(conn, vars, lang, log.toString()));
   
      rt.setTitle(Utility.messageBD(conn,rt.getType(),lang));
      return rt;
    } else return null;
  }
 
  /**
   * Rolls back current transaction deleting the already installed modules and 
   * recovering the backup for the modules to update.
   * 
   */
  private void rollback() {
    //Modules to install
    addLog("@RollbackInstallation@",MSG_ERROR);
    try {
      ImportModuleData.insertLog(pool, (vars==null?"0":vars.getUser()), "", "", "",  "Rollback installation", "E");
    } catch(ServletException ex) {ex.printStackTrace();}
    if (modulesToInstall != null && modulesToInstall.length>0){
      for (int i=0; i<modulesToInstall.length; i++) {
        try {
          //remove module from db (in case it is already there)
          ImportModuleData.setInDevelopment(pool, modulesToInstall[i].getModuleID());
          ImportModuleData.deleteDependencies(pool, modulesToInstall[i].getModuleID());
          ImportModuleData.deleteDBPrefix(pool, modulesToInstall[i].getModuleID());
          ImportModuleData.deleteModule(pool, modulesToInstall[i].getModuleID());
        } catch (Exception e) {
          e.printStackTrace();
          addLog("Error deleting module "+modulesToInstall[i].getName()+" from db. "+e.getMessage() ,MSG_ERROR);
        }
        
        File f = new File(obDir+"/modules/"+modulesToInstall[i].getPackageName());
        if (f.exists()) {
          if (Utility.deleteDir(f)) addLog("@DeletedDirectory@ "+f.getAbsolutePath(),MSG_ERROR);
          else addLog("@CouldntDeleteDirectory@ "+f.getAbsolutePath(),MSG_ERROR);
        }
      }
    }
    
    if (modulesToUpdate != null && modulesToUpdate.length>0) {
      for (int i=0; i<modulesToUpdate.length; i++) {
        if (modulesToUpdate[i].getModuleID().equals("0")) { //restore core
          File core[]=getCore();
          Utility.deleteDir(core);
          try {
            Zip.unzip(obDir+"/backup_install/"+modulesToUpdate[i].getPackageName()+"-"+modulesToUpdate[i].getVersionNo()+".zip", obDir);
          } catch(Exception e) {e.printStackTrace();}
        } else { //restore regular modules
          try {
            Utility.deleteDir(new File(obDir+"/modules/"+modulesToUpdate[i].getPackageName()));
            Zip.unzip(obDir+"/backup_install/"+modulesToUpdate[i].getPackageName()+"-"+modulesToUpdate[i].getVersionNo()+".zip", 
                      obDir+"/modules/"+modulesToUpdate[i].getPackageName());
          } catch(Exception e) {e.printStackTrace();}
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
      log4j.warn("  "+errors.getMessage());
    }else{
      if (modulesToInstall!=null && modulesToInstall.length>0) {
        log4j.info("Modules to install:");
        for (int i=0; i<modulesToInstall.length;i++) {
          log4j.info(modulesToInstall[i].getName()+" "+modulesToInstall[i].getVersionNo());
        }
      }
      if (modulesToUpdate!=null && modulesToUpdate.length>0) {
        log4j.info("Modules to update:");
        for (int i=0; i<modulesToUpdate.length;i++) {
          log4j.info(modulesToUpdate[i].getName()+" "+modulesToUpdate[i].getVersionNo());
        }
      }
    }
  }
  
  /**
   * Adds a message with a log level to the current instance log.
   * 
   */
  private void addLog(String m, int level){
    log4j.info(m);
    if (level>logLevel){
      logLevel = level;
      log = new StringBuffer(m);
    } else if (level==logLevel)
      log.append(m+"\n");
  }
  
  /**
   * Receives a  Vector<DynaBean> and tranforms it to a Module[]
   */  
  private Module[] dyanaBeanToModules(Vector<DynaBean> dynModulesToInstall, Vector<DynaBean> dynDependencies) {
    Module[] rt = new Module[dynModulesToInstall.size()];
    int i = 0;
    for (DynaBean dynModule : dynModulesToInstall) {
      rt[i] = new Module();
      rt[i].setModuleID((String)dynModule.get("AD_MODULE_ID"));
      rt[i].setVersionNo((String)dynModule.get("VERSION"));
      rt[i].setName((String)dynModule.get("NAME"));
      rt[i].setLicenseAgreement((String)dynModule.get("LICENSE"));
      rt[i].setLicenseType((String)dynModule.get("LICENSETYPE"));
      rt[i].setPackageName((String)dynModule.get("JAVAPACKAGE"));
      rt[i].setType((String)dynModule.get("TYPE"));
      rt[i].setDescription((String)dynModule.get("DESCRIPTION"));
      rt[i].setHelp((String)dynModule.get("HELP"));
      rt[i].setDependencies(dyanaBeanToDependencies(dynDependencies, rt[i].getModuleID()));
      rt[i].setModuleVersionID((String)dynModule.get("AD_MODULE_ID")); //To show details in local ad_module_id is used
      i++;
    }
    return rt;
  }

  /**
   * Returns the dependencies in Vector<DynaBean> dynDependencies for the ad_module_id module
   * as a ModuleDependency[], used by dyanaBeanToModules method
   *  
   */
  private ModuleDependency[] dyanaBeanToDependencies(Vector<DynaBean> dynDependencies, String ad_module_id) {
    ArrayList<ModuleDependency> dep =new ArrayList<ModuleDependency>();
    for (DynaBean dynModule : dynDependencies) {
      if (((String)dynModule.get("AD_MODULE_ID")).equals(ad_module_id)){
        ModuleDependency md = new ModuleDependency();
        md.setModuleID((String)dynModule.get("AD_DEPENDENT_MODULE_ID"));
        md.setVersionStart((String)dynModule.get("STARTVERSION"));
        md.setVersionEnd((String)dynModule.get("ENDVERSION"));
        dep.add(md);
      }
    }
    ModuleDependency rt[] = new ModuleDependency[dep.size()];
    for (int i=0;i<rt.length;i++){
      rt[i]=dep.get(i);
    }
    return rt;
  }

  /**
   * Adds the classpath entries to .classpath file from the modules in the Vector<DynaBean>
   * 
   */
  private void addDynaClasspathEntries(Vector<DynaBean> modulesToInstall) throws Exception{
    if (!(new File(obDir+"/.classpath").exists())) {
      log4j.info("No "+obDir+"/.classpath file");
      return;
    }
    log4j.info("Adding .claspath entries");
    DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
    Document doc = docBuilder.parse(obDir+"/.classpath");
    for (DynaBean module : modulesToInstall) {
       String dir = obDir+"/modules/"+(String)module.get("JAVAPACKAGE")+"/src";
       if (new File(dir).exists()) {
         addClassPathEntry(doc, dir);
       } else {
         log4j.info(dir+" does not exist, no claspath entry added");
       }
    }

    //Save the modified xml file to .classpath file
    Transformer transformer = TransformerFactory.newInstance().newTransformer();
    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
    FileOutputStream fout = new FileOutputStream(obDir+"/.classpath");
    StreamResult result = new StreamResult(fout);
    DOMSource source = new DOMSource(doc);
    transformer.transform(source, result);
    fout.close();
  }
  
  /**
   * Adds a single classpath entry to the xml file
   */
  private void addClassPathEntry(Document doc, String dir) throws Exception {
    log4j.info("adding entry for directory"+dir);
    Node root = doc.getFirstChild();
    Node classpath = doc.createElement("classpathentry");
    NamedNodeMap cpAttributes = classpath.getAttributes();
    
    Attr attr = doc.createAttribute("kind");
    attr.setValue("src");
    cpAttributes.setNamedItem(attr);
    attr  = doc.createAttribute("including");
    attr.setValue("**/*.java");
    cpAttributes.setNamedItem(attr);
    attr  = doc.createAttribute("src");
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
  private ByteArrayInputStream getCurrentEntryStream(ZipInputStream obxInputStream) throws Exception {
    ByteArrayOutputStream fout=new ByteArrayOutputStream();
    for (int c = obxInputStream.read(); c != -1; c = obxInputStream.read()) {
      fout.write(c);
    }
    fout.close();
    ByteArrayInputStream ba = new ByteArrayInputStream(fout.toByteArray());
    return ba;
  }
  
  /**
   * Inserts in database the Vector<DynaBean> with its dependencies
   * 
   * @param modulesToInstall
   * @param dependencies
   * @throws Exception
   */
  private void insertDynaModulesInDB(Vector<DynaBean> modulesToInstall, Vector<DynaBean> dependencies, Vector<DynaBean> dbPrefix) throws Exception {
    Properties obProperties = new Properties();
    obProperties.load(new FileInputStream(obDir+"/config/Openbravo.properties"));

    String url = obProperties.getProperty("bbdd.url")+(obProperties.getProperty("bbdd.rdbms").equals("POSTGRE")?"/"+obProperties.getProperty("bbdd.sid"):"");
   
    BasicDataSource ds = new BasicDataSource();
    ds.setDriverClassName(obProperties.getProperty("bbdd.driver"));
    ds.setUrl(url);
    ds.setUsername(obProperties.getProperty("bbdd.user"));
    ds.setPassword(obProperties.getProperty("bbdd.password"));    
    
    Platform platform = PlatformFactory.createNewPlatformInstance(ds);
    
    Integer seqNo = new Integer(ImportModuleData.selectSeqNo(pool));
    
    for (DynaBean module : modulesToInstall) {
      seqNo += 10;
      module.set("ISINDEVELOPMENT","Y");
      module.set("ISDEFAULT","N");
      module.set("STATUS","I");
      module.set("SEQNO", seqNo);
      module.set("UPDATE_AVAILABLE", null);
      log4j.info("Inserting in DB info for module: "+module.get("NAME"));
      platform.updateinsert(ds.getConnection(), db, module);
      addLog("@ModuleInstalled@ "+module.get("NAME")+" - "+module.get("VERSION"), MSG_SUCCESS);
    }
    for (DynaBean module : dependencies) {
      platform.updateinsert(ds.getConnection(), db, module);
    }
    for (DynaBean module : dbPrefix) {
      platform.updateinsert(ds.getConnection(), db, module);
    }
  }
  
  /**
   * Returns all the modules and dependencies described within the obx file (as InputStream)
   * @param dynModulesToInstall
   * @param dependencies
   * @param obx
   * @throws Exception
   */
  private void getModulesFromObx(Vector<DynaBean> dynModulesToInstall, Vector<DynaBean> dependencies, Vector<DynaBean> dbprefix,InputStream obx) throws Exception {
    ZipInputStream obxInputStream = new ZipInputStream(obx);
    ZipEntry entry = null;
    while (((entry = obxInputStream.getNextEntry()) != null)) {
     
      if (entry.getName().endsWith(".obx")) { //If it is a new module install it
        ByteArrayInputStream ba = getCurrentEntryStream(obxInputStream);
        obxInputStream.closeEntry();
        getModulesFromObx(dynModulesToInstall, dependencies, dbprefix, ba);
      } else if (entry.getName().replace("\\", "/").endsWith("src-db/database/sourcedata/AD_MODULE.xml")) {
        dynModulesToInstall.addAll(getEntryDynaBeans(obxInputStream));
        obxInputStream.closeEntry();
      } else if (entry.getName().replace("\\", "/").endsWith("src-db/database/sourcedata/AD_MODULE_DEPENDENCY.xml")) {
        dependencies.addAll(getEntryDynaBeans(obxInputStream));
        obxInputStream.closeEntry();
      } else if (entry.getName().replace("\\", "/").endsWith("src-db/database/sourcedata/AD_MODULE_DBPREFIX.xml")) {
        dbprefix.addAll(getEntryDynaBeans(obxInputStream));
        obxInputStream.closeEntry();
      }
    }
    obxInputStream.close();
  }
  
  /**
   * Returns all the modules and dependencies described within the obx file (as InputStream), it takes
   * into account whether the module is already installed to set it as a module to update
   * 
   * @param dynModulesToInstall
   * @param dependencies
   * @param obx
   * @throws Exception
   */
  private void getModulesFromObxLocal(InputStream obx) throws Exception {
    ZipInputStream obxInputStream = new ZipInputStream(obx);
    ZipEntry entry = null;
    while (((entry = obxInputStream.getNextEntry()) != null)) {
     
      if (entry.getName().endsWith(".obx")) { //If it is a new module install it
        ByteArrayInputStream ba = getCurrentEntryStream(obxInputStream);
        obxInputStream.closeEntry();
        getModulesFromObxLocal(ba);
      } else if (entry.getName().replace("\\", "/").endsWith("src-db/database/sourcedata/AD_MODULE.xml")) {
        Vector<DynaBean> v = getEntryDynaBeans(obxInputStream);
        for (DynaBean module : v) {
          if (ImportModuleData.moduleInstalled(pool, (String)module.get("AD_MODULE_ID"))) {
            //TODO: Check higher version
            dynModulesToUpdate.add(module);
          } else {
            dynModulesToInstall.add(module);
          }
        }
          
        obxInputStream.closeEntry();
      } else if (entry.getName().replace("\\", "/").endsWith("src-db/database/sourcedata/AD_MODULE_DEPENDENCY.xml")) {
        dependencies.addAll(getEntryDynaBeans(obxInputStream));
        obxInputStream.closeEntry();
      }
    }
    obxInputStream.close();
  }
  
  /**
   * Reads a ZipInputStream and returs a Vector<DynaBean> with its modules
   * 
   * @param obxInputStream
   * @return
   * @throws Exception
   */
  private Vector<DynaBean> getEntryDynaBeans(ZipInputStream obxInputStream) throws Exception{
    ByteArrayInputStream ba = getCurrentEntryStream(obxInputStream);
    DatabaseDataIO io = new DatabaseDataIO();
    DataReader dr = io.getConfiguredCompareDataReader(db);
    dr.getSink().start();
    dr.parse(ba);
    return ((DataToArraySink)dr.getSink()).getVector();
  }
  
  /**
   * Installs or updates the modules in the obx file
   * 
   * @param obx
   * @param moduleID The ID for the current module to install
   * @throws Exception
   */
  private void installModule(InputStream obx, String moduleID) throws Exception {
      if (!(new File(obDir+"/modules").canWrite())) {
        addLog("@CannotWriteDirectory@ "+obDir+"/modules", MSG_ERROR);
        throw new Exception("Cannot write on directory: "+obDir+"/modules");
      }
      
      ZipInputStream obxInputStream = new ZipInputStream(obx);
      ZipEntry entry = null;
      while ((entry = obxInputStream.getNextEntry()) != null) {
        if (entry.getName().endsWith(".obx")) { //If it is a new module install it
          if (installLocally) {
            ByteArrayOutputStream fout=new ByteArrayOutputStream();
            for (int c = obxInputStream.read(); c != -1; c = obxInputStream.read()) {
              fout.write(c);
            }
            fout.close();
            ByteArrayInputStream ba = new ByteArrayInputStream(fout.toByteArray());
            ByteArrayInputStream ba1 = new ByteArrayInputStream(fout.toByteArray());
            
            //Obtain the ID for the module to be installed (this is done for core)
            Vector<DynaBean> obxModule = new Vector<DynaBean>();
            getModulesFromObx(obxModule, new Vector<DynaBean>(), new Vector<DynaBean>(), ba1);
            
            installModule(ba, (String) obxModule.get(0).get("AD_MODULE_ID"));
          } //If install remotely it is no necessary to install the .obx because it will be get from CR 
          obxInputStream.closeEntry();
        } else { 
          
          String fileName = obDir+"/modules/"+entry.getName().replace("\\","/");
          File entryFile = new File(fileName);
          //Check whether the directory exists, if not create
          if (entryFile.getParent() != null) { 
            File dir = new File(entryFile.getParent()); 
            if (!dir.exists()) {
              log4j.info("Created dir: "+dir.getAbsolutePath());
              dir.mkdirs();
            }
          }
          //Unzip the file
          log4j.info("Installing " + fileName);
          FileOutputStream fout = new FileOutputStream(entryFile);
          for (int c = obxInputStream.read(); c != -1; c = obxInputStream.read()) {
            fout.write(c);
          }
          fout.close();
          obxInputStream.closeEntry();
        }
      }
      obxInputStream.close();
  }
  
  public boolean getIsLocal(){
    return installLocally;
  }
  
  /**
   * Inserts log in ad_module_log table
   * 
   */
  private void insertDBLog(){
    try {
      String user = vars==null?"0":vars.getUser();
      if (modulesToInstall!=null && modulesToInstall.length>0) {
        for (int i=0; i<modulesToInstall.length; i++){
          ImportModuleData.insertLog(pool, user, modulesToInstall[i].getModuleID(), 
                                           modulesToInstall[i].getModuleVersionID(), modulesToInstall[i].getName(),
                                           "Installed module "+modulesToInstall[i].getName()+" - "+modulesToInstall[i].getVersionNo(), "I");
        }
      }
      if (modulesToUpdate!=null && modulesToUpdate.length>0) {
          for (int i=0; i<modulesToUpdate.length; i++){
            ImportModuleData.insertLog(pool, user, modulesToUpdate[i].getModuleID(), 
                                              modulesToUpdate[i].getModuleVersionID(), modulesToUpdate[i].getName(),
                                             "Updated module "+modulesToUpdate[i].getName()+" to version "+modulesToUpdate[i].getVersionNo(), "U");
          }
      } 
    } catch (ServletException e) {e.printStackTrace();}
  }
 
  /**
   * Scans for updates for the existent modules and sets and returs the list of modules
   * that have updates available
   * 
   * @param conn
   * @param vars
   * @return
   */
  public static String[] scanForUpdates(ConnectionProvider conn, VariablesSecureApp vars){
    try {
      ArrayList<String> rt=new ArrayList<String>();
      String user = vars==null?"0":vars.getUser();
      ImportModuleData.insertLog(conn, user, "", "", "", "Scanning For Updates", "S");
      WebServiceImplServiceLocator loc;
      WebServiceImpl ws=null;
      SimpleModule[] updates;
      try {
        loc = new WebServiceImplServiceLocator();
        ws = (WebServiceImpl) loc.getWebService();
        HashMap<String, String> currentlyInstalledModules = getInstalledModules(conn);
        updates =  ws.moduleScanForUpdates(currentlyInstalledModules); 
      } catch (Exception e) {
        e.printStackTrace();
        try {
          ImportModuleData.insertLog(conn, user, "", "", "",  "Scan for updates: Couldn't contact with webservice server", "E");
        } catch(ServletException ex) {ex.printStackTrace();}
        return new String[0];
      }
      

      if (updates != null && updates.length>0) {
        
        for (int i=0; i<updates.length; i++) {
          if(!ImportModuleData.existsVersion(conn, updates[i].getVersionNo())) {
            ImportModuleData.updateNewVersionAvailable(conn, updates[i].getVersionNo(), updates[i].getModuleVersionID(), updates[i].getModuleID());
            ImportModuleData.insertLog(conn, user, updates[i].getModuleID(), updates[i].getModuleVersionID(), updates[i].getName(), 
                                      "Found new version "+updates[i].getVersionNo()+" for module "+updates[i].getName(), "S");
            rt.add(updates[i].getModuleID());
          }
        }
      }
      try {
        ImportModuleData.insertLog(conn, (vars==null?"0":vars.getUser()), "", "", "",  "Total: found "+rt.size()+" updates", "S");
      } catch(ServletException ex) {ex.printStackTrace();}
      String[] rt1 = new String[rt.size()];
      rt.toArray(rt1);
      return rt1;
    } catch (Exception e) {
      e.printStackTrace();
      try {
        ImportModuleData.insertLog(conn, (vars==null?"0":vars.getUser()), "", "", "",  "Scan for updates: Error: "+e.toString(), "E");
      } catch(ServletException ex) {ex.printStackTrace();}
      return new String[0];
    }
  }
  
  /**
   * Returns the current installed modules with its version
   * 
   * @param conn ConnectionProvider needed as it is a static method
   * @return HashMap<String,String> -> <ModuleId, VersionNo>
   */
  public static HashMap<String, String> getInstalledModules(ConnectionProvider conn) {
    HashMap<String, String> rt = new HashMap<String, String>();
    ImportModuleData data[]=null;
    try {
      data = ImportModuleData.selectInstalled(conn);
    } catch (Exception e) {e.printStackTrace();}
    if (data!=null) {
      for (int i=0; i<data.length; i++) {
        rt.put(data[i].adModuleId, data[i].version);
      }
    }
    return rt;
  }

  /**
   * Returns a File array with the directories that are part of core
   * @return
   */
  private File[] getCore() {
    File[] file = new File[10];
    file[0]= new File(obDir+"/legal");
    file[1]= new File(obDir+"/lib");
    file[2]= new File(obDir+"/WebContent");
    file[3]= new File(obDir+"/src-core");
    file[4]= new File(obDir+"/src-db");
    file[5]= new File(obDir+"/src-gen");
    file[6]= new File(obDir+"/src-trl");
    file[7]= new File(obDir+"/src-wad");
    file[8]= new File(obDir+"/src");
    file[9]= new File(obDir+"/web");
    return file;
  }
  
  /**
   * Returns the module with the ID that is in the module to install or update.
   *  
   * @param moduleID
   * @return
   */  
  public Module getModule(String moduleID) {
    for (int i=0; i<modulesToInstall.length; i++) {
      if (modulesToInstall[i].getModuleID().equals(moduleID)) return modulesToInstall[i];
    }
    for (int i=0; i<modulesToUpdate.length; i++) {
      if (modulesToUpdate[i].getModuleID().equals(moduleID)) return modulesToUpdate[i];
    }
    return null;
  }
  
  /**
   * Just for testing purposes
   * 
   */
  
  private static SimpleModule[] getModuleTest(){
    SimpleModule[] rt = new SimpleModule[2];

    rt[0] = new SimpleModule();
    rt[0].setModuleID("2");
    rt[0].setName("test");
    rt[0].setModuleVersionID("45");
    rt[0].setVersionNo("1.5");
    rt[0].setUpdateDescription("The new testing version");
    
    rt[1] = new SimpleModule();
    rt[1].setModuleID("0");
    rt[1].setName("core");
    rt[1].setModuleVersionID("44");
    rt[1].setVersionNo("2.6");
    rt[1].setUpdateDescription("The new core version");
    return rt;
  }
  public static void main(String[] args) {
    try {
      ImportModule im = new ImportModule(new CPStandAlone ("/ws/modularity/openbravo/config/Openbravo.properties"), "/ws/modularity/openbravo", null);
      im.checkDependenciesFileName("/ws/modularity/openbravo/test1-1.obx");
      im.execute("/ws/modularity/openbravo/test1-1.obx");
      log4j.info("END");
    } catch (Exception e) {e.printStackTrace();}
  }
}
