/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.0  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html
 * Software distributed under the License  is  distributed  on  an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific  language  governing  rights  and  limitations
 * under the License.
 * The Original Code is Openbravo ERP.
 * The Initial Developer of the Original Code is Openbravo SLU
 * All portions are Copyright (C) 2014 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

OB.APRM.AddTransaction = {};


OB.APRM.AddTransaction.onLoad = function (view) {
  var bankStatementLineId = view.callerField.record.id;
  view.theForm.addField(isc.OBTextItem.create({
    name: 'bankStatementLineId',
    value: bankStatementLineId
  }));
  view.theForm.hideItem('bankStatementLineId');
};


OB.APRM.AddTransaction.onProcess = function (view, actionHandlerCall) {

};

OB.APRM.AddTransaction.trxTypeOnChangeFunction = function (item, view, form, grid) {
  if (item.getValue() === 'BPW') {
    form.getItem('depositamt').setDisabled(true);
    form.getItem('withdrawalamt').setDisabled(false);
  } else if (item.getValue() === 'BPD') {
    form.getItem('depositamt').setDisabled(false);
    form.getItem('withdrawalamt').setDisabled(true);
  } else {
    form.getItem('depositamt').setDisabled(false);
    form.getItem('withdrawalamt').setDisabled(false);
  }
};

OB.APRM.AddTransaction.paymentOnChangeFunction = function (item, view, form, grid) {
  var callback, strPaymentId = item.getValue();
  if (strPaymentId !== null) {
    form.getItem('c_glitem_id').visible = false;
  } else {
    form.getItem('c_glitem_id').visible = true;
  }

  callback = function (response, data, request) {
    form.getItem('description').setValue(data.description);
    form.getItem('depositamt').setValue(data.depositamt);
    form.getItem('withdrawalamt').setValue(data.paymentamt);
    form.getItem('c_bpartner_id').setValue(data.cBpartnerId);
  };

  OB.RemoteCallManager.call('org.openbravo.advpaymentmngt.actionHandler.PaymentTransactionActionHandler', {
    strPaymentId: strPaymentId
  }, {}, callback);
};

OB.APRM.AddTransaction.glitemOnChangeFunction = function (item, view, form, grid) {
  var callback, strGLItemId = item.getValue();

  if (strGLItemId !== null) {
    form.getItem('fin_payment_id').visible = false;
  } else {
    form.getItem('fin_payment_id').visible = true;
  }

  callback = function (response, data, request) {
    form.getItem('description').setValue(data.description);

    
  };

  OB.RemoteCallManager.call('org.openbravo.advpaymentmngt.actionHandler.GLItemTransactionActionHandler', {
    strGLItemId: strGLItemId
  }, {}, callback);
};