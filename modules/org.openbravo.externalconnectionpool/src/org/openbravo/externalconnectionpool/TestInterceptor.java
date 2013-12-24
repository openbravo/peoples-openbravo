package org.openbravo.externalconnectionpool;

import java.lang.reflect.Method;

import org.apache.tomcat.jdbc.pool.ConnectionPool;
import org.apache.tomcat.jdbc.pool.JdbcInterceptor;
import org.apache.tomcat.jdbc.pool.PooledConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestInterceptor extends JdbcInterceptor {
  private static Logger log = LoggerFactory.getLogger(TestInterceptor.class);

  public void reset(ConnectionPool parent, PooledConnection con) {
    System.out.println("Interceptor - reset!");
  }

  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    Object object = super.invoke(proxy, method, args);
    // log.info("Interceptor - invoke: " + method.getName()/* , e */);
    // JdbcExternalConnectionPool pool = (JdbcExternalConnectionPool) ExternalConnectionPool
    // .getInstance("org.openbravo.externalconnectionpool.JdbcExternalConnectionPool");
    // pool.showPoolStats();
    // if (object instanceof Statement) {
    // Statement st = (Statement) object;
    // Exception: Method org.postgresql.jdbc4.Jdbc4Statement.setQueryTimeout(int) is not yet
    // implemented.
    // st.setQueryTimeout(2);
    // }
    return object;
  }
}
