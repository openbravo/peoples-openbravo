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

  OB.UTIL.processCustomerAddrClass = 'org.openbravo.retail.posterminal.CustomerAddrLoader';

  OB.UTIL.processCustomerAddr = function (changedCustomerAddrs, successCallback, errorCallback) {
    var customerAddrToJson = [];
    changedCustomerAddrs.each(function (customerAddr) {
      customerAddrToJson.push(customerAddr.get('json'));
    });
    this.proc = new OB.DS.Process(OB.UTIL.processCustomerAddrClass);
    if (OB.MobileApp.model.get('connectedToERP')) {
      this.proc.exec({
        terminalId: OB.MobileApp.model.get('terminal').id,
        customerAddr: customerAddrToJson
      }, function (data, message) {
        if (data && data.exception) {
          // The server response is an Error! -> Orders have not been processed
          changedCustomerAddrs.each(function (changedCustomerAddr) {
            changedCustomerAddr.set('isbeingprocessed', 'N');
            changedCustomerAddr.set('json', JSON.stringify(changedCustomerAddr.get('json')));
            OB.Dal.save(changedCustomerAddr, null, function (tx, err) {
              OB.UTIL.showError(err);
            });
          });
          if (errorCallback) {
            errorCallback();
          }
        } else {
          // Customers have been processed, delete them from the queue
          OB.Dal.removeAll(OB.Model.ChangedBPlocation, null, function () {
            successCallback();
          }, function (tx, err) {
            OB.UTIL.showError(OB.I18N.getLabel('OBPOS_errorRemovingLocallyProcessedBPLoc'));
          });
        }
      }, null, null, 4000);
    }
  };
}());