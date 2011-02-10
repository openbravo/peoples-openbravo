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
  
  // is set in the OBLinkedItemSectionItem.initWidget
  linkedItemSection: null,

  setHasChanged: function(value) {
    this.hasChanged = value;
    this.view.updateTabTitle();
    if (value && !this.isNew) {
      this.view.statusBar.setStateLabel('OBUIAPP_Modified', this.view.statusBar.newIcon);
    }
    
    if (value) {
      // signal that autosave is needed after this
      this.view.standardWindow.setDirtyEditForm(this);
    } else {
      // signal that no autosave is needed after this
      this.view.standardWindow.setDirtyEditForm(null);
    }
  },
  
  editRecord: function(record, preventFocus){
    var ret = this.Super('editRecord', arguments);
    this.doEditRecordActions(preventFocus, false);
    
    if (this.view.parentProperty) {
      this.setValue(this.view.parentProperty, record[this.view.parentProperty]);
    }
    
    return ret;
  },
  
  doEditRecordActions: function(preventFocus, isNew){
    // sometimes if an error occured we stay disabled
    // prevent this
    this.setDisabled(false);
    
    this.setHasChanged(false);

    // the new state can also be signaled through the record
    // this is to edit new records which have been edited before
    this.setNewState(isNew || this.getValue('_new'));
    
    // focus is done automatically, prevent the focus event if needed
    // the focus event will set the active view
    this.ignoreFirstFocusEvent = preventFocus;
    this.clearErrors();
    if (!isNew) {
      this.validateAfterFicReturn = true;
    }
    
    this.view.toolBar.updateButtonState();

    this.retrieveInitialValues(isNew);
    
    this.view.messageBar.hide();
    if (isNew) {
      this.view.statusBar.setStateLabel('OBUIAPP_New', this.view.statusBar.newIcon);
    } else {
      this.view.statusBar.setStateLabel();
    }

    this.resetFocusItem();
    if (!preventFocus) {
      this.focus();
    }
  },
  
  editNewRecord: function(preventFocus){
    var ret = this.Super('editNewRecord', arguments);
    this.doEditRecordActions(preventFocus, true);
    return ret;
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
  
  setNewState: function(isNew){
    this.isNew = isNew;
    this.view.statusBar.setNewState(isNew);
    this.view.updateTabTitle();
    this.enableLinkedItemSection(!isNew);
  },
  
  // reset the focus item to the first item which can get focus
  resetFocusItem: function(){
    var items = this.getItems();
    if (items) {
      for (var i = 0; i < items.length; i++) {
        var item = items[i];
        if (item.getCanFocus() && !item.isDisabled()) {
          this.setFocusItem(item);
          this.view.lastFocusedItem = item;
          return;
        }
      }
    }
  },
  
  setFindNewFocusItem: function() {
    var focusItem = this.getFocusItem();
    // no need to find a new item
    if (focusItem && !focusItem.isDisabled() && focusItem.getCanFocus()) {
      return;
    }
    var items = this.getItems();
    if (items) {
      for (var i = 0; i < items.length; i++) {
        var item = items[i];
        if (item.getCanFocus() && !item.isDisabled()) {
          item.focusInItem();
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
    this.Super('setFields', arguments);
    this.fieldsByInpColumnName = null;
    this.fieldsByColumnName = null;
  },
  
  retrieveInitialValues: function(isNew){
    var parentId = this.view.getParentId(), requestParams, parentColumn, me = this, mode;
    // note also in this case initial vvalues are passed in as in case of grid
    // editing the unsaved/error values from a previous edit session are maintained
    var allProperties = this.view.getContextInfo(false, true, false, false);
    
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
    if (parentId && isNew) {
      parentColumn = this.view.getPropertyDefinition(this.view.parentProperty).inpColumn;
      requestParams[parentColumn] = parentId;
    }
    allProperties._entityName = this.view.entity;
    
    this.setDisabled(true);

    OB.RemoteCallManager.call('org.openbravo.client.application.window.FormInitializationComponent', allProperties, requestParams, function(response, data, request){
      var editValues, editRow = me.view.viewGrid.getEditRow();
      if (editRow || editRow === 0) {
        editValues = me.view.viewGrid.getEditValues(me.view.viewGrid.getEditRow());
      }
      me.processFICReturn(response, data, request, editValues);
      // remember the initial values 
      me.rememberValues();
    });
  },
  
  processFICReturn: function(response, data, request, editValues){
    var modeIsNew = request.params.MODE === 'NEW';

    // needs to be recomputed as for grid editing the fields
    // are reset for every edit session
    this.fieldsByColumnName = null;
    
    // TODO: an error occured, handles this much better...
    if (!data || !data.columnValues) {
      this.setDisabled(false);
      return;
    }
    
    var columnValues = data.columnValues, calloutMessages = data.calloutMessages,
                       auxInputs = data.auxiliaryInputValues, prop, value, i,
                       dynamicCols = data.dynamicCols,
                       sessionAttributes = data.sessionAttributes;
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
          this.setValue(prop, auxInputs[prop].value);
          this.auxInputs[prop] = auxInputs[prop].value;
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
    this.view.toolBar.updateButtonState();
    // note onFieldChanged uses the form.readOnly set above
    this.onFieldChanged(this);
    this.focus();
    
    // save out the values to the grid before redrawing
    if (this.grid && this.grid.setEditValues && (this.grid.getEditRow() || this.grid.getEditRow() === 0)) {
      this.grid.setEditValues(this.grid.getEditRow(), this.getValues(), true);
    }
    
    this.setDisabled(false);
    
    // store the new values in the edit value 
    if (this.grid) {
      this.grid.storeUpdatedEditorValue(true);
    }

    if (this.validateAfterFicReturn) {
      delete this.validateAfterFicReturn;
      this.validate();
    }

    this.markForRedraw();
  },
  
  setDisabled: function(state) {
    this.allItemsDisabled = state;
  },
  
  processColumnValue: function(columnName, columnValue, editValues){
    var undef, data, record, length, valuePresent, currentValue, isDate, value, i, valueMap = {}, field = this.getFieldFromColumnName(columnName), entries = columnValue.entries;
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
    if (field.name === OB.Constants.ID) {
      return;
    }
    
    // note field can be a datasource field, see above, in that case
    // don't set the entries    
    if (field.form && entries) {
      for (i = 0; i < entries.length; i++) {
        id = entries[i][OB.Constants.ID] || null;
        identifier = entries[i][OB.Constants.IDENTIFIER] || '';
        valueMap[id] = (identifier === 'null' ? null : identifier);
      }
      field.setValueMap(valueMap);
      if (editValues) {
        // store the valuemap in the edit values so it can be retrieved later
        // when the form is rebuild
        editValues[prop + '._valueMap'] = valueMap;
      }
    }
    
    if (columnValue.value && columnValue.value === 'null') {
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
      } else {
        
        // set the identifier/display field if the identifier is passed also
        // note that when the field value is changed by the user the setting 
        // of the identifier field is done in the form item
        identifier = columnValue.identifier;
        if (!identifier && field.valueMap) {
          identifier = field.valueMap[columnValue.value];
        }
        if (identifier) {
          if (entries) {
            if (!field.valueMap) {
              field.valueMap = {};
            }
            field.valueMap[columnValue.value] = identifier;
          }
          if (field.form) {
            if (field.displayField) {
              field.form.setValue(field.displayField, identifier);
            } else {
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
    
    if (field.pickList) {
      field.pickList.invalidateCache();
      field.pickList.deselectAllRecords();
      field.selectDefaultItem();
    }
  },
  
  // called explicitly onblur and when non-editable fields change
  handleItemChange: function(item){
  
    if (item._hasChanged) {
      this.itemChangeActions();
      
      var i;
      for (i = 0; i < this.dynamicCols.length; i++) {
        if (this.dynamicCols[i] === item.inpColumnName) {
          item._hasChanged = false;
          this.doChangeFICCall(item);
          return;
        }
      }
    }
    item._hasChanged = false;
  },
  
  // note item can be null, is also called when the form is re-shown
  // to recompute combos
  doChangeFICCall: function(item){
    var parentId = null, me = this, requestParams, allProperties = this.view.getContextInfo(false, true, false, false);
    if (this.view.parentProperty) {
      parentId = this.view.getParentId();
    }
    
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
    
    this.setDisabled(true);

    // collect the context information    
    OB.RemoteCallManager.call('org.openbravo.client.application.window.FormInitializationComponent', allProperties, requestParams, function(response, data, request){
      var editValues, editRow = me.view.viewGrid.getEditRow();
      if (editRow || editRow === 0) {
        editValues = me.view.viewGrid.getEditValues(me.view.viewGrid.getEditRow());
      }
      me.processFICReturn(response, data, request, editValues);
    });
  },
  
  focus: function(){
    if (this.ignoreFirstFocusEvent) {
      this.ignoreFirstFocusEvent = false;
      return;
    }
    // handle the case that there is a focusitem but the focus is not yet visible
    // this happens in defaultedit mode where the parent change forces
    // a default edit mode opening in a child, in that case the focus is not
    // set in the child but it should be set when the somewhere in the child a click 
    // is done
    if (this.getFocusItem()) {
      this.getFocusItem().focusInItem();
    }
    var ret = this.Super("focus", arguments);
    return ret;
  },
  
  itemChanged: function(item, newValue){
    this.itemChangeActions(item);
    this.handleItemChange(item);
  },
  
  // these actions are done when the user types in a field
  // in contrast to other actions which are done at blur
  // see: handleItemChange
  itemChangeActions: function(){
    // remove the message
    this.setHasChanged(true);
    this.view.messageBar.hide();
    this.view.toolBar.updateButtonState();
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
    this.view.statusBar.setStateLabel(null);
    this.view.toolBar.updateButtonState();
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
    var i, length, flds, form = this;
    var record = form.view.viewGrid.getSelectedRecord(), recordIndex = form.view.viewGrid.getRecordIndex(record);
    
    form.isSaving = true;

    // remove the error message if any
    this.view.messageBar.hide();

    // disable some buttons
    this.view.toolBar.updateButtonState();
    
    var callback = function(resp, data, req){
      var index1, index2, errorCode, view = form.view;
      var status = resp.status;

      // if this is not done the selection gets lost
      if (recordIndex || recordIndex === 0) {
        var localRecord = view.viewGrid.getRecord(recordIndex);
        if (localRecord) {
          localRecord[view.viewGrid.selection.selectionProperty] = true;
        }
      }
      
      if (status === isc.RPCResponse.STATUS_SUCCESS) {
        // do remember values here to prevent infinite autosave loop
        form.rememberValues();
        
        //view.messageBar.setMessage(isc.OBMessageBar.TYPE_SUCCESS, null, OB.I18N.getLabel('OBUIAPP_SaveSuccess'));
        view.statusBar.setStateLabel('OBUIAPP_Saved', view.statusBar.checkedIcon);
        
        // force a fetch to place the grid on the correct location
        if (form.isNew) {
          view.viewGrid.targetRecordId = data.id;
          view.viewGrid.refreshContents();
        }
        
        this.setNewState(false);

        // success invoke the action, if any there
        view.standardWindow.autoSaveDone(view, true);

        // do this after doing autoSave as the setHasChanged will clean
        // the autosave info
        this.setHasChanged(false);
      } else if (status === isc.RPCResponse.STATUS_VALIDATION_ERROR && resp.errors) {
        form.handleFieldErrors(resp.errors);
        view.standardWindow.autoSaveDone(view, false);
      } else {
        view.setErrorMessageFromResponse(resp, data, req);
        view.standardWindow.autoSaveDone(view, false);
      }

      form.isSaving = false;
      view.toolBar.updateButtonState();
      return false;
    };
    
    // note validate will also set the formfocus, this is 
    // done by calling showErrors without the third parameter to true
    if (!this.validate()) {
      this.handleFieldErrors(null);
      form.view.standardWindow.autoSaveDone(form.view, false);
      form.isSaving = false;
      form.view.toolBar.updateButtonState();
      return;
    }
    
    // last parameter true prevents additional validation
    this.saveData(callback, {
      willHandleError: true,
      formSave: true
    }, true);
  },
  
  // overridden to prevent focus setting when autoSaving
  
  showErrors: function(errors, hiddenErrors, suppressAutoFocus){
    if (this.view.standardWindow.isAutoSaving) {
      return this.Super('showErrors', [errors, hiddenErrors, true]);
    }
    return this.Super('showErrors', arguments);
  },
  
  handleFieldErrors: function(errors){
    if (errors) {
      this.setErrors(errors, true);
    }
    var msg = OB.I18N.getLabel('OBUIAPP_ErrorInFields');
    var errorFld = this.getFirstErrorItem();
    // special case
    // if there is only an error on the id and no error on any field
    // display that message then
    if (errors && errors.id && !errorFld) {
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
      } else {
        errorFld.focusInItem();
      }
      return;
    }
    this.resetFocusItem();
  },
  
  getFirstErrorItem: function() {
    var flds = this.getFields();
    if (flds.length) {
      var length = flds.length;
      for (i = 0; i < length; i++) {
        if (flds[i].getErrors()) {
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
        extraStuff: ' id="' + item.ID + this.LINKBUTTONSUFFIX + '" class="OBFormFieldLinkButton" onclick="window[\'' + item.ID + '\'].linkButtonClick();"'
      };
      
      var imgHTML = isc.Canvas.imgHTML(searchIconObj);
      
      return '<span class="OBFormFieldLinkButton" onclick="window[\'' + item.ID + '\'].linkButtonClick();">' + titleHTML + '</span>&nbsp;' + imgHTML;
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
  }
};

isc.OBViewForm.addProperties(OB.ViewFormProperties);
