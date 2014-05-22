/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.1  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html
 * Software distributed under the License  is  distribfuted  on  an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific  language  governing  rights  and  limitations
 * under the License.
 * The Original Code is Openbravo ERP.
 * The Initial Developer of the Original Code is Openbravo SLU
 * All portions are Copyright (C) 2011-2014 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

isc.defineClass('OBPickAndExecuteGrid', isc.OBGrid);

// == OBPickAndExecuteGrid ==
//   OBPickAndExecuteGrid is the grid that is the actual parameter of the
//   OBPickAndExecuteView class.
isc.OBPickAndExecuteGrid.addProperties({
  dataProperties: {
    useClientFiltering: false,
    useClientSorting: false
  },
  view: null,
  dataSource: null,
  showFilterEditor: true,
  showErrorIcons: false,

  // Editing
  canEdit: true,
  editEvent: isc.EH.CLICK,
  autoSaveEdits: false,

  selectionAppearance: 'checkbox',
  autoFitFieldWidths: true,
  autoFitWidthApproach: 'title',
  canAutoFitFields: false,
  minFieldWidth: 75,
  width: '100%',
  height: '100%',
  autoFitFieldsFillViewport: true,
  confirmDiscardEdits: false,
  animateRemoveRecord: false,
  // this attribute helps to set an attribute only if the edit form has not been initialized
  editFormInitialized: false,
  removeFieldProperties: {
    width: 32
  },

  // prevents additional requests when loading data
  drawAllMaxCells: 0,

  //The Cell should be validated each time the focus is changed.
  validateByCell: true,
  // default selection
  selectionProperty: 'obSelected',

  shouldFixRowHeight: function () {
    return true;
  },

  initWidget: function () {
    var i, len = this.fields.length,
        theGrid, me = this,
        filterableProperties, canFilter;

    this.selectedIds = [];
    this.deselectedIds = [];

    // the getValuesAsCriteria function of the edit form of the filter editor should always be called with 
    // advanced = true to guarantee that the returned criteria will have the proper format
    this.filterEditorDefaults.editFormDefaults = this.filterEditorDefaults.editFormDefaults || {};
    this.filterEditorDefaults.editFormDefaults.originalGetValuesAsCriteria = isc.DynamicForm.getPrototype().getValuesAsCriteria;
    this.filterEditorDefaults.editFormDefaults.getValuesAsCriteria = function (advanced, textMatchStyle, returnNulls) {
      var useAdvancedCriteria = true;
      return this.originalGetValuesAsCriteria(useAdvancedCriteria, textMatchStyle, returnNulls);
    };

    this.filterEditorProperties = isc.shallowClone(this.filterEditorProperties);

    // the origSetValuesAsCriteria member is added as 'class' level
    // we only need to do it once
    if (!this.filterEditorProperties.origSetValuesAsCriteria) {

      this.filterEditorProperties.origSetValuesAsCriteria = this.filterEditorProperties.setValuesAsCriteria;

      this.filterEditorProperties.setValuesAsCriteria = function (criteria, advanced) {
        var orig = (criteria && criteria.criteria) || [],
            len = orig.length,
            crit, i;

        if (criteria._OrExpression) {
          for (i = 0; i < len; i++) {
            if (orig[i].fieldName && orig[i].fieldName === 'id') {
              continue;
            }

            if (orig[i].operator && orig[i]._constructor) {
              crit = orig[i];
              break;
            }
          }
        } else {
          crit = criteria;
        }

        this.origSetValuesAsCriteria(crit, advanced);
      };
    }

    // adding a reference to the plain field object to this grid
    // useful when working with custom field validators
    for (i = 0; i < len; i++) {
      this.fields[i].grid = this;
      if (this.fields[i].onChangeFunction) {
        // the default
        this.fields[i].onChangeFunction.sort = 50;

        OB.OnChangeRegistry.register(this.ID, this.parameterName + OB.Constants.FIELDSEPARATOR + this.fields[i].name, this.fields[i].onChangeFunction, 'default');
      }
    }
    this.setFields(this.fields);
    // Display logic for grid column
    this.evaluateDisplayLogicForGridColumns();

    // required to show the funnel icon and to work
    this.filterClause = this.gridProperties.filterClause;
    this.sqlFilterClause = this.gridProperties.sqlFilterClause;
    this.lazyFiltering = this.gridProperties.lazyFiltering;
    this.filterName = this.gridProperties.filterName;

    this.orderByClause = this.gridProperties.orderByClause;
    this.sqlOrderByClause = this.gridProperties.sqlOrderByClause;

    this.checkboxFieldProperties = isc.addProperties({}, this.checkboxFieldProperties | {}, {
      canFilter: true,
      frozen: true,
      canFreeze: true,
      showHover: true,
      prompt: OB.I18N.getLabel('OBUIAPP_GridSelectAllColumnPrompt'),
      filterEditorType: 'StaticTextItem'
    });

    OB.TestRegistry.register('org.openbravo.client.application.process.pickandexecute.Grid', this);

    // FIXME:---
    this.editFormProperties = {
      view: this.view.buttonOwnerView
    };

    this.autoFitExpandField = this.getLongestFieldName();


    this.dataSource.transformRequest = function (dsRequest) {
      dsRequest.params = dsRequest.params || {};
      if (me.view && me.view.theForm) {
        // include in the request the values of the parameters of the parameter window
        isc.addProperties(dsRequest.params, me.view.theForm.getValues());
      }
      return this.Super('transformRequest', arguments);
    };
    filterableProperties = this.getFields().findAll('canFilter', true);
    if (this.filterClause) {
      // if there is a filter clause always show the filterEditor, otherwise there would be no funnel
      // icon and it would not be possible to clear the filter clause
      canFilter = true;
    } else {
      canFilter = false;
      if (filterableProperties) {
        for (i = 0; i < filterableProperties.length; i++) {
          // when looking for filterable columns do not take into account the columns whose name starts with '_' (checkbox, delete button, etc) 
          if (!filterableProperties[i].name.startsWith('_')) {
            canFilter = true;
            break;
          }
        }
      }
    }
    // If there are no filterable columns, hide the filter editor
    if (!canFilter) {
      this.filterEditorProperties.visibility = 'hidden';
    }
    this.Super('initWidget', arguments);
  },

  evaluateDisplayLogicForGridColumns: function () {
    var currentValues = (this.contentView.view.theForm && this.contentView.view.theForm.getValues()) || {},
        contextInfo = (this.view.buttonOwnerView && this.view.buttonOwnerView.getContextInfo(false, true, true, true)) || {},
        i, fieldVisibility;
    // TODO: parse currentValues properly 
    isc.addProperties(contextInfo, currentValues);
    for (i = 0; i < this.completeFields.length; i++) {
      if (this.completeFields[i].displayLogicGrid && isc.isA.Function(this.completeFields[i].displayLogicGrid)) {
        fieldVisibility = this.completeFields[i].displayLogicGrid(currentValues, contextInfo);
        if (fieldVisibility) {
          this.showFields(this.completeFields[i].name);
        } else {
          this.hideFields(this.completeFields[i].name);
        }
      }
    }
  },

  getLongestFieldName: function () {
    var len = this.fields.length,
        maxWidth = -1,
        i, longestFieldName;
    for (i = 0; i < len; i++) {
      if (this.fields[i].displaylength > maxWidth) {
        longestFieldName = this.fields[i].name;
        maxWidth = this.fields[i].displaylength;
      }
    }
    return longestFieldName;
  },

  // when starting row editing make sure that the current
  // value and identifier are part of a valuemap
  // so that the combo shows the correct value without 
  // loading it from the backend
  rowEditorEnter: function (record, editValues, rowNum) {
    if (this.view.actionHandler !== "org.openbravo.advpaymentmngt.actionHandler.ModifyPaymentPlanActionHandler") {
      var i = 0,
          editRecord = this.getEditedRecord(rowNum),
          gridFld, identifier, formFld, value, form = this.getEditForm();

      if (editRecord) {
        // go through the fields and set the edit values
        for (i = 0; i < this.getFields().length; i++) {
          gridFld = this.getFields()[i];
          formFld = form.getField(gridFld.name);
          value = editRecord[gridFld.name];
          identifier = editRecord[gridFld.name + OB.Constants.FIELDSEPARATOR + OB.Constants.IDENTIFIER];
          if (formFld && value && identifier) {
            if (formFld.setEntry) {
              formFld.setEntry(value, identifier);
            } else {
              if (!formFld.valueMap) {
                formFld.valueMap = {};
              }
              formFld.valueMap[value] = identifier;
              form.setValue(formFld, value);
            }
          }
        }
      }
    }
    return this.Super('rowEditorEnter', arguments);
  },

  selectionChanged: function (record, state) {
    var recordIdx;

    if (this.viewProperties.selectionFn) {
      this.viewProperties.selectionFn(this, record, state);
    }

    recordIdx = this.getRecordIndex(record);

    if (!state && recordIdx !== -1) {
      this.discardEdits(recordIdx);
    }

    this.selectionUpdated(record, this.getSelectedRecords());

    this.Super('selectionChanged', arguments);
  },

  selectionUpdated: function (record, recordList) {
    var i, j, len = recordList.length,
        prevSelectedLen = this.selectedIds.length,
        recordId, found;

    // Look for deselected records (records in selectedIds not present in recordList)
    for (i = 0; i < prevSelectedLen; i++) {
      recordId = this.selectedIds[i];
      found = false;
      for (j = 0; j < len; j++) {
        if (recordId === recordList[j].id) {
          found = true;
          break;
        }
      }
      if (!found) {
        this.deselectedIds.push(recordId);
      }
    }

    this.selectedIds = [];

    for (i = 0; i < len; i++) {
      this.selectedIds.push(recordList[i].id);
      // Remove the record from deselectedIds
      this.deselectedIds.remove(recordList[i].id);
    }
    // refresh it all as multiple lines can be selected
    this.markForRedraw('Selection changed');

    this.Super('selectionUpdated', arguments);
  },

  cellEditEnd: function (editCompletionEvent, newValue, ficCallDone, autoSaveDone) {
    var rowNum = this.getEditRow(),
        colNum = this.getEditCol(),
        editField = this.getEditField(colNum),
        undef;

    // Execute onChangeFunctions if they exist
    if (this && OB.OnChangeRegistry.hasOnChange(this.view.viewId, editField)) {
      OB.OnChangeRegistry.call(this.ID, editField, this.view, this.view.theForm, this);
    }

    if (editField.required) {
      if (newValue === null || newValue === undef) {
        this.setFieldError(rowNum, editField.name, 'Invalid Value');
      } else {
        this.clearFieldError(rowNum, editField.name);
      }
    }
    this.Super('cellEditEnd', arguments);

    // after editing a field value read only can be affected
    this.handleReadOnlyLogic();
  },

  // disables/enables fields with read only logic
  handleReadOnlyLogic: function () {
    var form;
    if (!this.viewProperties.handleReadOnlyLogic) {
      return;
    }
    form = this.getEditForm();
    if (form) {
      this.viewProperties.handleReadOnlyLogic(form.getValues(), this.getContextInfo(), form);
    }
  },

  handleFilterEditorSubmit: function (criteria, context) {
    var ids = [],
        crit = {},
        len = this.selectedIds.length,
        i, c, found;
    //saved Data will be used to retain values after fetch through filters.
    if (len > 0) {
      this.data.savedData = this.data.localData;
    }

    for (i = 0; i < len; i++) {
      ids.push({
        fieldName: 'id',
        operator: 'equals',
        value: this.selectedIds[i]
      });
    }

    if (len > 0) {

      crit._constructor = 'AdvancedCriteria';
      crit._OrExpression = true; // trick to get a really _or_ in the backend
      crit.operator = 'or';
      crit.criteria = ids;

      c = (criteria && criteria.criteria) || [];
      found = false;

      for (i = 0; i < c.length; i++) {
        if (c[i].fieldName && c[i].value !== '') {
          found = true;
          break;
        }
      }

      if (!found) {

        if (!criteria) {
          criteria = {
            _constructor: 'AdvancedCriteria',
            operator: 'and',
            criteria: []
          };
        }

        // adding an *always true* sentence
        criteria.criteria.push({
          fieldName: 'id',
          operator: 'notNull'
        });
      }
      crit.criteria.push(criteria); // original filter
    } else {
      crit = criteria;
    }

    if (this._cleaningFilter) {
      // Always refresh when cleaning the filter
      if (!criteria) {
        criteria = {
          _constructor: 'AdvancedCriteria',
          operator: 'and',
          criteria: []
        };
      }
      criteria.criteria.push(isc.OBRestDataSource.getDummyCriterion());
      crit = criteria;
    }

    this.Super('handleFilterEditorSubmit', [crit, context]);
  },

  dataArrived: function (startRow, endRow) {
    var record, i, rows, selectedLen = this.selectedIds.length,
        len, savedRecord, index, j, fields;
    fields = this.getFields();
    for (i = 0; i < selectedLen; i++) {
      record = this.data.findByKey(this.selectedIds[i]);
      if (record) {
        record[this.selectionProperty] = true;
        if (this.data.savedData) {
          savedRecord = this.data.savedData.find('id', this.selectedIds[i]);
          //Setting editable fields from saved Data to retain values.
          for (j = 0; j < fields.length; j++) {
            if (fields[j].canEdit !== false) {
              record[fields[j].name] = savedRecord[fields[j].name];
            }
          }
        }
      }
    }

    len = this.deselectedIds.length;
    for (i = 0; i < len; i++) {
      record = this.data.findByKey(this.deselectedIds[i]);
      if (record) {
        record[this.selectionProperty] = false;
      }
    }

    if (selectedLen === 0) {
      // push all *selected* rows into selectedIds cache
      rows = this.data.allRows || this.data.localData || [];
      len = rows.length;
      for (i = 0; i < len; i++) {
        if (rows[i] && rows[i][this.selectionProperty]) {
          this.selectedIds.push(rows[i][OB.Constants.ID]);
        }
      }
    }

    this.Super('dataArrived', arguments);
  },

  recordClick: function (grid, record, recordNum, field, fieldNum, value, rawValue) {
    if (fieldNum === 0 && value.indexOf('unchecked.png') !== -1) {
      grid.endEditing();
      return false;
    }
    return this.Super('recordClick', arguments);
  },

  getOrgParameter: function () {
    var view = this.view && this.view.buttonOwnerView,
        context, i;

    if (view) {
      context = view.getContextInfo(true, false);

      for (i in context) {
        if (context.hasOwnProperty(i) && i.indexOf('organization') !== -1) {
          return context[i];
        }
      }
    }
    return OB.User.organizationId;
  },

  onFetchData: function (criteria, requestProperties) {
    requestProperties = requestProperties || {};
    requestProperties.params = this.getFetchRequestParams(requestProperties.params);
  },

  clearFilter: function () {
    this.filterClause = null;
    this._cleaningFilter = true;
    this.contentView.messageBar.hide();
    this.Super('clearFilter', arguments);
    delete this._cleaningFilter;
  },

  getFetchRequestParams: function (params) {
    var props = this.gridProperties || {},
        view = this.view && this.view.buttonOwnerView;

    params = params || {};
    if (view) {
      isc.addProperties(params, view.getContextInfo(true, false));
    }

    params[OB.Constants.ORG_PARAMETER] = this.getOrgParameter();

    if (this.orderByClause) {
      params[OB.Constants.ORDERBY_PARAMETER] = this.orderByClause;
    }

    if (this.sqlOrderByClause) {
      params[OB.Constants.SQL_ORDERBY_PARAMETER] = this.sqlOrderByClause;
    }

    if (this.filterClause) {
      if (props.whereClause) {
        params[OB.Constants.WHERE_PARAMETER] = ' ((' + props.whereClause + ') and (' + this.filterClause + ")) ";
      } else {
        params[OB.Constants.WHERE_PARAMETER] = this.filterClause;
      }
    } else if (props.whereClause) {
      params[OB.Constants.WHERE_PARAMETER] = props.whereClause;
    } else {
      params[OB.Constants.WHERE_PARAMETER] = null;
    }

    if (this.sqlFilterClause) {
      if (props.sqlWhereClause) {
        params[OB.Constants.SQL_WHERE_PARAMETER] = ' ((' + props.sqlWhereClause + ') and (' + this.sqlFilterClause + ")) ";
      } else {
        params[OB.Constants.SQL_WHERE_PARAMETER] = this.sqlFilterClause;
      }
    } else if (props.sqlWhereClause) {
      params[OB.Constants.SQL_WHERE_PARAMETER] = props.sqlWhereClause;
    } else {
      params[OB.Constants.SQL_WHERE_PARAMETER] = null;
    }

    return params;
  },

  getFieldByColumnName: function (columnName) {
    var i, len = this.fields.length,
        colName;

    if (!this.fieldsByColumnName) {
      this.fieldsByColumnName = [];
      for (i = 0; i < len; i++) {
        colName = this.fields[i].columnName;
        if (colName) {
          this.fieldsByColumnName[colName] = this.fields[i];
        }
      }
    }

    return this.fieldsByColumnName[columnName];
  },

  setValueMap: function (field, entries) {
    var len = entries.length,
        map = {},
        i, undef;

    for (i = 0; i < len; i++) {
      if (entries[i][OB.Constants.ID] !== undef) {
        map[entries[i][OB.Constants.ID]] = entries[i][OB.Constants.IDENTIFIER];
      }
    }

    this.Super('setValueMap', [field, map]);
  },

  processColumnValue: function (rowNum, columnName, columnValue) {
    var field;
    if (!columnValue) {
      return;
    }

    if (columnValue.entries) {
      field = this.getFieldByColumnName(columnName);
      if (!field) {
        return;
      }
      this.setValueMap(field.name, columnValue.entries);
    }
  },

  processFICReturn: function (response, data, request) {
    var context = response && response.clientContext,
        rowNum = context && context.rowNum,
        grid = context && context.grid,
        columnValues, prop, value, undef;


    if (rowNum === undef || !data || !data.columnValues) {
      return;
    }

    columnValues = data.columnValues;

    for (prop in columnValues) {
      if (columnValues.hasOwnProperty(prop)) {
        if (columnValues[prop] && columnValues[prop].entries && columnValues[prop].entries.length === 0) {
          delete columnValues[prop].entries;
        }
        grid.processColumnValue(rowNum, prop, columnValues[prop]);
      }
    }

    grid.handleReadOnlyLogic();
  },

  getContextInfo: function (rowNum) {
    var view = this.view && this.view.buttonOwnerView,
        contextInfo, record, fields, len, fld, i, value, undef, type;

    if (!view) {
      return;
    }
    contextInfo = isc.addProperties({}, this.view.parentWindow.activeView.getContextInfo(false, true, false, true));
    record = isc.addProperties({}, this.getRecord(rowNum), this.getEditValues(rowNum));
    fields = this.viewProperties.fields;
    len = fields.length;

    for (i = 0; i < len; i++) {
      fld = fields[i];
      value = record[fld.name];
      if (value !== undef) {
        if (fld.type) {
          type = isc.SimpleType.getType(fld.type);
          if (type.createClassicString) {
            contextInfo[fld.inpColumnName] = type.createClassicString(value);
          } else {
            contextInfo[fld.inpColumnName] = view.convertContextValue(value, fld.type);
          }
        } else {
          contextInfo[fld.inpColumnName] = view.convertContextValue(value, fld.type);
        }
      }
    }

    return contextInfo;
  },

  retrieveInitialValues: function (rowNum, colNum, newCell, newRow, suppressFocus) {
    var requestParams, allProperties, i, record;

    allProperties = this.getContextInfo(rowNum);
    record = this.getRecord(rowNum);

    requestParams = {
      MODE: (newRow ? 'NEW' : 'EDIT'),
      PARENT_ID: null,
      TAB_ID: this.viewProperties.tabId,
      ROW_ID: (!newRow && record ? record[OB.Constants.ID] : null)
    };

    OB.RemoteCallManager.call('org.openbravo.client.application.window.FormInitializationComponent', allProperties, requestParams, this.processFICReturn, {
      grid: this,
      rowNum: rowNum,
      colNum: colNum,
      newCell: newCell,
      newRow: newRow,
      suppressFocus: suppressFocus
    });
  },

  showInlineEditor: function (rowNum, colNum, newCell, newRow, suppressFocus) {
    var editForm, items, i, updatedBlur;
    // retrieve the initial values only if a new row has been selected
    // see issue https://issues.openbravo.com/view.php?id=20653
    if (newRow) {
      if (this.view.actionHandler === "org.openbravo.advpaymentmngt.actionHandler.ModifyPaymentPlanActionHandler") {
        this.retrieveInitialValues(rowNum, colNum, false, false, suppressFocus);
      } else {
        this.retrieveInitialValues(rowNum, colNum, newCell, newRow, suppressFocus);
      }
    }
    this.Super('showInlineEditor', arguments);

    // update the blur function of the formitems, so that the OnChangeRegistry functions are called
    // when the item loses the focus
    if (!this.editFormInitialized) {
      // the editForm is created the first time the inline editor is shown
      this.editFormInitialized = true;
      editForm = this.getEditForm();
      if (editForm) {
        items = editForm.getItems();
        updatedBlur = function (form, item) {
          this.original_blur(form, item);
          // Execute onChangeFunctions if they exist
          if (this && OB.OnChangeRegistry.hasOnChange(form.grid.ID, item)) {
            OB.OnChangeRegistry.call(form.grid.ID, item, form.grid.view, form.grid.view.theForm, form.grid);
          }
        };
        for (i = 0; i < items.length; i++) {
          items[i].original_blur = items[i].blur;
          items[i].blur = updatedBlur;
        }
      }
    }
  },

  hideInlineEditor: function (focusInBody, suppressCMHide) {
    var ret = this.Super('hideInlineEditor', arguments);
    this.validateRows();
    return ret;
  },

  validateRows: function () {
    var i, row, field, errors, editRowIndexes, editRowIDs, rowIndexID, data = this.data.allRows || this.data.localData;

    if (!this.neverValidate) {
      return;
    }

    editRowIndexes = this.getAllEditRows();
    editRowIDs = this.getAllEditRows(true);

    for (i = 0; i < this.fields.length; i++) {
      field = this.fields[i];

      if (!field.validationFn) {
        continue;
      }
      for (row = 0; row < data.length; row++) {
        errors = this.validateCellValue(row, i, data[row][field.name]);
        if (!errors || isc.isA.emptyArray(errors)) {
          if (editRowIndexes.indexOf(row) !== -1) {
            rowIndexID = editRowIDs[editRowIndexes.indexOf(row)];
          } else {
            rowIndexID = row;
          }
          this.clearFieldError(editRowIDs[row], field.name);
        } else {
          this.setFieldError(row, field.name, errors[0]);
        }
      }
    }
    this.recalculateSummaries();
  },

  removeRecord: function (rowNum, record) {
    var remove = true,
        removeFn = this.viewProperties && this.viewProperties.removeFn;

    if (removeFn && isc.isA.Function(removeFn)) {
      remove = removeFn(this, rowNum, record);
    }

    if (!remove) {
      this.validateRows();
      return;
    }

    this.Super('removeRecord', arguments);

    this.validateRows();
  },

  destroy: function () {
    if (this.dataSource) {
      this.dataSource.destroy();
    }
    this.Super('destroy', arguments);
  },

  checkShowFilterFunnelIcon: function (criteria) {
    this.Super('checkShowFilterFunnelIcon', [criteria, this.contentView.messageBar]);
  },

  removeRecordClick: function (rowNum, colNum) {
    this.Super('removeRecordClick', arguments);
    // prevents the deleted line from being partially displayed
    this.markForRedraw();
  },

  getMinFieldWidth: function (field, ignoreFieldWidth) {
    // items like _checkbox, _pin and _delete can have a width smaller than the min field width defined for the grid
    if (field && field.name && field.name.startsWith('_')) {
      return field.width;
    } else {
      this.Super('getMinFieldWidth', arguments);
    }
  }

});