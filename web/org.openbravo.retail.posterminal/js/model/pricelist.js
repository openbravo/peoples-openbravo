/*
 ************************************************************************************
 * Copyright (C) 2015-2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

(function() {
  var PriceList = OB.Data.ExtensibleModel.extend({
    modelName: 'PriceList',
    tableName: 'm_pricelist',
    entityName: 'PriceList',
    source: 'org.openbravo.retail.posterminal.master.PriceList',
    includeTerminalDate: true,
    paginationById: true,
    indexDBModel: OB.App.MasterdataModels.PriceList.getName(),
    legacyModel: true
  });

  PriceList.addProperties([
    {
      name: 'id',
      column: 'id',
      primaryKey: true,
      type: 'TEXT'
    },
    {
      name: 'name',
      column: 'name',
      type: 'TEXT'
    },
    {
      name: 'priceIncludesTax',
      column: 'priceIncludesTax',
      type: 'TEXT'
    },
    {
      name: 'c_currency_id',
      column: 'c_currency_id',
      type: 'TEXT'
    }
  ]);

  PriceList.addIndex([
    {
      name: 'obpos_price_list',
      columns: [
        {
          name: 'name',
          sort: 'asc'
        }
      ]
    }
  ]);

  //Register the model in the application
  OB.Data.Registry.registerModel(PriceList);
})();
