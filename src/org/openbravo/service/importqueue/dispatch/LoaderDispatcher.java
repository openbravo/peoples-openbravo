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

package org.openbravo.service.importqueue.dispatch;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.service.importqueue.MessageDispatcher;
import org.openbravo.service.importqueue.QueueException;

public class LoaderDispatcher implements MessageDispatcher {

  private static final Logger log = LogManager.getLogger();

  @Inject
  @Any
  private Instance<LoaderProcessor> queueProcessorsList;

  @Override
  public void dispatchMessage(JSONObject message) throws QueueException, JSONException {

    queueProcessorsList.forEach((LoaderProcessor p) -> {
      // Returns success or failure if processed
      // Returns null otherwise
      try {
        p.processMessage(message);
      } catch (QueueException | JSONException e) {
        log.error("Cannot process message.", e);
      }
    });
  }
}
