/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

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
          {kind: OB.MODEL.DayCash},
          {kind: OB.Model.Order},
          {kind: OB.Collection.OrderList},
          {kind: OB.COMP.ModalCancel},
          {kind: OB.DATA.Container, content: [
            {kind: OB.DATA.DepositEvents},
            {kind: OB.DATA.DropEvents},
            {kind: OB.DATA.DepositsDrops},
            {kind: OB.DATA.CashMgmtPaymentMethod}
          ]},
          {kind: B.KindJQuery('div'), attr: {'class': 'row'}, content: [
            {kind: B.KindJQuery('div'), attr: {'class': 'span6'}, content: [
               {kind: OB.COMP.ListDepositsDrops},
               {kind: OB.COMP.DepositsDropsTicket}
             ]},
            {kind: B.KindJQuery('div'), attr: {'class': 'span6'}, content: [
              {kind: B.KindJQuery('div'), content: [
                {kind: OB.COMP.CashMgmtInfo }
              ]},
              {kind: OB.COMP.CashMgmtKeyboard }
            ]}
          ]},
          {kind: OB.UI.ModalDropEvents},
          {kind: OB.UI.ModalDepositEvents},
          {kind: OB.DATA.DropDepSave},
          {kind: OB.DATA.Container, content: [
            {kind: OB.COMP.HWManager, attr: {'templatecashmgmt': 'res/printcashmgmt.xml'}}
          ]}
        ]}
      );
    }
  });

  // register
  OB.POS.windows['retail.cashmanagement'] = OB.COMP.CashManagement;
}());