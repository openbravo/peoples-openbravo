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
 * All portions are Copyright (C) 2011-2014 Openbravo SLU
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
  autoFetchData: false,
  hasPickList: true,

  pickListProperties: {
    fetchDelay: 400,
    showHeaderContextMenu: false,
    dataProperties: {
      useClientFiltering: false
    }
  },

  // don't do update value in all cases, updatevalue results in a data source request
  // to the server, so only do updatevalue when the user changes information
  // https://issues.openbravo.com/view.php?id=16611
  updateValue: function () {
    if (this.form && this.form.grid && (this.form.grid._storingUpdatedEditorValue || this.form.grid._showingEditor || this.form.grid._hidingInlineEditor)) {
      // prevent updatevalue while the form is being shown or hidden
      return;
    }
    this.Super('updateValue', arguments);
  },


  mapValueToDisplay: function (value) {
    var i, ret = this.Super('mapValueToDisplay', arguments),
        result;

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

  setValueFromRecord: function (record, fromPopup) {
    var currentValue = this.getValue(),
        identifierFieldName = this.name + OB.Constants.FIELDSEPARATOR + OB.Constants.IDENTIFIER,
        i;
    this._notUpdatingManually = true;
    if (!record) {
      this.storeValue(null);
      this.form.setValue(this.name, null);

      // make sure that the grid does not display the old identifier
      if (this.form.grid && this.form.grid.getEditForm()) {
        this.form.grid.setEditValue(this.form.grid.getEditRow(), this.name, '');
      }
    } else {
      this.form.setValue(this.name, record[OB.Constants.IDENTIFIER]);
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

  pickValue: function (value) {
    // get the selected record before calling the super, as this super call
    // will deselect the record
    var selectedRecord = this.pickList.getSelectedRecord(),
        ret = this.Super('pickValue', arguments);
    this.setValueFromRecord(selectedRecord);
    delete this.fullIdentifierEntered;
    return ret;
  },

  dataArrived: function (startRow, endRow, data) {
    var i, allRows;
    if (data && data.localData) {
      allRows = data.localData || data.allRows;
      for (i = 0; i < allRows.length; i++) {
        if (allRows[i] && allRows[i].id && allRows[i]._identifier) {
          this.valueMap[allRows[i].id] = allRows[i]._identifier;
        }
      }
    }
  },

  setValue: function (val) {
    var i, displayedVal;
    val = this.mapDisplayToValue(val);
    if (val && this.valueMap) {
      displayedVal = this.valueMap[val];
      for (i in this.valueMap) {
        if (this.valueMap.hasOwnProperty(i)) {
          if (this.valueMap[i] === displayedVal && i !== val) {
            // cleaning up valueMap: there are 2 values that display the same info, keep just the one for
            // the current value
            delete this.valueMap[i];
            break;
          }
        }
      }
    } else { //Select by default the first option in the picklist, if possible
      this.selectFirstPickListOption();
    }

    if (this._clearingValue) {
      this._editorEnterValue = null;
    }

    this.Super('setValue', arguments);
  },

  selectFirstPickListOption: function () {
    var firstRecord;
    if (this.pickList) {
      if (this.pickList.data && (this.pickList.data.totalRows > 0)) {
        firstRecord = this.pickList.data.get(0);
        this.pickList.selection.selectSingle(firstRecord);
        this.pickList.clearLastHilite();
        this.pickList.scrollRecordIntoView(0);
      }
    }
  },

  // changed handles the case that the user removes the value using the keyboard
  // this should do the same things as setting the value through the pickvalue
  changed: function (form, item, newValue) {
    var identifier;
    // only do the identifier actions when clearing
    // in all other cases pickValue is called
    if (!newValue) {
      this.setValueFromRecord(null);
    }
    if (OB.Utilities.isUUID(newValue)) {
      identifier = this.mapValueToDisplay(newValue);
    } else {
      identifier = newValue;
    }

    // check if the whole item identifier has been entered
    // see issue https://issues.openbravo.com/view.php?id=22821
    if (OB.Utilities.isUUID(this.mapDisplayToValue(identifier)) && this._notUpdatingManually !== true) {
      this.fullIdentifierEntered = true;
    } else {
      delete this.fullIdentifierEntered;
    }

    //Setting the element value again to align the cursor position correctly.
    //Before setting the value check if the identifier is part of the value map or the full identifier is entered.
    //If it fails set newValue as value.
    if ((this.valueMap && this.valueMap[newValue] === identifier && identifier && identifier.trim() !== '') || this.fullIdentifierEntered) {
      this.setElementValue(identifier);
    } else {
      this.setElementValue(newValue);
    }
  },

  filterDataBoundPickList: function (requestProperties, dropCache) {
    requestProperties = requestProperties || {};
    requestProperties.params = requestProperties.params || {};

    this.prepareDSRequest(requestProperties.params, this);

    requestProperties.params[isc.OBViewGrid.NO_COUNT_PARAMETER] = 'true';

    if (this.form.getFocusItem() !== this && !this.form.view.isShowingForm && this.getEnteredValue() === '' && this.savedEnteredValue) {
      this.setElementValue(this.savedEnteredValue);
      delete this.savedEnteredValue;
    } else if (this.form && this.form.view && this.form.view.isShowingForm && this.savedEnteredValue) {
      if (this.getEnteredValue() !== '') {
        this.setElementValue(this.savedEnteredValue + this.getEnteredValue());
      } else {
        this.setElementValue(this.savedEnteredValue);
      }
      delete this.savedEnteredValue;
    }

    return this.Super('filterDataBoundPickList', [requestProperties, dropCache]);
  },

  prepareDSRequest: function (params, field) {
    // on purpose not passing the third boolean param
    if (field.form && field.form.view && field.form.view.getContextInfo) {
      isc.addProperties(params, field.form.view.getContextInfo(false, true));
    } else if (field.view && field.view.sourceView && field.view.sourceView.getContextInfo) {
      isc.addProperties(params, field.view.sourceView.getContextInfo(false, true));
    }

    if (field.form && field.form.view && field.form.view.standardWindow) {
      isc.addProperties(params, {
        windowId: field.form.view.standardWindow.windowId,
        tabId: field.form.view.tabId,
        moduleId: field.form.view.moduleId
      });
    }

    // Include the windowId in the params if possible
    if (field.form && field.form.view && field.form.view.standardProperties && field.form.view.standardProperties.inpwindowId) {
      params.windowId = field.form.view.standardProperties.inpwindowId;
    }

    // also add the special ORG parameter
    if (params.inpadOrgId) {
      params[OB.Constants.ORG_PARAMETER] = params.inpadOrgId;
    }


  },

  init: function () {
    this.displayField = '_identifier';
    this.optionDataSource = OB.Datasource.create({
      dataURL: OB.Application.contextUrl + 'org.openbravo.service.datasource/ComboTableDatasourceService',
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