/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global Backbone */

(function() {

  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};


  enyo.kind({
    name: 'OB.UI.ReceiptsCounter',
    attributes: {
      style: 'position: absolute; top:0px; right: 0px;'
    },
    components: [{
      tag: 'button',
      classes: 'btnlink btnlink-gray',
      attributes: {
        style: 'position: relative; overflow: hidden; margin:0px; padding:0px; height:50px; width: 50px;',
        href: '#modalreceipts',
        'data-toggle': 'modal'
      },
      components: [{
        attributes: {
          style: 'position: absolute; top: -35px; right:-35px; background: #404040; height:70px; width: 70px; -webkit-transform: rotate(45deg); -moz-transform: rotate(45deg); -ms-transform: rotate(45deg); -transform: rotate(45deg);'
        }
      }, {
        name: 'counter',
        attributes: {
          style: 'position: absolute; top: 0px; right:0px; padding-top: 5px; padding-right: 10px; font-weight: bold; color: white;'
        }
      }]
    }],
    initComponents: function() {
      this.inherited(arguments);
      //FIXME
      //OB.UTIL.initContentView(this);
      this.$.counter.setContent("1");
      //      this.receiptlist = this.options.root.modelorderlist;
      //      this.receiptlist.on('reset add remove', function () {
      //        if (this.receiptlist.length > 1) {
      //          this.$el.show();
      //          this.counter.text((this.receiptlist.length - 1));
      //        } else {
      //          this.$el.hide();
      //          this.counter.html('&nbsp;');
      //        }
      //      }, this);
    }
  });

  //Refatored as enyo view -> OB.UI.ReceiptsCounter
  OB.COMP.ReceiptsCounter = Backbone.View.extend({
    tagName: 'div',
    attributes: {
      'style': 'position: absolute; top:0px; right: 0px;'
    },
    contentView: [{
      tag: 'button',
      attributes: {
        'class': 'btnlink btnlink-gray',
        'style': 'position: relative; overflow: hidden; margin:0px; padding:0px; height:50px; width: 50px;',
        'href': '#modalreceipts',
        'data-toggle': 'modal'
      },
      content: [{
        tag: 'div',
        attributes: {
          'style': 'position: absolute; top: -35px; right:-35px; background: #404040; height:70px; width: 70px; -webkit-transform: rotate(45deg); -moz-transform: rotate(45deg); -ms-transform: rotate(45deg); -transform: rotate(45deg);' // sqrt(2) * 50
        }
      }, {
        id: 'counter',
        tag: 'div',
        attributes: {
          'style': 'position: absolute; top: 0px; right:0px; padding-top: 5px; padding-right: 10px; font-weight: bold; color: white;'
        }
      }]
    }],
    initialize: function() {

      OB.UTIL.initContentView(this);

      this.receiptlist = this.options.root.modelorderlist;
      this.receiptlist.on('reset add remove', function() {
        if (this.receiptlist.length > 1) {
          this.$el.show();
          this.counter.text((this.receiptlist.length - 1));
        } else {
          this.$el.hide();
          this.counter.html('&nbsp;');
        }
      }, this);
    }
  });
}());