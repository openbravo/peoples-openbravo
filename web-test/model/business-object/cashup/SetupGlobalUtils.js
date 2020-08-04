/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
require('../../../../web/org.openbravo.retail.posterminal/app/model/business-object/cashup/CashupUtils');
require('../../../../web/org.openbravo.retail.posterminal/app/model/business-object/cashup/PaymentMethodUtils');
require('../../../../../org.openbravo.mobile.core/web/org.openbravo.mobile.core/app/model/business-object/messages/MessagesUtils.js');

OB.App.State = { Cashup: { Utils: {} }, Messages: { Utils: {} } };

OB.App.StateAPI.Cashup.utilities.forEach(util => {
  OB.App.State.Cashup.Utils[util.functionName] = util.implementation;
});
OB.App.StateAPI.Messages.utilities.forEach(util => {
  OB.App.State.Messages.Utils[util.functionName] = util.implementation;
});
