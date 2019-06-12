/*
 ************************************************************************************
 * Copyright (C) 2019 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

(function() {
  var OrderToIssue = OB.Data.ExtensibleModel.extend({
    modelName: 'OrderToIssue',
    tableName: 'ordertoissue',
    entityName: 'OrderToIssue',
    local: true,
    dataLimit: OB.Dal.REMOTE_DATALIMIT,
    source: ''
  });

  OrderToIssue.addProperties([
    {
      name: 'id',
      column: 'ordertoissue_id',
      primaryKey: true,
      type: 'TEXT'
    },
    {
      name: 'json',
      column: 'json',
      type: 'TEXT'
    }
  ]);

  OB.Data.Registry.registerModel(OrderToIssue);

  OB.OBPOSPointOfSale.Model.PointOfSale.prototype.models.push(
    OB.Model.OrderToIssue
  );
})();
