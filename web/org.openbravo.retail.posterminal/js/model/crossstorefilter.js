/*
 ************************************************************************************
 * Copyright (C) 2019 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

(function () {

  OB.Model.CrossStoreFilter = OB.Data.ExtensibleModel.extend({
    source: 'org.openbravo.retail.posterminal.master.CrossStoreFilter',
    dataLimit: OB.Dal.DATALIMIT,
    _modelName: 'CrossStoreFilter',
    forceRemoteEntity: true
  });

  OB.Model.CrossStoreFilter.addProperties([{
    name: 'id',
    column: 'id',
    primaryKey: true,
    filter: false,
    type: 'TEXT'
  }, {
    name: 'name',
    filter: false,
    type: 'TEXT'
  }, {
    name: 'stock',
    filter: false,
    type: 'NUMERIC'
  }, {
    name: 'orgName',
    column: 'orgName',
    filter: true,
    type: 'TEXT',
    caption: 'OBPOS_LblName'
  }, {
    name: 'stock',
    column: 'stock',
    caption: 'OBPOS_LblStock',
    text: 'OBPOS_LblStoresStock',
    filter: true,
    type: 'BOOL',
    isSelect: true,
    disableSorting: true
  }]);

}());