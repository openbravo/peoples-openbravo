/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.0  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html 
 * Software distributed under the License  is  distributed  on  an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific  language  governing  rights  and  limitations
 * under the License. 
 * The Original Code is Openbravo ERP. 
 * The Initial Developer of the Original Code is Openbravo SLU 
 * All portions are Copyright (C) 2009 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.base.seam.persistence;

import javax.persistence.EntityManager;

import org.hibernate.Session;
import org.jboss.seam.Component;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.log.Log;
import org.openbravo.dal.core.SessionHandler;

/**
 * A SessionHandler that is aware of Seam conversations and entity managers in that conversation. If
 * a Session is requested outside of a conversation then it operates in the same way as the standard
 * {@link SessionHandler}. If used as part of a conversation then the entity manager from the
 * conversation is used.
 * 
 * @author mtaal
 */
public class SeamSessionHandler extends SessionHandler {
  @org.jboss.seam.annotations.Logger
  private Log log;

  private EntityManager entityManager;
  private boolean runningInContext;

  @Override
  protected Session createSession() {
    return (Session) entityManager.getDelegate();
  }

  @Override
  protected void closeSession() {
    entityManager.close();
  }

  /**
   * Starts a transaction.
   */
  protected void begin() {
    runningInContext = Contexts.isConversationContextActive()
        && null != Component.getInstance(ConversationControlledTransaction.class, false);
    // get the entitymanager from the context
    if (runningInContext) {
      entityManager = (EntityManager) Component.getInstance("entityManager");
      setSession(createSession());
    } else {
      entityManager = EntityManagerFactoryController.getInstance().getEntityManagerFactory()
          .createEntityManager();
      super.begin();
    }
  }

  /**
   * If there was no conversation context then the super class is called. In other cases an error is
   * logged as this is not supported.
   * 
   * @see SessionHandler#commitAndClose()
   */
  public void commitAndClose() {
    if (!runningInContext) {
      super.commitAndClose();
    } else {
      log.error("Commit and close is called while the transaction is managed through the context.");
    }
  }

  /**
   * If not running as part of a conversation then calls the super class. In other cases an error is
   * logged as this method may not be called explicitly then.
   * 
   * @see SessionHandler#commitAndStart()
   */
  public void commitAndStart() {
    if (!runningInContext) {
      super.commitAndStart();
    } else {
      log.error("Commit and start is called while the transaction is managed through the context.");
    }
  }

  /**
   * If not running as part of a conversation then calls the super class. In other cases an error is
   * logged as this method may not be called explicitly then.
   * 
   * @see SessionHandler#rollback()
   */
  public void rollback() {
    if (!runningInContext) {
      super.rollback();
    } else {
      log.error("Rollback is called while the transaction is managed through the context.");
    }
  }

  /**
   * @return false in this implementation as transaction, flush and commit are done through Seam.
   */
  public boolean doSessionInViewPatter() {
    return !runningInContext;
  }

}