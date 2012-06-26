/*global window, B, $, Backbone */

(function () {

  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};

  OB.COMP.ButtonNew = OB.COMP.ToolbarButton.extend({
    icon: 'icon-asterisk icon-white btn-icon-left',
    label: OB.I18N.getLabel('OBPOS_LblNew'),
    clickEvent: function (e) {
      this.options.modelorderlist.addNewOrder();
    }
  });

  OB.COMP.ButtonDelete = OB.COMP.ToolbarButton.extend({
    icon: 'icon-trash icon-white btn-icon-left',
    label: OB.I18N.getLabel('OBPOS_LblDelete'),
    clickEvent: function (e) {
      if (window.confirm(OB.I18N.getLabel('OBPOS_MsgConfirmDelete'))) {
        this.options.modelorderlist.deleteCurrent();
      }
    }
  });

  OB.COMP.ButtonPrint = OB.COMP.ToolbarButton.extend({
    icon: 'icon-print icon-white btn-icon-left',
    label: OB.I18N.getLabel('OBPOS_LblPrint'),
    clickEvent: function (e) {
      var receipt = this.options.modelorder;
      receipt.calculateTaxes(function () {
        receipt.trigger('print');                        
      });      
    }
  });

  OB.COMP.MenuReturn = OB.COMP.MenuAction.extend({
    label: OB.I18N.getLabel('OBPOS_LblReturn'),
    clickEvent: function (e) {
      this.options.modelorder.setOrderTypeReturn();
    }
  });

  OB.COMP.MenuInvoice = OB.COMP.MenuAction.extend({
    label: OB.I18N.getLabel('OBPOS_LblInvoice'),
    clickEvent: function (e) {
      this.options.modelorder.setOrderInvoice();
    }
  });

}());