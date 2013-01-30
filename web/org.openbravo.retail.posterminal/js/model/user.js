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

  var User = Backbone.Model.extend({
    modelName: 'User',
    tableName: 'ad_user',
    entityName: 'User',
    source: 'org.openbravo.retail.posterminal.User',
    properties: ['id', 'name', 'password', 'terminalinfo', 'formatInfo', 'created', '_identifier', '_idx'],
    propertyMap: {
      'id': 'ad_user_id',
      'name': 'name',
      'password': 'password',
      'terminalinfo': 'terminalinfo',
      'formatInfo': 'formatInfo',
      'created': 'created',
      '_identifier': '_identifier',
      '_idx': '_idx'
    },
    createStatement: 'CREATE TABLE IF NOT EXISTS ad_user (ad_user_id TEXT PRIMARY KEY , name TEXT , password TEXT , terminalinfo TEXT, formatInfo TEXT, created TEXT, _identifier TEXT , _idx NUMERIC)',
    dropStatement: 'DROP TABLE IF EXISTS ad_user',
    insertStatement: 'INSERT INTO ad_user(ad_user_id, name, password, terminalinfo, formatInfo, created, _identifier, _idx)  VALUES (?, ?, ?, ?, ?, ?, ?, ?)',
    updateStatement: '',
    local: true
  });

  OB.Data.Registry.registerModel(User);
}());