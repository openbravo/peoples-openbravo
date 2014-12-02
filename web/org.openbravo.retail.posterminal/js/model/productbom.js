/*
 ************************************************************************************
 * Copyright (C) 2014 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global B, $, _, Backbone */
(function () {

  var ProductBOM = OB.Data.ExtensibleModel.extend({
    modelName: 'ProductBOM',
    tableName: 'm_product_bom',
    entityName: 'ProductBOM',
    source: 'org.openbravo.retail.posterminal.master.ProductBOM'
  });

  ProductBOM.addProperties([{
    name: 'id',
    column: 'id',
    primaryKey: true,
    type: 'TEXT'
  }, {
    name: 'product',
    column: 'product',
    type: 'TEXT'
  }, {
    name: 'bomproduct',
    column: 'bomproduct',
    type: 'TEXT'
  }, {
    name: 'bomquantity',
    column: 'bomquantity',
    type: 'NUMERIC'
  }, {
    name: 'bomprice',
    column: 'bomprice',
    type: 'NUMERIC'
  }]);

  ProductBOM.addIndex([{
    name: 'obpos_productbom',
    columns: [{
      name: 'product',
      sort: 'asc'
    }]
  }]);

  OB.Data.Registry.registerModel(ProductBOM);
}());