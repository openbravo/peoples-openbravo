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
 * All portions are Copyright (C) 2001-2010 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

var frm = null;
var isReceipt = true;

function isTrue(objectName) {
  return frm.elements[objectName].value == 'Y';
}

function initFIN_Utilities(_frm) {
  frm = _frm;
  isReceipt = isTrue('isReceipt');
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
  var precision = Number(frm.curPrecision.value);
  var chk = frm.inpScheduledPaymentDetailId;
  if (!chk)
    return;
  else if (!chk.length) {
    var scheduledPaymentDetailId = frm.elements["inpRecordId0"].value;
    var outstandingAmount = frm.elements["inpRecordAmt" + scheduledPaymentDetailId].value;
    if (outstandingAmount - amount > 0)
      outstandingAmount = amount;
    frm.elements["inpPaymentAmount" + scheduledPaymentDetailId].value = outstandingAmount;
    numberInputEvent('onchange', frm.elements["inpPaymentAmount" + scheduledPaymentDetailId]);
    if (!chk.checked) {
      chk.checked = true;
      updateData(chk.value, chk.checked);
    }
  } else {
    var total = chk.length;
    for ( var i = 0; i < total; i++) {
      var scheduledPaymentDetailId = frm.elements["inpRecordId" + i].value;
      var outstandingAmount = frm.elements["inpRecordAmt" + scheduledPaymentDetailId].value;
      if (Number(outstandingAmount) > Number(amount))
        outstandingAmount = amount;
      if (Number(amount) == 0) {
        frm.elements["inpPaymentAmount" + scheduledPaymentDetailId].value = "";
        numberInputEvent('onchange', frm.elements["inpPaymentAmount" + scheduledPaymentDetailId]);
        for ( var j = 0; j < total; j++) {
          if (chk[j].checked && chk[j].value == scheduledPaymentDetailId) {
            chk[j].checked = false;
            updateData(chk[j].value, chk[j].checked);
          }
        }
      } else {
        frm.elements["inpPaymentAmount" + scheduledPaymentDetailId].value = outstandingAmount;
        numberInputEvent('onchange', frm.elements["inpPaymentAmount" + scheduledPaymentDetailId]);
        for ( var j = 0; j < total; j++) {
          if (!chk[j].checked && chk[j].value == scheduledPaymentDetailId) {
            chk[j].checked = true;
            updateData(chk[j].value, chk[j].checked);
          }
        }
        amount = round(Number(amount) - Number(outstandingAmount), precision);
      }
    }
  }
  updateTotal();
  return true;
}

function updateTotal() {
  var chk = frm.inpScheduledPaymentDetailId;
  var precision = Number(frm.curPrecision.value);
  var total = 0;
  if (!chk)
    return;
  else if (!chk.length) {
    var scheduledPaymentDetailId = frm.elements["inpRecordId0"].value;
    var pendingAmount = frm.elements["inpRecordAmt" + scheduledPaymentDetailId].value;
    var amount = frm.elements["inpPaymentAmount" + scheduledPaymentDetailId].value;
    if (amount != "" && (Number(pendingAmount) - Number(amount) < 0)) {
      setWindowElementFocus(frm.elements["inpPaymentAmount" + scheduledPaymentDetailId]);
      showJSMessage(9);
      return false;
    } else
      initialize_MessageBox('messageBoxID');
    if (chk.checked)
      total = frm.elements["inpPaymentAmount" + scheduledPaymentDetailId].value;
  } else {
    var rows = chk.length;
    for ( var i = 0; i < rows; i++) {
      var scheduledPaymentDetailId = frm.elements["inpRecordId" + i].value;
      var pendingAmount = frm.elements["inpRecordAmt" + scheduledPaymentDetailId].value;
      var amount = frm.elements["inpPaymentAmount" + scheduledPaymentDetailId].value;
      if (amount != "" && (Number(pendingAmount) - Number(amount) < 0)) {
        setWindowElementFocus(frm.elements["inpPaymentAmount" + scheduledPaymentDetailId]);
        showJSMessage(9);
        return false;
      } else
        initialize_MessageBox('messageBoxID');
      if (chk[i].checked)
        total = round(Number(total) + Number(frm.elements["inpPaymentAmount" + scheduledPaymentDetailId].value), precision);
    }
  }
  frm.inpTotal.value = total;
  numberInputEvent('onchange', frm.inpTotal);
  document.getElementById('paramTotal').innerHTML = frm.inpTotal.value;
  if (!isReceipt)
    frm.inpActualPayment.value = frm.inpTotal.value;
  updateDifference();
}

function updateDifference() {
  var expected = frm.inpExpectedPayment.value;
  var total = frm.inpTotal.value;
  var amount = total;
  var precision = Number(frm.curPrecision.value);
  if (frm.inpActualPayment != null)
    amount = frm.inpActualPayment.value;
  if (expected - total > 0)
    frm.inpDifference.value = round(Number(expected) - Number(total), precision);
  else if (amount - total > 0)
    frm.inpDifference.value = round(Number(amount) - Number(total), precision);
  else
    frm.inpDifference.value = 0;
  numberInputEvent('onchange', frm.inpDifference);
  document.getElementById('paramDifference').innerHTML = frm.inpDifference.value;
  displayLogicElement('sectionDifference', (expected - total != 0 || amount - total > 0));
  displayLogicElement('sectionDifferenceBox', (expected - total != 0 || amount - total > 0));
  displayLogicElement('writeoff', (expected - total != 0))
  displayLogicElement('underpayment', (expected - total > 0));
  displayLogicElement('credit', (amount - total > 0));
  displayLogicElement('refund', (amount - total > 0));
}

function updateReadOnly(key, mark) {
  if (mark == null) mark = false;
  frm.elements["inpPaymentAmount" + key].disabled = !mark;
  var expectedAmount = frm.inpExpectedPayment.value;
  var recordAmount = frm.elements["inpRecordAmt" + key].value;
  var precision = Number(frm.curPrecision.value);

  if (mark) {
	frm.elements["inpPaymentAmount" + key].className = frm.elements["inpPaymentAmount" + key].className.replace(' readonly', '');
	frm.inpExpectedPayment.value = Number(expectedAmount) + Number(recordAmount);
  } else {
	var classText = frm.elements["inpPaymentAmount" + key].className;
	if (classText.search('readonly') == -1) {
	  frm.elements["inpPaymentAmount" + key].className = classText.concat(" readonly");
    }
	frm.inpExpectedPayment.value = round(Number(expectedAmount) - Number(recordAmount), precision);
  }
  numberInputEvent('onchange', frm.inpExpectedPayment);
  if (!mark) frm.elements["inpAllLines"].checked = false;
  return true;
}

function updateAll() {
  var frm = document.frmMain;
  var chk = frm.inpScheduledPaymentDetailId;
  var precision = Number(frm.curPrecision.value);
  if (!chk) return;
  else if (!chk.length) {
    frm.inpExpectedPayment.value = Number("");
    if (!chk.checked) {
  	  var recordAmount = frm.elements["inpRecordAmt" + chk.value].value;
  	  frm.inpExpectedPayment.value = Number(frm.inpExpectedPayment.value) + Number(recordAmount);
	}
    updateData(chk.value, chk.checked);
  } else {
    frm.inpExpectedPayment.value = Number("");
    var total = chk.length;
    for ( var i = 0; i < total; i++) {
      if (!chk[i].checked) {
  	    var recordAmount = frm.elements["inpRecordAmt" + chk[i].value].value;
  	    frm.inpExpectedPayment.value = round(Number(frm.inpExpectedPayment.value) + Number(recordAmount), precision);
	  }
    updateData(chk[i].value, chk[i].checked);
    }
  }
  return true;
}

/**
 * Formats the number and sets to the value property of the object.
 * @param {Form} object
 * @param {String} value
 * @return void
 */
function setFormatedNumber(object, value) {
  var decSeparator = getGlobalDecSeparator();
  var groupSeparator = getGlobalGroupSeparator();
  var groupInterval = getGlobalGroupInterval();
  var outputformat = object.getAttribute("outputformat");

  if(outputformat != null || typeof value === "number") {
    maskNumeric = formatNameToMask(outputformat);
    var formattedNumber = returnCalcToFormatted(value, maskNumeric, decSeparator, groupSeparator, groupInterval);
    object.value = formattedValue;
  } else {
	object.value = value;
  }
}

/**
 * Formats a number according to the settings defined in Formats.xml
 * @param {String} outputformat Output format: see Formats.xml
 * @param {Number} number The number to be formated.
 * @return String representation of the number.
 */
function formatNumber(outputformat, number) {
  var decSeparator = getGlobalDecSeparator();
  var groupSeparator = getGlobalGroupSeparator();
  var groupInterval = getGlobalGroupInterval();

  if(outputformat != null || typeof number === "number") {
    maskNumeric = formatNameToMask(outputformat);
    var formattedNumber = returnCalcToFormatted(number, maskNumeric, decSeparator, groupSeparator, groupInterval);
    return formattedNumber;
  } else {
    return number;
  }
}
