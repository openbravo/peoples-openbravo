/*
 ************************************************************************************
 * Copyright (C) 2018-2019 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

if (OB.MobileApp.model.hasPermission('OBRDM_EnableDeliveryModes', true)) {
  OB.UTIL.HookManager.registerHook('OBPOS_PreAddProductWithoutStock', function (args, callbacks) {
    if (args.line) {
      if (args.line.get('obrdmDeliveryMode') && args.line.get('obrdmDeliveryMode') !== 'PickAndCarry') {
        args.allowToAdd = false;
      }
    } else {
      if (args.order.get('orderType') === 2) {
        if (args.product.get('obrdmDeliveryModeLyw') && args.product.get('obrdmDeliveryModeLyw') !== 'PickAndCarry') {
          args.allowToAdd = false;
        }
      } else {
        if (args.product.get('obrdmDeliveryMode') && args.product.get('obrdmDeliveryMode') !== 'PickAndCarry') {
          args.allowToAdd = false;
        }
      }
    }
    OB.UTIL.HookManager.callbackExecutor(args, callbacks);
  });
}