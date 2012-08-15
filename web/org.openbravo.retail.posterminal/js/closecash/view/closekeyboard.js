/*global OB, enyo, _ */

/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

enyo.kind({
  name: 'OB.OBPOSCashUp.UI.CashUpKeyboard',
  published: {
    payments: null
  },
  kind: 'OB.UI.Keyboard',
  getPayment: function(modalpayment, receipt, key, name, provider) {
    return {
      permission: key,
      action: function(key) {
        //        var providerview = OB.POS.paymentProviders[provider];
        //        if (providerview) {
        //          modalpayment.show(receipt, key, name, providerview, OB.DEC.number(OB.I18N.parseNumber(txt)));
        //        } else {
        //        var totalCounted = 0;
        //        var counted = 0;
        //        this.options.modeldaycash.paymentmethods.each(function(payment){
        //        if(payment.get('name')===name){
        //          payment.set('counted',OB.DEC.add(0,txt));
        //          counted=OB.DEC.add(0,txt);
        //        }
        //        totalCounted= OB.DEC.add(totalCounted,payment.get('counted'));
        //        });
        //        this.options.modeldaycash.set('totalCounted',totalCounted);
        //        this.options.modeldaycash.set('totalDifference',OB.DEC.sub(totalCounted,this.options.modeldaycash.get('totalExpected')));
        //        $('button[button*="allokbutton"]').css('visibility','hidden');
        //        $('button[button="okbutton"][key*="'+key+'"]').hide();
        //        $('div[searchKey*="'+key+'"]').text(OB.I18N.formatCurrency(counted));
        //        $('div[searchKey*="'+key+'"]').show();
        //        if($('button[button="okbutton"][style!="display: none; "]').length===0){
        //          this.options.closenextbutton.$el.removeAttr('disabled');
        //        }
        //        }
      }
    };
  },

  init: function() {
    this.inherited(arguments);
    _.bind(this.getPayment, this);

    //    var i, max, payments;
    //    this.toolbar = [];
    //    this.receipt = options.modelorder;
    //    this.modalpayment = new OB.UI.ModalPayment(options).render();
    //    $('body').append(this.modalpayment.$el);
    //    payments = OB.POS.modelterminal.get('payments');
    //
    this.addToolbar({
      name: 'toolbarempty',
      buttons: []
    });




    this.showToolbar('toolbarempty');
    //    this.addToolbar(buttons);
    //    for (i = 0, max = payments.length; i < max; i++) {
    //      this.toolbar.push({definition: getPayment(this.modalpayment, this.receipt, payments[i].payment.searchKey, payments[i].payment._identifier, payments[i].payment.provider), label: payments[i].payment._identifier});
    //    }
    //    this.addToolbar('toolbarcountcash', this.toolbar);
    //    this.addToolbar([]);
    //    this.show('toolbarempty');
  },
  paymentsChanged: function() {
    var buttons = [];
    this.payments.each(function(payment) {
      buttons.push({
        command: payment.id,
        definition: this.getPayment(this.modalpayment, this.receipt, payment.searchKey, payment._identifier, payment.provider),
        label: payment.get('name')
      });
    }, this);
    this.addToolbar({
      name: 'toolbarcountcash',
      buttons: buttons
    });
  }

});