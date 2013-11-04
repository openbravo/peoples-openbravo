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
  tree: null,
  init: function () {
    this.Super('init', arguments);
    this.tree = isc.OBTreeItemTree.create({
      formItem: this
    });
    this.form.addChild(this.tree); // Added grid in the form to avoid position problems
  },

  moved: function () {
    this.tree.updatePosition();
    return this.Super('moved', arguments);
  },

  click: function () {
    this.tree.show();
    return this.Super('click', arguments);
  },
  focus: function () {
    this.tree.show();
    return this.Super('focus', arguments);
  },
  blur: function () {
    var me = this;
    setTimeout(function () {
      me.hideTreeIfNotFocused();
    }, 100);
    return this.Super('blur', arguments);
  },

  hideTreeIfNotFocused: function () {
    if (this.form.getFocusItem().ID !== this.ID) {
      this.tree.hide();
    }
  },

  changed: function (form, item, value) {
    this.fireOnPause('refreshTree', this.refreshTree, 500, this);
    return this.Super('changed', arguments);
  },

  refreshTree: function () {
    this.tree.fetchData();
  }
});

isc.ClassFactory.defineClass("OBTreeItemTree", isc.TreeGrid);

isc.OBTreeItemTree.addProperties({
  formItem: null,
  width: 150,
  fields: [{
    name: "Name"
  }, {
    name: "Job"
  }],
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
    var formItemWidth;
    if (this.formItem) {
      formItemWidth = this.formItem.getVisibleWidth();
      if (formItemWidth && formItemWidth - 2 > this.getWidth()) {
        this.setWidth(formItemWidth - 2);
      }
    }
    this.updatePosition();
    if (this.isEmpty() || this.formItem._hasChanged) {
      delete this.formItem._hasChanged;
      this.fetchData();
    }
    return this.Super('show', arguments);
  },

  updatePosition: function () {
    var me = this,
        interval;
    if (this.formItem) {
      this.placeNear(this.formItem.getPageLeft() + 2, this.formItem.getPageTop() + 26);
    }
  },

  setDataSource: function (ds, fields) {
    var me = this;
    ds.transformRequest = function (dsRequest) {
      dsRequest.params = dsRequest.params || {};
      //TODO: Do not hardcode!
      dsRequest.params.tableId = '25AECB3048E54220860BAA8F91813A55';
      dsRequest.params.tableTreeId = '3C762D1768204132B2D607C069397B40';
      return this.Super('transformRequest', arguments);
    };

    fields = [{
      title: 'name',
      name: 'commercialName',
      type: '_id_10'
    }];
    ds.primaryKeys = {
      id: 'id'
    };
    return this.Super("setDataSource", [ds, fields]);
  },
  rowDoubleClick: function (record, recordNum, fieldNum) {
    var id = record[OB.Constants.ID],
        identifier = record[OB.Constants.IDENTIFIER];
    if (!this.formItem.valueMap) {
      this.formItem.valueMap = {};
    }
    if (!this.formItem.valueMap[id]) {
      this.formItem.valueMap[id] = identifier;
    }
    this.formItem.setValue(id);
    this.hide();
  },

  fetchData: function (criteria, callback, requestProperties) {
    return this.Super("fetchData", [this.getCriteriaFromFormItem(), callback, requestProperties]);
  },

  getCriteriaFromFormItem: function () {
    var value = this.formItem.getValue(),
        criteria = {};
    if (!value) {
      return null;
    }
    if (OB.Utilities.isUUID(value)) {
      value = this.formItem.valueMap[value] ? this.formItem.valueMap[value] : value;
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