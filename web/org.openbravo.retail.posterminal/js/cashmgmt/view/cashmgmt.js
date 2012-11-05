/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global OB, enyo */

// Cash Management main window view
enyo.kind({
  name: 'OB.OBPOSCashMgmt.UI.CashManagement',
  kind: 'OB.UI.WindowView',
  windowmodel: OB.OBPOSCashMgmt.Model.CashManagement,
  tag: 'section',
  components: [{
    classes: 'row',
    components: [
    // 1st column: list of deposits/drops done or in process
    {
      classes: 'span6',
      components: [{
        kind: 'OB.OBPOSCashMgmt.UI.ListDepositsDrops'
      }]
    },
    //2nd column
    {
      classes: 'span6',
      components: [{
        kind: 'OB.OBPOSCashMgmt.UI.CashMgmtInfo'
      }, {
        kind: 'OB.OBPOSCashMgmt.UI.CashMgmtKeyboard'
      }]
    },
    //hidden stuff
    {
      components: [{
        kind: 'OB.OBPOSCashMgmt.UI.ModalDepositEvents',
        header: OB.I18N.getLabel('OBPOS_SelectDepositDestinations'),
        myId: 'modaldepositevents',
        type: 'DataDepositEvents'
      }, {
        kind: 'OB.OBPOSCashMgmt.UI.ModalDepositEvents',
        header: OB.I18N.getLabel('OBPOS_SelectDropDestinations'),
        myId: 'modaldropevents',
        type: 'DataDropEvents'

      }, {
        kind: OB.UI.ModalCancel
      }]
    }]
  }],

  init: function () {
    this.inherited(arguments);
    var depositEvent = this.model.getData('DataDepositEvents'),
        dropEvent = this.model.getData('DataDropEvents');

    // DepositEvent Collection is shown by OB.UI.Table, when selecting an option 'click' event 
    // is triggered, propagating this UI event to model here
    depositEvent.on('click', function (model) {
      this.model.depsdropstosend.trigger('paymentDone', model, this.currentPayment);
      delete this.currentPayment;
    }, this);

    dropEvent.on('click', function (model) {
      this.model.depsdropstosend.trigger('paymentDone', model, this.currentPayment);
      delete this.currentPayment;
    }, this);
  }
});


//OB.POS.registerWindow('retail.cashmanagement', OB.OBPOSCashMgmt.UI.CashManagement, 10);
OB.POS.registerWindow({
  windowClass: OB.OBPOSCashMgmt.UI.CashManagement,
  route: 'retail.cashmanagement',
  menuPosition: 10,
  online: true,
  menuLabel: OB.I18N.getLabel('OBPOS_LblCashManagement')
});