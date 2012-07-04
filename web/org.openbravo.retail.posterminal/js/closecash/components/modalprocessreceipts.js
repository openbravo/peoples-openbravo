/*global window, B, Backbone */

(function () {

  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};

  OB.COMP.ModalProcessReceipts = OB.COMP.ModalAction.extend({
    id: 'modalprocessreceipts',
    header: OB.I18N.getLabel('OBPOS_LblReceiptsToProcess'),

    setBodyContent: function() {
      return(
        {kind: B.KindJQuery('div'), content: [
          'There are receipts to process. It would take a while. Do you want to continue?'
        ]}
      );
    },

    setBodyButtons: function() {
      return(
        {kind: B.KindJQuery('div'), content: [
          {kind: OB.COMP.DialogOk},
          {kind: OB.COMP.DialogCancel}
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