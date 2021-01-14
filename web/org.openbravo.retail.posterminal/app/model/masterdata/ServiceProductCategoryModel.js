/*
 ************************************************************************************
 * Copyright (C) 2020-2021 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
(function ServiceProductDefinition() {
  class ServiceProductCategory extends OB.App.Class.MasterdataModel {
    constructor() {
      super();
      this.indices = [
        new OB.App.Class.Index({
          name: 'serviceProductRelatedCategory_idx',
          properties: [{ property: 'relatedCategory' }]
        })
      ];
    }

    getName() {
      return 'ServiceProductCategory';
    }

    isRemote() {
      return OB.App.Security.hasPermission('OBPOS_remote.product');
    }
  }
  OB.App.MasterdataController.registerModel(ServiceProductCategory);
})();
