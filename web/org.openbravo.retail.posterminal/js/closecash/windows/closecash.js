/*global B, $ */

(function () {

  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};

  OB.COMP.ButtonTest = OB.COMP.ToolbarButton.extend({
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
          {kind: OB.DATA.Container, content: [
               {kind: OB.DATA.PaymentMethod},
               {kind: OB.DATA.CloseCashPaymentMethod},
               {kind: OB.DATA.CashCloseReport}
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

        ]}
      );
    }
  });
  
  // register
  OB.POS.windows['retail.cashup'] = OB.COMP.CloseCash;
}());