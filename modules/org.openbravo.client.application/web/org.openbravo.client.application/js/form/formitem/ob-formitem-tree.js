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


// = Tree Item =
// Contains the OBTreeItem. This widget consists of three main parts:
// 1) a text item with a picker icon
// 2) a tree grid that will show data filtered by the text entered in the text item
// 3) a popup window showing a search grid and a tree grid with data
//
isc.ClassFactory.defineClass('OBTreeItem', isc.OBTextItem);

isc.ClassFactory.mixInInterface('OBTreeItem', 'OBLinkTitleItem');

isc.OBTreeItem.addProperties({
  showPickerIcon: true,
  pickerIconSrc: OB.Styles.skinsPath + 'Default/org.openbravo.client.application/images/form/comboBoxPicker.png',
  tree: null,
  init: function () {
    this.Super('init', arguments);
    this.tree = isc.OBTreeItemTree.create({
      treeItem: this
    });
    this.form.addChild(this.tree); // Added grid in the form to avoid position problems
    this.treeDisplayField = this.getTreeDisplayField();
    this.treeWindow = isc.OBTreeItemPopupWindow.create({
      // solves issue: https://issues.openbravo.com/view.php?id=17268
      title: (this.form && this.form.grid ? this.form.grid.getField(this.name).title : this.title),
      dataSource: this.optionDataSource,
      treeItem: this,
      valueField: this.valueField,
      displayField: this.displayField,
      treeGridFields: isc.shallowClone(this.treeGridFields)
    });
    this.enableShortcuts();
  },

  enableShortcuts: function () {
    var ksAction_ShowPopup, ksAction_ShowTree, ksAction_MoveToTree;
    ksAction_ShowPopup = function (caller) {
      caller.openTreeWindow();
      return false; //To avoid keyboard shortcut propagation
    };
    OB.KeyboardManager.Shortcuts.set('TreeItem_ShowPopup', 'OBTreeItem', ksAction_ShowPopup);

    ksAction_ShowTree = function (caller) {
      caller.tree.show();
      return false; //To avoid keyboard shortcut propagation
    };
    OB.KeyboardManager.Shortcuts.set('TreeItem_ShowTree', 'OBTreeItem', ksAction_ShowTree);

    ksAction_MoveToTree = function (caller) {
      if (caller.tree.isVisible()) {
        caller.tree.focus();
      }
      return false; //To avoid keyboard shortcut propagation
    };
    OB.KeyboardManager.Shortcuts.set('TreeItem_MoveToTree', 'OBTreeItem', ksAction_MoveToTree);
  },

  getTreeDisplayField: function () {
    if (!this.displayField.contains(OB.Constants.FIELDSEPARATOR)) {
      return this.displayField;
    } else {
      return this.displayField.substr(this.displayField.lastIndexOf(OB.Constants.FIELDSEPARATOR) + 1);
    }
  },

  keyPress: function (keyName, character, form, item, icon) {
    var response = OB.KeyboardManager.Shortcuts.monitor('OBTreeItem', this);
    if (response !== false) {
      response = this.Super('keyPress', arguments);
    }
    return response;
  },

  icons: [{
    src: OB.Styles.skinsPath + 'Default/org.openbravo.client.application/images/form/search_picker.png',
    width: 21,
    height: 21,
    click: function (form, item, icon) {
      item.openTreeWindow();
    }
  }],

  openTreeWindow: function () {
    var selectedValue = this.getValue(),
        criteria, innerCriteria;

    if (this.treeWindow.treeGrid) {
      if (OB.Utilities.isUUID(selectedValue)) {

        this.targetRecordId = selectedValue;
        this.targetRecordIdentifier = this.getDisplayValue();
      }
    }
    this.treeWindow.show(true);
  },

  showPicker: function () {
    this.toggleTreePicker();
  },

  toggleTreePicker: function () {
    this.pickerClicked = true;
    if (this.tree.isVisible()) {
      this.tree.hide();
    } else {
      this.tree.show();
    }
  },

  moved: function () {
    this.tree.updatePosition();
    return this.Super('moved', arguments);
  },

  blur: function () {
    var me = this;
    setTimeout(function () {
      me.hideTreeIfNotFocused();
    }, 100);
    return this.Super('blur', arguments);
  },

  focus: function () {
    this.tree.hide();
    return this.Super('focus', arguments);
  },

  click: function () {
    if (this.pickerClicked) {
      delete this.pickerClicked;
    } else {
      this.tree.hide();
    }
    return this.Super('click', arguments);
  },

  hideTreeIfNotFocused: function () {
    if (this.form && this.form.getFocusItem() && this.form.getFocusItem().ID !== this.ID) {
      this.tree.hide();
    }
  },

  changed: function (form, item, value) {
    if (!this.tree.isVisible()) {
      this.tree.show();
    }
    this.fireOnPause('refreshTree', this.refreshTree, 500, this);
    return this.Super('changed', arguments);
  },

  refreshTree: function () {
    this.tree.fetchData();
  },

  setValueFromRecord: function (record, fromPopup) {
    var currentValue = this.getValue(),
        identifierFieldName = this.name + OB.Constants.FIELDSEPARATOR + OB.Constants.IDENTIFIER,
        i;
    if (!record) {
      this.storeValue(null);
      this.form.setValue(this.name + OB.Constants.FIELDSEPARATOR + this.displayField, null);
      this.form.setValue(identifierFieldName, null);
    } else {
      this.storeValue(record[this.valueField]);
      this.form.setValue(this.name + OB.Constants.FIELDSEPARATOR + this.displayField, record[this.displayField]);
      this.form.setValue(identifierFieldName, record[OB.Constants.IDENTIFIER]);
      if (!this.valueMap) {
        this.valueMap = {};
      }

      this.valueMap[record[this.valueField]] = record[this.treeDisplayField].replace(/[\n\r]/g, '');
      this.updateValueMap();
    }

    // only jump to the next field if the value has really been set
    // do not jump to the next field if the event has been triggered by the Tab key,
    // to prevent a field from being skipped (see https://issues.openbravo.com/view.php?id=21419)
    if (currentValue && this.form.focusInNextItem && isc.EH.getKeyName() !== 'Tab') {
      this.form.focusInNextItem(this.name);
    }
    delete this._notUpdatingManually;
  }

});

isc.ClassFactory.defineClass("OBTreeItemTree", isc.TreeGrid);

isc.OBTreeItemTree.addProperties({
  treeItem: null,
  height: 200,
  showOpenIcons: false,
  showDropIcons: false,
  autoFetchData: false,
  nodeIcon: null,
  folderIcon: null,
  visibility: 'hidden',
  dataProperties: {
    modelType: "parent",
    rootValue: "0",
    idField: "nodeId",
    parentIdField: "parentId",
    openProperty: "isOpen"
  },


  init: function () {
    OB.Datasource.get(this.treeItem.dataSourceId, this, null, true);
    this.Super('init', arguments);
  },

  dataArrived: function () {
    var selectedValue, record, rowNum;
    this.Super('dataArrived', arguments);
    selectedValue = this.treeItem.getValue();
    record = this.data.find('id', selectedValue);
    //If there is a record selected in the item, select it
    if (record) {
      rowNum = this.getRecordIndex(record);
      this.selectSingleRecord(record);
      // give grid time to draw
      this.fireOnPause('scrollRecordIntoView', this.scrollRecordIntoView, [rowNum, true], this);
    }
  },


  show: function () {
    var treeItemWidth;
    if (this.treeItem) {
      treeItemWidth = this.treeItem.getVisibleWidth();
      if (treeItemWidth && treeItemWidth - 2 > this.getWidth()) {
        this.setWidth(treeItemWidth - 2);
      }
    }
    this.updatePosition();
    if (this.isEmpty()) {
      this.fetchData();
    }
    return this.Super('show', arguments);
  },

  updatePosition: function () {
    var me = this,
        interval;
    if (this.treeItem) {
      this.placeNear(this.treeItem.getPageLeft() + 2, this.treeItem.getPageTop() + 26);
    }
  },

  setDataSource: function (ds, fields) {
    var me = this;
    ds.transformRequest = function (dsRequest) {
      var target = window[dsRequest.componentId];
      dsRequest.params = dsRequest.params || {};
      dsRequest.params.treeReferenceId = target.treeItem.treeReferenceId;
      return this.Super('transformRequest', arguments);
    };

    fields = this.treeItem.pickListFields;
    ds.primaryKeys = {
      id: 'id'
    };
    return this.Super("setDataSource", [ds, fields]);
  },
  rowDoubleClick: function (record, recordNum, fieldNum) {
    var id = record[OB.Constants.ID],
        identifier = record[OB.Constants.IDENTIFIER];
    if (!this.treeItem.valueMap) {
      this.treeItem.valueMap = {};
    }
    if (!this.treeItem.valueMap[id]) {
      this.treeItem.valueMap[id] = identifier;
    }
    this.treeItem.setElementValue(identifier);
    this.treeItem.updateValue();
    this.treeItem.form.view.toolBar.updateButtonState(true);
    this.hide();
  },

  fetchData: function (criteria, callback, requestProperties) {
    return this.Super("fetchData", [this.getCriteriaFromTreeItem(), callback, requestProperties]);
  },

  getCriteriaFromTreeItem: function () {
    var value = this.treeItem.getValue(),
        criteria = {};
    if (!value) {
      return null;
    }
    if (OB.Utilities.isUUID(value)) {
      value = this.treeItem.valueMap[value] ? this.treeItem.valueMap[value] : value;
    }
    criteria.fieldName = this.getFields()[0].name;
    criteria.operator = 'iContains';
    criteria.value = value;
    return {
      criteria: criteria
    };
  },

  // Todo: duplicated code
  getCellCSSText: function (record, rowNum, colNum) {
    if (record.notFilterHit) {
      return "color:#606060;";
    } else {
      return "";
    }
  }
});

isc.ClassFactory.defineClass('OBTreeItemPopupWindow', isc.OBPopup);

isc.OBTreeItemPopupWindow.addProperties({
  canDragReposition: true,
  canDragResize: true,
  dismissOnEscape: true,
  showMaximizeButton: true,
  multiselect: false,

  defaultTreeGridField: {
    canFreeze: true,
    canGroupBy: false
  },

  initWidget: function () {
    var treeWindow = this,
        okButton, cancelButton, operator, i;


    //TODO:
    //    this.setFilterEditorProperties(this.treeGridFields);
    okButton = isc.OBFormButton.create({
      title: OB.I18N.getLabel('OBUISC_Dialog.OK_BUTTON_TITLE'),
      click: function () {
        treeWindow.setValueInField();
      }
    });
    cancelButton = isc.OBFormButton.create({
      title: OB.I18N.getLabel('OBUISC_Dialog.CANCEL_BUTTON_TITLE'),
      click: function () {
        treeWindow.closeClick();
      }
    });

    OB.Utilities.applyDefaultValues(this.treeGridFields, this.defaultTreeGridField);

    if (this.treeItem.popupTextMatchStyle === 'substring') {
      operator = 'iContains';
    } else {
      operator = 'iStartsWith';
    }

    for (i = 0; i < this.treeGridFields.length; i++) {
      this.treeGridFields[i].canSort = (this.treeGridFields[i].canSort === false ? false : true);
      if (this.treeGridFields[i].disableFilter) {
        this.treeGridFields[i].canFilter = false;
      } else {
        this.treeGridFields[i].canFilter = true;
      }
    }
    if (!this.dataSource.fields || !this.dataSource.fields.length || this.dataSource.fields.length === 0) {
      this.dataSource.fields = this.treeGridFields;
      this.dataSource.init();
    }
    this.treeGrid = isc.TreeGrid.create({

      treeItem: this.treeItem,
      treeWindow: this,
      view: this.treeItem.form.view,
      selectionAppearance: this.selectionAppearance,
      treePopup: this,
      showOpenIcons: false,
      showDropIcons: false,
      autoFetchData: false,
      nodeIcon: null,
      folderIcon: null,
      filterOnKeypress: true,

      dataProperties: {
        modelType: "parent",
        rootValue: "0",
        idField: "nodeId",
        parentIdField: "parentId",
        openProperty: "isOpen"
      },

      //TODO:
      //      width: this.treeGridProperties.width,
      //      height: this.treeGridProperties.height,
      width: '100%',
      height: '100%',
      showFilterEditor: true,
      alternateRecordStyles: true,
      dataSource: this.dataSource,
      sortField: this.displayField,

      init: function () {
        OB.Datasource.get(this.treeItem.dataSourceId, this, null, true);
        this.copyFunctionsFromViewGrid();
        this.Super('init', arguments);
      },

      copyFunctionsFromViewGrid: function () {
        var view = this.treeItem.form.view;
        this.filterEditorProperties = view.viewGrid.filterEditorProperties;
        this.checkShowFilterFunnelIconSuper = view.viewGrid.checkShowFilterFunnelIcon;
        this.isGridFiltered = view.viewGrid.isGridFiltered;
        this.isGridFilteredWithCriteria = view.viewGrid.isGridFilteredWithCriteria;
        this.isValidFilterField = view.viewGrid.isValidFilterField;
        this.convertCriteria = view.viewGrid.convertCriteria;
        this.resetEmptyMessage = view.viewGrid.resetEmptyMessage;
        this.filterData = view.viewGrid.filterData;
        this.loadingDataMessage = view.viewGrid.loadingDataMessage;
        this.emptyMessage = view.viewGrid.emptyMessage;
        this.noDataEmptyMessage = view.viewGrid.noDataEmptyMessage;
        this.filterNoRecordsEmptyMessage = view.viewGrid.filterNoRecordsEmptyMessage;
      },

      onFetchData: function (criteria, requestProperties) {
        requestProperties = requestProperties || {};
        requestProperties.params = this.getFetchRequestParams(requestProperties.params);
      },

      getFetchRequestParams: function (params) {
        params = params || {};
        //        if (this.treeItem) {
        //          isc.OBSelectorItem.prepareDSRequest(params, this.selector);
        //        }
        //        params._requestType = 'Window';
        if (this.getSelectedRecord()) {
          params._targetRecordId = this.targetRecordId;
        }
        return params;
      },

      dataArrived: function () {
        var record, rowNum, i, selectedRecords = [],
            ds, ids;
        this.Super('dataArrived', arguments);
        if (this.treeItem.targetRecordId) {
          record = this.data.find(OB.Constants.ID, this.treeItem.targetRecordId);
          rowNum = this.getRecordIndex(record);
          this.selectSingleRecord(record);
          // give grid time to draw
          this.fireOnPause('scrollRecordIntoView', this.scrollRecordIntoView, [rowNum, true], this);
          delete this.treeItem.targetRecordId;
        }
      },
      fields: this.treeGridFields,
      recordDoubleClick: function () {
        this.treeWindow.setValueInField();
      },

      handleFilterEditorSubmit: function (criteria, context) {
        var innerCriteria;
        if (this.treeItem.targetRecordId) {
          innerCriteria = {};
          innerCriteria.fieldName = OB.Constants.ID;
          innerCriteria.operator = 'equals';
          innerCriteria.value = this.treeItem.targetRecordId;
          criteria = {
            _constructor: 'AdvancedCriteria',
            criteria: [innerCriteria],
            operator: 'and'
          };
        }
        this.Super('handleFilterEditorSubmit', [criteria, context]);
      },

      setFilterValues: function (criteria) {
        var innerCriteria;
        if (criteria && criteria.criteria && criteria.criteria[0] && criteria.criteria[0].fieldName === OB.Constants.ID) {
          // Target record id criteria. Show the identifier of the row in the filter values
          innerCriteria = {};
          innerCriteria.fieldName = this.treeItem.treeDisplayField;
          innerCriteria.operator = 'iContains';
          innerCriteria.value = this.treeItem.getDisplayValue();
          criteria = {
            _constructor: 'AdvancedCriteria',
            criteria: [innerCriteria],
            operator: 'and'
          };
        }
        this.Super('setFilterValues', criteria);
      },

      selectionChanged: function (record, state) {
        if (this.treeItem.treeWindow.selectedIds) {
          if (state) {
            this.treeItem.treeWindow.selectId(record[OB.Constants.ID]);
          } else {
            this.treeItem.treeWindow.selectedIds.remove(record[OB.Constants.ID]);
          }
          this.markForRedraw('Selection changed');
        }

        this.Super('selectionChanged', arguments);
      },

      setDataSource: function (ds, fields) {
        var me = this;
        ds.transformRequest = function (dsRequest) {
          var target = window[dsRequest.componentId];
          dsRequest.params = dsRequest.params || {};
          dsRequest.params.treeReferenceId = target.treeItem.treeReferenceId;
          return this.Super('transformRequest', arguments);
        };

        fields = this.treePopup.treeGridFields;
        ds.primaryKeys = {
          id: 'id'
        };
        return this.Super("setDataSource", [ds, fields]);
      },

      setCriteria: function () {
        //setCriteria!
        return this.Super("setCriteria", arguments);
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
      },

      checkShowFilterFunnelIcon: function (criteria) {
        var innerCriteria;
        if (criteria && criteria.criteria && criteria.criteria[0] && criteria.criteria[0].fieldName === OB.Constants.ID) {
          // Target record id criteria. Show the identifier of the row in the filter values
          innerCriteria = {};
          innerCriteria.fieldName = this.treeItem.treeDisplayField;
          innerCriteria.operator = 'iContains';
          innerCriteria.value = this.treeItem.getDisplayValue();
          criteria = {
            _constructor: 'AdvancedCriteria',
            criteria: [innerCriteria],
            operator: 'and'
          };
        }
        this.checkShowFilterFunnelIconSuper(criteria);
      }
    });

    this.items = [this.treeGrid, isc.HLayout.create({
      styleName: this.buttonBarStyleName,
      height: this.buttonBarHeight,
      defaultLayoutAlign: 'center',
      members: [isc.LayoutSpacer.create({}), okButton, isc.LayoutSpacer.create({
        width: this.buttonBarSpace
      }), cancelButton, isc.LayoutSpacer.create({})]
    })];
    this.Super('initWidget', arguments);
  },

  closeClick: function () {
    this.hide(arguments);
    this.treeItem.focusInItem();
  },

  hide: function () {
    this.Super('hide', arguments);
    //focus is now moved to the next item in the form automatically
    if (!this.treeItem.form.getFocusItem()) {
      this.treeItem.focusInItem();
    }
  },

  show: function (applyDefaultFilter) {
    // draw now already otherwise the filter does not work the
    // first time    
    var ret = this.Super('show', arguments);
    if (applyDefaultFilter) {
      this.treeGrid.setFilterEditorCriteria(this.defaultFilter);
      this.treeGrid.filterByEditor();
    }
    if (this.treeGrid.isDrawn()) {
      this.treeGrid.focusInFilterEditor();
    } else {
      isc.Page.setEvent(isc.EH.IDLE, this.treeGrid, isc.Page.FIRE_ONCE, 'focusInFilterEditor');
    }

    if (this.treeItem.getValue()) {
      this.treeGrid.selectSingleRecord(this.treeGrid.data.find(this.valueField, this.treeItem.getValue()));
    } else {
      this.treeGrid.selectSingleRecord(null);
    }

    this.treeGrid.checkShowFilterFunnelIcon(this.treeGrid.getCriteria());

    return ret;
  },


  setValueInField: function () {
    this.treeItem.setValueFromRecord(this.treeGrid.getSelectedRecord(), true);
    this.hide();
  }

});