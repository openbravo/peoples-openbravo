/*
 ************************************************************************************
 * Copyright (C) 2019 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

(function() {
  OB.Model.OBRDM_OrderToSelectorIssue = OB.Data.ExtensibleModel.extend({
    modelName: 'OrderToSelectorIssue',
    entityName: 'OrderToSelectorIssue',
    local: false,
    legacyModel: true,
    source: ''
  });

  OB.Model.OBRDM_OrderToSelectorIssue.addProperties([
    {
      name: 'id',
      column: 'c_order_id',
      primaryKey: true,
      type: 'TEXT'
    },
    {
      name: 'documentNo',
      column: 'documentNo',
      type: 'TEXT'
    },
    {
      name: 'orderedDate',
      column: 'orderedDate',
      type: 'TEXT'
    },
    {
      name: 'bpName',
      column: 'bpName',
      type: 'TEXT'
    }
  ]);
})();
