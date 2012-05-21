/*global window, define, $, Backbone */

define(['builder', 'utilities', 'i18n', 'components/commonbuttons'], function (B) {
  
  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};  
 
  OB.COMP.ButtonNew = OB.COMP.Button.extend({
    icon: 'icon-asterisk icon-white',
    label: OB.I18N.getLabel('OBPOS_LblNew'),
    clickEvent: function (e) {
      e.preventDefault();
      this.options.modelorderlist.addNewOrder();
    }       
  });  
  
  OB.COMP.ButtonDelete = OB.COMP.Button.extend({
    icon: 'icon-trash  icon-white',
    label: OB.I18N.getLabel('OBPOS_LblDelete'),
    clickEvent: function (e) {
      e.preventDefault();
      if (window.confirm(OB.I18N.getLabel('OBPOS_MsgConfirmDelete'))) {
        this.options.modelorderlist.deleteCurrent();
      }
    }       
  });
  
  OB.COMP.ButtonPrint = OB.COMP.Button.extend({
    icon: 'icon-print  icon-white',
    label: OB.I18N.getLabel('OBPOS_LblPrint'),
    clickEvent: function (e) {
      e.preventDefault();
      this.options.modelorder.trigger('print');
    }       
  }); 
  
});    