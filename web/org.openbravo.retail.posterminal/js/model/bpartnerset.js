/*
 ************************************************************************************
 * Copyright (C) 2019 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

(function() {
  var BPSetLine = OB.Data.ExtensibleModel.extend({
    modelName: 'BPSetLine',
    tableName: 'c_bp_set_line',
    entityName: 'BPSetLine',
    dataLimit: OB.Dal.DATALIMIT,
    paginationById: true,
    source: 'org.openbravo.retail.posterminal.master.BPSetLine'
  });

  BPSetLine.addProperties([
    {
      name: 'id',
      column: 'c_bp_set_line_id',
      primaryKey: true,
      type: 'TEXT'
    },
    {
      name: 'bpSet',
      column: 'c_bp_set_id',
      type: 'TEXT'
    },
    {
      name: 'businessPartner',
      column: 'c_bpartner_id',
      type: 'TEXT'
    },
    {
      name: 'startingDate',
      column: 'startdate',
      type: 'DATE'
    },
    {
      name: 'endingDate',
      column: 'enddate',
      type: 'DATE'
    }
  ]);

  OB.Data.Registry.registerModel(BPSetLine);
})();
