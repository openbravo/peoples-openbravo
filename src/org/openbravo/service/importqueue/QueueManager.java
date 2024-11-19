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

package org.openbravo.service.importqueue;

import javax.enterprise.context.ApplicationScoped;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.weld.WeldUtils;
import org.openbravo.client.kernel.ApplicationInitializer;
import org.openbravo.service.importqueue.dispatch.LoaderDispatcher;
import org.openbravo.service.importqueue.pubsub.RabbitPublication;
import org.openbravo.service.importqueue.pubsub.RabbitSubscription;

@ApplicationScoped
public class QueueManager implements ApplicationInitializer {

  private QueueSubscription queuesub;
  private QueuePublication queuepub;

  @Override
  public void initialize() {

    MessageDispatcher dispatcher = configureDispatcher();

    // Message Queue (RabbitMQ)
    queuesub = WeldUtils.getInstanceFromStaticBeanManager(RabbitSubscription.class);
    queuesub.subscribe(dispatcher);
    queuepub = WeldUtils.getInstanceFromStaticBeanManager(RabbitPublication.class);

    // // Publishing to the Import Entry Manager
    // queuepub = WeldUtils.getInstanceFromStaticBeanManager(ImportEntryPublication.class);

    // // MemoryQueue
    // queuesub = WeldUtils.getInstanceFromStaticBeanManager(MemoryQueue.class);
    // queuesub.subscribe(dispatcher);
    // queuepub = WeldUtils.getInstanceFromStaticBeanManager(MemoryQueue.class);

    // // Import Entry Manager and RabbitMQ Combined
    // queuesub = WeldUtils.getInstanceFromStaticBeanManager(RabbitSubscription.class);
    // queuesub.subscribe(dispatcher);
    // queuepub = WeldUtils.getInstanceFromStaticBeanManager(ImportEntryPublication.class);
  }

  private MessageDispatcher configureDispatcher() {

    // Dispatcher LoaderDispatcher/LoaderProcessor
    return WeldUtils.getInstanceFromStaticBeanManager(LoaderDispatcher.class);

    // // Dispatcher createImportEntry
    // return WeldUtils.getInstanceFromStaticBeanManager(ImportEntryDispatcher.class);
  }

  public void publishMessage(JSONObject message) throws JSONException {
    queuepub.publish(message);
  }
}
