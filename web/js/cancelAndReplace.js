/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.1  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html 
 * Software distributed under the License  is  distributed  on  an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific  language  governing  rights  and  limitations
 * under the License. 
 * The Original Code is Openbravo ERP. 
 * The Initial Developer of the Original Code is Openbravo SLU 
 * All portions are Copyright (C) 2016 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

OB.CancelAndReplace = {};
OB.CancelAndReplace.ClientSideEventHandlers = {};
OB.CancelAndReplace.SALES_ORDERLINES_TAB = '187';

OB.CancelAndReplace.ClientSideEventHandlers.showMessage = function (view, form, grid, extraParameters, actions) {
  var data = extraParameters.data,
      newOrderedQuantity = data.orderedQuantity;

  view.messageBar.keepOnAutomaticRefresh = true;

  callback = function (response, cdata, request) {
    if (cdata && cdata.deliveredQuantity && cdata.deliveredQuantity > newOrderedQuantity) {
      // Update flow
      view.messageBar.setMessage(isc.OBMessageBar.TYPE_ERROR, null, OB.I18N.getLabel('CannotOrderLessThanDeliveredInCancelReplace'));
      return;
    }
    OB.EventHandlerRegistry.callbackExecutor(view, form, grid, extraParameters, actions);
  }

  if (data.replacedorderline && !extraParameters.isNewRecord) {
    // Calling action handler
    OB.RemoteCallManager.call('org.openbravo.common.actionhandler.CancelAndReplaceGetCancelledOrderLine', {
      orderLineId: data.replacedorderline
    }, {}, callback);
  } else {
    OB.EventHandlerRegistry.callbackExecutor(view, form, grid, extraParameters, actions);
  }

};

OB.EventHandlerRegistry.register(OB.CancelAndReplace.SALES_ORDERLINES_TAB, OB.EventHandlerRegistry.PRESAVE, OB.CancelAndReplace.ClientSideEventHandlers.showMessage, 'OBCancelAndReplace_ShowMessage');