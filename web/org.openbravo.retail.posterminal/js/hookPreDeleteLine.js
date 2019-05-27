/*
 ************************************************************************************
 * Copyright (C) 2018-2019 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global _ */

OB.UTIL.HookManager.registerHook('OBPOS_PreDeleteLine', function (args, c) {
  if (OB.MobileApp.model.hasPermission('OBRDM_EnableDeliveryModes', true)) {
    var undoDeliveryModes = [];
    _.each(args.selectedLines, function (line) {
      undoDeliveryModes.push({
        id: line.get('id'),
        prodcutId: line.get('product').get('id'),
        nameDelivery: line.get('nameDelivery'),
        obrdmDeliveryMode: line.get('obrdmDeliveryMode'),
        obrdmDeliveryDate: line.get('obrdmDeliveryDate'),
        obrdmDeliveryTime: line.get('obrdmDeliveryTime')
      });
    });
    args.order.set('undoDeliveryModes', undoDeliveryModes);
  }
  OB.UTIL.HookManager.callbackExecutor(args, c);
});