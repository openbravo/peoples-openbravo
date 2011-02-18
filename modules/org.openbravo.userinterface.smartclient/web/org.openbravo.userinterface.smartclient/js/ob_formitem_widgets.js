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

// = Form Item Widgets =
// Contains the following widgets:
// * OBDateItem: FormItem for dates
// * OBDateTimeItem: FormItem for DateTime
// * OBNumber: FormItem for numbers
// * OBYesNoItem: combo box for yes/no values

// == OBYesNoItem ==
// Extends SelectItem with preset yes and no values
isc.ClassFactory.defineClass("OBYesNoItem", SelectItem);

isc.OBYesNoItem.addProperties({
  mapValueToDisplay : function(value, a, b, c) {
    return OB.Utilities.getYesNoDisplayValue(value);
  },
  formatPickListValue: function(value, record, field, rowNum, colNum) {
    return OB.Utilities.getYesNoDisplayValue(value);
  }
});

// == OBDateItem ==
// OBDateItem inherits from SmartClient DateItem
// adds autocomplete and formatting based on the Openbravo date pattern
isc.ClassFactory.defineClass("OBDateItem", DateItem);

isc.OBDateItem.addClassProperties({

// ** {{{ autoCompleteData }}} **
//
// Autocomplets the date entered. 
// Parameters:
// * {{{dateFormat}}}: the dateFormat in OB format
// * {{{value}}}: the current entered value
  autoCompleteDate : function (dateFormat, value) {
    // if (!isTabPressed) {
    if (value === null) {
      return value;
    }
    fmt = OB.Utilities.Date.normalizeDisplayFormat(dateFormat);
    try {
      if (item.getSelectionRange()[0] != value.length) {
        return value; // If we are inserting in a position different from the
                      // last one, we don't autocomplete
      }
    } catch (ignored) {}
    var strDate = value;
    var b = fmt.match(/%./g);
    var i = 0, j = -1;
    var text = "";
    var length = 0;
    var pos = fmt.indexOf(b[0]) + b[0].length;
    var separator = fmt.substring(pos, pos + 1);
    var separatorH = "";
    pos = fmt.indexOf("%H");
    if (pos !== -1) {
      separatorH = fmt.substring(pos + 2, pos + 3);
    }
    while (strDate.charAt(i)) {
      if (strDate.charAt(i) == separator || strDate.charAt(i) == separatorH || strDate.charAt(i) === " ") {
        i++;
        continue;
      }
      if (length <= 0) {
        j++;
        if (j > 0) {
          if (b[j] === "%H") {
            text += " ";
          }
          else if (b[j] === "%M" || b[j] === "%S") {
            text += separatorH;
          }
          else {
            text += separator;
          }
        }
        switch (b[j]) {
        case "%d":
        case "%e":
          text += strDate.charAt(i);
          length = 2;
          break;
        case "%m":
          text += strDate.charAt(i);
          length = 2;
          break;
        case "%Y":
          text += strDate.charAt(i);
          length = 4;
          break;
        case "%y":
          text += strDate.charAt(i);
          length = 2;
          break;
        case "%H":
        case "%I":
        case "%k":
        case "%l":
          text += strDate.charAt(i);
          length = 2;
          break;
        case "%M":
          text += strDate.charAt(i);
          length = 2;
          break;
        case "%S":
          text += strDate.charAt(i);
          length = 2;
          break;
        }
      } else {
        text += strDate.charAt(i);
      }
      length--;
      i++;
    }
    return text;
    // IE doesn't detect the onchange event if text value is modified
    // programatically, so it's here called
    // if (i > 7 && (typeof (field.onchange)!="undefined")) field.onchange();
    // }
  },

// ** {{{ getDateBlock }}} **
//
// Return the part of the date denoted by the block (which is a number).  
// Parameters:
// * {{{str_date}}}: the complete date
// * {{{block}}}: number from 1 to 3, denotes the part of the date to return
  getDateBlock : function (str_date, block) {
    var datePattern = "^(\\d+)[\\-|\\/|/|:|.|\\.](\\d+)[\\-|\\/|/|:|.|\\.](\\d+)$";
    var dateRegExp = new RegExp(datePattern);
    if (!dateRegExp.exec(str_date)) {
      return null;
    }
    var dateBlock = [];
    dateBlock[1] = RegExp.$1;
    dateBlock[2] = RegExp.$2;
    dateBlock[3] = RegExp.$3;
    if (block === 1 || block === '1') {
      return dateBlock[1];
    } else if (block === 2 || block === '2') {
      return dateBlock[2];
    } else if (block === 3 || block === '3') {
      return dateBlock[3];
    } else {
      return dateBlock;
    }
  },
  
// ** {{{ expandDateYear }}} **
//
// Expands the year and day/month to 4 resp 2 digits.
// Parameters:
//* {{{dateFormat}}}: the date format in OB syntax
//* {{{value}}}: the date to expand
  expandDateYear : function (dateFormat, value) {
    var str_date = value;
    var str_dateFormat = dateFormat.replace("yyyy", "YYYY");
    if (str_date === null || str_dateFormat.indexOf('YYYY') === -1) {
      return value;
    }
    var centuryReference = 50;
    var dateBlock = [];
    dateBlock[1] = this.getDateBlock(str_date, 1);
    dateBlock[2] = this.getDateBlock(str_date, 2);
    dateBlock[3] = this.getDateBlock(str_date, 3);

    if (!dateBlock[1] || !dateBlock[2] || !dateBlock[3]) {
      return value;
    }

    var yearBlock;
    if (str_dateFormat.substr(1, 1) === 'Y') {
      yearBlock = 1;
    } else if (str_dateFormat.substr(7, 1) == 'Y') {
      yearBlock = 3;
    } else {
      return value;
    }

    if (dateBlock[yearBlock].length === 1) {
      dateBlock[yearBlock] = '000' + dateBlock[yearBlock];
    } else if (dateBlock[yearBlock].length === 2) {
      if (dateBlock[yearBlock] < centuryReference) {
        dateBlock[yearBlock] = '20' + dateBlock[yearBlock];
      } else {
        dateBlock[yearBlock] = '19' + dateBlock[yearBlock];
      }
    } else if (dateBlock[yearBlock].length === 3) {
      dateBlock[yearBlock] = '0' + dateBlock[yearBlock];
    } else if (dateBlock[yearBlock].length === 4) {
      return value;
    }

    var dateSeparator = str_dateFormat.toUpperCase().replace(/D/g, "").replace(/M/g, "").replace(/Y/g, "").substr(0, 1);
    var normalizedDate = dateBlock[1] + dateSeparator + dateBlock[2] + dateSeparator + dateBlock[3];
    return normalizedDate;
  }
});

// == OBDateItem properties ==
isc.OBDateItem.addProperties({

// ** {{{ dateFormat }}} **
// Dateformat function  
  dateFormat: OB.Format.date,
  
// ** {{{ useTextField }}} **
// use text field for date entry
  useTextField : true,
  
// ** {{{ changeOnKeypress }}} **
// Fire change event on key press.  
  changeOnKeypress: true,
  
// ** {{{ validateOnChange }}} **
// Validate input on change
  validateOnChange : false,
    
// ** {{{ change }}} **
// Called when changing a value.
  change :  function (form, item, value, oldValue) { /* transformInput */
    var isADate = value !== null && Object.prototype.toString.call(value) === '[object Date]'; 
    if (isADate) {
      return;
    }
    item.setValue(OBDateItem.autoCompleteDate(item.dateFormat, value));
  },
  
// ** {{{ blur }}} **
// Called when the focus leaves the field (sets value and validates)s
  blur : function () {
    this.setValue(OBDateItem.expandDateYear(this.dateFormat, this.getValue()));
    this.validateOBDateItem();
  },

// ** {{{ displayFormat }}} **
// Formats a date object to a String.
  displayFormat: function () {
    // this: is the date object to format
    var displayedDate = OB.Utilities.Date.JSToOB(this, OB.Format.date);
    return displayedDate;
  },

// ** {{{ inputFormat }}} **
// Parses the inputted value to javascript Date.
  inputFormat: function (value) {
    return OB.Utilities.Date.OBToJS(value, OB.Format.date);
  },

// ** {{{ validateOBDateItem }}} **
// Validate the entered date.
  validateOBDateItem : function () {
    var dateValue = OB.Utilities.Date.OBToJS(this.getValue(), this.dateFormat);
    var isValid = true;
    if (this.getValue() !== null && dateValue === null) {
      isValid = false;
    }
    var isRequired = this.required;
    if (typeof this.name === "undefined") {
      console.log("Item name undefined, this is an error, always set a name for a form item, this: " + this);
    }
    if (isValid === false) {
      this.form.setFieldErrors(this.name, 'The value entered is not valid', true);
    } else if (isRequired === true && this.getValue() === null) {
      this.form.setFieldErrors(this.name, 'The value entered is required', true);
    } else {
      this.form.clearFieldErrors(this.name, true);
    }
  }

});

isc.ClassFactory.defineClass("OBDateTimeItem", isc.OBDateItem);

// = OBDateTimeItem =
// The Openbravo DateTime form item.
isc.OBDateTimeItem.addProperties({

  //** {{{ dateFormat }}} **
//The date time format function.
  dateFormat: OB.Format.dateTime,

  //** {{{ displayFormat }}} **
  // Formats the date to a string.
  displayFormat: function () {
    // this: is the date object to format
    var displayedDate = OB.Utilities.Date.JSToOB(this, OB.Format.dateTime);
    return displayedDate;
  },

  //** {{{ inputFormat }}} **
  //Parses an input to a Date.
  inputFormat: function (value) {
    return OB.Utilities.Date.OBToJS(value, OB.Format.dateTime);
  }
});

isc.ClassFactory.defineClass("OBNumberItem", TextItem);

// = OBNumberItem =
// The Openbravo numeric form item.
isc.OBNumberItem.addClassProperties({
  
  OBNumberItemElement : null,
  getDefaultMaskNumeric : function () {
    var maskNumeric = "#,###.#";
    return maskNumeric;
  },
  getGlobalDecSeparator : function () {
    var decSeparator = ".";
    return decSeparator;
  },
  getGlobalGroupSeparator : function () {
    var groupSeparator = ",";
    return groupSeparator;
  },
  getGlobalGroupInterval : function () {
    var groupInterval = "3";
    return groupInterval;
  },
  returnNewCaretPosition : function (number, oldCaretPosition, groupSeparator) {
    var newCaretPosition = oldCaretPosition;
    for (var i = oldCaretPosition; i > 0; i--) {
      if (number.substring(i - 1, i) == groupSeparator) {
        newCaretPosition = newCaretPosition - 1;
      }
    }
    return newCaretPosition;
  },
  focusNumberInput : function (maskNumeric, decSeparator, groupSeparator, groupInterval) {
    if (maskNumeric === null || maskNumeric === "" || typeof maskNumeric === "undefined") {
      maskNumeric = this.OBNumberItemElement.maskNumeric;
    }
    if (decSeparator === null || decSeparator === "" || typeof decSeparator === "undefined") {
      decSeparator = this.OBNumberItemElement.decSeparator;
    }
    if (groupSeparator === null || groupSeparator === "" || typeof groupSeparator === "undefined") {
      groupSeparator = this.OBNumberItemElement.groupSeparator;
    }
    if (groupInterval === null || groupInterval === "" || typeof groupInterval === "undefined") {
      groupInterval = this.OBNumberItemElement.groupInterval;
    }

    if (maskNumeric === null || maskNumeric === "" || typeof maskNumeric === "undefined") {
      maskNumeric = this.getDefaultMaskNumeric();
    }
    if (decSeparator === null || decSeparator === "" || typeof decSeparator === "undefined") {
      decSeparator = this.getGlobalDecSeparator();
    }
    if (groupSeparator === null || groupSeparator === "" || typeof groupSeparator === "undefined") {
      groupSeparator = this.getGlobalGroupSeparator();
    }
    if (groupInterval === null || groupInterval === "" || typeof groupInterval === "undefined") {
      groupInterval = this.getGlobalGroupInterval();
    }

    var oldCaretPosition = this.OBNumberItemElement.getSelectionRange()[0];
    var newCaretPosition = this.returnNewCaretPosition(this.OBNumberItemElement.getValue(), oldCaretPosition, groupSeparator);

    var number = this.OBNumberItemElement.getValue();
    if (typeof number === "undefined" || !number) {
      number = "";
    }
    var isValid = this.validateNumber(number, maskNumeric, decSeparator, groupSeparator, groupInterval);
    if (!isValid) {
      return false;
    }
    var plainNumber = OB.Utilities.Number.OBMaskedToOBPlain(number, decSeparator, groupSeparator);
    this.OBNumberItemElement.setValue(plainNumber);
    this.OBNumberItemElement.setSelectionRange(newCaretPosition, newCaretPosition);
  },
  blurNumberInput : function (maskNumeric, decSeparator, groupSeparator, groupInterval) {
    if (maskNumeric === null || maskNumeric === "" || typeof maskNumeric === "undefined") {
      maskNumeric = this.OBNumberItemElement.maskNumeric;
    }
    if (decSeparator === null || decSeparator === "" || typeof decSeparator === "undefined") {
      decSeparator = this.OBNumberItemElement.decSeparator;
    }
    if (groupSeparator === null || groupSeparator === "" || typeof groupSeparator === "undefined") {
      groupSeparator = this.OBNumberItemElement.groupSeparator;
    }
    if (groupInterval === null || groupInterval === "" || typeof groupInterval === "undefined") {
      groupInterval = this.OBNumberItemElement.groupInterval;
    }

    if (maskNumeric === null || maskNumeric === "" || typeof maskNumeric === "undefined") {
      maskNumeric = this.getDefaultMaskNumeric();
    }
    if (decSeparator === null || decSeparator === "" || typeof decSeparator === "undefined") {
      decSeparator = this.getGlobalDecSeparator();
    }
    if (groupSeparator === null || groupSeparator === "" || typeof groupSeparator === "undefined") {
      groupSeparator = this.getGlobalGroupSeparator();
    }
    if (groupInterval === null || groupInterval === "" || typeof groupInterval === "undefined") {
      groupInterval = this.getGlobalGroupInterval();
    }

    var number = this.OBNumberItemElement.getValue();
    var isValid = this.validateNumber(number, maskNumeric, decSeparator, groupSeparator, groupInterval);
    /*
     * if (obj.getAttribute('maxlength')) { if (obj.value.length >
     * obj.getAttribute('maxlength')) { isValid = false; } }
     * updateNumberMiniMB(obj, isValid); //It doesn't apply in dojo043 inputs
     * since it has its own methods to update it
     */
    if (!isValid) {
      return false;
    }

    var formattedNumber = OB.Utilities.Number.OBPlainToOBMasked(number, maskNumeric, decSeparator, groupSeparator, groupInterval);
    this.OBNumberItemElement.setValue(formattedNumber);
  },
  replaceAt : function (string, what, ini, end) {
    if (typeof end === "undefined" || end === null || end === "null" || end === "") {
      end = ini;
    }
    if (ini > end) {
      var temp = ini;
      ini = end;
      end = temp;
    }
    var newString = "";
    newString = string.substring(0, ini) + what + string.substring(end + 1, string.length);
    return newString;
  },
  manageDecPoint : function (keyCode, decSeparator) {
    if (decSeparator === null || decSeparator === "") {
      decSeparator = this.getGlobalDecSeparator();
    }

    if (decSeparator === ".") {
      return true;
    }

    var caretPosition = this.OBNumberItemElement.getSelectionRange()[0];
    /*
     * if(keyCode>=65 && keyCode<=90) { setTimeout(function() {obj.value =
     * replaceAt(obj.value, "", caretPosition); setCaretToPos(obj,
     * caretPosition);},5); }
     */
    var inpMaxlength = this.OBNumberItemElement.length;
    var inpLength = this.OBNumberItemElement.getValue().length;
    var isInpMaxLength = false;
    if (inpMaxlength === null) {
      isInpMaxLength = false;
    } else if (inpLength >= inpMaxlength) {
      isInpMaxLength = true;
    }

    if (navigator.userAgent.toUpperCase().indexOf("OPERA") != -1 && keyCode==78) {
      keyCode = 110;
    }

    var obj = this;
    if (keyCode === 110) {
      setTimeout(function () {
        var newValue = obj.replaceAt(obj.OBNumberItemElement.getValue(), decSeparator, caretPosition);
        obj.OBNumberItemElement.setValue(newValue);
        obj.OBNumberItemElement.setSelectionRange(caretPosition + 1, caretPosition + 1);
      }, 5);
    }
    return true;
  },
  validateNumber :  function (number, maskNumeric, decSeparator, groupSeparator, groupInterval) {
    if (number === null || number === "" || typeof number === "undefined") {
      number = this.OBNumberItemElement.getValue();
    }
    if (maskNumeric === null || maskNumeric === "" || typeof maskNumeric === "undefined") {
      maskNumeric = this.OBNumberItemElement.maskNumeric;
    }
    if (decSeparator === null || decSeparator === "" || typeof decSeparator === "undefined") {
      decSeparator = this.OBNumberItemElement.decSeparator;
    }
    if (groupSeparator === null || groupSeparator === "" || typeof groupSeparator === "undefined") {
      groupSeparator = this.OBNumberItemElement.groupSeparator;
    }
    if (groupInterval === null || groupInterval === "" || typeof groupInterval === "undefined") {
      groupInterval = this.OBNumberItemElement.groupInterval;
    }

    if (maskNumeric === null || maskNumeric === "" || typeof maskNumeric === "undefined") {
      maskNumeric = this.getDefaultMaskNumeric();
    }
    if (decSeparator === null || decSeparator === "" || typeof decSeparator === "undefined") {
      decSeparator = this.getGlobalDecSeparator();
    }
    if (groupSeparator === null || groupSeparator === "" || typeof groupSeparator === "undefined") {
      groupSeparator = this.getGlobalGroupSeparator();
    }
    if (groupInterval === null || groupInterval === "" || typeof groupInterval === "undefined") {
      groupInterval = this.getGlobalGroupInterval();
    }

    if (number === null || number === "" || typeof number === "undefined") {
      return true;
    }

    var bolNegative = true;
    if (maskNumeric.indexOf("+") === 0) {
      bolNegative = false;
      maskNumeric = maskNumeric.substring(1, maskNumeric.length);
    }

    var bolDecimal = true;
    if (maskNumeric.indexOf(decSeparator) === -1) {
      bolDecimal = false;
    }
    var checkPattern = "";
    checkPattern += "^";
    if (bolNegative) {
      checkPattern += "([+]|[-])?";
    }
    checkPattern += "(\\d+)?((\\" + groupSeparator + "\\d{" + groupInterval + "})?)+";
    if (bolDecimal) {
      checkPattern += "(\\" + decSeparator + "\\d+)?";
    }
    checkPattern += "$";
    var checkRegExp = new RegExp(checkPattern);
    if (number.match(checkRegExp) && number.substring(0, 1) !== groupSeparator) {
      return true;
    }
    return false;
  },
  getRandomInteger : function (minInt, maxInt) {
    if (typeof minInt === "undefined") {
      minInt = 0;
    }
    if (typeof maxInt === "undefined") {
      maxInt = 100;
    }
    var randomInteger = minInt + (Math.random() * (maxInt - minInt));
    randomInteger = Math.round(randomInteger);
    return randomInteger;
  },
  getRandomString : function (num) {
    if (typeof num === "undefined") {
      num = 10;
    }
    var chars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXTZabcdefghiklmnopqrstuvwxyz";
    var randomString = '';
    for (var i = 0; i < num; i++) {
      var rnum = Math.floor(Math.random() * chars.length);
      randomString += chars.substring(rnum, rnum + 1);
    }
    return randomString;
  },
  validateOBNumberItem : function () {
    var value = this.OBNumberItemElement.getValue();
    var validatorLength = this.OBNumberItemElement.validators.length;
    var isValid = this.OBNumberItemElement.validators[validatorLength-1].condition(this.OBNumberItemElement, this.OBNumberItemElement.form, value);
    var isRequired = this.OBNumberItemElement.required;
    if (typeof this.OBNumberItemElement.name === "undefined") {
      this.name = "isc_" + OBNumberItem.getRandomString(OBNumberItem.getRandomInteger(6, 12));
    }
    if (isValid === false) {
      this.OBNumberItemElement.form.setFieldErrors(this.OBNumberItemElement.name, 'The value entered is not valid', true);
    } else if (isRequired === true && (value === null || value === "" || typeof value === "undefined")) {
      this.OBNumberItemElement.form.setFieldErrors(this.OBNumberItemElement.name, 'The value entered is required', true);
    } else {
      this.OBNumberItemElement.form.clearFieldErrors(this.OBNumberItemElement.name, true);
    }
  }
});

isc.OBNumberItem.addProperties({
  validateOnChange : false,
  keyPressFilter: "[0-9.,]",
  focus: function () {
    OBNumberItem.OBNumberItemElement = this;
    OBNumberItem.focusNumberInput(this.maskNumeric, this.decSeparator, this.groupSeparator, this.groupInterval);
  },
  blur: function () {
    OBNumberItem.OBNumberItemElement = this;
    OBNumberItem.blurNumberInput(this.maskNumeric, this.decSeparator, this.groupSeparator, this.groupInterval);
    OBNumberItem.validateOBNumberItem();
  },
  keyDown: function () {
    var keyCode = isc.EventHandler.getKeyCode();
    OBNumberItem.OBNumberItemElement = this;
    OBNumberItem.manageDecPoint(keyCode, this.decSeparator);
  },
  validators : [{
    type : "custom",
    condition : function (form, item, value) {
      return OBNumberItem.validateNumber(value);
    }
  }]
 // keyPress: function (keyName, character) { OBNumberItem.OBNumberItemElement
  // = this; var event = isc.Event; OBNumberItem.manageDecPoint(event); }
});