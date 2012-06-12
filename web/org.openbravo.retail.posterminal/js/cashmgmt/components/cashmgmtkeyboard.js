/*global define,$,_,Backbone */

define(['builder', 'utilities', 'arithmetic', 'i18n', 'components/keyboard'], function (B) {

  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};

  OB.COMP.CashMgmtKeyboard = OB.COMP.Keyboard.extend({


  	  initialize: function () {
      OB.COMP.Keyboard.prototype.initialize.call(this); // super.initialize();

      // Toolbars at the end...
//      {command:'payment.cash', definition: OB.COMP.KeyboardOrder.getPayment('payment.cash'), label: OB.I18N.getLabel('OBPOS_KbCash')},
//      {command:'payment.card', definition: OB.COMP.KeyboardOrder.getPayment('payment.card'), label: OB.I18N.getLabel('OBPOS_KbCard')},
//      {command:'payment.voucher', definition: OB.COMP.KeyboardOrder.getPayment('payment.voucher'), label: OB.I18N.getLabel('OBPOS_KbVoucher')},

      this.addCommand('line:dto', {
          'permission': 'order.discount',
          'action': function (txt) {
            if (this.line) {
               this.receipt.trigger('discount', this.line, parseNumber(txt));
            }
          }
        });

      this.attr({
        toolbarempty: [
          {command:'---', label: {kind: B.KindHTML('<span>&nbsp;</span>')}},
          {command:'---', label: {kind: B.KindHTML('<span>&nbsp;</span>')}},
          {command:'---', label: {kind: B.KindHTML('<span>&nbsp;</span>')}},
          {command:'---', label: {kind: B.KindHTML('<span>&nbsp;</span>')}},
          {command:'---', label: {kind: B.KindHTML('<span>&nbsp;</span>')}},
          {command:'---', label: {kind: B.KindHTML('<span>&nbsp;</span>')}}
        ],
        toolbarcashmgmt: [
	       {command:'line:dto', label: OB.I18N.getLabel('OBPOS_LblDepositCash')},
	       {command:'---', label: OB.I18N.getLabel('OBPOS_LblDropCash')},
	       {command:'---', label: OB.I18N.getLabel('OBPOS_LblDepositVoucher')},
	       {command:'---', label: OB.I18N.getLabel('OBPOS_LblDropVoucher')},
	       {command:'---', label: {kind: B.KindHTML('<span>&nbsp;</span>')}},
	       {command:'---', label: {kind: B.KindHTML('<span>&nbsp;</span>')}}
	     ]
      });

      this.show('toolbarcashmgmt');
    }
  });
});