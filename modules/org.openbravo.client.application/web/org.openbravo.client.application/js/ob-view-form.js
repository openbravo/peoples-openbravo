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
  dynamicCols: [],
  width: '100%',
  
  showErrorIcons: false,
  showErrorStyle: true,
  selectOnFocus: true,
  
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
  
  initWidget: function(){
    // iterate over the fields and set the datasource
    //    var field, i, fieldsNum = this.fields.length;
    //    for (i = 0; i < fieldsNum; i++) {
    //      field = this.fields[i];
    //      if (field.dataSourceId) {
    // field.optionDataSource = OB.Datasource.get(field.dataSourceId, field, 'optionDataSource');
    //      }
    //    }
    var ret = this.Super('initWidget', arguments);
    return ret;
  },
  
  editRecord: function(record){
    var ret = this.Super('editRecord', arguments);
    this.retrieveInitialValues(false);
    return ret;
  },
  
  editNewRecord: function(){
    var ret = this.Super('editNewRecord', arguments);
    this.view.toolBar.setLeftMemberDisabled(isc.OBToolbar.TYPE_DELETE, true);
    this.retrieveInitialValues(true);
    return ret;
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
    var parentId = null, me = this, mode;
    if (this.view.parentProperty) {
      parentId = this.getValue(this.view.parentProperty);
    }
    if (isNew) {
      mode = 'NEW';
    } else {
      mode = 'EDIT';
    }
    
    OB.RemoteCallManager.call('org.openbravo.client.application.window.FormInitializationComponent', {}, {
      MODE: 'EDIT',
      PARENT_ID: parentId,
      TAB_ID: this.view.tabId,
      ROW_ID: this.getValue(OB.Constants.ID)
    }, function(response, data, request){
      me.processFICReturn(response, data, request);
    });
  },
  
  processFICReturn: function(response, data, request){
    // TODO: an error occured, handles this much better...
    if (!data) {
      return;
    }
    var columnValues = data.columnValues, calloutMessages = data.calloutMessages, auxInputs = data.auxiliaryInputValues, prop, value;
    var dynamicCols = data.dynamicCols;
    if (columnValues) {
      for (prop in columnValues) {
        if (columnValues.hasOwnProperty(prop)) {
          this.processColumnValue(prop, columnValues[prop]);
        }
      }
    }
    if (calloutMessages && calloutMessages.length > 0) {
      // TODO: check as what type should call out messages be displayed
      this.view.messageBar.setMessage(isc.OBMessageBar.TYPE_INFO, null, calloutMessages);
    }
    if (auxInputs) {
      this.auxInputs = {};
      for (prop in auxInputs) {
        if (auxInputs.hasOwnProperty(prop)) {
          this.setValue(prop, auxInputs[prop].value);
          auxInputs[prop] = auxInputs[prop].value;
        }
      }
    }
    if (dynamicCols) {
      this.dynamicCols = dynamicCols;
    }
  },
  
  processColumnValue: function(columnName, columnValue){
    var data, record, length, valuePresent, currentValue, isDate, value, i, valueMap = {}, field = this.getFieldFromColumnName(columnName), entries = columnValue.entries;
    if (!field) {
      // ignore for now, the pk is also passed in
      //isc.warn('No field found using column name: ' + columnName + ' for tab ' + this.view.tabId);
      return;
    }
    if (entries) {
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
    } else if (columnValue.value || columnValue.value === 0) {
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
    
    field.redraw();
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
  
  doChangeFICCall: function(item){
    var parentId = null, me = this, requestParams, dataParams;
    var allProperties = {}, sessionProperties = {};
    
    if (this.view.parentProperty) {
      parentId = this.getValue(this.view.parentProperty);
    }
    
    // fills the allProperties    
    this.view.getContextInfo(allProperties, sessionProperties, true);
    
    requestParams = {
      MODE: 'CHANGE',
      PARENT_ID: parentId,
      TAB_ID: this.view.tabId,
      ROW_ID: this.getValue(OB.Constants.ID),
      CHANGED_COLUMN: item.inpColumnName
    };
    dataParams = isc.addProperties({}, allProperties, sessionProperties);
    
    // collect the context information    
    OB.RemoteCallManager.call('org.openbravo.client.application.window.FormInitializationComponent', dataParams, requestParams, function(response, data, request){
      me.processFICReturn(response, data, request);
    });
  },
  
  itemChanged: function(item, newValue){
    var ret = this.Super('itemChanged', arguments);
    this.itemChangeActions();
    return ret;
  },
  
  itemChangeActions: function(){
    // remove the message 
    this.view.messageBar.hide();
    this.view.toolBar.setLeftMemberDisabled(isc.OBToolbar.TYPE_SAVE, false);
    this.view.toolBar.setLeftMemberDisabled(isc.OBToolbar.TYPE_UNDO, false);
  },
  
  undo: function(){
    this.view.toolBar.setLeftMemberDisabled(isc.OBToolbar.TYPE_SAVE, true);
    this.view.toolBar.setLeftMemberDisabled(isc.OBToolbar.TYPE_UNDO, true);
    this.resetValues();
  },
  
  saveRow: function(){
    var i, length, flds;
    
    // disable the save
    this.view.toolBar.setLeftMemberDisabled(isc.OBToolbar.TYPE_SAVE, true);
    
    var callback = function(resp, data, req){
//      console.log(data);
//      console.log(resp.status);
//      console.log(resp.errors);
      var index1, index2, errorCode;
      var status = resp.status;
      if (status === isc.RPCResponse.STATUS_SUCCESS) {
        this.view.messageBar.setMessage(isc.OBMessageBar.TYPE_SUCCESS, null, OB.I18N.getLabel('OBUIAPP_SaveSuccess'));
        this.view.toolBar.setLeftMemberDisabled(isc.OBToolbar.TYPE_UNDO, true);
        this.view.toolBar.setLeftMemberDisabled(isc.OBToolbar.TYPE_DELETE, false);
      } else if (status === isc.RPCResponse.STATUS_VALIDATION_ERROR && resp.errors) {
        this.handleFieldErrors(resp.errors);
      } else {
        if (isc.isA.String(data) && data.indexOf('@') !== -1) {
          index1 = data.indexOf('@');
          index2 = data.indexOf('@', index1 + 1);
          if (index2 !== -1) {
            errorCode = data.substring(index1 + 1, index2);
            this.view.messageBar.setLabel(isc.OBMessageBar.TYPE_ERROR, null, errorCode);
          } else {
            this.view.messageBar.setMessage(isc.OBMessageBar.TYPE_ERROR, null, data);
          }
        } else {
          this.view.messageBar.setMessage(isc.OBMessageBar.TYPE_ERROR, null, data);
        }
      }
      
      return false;
    };
    
    if (!this.validate()) {
      this.handleFieldErrors();
      return;
    } else {
      // remove the error message if any
      this.view.messageBar.hide();
    }
    // last parameter true prevents additional validation
    this.saveData(callback, {
      willHandleError: true
    }, true);
  },
  
  handleFieldErrors: function(errors){
    if (errors) {
      this.setErrors(errors, true);
    }
    // set the error message
    this.view.messageBar.setMessage(isc.OBMessageBar.TYPE_ERROR, null, OB.I18N.getLabel('OBUIAPP_ErrorInFields'));
    
    // and focus to the first error field
    flds = this.getFields();
    length = flds.length;
    for (i = 0; i < length; i++) {
      if (flds[i].getErrors()) {
        flds[i].focusInItem();
        break;
      }
    }
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
  }
  
});

isc.RPCManager.addClassProperties({
  handleError: function(response, request){
    alert('error!');
  }
});
