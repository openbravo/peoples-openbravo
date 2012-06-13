/*global define,$,_,Backbone */

define(['builder', 'utilities', 'arithmetic', 'i18n', 'components/keyboard', 'cashmgmt/components/toolbarcashmgmt'], function (B) {

  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};

  OB.COMP.CashMgmtKeyboard = OB.COMP.Keyboard.extend({

	  initialize: function () {
		  // Toolbars at the end...
		  //    this.attr({
//      toolbarempty: [
//        {command:'---', label: {kind: B.KindHTML('<span>&nbsp;</span>')}},
//        {command:'---', label: {kind: B.KindHTML('<span>&nbsp;</span>')}},
//        {command:'---', label: {kind: B.KindHTML('<span>&nbsp;</span>')}},
//        {command:'---', label: {kind: B.KindHTML('<span>&nbsp;</span>')}},
//        {command:'---', label: {kind: B.KindHTML('<span>&nbsp;</span>')}},
//        {command:'---', label: {kind: B.KindHTML('<span>&nbsp;</span>')}}
//      ],
//       toolbarcashmgmt: [
//      {command:'line:dto', label: OB.I18N.getLabel('OBPOS_LblDepositCash')},
//      {command:'---', label: OB.I18N.getLabel('OBPOS_LblDropCash')},
//      {command:'---', label: OB.I18N.getLabel('OBPOS_LblDepositVoucher')},
//      {command:'---', label: OB.I18N.getLabel('OBPOS_LblDropVoucher')},
//      {command:'---', label: {kind: B.KindHTML('<span>&nbsp;</span>')}},
//      {command:'---', label: {kind: B.KindHTML('<span>&nbsp;</span>')}}
//    ]
//    });
    OB.COMP.Keyboard.prototype.initialize.call(this); // super.initialize();

    // Toolbars at the end...
    this.addToolbar('toolbarcashmgmt', new OB.COMP.ToolbarCashMgmt(this.options).toolbar);

      this.show('toolbarcashmgmt');
    }
  });
});