/*
 ************************************************************************************
 * Copyright (C) 2013-2018 Openbravo S.L.U.
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
    remote: 'OBPOS_remote.product',
    source: 'org.openbravo.retail.posterminal.master.Brand',
    dataLimit: OB.Dal.DATALIMIT
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

  Brand.addIndex([{
    name: 'obpos_in_brandproduct',
    columns: [{
      name: 'm_product_id',
      sort: 'asc'
    }]
  }]);

  OB.Data.Registry.registerModel(Brand);
}());