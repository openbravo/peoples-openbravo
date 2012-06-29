/*global B, $ */

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


  OB.COMP.CashManagement = OB.COMP.CustomView.extend({
    createView: function () {
      return (
        {kind: B.KindJQuery('section'), content: [

          {kind: OB.DATA.Container, content: [
            {kind: OB.DATA.DepositsDrops}
          ]},
          {kind: OB.DATA.DropDepSave},
          {kind: OB.MODEL.DayCash},
          {kind: OB.MODEL.Order},
          {kind: OB.MODEL.OrderList},
          {kind: OB.DATA.DepositDestinations},
          {kind: OB.DATA.DropDestinations},
          {kind: OB.COMP.ModalCancel},
          {kind: OB.COMP.ModalDropDepDestinations},
          {kind: B.KindJQuery('div'), attr: {'class': 'row'}, content: [
            {kind: B.KindJQuery('div'), attr: {'class': 'span6'}, content: [
               {kind: OB.COMP.ListDepositsDrops}
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
  
  // register
  OB.POS.windows['retail.cashmanagement'] = OB.COMP.CashManagement;  
}());