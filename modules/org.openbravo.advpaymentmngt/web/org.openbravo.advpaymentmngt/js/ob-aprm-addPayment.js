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
OB.APRM.AddPayment = {};

OB.APRM.AddPayment.onLoad = function (view) {
  OB.APRM.AddPayment.paymentMethodMulticurrency(null, view, null, null);
  OB.APRM.AddPayment.actualPaymentOnLoad(view);
};

OB.APRM.AddPayment.addNewGLItem = function (grid) {
  var selectedRecord = grid.view.parentWindow.views[0].getParentRecord();
  var returnObject = isc.addProperties({}, grid.data[0]);
  returnObject.organization = selectedRecord.organization;
  return returnObject;
};

OB.APRM.AddPayment.paymentMethodMulticurrency = function (item, view, form, grid) {
  var paymentMethodId, callback, isPayinIsMulticurrency;
  if (item) {
    paymentMethodId = item.getValue();
  } else {
    paymentMethodId = view.theForm.getItem('fin_paymentmethod_id').getValue();
  }

  callback = function (response, data, request) {
    isPayinIsMulticurrency = data.isPayinIsMulticurrency;
    if (isPayinIsMulticurrency) {
      if (form) {
        form.getItem('c_currency_to_id').visible = true;
        form.redraw();
      } else {
        view.theForm.getItem('c_currency_to_id').visible = true;
        view.theForm.redraw();
      }
    } else {
      if (form) {
        form.getItem('c_currency_to_id').visible = false;
        form.getItem('c_currency_to_id').setValue(form.getItem('c_currency_id').getValue());
        form.redraw();
      } else {
        view.theForm.getItem('c_currency_to_id').visible = false;
        view.theForm.redraw();
      }

    }
  };
  OB.RemoteCallManager.call('org.openbravo.advpaymentmngt.actionHandler.PaymentMethodMulticurrencyActionHandler', {
    paymentMethodId: paymentMethodId
  }, {}, callback);
};

OB.APRM.AddPayment.paymentMethodOnLoadFunction = function (view) {
  var paymentMethodId = view.theForm.getItem('fin_paymentmethod_id').getValue(),
      callback, isPayinIsMulticurrency;
  callback = function (response, data, request) {
    isPayinIsMulticurrency = data.isPayinIsMulticurrency;
    if (isPayinIsMulticurrency) {
      view.theForm.getItem('c_currency_to_id').visible = true;
      view.theForm.redraw();
    } else {
      view.theForm.getItem('c_currency_to_id').visible = false;
      view.theForm.redraw();
    }
  };
  OB.RemoteCallManager.call('org.openbravo.advpaymentmngt.actionHandler.PaymentMethodMulticurrencyActionHandler', {
    paymentMethodId: paymentMethodId
  }, {}, callback);
};

OB.APRM.AddPayment.transactionTypeOnChangeFunction = function (item, view, form, grid) {
  form.getItem('order_invoice').canvas.viewGrid.invalidateCache();
  form.getItem('order_invoice').canvas.viewGrid.fetchData(form.getItem('order_invoice').canvas.viewGrid.getCriteria());
  form.redraw();
};

OB.APRM.AddPayment.actualPaymentOnChange = function (item, view, form, grid) {
  var issotrx = form.getItem('issotrx').getValue();
  if (issotrx) {
    OB.APRM.AddPayment.distributeAmount(view, form);
  }

};

OB.APRM.AddPayment.actualPaymentOnLoad = function (view) {

  var orderInvoiceGrid = view.theForm.getItem('order_invoice').canvas.viewGrid;
  if (!isc.isA.ResultSet(orderInvoiceGrid.data) || !orderInvoiceGrid.data.lengthIsKnown()) {
    setTimeout(function () {
      OB.APRM.AddPayment.actualPaymentOnLoad(view);
    }, 500);
    return;
  }
  var issotrx = view.theForm.getItem('issotrx').getValue(),
      form = view.theForm;
  if (issotrx) {
    OB.APRM.AddPayment.distributeAmount(view, form);
  }

};
OB.APRM.AddPayment.distributeAmount = function (view, form) {
  var amount = form.getItem('actual_payment').getValue(),
      distributedAmount = 0,
      keepSelection = false,
      orderInvoice = form.getItem('order_invoice').canvas.viewGrid,
      scheduledPaymentDetailId, outstandingAmount, j, i, isGLItemEnabled = form.getItem('glitem').canvas.viewGrid.isVisible(),
      total, chk, credit, glitem;
  //cambiar   
  if (amount.Class === "String") {
    amount = OB.Utilities.Number.OBMaskedToJS(amount, form.getItem('actual_payment').typeInstance.decSeparator, form.getItem('actual_payment').typeInstance.groupSeparator);
  }
  //amounts de gl items
  if (isGLItemEnabled) {
    glitem = form.getItem('amount_gl_items').getValue() || 0;
    amount = amount - glitem;
  }
  //amount de credito
  credit = form.getItem('used_credit').getValue() || 0;
  amount = amount + credit;

  chk = orderInvoice.selectedIds;
  total = orderInvoice.data.totalRows;

  if ((amount > distributedAmount) || (amount === distributedAmount)) {
    amount = amount - distributedAmount;
  }
  for (i = 0; i < total; i++) {
    if (keepSelection) {
      continue;
    }
    outstandingAmount = orderInvoice.getRecord(i).outstandingAmount;
    if ((outstandingAmount < 0) && (amount < 0)) {
      if (Math.abs(outstandingAmount) > Math.abs(amount)) {
        outstandingAmount = amount;
      }
    } else {
      if (outstandingAmount > amount) {
        outstandingAmount = amount;
      }
    }
    if (amount === 0) {
      orderInvoice.setEditValue((i), 'amount', "");
      orderInvoice.deselectRecord(i);

    } else {
      orderInvoice.setEditValue((i), 'amount', outstandingAmount);
      orderInvoice.selectRecord(i);
      amount = amount - outstandingAmount;
    }

  }
  OB.APRM.AddPayment.updateTotal();
  return true;

};


OB.APRM.AddPayment.updateTotal = function () {

};

OB.APRM.AddPayment.updateData = function (key, mark, drivenByGrid, all) {


};