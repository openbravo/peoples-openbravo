/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.1  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use. this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html
 * Software distributed under the License  is  distributed  on  an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific  language  governing  rights  and  limitations
 * under the License.
 * The Original Code is Openbravo ERP.
 * The Initial Developer of the Original Code is Openbravo SLU
 * All portions are Copyright (C) 2014 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

// == OBFKComboItem ==
// UI Implementation for table and tableDir references
isc.ClassFactory.defineClass('OBFKComboItem', isc.OBSelectorItem);

isc.OBFKComboItem.addProperties({
  valueField: 'id',
  pickListFields: [{
    title: ' ',
    name: '_identifier',
    type: 'text'
  }],
  showSelectorGrid: false,
  selectorGridFields: [],
  extraSearchFields: [],
  displayField: '_identifier',

  init: function () {
    this.optionDataSource = OB.Datasource.create({
      dataURL: OB.Application.contextUrl + 'org.openbravo.service.datasource/ComboTableDatasourceService',
      fields: [{
        name: 'id',
        type: 'text',
        primaryKey: true
      }, {
        name: '_identifier'
      }],
      requestProperties: {
        params: {
          fieldId: this.id
        }
      }
    });
  }
});