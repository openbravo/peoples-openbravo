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

  setEntries: function (entries) {
    var length = entries.length,
        i, id, identifier, valueField = this.getValueFieldName(),
        valueMap = {};
    this.entries = [];
    for (i = 0; i < length; i++) {
      id = entries[i][OB.Constants.ID] || '';
      identifier = entries[i][OB.Constants.IDENTIFIER] || '';
      valueMap[id] = identifier.asHTML();
      this.entries[i] = {};
      this.entries[i][valueField] = id;
    }
    this.setValueMap(valueMap);
  },

  setEntry: function (id, identifier) {
    var i, entries = this.entries || [],
        entry = {},
        valueField = this.getValueFieldName(),
        length = entries.length;
    for (i = 0; i < length; i++) {
      if (entries[i][valueField] === id) {
        return;
      }
    }

    // not found add/create a new one
    entry[valueField] = id;
    entries.push(entry);

    this.setEntries(entries);
  },

  setValueFromRecord: function (record, fromPopup) {
    var currentValue = this.getValue(),
        identifierFieldName = this.name + OB.Constants.FIELDSEPARATOR + OB.Constants.IDENTIFIER,
        i;
    this._notUpdatingManually = true;
    if (!record) {
      this.storeValue(null);
      this.form.setValue(this.name + OB.Constants.FIELDSEPARATOR + this.displayField, null);
      this.form.setValue(identifierFieldName, null);

      // make sure that the grid does not display the old identifier
      if (this.form.grid && this.form.grid.getEditForm()) {
        this.form.grid.setEditValue(this.form.grid.getEditRow(), this.name, null);
      }
    } else {
      this.storeValue(record[this.valueField]);
      this.form.setValue(this.name, record[this.displayField]);
      if (!this.valueMap) {
        this.valueMap = {};
      }

      this.valueMap[record[this.valueField]] = record[this.displayField].replace(/[\n\r]/g, '');
      this.updateValueMap();
    }

    if (this.form && this.form.handleItemChange) {
      this._hasChanged = true;
      this.form.handleItemChange(this);
    }

    // only jump to the next field if the value has really been set
    // do not jump to the next field if the event has been triggered by the Tab key,
    // to prevent a field from being skipped (see https://issues.openbravo.com/view.php?id=21419)
    if (currentValue && this.form.focusInNextItem && isc.EH.getKeyName() !== 'Tab') {
      this.form.focusInNextItem(this.name);
    }
    delete this._notUpdatingManually;
  },

  // flag for table and tableDir references
  isComboReference: true,

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