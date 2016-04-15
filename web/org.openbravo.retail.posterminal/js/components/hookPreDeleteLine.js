/*
 ************************************************************************************
 * Copyright (C) 2015 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global, enyo */

OB.UTIL.HookManager.registerHook('OBPOS_PreDeleteLine', function (args, c) {
  enyo.$.scrim.show();
  args.order.get('lines').forEach(function (line, idx) {
    line.set('undoPosition', idx);
  });
  OB.UTIL.HookManager.callbackExecutor(args, c);
});