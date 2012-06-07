/*global window, define, $, Backbone */

define(['builder', 'utilities', 'i18n', 'components/commonbuttons'], function (B) {

  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};

  OB.COMP.ButtonNew = OB.COMP.ToolbarButton.extend({
    icon: 'icon-asterisk icon-white',
    label: OB.I18N.getLabel('OBPOS_LblNew'),
    clickEvent: function (e) {
      this.options.modelorderlist.addNewOrder();
    }
  });

  OB.COMP.ButtonDelete = OB.COMP.ToolbarButton.extend({
    icon: 'icon-trash  icon-white',
    label: OB.I18N.getLabel('OBPOS_LblDelete'),
    clickEvent: function (e) {
      if (window.confirm(OB.I18N.getLabel('OBPOS_MsgConfirmDelete'))) {
        this.options.modelorderlist.deleteCurrent();
      }
    }
  });

  OB.COMP.ButtonPrint = OB.COMP.ToolbarButton.extend({
    icon: 'icon-print  icon-white',
    label: OB.I18N.getLabel('OBPOS_LblPrint'),
    clickEvent: function (e) {
      this.options.modelorder.trigger('print');
    }
  });

  OB.COMP.MenuReturn = OB.COMP.MenuAction.extend({
    label: OB.I18N.getLabel('OBPOS_LblReturn'),
    clickEvent: function (e) {
      this.options.modelorder.setOrderTypeReturn();
    }
  });

});