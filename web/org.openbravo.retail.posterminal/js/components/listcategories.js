/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global Backbone */


enyo.kind({
  name: 'OB.UI.ListCategories',
  components: [{
    style: 'padding: 10px; border-bottom: 1px solid #cccccc;',
    components: [{
      tag: 'h3',
      content: OB.I18N.getLabel('OBPOS_LblCategories')
    }]
  }, {
    name: 'categoryTable',
    kind: 'OB.UI.Table',
    renderEmpty: 'OB.UI.RenderEmpty',
    renderLine: 'OB.UI.RenderCategory'
  }],

  init: function() {
	  console.log('init categories');
    var me = this;
    this.categories = new OB.Collection.ProductCategoryList();
    this.$.categoryTable.setCollection(this.categories);

    function errorCallback(tx, error) {
      OB.UTIL.showError("OBDAL error: " + error);
    }

    function successCallbackCategories(dataCategories, me) {
      if (dataCategories && dataCategories.length > 0) {
        me.categories.reset(dataCategories.models);
      } else {
        me.categories.reset();
      }
    }

    OB.Dal.find(OB.Model.ProductCategory, null, successCallbackCategories, errorCallback, this);
  }
});

(function() {

  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};



  OB.COMP.ListCategories = Backbone.View.extend({
    optionsid: 'ListCategories',
    tag: 'div',
    contentView: [{
      tag: 'div',
      attributes: {
        'style': 'padding: 10px; border-bottom: 1px solid #cccccc;'
      },
      content: [{
        tag: 'h3',
        content: [
        OB.I18N.getLabel('OBPOS_LblCategories')]
      }]
    }, {
      id: 'tableview',
      view: OB.UI.TableView.extend({
        style: 'list',
        renderEmpty: OB.COMP.RenderEmpty,
        renderLine: OB.COMP.RenderCategory
      })
    }],
    initialize: function() {

      this.options.root[this.optionsid] = this;
      OB.UTIL.initContentView(this);

      this.receipt = this.options.root.modelorder;
      this.categories = new OB.Collection.ProductCategoryList();
      this.tableview.registerCollection(this.categories);

      this.receipt.on('clear', function() {
        if (this.categories.length > 0) {
          this.categories.at(0).trigger('selected', this.categories.at(0));
        }
      }, this);

      function errorCallback(tx, error) {
        OB.UTIL.showError("OBDAL error: " + error);
      }

      function successCallbackCategories(dataCategories, me) {
        if (dataCategories && dataCategories.length > 0) {
          me.categories.reset(dataCategories.models);
        } else {
          me.categories.reset();
        }
      }

      OB.Dal.find(OB.Model.ProductCategory, null, successCallbackCategories, errorCallback, this);
    }
  });
}());