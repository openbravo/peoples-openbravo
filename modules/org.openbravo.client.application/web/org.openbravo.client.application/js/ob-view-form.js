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
  
  // ** {{ Layout Settings }} **
  numCols: 4,
  colWidths: ['24%', '24%', '24%', '24%'],
  
  titleSuffix: '',
  requiredTitleSuffix: '</b>',
  
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
    this.retrieveInitialValues();
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
  
  retrieveInitialValues: function(){
    var parentId = null, me = this;
    if (this.view.parentProperty) {
      parentId = this.getValue(this.view.parentProperty);
    }
    OB.RemoteCallManager.call('org.openbravo.client.application.window.FormInitializationComponent', null, {
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
    if (calloutMessages && calloutMessages.length > 0 && calloutMessages[0]) {
      // TODO: handle multiple messages
      this.view.standardWindow.messageBar.setMessage(calloutMessages[0]);
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
    
    if (columnValue.value) {
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
  
  handleItemChange: function(item){
    if (item._doFICCall) {
      var i;
      for (i = 0; i < this.dynamicCols.length; i++) {
        if (this.dynamicCols[i] === item.inpColumnName) {
          item._doFICCall = false;
          this.doChangeFICCall(item);
          return;
        }
      }
    }
    item._doFICCall = false;
  },
  
  doChangeFICCall: function(item){
    var parentId = null, me = this, requestParams;
    var allProperties = {}, sessionProperties = {};
    
    if (this.view.parentProperty) {
      parentId = this.getValue(this.view.parentProperty);
    }
    
    // fills the allProperties    
    this.view.getContextInfo(allProperties, sessionProperties);
    
    requestParams = isc.addProperties({
      MODE: 'CHANGE',
      PARENT_ID: parentId,
      TAB_ID: this.view.tabId,
      ROW_ID: this.getValue(OB.Constants.ID),
      CHANGED_COLUMN: item.inpColumnName
    }, allProperties, sessionProperties);
    
    // collect the context information    
    OB.RemoteCallManager.call('org.openbravo.client.application.window.FormInitializationComponent', null, requestParams, function(response, data, request){
      me.processFICReturn(response, data, request);
    });
  }
});
