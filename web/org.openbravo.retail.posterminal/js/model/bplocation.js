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

    var BPLocation = Backbone.Model.extend({
        modelName: 'BPLocation',
        tableName: 'c_bpartner_location',
        entityName: 'BPLocation',
        source: 'org.openbravo.retail.posterminal.master.BPLocation',
        dataLimit: 300,
        local: false,
        properties: ['id', 'bpartner', 'name', '_identifier', '_idx'],
        propertyMap: {
            'id': 'c_bpartner_location_id',
            'bpartner': 'c_bpartner_id',
            'name': 'name',
            '_identifier': '_identifier',
            '_idx': '_idx'
        },
        createStatement: 'CREATE TABLE IF NOT EXISTS c_bpartner_location (c_bpartner_location_id TEXT PRIMARY KEY , c_bpartner_id TEXT , name TEXT , _identifier TEXT , _idx NUMERIC)',
        dropStatement: 'DROP TABLE IF EXISTS c_bpartner_location',
        insertStatement: 'INSERT INTO c_bpartner_location (c_bpartner_location_id, c_bpartner_id, name, _identifier, _idx)  VALUES (?, ?, ?, ?, ?)'
    });

    OB.Data.Registry.registerModel(BPLocation);
}());