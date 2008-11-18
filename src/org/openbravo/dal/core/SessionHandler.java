/*
 * 
 * Copyright (C) 2001-2008 Openbravo S.L. Licensed under the Apache Software
 * License version 2.0 You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.openbravo.dal.core;

import java.io.Serializable;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.FlushMode;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.openbravo.base.provider.OBNotSingleton;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.session.SessionFactoryController;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.base.structure.Identifiable;
import org.openbravo.base.util.Check;

/**
 * Convenience holder of a session and transaction in a threadlocal.
 * 
 * TODO: revisit when looking at factory pattern and dependency injection
 * framework
 * 
 * @author mtaal
 */

public class SessionHandler implements OBNotSingleton {
    private static final Logger log = Logger.getLogger(SessionHandler.class);

    // The threadlocal which handles the session
    private static ThreadLocal<SessionHandler> sessionHandler = new ThreadLocal<SessionHandler>();

    // delete session handler from threadlocal
    public static void deleteSessionHandler() {
        log.debug("Removing sessionhandler");
        sessionHandler.set(null);
    }

    /** checks if a session handler is present for this thread */
    public static boolean isSessionHandlerPresent() {
        return sessionHandler.get() != null;
    }

    /** Returns the sessionhandler for this thread */
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
            OBProvider.getInstance().register(SessionHandler.class,
                    SessionHandler.class, false);
        }
        return OBProvider.getInstance().get(SessionHandler.class);
    }

    /** The session */
    private Session session;

    /** The transaction */
    private Transaction tx;

    /**
     * Sets the session handler at rollback so that the controller can rollback
     * at the end
     */
    private boolean doRollback = false;

    /** Returns the session */
    public Session getSession() {
        return session;
    }

    /** Saves the object in this session */
    public void save(Object obj) {
        if (Identifiable.class.isAssignableFrom(obj.getClass())) {
            session.save(((Identifiable) obj).getEntityName(), obj);
        } else {
            session.save(obj);
        }
    }

    /** Delete the object */
    public void delete(Object obj) {
        session.delete(obj);
    }

    @SuppressWarnings("unchecked")
    public <T extends Object> List<T> list(Class<T> clazz) {
        final Criteria c = session.createCriteria(clazz);
        return c.list();
    }

    /**
     * Queries for a certain type and returns that using the id.
     * 
     * Internally translates a class to an entityname because the hibernate
     * Session.get method can not handle class names if the entity was mapped
     * with entitynames.
     */
    @SuppressWarnings("unchecked")
    public <T extends Object> T find(Class<T> clazz, Object id) {
        if (Identifiable.class.isAssignableFrom(clazz)) {
            return (T) find(DalUtil.getEntityName(clazz), id);
        }
        return (T) session.get(clazz, (Serializable) id);
    }

    public BaseOBObject find(String entityName, Object id) {
        return (BaseOBObject) session.get(entityName, (Serializable) id);
    }

    /** Create a query object */
    public Query createQuery(String qryStr) {
        return session.createQuery(qryStr);
    }

    /** Starts a transaction */
    private void begin() {
        Check.isTrue(session == null, "Session must be null before begin");
        session = SessionFactoryController.getInstance().getSessionFactory()
                .openSession();
        session.setFlushMode(FlushMode.COMMIT);
        Check.isTrue(tx == null, "tx must be null before begin");
        tx = session.beginTransaction();
        log.debug("Transaction started");
    }

    /** Commits the transaction, should be called at the end of all the work */
    public void commitAndClose() {
        try {
            checkInvariant();
            tx.commit();
            tx = null;
        } finally {
            deleteSessionHandler();
            session.close();
        }
        session = null;
        log.debug("Transaction closed, session closed");
    }

    /** Commits the transaction, should be called at the end of all the work */
    public void commitAndStart() {
        checkInvariant();
        tx.commit();
        tx = null;
        tx = session.beginTransaction();
        log.debug("Committed and started new transaction");
    }

    /** Rollback */
    public void rollback() {
        log.debug("Rolling back transaction");
        try {
            checkInvariant();
            tx.rollback();
            tx = null;
        } finally {
            deleteSessionHandler();
            try {
                log.debug("Closing session");
                session.close();
            } finally {
                // purposely ignoring it to not hide other errors
            }
        }
        session = null;
    }

    /**
     * The invariant is that for begin, rollback and commit the session etc. are
     * alive
     */
    private void checkInvariant() {
        Check.isNotNull(session, "Session is null");
        Check.isNotNull(tx, "Tx is null");
        Check.isTrue(tx.isActive(), "Tx is active");
    }

    /** Set rollback */
    public void setDoRollback(boolean setRollback) {
        if (setRollback) {
            log.debug("Rollback is set to true");
        }
        this.doRollback = setRollback;
    }

    /** Returns the doRollback value */
    public boolean getDoRollback() {
        return doRollback;
    }
}