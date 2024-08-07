package org.openbravo.client.application.messageclient;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.openbravo.base.weld.WeldUtils;

public class MessageClientContextListener implements ServletContextListener {

  private MessageClientManager messageClientManager;

  @Override
  public void contextInitialized(ServletContextEvent servletContextEvent) {
    messageClientManager = WeldUtils.getInstanceFromStaticBeanManager(MessageClientManager.class);
    messageClientManager.start();
  }

  @Override
  public void contextDestroyed(ServletContextEvent servletContextEvent) {
    if (messageClientManager != null) {
      messageClientManager.shutdown();
    }
  }
}
