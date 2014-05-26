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
  var orderInvoiceGrid = view.theForm.getItem('order_invoice').canvas.viewGrid,
      glitemGrid = view.theForm.getItem('glitem').canvas.viewGrid,
      creditUseGrid = view.theForm.getItem('credit_to_use').canvas.viewGrid;

  OB.APRM.AddPayment.paymentMethodMulticurrency(null, view, null, null);
  OB.APRM.AddPayment.actualPaymentOnLoad(view);
  orderInvoiceGrid.selectionChanged = OB.APRM.AddPayment.selectionChanged;
  glitemGrid.selectionChanged = OB.APRM.AddPayment.selectionChangedGlitem;
  creditUseGrid.selectionChanged = OB.APRM.AddPayment.selectionChangedCredit;
};

OB.APRM.AddPayment.addNewGLItem = function (grid) {
  var selectedRecord = grid.view.parentWindow.views[0].getParentRecord();
  var returnObject = isc.addProperties({}, grid.data[0]);
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

OB.APRM.AddPayment.orderInvoiceAmountOnChange = function (item, view, form, grid) {
  OB.APRM.AddPayment.updateInvOrderTotal(form, grid);
  return true;
};

OB.APRM.AddPayment.orderInvoiceTotalAmountOnChange = function (item, view, form, grid) {
  OB.APRM.AddPayment.updateTotal(form);
  return true;
};

OB.APRM.AddPayment.glItemTotalAmountOnChange = function (item, view, form, grid) {
  OB.APRM.AddPayment.updateTotal(form);
  return true;
};

OB.APRM.AddPayment.distributeAmount = function (view, form) {
  var amount = new BigDecimal(String(form.getItem('actual_payment').getValue() || 0)),
      distributedAmount = new BigDecimal("0"),
      orderInvoice = form.getItem('order_invoice').canvas.viewGrid,
      scheduledPaymentDetailId, outstandingAmount, j, i, total, chk, credit, glitem;

  //amounts de gl items
  glitem = new BigDecimal(String(form.getItem('amount_gl_items').getValue() || 0));
  amount = amount.add(glitem);

  //amount de credito
  credit = new BigDecimal(String(form.getItem('used_credit').getValue() || 0));
  amount = amount.add(credit);

  chk = orderInvoice.selectedIds;
  total = orderInvoice.data.totalRows;

  if ((amount.compareTo(distributedAmount) > 0) || (amount.equals(distributedAmount))) {
    amount = amount.subtract(distributedAmount);
  }
  for (i = 0; i < total; i++) {
    if (!(!orderInvoice.selection[i])) {
      continue;
    }
    outstandingAmount = new BigDecimal(String(orderInvoice.getRecord(i).outstandingAmount));
    if ((outstandingAmount.compareTo(new BigDecimal("0")) < 0) && (amount.compareTo(new BigDecimal("0")) < 0)) {
      if (Math.abs(outstandingAmount) > Math.abs(amount)) {
        outstandingAmount = amount;
      }
    } else {
      if (outstandingAmount.compareTo(amount) > 0) {
        outstandingAmount = amount;
      }
    }
    // do not distribute again when the selectionChanged method is invoked
    orderInvoice.preventDistributingOnSelectionChanged = true;
    if (amount.signum() === 0) {
      orderInvoice.setEditValue((i), 'amount', '');
      orderInvoice.deselectRecord(i);

    } else {
      orderInvoice.setEditValue((i), 'amount', outstandingAmount.toString());
      orderInvoice.selectRecord(i);
      amount = amount.subtract(outstandingAmount);

    }
    delete orderInvoice.preventDistributingOnSelectionChanged;

  }
  OB.APRM.AddPayment.updateInvOrderTotal(form, orderInvoice);
  return true;

};


OB.APRM.AddPayment.updateTotal = function (form) {
  var invOrdTotalItem = form.getItem('amount_inv_ords'),
      glItemsTotalItem = form.getItem('amount_gl_items'),
      totalItem = form.getItem('total'),
      totalAmt;

  totalAmt = new BigDecimal(String(invOrdTotalItem.getValue() || 0));
  totalAmt = totalAmt.add(new BigDecimal(String(glItemsTotalItem.getValue() || 0)));

  totalItem.setValue(totalAmt.toString());
};

OB.APRM.AddPayment.updateInvOrderTotal = function (form, grid) {
  var amt, i, bdAmt, totalAmt = BigDecimal.prototype.ZERO,
      amountField = grid.getFieldByColumnName('amount'),
      selectedRecords = grid.getSelectedRecords(),
      invOrdTotalItem = form.getItem('amount_inv_ords');

  for (i = 0; i < selectedRecords.length; i++) {
    amt = grid.getEditedCell(grid.getRecordIndex(selectedRecords[i]), amountField);
    bdAmt = new BigDecimal(String(amt));
    totalAmt = totalAmt.add(bdAmt);
  }
  invOrdTotalItem.setValue(totalAmt.toString());
  OB.APRM.AddPayment.updateTotal(form);
  return true;
};

OB.APRM.AddPayment.selectionChanged = function (record, state) {
  var orderInvoice = this.view.theForm.getItem('order_invoice').canvas.viewGrid;
  if (!orderInvoice.preventDistributingOnSelectionChanged) {
    this.fireOnPause('updateButtonState', function () {
      OB.APRM.AddPayment.doSelectionChanged(record, state, this.view);
    }, 500);
    this.Super('selectionChanged', record, state);
  }
};


OB.APRM.AddPayment.doSelectionChanged = function (record, state, view) {
  var orderInvoice = view.theForm.getItem('order_invoice').canvas.viewGrid,
      amount = new BigDecimal(String(view.theForm.getItem('actual_payment').getValue() || 0)),
      distributedAmount = new BigDecimal(String(view.theForm.getItem('amount_inv_ords').getValue() || 0)),
      total, outstandingAmount, selectedIds, glitem, credit;

  selectedIds = orderInvoice.selectedIds;
  outstandingAmount = new BigDecimal(String(record.outstandingAmount));
  amount = amount.subtract(distributedAmount);
  //amounts de gl items
  glitem = new BigDecimal(String(view.theForm.getItem('amount_gl_items').getValue() || 0));
  amount = amount.add(glitem);

  //amount de credito
  credit = new BigDecimal(String(view.theForm.getItem('used_credit').getValue() || 0));
  amount = amount.add(credit);
  if (amount.signum() !== 0 && state) {
    if ((outstandingAmount.compareTo(new BigDecimal("0")) < 0) && (amount.compareTo(new BigDecimal("0")) < 0)) {
      if (Math.abs(outstandingAmount) > Math.abs(amount)) {
        outstandingAmount = amount;
      }
    } else {
      if (outstandingAmount.compareTo(amount) > 0) {
        outstandingAmount = amount;
      }
    }
    if (amount.signum() === 0) {
      orderInvoice.setEditValue(orderInvoice.getRecordIndex(record), 'amount', '');

    } else {
      orderInvoice.setEditValue(orderInvoice.getRecordIndex(record), 'amount', outstandingAmount.toString());
      orderInvoice.selection[orderInvoice.getRecordIndex(record)] = true;
    }
  }
  OB.APRM.AddPayment.updateInvOrderTotal(view.theForm, orderInvoice);

};

OB.APRM.AddPayment.updateGLItemsTotal = function (form) {
  var amt, i, bdAmt, totalAmt = BigDecimal.prototype.ZERO,
      grid = form.getItem('glitem').canvas.viewGrid,
      receivedInField = grid.getFieldByColumnName('received_in'),
      paidOutField = grid.getFieldByColumnName('paid_out'),
      selectedRecords = grid.getSelectedRecords(),
      glItemTotalItem = form.getItem('amount_gl_items'),
      issotrx = form.getItem('issotrx').getValue(),
      receivedInAmt, paidOutAmt;

  for (i = 0; i < selectedRecords.length; i++) {
    receivedInAmt = new BigDecimal(String(grid.getEditedCell(grid.getRecordIndex(selectedRecords[i]), receivedInField)));
    paidOutAmt = new BigDecimal(String(grid.getEditedCell(grid.getRecordIndex(selectedRecords[i]), paidOutField)));
    if (issotrx) {
      totalAmt = totalAmt.add(receivedInAmt).subtract(paidOutAmt);
    } else {
      totalAmt = totalAmt.add(paidOutAmt).subtract(receivedInAmt);
    }
  }
  glItemTotalItem.setValue(totalAmt.toString());
  OB.APRM.AddPayment.updateTotal(form);
  return true;
};


OB.APRM.AddPayment.updateCreditTotal = function (form) {
  var amt, i, bdAmt, totalAmt = BigDecimal.prototype.ZERO,
      grid = form.getItem('credit_to_use').canvas.viewGrid,
      amountField = grid.getFieldByColumnName('paymentAmount'),
      selectedRecords = grid.getSelectedRecords(),
      creditTotalItem = form.getItem('used_credit'),
      creditAmt;


  for (i = 0; i < selectedRecords.length; i++) {
    creditAmt = new BigDecimal(String(grid.getEditedCell(grid.getRecordIndex(selectedRecords[i]), amountField)));
    totalAmt = totalAmt.add(creditAmt);
  }
  creditTotalItem.setValue(totalAmt.toString());
  OB.APRM.AddPayment.updateTotal(form);
  return true;
};

OB.APRM.AddPayment.selectionChangedGlitem = function (record, state) {
  var glitem = this.view.theForm.getItem('glitem').canvas.viewGrid;
  if (!glitem.preventDistributingOnSelectionChanged) {
    this.fireOnPause('updateButtonState', function () {
      OB.APRM.AddPayment.doSelectionChangedGLitem(record, state, this.view);
    }, 500);
    this.Super('selectionChangedGlitem', record, state);
  }

};

OB.APRM.AddPayment.doSelectionChangedGLitem = function (record, state, view) {
  OB.APRM.AddPayment.updateGLItemsTotal(view.theForm);
};


OB.APRM.AddPayment.selectionChangedCredit = function (record, state) {

  var credit = this.view.theForm.getItem('credit_to_use').canvas.viewGrid;
  if (!credit.preventDistributingOnSelectionChanged) {
    this.fireOnPause('updateButtonState', function () {
      OB.APRM.AddPayment.doSelectionChangedCredit(record, state, this.view);
    }, 500);
    this.Super('selectionChangedCredit', record, state);
  }

};

OB.APRM.AddPayment.doSelectionChangedCredit = function (record, state, view) {
  OB.APRM.AddPayment.updateCreditTotal(view.theForm);
};