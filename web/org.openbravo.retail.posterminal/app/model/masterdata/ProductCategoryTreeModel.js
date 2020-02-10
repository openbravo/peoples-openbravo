/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
(function ProductCategoryTreeDefinition() {
  class ProductCategoryTree extends OB.App.Class.MasterdataModel {
    constructor() {
      super();
      this.indices = [
        new OB.App.Class.Index({
          name: 'productcategorytree_parentId_idx',
          properties: [{ property: 'parentId' }]
        }),
        new OB.App.Class.Index({
          name: 'productcategorytree_crossstore_idx',
          properties: [{ property: 'crossStore', isBoolean: true }]
        })
      ];
    }
  }
  OB.App.MasterdataController.registerModel(ProductCategoryTree);
})();
