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
package org.openbravo.role.inheritance.access;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.util.AnnotationLiteral;

import org.openbravo.base.structure.InheritedAccessEnabled;
import org.openbravo.role.inheritance.RoleInheritanceManager;

/**
 * An AccessTypeInjector is used by {@link RoleInheritanceManager} to retrieve the access types that
 * should be inherited
 */

@ApplicationScoped
public abstract class AccessTypeInjector implements Comparable<AccessTypeInjector> {

  /**
   * Returns the name of the inheritable class.
   * 
   * @return A String with the class name
   */
  public String getClassName() {
    return getClass().getAnnotation(AccessTypeInjector.Qualifier.class).value().getCanonicalName();
  }

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
   * Allows the comparison between AccessTypeInjector classes. The getPriority() method is used to
   * determine the comparison result.
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

  /**
   * Defines the qualifier used to register an access type.
   */
  @javax.inject.Qualifier
  @Retention(RetentionPolicy.RUNTIME)
  @Target({ ElementType.TYPE })
  public @interface Qualifier {
    /**
     * Retrieves the class of the access type
     */
    Class<? extends InheritedAccessEnabled> value();
  }

  /**
   * A class used to select the correct access type injector.
   */
  @SuppressWarnings("all")
  public static class Selector extends AnnotationLiteral<AccessTypeInjector.Qualifier> implements
      AccessTypeInjector.Qualifier {
    private static final long serialVersionUID = 1L;

    Class<? extends InheritedAccessEnabled> clazz;

    /**
     * Basic constructor
     * 
     * @param className
     *          The name of the class handled by the injector
     * 
     * @throws Exception
     *           In case the class is not found or is not an instance of InheritedAccessEnabled an
     *           exception is thrown
     */
    @SuppressWarnings("unchecked")
    public <T extends InheritedAccessEnabled> Selector(String className) throws Exception {
      this.clazz = (Class<? extends InheritedAccessEnabled>) Class.forName(className);
    }

    public Class<? extends InheritedAccessEnabled> value() {
      return this.clazz;
    }
  }
}