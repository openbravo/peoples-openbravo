package org.openbravo.apachejdbcconnectionpool;

import java.lang.reflect.Method;

import javax.enterprise.context.ApplicationScoped;

import org.apache.tomcat.jdbc.pool.ConnectionPool;
import org.apache.tomcat.jdbc.pool.JdbcInterceptor;
import org.apache.tomcat.jdbc.pool.PooledConnection;
import org.openbravo.database.PoolInterceptorProvider;

@ApplicationScoped
public class TestInterceptor extends JdbcInterceptor implements PoolInterceptorProvider {

  public void reset(ConnectionPool parent, PooledConnection con) {
    // Actions after a connection has been borrowed from the pool
  }

  // Gets invoked each time an operation on Connection is invoked.
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    // Actions before the method invocation
    Object object = super.invoke(proxy, method, args);
    // Actions before the method invocation
    return object;
  }

  @Override
  public String getPoolInterceptorsClassNames() {
    String fullClassName = this.getClass().getName();
    return fullClassName + ";";
  }
}
