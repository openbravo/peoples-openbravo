package org.openbravo.base;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;

import org.jfree.util.Log;
import org.openbravo.database.ConnectionProvider;

public class SystemStatusListener implements ServletContextListener {

  @Override
  public void contextDestroyed(ServletContextEvent arg0) {

  }

  @Override
  public void contextInitialized(ServletContextEvent sce) {
    ConnectionProvider cp = ConnectionProviderContextListener.getPool(sce.getServletContext());
    try {
      String st = SystemStatusListenerData.getSystemStatus(cp);
      if (st.equals("RB60") || st.equals("RB50"))
        SystemStatusListenerData.setSystemStatus(cp, "RB70");
    } catch (ServletException e) {
      Log.error("Error while updating system status", e);
    }

  }
}
