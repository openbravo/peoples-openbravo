/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.0  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html 
 * Software distributed under the License  is  distributed  on  an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific  language  governing  rights  and  limitations
 * under the License. 
 * The Original Code is Openbravo ERP. 
 * The Initial Developer of the Original Code is Openbravo SL 
 * All portions are Copyright (C) 2009 Openbravo SL 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.base.model.domaintype;

/**
 * The ModelReference implements the reference extensions used for the Data Access Layer. See <a
 * href
 * ="http://wiki.openbravo.com/wiki/Projects/Reference_Extension/Technical_Documentation#DAL">here
 * </a> for more information.
 * 
 * @author mtaal
 */

public interface PrimitiveDomainType extends DomainType {

  /**
   * The primitive type class (for example java.lang.Long) if this is a primitive type.
   * 
   * @return the class representing the primitive type
   */
  Class<?> getPrimitiveType();

  /**
   * The type used in the hibernate mapping. Most of the time is the same as the
   * {@link #getPrimitiveType()}. Can be used to set a hibnernate user type class. See the hibernate
   * documentation for more information on this.
   * 
   * @return the class representing the hibernate type
   */
  Class<?> getHibernateType();

  /**
   * Returns the id of the format definition to use for this domain type. Is normally only relevant
   * for numeric domain types. The id is the prefix part of the name in the Format.xml file. So for
   * example the id 'integer' maps to all the Format.xml entries with integer as a prefix.
   * 
   * @return the name of the format definition in the format.xml, if not relevant then null is
   *         returned.
   */
  String getFormatId();
}
