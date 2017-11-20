/*
 ************************************************************************************
 * Copyright (C) 2012-2016 Openbravo S.L.U.
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
    var i;
    var recordIds = [];
    for (i = 0; i < selectedRecords.length; i++) {
      recordIds.push(selectedRecords[i].id);
    }
    var callback = function (response, data, request) {
        if (data.title === 'Error') {
          isc.say(data.message);
        } else {
          OB.RemoteCallManager.call('org.openbravo.retail.posterminal.SaveDataActionHandler', recordIds, {}, function (response, data, request) {
            isc.say(data.message);
            params.button.contextView.viewGrid.refreshGrid();
          });
        }
        };

    OB.RemoteCallManager.call('org.openbravo.retail.posterminal.SaveDataOnProcessHandler', recordIds, {}, callback);
    return;
  };
  OB.OBPOS.Errors.clearError = function (params, view) {
    var selectedRecords = view.view.viewGrid.getSelectedRecords();
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