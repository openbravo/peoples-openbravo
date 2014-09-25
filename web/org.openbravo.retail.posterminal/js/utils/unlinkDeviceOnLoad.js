/*
 ************************************************************************************
 * Copyright (C) 2014 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global OB */

OB.POS.UnlinkDevice = window.OB.POS.UnlinkDevice || {};

OB.POS.UnlinkDevice.onLoad = function (view) {
  view.messageBar.setMessage('warning', OB.I18N.getLabel('OBPOS_UnlinkDeviceWarningTitle'), OB.I18N.getLabel('OBPOS_UnlinkDeviceWarningMsg'));
};