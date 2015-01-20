/*
 ************************************************************************************
 * Copyright (C) 2013 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global Backbone, _ */

(function () {

  var Brand = OB.Data.ExtensibleModel.extend({
    modelName: 'Brand',
    tableName: 'm_brand',
    entityName: 'Brand',
    source: 'org.openbravo.retail.posterminal.master.Brand',
    dataLimit: 300
  });

  Brand.addProperties([{
    name: 'id',
    column: 'm_product_id',
    primaryKey: true,
    type: 'TEXT'
  }, {
    name: 'name',
    column: 'name',
    type: 'TEXT'
  }, {
    name: '_identifier',
    column: '_identifier',
    filter: true,
    type: 'TEXT'
  }]);

  OB.Data.Registry.registerModel(Brand);
}());