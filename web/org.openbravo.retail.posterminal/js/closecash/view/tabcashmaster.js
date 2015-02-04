/*
 ************************************************************************************
 * Copyright (C) 2015 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global enyo, $ */

enyo.kind({
  name: 'OB.OBPOSCashUp.UI.RenderCashMasterLine',
  components: [{
    classes: 'row-fluid',
    components: [{
      classes: 'span12',
      style: 'border-bottom: 1px solid #cccccc;',
      components: [{
        style: 'float: left; display:table; width: 100%; ',
        components: [{
          style: 'padding: 10px 10px 10px 10px; display: table-cell; width: 70%;',
          name: 'name',
        }, {
          style: 'padding: 10px 10px 10px 0px; display: table-cell; width: 30%; ',
          name: 'cashUp',
        }]
      }]
    }]
  }],
  create: function () {
    this.inherited(arguments);
    this.$.name.setContent(this.model.get('name'));
    this.$.cashUp.setContent(this.model.get('finish') ? OB.I18N.getLabel('OBMOBC_LblYes') : OB.I18N.getLabel('OBMOBC_LblNo'));
  }
});

enyo.kind({
  name: 'OB.OBPOSCashUp.UI.CashMaster',
  published: {
    paymentToKeep: null
  },
  components: [{
    classes: 'tab-pane',
    components: [{
      style: 'overflow:auto; height: 500px; margin: 5px',
      components: [{
        style: 'background-color: #ffffff; color: black; padding: 5px;',
        components: [{
          classes: 'row-fluid',
          components: [{
            classes: 'span12',
            components: [{
              name: 'stepsheader',
              style: 'padding: 10px; border-bottom: 1px solid #cccccc; text-align:center;',
              renderHeader: function (step, count) {
                this.setContent(OB.I18N.getLabel('OBPOS_LblStepNumber', [step, count]) + " " + OB.I18N.getLabel('OBPOS_LblStepMaster') + OB.OBPOSCashUp.UI.CashUp.getTitleExtensions());
              }
            }]
          }]
        }, {
          style: 'background-color: #ffffff; color: black;',
          components: [{
            components: [{
              classes: 'row-fluid',
              components: [{
                classes: 'span12',
                style: 'border-bottom: 1px solid #cccccc;',
                components: [{
                  style: 'float: left; display:table; width: 100%; ',
                  components: [{
                    style: 'padding: 10px 10px 10px 10px; display: table-cell; width: 70%;',
                    initComponents: function () {
                      this.setContent(OB.I18N.getLabel('OBPOS_LblTerminal'));
                    }
                  }, {
                    style: 'padding: 10px 10px 10px 0px; display: table-cell; width: 30%;',
                    initComponents: function () {
                      this.setContent(OB.I18N.getLabel('OBPOS_LblCashupSlaveClosed'));
                    }
                  }]
                }]
              }]
            }, {
              name: 'slaveList',
              kind: 'OB.UI.Table',
              renderLine: 'OB.OBPOSCashUp.UI.RenderCashMasterLine',
              renderEmpty: 'OB.UI.RenderEmpty',
              listStyle: 'list'
            }]
          }]
        }]
      }]
    }]
  }],

  displayStep: function (model) {
    // this function is invoked when displayed.   
    var me = this;
    this.$.stepsheader.renderHeader(model.stepNumber('OB.CashUp.Master'), model.stepCount());
    if (!model.get('slavesCashupCompleted')) {
      new OB.DS.Process('org.openbravo.retail.posterminal.ProcessCashCloseMaster').exec({
        masterterminal: OB.POS.modelterminal.get('terminal').id,
        cashUpId: OB.POS.modelterminal.get('terminal').cashUpId
      }, function (data) {
        if (data && data.exception) {
          // Error handler 
          OB.log('error', data.exception.message);
          OB.UTIL.showAlert.display(data.exception.message, OB.I18N.getLabel('OBMOBC_LblError'), 'alert-error', false);
        } else {
          var col = new Backbone.Collection();
          col.add(data.terminals);
          me.$.slaveList.setCollection(col);
          if (data.finishAll) {
            me.updateCashReport(model, data.payments);
          }
          model.set('slavesCashupCompleted', data.finishAll);
        }
      });
    }
  },

  updateCashReport: function (model, payments) {
    _.each(model.get('paymentList').models, function (item) {
      _.each(payments, function (payment) {
        if (item.get('searchKey') === payment.searchKey) {
          //item.set('startingCash', OB.DEC.add(item.get('startingCash'), payment.startingCash));
          item.set('totalDeposits', OB.DEC.add(item.get('totalDeposits'), payment.totalDeposits));
          item.set('totalDrops', OB.DEC.add(item.get('totalDrops'), payment.totalDrops));
          item.set('totalReturns', OB.DEC.add(item.get('totalReturns'), payment.totalReturns));
          item.set('totalSales', OB.DEC.add(item.get('totalSales'), payment.totalSales));
          var cTotalDeposits = OB.DEC.sub(item.get('totalDeposits'), OB.DEC.abs(item.get('totalDrops')));
          expected = OB.DEC.add(OB.DEC.add(item.get('startingCash'), OB.DEC.sub(item.get('totalSales'), OB.DEC.abs(item.get('totalReturns')))), cTotalDeposits);
          var fromCurrencyId = item.get('paymentMethod').currency;
          item.set('expected', OB.UTIL.currency.toDefaultCurrency(fromCurrencyId, expected));
          item.set('foreignExpected', expected);
        }
      });
    });
  }

});