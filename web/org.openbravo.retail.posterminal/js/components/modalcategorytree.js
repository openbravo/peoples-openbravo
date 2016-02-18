/*
 ************************************************************************************
 * Copyright (C) 2015-2016 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global enyo, $, _ */

enyo.kind({
  kind: 'OB.UI.Modal',
  name: 'OB.UI.ModalCategoryTree',
  topPosition: '60px',
  style: 'width: 400px',
  events: {
    onHideThisPopup: '',
    onSelectCategoryTreeItem: ''
  },
  body: {
    kind: 'OB.UI.ListCategories',
    classes: 'product_category_search',
    showBestSellers: false,
    showAllCategories: true,
    tableName: 'searchCategoryTable'
  },
  initComponents: function () {
    this.inherited(arguments);
    this.$.body.$.listCategories.$[this.$.body.$.listCategories.tableName].$.theader.hide();
    this.$.closebutton.hide();
    this.$.header.hide();
  },
  executeOnShow: function () {
    var me = this;
    this.startShowing = true;
    this.$.body.$.listCategories.loadCategories(function () {
      me.$.body.$.listCategories.setStyle('margin-top: -10px');
      var showOnlyReal = me.args.showOnlyReal || false;
      _.each(me.$.body.$.listCategories.$[me.$.body.$.listCategories.tableName].$.tbody.children, function (item) {
        if (item.renderline.model.get('realCategory') === 'N') {
          item.setShowing(!showOnlyReal);
        }
      }, me);
      if (me.args.selectCategory) {
        var category = me.$.body.$.listCategories.categories.get(me.args.selectCategory);
        if (category) {
          category.trigger('selected');
          me.$.body.$.listCategories.$[me.$.body.$.listCategories.tableName].setSelectedModels([category], true);
        }
      }
      if (me.$.body.$.listCategories.$[me.$.body.$.listCategories.tableName].selected) {
        me.$.body.$.listCategories.categoryExpandSelected();
      }
      me.startShowing = false;
    });
  },
  init: function () {
    this.$.body.$.listCategories.categories.on('selected', function (category) {
      if (category && !this.startShowing) {
        if (this.args && this.args.notSelectSummary && category.get('issummary')) {
          this.$.body.$.listCategories.categoryExpandCollapse(this, {
            categoryId: category.get('id'),
            expand: category.get('treeNode') === 'COLLAPSED'
          });
          return;
        }
        var childrenIds = '',
            children = this.$.body.$.listCategories.categoryGetChildren(category.id);
        _.each(children, function (category) {
          if (childrenIds !== '') {
            childrenIds += ', ';
          }
          childrenIds += "'" + category.id + "'";
        });
        this.doSelectCategoryTreeItem({
          category: category,
          children: childrenIds,
          origin: this.args ? this.args.origin : null
        });
        this.doHideThisPopup();
      }
    }, this);
  }
});