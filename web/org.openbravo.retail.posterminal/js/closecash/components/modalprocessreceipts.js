/*global window, B, Backbone */

(function () {

  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};


  OB.COMP.ModalProcessReceipts = OB.COMP.Modal.extend({
    id: 'modalprocessreceipts',
    header: OB.I18N.getLabel('OBPOS_LblReceiptsToProcess'),
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
          {kind: B.KindJQuery('div'), attr: {'class': 'modal-dialog-content-text'}, content: ['There are  receipts to process. It would take a while. Do you want to continue?']},
          {kind: B.KindJQuery('div'), attr: {'class': 'modal-dialog-content-buttons-container'}, content: [
            {kind: OB.COMP.DialogOk},
            {kind: OB.COMP.DialogCancel}
          ]}
        ]}
      );
    }
  });

  // Exit
  OB.COMP.DialogOk = OB.COMP.Button.extend({
    render: function () {
      this.$el.addClass('btnlink btnlink-gray modal-dialog-content-button');
      this.$el.html(OB.I18N.getLabel('OBPOS_LblOk'));
      this.$el.attr('data-dismiss', 'modal');
      return this;
    },
    clickEvent: function (e) {
      OB.UTIL.showLoading(true);
      var orderarraytoprocess = [];
      this.options.orderlisttoprocess.each(function (order){
        orderarraytoprocess.push(JSON.parse(order.get('json')));
      });
      this.proc = new OB.DS.Process('org.openbravo.retail.posterminal.ProcessOrder');
      this.proc.exec({
        order: orderarraytoprocess
      }, function (data, message) {
      if (data && data.exception) {
        OB.UTIL.showLoading(false);
        OB.UTIL.showError(OB.I18N.getLabel('OBPOS_MsgDropDepNotSaved'));
      } else {
        OB.UTIL.showLoading(false);
        OB.UTIL.showSuccess("ALL tickets processed");
      }
     });
      return true;
    }
  });

  OB.COMP.DialogCancel = OB.COMP.Button.extend({
    render: function () {
      this.$el.addClass('btnlink btnlink-gray modal-dialog-content-button');
      this.$el.html(OB.I18N.getLabel('OBPOS_LblCancel'));
      this.$el.attr('data-dismiss', 'modal');
      return this;
    },
    clickEvent: function (e) {
      window.location=OB.POS.hrefWindow('retail.pointofsale');
    }
  });
}());