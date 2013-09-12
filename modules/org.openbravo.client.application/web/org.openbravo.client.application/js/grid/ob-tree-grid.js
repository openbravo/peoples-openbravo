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
    parentRecordId: null,
    
    setDataSource: function(ds, fields) {
        var me = this;
        ds.transformRequest = function(dsRequest) {
            dsRequest.params = dsRequest.params || {};
            dsRequest.params.referencedTableId =  me.referencedTableId;
            dsRequest.params.parentRecordId =  me.parentRecordId;
            return this.Super('transformRequest', arguments);
        };
        ds.primaryKeys = {id:'id'};
        return this.Super("setDataSource", [ds, fields]);
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
    destroyOnClose: true,

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
        click: function() {
            this.creator.accept();
        }
    },

    clearButtonDefaults: {
        _constructor: 'OBFormButton',
        height: 22,
        width: 80,
        canFocus: true,
        autoParent: 'buttonLayout',
        click: function() {
            this.creator.clearValues();
        }
    },

    cancelButtonDefaults: {
        _constructor: 'OBFormButton',
        height: 22,
        width: 80,
        canFocus: true,
        autoParent: 'buttonLayout',
        click: function() {
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
    accept: function() {
        if (this.callback) {
            this.fireCallback(this.callback, 'value', [this.getValue()]);
        }
        this.hide();
    },

    clearValues: function() {
        this.tree.deselectAllRecords();
    },

    cancel: function() {
        this.hide();
    },

    initWidget: function() {
        var me = this,
            dataArrived, checkInitialNodes, getNodeByID;

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
        this.tree = isc.OBTreeGrid.create({
            referencedTableId: '3C7E0EEF116B4AD383AD9E25E712F281',
            parentRecordId: null,
            width: 500,
            height: 400,
            canReorderRecords: true,
            canAcceptDroppedRecords: true,
            dropIconSuffix: "into",
            showOpenIcons: false,
            showDropIcons: false,
            nodeIcon: null,
            folderIcon: null,
            autoFetchData: true,
            closedIconSuffix: "",
            dataProperties: {
                modelType: "parent",
                rootValue: "0",
                nameProperty: "Name",
                idField: "id",
                parentIdField: "parentId",
                openProperty: "isOpen"
            }
        });

        OB.Datasource.get('90034CAE96E847D78FBEF6D38CB1930D', this.tree, null, true);

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
    }
});