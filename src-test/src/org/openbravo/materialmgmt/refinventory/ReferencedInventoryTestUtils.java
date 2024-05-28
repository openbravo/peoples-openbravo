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
package org.openbravo.materialmgmt.refinventory;

import org.openbravo.base.provider.OBProvider;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.materialmgmt.onhandquantity.ReferencedInventory;
import org.openbravo.model.materialmgmt.onhandquantity.ReferencedInventoryType;
import org.openbravo.test.base.TestConstants.Orgs;

/**
 * Provides test utilities to deal with handling units
 */
public class ReferencedInventoryTestUtils {

  private ReferencedInventoryTestUtils() {
  }

  /**
   * Creates a new handling unit
   *
   * @param searchKey
   *          the search key of the handling unit
   * @param type
   *          the type of the handling unit
   *
   * @return the new handling unit
   */
  public static ReferencedInventory createHandlingUnit(String searchKey,
      ReferencedInventoryType type) {
    ReferencedInventory handlingUnit = OBProvider.getInstance().get(ReferencedInventory.class);
    handlingUnit.setOrganization(OBDal.getInstance().getProxy(Organization.class, Orgs.ESP));
    handlingUnit.setReferencedInventoryType(type);
    handlingUnit.setSearchKey(searchKey);
    OBDal.getInstance().save(handlingUnit);
    return handlingUnit;
  }

  /**
   * Creates a new handling unit type
   *
   * @param name
   *          the name of the handling unit type
   *
   * @return the new handling unit type
   */
  public static ReferencedInventoryType createHandlingUnitType(String name) {
    ReferencedInventoryType type = OBProvider.getInstance().get(ReferencedInventoryType.class);
    type.setOrganization(OBDal.getInstance().getProxy(Organization.class, Orgs.ESP));
    type.setName(name);
    type.setShared(false);
    OBDal.getInstance().save(type);
    return type;
  }
}
