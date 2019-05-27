/*
 ************************************************************************************
 * Copyright (C) 2018-2019 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

if (OB.MobileApp.model.hasPermission('OBRDM_EnableDeliveryModes', true)) {
  OB.UTIL.HookManager.registerHook('OBPOS_PrePaymentHook', function (args, callbacks) {
    var receipt = args.context.get('order'),
        hasErrorLines = false;
    if (receipt.get('isQuotation') !== true) {
      receipt.get('lines').forEach(function (line) {
        if ((!line.get('obrdmDeliveryMode') || line.get('obrdmDeliveryMode') === 'PickAndCarry') && !line.get('obposCanbedelivered') && line.get('deliveredQuantity') !== line.get('qty')) {
          hasErrorLines = true;
          args.cancellation = true;
        }
      });
    }
    if (hasErrorLines) {
      OB.UTIL.showConfirmation.display(OB.I18N.getLabel('OBMOBC_Error'), OB.I18N.getLabel('OBRDM_PickAndCarryError'));
    }
    OB.UTIL.HookManager.callbackExecutor(args, callbacks);
  });
}