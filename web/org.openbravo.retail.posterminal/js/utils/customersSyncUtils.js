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

  OB.UTIL.processCustomerClass = 'org.openbravo.retail.posterminal.CustomerLoader';

  OB.UTIL.processCustomers = function (changedCustomers, successCallback, errorCallback) {
    var customersToJson = [];
    changedCustomers.each(function (customer) {
      customersToJson.push(customer.get('json'));
    });
    this.proc = new OB.DS.Process(OB.UTIL.processCustomerClass);
    if (OB.MobileApp.model.get('connectedToERP')) {
      this.proc.exec({
        customer: customersToJson
      }, function (data, message) {
        if (data && data.exception) {
          // The server response is an Error! -> Orders have not been processed
          changedCustomers.each(function (changedCustomer) {
            changedCustomer.set('isbeingprocessed', 'N');
            changedCustomer.set('json', JSON.stringify(changedCustomer.get('json')));
            OB.Dal.save(changedCustomer, null, function (tx, err) {
              OB.UTIL.showError(err);
            });
          });
          if (errorCallback) {
            errorCallback();
          }
        } else {
          // Customers have been processed, delete them from the queue
          OB.Dal.removeAll(OB.Model.ChangedBusinessPartners, null, function () {
            successCallback();
          }, function (tx, err) {
            OB.UTIL.showError(OB.I18N.getLabel('OBPOS_errorRemovingLocallyProcessedCustomer'));
          });
        }
      });
    }
  };
}());