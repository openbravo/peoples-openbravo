/*
 ************************************************************************************
 * Copyright (C) 2016 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global Backbone, _ */

(function () {

  var Country = OB.Data.ExtensibleModel.extend({
    modelName: 'Country',
    tableName: 'c_country',
    entityName: 'Country',
    source: 'org.openbravo.retail.posterminal.master.Country'
  });

  Country.addProperties([{
    name: 'id',
    column: 'c_country_id',
    primaryKey: true,
    type: 'TEXT'
  }, {
    name: '_identifier',
    column: '_identifier',
    type: 'TEXT'
  }]);

  OB.Data.Registry.registerModel(Country);
}());