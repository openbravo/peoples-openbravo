/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/* global global */
global.OB = {
  App: {
    Class: {},
    UUID: { generate: jest.fn() }
  }
};

global.lodash = require('../../../../../org.openbravo.mobile.core/web/org.openbravo.mobile.core/lib/vendor/lodash-4.17.15');
require('../../../../../org.openbravo.client.kernel/web/org.openbravo.client.kernel/js/BigDecimal-all-1.0.3');
require('../../../../../org.openbravo.mobile.core/web/org.openbravo.mobile.core/source/utils/ob-arithmetic');
require('../../../../../org.openbravo.mobile.core/web/org.openbravo.mobile.core/app/model/application-state/StateAPI');
require('../../../../../org.openbravo.mobile.core/web/org.openbravo.mobile.core/app/util/ArrayUtils');
require('../../../../web/org.openbravo.retail.posterminal/app/model/business-object/ticket/Ticket');
require('../../../../web/org.openbravo.retail.posterminal/app/model/business-object/ticket-list/TicketList');
require('../../../../web/org.openbravo.retail.posterminal/app/model/business-object/document-sequence/DocumentSequence');
require('../../../../web/org.openbravo.retail.posterminal/app/model/business-object/cashup/Cashup');
require('../../../../../org.openbravo.mobile.core/web/org.openbravo.mobile.core/app/model/business-object/messages/Messages');
require('../../../../web/org.openbravo.retail.posterminal/app/model/business-object/ticket/TicketUtils');
require('../../../../web/org.openbravo.retail.posterminal/app/model/business-object/ticket/CompleteTicketUtils');
require('../../../../web/org.openbravo.retail.posterminal/app/model/business-object/ticket/LoadTicketUtils');
require('../../../../web/org.openbravo.retail.posterminal/app/model/business-object/ticket-list/TicketListUtils');
require('../../../../web/org.openbravo.retail.posterminal/app/model/business-object/document-sequence/DocumentSequenceUtils');
require('../../../../web/org.openbravo.retail.posterminal/app/model/business-object/cashup/CashupUtils');
require('../../../../web/org.openbravo.retail.posterminal/app/model/business-object/cashup/PaymentMethodUtils');
require('../../../../../org.openbravo.mobile.core/web/org.openbravo.mobile.core/app/model/business-object/messages/MessagesUtils');
