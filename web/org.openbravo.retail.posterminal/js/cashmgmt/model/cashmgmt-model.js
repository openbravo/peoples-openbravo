/*
 ************************************************************************************
 * Copyright (C) 2012-2015 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global OB, Backbone, _, TestRegistry */

OB.OBPOSCashMgmt = OB.OBPOSCashMgmt || {};
OB.OBPOSCashMgmt.Model = OB.OBPOSCashMgmt.Model || {};
OB.OBPOSCashMgmt.UI = OB.OBPOSCashMgmt.UI || {};

// Window model
OB.OBPOSCashMgmt.Model.CashManagement = OB.Model.WindowModel.extend({
  models: [OB.Model.CashManagement],
  init: function () {
    var payments = new Backbone.Collection(),
        me = this,
        slavePayments = null,
        paymentMth, criteria, runSyncProcessCM, error, addedCashMgmt, selectedPayment;
    this.set('payments', new Backbone.Collection());
    this.set('cashMgmtDropEvents', new Backbone.Collection(OB.MobileApp.model.get('cashMgmtDropEvents')));
    this.set('cashMgmtDepositEvents', new Backbone.Collection(OB.MobileApp.model.get('cashMgmtDepositEvents')));

    function loadCashup() {
      //        this.slavePayments = slavePayments;
      OB.Dal.find(OB.Model.CashUp, {
        'isprocessed': 'N'
      }, function (cashUp) {
        OB.Dal.find(OB.Model.PaymentMethodCashUp, {
          'cashup_id': cashUp.at(0).get('id'),
          _orderByClause: 'searchKey desc'
        }, function (pays) {
          payments = pays;
          payments.each(function (pay) {
            criteria = {
              'paymentMethodId': pay.get('paymentmethod_id'),
              'cashup_id': cashUp.at(0).get('id')
            };
            paymentMth = OB.MobileApp.model.get('payments').filter(function (payment) {
              return payment.payment.id === pay.get('paymentmethod_id');
            })[0].paymentMethod;

            if (OB.POS.modelterminal.get('terminal').isslave && paymentMth.isshared) {
              return true;
            }

            if (paymentMth.allowdeposits || paymentMth.allowdrops) {
              OB.Dal.find(OB.Model.CashManagement, criteria, function (cashmgmt, pay) {
                if (cashmgmt.length > 0) {
                  pay.set('listdepositsdrops', cashmgmt.models);
                }
                if (slavePayments) {
                  // Accumulate slave payments
                  _.each(slavePayments, function (slavePay) {
                    if (slavePay.searchKey === pay.get('searchKey')) {
                      pay.set('startingCash', OB.DEC.add(pay.get('startingCash'), slavePay.startingCash));
                      pay.set('totalDeposits', OB.DEC.add(pay.get('totalDeposits'), slavePay.totalDeposits));
                      pay.set('totalDrops', OB.DEC.add(pay.get('totalDrops'), slavePay.totalDrops));
                      pay.set('totalReturns', OB.DEC.add(pay.get('totalReturns'), slavePay.totalReturns));
                      pay.set('totalSales', OB.DEC.add(pay.get('totalSales'), slavePay.totalSales));
                    }
                  });
                }
                me.get('payments').add(pay);
              }, null, pay);
            }
          });
        });
      }, null, this);
    };


    if (OB.POS.modelterminal.get('terminal').ismaster) {
      // Load current cashup info from slaves
      new OB.DS.Process('org.openbravo.retail.posterminal.ProcessCashMgmtMaster').exec({
        cashUpId: OB.POS.modelterminal.get('terminal').cashUpId,
        terminalSlave: OB.POS.modelterminal.get('terminal').isslave
      }, function (data) {
        if (data && data.exception) {
          // Error handler 
          OB.log('error', data.exception.message);
          OB.UTIL.showAlert.display(data.exception.message, OB.I18N.getLabel('OBMOBC_LblError'), 'alert-error', false);
        } else {
          slavePayments = data;
          loadCashup();
        }
      });
    } else {
      // Load terminal cashup info (without slaves info)
      loadCashup();
    }

    this.depsdropstosave = new Backbone.Collection();
    this.depsdropstosave.on('paymentDone', function (model, p) {
      error = false;
      payments.each(function (pay) {
        if (p.id === pay.get('paymentmethod_id')) {
          error = (p.type === 'drop' && OB.DEC.sub(pay.get('total'), OB.DEC.mul(p.amount, p.rate)) < 0);
        }
      });

      if (error) {
        OB.UTIL.showError(OB.I18N.getLabel('OBPOS_MsgMoreThanAvailable'));
        return;
      }

      OB.Dal.find(OB.Model.CashUp, {
        'isprocessed': 'N'
      }, function (cashUp) {
        addedCashMgmt = new OB.Model.CashManagement({
          id: OB.Dal.get_uuid(),
          description: p.identifier + ' - ' + model.get('name'),
          amount: p.amount,
          origAmount: OB.DEC.mul(p.amount, p.rate),
          type: p.type,
          reasonId: model.get('id'),
          paymentMethodId: p.id,
          user: OB.MobileApp.model.get('context').user._identifier,
          userId: OB.MobileApp.model.get('context').user.id,
          time: new Date().toString().substring(16, 21),
          isocode: p.isocode,
          glItem: p.glItem,
          cashup_id: cashUp.at(0).get('id'),
          isbeingprocessed: 'N'
        });
        me.depsdropstosave.add(addedCashMgmt);

        selectedPayment = payments.filter(function (payment) {
          return payment.get('paymentmethod_id') === p.id;
        })[0];
        if (selectedPayment.get('listdepositsdrops')) {
          selectedPayment.get('listdepositsdrops').push(addedCashMgmt);
          selectedPayment.trigger('change');
        } else {
          selectedPayment.set('listdepositsdrops', [addedCashMgmt]);
        }

      }, null, this);
    }, this);

    this.depsdropstosave.on('makeDeposits', function () {
      // Done button has been clicked
      me = this;
      TestRegistry.CashMgmt = TestRegistry.CashMgmt || {};
      TestRegistry.CashMgmt.isCashDepositPrinted = false;

      OB.UTIL.showLoading(true);

      if (this.depsdropstosave.length === 0) {
        // Nothing to do go to main window
        OB.POS.navigate('retail.pointofsale');
        return true;
      }

      this.printCashMgmt = new OB.OBPOSCashMgmt.Print.CashMgmt();

      TestRegistry.CashMgmt.isCashDepositPrinted = true;

      function runSync() {
        if (OB.MobileApp.model.get('connectedToERP')) {
          OB.MobileApp.model.runSyncProcess(function () {
            OB.UTIL.showLoading(false);
            me.set("finished", true);
            if (OB.MobileApp.model.hasPermission('OBPOS_print.cashmanagement')) {
              me.printCashMgmt.print(me.depsdropstosave.toJSON());
            }
          });
        } else {
          OB.UTIL.showLoading(false);
          me.set("finished", true);
          if (OB.MobileApp.model.hasPermission('OBPOS_print.cashmanagement')) {
            me.printCashMgmt.print(me.depsdropstosave.toJSON());
          }
        }
      }

      var paymentList = new Backbone.Collection(),
          found = false,
          i;

      function addAttributes(depdrop) {
        var payment = new OB.Model.PaymentMethodCashUp();
        if (depdrop.get('type') === 'deposit') {
          payment.set('paymentMethodId', depdrop.get('paymentMethodId'));
          payment.set('cashup_id', depdrop.get('cashup_id'));
          payment.set('totalDeposits', depdrop.get('amount'));
          payment.set('totalDrops', 0);
        } else {
          payment.set('paymentMethodId', depdrop.get('paymentMethodId'));
          payment.set('cashup_id', depdrop.get('cashup_id'));
          payment.set('totalDrops', depdrop.get('amount'));
          payment.set('totalDeposits', 0);
        }
        return payment;
      }
      _.each(this.depsdropstosave.models, function (depdrop, index) {
        if (paymentList.length > 0) {
          for (i = 0; i < paymentList.length; i++) {
            found = false;
            if (paymentList.models[i].get('paymentMethodId') === depdrop.get('paymentMethodId')) {
              var paymentMethod = paymentList.models[i],
                  totalDeposits = 0,
                  totalDrops = 0,
                  depos = paymentMethod.get('totalDeposits'),
                  drop = paymentMethod.get('totalDrops');
              if (depdrop.get('type') === 'deposit') {
                totalDeposits = OB.DEC.add(depos, depdrop.get('amount'));
                paymentMethod.set('totalDeposits', totalDeposits);
              } else {
                totalDrops = OB.DEC.add(drop, depdrop.get('amount'));
                paymentMethod.set('totalDrops', totalDrops);
              }
              found = true;
              break;
            }
          }
          if (!found) {
            paymentList.add(addAttributes(depdrop));
          }
        } else {
          paymentList.add(addAttributes(depdrop));
        }
      }, this);

      runSyncProcessCM = _.after(this.depsdropstosave.models.length, runSync);
      // Sending drops/deposits to backend
      _.each(this.depsdropstosave.models, function (depdrop, index) {
        OB.Dal.save(depdrop, function () {
          OB.UTIL.sumCashManagementToCashup(paymentList.models[index]);
          OB.UTIL.calculateCurrentCash();
          runSyncProcessCM();
        }, function (error) {
          OB.UTIL.showLoading(false);
          me.set("finishedWrongly", true);
          return;
        }, true);
      }, this);
    }, this);
  }
});