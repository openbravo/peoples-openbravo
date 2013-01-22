/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global enyo,_ */

enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.Payment',
  published: {
    receipt: null
  },
  handlers: {
    onButtonStatusChanged: 'buttonStatusChanged'
  },
  buttonStatusChanged: function (inSender, inEvent) {
    var payment, paymentstatus = this.receipt.getPaymentStatus();
    if (!_.isUndefined(inEvent.value.payment)) {
      payment = inEvent.value.payment;
      this.receipt.selectedPayment = payment.payment.searchKey;
      if (!_.isNull(paymentstatus.change)) {
        this.$.change.setContent(OB.DEC.mul(paymentstatus.change, payment.mulrate) + payment.symbol);
      } else if (paymentstatus.pending) {
        this.$.totalpending.setContent(OB.DEC.mul(paymentstatus.pending, payment.mulrate) + payment.symbol);
      }
    }
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
          }, {
            tag: 'span',
            name: 'donezerolbl',
            content: OB.I18N.getLabel('OBPOS_MsgPaymentAmountZero')
          }, {
            name: 'creditsalesaction',
            kind: 'OB.OBPOSPointOfSale.UI.CreditButton'
          }]
        }, {
          style: 'overflow:auto; width: 100%;',
          components: [{
            style: 'padding: 5px',
            components: [{
              style: 'margin: 2px 0px 0px 0px; border-bottom: 1px solid #cccccc;'
            }, {
              kind: 'OB.UI.ScrollableTable',
              scrollAreaMaxHeight: '160px',
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

    this.receipt.on('change:payment change:change calculategross change:bp', function () {
      this.updatePending();
    }, this);
    this.updatePending();
  },


  updatePending: function () {
    var paymentstatus = this.receipt.getPaymentStatus();
    var symbol = '',
        rate = OB.DEC.One;
    if (!_.isUndefined(this.receipt) && !_.isUndefined(OB.POS.terminal.terminal.paymentnames[this.receipt.selectedPayment])) {
      symbol = OB.POS.terminal.terminal.paymentnames[this.receipt.selectedPayment].symbol;
      rate = OB.POS.terminal.terminal.paymentnames[this.receipt.selectedPayment].mulrate;
    }
    if (paymentstatus.change) {
      this.$.change.setContent(OB.DEC.mul(paymentstatus.change, rate) + symbol);
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
      this.$.creditsalesaction.hide();
    } else {
      this.$.totalpending.setContent(OB.DEC.mul(paymentstatus.pending, rate) + symbol);
      this.$.totalpending.show();
      this.$.totalpendinglbl.show();
      this.$.doneaction.hide();
      if (this.$.doneButton.drawerpreference) {
        this.$.doneButton.setContent(OB.I18N.getLabel('OBPOS_LblOpen'));
        this.$.doneButton.drawerOpened = false;
      }
      if (OB.POS.modelterminal.get('terminal').allowpayoncredit && this.receipt.get('bp')) {
        if (this.receipt.get('bp').get('creditLimit') > 0) {
          this.$.creditsalesaction.show();
        } else {
          this.$.creditsalesaction.hide();
        }
      }
    }

    if (paymentstatus.done || this.receipt.getGross() === 0) {
      this.$.exactaction.hide();
      this.$.creditsalesaction.hide();
    } else {
      this.$.exactaction.show();
      if (OB.POS.modelterminal.get('terminal').allowpayoncredit && this.receipt.get('bp')) {
        if (this.receipt.get('bp').get('creditLimit') > 0) {
          this.$.creditsalesaction.show();
        } else {
          this.$.creditsalesaction.hide();
        }
      }
    }

    if (paymentstatus.done && !paymentstatus.change && !paymentstatus.overpayment) {
      if (this.receipt.getGross() === 0) {
        this.$.exactlbl.hide();
        this.$.donezerolbl.show();
      } else {
        this.$.donezerolbl.hide();
        this.$.exactlbl.show();
      }
    } else {
      this.$.exactlbl.hide();
      this.$.donezerolbl.hide();
    }
  }
});

enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.DoneButton',
  kind: 'OB.UI.RegularButton',
  content: OB.I18N.getLabel('OBPOS_LblDone'),
  drawerOpened: true,
  init: function (model) {
    this.model = model;
    this.model.get('order').on('change:openDrawer', function () {
      this.drawerpreference = this.model.get('order').get('openDrawer');
      if (this.drawerpreference) {
        this.drawerOpened = false;
        this.setContent(OB.I18N.getLabel('OBPOS_LblOpen'));
      } else {
        this.drawerOpened = true;
        this.setContent(OB.I18N.getLabel('OBPOS_LblDone'));
      }
    }, this);
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
  classes: 'btn-icon-adaptative btn-icon-check btnlink-green',
  style: 'width: 73px',
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
      style: 'float: left; width: 20%; padding: 5px 0px 0px 0px;'
    }, {
      name: 'info',
      style: 'float: left; width: 15%; padding: 5px 0px 0px 0px;'
    }, {
      name: 'foreignAmount',
      style: 'float: left; width: 20%; padding: 5px 0px 0px 0px; text-align: right;'
    }, {
      name: 'amount',
      style: 'float: left; width: 25%; padding: 5px 0px 0px 0px; text-align: right;'
    }, {
      style: 'float: left; width: 20%; text-align: right;',
      components: [{
        kind: 'OB.OBPOSPointOfSale.UI.RemovePayment'
      }]
    }, {
      style: 'clear: both;'
    }]
  }],
  initComponents: function () {
    this.inherited(arguments);
    this.$.name.setContent(OB.POS.modelterminal.getPaymentName(this.model.get('kind')) || this.model.get('name'));
    this.$.amount.setContent(this.model.printAmount());
    if (this.model.get('rate') && this.model.get('rate') !== '1') {
      this.$.foreignAmount.setContent(this.model.printForeignAmount());
    } else {
      this.$.foreignAmount.setContent('');
    }
    if (this.model.get('description')) {
      this.$.info.setContent(this.model.get('description'));
    } else {
      if (this.model.get('paymentData')) {
        this.$.info.setContent(this.model.get('paymentData').Name);
      } else {
        this.$.info.setContent('');
      }
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
    var me = this;
    if (_.isUndefined(this.deleting) || this.deleting === false) {
      this.deleting = true;
      this.removeClass('btn-icon-clearPayment');
      this.addClass('btn-icon-loading');

      this.doRemovePayment({
        payment: this.owner.model,
        removeCallback: function (hasError, error) {
          me.deleting = false;
          if (hasError) {
            OB.UTIL.showError(error);
          }
          me.removeClass('btn-icon-loading');
          me.addClass('btn-icon-clearPayment');
        }
      });
    }
  }
});

enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.CreditButton',
  kind: 'OB.UI.SmallButton',
  content: OB.I18N.getLabel('OBPOS_LblCreditSales'),
  classes: 'btn-icon-small btnlink-green',
  style: 'width: 120px; float: right; margin: 0px',
  permission: 'OBPOS_receipt.creditsales',
  events: {
    onShowPopup: ''
  },
  init: function (model) {
    this.model = model;
  },
  tap: function () {
    var process = new OB.DS.Process('org.openbravo.retail.posterminal.CheckBusinessPartnerCredit');
    var me = this;
    if (OB.POS.modelterminal.get('connectedToERP')) {
      //this.setContent(OB.I18N.getLabel('OBPOS_LblLoading'));
      process.exec({
        businessPartnerId: this.model.get('order').get('bp').get('id'),
        totalPending: this.model.get('order').getPending()
      }, function (data) {
        if (data) {
          if (data.enoughCredit) {
            me.doShowPopup({
              popup: 'modalEnoughCredit',
              args: {
                order: me.model.get('order')
              }
            });
            //this.setContent(OB.I18N.getLabel('OBPOS_LblCreditSales'));
          } else {
            var bpName = data.bpName;
            var actualCredit = data.actualCredit;
            me.doShowPopup({
              popup: 'modalNotEnoughCredit',
              args: {
                bpName: bpName,
                actualCredit: actualCredit
              }
            });
            //this.setContent(OB.I18N.getLabel('OBPOS_LblCreditSales'));
            //OB.UI.UTILS.domIdEnyoReference['modalNotEnoughCredit'].$.bodyContent.children[0].setContent();
          }
        } else {
          OB.UTIL.showError(OB.I18N.getLabel('OBPOS_MsgErrorCreditSales'));
        }
      });
    } else {
      var actualCredit;
      var creditLimit = this.model.get('order').get('bp').get('creditLimit');
      var creditUsed = this.model.get('order').get('bp').get('creditUsed');
      var totalPending = this.model.get('order').getPending();
      if ((creditLimit - creditUsed) >= totalPending) {
        this.doShowPopup({
          popup: 'modalEnoughCredit',
          args: {
            order: this.model.get('order')
          }
        });
      } else {
        actualCredit = creditLimit - creditUsed;
        this.doShowPopup({
          popup: 'modalNotEnoughCredit',
          args: {
            bpName: this.model.get('order').get('bp'),
            actualCredit: actualCredit
          }
        });
      }
    }

    //		if(this.drawerpreference){
    //		  if(this.drawerOpened){
    //		  this.owner.receipt.trigger('paymentDone');
    //	      this.drawerOpened= false;
    //	      this.setContent(OB.I18N.getLabel('OBPOS_LblOpen'));
    //	    }else{
    //	      this.owner.receipt.trigger('openDrawer');
    //	      this.drawerOpened= true;
    //	      this.setContent(OB.I18N.getLabel('OBPOS_LblDone'));
    //	      }
    //	   }else{
    //	     this.owner.receipt.trigger('paymentDone');
    //	     this.owner.receipt.trigger('openDrawer');
    //	   }
  }
});