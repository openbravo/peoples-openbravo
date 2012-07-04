/*global window, B, $, Backbone */

(function () {

  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};

  OB.COMP.ButtonNew = OB.COMP.ToolbarButton.extend({
    icon: 'btn-icon btn-icon-new',
    //label: OB.I18N.getLabel('OBPOS_LblNew'),
    clickEvent: function (e) {
      this.options.modelorderlist.addNewOrder();
    }
  });

  OB.COMP.ButtonDelete = OB.COMP.ToolbarButton.extend({
    icon: 'btn-icon btn-icon-delete',
    //label: OB.I18N.getLabel('OBPOS_LblDelete'),
    clickEvent: function (e) {
      if (window.confirm(OB.I18N.getLabel('OBPOS_MsgConfirmDelete'))) {
        // If the model order does not have an id, it has not been
        // saved in the database yet, so there is no need to remove it
        if (this.options.modelorder.get('id')) {
          // makes sure that the current order has the id
          this.options.modelorderlist.saveCurrent();
          // removes the current order from the database
          OB.Dal.remove(this.options.modelorderlist.current, null,null);
        }
        this.options.modelorderlist.deleteCurrent();
      }
    }
  });

  OB.COMP.ButtonPrint = OB.COMP.ToolbarButton.extend({
    icon: 'btn-icon btn-icon-print',
    //label: OB.I18N.getLabel('OBPOS_LblPrint'),
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