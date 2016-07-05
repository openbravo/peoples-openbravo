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
 * All portions are Copyright (C) 2014-2016 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

OB.APRM.FindTransactions = {};

OB.APRM.FindTransactions.onProcess = function (view, actionHandlerCall, clientSideValidationFail) {
  var execute;

  execute = function (ok) {
    if (ok) {
      actionHandlerCall();
    } else {
      clientSideValidationFail();
    }
  };

  if (view && typeof view.getContextInfo === 'function' && view.callerField && view.callerField.view && typeof view.callerField.view.getContextInfo === 'function') {
    var i, trxSelection = view.getContextInfo().findtransactiontomatch._selection;

    if (trxSelection) {
      var totalTrxAmt = 0,
          blineAmt = view.callerField.record.amount,
          hideSplitConfirmation = OB.PropertyStore.get('APRM_MATCHSTATEMENT_HIDE_PARTIALMATCH_POPUP', view.windowId);
      for (i = 0; i < trxSelection.length; i++) {
        var trxDepositAmt = trxSelection[i].depositAmount,
            trxPaymentAmt = trxSelection[i].paymentAmount,
            trxAmt = trxDepositAmt - trxPaymentAmt;
        totalTrxAmt = totalTrxAmt + trxAmt;
      }
      if (Math.abs(totalTrxAmt) <= Math.abs(blineAmt)) {
        // Split required
        if (hideSplitConfirmation === 'Y') {
          // Continue with the match
          actionHandlerCall();
        } else {
          if (isc.isA.emptyObject(OB.TestRegistry.registry)) {
            isc.confirm(OB.I18N.getLabel('APRM_SplitBankStatementLineConfirm'), execute);
          } else {
            execute(true);
          }
        }
      } else {
        // Sum of amounts of selected transaction
        // exceeds bank statement line amount
        view.messageBar.setMessage(isc.OBMessageBar.TYPE_ERROR, null, OB.I18N.getLabel('APRM_TRXAMT_EXCEED_BSLAMT', [totalTrxAmt, blineAmt]));
        clientSideValidationFail();
      }
    } else {
      // No Transaction selected
      view.messageBar.setMessage(isc.OBMessageBar.TYPE_ERROR, null, OB.I18N.getLabel('APRM_SELECT_RECORD_ERROR'));
      clientSideValidationFail();
    }
  }
};