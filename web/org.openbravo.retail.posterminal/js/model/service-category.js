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

  var ServiceProductCategory = OB.Data.ExtensibleModel.extend({
    modelName: 'ServiceProductCategory',
    tableName: 'm_product_category_service',
    entityName: 'ServiceProductCategory',
    source: 'org.openbravo.retail.posterminal.master.ServiceProductCategory',
    dataLimit: 100,
    includeTerminalDate: true,
    remote: 'OBPOS_remote.product'
  });

  ServiceProductCategory.addProperties([{
    name: 'id',
    column: 'm_product_category_service_id',
    primaryKey: true,
    type: 'TEXT'
  }, {
    name: 'service',
    column: 'm_product_id',
    type: 'TEXT'
  }, {
    name: 'relatedCategory',
    column: 'm_product_category_id',
    type: 'TEXT'
  }]);

  ServiceProductCategory.addIndex([{
    name: 'obpos_servicecat_service',
    columns: [{
      name: 'm_product_id',
      sort: 'asc'
    }]
  }, {
    name: 'obpos_servicecat_category',
    columns: [{
      name: 'm_product_category_id',
      sort: 'asc'
    }]
  }]);

  OB.Data.Registry.registerModel(ServiceProductCategory);
}());