/*
 ************************************************************************************
 * Copyright (C) 2019 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global OB, Promise */

OB.MobileApp.statesRegistry.register(
new OB.State.BackboneProperty({
  window: 'retail.pointofsale',
  name: 'receipt.isEditable',
  object: function (view) {
    return view.model.get('order');
  },
  property: 'isEditable'
}));

OB.MobileApp.statesRegistry.register(
new OB.State.BackboneProperty({
  window: 'retail.pointofsale',
  name: 'receipt.generateInvoice',
  object: function (view) {
    return view.model.get('order');
  },
  property: 'generateInvoice'
}));


OB.MobileApp.statesRegistry.register(
new OB.State.BackboneProperty({
  window: 'retail.pointofsale',
  name: 'receipt.bp',
  object: function (view) {
    return view.model.get('order');
  },
  property: 'bp'
}));


OB.MobileApp.statesRegistry.register(
new OB.State.BackboneProperty({
  window: 'retail.pointofsale',
  name: 'receipt.isQuotation',
  object: function (view) {
    return view.model.get('order');
  },
  property: 'isQuotation'
}));