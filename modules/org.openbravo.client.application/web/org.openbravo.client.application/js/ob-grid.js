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
isc.ClassFactory.defineClass('OBGrid', isc.ListGrid);

// = OBGrid =
// The OBGrid combines common grid functionality usefull for different 
// grid implementations.
isc.OBGrid.addProperties({

  recordComponentPoolingMode: 'recycle',
  showRecordComponentsByCell: true,
  recordComponentPosition: 'within',
  poolComponentsPerColumn: true,
  showRecordComponents: true,
  
  createRecordComponent: function(record, colNum){
    var field = this.getField(colNum), rowNum = this.getRecordIndex(record);
    if (field.isLink && record[field.name]) {
      var linkButton = isc.OBGridLinkButton.create({
        grid: this,
        title: this.formatLinkValue(record, field, colNum, rowNum, record[field.name]),
        record: record,
        rowNum: rowNum,
        colNum: colNum
      });
      return linkButton;
    }
    return null;
  },
  
  updateRecordComponent: function(record, colNum, component, recordChanged){
    var field = this.getField(colNum), rowNum = this.getRecordIndex(record);
    if (field.isLink && record[field.name]) {
      component.setTitle(this.formatLinkValue(record, field, colNum, rowNum, record[field.name]));
      component.record = record;
      component.rowNum = rowNum;
      component.colNum = colNum;
      return component;
    }
    return null;
  },
  
  formatLinkValue: function(record, field, colNum, rowNum, value){
    if (typeof value === 'undefined' || value === null) {
      return '';
    }
    var simpleType = isc.SimpleType.getType(field.type, this.dataSource);
    // note: originalFormatCellValue is set in the initWidget below
    if (field && field.originalFormatCellValue) {
      return field.originalFormatCellValue(value, record, rowNum, colNum, this);
    } else if (simpleType.shortDisplayFormatter) {
      return simpleType.shortDisplayFormatter(value, field, this, record, rowNum, colNum);
    }
    return value;
  },
  
  initWidget: function(){
    // prevent the value to be displayed in case of a link
    var i, field, formatCellValueFunction = function(value, record, rowNum, colNum, grid){
      return '';
    };
    for (i = 0; i < this.fields.length; i++) {
      field = this.fields[i];
      if (field.isLink) {
        // store the originalFormatCellValue if not already set
        if (field.formatCellValue && !field.formatCellValueFunctionReplaced) {
          field.originalFormatCellValue = field.formatCellValue;
        }
        field.formatCellValueFunctionReplaced = true;
        field.formatCellValue = formatCellValueFunction;
      }
    }
    return this.Super('initWidget', arguments);
  },
  
  // = exportData =
  // The exportData function exports the data of the grid to a file. The user will 
  // be presented with a save-as dialog.
  // Parameters:
  // * {{{exportProperties}}} defines different properties used for controlling the export, currently only the 
  // exportProperties.exportFormat is supported (which is defaulted to csv).
  // * {{{data}}} the parameters to post to the server, in addition the filter criteria of the grid are posted.  
  exportData: function(exportProperties, data){
    var d = data || {}, expProp = exportProperties || {}, dsURL = this.dataSource.dataURL;
    
    isc.addProperties(d, {
      _dataSource: this.dataSource.ID,
      _operationType: 'fetch',
      _noCount: true, // never do count for export
      exportAs: expProp.exportAs || 'csv',
      exportToFile: true
    }, this.getCriteria());
    
    OB.Utilities.postThroughHiddenForm(dsURL, d);
  }
});

isc.ClassFactory.defineClass('OBGridSummary', isc.OBGrid);

isc.OBGridSummary.addProperties({
  getCellStyle: function(record, rowNum, colNum){
    var field = this.getField(colNum);
    if (field.summaryFunction === "sum" && this.summaryRowStyle_sum) {
      return this.summaryRowStyle_sum;
    } else if (field.summaryFunction === "avg" && this.summaryRowStyle_avg) {
      return this.summaryRowStyle_avg;
    } else {
      return this.summaryRowStyle;
    }
  }
});

isc.ClassFactory.defineClass('OBGridHeaderImgButton', isc.ImgButton);

isc.ClassFactory.defineClass('OBGridLinkButton', isc.Button);
isc.OBGridLinkButton.addProperties({
  action: function(){
    if (this.grid && this.grid.cellClick) {
      this.grid.cellClick(this.record, this.rowNum, this.colNum);
    }
  }
});
