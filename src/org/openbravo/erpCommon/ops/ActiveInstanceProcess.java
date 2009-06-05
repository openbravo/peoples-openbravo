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
 * All portions are Copyright (C) 2009 Openbravo SL 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.erpCommon.ops;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import org.apache.log4j.Logger;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.model.ad.system.System;
import org.openbravo.scheduling.Process;
import org.openbravo.scheduling.ProcessBundle;

public class ActiveInstanceProcess implements Process {

  private static final Logger log = Logger.getLogger(ActiveInstanceProcess.class);
  // TODO: change this testing machine by actual butler one
  private static final String BUTLER_URL = "http://localhost2:8882/openbravo/activate";

  @Override
  public void execute(ProcessBundle bundle) throws Exception {
    String publicKey = (String) bundle.getParams().get("publicKey");
    String purpose = (String) bundle.getParams().get("purpose");
    String instanceNo = (String) bundle.getParams().get("instanceNo");
    OBError msg = new OBError();

    bundle.setResult(msg);

    // HeartbeatProcess hp = new HeartbeatProcess();
    // // System.out.println("in: " + hp.isInternetAvailable(bundle.getConnection()));
    // String CERT_ALIAS = "openbravo-butler";
    //
    // URL url = null;
    // try {
    // url = new URL("http://localhost:8881/butler/activate");
    // } catch (MalformedURLException e) { // Won't happen
    // log.error(e.getMessage(), e);
    // }
    //
    // System.out.println("r:" + HttpsUtils.sendSecure(url, "oo", CERT_ALIAS, "changeit"));
    // TODO: once actual machine is ready do ssl call
    String[] result = send(publicKey, purpose, instanceNo);

    if (result.length == 2 && result[0] != null && result[1] != null
        && result[0].equals("@Success@")) {
      // now we have the activation key, lets save it
      System sys = OBDal.getInstance().get(System.class, "0");
      sys.setActivationKey(result[1]);
      sys.setInstanceKey(publicKey);
      ActivationKey ak = new ActivationKey();
      if (ak.isActive()) {
        msg.setType("Success");
        msg.setMessage(result[0]);
      } else {
        msg.setType("Error");
        msg.setMessage(ak.getErrorMessage());
      }

    } else {
      // If there is error do not save keys, thus we maitain previous ones in case they were valid
      msg.setType("Error");
      msg.setMessage(result[0]);
    }

  }

  private String[] send(String publickey, String purpose, String instanceNo) throws Exception {
    log.debug("Sending request");
    String content = "publickey=" + URLEncoder.encode(publickey, "utf-8");
    content += "&purpose=" + purpose;
    if (instanceNo != null && !instanceNo.equals(""))
      content += "&instanceNo=" + instanceNo;

    URL url = new URL(BUTLER_URL);
    HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();

    urlConn.setRequestProperty("Keep-Alive", "300");
    urlConn.setRequestProperty("Connection", "keep-alive");
    urlConn.setRequestMethod("POST");
    urlConn.setDoInput(true);
    urlConn.setDoOutput(true);
    urlConn.setUseCaches(false);
    urlConn.setAllowUserInteraction(false);

    urlConn.setRequestProperty("Content-type", "application/x-www-form-urlencoded");
    urlConn.setRequestProperty("Content-length", "" + content.length());

    DataOutputStream out = new DataOutputStream(urlConn.getOutputStream());

    out.writeBytes(content);
    out.flush();
    out.close();
    // get input connection
    BufferedReader in = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
    String msg = in.readLine();
    String result[] = new String[2];
    result[0] = msg;
    String line;

    log.debug("Response message:" + msg);
    while ((line = in.readLine()) != null) {
      log.debug("Content:" + line);
      result[1] = line;
    }
    in.close();
    return result;

  }
}
