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

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.rmi.RemoteException;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.apache.axis.AxisFault;
import org.apache.log4j.Logger;
import org.openbravo.data.FieldProvider;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.obps.ActivationKey;
import org.openbravo.erpCommon.utility.HttpsUtils;
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
   * 
   * @param conn
   *          DB connection
   * @param modules
   *          List of module to order
   * @return modules list ordered
   */
  public static ArrayList<String> orderByDependency(ConnectionProvider conn,
      ArrayList<String> modules) {
    try {
      ArrayList<String> rt = new ArrayList<String>(); // return object
      ArrayList<String> checked = new ArrayList<String>(); // checked list
      // to avoid
      // endless
      // loops

      ArrayList<String> roots = selectRoots(conn); // selects all the root
      // nodes (no
      // dependencies) from
      // the complete tree

      for (int i = 0; i < roots.size(); i++) {
        if (rt.indexOf(roots.get(i)) == -1 && modules.indexOf(roots.get(i)) != -1)
          rt.add(roots.get(i)); // Adds all the root trees in the
        // return list

        checked.add(roots.get(i));
        checkTree(conn, roots.get(i), rt, modules, checked);
      }

      return rt;
    } catch (Exception e) {
      log4j.error(e);
      return modules;
    }
  }

  private static ArrayList<String> selectRoots(ConnectionProvider conn) throws ServletException {
    ArrayList<String> rt = new ArrayList<String>();

    ModuleUtilityData module[] = ModuleUtilityData.selectRootNodes(conn);
    if (module != null) {
      for (int i = 0; i < module.length; i++) {
        rt.add(module[i].adModuleId);
      }
    }

    return rt;
  }

  private static void checkTree(ConnectionProvider conn, String root, ArrayList<String> rt,
      ArrayList<String> list, ArrayList<String> checked) throws ServletException {
    ModuleUtilityData parents[] = ModuleUtilityData.selectParents(conn, root);
    if (parents == null || parents.length == 0)
      return;
    for (int i = 0; i < parents.length; i++) {
      if (checked.indexOf(parents[i].adModuleId) == -1) {
        checked.add(parents[i].adModuleId);
        if (list.indexOf(parents[i].adModuleId) != -1) {
          rt.add(parents[i].adModuleId);
        }
        checkTree(conn, parents[i].adModuleId, rt, list, checked);
      }
    }
  }

  /**
   * Modifies the passed modules {@link FieldProvider} parameter ordering it taking into account
   * dependencies.
   * 
   * @param modules
   *          {@link FieldProvider} that will be sorted. It must contain at least a field named
   *          <i>adModuleId</i>
   */
  public static void orderModuleByDependency(ConnectionProvider pool, FieldProvider[] modules) {
    if (modules == null || modules.length == 0)
      return;
    final ArrayList<String> list = new ArrayList<String>();
    for (int i = 0; i < modules.length; i++) {
      list.add((String) modules[i].getField("adModuleId"));
    }
    final ArrayList<String> orderList = orderByDependency(pool, list);
    final FieldProvider[] rt = new FieldProvider[modules.length];
    int j = 0;
    for (int i = 0; i < orderList.size(); i++) {
      for (FieldProvider module : modules) {
        if (module.getField("adModuleId").equals(orderList.get(i))) {
          rt[j] = module;
          j++;
        }
      }
    }
    modules = rt;
    return;
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
  public static InputStream getRemoteModule(ImportModule im, String moduleVersionID) {
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
      return null;
    }

    try {
      isCommercial = ws.isCommercial(moduleVersionID);
      strUrl = ws.getURLforDownload(moduleVersionID);
    } catch (AxisFault e1) {
      im.addLog("@" + e1.getFaultCode() + "@", ImportModule.MSG_ERROR);
      return null;
    } catch (RemoteException e) {
      im.addLog(e.getMessage(), ImportModule.MSG_ERROR);
      return null;
    }

    if (isCommercial && !ActivationKey.isActiveInstance()) {
      im.addLog("@NotCommercialModulesAllowed@", ImportModule.MSG_ERROR);
      return null;
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
        return conn.getInputStream();
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
    return null;

  }
}
