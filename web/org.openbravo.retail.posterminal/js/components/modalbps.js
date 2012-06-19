/*global window, B, Backbone */

(function () {

  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};

  OB.COMP.ModalBPs = OB.COMP.Modal.extend({

    id: 'modalcustomer',
    header: OB.I18N.getLabel('OBPOS_LblAssignCustomer'),
    getContentView: function () {
      return (
        {kind: OB.COMP.SearchBP}
      );
    }
  });

}());