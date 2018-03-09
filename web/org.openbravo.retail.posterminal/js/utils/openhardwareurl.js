/*
 ************************************************************************************
 * Copyright (C) 2018 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global OB */


(function () {
  OB.POS = window.OB.POS || {};

  function openURL(data, view, field) {

    var grid = view.view.viewGrid;
    var selectedRecords = grid.getSelectedRecords();
    var i;
    var url;
    var parser;

    if (selectedRecords.length > 0) {
      isc.confirm(OB.I18N.getLabel('OBPOS_OpenHardwareURLConfirmation'), function (value) {
        if (value !== null && value) {
          for (i = 0; i < selectedRecords.length; i++) {
            parser = document.createElement('a');
            parser.href = selectedRecords[i][field];
            window.open(parser.protocol + '//' + parser.host);
          }
        }
      });
    }
  }

  OB.POS.openTerminalHardwareURL = function (data, view) {
    openURL(data, view, 'hardwareurl');
  };

  OB.POS.openHardwareURL = function (data, view) {
    openURL(data, view, 'hardwareURL');
  };

}());