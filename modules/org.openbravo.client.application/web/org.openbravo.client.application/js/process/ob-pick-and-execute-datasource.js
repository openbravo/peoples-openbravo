/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.1  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html
 * Software distributed under the License  is  distribfuted  on  an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific  language  governing  rights  and  limitations
 * under the License.
 * The Original Code is Openbravo ERP.
 * The Initial Developer of the Original Code is Openbravo SLU
 * All portions are Copyright (C) 2011-2022 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

isc.defineClass('OBPickAndExecuteDataSource', isc.OBRestDataSource);

isc.OBPickAndExecuteDataSource.addProperties({
  fieldExists: function(fieldName) {
    const name = fieldName.startsWith('-') ? fieldName.substring(1) : fieldName;
    if (this.fields && this.fields[name]) {
      return true;
    }
    // look if it is the name of the display field of one of the view grid fields
    const gridFields = (this.view && this.view.gridFields) || [];
    return gridFields.some(f => f.displayField === name);
  }
});
