/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global B, $ */

(function() {

  OB = window.OB || {};

  OB.UI = window.OB.UI || {};

  OB.Model.WindowModel = Backbone.Model.extend({
    models: [],
    data: {},

    initialize: function() {
      var me = this,
          queue = {};
      _.each(this.models, function(item) {
        var ds;

        if (item.prototype.online) {
          ds = new OB.DS.DataSource(new OB.DS.Request(item, OB.POS.modelterminal.get('terminal').client, OB.POS.modelterminal.get('terminal').organization, OB.POS.modelterminal.get('terminal').id));

          queue[item.prototype.modelName] = false;
          ds.on('ready', function() {
            me.data[item.prototype.modelName] = new Backbone.Collection(ds.cache);
            console.log('loaded model', item);
            queue[item.prototype.modelName] = true;
            if (OB.UTIL.queueStatus(queue)) {
              me.trigger('ready');
            }
          });
          ds.load(item.params);
        }
      });

      this.on('ready', function() {
        if (this.init) {
          this.init();
        }
      }, this)
      //TODO: load offline models when regesitering window
    },

    getData: function(dsName) {
      return this.data[dsName];
    }
  });

  OB.Model.CashManagement = OB.Model.WindowModel.extend({
    models: [OB.Model.DepositsDrops, OB.Model.CashMgmtPaymentMethod, OB.Model.DropEvents, OB.Model.DepositEvents],
    init: function() {
      var depositEvent = this.getData('DataDepositEvents'),
          depList = this.getData('DataDepositsDrops'),
          deposits = this.getData('DataDepositsDrops').at(0).get('listdepositsdrops');

      this.depsdropstosend = new Backbone.Collection();

      depositEvent.on('paymentDone', function(model, p) {
        console.log('paymentDone', this, p);
        deposits.push({
          deposit: p.amount,
          drop: 0,
          description: p.identifier + ' - ' + model.get('name'),
          name: p.destinationKey,
          user: OB.POS.modelterminal.get('context').user._identifier,
          time: new Date()
        });

        this.depsdropstosend.add({
          amount: p.amount,
          description: p.identifier + ' - ' + model.get('name'),
          paymentMethodId: p.id,
          type: p.type,
          reasonId: model.get('id'),
          user: OB.POS.modelterminal.get('context').user._identifier,
          time: new Date().toString().substring(16, 21)
        });
        depList.trigger('reset');
      }, this);

      this.depsdropstosend.on('makeDeposits', function() {
        var process = new OB.DS.Process('org.openbravo.retail.posterminal.ProcessCashMgmt');
        process.exec({
        	depsdropstosend: this.depsdropstosend.toJSON()
        }, function(data, message){
        	console.log('done...',data,message);
        });
      }, this);
    }
  });

  OB.UI.WindowView = Backbone.View.extend({
    windowmodel: null,

    initialize: function() {
      var me = this;
      this.model = new this.windowmodel();
      this.model.on('ready', function() {
        OB.UTIL.initContentView(me);
        if (me.init) {
          me.init();
        }
        console.log('ready...');
        me.trigger('ready');
      });
      // OB.UTIL.initContentView(this);
      // debugger;
    }

  });

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
      //hidden stuff XXX:???
      {
        tag: 'div',
        content: [{
          view: OB.UI.ModalDepositEvents
        }]
      }]
    }],

    init: function() {
      var depositEvent = this.model.getData('DataDepositEvents');
      depositEvent.on('click', function(model) {
        depositEvent.trigger('paymentDone', model, this.options.currentPayment);
        delete this.options.currentPayment;
      }, this);
    }
  });

  //            	  OB.COMP.CustomView.extend({
  //                createView: function () {
  //                  return (
  //                    {kind: B.KindJQuery('section'), content: [
  //                      {kind: OB.COMP.ModalCancel},
  //                      {kind: OB.DATA.Container, content: [
  //                        {kind: OB.DATA.DepositEvents},
  //                        {kind: OB.DATA.DropEvents},
  //                        {kind: OB.DATA.DepositsDrops},
  //                        {kind: OB.DATA.CashMgmtPaymentMethod}
  //                      ]},
  //                      {kind: B.KindJQuery('div'), attr: {'class': 'row'}, content: [
  //                        {kind: B.KindJQuery('div'), attr: {'class': 'span6'}, content: [
  //                           {kind: OB.COMP.ListDepositsDrops}
  //                          // {kind: OB.COMP.DepositsDropsTicket}
  //                         ]},
  //                         
  //                        {kind: B.KindJQuery('div'), attr: {'class': 'span6'}, content: [
  //                          {kind: B.KindJQuery('div'), content: [
  //                            {kind: OB.COMP.CashMgmtInfo }
  //                          ]},
  //                          {kind: OB.COMP.CashMgmtKeyboard }
  //                        ]}
  //                      ]},
  //                      {kind: OB.UI.ModalDropEvents},
  //                      {kind: OB.UI.ModalDepositEvents},
  //                      {kind: OB.DATA.DropDepSave},
  //                      {kind: OB.DATA.Container, content: [
  //                        {kind: OB.COMP.HWManager, attr: {'templatecashmgmt': 'res/printcashmgmt.xml'}}
  //                      ]}
  //                    ]}
  //                  );
  //                }
  //              });
  //  // register
  OB.POS.registerWindow('retail.cashmanagement', OB.UI.CashManagement);
}());