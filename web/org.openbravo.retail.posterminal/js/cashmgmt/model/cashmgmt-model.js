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

// Window model
OB.OBPOSCashMgmt.Model.CashManagement = OB.Model.WindowModel.extend({
  models: [OB.Model.CashManagement],
  init: function () {
    var payments = new Backbone.Collection(),
        me = this,
        paymentMth, criteria, runSyncProcessCM, error, addedCashMgmt, selectedPayment;
    this.set('payments', new Backbone.Collection());
    this.set('cashMgmtDropEvents', new Backbone.Collection(OB.POS.modelterminal.get('cashMgmtDropEvents')));
    this.set('cashMgmtDepositEvents', new Backbone.Collection(OB.POS.modelterminal.get('cashMgmtDepositEvents')));
    OB.Dal.find(OB.Model.PaymentMethodCashUp, null, function (pays) {
      payments = pays;
      payments.each(function (pay) {
        criteria = {
          'paymentMethodId': pay.get('id')
        }
        paymentMth = OB.POS.modelterminal.get('payments').filter(function (payment) {
          return payment.payment.id === pay.get('id');
        })[0].paymentMethod;

        if (paymentMth.allowdeposits || paymentMth.allowdrops) {
          OB.Dal.find(OB.Model.CashManagement, criteria, function (cashmgmt, pay) {
            if (cashmgmt.length > 0) {
              pay.set('listdepositsdrops', cashmgmt.models);
            }
            me.get('payments').add(pay);
          }, null, pay);
        }
      });
    });

    this.depsdropstosave = new Backbone.Collection();
    this.depsdropstosave.on('paymentDone', function (model, p) {
      error = false;
      payments.each(function (pay) {
        if (p.id === pay.get('id')) {
          error = (p.type === 'drop' && OB.DEC.sub(pay.get('total'), OB.DEC.mul(p.amount, p.rate)) < 0);
        }
      });

      if (error) {
        OB.UTIL.showError(OB.I18N.getLabel('OBPOS_MsgMoreThanAvailable'));
        return;
      }
      addedCashMgmt = new OB.Model.CashManagement({
        id: OB.Dal.get_uuid(),
        description: p.identifier + ' - ' + model.get('name'),
        amount: p.amount,
        origAmount: OB.DEC.mul(p.amount, p.rate),
        type: p.type,
        reasonId: model.get('id'),
        paymentMethodId: p.id,
        user: OB.POS.modelterminal.get('context').user._identifier,
        time: new Date().toString().substring(16, 21),
        isocode: p.isocode,
        isbeingprocessed: 'N'
      });

      this.depsdropstosave.add(addedCashMgmt);

      selectedPayment = payments.filter(function (payment) {
        return payment.get('id') === p.id;
      })[0];
      if (selectedPayment.get('listdepositsdrops')) {
        selectedPayment.get('listdepositsdrops').push(addedCashMgmt);
        selectedPayment.trigger('change');
      } else {
        selectedPayment.set('listdepositsdrops', [addedCashMgmt]);
      }

      if (p.iscash || p.allowopendrawer) {
        OB.POS.hwserver.openDrawer();
      }
    }, this);

    this.depsdropstosave.on('makeDeposits', function () {
      // Done button has been clicked
      me = this;
      OB.UTIL.showLoading(true);

      if (this.depsdropstosave.length === 0) {
        // Nothing to do go to main window
        OB.POS.navigate('retail.pointofsale');
        return true;
      }

      this.printCashMgmt = new OB.OBPOSCashMgmt.Print.CashMgmt();

      function runSync() {
        if (OB.MobileApp.model.get('connectedToERP')) {
          OB.MobileApp.model.runSyncProcess(null, function () {
            OB.UTIL.showLoading(false);
            me.set("finished", true);
            if (OB.POS.modelterminal.hasPermission('OBPOS_print.cashmanagement')) {
              me.printCashMgmt.print(me.depsdropstosave.toJSON());
            }
          });
        } else {
          OB.UTIL.showLoading(false);
          me.set("finished", true);
          if (OB.POS.modelterminal.hasPermission('OBPOS_print.cashmanagement')) {
            me.printCashMgmt.print(me.depsdropstosave.toJSON());
          }
        }

      };
      runSyncProcessCM = _.after(this.depsdropstosave.models.length, runSync);
      // Sending drops/deposits to backend
      _.each(this.depsdropstosave.models, function (depdrop, index) {
        OB.Dal.save(depdrop, function () {
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