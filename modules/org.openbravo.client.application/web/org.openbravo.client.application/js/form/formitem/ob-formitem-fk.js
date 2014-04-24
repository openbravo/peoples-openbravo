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
 * All portions are Copyright (C) 2011-2012 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

// == OBFKItem ==
// Extends OBListItem
isc.ClassFactory.defineClass('OBFKItem', isc.OBListItem);

isc.ClassFactory.mixInInterface('OBFKItem', 'OBLinkTitleItem');

isc.OBFKItem.addProperties({
  operator: 'iContains',

  fetchMissingValues: false,
  showOptionsFromDataSource: true,
  autoFetchData: false,
  hasPickList: true,
  tempValueMap: this.valueMap,

  mapValueToDisplay: function (value) {
    var i, ret = this.Super('mapValueToDisplay', arguments),
        result;

    // the datasource should handle it
/*if (this.optionDataSource) {
      return ret;
    }*/

    if (this.valueMap) {
      // handle multi-select
      if (isc.isA.Array(value)) {
        this.lastSelectedValue = value;
        for (i = 0; i < value.length; i++) {
          if (i > 0) {
            result += this.multipleValueSeparator;
          }
          // encode or and and
          result += OB.Utilities.encodeSearchOperator(this.Super('mapValueToDisplay', value[i]));
        }
      } else if (this.valueMap[value]) {
        this.lastSelectedValue = value;
        return this.valueMap[value].unescapeHTML();
      }
    }

    if (ret === value && this.isDisabled()) {
      return '';
    }

    // don't update the valuemap if the value is null or undefined
    if (ret === value && value) {
      if (!this.valueMap) {
        this.valueMap = {};
        this.valueMap[value] = '';
        return '';
      } //there may be cases if the value is an number within 10 digits, it is identified as an UUID. In that case check is done to confirm whether it is indeed UUID by checking if it is available in the valueMap.
      else if (!this.valueMap[value] && OB.Utilities.isUUID(value) && this.valueMap.hasOwnProperty(value)) {
        return '';
      }
    }
    return ret;
  },

  dataArrived: function (startRow, endRow, data) {
    var i, allRows;
    if (data && data.allRows) {
      allRows = data.allRows;
      for (i = 0; i < allRows.length; i++) {
        this.valueMap[allRows[i].id] = allRows[i]._identifier;
      }
    }
  },

  // set the identifier field also, that's what gets displayed in the grid
  changed: function (form, item, value) {
    if (!this._pickedValue && value) {
      return;
    }
    if (value === undefined) {
      if (this.getElementValue() !== undefined) {
        value = this.getElementValue();
      }
    }
    var display = this.mapValueToDisplay(value),
        identifierFieldName = this.name + OB.Constants.FIELDSEPARATOR + OB.Constants.IDENTIFIER;
    form.setValue(identifierFieldName, display);
    // make sure that the grid does not display the old identifier
    if (form.grid) {
      form.grid.setEditValue(form.grid.getEditRow(), identifierFieldName, display);
    }
    return this.Super('changed', arguments);
  },

  setValue: function (val) {
    if (this._clearingValue) {
      this._editorEnterValue = null;
    }
    this.Super('setValue', arguments);
  },

  init: function () {
    this.displayField = '_identifier';
    this.optionDataSource = OB.Datasource.create({
      dataURL: '/openbravo/org.openbravo.service.datasource/ComboTableDatasourceService',
      fields: [{
        name: 'id',
        type: this.type,
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