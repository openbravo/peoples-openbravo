/*
 ************************************************************************************
 * Copyright (C) 2018-2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global OB */

(function() {
  var ProductServiceLinked = OB.Data.ExtensibleModel.extend({
    modelName: 'ProductServiceLinked',
    tableName: 'ProductServiceLinked',
    entityName: 'M_PRODUCT_SERVICELINKED',
    source: 'org.openbravo.retail.posterminal.master.ProductServiceLinked',
    dataLimit: 100,
    includeTerminalDate: true,
    paginationById: true,
    remote: 'OBPOS_remote.product',
    indexDBModel: OB.App.MasterdataModels.ProductServiceLinked.getName(),
    legacyModel: true
  });

  ProductServiceLinked.addProperties([
    {
      name: 'id',
      column: 'id',
      primaryKey: true,
      type: 'TEXT'
    },
    {
      name: 'product',
      column: 'product',
      type: 'TEXT'
    },
    {
      name: 'productCategory',
      column: 'productCategory',
      type: 'TEXT'
    },
    {
      name: 'taxCategory',
      column: 'taxCategory',
      type: 'TEXT'
    }
  ]);

  ProductServiceLinked.addIndex([
    {
      name: 'ProductServiceLinked_inx',
      columns: [
        {
          name: 'product',
          sort: 'asc'
        },
        {
          name: 'productCategory',
          sort: 'asc'
        }
      ]
    }
  ]);

  OB.Data.Registry.registerModel(ProductServiceLinked);
})();
