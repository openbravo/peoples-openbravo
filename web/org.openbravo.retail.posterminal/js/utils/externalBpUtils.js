/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

// Returns true if extbpEnabled is enabled
OB.UTIL.externalBp = function() {
  return OB.MobileApp.model.get('externalBpIntegration') ? true : false;
};

// Returns the popup name of the modalcustomer
OB.UTIL.modalCustomer = function() {
  return OB.UTIL.externalBp()
    ? 'modalExternalBusinessPartner'
    : 'modalcustomer';
};

OB.UTIL.filterColumn = function(fullFlt) {
  if (OB.UTIL.externalBp() && fullFlt.column === 'businessPartner') {
    return 'externalBusinessPartnerReference';
  }
  return fullFlt.name;
};

OB.Model.ExternalBpIntegration = OB.Data.ExtensibleModel.extend({});
