/*global define, $ */

define(['builder', 'i18n', 'data/datamaster',
        'components/commonbuttons', 'components/hwmanager','model/terminal', 'model/order',
        'cashmgmt/components/cashmgmtbuttons',
        'cashmgmt/windows/cashmgmtinfo',
        'cashmgmt/components/cashmgmtkeyboard',
        'cashmgmt/windows/depositsdrops', 'cashmgmt/components/modaldropdestinations'
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


  OB.COMP.CashManagement = OB.COMP.CustomView.extend({
    createView: function () {

      return (
        {kind: B.KindJQuery('section'), content: [

          {kind: OB.MODEL.DayCash},
          {kind: OB.MODEL.Order},
          {kind: OB.MODEL.OrderList},


          {kind: B.KindJQuery('div'), attr: {'class': 'row'}, content: [
            {kind: B.KindJQuery('div'), attr: {'class': 'span12'}, content: [
//              {kind: B.KindJQuery('div'), content: [{kind: B.KindHTML('<span>&nbsp;</span>')}]},
//              {kind: OB.COMP.ButtonTabPendingReceipts},
//              {kind: B.KindJQuery('div'), content: [{kind: B.KindHTML('<span>&nbsp;</span>')}]},
//              {kind: B.KindJQuery('div'), content: [{kind: B.KindHTML('<span>&nbsp;</span>')}]}
            ]}
          ]},
          {kind: B.KindJQuery('div'), attr: {'class': 'row'}, content: [
            {kind: B.KindJQuery('div'), attr: {'class': 'span6'}, content: [
               {kind: OB.COMP.DepositsDrops},
               {kind: OB.COMP.ModalDropDestinations},
               {kind: OB.COMP.CashChange}
             ]},
            {kind: B.KindJQuery('div'), attr: {'class': 'span6'}, content: [
              {kind: B.KindJQuery('div'), content: [
                {kind: OB.COMP.CashMgmtInfo }
              ]},
              {kind: OB.COMP.CashMgmtKeyboard }
            ]}
          ]}
        ]}
      );
    }
  });
  return OB.COMP.CashManagement;
});