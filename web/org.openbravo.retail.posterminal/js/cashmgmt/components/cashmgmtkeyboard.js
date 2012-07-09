/*global B,$,_,Backbone */

(function () {

  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};

  OB.COMP.CashMgmtKeyboard = OB.COMP.Keyboard.extend({
  _id: 'cashmgmtkeyboard',
  initialize: function () {
      OB.COMP.Keyboard.prototype.initialize.call(this); // super.initialize();
      this.addToolbar('toolbarcashmgmt', new OB.UI.ToolbarCashMgmt(this.options).toolbar);
    }
  });
}());