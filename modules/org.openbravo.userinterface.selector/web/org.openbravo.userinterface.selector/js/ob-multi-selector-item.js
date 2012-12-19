/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.1  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html
 * Software distributed under the License  is  distributed  on  an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific  language  governing  rights  and  limitations
 * under the License.
 * The Original Code is Openbravo ERP.
 * The Initial Developer of the Original Code is Openbravo SLU
 * All portions are Copyright (C) 2012 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

isc.ClassFactory.defineClass('OBMultiSelectorItem', isc.MultiComboBoxItem);
//isc.ClassFactory.mixInInterface('OBMultiSelectorItem', 'OBSelectorItem');
//isc.OBMultiSelectorItem.addProperties(isc.addProperties({},isc.OBSelectorItem));
isc.OBMultiSelectorItem.addProperties({
  hasPickList: true,

  popupTextMatchStyle: 'startswith',
  suggestionTextMatchStyle: 'startswith',
  showOptionsFromDataSource: true,

  // https://issues.openbravo.com/view.php?id=18739
  selectOnFocus: false,
  // still do select on focus initially
  doInitialSelectOnFocus: true,

  // Setting this to false results in the picklist to be shown 
  // on focus, specific SC logic
  //  addUnknownValues: false,
  // ** {{{ selectorGridFields }}} **
  // the definition of the columns in the popup window
  selectorGridFields: [{
    title: OB.I18N.getLabel('OBUISC_Identifier'),
    name: OB.Constants.IDENTIFIER
  }],

  // Do not fetch data upon creation
  // http://www.smartclient.com/docs/8.1/a/b/c/go.html#attr..ComboBoxItem.optionDataSource
  fetchMissingValues: false,

  autoFetchData: false,
  // showPickerIcon: true,
  //  selectors should not be validated on change, only after its content has been deleted
  //  and after an option of the combo has been selected
  //  see issue 19956 (https://issues.openbravo.com/view.php?id=19956)
  validateOnChange: false,
  completeOnTab: true,
  // note validateonexit does not work when completeOnTab is true
  // setting it anyway, the this.validate() is called in the blur
  validateOnExit: true,

  pickListProperties: {
    fetchDelay: 400,
    showHeaderContextMenu: false,
    dataProperties: {
      useClientFiltering: false
    }
  },
  // ---
  //  hidePickListOnBlur: function () {
  //    // when the form gets redrawn the focus may not be in
  //    // the item but it is still the item which gets the focus
  //    // after redrawing
  //    if (this.form && this.form._isRedrawing && this.form.getFocusItem() === this) {
  //      return;
  //    }
  //
  //    this.Super('hidePickListOnBlur', arguments);
  //  },
  //  setUpPickList: function (show, queueFetches, request) {
  //    this.pickListProperties.canResizeFields = true;
  //    // Set the pickListWidth just before being shown.
  //    this.setPickListWidth();
  //    this.Super('setUpPickList', arguments);
  //  },
  //  // don't do update value in all cases, updatevalue results in a data source request
  //  // to the server, so only do updatevalue when the user changes information
  //  // https://issues.openbravo.com/view.php?id=16611
  //  updateValue: function () {
  //    if (this.form && this.form.grid && (this.form.grid._storingUpdatedEditorValue || this.form.grid._showingEditor || this.form.grid._hidingInlineEditor)) {
  //      // prevent updatevalue while the form is being shown or hidden
  //      return;
  //    }
  //    this.Super('updateValue', arguments);
  //  },
  //---
  setValue: function (val) {
    var i, displayedVal;

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
  //  changed: function (form, item, newValue) {
  //    // only do the identifier actions when clearing
  //    // in all other cases pickValue is called
  //    if (!newValue) {
  //      this.setValueFromRecord(null);
  //    }
  //    //Setting the element value again to align the cursor position correctly.
  //    this.setElementValue(newValue);
  //  },
  setPickListWidth: function () {
    var extraWidth = 0,
        fieldWidth = this.getVisibleWidth();
    if (this.pickListFields.length > 1) {
      extraWidth = 150 * (this.pickListFields.length - 1);
    }

    this.pickListWidth = (fieldWidth < 150 ? 150 : fieldWidth) + extraWidth;
  },

  enableShortcuts: function () {
    var ksAction_ShowPopup;

    ksAction_ShowPopup = function (caller) {
      caller.openSelectorWindow();
      return false; //To avoid keyboard shortcut propagation
    };
    OB.KeyboardManager.Shortcuts.set('Selector_ShowPopup', ['OBSelectorItem', 'OBSelectorItem.icon'], ksAction_ShowPopup);
  },

  init: function () {
    this.enableShortcuts();
    this.icons = [{
      selector: this,
      src: this.popupIconSrc,
      width: this.popupIconWidth,
      height: this.popupIconHeight,
      hspace: this.popupIconHspace,
      keyPress: function (keyName, character, form, item, icon) {
        var response = OB.KeyboardManager.Shortcuts.monitor('OBSelectorItem.icon', this.selector);
        if (response !== false) {
          response = this.Super('keyPress', arguments);
        }
        return response;
      },
      click: function (form, item, icon) {
        item.openSelectorWindow();
      }
    }];

    if (this.disabled) {
      // TODO: disable, remove icons
      this.icons = null;
    }
    if (!this.showSelectorGrid) {
      this.icons = null;
    }

    if (this.showSelectorGrid && !this.form.isPreviewForm) {

      this.selectorGridFields.unshift({
        name: '_pin',
        type: 'boolean',
        title: '&nbsp;',
        canEdit: false,
        canFilter: false,
        canSort: false,
        canReorder: false,
        canHide: false,
        frozen: true,
        canFreeze: false,
        canDragResize: false,
        canGroupBy: false,
        autoExpand: false,
        width: OB.Styles.Process.PickAndExecute.pinColumnWidth,
        formatCellValue: function (value, record, rowNum, colNum, grid) {
          if (record[grid.selectionProperty]) {
            return '<img class="' + OB.Styles.Process.PickAndExecute.iconPinStyle + '" src="' + OB.Styles.Process.PickAndExecute.iconPinSrc + '" />';
          }
          return '';
        },
        formatEditorValue: function (value, record, rowNum, colNum, grid) {
          return this.formatCellValue(arguments);
        }
      });
      this.selectorWindow = isc.OBSelectorPopupWindow.create({
        // solves issue: https://issues.openbravo.com/view.php?id=17268
        title: (this.form && this.form.grid ? this.form.grid.getField(this.name).title : this.title),
        dataSource: this.optionDataSource,
        selector: this,
        valueField: this.valueField,
        displayField: this.displayField,
        selectorGridFields: isc.shallowClone(this.selectorGridFields),
        selectionAppearance: 'checkbox',
        multiselect: true
      });
    }

    this.optionCriteria = {
      _selectorDefinitionId: this.selectorDefinitionId
    };

    //debugger;
    return this.Super('init', arguments);


  },




  initWidget: function () {
    this.buttonDefaults.iconOrientation = 'left';
    this.Super('initWidget', arguments);
  },

  setValueFromRecord: function (record, fromPopup) {
    var currentValue = this.getValue(),
        identifierFieldName = this.name + OB.Constants.FIELDSEPARATOR + OB.Constants.IDENTIFIER,
        i;

    if (!this.valueMap) {
      this.valueMap = {};
    }

    this.valueMap[record[OB.Constants.ID]] = record[OB.Constants.IDENTIFIER];
    this.updateValueMap();

    this._insertButtonForValue(record[OB.Constants.IDENTIFIER]);
    return;
  },

  // override blur to not do any change handling
  blur: function (form, item) {},

  handleOutFields: function (record) {
    var i, j, outFields = this.outFields,
        form = this.form,
        grid = this.grid,
        item, value, fields, numberFormat;

    if ((!form || (form && !form.fields)) && (!grid || (grid && !grid.fields))) {
      // not handling out fields
      return;
    }

    fields = form.fields || grid.fields;
    for (i in outFields) {
      if (outFields.hasOwnProperty(i)) {
        if (outFields[i].suffix) {
          // when it has a suffix
          if (record) {
            value = record[i];
            if (typeof value === 'undefined') {
              form.hiddenInputs[this.outHiddenInputPrefix + outFields[i].suffix] = '';
              continue;
            }
            if (isc.isA.Number(value)) {
              if (outFields[i].formatType && outFields[i].formatType !== '') {
                value = OB.Utilities.Number.JSToOBMasked(value, OB.Format.formats[outFields[i].formatType], OB.Format.defaultDecimalSymbol, OB.Format.defaultGroupingSymbol, OB.Format.defaultGroupingSize);
              } else {
                value = value.toString().replace('.', OB.Format.defaultDecimalSymbol);
              }
            }
            form.hiddenInputs[this.outHiddenInputPrefix + outFields[i].suffix] = value;
            item = form.getItem(outFields[i].fieldName);
            if (item && item.valueMap) {
              item.valueMap[value] = record[outFields[i].fieldName + OB.Constants.FIELDSEPARATOR + OB.Constants.IDENTIFIER];
            }
          } else {
            form.hiddenInputs[this.outHiddenInputPrefix + outFields[i].suffix] = null;
          }
        } else {
          // it does not have a suffix
          for (j = 0; j < fields.length; j++) {
            if (fields[j].name !== '' && fields[j].name === outFields[i].fieldName) {
              if (record) {
                value = record[i];
                if (typeof value === 'undefined') {
                  continue;
                }
              } else {
                value = null;
              }
              fields[j].setValue(value);
            }
          }
        }
      }
    }
  },

  openSelectorWindow: function () {
    // always refresh the content of the grid to force a reload
    // if the organization has changed
    if (this.selectorWindow.selectorGrid) {
      this.selectorWindow.selectorGrid.invalidateCache();
    }
    this.selectorWindow.open();
  },

  keyPress: function (item, form, keyName, characterValue) {
    var response = OB.KeyboardManager.Shortcuts.monitor('OBSelectorItem', this);
    if (response !== false) {
      response = this.Super('keyPress', arguments);
    }
    return response;
  },

  //  pickValue: function (value) {
  //    debugger;
  //    // get the selected record before calling the super, as this super call
  //    // will deselect the record
  //    var selectedRecord = this.pickList.getSelectedRecord(),
  //        ret = this.Super('pickValue', arguments);
  //    this.setValueFromRecord(selectedRecord);
  //    return ret;
  //  },
  filterDataBoundPickList: function (requestProperties, dropCache) {
    requestProperties = requestProperties || {};
    requestProperties.params = requestProperties.params || {};

    // sometimes the value is passed as a filter criteria remove it
    if (this.getValueFieldName() && requestProperties.params[this.getValueFieldName()]) {
      requestProperties.params[this.getValueFieldName()] = null;
    }

    // do not prevent the count operation
    requestProperties.params[isc.OBViewGrid.NO_COUNT_PARAMETER] = 'true';

    // on purpose not passing the third boolean param
    if (this.form && this.form.view && this.form.getContextInfo) {
      isc.addProperties(requestProperties.params, this.form.view.getContextInfo(false, true));
    } else if (this.view && this.view.sourceView && this.view.sourceView.getContextInfo) {
      isc.addProperties(requestProperties.params, this.view.sourceView.getContextInfo(false, true));
    }

    if (this.form && this.form.view && this.form.view.standardWindow) {
      isc.addProperties(requestProperties.params, {
        windowId: this.form.view.standardWindow.windowId,
        tabId: this.form.view.tabId,
        moduleId: this.form.view.moduleId
      });
    }

    // also add the special ORG parameter
    if (requestProperties.params.inpadOrgId) {
      requestProperties.params[OB.Constants.ORG_PARAMETER] = requestProperties.params.inpadOrgId;
    }

    if (this.form.getFocusItem() !== this && !this.form.view.isShowingForm && this.getEnteredValue() === '' && this.savedEnteredValue) {
      this.setElementValue(this.savedEnteredValue);
      delete this.savedEnteredValue;
    } else if (this.form && this.form.view && this.form.view.isShowingForm && this.getEnteredValue() === '' && this.savedEnteredValue) {
      this.setElementValue(this.savedEnteredValue);
      delete this.savedEnteredValue;
    }

    var criteria = this.getPickListFilterCriteria(),
        i;
    for (i = 0; i < criteria.criteria.length; i++) {
      if (criteria.criteria[i].fieldName === this.displayField) {
        // for the suggestion box it is one big or
        requestProperties.params[OB.Constants.OR_EXPRESSION] = 'true';
      }
    }

    // adds the selector id to filter used to get filter information
    requestProperties.params._selectorDefinitionId = this.selectorDefinitionId;

    // add field's default filter expressions
    requestProperties.params.filterClass = 'org.openbravo.userinterface.selector.SelectorDataSourceFilter';

    // the additional where clause
    requestProperties.params[OB.Constants.WHERE_PARAMETER] = this.whereClause;

    // and sort according to the display field
    // initially
    requestProperties.params[OB.Constants.SORTBY_PARAMETER] = this.displayField;

    return this.Super('filterDataBoundPickList', [requestProperties, dropCache]);
  },

  getPickListFilterCriteria: function () {
    var crit = this.Super('getPickListFilterCriteria', arguments),
        operator;
    this.pickList.data.useClientFiltering = false;
    var criteria = {
      operator: 'or',
      _constructor: 'AdvancedCriteria',
      criteria: []
    };

    // add a dummy criteria to force a fetch
    criteria.criteria.push(isc.OBRestDataSource.getDummyCriterion());

    // only filter if the display field is also passed
    // the displayField filter is not passed when the user clicks the drop-down button
    // display field is passed on the criteria.
    var displayFieldValue = null,
        i;
    if (crit.criteria) {
      for (i = 0; i < crit.criteria.length; i++) {
        if (crit.criteria[i].fieldName === this.displayField) {
          displayFieldValue = crit.criteria[i].value;
        }
      }
    } else if (crit[this.displayField]) {
      displayFieldValue = crit[this.displayField];
    }
    if (displayFieldValue !== null) {
      if (this.textMatchStyle === 'substring') {
        operator = 'iContains';
      } else {
        operator = 'iStartsWith';
      }
      for (i = 0; i < this.extraSearchFields.length; i++) {
        criteria.criteria.push({
          fieldName: this.extraSearchFields[i],
          operator: operator,
          value: displayFieldValue
        });
      }
      criteria.criteria.push({
        fieldName: this.displayField,
        operator: operator,
        value: displayFieldValue
      });
    }
    return criteria;
  },

  //  getDisplayValue: function (value) {
  //    var ret;
  //
  //    if (this.valueMap && this.valueMap[value]) {
  //      return this.valueMap[value];
  //    }
  //    return value;
  //  },
  mapValueToDisplay: function (value) {
    var ret = this.Super('mapValueToDisplay', arguments);
    if (ret === value && this.isDisabled()) {
      return '';
    }
    // if value is null then don't set it in the valueMap, this results 
    // in null being displayed in the combobox
    if (ret === value && value) {
      if (!this.valueMap) {
        this.vOBSelectorItemalueMap = {};
        this.valueMap[value] = '';
        return '';
      } else if (!this.valueMap[value] && OB.Utilities.isUUID(value)) {
        return '';
      }
    }
    if (value && value !== '' && ret === '') {
      this.savedEnteredValue = value;
    }
    return ret;
  },

  mapDisplayToValue: function (value) {
    if (value === '') {
      return null;
    }
    return this.Super('mapDisplayToValue', arguments);
  },
  destroy: function () {
    // Explicitly destroy the selector window to avoid memory leaks
    if (this.selectorWindow) {
      this.selectorWindow.destroy();
      this.selectorWindow = null;
    }
    this.Super('destroy', arguments);
  }
});

isc.OBMultiSelectorItem.addProperties({
  layoutStyle: 'vertical',
  useInsertionOrder: false,

  _createCanvas: function () {
    // Overriding _createCanvas to change layout, selected items
    // are displayed in a scrollable VLayout
    var canvasProperties, combo, myLayout;

    this.Super("_createCanvas", arguments);

    // remove combo from buttons layout to draw it appart
    combo = this._buttonsLayout.getMember(0);
    this._buttonsLayout.removeMember(combo, true);

    canvasProperties = {
      autoDraw: false,
      height: 1,
      minHeight: 0,
      layoutTopMargin: 1,
      members: [combo, this._buttonsLayout]
    };

    // remove all members in canvas
    this.canvas.removeMembers(this.canvas.getMembers());

    // to recreate it again
    myLayout = isc.VLayout.create(canvasProperties);
    this.canvas.addMember(myLayout);
  },

  _createVStack: function () {
    // Overriding _createVStack to change default overflow
    return isc.VStack.create({
      autoDraw: false,
      height: 150,
      overflow: 'auto',
      members: [],
      animateMembers: true,
      animateMemberTime: 100
    });
  }
});