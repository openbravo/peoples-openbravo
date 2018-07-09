/*
 ************************************************************************************
 * Copyright (C) 2016-2018 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.retail.posterminal.process;

import org.hibernate.query.Query;
import org.openbravo.dal.service.OBDal;
import org.openbravo.mobile.core.process.MobileImportEntryProcessorRunnable;
import org.openbravo.service.importprocess.ImportEntry;

/**
 *
 * @author migueldejuana
 *
 */
public abstract class SerializedByTermImportEntryProcessorRunnable extends
    MobileImportEntryProcessorRunnable {
  public int countEntries(String importStatus, ImportEntry importEntry) {
    final String whereClause = ImportEntry.PROPERTY_IMPORTSTATUS + "='" + importStatus + "' and "
        + ImportEntry.PROPERTY_CREATIONDATE + "<=:creationDate and "
        + ImportEntry.PROPERTY_CREATEDTIMESTAMP + "<:createdtimestamp and "
        + ImportEntry.PROPERTY_OBPOSPOSTERMINAL + "=:terminal and id!=:id";
    final Query<Object> qry = OBDal
        .getInstance()
        .getSession()
        .createQuery("select 1 from " + ImportEntry.ENTITY_NAME + " where " + whereClause,
            Object.class);
    qry.setParameter("id", importEntry.getId());
    qry.setParameter("creationDate", importEntry.getCreationDate());
    qry.setParameter("terminal", importEntry.getOBPOSPOSTerminal());
    qry.setParameter("createdtimestamp", importEntry.getCreatedtimestamp());
    qry.setMaxResults(1);
    return qry.list().size();
  }
}
