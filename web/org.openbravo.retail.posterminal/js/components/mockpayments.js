/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global window, Backbone */

  enyo.kind({
    name: 'OB.UI.MockPayment',
    components: [{
      components: [{
        classes: 'row-fluid',
        components: [{
          classes: 'span6',
          content: OB.I18N.getLabel('OBPOS_LblModalType')
        }, {
          name: 'paymenttype',
          classes: 'span6',
          style: 'font-weight: bold;'
        }]
      }, {
        classes: 'row-fluid',
        components: [{
          classes: 'span6',
          content: OB.I18N.getLabel('OBPOS_LblModalAmount')
        }, {
          name: 'paymentamount',
          classes: 'span6',
          style: 'font-weight: bold;'
        }]
      }]
    }, {
      kind: 'OB.UI.Button',
      classes: 'btnlink',
      style: 'float: right;',
      content: 'OK',
      tap: function() {
        this.owner.receipt.addPayment(new OB.Model.PaymentLine({
          'kind': this.owner.key,
          'name': this.owner.paymentType,
          'amount': this.owner.paymentAmount
        }));
        
        $('#modalp').modal('hide');
      }
    }],
    initComponents: function() {
      this.inherited(arguments);
      this.$.paymenttype.setContent(this.paymentType);
      this.$.paymentamount.setContent(this.paymentAmount);
    }
  });

  // register
  OB.POS.paymentProviders.push({
    property: 'testPayments',
    view: OB.UI.MockPayment
  });
