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
 * All portions are Copyright (C) 2022 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

isc.ClassFactory.defineClass(
  'OBCharacteristicValueFilterItem',
  isc.OBFKFilterTextItem
);

isc.OBCharacteristicValueFilterItem.addProperties({
  /*init: function() {
    this.Super('init', arguments);
    const grid = this.form.grid.sourceWidget;
    const gridField = grid.getField(this.name);
  },*/

  createDataSource: function(grid, gridField) {
    const dataSource = OB.Datasource.create({
      dataURL:
        '/openbravo/org.openbravo.service.datasource/CharacteristicValue',
      requestProperties: {},
      fields: this.pickListFields
    });
    return dataSource;
  },

  /*getAppliedCriteria: function() {
    const baseCriteria = {
      operator: 'and',
      _constructor: 'AdvancedCriteria',
      criteria: [
        {
          fieldName: 'characteristic.id',
          operator: 'equals',
          value: '015D6C6072AC4A13B7573A261B2011BC'
        }
      ]
    };
    return baseCriteria;
  }*/

  getPickListFilterCriteria: function() {
    const baseCriteria = {
      operator: 'and',
      _constructor: 'AdvancedCriteria',
      criteria: [
        {
          fieldName: 'characteristic.id',
          operator: 'equals',
          value: this.characteristicId
        }
      ]
    };
    return baseCriteria;
  }
});
