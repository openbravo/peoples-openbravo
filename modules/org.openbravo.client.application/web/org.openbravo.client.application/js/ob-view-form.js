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
 * All portions are Copyright (C) 2010 Openbravo SLU
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
  numCols: 4,
  
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
    var ret = this.Super("editRecord", arguments);
    this.retrieveInitialValues();
    return ret;
  },
  
  getFieldFromInpColumnName: function(inpColumnName){
    if (!this.fieldsByInpColumnName) {
      var localResult = [], fields = this.getFields();
      for (var i = 0; i < fields.length; i++) {
        localResult[fields[i].inpColumnName] = fields[i];
      }
      this.fieldsByInpColumnName = localResult;
    }
    return this.fieldsByInpColumnName[inpColumnName.toLowerCase()];
  },
  
  getFieldFromColumnName: function(columnName){
    if (!this.fieldsByColumnName) {
      var localResult = [], fields = this.getFields();
      for (var i = 0; i < fields.length; i++) {
        localResult[fields[i].columnName] = fields[i];
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
      me.processInitialValues(response, data, request);
    });
  },
  
  processInitialValues: function(response, data, request){
    // TODO: an error occured, handles this much better...
    if (!data) {
      return;
    }
    var columnValues = data.columnValues, calloutMessages = data.calloutMessages, auxInputs = data.auxiliaryInputValues, prop, value;
    if (columnValues) {
      for (prop in columnValues) {
        if (columnValues.hasOwnProperty(prop)) {
          this.processColumnValue(prop, columnValues[prop]);
        }
      }
    }
    if (calloutMessages) {
      // show messages...
    }
    if (auxInputs) {
      for (prop in auxInputs) {
        if (auxInputs.hasOwnProperty(prop)) {
          this.setValue(prop, auxInputs[prop].value);
        }
      }
    }
  },
  
  processColumnValue: function(columnName, columnValue){
    var isDate, i, valueMap = {}, field = this.getFieldFromColumnName(columnName), entries = columnValue.entries;
    if (!field) {
      // ignore for now, the pk is also passed in
      //isc.warn('No field found using column name: ' + columnName + ' for tab ' + this.view.tabId);
      return;
    }
    if (entries && entries.length > 0) {
      if (field.getDataSource()) {
        field.getDataSource().setCacheData(entries, true);
      } else {
        for (i = 0; i < entries.length; i++) {
          valueMap[entries[i][OB.Constants.ID]] = entries[i][OB.Constants.IDENTIFIER];
        }
        field.setValueMap(valueMap);
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
  }
});
