/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global B, window, define, $, Backbone */

(function () {

  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};

  OB.COMP.Done = OB.COMP.RegularButton.extend({
    _id: 'donebutton',
    label: OB.I18N.getLabel('OBPOS_LblDone'),
    attributes: {'style': 'min-width: 115px;'},
    render: function () {
      OB.COMP.RegularButton.prototype.render.call(this); // super.initialize();
      this.$el.addClass('btnlink-white').addClass('btnlink-fontgray');
      this.$el.addClass('hidden');
      return this;
    },
    clickEvent: function (e) {
        this.destinations.trigger('depositdrop');
    }
  });

  OB.COMP.ButtonNextCashMgmt = OB.COMP.RegularButton.extend({
    _id: 'cashmgmtnextbutton',
    label: OB.I18N.getLabel('OBPOS_LblDone'),
    attributes: {'style': 'min-width: 115px;'},
    render: function () {
      OB.COMP.RegularButton.prototype.render.call(this); // super.initialize();
      this.$el.addClass('btnlink-white').addClass('btnlink-fontgray');
      return this;
    },
    clickEvent: function (e) {
       this.options.ListDepositsDrops.listdepositsdrops.trigger('depositdrop');
   }
  });
}());