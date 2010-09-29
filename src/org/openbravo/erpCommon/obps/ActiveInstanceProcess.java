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
 * All portions are Copyright (C) 2009 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.erpCommon.obps;

import java.net.URL;
import java.net.URLEncoder;

import org.apache.log4j.Logger;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.HttpsUtils;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.model.ad.module.Module;
import org.openbravo.model.ad.system.System;
import org.openbravo.model.ad.system.SystemInformation;
import org.openbravo.scheduling.Process;
import org.openbravo.scheduling.ProcessBundle;

public class ActiveInstanceProcess implements Process {

  private static final Logger log = Logger.getLogger(ActiveInstanceProcess.class);
  private static final String BUTLER_URL = "https://butler.openbravo.com:443/heartbeat-server/activate";
  private static final String CERT_ALIAS = "openbravo-butler";

  @Override
  public void execute(ProcessBundle bundle) throws Exception {
    String publicKey = (String) bundle.getParams().get("publicKey");
    String purpose = (String) bundle.getParams().get("purpose");
    String instanceNo = (String) bundle.getParams().get("instanceNo");
    Boolean activate = (Boolean) bundle.getParams().get("activate");
    OBError msg = new OBError();

    bundle.setResult(msg);

    if (!HttpsUtils.isInternetAvailable()) {
      msg.setType("Error");
      msg.setMessage("@WSError@");
    }

    String[] result = send(publicKey, purpose, instanceNo, activate);

    if (result.length == 2 && result[0] != null && result[1] != null
        && result[0].equals("@Success@")) {
      // now we have the activation key, lets save it

      SystemInformation sysInfo = OBDal.getInstance().get(SystemInformation.class, "0");
      sysInfo.setInstancePurpose(purpose);
      ActivationKey ak = new ActivationKey(publicKey, result[1]);
      String nonAllowedMods = ak.verifyInstalledModules();
      if (!nonAllowedMods.isEmpty()) {
        msg.setType("Error");
        msg.setMessage("@LicenseWithoutAccessTo@ " + nonAllowedMods);
      } else {
        System sys = OBDal.getInstance().get(System.class, "0");
        sys.setActivationKey(result[1]);
        sys.setInstanceKey(publicKey);
        ActivationKey.setInstance(ak);
        if (ak.isActive()) {
          msg.setType("Success");
          msg.setMessage(result[0]);
        } else {
          msg.setType("Error");
          msg.setMessage(ak.getErrorMessage());
        }
      }
    } else {
      // If there is error do not save keys, thus we maintain previous ones in case they were valid
      msg.setType("Error");
      msg.setMessage(result[0]);
      log.error(result[0]);
    }

  }

  /**
   * Sends the request for the activation key.
   * 
   * @param publickey
   *          Instance's public key
   * @param purpose
   *          Instance's purpose
   * @param instanceNo
   *          current instance number (for reactivation purposes)
   * @param activate
   *          activate (true) or cancel (false)
   * @return returns a String[] with 2 elements, the first one in the message (@Success@ in case of
   *         success) and the second one the activation key
   * @throws Exception
   */
  private String[] send(String publickey, String purpose, String instanceNo, boolean activate)
      throws Exception {
    log.debug("Sending request");
    String content = "publickey=" + URLEncoder.encode(publickey, "utf-8");
    content += "&purpose=" + purpose;
    if (!activate) {
      content += "&cancel=Y";
    }
    if (instanceNo != null && !instanceNo.equals(""))
      content += "&instanceNo=" + instanceNo;

    try {
      OBContext.setAdminMode();
      Module core = OBDal.getInstance().get(Module.class, "0");
      content += "&erpversion=" + core.getVersion();
    } finally {
      OBContext.restorePreviousMode();
    }

    URL url = new URL(BUTLER_URL);
    try {
      String result = HttpsUtils.sendSecure(url, content);
      log.debug("Activation key response:" + result);

      return result.split("\n");
    } catch (Exception e) {
      String result[] = { "@HB_SECURE_CONNECTION_ERROR@", "" };
      log.error("Error connecting server", e);
      return result;
    }

  }
}
