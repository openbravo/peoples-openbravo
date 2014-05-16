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

OB.APRM.AddPayment.onLoad = function(view) {
    OB.APRM.paymentMethodOnLoadFunction(view);
};

OB.APRM.AddPayment.addNewGLItem = function(grid) {
    var selectedRecord = grid.view.parentWindow.views[0].getParentRecord();
    var returnObject = isc.addProperties({}, grid.data[0]);
    returnObject.organization = selectedRecord.organization;
    return returnObject;
};

OB.APRM.AddPayment.paymentMethodOnChangeFunction = function(item, view, form, grid) {
    var paymentMethodId = item.getValue(),
        callback, isPayinIsMulticurrency;
    callback = function(response, data, request) {
        isPayinIsMulticurrency = data.isPayinIsMulticurrency;
        if (isPayinIsMulticurrency) {
            form.getItem('c_currency_to_id').visible = true;
            form.redraw();
        } else {
            form.getItem('c_currency_to_id').visible = false;
            form.getItem('c_currency_to_id').setValue(form.getItem('c_currency_id').getValue());
            form.redraw();
        }
    };
    OB.RemoteCallManager.call('org.openbravo.advpaymentmngt.actionHandler.PaymentMethodMulticurrencyActionHandler', {
        paymentMethodId: paymentMethodId
    }, {}, callback);
};

OB.APRM.AddPayment.paymentMethodOnLoadFunction = function(view) {
    var paymentMethodId = view.theForm.getItem('fin_paymentmethod_id').getValue(),
        callback, isPayinIsMulticurrency;
    callback = function(response, data, request) {
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

OB.APRM.AddPayment.transactionTypeOnChangeFunction = function(item, view, form, grid) {
    form.getItem('order_invoice').canvas.viewGrid.invalidateCache();
    form.getItem('order_invoice').canvas.viewGrid.fetchData(form.getItem('order_invoice').canvas.viewGrid.getCriteria());
    form.redraw();
};

OB.APRM.AddPayment.distributeAmount = function(view) {
    var actualPayment = view.theForm.getItem('actual_payment').getValue(),
        distributedAmount = 0,
        keepSelection = false,
        chk=view.theForm.getItem('order_invoice').canvas.viewGrid.selectedIds, scheduledPaymentDetailId, outstandingAmount, j, i,
        isGLItemEnabled=view.theForm.getItem('glitem').canvas.viewGrid.isVisible() ;
    if (isGLItemEnabled) {
        actualPayment = actualPayment - view.theForm.getItem('amount_gl_items').getValue();
    }
    
    
    
    OB.APRM.AddPayment.updateTotal();
    return true;

};


OB.APRM.AddPayment.updateTotal = function() {
	
};