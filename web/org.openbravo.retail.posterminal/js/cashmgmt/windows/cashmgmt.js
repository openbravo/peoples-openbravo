/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

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
    }, this);

    //TODO: load offline models when regesitering window
  },

  getData: function(dsName) {
    return this.data[dsName];
  }
});

OB.Model.CashManagement = OB.Model.WindowModel.extend({
  models: [OB.Model.DepositsDrops, OB.Model.CashMgmtPaymentMethod, OB.Model.DropEvents, OB.Model.DepositEvents],
  init: function() {
    var depList = this.getData('DataDepositsDrops');


    this.depsdropstosend = new Backbone.Collection();

    this.depsdropstosend.on('paymentDone', function(model, p) {
      var deposits, error = false;
      depList.each(function(dep) {
        if (p.destinationKey === dep.get('paySearchKey')) {
          error = (p.type === 'drop' && OB.DEC.sub(dep.get('total'), p.amount) < 0);
          deposits = dep.get('listdepositsdrops');
        }
      });
      if (error) {
        console.log('eerr');
        OB.UTIL.showError(OB.I18N.getLabel('OBPOS_MsgMoreThanAvailable'));
        return;
      }
      console.log('paymentDone', this, p);
      var payment = {
        description: p.identifier + ' - ' + model.get('name'),
        name: p.destinationKey,
        user: OB.POS.modelterminal.get('context').user._identifier,
        time: new Date()
      };

      if (p.type === 'drop') {
        payment.deposit = 0;
        payment.drop = p.amount;
      } else {
        payment.deposit = p.amount;
        payment.drop = 0
      }

      deposits.push(payment);

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
      var process = new OB.DS.Process('org.openbravo.retail.posterminal.ProcessCashMgmt'),
          me = this;

      OB.UTIL.showLoading(true);
      if (this.depsdropstosend.length === 0) {
        OB.POS.navigate('retail.pointofsale');
        return true;
      }

      process.exec({
        depsdropstosend: this.depsdropstosend.toJSON()
      }, function(data, message) {
        if (data && data.exception) {
          OB.UTIL.showLoading(false);
          OB.UTIL.showError(OB.I18N.getLabel('OBPOS_MsgErrorDropDep'));
        } else {
          // XXX: is this the way to print?
          var hw = new OB.COMP.HWManager(me);
          hw.depsdropstosend = me.depsdropstosend.toJSON();
          hw.attr({
            templatecashmgmt: 'res/printcashmgmt.xml'
          });
          me.trigger('print');
        }
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
      OB.POS.modelterminal.trigger('window:ready', me);
    });
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