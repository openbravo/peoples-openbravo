/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global OB, enyo, Backbone, _, $ */

OB.OBPOSCashUp = OB.OBPOSCashUp || {};
OB.OBPOSCashUp.Model = OB.OBPOSCashUp.Model || {};
OB.OBPOSCashUp.UI = OB.OBPOSCashUp.UI || {};

//Window model
OB.OBPOSCashUp.Model.CashUp = OB.Model.TerminalWindowModel.extend({
  models: [OB.Model.Order],
  defaults: {
    step: OB.DEC.Zero,
    allowedStep: OB.DEC.Zero,
    totalExpected: OB.DEC.Zero,
    totalCounted: OB.DEC.Zero,
    totalDifference: OB.DEC.Zero,
    pendingOrdersToProcess: false,
    otherInput: OB.DEC.Zero
  },
  cashupstepsdefinition: ['OB.CashUp.StepPendingOrders', 'OB.CashUp.CashPayments', 'OB.CashUp.PaymentMethods', 'OB.CashUp.CashToKeep', 'OB.CashUp.PostPrintAndClose'],
  init: function () {
    //Check for orders wich are being processed in this moment.
    //cancel -> back to point of sale
    //Ok -> Continue closing without these orders
    var undf, me = this,
        newstep, expected = 0,
        totalStartings = 0,
        startings = [],
        cashUpReport, tempList = new Backbone.Collection();

    //steps
    this.set('step', 1);
    this.set('substep', 0);

    // Create steps instances
    this.cashupsteps = [];
    _.each(this.cashupstepsdefinition, function (s) {
      newstep = enyo.createFromKind(s);
      newstep.model = this;
      this.cashupsteps.push(newstep);
    }, this);

    this.set('orderlist', new OB.Collection.OrderList());
    this.set('paymentList', new Backbone.Collection());
    OB.Dal.find(OB.Model.CashUp, {
      'isbeingprocessed': 'N'
    }, function (cashUp) {
      OB.Dal.find(OB.Model.PaymentMethodCashUp, {
        'cashup_id': cashUp.at(0).get('id'),
        '_orderByClause': 'name asc'
      }, function (payMthds) { //OB.Dal.find success
        _.each(OB.POS.modelterminal.get('payments'), function (payment, index) {
          expected = 0;
          var auxPay = payMthds.filter(function (payMthd) {
            return payMthd.get('paymentmethod_id') === payment.payment.id;
          })[0];
          if (!auxPay) { //We cannot find this payment in local database, it must be a new payment method, we skip it.
            return;
          }
          auxPay.set('_id', payment.payment.searchKey);
          auxPay.set('isocode', payment.isocode);
          auxPay.set('paymentMethod', payment.paymentMethod);
          auxPay.set('id', payment.payment.id);
          OB.Dal.find(OB.Model.CashManagement, {
            'cashup_id': cashUp.at(0).get('id'),
            'paymentMethodId': payment.payment.id
          }, function (cashMgmts, args) {
            expected = OB.DEC.add(OB.DEC.add(OB.DEC.mul(auxPay.get('startingCash'), auxPay.get('rate')), OB.DEC.sub(OB.DEC.mul(auxPay.get('totalSales'), auxPay.get('rate')), OB.DEC.mul(auxPay.get('totalReturns'), auxPay.get('rate')))), _.reduce(cashMgmts.models, function (accum, trx) {
              if (trx.get('type') === 'deposit') {
                return OB.DEC.add(accum, trx.get('origAmount'));
              } else {
                return OB.DEC.sub(accum, trx.get('origAmount'));
              }
            }, 0));
            auxPay.set('expected', expected);
            auxPay.set('foreignExpected', OB.DEC.div(expected, auxPay.get('rate')));
            tempList.add(auxPay);
            if (args.index === OB.POS.modelterminal.get('payments').length - 1) {
              me.get('paymentList').reset(tempList.models);
              me.set('totalExpected', _.reduce(me.get('paymentList').models, function (total, model) {
                return OB.DEC.add(total, model.get('expected'));
              }, 0));
              me.set('totalDifference', OB.DEC.sub(me.get('totalDifference'), me.get('totalExpected')));
            }
          }, null, {
            me: me,
            index: index
          });
        }, this);
      });
    }, this);

    this.convertExpected();
    this.setIgnoreStep3();

    this.set('cashUpReport', new Backbone.Collection());
    OB.Dal.find(OB.Model.CashUp, {
      'isbeingprocessed': 'N'
    }, function (cashUp) {
      cashUpReport = cashUp.at(0);
      OB.Dal.find(OB.Model.CashManagement, {
        'cashup_id': cashUpReport.get('id'),
        'type': 'deposit'
      }, function (cashMgmts) {
        cashUpReport.set('deposits', cashMgmts.models);
        cashUpReport.set('totalDeposits', _.reduce(cashMgmts.models, function (accum, trx) {
          return OB.DEC.add(accum, trx.get('origAmount'));
        }, 0));
      }, this);
      OB.Dal.find(OB.Model.CashManagement, {
        'cashup_id': cashUpReport.get('id'),
        'type': 'drop'
      }, function (cashMgmts) {
        cashUpReport.set('drops', cashMgmts.models);
        cashUpReport.set('totalDrops', _.reduce(cashMgmts.models, function (accum, trx) {
          return OB.DEC.add(accum, trx.get('origAmount'));
        }, 0));
      }, this);
      OB.Dal.find(OB.Model.TaxCashUp, {
        'cashup_id': cashUpReport.get('id'),
        'orderType': {
          operator: '!=',
          value: '1'
        }
      }, function (taxcashups) {
        cashUpReport.set('salesTaxes', taxcashups.models);
      }, this);
      OB.Dal.find(OB.Model.TaxCashUp, {
        'cashup_id': cashUpReport.get('id'),
        'orderType': '1'
      }, function (taxcashups) {
        cashUpReport.set('returnsTaxes', taxcashups.models);
      }, this);
      OB.Dal.find(OB.Model.PaymentMethodCashUp, {
        'cashup_id': cashUpReport.get('id'),
        '_orderByClause': 'name asc'
      }, function (payMthds) { //OB.Dal.find success
        cashUpReport.set('totalStartings', _.reduce(payMthds.models, function (accum, trx) {
          return OB.DEC.add(accum, OB.DEC.mul(trx.get('startingCash'), trx.get('rate')));
        }, 0));
        _.each(payMthds.models, function (p, index) {
          var auxPay = OB.POS.modelterminal.get('payments').filter(function (pay) {
            return pay.payment.id === p.get('paymentmethod_id');
          })[0];
          if (!auxPay) { //We cannot find this payment in local database, it must be a new payment method, we skip it.
            return;
          }
          cashUpReport.get('deposits').push(new Backbone.Model({
            origAmount: OB.DEC.add(0, OB.DEC.mul(p.get('totalSales'), p.get('rate'))),
            amount: OB.DEC.add(0, p.get('totalSales')),
            description: p.get('name'),
            isocode: auxPay.isocode,
            rate: p.get('rate')
          }));
          cashUpReport.set('totalDeposits', OB.DEC.add(cashUpReport.get('totalDeposits'), OB.DEC.mul(p.get('totalSales'), p.get('rate'))));
          cashUpReport.get('drops').push(new Backbone.Model({
            origAmount: OB.DEC.mul(OB.DEC.add(0, p.get('totalReturns')), p.get('rate')),
            amount: OB.DEC.add(0, p.get('totalReturns')),
            description: p.get('name'),
            isocode: auxPay.isocode,
            rate: p.get('rate')
          }));
          cashUpReport.set('totalDrops', OB.DEC.add(cashUpReport.get('totalDrops'), OB.DEC.mul(p.get('totalReturns'), p.get('rate'))));
          startings.push(new Backbone.Model({
            amount: OB.DEC.mul(p.get('startingCash'), p.get('rate')),
            description: 'Starting ' + p.get('name'),
            isocode: auxPay.isocode,
            rate: p.get('rate'),
            paymentId: p.get('paymentmethod_id')
          }));
        }, this);
        cashUpReport.set('startings', startings);
        //FIXME: We are not sure if other finds are done.
        OB.MobileApp.model.hookManager.executeHooks('OBPOS_EditCashupReport', {
          cashUpReport: cashUpReport
        }, function (args) {
          me.get('cashUpReport').add(args.cashUpReport);
        });
      }, this);
    }, this);

    this.get('paymentList').on('change:counted', function (mod) {
      mod.set('difference', OB.DEC.sub(mod.get('counted'), mod.get('expected')));
      if (mod.get('foreignCounted') !== null && mod.get('foreignCounted') !== undf && mod.get('foreignExpected') !== null && mod.get('foreignExpected') !== undf) {
        mod.set('foreignDifference', OB.DEC.sub(mod.get('foreignCounted'), mod.get('foreignExpected')));
      }
      this.set('totalCounted', _.reduce(this.get('paymentList').models, function (total, model) {
        return model.get('counted') ? OB.DEC.add(total, model.get('counted')) : total;
      }, 0), 0);
      if (mod.get('counted') === OB.DEC.Zero) {
        this.trigger('change:totalCounted');
      }
    }, this);

    OB.Dal.find(OB.Model.Order, {
      hasbeenpaid: 'N'
    }, function (pendingOrderList, me) {
      var emptyOrders;
      // Detect empty orders and remove them from here
      emptyOrders = _.filter(pendingOrderList.models, function (pendingorder) {
        if (pendingorder && pendingorder.get('lines') && pendingorder.get('lines').length === 0) {
          return true;
        }
      });

      _.each(emptyOrders, function (orderToRemove) {
        pendingOrderList.remove(orderToRemove);
      });

      // Recalculate total properly for all  pendingorders.
      pendingOrderList.each(function (pendingorder) {
        OB.DATA.OrderTaxes(pendingorder);
        pendingorder.calculateGross();
      });

      me.get('orderlist').reset(pendingOrderList.models);
    }, function (tx, error) {
      OB.UTIL.showError("OBDAL error: " + error);
    }, this);

    this.printCashUp = new OB.OBPOSCashUp.Print.CashUp();
  },
  //Previous next
  allowNext: function () {
    return this.cashupsteps[this.get('step') - 1].allowNext();
  },
  allowPrevious: function () {
    return this.get('step') > 1;
  },
  setIgnoreStep3: function () {
    var result = null;
    _.each(this.get('paymentList').models, function (model) {
      if (model.get('paymentMethod').automatemovementtoother === false) {
        model.set('qtyToKeep', 0);
        model.set('foreignCounted', 0);
        if (result !== false) {
          result = true;
        }
      } else {
        //fix -> break
        result = false;
        return false;
      }
    }, this);
    this.set('ignoreStep3', result);
  },
  showStep: function (leftpanel$) {
    var currentstep = this.get('step') - 1;
    var i;
    var stepcomponent;

    for (i = 0; i < this.cashupsteps.length; i++) {
      stepcomponent = this.cashupsteps[i].getStepComponent(leftpanel$);
      stepcomponent.setShowing(i === currentstep);
      if (i === currentstep) {
        stepcomponent.displayStep(this);
      }
    }
  },
  getStepToolbar: function () {
    var currentstep = this.get('step') - 1;
    return this.cashupsteps[currentstep].getToolbarName();
  },
  nextButtonI18NLabel: function () {
    var currentstep = this.get('step') - 1;
    return this.cashupsteps[currentstep].nextButtonI18NLabel();
  },
  isFinishedWizard: function (step) {
    return step > this.cashupsteps.length;
  },
  getSubstepsLength: function (step) {
    return this.cashupsteps[step - 1].getSubstepsLength(this);
  },
  isSubstepAvailable: function (step, substep) {
    return this.cashupsteps[step - 1].isSubstepAvailable(this, substep);
  },
  verifyStep: function (leftpanel$, callback) {
    var currentstep = this.get('step') - 1;
    var stepcomponent = this.cashupsteps[currentstep].getStepComponent(leftpanel$);
    if (stepcomponent.verifyStep) {
      return stepcomponent.verifyStep(this, callback);
    } else {
      callback();
    }
  },
  isPaymentMethodListVisible: function () {
    return this.get('step') === 2;
  },
  // Step 2: logic, expected vs counted 
  countAll: function () {
    this.get('paymentList').each(function (model) {
      model.set('foreignCounted', OB.DEC.add(0, model.get('foreignExpected')));
      model.set('counted', OB.DEC.add(0, model.get('expected')));
    });
  },

  //step 3
  validateCashKeep: function (qty) {
    var unfd, result = {
      result: false,
      message: ''
    };
    if (qty !== unfd && qty !== null && $.isNumeric(qty)) {
      if (this.get('paymentList').at(this.get('substep')).get('foreignCounted') >= qty) {
        result.result = true;
        result.message = '';
      } else {
        result.result = false;
        result.message = OB.I18N.getLabel('OBPOS_MsgMoreThanCounted');
      }
    } else {
      result.result = false;
      result.message = 'Not valid number to keep';
    }
    if (!result.result) {
      this.get('paymentList').at(this.get('substep')).set('qtyToKeep', null);
    }
    return result;
  },

  //Step 4
  getCountCashSummary: function () {
    var countCashSummary, counter, enumConcepts, enumSecondConcepts, enumSummarys, i, undf, model, value = OB.DEC.Zero,
        second = OB.DEC.Zero;
    countCashSummary = {
      expectedSummary: [],
      countedSummary: [],
      differenceSummary: [],
      qtyToKeepSummary: [],
      qtyToDepoSummary: [],
      totalCounted: this.get('totalCounted'),
      totalExpected: this.get('totalExpected'),
      totalDifference: this.get('totalDifference'),
      totalQtyToKeep: _.reduce(this.get('paymentList').models, function (total, model) {
        if (model.get('qtyToKeep')) {
          return OB.DEC.add(total, OB.DEC.mul(model.get('qtyToKeep'), model.get('rate')));
        } else {
          return total;
        }
      }, 0),
      totalQtyToDepo: _.reduce(this.get('paymentList').models, function (total, model) {
        if (model.get('qtyToKeep') !== null && model.get('qtyToKeep') !== undf && model.get('foreignCounted') !== null && model.get('foreignCounted') !== undf) {
          return OB.DEC.add(total, OB.DEC.mul(OB.DEC.sub(model.get('foreignCounted'), model.get('qtyToKeep')), model.get('rate')));
        } else if (model.get('foreignCounted') !== null && model.get('foreignCounted') !== undf) {
          return OB.DEC.add(total, OB.DEC.mul(model.get('foreignCounted'), model.get('rate')));
        } else {
          return total;
        }
      }, 0)
    };
    //First we fix the qty to keep for non-automated payment methods
    _.each(this.get('paymentList').models, function (model) {
      if (OB.UTIL.isNullOrUndefined(model.get('qtyToKeep'))) {
        model.set('qtyToKeep', model.get('counted'));
      }
    });

    enumSummarys = ['expectedSummary', 'countedSummary', 'differenceSummary', 'qtyToKeepSummary', 'qtyToDepoSummary'];
    enumConcepts = ['expected', 'counted', 'difference', 'qtyToKeep', 'foreignCounted'];
    enumSecondConcepts = ['foreignExpected', 'foreignCounted', 'foreignDifference', 'qtyToKeep', 'qtyToKeep'];
    for (counter = 0; counter < 5; counter++) {
      var sortedPays = _.sortBy(this.get('paymentList').models, function (p) {
        return p.get('name');
      });
      for (i = 0; i < sortedPays.length; i++) {
        model = sortedPays[i];
        if (!model.get(enumConcepts[counter])) {
          countCashSummary[enumSummarys[counter]].push(new Backbone.Model({
            name: model.get('name'),
            value: 0,
            second: 0,
            isocode: ''
          }));
        } else {
          switch (enumSummarys[counter]) {
          case 'qtyToKeepSummary':
            if (model.get(enumSecondConcepts[counter]) !== null && model.get(enumSecondConcepts[counter]) !== undf) {
              value = OB.DEC.mul(model.get(enumConcepts[counter]), model.get('rate'));
              second = model.get(enumSecondConcepts[counter]);
            }
            break;
          case 'qtyToDepoSummary':
            if (model.get(enumSecondConcepts[counter]) !== null && model.get(enumSecondConcepts[counter]) !== undf && model.get('rate') !== '1') {
              second = OB.DEC.sub(model.get(enumConcepts[counter]), model.get(enumSecondConcepts[counter]));
            } else {
              second = OB.DEC.Zero;
            }
            if (model.get(enumSecondConcepts[counter]) !== null && model.get(enumSecondConcepts[counter]) !== undf) {
              value = OB.DEC.mul(OB.DEC.sub(model.get(enumConcepts[counter]), model.get(enumSecondConcepts[counter])), model.get('rate'));
            } else {
              value = OB.DEC.Zero;
            }

            break;
          default:
            value = model.get(enumConcepts[counter]);
            second = model.get(enumSecondConcepts[counter]);
          }
          countCashSummary[enumSummarys[counter]].push(new Backbone.Model({
            name: model.get('name'),
            value: value,
            second: second,
            isocode: model.get('isocode')
          }));
        }
      }
    }
    return countCashSummary;
  },
  additionalProperties: [],
  propertyFunctions: [],
  processAndFinishCashUp: function () {
    var objToSend = {
      terminalId: OB.POS.modelterminal.get('terminal').id,
      cashUpId: OB.UTIL.get_UUID(),
      cashCloseInfo: [],
      cashUpDate: this.get('cashUpReport').at(0).get('time')
    },
        server = new OB.DS.Process('org.openbravo.retail.posterminal.ProcessCashClose'),
        me = this,
        i;
    OB.UTIL.showLoading(true);
    OB.Dal.find(OB.Model.CashUp, {
      'isbeingprocessed': 'N'
    }, function (cashUp) {
      objToSend = new Backbone.Model({
        terminalId: OB.POS.modelterminal.get('terminal').id,
        cashUpId: cashUp.at(0).get('id'),
        cashCloseInfo: [],
        cashUpDate: me.get('cashUpReport').at(0).get('time')
      });
      for (i = 0; i < me.additionalProperties.length; i++) {
        objToSend.set(me.additionalProperties[i], me.propertyFunctions[i](OB.POS.modelterminal.get('terminal').id, cashUp.at(0)));
      }
      _.each(me.get('paymentList').models, function (curModel) {
        var cashCloseInfo = {
          expected: 0,
          difference: 0,
          paymentTypeId: 0,
          paymentMethod: {}
        };
        cashCloseInfo.paymentTypeId = curModel.get('id');
        cashCloseInfo.difference = curModel.get('difference');
        cashCloseInfo.foreignDifference = curModel.get('foreignDifference');
        cashCloseInfo.expected = curModel.get('expected');
        cashCloseInfo.foreignExpected = curModel.get('foreignExpected');
        curModel.get('paymentMethod').amountToKeep = curModel.get('qtyToKeep');
        cashCloseInfo.paymentMethod = curModel.get('paymentMethod');
        objToSend.get('cashCloseInfo').push(cashCloseInfo);
      }, this);

      objToSend.set('cashMgmtIds', []);
      OB.Dal.find(OB.Model.CashManagement, {
        'cashup_id': cashUp.at(0).get('id')
      }, function (cashMgmts) {
        _.each(cashMgmts.models, function (cashMgmt) {
          objToSend.get('cashMgmtIds').push(cashMgmt.get('id'));
        });
        cashUp.at(0).set('userId', OB.POS.modelterminal.get('context').user.id);
        objToSend.set('userId', OB.POS.modelterminal.get('context').user.id);
        cashUp.at(0).set('objToSend', JSON.stringify(objToSend.toJSON()));
        cashUp.at(0).set('isbeingprocessed', 'Y');
        OB.Dal.save(cashUp.at(0), null, null);
        if (OB.MobileApp.model.get('connectedToERP')) {
          if (OB.POS.modelterminal.hasPermission('OBPOS_print.cashup')) {
            me.printCashUp.print(me.get('cashUpReport').at(0), me.getCountCashSummary());
          }
          OB.MobileApp.model.runSyncProcess(null, null, null, function () {
            OB.UTIL.calculateCurrentCash();
            OB.UTIL.showLoading(false);
            me.set("finished", true);
          });
        } else {
          OB.Dal.save(cashUp.at(0), function () {
            if (OB.POS.modelterminal.hasPermission('OBPOS_print.cashup')) {
              me.printCashUp.print(me.get('cashUpReport').at(0), me.getCountCashSummary());
            }
            OB.UTIL.initCashUp(function () {
              OB.UTIL.calculateCurrentCash();
              OB.UTIL.showLoading(false);
              me.set("finished", true);
            });
          }, null);
        }
      }, null, null);
    }, null, this);
  },
  convertExpected: function () {
    _.each(this.get('paymentList').models, function (model) {
      model.set('foreignExpected', model.get('expected'));
      model.set('expected', OB.DEC.mul(model.get('expected'), model.get('rate')));
    }, this);
  }
});