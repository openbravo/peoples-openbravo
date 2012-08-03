/*global OB, enyo */

/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

// Cash Management main window view
OB.OBPOSCasgMgmt.UI.CashManagement = OB.UI.WindowView.extend({
  windowmodel: OB.OBPOSCasgMgmt.Model.CashManagement,
  tagName: 'section',
  contentView: [{
    tag: 'div',
    attributes: {
      'class': 'row'
    },
    content: [
    // 1st column: list of deposits/drops done or in process
    {
      tag: 'div',
      attributes: {
        'class': 'span6'
      },
      content: [{
        view: OB.OBPOSCasgMgmt.UI.ListDepositsDrops
      }]
    },
    //2nd column:
    {
      tag: 'div',
      attributes: {
        'class': 'span6'
      },
      content: [{
        tag: 'div',
        attributes: {
          'class': 'span6'
        },
        content: [{
          view: OB.OBPOSCasgMgmt.UI.CashMgmtInfo
        }]
      }, {
        view: OB.OBPOSCasgMgmt.UI.CashMgmtKeyboard
      }]
    },
    //hidden stuff 
    {
      tag: 'div',
      content: [{
        view: OB.UI.ModalDepositEvents.extend({
          id: 'modaldepositevents',
          header: OB.I18N.getLabel('OBPOS_SelectDepositDestinations'),
          type: 'DataDepositEvents'
        })
      }, {
        view: OB.UI.ModalDepositEvents.extend({
          id: 'modaldropevents',
          header: OB.I18N.getLabel('OBPOS_SelectDropDestinations'),
          type: 'DataDropEvents'
        })
      }, {
        view: OB.COMP.ModalCancel
      }]
    }]
  }]

  //  init: function() {
  //    var depositEvent = this.model.getData('DataDepositEvents'),
  //        dropEvent = this.model.getData('DataDropEvents');
  //
  //    // DepositEvent Collection is shown by TableView, when selecting an option 'click' event 
  //    // is triggered, propagating this UI event to model here
  //    depositEvent.on('click', function(model) {
  //      this.model.depsdropstosend.trigger('paymentDone', model, this.options.currentPayment);
  //      delete this.options.currentPayment;
  //    }, this);
  //
  //    dropEvent.on('click', function(model) {
  //      this.model.depsdropstosend.trigger('paymentDone', model, this.options.currentPayment);
  //      delete this.options.currentPayment;
  //    }, this);
  //  }
});



enyo.kind({
  name: 'OB.OBPOSCasgMgmt.UI.CashManagement',
  kind: 'OB.UI.WindowView',
  windowmodel: OB.OBPOSCasgMgmt.Model.CashManagement,
  tag: 'section',
  components: [{
    tag: 'div',
    classes: 'row',
    components: [
    // 1st column: list of deposits/drops done or in process
    {
      tag: 'div',
      classes: 'span6',
      components: [{
        kind: 'OB.OBPOSCasgMgmt.UI.ListDepositsDrops'
      }]
    },
    //2nd column
    {
      tag: 'div',
      classes: 'span6',
      components: [{
        tag: 'div',
        classes: 'span6',
        components: [{
          kind: 'OB.OBPOSCasgMgmt.UI.CashMgmtInfo'
        }, {
          kind: 'OB.OBPOSCasgMgmt.UI.CashMgmtKeyboard'
        }]
      }]
    },
    //hidden stuff
    {
      tag: 'div',
      components: [{
        kind: 'OB.OBPOSCasgMgmt.UI.ModalDepositEvents',
        header: OB.I18N.getLabel('OBPOS_SelectDepositDestinations'),
        myId: 'modaldepositevents',
        type: 'DataDepositEvents'
      }, {
        kind: 'OB.OBPOSCasgMgmt.UI.ModalDepositEvents',
        header: OB.I18N.getLabel('OBPOS_SelectDropDestinations'),
        myId: 'modaldropevents',
        type: 'DataDropEvents'

      }, {
        kind: OB.UI.ModalCancel
      }]
    }]
  }],

  init: function() {
    this.inherited(arguments);
    var depositEvent = this.model.getData('DataDepositEvents'),
        dropEvent = this.model.getData('DataDropEvents');

    // DepositEvent Collection is shown by OB.UI.Table, when selecting an option 'click' event 
    // is triggered, propagating this UI event to model here
    depositEvent.on('click', function(model) {
      this.model.depsdropstosend.trigger('paymentDone', model, this.currentPayment);
      delete this.currentPayment;
    }, this);

    dropEvent.on('click', function(model) {
      this.model.depsdropstosend.trigger('paymentDone', model, this.currentPayment);
      delete this.currentPayment;
    }, this);
  }
});


OB.POS.registerWindow('retail.cashmanagement', OB.OBPOSCasgMgmt.UI.CashManagement, 10);