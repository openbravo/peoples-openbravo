/*
 ************************************************************************************
 * Copyright (C) 2016-2019 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

(function() {
  OB.Model.BPartnerFilter = OB.Data.ExtensibleModel.extend({
    remote: 'OBPOS_remote.customer',
    source: 'org.openbravo.retail.posterminal.master.BPartnerFilter',
    dataLimit: OB.Dal.DATALIMIT,
    _modelName: 'BPartnerFilter'
  });

  OB.Model.BPartnerFilter.addProperties([
    {
      name: 'id',
      column: 'id',
      primaryKey: true,
      filter: false,
      type: 'TEXT',
      location: false
    },
    {
      name: 'bpartnerId',
      column: 'id',
      filter: false,
      type: 'TEXT',
      location: false,
      entity: 'BusinessPartner'
    },
    {
      name: '_identifier',
      column: '_identifier',
      filter: false,
      type: 'TEXT',
      location: false
    },
    {
      name: 'customerBlocking',
      column: 'customerBlocking',
      filter: false,
      type: 'TEXT',
      location: false,
      entity: 'BusinessPartner'
    },
    {
      name: 'salesOrderBlocking',
      column: 'salesOrderBlocking',
      filter: false,
      type: 'TEXT',
      location: false,
      entity: 'BusinessPartner'
    },
    {
      name: 'bpName',
      column: 'name',
      serverColumn: 'bp.name',
      filter: true,
      type: 'TEXT',
      caption: 'OBPOS_LblFullName',
      location: false,
      entity: 'BusinessPartner'
    },
    {
      name: 'searchKey',
      column: 'searchKey',
      serverColumn: 'bp.searchKey',
      filter: true,
      type: 'TEXT',
      caption: 'OBPOS_LblSearchKey',
      location: false,
      entity: 'BusinessPartner'
    },
    {
      name: 'bpCategory',
      column: 'businessPartnerCategory_name',
      serverColumn: 'bp.businessPartnerCategory.name',
      filter: true,
      type: 'TEXT',
      caption: 'OBPOS_BPCategory',
      location: false,
      entity: 'BusinessPartner'
    },
    {
      name: 'taxID',
      column: 'taxID',
      serverColumn: 'bp.taxID',
      filter: true,
      type: 'TEXT',
      caption: 'OBPOS_LblTaxId',
      location: false,
      entity: 'BusinessPartner'
    },
    {
      name: 'postalCode',
      column: 'postalCode',
      serverColumn: 'bpl.locationAddress.postalCode',
      filter: true,
      type: 'TEXT',
      caption: 'OBPOS_LblPostalCode',
      location: true,
      entity: 'BusinessPartnerLocation'
    },
    {
      name: 'cityName',
      column: 'cityName',
      serverColumn: 'bpl.locationAddress.cityName',
      filter: true,
      type: 'TEXT',
      caption: 'OBPOS_LblCity',
      location: true,
      entity: 'BusinessPartnerLocation'
    },
    {
      name: 'locName',
      column: 'name',
      serverColumn: 'bpl.name',
      filter: true,
      type: 'TEXT',
      caption: 'OBPOS_LblAddress',
      location: true,
      entity: 'BusinessPartnerLocation'
    },
    {
      name: 'phone',
      column: 'phone',
      serverColumn: 'ulist.phone',
      filter: true,
      type: 'TEXT',
      caption: 'OBPOS_LblPhone',
      location: false,
      entity: 'BusinessPartner'
    },
    {
      name: 'email',
      column: 'email',
      serverColumn: 'ulist.email',
      filter: true,
      type: 'TEXT',
      caption: 'OBPOS_LblEmail',
      location: false,
      entity: 'BusinessPartner'
    },
    {
      name: 'bpLocactionId',
      column: 'id',
      filter: false,
      type: 'TEXT',
      location: true,
      entity: 'BusinessPartnerLocation'
    },
    {
      name: 'isBillTo',
      column: 'isBillTo',
      filter: false,
      type: 'TEXT',
      location: true,
      entity: 'BusinessPartnerLocation'
    },
    {
      name: 'isShipTo',
      column: 'isShipTo',
      filter: false,
      type: 'TEXT',
      location: true,
      entity: 'BusinessPartnerLocation'
    }
  ]);
})();
