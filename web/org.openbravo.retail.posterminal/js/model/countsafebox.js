/*
 ************************************************************************************
 * Copyright (C) 2013 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global OB */

(function() {
  var CountSafeBox = OB.Data.ExtensibleModel.extend({
    modelName: 'CountSafeBox',
    tableName: 'countSafeBox',
    entityName: 'CountSafeBox',
    source: '',
    local: true
  });

  CountSafeBox.addProperties([
    {
      name: 'id',
      column: 'countSafeBox_id',
      primaryKey: true,
      type: 'TEXT'
    },
    {
      name: 'creationDate',
      column: 'creationDate',
      type: 'TEXT'
    },
    {
      name: 'userId',
      column: 'userId',
      type: 'TEXT'
    },
    {
      name: 'objToSend',
      column: 'objToSend',
      type: 'TEXT'
    },
    {
      name: 'isbeingprocessed',
      column: 'isbeingprocessed',
      type: 'TEXT'
    },
    {
      name: 'isprocessed',
      column: 'isprocessed',
      type: 'TEXT'
    },
    {
      name: 'safebox',
      column: 'safebox',
      type: 'TEXT'
    }
  ]);

  OB.Data.Registry.registerModel(CountSafeBox);
})();
