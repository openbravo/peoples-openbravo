/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global window, B, $, Backbone */

(function() {

  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};

  enyo.kind({
    name: 'OB.UI.ButtonNew',
    kind: 'OB.UI.ToolbarButton',
    icon: 'btn-icon btn-icon-new',
    tap: function() {
      this.options.modelorderlist.addNewOrder();
    }
  });

  //  OB.COMP.ButtonNew = OB.COMP.ToolbarButton.extend({
  //    icon: 'btn-icon btn-icon-new',
  //    //label: OB.I18N.getLabel('OBPOS_LblNew'),
  //    clickEvent: function(e) {
  //      this.options.modelorderlist.addNewOrder();
  //    }
  //  });
  enyo.kind({
    name: 'OB.UI.ButtonDelete',
    kind: 'OB.UI.ToolbarButton',
    icon: 'btn-icon btn-icon-delete',
    tap: function() {
      $('#modalDeleteReceipt').modal('show');
    }
  });

  //  OB.COMP.ButtonDelete = OB.COMP.ToolbarButton.extend({
  //    icon: 'btn-icon btn-icon-delete',
  //    //label: OB.I18N.getLabel('OBPOS_LblDelete'),
  //    clickEvent: function (e) {
  //      $('#modalDeleteReceipt').modal('show');
  //    }
  //  });
  enyo.kind({
    name: 'OB.UI.ButtonPrint',
    kind: 'OB.UI.ToolbarButton',
    icon: 'btn-icon btn-icon-print',
    tap: function() {
      var receipt = this.options.modelorder;
      receipt.calculateTaxes(function() {
        receipt.trigger('print');
      });
    }
  });

  //  OB.COMP.ButtonPrint = OB.COMP.ToolbarButton.extend({
  //    icon: 'btn-icon btn-icon-print',
  //    //label: OB.I18N.getLabel('OBPOS_LblPrint'),
  //    clickEvent: function(e) {
  //      var receipt = this.options.modelorder;
  //      receipt.calculateTaxes(function() {
  //        receipt.trigger('print');
  //      });
  //    }
  //  });
  enyo.kind({
    name: 'OB.UI.MenuReturn',
    kind: 'OB.UI.MenuAction',
    label: OB.I18N.getLabel('OBPOS_LblReturn'),
    tap: function () {
    	//TODO: do this...
    }
  });

  OB.COMP.MenuReturn = OB.COMP.MenuAction.extend({
    label: OB.I18N.getLabel('OBPOS_LblReturn'),
    clickEvent: function(e) {
      this.options.modelorder.setOrderTypeReturn();
    }
  });

  
  enyo.kind({
	    name: 'OB.UI.MenuInvoice',
	    kind: 'OB.UI.MenuAction',
	    label: OB.I18N.getLabel('OBPOS_LblInvoice'),
	    tap: function () {
	    	//TODO: do this...
	    }
	  });
  
  OB.COMP.MenuInvoice = OB.COMP.MenuAction.extend({
    label: OB.I18N.getLabel('OBPOS_LblInvoice'),
    clickEvent: function(e) {
      this.options.modelorder.setOrderInvoice();
    }
  });
}());