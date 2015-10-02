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
 * All portions are Copyright (C) 2015 Openbravo SLU 
 * All Rights Reserved. 
 ************************************************************************
 */
package org.openbravo.roleInheritance;

/**
 * Defines the priorities of the access type injectors for the {@link RoleInheritanceManager} class
 * that defines in which order the accesses should be calculated
 * 
 */

public abstract class AccessTypePriorityHandler {
  /**
   * Returns the priority of this injector.
   * 
   * @return an integer that represents the priority of this injector
   */
  public int getPriority() {
    return 100;
  }
}