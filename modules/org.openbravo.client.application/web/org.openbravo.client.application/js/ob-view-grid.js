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
isc.ClassFactory.defineClass('OBViewGrid', isc.OBGrid);

isc.OBViewGrid.addClassProperties({
  EDIT_LINK_FIELD_NAME: '_editLink',
  NO_COUNT_PARAMETER: '_noCount', // prevent the count operation on the server
  // note following 2 values should be the same
  // ListGrid._$ArrowUp and ListGrid._$ArrowDown
  ARROW_UP_KEY_NAME: 'Arrow_Up',
  ARROW_DOWN_KEY_NAME: 'Arrow_Down',
  ERROR_MESSAGE_PROP: isc.OBViewGrid.ERROR_MESSAGE_PROP
});

// = OBViewGrid =
// The OBViewGrid is the Openbravo specific subclass of the Smartclient
// ListGrid.
isc.OBViewGrid.addProperties({

  // ** {{{ view }}} **
  // The view member contains the pointer to the composite canvas which
  // handles this form
  // and the grid and other related components.
  view: null,
  
  // ** {{{ foreignKeyFieldNames }}} **
  // The list of fields which are foreign keys, these require custom
  // filtering.
  foreignKeyFieldNames: [],
  
  // ** {{{ editGrid }}} **
  // Controls if an edit link column is created in the grid, set to false to
  // prevent this.
  editGrid: true,
  
  // ** {{{ editLinkFieldProperties }}} **
  // The properties of the ListGridField created for the edit links.
  editLinkFieldProperties: {
    type: 'text',
    canSort: false,
    frozen: true,
    canFreeze: true,
    canEdit: false,
    canGroupBy: false,
    canHide: false,
    showTitle: true,
    title: '&nbsp;',
    autoFitWidth: true,
    canDragResize: false,
    canFilter: true,
    autoExpand: false,
    filterEditorType: 'StaticTextItem',
    filterEditorProperties: {
      textAlign: 'center'
    },
    name: isc.OBViewGrid.EDIT_LINK_FIELD_NAME
  },
  
  // ** {{{ dataPageSize }}} **
  // The data page size used for loading paged data from the server.
  dataPageSize: 100,
  
  autoFitFieldWidths: true,
  autoFitWidthApproach: 'title',
  canAutoFitFields: false,
  width: '100%',
  height: '100%',
  
  autoFetchTextMatchStyle: 'substring',
  showFilterEditor: true,
  canEdit: true,
  alternateRecordStyles: true,
  canReorderFields: true,
  canFreezeFields: true,
  canAddFormulaFields: true,
  canAddSummaryFields: true,
  canGroupBy: false,
  selectionAppearance: 'checkbox',
  useAllDataSourceFields: false,
  editEvent: 'none',
  showCellContextMenus: true,
  canOpenRecordEditor: true,
  showDetailFields: true,
  showErrorIcons: false,
  
  // internal sc grid property, see the ListGrid source code
  preserveEditsOnSetData: false,
  
  // enabling this results in a slower user interaction
  // it is better to allow fast grid interaction and if an error occurs
  // dismiss any new records being edited and go back to the edit row
  // which causes the error
  waitForSave: false,
  stopOnErrors: false,
  confirmDiscardEdits: false,
  canMultiSort: false,
  
  emptyMessage: OB.I18N.getLabel('OBUISC_ListGrid.loadingDataMessage'),
  discardEditsSaveButtonTitle: OB.I18N.getLabel('UINAVBA_Save'),
  
  quickDrawAheadRatio: 6.0,
  drawAheadRatio: 4.0,
  // note: don't set drawAllMaxCells too high as it results in extra reads
  // of data, Smartclient will try to read until drawAllMaxCells has been
  // reached
  drawAllMaxCells: 100,
  
  // keeps track if we are in objectSelectionMode or in toggleSelectionMode
  // objectSelectionMode = singleRecordSelection === true
  singleRecordSelection: false,
  
  // editing props
  rowEndEditAction: 'next',
  listEndEditAction: 'next',
  enforceVClipping: true,
  validateByCell: true,
  
  currentEditColumnLayout: null,
  
  recordBaseStyleProperty: '_recordStyle',
  
  // modal editing is not possible as we need to be able to do 
  // undo, which means clicks outside of the current form.
  modalEditing: false,
  
  timeFormatter: 'to24HourTime',
  
  dataProperties: {
    useClientFiltering: false,
    useClientSorting: false,
    
    transformData: function(newData, dsResponse){
      // only do this stuff for fetch operations, in other cases strange things
      // happen as update/delete operations do not return the totalRows parameter
      if (dsResponse && dsResponse.context && dsResponse.context.operationType !== 'fetch') {
        return;
      }
      // correct the length if there is already data in the localData array
      if (this.localData) {
        for (var i = dsResponse.endRow + 1; i < this.localData.length; i++) {
          if (!Array.isLoading(this.localData[i]) && this.localData[i]) {
            dsResponse.totalRows = i + 1;
          } else {
            break;
          }
        }
      }
      if (this.localData && this.localData[dsResponse.totalRows]) {
        this.localData[dsResponse.totalRows] = null;
      }
    }
  },
  
  refreshFields: function(){
    this.setFields(this.completeFields.duplicate());
  },
  
  initWidget: function(){
    // make a copy of the dataProperties otherwise we get 
    // change results that values of one grid are copied/coming back
    // in other grids
    this.dataProperties = isc.addProperties({}, this.dataProperties);
    
    var thisGrid = this, localEditLinkField;
    if (this.editGrid) {
      // add the edit pencil in the beginning
      localEditLinkField = isc.addProperties({}, this.editLinkFieldProperties);
      localEditLinkField.width = this.editLinkColumnWidth;
      this.fields.unshift(localEditLinkField);
    }
    
    this.editFormDefaults = isc.addProperties({}, OB.ViewFormProperties, this.editFormDefaults);
    
    // added for showing counts in the filtereditor row
    this.checkboxFieldDefaults = isc.addProperties(this.checkboxFieldDefaults, {
      canFilter: true,
      // frozen is much nicer, but check out this forum discussion:
      // http://forums.smartclient.com/showthread.php?p=57581
      frozen: true,
      canFreeze: true,
      showHover: true,
      prompt: OB.I18N.getLabel('OBUIAPP_GridSelectAllColumnPrompt'),
      filterEditorProperties: {
        textAlign: 'center'
      },
      filterEditorType: 'StaticTextItem'
    });
    
    var ret = this.Super('initWidget', arguments);
    
    this.noDataEmptyMessage = OB.I18N.getLabel('OBUISC_ListGrid.loadingDataMessage'); // OB.I18N.getLabel('OBUIAPP_GridNoRecords')
    // + ' <span
    // onclick="window[\''
    // + this.ID +
    // '\'].createNew();"
    // class="OBLabelLink">'
    // +
    // OB.I18N.getLabel('OBUIAPP_GridCreateOne')+
    // '</span>';
    this.filterNoRecordsEmptyMessage = OB.I18N.getLabel('OBUIAPP_GridFilterNoResults') +
    ' <span onclick="window[\'' +
    this.ID +
    '\'].clearFilter();" class="OBLabelLink">' +
    OB.I18N.getLabel('OBUIAPP_GridClearFilter') +
    '</span>';
    
    return ret;
  },
  
  // add the properties from the form
  addFormProperties: function(props){
    isc.addProperties(this.editFormDefaults, props);
  },
  
  getCellAlign: function(record, rowNum, colNum){
    if (rowNum === this.getEditRow()) {
      return 'center';
    }
    return this.Super('getCellAlign', arguments);
  },
  
  // overridden to support hover on the header for the checkbox field
  setFieldProperties: function(field, properties){
    var localField = field;
    if (isc.isA.Number(localField)) {
      localField = this.fields[localField];
    }
    if (this.isCheckboxField(localField) && properties) {
      properties.showHover = true;
      properties.prompt = OB.I18N.getLabel('OBUIAPP_GridSelectAllColumnPrompt');
    }
    
    return this.Super('setFieldProperties', arguments);
  },
  
  cellHoverHTML: function(record, rowNum, colNum){
    var field = this.getField(colNum), cellErrors, msg = '', i;
    if (this.isCheckboxField(field)) {
      return OB.I18N.getLabel('OBUIAPP_GridSelectColumnPrompt');
    }
    if (this.cellHasErrors(rowNum, colNum)) {
      cellErrors = this.getCellErrors(rowNum, colNum);
      // note cellErrors can be a string or array
      // accidentally both have the length property
      if (cellErrors && cellErrors.length > 0) {
        return OB.Utilities.getPromptString(cellErrors);
      }
    }
    if (record[isc.OBViewGrid.ERROR_MESSAGE_PROP]) {
      return record[isc.OBViewGrid.ERROR_MESSAGE_PROP];
    }
    
    return this.Super('cellHoverHTML', arguments);
  },
  
  setView: function(view){
    this.view = view;
    this.editFormDefaults.view = view;
    if (this.view.standardWindow.viewState && this.view.standardWindow.viewState[this.view.tabId]) {
      this.setViewState(this.view.standardWindow.viewState[this.view.tabId]);
    }
    
    if (this.getField(this.view.parentProperty)) {
      this.getField(this.view.parentProperty).canFilter = false;
      this.getField(this.view.parentProperty).canEdit = false;
    }
    
  },
  
  show: function(){
    var ret = this.Super('show', arguments);
    
    this.view.toolBar.updateButtonState(true);
    
    this.updateRowCountDisplay();
    
    this.resetEmptyMessage();
    
    return ret;
  },
  
  headerClick: function(fieldNum, header, autoSaveDone){
    if (!autoSaveDone) {
      var actionObject = {
        target: this,
        method: this.headerClick,
        parameters: [fieldNum, header, true]
      };
      this.view.standardWindow.doActionAfterAutoSave(actionObject, true);
      return;
    }
    var field = this.fields[fieldNum];
    if (this.isCheckboxField(field) && this.singleRecordSelection) {
      this.deselectAllRecords();
      this.singleRecordSelection = false;
    }
    return this.Super('headerClick', arguments);
  },
  
  deselectAllRecords: function(preventUpdateSelectInfo, autoSaveDone){
    if (!autoSaveDone) {
      var actionObject = {
        target: this,
        method: this.deselectAllRecords,
        parameters: [preventUpdateSelectInfo, true]
      };
      this.view.standardWindow.doActionAfterAutoSave(actionObject, true);
      return;
    }
    
    this.allSelected = false;
    var ret = this.Super('deselectAllRecords', arguments);
    this.lastSelectedRecord = null;
    if (!preventUpdateSelectInfo) {
      this.selectionUpdated();
    }
    return ret;
  },
  
  selectAllRecords: function(autoSaveDone){
    if (!autoSaveDone) {
      var actionObject = {
        target: this,
        method: this.selectAllRecords,
        parameters: [true]
      };
      this.view.standardWindow.doActionAfterAutoSave(actionObject, true);
      return;
    }
    this.allSelected = true;
    var ret = this.Super('selectAllRecords', arguments);
    this.selectionUpdated();
    return ret;
  },
  
  updateRowCountDisplay: function(){
    var newValue = '', length = this.data.getLength();
    if (length > this.dataPageSize) {
      newValue = '>' + this.dataPageSize;
    } else if (length === 0) {
      newValue = '&nbsp;';
    } else {
      newValue = '' + length;
    }
    if (this.filterEditor && this.filterEditor.getEditForm()) {
      this.filterEditor.getEditForm().setValue(isc.OBViewGrid.EDIT_LINK_FIELD_NAME, newValue);
    }
  },
  
  refreshContents: function(callback){
    this.resetEmptyMessage();
    this.view.updateTabTitle();
    
    // do not refresh if the parent is not selected and we have no data
    // anyway
    if (this.view.parentProperty && (!this.data || !this.data.getLength || this.data.getLength() === 0)) {
      selectedValues = this.view.parentView.viewGrid.getSelectedRecords();
      if (selectedValues.length === 0) {
        if (callback) {
          callback();
        }
        return;
      }
    }
    
    var context = {
      showPrompt: false,
      textMatchStyle: this.autoFetchTextMatchStyle
    };
    this.filterData(this.getCriteria(), callback, context);
  },
  
  // the dataarrived method is where different actions are done after
  // data has arrived in the grid:
  // - open the edit view if default edit mode is enabled
  // - if the user goes directly to a tab (from a link in another window)
  // then
  // opening the relevant record is done here or if no record is passed grid
  // mode is opened
  // - if there is only one record then select it directly
  dataArrived: function(startRow, endRow){
    // do this now, to replace the loading message
    if (this.view.readOnly) {
      this.noDataEmptyMessage = OB.I18N.getLabel('OBUIAPP_NoDataInGrid');
    } else {
      this.noDataEmptyMessage = OB.I18N.getLabel('OBUIAPP_GridNoRecords') +
      ' <span onclick="window[\'' +
      this.ID +
      '\'].view.newRow();" class="OBLabelLink">' +
      OB.I18N.getLabel('OBUIAPP_GridCreateOne') +
      '</span>';
    }
    this.resetEmptyMessage();
    
    var record, ret = this.Super('dataArrived', arguments);
    this.updateRowCountDisplay();
    if (this.getSelectedRecords() && this.getSelectedRecords().length > 0) {
      this.selectionUpdated();
    }
    
    if (this.targetOpenNewEdit) {
      delete this.targetOpenNewEdit;
      // not passing record opens new
      this.view.editRecord();
    } else if (this.targetOpenGrid) {
      // direct link from other window but without a record id
      // so just show grid mode
      // don't need to do anything here
      delete this.targetOpenGrid;
    } else if (this.targetRecordId) {
      // direct link from other tab to a specific record
      this.delayedHandleTargetRecord(startRow, endRow);
    } else if (this.view.shouldOpenDefaultEditMode() && !this.view.isShowingForm) {
      // ui-pattern: single record/edit mode
      this.view.openDefaultEditView(this.getRecord(startRow));
    } else if (this.data && this.data.getLength() === 1) {
      // one record select it directly
      record = this.getRecord(0);
      // this select method prevents state changing if the record
      // was already selected
      this.doSelectSingleRecord(record);
    } else if (this.lastSelectedRecord) {
      // if nothing was select, select the record again
      if (!this.getSelectedRecord()) {
        // if it is still in the cache ofcourse
        var gridRecord = this.data.find(OB.Constants.ID, this.lastSelectedRecord.id);
        if (gridRecord) {
          this.doSelectSingleRecord(gridRecord);
        }
      } else if (this.getSelectedRecords() && this.getSelectedRecords().length !== 1) {
        this.lastSelectedRecord = null;
      }
    }
    
    if (this.actionAfterDataArrived) {
      this.actionAfterDataArrived();
      this.actionAfterDataArrived = null;
    }
    
    return ret;
  },
  
  refreshGrid: function(callback){
    if (this.getSelectedRecord()) {
      this.targetRecordId = this.getSelectedRecord()[OB.Constants.ID];
    }
    this.actionAfterDataArrived = callback;
    this.invalidateCache();
  },
  
  // with a delay to handle the target record when the body has been drawn
  delayedHandleTargetRecord: function(startRow, endRow){
    var rowTop, recordIndex, i, data = this.data, tmpTargetRecordId = this.targetRecordId;
    if (!this.targetRecordId) {
      return;
    }
    if (this.body) {
      // don't need it anymore
      delete this.targetRecordId;
      var gridRecord = data.find(OB.Constants.ID, tmpTargetRecordId);
      
      // no grid record found, stop here
      if (!gridRecord) {
        return;
      }
      recordIndex = this.getRecordIndex(gridRecord);
      
      if (data.criteria) {
        data.criteria._targetRecordId = null;
      }
      
      // remove the isloading status from rows
      for (i = 0; i < startRow; i++) {
        if (Array.isLoading(data.localData[i])) {
          data.localData[i] = null;
        }
      }
      
      this.scrollRecordIntoView(recordIndex, true);
      this.doSelectSingleRecord(gridRecord);
      
      // go to the children, if needed
      if (this.view.standardWindow.directTabInfo) {
        this.view.openDirectChildTab();
      }
      
      //      if (this.view.isShowingForm) {
      //        this.view.viewForm.editRecord(gridRecord);
      //      }
    } else {
      // wait a bit longer til the body is drawn
      this.delayCall('delayedHandleTargetRecord', [startRow, endRow], 200, this);
    }
  },
  
  // Prevents empty message to be shown in frozen part
  // http://forums.smartclient.com/showthread.php?p=57581
  createBodies: function(){
    var ret = this.Super('createBodies', arguments);
    if (this.frozenBody) {
      this.frozenBody.showEmptyMessage = false;
    }
    return ret;
  },
  
  selectRecordById: function(id, forceFetch){
    if (forceFetch) {
      this.targetRecordId = id;
      this.filterData(this.getCriteria());
      return;
    }
    
    var recordIndex, gridRecord = this.data.find(OB.Constants.ID, id);
    // no grid record fetch it
    if (!gridRecord) {
      this.targetRecordId = id;
      this.filterData(this.getCriteria());
      return;
    }
    recordIndex = this.getRecordIndex(gridRecord);
    this.scrollRecordIntoView(recordIndex, true);
    this.doSelectSingleRecord(gridRecord);
  },
  
  // overridden to prevent extra firing of selection updated event
  selectSingleRecord: function(record){
    this.deselectAllRecords(true);
    this.selectRecord(record);
    
    // keep it to try to get it back when the selection gets lost when
    // loading data
    this.lastSelectedRecord = record;
  },
  
  // overridden to prevent extra firing of selection updated event
  // selectrecords will fire it once
  selectRecord: function(record, state, colNum){
    this.selectRecords(record, state, colNum);
  },
  
  filterData: function(criteria, callback, requestProperties){
    if (!requestProperties) {
      requestProperties = {};
    }
    requestProperties.showPrompt = false;
    
    var theView = this.view;
    var newCallBack = function(){
      theView.recordSelected();
      if (callback) {
        callback();
      }
    };
    
    return this.Super('filterData', [this.convertCriteria(criteria), newCallBack, requestProperties]);
  },
  
  fetchData: function(criteria, callback, requestProperties){
    if (!requestProperties) {
      requestProperties = {};
    }
    requestProperties.showPrompt = false;
    
    var theView = this.view;
    
    var newCallBack = function(){
      theView.recordSelected();
      if (callback) {
        callback();
      }
    };
    
    return this.Super('fetchData', [this.convertCriteria(criteria), newCallBack, requestProperties]);
  },
  
  handleFilterEditorSubmit: function(criteria, context, autoSaveDone){
    if (!autoSaveDone) {
      var actionObject = {
        target: this,
        method: this.handleFilterEditorSubmit,
        parameters: [criteria, context, true]
      };
      this.view.standardWindow.doActionAfterAutoSave(actionObject, true);
      return;
    }
    
    this.Super('handleFilterEditorSubmit', arguments);
  },
  
  getInitialCriteria: function(){
    var criteria = this.Super('getInitialCriteria', arguments);
    
    return this.convertCriteria(criteria);
  },
  
  getCriteria: function(){
    var criteria = this.Super('getCriteria', arguments) || {};
    criteria = this.convertCriteria(criteria);
    return criteria;
  },
  
  convertCriteria: function(criteria){
    var selectedValues, prop, fld, value;
    
    criteria = isc.addProperties({}, criteria || {});
    
    if (this.targetRecordId) {
      // do not filter on anything with a targetrecord
      criteria = {};
      // remove the filter clause we don't want to use
      this.filterClause = null;
      criteria._targetRecordId = this.targetRecordId;
    }
    
    // filter criteria for foreign key fields should be on the identifier
    // note is again repaired in the filtereditor setValuesAsCriteria
    // see the ob-grid.js 
    for (prop in criteria) {
      if (criteria.hasOwnProperty(prop)) {
        if (prop === this.view.parentProperty) {
          continue;
        }
        fld = this.getField(prop);
        if (fld && fld.foreignKeyField) {
          value = criteria[prop];
          delete criteria[prop];
          criteria[prop + '.' + OB.Constants.IDENTIFIER] = value;
        }
      }
    }
    
    // note pass in criteria otherwise infinite looping!
    this.resetEmptyMessage(criteria);
    
    if (this.view.parentProperty) {
      selectedValues = this.view.parentView.viewGrid.getSelectedRecords();
      if (selectedValues.length === 0) {
        criteria[this.view.parentProperty] = '-1';
      } else if (selectedValues.length > 1) {
        criteria[this.view.parentProperty] = '-1';
      } else {
        criteria[this.view.parentProperty] = selectedValues[0][OB.Constants.ID];
      }
    }
    
    // prevent the count operation
    criteria[isc.OBViewGrid.NO_COUNT_PARAMETER] = 'true';
    
    if (this.orderByClause) {
      criteria[OB.Constants.ORDERBY_PARAMETER] = this.orderByClause;
    }
    
    if (this.filterClause) {
      if (this.whereClause) {
        criteria[OB.Constants.WHERE_PARAMETER] = ' ((' + this.whereClause + ') and (' + this.filterClause + ")) ";
      } else {
        criteria[OB.Constants.WHERE_PARAMETER] = this.filterClause;
      }
      this.checkShowFilterFunnelIcon(criteria);
    } else if (this.whereClause) {
      criteria[OB.Constants.WHERE_PARAMETER] = this.whereClause;
      this.checkShowFilterFunnelIcon(criteria);
    } else {
      criteria[OB.Constants.WHERE_PARAMETER] = null;
      this.checkShowFilterFunnelIcon(criteria);
    }
    
    // add all the new session properties context info to the criteria
    isc.addProperties(criteria, this.view.getContextInfo(true, false));
    
    return criteria;
  },
  
  createNew: function(){
    this.view.editRecord();
  },
  
  clearFilter: function(){
    delete this.filterClause;
    this.filterEditor.getEditForm().clearValues();
    this.filterEditor.performAction();
  },
  
  // determine which field can be autoexpanded to use extra space
  getAutoFitExpandField: function(){
    for (var i = 0; i < this.autoExpandFieldNames.length; i++) {
      var field = this.getField(this.autoExpandFieldNames[i]);
      if (field && field.name) {
        return field.name;
      }
    }
    return this.Super('getAutoFitExpandField', arguments);
  },
  
  recordClick: function(viewer, record, recordNum, field, fieldNum, value, rawValue){
    var actionObject = {
      target: this,
      method: this.handleRecordSelection,
      parameters: [viewer, record, recordNum, field, fieldNum, value, rawValue, false]
    };
    this.view.standardWindow.doActionAfterAutoSave(actionObject, true);
  },
  
  recordDoubleClick: function(viewer, record, recordNum, field, fieldNum, value, rawValue){
    var actionObject = {
      target: this.view,
      method: this.view.editRecord,
      parameters: [record]
    };
    this.view.standardWindow.doActionAfterAutoSave(actionObject, true);
  },
  
  resetEmptyMessage: function(criteria){
    criteria = criteria || this.getCriteria();
    if (!this.view) {
      this.emptyMessage = this.noDataEmptyMessage;
    } else if (this.isGridFiltered(criteria)) {
      this.emptyMessage = this.filterNoRecordsEmptyMessage;
    } else if (this.view.isRootView) {
      this.emptyMessage = this.noDataEmptyMessage;
    } else {
      selectedValues = this.view.parentView.viewGrid.getSelectedRecords();
      if (selectedValues.length === 0) {
        this.emptyMessage = OB.I18N.getLabel('OBUIAPP_NoParentSelected');
      } else if (selectedValues.length === 1 && selectedValues[0]._new) {
        this.emptyMessage = OB.I18N.getLabel('OBUIAPP_ParentIsNew');
      } else if (selectedValues.length > 1) {
        this.emptyMessage = OB.I18N.getLabel('OBUIAPP_MultipleParentsSelected');
      } else {
        this.emptyMessage = this.noDataEmptyMessage;
      }
    }
  },
  
  // +++++++++++++++++++++++++++++ Context menu on record click +++++++++++++++++++++++
  
  cellContextClick: function(record, rowNum, colNum){
    //this.handleRecordSelection(null, record, rowNum, null, colNum, null, null, true);
    this.view.setAsActiveView();
    var ret = this.Super('cellContextClick', arguments);
    return ret;
  },
  
  makeCellContextItems: function(record, rowNum, colNum){
    var sourceWindow = this.view.standardWindow.windowId;
    var menuItems = [];
    var field = this.getField(colNum);
    var grid = this;
    menuItems.add({
      title: OB.I18N.getLabel('OBUIAPP_CreateNewRecord'),
      click: function(){
        grid.startEditingNew(rowNum);
      }
    });
    if (this.canEdit && this.isWritable(record) && !this.view.readOnly) {
      menuItems.add({
        title: OB.I18N.getLabel('OBUIAPP_EditInGrid'),
        click: function(){
          grid.endEditing();
          grid.startEditing(rowNum, colNum);
        }
      });
    }
    if (field.canFilter) {
      menuItems.add({
        title: OB.I18N.getLabel('OBUIAPP_UseAsFilter'),
        click: function(){
          var value;
          var filterCriteria = grid.getCriteria();
          // a foreign key field, use the displayfield/identifier
          if (field.foreignKeyField && field.displayField) {
            value = record[field.displayField];
            filterCriteria[field.displayField] = value;
          } else {
            value = grid.getEditDisplayValue(rowNum, colNum, record);
            filterCriteria[field.name] = value;
          }
          grid.setCriteria(filterCriteria);
          grid.checkShowFilterFunnelIcon(grid.getCriteria());
          grid.filterData(grid.getCriteria());
        }
      });
    }
    if (field.foreignKeyField) {
      menuItems.add({
        title: OB.I18N.getLabel('OBUIAPP_OpenOnTab'),
        click: function(){
          var fldName = field.name;
          var dotIndex = fldName.indexOf('.');
          if (dotIndex !== -1) {
            fldName = fldName.substring(0, dotIndex);
          }
          OB.Utilities.openDirectView(sourceWindow, field.referencedKeyColumnName, field.targetEntity, record[fldName]);
        }
      });
    }
    
    return menuItems;
  },
  
  // +++++++++++++++++++++++++++++ Record Selection Handling +++++++++++++++++++++++
  
  updateSelectedCountDisplay: function(){
    var selection = this.getSelection();
    var selectionLength = selection.getLength();
    var newValue = '&nbsp;';
    if (selectionLength > 0) {
      newValue = selectionLength + '';
    }
    if (this.filterEditor) {
      this.filterEditor.getEditForm().setValue(this.getCheckboxField().name, newValue);
    }
  },
  
  // note when solving selection issues in the future also
  // consider using the selectionChanged method, but that
  // one has as disadvantage that it is called multiple times
  // for one select/deselect action
  selectionUpdated: function(record, recordList){
  
    // close any editors we may have
    this.closeAnyOpenEditor();
    this.stopHover();
    this.updateSelectedCountDisplay();
    this.view.recordSelected();
    if (this.getSelectedRecords() && this.getSelectedRecords().length !== 1) {
      this.lastSelectedRecord = null;
    } else {
      this.lastSelectedRecord = this.getSelectedRecord();
    }
  },
  
  selectOnMouseDown: function(record, recordNum, fieldNum, autoSaveDone){
    // don't change selection on right mouse down
    var EH = isc.EventHandler, eventType;
    
    if (!autoSaveDone) {
      var actionObject = {
        target: this,
        method: this.selectOnMouseDown,
        parameters: [record, recordNum, fieldNum, true]
      };
      this.view.standardWindow.doActionAfterAutoSave(actionObject, true);
      return;
      // only call this method in case a checkbox click was done
      // in all other cases the recordClick will be called later
      // anyway
      //      if (this.getCheckboxFieldPosition() === fieldNum) {
      //        this.setActionAfterAutoSave(this, this.selectOnMouseDown, arguments);
      //      }
    }
    
    var previousSingleRecordSelection = this.singleRecordSelection;
    var currentSelectedRecordSelected = (this.getSelectedRecord() === record);
    if (this.getCheckboxFieldPosition() === fieldNum) {
      if (this.singleRecordSelection) {
        this.deselectAllRecords(true);
      }
      this.singleRecordSelection = false;
      this.Super('selectOnMouseDown', arguments);
      
      // handle a special case:
      // - singlerecordmode: checkbox is not checked
      // - user clicks on checkbox
      // in this case move to multi select mode and keep the record selected
      if (previousSingleRecordSelection && currentSelectedRecordSelected) {
        this.selectSingleRecord(record);
      }
      
      this.selectionUpdated();
      
      this.markForRedraw('Selection checkboxes need to be redrawn');
    } else {
      // do some checking, the handleRecordSelection should only be called
      // in case of keyboard navigation and not for real mouse clicks,
      // these are handled by the recordClick and recordDoubleClick methods
      // if this method here would also handle mouseclicks then the
      // doubleClick
      // event is not captured anymore
      eventType = EH.getEventType();
      if (!EH.isMouseEvent(eventType)) {
        this.handleRecordSelection(null, record, recordNum, null, fieldNum, null, null, true);
      }
    }
  },
  
  handleRecordSelection: function(viewer, record, recordNum, field, fieldNum, value, rawValue, fromSelectOnMouseDown){
    var EH = isc.EventHandler;
    var keyName = EH.getKey();
    
    // stop editing if the user clicks out of the row
    if (this.getEditRow() && this.getEditRow() !== recordNum) {
      this.endEditing();
    }
    // do nothing, click in the editrow itself
    if (this.getEditRow() && this.getEditRow() === recordNum) {
      return;
    }
    
    // if the arrow key was pressed and no ctrl/shift pressed then
    // go to single select mode
    var arrowKeyPressed = keyName && (keyName === isc.OBViewGrid.ARROW_UP_KEY_NAME || keyName === isc.OBViewGrid.ARROW_DOWN_KEY_NAME);
    
    var previousSingleRecordSelection = this.singleRecordSelection;
    if (arrowKeyPressed) {
      if (EH.ctrlKeyDown() || EH.shiftKeyDown()) {
        // move to multi-select mode, let the standard do it for us
        this.singleRecordSelection = false;
      } else {
        this.doSelectSingleRecord(record);
      }
    } else if (this.getCheckboxFieldPosition() === fieldNum) {
      if (this.singleRecordSelection) {
        this.deselectAllRecords(true);
      }
      // click in checkbox field is done by standard logic
      // in the selectOnMouseDown
      this.singleRecordSelection = false;
      this.selectionUpdated();
    } else if (isc.EventHandler.ctrlKeyDown()) {
      // only do something if record clicked and not from selectOnMouseDown
      // this method got called twice from one clicK: through recordClick
      // and
      // to selectOnMouseDown. Only handle one.
      if (!fromSelectOnMouseDown) {
        this.singleRecordSelection = false;
        // let ctrl-click also deselect records
        if (this.isSelected(record)) {
          this.deselectRecord(record);
        } else {
          this.selectRecord(record);
        }
      }
    } else if (isc.EventHandler.shiftKeyDown()) {
      this.singleRecordSelection = false;
      this.selection.selectOnMouseDown(this, recordNum, fieldNum);
    } else {
      // click on the record which was already selected
      this.doSelectSingleRecord(record);
    }
    
    this.updateSelectedCountDisplay();
    
    // mark some redraws if there are lines which don't
    // have a checkbox flagged, so if we move from single record selection
    // to multi record selection
    if (!this.singleRecordSelection && previousSingleRecordSelection) {
      this.markForRedraw('Selection checkboxes need to be redrawn');
    }
  },
  
  selectRecordForEdit: function(record){
    this.Super('selectRecordForEdit', arguments);
    this.doSelectSingleRecord(record);
  },
  
  doSelectSingleRecord: function(record){
    // if this record is already selected and the only one then do nothing
    // note that when navigating with the arrow key that at a certain 2 are
    // selected
    // when going into this method therefore the extra check on length === 1
    if (this.singleRecordSelection && this.isSelected(record) && this.getSelection().length === 1) {
      return;
    }
    this.singleRecordSelection = true;
    this.selectSingleRecord(record);
    
    // deselect the checkbox in the top
    var fieldNum = this.getCheckboxFieldPosition(), field = this.fields[fieldNum];
    var icon = this.checkboxFieldFalseImage || this.booleanFalseImage;
    var title = this.getValueIconHTML(icon, field);
    
    this.setFieldTitle(fieldNum, title);
  },
  
  // overridden to prevent the checkbox to be shown when only one
  // record is selected.
  getCellValue: function(record, recordNum, fieldNum, gridBody){
    var field = this.fields[fieldNum];
    if (!field || this.allSelected) {
      return this.Super('getCellValue', arguments);
    }
    // do all the cases which are handled in the super directly
    if (this.isCheckboxField(field)) {
      // NOTE: code copied from super class
      var icon;
      if (!this.body.canSelectRecord(record)) {
        // record cannot be selected but we want the space allocated for the
        // checkbox anyway.
        icon = '[SKINIMG]/blank.gif';
      } else if (this.singleRecordSelection && !this.allSelected) {
        // always show the false image
        icon = (this.checkboxFieldFalseImage || this.booleanFalseImage);
      } else {
        // checked if selected, otherwise unchecked
        var isSel = this.selection.isSelected(record) ? true : false;
        icon = isSel ? (this.checkboxFieldTrueImage || this.booleanTrueImage) : (this.checkboxFieldFalseImage || this.booleanFalseImage);
      }
      // if the record is disabled, make the checkbox image disabled as well
      // or if the record is new then also show disabled
      if (!record || record[this.recordEnabledProperty] === false) {
        icon = icon.replace('.', '_Disabled.');
      }
      
      var html = this.getValueIconHTML(icon, field);
      
      return html;
    } else {
      return this.Super('getCellValue', arguments);
    }
  },
  
  getSelectedRecords: function(){
    return this.getSelection();
  },
  
  // +++++++++++++++++ functions for the editing +++++++++++++++++
  
  startEditingNew: function(rowNum){
    var insertRow = this.getDrawArea()[0];
    if (rowNum || rowNum === 0) {
      insertRow = rowNum + 1;
    }
    this.createNewRecordForEditing(insertRow);
    this.startEditing(insertRow);
  },
  
  initializeEditValues: function(rowNum, colNum){
    var record = this.getRecord(rowNum);
    // no record create one
    if (!record) {
      this.createNewRecordForEditing(rowNum);
    }
    return this.Super('initializeEditValues', arguments);
  },
  
  createNewRecordForEditing: function(rowNum){
    // note: the id is dummy, will be replaced when the save succeeds 
    var record = {
      _new: true,
      id: '_' + new Date().getTime()
    };
    
    this.data.insertCacheData(record, rowNum);
    this.updateRowCountDisplay();
    this.redraw();
  },
  
  editFailed: function(rowNum, colNum, newValues, oldValues, editCompletionEvent, dsResponse, dsRequest){
    var record = this.getRecord(rowNum), editRow, editSession;
    var view = this.view;
    
    // set the default error message, 
    // is possibly overridden in the next call
    if (!record[isc.OBViewGrid.ERROR_MESSAGE_PROP]) {
      this.setRecordErrorMessage(rowNum, OB.I18N.getLabel('OBUIAPP_ErrorInFields'));
    } else {
      record[this.recordBaseStyleProperty] = this.recordStyleError;
    }
    
    if (!this.isVisible()) {
      isc.warn(OB.I18N.getLabel('OBUIAPP_TabWithErrors', [this.view.tabTitle]));
    } else if (view.standardWindow.forceDialogOnFailure && !this.view.isActiveView) {
      isc.warn(OB.I18N.getLabel('OBUIAPP_AutoSaveError', [this.view.tabTitle]));
    }
    
    view.standardWindow.cleanUpAutoSaveProperties();
    view.updateTabTitle();
    view.toolBar.updateButtonState(true);
    
    // if nothing else got selected, select ourselves then
    if (!this.getSelectedRecord()) {
      this.selectRecord(record);
    }
  },
  
  editComplete: function(rowNum, colNum, newValues, oldValues, editCompletionEvent, dsResponse){
    var record = this.getRecord(rowNum), editRow, editSession, autoSaveAction;
    
    // a new id has been computed use that now    
    if (record && record._newId) {
      record.id = record._newId;
      delete record._newId;
    }
    
    // during save the record looses the link to the editColumnLayout,
    // restore it
    if (oldValues.editColumnLayout && !record.editColumnLayout) {
      var editColumnLayout = oldValues.editColumnLayout;
      editColumnLayout.record = record;
      record.editColumnLayout = editColumnLayout;
    }
    if (record.editColumnLayout) {
      record.editColumnLayout.editButton.setErrorState(false);
      record.editColumnLayout.showEditOpen();
    }
    
    // remove any new pointer
    delete record._new;
    
    // success invoke the action, if any there
    this.view.standardWindow.autoSaveDone(this.view, true);
    
    // if nothing else got selected, select ourselves then
    if (!this.getSelectedRecord()) {
      this.selectRecord(record);
      this.view.refreshChildViews();
    } else if (this.getSelectedRecord() === record) {
      this.view.refreshChildViews();
    }
        
    // remove the error style/message
    this.setRecordErrorMessage(rowNum, null);
    // update after the error message has been removed
    this.view.updateTabTitle();
    this.view.toolBar.updateButtonState(true);
    this.view.messageBar.hide();
    this.view.refreshParentRecord();
  },
  
  undoEditSelectedRows: function(){
    var selectedRecords = this.getSelectedRecords(), toRemove = [];
    for (var i = 0; i < selectedRecords.length; i++) {
      var rowNum = this.getRecordIndex(selectedRecords[i]);
      var record = selectedRecords[i];
      this.Super('discardEdits', [rowNum, false, false, isc.ListGrid.PROGRAMMATIC]);
      // remove the record if new
      if (record._new) {
        toRemove.push({
          id: record.id
        });
      } else {
        // remove the error style/msg    
        this.setRecordErrorMessage(rowNum, null);
      }
    }
    this.deselectAllRecords();
    this.view.refreshChildViews();
    if (toRemove.length > 0) {
      this.data.handleUpdate('remove', toRemove);
      this.updateRowCountDisplay();
    }
    this.view.standardWindow.cleanUpAutoSaveProperties();
    this.view.updateTabTitle();
    this.view.toolBar.updateButtonState(true);
  },
  
  discardEdits: function(rowNum, colNum, dontHideEditor, editCompletionEvent, preventConfirm){
    var localArguments = arguments;
    var me = this, record = this.getRecord(rowNum);
    
    if (!preventConfirm && 
    (record._new || this.getEditForm().hasChanged || this.rowHasErrors(rowNum))) {
      isc.ask(OB.I18N.getLabel('OBUIAPP_ConfirmCancelEdit'), function(value){
        if (value) {
        
          me.Super('discardEdits', localArguments);
          
          // remove the record if new
          if (record._new) {
            me.data.handleUpdate('remove', [{
              id: record.id
            }]);
            me.updateRowCountDisplay();
            me.view.refreshChildViews();
          } else {
            // remove the error style/msg    
            me.setRecordErrorMessage(rowNum, null);
          }
          
          me.view.standardWindow.cleanUpAutoSaveProperties();
          
          // update after removing the error msg
          me.view.updateTabTitle();
          me.view.toolBar.updateButtonState(true);
        }
      });
    } else {
      me.Super('discardEdits', localArguments);
      
      // remove the record if new
      if (record && record._new) {
        me.data.handleUpdate('remove', [{
          id: record.id
        }]);
        me.updateRowCountDisplay();
      } else {
        // remove the error style/msg    
        me.setRecordErrorMessage(rowNum, null);
      }
      
      this.view.standardWindow.cleanUpAutoSaveProperties();
       
      // update after removing the error msg
      this.view.updateTabTitle();
      this.view.toolBar.updateButtonState(true);
    }
  },
  
  saveEdits: function (editCompletionEvent, callback, rowNum, colNum, validateOnly) {
    var ret = this.Super('saveEdits', arguments);
    // save was not done, because there were no changes probably
    if (!ret) {
      this.view.standardWindow.cleanUpAutoSaveProperties();
      this.view.updateTabTitle();
      this.view.toolBar.updateButtonState(true);
    }
    return ret;
  },
  
  // saveEditedValues: when saving, first check if a FIC call needs to be done to update to the 
  // latest values. This can happen when the focus is in a field and the save action is
  // done, at that point first try to force a fic call (handleItemChange) and if that
  // indeed happens stop the saveEdit until the fic returns
  saveEditedValues: function (rowNum, colNum, newValues, oldValues, 
                             editValuesID, editCompletionEvent, saveCallback, ficCallDone) {
    if (!rowNum && rowNum !== 0) {
      rowNum = this.getEditRow();
    }
    if (!colNum && colNum !== 0) {
      colNum = this.getEditCol();
    }
    
    // nothing changed just fire the calback and bail
    if (!ficCallDone && this.getEditForm() && !this.getEditForm().hasChanged && !this.getEditForm().isNew) {
      if (saveCallback) {
          this.fireCallback(saveCallback, 
                            "rowNum,colNum,editCompletionEvent,success", 
                            [rowNum,colNum,editCompletionEvent,success]
          );
      }
      return true;
    }
    
    if (ficCallDone) {
      // reset the new values as this can have changed because of a fic call
      newValues = this.getEditValues(editValuesID);
    } else {
      var editForm = this.getEditForm(), focusItem = editForm.getFocusItem();
      if (focusItem) {
        focusItem.updateValue();
        editForm.handleItemChange(focusItem);
        if (editForm.inFicCall) {
          // use editValues object as the edit form will be re-used for a next row
          var editValues = this.getEditValues(editValuesID);
          editValues.actionAfterFicReturn = {
            target: this,
            method: this.saveEditedValues,
            parameters: [rowNum, colNum, newValues, oldValues, editValuesID, editCompletionEvent, saveCallback, true]
          };
          return;
        }
      }
    }
    this.Super('saveEditedValues', [rowNum, colNum, newValues, oldValues, editValuesID, editCompletionEvent, saveCallback]);
  },

  autoSave: function(){
    this.storeUpdatedEditorValue();
    this.endEditing();
  },
  
  // is called when clicking a header
  hideInlineEditor: function(){
    var rowNum = this.getEditRow(), record = this.getRecord(rowNum);
    
    // clear the errors so that they don't show up at the next row
    if (this.getEditForm()) {
      this.getEditForm().clearErrors();
    }
    
    this.view.messageBar.hide();
    if (record && record.editColumnLayout) {
      record.editColumnLayout.showEditOpen();
    } else if (this.getEditForm().getValues().editColumnLayout) {
      this.getEditForm().getValues().editColumnLayout.showEditOpen();
    }
    return this.Super('hideInlineEditor', arguments);
  },
  
  getEditDisplayValue: function(rowNum, colNum, record){
    // somehow this extra call is needed to not restore
    // the old value when the new value is null
    this.storeUpdatedEditorValue();
    return this.Super('getEditDisplayValue', arguments);
  },
  
  showInlineEditor: function(rowNum, colNum, newCell, newRow, suppressFocus){
    if (newRow) {
      if (this.getEditForm()) {
        this.getEditForm().clearErrors();
      }
      // if the focus does not get suppressed then the clicked field will receive focus
      // and won't be disabled so the user can already start typing      
      suppressFocus = true; 
    }

    var ret = this.Super('showInlineEditor', [rowNum, colNum, newCell, newRow, suppressFocus]);
    if (!newRow) {
      return ret;
    }
    
    if (this.getEditForm() && newRow) {
      // set the field to focus on after returning from the fic
      this.getEditForm().setFocusItem(this.getField(colNum).name);
    }
    
    var record = this.getRecord(rowNum);
    
    this.view.isEditingGrid = true;
    
    record[this.recordBaseStyleProperty] = this.baseStyleEdit;
    
    // also called in case of new
    var form = this.getEditForm();
    
    // also make sure that the new indicator is send to the server
    if (record._new) {
      form.setValue('_new', true);
    }
    
    form.doEditRecordActions(false, record._new && !record._editedBefore);
    record._editedBefore = true;
    
    // must be done after doEditRecordActions    
    if (this.rowHasErrors(rowNum)) {
      this.getEditForm().setErrors(this.getRowValidationErrors(rowNum));
      this.view.standardWindow.setDirtyEditForm(form);
    }
    
    if (record && record.editColumnLayout) {
      record.editColumnLayout.showSaveCancel();
    }
    
    this.view.messageBar.hide();
    
    return ret;
  },
  
  rowEditorExit: function(editCompletionEvent, record, newValues, rowNum){
    isc.Log.logDebug('rowEditorExit ' + this.getEditRow(), 'OB');
    if (!this.rowHasErrors(rowNum)) {
      record[this.recordBaseStyleProperty] = null;
    }
    
    if (record && record.editColumnLayout) {
      isc.Log.logDebug('hideInlineEditor has record and editColumnLayout', 'OB');
      record.editColumnLayout.showEditOpen();
    } else if (this.currentEditColumnLayout) {
      this.currentEditColumnLayout.showEditOpen();
    } else {
      isc.Log.logDebug('hideInlineEditor has NO record and editColumnLayout', 'OB');
    }
    this.view.isEditingGrid = false;
    this.refreshRow(rowNum);
  },
  
  closeAnyOpenEditor: function(){
    // close any editors we may have
    if (this.getEditRow() || this.getEditRow() === 0) {
      this.endEditing();
    }
  },
  
  validateField: function(field, validators, value, record, options){
    // Smartclient passes in the grid field, use the editform field
    // as it contains the latest valuemap
    var editField = this.getEditForm().getField(field.name) || field;
    var ret = this.Super('validateField', [editField, validators, value, record, options]);
    return ret;
  },
  
  refreshEditRow: function() {
    var editRow = this.view.viewGrid.getEditRow();
    if (editRow || editRow === 0) {
      // don't refresh the frozen fields, this give strange
      // styling issues in chrome
      for (var i = 0; i < this.view.viewGrid.fields.length; i++) {
        if (!this.fieldIsFrozen(i)) {
          this.view.viewGrid.refreshCell(editRow, i, true);
        }
      }
    }
  },
  
  // the form gets recreated many times, maintain the already read valuemap
  getEditorValueMap: function(field, values){
    var editRow = this.getEditRow(), editValues = this.getEditValues(editRow);
    // valuemap is set in the processcolumnvalues of the ob-view-form.js
    if (editValues && editValues[field.name + '._valueMap']) {
      return editValues[field.name + '._valueMap'];
    }
    
    if (this.getEditForm() && this.getEditForm().getField(field.name)) {
      var liveField = this.getEditForm().getField(field.name);
      if (liveField.valueMap) {
        return liveField.valueMap;
      }
    }
    
    return this.Super('getEditorValueMap', arguments);
  },
  
  cellHasErrors: function(rowNum, fieldID){
    var record = this.getRecord(rowNum);
    var itemName;
    if (this.Super('cellHasErrors', arguments)) {
      return true;
    }
    if (this.getEditRow() === rowNum) {
      itemName = this.getEditorName(rowNum, fieldID);
      
      if (this.getEditForm().hasFieldErrors(itemName)) {
        return true;
      }
      // sometimes the error is there but the error message is null
      if (this.getEditForm().getErrors().hasOwnProperty(itemName)) {
        return true;
      }
    }
    return false;
  },
  
  getCellErrors: function(rowNum, fieldName){
    var itemName;
    var ret = this.Super('getCellErrors', arguments);
    if (this.getEditRow() === rowNum) {
      return this.getEditForm().getFieldErrors(itemName);
    }
    return ret;
  },
  
  rowHasErrors: function(rowNum, colNum){
    if (this.Super('rowHasErrors', arguments)) {
      return true;
    }
    if (this.getEditRow() === rowNum && this.getEditForm().hasErrors()) {
      return true;
    }
    var record = this.getRecord(rowNum);
    if (record) {
      return record[isc.OBViewGrid.ERROR_MESSAGE_PROP];
    }
    return false;
  },
  
  // we are being reshown, get new values for the combos
  visibilityChanged: function(visible){
    if (visible && this.getEditRow()) {
      this.getEditForm().doChangeFICCall();
    }
    if (!this.view.isVisible() && this.hasErrors()) {
      isc.warn(OB.I18N.getLabel('OBUIAPP_TabWithErrors', [this.view.tabTitle]));
    }
  },
  
  isWritable: function(record){
    return !record._readOnly;
  },
  
  setRecordErrorMessage: function(rowNum, msg){
    var record = this.getRecord(rowNum);
    record[isc.OBViewGrid.ERROR_MESSAGE_PROP] = msg;
    if (msg) {
      record[this.recordBaseStyleProperty] = this.recordStyleError;
    } else {
      record[this.recordBaseStyleProperty] = null;
    }
    if (record.editColumnLayout) {
      record.editColumnLayout.editButton.setErrorState(msg);
      record.editColumnLayout.editButton.setErrorMessage(msg);
    }
    this.refreshRow(rowNum);
  },
  
  setRecordFieldErrorMessages: function(rowNum, errors){
    var record = this.getRecord(rowNum);
    if (record.editColumnLayout) {
      record.editColumnLayout.editButton.setErrorState(errors);
      record.editColumnLayout.editButton.setErrorMessage(OB.I18N.getLabel('OBUIAPP_ErrorInFields'));
    }
    this.setRowErrors(rowNum, errors);
    if (errors) {
      record[this.recordBaseStyleProperty] = this.recordStyleError;
    } else {
      record[this.recordBaseStyleProperty] = null;
    }
    
    if (this.frozenBody) {
      this.frozenBody.markForRedraw();
    }
    this.body.markForRedraw();
  },
  
  // overridden to handle the case that the rowNum is in fact 
  // an edit state id
  getRecord: function(rowNum){
    if (!isc.isA.Number(rowNum)) {
      // an edit id
      rowNum = this.getEditSessionRowNum(rowNum);
      return this.Super('getRecord', [rowNum]);
    }
    return this.Super('getRecord', arguments);
  },
  
  // +++++++++++++++++ functions for the edit-link column +++++++++++++++++
  
  createRecordComponent: function(record, colNum){
    var layout = this.Super('createRecordComponent', arguments), rowNum;
    if (layout) {
      return layout;
    }
    if (this.isEditLinkColumn(colNum)) {
      rowNum = this.getRecordIndex(record);
      layout = isc.OBGridButtonsComponent.create({
        record: record,
        grid: this,
        rowNum: rowNum
      });
      layout.editButton.setErrorState(this.rowHasErrors(rowNum));
      layout.editButton.setErrorMessage(record[isc.OBViewGrid.ERROR_MESSAGE_PROP]);
      layout.showEditOpen();
      record.editColumnLayout = layout;
    }
    return layout;
  },
  
  updateRecordComponent: function(record, colNum, component, recordChanged){
    var superComponent = this.Super('updateRecordComponent', arguments);
    if (superComponent) {
      return superComponent;
    }
    if (this.isEditLinkColumn(colNum)) {
      // clear the previous record pointer
      if (recordChanged && component.record.editColumnLayout === component) {
        component.record.editColumnLayout = null;
      }
      component.record = record;
      record.editColumnLayout = component;
      component.rowNum = this.getRecordIndex(record);
      component.editButton.setErrorState(this.rowHasErrors(component.rowNum));
      component.editButton.setErrorMessage(record[isc.OBViewGrid.ERROR_MESSAGE_PROP]);
      component.showEditOpen();
      return component;
    }
    return null;
  },
  
  isEditLinkColumn: function(colNum){
    var fieldName = this.getFieldName(colNum);
    return (fieldName === isc.OBViewGrid.EDIT_LINK_FIELD_NAME);
  },
  
  reorderField: function(fieldNum, moveToPosition){
    var res = this.Super('reorderField', arguments);
    this.view.standardWindow.storeViewState();
    return res;
  },
  
  hideField: function(field, suppressRelayout){
    var res = this.Super('hideField', arguments);
    this.view.standardWindow.storeViewState();
    return res;
  },
  
  showField: function(field, suppressRelayout){
    var res = this.Super('showField', arguments);
    this.view.standardWindow.storeViewState();
    return res;
  },
  
  resizeField: function(fieldNum, newWidth, storeWidth){
    var res = this.Super('resizeField', arguments);
    this.view.standardWindow.storeViewState();
    return res;
  }
  
});

// = OBGridToolStrip =
// The component which is inside of OBGridButtonsComponent
isc.ClassFactory.defineClass('OBGridToolStrip', isc.ToolStrip);

isc.OBGridToolStrip.addProperties({});

// = OBGridToolStripIcon =
// The icons which are inside of OBGridToolStrip
isc.ClassFactory.defineClass('OBGridToolStripIcon', isc.ImgButton);

isc.OBGridToolStripIcon.addProperties({
  buttonType: null, /* This could be: edit - form - cancel - save */
  initWidget: function(){
    if (this.initWidgetStyle) {
      this.initWidgetStyle();
    }
    this.Super('initWidget', arguments);
  }
});

// = OBGridToolStripSeparator =
// The separator between icons of OBGridToolStrip
isc.ClassFactory.defineClass('OBGridToolStripSeparator', isc.Img);

isc.OBGridToolStripSeparator.addProperties({});

// = OBGridButtonsComponent =
// The component which is used to create the contents of the
// edit open column in the grid
isc.ClassFactory.defineClass('OBGridButtonsComponent', isc.HLayout);

isc.OBGridButtonsComponent.addProperties({
  OBGridToolStrip: null,
  saveCancelLayout: null,
  
  // the grid to which this component belongs
  grid: null,
  
  rowNum: null,
  
  // the record to which this component belongs
  record: null,
  
  initWidget: function(){
    var me = this, formButton, cancelButton, saveButton;
    
    this.progressIcon = isc.Img.create(this.grid.progressIconDefaults);
    
    this.editButton = isc.OBGridToolStripIcon.create({
      buttonType: 'edit',
      originalPrompt: OB.I18N.getLabel('OBUIAPP_GridEditButtonPrompt'),
      prompt: OB.I18N.getLabel('OBUIAPP_GridEditButtonPrompt'),
      action: function(){
        var actionObject = {
          target: me,
          method: me.doEdit,
          parameters: null
        };
        me.grid.view.standardWindow.doActionAfterAutoSave(actionObject, true);
      },
      
      setErrorMessage: function(msg){
        if (msg) {
          this.prompt = msg + '<br><br>' + this.originalPrompt;
        } else {
          this.prompt = this.originalPrompt;
        }
      },
      
      showable: function(){
        return !me.grid.view.readOnly && !me.record._readOnly;
      },
      
      show: function(){
        if (!this.showable()) {
          return;
        }
        return this.Super('show', arguments);
      }
    });
    
    formButton = isc.OBGridToolStripIcon.create({
      buttonType: 'form',
      prompt: OB.I18N.getLabel('OBUIAPP_GridFormButtonPrompt'),
      action: function(){
        var actionObject = {
          target: me,
          method: me.doOpen,
          parameters: null
        };
        me.grid.view.standardWindow.doActionAfterAutoSave(actionObject, true);
      }
    });
    
    cancelButton = isc.OBGridToolStripIcon.create({
      buttonType: 'cancel',
      prompt: OB.I18N.getLabel('OBUIAPP_GridCancelButtonPrompt'),
      action: function(){
        me.doCancel();
      }
    });
    
    saveButton = isc.OBGridToolStripIcon.create({
      buttonType: 'save',
      prompt: OB.I18N.getLabel('OBUIAPP_GridSaveButtonPrompt'),
      action: function(){
        me.doSave();
      }
    });
    
    this.buttonSeparator1 = isc.OBGridToolStripSeparator.create({});
    
    if (me.grid.view.readOnly) {
      this.buttonSeparator1.visibility = 'hidden';
    }
    
    buttonSeparator2 = isc.OBGridToolStripSeparator.create({});
    
    this.OBGridToolStrip = isc.OBGridToolStrip.create({
      members: [formButton, this.buttonSeparator1, this.editButton, cancelButton, buttonSeparator2, saveButton]
    });
    
    this.addMember(this.progressIcon);
    this.addMember(this.OBGridToolStrip);
    this.OBGridToolStrip.hideMember(5);
    this.OBGridToolStrip.hideMember(4);
    this.OBGridToolStrip.hideMember(3);
  },
  
  toggleProgressIcon: function(toggle){
    if (toggle) {
      this.progressIcon.show();
      this.OBGridToolStrip.hide();
    } else {
      this.progressIcon.hide();
      this.OBGridToolStrip.show();
    }
  },
  
  showEditOpen: function(){
    this.OBGridToolStrip.hideMember(5);
    this.OBGridToolStrip.hideMember(4);
    this.OBGridToolStrip.hideMember(3);
    this.OBGridToolStrip.showMember(0);
    if (this.editButton.showable()) {
      this.OBGridToolStrip.showMember(1);
      this.OBGridToolStrip.showMember(2);
    } else {
      this.OBGridToolStrip.hideMember(1);
      this.OBGridToolStrip.hideMember(2);
    }
    this.grid.currentEditColumnLayout = null;
  },
  
  showSaveCancel: function(){
    this.OBGridToolStrip.hideMember(2);
    this.OBGridToolStrip.hideMember(1);
    this.OBGridToolStrip.hideMember(0);
    this.OBGridToolStrip.showMember(3);
    this.OBGridToolStrip.showMember(4);
    this.OBGridToolStrip.showMember(5);
    this.grid.currentEditColumnLayout = this;
  },
  
  doEdit: function(){
    this.showSaveCancel();
    this.grid.selectSingleRecord(this.record);
    this.grid.startEditing(this.rowNum);
  },
  
  doOpen: function(){
    this.grid.endEditing();
    this.grid.view.editRecord(this.record);
  },
  
  doSave: function(){
    // note change back to editOpen is done in the editComplete event of the
    // grid itself
    this.grid.endEditing();
  },
  
  doCancel: function(){
    this.grid.cancelEditing();
  }  
});
