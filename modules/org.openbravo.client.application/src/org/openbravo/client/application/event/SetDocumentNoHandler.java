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
 * All portions are Copyright (C) 2011 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.client.application.event;

import javax.enterprise.event.Observes;

import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.model.Property;
import org.openbravo.client.kernel.RequestContext;
import org.openbravo.client.kernel.event.EntityNewEvent;
import org.openbravo.client.kernel.event.EntityPersistenceEventObserver;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.common.enterprise.DocumentType;
import org.openbravo.model.common.order.Order;
import org.openbravo.service.db.DalConnectionProvider;

/**
 * Listens to save events on purchase and sales orders and sets the document no.
 * 
 * @see Utility#getDocumentNo(java.sql.Connection, org.openbravo.database.ConnectionProvider,
 *      org.openbravo.base.secureApp.VariablesSecureApp, String, String, String, String, boolean,
 *      boolean)
 * 
 * @author mtaal
 */
public class SetDocumentNoHandler extends EntityPersistenceEventObserver {

  private static Entity[] entities = new Entity[] { ModelProvider.getInstance().getEntity(
      Order.ENTITY_NAME) };
  private static Property documentNoProperty = entities[0].getProperty(Order.PROPERTY_DOCUMENTNO);
  private static Property docTypeTargetProperty = entities[0]
      .getProperty(Order.PROPERTY_TRANSACTIONDOCUMENT);
  private static Property docTypeProperty = entities[0].getProperty(Order.PROPERTY_DOCUMENTTYPE);

  public void onSave(@Observes EntityNewEvent event) {

    if (isValidEvent(event)) {
      String documentNo = (String) event.getCurrentState(documentNoProperty);
      if (documentNo == null || documentNo.startsWith("<")) {
        final DocumentType docTypeTarget = (DocumentType) event
            .getCurrentState(docTypeTargetProperty);
        final DocumentType docType = (DocumentType) event.getCurrentState(docTypeProperty);
        // use empty strings instead of null
        final String docTypeTargetId = docTypeTarget != null ? docTypeTarget.getId() : "";
        final String docTypeId = docType != null ? docType.getId() : "";
        String windowId = RequestContext.get().getRequestParameter("windowId");
        if (windowId == null) {
          windowId = "";
        }

        // recompute it
        documentNo = Utility.getDocumentNo(OBDal.getInstance().getConnection(false),
            new DalConnectionProvider(false), RequestContext.get().getVariablesSecureApp(),
            windowId, Order.TABLE_NAME, docTypeTargetId, docTypeId, false, true);
        event.setCurrentState(documentNoProperty, documentNo);
      }
    }
  }

  @Override
  protected Entity[] getObservedEntities() {
    return entities;
  }
}
