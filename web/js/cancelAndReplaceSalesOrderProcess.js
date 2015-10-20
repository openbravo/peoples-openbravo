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
 * All portions are Copyright (C) 2015 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

OB = OB || {};

OB.CancelAndReplaceSalesOrder = OB.CancelAndReplaceSalesOrder || {};

OB.CancelAndReplaceSalesOrder = {
  execute: function (params, view) {
    var selection = params.button.contextView.viewGrid.getSelectedRecords(),
        tabId = view.view.tabId,
        callback;

    callback = function (rpcResponse, result, rpcRequest) {
      result.responseActions._processView = params.button.view;
      OB.Utilities.Action.executeJSON(result.responseActions, null, null, params.button.view);
      params.button.closeProcessPopup();
    };
    
    OB.RemoteCallManager.call('org.openbravo.common.actionhandler.CancelAndReplaceSalesOrder', {
      inpcOrderId: selection[0].id,
      inpTabId: tabId
    }, {}, callback);
  },

  cancelAndReplaceSalesOrder: function (params, view) {
    OB.CancelAndReplaceSalesOrder.execute(params, view);
  }
};