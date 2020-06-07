/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global OB */

// Returns true if extbpEnabled is enabled
OB.UTIL.externalBp = function() {
  let isexternalBp = false;
  if (OB.MobileApp.model.get('externalBpIntegration')) {
    isexternalBp = true;
  }
  return isexternalBp;
};

// Returns the popup name of the modalcustomer
OB.UTIL.modalCustomer = function() {
  let modalCustomer;
  if (OB.UTIL.externalBp()) {
    modalCustomer = 'modalExternalBusinessPartner';
  } else {
    modalCustomer = 'modalcustomer';
  }
  return modalCustomer;
};

OB.UTIL.filterColumn = function(fullFlt) {
  if (fullFlt.column === 'businessPartner' && OB.UTIL.externalBp()) {
    return 'externalBusinessPartnerReference';
  } else {
    return fullFlt.name;
  }
};

OB.Model.ExternalBpIntegration = OB.Data.ExtensibleModel.extend({});
