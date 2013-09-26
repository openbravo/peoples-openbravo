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
  autoFetchData: true,
  closedIconSuffix: "",
  selectionAppearance: "checkbox",
  showSelectedStyle: true,
  // Can't reparent with cascade selection 
  //  showPartialSelection: true,
  //  cascadeSelection: true,
  dataProperties: {
    modelType: "parent",
    rootValue: "0",
    idField: "id",
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

      dsRequest.params.selectedRecords = me.getSelectedRecordsString();
      dsRequest.params._selectedProperties = me.getSelectedPropertiesString();
      return this.Super('transformRequest', arguments);
    };
    if (!fields) {
      fields = me.fields;
    }
    ds.primaryKeys = {
      id: 'id'
    };
    return this.Super("setDataSource", [ds, fields]);
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
  }
});

isc.ClassFactory.defineClass('OBTreeGridPopup', isc.OBPopup);

isc.OBTreeGridPopup.addProperties({
  isModal: true,
  showModalMask: true,
  dismissOnEscape: true,
  autoCenter: true,
  autoSize: true,
  vertical: true,
  showMinimizeButton: false,
  destroyOnClose: false,
  referencedTableId: null,
  parentRecordId: null,
  visibility: 'hidden',

  mainLayoutDefaults: {
    _constructor: 'VLayout',
    width: 380,
    height: 105,
    layoutMargin: 5
  },

  buttonLayoutDefaults: {
    _constructor: 'HLayout',
    width: '100%',
    height: 22,
    layoutAlign: 'right',
    align: 'right',
    membersMargin: 5,
    autoParent: 'mainLayout'
  },

  okButtonDefaults: {
    _constructor: 'OBFormButton',
    height: 22,
    width: 80,
    canFocus: true,
    autoParent: 'buttonLayout',
    click: function () {
      this.creator.accept();
    }
  },

  clearButtonDefaults: {
    _constructor: 'OBFormButton',
    height: 22,
    width: 80,
    canFocus: true,
    autoParent: 'buttonLayout',
    click: function () {
      this.creator.clearValues();
    }
  },

  cancelButtonDefaults: {
    _constructor: 'OBFormButton',
    height: 22,
    width: 80,
    canFocus: true,
    autoParent: 'buttonLayout',
    click: function () {
      this.creator.cancel();
    }
  },

  //	  /**
  //	   * Based on values selected in the tree, returns the ones that are
  //	   * going to be used for visualization and/or filtering:
  //	   *
  //	   *   -Filtering: includes all selected leaf nodes
  //	   *   -Visualization: includes the top in branch fully selected nodes
  //	   */
  //	  getValue: function () {
  //	  },
  accept: function () {
    if (this.callback) {
      this.fireCallback(this.callback, 'value', [this.getValue()]);
    }
    this.hide();
  },

  clearValues: function () {
    this.tree.deselectAllRecords();
  },

  cancel: function () {
    this.hide();
  },

  initWidget: function () {
    var me = this,
        dataArrived, checkInitialNodes, getNodeByID, gridFields;

    this.Super('initWidget', arguments);

    this.addAutoChild('mainLayout');

    //	    /**
    //	     * Overrides dataArrived to initialize the tree initial selection
    //	     * based on the filter initial criteria
    //	     */
    //	    dataArrived = function () {
    //	      var internalValue, nodeList, i, j;
    //	      this.Super('dataArrived', arguments);
    //	      if (this.topElement && this.topElement.creator && this.topElement.creator.internalValue) {
    //	        this.checkInitialNodes(this.topElement.creator.internalValue);
    //	      }
    //	    };
    //	    /**
    //	     * Marks the checkboxes of the nodes that
    //	     * are present in the initial criteria
    //	     */
    //	    checkInitialNodes = function (internalValue) {
    //	      var c, v, value, node, characteristic;
    //	      for (c in internalValue) {
    //	        if (internalValue.hasOwnProperty(c)) {
    //	          characteristic = internalValue[c];
    //	          for (v = 0; v < characteristic.values.length; v++) {
    //	            value = characteristic.values[v];
    //	            if (value.filter) {
    //	              node = this.getNodeByID(value.value);
    //	              if (node) {
    //	                this.selectRecord(node);
    //	              }
    //	            }
    //	          }
    //	        }
    //	      }
    //	    };
    //	    /**
    //	     * Returns a tree node given its id
    //	     */
    //	    getNodeByID = function (nodeId) {
    //	      var i, node, nodeList = this.data.getNodeList();
    //	      for (i = 0; i < nodeList.length; i++) {
    //	        node = nodeList[i];
    //	        if (node.id === nodeId) {
    //	          return node;
    //	        }
    //	      }
    //	      return null;
    //	    };
    gridFields = this.getTreeGridFields();

    this.tree = isc.OBTreeGrid.create({
      view: this.view,
      referencedTableId: this.referencedTableId,
      fields: gridFields,
      orderedTree: this.orderedTree,
      treeStructure: this.treeStructure,

      width: 500,
      height: 400
    });

    OB.Datasource.get(this.dataSourceId, this.tree, null, true);

    this.mainLayout.addMember(this.tree);
    this.addAutoChild('buttonLayout');
    this.addAutoChild('okButton', {
      canFocus: true,
      title: OB.I18N.getLabel('OBUISC_Dialog.OK_BUTTON_TITLE')
    });
    this.addAutoChild('clearButton', {
      canFocus: true,
      title: OB.I18N.getLabel('OBUIAPP_Clear')
    });
    this.addAutoChild('cancelButton', {
      canFocus: true,
      title: OB.I18N.getLabel('OBUISC_Dialog.CANCEL_BUTTON_TITLE')
    });
    this.addItem(this.mainLayout);
  },

  getTreeGridFields: function () {
    var fields = isc.clone(this.view.gridFields),
        i, nDeleted = 0;
    for (i = 0; i < fields.length; i++) {
      if (fields[i].name[0] === '_') {
        fields.splice(i - nDeleted, 1);
        nDeleted = nDeleted + 1;
      }
    }
    return fields;
  },

  show: function () {
    var callback;
    // If the parent record id has changed, fetch the data again
    var currentParentTabRecordId = this.tree.getParentTabRecordId();
    if (currentParentTabRecordId !== this.tree.parentTabRecordId) {
      callback = function () {
        var me = this;
        me.show();
      };
      this.tree.fetchData(null, callback);
    }
    return this.Super('show', arguments);
  },

  hide: function () {
    this.Super('hide', arguments);
    if (this.treeStructure === 'LinkToParent') {
      this.view.viewGrid.refreshGrid();
    }
  }
});