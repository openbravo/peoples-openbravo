/*
 ************************************************************************************
 * Copyright (C) 2017 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global OB */

OB.OBPOS = OB.OBPOS || {};
OB.OBPOS.ClientSideEventHandlers = OB.OBPOS.ClientSideEventHandlers || {};
OB.OBPOS.CHARACTERISTICS_HEADER_TAB = 'A661A0A05DCD4650BCB14B010C87F0AA';
OB.OBPOS.ClientSideEventHandlers.refreshCharacteristicsGrid = function (view, form, grid, extraParameters, actions) {
  var callback, isDelete = false,
      recordsToDelete = null;
  if (extraParameters && extraParameters.recordsToDelete) {
    isDelete = true;
    recordsToDelete = extraParameters.recordsToDelete;
  }
  callback = function (response, cdata, request) {
    if (cdata && cdata.warn) {
      view.messageBar.setMessage(isc.OBMessageBar.TYPE_WARNING, cdata.warnMessageTitle, cdata.warnMessageBody);
    }
    OB.EventHandlerRegistry.callbackExecutor(view, form, grid, extraParameters, actions);
  };

  OB.RemoteCallManager.call('org.openbravo.retail.posterminal.actionHandler.ProductCharacteristicActionHandler', {
    isDelete: isDelete,
    recordsToDelete: recordsToDelete
  }, {}, callback);
};
OB.EventHandlerRegistry.register(OB.OBPOS.CHARACTERISTICS_HEADER_TAB, OB.EventHandlerRegistry.POSTSAVE, OB.OBPOS.ClientSideEventHandlers.refreshCharacteristicsGrid, 'OBPOS_refreshCharacteristicsGrid');
OB.EventHandlerRegistry.register(OB.OBPOS.CHARACTERISTICS_HEADER_TAB, OB.EventHandlerRegistry.PREDELETE, OB.OBPOS.ClientSideEventHandlers.refreshCharacteristicsGrid, 'OBPOS_refreshCharacteristicsGrid');