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

package org.openbravo.service.importqueue.pubsub;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.service.importprocess.ImportEntryManager;
import org.openbravo.service.importqueue.QueuePublication;

@ApplicationScoped
public class ImportEntryPublication implements QueuePublication {

  private static final Logger log = LogManager.getLogger();

  @Inject
  private ImportEntryManager importEntryManager;

  @Override
  public void publish(JSONObject message) {
    try {
      String id = message.getString("messageId");
      String qualifier = message.getString("entrykey");
      importEntryManager.createImportEntry(id, qualifier, message.toString());
    } catch (JSONException e) {
      log.error(e);
      throw new RuntimeException(e);
    }
  }
}
