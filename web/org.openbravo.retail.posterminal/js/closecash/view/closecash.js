/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global OB, enyo*/

enyo.kind({
  name: 'OB.OBPOSCashUp.UI.CashUp',
  kind: 'OB.UI.WindowView',
  windowmodel: OB.OBPOSCashUp.Model.CashUp,
  handlers: {
    onNext: 'nextStep',
    onPrev: 'prevStep',
    onButtonOk: 'buttonOk'
  },
  buttonOk: function(inSender, inEvent) {
    debugger;
//    $('button[button*="allokbutton"]').css('visibility','hidden');
//    var elem = this.me.options.modeldaycash.paymentmethods.get(this.options[this._id].rowid);
//    this.options['counted_'+this.options[this._id].rowid].$el.text(OB.I18N.formatCurrency(elem.get('expected')));
//    elem.set('counted',OB.DEC.add(0,elem.get('expected')));
//    this.me.options.modeldaycash.set('totalCounted',OB.DEC.add(this.me.options.modeldaycash.get('totalCounted'),elem.get('counted')));
//    this.options['counted_'+this.rowid].$el.show();
//    if($('button[button="okbutton"][style!="display: none; "]').length===0){
//      this.me.options.closenextbutton.$el.removeAttr('disabled');
//    }
  },
  prevStep: function(inSender, inEvent) {
    switch (this.model.get('step')) {
    case 0:
      break;
    case 1:
      this.$.listPendingReceipts.show();
      this.$.listPaymentMethods.hide();
      this.$.cashUpInfo.$.buttonPrev.setDisabled(true);
      this.$.cashUpKeyboard.showToolbar('toolbarempty');
      //show toolbarempty
      this.model.set('step', this.model.get('step') - 1);
      //    if($('button[button="okbutton"][style!="display: none; "]').length!==0){
      //      this.$el.attr('disabled','disabled');
      //    }
      break;
    case 2:
      this.$.cashToKeep.hide();
      this.$.listPaymentMethods.show();
      this.$.cashUpKeyboard.showToolbar('toolbarcountcash');
      this.model.set('step', this.model.get('step') - 1);
      break;
    case 3:
      this.$.postPrintClose.hide();
      this.$.cashToKeep.show();
      this.model.set('step', this.model.get('step') - 1);
      break;
    default:
    }
  },
  nextStep: function(inSender, inEvent) {
    switch (this.model.get('step')) {
    case 0:
      this.$.listPendingReceipts.hide();
      this.$.listPaymentMethods.show();
      this.$.cashUpInfo.$.buttonPrev.setDisabled(false);
      this.$.cashUpKeyboard.showToolbar('toolbarcountcash');
      this.model.set('step', this.model.get('step') + 1);
      //show toolbarcountcash
      //      if($('button[button="okbutton"][style!="display: none; "]').length!==0){
      //        this.$el.attr('disabled','disabled');
      //      }
      break;
    case 1:
      this.$.cashToKeep.show();
      this.$.listPaymentMethods.hide();
      this.$.cashUpKeyboard.showToolbar('toolbarempty');
      this.model.set('step', this.model.get('step') + 1);
      break;
    case 2:
      this.$.postPrintClose.show();
      this.$.cashToKeep.hide();
      this.model.set('step', this.model.get('step') + 1);
      break;
    default:
    }
  },
  components: [{
    classes: 'row',
    components: [
    // 1st column: list of pending receipts
    {
      classes: 'span6',
      components: [{
        kind: 'OB.OBPOSCashUp.UI.ListPendingReceipts'
      }]
    },
    // 1st column: list of count cash per payment method
    {
      classes: 'span6',
      components: [{
        kind: 'OB.OBPOSCashUp.UI.ListPaymentMethods',
        showing: false
      }]
    },
    // 1st column: Radio buttons to choose how much to keep in cash
    {
      classes: 'span6',
      components: [{
        kind: 'OB.OBPOSCashUp.UI.CashToKeep',
        showing: false
      }]
    },
    // 1st column: Cash up Report previous to finish the proccess
    {
      classes: 'span6',
      components: [{
        kind: 'OB.OBPOSCashUp.UI.PostPrintClose',
        showing: false
      }]
    },
    //2nd column
    {
      classes: 'span6',
      components: [{
        classes: 'span6',
        components: [{
          kind: 'OB.OBPOSCashUp.UI.CashUpInfo'
        }, {
          kind: 'OB.OBPOSCashUp.UI.CashUpKeyboard'
        }]
      }]
    }, {
      kind: 'OB.UI.ModalCancel'
    }, {
      //  kind: OB.UI.ModalFinishClose
    }]
  }],
  init: function() {
    this.inherited(arguments);
    var me = this;
    OB.Dal.find(OB.Model.Order,{hasbeenpaid:'N'}, function (fetchedOrderList) { //OB.Dal.find success
      if (fetchedOrderList && fetchedOrderList.length !== 0) {
        me.model.orderlist.reset(fetchedOrderList.models);
        me.$.listPendingReceipts.$.pendingReceiptList.setCollection(fetchedOrderList);
//        ctx.closenextbutton.$el.attr('disabled','disabled');
//        orderlist.reset(fetchedOrderList.models);
      }
    }, function () { }); //OB.Dal.find error
    
    this.model.payList.each(function(payList) {
      this.model.set('totalExpected', OB.DEC.add(this.model.get('totalExpected'),payList.get('expected')));
    }, this);
    this.$.listPaymentMethods.$.total.setContent(this.model.get('totalExpected'));
  }
});

OB.POS.registerWindow({
  windowClass: OB.OBPOSCashUp.UI.CashUp,
  route: 'retail.cashup',
  menuPosition: 20,
  menuLabel: OB.I18N.getLabel('OBPOS_LblCloseCash')
});

//  return (
//      {kind: B.KindJQuery('section'), content: [
//
//        {kind: OB.MODEL.DayCash},
//        {kind: OB.Model.Order},
//        {kind: OB.Collection.OrderList},
//        {kind: OB.DATA.CloseCashPaymentMethod},
//        {kind: OB.DATA.PaymentCloseCash},
//        {kind: OB.COMP.ModalCancel},
//        {kind: OB.COMP.ModalFinishClose},
//        {kind: OB.COMP.ModalProcessReceipts},
//        {kind: OB.DATA.Container, content: [
//             {kind: OB.DATA.CloseCashPaymentMethod},
//             {kind: OB.DATA.CashCloseReport},
//             {kind: OB.COMP.HWManager, attr: {'templatecashup': 'res/printcashup.xml'}}
//        ]},
//        {kind: B.KindJQuery('div'), attr: {'class': 'row'}, content: [
//          {kind: B.KindJQuery('div'), attr: {'class': 'span6'}, content: [
//             {kind: OB.COMP.PendingReceipts},
//             {kind: OB.COMP.CountCash},
//             {kind: OB.COMP.CashToKeep},
//             {kind: OB.COMP.PostPrintClose}
//           ]},
//
//          {kind: B.KindJQuery('div'), attr: {'class': 'span6'}, content: [
//            {kind: B.KindJQuery('div'), content: [
//              {kind: OB.COMP.CloseInfo }
//            ]},
//            {kind: OB.COMP.CloseKeyboard }
//          ]}
//        ]}
//
//      ], init: function () {
//        var ctx = this.context;
//        OB.UTIL.showLoading(true);
//        ctx.on('domready', function () {
//          var orderlist = this.context.modelorderlist;
//          OB.Dal.find(OB.Model.Order, {hasbeenpaid:'Y'}, function (fetchedOrderList) { //OB.Dal.find success
//            var currentOrder = {};
//            if (fetchedOrderList && fetchedOrderList.length !== 0) {
//              ctx.orderlisttoprocess = fetchedOrderList;
//              OB.UTIL.showLoading(false);
//              $('#modalprocessreceipts').modal('show');
//            }else{
//              OB.UTIL.showLoading(false);
//            }
//          }, function () { //OB.Dal.find error
//          });
//
//          OB.Dal.find(OB.Model.Order,{hasbeenpaid:'N'}, function (fetchedOrderList) { //OB.Dal.find success
//            var currentOrder = {};
//            if (fetchedOrderList && fetchedOrderList.length !== 0) {
//              ctx.closenextbutton.$el.attr('disabled','disabled');
//              orderlist.reset(fetchedOrderList.models);
//            }
//          }, function () { //OB.Dal.find error
//          });
//        }, this);
//      }}
//    );
//  }
//});