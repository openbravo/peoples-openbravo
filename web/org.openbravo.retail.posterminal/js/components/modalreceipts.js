/*global window, define, Backbone */

define(['builder', 'utilities', 'utilitiesui', 'i18n', 'components/commonbuttons', 'components/listreceipts'], function (B) {
   
  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};
  
  OB.COMP.ModalReceipts = OB.COMP.Modal.extend({

    id: 'modalreceipts',
    header: OB.I18N.getLabel('OBPOS_LblAssignReceipt'),
    getContentView: function () {
      return (
        {kind: OB.COMP.ListReceipts}      
      );
    },
    showEvent: function (e) {
      // custom bootstrap event, no need to prevent default
      this.options.modelorderlist.saveCurrent();
    }      
  });  
  
});  