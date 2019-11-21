/*
 ************************************************************************************
 * Copyright (C) 2019 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

(function() {
  class Discount extends OB.App.MasterdataModel {
    constructor() {
      super();
      this.indices = [
        {
          indexName: 'id',
          keyPath: 'id',
          objectParameters: { unique: true }
        },
        {
          indexName: 'name',
          keyPath: 'name'
        }
      ];
    }
  }
  class DiscountFilterRole extends OB.App.MasterdataModel {
    constructor() {
      super();
      this.indices = [
        {
          indexName: '_identifier',
          keyPath: '_identifier'
        }
      ];
    }
  }
  class DiscountFilterBusinessPartner extends OB.App.MasterdataModel {
    constructor() {
      super();
      this.indices = [
        {
          indexName: '_identifier',
          keyPath: '_identifier'
        }
      ];
    }
  }
  class DiscountFilterBusinessPartnerGroup extends OB.App.MasterdataModel {
    constructor() {
      super();
      this.indices = [
        {
          indexName: '_identifier',
          keyPath: '_identifier'
        }
      ];
    }
  }
  class DiscountFilterBusinessPartnerSet extends OB.App.MasterdataModel {
    constructor() {
      super();
      this.indices = [
        {
          indexName: '_identifier',
          keyPath: '_identifier'
        }
      ];
    }
  }
  class DiscountFilterProduct extends OB.App.MasterdataModel {
    constructor() {
      super();
      this.indices = [
        {
          indexName: '_identifier',
          keyPath: '_identifier'
        }
      ];
    }
  }
  class DiscountFilterProductCategory extends OB.App.MasterdataModel {
    constructor() {
      super();
      this.indices = [
        {
          indexName: '_identifier',
          keyPath: '_identifier'
        }
      ];
    }
  }
  class DiscountFilterCharacteristic extends OB.App.MasterdataModel {
    constructor() {
      super();
      this.indices = [
        {
          indexName: '_identifier',
          keyPath: '_identifier'
        }
      ];
    }
  }
  class DiscountFilterPriceList extends OB.App.MasterdataModel {
    constructor() {
      super();
      this.indices = [
        {
          indexName: '_identifier',
          keyPath: '_identifier'
        }
      ];
    }
  }

  OB.App.MasterdataController.registerModel(Discount);
  OB.App.MasterdataController.registerModel(DiscountFilterRole);
  OB.App.MasterdataController.registerModel(DiscountFilterBusinessPartner);
  OB.App.MasterdataController.registerModel(DiscountFilterBusinessPartnerGroup);
  OB.App.MasterdataController.registerModel(DiscountFilterBusinessPartnerSet);
  OB.App.MasterdataController.registerModel(DiscountFilterProduct);
  OB.App.MasterdataController.registerModel(DiscountFilterProductCategory);
  OB.App.MasterdataController.registerModel(DiscountFilterCharacteristic);
  OB.App.MasterdataController.registerModel(DiscountFilterPriceList);
})();
