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
 * All portions are Copyright (C) 2013-2018 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.event;

import java.util.Date;
import java.util.List;

import javax.enterprise.event.Observes;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.model.Property;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.client.kernel.event.EntityDeleteEvent;
import org.openbravo.client.kernel.event.EntityPersistenceEventObserver;
import org.openbravo.client.kernel.event.EntityUpdateEvent;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.businessUtility.Preferences;
import org.openbravo.erpCommon.utility.PropertyException;
import org.openbravo.model.common.businesspartner.BusinessPartner;
import org.openbravo.model.common.enterprise.Warehouse;
import org.openbravo.model.common.order.Order;
import org.openbravo.model.common.order.OrderDiscount;
import org.openbravo.model.common.order.OrderLine;

public class OrderEventHandler extends EntityPersistenceEventObserver {

  private static Entity[] entities = { ModelProvider.getInstance().getEntity(Order.ENTITY_NAME) };
  protected Logger logger = Logger.getLogger(this.getClass());

  @Override
  protected Entity[] getObservedEntities() {
    return entities;
  }

  public void onUpdate(@Observes EntityUpdateEvent event) {
    if (!isValidEvent(event)) {
      return;
    }
    final Entity orderEntity = ModelProvider.getInstance().getEntity(Order.ENTITY_NAME);
    final Property orderDateProperty = orderEntity.getProperty(Order.PROPERTY_ORDERDATE);
    final Property scheduledDateProperty = orderEntity
        .getProperty(Order.PROPERTY_SCHEDULEDDELIVERYDATE);
    final Property warehouseProperty = orderEntity.getProperty(Order.PROPERTY_WAREHOUSE);
    final Property businessPartnerProperty = orderEntity
        .getProperty(Order.PROPERTY_BUSINESSPARTNER);
    String orderId = (String) event.getTargetInstance().getId();
    Date newOrderDate = (Date) event.getCurrentState(orderDateProperty);
    Date oldOrderDate = (Date) event.getPreviousState(orderDateProperty);
    Date newScheduledDate = (Date) event.getCurrentState(scheduledDateProperty);
    Date oldScheduledDate = (Date) event.getPreviousState(scheduledDateProperty);
    Warehouse newWarehouse = (Warehouse) event.getCurrentState(warehouseProperty);
    Warehouse oldWarehouse = (Warehouse) event.getPreviousState(warehouseProperty);
    String newBPId = ((BusinessPartner) event.getCurrentState(businessPartnerProperty)).getId();
    String oldBPId = ((BusinessPartner) event.getPreviousState(businessPartnerProperty)).getId();

    OBCriteria<OrderLine> orderLineCriteria = OBDal.getInstance().createCriteria(OrderLine.class);
    orderLineCriteria.add(Restrictions.eq(OrderLine.PROPERTY_SALESORDER,
        OBDal.getInstance().get(Order.class, orderId)));
    List<OrderLine> orderLines = orderLineCriteria.list();
    if (CollectionUtils.isNotEmpty(orderLines)) {
      boolean syncOrderDate = isDateChangedAndPreferenceIsNotActivated(newOrderDate, oldOrderDate,
          "DoNotSyncDateOrdered");
      boolean syncDeliveredDate = isDateChangedAndPreferenceIsNotActivated(newScheduledDate,
          oldScheduledDate, "DoNotSyncDateDelivered");
      boolean syncWarehouse = isIdChangedAndPreferenceIsNotActivated(newWarehouse, oldWarehouse,
          "DoNotSyncWarehouse");

      if (syncOrderDate || syncDeliveredDate || syncWarehouse) {
        for (OrderLine lines : orderLines) {
          if (syncOrderDate) {
            lines.setOrderDate(newOrderDate);
          }
          if (syncDeliveredDate) {
            lines.setScheduledDeliveryDate(newScheduledDate);
          }
          if (syncWarehouse) {
            lines.setWarehouse(newWarehouse);
          }
        }
      }
    }

    // Remove discount information
    if (!StringUtils.equals(newBPId, oldBPId)) {
      StringBuilder deleteHql = new StringBuilder();
      deleteHql.append(" delete from " + OrderDiscount.ENTITY_NAME);
      deleteHql.append(" where " + OrderDiscount.PROPERTY_SALESORDER + ".id = :orderId");
      Query deleteQry = OBDal.getInstance().getSession().createQuery(deleteHql.toString());
      deleteQry.setParameter("orderId", orderId);
      deleteQry.executeUpdate();
    }
  }

  /**
   * Returns if the field should be sync or not taking into account the date has been changed and
   * passed preference doesn't exists or it is not active
   * 
   * @param newDate
   *          The new date value
   * @param oldDate
   *          The old date value
   * @param preferenceSearchKey
   *          The preference search key
   * @return true if the field should be updated or false if not
   */
  private boolean isDateChangedAndPreferenceIsNotActivated(Date newDate, Date oldDate,
      String preferenceSearchKey) {
    boolean syncField = false;
    if (newDate != null && oldDate != null && newDate.compareTo(oldDate) != 0) {
      syncField = !StringUtils.equals(Preferences.YES, getPreferenceValue(preferenceSearchKey));
    }
    return syncField;
  }

  /**
   * Returns if the field should be sync or not taking into account the object ID has been changed
   * and passed preference doesn't exists or it is not active. It compares IDs for any BaseOBObject
   * 
   * @param newObj
   *          The new object value
   * @param oldObj
   *          The old object value
   * @param preferenceSearchKey
   *          The preference search key
   * @return true if the field should be updated or false if not
   */
  private boolean isIdChangedAndPreferenceIsNotActivated(BaseOBObject newObj, BaseOBObject oldObj,
      String preferenceSearchKey) {
    boolean syncField = false;
    if (newObj != null && oldObj != null
        && !StringUtils.equals(newObj.getId().toString(), oldObj.getId().toString())) {
      syncField = !StringUtils.equals(Preferences.YES, getPreferenceValue(preferenceSearchKey));
    }
    return syncField;
  }

  /**
   * Returns the property value of a preference with a certain search key, or Preferences.NO if the
   * property can't be found.
   * 
   * @param preferenceKey
   *          The preference search key
   * @return Preferences.NO If the propery is not found or the property value if it exists
   */
  private String getPreferenceValue(String preferenceKey) {
    String syncField;
    try {
      syncField = Preferences.getPreferenceValue(preferenceKey, true, OBContext.getOBContext()
          .getCurrentClient(), OBContext.getOBContext().getCurrentOrganization(), OBContext
          .getOBContext().getUser(), OBContext.getOBContext().getRole(), null);
    } catch (PropertyException e) {
      // if property not found, sync the field
      syncField = Preferences.NO;
    }
    return syncField;
  }

  public void onDelete(@Observes EntityDeleteEvent event) {
    if (!isValidEvent(event)) {
      return;
    }
    final Entity orderEntity = ModelProvider.getInstance().getEntity(Order.ENTITY_NAME);
    final Property quotationProperty = orderEntity.getProperty(Order.PROPERTY_QUOTATION);
    Order quotation = (Order) event.getCurrentState(quotationProperty);
    if (quotation != null) {
      quotation.setDocumentStatus("UE");
    }
  }
}
