/*
 ************************************************************************************
 * Copyright (C) 2016-2020 Openbravo S.L.U.
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
    _modelName: 'BPartnerFilter',
    equivalentModel: 'BusinessPartner'
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
      column: 'bpartnerId',
      filter: false,
      type: 'TEXT',
      location: false,
      entityColumn: 'id',
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
      entityColumn: 'customerBlocking',
      entity: 'BusinessPartner'
    },
    {
      name: 'salesOrderBlocking',
      column: 'salesOrderBlocking',
      filter: false,
      type: 'TEXT',
      location: false,
      entityColumn: 'salesOrderBlocking',
      entity: 'BusinessPartner'
    },
    {
      name: 'bpName',
      column: 'bpName',
      serverColumn: 'bp.name',
      filter: true,
      type: 'TEXT',
      caption: 'OBPOS_LblFullName',
      location: false,
      entityColumn: 'name',
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
      entityColumn: 'searchKey',
      entity: 'BusinessPartner'
    },
    {
      name: 'bpCategory',
      column: 'bpCategory',
      serverColumn: 'bp.businessPartnerCategory.name',
      filter: true,
      type: 'TEXT',
      caption: 'OBPOS_BPCategory',
      location: false,
      entityColumn: 'businessPartnerCategory_name',
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
      entityColumn: 'taxID',
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
      entityColumn: 'postalCode',
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
      entityColumn: 'cityName',
      entity: 'BusinessPartnerLocation'
    },
    {
      name: 'locName',
      column: 'locName',
      serverColumn: 'bpl.name',
      filter: true,
      type: 'TEXT',
      caption: 'OBPOS_LblAddress',
      location: true,
      entityColumn: 'name',
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
      entityColumn: 'phone',
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
      entityColumn: 'email',
      entity: 'BusinessPartner'
    },
    {
      name: 'bpLocactionId',
      column: 'bpLocationId',
      filter: false,
      type: 'TEXT',
      location: true,
      entityColumn: 'id',
      entity: 'BusinessPartnerLocation'
    },
    {
      name: 'isBillTo',
      column: 'isBillTo',
      filter: false,
      type: 'TEXT',
      location: true,
      entityColumn: 'isBillTo',
      entity: 'BusinessPartnerLocation'
    },
    {
      name: 'isShipTo',
      column: 'isShipTo',
      filter: false,
      type: 'TEXT',
      location: true,
      entityColumn: 'isShipTo',
      entity: 'BusinessPartnerLocation'
    }
  ]);
})();
