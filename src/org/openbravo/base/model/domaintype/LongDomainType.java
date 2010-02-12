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
 * The type for an integer/long column.
 * 
 * @author mtaal
 */

public class LongDomainType extends BasePrimitiveDomainType {

  /**
   * @return class of the {@link Long}
   * @see org.openbravo.base.model.domaintype.DomainType#getPrimitiveType()
   */
  public Class<?> getPrimitiveType() {
    return Long.class;
  }

  public void checkIsValidValue(Property property, Object value) throws ValidationException {
    if (value == null) {
      return;
    }
    if (Integer.class.isInstance(value)) {
      // is allowed
      return;
    }
    super.checkIsValidValue(property, value);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.openbravo.base.model.domaintype.PrimitiveDomainType#getFormatId()
   */
  public String getFormatId() {
    return "integer";
  }
}
