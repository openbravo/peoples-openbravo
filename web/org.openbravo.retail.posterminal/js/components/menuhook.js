/*
 ************************************************************************************
 * Copyright (C) 2014-2018 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global enyo, Backbone, $, _ */

OB.UTIL.HookManager.registerHook('OBMOBC_PreWindowNavigate', function (args, callbacks) {
  var destWindow = args.window;
  if (destWindow.approvalType) {
    OB.UTIL.Approval.requestApproval(
    null, destWindow.approvalType, function (approved, supervisor, approvalType) {
      if (approved) {
        args.cancellation = false;
      } else {
        args.cancellation = true;
      }
      OB.UTIL.HookManager.callbackExecutor(args, callbacks);
    });
  } else {
    OB.UTIL.HookManager.callbackExecutor(args, callbacks);
  }
});

OB.UTIL.HookManager.registerHook('OBMOBC_PostWindowNavigate', function (args, callbacks) {
  // Refresh Master Data
  OB.UTIL.checkRefreshMasterDataOnNavigate();
});