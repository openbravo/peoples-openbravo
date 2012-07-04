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
          {kind: OB.MODEL.Order},
          {kind: OB.MODEL.OrderList},
          {kind: OB.DATA.CloseCashPaymentMethod},
          {kind: OB.DATA.PaymentCloseCash},
          {kind: OB.COMP.ModalCancel},
          {kind: OB.COMP.ModalFinishClose},
          {kind: OB.COMP.ModalProcessReceipts},
          {kind: OB.DATA.Container, content: [
               {kind: OB.DATA.PaymentMethod},
               {kind: OB.DATA.CloseCashPaymentMethod},
               {kind: OB.DATA.CashCloseReport},
               {kind: OB.COMP.HWManager, attr: {'templatecashup': 'res/printcashup.xml'}}
          ]},
          {kind: B.KindJQuery('div'), attr: {'class': 'row'}, content: [
            {kind: B.KindJQuery('div'), attr: {'class': 'span12'}, content: [
//              {kind: B.KindJQuery('div'), content: [{kind: B.KindHTML('<span>&nbsp;</span>')}]} ,
//              {kind: OB.COMP.ButtonTabPendingReceipts},
//              {kind: B.KindJQuery('div'), content: [{kind: B.KindHTML('<span>&nbsp;</span>')}]},
//              {kind: B.KindJQuery('div'), content: [{kind: B.KindHTML('<span>&nbsp;</span>')}]}
            ]}
          ]},

          {kind: B.KindJQuery('div'), attr: {'class': 'row'}, content: [
            {kind: B.KindJQuery('div'), attr: {'class': 'span6'}, content: [
               {kind: OB.COMP.PendingReceipts},
               {kind: OB.COMP.CountCash},
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
          ctx.on('domready', function () {
            var orderlist = this.context.modelorderlist;
            OB.Dal.find(OB.MODEL.Order, {hasbeenpaid:'Y'}, function (fetchedOrderList) { //OB.Dal.find success
              var currentOrder = {};
              OB.UTIL.showLoading(true);
              if (fetchedOrderList && fetchedOrderList.length !== 0) {
                ctx.orderlisttoprocess = fetchedOrderList;
                OB.UTIL.showLoading(false);
                $('#modalprocessreceipts').modal('show');
              }
            }, function () { //OB.Dal.find error
              OB.UTIL.showError('Find error');
            });

            OB.Dal.find(OB.MODEL.Order, '', function (fetchedOrderList) { //OB.Dal.find success
              var currentOrder = {};
              if (fetchedOrderList && fetchedOrderList.length !== 0) {
                var transformedOrderList = [];
                // The order object is stored in the json property of the row fetched from the database
                _.each(fetchedOrderList.models, function(model) {
                  transformedOrderList.push(JSON.parse(model.get('json')));
                });
                orderlist.reset(transformedOrderList);
              }
            }, function () { //OB.Dal.find error
              OB.UTIL.showError('Find error');
            });
          }, this);
        }}
      );
    }
  });
  
  // register
  OB.POS.windows['retail.cashup'] = OB.COMP.CloseCash;
}());