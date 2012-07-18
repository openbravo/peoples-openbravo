/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global B, $, _, Backbone */

(function () {

  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};

  OB.COMP.CloseKeyboard = OB.COMP.Keyboard.extend({
    _id: 'closekeyboard',
    initialize: function () {
      OB.COMP.Keyboard.prototype.initialize.call(this); // super.initialize();
      this.addToolbar('toolbarempty', {
          toolbarempty: [
                         {command:'---', label: {kind: B.KindHTML('<span>&nbsp;</span>')}},
                         {command:'---', label: {kind: B.KindHTML('<span>&nbsp;</span>')}},
                         {command:'---', label: {kind: B.KindHTML('<span>&nbsp;</span>')}},
                         {command:'---', label: {kind: B.KindHTML('<span>&nbsp;</span>')}},
                         {command:'---', label: {kind: B.KindHTML('<span>&nbsp;</span>')}},
                         {command:'---', label: {kind: B.KindHTML('<span>&nbsp;</span>')}}
                       ]
                     });
      this.addToolbar('toolbarcountcash', new OB.COMP.ToolbarCountCash(this.options).toolbar);
      this.show('toolbarempty');
    }
  });

}());