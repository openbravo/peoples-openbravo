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

  OB.UTIL.processCashMgmtClass = 'org.openbravo.retail.posterminal.ProcessCashMgmt';

  OB.UTIL.processCashMgmt = function (successCallback, errorCallback) {
    var customersToJson = [],
        me = this,
        criteria = {
        'isbeingprocessed': 'N'
        };
    this.proc = new OB.DS.Process(OB.UTIL.processCashMgmtClass);
    if (OB.MobileApp.model.get('connectedToERP')) {
      OB.Dal.find(OB.Model.CashManagement, criteria, function (cashmgmts) {
        if (cashmgmts.length > 0) {
          me.proc.exec({
            depsdropstosend: cashmgmts.toJSON()
          }, function (data, message) {
            if (data && data.exception) {
              // The server response is an Error! -> Orders have not been processed
              if (errorCallback) {
                errorCallback();
              }
            } else {
              cashmgmts.each(function (cashmgmt) {
                cashmgmt.set('isbeingprocessed', 'Y');
                OB.Dal.save(cashmgmt, null, function (tx, err) {
                  OB.UTIL.showError(err);
                });
              });
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