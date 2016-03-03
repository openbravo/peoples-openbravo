/*
 ************************************************************************************
 * Copyright (C) 2015 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global Backbone, _ */

(function () {

  var CharacteristicValue = OB.Data.ExtensibleModel.extend({
    modelName: 'CharacteristicValue',
    tableName: 'm_ch_value',
    entityName: 'CharacteristicValue',
    remote: 'OBPOS_remote.product',
    dataLimit: OB.Dal.DATALIMIT,
    source: 'org.openbravo.retail.posterminal.master.CharacteristicValue'
  });

  CharacteristicValue.addProperties([{
    name: 'id',
    column: 'id',
    primaryKey: true,
    type: 'TEXT'
  }, {
    name: 'name',
    column: 'name',
    type: 'TEXT'
  }, {
    name: 'characteristic_id',
    column: 'characteristic_id',
    type: 'TEXT'
  }, {
    name: 'parent',
    column: 'parent',
    type: 'TEXT'
  }, {
    name: '_identifier',
    column: '_identifier',
    type: 'TEXT'
  }]);

  OB.Data.Registry.registerModel(CharacteristicValue);
}());