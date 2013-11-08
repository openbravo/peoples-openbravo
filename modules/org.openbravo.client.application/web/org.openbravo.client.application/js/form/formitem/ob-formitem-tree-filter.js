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
 * All portions are Copyright (C) 2013 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

isc.ClassFactory.defineClass('OBTreeItemPopupFilterWindow', isc.OBPopup);

isc.OBTreeItemPopupFilterWindow.addProperties({
  canDragReposition: true,
  canDragResize: true,
  dismissOnEscape: true,
  destroyOnClose: false,
  showMaximizeButton: true,
  multiselect: true,

  defaultTreeGridField: {
    canFreeze: true,
    canGroupBy: false
  },

  initWidget: function () {
    var treeWindow = this,
        okButton, cancelButton, i;

    okButton = isc.OBFormButton.create({
      title: OB.I18N.getLabel('OBUISC_Dialog.OK_BUTTON_TITLE'),
      click: function () {
        treeWindow.accept();
      }
    });
    cancelButton = isc.OBFormButton.create({
      title: OB.I18N.getLabel('OBUISC_Dialog.CANCEL_BUTTON_TITLE'),
      click: function () {
        treeWindow.closeClick();
      }
    });

    OB.Utilities.applyDefaultValues(this.treeGridFields, this.defaultTreeGridField);

    for (i = 0; i < this.treeGridFields.length; i++) {
      this.treeGridFields[i].canSort = (this.treeGridFields[i].canSort === false ? false : true);
      if (this.treeGridFields[i].disableFilter) {
        this.treeGridFields[i].canFilter = false;
      } else {
        this.treeGridFields[i].canFilter = true;
      }
    }
    this.treeGrid = isc.TreeGrid.create({
      view: this.view,
      treePopup: this,
      showOpenIcons: false,
      showDropIcons: false,
      autoFetchData: true,
      nodeIcon: null,
      folderIcon: null,
      filterOnKeypress: true,
      selectionAppearance: "checkbox",
      dataSourceId: this.dataSourceId,
      treeReferenceId: this.treeReferenceId,
      dataProperties: {
        modelType: "parent",
        rootValue: "0",
        idField: "nodeId",
        parentIdField: "parentId",
        openProperty: "isOpen"
      },

      width: '100%',
      height: '100%',
      bodyStyleName: 'OBGridBody',
      showFilterEditor: true,
      alternateRecordStyles: true,
      sortField: this.displayField,

      init: function () {
        OB.Datasource.get(this.dataSourceId, this, null, true);
        this.copyFunctionsFromViewGrid();
        this.Super('init', arguments);
      },

      copyFunctionsFromViewGrid: function () {
        this.filterEditorProperties = this.view.viewGrid.filterEditorProperties;
        this.checkShowFilterFunnelIcon = this.view.viewGrid.checkShowFilterFunnelIcon;
        this.isGridFiltered = this.view.viewGrid.isGridFiltered;
        this.isGridFilteredWithCriteria = this.view.viewGrid.isGridFilteredWithCriteria;
        this.isValidFilterField = this.view.viewGrid.isValidFilterField;
        this.convertCriteria = this.view.viewGrid.convertCriteria;
        this.resetEmptyMessage = this.view.viewGrid.resetEmptyMessage;
        this.filterData = this.view.viewGrid.filterData;
        this.loadingDataMessage = this.view.viewGrid.loadingDataMessage;
        this.emptyMessage = this.view.viewGrid.emptyMessage;
        this.noDataEmptyMessage = this.view.viewGrid.noDataEmptyMessage;
        this.filterNoRecordsEmptyMessage = this.view.viewGrid.filterNoRecordsEmptyMessage;
      },

      onFetchData: function (criteria, requestProperties) {
        requestProperties = requestProperties || {};
        requestProperties.params = this.getFetchRequestParams(requestProperties.params);
      },

      getFetchRequestParams: function (params) {
        params = params || {};
        if (this.getSelectedRecord()) {
          params._targetRecordId = this.targetRecordId;
        }
        return params;
      },

      dataArrived: function () {
        var record, rowNum, i, selectedRecords = [],
            ds, ids;
        this.Super('dataArrived', arguments);
      },

      fields: this.treeGridFields,

      handleFilterEditorSubmit: function (criteria, context) {
        this.Super('handleFilterEditorSubmit', [criteria, context]);
      },

      setDataSource: function (ds, fields) {
        var me = this;
        ds.transformRequest = function (dsRequest) {
          var target = window[dsRequest.componentId];
          dsRequest.params = dsRequest.params || {};
          dsRequest.params.treeReferenceId = target.treeReferenceId;
          return this.Super('transformRequest', arguments);
        };

        fields = this.treePopup.treeGridFields;
        ds.primaryKeys = {
          id: 'id'
        };
        return this.Super("setDataSource", [ds, fields]);
      },

      // Todo: duplicated code
      getCellCSSText: function (record, rowNum, colNum) {
        if (record.notFilterHit) {
          return "color:#606060;";
        } else {
          return "";
        }
      },

      // show or hide the filter button
      filterEditorSubmit: function (criteria) {
        this.checkShowFilterFunnelIcon(criteria);
      },

      clearFilter: function (keepFilterClause, noPerformAction) {
        var i = 0,
            fld, length;
        this.forceRefresh = true;
        if (this.filterEditor) {
          if (this.filterEditor.getEditForm()) {
            this.filterEditor.getEditForm().clearValues();

            // clear the date values in a different way
            length = this.filterEditor.getEditForm().getFields().length;

            for (i = 0; i < length; i++) {
              fld = this.filterEditor.getEditForm().getFields()[i];
              if (fld.clearFilterValues) {
                fld.clearFilterValues();
              }
            }
          } else {
            this.filterEditor.setValuesAsCriteria(null);
          }
        }
        if (!noPerformAction) {
          this.filterEditor.performAction();
        }
      }
    });


    this.items = [isc.VLayout.create({
      height: this.height,
      width: this.width,
      members: [this.treeGrid, isc.HLayout.create({
        styleName: this.buttonBarStyleName,
        height: 40,
        defaultLayoutAlign: 'center',
        members: [isc.LayoutSpacer.create({}), okButton, isc.LayoutSpacer.create({
          width: 20
        }), cancelButton, isc.LayoutSpacer.create({})]
      })]
    })];
    this.Super('initWidget', arguments);
  },

  show: function (refreshGrid) {
    if (refreshGrid) {
      this.treeGrid.invalidateCache();
    }
    var ret = this.Super('show', arguments);
    if (this.treeGrid.isDrawn()) {
      this.treeGrid.focusInFilterEditor();
    } else {
      isc.Page.setEvent(isc.EH.IDLE, this.treeGrid, isc.Page.FIRE_ONCE, 'focusInFilterEditor');
    }

    this.treeGrid.checkShowFilterFunnelIcon(this.treeGrid.getCriteria());

    return ret;
  },

  resized: function () {
    this.items[0].setWidth(this.width - 4);
    this.items[0].setHeight(this.height - 40);
    this.items[0].redraw();
    return this.Super('resized', arguments);
  },

  accept: function () {
    if (this.changeCriteriacallback) {
      this.fireCallback(this.changeCriteriacallback, 'value', [this.getCriteria()]);
    }
    this.hide();
  },

  clearValues: function () {
    this.treeGrid.deselectAllRecords();
  },

  getCriteria: function () {
    var selection = this.treeGrid.getSelection(),
        criteria = {},
        i, len = selection.length,
        fieldName = this.fieldName + OB.Constants.FIELDSEPARATOR + OB.Constants.IDENTIFIER;
    if (len === 0) {
      return {};
    } else if (len === 1) {
      criteria._constructor = 'AdvancedCriteria';
      criteria.operator = 'and';
      criteria.criteria = [{
        fieldName: fieldName,
        operator: 'equals',
        value: selection[0][OB.Constants.IDENTIFIER]
      }];
    } else {
      criteria._constructor = 'AdvancedCriteria';
      criteria._OrExpression = true; // trick to get a really _or_ in the backend
      criteria.operator = 'or';
      criteria.fieldName = fieldName;
      criteria.criteria = [];
      for (i = 0; i < len; i++) {
        criteria.criteria.push({
          fieldName: fieldName,
          operator: 'equals',
          value: selection[i][OB.Constants.IDENTIFIER]
        });
      }
    }
    return criteria;
  }

});

isc.ClassFactory.defineClass('OBTreeFilterItem', isc.OBTextItem);

isc.OBTreeFilterItem.addProperties({
  showPickerIcon: true,
  filterDialogConstructor: isc.OBTreeItemPopupFilterWindow,
  lastValueFromPopup: null,
  pickerIconDefaults: {
    name: 'showDateRange',
    src: '../web/org.openbravo.userinterface.smartclient/openbravo/skins/Default/org.openbravo.client.application/images/form/productCharacteristicsFilter_ico.png',
    width: 21,
    height: 21,
    showOver: false,
    showFocused: false,
    showFocusedWithItem: false,
    hspace: 0,
    click: function (form, item, icon) {
      if (!item.disabled) {
        item.showDialog();
      }
    }
  },

  filterDialogCallback: function (criteria) {
    this.grid.parentElement.setFilterEditorCriteria(criteria);
    this.lastValueFromPopup = this.getValue();
    this.form.grid.performAction();
  },

  init: function () {
    var field;
    this.Super('init', arguments);
    field = this.grid.getField(this.name);
    this.criteriaField = field.displayField;
    this.addAutoChild('filterDialog', {
      title: this.title,
      filterItem: this,
      treeGridFields: field.editorProperties.selectorGridFields,
      treeReferenceId: field.editorProperties.treeReferenceId,
      dataSourceId: field.editorProperties.dataSourceId,
      fieldName: field.name,
      view: this.grid.parentElement.view,
      changeCriteriacallback: this.getID() + '.filterDialogCallback(value)'
    }, 'isc.OBTreeItemPopupFilterWindow');
  },

  showDialog: function () {
    var hasChanged = false;
    if (this.lastValueFromPopup !== this.getValue()) {
      hasChanged = true;
    }
    this.filterDialog.show(hasChanged);
  }
});