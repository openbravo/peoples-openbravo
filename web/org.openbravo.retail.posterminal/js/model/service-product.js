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

  var ServiceProduct = OB.Data.ExtensibleModel.extend({
    modelName: 'ServiceProduct',
    tableName: 'm_product_service',
    entityName: 'ServiceProduct',
    source: 'org.openbravo.retail.posterminal.master.ServiceProduct',
    dataLimit: 100,
    includeTerminalDate: true,
    remote: 'OBPOS_remote.product'
  });

  ServiceProduct.addProperties([{
    name: 'id',
    column: 'm_product_service_id',
    primaryKey: true,
    type: 'TEXT'
  }, {
    name: 'service',
    column: 'm_product_id',
    type: 'TEXT'
  }, {
    name: 'relatedProduct',
    column: 'm_related_product_id',
    type: 'TEXT'
  }]);

  ServiceProduct.addIndex([{
    name: 'obpos_serviceprod_service',
    columns: [{
      name: 'm_product_id',
      sort: 'asc'
    }]
  }, {
    name: 'obpos_serviceprod_product',
    columns: [{
      name: 'm_related_product_id',
      sort: 'asc'
    }]
  }]);

  OB.Data.Registry.registerModel(ServiceProduct);
}());
