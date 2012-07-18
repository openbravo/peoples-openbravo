/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

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