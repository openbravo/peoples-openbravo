package org.openbravo.base;

import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.model.common.order.Order;

@Entity(Order.class)
public class OrderEntityOrganizationProperty implements OrganizationPropertyHook {

  @Override
  public String getOrganizationProperty(BaseOBObject bob) {
    Order order = (Order) bob;
    return order.getTransactionDocument().isReturn() && order.getTrxOrganization() != null
        ? Order.PROPERTY_TRXORGANIZATION
        : Order.PROPERTY_ORGANIZATION;
  }
}
