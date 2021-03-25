/*
 ************************************************************************************
 * Copyright (C) 2020-2021 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/* global global */
global.OB = {
  App: {
    StateBackwardCompatibility: { setProperties: jest.fn() },
    Class: {},
    Security: { hasPermission: jest.fn(), requestApprovalForAction: jest.fn() },
    UUID: { generate: jest.fn() },
    StockChecker: { hasStock: jest.fn() },
    View: {
      DialogUIHandler: { askConfirmation: jest.fn(), inputData: jest.fn() }
    },
    UserNotifier: { notifyWarning: jest.fn() }
  },
  UTIL: {
    HookManager: { registerHook: jest.fn() }
  }
};

global.lodash = require('../../../../../org.openbravo.mobile.core/web/org.openbravo.mobile.core/lib/vendor/lodash-4.17.15');
require('../../../../../org.openbravo.client.kernel/web/org.openbravo.client.kernel/js/BigDecimal-all-1.0.3');
require('../../../../../org.openbravo.mobile.core/web/org.openbravo.mobile.core/source/utils/ob-arithmetic');
require('../../../../../org.openbravo.mobile.core/web/org.openbravo.mobile.core/app/model/application-state/StateAPI');
require('../../../../../org.openbravo.mobile.core/web/org.openbravo.mobile.core/app/exception/TranslatableError');
require('../../../../../org.openbravo.mobile.core/web/org.openbravo.mobile.core/app/model/application-state/ActionCanceled');
require('../../../../../org.openbravo.mobile.core/web/org.openbravo.mobile.core/app/model/application-state/ActionSilentlyCanceled');
require('../../../../../org.openbravo.mobile.core/web/org.openbravo.mobile.core/app/util/ArrayUtils');
require('../../../../web/org.openbravo.retail.posterminal/app/model/business-logic/stock/StockChecker');
require('../../../../web/org.openbravo.retail.posterminal/app/model/business-object/ticket/Ticket');
require('../../../../web/org.openbravo.retail.posterminal/app/model/business-object/ticket/TicketUtils');
require('../../../../web/org.openbravo.retail.posterminal/app/model/business-object/ticket/CompleteTicketUtils');
require('../../../../web/org.openbravo.retail.posterminal/app/model/business-object/ticket/LoadTicketUtils');
require('../../../../web/org.openbravo.retail.posterminal/app/model/business-object/ticket/AddPaymentUtils');
require('../../../../web/org.openbravo.retail.posterminal/app/model/business-object/ticket/AddProductUtils');
