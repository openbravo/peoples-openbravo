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
 * All portions are Copyright (C) 2008-2016 Openbravo SLU
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.dal.core;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.hibernate.FlushMode;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.model.Entity;
import org.openbravo.base.provider.OBNotSingleton;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.session.OBPropertiesProvider;
import org.openbravo.base.session.SessionFactoryController;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.base.structure.Identifiable;
import org.openbravo.base.util.Check;
import org.openbravo.dal.service.OBDal;
import org.openbravo.database.ExternalConnectionPool;
import org.openbravo.database.SessionInfo;
import org.openbravo.service.db.DbUtility;

/**
 * Keeps the Hibernate Session and Transaction in a ThreadLocal so that it is available throughout
 * the application. This class provides convenience methods to get a Session and to
 * create/commit/rollback a transaction.
 * 
 * @author mtaal
 */
// TODO: revisit when looking at factory pattern and dependency injection
// framework
public class SessionHandler implements OBNotSingleton {
  private static final Logger log = Logger.getLogger(SessionHandler.class);

  private static ExternalConnectionPool externalConnectionPool;
  private static String rbdms;

  {
    String poolClassName = OBPropertiesProvider.getInstance().getOpenbravoProperties()
        .getProperty("db.externalPoolClassName");
    if (poolClassName != null && !"".equals(poolClassName)) {
      try {
        externalConnectionPool = ExternalConnectionPool.getInstance(poolClassName);
      } catch (Throwable e) {
        externalConnectionPool = null;
        log.warn("External connection pool class not found: " + poolClassName, e);
      }
    }
    rbdms = (String) OBPropertiesProvider.getInstance().getOpenbravoProperties().get("bbdd.rdbms");
  }

  // The threadlocal which handles the session
  private static ThreadLocal<SessionHandler> sessionHandler = new ThreadLocal<SessionHandler>();

  /**
   * Removes the current SessionHandler from the ThreadLocal. A call to getSessionHandler will
   * create a new SessionHandler, session and transaction.
   */
  public static void deleteSessionHandler() {
    log.debug("Removing sessionhandler");
    sessionHandler.set(null);
  }

  /** @return true if a session handler is present for this thread, false */
  public static boolean isSessionHandlerPresent() {
    return sessionHandler.get() != null;
  }

  /**
   * Returns the SessionHandler of this thread. If there is none then a new one is created and a
   * Hibernate Session is created and a transaction is started.
   * 
   * @return the sessionhandler for this thread
   */
  public static SessionHandler getInstance() {
    SessionHandler sh = sessionHandler.get();
    if (sh == null) {
      log.debug("Creating sessionHandler");
      sh = getCreateSessionHandler();
      sh.begin();
      sessionHandler.set(sh);
    }
    return sh;
  }

  private static boolean checkedSessionHandlerRegistration = false;

  private static SessionHandler getCreateSessionHandler() {
    if (!checkedSessionHandlerRegistration
        && !OBProvider.getInstance().isRegistered(SessionHandler.class)) {
      OBProvider.getInstance().register(SessionHandler.class, SessionHandler.class, false);
    }
    return OBProvider.getInstance().get(SessionHandler.class);
  }

  private Map<String, Session> session = new HashMap<>();
  private Map<String, Transaction> tx = new HashMap<>();
  private Map<String, Connection> connection = new HashMap<>();
  private List<Connection> associatedConnections = new ArrayList<>();

  // Sets the session handler at rollback so that the controller can rollback
  // at the end
  private boolean doRollback = false;

  /** @return the session */
  public Session getSession() {
    return getSession("DEFAULT");
  }

  public Session getSession(String pool) {
    Session theSession = session.get(pool);
    if (theSession == null) {
      begin(pool);
    }
    return session.get(pool);
  }

  protected void setSession(Session thisSession) {
    setSession("DEFAULT", thisSession);
  }

  private void setSession(String pool, Session thisSession) {
    session.put(pool, thisSession);
  }

  public void setConnection(Connection newConnection) {
    setConnection("DEFAULT", newConnection);
  }

  public void setConnection(String pool, Connection newConnection) {
    connection.put(pool, newConnection);
  }

  /** Gets current session's {@code Connection} if it's set, {@code null} if not. */
  public Connection getConnection() {
    return getConnection("DEFAULT");
  }

  public Connection getConnection(String pool) {
    return connection.get(pool);
  }

  protected Session createSession() {
    return createSession("DEFAULT");
  }

  protected Session createSession(String pool) {
    SessionFactory sf = SessionFactoryController.getInstance().getSessionFactory();
    // Checks if the session connection has to be obtained using an external connection pool
    if (externalConnectionPool != null && connection.get(pool) == null) {
      Connection externalConnection;
      try {
        externalConnection = getNewConnection(pool);
        setConnection(pool, externalConnection);
      } catch (SQLException e) {
        throw new OBException("Could not get connection to create DAL session", e);
      }
    }

    if (connection.get(pool) != null) {
      // If the connection has been obtained using an external connection pool it is passed to
      // openSession, to prevent a new connection to be created using the Hibernate default
      // connection pool
      return sf.openSession(connection.get(pool));
    } else {
      return sf.openSession();
    }
  }

  /**
   * Returns true when the current SessionHandler has a transaction and it is active.
   */
  public boolean isCurrentTransactionActive() {
    return isCurrentTransactionActive("DEFAULT");
  }

  public boolean isCurrentTransactionActive(String pool) {
    return tx.containsKey(pool) && tx.get(pool).isActive();
  }

  /**
   * Begins a new Transaction on the current HibernateSession and assigns it to the SessionHandler.
   * 
   * @throws OBException
   *           if there is already an available active transaction.
   */
  public void beginNewTransaction() throws OBException {
    beginNewTransaction("DEFAULT");
  }

  public void beginNewTransaction(String pool) throws OBException {
    if (isCurrentTransactionActive(pool)) {
      throw new OBException(
          "Not possible to start a new transaction while there is still one active.");
    }
    tx.put(pool, getSession(pool).beginTransaction());
  }

  /** Gets a new {@code Connection} from the connection pool. */
  public Connection getNewConnection() throws SQLException {
    return getNewConnection("DEFAULT");
  }

  public Connection getNewConnection(String pool) throws SQLException {
    Connection newConnection;
    if (externalConnectionPool != null) {
      newConnection = externalConnectionPool.getConnection(pool);
      try {
        // Autocommit is disabled because DAL is taking into account his logical and DAL is setting
        // autoCommint to false to maintain transactional way of working.
        newConnection.setAutoCommit(false);
      } catch (SQLException e) {
        log.error("Error setting connection to auto-commit mode", e);
      }
    } else {
      // getting connection from Hibernate pool
      newConnection = ((DalSessionFactory) SessionFactoryController.getInstance()
          .getSessionFactory()).getConnectionProvider().getConnection();
      SessionInfo.initDB(newConnection, rbdms);
    }
    return newConnection;
  }

  protected void closeSession(String pool) {
    for (Connection conn : associatedConnections) {
      try {
        if (!conn.isClosed()) {
          conn.setAutoCommit(false);
          conn.close();
        }
      } catch (SQLException e) {
        e.printStackTrace();
      }
    }

    if (session.get(pool) != null && session.get(pool).isOpen()) {
      session.get(pool).close();
    }
  }

  /**
   * Saves the object in this getSession().
   * 
   * @param obj
   *          the object to persist
   */
  public void save(Object obj) {
    save("DEFAULT", obj);
  }

  public void save(String pool, Object obj) {
    if (Identifiable.class.isAssignableFrom(obj.getClass())) {
      getSession(pool).saveOrUpdate(((Identifiable) obj).getEntityName(), obj);
    } else {
      getSession(pool).saveOrUpdate(obj);
    }
  }

  /**
   * Delete the object from the db.
   * 
   * @param obj
   *          the object to remove
   */
  public void delete(Object obj) {
    delete("DEFAULT", obj);
  }

  public void delete(String pool, Object obj) {
    if (Identifiable.class.isAssignableFrom(obj.getClass())) {
      getSession(pool).delete(((Identifiable) obj).getEntityName(), obj);
    } else {
      getSession(pool).delete(obj);
    }
  }

  /**
   * Queries for a certain object using the class and id. If not found then null is returned.
   * 
   * @param clazz
   *          the class to query
   * @param id
   *          the id to use for querying
   * @return the retrieved object, can be null
   */
  public <T extends Object> T find(Class<T> clazz, Object id) {
    return find("DEFAULT", clazz, id);
  }

  @SuppressWarnings("unchecked")
  public <T extends Object> T find(String pool, Class<T> clazz, Object id) {
    // translates a class to an entityname because the hibernate
    // getSession().get method can not handle class names if the entity was
    // mapped with entitynames.
    if (Identifiable.class.isAssignableFrom(clazz)) {
      return (T) find(pool, DalUtil.getEntityName(clazz), id);
    }
    return (T) getSession(pool).get(clazz, (Serializable) id);
  }

  /**
   * Queries for a certain object using the entity name and id. If not found then null is returned.
   * 
   * @param entityName
   *          the name of the entity to query
   * @param id
   *          the id to use for querying
   * @return the retrieved object, can be null
   * 
   * @see Entity
   */
  public BaseOBObject find(String entityName, Object id) {
    return find("DEFAULT", entityName, id);
  }

  public BaseOBObject find(String pool, String entityName, Object id) {
    return (BaseOBObject) getSession(pool).get(entityName, (Serializable) id);
  }

  /**
   * Create a query object from the current getSession().
   * 
   * @param qryStr
   *          the HQL query
   * @return a new Query object
   */
  public Query createQuery(String qryStr) {
    return createQuery("DEFAULT", qryStr);
  }

  public Query createQuery(String pool, String qryStr) {
    return getSession().createQuery(qryStr);
  }

  /**
   * Starts a transaction.
   */
  protected void begin() {
    begin("DEFAULT");
  }

  protected void begin(String pool) {
    Check.isTrue(session.get(pool) == null, "Session must be null before begin");
    setSession(pool, createSession(pool));
    getSession(pool).setFlushMode(FlushMode.COMMIT);
    Check.isTrue(tx.get(pool) == null, "tx must be null before begin");
    tx.put(pool, getSession(pool).beginTransaction());
    log.debug("Transaction started");
  }

  /**
   * Commits the transaction and closes the session, should normally be called at the end of all the
   * work.
   */
  public void commitAndClose() {
    commitAndClose("DEFAULT");
  }

  public void commitAndClose(String pool) {
    boolean err = true;
    try {
      Check.isFalse(TriggerHandler.getInstance().isDisabled(),
          "Triggers disabled, commit is not allowed when in triggers-disabled mode, "
              + "call TriggerHandler.enable() before committing");

      checkInvariant();
      flushRemainingChanges(pool);
      if (connection.get(pool) == null
          || (connection.get(pool) != null && !connection.get(pool).isClosed())) {
        if (connection != null) {
          connection.get(pool).setAutoCommit(false);
        }
        tx.get(pool).commit();
      }
      tx.remove(pool);
      err = false;
    } catch (SQLException e) {
      log.error("Error while closing the connection", DbUtility.getUnderlyingSQLException(e));
    } finally {
      if (err) {
        try {
          tx.get(pool).rollback();
          tx.remove(pool);
        } catch (Throwable t) {
          // ignore these exception not to hide others
        }
      }
      try {
        if (connection.get(pool) != null && !connection.get(pool).isClosed()) {
          connection.get(pool).close();
        }
      } catch (SQLException e) {
        log.error("Error while closing the connection", e);
      }
      deleteSessionHandler();
      closeSession(pool);
    }
    setSession(null);
    log.debug("Transaction closed, session closed");
  }

  /**
   * Commits the transaction and starts a new transaction.
   */
  public void commitAndStart() {
    commitAndStart("DEFAULT");
  }

  public void commitAndStart(String pool) {
    Check.isFalse(TriggerHandler.getInstance().isDisabled(),
        "Triggers disabled, commit is not allowed when in triggers-disabled mode, "
            + "call TriggerHandler.enable() before committing");

    checkInvariant();
    flushRemainingChanges(pool);
    tx.get(pool).commit();
    tx.remove(pool);
    tx.put(pool, getSession(pool).beginTransaction());
    log.debug("Committed and started new transaction");
  }

  private void flushRemainingChanges(String pool) {

    // business event handlers can change the data
    // during flush, flush several times until
    // the session is really cleaned up
    int countFlushes = 0;
    while (OBDal.getInstance(pool).getSession().isDirty()) {
      OBDal.getInstance(pool).flush();
      countFlushes++;
      // arbitrary point to give up...
      if (countFlushes > 100) {
        log.error("Infinite loop in flushing session, tried more than 100 flushes");
        break;
      }
    }
  }

  /**
   * Rolls back the transaction and closes the getSession().
   */
  public void rollback() {
    rollback("DEFAULT");
  }

  public void rollback(String pool) {
    log.debug("Rolling back transaction");
    try {
      checkInvariant();
      if (connection.get(pool) == null
          || (connection.get(pool) != null && !connection.get(pool).isClosed())) {
        tx.get(pool).rollback();
      }
      tx.remove(pool);
    } catch (SQLException e) {
      log.error("Error while closing the connection", e);
    } finally {
      deleteSessionHandler();
      try {
        if (connection.get(pool) != null && !connection.get(pool).isClosed()) {
          connection.get(pool).close();
        }
        log.debug("Closing session");
        closeSession(pool);
      } catch (SQLException e) {
        log.error("Error while closing the connection", e);
      }
    }
    setSession(null);
  }

  /**
   * The invariant is that for begin, rollback and commit the session etc. are alive
   */
  private void checkInvariant() {
    checkInvariant("DEFAULT");
  }

  private void checkInvariant(String pool) {
    Check.isNotNull(getSession(pool), "Session is null");
    Transaction theTx = tx.get(pool);
    Check.isNotNull(theTx, "Tx is null");
    Check.isTrue(theTx.isActive(), "Tx is not active");
  }

  /**
   * Registers that the transaction should be rolled back. Is used by the {@link DalThreadHandler}.
   * 
   * @param setRollback
   *          if true then the transaction will be rolled back at the end of the thread.
   */
  public void setDoRollback(boolean setRollback) {
    if (setRollback) {
      log.debug("Rollback is set to true");
    }
    this.doRollback = setRollback;
  }

  /** @return the doRollback value */
  public boolean getDoRollback() {
    return doRollback;
  }

  /**
   * Returns true if the session-in-view pattern should be supported. That is that the session is
   * closed and committed at the end of the request.
   * 
   * @return always true in this implementation
   */
  public boolean doSessionInViewPatter() {
    return true;
  }

  public void associateConnection(Connection connection2) {
    associatedConnections.add(connection2);
  }
}
