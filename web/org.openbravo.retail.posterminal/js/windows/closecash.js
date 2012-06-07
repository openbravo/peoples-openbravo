/*global define, $ */

define(['builder', 'i18n', 'data/datamaster',
        'components/commonbuttons', 'components/hwmanager',
        'model/daycash','model/terminal', 'model/order',
        'windows/closebuttons',
        'windows/closeinfo',
        'windows/closekeyboard',
        'windows/tabpendingreceipts', 'windows/tabcountcash', 'windows/tabpostprintclose', 'windows/closekeyboard', 'components/listpaymentmethod'
        ], function (B) {

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

          {kind: OB.DATA.PaymentMethod},

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

  return OB.COMP.CloseCash;
});