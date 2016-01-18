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
    remote: 'OBPOS_remote.customer',
    source: 'org.openbravo.retail.posterminal.master.BPartnerFilter'
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
    column: 'c_bpartner_id',
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
    column: 'customerBlocking',
    filter: false,
    type: 'TEXT',
    location: false
  }, {
    name: 'salesOrderBlocking',
    column: 'salesOrderBlocking',
    filter: false,
    type: 'TEXT',
    location: false
  }, {
    name: 'bpName',
    column: 'name',
    filter: true,
    type: 'TEXT',
    caption: 'OBPOS_LblName',
    location: false
  }, {
    name: 'bpCategory',
    column: 'c_bp_group_name',
    filter: true,
    type: 'TEXT',
    caption: 'OBPOS_BPCategory',
    location: false
  }, {
    name: 'taxID',
    column: 'taxID',
    filter: true,
    type: 'TEXT',
    caption: 'OBPOS_LblTaxId',
    location: false
  }, {
    name: 'phone',
    column: 'phone',
    filter: true,
    type: 'TEXT',
    caption: 'OBPOS_LblPhone',
    location: false
  }, {
    name: 'email',
    column: 'email',
    filter: true,
    type: 'TEXT',
    caption: 'OBPOS_LblEmail',
    location: false
  }, {
    name: 'bpLocactionId',
    column: 'c_bpartner_location_id',
    filter: false,
    type: 'TEXT',
    location: true
  }, {
    name: 'locName',
    column: 'locName',
    filter: true,
    type: 'TEXT',
    caption: 'OBPOS_LblAddress',
    location: true
  }, {
    name: 'postalCode',
    column: 'postalCode',
    filter: true,
    type: 'TEXT',
    caption: 'OBPOS_LblPostalCode',
    location: true
  }, {
    name: 'cityName',
    column: 'cityName',
    filter: true,
    type: 'TEXT',
    caption: 'OBPOS_LblCity',
    location: true
  }]);

}());