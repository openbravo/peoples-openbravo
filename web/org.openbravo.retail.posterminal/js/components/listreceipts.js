/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global Backbone */

(function () {

  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};

  OB.COMP.ListReceipts = Backbone.View.extend({
    optionsid: 'ListReceipts',
    tag: 'div',
    className: 'row-fluid',
    contentView: [{
      tag: 'div',
      attributes: {
        'class': 'span12'
      },
      content: [{
        tag: 'div',
        attributes: {
          'style': 'border-bottom: 1px solid #cccccc;'
        }
      }, {
        tag: 'div',
        content: [{
          id: 'tableview',
          view: OB.UI.TableView.extend({
            renderLine: OB.COMP.RenderOrder
          })
        }]
      }]
    }],
    initialize: function () {
      this.options[this.optionsid] = this;
      OB.UTIL.initContentView(this);

      var me = this;

      this.receipt = this.options.modelorder;
      this.receiptlist = this.options.modelorderlist;
      this.tableview.registerCollection(this.receiptlist);

      this.receiptlist.on('click', function (model, index) {
        this.receiptlist.load(model);
      }, this);
    }
  });
}());