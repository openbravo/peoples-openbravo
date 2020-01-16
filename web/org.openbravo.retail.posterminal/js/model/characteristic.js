/*
 ************************************************************************************
 * Copyright (C) 2015-2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
(function() {
  class Characteristic extends OB.App.Class.MasterdataModel {
    constructor() {
      super();
      this.indices = [
        new OB.App.Class.Index({
          name: 'characteristicId_idx',
          properties: [{ property: 'id' }]
        })
      ];
    }
  }
  OB.App.MasterdataController.registerModel(Characteristic);

  let CharacteristicMD = OB.Data.ExtensibleModel.extend({
    modelName: 'Characteristic',
    tableName: 'm_characteristic',
    entityName: 'Characteristic',
    remote: 'OBPOS_remote.product',
    dataLimit: OB.Dal.DATALIMIT,
    remoteDataLimit: OB.Dal.REMOTE_DATALIMIT,
    source: 'org.openbravo.retail.posterminal.master.Characteristic',
    indexDBModel: Characteristic.prototype.getName()
  });

  CharacteristicMD.addProperties([
    {
      name: 'id',
      column: 'id',
      primaryKey: true,
      type: 'TEXT'
    },
    {
      name: '_identifier',
      column: '_identifier',
      type: 'TEXT'
    }
  ]);

  OB.Data.Registry.registerModel(CharacteristicMD);
})();
