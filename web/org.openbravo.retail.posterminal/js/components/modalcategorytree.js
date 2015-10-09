/*
 ************************************************************************************
 * Copyright (C) 2015 Openbravo S.L.U.
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
    this.$.body.$.listCategories.setStyle('margin-top: -10px');
    if (this.args.selectCategory) {
      var category = this.$.body.$.listCategories.categories.get(this.args.selectCategory);
      if (category) {
        category.trigger('selected');
        this.$.body.$.listCategories.$[this.$.body.$.listCategories.tableName].setSelectedModels([category], true);
      }
    }
    if (this.$.body.$.listCategories.$[this.$.body.$.listCategories.tableName].selected) {
      this.$.body.$.listCategories.categoryExpandSelected();
    }
  },
  init: function () {
    this.$.body.$.listCategories.categories.on('selected', function (category) {
      if (category) {
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