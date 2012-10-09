/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

//jslint

/*global Backbone */


(function () {

  var BusinessPartner = Backbone.Model.extend({
    modelName: 'BusinessPartner',
    tableName: 'c_bpartner',
    entityName: 'BusinessPartner',
    source: 'org.openbravo.retail.posterminal.master.BusinessPartner',
    properties: [
     'id',
     'searchKey',
     'name',
     'description',
     'taxId',
     'taxCategory',
     'paymentMethod',
     'paymentTerms',
     'invoiceTerms',
     'locId',
     'locName',
     'email',
     '_identifier',
     '_idx'
    ],
    propertyMap: {
     'id': 'c_bpartner_id',
     'searchKey': 'value',
     'name': 'name',
     'description': 'description',
     'taxId': 'taxID',
     'taxCategory': 'so_bp_taxcategory_id',
     'paymentMethod': 'FIN_Paymentmethod_ID',
     'paymentTerms': 'c_paymentterm_id',
     'invoiceTerms': 'invoicerule',
     'locId': 'c_bpartnerlocation_id',
     'locName': 'c_bpartnerlocation_name',
     '_identifier': '_identifier',
     'email': 'email',
     '_idx': '_idx'
    },
    createStatement: 'CREATE TABLE IF NOT EXISTS c_bpartner (c_bpartner_id TEXT PRIMARY KEY , value TEXT , name TEXT , description TEXT , taxID TEXT , so_bp_taxcategory_id TEXT, FIN_Paymentmethod_ID TEXT, c_paymentterm_id TEXT, invoicerule TEXT, c_bpartnerlocation_id TEXT , c_bpartnerlocation_name TEXT , _identifier TEXT , email TEXT, _idx NUMERIC)',
    dropStatement: 'DROP TABLE IF EXISTS c_bpartner',
    insertStatement: 'INSERT INTO c_bpartner(c_bpartner_id, value, name, description, taxID, so_bp_taxcategory_id, FIN_Paymentmethod_ID, c_paymentterm_id, invoicerule, c_bpartnerlocation_id, c_bpartnerlocation_name, _identifier, email, _idx)  VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)',
    updateStatement: ''
  });

  var BusinessPartnerList = Backbone.Collection.extend({
    model: BusinessPartner
  });

  window.OB = window.OB || {};
  window.OB.Model = window.OB.Model || {};
  window.OB.Collection = window.OB.Collection || {};

  window.OB.Model.BusinessPartner = BusinessPartner;
  window.OB.Collection.BusinessPartnerList = BusinessPartnerList;
}());