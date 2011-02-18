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
 * All portions are Copyright (C) 2010 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

//Global variables definition
var frm = null;
var isReceipt = true;
var globalMaskNumeric = "#0.00";
var globalDecSeparator = ".";
var globalGroupSeparator = ",";
var globalGroupInterval = "3";

function isTrue(objectName) {
  return frm.elements[objectName].value == 'Y';
}

function initFIN_Utilities(_frm) {
  frm = _frm;
  isReceipt = isTrue('isReceipt');
  globalMaskNumeric = getDefaultMaskNumeric();
  globalDecSeparator = getGlobalDecSeparator();
  globalGroupSeparator = getGlobalGroupSeparator();
  globalGroupInterval = getGlobalGroupInterval();
}

function processLabels() {
  var receiptlbls = getElementsByName('lblR');
  for ( var i = 0; i < receiptlbls.length; i++) {
    displayLogicElement(receiptlbls[i].id, isReceipt);
  }
  var paidlbls = getElementsByName('lblP');
  for ( var i = 0; i < paidlbls.length; i++) {
    displayLogicElement(paidlbls[i].id, !isReceipt);
  }
}

function distributeAmount(_amount) {
  var amount = _amount;
  var chk = frm.inpScheduledPaymentDetailId;
  if (!chk) {
    updateTotal();
    return;
  } else if (!chk.length) {
    var scheduledPaymentDetailId = frm.elements["inpRecordId0"].value;
    var outstandingAmount = frm.elements["inpRecordAmt" + scheduledPaymentDetailId].value;
    if ( compare(outstandingAmount, '>', amount) )
      outstandingAmount = amount;
    frm.elements["inpPaymentAmount" + scheduledPaymentDetailId].value = outstandingAmount;
    if (!chk.checked) {
      chk.checked = true;
      updateData(chk.value, chk.checked);
    }
  } else {
    var total = chk.length;
    for ( var i = 0; i < total; i++) {
      var scheduledPaymentDetailId = frm.elements["inpRecordId" + i].value;
      var outstandingAmount = frm.elements["inpRecordAmt" + scheduledPaymentDetailId].value;
      if ( compare(outstandingAmount, '>', amount) )
        outstandingAmount = amount;
      if ( compare(amount, '==', 0) ) {
        frm.elements["inpPaymentAmount" + scheduledPaymentDetailId].value = "";
        for ( var j = 0; j < total; j++) {
          if (chk[j].checked && chk[j].value == scheduledPaymentDetailId) {
            chk[j].checked = false;
            updateData(chk[j].value, chk[j].checked);
          }
        }
      } else {
        frm.elements["inpPaymentAmount" + scheduledPaymentDetailId].value = outstandingAmount;
        for ( var j = 0; j < total; j++) {
          if (!chk[j].checked && chk[j].value == scheduledPaymentDetailId) {
            chk[j].checked = true;
            updateData(chk[j].value, chk[j].checked);
          }
        }
        amount = subtract(amount, outstandingAmount);
      }
    }
  }
  updateTotal();
  return true;
}
function isBetweenZeroAndMaxValue(value, maxValue){
  return ((compare(value, '>', 0) && compare(value, '<=', maxValue)) ||
          (compare(value, '<', 0) && compare(value, '>=', maxValue)));
}

function updateTotal() {
  var chk = frm.inpScheduledPaymentDetailId;
  var total = 0;
  if (!chk) {
    if (frm.inpGeneratedCredit && !isReceipt){
    	frm.inpActualPayment.value = frm.inpGeneratedCredit.value;
    }
    updateDifference();
    return;
  }else if (!chk.length) {
    var scheduledPaymentDetailId = frm.elements["inpRecordId0"].value;
    var pendingAmount = frm.elements["inpRecordAmt" + scheduledPaymentDetailId].value;
    var amount = frm.elements["inpPaymentAmount" + scheduledPaymentDetailId].value;
    if ( amount != "" && !isBetweenZeroAndMaxValue(amount, pendingAmount) ) {
      setWindowElementFocus(frm.elements["inpPaymentAmount" + scheduledPaymentDetailId]);
      showJSMessage(9);
      return false;
    } else
      initialize_MessageBox('messageBoxID');
    if (chk.checked)
      total = (frm.elements["inpPaymentAmount" + scheduledPaymentDetailId].value == '') ? "0" : frm.elements["inpPaymentAmount" + scheduledPaymentDetailId].value;
  } else {
    var rows = chk.length;
    for ( var i = 0; i < rows; i++) {
      var scheduledPaymentDetailId = frm.elements["inpRecordId" + i].value;
      var pendingAmount = frm.elements["inpRecordAmt" + scheduledPaymentDetailId].value;
      var amount = frm.elements["inpPaymentAmount" + scheduledPaymentDetailId].value;
      if (amount != "" && !isBetweenZeroAndMaxValue(amount, pendingAmount) ) {
        setWindowElementFocus(frm.elements["inpPaymentAmount" + scheduledPaymentDetailId]);
        showJSMessage(9);
        return false;
      } else
        initialize_MessageBox('messageBoxID');
      if (chk[i].checked)
        total = (frm.elements["inpPaymentAmount" + scheduledPaymentDetailId].value == '') ? total : add(total,frm.elements["inpPaymentAmount" + scheduledPaymentDetailId].value);
    }
  }
  frm.inpTotal.value = total;
  document.getElementById('paramTotal').innerHTML = frm.inpTotal.value;
  if (!isReceipt) {
    if (frm.inpUseCredit.checked) {
      if ( compare(total, '>',frm.inpCredit.value) )
        frm.inpActualPayment.value = subtract(total, frm.inpCredit.value);
      else
        frm.inpActualPayment.value = 0;
    } else {
      frm.inpActualPayment.value = frm.inpTotal.value;
      if (frm.inpGeneratedCredit)
    	frm.inpActualPayment.value = add(frm.inpTotal.value, frm.inpGeneratedCredit.value);
    }
  }
  updateDifference();
}

function updateDifference() {
  var expected = frm.inpExpectedPayment.value;
  if (expected == '')
    expected = 0;
  var total = frm.inpTotal.value;
  var amount = total;
  //var precision = Number(frm.curPrecision.value);
  if (frm.inpActualPayment != null)
    amount = frm.inpActualPayment.value;
  if (frm.inpUseCredit.checked)
    amount = add(amount, frm.inpCredit.value);
  if ( compare(abs(expected), '>', abs(total)) )
	frm.inpDifference.value = subtract(expected, total);
  else if ( compare(abs(amount), '>', abs(total)) )
	frm.inpDifference.value = subtract(amount, total);
  else
    frm.inpDifference.value = 0;
  document.getElementById('paramDifference').innerHTML = frm.inpDifference.value;
  displayLogicElement('sectionDifference', ( compare(expected, '!=', total) || compare(abs(amount), '>', abs(total)) ) );
  displayLogicElement('sectionDifferenceBox', ( compare(expected, '!=', total) || compare(abs(amount), '>', abs(total)) ) );
  displayLogicElement('writeoff', compare(expected, '!=', total) );
  displayLogicElement('underpayment', compare(abs(expected), '>', abs(total)) );
  displayLogicElement('credit', compare(abs(amount), '>', abs(total)) );
  displayLogicElement('refund', compare(abs(amount), '>', abs(total)) );
  if ( compare(abs(amount), '>', abs(total)) )
    selectDifferenceAction('credit');
  else if ( compare(abs(expected), '>', abs(total)) )
    selectDifferenceAction('underpayment');
}

function selectDifferenceAction(value) {
  var diffAction = frm.inpDifferenceAction;
  for (var i = 0; i < diffAction.length; i++) {
    diffAction[i].checked = false;
    diffAction[i].checked = (diffAction[i].value == value);
  }
}

function updateReadOnly(key, mark) {
  if (mark == null) mark = false;
  frm.elements["inpPaymentAmount" + key].disabled = !mark;
  var expectedAmount = frm.inpExpectedPayment.value;
  var recordAmount = frm.elements["inpRecordAmt" + key].value;

  if (mark) {
    frm.elements["inpPaymentAmount" + key].className = frm.elements["inpPaymentAmount" + key].className.replace(' readonly', '');
    frm.inpExpectedPayment.value = add(expectedAmount, recordAmount);
  } else {
    var classText = frm.elements["inpPaymentAmount" + key].className;
    if (classText.search('readonly') == -1) {
      frm.elements["inpPaymentAmount" + key].className = classText.concat(" readonly");
    }
    frm.elements["inpPaymentAmount" + key].value = '';
    frm.inpExpectedPayment.value = subtract(expectedAmount, recordAmount);
  }
  if (!mark) frm.elements["inpAllLines"].checked = false;
  return true;
}

function updateAll() {
  var frm = document.frmMain;
  var chk = frm.inpScheduledPaymentDetailId;
  if (!chk) return;
  else if (!chk.length) {
    frm.inpExpectedPayment.value = "0";
    if (!chk.checked) {
  	  var recordAmount = frm.elements["inpRecordAmt" + chk.value].value;
  	  frm.inpExpectedPayment.value = add(frm.inpExpectedPayment.value, recordAmount);
	}
    updateData(chk.value, chk.checked);
  } else {
    frm.inpExpectedPayment.value = "0";
    var total = chk.length;
    for ( var i = 0; i < total; i++) {
      if (!chk[i].checked) {
  	    var recordAmount = frm.elements["inpRecordAmt" + chk[i].value].value;
  	    frm.inpExpectedPayment.value = add(frm.inpExpectedPayment.value, recordAmount);
	  }
    updateData(chk[i].value, chk[i].checked);
    }
  }
  return true;
}

/**
* Function to operate with formatted number
* @param {Number} number1 The first operand
* @param {String} operator The operator (+ - * / % < > <= >= ...)
* @param {Number} number2 The second operand
* @param {String} result_maskNumeric The numeric mask of the result
* @param {String} decSeparator The decimal separator of the number
* @param {String} groupSeparator The group separator of the number
* @param {String} groupInterval The group interval of the number
* @return The result of the operation or true or false if the operator is (< > <= >= ...)
* @type String or Boolean
* @deprecated TO BE REMOVED ON MP22
*/
function formattedNumberOpTemp(number1, operator, number2, result_maskNumeric, decSeparator, groupSeparator, groupInterval) {
  if (result_maskNumeric == null || result_maskNumeric == "") result_maskNumeric = getDefaultMaskNumeric();
  if (decSeparator == null || decSeparator == "") decSeparator = getGlobalDecSeparator();
  if (groupSeparator == null || groupSeparator == "") groupSeparator = getGlobalGroupSeparator();
  if (groupInterval == null || groupInterval == "") groupInterval = getGlobalGroupInterval();

  var result;

  number1 = returnFormattedToCalc(number1, decSeparator, groupSeparator);
  number1 = parseFloat(number1);

  number2 = returnFormattedToCalc(number2, decSeparator, groupSeparator);
  number2 = parseFloat(number2);

  if (operator == "sqrt") {
    result = Math.sqrt(number1);
  } else if (operator == "round") {
    result = roundNumber(number1, number2);
  } else {
	result = eval('('+number1+')' + operator + '('+number2+')');
  }
  if (result != true && result != false && result != null && result != "") {
    result = returnCalcToFormatted(result, result_maskNumeric, decSeparator, groupSeparator, groupInterval)
  }
  return result;
}

/**
 * Calculates the absolute value using the global formats
 * @param {String} number1 The number
 * @return The result of the Math.abs() operation in a formatted string
 * @type String
 */
function abs(number1) {
  var result;
  number1 = returnFormattedToCalc(number1, globalDecSeparator, globalGroupSeparator);
  number1 = parseFloat(number1);
  result = Math.abs(number1);

  if (result != null && result != "") {
    result = returnCalcToFormatted(result, globalMaskNumeric, globalDecSeparator, globalGroupSeparator, globalGroupInterval);
  }
  return result;
}

/**
 * Arithmetic add operation of two Strings using the global formats.
 * @param {String} number1 The first operand
 * @param {String} number2 The second operand
 * @return The result of adding number1 to number2 using the global formats.
 * @type String
 */
function add(number1, number2) {
  return formattedNumberOpTemp(number1, '+', number2, globalMaskNumeric, globalDecSeparator, globalGroupSeparator, globalGroupInterval);
}

/**
 * Arithmetic subtract operation of two Strings using the global formats.
 * @param {String} number1 The first operand
 * @param {String} number2 The second operand
 * @return The result of adding number1 to number2 using the global formats.
 * @type String
 */
function subtract(number1, number2) {
  return formattedNumberOpTemp(number1, '-', number2, globalMaskNumeric, globalDecSeparator, globalGroupSeparator, globalGroupInterval);
}

/**
 * Compares two Strings using the operator
 * @param {String} number1 The first operand
 * @param {String} operator The operator (+ - * / % < > <= >= ...)
 * @param {String} number2 The second operand
 * @return true or false
 * @type boolean
 */
function compare(number1, operator, number2) {
  return formattedNumberOpTemp(number1, operator, number2, globalMaskNumeric, globalDecSeparator, globalGroupSeparator, globalGroupInterval);	
}

function validateSelectedAmounts(recordID, existsPendingAmount){
  if (existsPendingAmount == null) existsPendingAmount = false;
  var pendingAmount = document.frmMain.elements["inpRecordAmt"+recordID].value;
  var amount = document.frmMain.elements["inpPaymentAmount"+recordID].value;
  if (amount==null || amount=="") {
    setWindowElementFocus(frm.elements["inpPaymentAmount"+recordID]);
    showJSMessage(7);
    return false;
  }
  if ( !isBetweenZeroAndMaxValue(amount, pendingAmount) ) {
    setWindowElementFocus(frm.elements["inpPaymentAmount"+recordID]);
    showJSMessage(9);
    return false;
  }
  if ( existsPendingAmount && compare(amount, '<', pendingAmount) ) {
    setWindowElementFocus(frm.elements["inpPaymentAmount"+recordID]);
    showJSMessage('APRM_JSNOTALLAMOUTALLOCATED');
    return false;
  }
  return true;
}

/**
 * 
 * @param allowCreditGeneration true if it is allowed to not select any pending payment if actualPayment amount is not
 *        zero.
 * @return true if validations are fine.
 */
function validateSelectedPendingPayments(allowCreditGeneration) {
  if (allowCreditGeneration === undefined) allowCreditGeneration = false;
  var actualPayment = document.frmMain.inpActualPayment.value;
  var expectedPayment = document.frmMain.inpExpectedPayment.value;
  if (document.frmMain.inpUseCredit.checked) {
    if ( compare (expectedPayment, '<=', actualPayment)) {
      setWindowElementFocus(document.frmMain.inpUseCredit);
      showJSMessage('APRM_JSCANNOTUSECREDIT');
      return false;
    }
    actualPayment = add(actualPayment, document.frmMain.inpCredit.value);
  }
  var selectedTotal = document.frmMain.inpTotal.value;
  if ( compare(selectedTotal, '>', actualPayment) ) {
    setWindowElementFocus(document.frmMain.inpActualPayment);
    showJSMessage('APRM_JSMOREAMOUTALLOCATED');
    return false;
  }
  var chk = frm.inpScheduledPaymentDetailId;
  if (!chk) return true;
  else if (!chk.length) {
    if (chk.checked) {
      if (!validateSelectedAmounts(chk.value, compare(selectedTotal, '<', actualPayment))) return false;
    } else if ( !allowCreditGeneration || compare(document.frmMain.inpDifference.value, '==', "0") ){
      showJSMessage('APRM_JSNOTLINESELECTED');
      return false;
    }
  } else {
    var total = chk.length;
    var isAnyChecked = false;
    for (var i=0;i<total;i++) {
      if (chk[i].checked) {
        isAnyChecked = true;
        if (!validateSelectedAmounts(chk[i].value, compare(selectedTotal, '<', actualPayment))) return false;
      }
    }
    if (!isAnyChecked &&
        (!allowCreditGeneration || compare(document.frmMain.inpDifference.value, '==', "0")) 
        ) {
      showJSMessage('APRM_JSNOTLINESELECTED');
      return false;
    }
  }
  return true;
}