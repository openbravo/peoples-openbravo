/*
 ************************************************************************************
 * Copyright (C) 2019-2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

OB.MobileApp.statesRegistry.register(
  new OB.State.BackboneProperty({
    window: 'retail.pointofsale',
    name: 'window.currentView',
    object: function(view) {
      return view.model.get('leftColumnViewManager');
    },
    property: 'currentView'
  })
);

OB.MobileApp.statesRegistry.register(
  new OB.State.BackboneProperty({
    window: 'retail.pointofsale',
    name: 'window.currentExternalBpOpenedData',
    object: function(view) {
      return view.model;
    },
    property: 'externalBpOpenedData'
  })
);

OB.MobileApp.statesRegistry.register(
  new OB.State.BackboneProperty({
    window: 'retail.pointofsale',
    name: 'window.currentExternalBpOpenedDialog',
    object: function(view) {
      return view.model;
    },
    property: 'externalBpOpenedDialog'
  })
);
