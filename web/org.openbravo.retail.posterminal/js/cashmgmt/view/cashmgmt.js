/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

OB.UI.CashManagement = OB.UI.WindowView.extend({
  windowmodel: OB.Model.CashManagement,
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
        view: OB.COMP.ListDepositsDrops
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
          view: OB.COMP.CashMgmtInfo
        }]
      }, {
        view: OB.COMP.CashMgmtKeyboard
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
  }],

  init: function() {
    var depositEvent = this.model.getData('DataDepositEvents'),
        dropEvent = this.model.getData('DataDropEvents');

    // DepositEvent Collection is shown by TableView, when selecting an option 'click' event 
    // is triggered, propagating this UI event to model here
    depositEvent.on('click', function(model) {
      this.model.depsdropstosend.trigger('paymentDone', model, this.options.currentPayment);
      delete this.options.currentPayment;
    }, this);

    dropEvent.on('click', function(model) {
      this.model.depsdropstosend.trigger('paymentDone', model, this.options.currentPayment);
      delete this.options.currentPayment;
    }, this);
  }
});


OB.POS.registerWindow('retail.cashmanagement', OB.UI.CashManagement, 10);