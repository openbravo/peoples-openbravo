/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global enyo */

enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.Payment',
  published: {
    receipt: null
  },
  components: [{
    style: 'background-color: #363636; color: white; height: 200px; margin: 5px; padding: 5px',
    components: [{
      classes: 'row-fluid',
      components: [{
        classes: 'span12'
      }]
    }, {
      classes: 'row-fluid',
      components: [{
        classes: 'span9',
        components: [{
          style: 'padding: 10px 0px 0px 10px;',
          components: [{
            tag: 'span',
            name: 'totalpending',
            style: 'font-size: 24px; font-weight: bold;'
          }, {
            tag: 'span',
            name: 'totalpendinglbl',
            content: OB.I18N.getLabel('OBPOS_PaymentsRemaining')
          }, {
            tag: 'span',
            name: 'change',
            style: 'font-size: 24px; font-weight: bold;'
          }, {
            tag: 'span',
            name: 'changelbl',
            content: OB.I18N.getLabel('OBPOS_PaymentsChange')
          }, {
            tag: 'span',
            name: 'overpayment',
            style: 'font-size: 24px; font-weight: bold;'
          }, {
            tag: 'span',
            name: 'overpaymentlbl',
            content: OB.I18N.getLabel('OBPOS_PaymentsOverpayment')
          }, {
            tag: 'span',
            name: 'exactlbl',
            content: OB.I18N.getLabel('OBPOS_PaymentsExact')
          }]
        }, {
          style: 'overflow:auto; width: 100%;',
          components: [{
            style: 'padding: 5px',
            components: [{
              style: 'margin: 2px 0px 0px 0px; border-bottom: 1px solid #cccccc;'
            }, {
              kind: 'OB.UI.Table',
              name: 'payments',
              renderEmpty: enyo.kind({
                style: 'height: 36px'
              }),
              renderLine: 'OB.OBPOSPointOfSale.UI.RenderPaymentLine'
            }]
          }]
        }]
      }, {
        classes: 'span3',
        components: [{
          style: 'float: right;',
          name: 'doneaction',
          components: [{
            kind: 'OB.OBPOSPointOfSale.UI.DoneButton'
          }]
        }, {
          style: 'float: right;',
          name: 'exactaction',
          components: [{
            kind: 'OB.OBPOSPointOfSale.UI.ExactButton'
          }]
        }]
      }]

    }]
  }],

  receiptChanged: function () {
    this.$.payments.setCollection(this.receipt.get('payments'));

    this.receipt.on('change:payment change:change calculategross', function () {
      this.updatePending();
    }, this);
    this.updatePending();
  },


  updatePending: function () {
    var paymentstatus = this.receipt.getPaymentStatus();
    if (paymentstatus.change) {
      this.$.change.setContent(paymentstatus.change);
      this.$.change.show();
      this.$.changelbl.show();
    } else {
      this.$.change.hide();
      this.$.changelbl.hide();
    }
    if (paymentstatus.overpayment) {
      this.$.overpayment.setContent(paymentstatus.overpayment);
      this.$.overpayment.show();
      this.$.overpaymentlbl.show();
    } else {
      this.$.overpayment.hide();
      this.$.overpaymentlbl.hide();
    }
    if (paymentstatus.done) {
      this.$.totalpending.hide();
      this.$.totalpendinglbl.hide();
      this.$.doneaction.show();
    } else {
      this.$.totalpending.setContent(paymentstatus.pending);
      this.$.totalpending.show();
      this.$.totalpendinglbl.show();
      this.$.doneaction.hide();
      if (this.$.doneButton.drawerpreference) {
        this.$.doneButton.setContent(OB.I18N.getLabel('OBPOS_LblOpen'));
        this.$.doneButton.drawerOpened = false;
      }
    }

    if (paymentstatus.done || this.receipt.getGross() === 0) {
      this.$.exactaction.hide();
    } else {
      this.$.exactaction.show();
    }

    if (paymentstatus.done && !paymentstatus.change && !paymentstatus.overpayment) {
      this.$.exactlbl.show();
    } else {
      this.$.exactlbl.hide();
    }
  }
});

enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.DoneButton',
  kind: 'OB.UI.RegularButton',
  content: OB.I18N.getLabel('OBPOS_LblDone'),
  drawerOpened: true,
  init: function () {
    this.drawerpreference = OB.POS.modelterminal.get('terminal').drawerpreference;
    if (this.drawerpreference) {
      this.drawerOpened = false;
      this.setContent(OB.I18N.getLabel('OBPOS_LblOpen'));
    } else {
      this.drawerOpened = true;
      this.setContent(OB.I18N.getLabel('OBPOS_LblDone'));
    }
  },
  tap: function () {
    if (this.drawerpreference) {
      if (this.drawerOpened) {
        this.owner.receipt.trigger('paymentDone');
        this.drawerOpened = false;
        this.setContent(OB.I18N.getLabel('OBPOS_LblOpen'));
      } else {
        this.owner.receipt.trigger('openDrawer');
        this.drawerOpened = true;
        this.setContent(OB.I18N.getLabel('OBPOS_LblDone'));
      }
    } else {
      this.owner.receipt.trigger('paymentDone');
      this.owner.receipt.trigger('openDrawer');
    }
  }
});

enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.ExactButton',
  events: {
    onExactPayment: ''
  },
  kind: 'OB.UI.RegularButton',
  classes: 'btn-icon-small btn-icon-check btnlink-green',
  style: 'width: 69px',
  tap: function () {
    this.doExactPayment();
  }
});

enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.RenderPaymentLine',
  classes: 'btnselect',
  components: [{
    style: 'color:white;',
    components: [{
      name: 'name',
      style: 'float: left; width: 15%; padding: 5px 0px 0px 0px;'
    }, {
      name: 'info',
      style: 'float: left; width: 20%; padding: 5px 0px 0px 0px;'
    }, {
      name: 'foreignAmount',
      style: 'float: left; width: 25%; padding: 5px 0px 0px 0px; text-align: right;'
    }, {
      name: 'amount',
      style: 'float: left; width: 25%; padding: 5px 0px 0px 0px; text-align: right;'
    }, {
      style: 'float: left; width: 15%; text-align: right;',
      components: [{
        kind: 'OB.OBPOSPointOfSale.UI.RemovePayment'
      }]
    }, {
      style: 'clear: both;'
    }]
  }],
  initComponents: function () {
    this.inherited(arguments);
    this.$.name.setContent(OB.POS.modelterminal.getPaymentName(this.model.get('kind')));
    this.$.amount.setContent(this.model.printAmount());
    if (this.model.get('rate') && this.model.get('rate') !== '1') {
      this.$.foreignAmount.setContent(this.model.printForeignAmount());
    } else {
      this.$.foreignAmount.setContent('');
    }
    if (this.model.get('paymentData')) {
      this.$.info.setContent(this.model.get('paymentData').Name);
    } else {
      this.$.info.setContent('');
    }
  }
});

enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.RemovePayment',
  events: {
    onRemovePayment: ''
  },
  kind: 'OB.UI.SmallButton',
  classes: 'btnlink-darkgray btnlink-payment-clear btn-icon-small btn-icon-clearPayment',
  tap: function () {
    this.doRemovePayment({
      payment: this.owner.model
    });
  }
});