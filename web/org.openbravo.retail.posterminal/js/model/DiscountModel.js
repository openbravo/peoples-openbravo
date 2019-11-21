/*
 ************************************************************************************
 * Copyright (C) 2019 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

(function() {
  class Discount extends OB.MasterdataModelDefinition {
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
  class DiscountFilterRole extends OB.MasterdataModelDefinition {
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
  class DiscountFilterBusinessPartner extends OB.MasterdataModelDefinition {
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
  class DiscountFilterBusinessPartnerGroup extends OB.MasterdataModelDefinition {
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
  class DiscountFilterBusinessPartnerSet extends OB.MasterdataModelDefinition {
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
  class DiscountFilterProduct extends OB.MasterdataModelDefinition {
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
  class DiscountFilterProductCategory extends OB.MasterdataModelDefinition {
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
  class DiscountFilterCharacteristic extends OB.MasterdataModelDefinition {
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
  class DiscountFilterPriceList extends OB.MasterdataModelDefinition {
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

  OB.MasterdataController.registerModel(Discount);
  OB.MasterdataController.registerModel(DiscountFilterRole);
  OB.MasterdataController.registerModel(DiscountFilterBusinessPartner);
  OB.MasterdataController.registerModel(DiscountFilterBusinessPartnerGroup);
  OB.MasterdataController.registerModel(DiscountFilterBusinessPartnerSet);
  OB.MasterdataController.registerModel(DiscountFilterProduct);
  OB.MasterdataController.registerModel(DiscountFilterProductCategory);
  OB.MasterdataController.registerModel(DiscountFilterCharacteristic);
  OB.MasterdataController.registerModel(DiscountFilterPriceList);
})();
