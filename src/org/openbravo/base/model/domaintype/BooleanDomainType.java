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
 * The Initial Developer of the Original Code is Openbravo SLU 
 * All portions are Copyright (C) 2009 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.base.model.domaintype;

/**
 * The type for a yes/no or boolean column.
 * 
 * @author mtaal
 */

public class BooleanDomainType extends BasePrimitiveDomainType {

  /**
   * @return class of the {@link Boolean}
   * @see org.openbravo.base.model.domaintype.DomainType#getPrimitiveType()
   */
  public Class<?> getPrimitiveType() {
    return Boolean.class;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.openbravo.base.model.domaintype.PrimitiveDomainType#createFromString(java.lang.String)
   */
  @Override
  public Object createFromString(String strValue) {
    if (strValue == null || strValue.trim().length() == 0) {
      return null;
    }
    return new Boolean(strValue);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.openbravo.base.model.domaintype.PrimitiveDomainType#getXMLSchemaType()
   */
  @Override
  public String getXMLSchemaType() {
    return "ob:boolean";
  }

}
