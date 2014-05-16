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
  addDummyCriterion: false,
  textMatchStyle: 'substring',
  pickListFields: [{
    title: ' ',
    name: '_identifier',
    type: 'text'
  }],
  showSelectorGrid: false,
  selectorGridFields: [],
  extraSearchFields: [],
  displayField: '_identifier',

  // flag for table and tableDir references
  isComboReference: true,

  filterPickList: function () {
    if (this.preventPickListRequest) {
      // nothing to filter, prevent DS request in this case
      delete this.preventPickListRequest;
      this.addDummyCriterion = false;
      return;
    }

    if (this.wholeMapSet) {
      // Ignore any requestProperties passed in for a client-only filter.
      var records = this.filterClientPickListData();
      if (this.pickList.data !== records) {
        this.pickList.setData(records);
      }

      // explicitly fire filterComplete() as we have now filtered the data for the 
      // pickList
      this.filterComplete();
      if (!this.isPickListShown()) {
        this.placePickList();
        this.pickList.show();
      }
      return;
    }
    this.Super('filterPickList', arguments);
  },

  getPickListFilterCriteria: function () {
    var criteria = this.Super('getPickListFilterCriteria', arguments),
        simpleCriteria = {},
        i;
    if (this.wholeMapSet) {
      // filterClientPickListData doesn't support advanced criteria, let's transform it
      // here to make it work
      if (criteria.criteria) {
        for (i = 0; i < criteria.criteria.length; i++) {
          simpleCriteria[criteria.criteria[i].fieldName] = criteria.criteria[i].value;
        }
      }
      return simpleCriteria;
    }

    return criteria;
  },

  init: function () {
    this.Super('init', arguments);
    this.optionDataSource = OB.Datasource.create({
      dataURL: OB.Application.contextUrl + 'org.openbravo.service.datasource/ComboTableDatasourceService',
      fields: [{
        name: 'id',
        type: 'text',
        primaryKey: true,
        escapeHTML: true
      }, {
        name: '_identifier',
        escapeHTML: true
      }],
      requestProperties: {
        params: {
          fieldId: this.id
        }
      }
    });
  }
});