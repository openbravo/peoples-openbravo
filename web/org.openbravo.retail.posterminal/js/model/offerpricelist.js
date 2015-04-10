/*
 ************************************************************************************
 * Copyright (C) 2015 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global Backbone */

(function () {

  var OfferPriceList = OB.Data.ExtensibleModel.extend({
    modelName: 'OfferPriceList',
    tableName: 'm_offer_pricelist',
    entityName: 'OfferPriceList',
    source: 'org.openbravo.retail.posterminal.master.OfferPriceList',
    includeTerminalDate: true,
    initialize: function () {

    }
  });

  OfferPriceList.addProperties([{
    name: 'm_offer_pricelist_id',
    column: 'm_offer_pricelist_id',
    primaryKey: true,
    type: 'TEXT'
  }, {
    name: 'm_offer_id',
    column: 'm_offer_id',
    type: 'TEXT'
  }, {
    name: 'm_pricelist_id',
    column: 'm_pricelist_id',
    type: 'TEXT'
  }]);

  OfferPriceList.addIndex([{
    name: 'm_offer_pricelist',
    columns: [{
      name: 'm_offer_id',
      sort: 'asc'
    }, {
      name: 'm_pricelist_id',
      sort: 'asc'
    }]
  }]);

  //Register the model in the application 
  OB.Data.Registry.registerModel(OfferPriceList);
}());