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

import javax.enterprise.context.ApplicationScoped;

import org.openbravo.base.structure.InheritedAccessEnabled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An AccessTypeInjector is used by {@link RoleInheritanceManager} to retrieve the access types that
 * should be inherited
 */

@ApplicationScoped
public abstract class AccessTypeInjector implements Comparable<AccessTypeInjector> {
  private static final Logger log = LoggerFactory.getLogger(AccessTypeInjector.class);

  /**
   * Returns the name of the inheritable class.
   * 
   * @return A String with the class name
   */
  public abstract String getClassName();

  /**
   * Returns the secured object.
   * 
   * @return a String with the name of the method to retrieve the secured element
   */
  public abstract String getSecuredElement();

  /**
   * Returns the priority of this injector. It is used to determine the order when adding, updating
   * or removing a particular access, if needed.
   * 
   * @return an integer that represents the priority of this injector
   */
  public int getPriority() {
    return 100;
  }

  /**
   * Allows comparation between AccessTypeInjector classes. The getPriority() method is used to
   * determine the comparation result.
   * 
   * @return a negative integer, zero, or a positive integer as this object priority is less than,
   *         equal to, or greater than the priority of the specified AccessTypeInjector object.
   */
  public int compareTo(AccessTypeInjector accessType) throws ClassCastException {
    if (!(accessType instanceof AccessTypeInjector))
      throw new ClassCastException("An AccessTypeInjector object was expected");
    int accessTypePriority = ((AccessTypeInjector) accessType).getPriority();
    return this.getPriority() - accessTypePriority;
  }

  public boolean hasValidAccess() {
    try {
      Class<?> accessClass = Class.forName(getClassName());
      return InheritedAccessEnabled.class.isAssignableFrom(accessClass);
    } catch (ClassNotFoundException e) {
      log.debug("Invalid class name for AccessTypeInjector: ", getClassName());
      return false;
    }
  }
}