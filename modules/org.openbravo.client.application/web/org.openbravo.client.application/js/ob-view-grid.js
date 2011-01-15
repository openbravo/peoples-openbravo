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
  ARROW_DOWN_KEY_NAME: 'Arrow_Down'
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
    // frozen is much nicer, but check out this forum discussion:
    // http://forums.smartclient.com/showthread.php?p=57581 
    //frozen: true,
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
  
  // internal sc grid property, see the ListGrid source code
  preserveEditsOnSetData: false,
  
  // enabling this results in a slower user interaction
  // it is better to allow fast grid interaction and if an error occurs
  // dismiss any new records being edited and go back to the edit row 
  // which causes the error
  waitForSave: false,
  stopOnErrors: false,
  confirmDiscardEdits: true,
  
  canMultiSort: false,
  
  emptyMessage: OB.I18N.getLabel('OBUIAPP_NoDataInGrid'),
  discardEditsSaveButtonTitle: OB.I18N.getLabel('UINAVBA_Save'),
  
  quickDrawAheadRatio: 6.0,
  drawAheadRatio: 4.0,
  drawAllMaxCells: 500,
  
  // keeps track if we are in objectSelectionMode or in toggleSelectionMode
  // objectSelectionMode = singleRecordSelection === true  
  singleRecordSelection: false,
  
  dataProperties: {
    useClientFiltering: false,
    useClientSorting: false,
    
    transformData: function(newData, dsResponse){
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
      return this.Super('transformData', arguments);
    }
  },
  
  currentEditColumnLayout: null,
  
  refreshFields: function(){
    this.setFields(this.completeFields.duplicate());
  },
  
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
      record.editColumnLayout = layout;
    }
    return layout;
  },
  
  isEditLinkColumn: function(colNum){
    var fieldName = this.getFieldName(colNum);
    return (fieldName === isc.OBViewGrid.EDIT_LINK_FIELD_NAME);
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
      return component;
    }
    return null;
  },
  
  initWidget: function(){
    var editFormProps = {}, thisGrid = this, localEditLinkField;
    if (this.editGrid) {
      // add the edit pencil in the beginning
      localEditLinkField = isc.addProperties({}, this.editLinkFieldProperties);
      localEditLinkField.width = this.editLinkColumnWidth;
      this.fields.unshift(localEditLinkField);
    }
    
    // added for showing counts in the filtereditor row
    this.checkboxFieldDefaults = isc.addProperties(this.checkboxFieldDefaults, {
      canFilter: true,
      // frozen is much nicer, but check out this forum discussion:
      // http://forums.smartclient.com/showthread.php?p=57581 
      //frozen: true,
      canFreeze: true,
      filterEditorProperties: {
        textAlign: 'center'
      },
      filterEditorType: 'StaticTextItem'
    });
    
    this.Super('initWidget', arguments);
  },
  
  show: function(){
    var ret = this.Super('show', arguments);
    this.view.toolBar.setLeftMemberDisabled(isc.OBToolbar.TYPE_REFRESH, false);
    this.view.toolBar.setLeftMemberDisabled(isc.OBToolbar.TYPE_SAVE, true);
    // enable the delete if there are records selected
    if (this.getSelection() && this.getSelection().length > 0) {
      this.view.toolBar.setLeftMemberDisabled(isc.OBToolbar.TYPE_DELETE, this.view.readOnly || false);
    }
    return ret;
  },
  
  headerClick: function(fieldNum, header){
    var field = this.fields[fieldNum];
    if (this.isCheckboxField(field) && this.singleRecordSelection) {
      this.deselectAllRecords();
      this.singleRecordSelection = false;
    }
    return this.Super('headerClick', arguments);
  },
  
  deselectAllRecords: function(){
    this.allSelected = false;
    return this.Super('deselectAllRecords', arguments);
  },
  
  selectAllRecords: function(){
    this.allSelected = true;
    return this.Super('selectAllRecords', arguments);
  },
  
  updateRowCountDisplay: function(){
    var newValue = '';
    if (this.data.getLength() > this.dataPageSize) {
      newValue = '>' + this.dataPageSize;
    } else if (this.data.getLength() === 0) {
      newValue = '&nbsp;';
    } else {
      newValue = '' + this.data.getLength();
    }
    if (this.filterEditor) {
      this.filterEditor.getEditForm().setValue(isc.OBViewGrid.EDIT_LINK_FIELD_NAME, newValue);
    }
  },
  
  refreshContents: function(callback){
  
    // do not refresh if the parent is not selected and we have no data anyway
    if (this.view.parentProperty && this.data && this.data.getLength && this.data.getLength() === 0) {
      selectedValues = this.view.parentView.viewGrid.getSelectedRecords();
      if (selectedValues.length === 0) {
        this.emptyMessage = OB.I18N.getLabel('OBUIAPP_NoParentSelected');
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
  // - if the user goes directly to a tab (from a link in another window) then
  //   opening the relevant record is done here or if no record is passed grid
  //   mode is opened
  // - if there is only one record then select it directly
  dataArrived: function(startRow, endRow){
    var record, ret = this.Super('dataArrived', arguments);
    this.updateRowCountDisplay();
    this.updateSelectedCountDisplay();
    
    if (this.targetOpenGrid) {
      // direct link from other window but without a record id
      // so just show grid mode
      // don't need to do anything here
      delete this.targetOpenGrid;
    } else if (this.targetRecordId) {
      // direct link from other tab to a specific record
      this.delayedHandleTargetRecord(startRow, endRow);
    } else if (this.view.shouldOpenDefaultEditMode()) {
      // ui-pattern: single record/edit mode
      this.view.openDefaultEditView(this.getRecord(startRow));
    } else if (this.data && this.data.getLength() === 1) {
      // one record select it directly
      record = this.getRecord(0);
      // this select method prevents state changing if the record
      // was already selected
      this.doSelectSingleRecord(record);
    }
    
    return ret;
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
      
      this.doSelectSingleRecord(gridRecord);
      this.scrollRecordIntoView(recordIndex, true);
      
      // go to the children
      this.view.openDirectChildTab();
    } else {
      // wait a bit longer til the body is drawn
      this.delayCall('delayedHandleTargetRecord', [startRow, endRow], 200, this);
    }
  },
  
  selectRecordById: function(id){
    var recordIndex, gridRecord = this.data.find(OB.Constants.ID, id);
    
    // no grid record found, stop here
    if (!gridRecord) {
      return;
    }
    recordIndex = this.getRecordIndex(gridRecord);
    this.scrollRecordIntoView(recordIndex, true);
    this.doSelectSingleRecord(gridRecord);
  },
  
  // overridden to prevent extra firing of selection updated event  
  selectSingleRecord: function(record){
    this.deselectAllRecords();
    this.selectRecord(record);
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
    criteria = criteria || {};
    
    criteria = OB.Utilities._getTabInfoRequestProperties(this.view, criteria);
    
    var isFiltering = criteria.length > 0;
    var filterValues = [];
    var index = 0, selectedValues;
    var fkFieldsLength = this.foreignKeyFieldNames.getLength(), filterValue = null;
    
    if (this.targetRecordId) {
      criteria._targetRecordId = this.targetRecordId;
    }
    
    if (this.view.parentProperty) {
      selectedValues = this.view.parentView.viewGrid.getSelectedRecords();
      if (selectedValues.length === 0) {
        criteria[this.view.parentProperty] = '-1';
        this.emptyMessage = OB.I18N.getLabel('OBUIAPP_NoParentSelected');
      } else if (selectedValues.length > 1) {
        criteria[this.view.parentProperty] = '-1';
        this.emptyMessage = OB.I18N.getLabel('OBUIAPP_MultipleParentsSelected');
      } else {
        this.emptyMessage = OB.I18N.getLabel('OBUIAPP_NoDataInGrid');
        criteria[this.view.parentProperty] = selectedValues[0][OB.Constants.ID];
      }
    }
    
    // now repair the filtering on foreign keys to use the identifier
    //    for (index = 0; index < fkFieldsLength; index++) {
    //      if (criteria[this.foreignKeyFieldNames[index]]) {
    //        filterValue = criteria[this.foreignKeyFieldNames[index]];
    //        delete criteria[this.foreignKeyFieldNames[index]];
    //        criteria[this.foreignKeyFieldNames[index] + '.' +
    //        OB.Constants.IDENTIFIER] = filterValue;
    //      }
    //    }
    
    // prevent the count operation
    criteria[isc.OBViewGrid.NO_COUNT_PARAMETER] = 'true';
    
    if (this.orderByClause) {
      criteria[OB.Constants.ORDERBY_PARAMETER] = this.orderByClause;
    }
    
    if (this.whereClause) {
      criteria[OB.Constants.WHERE_PARAMETER] = this.whereClause;
    }
    
    // add all the new session properties context info to the criteria
    isc.addProperties(criteria, this.view.getContextInfo(true, false));
    
    return criteria;
  },
  
  // determine which field can be autoexpanded to use extra space  
  getAutoFitExpandField: function(){
    for (var i = 0; i < this.fields.length; i++) {
      var field = this.fields[i];
      if (field.autoExpand) {
        return field;
      }
    }
    return this.Super('getAutoFitExpandField', arguments);
  },
  
  recordClick: function(viewer, record, recordNum, field, fieldNum, value, rawValue){
    var me = this, EH = isc.EventHandler;
    if (EH.isMouseEvent(EH.getEventType())) {
      record._dblClickWaiting = true;
      isc.Timer.setTimeout(function(){
        // if no double click happened then do the single click
        if (record._dblClickWaiting) {
          record._dblClickWaiting = false;
          me.handleRecordSelection(viewer, record, recordNum, field, fieldNum, value, rawValue, false);
        }
      }, OB.Constants.DBL_CLICK_DELAY);
    } else {
      me.handleRecordSelection(viewer, record, recordNum, field, fieldNum, value, rawValue, false);
    }
  },
  
  recordDoubleClick: function(viewer, record, recordNum, field, fieldNum, value, rawValue){
    record._dblClickWaiting = false;
    this.view.editRecord(record);
  },
  
  //+++++++++++++++++++++++++++++ Context menu on record click +++++++++++++++++++++++
  
  makeCellContextItems: function(record, rowNum, colNum){
    var sourceWindow = this.view.standardWindow.windowId;
    var menuItems = [];
    var field = this.getField(colNum);
    var grid = this;
    if (this.canEdit) {
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
  
  //+++++++++++++++++++++++++++++ Record Selection Handling +++++++++++++++++++++++
  
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
  
  selectionUpdated: function(record, recordList){
  
    this.stopHover();
    this.updateSelectedCountDisplay();
    
    // nothing changed, go away then, happens when saving
    if (this.singleRecordSelection && this.view.lastRecordSelected && this.getSelection().length === 1 && this.getSelection()[0].id === this.view.lastRecordSelected.id) {
      // instance may have been updated, update the instance in the view
      this.view.lastRecordSelected = this.getSelection()[0];
      return;
    }
  
    isc.Log.logDebug('Selection updated ' + record, 'OB');

    this.view.recordSelected();

    // enable/disable the delete if there are records selected
    this.view.toolBar.setLeftMemberDisabled(isc.OBToolbar.TYPE_DELETE, (!this.getSelection() || this.getSelection().length === 0));
  },
  
  selectOnMouseDown: function(record, recordNum, fieldNum){
  
    // don't change selection on right mouse down
    var EH = isc.EventHandler, eventType;
    if (EH.rightButtonDown()) {
      return;
    }
    
    var previousSingleRecordSelection = this.singleRecordSelection;
    var currentSelectedRecordSelected = (this.getSelectedRecord() === record);
    if (this.getCheckboxFieldPosition() === fieldNum) {
      if (this.singleRecordSelection) {
        this.deselectAllRecords();
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
      
      this.markForRedraw('Selection checkboxes need to be redrawn');
    } else {
      // do some checking, the handleRecordSelection should only be called
      // in case of keyboard navigation and not for real mouse clicks,
      // these are handled by the recordClick and recordDoubleClick methods
      // if this method here would also handle mouseclicks then the doubleClick
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
    
    // don't change selection on right mouse down
    if (EH.rightButtonDown()) {
      return;
    }
    
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
        this.deselectAllRecords();
      }
      // click in checkbox field is done by standard logic
      this.singleRecordSelection = false;
    } else if (isc.EventHandler.ctrlKeyDown()) {
      // only do something if record clicked and not from selectOnMouseDown
      // this method got called twice from one clicK: through recordClick and
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
    // note that when navigating with the arrow key that at a certain 2 are selected
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
        // record cannot be selected but we want the space allocated for the checkbox anyway.
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
      if (record[this.recordEnabledProperty] === false) {
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
  
  //+++++++++++++++++ functions for the edit-open column handling +++++++++++++++++
  
  editComplete: function(rowNum, colNum, newValues, oldValues, editCompletionEvent, dsResponse){
    // during save the record looses the link to the editColumnLayout, restore it
    var record = this.getRecord(rowNum);
    if (oldValues.editColumnLayout && !record.editColumnLayout) {
      record.editColumnLayout = oldValues.editColumnLayout;
    }
    if (record.editColumnLayout) {
      record.editColumnLayout.showEditOpen();
    }
    return this.Super('editComplete', arguments);
  },
  
  discardEdits: function(rowNum, colNum, dontHideEditor, editCompletionEvent){
    var localArguments = arguments;
    var me = this;
    if (this.getEditForm().valuesHaveChanged()) {
      isc.ask(OB.I18N.getLabel('OBUIAPP_ConfirmCancelEdit'), function(value){
        if (value) {
          me.Super('discardEdits', localArguments);
        }
      });
    } else {
      me.Super('discardEdits', localArguments);
    }
  },
  
  showInlineEditor: function(rowNum, colNum, newCell, newRow, suppressFocus){
    if (this.baseStyleEdit) {
      this.baseStyle = this.baseStyleEdit;
    }
    var result = this.Super('showInlineEditor', arguments);
    
    var record = this.getRecord(rowNum);
    if (record && record.editColumnLayout) {
      record.editColumnLayout.showSaveCancel();
    }
    
    return result;
  },
  
  hideInlineEditor: function(){
    isc.Log.logDebug('hideInlineEditor ' + this.getEditRow(), 'OB');
    if (this.baseStyleView) {
      this.baseStyle = this.baseStyleView;
    }
    var rowNum = this.getEditRow();
    var record = this.getRecord(rowNum);
    var editColumnLayout = record.editColumnLayout;
    if (record && record.editColumnLayout) {
      isc.Log.logDebug('hideInlineEditor has record and editColumnLayout', 'OB');
      record.editColumnLayout.showEditOpen();
    } else if (this.currentEditColumnLayout) {
      this.currentEditColumnLayout.showEditOpen();
    } else {
      isc.Log.logDebug('hideInlineEditor has NO record and editColumnLayout', 'OB');
    }
    return this.Super('hideInlineEditor', arguments);
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
    var me = this;
    
    this.progressIcon = isc.Img.create(this.grid.progressIconDefaults);
    
    editIcon = isc.OBGridToolStripIcon.create({
      buttonType: 'edit',
      action: function(){
        me.doEdit();
      }
    });
    
    formIcon = isc.OBGridToolStripIcon.create({
      buttonType: 'form',
      action: function(){
        me.doOpen();
      }
    });
    
    cancelIcon = isc.OBGridToolStripIcon.create({
      buttonType: 'cancel',
      action: function(){
        me.doCancel();
      }
    });
    
    saveIcon = isc.OBGridToolStripIcon.create({
      buttonType: 'save',
      action: function(){
        me.doSave();
      }
    });
    
    buttonSeparator1 = isc.OBGridToolStripSeparator.create({});
    
    buttonSeparator2 = isc.OBGridToolStripSeparator.create({});
    
    this.OBGridToolStrip = isc.OBGridToolStrip.create({
      members: [formIcon, buttonSeparator1, editIcon, cancelIcon, buttonSeparator2, saveIcon]
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
    this.OBGridToolStrip.showMember(2);
    this.OBGridToolStrip.showMember(1);
    this.OBGridToolStrip.showMember(0);
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
    this.grid.view.editRecord(this.record);
  },
  
  doSave: function(){
    // note change back to editOpen is done in the editComplete event of the grid
    // itself
    this.grid.saveEdits();
  },
  
  doCancel: function(){
    this.grid.cancelEditing();
  }
  
});
