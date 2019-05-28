/*
 ************************************************************************************
 * Copyright (C) 2018-2019 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global _ */

OB.UTIL.HookManager.registerHook('OBPOS_PostUndo_DeleteLine', function (args, c) {
  if (OB.MobileApp.model.hasPermission('OBRDM_EnableDeliveryModes', true)) {
    var undoDeliveryModes = args.order.get('undoDeliveryModes'),
        lines = args.order.get('lines').models;
    _.each(undoDeliveryModes, function (delivery) {
      var line = _.find(lines, function (l) {
        return l.get('id') === delivery.id;
      });
      if (line) {
        line.set('nameDelivery', delivery.nameDelivery);
        line.set('obrdmDeliveryMode', delivery.obrdmDeliveryMode);
        line.set('obrdmDeliveryDate', delivery.obrdmDeliveryDate);
        line.set('obrdmDeliveryTime', delivery.obrdmDeliveryTime);
      }
    });
  }
  OB.UTIL.HookManager.callbackExecutor(args, c);
});