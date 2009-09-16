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

package org.openbravo.service.db;

import java.util.HashMap;
import java.util.Map;

import org.openbravo.data.FieldProvider;

/**
 * Implementation of a {@link FieldProvider} which is backed with a normal Map. This can be used to
 * pass computed information to the XMLEngine.
 * 
 * @author mtaal
 */
public class DalMapFieldProvider implements FieldProvider {

  private Map<String, String> values = new HashMap<String, String>();

  public String getField(String fieldName) {
    return values.get(fieldName);
  }

  public void setValue(String fieldName, String value) {
    values.put(fieldName, value);
  }

}
