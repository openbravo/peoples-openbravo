/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global OB, Backbone */

OB.OBPOSCashMgmt = OB.OBPOSCashMgmt || {};
OB.OBPOSCashMgmt.Model = OB.OBPOSCashMgmt.Model || {};
OB.OBPOSCashMgmt.UI = OB.OBPOSCashMgmt.UI || {};

// Data models
OB.OBPOSCashMgmt.Model.DepositsDrops = Backbone.Model.extend({
  source: 'org.openbravo.retail.posterminal.term.CashMgmtDepositsDrops',
  modelName: 'DataDepositsDrops',
  online: true
});

OB.OBPOSCashMgmt.Model.CashMgmtPaymentMethod = Backbone.Model.extend({
  source: 'org.openbravo.retail.posterminal.term.CashMgmtPayments',
  modelName: 'DataCashMgmtPaymentMethod',
  online: true
});

OB.OBPOSCashMgmt.Model.DropEvents = Backbone.Model.extend({
  source: 'org.openbravo.retail.posterminal.term.CashMgmtDropEvents',
  modelName: 'DataDropEvents',
  online: true
});

OB.OBPOSCashMgmt.Model.DepositEvents = Backbone.Model.extend({
  source: 'org.openbravo.retail.posterminal.term.CashMgmtDepositEvents',
  modelName: 'DataDepositEvents',
  online: true
});


// Window model
OB.OBPOSCashMgmt.Model.CashManagement = OB.Model.WindowModel.extend({
  models: [OB.OBPOSCashMgmt.Model.DepositsDrops, OB.OBPOSCashMgmt.Model.CashMgmtPaymentMethod, OB.OBPOSCashMgmt.Model.DropEvents, OB.OBPOSCashMgmt.Model.DepositEvents],
  init: function () {

    var depList = this.getData('DataDepositsDrops'),
        me = this;

    this.depsdropstosend = new Backbone.Collection();

    this.depsdropstosend.on('paymentDone', function (model, p) {
      // Payment done locally, saving it in local list
      var deposits, error = false,
          tmp;

      depList.each(function (dep) {
        if (p.destinationKey === dep.get('paySearchKey')) {
          error = (p.type === 'drop' && OB.DEC.sub(dep.get('total'), OB.DEC.mul(p.amount, p.rate)) < 0);
          deposits = dep.get('listdepositsdrops');
          tmp = dep;
        }
      });

      if (error) {
        OB.UTIL.showError(OB.I18N.getLabel('OBPOS_MsgMoreThanAvailable'));
        return;
      }

      p.amount = OB.DEC.add(p.amount, 0);

      var payment = {
        description: p.identifier + ' - ' + model.get('name'),
        name: p.destinationKey,
        user: OB.POS.modelterminal.get('context').user._identifier,
        time: new Date(),
        origAmount: OB.DEC.mul(p.amount, p.rate),
        isocode: p.isocode
      };

      if (p.type === 'drop') {
        payment.deposit = 0;
        payment.drop = p.amount;
      } else {
        payment.deposit = p.amount;
        payment.drop = 0;
      }

      deposits.push(payment);

      this.depsdropstosend.add({
        amount: p.amount,
        description: p.identifier + ' - ' + model.get('name'),
        paymentMethodId: p.id,
        type: p.type,
        reasonId: model.get('id'),
        user: OB.POS.modelterminal.get('context').user._identifier,
        time: new Date().toString().substring(16, 21),
        origAmount: OB.DEC.mul(p.amount, p.rate),
        isocode: p.isocode
      });
      tmp.trigger('change');
    }, this);

    this.depsdropstosend.on('makeDeposits', function () {
      // Done button has been clicked
      var process = new OB.DS.Process('org.openbravo.retail.posterminal.ProcessCashMgmt'),
          me = this;

      OB.UTIL.showLoading(true);

      if (this.depsdropstosend.length === 0) {
        // Nothing to do go to main window
        OB.POS.navigate('retail.pointofsale');
        return true;
      }

      // Sending drops/deposits to backend
      process.exec({
        depsdropstosend: this.depsdropstosend.toJSON()
      }, function (data, message) {
        if (data && data.exception) {
          OB.UTIL.showLoading(false);
          me.set("finishedWrongly", true);
        } else {
          if (OB.POS.modelterminal.hasPermission('OBPOS_print.cashmanagement')) {
            me.printCashMgmt.print(me.depsdropstosend.toJSON());
          }
          OB.UTIL.showLoading(false);
          me.set("finished", true);
        }
      });
    }, this);

    this.printCashMgmt = new OB.OBPOSCashMgmt.Print.CashMgmt();
  }
});