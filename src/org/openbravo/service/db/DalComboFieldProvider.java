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

import java.util.ArrayList;
import java.util.List;

import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.data.FieldProvider;

/**
 * Creates a {@link FieldProvider} which reads data from a {@link BaseOBObject} and sets the correct
 * fields to populate a combobox.
 * 
 * @author mtaal
 */
public class DalComboFieldProvider extends DalMapFieldProvider {

  /**
   * Create an array of field providers from a list of {@link BaseOBObject}.
   * 
   * @param objects
   *          the list of objects for which to create FieldProvider objects
   * @return an array with the created FieldProviders
   */
  public static <T extends BaseOBObject> FieldProvider[] createFieldProviders(List<T> objects) {
    final List<FieldProvider> fieldProviders = new ArrayList<FieldProvider>();
    for (BaseOBObject object : objects) {
      final DalComboFieldProvider comboFieldProvider = new DalComboFieldProvider();
      comboFieldProvider.setOBObject(object);
      fieldProviders.add(comboFieldProvider);
    }
    return fieldProviders.toArray(new FieldProvider[fieldProviders.size()]);
  }

  public void setOBObject(BaseOBObject bob) {
    setValue("id", (String) bob.getId());
    setValue("name", bob.getIdentifier());
    setValue("description", bob.getIdentifier());
  }
}
