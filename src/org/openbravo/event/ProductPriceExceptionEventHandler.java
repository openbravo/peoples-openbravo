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
 * All portions are Copyright (C) 2022 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.event;

import java.util.Date;

import javax.enterprise.event.Observes;

import org.hibernate.query.Query;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.client.kernel.event.EntityNewEvent;
import org.openbravo.client.kernel.event.EntityPersistenceEventObserver;
import org.openbravo.client.kernel.event.EntityUpdateEvent;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.model.ad.system.Client;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.pricing.pricelist.ProductPrice;
import org.openbravo.model.pricing.pricelist.ProductPriceException;

class ProductPriceExceptionObserver extends EntityPersistenceEventObserver {

  private static Entity[] entities = {
      ModelProvider.getInstance().getEntity(ProductPriceException.ENTITY_NAME) };

  @Override
  protected Entity[] getObservedEntities() {
    return entities;
  }

  public void onSave(@Observes EntityNewEvent event) {
    if (!isValidEvent(event)) {
      return;
    }
    final ProductPriceException pppe = (ProductPriceException) event.getTargetInstance();
    if (existsRecord(pppe.getId(), pppe.getClient(), pppe.getOrganization(), pppe.getProductPrice(),
        pppe.getValidFromDate(), pppe.getValidToDate())) {
      throw new OBException(OBMessageUtils.getI18NMessage("ProductPriceExceptionExists"));
    }
  }

  public void onUpdate(@Observes EntityUpdateEvent event) {
    if (!isValidEvent(event)) {
      return;
    }
    final ProductPriceException pppe = (ProductPriceException) event.getTargetInstance();
    if (existsRecord(pppe.getId(), pppe.getClient(), pppe.getOrganization(), pppe.getProductPrice(),
        pppe.getValidFromDate(), pppe.getValidToDate())) {
      throw new OBException(OBMessageUtils.getI18NMessage("ProductPriceExceptionExists"));
    }
  }

  // Check if exists another record using this validFromDate - validToDate in the same dates
  private boolean existsRecord(final String exceptionId, final Client client,
      final Organization organization, final ProductPrice productPrice, final Date validFrom,
      final Date validTo) {
    //@formatter:off
    final String hql =
                  "select pppe.id" +
                  "  from PricingProductPriceException as pppe" +
                  " where pppe.id != :exceptionId" +
                  "   and pppe.client.id = :clientId" +
                  "   and pppe.organization.id = :orgId" +
                  "   and pppe.productPrice.id = :productPriceId" +
                  "   and" +
                  "     (" +
                  "       (" +
                  "         :validFrom between pppe.validFromDate and pppe.validToDate" +
                  "         or :validTo between pppe.validFromDate and pppe.validToDate" +
                  "       )" +
                  "       or" +
                  "         (" +
                  "           :validFrom < pppe.validFromDate" +
                  "           and :validTo > pppe.validToDate" +
                  "         )" +
                  "     )";
    //@formatter:on

    final Query<String> query = OBDal.getInstance()
        .getSession()
        .createQuery(hql, String.class)
        .setParameter("exceptionId", exceptionId)
        .setParameter("clientId", client.getId())
        .setParameter("orgId", organization.getId())
        .setParameter("productPriceId", productPrice.getId())
        .setParameter("validFrom", validFrom)
        .setParameter("validTo", validTo)
        .setMaxResults(1);

    return !query.list().isEmpty();
  }
}
