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
 * All portions are Copyright (C) 2011 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
// = OBSelectorPopupWindow =
// The selector popup window shown when clicking the picker icon. Contains 
// a selection grid and cancel/ok buttons.
//
isc.ClassFactory.defineClass('OBSelectorPopupWindow', isc.Window);

isc.OBSelectorPopupWindow.addProperties({
  canDragReposition: true,
  canDragResize: true,
  dismissOnEscape: true,
  showMaximizeButton: true,
  
  defaultSelectorGridField: {
    canFreeze: true,
    canGroupBy: false,
    filterOnKeypress: true
  },
  
  initWidget: function(){
    var selectorWindow = this;
    this.setFilterEditorProperties(this.selectorGridFields);
    
    var okButton = isc.OBFormButton.create({
      title: OB.I18N.getLabel('OBUISC_Dialog.OK_BUTTON_TITLE'),
      click: function(){
        selectorWindow.setValueInField();
      }
    });
    var cancelButton = isc.OBFormButton.create({
      title: OB.I18N.getLabel('OBUISC_Dialog.CANCEL_BUTTON_TITLE'),
      click: function(){
        selectorWindow.hide();
      }
    });
    
    OB.Utilities.applyDefaultValues(this.selectorGridFields, this.defaultSelectorGridField);
    
    this.selectorGrid = isc.OBGrid.create({
    
      selector: this.selector,
      
      dataProperties: {
        useClientFiltering: false,
        useClientSorting: false
      },
      
      width: this.selectorGridProperties.width,
      height: this.selectorGridProperties.height,
      alternateRecordStyles: this.selectorGridProperties.alternateRecordStyles,
      dataSource: this.dataSource,
      showFilterEditor: true,
      sortField: this.displayField,
      filterData: function(criteria, callback, requestProperties){
        return this.Super('filterData', [this.convertCriteria(criteria), callback, requestProperties]);
      },
      fetchData: function(criteria, callback, requestProperties){
        return this.Super('fetchData', [this.convertCriteria(criteria), callback, requestProperties]);
      },
      convertCriteria: function(criteria){
      
        if (!criteria) {
          criteria = {};
        }
        
        this.selector.form.view.getContextInfo(criteria, {});
        
        // also adds the special ORG parameter
        if (this.selector.form.getField('organization')) {
          criteria[OB.Constants.ORG_PARAMETER] = this.selector.form.getValue('organization');
        }
        criteria[OB.Constants.WHERE_PARAMETER] = this.selector.whereClause;
        
        // set the default sort option
        criteria[OB.Constants.SORTBY_PARAMETER] = this.displayField;
        criteria[OB.Constants.TEXT_MATCH_PARAMETER_OVERRIDE] = this.selector.popupTextMatchStyle;
        
        criteria._selectorDefinitionId = this.selector.selectorDefinitionId;
        criteria._requestType = 'Window';
        return criteria;
      },
      dataArrived: function(){
      
        this.Super('dataArrived', arguments);
        
        // check if a record has been selected, if
        // not take the one
        // from the selectorField
        // by doing this when data arrives the selection
        // will show up
        // when the record shows in view
        if (!this.getSelectedRecord()) {
          if (this.selector.getValue()) {
            this.selectSingleRecord(this.data.find(this.valueField, this.selector.getValue()));
          } else {
            this.selectSingleRecord(null);
          }
        }
      },
      fields: this.selectorGridFields,
      recordDoubleClick: function(){
        selectorWindow.setValueInField();
      }
    });
    
    this.items = [this.selectorGrid, isc.HLayout.create({
      styleName: this.buttonBarStyleName,
      height: this.buttonBarHeight,
      defaultLayoutAlign: 'center',
      members: [isc.LayoutSpacer.create({}), okButton, isc.LayoutSpacer.create({
        width: this.buttonBarSpace
      }), cancelButton, isc.LayoutSpacer.create({})]
    })];
    return this.Super('initWidget', arguments);
  },
  
  setFilterEditorProperties: function(gridFields){
    var selectorWindow = this;
    var keyPressFunction = function(item, form, keyName, characterValue){
      if (keyName === 'Escape') {
        selectorWindow.hide();
        return false;
      }
      return true;
    };
    
    var clickFunction = function(form, item, icon){
      item.setValue(null);
      selectorWindow.selectorGrid.focusInFilterEditor(item);
      selectorWindow.selectorGrid.filterByEditor();
    };
    
    for (var i = 0; i < gridFields.length; i++) {
      var gridField = gridFields[i];
      if (!gridField.filterEditorProperties) {
        gridField.filterEditorProperties = {
          required: false
        };
      } else {
        gridField.filterEditorProperties.required = false;
      }
      
      gridField.filterEditorProperties.keyPress = keyPressFunction;
      
      if (!gridField.filterEditorProperties.icons) {
        gridField.filterEditorProperties.icons = [];
      }
      
      gridField.filterEditorProperties.showLabel = false;
      gridField.filterEditorProperties.showTitle = false;
      
      gridField.filterEditorProperties.textMatchStyle = selectorWindow.selector.popupTextMatchStyle;
      
      // add the icon on the right to the other icons
      //      var icons = gridField.filterEditorProperties.icons;
      //      var iconsLength = icons.length;
      //      icons[iconsLength] = {
      //        showDown: true,
      //        showDownIcon: true,
      //        showFocused: true,
      //        showOver: true,
      //        src: '[SKINIMG]../../org.openbravo.client.application/images/form/filterClear.png',
      //        // note unsupportedfeature:
      //        // http://forums.smartclient.com/showthread.php?p=34868
      //        width: 15,
      //        height: 15,
      //        hspace: 0,
      //        click: clickFunction
      //      };
    }
  },
  
  hide: function(){
    this.Super('hide', arguments);
    this.selector.focusInItem();
  },
  
  show: function(){
    // draw now already otherwise the filter does not work the
    // first time    
    var ret = this.Super('show', arguments);
    this.selectorGrid.setFilterEditorCriteria(this.defaultFilter);
    this.selectorGrid.filterByEditor();
    this.selectorGrid.focusInFilterEditor();
    
    if (this.selector.getValue()) {
      this.selectorGrid.selectSingleRecord(this.selectorGrid.data.find(this.valueField, this.selector.getValue()));
    } else {
      this.selectorGrid.selectSingleRecord(null);
    }
    
    return ret;
  },
  
  open: function(){
    var selectorWindow = this, data = {
      '_selectorDefinitionId': this.selectorDefinitionId
    };
    
    this.selector.form.view.getContextInfo(data, {});
    
    var callback = function(resp, data, req){
      selectorWindow.fetchDefaultsCallback(resp, data, req);
    };
    OB.RemoteCallManager.call('org.openbravo.userinterface.selector.SelectorDefaultFilterActionHandler', data, data, callback);
  },
  
  fetchDefaultsCallback: function(rpcResponse, data, rpcRequest){
    var defaultFilter = {};
    if (data) {
      defaultFilter = {}; // Reset filter
      isc.addProperties(defaultFilter, data);
    }
    
    if (this.selector.defaultPopupFilterField && this.selector.getDisplayValue() && this.selector.getDisplayValue() !== '&nbsp;') {
      // Prevents overriding a default
      // value with empty
      defaultFilter[this.selector.defaultPopupFilterField] = this.selector.getDisplayValue();
    }
    
    // adds the selector id to filter used to get filter information
    defaultFilter._selectorDefinitionId = this.selector.selectorDefinitionId;
    this.defaultFilter = defaultFilter;
    this.show();
  },
  
  setValueInField: function(){
    var i, fld, record = this.selectorGrid.getSelectedRecord(), form = this.selector.form, outFields = this.selector.outFields;
    if (!record) {
      if (!selected) {
        for (i in outFields) {
          if (outFields.hasOwnProperty(i)) {
            if (!outFields[i]) {
              // skip id and _identifier and other columns without
              // associated tab field
              continue;
            }
            fld = form.getFieldFromInpColumnName(outFields[i]);
            if (fld) {
              fld.clearValue();
            } else {
              form.hiddenInputs[outFields[i]] = null;
            }
          }
        }
        return;
      }
    } else {
    
      for (i in outFields) {
        if (outFields.hasOwnProperty(i)) {
          if (!outFields[i]) {
            // skip id and _identifier and other columns without
            // associated tab field
            continue;
          }
          fld = form.getFieldFromInpColumnName(outFields[i]);
          if (fld) {
            fld.setValue(record[i]);
          } else {
            form.hiddenInputs[outFields[i]] = record[i];
          }
        }
      }
    }
    this.selector.setValueFromGrid(record);
    this.hide();
  }
});

// = Selector Item =
// Contains the OBSelector Item. This widget consists of two main parts:
// 1) a combo box with a picker icon
// 2) a popup window showing a search grid with data
//
isc.ClassFactory.defineClass('OBSelectorItem', ComboBoxItem);

isc.ClassFactory.mixInInterface('OBSelectorItem', 'OBLinkTitleItem');

isc.OBSelectorItem.addProperties({
  popupTextMatchStyle: 'startswith',
  suggestionTextMatchStyle: 'startswith',
  
  // ** {{{ selectorGridFields }}} **
  // the definition of the columns in the popup window
  selectorGridFields: [{
    title: OB.I18N.getLabel('OBUISC_Identifier'),
    name: OB.Constants.IDENTIFIER
  }],
  
  selectOnFocus: true,
  autoFetchData: false,
  showPickerIcon: true,
  validateOnChange: true,
  completeOnTab: true,
  
  pickListProperties: {
    fetchDelay: 400,
    showHeaderContextMenu: false
  },
  
  valueMap: {},
  
  init: function(){
    this.icons = [{
      src: this.popupIconSrc,
      width: this.popupIconWidth,
      height: this.popupIconHeight,
      keyPress: function(keyName, character, form, item, icon){
        if (keyName === 'Enter' && isc.EventHandler.ctrlKeyDown()) {
          item.openSelectorWindow();
          return false;
        }
        return true;
      },
      click: function(form, item, icon){
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
    
    if (this.showSelectorGrid) {
      this.selectorWindow = isc.OBSelectorPopupWindow.create({
        title: this.title,
        dataSource: this.optionDataSource,
        selector: this,
        valueField: this.valueField,
        displayField: this.displayField,
        selectorGridFields: isc.shallowClone(this.selectorGridFields)
      });
    }
    
    return this.Super('init', arguments);
  },
  
  setValueFromGrid: function(record){
    if (!record) {
      this.clearValue();
    } else {
      this.setValue(record[this.valueField]);
    }
    this._doFICCall = true;
    this.form.handleItemChange(this);
  },
  
  getTitleHTML: function(){
    // calls the method from the OBLinkTitleItem interface
    return this.getLinkTitleHTML();
  },
  
  changed: function(){
    var ret = this.Super('changed', arguments);
    if (this.form && this.form.handleItemChange) {
      this.form.handleItemChange(this);
    }
    return ret;
  },
  
  openSelectorWindow: function(){
    this.selectorWindow.open();
  },
  
  keyPress: function(item, form, keyName, characterValue){
    if (keyName === 'Enter' && isc.EventHandler.ctrlKeyDown()) {
      this.openSelectorWindow(form, item, null);
      return false;
    }
    return true;
  },
  
  getPickListFilterCriteria: function(){
    var criteria = this.Super('getPickListFilterCriteria'), defValue, prop;
    
    if (!criteria) {
      criteria = {};
    }
    
    // also add the special ORG parameter
    if (this.form.getField('organization')) {
      criteria[OB.Constants.ORG_PARAMETER] = this.form.getValue('organization');
    }
    
    this.form.view.getContextInfo(criteria, {});
    
    // adds the selector id to filter used to get filter information
    criteria._selectorDefinitionId = this.selectorDefinitionId;
    
    // only filter if the display field is also
    // passed
    // the displayField filter is not passed when
    // the user clicks the
    // drop-down button
    if (criteria[this.displayField]) {
      for (var i = 0; i < this.extraSearchFields.length; i++) {
        if (!criteria[this.extraSearchFields[i]]) {
          criteria[this.extraSearchFields[i]] = this.getDisplayValue();
        }
      }
      
      // for the suggestion box it is one big or
      criteria[OB.Constants.OR_EXPRESSION] = 'true';
    }
    
    // the additional where clause
    criteria[OB.Constants.WHERE_PARAMETER] = this.whereClause;
    
    // and sort according to the display field
    // initially
    criteria[OB.Constants.SORTBY_PARAMETER] = this.displayField;
    
    return criteria;
  }
});

isc.ClassFactory.defineClass('OBSelectorLinkItem', StaticTextItem);

isc.ClassFactory.mixInInterface('OBSelectorLinkItem', 'OBLinkTitleItem');

isc.OBSelectorLinkItem.addProperties({
  
  setValue: function(value){
    var ret = this.Super('setValue', arguments);
    // in this case the clearIcon needs to be shown or hidden
    if (!this.disabled && !this.required) {
      if (value) {
        this.showIcon(this.instanceClearIcon);
      } else {
        this.hideIcon(this.instanceClearIcon);
      }
    }
    return ret;
  },
  
  showPicker: function(){
    this.selectorWindow.open();
  },
  
  setValueFromGrid: function(record){
    if (!record) {
      this.clearValue();
      this.form.clearValue(this.displayField);
    } else {
      // use a special valuemap to store the value
      if (!this.valueMap) {
        this.valueMap = {};
      }
      this.setValue(record[this.gridValueField]);
      this.valueMap[this.getValue()] = record[this.gridDisplayField];
      this.form.setValue(this.displayField, record[this.gridDisplayField]);
      this.updateValueMap(true);
    }
    this._doFICCall = true;
    this.form.handleItemChange(this);
  },
  
  init: function(){
    if (this.disabled) {
      this.showPickerIcon = false;
    }
    
    this.instanceClearIcon = isc.shallowClone(this.clearIcon);
    this.instanceClearIcon.showIf= function(form, item){
      if (item.disabled) {
        return false;
      }
      if (item.required) {
        return false;
      }
      if (item.getValue()) {
        return true;
      }
      return false;
    };
    
    this.instanceClearIcon.click = function(){
      this.formItem.clearValue();
    };

    this.icons = [this.instanceClearIcon];
    this.icons[0].formItem = this;
    
    if (this.disabled) {
      // TODO: disable, remove icons
      this.icons = null;
    }
    
    this.selectorWindow = isc.OBSelectorPopupWindow.create({
      title: this.title,
      dataSource: this.dataSource,
      selector: this,
      valueField: this.gridValueField,
      displayField: this.gridDisplayField,
      selectorGridFields: isc.shallowClone(this.selectorGridFields)
    });
    
    return this.Super('init', arguments);
  },
  
  getTitleHTML: function(){
    // calls the method from the OBLinkTitleItem interface
    return this.getLinkTitleHTML();
  },
  
  changed: function(){
    var ret = this.Super('changed', arguments);
    if (this.form && this.form.handleItemChange) {
      this.form.handleItemChange(this);
    }
    return ret;
  }
});
