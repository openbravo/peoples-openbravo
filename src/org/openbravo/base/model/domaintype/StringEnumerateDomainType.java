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
 * The type of a column which can only have a value from a pre-defined set.
 * 
 * @author mtaal
 */

public class StringEnumerateDomainType extends BaseEnumerateDomainType<String> {

  /**
   * As a standard only a string/varchar column can have enumerates.
   * 
   * @return class of {@link String}.
   * @see org.openbravo.base.model.domaintype.DomainType#getPrimitiveType()
   */
  public Class<?> getPrimitiveType() {
    return String.class;
  }
}
