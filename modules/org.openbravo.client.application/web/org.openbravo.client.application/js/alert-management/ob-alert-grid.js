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
 * All portions are Copyright (C) 2011 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

isc.ClassFactory.defineClass('OBAlertGrid', isc.OBGrid);
isc.OBAlertGrid.addProperties({
  alertStatus: null,

  width: '100%',
  height: '100%',
  dataSource: null,
  canEdit: true,
  alternateRecordStyles: true,
  showFilterEditor: true,
  canReorderFields: false,
  canFreezeFields: false,
  canGroupBy: false,
  canAutoFitFields: false,
  selectionType: 'simple',
  editEvent: 'click',
  //editOnFocus: true,
  showCellContextMenus: true,

  dataProperties: {
    useClientFiltering: false//,
    //useClientSorting: false
  },
  
  gridFields: [
    { name: 'alertRule',
      title: OB.I18N.getLabel('OBUIAPP_AlertGrid_AlertRule'),
      displayField: 'alertRule._identifier',
      canFilter: true,
      canEdit: false,
      filterOnKeypress: true,
      filterEditorType: 'OBFKFilterTextItem',
      type: '_id_19'
    },
    { name: 'description',
      title: OB.I18N.getLabel('OBUIAPP_AlertGrid_Alert'),
      canFilter: true,
      canEdit: false,
      filterOnKeypress: true,
      filterEditorType: 'OBTextItem'
        //, type: '_id_10'
    },
    { name: 'creationDate',
      title: OB.I18N.getLabel('OBUIAPP_AlertGrid_Time'),
      canFilter: true,
      canEdit: false,
      filterEditorType: 'OBMiniDateRangeItem',
      type: '_id_16'
    },
    { name: 'comments',
      title: OB.I18N.getLabel('OBUIAPP_AlertGrid_Note'),
      canFilter: true,
      canEdit: true,
      filterOnKeypress: true,
      filterEditorType: 'OBTextItem',
      editorType: 'OBTextItem',
      editorProperties: {
        width: '90%',
        columnName: 'comments',
        disabled: false,
        updatable: true
      }
      //, type: '_id_10'
    },
    { name: 'recordID',
      title: OB.I18N.getLabel('OBUIAPP_AlertGrid_Record'),
      canFilter: true,
      canEdit: false,
      isLink: true,
      filterOnKeypress: true,
      filterEditorType: 'OBTextItem'
      //, type: '_id_10'
    }
    ],
  
  initWidget: function() {
    // added for showing counts in the filtereditor row
    this.checkboxFieldDefaults = isc.addProperties(this.checkboxFieldDefaults, {
      canFilter: true,
      frozen: true,
      canFreeze: true,
      showHover: true,
      prompt: OB.I18N.getLabel('OBUIAPP_GridSelectAllColumnPrompt'),
      filterEditorProperties: {
        textAlign: 'center'
      },
      filterEditorType: 'StaticTextItem'
    });

    this.contextMenu = this.getMenuConstructor().create({items: []});
    
    OB.Datasource.get('ADAlert', this);
    
    this.Super('initWidget', arguments);
  },
  
  setDataSource: function() {
    this.Super('setDataSource', arguments);
    // Some properties need to be set when the datasource is loaded to avoid errors when form is
    // open the first time.
    this.setFields(this.gridFields);
    this.setSelectionAppearance('checkbox');
    this.sort('creationDate', 'descending');

    this.fetchData();
  },
  
  dataArrived: function(startRow, endRow){
    this.getGridTotalRows();
    return this.Super('dataArrived', arguments);
  },

  getGridTotalRows: function(){
    var criteria = this.getCriteria() || {}, requestProperties = {};

    if (!OB.AlertManagement.sections[this.alertStatus].expanded) {
      // fetch to the datasource with an empty criteria to get all the rows
      requestProperties.params = requestProperties.params || {};
      requestProperties.params[OB.Constants.WHERE_PARAMETER] = this.getFilterClause();
      requestProperties.clientContext = {alertStatus: this.alertStatus};
      this.dataSource.fetchData(criteria, function(dsResponse, data, dsRequest){
          OB.AlertManagement.setTotalRows(dsResponse.totalRows, dsResponse.clientContext.alertStatus);
        }, requestProperties );

    } else {
      OB.AlertManagement.setTotalRows(this.getTotalRows(), this.alertStatus);
    }
  },
  
  clearFilter: function(){
    delete this.filterClause;
    this.filterEditor.getEditForm().clearValues();
    this.filterEditor.performAction();
  },
  
  onFetchData: function(criteria, requestProperties){
    requestProperties = requestProperties || {};
    requestProperties.params = requestProperties.params || {};

    requestProperties.params[OB.Constants.WHERE_PARAMETER] = this.getFilterClause();
  },
  
  getFilterClause: function() {
    var i, filterClause = '',
        alertRuleIds = '',
        arlength = OB.AlertManagement.alertRules.length,
        whereClause = 'status = upper(\'' + this.alertStatus + '\')';

    for (i = 0; i < arlength; i++) {
      if (alertRuleIds !== '') {
        alertRuleIds += ',';
      }
      alertRuleIds += '\'' + OB.AlertManagement.alertRules[i].alertRuleId +'\'';
      // if an alertRule has some alerts to filter by, add them to the where clause as:
      // alerts are of a different alertRule or only the alerts predefined
      // this only happens if the alertRule has an SQL filter expression defined
      if (OB.AlertManagement.alertRules[i].alerts) {
        filterClause += ' and (e.alertRule.id != \'' + OB.AlertManagement.alertRules[i].alertRuleId + '\'';
        filterClause += ' or e.id in (' +OB.AlertManagement.alertRules[i].alerts + '))';
      }
    }
    whereClause += ' and alertRule.id in (' + alertRuleIds + ')';
    if (filterClause !== '') {
      whereClause += filterClause;
    }
    return whereClause;
  },
  
  cellClick: function (record, rowNum, colNum) {
    var i, tabId, field = this.getField(colNum);
    for (i = 0; i < OB.AlertManagement.alertRules.length; i++) {
      if (OB.AlertManagement.alertRules[i].alertRuleId === record.alertRule) {
        tabId = OB.AlertManagement.alertRules[i].tabId;
      }
    }
    if (field.isLink && tabId && tabId !== '') {
      OB.Utilities.openDirectTab(tabId, record.referenceSearchKey);
    }
  },
  
  selectionChanged: function(record, state){
    this.updateSelectedCountDisplay();
    this.Super('selectionChanged', arguments);
  },
  
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
  },
  
  makeCellContextItems: function(record, rowNum, colNum){
    var menuItems = [];
    var grid = this;
    if (grid.alertStatus === 'Acknowledged' || grid.alertStatus === 'Suppressed') {
      menuItems.add({
        title: OB.I18N.getLabel('OBUIAPP_MoveToStatus', [OB.AlertManagement.translatedStatus.New]),
        click: function(){
          OB.AlertManagement.moveToStatus(record.id, grid.alertStatus, 'New');
        }
      });
    }
    if (grid.alertStatus === 'New' || grid.alertStatus === 'Suppressed') {
      menuItems.add({
        title: OB.I18N.getLabel('OBUIAPP_MoveToStatus', [OB.AlertManagement.translatedStatus.Acknowledged]),
        click: function(){
          OB.AlertManagement.moveToStatus(record.id, grid.alertStatus, 'Acknowledged');
        }
      });
    }
    if (grid.alertStatus === 'New' || grid.alertStatus === 'Acknowledged') {
      menuItems.add({
        title: OB.I18N.getLabel('OBUIAPP_MoveToStatus', [OB.AlertManagement.translatedStatus.Suppressed]),
        click: function(){
          OB.AlertManagement.moveToStatus(record.id, grid.alertStatus, 'Suppressed');
        }
      });
    }
    return menuItems;
  }

});