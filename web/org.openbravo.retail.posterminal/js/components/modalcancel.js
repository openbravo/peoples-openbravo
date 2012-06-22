/*global window, B, Backbone */

(function () {

  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};


  OB.COMP.ModalCancel = OB.COMP.Modal.extend({
    id: 'modalCancel',
    header: OB.I18N.getLabel('OBPOS_LblCancel'),
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
          {kind: B.KindJQuery('div'), attr: {'class': 'modal-dialog-content-text'}, content: [OB.I18N.getLabel('OBPOS_CancelDialog')]},
          {kind: B.KindJQuery('div'), attr: {'class': 'modal-dialog-content-buttons-container'}, content: [
            {kind: OB.COMP.CancelDialogOk},
            {kind: OB.COMP.CancelDialogCancel}
          ]}
        ]}
      );
    }
  });


  // Exit
  OB.COMP.CancelDialogOk = OB.COMP.Button.extend({
    render: function () {
      this.$el.addClass('btnlink btnlink-gray modal-dialog-content-button');
      this.$el.html(OB.I18N.getLabel('OBPOS_LblOk'));
      return this;
    },
    clickEvent: function (e) {
      window.location=OB.POS.hrefWindow('retail.pointofsale');
    }
  });

  // Cancel
  OB.COMP.CancelDialogCancel = OB.COMP.Button.extend({
    render: function () {
      this.$el.addClass('btnlink btnlink-gray modal-dialog-content-button');
      this.$el.html(OB.I18N.getLabel('OBPOS_LblCancel'));
      this.$el.attr('data-dismiss', 'modal');
      return this;
    },
    clickEvent: function (e) {
      return true;
    }
  });

}());