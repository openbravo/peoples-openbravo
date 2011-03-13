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
  canSelectText: true,
  
  createRecordComponent: function(record, colNum){
    var field = this.getField(colNum), rowNum = this.getRecordIndex(record);
    if (field.isLink && record[field.name]) {
      var linkButton = isc.OBGridLinkLayout.create({
        grid: this,
        align: this.getCellAlign(record, rowNum, colNum),
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
      component.align = this.getCellAlign(record, rowNum, colNum);
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
    var i, thisGrid = this, field, formatCellValueFunction = function(value, record, rowNum, colNum, grid){
      return '';
    };

    if (this.fields) {
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
    }
    
    this.filterEditorProperties = {

      setEditValue : function (rowNum, colNum, newValue, suppressDisplay, suppressChange) {
        // prevent any setting of non fields in the filter editor
        // this prevents a specific issue that smartclient will set a value
        // in the {field.name}._identifier (for example warehouse._identifier)
        // because it thinks that the field does not have its own datasource
        if (isc.isA.String(colNum) && !this.getField(colNum)) {
          return;
        }
        return this.Super('setEditValue', arguments);
      },
        
      getValuesAsCriteria : function (advanced, textMatchStyle, returnNulls) {
        return this.Super('getValuesAsCriteria', [true, textMatchStyle, returnNulls]);
      },
      
      // is needed to display information in the checkbox field 
      // header in the filter editor row
      isCheckboxField: function(){
        return false;
      },
      
      filterImg: {
        src: '[SKINIMG]../../org.openbravo.client.application/images/grid/funnel-icon.png'
      },
      
      makeActionButton: function(){
        var ret = this.Super('makeActionButton', arguments);
        this.filterImage.setLeft(this.computeFunnelLeft(2));
        var layout = isc.HLayout.create({
          styleName: 'OBGridFilterFunnelBackground',
          width: '100%',
          height: '100%',
          left: this.computeFunnelLeft()
        });
        this.funnelLayout = layout;
        this.addChild(layout);
        return ret;
      },
      
      computeFunnelLeft: function(correction) {
        correction = correction || 0;
        return this.getInnerWidth() - this.getScrollbarSize() - 3 + correction;
      },
      
      // keep the funnel stuff placed correctly
      layoutChildren : function () {
        var ret = this.Super("layoutChildren", arguments);
        if (this.funnelLayout) { 
            this.funnelLayout.setLeft(this.computeFunnelLeft());
        }
        if (this.filterImage) { 
          this.filterImage.setLeft(this.computeFunnelLeft(2));
        }
        return ret;
      },
      
      // repair that filter criteria on fk fields can be 
      // on the identifier instead of the field itself.
      // after applying the filter the grid will set the criteria
      // back in the filtereditor effectively clearing
      // the filter field. The code here repairs/prevents this.
      setValuesAsCriteria: function (criteria, refresh) {
        // create an edit form right away
        if (!this.getEditForm()) {
          this.makeEditForm();
        }
        var prop, fullPropName;
        // make a copy so that we don't change the object
        // which is maybe used somewhere else
        criteria = isc.clone(criteria);
        var internCriteria = criteria.criteria;
        if (internCriteria) {
          // now remove anything which is not a field
          // otherwise smartclient will keep track of them and send them again
          var fields = this.getEditForm().getFields();
          for (i = internCriteria.length - 1; i >=0; i--) {
            prop = internCriteria[i].fieldName;
            // happens when the internCriteria[i], is again an advanced criteria
            if (!prop) {
              continue;
            }
            fullPropName = prop;
            if (prop.endsWith('.' + OB.Constants.IDENTIFIER)) {
              var index = prop.lastIndexOf('.');
              prop = prop.substring(0, index);
            }
            var fnd = false;
            for (var j = 0; j < fields.length; j++) {
              if (fields[j].displayField === fullPropName) {
                fnd = true;
                break;
              }
              if (fields[j].name === prop) {
                internCriteria[i].fieldName = prop;
                fnd = true;
                break;
              }
              if (fields[j].name === fullPropName) {
                fnd = true;
                break;
              }
            }
            if (!fnd) {
              internCriteria.removeAt(i);
            }
          }
        }

        return this.Super('setValuesAsCriteria', [criteria, refresh]);
      },

      actionButtonProperties: {
        baseStyle: 'OBGridFilterFunnelIcon',
        visibility: 'hidden',
        showFocused: false,
        showDisabled: false,
        prompt: OB.I18N.getLabel('OBUIAPP_GridFilterIconToolTip'),
        initWidget: function(){
          thisGrid.filterImage = this;
          this.recordEditor.filterImage = this;
          if (thisGrid.filterClause) {
            this.prompt = OB.I18N.getLabel('OBUIAPP_GridFilterImplicitToolTip');      
            this.visibility = 'inherit';
          }
          this.Super('initWidget', arguments);
        },
        click: function(){
          // get rid of the initial filter clause
          delete thisGrid.filterClause;
          this.recordEditor.getEditForm().clearValues();
          this.recordEditor.performAction();
        }
      }
    };
    
    this.Super('initWidget', arguments);
  },
  
  showSummaryRow: function(){
    var i, fld, fldsLength, newFields = [];
    var ret = this.Super('showSummaryRow', arguments);
    if (this.summaryRow && !this.summaryRowFieldRepaired) {
      // the summaryrow shares the same field instances as the 
      // original grid, this must be repaired as the grid and
      // and the summary row need different behavior.
      // copy the fields and repair specific parts
      // don't support links in the summaryrow
      fldsLength = this.summaryRow.fields.length;
      for (i = 0; i < fldsLength; i++) {
        fld = isc.addProperties({}, this.summaryRow.fields[i]);
        newFields[i] = fld;
        fld.isLink = false;
        if (fld.originalFormatCellValue) {
          fld.formatCellValue = fld.originalFormatCellValue;
          fld.originalFormatCellValue = null;
        } else {
          fld.formatCellValue = null;
        }
      }
      this.summaryRow.isSummaryRow = true;
      this.summaryRowFieldRepaired = true;
      this.summaryRow.setFields(newFields);
    }
    return ret;
  },
  
  // show or hide the filter button
  filterEditorSubmit: function(criteria){
    this.checkShowFilterFunnelIcon(criteria);
  },
  
  checkShowFilterFunnelIcon: function (criteria) {
    if (!this.filterImage) {
      return;
    }
    var gridIsFiltered = this.isGridFiltered(criteria);
    if (this.filterClause && gridIsFiltered) {
      this.filterImage.prompt = OB.I18N.getLabel('OBUIAPP_GridFilterBothToolTip');      
      this.filterImage.show(true);
    } else if (this.filterClause) {
      this.filterImage.prompt = OB.I18N.getLabel('OBUIAPP_GridFilterImplicitToolTip');      
      this.filterImage.show(true);
    } else if (gridIsFiltered) {
      this.filterImage.prompt = OB.I18N.getLabel('OBUIAPP_GridFilterExplicitToolTip');      
      this.filterImage.show(true);
    } else {
      this.filterImage.prompt = OB.I18N.getLabel('OBUIAPP_GridFilterIconToolTip');
      this.filterImage.hide();
    }
  },
  
  isGridFiltered: function(criteria) {
    if (!this.filterEditor) {
      return false;
    }
    if (this.filterClause) {
      return true;
    }
    if (!criteria) {
      return false;
    }
    return this.isGridFilteredWithCriteria(criteria.criteria);
  },
  
  isGridFilteredWithCriteria: function(criteria) {
    if (!criteria) {
      return false;
    }
    for (var i = 0; i < criteria.length; i++) {
      var criterion = criteria[i];
      var prop = criterion.fieldName;
      var fullPropName = prop;
      if (!prop) {
        if (this.isGridFilteredWithCriteria(criterion.criteria)) {
          return true;
        }
        continue;
      }
      var value = criterion.value;
      // see the description in setValuesAsCriteria above
      if (prop.endsWith('.' + OB.Constants.IDENTIFIER)) {
        var index = prop.lastIndexOf('.');
        prop = prop.substring(0, index);
      }
      
      var field = this.filterEditor.getField(prop);
      if (this.isValidFilterField(field) && (value === false || value || value === 0)) {
        return true;
      }
      
      field = this.filterEditor.getField(fullPropName);
      if (this.isValidFilterField(field) && (value === false || value || value === 0)) {
        return true;
      }
    }
    return false;
  },
  
  isValidFilterField: function(field){
    if (!field) {
      return false;
    }
    return !field.name.startsWith('_') && field.canFilter;
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
      viewState: expProp.viewState,
      tab: expProp.tab,
      exportToFile: true,
      _textMatchStyle: 'substring'
    }, this.getCriteria(), this.getFetchRequestParams());
    
    OB.Utilities.postThroughHiddenForm(dsURL, d);
  },
  
  getFetchRequestParams: function(params) {
    return params;
  },
  
  editorKeyDown : function (item, keyName) {
    if (item) {
      if (typeof item.keyDownAction === 'function') {
        item.keyDownAction();
      }
    }
    return this.Super('editorKeyDown', arguments);
  }
});

isc.ClassFactory.defineClass('OBGridSummary', isc.OBGrid);

isc.OBGridSummary.addProperties({
  getCellStyle: function(record, rowNum, colNum){
    var field = this.getField(colNum);
    if (field.summaryFunction === 'sum' && this.summaryRowStyle_sum) {
      return this.summaryRowStyle_sum;
    } else if (field.summaryFunction === 'avg' && this.summaryRowStyle_avg) {
      return this.summaryRowStyle_avg;
    } else {
      return this.summaryRowStyle;
    }
  }
});

isc.ClassFactory.defineClass('OBGridHeaderImgButton', isc.ImgButton);

isc.ClassFactory.defineClass('OBGridLinkLayout', isc.HLayout);
isc.OBGridLinkLayout.addProperties({
  overflow: 'clip-h',
  btn: null,
  height: 1,
  width: '100%',
  
  initWidget: function(){
    this.btn = isc.OBGridLinkButton.create({});
    this.btn.setTitle(this.title);
    this.btn.owner = this;
    this.addMember(this.btn);
    this.Super('initWidget', arguments);
  },
  
  setTitle: function(title){
    this.btn.setTitle(title);
  },
  
  doAction: function(){
    if (this.grid && this.grid.doCellClick) {
      this.grid.doCellClick(this.record, this.rowNum, this.colNum);
    } else if (this.grid && this.grid.cellClick) {
      this.grid.cellClick(this.record, this.rowNum, this.colNum);
    }
  }
  
});

isc.ClassFactory.defineClass('OBGridLinkButton', isc.Button);

isc.OBGridLinkButton.addProperties({
  action: function(){
    this.owner.doAction();
  }
});
