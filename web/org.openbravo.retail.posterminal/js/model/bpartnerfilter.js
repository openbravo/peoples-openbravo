/*
 ************************************************************************************
 * Copyright (C) 2016 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global Backbone, _ */

(function () {

  OB.Model.BPartnerFilter = OB.Data.ExtensibleModel.extend({
    modelName: 'BPartnerFilter',
    entityName: 'BPartnerFilter',
    remote: 'OBPOS_remote.customer',
    source: 'org.openbravo.retail.posterminal.master.BPartnerFilter',
    dataLimit: OB.Dal.DATALIMIT
  });

  OB.Model.BPartnerFilter.addProperties([{
    name: 'id',
    column: 'id',
    primaryKey: true,
    filter: false,
    type: 'TEXT',
    location: false
  }, {
    name: 'bpartnerId',
    column: 'bp.c_bpartner_id',
    filter: false,
    type: 'TEXT',
    location: false
  }, {
    name: '_identifier',
    column: '_identifier',
    filter: false,
    type: 'TEXT',
    location: false
  }, {
    name: 'customerBlocking',
    column: 'bp.customerBlocking',
    filter: false,
    type: 'TEXT',
    location: false
  }, {
    name: 'salesOrderBlocking',
    column: 'bp.salesOrderBlocking',
    filter: false,
    type: 'TEXT',
    location: false
  }, {
    name: 'bpName',
    column: 'bp.name',
    filter: true,
    type: 'TEXT',
    caption: 'OBPOS_LblName',
    location: false
  }, {
    name: 'bpCategory',
    column: 'bp.c_bp_group_name',
    filter: true,
    type: 'TEXT',
    caption: 'OBPOS_BPCategory',
    location: false
  }, {
    name: 'taxID',
    column: 'bp.taxID',
    filter: true,
    type: 'TEXT',
    caption: 'OBPOS_LblTaxId',
    location: false
  }, {
    name: 'phone',
    column: 'bp.phone',
    filter: true,
    type: 'TEXT',
    caption: 'OBPOS_LblPhone',
    location: false
  }, {
    name: 'email',
    column: 'bp.email',
    filter: true,
    type: 'TEXT',
    caption: 'OBPOS_LblEmail',
    location: false
  }, {
    name: 'bpLocactionId',
    column: 'loc.c_bpartner_location_id',
    filter: false,
    type: 'TEXT',
    location: true
  }, {
    name: 'locName',
    column: 'loc.name',
    filter: true,
    type: 'TEXT',
    caption: 'OBPOS_LblAddress',
    location: true
  }, {
    name: 'postalCode',
    column: 'loc.postalCode',
    filter: true,
    type: 'TEXT',
    caption: 'OBPOS_LblPostalCode',
    location: true
  }, {
    name: 'cityName',
    column: 'loc.cityName',
    filter: true,
    type: 'TEXT',
    caption: 'OBPOS_LblCity',
    location: true
  }]);

}());