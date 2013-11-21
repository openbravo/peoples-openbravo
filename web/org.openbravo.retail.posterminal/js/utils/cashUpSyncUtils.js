/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global B,_*/

(function () {

  OB = window.OB || {};
  OB.UTILS = window.OB.UTILS || {};

  OB.UTIL.processCashUpClass = 'org.openbravo.retail.posterminal.ProcessCashClose';

  OB.UTIL.processCashUp = function (successCallback, errorCallback) {
    var me = this,
        cashupsToSend = [];
    this.proc = new OB.DS.Process(OB.UTIL.processCashUpClass);
    if (OB.MobileApp.model.get('connectedToERP')) {
      OB.Dal.find(OB.Model.CashUp, null, function (cashups) {
        if (cashups.length > 0) {
          _.each(cashups.models, function (cashup) {
            cashupsToSend.push(JSON.parse(cashup.get('objToSend')));
          }, this);
          me.proc.exec({
            cashups: cashupsToSend
          }, function (data, message) {
            if (data && data.exception) {
              // The server response is an Error! -> Orders have not been processed
              if (errorCallback) {
                errorCallback();
              }
            } else {
              if (successCallback) {
                successCallback();
              }
            }
          }, null, null, 4000);
        }
      }, null, this);
    }
  };
}());