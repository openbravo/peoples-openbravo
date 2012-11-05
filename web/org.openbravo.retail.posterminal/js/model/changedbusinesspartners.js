/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global Backbone */

(function () {
  var ChangedBusinessPartners = Backbone.Model.extend({
    modelName: 'ChangedBusinessPartners',
    tableName: 'changedbusinesspartners',
    entityName: '',
    source: '',
    local: true,
    properties: ['id', 'json', 'c_bpartner_id', 'isbeingprocessed'],
    propertyMap: {
      'id': 'changedbusinesspartners_id',
      'json': 'json',
      'c_bpartner_id': 'c_bpartner_id',
      'isbeingprocessed': 'isbeingprocessed'
    },

    createStatement: 'CREATE TABLE IF NOT EXISTS changedbusinesspartners (changedbusinesspartners_id TEXT PRIMARY KEY, json TEXT, c_bpartner_id TEXT, isbeingprocessed TEXT)',
    dropStatement: 'DROP TABLE IF EXISTS changedbusinesspartners',
    insertStatement: 'INSERT INTO changedbusinesspartners(changedbusinesspartners_id, json, c_bpartner_id, isbeingprocessed) VALUES (?,?,?,?)'
  });

  var ChangedBusinessPartnersList = Backbone.Collection.extend({
    model: ChangedBusinessPartners
  });

  window.OB = window.OB || {};
  window.OB.Model = window.OB.Model || {};
  window.OB.Collection = window.OB.Collection || {};

  window.OB.Model.ChangedBusinessPartners = ChangedBusinessPartners;
  window.OB.Collection.ChangedBusinessPartnersList = ChangedBusinessPartnersList;
}());