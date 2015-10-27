/*
 ************************************************************************************
 * Copyright (C) 2015 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global Backbone */

(function () {

  var OBPOSFiles = OB.Data.ExtensibleModel.extend({
    modelName: 'OBPOSFiles',
    tableName: 'obpos_files',
    entityName: 'OBPOSFiles',
    source: 'org.openbravo.retail.posterminal.master.POSFiles',
    dataLimit: OB.Dal.DATALIMIT,
    includeTerminalDate: true,
    remote: 'OBPOS_remote.obposfiles'
  });

  OBPOSFiles.addProperties([{
    name: 'id',
    column: 'obpos_files_id',
    primaryKey: true,
    type: 'TEXT'
  }, {
    name: 'name',
    column: 'name',
    type: 'TEXT'
  }, {
    name: 'binaryData',
    column: 'binaryData',
    type: 'TEXT'
  }]);

  OBPOSFiles.addIndex([{
    name: 'obpos_files_name_idx',
    columns: [{
      name: 'name',
      sort: 'asc'
    }]
  }]);

  OB.Data.Registry.registerModel(OBPOSFiles);
}());