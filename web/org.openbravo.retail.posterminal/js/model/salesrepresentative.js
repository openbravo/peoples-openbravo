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

  var SalesRepresentative = OB.Data.ExtensibleModel.extend({
    modelName: 'SalesRepresentative',
    tableName: 'ad_sales_representative',
    entityName: 'SalesRepresentative',
    source: 'org.openbravo.retail.posterminal.master.SalesRepresentative',
    dataLimit: 300
  });

  SalesRepresentative.addProperties([{
    name: 'id',
    column: 'ad_sales_representative_id',
    primaryKey: true,
    type: 'TEXT'
  }, {
    name: 'name',
    column: 'name',
    type: 'TEXT'
  }, {
    name: 'username',
    column: 'username',
    type: 'TEXT'
  }, {
    name: '_identifier',
    column: '_identifier',
    type: 'TEXT'
  }]);

  OB.Data.Registry.registerModel(SalesRepresentative);
}());