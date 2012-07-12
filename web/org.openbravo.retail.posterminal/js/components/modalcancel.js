/*global window, B, Backbone */

(function () {

  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};

  OB.COMP.ModalCancel = OB.COMP.ModalAction.extend({
    id: 'modalCancel',
    header: OB.I18N.getLabel('OBPOS_LblCancel'),

    setBodyContent: function() {
      return(
        {kind: B.KindJQuery('div'), content: [
          OB.I18N.getLabel('OBPOS_ProcessCancelDialog')
        ]}
      );
    },

    setBodyButtons: function() {
      return(
        {kind: B.KindJQuery('div'), content: [
          {kind: OB.COMP.CancelDialogOk},
          {kind: OB.COMP.CancelDialogCancel}
        ]}
      );
    }
  });

  // Exit
  OB.COMP.CancelDialogOk = OB.COMP.Button.extend({
    className: 'btnlink btnlink-gray modal-dialog-content-button',
    render: function () {
      this.$el.html(OB.I18N.getLabel('OBPOS_LblOk'));
      return this;
    },
    clickEvent: function (e) {
      window.location=OB.POS.hrefWindow('retail.pointofsale');
    }
  });

  // Cancel
  OB.COMP.CancelDialogCancel = OB.COMP.Button.extend({
    attributes: {
      'data-dismiss': 'modal'
    },
    className: 'btnlink btnlink-gray modal-dialog-content-button',
    render: function () {
      this.$el.html(OB.I18N.getLabel('OBPOS_LblCancel'));
      return this;
    },
    clickEvent: function (e) {
    }
  });

}());