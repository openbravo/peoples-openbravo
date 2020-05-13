/*
 ************************************************************************************
 * Copyright (C) 2019-2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global OB*/

OB.MobileApp.statesRegistry.register(
  new OB.State.BackboneProperty({
    window: 'retail.pointofsale',
    name: 'receipt.isEditable',
    object: function(view) {
      return view.model.get('order');
    },
    property: 'isEditable'
  })
);

OB.MobileApp.statesRegistry.register(
  new OB.State.BackboneProperty({
    window: 'retail.pointofsale',
    name: 'receipt.generateInvoice',
    object: function(view) {
      return view.model.get('order');
    },
    property: 'fullInvoice'
  })
);

OB.MobileApp.statesRegistry.register(
  new OB.State.BackboneProperty({
    window: 'retail.pointofsale',
    name: 'receipt.bp',
    object: function(view) {
      return view.model.get('order');
    },
    property: 'bp'
  })
);

OB.MobileApp.statesRegistry.register(
  new OB.State.BackboneProperty({
    window: 'retail.pointofsale',
    name: 'receipt.isQuotation',
    object: function(view) {
      return view.model.get('order');
    },
    property: 'isQuotation'
  })
);

OB.MobileApp.statesRegistry.register(
  new OB.State.BackboneProperty({
    window: 'retail.pointofsale',
    name: 'receipt.isLayaway',
    object: function(view) {
      return view.model.get('order');
    },
    property: 'isLayaway'
  })
);

OB.MobileApp.statesRegistry.register(
  new OB.State.BackboneProperty({
    window: 'retail.pointofsale',
    name: 'receipt.isPaid',
    object: function(view) {
      return view.model.get('order');
    },
    property: 'isPaid'
  })
);

OB.MobileApp.statesRegistry.register(
  new OB.State.BackboneProperty({
    window: 'retail.pointofsale',
    name: 'receipt.hasBeenPaid',
    object: function(view) {
      return view.model.get('order');
    },
    property: 'hasbeenpaid'
  })
);

OB.MobileApp.statesRegistry.register(
  new OB.State.BackboneProperty({
    window: 'retail.pointofsale',
    name: 'receipt.orderType',
    object: function(view) {
      return view.model.get('order');
    },
    property: 'orderType'
  })
);

OB.MobileApp.statesRegistry.register(
  new OB.State.BackboneProperty({
    window: 'retail.pointofsale',
    name: 'receipt.hasServices',
    object: function(view) {
      return view.model.get('order');
    },
    property: 'hasServices'
  })
);

OB.MobileApp.statesRegistry.register(
  new OB.State.BackboneProperty({
    window: 'retail.pointofsale',
    name: 'receipt.gross',
    object: function(view) {
      return view.model.get('order');
    },
    property: 'gross'
  })
);

OB.MobileApp.statesRegistry.register(
  new OB.State.BackboneProperty({
    window: 'retail.pointofsale',
    name: 'receipt.replacedOrder',
    object: function(view) {
      return view.model.get('order');
    },
    property: 'replacedorder'
  })
);
