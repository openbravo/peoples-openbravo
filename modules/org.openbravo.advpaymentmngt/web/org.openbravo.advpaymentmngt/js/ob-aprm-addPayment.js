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
    var i, selectedRecords = this.getSelectedRecords();
    this.Super('dataArrived', arguments);
    for (i = 0; i < selectedRecords; i++) {
      this.setEditValues(this.getRecordIndex(selectedRecords[i]), selectedRecords[i]);
    }
  },
  ordInvTransformData: function (newData, dsResponse) {
    var i, j, record, data, ids, invno, ordno, grid, idsArray, editedRecord, isSelected, selectedRecord, curAmount, curOutstandingAmt, curPending, availableAmt, checkContainsAny = function (arrayBase, arrayCompare) {
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
      grid.data.savedData = [];
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
        idsArray = ids.replaceAll(' ', '').split(',');
        isSelected = false;
        editedRecord = isc.addProperties({}, record);

        curAmount = isc.isA.Number(editedRecord.amount) ? new BigDecimal(String(editedRecord.amount)) : new BigDecimal('0');
        curOutstandingAmt = isc.isA.Number(editedRecord.outstandingAmount) ? new BigDecimal(String(editedRecord.outstandingAmount)) : new BigDecimal('0');
        for (j = 0; j < grid.editedSelectedRecords.length; j++) {
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
          grid.data.savedData.push(editedRecord);
          //record = editedRecord;
        }
      }
    }
    grid.changedTrxType = false;
    return data;
  }
};

OB.APRM.AddPayment.onLoad = function (view) {
  var form = view.theForm,
      orderInvoiceGrid = form.getItem('order_invoice').canvas.viewGrid,
      glitemGrid = form.getItem('glitem').canvas.viewGrid,
      creditUseGrid = form.getItem('credit_to_use').canvas.viewGrid,
      overpaymentAction = form.getItem('overpayment_action'),
      payment = form.getItem('fin_payment_id').getValue();

  OB.APRM.AddPayment.paymentMethodMulticurrency(view, view.theForm, !payment);
  glitemGrid.fetchData();
  creditUseGrid.fetchData();
  orderInvoiceGrid.selectionChanged = OB.APRM.AddPayment.selectionChanged;
  orderInvoiceGrid.dataProperties.transformData = OB.APRM.AddPayment.ordInvTransformData;
  glitemGrid.removeRecordClick = OB.APRM.AddPayment.removeRecordClick;
  creditUseGrid.selectionChanged = OB.APRM.AddPayment.selectionChangedCredit;
  orderInvoiceGrid.dataArrived = OB.APRM.AddPayment.ordInvDataArrived;

  form.isCreditAllowed = form.getItem('received_from').getValue() !== undefined;
  OB.APRM.AddPayment.checkSingleActionAvailable(form);
  overpaymentAction.originalValueMap = isc.addProperties({}, overpaymentAction.getValueMap());

};

OB.APRM.AddPayment.addNewGLItem = function (grid) {
  var selectedRecord = grid.view.parentWindow.views[0].getParentRecord();
  var returnObject = isc.addProperties({}, grid.data[0]);
  return returnObject;
};

OB.APRM.AddPayment.paymentMethodMulticurrency = function (view, form, recalcConvRate) {
  var callback, financialAccountId = form.getItem('fin_financial_account_id').getValue(),
      paymentMethodId = form.getItem('fin_paymentmethod_id').getValue(),
      isSOTrx = form.getItem('issotrx').getValue(),
      currencyId = form.getItem('c_currency_id').getValue(),
      paymentDate = form.getItem('payment_date').getValue(),
      orgId = form.getItem('ad_org_id').getValue();

  callback = function (response, data, request) {
    var isPayIsMulticurrency = data.isPayIsMulticurrency,
        isWrongFinancialAccount = data.isWrongFinancialAccount;
    if (isWrongFinancialAccount) {
      form.getItem('fin_financial_account_id').setValue('');
    } else {
      form.getItem('c_currency_to_id').setValue(data.currencyToId);
      if (recalcConvRate && isc.isA.Number(data.conversionrate)) {
        form.getItem('conversion_rate').setValue(Number(data.conversionrate));
        OB.APRM.AddPayment.updateConvertedAmount(view, form, false);
      }
    }
    if (isPayIsMulticurrency && currencyId !== data.currencyToId) {
      form.getItem('conversion_rate').visible = true;
      form.getItem('converted_amount').visible = true;
      form.getItem('c_currency_to_id').visible = true;
    } else {
      form.getItem('c_currency_to_id').visible = false;
      form.getItem('conversion_rate').visible = false;
      form.getItem('converted_amount').visible = false;
    }
    form.redraw();
  };

  OB.RemoteCallManager.call('org.openbravo.advpaymentmngt.actionHandler.PaymentMethodMulticurrencyActionHandler', {
    paymentMethodId: paymentMethodId,
    currencyId: currencyId,
    isSOTrx: isSOTrx,
    financialAccountId: financialAccountId,
    paymentDate: paymentDate,
    orgId: orgId
  }, {}, callback);
};

OB.APRM.AddPayment.checkSingleActionAvailable = function (form) {
  var documentAction = form.getItem('document_action');
  documentAction.fetchData(function (item, dsResponse, data, dsRequest) {
    if (dsResponse.totalRows === 1) {
      item.setValueFromRecord(data[0]);
    } else {
      item.clearValue();
    }
  });
};

OB.APRM.AddPayment.financialAccountOnChange = function (item, view, form, grid) {
  OB.APRM.AddPayment.paymentMethodMulticurrency(view, form, true);
  OB.APRM.AddPayment.checkSingleActionAvailable(form);
};

OB.APRM.AddPayment.paymentMethodOnChange = function (item, view, form, grid) {
  OB.APRM.AddPayment.paymentMethodMulticurrency(view, form, true);
  OB.APRM.AddPayment.checkSingleActionAvailable(form);
};

OB.APRM.AddPayment.transactionTypeOnChangeFunction = function (item, view, form, grid) {
  var ordinvgrid = form.getItem('order_invoice').canvas.viewGrid,
      selectedRecords = ordinvgrid.getSelectedRecords(),
      editedRecord, i, editedSelectedRecords = [],
      newCriteria;

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

  // fetch data after change trx type, filters should be preserved and ids of
  // the selected records should be sent
  newCriteria = ordinvgrid.addSelectedIDsToCriteria(ordinvgrid.getCriteria(), true);
  newCriteria.criteria = newCriteria.criteria || [];
  // add dummy criterion to force fetch
  newCriteria.criteria.push(isc.OBRestDataSource.getDummyCriterion());
  ordinvgrid.fetchData(newCriteria);

  form.redraw();
};

OB.APRM.AddPayment.actualPaymentOnChange = function (item, view, form, grid) {
  var issotrx = form.getItem('issotrx').getValue();
  if (issotrx) {
    OB.APRM.AddPayment.distributeAmount(view, form, true);
    OB.APRM.AddPayment.updateConvertedAmount(view, form, false);
  }
};

OB.APRM.AddPayment.orderInvoiceOnLoadGrid = function (grid) {
  var issotrx = this.view.theForm.getItem('issotrx').getValue(),
      payment = this.view.theForm.getItem('fin_payment_id').getValue();
  grid.isReady = true;
  if (!issotrx && payment) {
    OB.APRM.AddPayment.updateInvOrderTotal(this.view.theForm, grid);
  } else {
    OB.APRM.AddPayment.distributeAmount(this.view, this.view.theForm, false);
  }
  OB.APRM.AddPayment.tryToUpdateActualExpected(this.view.theForm);
};

OB.APRM.AddPayment.glitemsOnLoadGrid = function (grid) {
  grid.isReady = true;
  OB.APRM.AddPayment.updateGLItemsTotal(this.view.theForm, 0, false);
  OB.APRM.AddPayment.tryToUpdateActualExpected(this.view.theForm);
};

OB.APRM.AddPayment.creditOnLoadGrid = function (grid) {
  grid.isReady = true;
  OB.APRM.AddPayment.updateCreditTotal(this.view.theForm);
  OB.APRM.AddPayment.tryToUpdateActualExpected(this.view.theForm);
};


OB.APRM.AddPayment.tryToUpdateActualExpected = function (form) {
  var orderInvoiceGrid = form.getItem('order_invoice').canvas.viewGrid,
      glitemGrid = form.getItem('glitem').canvas.viewGrid,
      creditGrid = form.getItem('credit_to_use').canvas.viewGrid;

  if (orderInvoiceGrid.isReady && glitemGrid.isReady && creditGrid.isReady) {
    OB.APRM.AddPayment.updateActualExpected(form);
  }
};

OB.APRM.AddPayment.orderInvoiceAmountOnChange = function (item, view, form, grid) {
  OB.APRM.AddPayment.updateActualExpected(form);
  OB.APRM.AddPayment.updateInvOrderTotal(form, grid);
  return true;
};

OB.APRM.AddPayment.orderInvoiceTotalAmountOnChange = function (item, view, form, grid) {
  OB.APRM.AddPayment.updateActualExpected(form);
  OB.APRM.AddPayment.updateTotal(form);

  return true;
};

OB.APRM.AddPayment.glItemTotalAmountOnChange = function (item, view, form, grid) {
  OB.APRM.AddPayment.updateActualExpected(form);
  OB.APRM.AddPayment.updateTotal(form);
  return true;
};

OB.APRM.AddPayment.distributeAmount = function (view, form, onActualPaymentChange) {
  var amount = new BigDecimal(String(form.getItem('actual_payment').getValue() || 0)),
      distributedAmount = new BigDecimal("0"),
      orderInvoice = form.getItem('order_invoice').canvas.viewGrid,
      scheduledPaymentDetailId, outstandingAmount, j, i, total, chk, credit, glitem, difference = new BigDecimal(String(form.getItem('difference').getValue() || 0)),
      writeoff, issotrx = form.getItem('issotrx').getValue(),
      amt, negativeamt = BigDecimal.prototype.ZERO,
      differenceamt = BigDecimal.prototype.ZERO,
      payment = form.getItem('fin_payment_id').getValue();

  // glitems amount
  glitem = new BigDecimal(String(form.getItem('amount_gl_items').getValue() || 0));
  amount = amount.subtract(glitem);

  // credit amount
  credit = new BigDecimal(String(form.getItem('used_credit').getValue() || 0));
  amount = amount.add(credit);

  chk = orderInvoice.selectedIds;
  total = orderInvoice.data.totalRows;

  if ((amount.compareTo(distributedAmount) > 0) || (amount.equals(distributedAmount))) {
    amount = amount.subtract(distributedAmount);
  }

  for (i = 0; i < total; i++) {
    outstandingAmount = new BigDecimal(String(orderInvoice.getRecord(i).outstandingAmount));
    if (outstandingAmount.signum() < 0) {
      negativeamt = negativeamt.add(new BigDecimal(Math.abs(outstandingAmount).toString()));
    }
  }

  if (amount.compareTo(negativeamt.negate()) > 0 && (onActualPaymentChange || payment)) {
    amount = amount.add(negativeamt);
  }

  for (i = 0; i < total; i++) {
    writeoff = orderInvoice.getEditValues(i).writeoff;
    amt = new BigDecimal(String(orderInvoice.getEditValues(i).amount || 0));
    if (writeoff === null || writeoff === undefined) {
      writeoff = orderInvoice.getRecord(i).writeoff;
      amt = new BigDecimal(String(orderInvoice.getRecord(i).amount || 0));
    }
    if (writeoff && issotrx) {
      amount = amount.subtract(amt);
      continue;
    } else {
      outstandingAmount = new BigDecimal(String(orderInvoice.getRecord(i).outstandingAmount));
      if (payment && !onActualPaymentChange && orderInvoice.getRecord(i).obSelected) {
        outstandingAmount = new BigDecimal(String(orderInvoice.getRecord(i).amount));
      } else if ((outstandingAmount.compareTo(new BigDecimal("0")) < 0) && (amount.compareTo(new BigDecimal("0")) < 0)) {
        if (Math.abs(outstandingAmount) > Math.abs(amount)) {
          differenceamt = outstandingAmount.subtract(amount);
          outstandingAmount = amount;
          amount = amount.subtract(differenceamt);
        }
      } else {
        if (outstandingAmount.signum() > -1 && amount.signum() > -1 && outstandingAmount.compareTo(amount) > 0) {
          outstandingAmount = amount;
        }
      }
      // do not distribute again when the selectionChanged method is invoked
      orderInvoice.preventDistributingOnSelectionChanged = true;
      if (amount.signum() === 0) {
        if (outstandingAmount.signum() < 0 && (onActualPaymentChange || payment)) {
          orderInvoice.setEditValue((i), 'amount', Number(outstandingAmount.toString()));
          orderInvoice.selectRecord(i);
        } else {
          orderInvoice.setEditValue((i), 'amount', Number("0"));
          orderInvoice.deselectRecord(i);
        }
      } else if (amount.signum() === 1) {
        orderInvoice.setEditValue((i), 'amount', Number(outstandingAmount.toString()));
        orderInvoice.selectRecord(i);
        if (outstandingAmount.signum() >= 0 || amount.signum() <= 0) {
          amount = amount.subtract(outstandingAmount);
        }
      } else {
        if (outstandingAmount.signum() < 0) {
          orderInvoice.setEditValue((i), 'amount', Number(outstandingAmount.toString()));
          orderInvoice.selectRecord(i);
        } else {
          orderInvoice.setEditValue((i), 'amount', Number("0"));
          orderInvoice.deselectRecord(i);
        }
      }
      delete orderInvoice.preventDistributingOnSelectionChanged;
    }
  }
  OB.APRM.AddPayment.updateActualExpected(form);
  OB.APRM.AddPayment.updateInvOrderTotal(form, orderInvoice);
  return true;

};


OB.APRM.AddPayment.updateTotal = function (form) {
  var invOrdTotalItem = form.getItem('amount_inv_ords'),
      glItemsTotalItem = form.getItem('amount_gl_items'),
      totalItem = form.getItem('total'),
      totalAmt, glAmt;

  totalAmt = new BigDecimal(String(invOrdTotalItem.getValue() || 0));
  totalAmt = totalAmt.add(new BigDecimal(String(glItemsTotalItem.getValue() || 0)));

  totalItem.setValue(Number(totalAmt.toString()));
  OB.APRM.AddPayment.updateDifference(form);
};

OB.APRM.AddPayment.updateDifference = function (form) {
  var total = new BigDecimal(String(form.getItem('total').getValue() || 0)),
      actualPayment = new BigDecimal(String(form.getItem('actual_payment').getValue() || 0)),
      expectedPayment = new BigDecimal(String(form.getItem('expected_payment').getValue() || 0)),
      credit = new BigDecimal(String(form.getItem('used_credit').getValue() || 0)),
      differenceItem = form.getItem('difference'),
      expectedDifferenceItem = form.getItem('expectedDifference'),
      paymentId = form.getItem('fin_payment_id').getValue() || '',
      receivedFrom = form.getItem('received_from').getValue() || '',
      totalGLItems = new BigDecimal(String(form.getItem('amount_gl_items').getValue() || 0)),
      diffAmt = actualPayment.add(credit).subtract(total),
      expectedDiffAmt = expectedPayment.add(credit).subtract(total).add(totalGLItems);
  differenceItem.setValue(Number(diffAmt.toString()));
  if (expectedDiffAmt.signum() === 0) {
    expectedDifferenceItem.setValue(Number(diffAmt.toString()));
  } else {
    expectedDifferenceItem.setValue(Number(expectedDiffAmt.toString()));
  }


  if (diffAmt.signum() !== 0) {
    OB.APRM.AddPayment.updateDifferenceActions(form);
  }
};

OB.APRM.AddPayment.updateDifferenceActions = function (form) {
  var issotrx = form.getItem('issotrx').getValue(),
      overpaymentAction = form.getItem('overpayment_action'),
      actualPayment = new BigDecimal(String(form.getItem('actual_payment').getValue() || 0)),
      newValueMap = {},
      defaultValue = '',
      overpaymentVM;
  // Update difference action available values.
  if (form.isCreditAllowed) {
    newValueMap.CR = overpaymentAction.originalValueMap.CR;
    if (issotrx || actualPayment.signum() === 0) {
      // On payment outs allow refund of credit (when actual payment is zero and something is being paid).
      newValueMap.RE = overpaymentAction.originalValueMap.RE;
    } else {
      defaultValue = 'CR';
    }
  }
  overpaymentAction.setValueMap(newValueMap);
  overpaymentAction.setValue(defaultValue);
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
  invOrdTotalItem.setValue(Number(totalAmt.toString()));
  OB.APRM.AddPayment.updateTotal(form);
  return true;
};

OB.APRM.AddPayment.selectionChanged = function (record, state) {
  var orderInvoice = this.view.theForm.getItem('order_invoice').canvas.viewGrid;
  if (!orderInvoice.preventDistributingOnSelectionChanged) {
    this.fireOnPause('updateButtonState' + record.id, function () {
      OB.APRM.AddPayment.doSelectionChanged(record, state, this.view);
    }, 200);
    this.Super('selectionChanged', record, state);
  }
};


OB.APRM.AddPayment.doSelectionChanged = function (record, state, view) {
  var orderInvoice = view.theForm.getItem('order_invoice').canvas.viewGrid,
      amount = new BigDecimal(String(view.theForm.getItem('actual_payment').getValue() || 0)),
      distributedAmount = new BigDecimal(String(view.theForm.getItem('amount_inv_ords').getValue() || 0)),
      total, outstandingAmount, selectedIds, glitem, credit, actualPaymentAmount, expectedPaymentAmount, totalAmount, i, issotrx = view.theForm.getItem('issotrx').getValue();

  selectedIds = orderInvoice.selectedIds;
  outstandingAmount = new BigDecimal(String(record.outstandingAmount));
  amount = amount.subtract(distributedAmount);
  // glitems amount
  glitem = new BigDecimal(String(view.theForm.getItem('amount_gl_items').getValue() || 0));
  amount = amount.add(glitem);

  // credit amount
  credit = new BigDecimal(String(view.theForm.getItem('used_credit').getValue() || 0));
  amount = amount.add(credit);

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
        orderInvoice.setEditValue(orderInvoice.getRecordIndex(record), 'amount', Number("0"));

      } else {
        orderInvoice.setEditValue(orderInvoice.getRecordIndex(record), 'amount', Number(outstandingAmount.toString()));
      }
    }

  } else {
    for (i = 0; i < selectedIds.length; i++) {
      if (selectedIds[i] === record.id) {
        orderInvoice.setEditValue(orderInvoice.getRecordIndex(record), 'amount', Number(outstandingAmount.toString()));
      }
    }
  }
  OB.APRM.AddPayment.updateInvOrderTotal(view.theForm, orderInvoice);
  OB.APRM.AddPayment.updateActualExpected(view.theForm);
  OB.APRM.AddPayment.updateDifference(view.theForm);
};

OB.APRM.AddPayment.updateActualExpected = function (form) {
  var orderInvoice = form.getItem('order_invoice').canvas.viewGrid,
      issotrx = form.getItem('issotrx').getValue(),
      totalAmountoutstanding = BigDecimal.prototype.ZERO,
      difference = new BigDecimal(String(form.getItem('difference').getValue() || 0)),
      totalAmount = BigDecimal.prototype.ZERO,
      selectedRecords, actualPayment = form.getItem('actual_payment'),
      actualPaymentAmt = new BigDecimal(String(form.getItem('actual_payment').getValue() || 0)),
      expectedPayment = form.getItem('expected_payment'),
      generateCredit = new BigDecimal(String(form.getItem('generateCredit').getValue() || 0)),
      glitemtotal = new BigDecimal(String(form.getItem('amount_gl_items').getValue() || 0)),
      credit = new BigDecimal(String(form.getItem('used_credit').getValue() || 0)),
      actpayment, i;
  selectedRecords = orderInvoice.getSelectedRecords();
  for (i = 0; i < selectedRecords.length; i++) {
    totalAmountoutstanding = totalAmountoutstanding.add(new BigDecimal(String(orderInvoice.getEditedCell(orderInvoice.getRecordIndex(selectedRecords[i]), orderInvoice.getFieldByColumnName('outstandingAmount')))));
    totalAmount = totalAmount.add(new BigDecimal(String(orderInvoice.getEditedCell(orderInvoice.getRecordIndex(selectedRecords[i]), orderInvoice.getFieldByColumnName('amount')))));
  }
  if (selectedRecords.length > 0) {
    expectedPayment.setValue(Number(totalAmountoutstanding));
  } else {
    expectedPayment.setValue(Number('0'));
  }
  if (!issotrx) {
    actpayment = totalAmount.add(glitemtotal).add(generateCredit);
    actualPayment.setValue(Number(actpayment));
    if (credit.compareTo(BigDecimal.prototype.ZERO) > 0) {
      if (credit.compareTo(actpayment) > 0) {
        actualPayment.setValue(Number('0'));
      } else {
        actualPayment.setValue(Number(actpayment.subtract(credit)));
      }
    }
    OB.APRM.AddPayment.updateDifference(form);
    OB.APRM.AddPayment.updateConvertedAmount(null, form, false);
  }

  // force redraw to ensure display logic is properly executed
  form.redraw();
};

OB.APRM.AddPayment.removeRecordClick = function (rowNum, record) {
  var grid = this.view.theForm.getItem('glitem').canvas.viewGrid,
      glItemTotalItem = this.view.theForm.getItem('amount_gl_items'),
      receivedInField = grid.getFieldByColumnName('received_in'),
      paidOutField = grid.getFieldByColumnName('paid_out'),
      issotrx = this.view.theForm.getItem('issotrx').getValue();

  this.Super('removeRecordClick', rowNum, record);

  OB.APRM.AddPayment.updateGLItemsTotal(this.view.theForm, rowNum, true);

};

OB.APRM.AddPayment.updateGLItemsTotal = function (form, rowNum, remove) {
  var amt, i, bdAmt, totalAmt = BigDecimal.prototype.ZERO,
      grid = form.getItem('glitem').canvas.viewGrid,
      receivedInField = grid.getFieldByColumnName('received_in'),
      paidOutField = grid.getFieldByColumnName('paid_out'),
      allRecords, glItemTotalItem = form.getItem('amount_gl_items'),
      issotrx = form.getItem('issotrx').getValue(),
      receivedInAmt, paidOutAmt;

  grid.saveAllEdits();

  allRecords = grid.data.allRows.length || 0;
  for (i = 0; i < allRecords; i++) {
    if (!(remove && i === rowNum)) {
      receivedInAmt = new BigDecimal(String(grid.getEditedCell(i, receivedInField) || 0));
      paidOutAmt = new BigDecimal(String(grid.getEditedCell(i, paidOutField) || 0));

      if (issotrx) {
        totalAmt = totalAmt.add(receivedInAmt);
        totalAmt = totalAmt.subtract(paidOutAmt);

      } else {
        totalAmt = totalAmt.subtract(receivedInAmt);
        totalAmt = totalAmt.add(paidOutAmt);
      }
    }
  }
  if (allRecords === 0) {
    totalAmt = BigDecimal.prototype.ZERO;
  }

  glItemTotalItem.setValue(Number(totalAmt.toString()));
  OB.APRM.AddPayment.updateTotal(form);
  return true;
};

OB.APRM.AddPayment.glItemAmountOnChange = function (item, view, form, grid) {
  var receivedInField = grid.getFieldByColumnName('received_in'),
      paidOutField = grid.getFieldByColumnName('paid_out'),
      receivedInAmt, paidOutAmt;
  receivedInAmt = new BigDecimal(String(grid.getEditedCell(item.rowNum, receivedInField) || 0));
  paidOutAmt = new BigDecimal(String(grid.getEditedCell(item.rowNum, paidOutField) || 0));
  if (item.columnName === "received_in" && receivedInAmt.signum() !== 0) {
    grid.setEditValue(item.rowNum, 'paidOut', Number('0'));

  } else if (item.columnName === "paid_out" && paidOutAmt.signum() !== 0) {
    grid.setEditValue(item.rowNum, 'receivedIn', Number('0'));
  }

  OB.APRM.AddPayment.updateGLItemsTotal(form, item.rowNum, false);
  OB.APRM.AddPayment.updateActualExpected(form);
  OB.APRM.AddPayment.updateDifference(form);
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
  creditTotalItem.setValue(Number(totalAmt.toString()));
  OB.APRM.AddPayment.updateTotal(form);
  return true;
};

OB.APRM.AddPayment.updateCreditOnChange = function (item, view, form, grid) {
  var issotrx = form.getItem('issotrx').getValue();
  OB.APRM.AddPayment.updateCreditTotal(form);
  if (issotrx) {
    OB.APRM.AddPayment.distributeAmount(view, form, true);
  }
  OB.APRM.AddPayment.updateDifference(form);
  return true;
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


OB.APRM.AddPayment.orderInvoiceGridValidation = function (item, validator, value, record) {
  if (!isc.isA.Number(record.amount)) {
    item.grid.view.messageBar.setMessage(isc.OBMessageBar.TYPE_ERROR, null, OB.I18N.getLabel('APRM_NotValidNumber'));
    return false;
  }
  var i, row, allRows = item.grid.data.allRows,
      outstanding = new BigDecimal(String(record.outstandingAmount)),
      paidamount = new BigDecimal(String(record.amount));

  if (outstanding.abs().compareTo(paidamount.abs()) < 0) {
    item.grid.view.messageBar.setMessage(isc.OBMessageBar.TYPE_ERROR, null, OB.I18N.getLabel('APRM_MoreAmountThanOutstanding'));
    return false;
  }

  if ((paidamount.signum() === 0) && (record.writeoff === false)) {
    item.grid.view.messageBar.setMessage(isc.OBMessageBar.TYPE_ERROR, null, OB.I18N.getLabel('APRM_JSZEROUNDERPAYMENT'));
    return false;
  }

  if ((paidamount.signum() < 0 && outstanding.signum() > 0) || (paidamount.signum() > 0 && outstanding.signum() < 0)) {
    item.grid.view.messageBar.setMessage(isc.OBMessageBar.TYPE_ERROR, null, OB.I18N.getLabel('APRM_ValueOutOfRange'));
    return false;
  }
  return true;
};


OB.APRM.AddPayment.creditValidation = function (item, validator, value, record) {
  if (!isc.isA.Number(record.paymentAmount)) {
    item.grid.view.messageBar.setMessage(isc.OBMessageBar.TYPE_ERROR, null, OB.I18N.getLabel('APRM_NotValidNumber'));

    return false;
  }
  var i, row, allRows = item.grid.data.allRows,
      outstanding = new BigDecimal(String(record.outstandingAmount)),
      paidamount = new BigDecimal(String(record.paymentAmount));

  if (outstanding.abs().compareTo(paidamount.abs()) < 0) {
    item.grid.view.messageBar.setMessage(isc.OBMessageBar.TYPE_ERROR, null, OB.I18N.getLabel('APRM_MoreAmountThanOutstanding'));
    return false;
  }

  if (paidamount.signum() === 0) {
    item.grid.view.messageBar.setMessage(isc.OBMessageBar.TYPE_ERROR, null, OB.I18N.getLabel('aprm_biggerthanzero'));
    return false;
  }
  return true;
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
          newExchangeRate = actualConverted.divide(actualPayment, 15, 2);
          exchangeRateItem.setValue(Number(newExchangeRate.toString()));
        }
      } else {
        exchangeRateItem.setValue(Number(newExchangeRate.toString));
      }
    } else {
      if (exchangeRate) {
        newConvertedAmount = actualPayment.multiply(exchangeRate).setScale(currencyPrecision, BigDecimal.prototype.ROUND_HALF_UP);
        exchangeRateItem.setValue(Number(exchangeRate.toString()));
        actualConvertedItem.setValue(Number(newConvertedAmount.toString()));
      } else {
        actualConvertedItem.setValue(Number(actualConverted.toString()));
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

  valArray = valArray.filter(function (elem, pos, self) {
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
      total = new BigDecimal(String(view.theForm.getItem('total').getValue() || 0)),
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
      amount, outstandingAmount, i, callbackOnProcessActionHandler, writeoff;

  // Check if there is pending amount to distribute that could be distributed
  for (i = 0; i < selectedRecords.length; i++) {
    amount = new BigDecimal(String(orderInvoiceGrid.getEditedCell(orderInvoiceGrid.getRecordIndex(selectedRecords[i]), amountField)));
    outstandingAmount = new BigDecimal(String(orderInvoiceGrid.getRecord(i).outstandingAmount));
    totalOustandingAmount = totalOustandingAmount.add(outstandingAmount);
  }
  for (i = 0; i < orderInvoiceGrid.data.totalRows; i++) {
    writeoff = orderInvoiceGrid.getEditValues(i).writeoff;
    if (writeoff === null || writeoff === undefined) {
      writeoff = orderInvoiceGrid.getRecord(i).writeoff;
    }
    if (writeoff) {
      writeOffLineAmount = new BigDecimal(String(orderInvoiceGrid.getRecord(i).outstandingAmount || 0)).subtract(new BigDecimal(String(orderInvoiceGrid.getEditedRecord(i).amount || 0)));
      totalWriteOffAmount = totalWriteOffAmount.add(writeOffLineAmount);
    }
  }

  // If there is Overpayment check it exists a business partner
  if (overpaymentAction && receivedFrom === null) {
    view.messageBar.setMessage(isc.OBMessageBar.TYPE_ERROR, null, OB.I18N.getLabel('APRM_CreditWithoutBPartner'));
    return false;
  }

  if (actualPayment.compareTo(total) > 0 && totalOustandingAmount.compareTo(amountInvOrds.add(totalWriteOffAmount)) > 0) {
    // Not all the payment amount has been allocated
    view.messageBar.setMessage(isc.OBMessageBar.TYPE_ERROR, null, OB.I18N.getLabel('APRM_JSNOTALLAMOUTALLOCATED'));
    return false;
  } else if (total.compareTo(actualPayment.add(creditTotalItem)) > 0) {
    // More than available amount has been distributed
    view.messageBar.setMessage(isc.OBMessageBar.TYPE_ERROR, null, OB.I18N.getLabel('APRM_JSMOREAMOUTALLOCATED'));
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
        view.messageBar.setMessage(isc.OBMessageBar.TYPE_ERROR, null, OB.I18N.getLabel('APRM_NotAllowWriteOff'));
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