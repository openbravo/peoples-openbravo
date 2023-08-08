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
 * All portions are Copyright (C) 2023 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.client.application.attachment;

import java.util.Optional;

import org.hibernate.criterion.Restrictions;
import org.openbravo.base.model.Entity;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.base.structure.ClientEnabled;
import org.openbravo.base.structure.OrganizationEnabled;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.ad.utility.ReprintableDocument;

/**
 * Represents a document used as the source to generate the data of a {@link ReprintableDocument}
 */
public abstract class SourceDocument<D extends BaseOBObject & ClientEnabled & OrganizationEnabled> {
  protected String id;

  /**
   * Supported document types that can be linked to a {@link ReprintableDocument}
   */
  public enum DocumentType {
    INVOICE, ORDER;

  }

  public static SourceDocument<?> newSourceDocument(String id, DocumentType documentType) {
    switch (documentType) {
      case INVOICE:
        return new InvoiceSourceDocument(id);
      case ORDER:
        return new OrderSourceDocument(id);
      default:
        throw new IllegalArgumentException("Unknown document type");
    }
  }

  /**
   * @return the DAL property that references to the source document in the
   *         {@link ReprintableDocument} model
   */
  abstract String getProperty();

  /**
   * @return the BaseOBObject of the source document, obtained based on its type
   */

  abstract D getBOB();

  protected abstract Entity getEntity();

  /**
   * Builds a new source document for a {@link ReprintableDocument}
   * 
   * @param id
   *          the ID of the source document
   */
  protected SourceDocument(String id) {
    this.id = id;
  }

  /**
   * @return true if the document exists in the database or false otherwise
   */
  public boolean exists() {
    //@formatter:off
    String hql = "select 1" +
                 "  from " + getEntity() + " e" +
                 " where e.id = :id";
    //@formatter:on
    return OBDal.getInstance()
        .getSession()
        .createQuery(hql, Integer.class)
        .setParameter("id", id)
        .uniqueResult() != null;
  }

  /**
   * @return an Optional representing the ReprintableDocument linked to the document. If the
   *         document does not have a ReprintableDocument linked to it, an empty Optional is
   *         returned.
   */
  public Optional<ReprintableDocument> getReprintableDocument() {
    // use admin mode because the ReprintableDocument entity is not readable by default
    try {
      OBContext.setAdminMode(true);
      ReprintableDocument document = (ReprintableDocument) OBDal.getInstance()
          .createCriteria(ReprintableDocument.class)
          .add(Restrictions.eq(getProperty(), getBOB()))
          .setMaxResults(1)
          .uniqueResult();
      return Optional.ofNullable(document);
    } finally {
      OBContext.restorePreviousMode();
    }
  }
}
