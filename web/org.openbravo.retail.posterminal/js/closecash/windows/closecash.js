/*global B, $ , _*/

(function () {

  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};

  OB.COMP.ButtonTest = OB.COMP.RegularButton.extend({
    icon: 'icon-leaf icon-white',
    label: ' **Leaf**',
    clickEvent: function (e) {
      alert('pressed');
    }
  });

  OB.COMP.CloseCash = OB.COMP.CustomView.extend({
    createView: function () {

      return (
        {kind: B.KindJQuery('section'), content: [

          {kind: OB.MODEL.DayCash},
          {kind: OB.Model.Order},
          {kind: OB.Collection.OrderList},
          {kind: OB.DATA.CloseCashPaymentMethod},
          {kind: OB.DATA.PaymentCloseCash},
          {kind: OB.COMP.ModalCancel},
          {kind: OB.COMP.ModalFinishClose},
          {kind: OB.COMP.ModalProcessReceipts},
          {kind: OB.DATA.Container, content: [
               {kind: OB.DATA.CloseCashPaymentMethod},
               {kind: OB.DATA.CashCloseReport},
               {kind: OB.COMP.HWManager, attr: {'templatecashup': 'res/printcashup.xml'}}
          ]},
          {kind: B.KindJQuery('div'), attr: {'class': 'row'}, content: [
            {kind: B.KindJQuery('div'), attr: {'class': 'span6'}, content: [
               {kind: OB.COMP.PendingReceipts},
               {kind: OB.COMP.CountCash},
               {kind: OB.COMP.CashToKeep},
               {kind: OB.COMP.PostPrintClose}
             ]},

            {kind: B.KindJQuery('div'), attr: {'class': 'span6'}, content: [
              {kind: B.KindJQuery('div'), content: [
                {kind: OB.COMP.CloseInfo }
              ]},
              {kind: OB.COMP.CloseKeyboard }
            ]}
          ]}

        ], init: function () {
          var ctx = this.context;
          OB.UTIL.showLoading(true);
          ctx.on('domready', function () {
            var orderlist = this.context.modelorderlist;
            OB.Dal.find(OB.Model.Order, {hasbeenpaid:'Y'}, function (fetchedOrderList) { //OB.Dal.find success
              var currentOrder = {};
              if (fetchedOrderList && fetchedOrderList.length !== 0) {
                ctx.orderlisttoprocess = fetchedOrderList;
                OB.UTIL.showLoading(false);
                $('#modalprocessreceipts').modal('show');
              }else{
                OB.UTIL.showLoading(false);
              }
            }, function () { //OB.Dal.find error
            });

            OB.Dal.find(OB.Model.Order,{hasbeenpaid:'N'}, function (fetchedOrderList) { //OB.Dal.find success
              var currentOrder = {};
              if (fetchedOrderList && fetchedOrderList.length !== 0) {
                ctx.closenextbutton.$el.attr('disabled','disabled');
                orderlist.reset(fetchedOrderList.models);
              }
            }, function () { //OB.Dal.find error
            });
          }, this);
        }}
      );
    }
  });

  // register
  OB.POS.windows['retail.cashup'] = OB.COMP.CloseCash;
}());