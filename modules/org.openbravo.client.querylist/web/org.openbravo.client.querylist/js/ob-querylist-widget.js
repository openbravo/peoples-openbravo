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
// = OBQueryListWidget =
//
// Implements the Query / List widget superclass.
//
isc.defineClass('OBQueryListWidget', isc.OBWidget).addProperties({

  widgetId: null,
  widgetInstanceId: null,
  rowsNumber: null,
  fields: null,
  grid: null,
  gridProperties: {},
  actionHandler: 'org.openbravo.client.querylist.QueryListActionHandler',
  viewMode: 'widget',

  initWidget: function(){
    this.Super('initWidget', arguments);
    // Calculate height
    var currentHeight = this.getHeight(), 
        //currentBodyHeight = this.body.getHeight(),
        headerHeight = this.headerDefaults.height,
        newGridHeight = this.grid.headerHeight +
                      (this.grid.cellHeight * (this.rowsNumber ? this.rowsNumber : 10)) +
                      this.grid.summaryRowHeight + 2;

    this.setHeight(headerHeight + newGridHeight + 13);
  },

  setDbInstanceId: function (instanceId) {
    this.Super('setDbInstanceId', instanceId);
    this.grid.fetchData();
  },

  createWindowContents: function(){
    var layout = isc.VStack.create({
      height: '100%',
      width: '100%',
      styleName: ''
    }), url, params = {};
    
    this.grid = isc.OBQueryListGrid.create(isc.addProperties({
      widget: this,
      fields: this.fields
    }, this.gridProperties));
    
    layout.addMembers(this.grid);
    return layout;
  },
  
  refresh: function(){
    this.grid.invalidateCache();
    this.grid.filterData();
  },

  exportGrid: function() {
    var grid = this.widget.grid;
    var requestProperties = {
          exportAs: 'csv',
          //exportFilename: 'Query/List_widget.csv',
          exportDisplay: 'download',
          params: {
            exportToFile: true
          }
        };
    var additionalProperties = {
          widgetInstanceId: this.widget.dbInstanceId
        };

    grid.exportData(requestProperties, additionalProperties);
  }
  
});

isc.ClassFactory.defineClass('OBQueryListGrid', isc.OBGrid);

isc.OBQueryListGrid.addProperties({
  width: '100%',
  height: '100%',
  dataSource: null,
  
  // some common settings
  //showFilterEditor: false,
  //filterOnKeypress: false,
  
  canEdit: false,
  alternateRecordStyles: true,
  canReorderFields: false,
  canFreezeFields: false,
  canGroupBy: false,
  autoFetchData: false,
  canAutoFitFields: false,
  showGridSummary: true,
  
  //autoFitFieldWidths: true,
  //autoFitWidthApproach: 'title',
  
  initWidget: function(){
    OB.Datasource.get('DD17275427E94026AD721067C3C91C18', this);
    this.Super('initWidget', arguments);
  },
  
  setDataSource: function(ds){
    if (ds) {
      ds.fields = this.widget.fields;
      this.dataSource = ds;

      if(this.widget && this.widget.dbInstanceId) {
        this.filterData();
      }

    }
  },
  
  filterData: function(criteria, callback, requestProperties){
    var crit = criteria || {},
    reqProperties = requestProperties || {};

    reqProperties.showPrompt = false;
    crit.widgetInstanceId = this.widget.dbInstanceId;
    crit.rowsNumber = this.widget.rowsNumber;
    crit.viewMode = this.widget.viewMode;
    return this.Super('filterData', [crit, callback, reqProperties]);
  },
  
  fetchData: function(criteria, callback, requestProperties){
    var crit = criteria || {},
        reqProperties = requestProperties || {};

    reqProperties.showPrompt = false;
    crit.widgetInstanceId = this.widget.dbInstanceId;
    crit.rowsNumber = this.widget.rowsNumber;
    crit.viewMode = this.widget.viewMode;
    return this.Super('fetchData', [crit, callback, reqProperties]);
  },

  cellClick: function (record, rowNum, colNum) {
    var field = this.getField(colNum);
    if (field.isLink) {
      if (field.OB_TabId && field.OB_LinkExpression) {
        OB.Layout.ViewManager.openView('OBClassicWindow',  {
            tabId: field.OB_TabId,
            recordId: record[field.OB_LinkExpression],
            windowId: field.OB_WindowId,
            command: 'DIRECT',
            tabTitle: field.OB_WindowTitle,
            mappingName: field.OB_mappingName,
            keyParameter: field.OB_keyParameter
        });
      }
    }
  }
});
