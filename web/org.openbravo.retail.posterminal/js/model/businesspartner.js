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
     'locId',
     'locName',
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
     'locId': 'c_bpartnerlocation_id',
     'locName': 'c_bpartnerlocation_name',
     '_identifier': '_identifier',
     '_idx': '_idx'
    },
    createStatement: 'CREATE TABLE IF NOT EXISTS c_bpartner (c_bpartner_id TEXT PRIMARY KEY , value TEXT , name TEXT , description TEXT , taxID TEXT , so_bp_taxcategory_id TEXT, c_bpartnerlocation_id TEXT , c_bpartnerlocation_name TEXT , _identifier TEXT , _idx NUMERIC)',
    dropStatement: 'DROP TABLE IF EXISTS c_bpartner',
    insertStatement: 'INSERT INTO c_bpartner(c_bpartner_id, value, name, description, taxID, so_bp_taxcategory_id, c_bpartnerlocation_id, c_bpartnerlocation_name, _identifier, _idx)  VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)',
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