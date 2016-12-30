/*
 ************************************************************************************
 * Copyright (C) 2015 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.retail.posterminal;

import javax.enterprise.context.ApplicationScoped;

import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.model.Entity;
import org.openbravo.client.kernel.ComponentProvider.Qualifier;
import org.openbravo.service.db.DbUtility;

/**
 */
@ApplicationScoped
@Qualifier(ExternalOrderLoader.APP_NAME)
public class ExternalOrderLoaderErrorHandler extends POSDataSynchronizationErrorHandler {

  @Override
  public void handleError(Throwable t, Entity entity, JSONObject result, JSONObject jsonRecord) {
    if (ExternalOrderLoader.isSynchronizedRequest()) {
      Throwable localT = t;
      if (localT instanceof OBException && localT.getCause() != null) {
        localT = t.getCause();
      }
      ExternalOrderLoader.setCurrentException(DbUtility.getUnderlyingSQLException(localT));
      if (ExternalOrderLoader.getCurrentException() instanceof RuntimeException) {
        throw (RuntimeException) ExternalOrderLoader.getCurrentException();
      }
      throw new OBException(ExternalOrderLoader.getCurrentException());
    }
    super.handleError(t, entity, result, jsonRecord);
  }

  public boolean setImportEntryStatusToError() {
    return false;
  }

}