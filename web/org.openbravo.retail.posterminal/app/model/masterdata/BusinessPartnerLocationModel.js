/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
(function BusinessPartnerLocationDefinition() {
  class BusinessPartnerLocation extends OB.App.Class.MasterdataModel {
    constructor() {
      super();
      this.indices = [
        new OB.App.Class.Index({
          name: 'bPartnerLocation_search_isBillTo',
          properties: [
            { property: 'bpartner' },
            { property: 'isBillTo', isBoolean: true }
          ]
        }),
        new OB.App.Class.Index({
          name: 'bPartnerLocation_search_isShipTo',
          properties: [
            { property: 'bpartner' },
            { property: 'isShipTo', isBoolean: true }
          ]
        }),
        new OB.App.Class.Index({
          name: 'bPartnerLocation_bpartner',
          properties: [{ property: 'bpartner' }]
        })
      ];
      this.searchProperties = ['name', 'postalCode', 'cityName', 'bpartner'];
    }

    // eslint-disable-next-line class-methods-use-this
    getName() {
      return 'BusinessPartnerLocation';
    }

    // eslint-disable-next-line class-methods-use-this
    isRemote() {
      return OB.App.Security.hasPermission('OBPOS_remote.customer');
    }
  }
  OB.App.MasterdataController.registerModel(BusinessPartnerLocation);
})();