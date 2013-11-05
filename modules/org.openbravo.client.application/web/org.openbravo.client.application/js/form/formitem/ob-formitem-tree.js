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
    this.treeWindow = isc.OBTreeItemPopupWindow.create({
      // solves issue: https://issues.openbravo.com/view.php?id=17268
      title: (this.form && this.form.grid ? this.form.grid.getField(this.name).title : this.title),
      dataSource: this.optionDataSource,
      treeItem: this,
      valueField: this.valueField,
      displayField: this.displayField,
      treeGridFields: isc.shallowClone(this.treeGridFields)
    });

  },

  icons: [{
    src: OB.Styles.skinsPath + 'Default/org.openbravo.client.application/images/form/search_picker.png',
    width: 21,
    height: 21,
    click: function (form, item, icon) {
      item.openSelectorWindow();
    }
  }],

  openSelectorWindow: function () {
    if (this.treeWindow.treeGrid) {
      this.treeWindow.treeGrid.invalidateCache();
    }
    this.treeWindow.open();
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
    if (this.form.getFocusItem().ID !== this.ID) {
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
  }
});

isc.ClassFactory.defineClass("OBTreeItemTree", isc.TreeGrid);

isc.OBTreeItemTree.addProperties({
  treeItem: null,
  width: 150,
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
    //TODO:
    OB.Datasource.get('610BEAE5E223447DBE6FF672B703F72F', this, null, true);
    this.Super('init', arguments);
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
      //TODO: Do not hardcode!
      dsRequest.params.treeReferenceId = target.treeItem.treeReferenceId;
      dsRequest.params.tableTreeId = '3C762D1768204132B2D607C069397B40';
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
      selectionAppearance: this.selectionAppearance,

      // drawAllMaxCells is set to 0 to prevent extra reads of data
      // Smartclient will try to read until drawAllMaxCells has been reached
      drawAllMaxCells: 0,

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
      width: 300,
      height: 300,
      alternateRecordStyles: true,
      dataSource: this.dataSource,
      showFilterEditor: true,
      sortField: this.displayField,

      init: function () {
        //TODO:
        OB.Datasource.get('610BEAE5E223447DBE6FF672B703F72F', this, null, true);
        this.Super('init', arguments);
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
        if (this.targetRecordId) {
          record = this.data.find(this.treeItem.valueField, this.targetRecordId);
          rowNum = this.getRecordIndex(record);
          this.selectSingleRecord(record);
          // give grid time to draw
          this.fireOnPause('scrollRecordIntoView', this.scrollRecordIntoView, [rowNum, true], this);
          delete this.targetRecordId;
        }
      },
      fields: this.treeGridFields,
      recordDoubleClick: function () {
        treeWindow.setValueInField();
      },

      handleFilterEditorSubmit: function (criteria, context) {
        var ids = [],
            crit = {},
            len, i, c, found, fixedCriteria;
        if (!treeWindow.multiselect) {
          this.Super('handleFilterEditorSubmit', arguments);
          return;
        }

        if (criteria && criteria.criteria) {
          fixedCriteria = [];
          // remove from criteria dummy one created to preserve selected items
          for (i = 0; i < criteria.criteria.length; i++) {
            if (!criteria.criteria[i].dummyCriteria && criteria.criteria[i].fieldName !== '_treeDefinitionId') {
              fixedCriteria.push(criteria.criteria[i]);
            }
          }
          criteria.criteria = fixedCriteria;
        }

        len = this.treeItem.treeWindow.selectedIds.length;
        for (i = 0; i < len; i++) {
          ids.push({
            fieldName: 'id',
            operator: 'equals',
            value: this.treeItem.treeWindow.selectedIds[i]
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
            if (c[i].fieldName && c[i].fieldName !== '_treeDefinitionId' && c[i].value !== '') {
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
              operator: 'notNull',
              dummyCriteria: true
            });
          }
          crit.criteria.push(criteria); // original filter
        } else {
          crit = criteria;
        }
        this.Super('handleFilterEditorSubmit', [crit, context]);
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
          //TODO: Do not hardcode!
          dsRequest.params.treeReferenceId = target.treeItem.treeReferenceId;
          dsRequest.params.tableTreeId = '3C762D1768204132B2D607C069397B40';
          return this.Super('transformRequest', arguments);
        };

        fields = this.treeItem.treeGridFields;
        ds.primaryKeys = {
          id: 'id'
        };
        return this.Super("setDataSource", [ds, fields]);
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

    return ret;
  },

  open: function () {
    var treeWindow = this,
        callback, data;

    data = {
      '_treeDefinitionId': this.treeDefinitionId || this.treeItem.treeDefinitionId
    };

    // on purpose not passing the third boolean param
    if (this.treeItem && this.treeItem.form && this.treeItem.form.view && this.treeItem.form.view.getContextInfo) {
      isc.addProperties(data, this.treeItem.form.view.getContextInfo(false, true));
    } else if (this.view && this.view.sourceView && this.view.sourceView.getContextInfo) {
      isc.addProperties(data, this.view.sourceView.getContextInfo(false, true));
    }

    callback = function (resp, data, req) {
      treeWindow.fetchDefaultsCallback(resp, data, req);
    };
    OB.RemoteCallManager.call('org.openbravo.userinterface.selector.SelectorDefaultFilterActionHandler', data, data, callback);
  },

  fetchDefaultsCallback: function (rpcResponse, data, rpcRequest) {
    var defaultFilter = {};
    if (data) {
      defaultFilter = {}; // Reset filter
      isc.addProperties(defaultFilter, data);
    }

    // adds the selector id to filter used to get filter information
    defaultFilter._treeDefinitionId = this.treeItem.treeReferenceId;
    this.defaultFilter = defaultFilter;
    this.treeGrid.targetRecordId = this.treeItem.getValue();
    this.show(true);
  }
});