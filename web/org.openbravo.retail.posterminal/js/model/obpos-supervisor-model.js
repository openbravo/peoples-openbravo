/*
 ************************************************************************************
 * Copyright (C) 2013 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global Backbone*/

/**
 * Local model to keep the supervisor and which type of approval can do each of them
 */
OB.Data.Registry.registerModel(Backbone.Model.extend({
  modelName: 'Supervisor',
  tableName: 'supervisor',
  entityName: 'Supervisor',
  properties: ['id', 'name', 'password', 'permissions', 'created', '_identifier', '_idx'],
  propertyMap: {
    'id': 'supervisor_id',
    'name': 'name',
    'password': 'password',
    'permissions': 'permissions',
    'created': 'created',
    '_identifier': '_identifier',
    '_idx': '_idx'
  },
  createStatement: 'CREATE TABLE IF NOT EXISTS supervisor (supervisor_id TEXT PRIMARY KEY , name TEXT , password TEXT , permissions TEXT, created TEXT, _identifier TEXT , _idx NUMERIC)',
  dropStatement: 'DROP TABLE IF EXISTS supervisor',
  insertStatement: 'INSERT INTO supervisor(supervisor_id, name, password, permissions, created, _identifier, _idx)  VALUES (?, ?, ?, ?, ?, ?, ?)',
  updateStatement: '',
  local: true
}));