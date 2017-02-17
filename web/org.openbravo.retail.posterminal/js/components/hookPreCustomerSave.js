/*
 ************************************************************************************
 * Copyright (C) 2017 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global OB, enyo, Backbone */

(function () {

  OB.UTIL.HookManager.registerHook('OBPOS_PreCustomerSave', function (args, callback) {

    if (args.meObject.customer && args.validations) {

      //Validate anonumous customer edit allowed
      if (OB.MobileApp.model.get('terminal').businessPartner === args.meObject.customer.id && OB.MobileApp.model.hasPermission('OBPOS_NotAllowEditAnonymousCustomer', true)) {
        OB.UTIL.showError(OB.I18N.getLabel('OBPOS_CannotEditAnonymousCustomer'));
        args.cancellation = true;
        return;
      }

    }

    OB.UTIL.HookManager.callbackExecutor(args, callback);
  });

}());