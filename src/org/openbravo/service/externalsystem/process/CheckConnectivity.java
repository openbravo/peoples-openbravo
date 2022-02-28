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
 * All portions are Copyright (C) 2022 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.service.externalsystem.process;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.client.application.process.BaseProcessActionHandler;
import org.openbravo.client.application.process.ResponseActionsBuilder.MessageType;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.service.externalsystem.ExternalSystem;
import org.openbravo.service.externalsystem.ExternalSystemFactory;
import org.openbravo.service.externalsystem.ExternalSystemResponse;
import org.openbravo.service.externalsystem.ExternalSystemResponse.Type;

/**
 * Process that checks the connectivity of an HTTP protocol based external system with a given
 * configuration
 */
public class CheckConnectivity extends BaseProcessActionHandler {
  private static final Logger log = LogManager.getLogger();

  @Inject
  private ExternalSystemFactory externalSystemFactory;

  @Override
  protected JSONObject doExecute(Map<String, Object> parameters, String content) {
    try {
      JSONObject data = new JSONObject(content);
      String configurationId = data.getString("inpcExternalSystemId");
      Optional<ExternalSystem> externalSystem = externalSystemFactory
          .getExternalSystem(configurationId);
      if (externalSystem.isPresent()) {
        return externalSystem.get().test(getDataToSend()).thenApply(this::handleResponse).get();
      } else {
        return buildError("C_ConnCheckProcessError");
      }
    } catch (Exception ex) {
      log.error("Connectivity check failed", ex);
      return buildError("C_ConnCheckProcessError");
    }
  }

  private InputStream getDataToSend() {
    JSONObject data = new JSONObject();
    return new ByteArrayInputStream(data.toString().getBytes());
  }

  private JSONObject handleResponse(ExternalSystemResponse response) {
    if (Type.ERROR.equals(response.getType())) {
      return buildError("C_ConnCheckFailed", response.getError());
    }
    return getResponseBuilder()
        .showMsgInProcessView(MessageType.SUCCESS, OBMessageUtils.getI18NMessage("OBUIAPP_Success"),
            OBMessageUtils.getI18NMessage("C_ConnCheckSuccess"))
        .build();
  }

  private JSONObject buildError(String message, String... messageParams) {
    return getResponseBuilder()
        .showMsgInProcessView(MessageType.ERROR, OBMessageUtils.getI18NMessage("OBUIAPP_Error"),
            OBMessageUtils.getI18NMessage(message, messageParams))
        .build();
  }
}
