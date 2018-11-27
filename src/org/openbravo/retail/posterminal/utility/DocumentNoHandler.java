/*
 ************************************************************************************
 * Copyright (C) 2018 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.retail.posterminal.utility;

import org.openbravo.base.model.Entity;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.client.kernel.RequestContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.common.enterprise.DocumentType;
import org.openbravo.service.db.DalConnectionProvider;

public class DocumentNoHandler {

  private Entity entity;
  private DocumentType doctypeTarget;
  private DocumentType doctype;
  private BaseOBObject bob;
  private String propertyName = "documentNo";

  public DocumentNoHandler(BaseOBObject bob, Entity entity, DocumentType doctypeTarget,
      DocumentType doctype) {
    this.entity = entity;
    this.doctypeTarget = doctypeTarget;
    this.doctype = doctype;
    this.bob = bob;
  }

  public void setDocumentNoAndSave() {
    final String docNo = getDocumentNumber(entity, doctypeTarget, doctype);
    bob.setValue(propertyName, docNo);
    OBDal.getInstance().save(bob);
  }

  public String getDocumentNumber(Entity localEntity, DocumentType localDoctypeTarget,
      DocumentType localDoctype) {
    return Utility.getDocumentNo(OBDal.getInstance().getConnection(false),
        new DalConnectionProvider(false), RequestContext.get().getVariablesSecureApp(), "",
        localEntity.getTableName(), localDoctypeTarget == null ? "" : localDoctypeTarget.getId(),
        localDoctype == null ? "" : localDoctype.getId(), false, true);
  }

}
