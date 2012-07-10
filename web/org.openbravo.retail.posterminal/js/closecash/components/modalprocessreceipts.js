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
            OB.I18N.getLabel('OBPOS_MsgReceiptsProcess')
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
      var orderarraytoprocess = [], me = this;
      OB.UTIL.showLoading(true);
      this.options.orderlisttoprocess.each(function (order){
        orderarraytoprocess.push(JSON.parse(order.get('json')));
      });
      this.proc = new OB.DS.Process('org.openbravo.retail.posterminal.ProcessOrder');
      this.proc.exec({
        order: orderarraytoprocess
      }, function (data, message) {
      if (data && data.exception) {
        OB.UTIL.showLoading(false);
        OB.UTIL.showError(OB.I18N.getLabel('OBPOS_MsgErrorProcessOrder'));
      } else {
        me.options.orderlisttoprocess.each(function (order){
          me.options.modelorderlist.remove(order);
          OB.Dal.remove(order, function(){
          }, function(){
          });
        });
        OB.UTIL.showLoading(false);
        OB.UTIL.showSuccess(OB.I18N.getLabel('OBPOS_MsgSuccessProcessOrder'));
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
      if (window.location.search !== OB.POS.hrefWindow('retail.pointofsale')) {
        window.location=OB.POS.hrefWindow('retail.pointofsale');
      }
    }
  });
}());