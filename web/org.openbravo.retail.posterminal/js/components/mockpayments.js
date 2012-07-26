/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global window, Backbone */

(function () {

  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};

  var OKButton = OB.COMP.Button.extend({
    render: function () {
      this.$el.addClass('btnlink');
      this.$el.css('float', 'right');
      this.$el.text('OK');
      return this;
    },
    clickEvent: function (e) {
      var parent = this.options.parent;
      parent.receipt.addPayment(new OB.Model.PaymentLine({
        'kind': parent.key,
        'name': parent.name,
        'amount': parent.amount
      }));
      parent.$el.parents('.modal').filter(':first').modal('hide'); // If in a modal dialog, close it
    }
  });

  OB.COMP.MockPayment = Backbone.View.extend({
    tagName: 'div',
    contentView: [{
      tag: 'div',
      content: [{
        tag: 'div',
        attributes: {
          'class': 'row-fluid'
        },
        content: [{
          tag: 'div',
          attributes: {
            'class': 'span6'
          },
          content: [OB.I18N.getLabel('OBPOS_LblModalType')]
        }, {
          tag: 'div',
          id: 'paymenttype',
          attributes: {
            'class': 'span6',
            style: 'font-weight: bold;'
          }
        }]
      }, {
        tag: 'div',
        attributes: {
          'class': 'row-fluid'
        },
        content: [{
          tag: 'div',
          attributes: {
            'class': 'span6'
          },
          content: [OB.I18N.getLabel('OBPOS_LblModalAmount')]
        }, {
          tag: 'div',
          id: 'paymentamount',
          attributes: {
            'class': 'span6',
            style: 'font-weight: bold;'
          }
        }]
      }]
    }, {
      view: OKButton
    }],

    initialize: function () {
      OB.UTIL.initContentView(this);
    },

    show: function (receipt, key, name, amount) {

      this.paymenttype.text(name);
      this.paymentamount.text(OB.I18N.formatCurrency(amount));

      this.receipt = receipt;
      this.key = key;
      this.name = name;
      this.amount = amount;
    }
  });

  // register
  OB.POS.paymentProviders.push({ property: "someProperty", view: OB.COMP.MockPayment});
}());