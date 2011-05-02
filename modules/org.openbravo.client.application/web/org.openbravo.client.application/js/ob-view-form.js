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
 * All portions are Copyright (C) 2010-2011 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

isc.ClassFactory.defineClass('OBViewForm', isc.DynamicForm);

// = OBViewForm =
// The OBViewForm is the Openbravo specific subclass of the Smartclient
// DynamicForm. The properties of the view form are stored in a separate object
// as they are re-used to create the editor in the grid. The properties are added to the viewform at the bottom
// of this file.

OB.ViewFormProperties = {

  // ** {{{ view }}} **
  // The view member contains the pointer to the composite canvas which
  // handles this form
  // and the grid and other related components.
  view: null,
  auxInputs: {},
  hiddenInputs: {},
  sessionAttributes: {},
  dynamicCols: [],
  width: '100%',
  height: '100%',
    
  showErrorIcons: false,
  showErrorStyle: true,
  selectOnFocus: false,
  autoComplete: true,
  redrawOnDisable: true,
  
  // ** {{ Layout Settings }} **
  numCols: 4,
  colWidths: ['24%', '24%', '24%', '24%'],
  
  titleOrientation: 'top',
  titleSuffix: '</b>',
  titlePrefix: '<b>',
  requiredTitleSuffix: ' *</b>',
  requiredRightTitlePrefix: '<b>* ',
  rightTitlePrefix: '<b>',
  rightTitleSuffix: '</b>',
  
  fieldsByInpColumnName: null,
  fieldsByColumnName: null,
  
  isNew: false,
  hasChanged: false,
  
  // is false for forms used in grid editing
  // true for the main form
  isViewForm: false,

  // Name to the first focused field defined in AD
  firstFocusedField: null,

  // Name of the fields shown in status bar
  statusBarFields: [],

  // Last returned array of function getStatusBarFields
  statusBarFieldsJSArray: [],

  // is set in the OBNoteSectionItem.initWidget
  noteSection: null,
  
  // is set in the OBLinkedItemSectionItem.initWidget
  linkedItemSection: null,

  initWidget: function() {
    this._preventFocusChanges = true;
    var length, i, item;
    // add the obFormProperties to ourselves, the obFormProperties
    // are re-used for inline grid editing
    isc.addProperties(this, this.obFormProperties);

    this.Super('initWidget', arguments);

    length = this.getItems().length;

    for(i = 0; i < length; i++) {
      item = this.getItem(i);
      if(item && item.firstFocusedField) {
        this.firstFocusedField = item.name;
        break;
      }
    }
    delete this._preventFocusChanges;
  },

  getStatusBarFields: function() {
    var statusBarFields = [[],[]], i, item, value;
    for(i = 0; i < this.statusBarFields.length; i++) {
      item = this.getItem(this.statusBarFields[i]);
      value = item.getDisplayValue();
      if(item && value !== null && value !== '') {

        if(value === item.getTitle() && typeof item.getValue() === 'boolean') { // Checkbox items return the title as display value
          if (item.getValue()) {
            value = OB.I18N.getLabel('OBUIAPP_Yes');
          } else {
            value = OB.I18N.getLabel('OBUIAPP_No');
          }
        }

        statusBarFields[0].push(item.getTitle());
        statusBarFields[1].push(value);
      }
    }
    this.statusBarFieldsJSArray = statusBarFields;
    return statusBarFields;
  },
  
  setHasChanged: function(value) {
    this.hasChanged = value;
    this.view.updateTabTitle();
    if (value && !this.isNew && this.view.statusBar.mode !== 'EDIT') {
      this.view.statusBar.mode = "EDIT";
      this.view.statusBar.setContentLabel(this.view.statusBar.newIcon, 'OBUIAPP_Editing', this.statusBarFieldsJSArray);
    }
    
    if (value) {
      // signal that autosave is needed after this
      this.view.standardWindow.setDirtyEditForm(this);
      this.validateAfterFicReturn = true;
    } else {
      // signal that no autosave is needed after this
      this.view.standardWindow.setDirtyEditForm(null);
    }
  },
  
  editRecord: function(record, preventFocus, hasChanges, focusFieldName){
    var ret = this.Super('editRecord', arguments);
    this.doEditRecordActions(preventFocus, record._new);
    if (hasChanges) {
      this.setHasChanged(true);
    }
    
    this.view.setTargetRecordInWindow(record.id);
    
    // used when clicking on a cell in a grid
    if (!preventFocus && focusFieldName) {
      this.forceFocusedField = focusFieldName;
    }
    
    return ret;
  },
  
  doEditRecordActions: function(preventFocus, isNew){
    delete this.validateAfterFicReturn;
    
    // sometimes if an error occured we stay disabled
    // prevent this
    this.setDisabled(false);
    
    this.setHasChanged(false);

    this.setNewState(isNew);
    
    // focus is done automatically, prevent the focus event if needed
    // the focus event will set the active view
    this.clearErrors();
    if (!isNew) {
      this.validateAfterFicReturn = true;
    }
    

    this.ignoreFirstFocusEvent = preventFocus;
    this.retrieveInitialValues(isNew);
    
    if (isNew) {
      this.view.statusBar.mode = 'NEW';
      this.view.statusBar.setContentLabel(this.view.statusBar.newIcon, 'OBUIAPP_New');
    }
  },
  
  editNewRecord: function(preventFocus){
    var ret = this.Super('editNewRecord', arguments);
    this.doEditRecordActions(preventFocus, true);
    return ret;
  },
  
  // set parent display info in the record
  setParentDisplayInfo: function() {    
    if (this.view.parentProperty) {
      var parentRecord = this.view.getParentRecord();
      if (parentRecord) {
        this.setValue(this.view.parentProperty, parentRecord.id);
        this.setValue(this.view.parentProperty + '.' + OB.Constants.IDENTIFIER, parentRecord[OB.Constants.IDENTIFIER]);
        if (this.getField(this.view.parentProperty) && !this.getField(this.view.parentProperty).valueMap) {
          var valueMap = {};
          this.getField(this.view.parentProperty).valueMap = valueMap;
          valueMap[parentRecord.id] = parentRecord[OB.Constants.IDENTIFIER];
        }
      }
    }
  },
  

  enableNoteSection: function(enable){
	    if (!this.noteSection) {
	      return;
	    }
	    if (enable) {
	      this.noteSection.setRecordInfo(this.view.entity, this.getValue(OB.Constants.ID));
	      this.noteSection.collapseSection();
	      this.noteSection.refresh();
	      this.noteSection.show();
	    } else {
	      this.noteSection.hide();
	    }
  },
  
  enableLinkedItemSection: function(enable){
    if (!this.linkedItemSection) {
      return;
    }
    if (enable) {
      this.linkedItemSection.collapseSection();
      this.linkedItemSection.setRecordInfo(this.view.entity, this.getValue(OB.Constants.ID));
      this.linkedItemSection.show();
    } else {
      this.linkedItemSection.hide();
    }
  },
  
  // add the undo buttons to the clickmask so that no save happens when 
  // clicking undo
  showClickMask : function(clickAction, mode, unmaskedTargets) {
    if (!isc.isA.Array(unmaskedTargets)) {
      if (!unmaskedTargets) {
        unmaskedTargets = [];
      } else {
        unmaskedTargets = [unmaskedTargets];
      }
    } 
    // the main undo button
    unmaskedTargets.push(this.view.toolBar.getLeftMember('undo'));
    
    // the row cancel button
    var editRow = this.view.viewGrid.getEditRow();
    if (editRow || editRow === 0) {
      var record = this.view.viewGrid.getRecord(editRow);
      if (record && record.editColumnLayout) {
        unmaskedTargets.push(record.editColumnLayout.cancelButton);
      }
    }
    this.Super('showClickMask', [clickAction, mode, unmaskedTargets]);
  },  
  
  setNewState: function(isNew){
    this.isNew = isNew;
    this.view.statusBar.setNewState(isNew);
    this.view.updateTabTitle();
    this.enableNoteSection(!isNew);
    this.enableLinkedItemSection(!isNew);

    if (isNew) {
      this.view.statusBar.newIcon.prompt = OB.I18N.getLabel('OBUIAPP_NewIconPrompt');
    } else {
      this.view.statusBar.newIcon.prompt = OB.I18N.getLabel('OBUIAPP_EditIconPrompt');
    }
    
    // see issue:
    // 16064: Autosave error is triggered when closing a tab, even if the form wasn't touched
    // https://issues.openbravo.com/view.php?id=16064
    // this is inline with current behavior
    // NOTE: changed to reset the edit form when closing the form, so only there
    // so autosave always works except when closing if nothing has changed
    if (isNew) {
      // signal that autosave is needed after this
      this.view.standardWindow.setDirtyEditForm(this);
    }
  },
  
  resetFocusItem: function() {
    var items = this.getItems(), length = items.length, item;
    
    var errorFld = this.getFirstErrorItem();
    if (errorFld) {
      this.setFocusInItem(errorFld, true);
      return;
    }
    
    if (this.forceFocusedField) {
      item = this.getItem(this.forceFocusedField);
      if(item && item.getCanFocus()) {
        this.setFocusInItem(item, true);
        delete this.forceFocusedField;
        return;
      }
      delete this.forceFocusedField;
    }

    if(this.firstFocusedField) {
      item = this.getItem(this.firstFocusedField);
      if(item && item.getCanFocus()) {
        this.setFocusInItem(item);
        return;
      }
    }

    // is set for inline grid editing for example
    if (this.getFocusItem() && this.getFocusItem().getCanFocus()) {
      if (this.view.isActiveView()) {
        this.getFocusItem().focusInItem();
      }
      this.view.lastFocusedItem = this.getFocusItem();
      return;
    }

    if (items) {
      for (var i = 0; i < length; i++) {
        item = items[i];
        if (item.getCanFocus() && !item.isDisabled()) {
          this.setFocusInItem(item);
          return;
        }
      }
    }
  },
  
  setFocusInItem: function(item, doFocus) {
    this.setFocusItem(item);
    this.view.lastFocusedItem = item;
    if (doFocus && this.view.isActiveView()) {
      item.focusInItem();
    }
  },
  
  setFindNewFocusItem: function() {
    var focusItem = this.getFocusItem(), item, items = this.getItems(),
        length = items.length;
    
    // used when double clicking a specific cell in a record
    if (this.forceFocusedField) {
      item = this.getItem(this.forceFocusedField);
      if(item && item.getCanFocus()) {
        this.setFocusInItem(item, true);
        delete this.forceFocusedField;
        return;
      }
      delete this.forceFocusedField;
    }
    
    if(this.firstFocusedField) {
      item = this.getItem(this.firstFocusedField);
      if(item && item.getCanFocus()) {
        this.setFocusInItem(item, true);
        return;
      }
    }

    // no need to find a new item
    if (focusItem && !focusItem.isDisabled() && focusItem.getCanFocus()) {
      return;
    }

    if (items) {
      for (var i = 0; i < length; i++) {
        item = items[i];
        if (item.getCanFocus() && !item.isDisabled()) {
          this.setFocusInItem(item, true);
          return;
        }
      }
    }    
  },
  
  getFieldFromInpColumnName: function(inpColumnName){
    if (!this.fieldsByInpColumnName) {
      var localResult = [], fields = this.getFields();
      for (var i = 0; i < fields.length; i++) {
        if (fields[i].inpColumnName) {
          localResult[fields[i].inpColumnName.toLowerCase()] = fields[i];
        }
      }
      this.fieldsByInpColumnName = localResult;
    }
    return this.fieldsByInpColumnName[inpColumnName.toLowerCase()];
  },
  
  getFieldFromColumnName: function(columnName){
    if (!this.fieldsByColumnName) {
      var localResult = [], fields = this.getFields();
      for (var i = 0; i < fields.length; i++) {
        if (fields[i].columnName) {
          localResult[fields[i].columnName.toLowerCase()] = fields[i];
        }
      }
      this.fieldsByColumnName = localResult;
    }
    return this.fieldsByColumnName[columnName.toLowerCase()];
  },
  
  setFields: function(){
    // is used in various places, prevent focus and scroll events
    this._preventFocusChanges = true;
    this.Super('setFields', arguments);
    delete this._preventFocusChanges;
    this.fieldsByInpColumnName = null;
    this.fieldsByColumnName = null;
  },
  
  retrieveInitialValues: function(isNew){
    this.setParentDisplayInfo();
    
    var parentId = this.view.getParentId(), requestParams, parentColumn, me = this, mode;
    // note also in this case initial vvalues are passed in as in case of grid
    // editing the unsaved/error values from a previous edit session are maintained
    var allProperties = this.view.getContextInfo(false, true, false, true);
    
    if (isNew) {
      mode = 'NEW';
    } else {
      mode = 'EDIT';
    }
    
    requestParams = {
      MODE: mode,
      PARENT_ID: parentId,
      TAB_ID: this.view.tabId,
      ROW_ID: this.getValue(OB.Constants.ID)
    };
    if (parentId && isNew && this.view.parentProperty) {
      parentColumn = this.view.getPropertyDefinition(this.view.parentProperty).inpColumn;
      requestParams[parentColumn] = parentId;
    }
    allProperties._entityName = this.view.entity;
    
    this.setDisabled(true);

    // note that only the fields with errors are validated anyway
    this.validateAfterFicReturn = true;

    // get the editRow before doing the call
    var editRow = me.view.viewGrid.getEditRow();
    OB.RemoteCallManager.call('org.openbravo.client.application.window.FormInitializationComponent', allProperties, requestParams, function(response, data, request){
      var editValues;
      if (editRow || editRow === 0) {
        editValues = me.view.viewGrid.getEditValues(editRow);
      }
      me.processFICReturn(response, data, request, editValues, editRow);
      if (!this.grid || this.grid.getEditRow() !== editRow) {
        // remember the initial values, if we are still editing the same row
        me.rememberValues();
      }
      // do some focus stuff
      me.resetFocusItem();
      if (me.ignoreFirstFocusEvent) {
        delete this.ignoreFirstFocusEvent;
        return;
      }
      if (me.getFocusItem() && me.view.isActiveView()) {
        me.getFocusItem().focusInItem();
      }
    });
  },
  
  processFICReturn: function(response, data, request, editValues, editRow){
    var modeIsNew = request.params.MODE === 'NEW';

    // needs to be recomputed as for grid editing the fields
    // are reset for every edit session
    this.fieldsByColumnName = null;
    
    // TODO: an error occured, handles this much better...
    if (!data || !data.columnValues) {
      this.setDisabled(false);
      this.validate();
      delete this.inFicCall;
      return;
    }
    
    var columnValues = data.columnValues, calloutMessages = data.calloutMessages,
                       auxInputs = data.auxiliaryInputValues, prop, value, i,
                       dynamicCols = data.dynamicCols,
                       sessionAttributes = data.sessionAttributes;

    // edit row has changed when returning, don't update the form anymore
    if (this.grid && this.grid.getEditRow() !== editRow) {
      if (columnValues) {
        for (prop in columnValues) {
          if (columnValues.hasOwnProperty(prop)) {
            this.setColumnValuesInEditValues(prop, columnValues[prop], editValues);
          }
        }
      }
      if (editValues && editValues.actionAfterFicReturn) {
        OB.Utilities.callAction(editValues.actionAfterFicReturn);
        delete editValues.actionAfterFicReturn;
      }
      return;
    }

    if (columnValues) {
      for (prop in columnValues) {
        if (columnValues.hasOwnProperty(prop)) {
          this.processColumnValue(prop, columnValues[prop], editValues);
        }
      }
    }

    // apparently sometimes an empty string is returned
    if (calloutMessages && calloutMessages.length > 0 && calloutMessages[0] !== '') {
      // TODO: check as what type should call out messages be displayed
      this.view.messageBar.setMessage(isc.OBMessageBar.TYPE_INFO, null, calloutMessages[0]);
    }
    if (auxInputs) {
      this.auxInputs = {};
      for (prop in auxInputs) {
        if (auxInputs.hasOwnProperty(prop)) {
          value = typeof auxInputs[prop].value !== 'undefined' ? auxInputs[prop].value : '';
          this.setValue(prop, value);
          this.auxInputs[prop] = value;
        }
      }
    }

    if(sessionAttributes) {
      this.sessionAttributes = sessionAttributes;
    }

    if (dynamicCols) {
      this.dynamicCols = dynamicCols;
    }
    if (data._readOnly || this.view.readOnly) {
      this.readOnly = true;
    } else {
      this.readOnly = false;
    }

    // grid editing    
    if (this.grid && this.grid.setEditValues && this.grid.getEditRow() === editRow) {
      // keep it as it is overwritten by the setEditValues
      var tmpActionAfterFic = null;
      if (editValues && editValues.actionAfterFicReturn) {
        tmpActionAfterFic = editValues.actionAfterFicReturn;
      }
      this.grid.setEditValues(this.grid.getEditRow(), this.getValues(), true);
      this.grid.storeUpdatedEditorValue(true);
      if (editValues) {
        editValues.actionAfterFicReturn = tmpActionAfterFic;
      }
    }

    this.setDisabled(false);

    if (this.validateAfterFicReturn) {
      delete this.validateAfterFicReturn;
      // only validate the fields which have errors
      for (i = 0; i < this.getFields().length; i++) {
        if (this.hasFieldErrors(this.getFields()[i].name)) {
          this.getFields()[i].validate();
        }
      }
    }

    this.markForRedraw();

    this.view.toolBar.updateButtonState(true);
    if (request.params.MODE === 'EDIT') {
      this.view.statusBar.mode = 'VIEW';
      this.view.statusBar.setContentLabel(null, null, this.getStatusBarFields());
    }

    // note onFieldChanged uses the form.readOnly set above
    this.onFieldChanged(this);

    delete this.inFicCall;
    if (this.callSaveAfterFICReturn) {
      delete this.callSaveAfterFICReturn;
      this.saveRow(true);
    }
    if (editValues && editValues.actionAfterFicReturn) {
      OB.Utilities.callAction(editValues.actionAfterFicReturn);
      delete editValues.actionAfterFicReturn;
    }

    if(data.jscode) {
      for(i = 0; i < data.jscode.length; i++) {
        eval(data.jscode[i]);
      }
    }
  },
  
  setDisabled: function(state) {
    var previousAllItemsDisabled = this.allItemsDisabled;
    this.allItemsDisabled = state;

    if (previousAllItemsDisabled !== this.allItemsDisabled) {
      if (this.getFocusItem()) {
        if (this.allItemsDisabled) {
          this.getFocusItem().blurItem();
          this.redraw();
          this.view.viewGrid.refreshEditRow();
        } else {
          this.redraw();
          this.view.viewGrid.refreshEditRow();
          // reset the canfocus
          for (var i = 0; i < this.getFields().length; i++) {
            delete this.getFields()[i].canFocus;
          }
          if (this.view.isActiveView()) {
            this.getFocusItem().focusInItem();
          }
        }
      } else {
        this.redraw();
        this.view.viewGrid.refreshEditRow();
      }
    }
  },
  
  refresh: function() {
    var criteria = {
        id: this.getValue(OB.Constants.ID)
    };
    this.fetchData(criteria);
  },
  
  processColumnValue: function(columnName, columnValue, editValues){
    var isDate, i, valueMap = {}, field = this.getFieldFromColumnName(columnName), entries = columnValue.entries;
    // not a field on the form, probably a datasource field
    var prop = this.view.getPropertyFromDBColumnName(columnName);
    var id, identifier;
    if (!field) {
      if (!prop) {
        return;
      }
      field = this.getDataSource().getField(prop);
      if (!field) {
        return;
      }
    }
    
    // ignore the id
    if (prop === OB.Constants.ID) {
      return;
    }
    
    // note field can be a datasource field, see above, in that case
    // don't set the entries    
    if (field.form && entries) {
      var required = field.required;
      for (i = 0; i < entries.length; i++) {
        id = entries[i][OB.Constants.ID] || '';
        identifier = entries[i][OB.Constants.IDENTIFIER] || '';
        valueMap[id] = identifier;
      }
      field.setValueMap(valueMap);
    }
    
    if (editValues && field.valueMap) {
      // store the valuemap in the edit values so it can be retrieved later
      // when the form is rebuild
      editValues[prop + '._valueMap'] = field.valueMap;
    }
    
    if (columnValue.value && (columnValue.value === 'null' || columnValue.value === '')) {
      // handle the case that the FIC returns a null value as a string
      // should be repaired in the FIC
      // note: do not use clearvalue as this removes the value from the form
      this.setValue(field.name, null);
    } else if (columnValue.value || columnValue.value === 0 || columnValue.value === false) {
      isDate = field.type &&
      (isc.SimpleType.getType(field.type).inheritsFrom === 'date' ||
      isc.SimpleType.getType(field.type).inheritsFrom === 'datetime');
      if (isDate) {
        this.setValue(field.name, isc.Date.parseSchemaDate(columnValue.value));
      } else if(columnValue.hasDateDefault){
        this.setValue(field.name, columnValue.classicValue);
      } else {
        
        // set the identifier/display field if the identifier is passed also
        // note that when the field value is changed by the user the setting 
        // of the identifier field is done in the form item
        identifier = columnValue.identifier;
        if (!identifier && field.valueMap) {
          identifier = field.valueMap[columnValue.value];
        }
        if (identifier) {
          if (!field.valueMap) {
            field.valueMap = {};
          }
          field.valueMap[columnValue.value] = identifier;
          if (field.form) {
            // only set the display field name if the field does not have its own
            // datasource and the field displayfield contains a dot, otherwise 
            // it is a direct field
            if (field.displayField && field.displayField.contains('.') && !this.getField(field.displayField) && !field.optionDataSource && !field.getDataSource()) {
              field.form.setValue(field.displayField, identifier);
            } else if (!field.displayField) {
              field.form.setValue(field.name + '.' + OB.Constants.IDENTIFIER, identifier);
            }
          }
        }
        
        this.setValue(field.name, columnValue.value);
      }
    } else {
      // note: do not use clearvalue as this removes the value from the form
      // which results it to not be sent to the server anymore
      this.setValue(field.name, null);
    }
    
    // store the textualvalue so that it is correctly send back to the server
    if (field) {
      var typeInstance = SimpleType.getType(field.type);
      if (columnValue.classicValue && typeInstance.decSeparator) {
        this.setTextualValue(field.name, columnValue.classicValue, typeInstance);
      }
    }
  },
  
  setColumnValuesInEditValues: function(columnName, columnValue, editValues){
    // no editvalues even anymore, go away
    if (!editValues) {
      return;
    }
    var id, identifier, field = this.getFieldFromColumnName(columnName), i, valueMap = {}, 
      entries = columnValue.entries;
    var prop = this.view.getPropertyFromDBColumnName(columnName);
    
    // ignore the id
    if (prop === OB.Constants.ID) {
      return;
    }
    
    if (entries) {
      for (i = 0; i < entries.length; i++) {
        id = entries[i][OB.Constants.ID] || '';
        identifier = entries[i][OB.Constants.IDENTIFIER] || '';
        valueMap[id] = identifier;
      }
      editValues[prop + '._valueMap'] = valueMap;
    }
    
    if (columnValue.value && (columnValue.value === 'null' || columnValue.value === '')) {
      // handle the case that the FIC returns a null value as a string
      // should be repaired in the FIC
      // note: do not use clearvalue as this removes the value from the form
      editValues[prop] = null;
    } else if (columnValue.value || columnValue.value === 0 || columnValue.value === false) {
      isDate = field && field.type &&
        (isc.SimpleType.getType(field.type).inheritsFrom === 'date' ||
        isc.SimpleType.getType(field.type).inheritsFrom === 'datetime');
      if (isDate) {
        editValues[prop] = isc.Date.parseSchemaDate(columnValue.value);
      } else {
        
        // set the identifier/display field if the identifier is passed also
        // note that when the field value is changed by the user the setting 
        // of the identifier field is done in the form item
        identifier = columnValue.identifier;
        if (!identifier && valueMap) {
          identifier = valueMap[columnValue.value];
        }
        if (identifier) {
          editValues[prop + '.' + OB.Constants.IDENTIFIER] = identifier;                
        }
        editValues[prop] = columnValue.value;
      }
    } else {
      // note: do not use clearvalue as this removes the value from the form
      // which results it to not be sent to the server anymore
      editValues[prop] = null;
    }
    
    // store the textualvalue so that it is correctly send back to the server
    if (field) {
      var typeInstance = SimpleType.getType(field.type);
      if (columnValue.classicValue && typeInstance.decSeparator) {
        this.setTextualValue(field.name, columnValue.classicValue, typeInstance, editValues);
      }
    }
  },
  
  // note textValue is in user format using users decimal and group separator
  setTextualValue: function(fldName, textValue, type, editValues) {    
    if (!textValue || textValue.trim() === '') {
      textValue = '';
    } else {
      textValue = OB.Utilities.Number.OBMaskedToOBPlain(textValue, type.decSeparator, type.groupSeparator);
      textValue = textValue.replace(type.decSeparator, '.');
    }
    if (editValues) {
      editValues[fldName + '_textualValue'] = textValue;
    } else if (this.grid) {
      this.grid.getEditValues(this.grid.getEditRow())[fldName + '_textualValue'] = textValue;
    }
    this.setValue(fldName + '_textualValue', textValue);
  },

  // called explicitly onblur and when non-editable fields change
  handleItemChange: function(item){
  
    if (item._hasChanged) {
      this.itemChangeActions();
      
      var i;
      for (i = 0; i < this.dynamicCols.length; i++) {
        if (this.dynamicCols[i] === item.inpColumnName) {
          item._hasChanged = false;
          this.inFicCall= true;
          this.doChangeFICCall(item);
          return true;
        }
      }
    }
    item._hasChanged = false;
  },
  
  // note item can be null, is also called when the form is re-shown
  // to recompute combos
  doChangeFICCall: function(item){
    var parentId = null, me = this, requestParams, allProperties = this.view.getContextInfo(false, true, false, true);
    if (this.view.parentProperty) {
      parentId = this.view.getParentId();
    }
    this.setParentDisplayInfo();

    requestParams = {
      MODE: 'CHANGE',
      PARENT_ID: parentId,
      TAB_ID: this.view.tabId,
      ROW_ID: this.getValue(OB.Constants.ID)
    };
    if (item) {
      requestParams.CHANGED_COLUMN = item.inpColumnName;
    }
    allProperties._entityName = this.view.entity;

    // disable with a delay to allow the focus to be moved to a new field
    // before disabling
    // only do this if there is no popup currently
    if (!this.view.standardWindow.inAutoSaveConfirmation) {
      this.delayCall('setDisabled', [true], 10);
    }

    var editRow = this.view.viewGrid.getEditRow();
    
    OB.RemoteCallManager.call('org.openbravo.client.application.window.FormInitializationComponent', allProperties, requestParams, function(response, data, request){
      var editValues;
      if (editRow || editRow === 0) {
        editValues = me.view.viewGrid.getEditValues(editRow);
      }
      me.processFICReturn(response, data, request, editValues, editRow);
    });
  },
  
  itemChanged: function(item, newValue){
    this.itemChangeActions(item);
  },
  
  // these actions are done when the user types in a field
  // in contrast to other actions which are done at blur
  // see: handleItemChange
  itemChangeActions: function(){
    // special case, item change is called when the inline form is being hidden
    if (!this.view.isShowingForm && !this.view.isEditingGrid) {
      return;
    }
    
    // remove the message
    this.setHasChanged(true);
    this.view.messageBar.hide();
    this.view.toolBar.updateButtonState(true);
  },
  
  // make sure that any field errors also appear in the grid
  setFieldErrors: function(itemName, msg, display) {
    this.Super('setFieldErrors', arguments);
    if (this.grid && this.view.isEditingGrid) {
      this.grid.setFieldError(this.grid.getEditRow(), itemName, msg, !display);
    }
  },
  
  resetForm: function(){
    this.resetValues();
    this.clearErrors();
    this.setHasChanged(false);
  },
  
  undo: function(){
    this.view.messageBar.hide();
    this.resetValues();
    this.setHasChanged(false);
    this.view.statusBar.mode = 'VIEW';
    this.view.statusBar.setContentLabel(null, null, this.getStatusBarFields());
    this.view.toolBar.updateButtonState(true);
  },
  
  autoSave: function(){
    if (this.isViewForm) {
      this.saveRow();
    } else {
      // grid editing, forward to the grid
      this.view.viewGrid.autoSave();
    }
  },
  
  // always let the saveRow callback handle the error
  saveEditorReply : function (response, data, request) {
    return true;
  },
  
  // Note: saveRow is not called in case of grid editing
  // there the save call is done through the grid saveEditedValues
  // function
  saveRow: function(){
    // store the value of the current focus item
    if (this.getFocusItem()) {
      this.getFocusItem().updateValue();
      this.handleItemChange(this.getFocusItem());
    }
    
    var i, length, flds, form = this, ficCallDone;
    var record = form.view.viewGrid.getSelectedRecord(),
      // note record does not have to be set in case new and no
      // previously selected record
      recordIndex = (record ? form.view.viewGrid.getRecordIndex(record) : -1);
    
    form.isSaving = true;

    // remove the error message if any
    this.view.messageBar.hide();
    
    var callback = function(resp, data, req){
      var index1, index2, errorCode, view = form.view, localRecord;
      var status = resp.status;

      // if no recordIndex then select explicitly
      if (recordIndex === -1) {
        var id = form.getValue('id');
        record = view.viewGrid.data.find('id', id);
        recordIndex = view.viewGrid.data.indexOf(record);
      }
      
      // not in the filter, insert the record in the cachedata so it will be made visible
      if (status === isc.RPCResponse.STATUS_SUCCESS && recordIndex === -1) {
        var visibleRows = view.viewGrid.body.getVisibleRows();
        if (visibleRows[0] !== -1) {
          view.viewGrid.data.insertCacheData(data, visibleRows[0]);
          recordIndex = visibleRows[0];
        }
      }
      
      if (recordIndex || recordIndex === 0) {
        // if this is not done the selection gets lost
        localRecord = view.viewGrid.data.get(recordIndex);
        if (localRecord) {
          localRecord[view.viewGrid.selection.selectionProperty] = true;
        }

        // a new id has been computed use that now    
        if (localRecord && localRecord._newId) {
          localRecord.id = localRecord._newId;
          delete localRecord._newId;
        }
        
        view.viewGrid.scrollToRow(recordIndex);
      }
      
      if (status === isc.RPCResponse.STATUS_SUCCESS) {
        // do remember values here to prevent infinite autosave loop
        form.rememberValues();
        
        //view.messageBar.setMessage(isc.OBMessageBar.TYPE_SUCCESS, null, OB.I18N.getLabel('OBUIAPP_SaveSuccess'));
        this.view.statusBar.mode = 'SAVED';
        view.statusBar.setContentLabel(view.statusBar.checkedIcon, 'OBUIAPP_Saved', this.getStatusBarFields());
        
        view.setRecentDocument(this.getValues());
        
        if (localRecord && localRecord !== view.viewGrid.getSelectedRecord()) {
          localRecord[view.viewGrid.selection.selectionProperty] = false;
          view.viewGrid.doSelectSingleRecord(localRecord);
        }
        
        view.updateLastSelectedState();
                
        // remove any new pointer
        if (this.getValue('_new')) {
          this.clearValue('_new');
        }

        view.viewGrid.markForRedraw();
        
        if (form.isNew) {
          view.refreshChildViews();
        }

        // success invoke the action, if any there
        view.standardWindow.autoSaveDone(view, true);

        // do this after doing autoSave as the setHasChanged will clean
        // the autosave info
        form.setHasChanged(false);
        
        // remove any edit info in the grid
        view.viewGrid.discardEdits(recordIndex, null, false, isc.ListGrid.PROGRAMMATIC, true);
        
        // change some labels
        form.setNewState(false);
        
        view.refreshParentRecord();

      } else if (status === isc.RPCResponse.STATUS_VALIDATION_ERROR && resp.errors) {
        form.handleFieldErrors(resp.errors);
        view.standardWindow.autoSaveDone(view, false);
      } else {
        view.setErrorMessageFromResponse(resp, data, req);
        view.standardWindow.autoSaveDone(view, false);
      }

      form.isSaving = false;
      view.toolBar.updateButtonState(true);
      return false;
    };
    
    // note validate will also set the formfocus, this is 
    // done by calling showErrors without the third parameter to true
    if (!this.validate()) {
      this.handleFieldErrors(null);
      form.view.standardWindow.autoSaveDone(form.view, false);
      form.isSaving = false;
      form.view.toolBar.updateButtonState(true);
      return;
    }
    
    if (this.inFicCall) {
      this.callSaveAfterFICReturn = true;
    } else {
      // last parameter true prevents additional validation
      this.saveData(callback, {
        willHandleError: true,
        formSave: true
      }, true);
    }
  },
  
  focusInNextItem: function(currentItem) {
    var flds = (this.grid ? this.grid.getFields() : this.getFields);
    var chooseNextItem, i, nextItem, length = flds.length;
    for (i = 0; i < length; i++) {
      var item = this.getItem(flds[i].name);
      if (!item) {
        continue;
      }
      // some items don't have a name, ignore those
      // !item.disabled because sometimes the whole form is disabled and needs to 
      // be focused after enabling (after the fic call returns)
      if (chooseNextItem && item.name && item.isFocusable && (item.isFocusable() || !item.disabled)) {
        nextItem = item;
        break;
      }
      if (item.name === currentItem) {
        chooseNextItem = true;
      }
    }
    if (nextItem) {
      // in the fic call, all items are disabled, so let it be focused
      // when returning
      if (this.inFicCall) {
        this.setFocusItem(nextItem);
      } else {
        this.focusInItem(nextItem);
      }
    }
  },
  
  // overridden to prevent focus setting when autoSaving  
  showErrors: function(errors, hiddenErrors, suppressAutoFocus){
    if (this.view.standardWindow.isAutoSaving) {
      return this.Super('showErrors', [errors, hiddenErrors, true]);
    }
    return this.Super('showErrors', arguments);
  },
  
  handleFieldErrors: function(errors){
    var msg = OB.I18N.getLabel('OBUIAPP_ErrorInFields');
    var additionalMsg = '';
    if (errors) {
      this.setErrors(errors, true);
      for (var err in errors) {
        if (errors.hasOwnProperty(err)) {
          var fld = this.getField(err); 
          if (!fld || !fld.visible) {
            if (additionalMsg !== '') {
              additionalMsg = additionalMsg + '<br/>';
            }
            additionalMsg = additionalMsg + errors[err]; 
          }
        }
      }
      if (additionalMsg) {
        msg = additionalMsg;
      }
    }
    var errorFld = this.getFirstErrorItem();
    // special case
    // if there is only an error on the id and no error on any field
    // display that message then
    if (!additionalMsg && errors && errors.id && !errorFld && errors.id.errorMessage) {
      msg = errors.id.errorMessage;
    }
       
    // set the error message
    this.view.messageBar.setMessage(isc.OBMessageBar.TYPE_ERROR, null, msg);
    
    // and focus to the first error field
    this.setFocusInErrorField();
  },
  
  setFocusInErrorField: function(){
    var errorFld = this.getFirstErrorItem();
    if (errorFld) {
      if (this.view.standardWindow.isAutoSaving) {
        // otherwise the focus results in infinite cycles
        // with views getting activated all the time
        this.setFocusItem(errorFld);
      } else if (this.view.isActiveView()){
        errorFld.focusInItem();
      }
      return;
    }
  },
  
  getFirstErrorItem: function() {
    var flds = this.getFields(), errs = this.getErrors();
    if (flds.length) {
      var length = flds.length;
      for (i = 0; i < length; i++) {
        if (flds[i].getErrors() || errs[flds[i].name]) {
          return flds[i];
        }
      }
    }
    return null;
  },
  
  // overridden to show the error when hovering over items
  titleHoverHTML: function(item){
    var errs = item.getErrors();
    if (!errs) {
      return this.Super('titleHoverHTML', arguments);
    }
    return OB.Utilities.getPromptString(errs);
  },
  
  itemHoverHTML: function(item){
    var errs = item.getErrors();
    if (!errs) {
      return this.Super('itemHoverHTML', arguments);
    }
    return OB.Utilities.getPromptString(errs);
  },
  
  // overridden here to place the link icon after the mandatory sign
  getTitleHTML: function(item, error){
    var titleHTML = this.Super('getTitleHTML', arguments);
    
    if (item.showLinkIcon && item.targetEntity && OB.AccessibleEntities[item.targetEntity]) {
    
      // the parent property does not need a link, as it is shown in the 
      // parent view
      if (item.parentProperty) {
        return titleHTML;
      }
      
      var searchIconObj = {
        src: item.newTabIconSrc,
        height: item.newTabIconSize,
        width: item.newTabIconSize,
        align: 'absmiddle',
        extraStuff: ' id="' + item.ID + this.LINKBUTTONSUFFIX + '" class="OBFormFieldLinkButton" '
      };
      
      var imgHTML = isc.Canvas.imgHTML(searchIconObj);
      
      // handle a small issue in chrome/firefox that the user agents stylesheet
      // sets a default cursor on labels
      if (titleHTML.contains('LABEL')) {
        titleHTML = titleHTML.replace('<LABEL', '<LABEL style="cursor: pointer"');
      }
      
      return '<span class="OBFormFieldLinkButton">' + titleHTML + '</span>&nbsp;' + imgHTML;
    }
    
    return titleHTML;
  },
  
  // we are being reshown, get new values for the combos
  visibilityChanged: function(visible){
    if (visible && (this.view.isShowingForm || this.view.isEditingGrid)) {
      this.doChangeFICCall();
    }
  },

  onFieldChanged: function(form, item, value) {
    // To be implemented dynamically
  },
    
  // overridden to prevent updating of a time value which 
  // has only been edited half, only do this if we are in change
  // handling (to enable buttons etc.)
  updateFocusItemValue: function() {
    var focusItem = this.getFocusSubItem();
    if (this.inChangeHandling && focusItem && !focusItem.changeOnKeypress) {
      return;
    }
    return this.Super('updateFocusItemValue', arguments);
  } 

};

isc.OBViewForm.addProperties(OB.ViewFormProperties);
