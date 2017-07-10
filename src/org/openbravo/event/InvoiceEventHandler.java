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
 * All portions are Copyright (C) 2017 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 *************************************************************************
 */
package org.openbravo.event;

import javax.enterprise.event.Observes;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.model.Property;
import org.openbravo.client.kernel.event.EntityPersistenceEventObserver;
import org.openbravo.client.kernel.event.EntityUpdateEvent;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.common.businesspartner.BusinessPartner;
import org.openbravo.model.common.invoice.Invoice;

public class InvoiceEventHandler extends EntityPersistenceEventObserver {
  private static Entity[] entities = { ModelProvider.getInstance().getEntity(Invoice.ENTITY_NAME) };
  protected Logger logger = Logger.getLogger(this.getClass());

  @Override
  protected Entity[] getObservedEntities() {
    return entities;
  }

  public void onUpdate(@Observes EntityUpdateEvent event) {
    if (!isValidEvent(event)) {
      return;
    }
    final Entity invoiceEntity = ModelProvider.getInstance().getEntity(Invoice.ENTITY_NAME);
    final Property businessPartnerProperty = invoiceEntity
        .getProperty(Invoice.PROPERTY_BUSINESSPARTNER);

    BusinessPartner newBusinessPartner = (BusinessPartner) event
        .getCurrentState(businessPartnerProperty);
    BusinessPartner oldBusinessPartner = (BusinessPartner) event
        .getPreviousState(businessPartnerProperty);

    // update discount information
    if (newBusinessPartner != null && oldBusinessPartner != null
        && !StringUtils.equals(newBusinessPartner.getId(), oldBusinessPartner.getId())) {
      StringBuilder removeQuery = new StringBuilder("delete from InvoiceDiscount disc");
      removeQuery.append(" where disc.invoice.id = :invoiceId");

      Query updateQry = OBDal.getInstance().getSession().createQuery(removeQuery.toString());
      updateQry.setString("invoiceId", event.getId());
      updateQry.executeUpdate();
    }
  }
}