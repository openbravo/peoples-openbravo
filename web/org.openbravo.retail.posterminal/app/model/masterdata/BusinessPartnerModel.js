/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
(function BusinessPartnerDefinition() {
  class BusinessPartner extends OB.App.Class.MasterdataModel {
    constructor() {
      super();
      this.indices = [];
      this.searchProperties = [
        'name',
        'searchKey',
        'businessPartnerCategory_name',
        'taxID',
        'phone',
        'email',
        'id'
      ];
    }
  }
  OB.App.MasterdataController.registerModel(BusinessPartner);
})();