/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global window, B, Backbone */

(function() {

  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};

  enyo.kind({
    name: 'OB.UI.ButtonTabPayment',
    kind: 'OB.UI.ToolbarButtonTab',
    tabPanel: '#payment',
    tap: function() {
    	var keyboard = this.owner.owner.owner.owner.owner.$.keyboard;
    	keyboard.showToolbar('toolbarpayment');
    },
    attributes: {
      style: 'text-align: center; font-size: 30px;',
    },
    components: [{
      tag: 'span',
      attributes: {
        style: 'font-weight: bold; margin: 0px 5px 0px 0px;'
      },
      components: [{
        kind: 'OB.UI.Total'
      }]
    }],
    initComponents: function() {
      this.inherited(arguments);
      this.addRemoveClass('btnlink-gray', true);
    }
  });

  //refactored using enyo -> OB.UI.ButtonTabPayment
  OB.COMP.ButtonTabPayment = OB.COMP.ToolbarButtonTab.extend({
    tabpanel: '#payment',
    initialize: function() {
      OB.COMP.ToolbarButtonTab.prototype.initialize.call(this); // super.initialize();
      this.$el.append(B({
        kind: B.KindJQuery('div'),
        attr: {
          'style': 'text-align: center; font-size: 30px;'
        },
        content: [{
          kind: B.KindJQuery('span'),
          attr: {
            'style': 'font-weight: bold; margin: 0px 5px 0px 0px;'
          },
          content: [{
            kind: OB.COMP.Total
          }]
        }, {
          kind: B.KindJQuery('span'),
          content: [
          //OB.I18N.getLabel('OBPOS_LblPay')
          ]
        }]
      }, this.options).$el);
    },
    render: function() {
      OB.COMP.ToolbarButtonTab.prototype.render.call(this); // super.initialize();
      this.$el.removeClass('btnlink-gray');
      return this;
    },
    shownEvent: function(e) {
      this.options.keyboard.show('toolbarpayment');
    }
  });

  enyo.kind({
    name: 'OB.UI.TabPayment',
    classes: 'tab-pane',
    components: [{
      kind: 'OB.UI.Payment'
    }],
    makeId: function() {
      return 'payment';
    }
  });

  OB.COMP.TabPayment = Backbone.View.extend({
    tagName: 'div',
    attributes: {
      'id': 'payment',
      'class': 'tab-pane'
    },
    initialize: function() {
      var paymentCoins = new OB.COMP.Payment(this.options);
      this.$el.append(paymentCoins.$el);
    }
  });

}());