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

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.apache.axis.AxisFault;
import org.apache.log4j.Logger;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.data.FieldProvider;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.obps.ActivationKey;
import org.openbravo.erpCommon.utility.HttpsUtils;
import org.openbravo.model.ad.module.Module;
import org.openbravo.model.ad.module.ModuleDependency;
import org.openbravo.services.webservice.WebServiceImpl;
import org.openbravo.services.webservice.WebServiceImplServiceLocator;

/**
 * This class implements different utilities related to modules
 * 
 * 
 */
public class ModuleUtiltiy {
  protected static Logger log4j = Logger.getLogger(ModuleUtiltiy.class);

  /**
   * It receives an ArrayList<String> with modules IDs and returns the same list ordered taking into
   * account the module dependency tree.
   * <p/>
   * Note that the module list must be a complete list of modules, no dependencies will be checked
   * for more than one level of deep, this means that passing an incomplete list might not be
   * ordered correctly.
   * 
   * @param modules
   *          List of module to order
   * @return modules list ordered
   * @throws Exception
   */
  public static List<String> orderByDependency(List<String> modules) throws Exception {

    Map<String, List<String>> modsWithDeps = getModsDeps(modules);
    List<String> rt = orderDependencies(modsWithDeps);
    return rt;
  }

  /**
   * Deprecated use {@link ModuleUtiltiy#orderByDependency(ArrayList)} instead
   * 
   * @param conn
   * @param modules
   * @return
   * @throws Exception
   */
  @Deprecated
  public static ArrayList<String> orderByDependency(ConnectionProvider conn,
      ArrayList<String> modules) {
    try {
      return (ArrayList<String>) orderByDependency(modules);
    } catch (Exception e) {
      log4j.error("error in orderByDependency", e);
      return modules;
    }
  }

  /**
   * Modifies the passed modules {@link FieldProvider} parameter ordering it taking into account
   * dependencies.
   * <p/>
   * 
   * @param modules
   *          {@link FieldProvider} that will be sorted. It must contain at least a field named
   *          <i>adModuleId</i>
   * @throws Exception
   */
  public static void orderModuleByDependency(FieldProvider[] modules) throws Exception {
    boolean adminMode = OBContext.getOBContext().isInAdministratorMode();
    OBContext.getOBContext().setInAdministratorMode(true);
    try {
      List<Module> allModules = OBDal.getInstance().createCriteria(Module.class).list();
      ArrayList<String> allMdoulesId = new ArrayList<String>();
      for (Module mod : allModules) {
        allMdoulesId.add(mod.getId());
      }
      List<String> modulesOrder = orderByDependency(allMdoulesId);

      FieldProvider[] fpModulesOrder = new FieldProvider[modules.length];
      int i = 0;
      for (String modId : modulesOrder) {
        for (int j = 0; j < modules.length; j++) {
          if (modules[j].getField("adModuleId").equals(modId)) {
            fpModulesOrder[i] = modules[j];
            i++;
          }
        }
      }

      for (int j = 0; j < modules.length; j++) {
        modules[j] = fpModulesOrder[j];
      }
    } finally {
      OBContext.getOBContext().setInAdministratorMode(adminMode);
    }

  }

  /**
   * Deprecated, use instead {@link ModuleUtiltiy#orderModuleByDependency(FieldProvider[])}
   * 
   * @param pool
   * @param modules
   */
  @Deprecated
  public static void orderModuleByDependency(ConnectionProvider pool, FieldProvider[] modules) {
    try {
      orderModuleByDependency(modules);
    } catch (Exception e) {
      log4j.error("Error in orderModuleByDependency", e);
    }
  }

  /**
   * Orders modules by dependencies. It adds to a List the modules that have not dependencies to the
   * ones in the list and calls itself recursively
   */
  private static List<String> orderDependencies(Map<String, List<String>> modsWithDeps)
      throws Exception {
    ArrayList<String> rt = new ArrayList<String>();

    for (String moduleId : modsWithDeps.keySet()) {
      if (noDependenciesFromModule(moduleId, modsWithDeps)) {
        rt.add(moduleId);
      }
    }

    for (String modId : rt) {
      modsWithDeps.remove(modId);
    }

    if (rt.size() == 0) {
      throw new Exception("Recursive module dependencies found!" + modsWithDeps.size());
    }

    if (modsWithDeps.size() != 0) {
      rt.addAll(orderDependencies(modsWithDeps));
    }
    return rt;
  }

  /**
   * Checks the module has not dependencies to other modules in the list
   * 
   */
  private static boolean noDependenciesFromModule(String checkModule,
      Map<String, List<String>> modsWithDeps) {

    List<String> moduleDependencies = modsWithDeps.get(checkModule);

    for (String module : modsWithDeps.keySet()) {
      if (moduleDependencies.contains(module)) {
        return false;
      }
    }
    return true;
  }

  /**
   * Returns a Map with all the modules and their dependencies
   */
  private static Map<String, List<String>> getModsDeps(List<String> modules) {
    Map<String, List<String>> rt = new HashMap<String, List<String>>();
    for (String moduleId : modules) {
      Module module = OBDal.getInstance().get(Module.class, moduleId);
      ArrayList<String> deps = new ArrayList<String>();
      for (ModuleDependency dep : module.getModuleDependencyList()) {
        deps.add(dep.getDependentModule().getId());
      }
      rt.put(moduleId, deps);
    }
    return rt;
  }

  static RemoteModule getRemoteModule(ImportModule im, String moduleVersionID) {
    RemoteModule remoteModule = new RemoteModule();
    WebServiceImplServiceLocator loc;
    WebServiceImpl ws = null;
    String strUrl = "";
    boolean isCommercial;

    try {
      loc = new WebServiceImplServiceLocator();
      ws = loc.getWebService();
    } catch (final Exception e) {
      log4j.error(e);
      im.addLog("@CouldntConnectToWS@", ImportModule.MSG_ERROR);
      try {
        ImportModuleData.insertLog(ImportModule.pool, (im.vars == null ? "0" : im.vars.getUser()),
            "", "", "", "Couldn't contact with webservice server", "E");
      } catch (final ServletException ex) {
        log4j.error(ex);
      }
      remoteModule.setError(true);
      return remoteModule;
    }

    try {
      isCommercial = ws.isCommercial(moduleVersionID);
      strUrl = ws.getURLforDownload(moduleVersionID);
    } catch (AxisFault e1) {
      im.addLog("@" + e1.getFaultCode() + "@", ImportModule.MSG_ERROR);
      remoteModule.setError(true);
      return remoteModule;
    } catch (RemoteException e) {
      im.addLog(e.getMessage(), ImportModule.MSG_ERROR);
      remoteModule.setError(true);
      return remoteModule;
    }

    if (isCommercial && !ActivationKey.isActiveInstance()) {
      im.addLog("@NotCommercialModulesAllowed@", ImportModule.MSG_ERROR);
      remoteModule.setError(true);
      return remoteModule;
    }

    try {
      URL url = new URL(strUrl);
      HttpURLConnection conn = null;

      if (strUrl.startsWith("https://")) {
        ActivationKey ak = new ActivationKey();
        String instanceKey = "obinstance=" + URLEncoder.encode(ak.getPublicKey(), "utf-8");
        conn = HttpsUtils.sendHttpsRequest(url, instanceKey, "localhost-1", "changeit");
      } else {
        conn = (HttpURLConnection) url.openConnection();
        conn.setRequestProperty("Keep-Alive", "300");
        conn.setRequestProperty("Connection", "keep-alive");
        conn.setRequestMethod("GET");
        conn.setDoInput(true);
        conn.setDoOutput(true);
        conn.setUseCaches(false);
        conn.setAllowUserInteraction(false);
      }

      if (conn.getResponseCode() == HttpServletResponse.SC_OK) {
        // OBX is ready to be used
        remoteModule.setObx(conn.getInputStream());
        String size = conn.getHeaderField("Content-Length");
        if (size != null) {
          remoteModule.setSize(new Integer(size));
        }
        return remoteModule;
      }

      // There is an error, let's check for a parseable message
      String msg = conn.getHeaderField("OB-ErrMessage");
      if (msg != null) {
        im.addLog(msg, ImportModule.MSG_ERROR);
      } else {
        im.addLog("@ErrorDownloadingOBX@ " + conn.getResponseCode(), ImportModule.MSG_ERROR);
      }
    } catch (Exception e) {
      im.addLog("@ErrorDownloadingOBX@ " + e.getMessage(), ImportModule.MSG_ERROR);
    }
    remoteModule.setError(true);
    return remoteModule;
  }

  /**
   * Obtains remotelly an obx for the desired moduleVersionID
   * 
   * @param im
   *          {@link ImportModule} instance used to add the log
   * @param moduleVersionID
   *          ID for the module version to obtain
   * @return An {@link InputStream} with containing the obx for the module (null if error)
   */
  // public static InputStream getRemoteModule(ImportModule im, String moduleVersionID) {
  // RemoteModule module = getRemoteModule(im, moduleVersionID);
  // return module.getObx();
  //
  // }
}
