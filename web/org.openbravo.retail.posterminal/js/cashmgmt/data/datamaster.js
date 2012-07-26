/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */


OB.Model = OB.Model || {};

OB.Model.DepositsDrops = Backbone.Model.extend({
  source: 'org.openbravo.retail.posterminal.term.CashMgmtDepositsDrops',
  modelName: 'DataDepositsDrops',
  online: true
});

OB.Model.CashMgmtPaymentMethod = Backbone.Model.extend({
  source: 'org.openbravo.retail.posterminal.term.CashMgmtPayments',
  modelName: 'DataCashMgmtPaymentMethod',
  online: true
});

OB.Model.DropEvents = Backbone.Model.extend({
  source: 'org.openbravo.retail.posterminal.term.CashMgmtDropEvents',
  modelName: 'DataDropEvents',
  online: true
});

OB.Model.DepositEvents = Backbone.Model.extend({
  source: 'org.openbravo.retail.posterminal.term.CashMgmtDepositEvents',
  modelName: 'DataDepositEvents',
  online: true
});