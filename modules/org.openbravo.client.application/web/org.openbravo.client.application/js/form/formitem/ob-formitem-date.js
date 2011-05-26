/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.1  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use. this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html
 * Software distributed under the License  is  distributed  on  an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific  language  governing  rights  and  limitations
 * under the License.
 * The Original Code is Openbravo ERP.
 * The Initial Developer of the Original Code is Openbravo SLU
 * All portions are Copyright (C) 2011 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

// == OBDateItem ==
// OBDateItem inherits from SmartClient DateItem
// adds autocomplete and formatting based on the Openbravo date pattern
isc.ClassFactory.defineClass('OBDateItem', isc.DateItem);

isc.OBDateItem.addProperties({
  operator: 'equals',
  // ** {{{ pickerConstructor }}} **
  // Picker constructor class
  pickerConstructor: 'OBDateChooser',
  useSharedPicker: true,
  
  // ** {{{ dateFormat }}} **
  // Dateformat function
  dateFormat: OB.Format.date,
  
  // ** {{{ useTextField }}} **
  // use text field for date entry
  useTextField: true,
  
  // ** {{{ changeOnKeypress }}} **
  // Fire change event on key press.
  changeOnKeypress: true,
  
  // is done by the blur event defined here
  validateOnExit: false,
  validateOnChange: false,
  
  textAlign: 'left',
  
  // to prevent infinite looping as setFormErrors will also blur
  inBlur: false,

  dateParts : [],

  init: function() {
    var i, dateFormatUpper, index = 0, currentTime;
    dateFormatUpper = this.dateFormat.toUpperCase();
    this.dateSeparator = this.dateFormat.toUpperCase().replace(/D/g, '')
        .replace(/M/g, '').replace(/Y/g, '').substr(0, 1);
    for (i = 0; i < dateFormatUpper.length; i++) {
      if (this.isSeparator(dateFormatUpper, i)) {
        index++;
      } else {
        this.dateParts[index] = dateFormatUpper.charAt(i);
      }
    }
    currentTime = new Date();
    this.currentMonth = String(currentTime.getMonth() + 1);
    if (this.currentMonth.length === 1) {
      this.currentMonth = '0' + this.currentMonth;
    }
    this.currentDay = String(currentTime.getDate());
    if (this.currentDay.length === 1) {
      this.currentDay = '0' + this.currentDay;
    }
    this.currentYear = String(currentTime.getFullYear());

    this.Super('init', arguments);
  },

  // compare while ignoring milli difference
  compareValues: function (value1, value2) {
    // not a date let the super class do it
    if (!isc.isA.Date(value1) || !isc.isA.Date(value2)) {
      return this.Super('compareValues', arguments);
    }
    var difference = value1.getTime() - value2.getTime();
    if (difference < -1000) {
      return true;
    } else if (difference > 1000) {
      return false;
    } else {
      return true;
    }
  },
  
  // ** {{{ blur }}} **
  // Called when the focus leaves the field (sets value and validates)
  blur: function(){
    var newValue = this.blurValue(), oldValue = this.getValue(), editRow;
    
    if (oldValue !== newValue) {
      this.storeValue(OB.Utilities.Date.OBToJS(newValue, this.dateFormat));
    }
    if (!this.inBlur) {
      this.inBlur = true;
      this.checkOBDateItemValue();
      this.inBlur = false;
    }
    return this.Super('blur', arguments);
  },
  
  blurValue: function() {
    return this.parseValue();
  },

  parseValue: function() {
    var i, str = this.getValue(), parts = [ '', '', '' ], partIndex = 0, result;
    if (!str || isc.isA.Date(str)) {
      return str;
    }
    for (i = 0; i < str.length; i++) {
      if (this.isNumber(str, i)) {
        if (this.reachedLength(parts[partIndex], partIndex)) {
          partIndex++;
        }
        if (partIndex === 3) {
          break;
        }
        parts[partIndex] = parts[partIndex] + str.charAt(i);
      } else if (this.isSeparator(str, i)) {
        partIndex++;
      } else {
        // invalid date
        return str;
      }
      if (partIndex === 3) {
        break;
      }
    }
    for (i = 0; i < 3; i++) {
      parts[i] = this.expandPart(parts[i], i);
    }
    return parts[0] + this.dateSeparator + parts[1] + this.dateSeparator
        + parts[2];
  },

  expandPart : function(part, index) {
    var year;
    if (this.reachedLength(part, index)) {
      return part;
    }
    if (part === '') {
      if (this.dateParts[index] === 'D') {
        return this.currentDay;
      } else if (this.dateParts[index] === 'M') {
        return this.currentMonth;
      } else {
        return this.currentYear;
      }
    } else if (this.dateParts[index] === 'Y') {
      year = parseInt(part, 10);
      if (year <= 50) {
        return String(2000 + year);
      } else if (year < 100) {
        return String(1900 + year);
      } else {
        return '2' + part;
      }
    } else if (part.length === 1) {
      return '0' + part;
    }
    return part;
  },

  reachedLength : function(part, index) {
    var maxLength;
    if (this.dateParts[index] === 'D' || this.dateParts[index] === 'M') {
      maxLength = 2;
    } else {
      maxLength = 4;
    }
    return part.length >= maxLength;
  },

  isNumber : function(str, position) {
    return str.charAt(position) >= '0' && str.charAt(position) <= '9';
  },

  isSeparator : function(str, position) {
    return str.charAt(position) === '-' || str.charAt(position) === '\\'
        || str.charAt(position) === '/';
  },

  // ** {{{ checkOBDateItemValue }}} **
  // Validate the entered date and add a form error, is called onblur
  checkOBDateItemValue: function(){
    var value = this.getValue();
    var validatorLength = this.validators.length;
    var isValid = this.validators[validatorLength - 1].condition(this, this.form, value);
    var isRequired = this.required;
    if (typeof this.name === 'undefined') {
      this.name = 'isc_' + this.getRandomString(this.getRandomInteger(6, 12));
    }
    if (isValid === false) {
      this.form.addFieldErrors(this.name, isc.OBDateItem.invalidValueLabel, false);
      this.form.markForRedraw();
    } else if (isRequired === true &&
    (value === null || value === '' || typeof value === 'undefined')) {
      this.form.addFieldErrors(this.name, isc.OBDateItem.requiredValueLabel, false);
      this.form.markForRedraw();
    } else {
      this.form.clearFieldErrors(this.name, false);
      this.form.markForRedraw();
    }
  },
  
  validateOBDateItem: function(value){
    var dateValue = OB.Utilities.Date.OBToJS(value, this.dateFormat);
    var isValid = true;
    if (this.getValue() && dateValue === null) {
      isValid = false;
    }
    var isRequired = this.required;
    if (isValid === false) {
      return false;
    } else if (isRequired === true && value === null) {
      return false;
    }
    return true;
  },
  
  validators: [{
    type: 'custom',
    condition: function(item, validator, value){
      return item.validateOBDateItem(value);
    }
  }]
 
});

OB.I18N.getLabel('OBUIAPP_InvalidValue', null, isc.OBDateItem, 'invalidValueLabel');
OB.I18N.getLabel('OBUISC_Validator.requiredField', null, isc.OBDateItem, 'requiredValueLabel');
