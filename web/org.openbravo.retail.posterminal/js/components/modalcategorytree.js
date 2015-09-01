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
  kind: 'OB.UI.ScrollableTableHeader',
  name: 'OB.UI.ModalCategoryListHeader',
  style: 'padding: 10px; border-bottom: 1px solid #cccccc;',
  components: [{
    classes: 'product_category_title',
    style: 'background-color: #ffffff',
    name: 'title',
    content: ''
  }],
  tap: function () {
    var modal = this.owner.owner.owner.owner.owner;
    modal.$.body.$.listCategories.categoryUnselect();
    modal.doSelectCategoryTreeItem({
      category: new OB.Model.ProductCategory({
        id: '__all__',
        display: true,
        issummary: false,
        level: 0,
        parentId: "0",
        treeNode: "COLLAPSED",
        name: OB.I18N.getLabel('OBMOBC_SearchAllCategories'),
        _identifier: OB.I18N.getLabel('OBMOBC_SearchAllCategories')
      }),
      children: ''
    });
    modal.doHideThisPopup();
  },
  initComponents: function () {
    this.inherited(arguments);
    this.$.title.setContent(OB.I18N.getLabel('OBPOS_SearchAllCategories'));
  }
});

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
    showAllCategories: false
  },
  initComponents: function () {
    this.inherited(arguments);
    this.$.body.$.listCategories.$.categoryTable.renderHeader = 'OB.UI.ModalCategoryListHeader';
    this.$.closebutton.hide();
    this.$.header.hide();
  },
  executeOnShow: function () {
    this.$.body.$.listCategories.setStyle('margin-top: -10px');
    if (this.$.body.$.listCategories.$.categoryTable.selected) {
      this.$.body.$.listCategories.categoryExpandSelected();
      this.$.body.$.listCategories.$.categoryTable.$.theader.$.modalCategoryListHeader.$.title.setStyle('background-color: #ffffff;');
    } else {
      this.$.body.$.listCategories.$.categoryTable.$.theader.$.modalCategoryListHeader.$.title.setStyle('background-color: #f8e859;');
    }
  },
  init: function () {
    this.$.body.$.listCategories.categories.on('selected', function (category) {
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
        children: childrenIds
      });
      this.doHideThisPopup();
    }, this);
  }
});