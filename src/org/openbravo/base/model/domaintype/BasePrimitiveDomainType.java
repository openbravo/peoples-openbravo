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

import org.openbravo.base.model.Property;
import org.openbravo.base.validation.ValidationException;

/**
 * The base class for primitive property types. Subclasses only need to implement
 * {@link DomainType#getPrimitiveType()}.
 * 
 * @author mtaal
 */
public abstract class BasePrimitiveDomainType extends BaseDomainType implements PrimitiveDomainType {

  /**
   * The type used in the hibernate mapping. Most of the time is the same as the
   * {@link #getPrimitiveType()}. Can be used to set a hibnernate user type class. See the hibernate
   * documentation for more information on this.
   * 
   * This method will be moved to the PrimitiveDomainType in a later stage.
   * 
   * @return the class representing the hibernate type
   */
  public Class<?> getHibernateType() {
    return getPrimitiveType();
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.openbravo.base.model.domaintype.DomainType#checkIsValidValue(org.openbravo.base.model.Property
   * , java.lang.Object)
   */
  public void checkIsValidValue(Property property, Object value) throws ValidationException {
    if (value == null) {
      return;
    }
    if (!getPrimitiveType().isInstance(value)) {
      final ValidationException ve = new ValidationException();
      ve.addMessage(property, "Property " + property + " only allows instances of "
          + getPrimitiveType().getName() + " but the value is an instanceof "
          + value.getClass().getName());
      throw ve;
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.openbravo.base.model.domaintype.PrimitiveDomainType#getFormatId()
   */
  public String getFormatId() {
    return null;
  }

}
