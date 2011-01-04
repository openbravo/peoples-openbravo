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
isc.ClassFactory.defineClass('OBViewGrid', isc.OBGrid);

isc.OBViewGrid.addClassProperties({
  EDIT_LINK_FIELD_NAME: '_editLink',
  NO_COUNT_PARAMETER: '_noCount', // prevent the count operation on the server
  // note following 2 values should be the same
  // ListGrid._$ArrowUp and ListGrid._$ArrowDown
  ARROW_UP_KEY_NAME: 'Arrow_Up',
  ARROW_DOWN_KEY_NAME: 'Arrow_Down'
});

/*
 * Todos: - edit button klik selects one record, deselects others, opens edit
 * view and refreshes child windows. - a grid which gets opened always selects
 * the first record - double click on a record does the same as the edit button -
 * single click opens bubble: edit in grid, use as filter, open on tab
 */
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
    canFreeze: false,
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
  
  waitForSave: true,
  stopOnErrors: true,
  confirmDiscardEdits: true,
  
  canMultiSort: false,
  
  emptyMessage: OB.I18N.getLabel('OBUIAPP_NoDataInGrid'),
  discardEditsSaveButtonTitle: OB.I18N.getLabel('UINAVBA_Save'),
  
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
    var layout = this.Super('createRecordComponent',arguments), rowNum;
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
      filterEditorProperties: {
        textAlign: 'center'
      },
      filterEditorType: 'StaticTextItem'
    });
    
    this.filterEditorProperties = {
      isCheckboxField: function(){
        return false;
      },
      actionButtonProperties: {
        visibility: 'hidden'
      }
    };
    
    this.Super('initWidget', arguments);
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
    var context = {
      showPrompt: false,
      textMatchStyle: this.autoFetchTextMatchStyle
    };
    this.filterData(this.getCriteria(), callback, context);
  },
  
  dataArrived: function(startRow, endRow){
    var record, ret = this.Super('dataArrived', arguments);
    this.updateRowCountDisplay();
    this.updateSelectedCountDisplay();
    
    if (this.targetRecordId) {
      this.delayedHandleTargetRecord(startRow, endRow);
    } else if (this.view.shouldOpenDefaultEditMode()) {
      this.view.openDefaultEditView(this.getRecord(startRow));   
    }
    
    return ret;
  },
  
  // handle the target record when the body has been drawn
  delayedHandleTargetRecord: function(startRow, endRow){
    var rowTop, recordIndex, i, data = this.data;
    if (!this.targetRecordId) {
      return;
    }
    if (this.body) {
      var gridRecord = data.find('id', this.targetRecordId);
      
      // no grid record found, stop here
      if (!gridRecord) {
        return;
      }
      recordIndex = this.getRecordIndex(gridRecord);
      
      this.targetRecordId = null;
      if (data.criteria) {
        data.criteria._targetRecordId = null;
      }
      
      for (i = 0; i < startRow; i++) {
        if (Array.isLoading(data.localData[i])) {
          data.localData[i] = null;
        }
      }
      
      this.scrollRecordIntoView(recordIndex, false);
      if (this.view.defaultEditMode) {
        this.view.editRecord(gridRecord);   
      } else {
        this.doSelectSingleRecord(gridRecord);
      }
      
      isc.Page.waitFor(this, 'delayedHandleTargetRecord', {
        method: this.view.openDirectChildTab(),
        args: [],
        target: this.view
      });
    } else {
      // wait a bit longer
      this.delayCall('delayedHandleTargetRecord', [startRow, endRow], 500, this);
    }
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
  
  getInitialCriteria: function() {
    var criteria = this.Super('getInitialCriteria', arguments);
    
    return this.convertCriteria(criteria);
  },
  
  getCriteria: function() {
    var criteria = this.Super('getCriteria', arguments);
    if (!criteria) {
      criteria = {};
    }
    criteria = this.convertCriteria(criteria);
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
  
  convertCriteria: function(criteria){
    if (!criteria) {
      criteria = {};
    }
    
    criteria = OB.Utilities._getTabInfoRequestProperties(this.view, criteria);    

    var isFiltering = criteria.length > 0;
    var filterValues = [];
    var index = 0, selectedValues;
    var fkFieldsLength = this.foreignKeyFieldNames.getLength(), filterValue = null;
    
    if (this.targetRecordId) {
      criteria._targetRecordId = this.targetRecordId;
    }
    
    if (this.view.parentProperty) {
      if (!this.view.parentView) {
        criteria[this.view.parentProperty] = '-1';
      } else {
        selectedValues = this.view.parentView.viewGrid.getSelectedRecords();
      }
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
    
    return criteria;
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
    if (!this.singleRecordSelection && selectionLength > 0) {
      newValue = selectionLength + '';
    }
    if (this.filterEditor) {
      this.filterEditor.getEditForm().setValue(this.getCheckboxField().name, newValue);
    }
  },
  
  // selectionChanged is called when the user makes changes
  selectionChanged: function(record, state){
    this.stopHover();
    
    // stop editing if the selection is changing  
    var rowNum = this.getRecordIndex(record);
    
    if (this.getEditRow()) {
      if (this.getEditRow() !== rowNum) {
        this.endEditing();
      } else {
        // don't do any updates
        this.updateSelectedCountDisplay();
        return;
      }
    }
    
    isc.Log.logDebug('Selection changed ' + state, 'OB');
    this.updateSelectedCountDisplay();
    this.view.recordSelected();
  },
  
  // selectionUpdated is called when the grid selection is changed
  // programmatically
  selectionUpdated: function(record, recordList){
    isc.Log.logDebug('Selection updated ' + record, 'OB');
    this.updateSelectedCountDisplay();
    this.view.recordSelected();
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
  initWidget: function() {
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
    
    this.addMember(this.OBGridToolStrip);
    this.OBGridToolStrip.hideMember(5);
    this.OBGridToolStrip.hideMember(4);
    this.OBGridToolStrip.hideMember(3);
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
