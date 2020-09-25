/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.retail.posterminal.event;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.event.Observes;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.model.Property;
import org.openbravo.client.kernel.event.EntityPersistenceEventObserver;
import org.openbravo.client.kernel.event.EntityUpdateEvent;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.retail.posterminal.TerminalType;
import org.openbravo.retail.posterminal.TerminalTypePaymentMethod;

public class TerminalTypeEventHandler extends EntityPersistenceEventObserver {

  private static Entity[] entities = {
      ModelProvider.getInstance().getEntity(TerminalType.ENTITY_NAME) };
  protected Logger logger = LogManager.getLogger();

  @Override
  protected Entity[] getObservedEntities() {
    return entities;
  }

  public void onUpdate(@Observes EntityUpdateEvent event) {
    if (!isValidEvent(event)) {
      return;
    }

    final TerminalType terminalType = (TerminalType) event.getTargetInstance();
    final Property safeBoxProperty = entities[0].getProperty(TerminalType.PROPERTY_SAFEBOX);
    if ((Boolean) event.getPreviousState(safeBoxProperty)
        && !(Boolean) event.getCurrentState(safeBoxProperty)) {
      OBQuery<TerminalTypePaymentMethod> query = OBDal.getInstance()
          .createQuery(TerminalTypePaymentMethod.class,
              " as e where e.obposTerminaltype.id = :terminalType and e.issafebox = true");
      query.setFilterOnActive(false);
      query.setNamedParameter("terminalType", terminalType.getId());
      List<TerminalTypePaymentMethod> listPaymentMethod = query.list();
      if (listPaymentMethod.size() > 0) {
        List<String> listPaymentName = new ArrayList<String>();
        for (TerminalTypePaymentMethod paymentMethod : listPaymentMethod) {
          listPaymentName.add(paymentMethod.getName());
        }
        throw new OBException(
            String.format(OBMessageUtils.messageBD("OBPOS_PaymentMethodSafeBoxDefined"),
                String.join(", ", listPaymentName)));
      }
    }
  }
}
