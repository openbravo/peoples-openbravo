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
 * All portions are Copyright (C) 2024 Openbravo SLU
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.common.hooks.timezone;

import org.openbravo.base.Entity;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.model.common.order.Order;

/**
 * Selector for the {@link Entity} annotation, allow's to implement the logic for the Order entity
 * needed to get the organization property that will define the time zone. This logic will be
 * executed record per record.
 */

@Entity(Order.class)
public class OrderTimeZoneOrganizationPropertyHook implements TimeZoneOrganizationPropertyHook {

  /**
   * @return the name of the property that references the organization with the time zone used to
   *         compute the organization time zone based properties of the given BaseOBObject. In case
   *         the BaseOBObject is a return the trxOrganization property will be used and in any other
   *         case (bob is a order) the organization property will be used.
   * 
   * @param bob
   *          a BaseOBObject that contains the record information used to retrieve the organization
   *          property.
   * 
   */
  @Override
  public String getOrganizationProperty(BaseOBObject bob) {
    Order order = (Order) bob;
    return order.getTransactionDocument().isReturn() && order.getTrxOrganization() != null
        ? Order.PROPERTY_TRXORGANIZATION
        : Order.PROPERTY_ORGANIZATION;
  }
}
