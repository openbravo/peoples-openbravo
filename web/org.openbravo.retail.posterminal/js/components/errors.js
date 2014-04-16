/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

(function () {

  OB.OBPOS = {};

  OB.OBPOS.Errors = {};

  OB.OBPOS.Errors.saveOrder = function (params, view) {
    var selectedRecords = view.view.viewGrid.getSelectedRecords();
    var i, requestOrderParams, requestBPParams;
    var recordIds = [];
    for (i = 0; i < selectedRecords.length; i++) {
      recordIds.push(selectedRecords[i].id);
    }

    OB.RemoteCallManager.call('org.openbravo.retail.posterminal.SaveDataActionHandler', recordIds, {}, function (response, data, request) {
      isc.say(data.message);
      params.button.closeProcessPopup();
    });

    return;
  };
  OB.OBPOS.Errors.clearError = function (params, view) {
    var selectedRecords = view.view.viewGrid.getSelectedRecords();
    var theParams = params;
    var i, requestParams;
    var ids = [];

    var callback = function (response, data, request) {
        isc.say(data.message);
        params.button.closeProcessPopup();
        };

    for (i = 0; i < selectedRecords.length; i++) {
      ids.push(selectedRecords[i].id);
    }

    requestParams = {
      recordIds: ids
    };
    isc.confirm(OB.I18N.getLabel('OBPOS_ClearError'), function (value) {
      if (value !== null && value) {
        OB.RemoteCallManager.call('org.openbravo.retail.posterminal.ClearErrorActionHandler', ids, requestParams, callback);
      }
    });
  };

}());