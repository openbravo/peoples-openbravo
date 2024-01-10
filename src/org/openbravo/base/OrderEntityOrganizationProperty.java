package org.openbravo.base;

import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.base.structure.OrganizationEnabled;
import org.openbravo.model.common.order.Order;

@Entity("Order")
public class OrderEntityOrganizationProperty implements OrganizationPropertyHook {

  @Override
  public String getOrganizationProperty(BaseOBObject bob) {
    Order order = (Order) bob;
    if (order.getTransactionDocument().isReturn()
        && !order.getTrxOrganization().getId().isEmpty()) {
      return order.getTrxOrganization().getId();
    }
    return ((OrganizationEnabled) bob).getOrganization().getId();
  }

}
