package org.openbravo.service.importqueue;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.weld.WeldUtils;
import org.openbravo.client.kernel.ApplicationInitializer;
import org.openbravo.service.importqueue.impl.RabbitPublication;
import org.openbravo.service.importqueue.impl.RabbitSubscription;

@ApplicationScoped
public class QueueManager implements ApplicationInitializer {

  @Inject
  private MessageDispatcher messagedispatcher;

  private QueueSubscription queuesub;
  private QueuePublication queuepub;

  @Override
  public void initialize() {
    queuesub = WeldUtils.getInstanceFromStaticBeanManager(RabbitSubscription.class);
    queuesub.subscribe(messagedispatcher);

    queuepub = WeldUtils.getInstanceFromStaticBeanManager(RabbitPublication.class);

    // queuesub = WeldUtils.getInstanceFromStaticBeanManager(NoQueue.class);
    // queuesub.subscribe(this);
    // queuepub = WeldUtils.getInstanceFromStaticBeanManager(NoQueue.class);
  }

  public void publishMessage(JSONObject message) {
    queuepub.publish(message);
  }
}
