/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 *
 * Contributed by Qualian Technologies Pvt. Ltd.
 ************************************************************************************
 */

/*global Backbone */

(function () {
  var ChangedBPlocation = Backbone.Model.extend({
    modelName: 'ChangedBPlocation',
    tableName: 'changedbplocation',
    entityName: '',
    source: '',
    local: true,
    properties: ['id', 'json', 'c_bpartner_location_id', 'isbeingprocessed'],
    propertyMap: {
      'id': 'changedbplocation_id',
      'json': 'json',
      'c_bpartner_location_id': 'c_bpartner_location_id',
      'isbeingprocessed': 'isbeingprocessed'
    },

    createStatement: 'CREATE TABLE IF NOT EXISTS changedbplocation (changedbplocation_id TEXT PRIMARY KEY, json TEXT, c_bpartner_location_id TEXT, isbeingprocessed TEXT)',
    dropStatement: 'DROP TABLE IF EXISTS changedbplocation',
    insertStatement: 'INSERT INTO changedbplocation(changedbplocation_id, json, c_bpartner_location_id, isbeingprocessed) VALUES (?,?,?,?)'
  });

  OB.Data.Registry.registerModel(ChangedBPlocation);
}());