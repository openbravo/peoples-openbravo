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
OB.APRM.AddPayment = {
    ordInvDataArrived: function (startRow, endRow) {
      var i,
        selectedRecords = this.getSelectedRecords();
      this.Super('dataArrived', arguments);
      for (i = 0; i < selectedRecords; i++) {
        this.setEditValues(this.getRecordIndex(selectedRecords[i]), selectedRecords[i]);
      }
    },
    ordInvTransformData: function (newData, dsResponse) {
      var i, j, record, data, ids, invno, ordno, grid, idsArray,
        editedRecord, isSelected, selectedRecord,
        curAmount, curOutstandingAmt, curPending, availableAmt,
        checkContainsAny = function (arrayBase, arrayCompare) {
        var i;
        for (i = 0; i < arrayCompare.length; i++) {
          if (arrayBase.contains(arrayCompare[i].trim())) {
            return true;
          }
        }
        return false;
      };

      data = this.Super('transformData', arguments) || newData;
      if (this.dataSource.view.parameterName !== 'order_invoice') {
        return data;
      }
      grid = this.dataSource.view.viewGrid;
      if (grid.changedTrxType) {
        grid.selectedIds = [];
        grid.deselectedIds = [];
        //grid.data.savedData = [];
      }
      for (i = 0; i < data.length; i++) {
        record = data[i];
        ids = OB.APRM.AddPayment.orderAndRemoveDuplicates(record.id);
        record.id = ids;
        invno = OB.APRM.AddPayment.orderAndRemoveDuplicates(record.invoiceNo);
        record.invoiceNo = invno;
        ordno = OB.APRM.AddPayment.orderAndRemoveDuplicates(record.salesOrderNo);
        record.salesOrderNo = ordno;
        if (grid.changedTrxType && grid.editedSelectedRecords && grid.editedSelectedRecords.length >= 1) {
          idsArray = ids.replaceAll(' ','').split(',');
          isSelected = false;
          editedRecord = isc.addProperties({}, record);
          curAmount = new BigDecimal(String(editedRecord.amount));
          curOutstandingAmt = new BigDecimal(String(editedRecord.outstandingAmount));
          for (j = 0; j < grid.editedSelectedRecords.length; j ++) {
            selectedRecord = grid.editedSelectedRecords[j];

            if (checkContainsAny(idsArray, selectedRecord.ids)) {
              isSelected = true;
              curPending = curOutstandingAmt.subtract(curAmount);
              availableAmt = new BigDecimal(String(selectedRecord.amount));
              if (availableAmt.subtract(curPending).signum() === 1) {
                curAmount = BigDecimal.prototype.ZERO.add(curOutstandingAmt);
                selectedRecord.amount = Number(availableAmt.subtract(curPending).toString());
              } else {
                curAmount = curAmount.add(availableAmt);
                selectedRecord.amount = 0;
              }
              
              if (selectedRecord.Writeoff) {
                record.Writeoff = true;
              }
              record.obSelected = true;
            }
          }
          if (isSelected) {
            record.amount = Number(curAmount.toString());
            grid.selectedIds.push(record.id);
            //grid.data.savedData.push(editedRecord);
            //record = editedRecord;
          }
        }
      }
      grid.changedTrxType = false;
      return data;
    }
};

OB.APRM.AddPayment.onLoad = function (view) {
  var orderInvoiceGrid = view.theForm.getItem('order_invoice').canvas.viewGrid,
      glitemGrid = view.theForm.getItem('glitem').canvas.viewGrid,
      creditUseGrid = view.theForm.getItem('credit_to_use').canvas.viewGrid;

  OB.APRM.AddPayment.paymentMethodMulticurrency(null, view, null, null);
  OB.APRM.AddPayment.actualPaymentOnLoad(view);
  orderInvoiceGrid.selectionChanged = OB.APRM.AddPayment.selectionChanged;
  orderInvoiceGrid.dataProperties.transformData = OB.APRM.AddPayment.ordInvTransformData;
  orderInvoiceGrid.dataArrived = OB.APRM.AddPayment.ordInvDataArrived;
  glitemGrid.selectionChanged = OB.APRM.AddPayment.selectionChangedGlitem;
  creditUseGrid.selectionChanged = OB.APRM.AddPayment.selectionChangedCredit;
};

OB.APRM.AddPayment.addNewGLItem = function (grid) {
  var selectedRecord = grid.view.parentWindow.views[0].getParentRecord();
  var returnObject = isc.addProperties({}, grid.data[0]);
  return returnObject;
};

OB.APRM.AddPayment.paymentMethodMulticurrency = function (item, view, form, grid) {
  var paymentMethodId, callback, isPayinIsMulticurrency, _form;
  if (item) {
    paymentMethodId = item.getValue();
  } else {
    paymentMethodId = view.theForm.getItem('fin_paymentmethod_id').getValue();
  }

  if (!form) {
    _form = view.theForm;
  } else {
    _form = form;
  }

  callback = function (response, data, request) {
    isPayinIsMulticurrency = data.isPayinIsMulticurrency;
    if (isPayinIsMulticurrency) {
      if (_form.getItem('c_currency_id').getValue() !== _form.getItem('c_currency_to_id').getValue()) {
        _form.getItem('conversion_rate').visible = true;
        _form.getItem('converted_amount').visible = true;
        _form.getItem('c_currency_to_id').visible = true;
      } else {
        _form.getItem('conversion_rate').visible = false;
        _form.getItem('converted_amount').visible = false;
        _form.getItem('c_currency_to_id').visible = false;
      }
      _form.redraw();
    } else {
      _form.getItem('c_currency_to_id').visible = false;
      _form.getItem('conversion_rate').visible = false;
      _form.getItem('converted_amount').visible = false;
      _form.redraw();
    }
  };
  if (_form.getItem('fin_payment_id').getValue() !== null && _form.getItem('fin_payment_id').getValue() !== undefined && _form.getItem('fin_payment_id').getValue() !== '') {
    OB.RemoteCallManager.call('org.openbravo.advpaymentmngt.actionHandler.PaymentMethodMulticurrencyActionHandler', {
      paymentMethodId: paymentMethodId
    }, {}, callback);
  } else {
    _form.getItem('c_currency_to_id').visible = false;
    _form.getItem('conversion_rate').visible = false;
    _form.getItem('converted_amount').visible = false;
    _form.redraw();
  }
};

OB.APRM.AddPayment.transactionTypeOnChangeFunction = function (item, view, form, grid) {
  var ordinvgrid = form.getItem('order_invoice').canvas.viewGrid,
      selectedRecords = ordinvgrid.getSelectedRecords(),
      editedRecord, i,
      editedSelectedRecords = [];

  if (item.getValue() === item.oldSelectedValue) {
    // only fetch new data if the selected value has changed.
    return;
  }
  item.oldSelectedValue = item.getValue();
  // Load current selection values to redistribute amounts when new data is loaded.
  for (i = 0; i < selectedRecords.length; i++) {
    editedRecord = ordinvgrid.getEditedRecord(ordinvgrid.getRecordIndex(selectedRecords[i]));
    editedRecord.ids = selectedRecords[i].id.replaceAll(' ', '').split(",");
    editedSelectedRecords.push(editedRecord);
  }
  ordinvgrid.editedSelectedRecords = editedSelectedRecords;
  ordinvgrid.changedTrxType = true;
  ordinvgrid.invalidateCache();
//  ordinvgrid.fetchData(ordinvgrid.getCriteria());
  form.redraw();
};

OB.APRM.AddPayment.actualPaymentOnChange = function (item, view, form, grid) {
  var issotrx = form.getItem('issotrx').getValue();
  if (issotrx) {
    OB.APRM.AddPayment.distributeAmount(view, form);
    OB.APRM.AddPayment.updateConvertedAmount(view, form, false);
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
  OB.APRM.AddPayment.distributeAmount(view, form);

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
      invOrdTotalItem = form.getItem('amount_inv_ords'),
      recordAmount = {};

  for (i = 0; i < selectedRecords.length; i++) {
    amt = grid.getEditedCell(grid.getRecordIndex(selectedRecords[i]), amountField);
    bdAmt = new BigDecimal(String(amt));
    recordAmount.amount = bdAmt;
    recordAmount.ids = selectedRecords[i].id.split(",");
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
      total, outstandingAmount, selectedIds, glitem, credit, actualPayment, expectedPayment, actualPaymentAmount, expectedPaymentAmount;

  selectedIds = orderInvoice.selectedIds;
  outstandingAmount = new BigDecimal(String(record.outstandingAmount));
  amount = amount.subtract(distributedAmount);
  //amounts de gl items
  glitem = new BigDecimal(String(view.theForm.getItem('amount_gl_items').getValue() || 0));
  amount = amount.add(glitem);

  //amount de credito
  credit = new BigDecimal(String(view.theForm.getItem('used_credit').getValue() || 0));
  amount = amount.add(credit);
  var issotrx = view.theForm.getItem('issotrx').getValue();
  if (issotrx) {
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

      }
    }
  } else {
    orderInvoice.setEditValue(orderInvoice.getRecordIndex(record), 'amount', outstandingAmount.toString());
    actualPaymentAmount = new BigDecimal(String(view.theForm.getItem('actual_payment').getValue() || 0));
    actualPayment = view.theForm.getItem('actual_payment');
    actualPayment.setValue((actualPaymentAmount.add(outstandingAmount)).toString());

    expectedPaymentAmount = new BigDecimal(String(view.theForm.getItem('expected_payment').getValue() || 0));
    expectedPayment = view.theForm.getItem('expected_payment');
    expectedPayment.setValue((actualPaymentAmount.add(outstandingAmount)).toString());

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

OB.APRM.AddPayment.conversionRateOnChange = function (item, view, form, grid) {
  OB.APRM.AddPayment.updateConvertedAmount(view, form, false);
};

OB.APRM.AddPayment.convertedAmountOnChange = function (item, view, form, grid) {
  OB.APRM.AddPayment.updateConvertedAmount(view, form, true);
};

OB.APRM.AddPayment.updateConvertedAmount = function (view, form, recalcExchangeRate) {
  if (form.getItem('fin_payment_id').getValue() !== null && form.getItem('fin_payment_id').getValue() !== undefined && form.getItem('fin_payment_id').getValue() !== '') {
    var exchangeRate = new BigDecimal(String(form.getItem('conversion_rate').getValue() || 1)),
        expectedConverted = new BigDecimal(String(form.getItem('converted_amount').getValue() || 0)),
        actualConverted = new BigDecimal(String(form.getItem('converted_amount').getValue() || 0)),
        expectedPayment = new BigDecimal(String(form.getItem('expected_payment').getValue() || 0)),
        actualPayment = new BigDecimal(String(form.getItem('actual_payment').getValue() || 0)),
        actualConvertedItem = form.getItem('converted_amount'),
        exchangeRateItem = form.getItem('conversion_rate'),
        newConvertedAmount = BigDecimal.prototype.ZERO,
        newExchangeRate = BigDecimal.prototype.ONE,
        currencyPrecision = form.getItem('StdPrecision').getValue();

    if (actualConverted && exchangeRate) {
      if (recalcExchangeRate) {
        if (actualConverted && actualPayment) {
          if (actualPayment.compareTo(newConvertedAmount) !== 0) {
            newExchangeRate = actualConverted.divide(actualPayment, currencyPrecision, 2);
            exchangeRateItem.setValue(newExchangeRate.toString());
          }
        } else {
          exchangeRateItem.setValue(newExchangeRate.toString);
        }
      } else {
        if (exchangeRate) {
          newConvertedAmount = actualPayment.multiply(exchangeRate).setScale(currencyPrecision, BigDecimal.prototype.ROUND_HALF_UP);
          actualConvertedItem.setValue(newConvertedAmount.toString());
        } else {
          actualConvertedItem.setValue(actualConverted.toString());
        }
      }
    }
  }
};

/*
 * Retrieves a string of comma separated values and returns it ordered and with the duplicates removed.
 */
OB.APRM.AddPayment.orderAndRemoveDuplicates = function (val) {
  var valArray = val.replaceAll(' ', '').split(',').sort(),
    retVal, length;
  
  valArray = valArray.filter(function(elem, pos, self) {
    return self.indexOf(elem) === pos;
  });
  
  retVal = valArray.toString().replaceAll(',', ', ');
  return retVal;
};

OB.APRM.AddPayment.onProcess = function (view, actionHandlerCall) {
  var orderInvoiceGrid = view.theForm.getItem('order_invoice').canvas.viewGrid,
      receivedFrom = view.theForm.getItem('received_from').getValue(),
      issotrx = view.theForm.getItem('issotrx').getValue(),
      finFinancialAccount = view.theForm.getItem('fin_financial_account_id').getValue(),
      amountInvOrds = new BigDecimal(String(view.theForm.getItem('amount_inv_ords').getValue() || 0)),
      actualPayment = new BigDecimal(String(view.theForm.getItem('actual_payment').getValue() || 0)),
      overpaymentAction = view.theForm.getItem('overpayment_action').getValue(),
      creditTotalItem = new BigDecimal(String(view.theForm.getItem('used_credit').getValue() || 0)),
      amountField = orderInvoiceGrid.getFieldByColumnName('amount'),
      writeoffField = orderInvoiceGrid.getFieldByColumnName('writeoff'),
      selectedRecords = orderInvoiceGrid.getSelectedRecords(),
      writeOffLimitPreference = OB.PropertyStore.get('WriteOffLimitPreference', view.windowId),
      totalWriteOffAmount = BigDecimal.prototype.ZERO,
      writeOffLineAmount = BigDecimal.prototype.ZERO,
      totalOustandingAmount = BigDecimal.prototype.ZERO,
      amount, outstandingAmount, i, callbackOnProcessActionHandler;

  // Check if there is pending amount to distribute that could be distributed
  for (i = 0; i < selectedRecords.length; i++) {
    amount = new BigDecimal(String(orderInvoiceGrid.getEditedCell(orderInvoiceGrid.getRecordIndex(selectedRecords[i]), amountField)));
    outstandingAmount = new BigDecimal(String(orderInvoiceGrid.getRecord(i).outstandingAmount));
    totalOustandingAmount = totalOustandingAmount.add(outstandingAmount);
    if (orderInvoiceGrid.getEditedCell(orderInvoiceGrid.getRecordIndex(selectedRecords[i]), writeoffField)) {
      writeOffLineAmount = outstandingAmount.subtract(amount);
      totalWriteOffAmount = totalWriteOffAmount.add(writeOffLineAmount);
    }
  }

  // If there is Overpayment check it exists a business partner
  if (overpaymentAction !== null && receivedFrom === null) {
    view.messageBar.setMessage(isc.OBMessageBar.TYPE_ERROR, "Error", OB.I18N.getLabel('APRM_CreditWithoutBPartner'));
    return false;
  }

  actualPayment = actualPayment.add(creditTotalItem);
  if (actualPayment.compareTo(amountInvOrds) > 0 && totalOustandingAmount.compareTo(amountInvOrds) > 0) {
    // Not all the payment amount has been allocated
    view.messageBar.setMessage(isc.OBMessageBar.TYPE_ERROR, "Error", OB.I18N.getLabel('APRM_JSNOTALLAMOUTALLOCATED'));
    return false;
  }

  callbackOnProcessActionHandler = function (response, data, request) {
    //Check if there are blocked Business Partners
    if (data.message.severity === 'error') {
      view.messageBar.setMessage(isc.OBMessageBar.TYPE_ERROR, data.message.title, data.message.text);
      return false;
    }
    // Check if the write off limit has been exceeded
    if (writeOffLimitPreference === 'Y') {
      if (totalWriteOffAmount > data.writeofflimit) {
        view.messageBar.setMessage(isc.OBMessageBar.TYPE_ERROR, "Error", OB.I18N.getLabel('APRM_NotAllowWriteOff'));
        return false;
      }
    }
    actionHandlerCall(view);
  };

  OB.RemoteCallManager.call('org.openbravo.advpaymentmngt.actionHandler.AddPaymentOnProcessActionHandler', {
    issotrx: issotrx,
    receivedFrom: receivedFrom,
    selectedRecords: selectedRecords,
    finFinancialAccount: finFinancialAccount
  }, {}, callbackOnProcessActionHandler);
};