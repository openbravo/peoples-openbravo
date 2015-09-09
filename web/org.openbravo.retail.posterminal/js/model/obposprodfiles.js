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

  var OBPOSProdFiles = OB.Data.ExtensibleModel.extend({
    modelName: 'OBPOSProdFiles',
    tableName: 'obpos_prod_files',
    entityName: 'OBPOSProdFiles',
    source: 'org.openbravo.retail.posterminal.master.POSProdFiles',
    dataLimit: OB.Dal.DATALIMIT,
    includeTerminalDate: true,
    remote: 'OBPOS_remote.obposfiles'
  });

  OBPOSProdFiles.addProperties([{
    name: 'id',
    column: 'obpos_prod_files_id',
    primaryKey: true,
    type: 'TEXT'
  }, {
    name: 'product',
    column: 'm_product_id',
    type: 'TEXT'
  }, {
    name: 'posfile',
    column: 'obpos_file_id',
    type: 'TEXT'
  }, {
    name: 'printer',
    column: 'printer',
    type: 'NUMERIC'
  }]);

  OBPOSProdFiles.addIndex([{
    name: 'obpos_prod_files_pos_file_idx',
    columns: [{
      name: 'obpos_file_id',
      sort: 'desc'
    }]
  }, {
    name: 'obpos_prod_files_prod_id_idx',
    columns: [{
      name: 'm_product_id',
      sort: 'desc'
    }]
  }]);

  OB.Data.Registry.registerModel(OBPOSProdFiles);
}());
