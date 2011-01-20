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
// DynamicForm.

isc.OBViewForm.addProperties({

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
  
  showErrorIcons: false,
  showErrorStyle: true,
  selectOnFocus: true,
  autoComplete: true,
  
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
  
  // is set in the OBLinkedItemSectionItem.initWidget
  linkedItemSection: null,

  setHasChanged: function(value) {
    this.hasChanged = value;
    this.view.updateTabTitle();
  },
  
  editRecord: function(record, preventFocus){
    this.setHasChanged(false);

    this.setNewState(false);
    
    // focus is done automatically, prevent the focus event if needed
    // the focus event will set the active view
    this.ignoreFirstFocusEvent = preventFocus;
    
    var ret = this.Super('editRecord', arguments);
    this.clearErrors();
    
    this.view.setToolBarButtonState();
    
    this.retrieveInitialValues(false);
    
    this.view.messageBar.hide();

    this.resetFocusItem();
    if (!preventFocus) {
      this.focus();
    }
    return ret;
  },
  
  editNewRecord: function(preventFocus){
    // focus is done automatically, prevent the focus event if needed
    // the focus event will set the active view
    this.ignoreFirstFocusEvent = preventFocus;
    
    this.setHasChanged(false);

    this.setNewState(true);
    
    var ret = this.Super('editNewRecord', arguments);
    this.clearErrors();
    
    this.view.setToolBarButtonState();
    
    this.retrieveInitialValues(true);
    
    this.resetFocusItem();
    this.focus();
    
    this.view.messageBar.hide();
    return ret;
  },
  
  enableLinkedItemSection: function(enable){
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
        if (!item.isDisabled() && (item.getCanFocus() || item.canFocus)) {
          this.setFocusItem(item);
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
  
  getPropertyFromColumnName: function(columnName){
    var length = this.view.propertyToColumns.length;
    for (var i = 0; i < length; i++) {
      var propDef = this.view.propertyToColumns[i];
      if (propDef.dbColumn === columnName) {
        return propDef.property;
      }
    }
    return null;
  },
  
  setFields: function(){
    this.Super('setFields', arguments);
    this.fieldsByInpColumnName = null;
    this.fieldsByColumnName = null;
  },
  
  retrieveInitialValues: function(isNew){
    var parentId = this.view.getParentId(), requestParams, parentColumn, me = this, mode;
    
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
      parentColumn = this.getField(this.view.parentProperty).inpColumnName;
      requestParams[parentColumn] = parentId;
    }
    
    OB.RemoteCallManager.call('org.openbravo.client.application.window.FormInitializationComponent', {}, requestParams, function(response, data, request){
      me.processFICReturn(response, data, request);
      // remember the initial values 
      me.rememberValues();
    });
  },
  
  processFICReturn: function(response, data, request){
    // TODO: an error occured, handles this much better...
    if (!data) {
      return;
    }
    var columnValues = data.columnValues, calloutMessages = data.calloutMessages,
                       auxInputs = data.auxiliaryInputValues, prop, value,
                       dynamicCols = data.dynamicCols,
                       sessionAttributes = data.sessionAttributes;
    if (columnValues) {
      for (prop in columnValues) {
        if (columnValues.hasOwnProperty(prop)) {
          this.processColumnValue(prop, columnValues[prop]);
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
    if (!data.writable || this.view.readOnly) {
      this.readOnly = true;
    } else {
      this.readOnly = false;
    }
    this.view.setToolBarButtonState();
    this.view.toolBar.refreshCustomButtons();
    this.onFieldChanged(this);
    this.focus();
  },
  
  processColumnValue: function(columnName, columnValue){
    var data, record, length, valuePresent, currentValue, isDate, value, i, valueMap = {}, field = this.getFieldFromColumnName(columnName), entries = columnValue.entries;
    if (!field) {
      // not a field on the form, probably a datasource field
      var prop = this.getPropertyFromColumnName(columnName);
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
      if (field.getDataSource()) {
        field.getDataSource().setCacheData(entries, true);
      } else {
        for (i = 0; i < entries.length; i++) {
          valueMap[entries[i][OB.Constants.ID]] = entries[i][OB.Constants.IDENTIFIER];
        }
        field.setValueMap(valueMap);
      }
      
      // rereads the picklist      
      if (field.pickList) {
        field.pickList.invalidateCache();
        field.pickList.deselectAllRecords();
        field.selectItemFromValue(field.getValue());
      }
    }
    
    if (columnValue.value && columnValue.value === 'null') {
      // handle the case that the FIC returns a null value as a string
      // should be repaired in the FIC
      this.clearValue(field.name);
    } else if (columnValue.value || columnValue.value === 0 || columnValue.value === false) {
      isDate = field.type &&
      (isc.SimpleType.getType(field.type).inheritsFrom === 'date' ||
      isc.SimpleType.getType(field.type).inheritsFrom === 'datetime');
      if (isDate) {
        this.setValue(field.name, isc.Date.parseSchemaDate(columnValue.value));
      } else {
        this.setValue(field.name, columnValue.value);
      }
    } else {
      this.clearValue(field.name);
    }
    
    if (field.redraw) {
      field.redraw();
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
    var parentId = null, me = this, requestParams, allProperties = this.view.getContextInfo(false, true);
    
    if (this.view.parentProperty) {
      parentId = this.getValue(this.view.parentProperty);
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
    
    // collect the context information    
    OB.RemoteCallManager.call('org.openbravo.client.application.window.FormInitializationComponent', allProperties, requestParams, function(response, data, request){
      me.processFICReturn(response, data, request);
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
    var ret = this.Super('itemChanged', arguments);
    this.itemChangeActions();
    return ret;
  },
  
  itemChangeActions: function(){
    // remove the message
    this.setHasChanged(true);
    this.view.messageBar.hide();
    this.view.setToolBarButtonState();
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
    this.view.setToolBarButtonState();
  },
  
  // action defines the action to call when the save succeeds
  // forceDialogOnFailure: if true then even if the form is visible
  // still a dialog is shown, this becomes sometimes autosave is done
  // before actually the form gets hidden
  autoSave: function(action, forceDialogOnFailure){
  
    this.setAutoSaveFormInActiveView(this);
    if (!this.view.standardWindow.isAutoSave() && this.hasChanged && action) {
      this.autoSaveConfirmAction(action);
    } else if (this.view.standardWindow.isAutoSave() && this.hasChanged && !this.inAutoSave) {
      this.inAutoSave = true;
      this.saveRow(action, true, forceDialogOnFailure);
    } else {
      this.callAutoSaveAction(action);
    }
    return true;
  },
  
  setAutoSaveFormInActiveView: function(form) {
    // the view of the form can be another than
    // the current active view
    if (this.view.standardWindow.activeView) {
      this.view.standardWindow.activeView.autoSaveForm = form;
    }
  },
  
  setActionAfterAutoSave: function(action) {
    if (!this.autoSaveAction) {
      this.autoSaveAction = action;
    }
  },
  
  autoSaveConfirmAction: function(action){
    action = this.autoSaveAction || action;
    this.autoSaveAction = null;

    var form = this;
    var callback = function(ok){
      if (ok) {
        form.resetForm();
        form.callAutoSaveAction(action);
      } else {
        // and focus to the first error field
        form.setFocusInErrorField(true);
        form.focus();
      }
    };
    isc.ask(OB.I18N.getLabel('OBUIAPP_AutoSaveNotPossibleExecuteAction'), callback);
  },
  
  // always let the saveRow callback handle the error
  saveEditorReply : function (response, data, request) {
    return true;
  },
  
  saveRow: function(action, autoSave, forceDialogOnFailure){
    var i, length, flds, form = this;
    
    form.isSaving = true;
    
    // disable some buttons
    this.view.setToolBarButtonState();
    
    var callback = function(resp, data, req){
      var index1, index2, errorCode, view = form.view;
      var status = resp.status;
      if (status === isc.RPCResponse.STATUS_SUCCESS) {
        // needs to be done before the selectRecordById
        this.setAutoSaveFormInActiveView(null);

        // do remember values here to prevent infinite autosave loop
        form.rememberValues();
        
        view.messageBar.setMessage(isc.OBMessageBar.TYPE_SUCCESS, null, OB.I18N.getLabel('OBUIAPP_SaveSuccess'));
        
        if (form.isNew) {
          // force a fetch to place the grid on the correct location
          view.viewGrid.selectRecordById(data.id, true);
        } else {
          // after save the grid selection seems to have gone, repair it
          view.viewGrid.selectRecordById(data.id, false);
        }
        
        this.setNewState(false);
        this.setHasChanged(false);

        // success invoke the action:
        form.callAutoSaveAction(action);
      } else if (status === isc.RPCResponse.STATUS_VALIDATION_ERROR && resp.errors) {
        form.handleFieldErrors(resp.errors, autoSave);
      } else {
        view.setErrorMessageFromResponse(resp, data, req);
      }

      form.isSaving = false;
      
      // an error occured, show a popup
      if (status !== isc.RPCResponse.STATUS_SUCCESS) {
        // if there is an action, ask for confirmation
        action = form.autoSaveAction || action;
        if (action && autoSave) {
          this.autoSaveConfirmAction(action);
        } else if (!view.isVisible() || forceDialogOnFailure) {
          isc.warn(OB.I18N.getLabel('OBUIAPP_AutoSaveError', [view.tabTitle]));
       }
      }
      // only show the warning once, allow actions to take place
      // from now on
      this.setAutoSaveFormInActiveView(null);
      form.inAutoSave = false;
      view.setToolBarButtonState();
      return false;
    };
    
    // note validate will also set the formfocus, this is 
    // done by calling showErrors without the third parameter to true
    if (!this.validate()) {
      this.handleFieldErrors(null, autoSave);
      
      action = form.autoSaveAction || action;

      if (!form.view.isVisible() || forceDialogOnFailure) {
        isc.warn(OB.I18N.getLabel('OBUIAPP_AutoSaveError', [this.view.tabTitle]));
      } else if (action) {
        this.autoSaveConfirmAction(action);
      }
      form.inAutoSave = false;
      form.isSaving = false;
      form.view.setToolBarButtonState();
      this.setAutoSaveFormInActiveView(null);
      return;
    } else {
      // remove the error message if any
      this.view.messageBar.hide();
    }
    // last parameter true prevents additional validation
    this.saveData(callback, {
      willHandleError: true,
      formSave: true
    }, true);
  },
  
  // overridden to prevent focus setting when autoSaving
  
  showErrors: function(errors, hiddenErrors, suppressAutoFocus){
    if (this.inAutoSave) {
      return this.Super('showErrors', [errors, hiddenErrors, true]);
    }
    return this.Super('showErrors', arguments);
  },
  
  handleFieldErrors: function(errors, autoSave){
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
    this.setFocusInErrorField(autoSave || this.inAutoSave);
    this.inAutoSave = false;
  },
  
  setFocusInErrorField: function(autoSave){
    var errorFld = this.getFirstErrorItem();
    if (errorFld) {
      if (autoSave) {
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
    flds = this.getFields();
    length = flds.length;
    for (i = 0; i < length; i++) {
      if (flds[i].getErrors()) {
        return flds[i];
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
    return this.getErrorPromptString(errs);
  },
  
  itemHoverHTML: function(item){
    var errs = item.getErrors();
    if (!errs) {
      return this.Super('itemHoverHTML', arguments);
    }
    return this.getErrorPromptString(errs);
  },
  
  getErrorPromptString: function(errors){
    var errorString = '';
    if (!isc.isAn.Array(errors)) {
      errors = [errors];
    }
    for (var i = 0; i < errors.length; i++) {
      errorString += (i > 0 ? '<br>' : '') + errors[i].asHTML();
    }
    return errorString;
  },
  
  // overridden here to place the link icon after the mandatory sign
  getTitleHTML: function(item, error){
    var titleHTML = this.Super('getTitleHTML', arguments);
    
    if (item.showLinkIcon) {
    
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
      
      return titleHTML + '&nbsp;' + imgHTML;
    }
    
    return titleHTML;
  },
  
  // we are being reshown, get new values for the combos
  visibilityChanged: function(visible){
    if (visible && this.view.isShowingForm) {
      this.doChangeFICCall();
    }
  },
  
  callAutoSaveAction: function(action) {
    // get rid of this value
    this.setAutoSaveFormInActiveView(null);
    
    // note pick up the form.autoSaveAction late to give 
    // time to set it
    action = this.autoSaveAction || action;
    this.autoSaveAction = null;
    if (!action) {
      return;
    }
    if (action.callback) {
      action.callback();
    } else {
      action.method.apply(action.target, action.parameters);
    }
  },

  onFieldChanged: function(form, item, value) {
    isc.Log.logWarn('To be implemented dynamically');
  }
});
