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
isc.ClassFactory.defineClass('OBTreeGrid', isc.TreeGrid);

isc.OBTreeGrid.addProperties({
  referencedTableId: null,
  parentTabRecordId: null,
  view: null,
  orderedTree: false,

  arrowKeyAction: "select",
  canPickFields: false,
  canDropOnLeaves: true,
  canHover: false,
  canReorderRecords: true,
  canAcceptDroppedRecords: true,
  dropIconSuffix: "into",
  showOpenIcons: false,
  showDropIcons: false,
  nodeIcon: null,
  folderIcon: null,
  autoFetchData: false,
  closedIconSuffix: "",
  selectionAppearance: "checkbox",
  showSelectedStyle: true,
  // the grid will be refreshed when:
  // - The tree category is LinkToParent and
  // - There has been at least a reparent
  needsViewGridRefresh: false,
  // Can't reparent with cascade selection 
  //  showPartialSelection: true,
  //  cascadeSelection: true,
  dataProperties: {
    modelType: "parent",
    rootValue: "0",
    idField: "nodeId",
    parentIdField: "parentId",
    openProperty: "isOpen"
  },

  initWidget: function () {
    this.Super('initWidget', arguments);
    if (this.orderedTree) {
      this.canSort = false;
    } else {
      this.canSort = true;
    }
  },

  setDataSource: function (ds, fields) {
    var me = this;

    ds.transformRequest = function (dsRequest) {
      dsRequest.params = dsRequest.params || {};
      dsRequest.params.referencedTableId = me.referencedTableId;
      me.parentTabRecordId = me.getParentTabRecordId();
      dsRequest.params.parentRecordId = me.parentTabRecordId;
      dsRequest.params.tabId = me.view.tabId;
      if (dsRequest.dropIndex || dsRequest.dropIndex === 0) {
        //Only send the index if the tree is ordered
        dsRequest = me.addOrderedTreeParameters(dsRequest);
      }
      if (!me.view.isShowingTree) {
        dsRequest.params.selectedRecords = me.getSelectedRecordsString();
      } else {
        delete dsRequest.params.selectedRecords;
      }
      dsRequest.params._selectedProperties = me.getSelectedPropertiesString();
      // Includes the context, it could be used in the hqlwhereclause
      isc.addProperties(dsRequest.params, me.view.getContextInfo(true, false));
      dsRequest.willHandleError = true;
      return this.Super('transformRequest', arguments);
    };

    ds.transformResponse = function (dsResponse, dsRequest, jsonData) {
      if (jsonData.response.message) {
        me.view.messageBar.setMessage(jsonData.response.message.messageType, null, jsonData.response.message.message);
      }
      return this.Super('transformResponse', arguments);
    };

    fields = this.getTreeGridFields(me.fields);
    ds.primaryKeys = {
      id: 'id'
    };
    return this.Super("setDataSource", [ds, fields]);
  },

  getTreeGridFields: function (fields) {
    var treeGridFields = isc.shallowClone(fields),
        i, nDeleted = 0;
    for (i = 0; i < treeGridFields.length; i++) {
      if (treeGridFields[i - nDeleted].name[0] === '_') {
        treeGridFields.splice(i - nDeleted, 1);
        nDeleted = nDeleted + 1;
      }
    }
    return treeGridFields;
  },

  addOrderedTreeParameters: function (dsRequest) {
    var childrenOfNewParent, prevNode, nextNode;
    if (this.orderedTree) {
      dsRequest.params.dropIndex = dsRequest.dropIndex;
      childrenOfNewParent = this.getData().getChildren(dsRequest.newParentNode);
      if (childrenOfNewParent.length !== 0) {
        if (dsRequest.dropIndex === 0) {
          nextNode = childrenOfNewParent[dsRequest.dropIndex];
          dsRequest.params.nextNodeId = nextNode.id;
        } else if (dsRequest.dropIndex === childrenOfNewParent.length) {
          prevNode = childrenOfNewParent[dsRequest.dropIndex - 1];
          dsRequest.params.prevNodeId = prevNode.id;
        } else {
          prevNode = childrenOfNewParent[dsRequest.dropIndex - 1];
          dsRequest.params.prevNodeId = prevNode.id;
          nextNode = childrenOfNewParent[dsRequest.dropIndex];
          dsRequest.params.nextNodeId = nextNode.id;
        }
      }
    }
    return dsRequest;
  },

  getSelectedRecordsString: function () {
    var selectedRecordsString = '[',
        first = true,
        selectedRecords = this.view.viewGrid.getSelectedRecords(),
        len = selectedRecords.length,
        i;
    for (i = 0; i < len; i++) {
      if (first) {
        first = false;
        selectedRecordsString = selectedRecordsString + "'" + selectedRecords[i][OB.Constants.ID] + "'";
      } else {
        selectedRecordsString = selectedRecordsString + ',' + "'" + selectedRecords[i][OB.Constants.ID] + "'";
      }
    }
    selectedRecordsString = selectedRecordsString + ']';
    return selectedRecordsString;
  },

  getParentTabRecordId: function () {
    var parentRecordId = null;
    if (!this.view.parentView) {
      return null;
    }
    return this.view.parentView.viewGrid.getSelectedRecord().id;
  },

  getSelectedPropertiesString: function () {
    var selectedProperties = '[',
        first = true,
        len = this.fields.length,
        i;
    for (i = 0; i < len; i++) {
      if (first) {
        first = false;
        selectedProperties = selectedProperties + "'" + this.fields[i].name + "'";
      } else {
        selectedProperties = selectedProperties + ',' + "'" + this.fields[i].name + "'";
      }
    }
    selectedProperties = selectedProperties + ']';
    return selectedProperties;
  },

  // smartclients transferNodes does not update the tree it a node is moved within its same parent
  // do it here
  transferNodes: function (nodes, folder, index, sourceWidget, callback) {
    var node, dataSource, oldValues, dragTree, dropNeighbor, dataSourceProperties, i;
    if (this.movedToSameParent(nodes, folder)) {
      dragTree = sourceWidget.getData();
      dataSource = this.getDataSource();
      for (i = 0; i < nodes.length; i++) {
        node = nodes[i];
        oldValues = isc.addProperties({}, node);
        dataSourceProperties = {
          oldValues: oldValues,
          parentNode: this.data.getParent(node),
          newParentNode: folder,
          dragTree: dragTree,
          draggedNode: node,
          draggedNodeList: nodes,
          dropIndex: index
        };
        if (index > 0) {
          dataSourceProperties.dropNeighbor = this.data.getChildren(folder)[index - 1];
        }
        this.updateDataViaDataSource(node, dataSource, dataSourceProperties, sourceWidget);
      }
    } else {
      if (this.treeStructure === 'LinkToParent') {
        this.needsViewGridRefresh = true;
      }
    }

    this.Super('transferNodes', arguments);
  },

  movedToSameParent: function (nodes, newParent) {
    var i, len = nodes.length;
    for (i = 0; i < len; i++) {
      if (nodes[i].parentId !== newParent.id) {
        return false;
      }
    }
    return true;
  },

  getNodeByID: function (nodeId) {
    var i, node, nodeList = this.data.getNodeList();
    for (i = 0; i < nodeList.length; i++) {
      node = nodeList[i];
      if (node.id === nodeId) {
        return node;
      }
    }
    return null;
  },

  setView: function (view) {
    this.view = view;
  },

  treeDataArrived: function () {
    var i, selectedRecords, node;
    selectedRecords = this.view.viewGrid.getSelectedRecords();
    for (i = 0; i < selectedRecords.length; i++) {
      node = this.getNodeByID(selectedRecords[i].id);
      this.selectRecord(node);
    }
  },

  recordDoubleClick: function (viewer, record, recordNum, field, fieldNum, value, rawValue) {
    this.view.editRecordFromTreeGrid(record, false, (field ? field.name : null));
  },

  show: function () {
    this.setFields(this.getTreeGridFields(this.view.viewGrid.getFields()));
    this.view.toolBar.updateButtonState();
    this.Super('show', arguments);
  },

  hide: function () {
    if (this.needsViewGridRefresh) {
      this.needsViewGridRefresh = false;
      this.view.viewGrid.refreshGrid();
    }
    this.Super('hide', arguments);
  },

  rowMouseDown: function (record, rowNum, colNum) {
    this.Super('rowMouseDown', arguments);
    if (!isc.EventHandler.ctrlKeyDown()) {
      this.deselectAllRecords();
    }
    this.selectRecord(rowNum);
  },

  recordClick: function (viewer, record, recordNum, field, fieldNum, value, rawValue) {
    if (isc.EH.getEventType() === 'mouseUp') {
      // Don't do anything on the mouseUp event, the record is actually selected in the mouseDown event
      return;
    }
    this.deselectAllRecords();
    this.selectRecord(recordNum);
  },

  selectionUpdated: function (record, recordList) {
    var me = this,
        callback = function () {
        me.delayedSelectionUpdated();
        };
    // wait 2 times longer than the fire on pause delay default
    this.fireOnPause('delayedSelectionUpdated_' + this.ID, callback, this.fireOnPauseDelay * 2);
  },

  delayedSelectionUpdated: function (record, recordList) {
    var selectedRecordId = this.getSelectedRecord() ? this.getSelectedRecord().id : null,
        length, tabViewPane, i;
    // refresh the tabs
    if (this.view.childTabSet) {
      length = this.view.childTabSet.tabs.length;
      for (i = 0; i < length; i++) {
        tabViewPane = this.view.childTabSet.tabs[i].pane;
        if (!selectedRecordId || selectedRecordId !== tabViewPane.parentRecordId) {
          tabViewPane.doRefreshContents(true);
        }
      }
    }
  },

  getCellCSSText: function (record, rowNum, colNum) {
    if (record.filterHit === true) {
      return "font-weight:bold;";
    } else {
      return "";
    }
  }

});