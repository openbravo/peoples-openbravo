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
 * All portions are Copyright (C) 2018 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.common.actionhandler.createlinesfromprocess;

import java.util.Map;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.base.weld.WeldUtils;
import org.openbravo.client.application.process.BaseProcessActionHandler;
import org.openbravo.common.actionhandler.createlinesfromprocess.util.CreateLinesFromUtil;
import org.openbravo.model.common.invoice.Invoice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract class to be implemented by any process that creates lines from any Openbravo
 * BaseOBObject
 * 
 * @param <T>
 *          Invoice lines will be created from an object whose class extends from the BaseOBObject
 */
abstract class CreateInvoiceLinesFrom<T extends BaseOBObject> extends BaseProcessActionHandler {
  private static final Logger log = LoggerFactory.getLogger(CreateInvoiceLinesFrom.class);

  protected abstract Class<T> getFromClass();

  @Override
  protected JSONObject doExecute(Map<String, Object> parameters, String content) {
    JSONObject jsonRequest = null;
    try {
      // Request Parameters
      jsonRequest = new JSONObject(content);
      final String requestedAction = CreateLinesFromUtil.getRequestedAction(jsonRequest);
      JSONArray selectedLines = CreateLinesFromUtil.getSelectedLines(jsonRequest);
      Invoice currentInvoice = CreateLinesFromUtil.getCurrentInvoice(jsonRequest);

      if (CreateLinesFromUtil.requestedActionIsDoneAndThereAreSelectedOrderLines(requestedAction,
          selectedLines)) {
        // CreateLinesFromProcess is instantiated using Weld so it can use Dependency Injection
        CreateLinesFromProcess createLinesFromProcess = WeldUtils
            .getInstanceFromStaticBeanManager(CreateLinesFromProcess.class);
        int createdInvoiceLinesCount = createLinesFromProcess.createInvoiceLinesFromDocumentLines(
            selectedLines, currentInvoice, getFromClass());
        jsonRequest.put(CreateLinesFromUtil.MESSAGE,
            CreateLinesFromUtil.getSuccessMessage(createdInvoiceLinesCount));
      }
    } catch (Exception e) {
      log.error("Error in Invoice CreateLinesFrom Action Handler", e);

      try {
        if (jsonRequest != null) {
          jsonRequest.put(CreateLinesFromUtil.MESSAGE, CreateLinesFromUtil.getErrorMessage(e));
        }
      } catch (Exception e2) {
        log.error(e.getMessage(), e2);
      }
    }

    return jsonRequest;
  }

}
