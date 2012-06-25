/*global window, B, Backbone */

(function () {

  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};


  OB.COMP.ModalFinishClose = OB.COMP.Modal.extend({
    id: 'modalFinishClose',
    header: OB.I18N.getLabel('OBPOS_LblGoodjob'),
    initialize: function () {
      OB.COMP.Modal.prototype.initialize.call(this); // super.initialize();
      var theModal = this.$el,
          theHeader = theModal.children(':first'),
          theBody = theModal.children(':nth-child(2)'),
          theHeaderText = theHeader.children(':nth-child(2)');
      theModal.addClass('modal-dialog');
      theHeader.addClass('modal-dialog-header');
      theBody.addClass('modal-dialog-body');
      theHeaderText.addClass('modal-dialog-header-text');
    },
    getContentView: function () {
      return (
        {kind: B.KindJQuery('div'), content: [
          {kind: B.KindJQuery('div'), attr: {'class': 'modal-dialog-content-text'}, content: [OB.I18N.getLabel('OBPOS_FinishCloseDialog')]},
          {kind: B.KindJQuery('div'), attr: {'class': 'modal-dialog-content-buttons-container'}, content: [
            {kind: OB.COMP.CloseDialogOk}
          ]}
        ]}
      );
    }
  });
  // Exit
  OB.COMP.CloseDialogOk = OB.COMP.Button.extend({
    render: function () {
      this.$el.addClass('btnlink btnlink-gray modal-dialog-content-button');
      this.$el.html(OB.I18N.getLabel('OBPOS_LblOk'));
      return this;
    },
    clickEvent: function (e) {
      window.location=OB.POS.hrefWindow('retail.pointofsale');
    }
  });

}());