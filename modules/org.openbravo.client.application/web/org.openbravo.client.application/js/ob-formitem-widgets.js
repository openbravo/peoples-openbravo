/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.1  (the  "License"),  being   the  Mozilla   Public  License
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
// * OBCheckBoxItem: yes/no item.
// * OBSearchItem: item used for search fields.
// * OBFormButton: button used in forms.
// * OBTextItem: string/text item
// * OBTextAreaItem: string/text-area item
// * OBDateItem: FormItem for dates
// * OBDateTimeItem: FormItem for DateTime
// * OBNumber: FormItem for numbers
// * OBYesNoItem: combo box for yes/no values
// * OBLinkTitleItem: an interface supporting a link button in the title.
// * OBFKItem: combo box for foreign key references
// * OBListItem: combo box for list references

// == OBCheckboxItem ==
// Item used for Openbravo yes/no fields.
isc.ClassFactory.defineClass('OBCheckboxItem', CheckboxItem);

isc.OBCheckboxItem.addProperties({
  // no validation on change or exit here
  
  textBoxStyle: 'OBFormFieldLabel',
  defaultValue: false
});

// == OBLinkTitleItem ==
// Item used for creating a link button in the title. Note part of the logic
// is implemented in the OBViewForm.getTitleHTML.
isc.ClassFactory.defineInterface('OBLinkTitleItem');

isc.OBLinkTitleItem.addProperties({
  showLinkIcon: true,
  
  linkButtonClick: function(){
    var sourceWindow = this.form.view.standardWindow.windowId;
    OB.Utilities.openDirectView(sourceWindow, this.referencedKeyColumnName, this.targetEntity, this.getValue());
  }
});

// == OBSearchItem ==
// Item used for Openbravo search fields.
isc.ClassFactory.defineClass('OBSearchItem', StaticTextItem);

isc.ClassFactory.mixInInterface('OBSearchItem', 'OBLinkTitleItem');

// a global function as it is called from classic windows
function closeSearch(action, value, display, parameters, wait){
  var length, i, hiddenInputName, targetFld = isc.OBSearchItem.openSearchItem;
  if (action === 'SAVE') {
    if (!targetFld.valueMap) {
      targetFld.valueMap = {};
    }
    
    targetFld.setValue(value);
    targetFld.valueMap[targetFld.getValue()] = display;
    targetFld.form.setValue(targetFld.displayField, display);
    targetFld.updateValueMap(true);
    
    if (parameters && parameters.length > 0) {
      length = parameters.length;
      for (i = 0; i < length; i++) {
        hiddenInputName = ((parameters[i].esRef) ? targetFld.inpColumnName : '') + parameters[i].campo;
        // Revisit for grid editor, maybe setting the value in the form will set it
        // in the record to be kepped there 
        targetFld.form.hiddenInputs[hiddenInputName] = parameters[i].valor;
      }
    }
    targetFld._hasChanged = true;
    targetFld.form.handleItemChange(targetFld);
  }
  isc.OBSearchItem.openedWindow.close();
  isc.OBSearchItem.openSearchItem = null;
}

isc.OBSearchItem.addProperties({
  showPickerIcon: true,
  canFocus: true,

  setValue: function(value){
    var ret = this.Super('setValue', arguments);
    // in this case the clearIcon needs to be shown or hidden
    if (!this.disabled && !this.required) {
      if (value) {
        this.showIcon(this.instanceClearIcon);
      } else {
        this.hideIcon(this.instanceClearIcon);
      }
    }
    return ret;
  },
  
  init: function(){
    this.instanceClearIcon = isc.shallowClone(this.clearIcon);
    this.instanceClearIcon.formItem = this;
    
    this.instanceClearIcon.showIf = function(form, item){
      if (item.disabled) {
        return false;
      }
      if (item.required) {
        return false;
      }
      if (form && form.view && form.view.readOnly) {
        return false;
      }
      if (item.getValue()) {
        return true;
      }
      return false;
    };
    
    this.instanceClearIcon.click = function(){
      this.formItem.setValue(null);
      this.formItem.form.itemChangeActions();
    };
    
    this.icons = [this.instanceClearIcon];
    
    return this.Super('init', arguments);
  },
  
  changed: function(){
    var ret = this.Super('changed', arguments);
    if (this.form && this.form.handleItemChange) {
      this.form.handleItemChange(this);
    }
    return ret;
  },
  
  showPicker: function(){
    var parameters = [], index = 0, i = 0, length, fld, inpName;
    parameters[index++] = 'inpIDValue';
    if (this.getValue()) {
      parameters[index++] = this.getValue();
    } else {
      parameters[index++] = '';
    }
    parameters[index++] = 'WindowID';
    parameters[index++] = this.form.view.standardWindow.windowId;
    length = this.inFields.length;
    for (i = 0; i < length; i++) {
      inpName = this.inFields[i];
      fld = this.form.getFieldFromInpColumnName(inpName);
      if (fld && fld.getValue()) {
        parameters[index++] = 'inp' + fld.columnName;
        parameters[index++] = fld.getValue();
      }
    }
    this.openSearchWindow(this.searchUrl, parameters, this.getValue());
  },
  
  openSearchWindow: function(url, parameters, strValueID){
    var height, width, top, left;
    var complementsNS4 = '';
    var auxField = '';
    var hidden;
    
    if (url.indexOf('Location') !== -1) {
      height = 300;
      width = 600;
    } else {
      height = (screen.height - 100);
      width = 900;
    }
    top = parseInt((screen.height - height) / 2, 10);
    left = parseInt((screen.width - width) / 2, 10);
    
    if (isc.OBSearchItem.openedWindow) {
      isc.OBSearchItem.openedWindow.close();
      this.clearUnloadEventHandling();
    }
    isc.OBSearchItem.openedWindow = null;
    
    if (strValueID) {
      auxField = 'inpNameValue=' + encodeURIComponent(this.form.getValue(this.displayField));
    }
    if (parameters) {
      var total = parameters.length;
      for (var i = 0; i < total; i++) {
        if (auxField !== '') {
          auxField += '&';
        }
        // TODO: check this
        //        if (parameters[i] === 'isMultiLine' && parameters[i + 1] == 'Y') {
        //          gIsMultiLineSearch = true;
        //        }
        auxField += parameters[i] + '=' + ((parameters[i + 1] !== null) ? encodeURIComponent(parameters[i + 1]) : '');
        if (parameters[i] === 'Command') {
          hidden = true;
        }
        i++;
      }
    }
    
    if (navigator.appName.indexOf('Netscape')) {
      complementsNS4 = 'alwaysRaised=1, dependent=1, directories=0, hotkeys=0, menubar=0, ';
    }
    var complements = complementsNS4 + 'height=' + height + ', width=' + width + ', left=' + left + ', top=' + top + ', screenX=' + left + ', screenY=' + top + ', location=0, resizable=1, scrollbars=1, status=0, toolbar=0, titlebar=0, modal=\'yes\'';
    isc.OBSearchItem.openedWindow = window.open(OB.Application.contextUrl + url + ((auxField === '') ? '' : '?' + auxField), 'SELECTOR', complements);
    if (isc.OBSearchItem.openedWindow) {
      isc.OBSearchItem.openedWindow.focus();
      this.setUnloadEventHandling();
    }
    isc.OBSearchItem.openSearchItem = this;
  },
  
  setUnloadEventHandling: function(){
    var me = this;
    if (document.layers) {
      document.captureEvents(Event.UNLOAD);
    }
    window.onunload = function(){
      if (isc.OBSearchItem.openedWindow) {
        isc.OBSearchItem.openedWindow.close();
      }
      isc.OBSearchItem.openedWindow = null;
      me.clearUnloadEventHandling();
    };
  },
  
  clearUnloadEventHandling: function(){
    if (document.layers) {
      window.releaseEvents(Event.UNLOAD);
    }
    window.onunload = function(){
    };
  }
});

// == OBEncryptedItem ==
// The type used for encrypted items.
isc.ClassFactory.defineClass('OBEncryptedItem', isc.PasswordItem);

// add specific properties here
isc.OBEncryptedItem.addProperties({});

// == OBFormButton ==
// The default form button.
isc.ClassFactory.defineClass('OBFormButton', Button);

isc.OBFormButton.addProperties({
  autoFit: true
});

// == OBTextItem ==
// Input for normal strings
isc.ClassFactory.defineClass('OBTextItem', TextItem);

isc.OBTextItem.addProperties({
  validateOnExit: true
});

// == OBTextAreaItem ==
// Input for large strings
isc.ClassFactory.defineClass('OBTextAreaItem', TextAreaItem);

isc.OBTextAreaItem.addProperties({
  validateOnExit: true
});

// used in the grid
isc.ClassFactory.defineClass('OBPopUpTextAreaItem', PopUpTextAreaItem);

isc.OBPopUpTextAreaItem.addProperties({
  validateOnExit: true,
  popUpOnEnter: true
});

// == OBSectionItem ==
// Form sections
isc.ClassFactory.defineClass('OBSectionItem', SectionItem);

isc.OBSectionItem.addProperties({
  // revisit when/if we allow disabling of section items
  // visual state of disabled or non-disabled stays the same now
  showDisabled: false,
  
  initWidget: function(){
    var ret = this.Super('initWidget', arguments);
    return ret;
  },
  
  // never disable a section item
  isDisabled: function(){
    return false;
  }
});

// == OBListItem ==
// Combo box for list references
isc.ClassFactory.defineClass('OBListItem', SelectItem);

isc.OBListItem.addProperties({

  itemData: null,
  
  cachePickListResults: false,
  // combos are validated on change, other items are 
  // validated on exit
  validateOnChange: true,
  
  changed: function(){
    var ret = this.Super('changed', arguments);
    if (this.form && this.form.handleItemChange) {
      this.form.handleItemChange(this);
    }
    return ret;
  },
  
  getValueMap: function(){
    if (this.itemData) {
      return this.itemData;
    }
    return this.Super('getValueMap', arguments);
  }
  
});

// == OBFKItem ==
// Extends SelectItem with suggestion box behavior for foreign key references.
isc.ClassFactory.defineClass('OBFKItem', SelectItem);

isc.ClassFactory.mixInInterface('OBFKItem', 'OBLinkTitleItem');

isc.OBFKItem.addProperties({
  selectOnFocus: true,
  autoFetchData: false,
  completeOnTab: true,
  displayField: OB.Constants.IDENTIFIER,
  valueField: OB.Constants.ID,
  textMatchStyle: 'substring',
  // combos are validated on change, other items are 
  // validated on exit
  validateOnChange: true,
  useClientFiltering: false,
  
  cachePickListResults: false,
  
  itemData: null,
  optionDataSource: null,
  
  getDataSource: function(){
    return this.getOptionDataSource();
  },
  
  changed: function(){
    var ret = this.Super('changed', arguments);
    if (this.form && this.form.handleItemChange) {
      this.form.handleItemChange(this);
    }
    return ret;
  },
  
  getOptionDataSource: function(){
    if (this.optionDataSource) {
      return this.optionDataSource;
    }
    // set a client only datasource
    this.showOptionsFromDataSource = true;
    this.optionDataSource = isc.DataSource.create({
      fields: [{
        name: OB.Constants.ID,
        type: 'text'
      }, {
        name: OB.Constants.IDENTIFIER,
        type: 'text'
      }],
      clientOnly: true,
      testData: this.itemData
    });
    return this.optionDataSource;
  },
  
  getPickListFilterCriteria: function(){
    var criteria = this.Super('getPickListFilterCriteria'), defValue, prop;
    
    // stop here if a client only datasource
    if (this.optionDataSource && this.optionDataSource.clientOnly) {
      return criteria;
    }
    
    if (!criteria) {
      criteria = {};
    }
    
    // and sort according to the display field
    // initially
    criteria[OB.Constants.SORTBY_PARAMETER] = this.displayField;
    
    return criteria;
  }
});

// == OBYesNoItem ==
// Extends SelectItem with preset yes and no values
isc.ClassFactory.defineClass('OBYesNoItem', SelectItem);

isc.OBYesNoItem.addProperties({
  mapValueToDisplay: function(value, a, b, c){
    return OB.Utilities.getYesNoDisplayValue(value);
  },
  formatPickListValue: function(value, record, field, rowNum, colNum){
    return OB.Utilities.getYesNoDisplayValue(value);
  }
});

// == OBDateChooser ==
// OBDateChooser inherits from SmartClient DateChooser
// extends standard DateChooser implementation to be used in OBDateItem
isc.ClassFactory.defineClass('OBDateChooser', DateChooser);

isc.OBDateChooser.addProperties({
  firstDayOfWeek: 1  
});

// == OBDateItem ==
// OBDateItem inherits from SmartClient DateItem
// adds autocomplete and formatting based on the Openbravo date pattern
isc.ClassFactory.defineClass('OBDateItem', DateItem);

isc.OBDateItem.addClassProperties({

  // ** {{{ autoCompleteData }}} **
  //
  // Autocomplets the date entered.
  // Parameters:
  // * {{{dateFormat}}}: the dateFormat in OB format
  // * {{{value}}}: the current entered value
  autoCompleteDate: function(dateFormat, value, item){
    // if (!isTabPressed) {
    if (value === null) {
      return value;
    }
    fmt = OB.Utilities.Date.normalizeDisplayFormat(dateFormat);
    try {
      if (item.getSelectionRange() && item.getSelectionRange()[0] !== value.length) {
        return value; // If we are inserting in a position different from
        // the
        // last one, we don't autocomplete
      }
    } catch (ignored) {
    }
    var strDate = value;
    var b = fmt.match(/%./g);
    var i = 0, j = -1;
    var text = '';
    var length = 0;
    var pos = fmt.indexOf(b[0]) + b[0].length;
    var separator = fmt.substring(pos, pos + 1);
    var separatorH = '';
    pos = fmt.indexOf('%H');
    if (pos !== -1) {
      separatorH = fmt.substring(pos + 2, pos + 3);
    }
    while (strDate.charAt(i)) {
      if (strDate.charAt(i) === separator ||
      strDate.charAt(i) === separatorH ||
      strDate.charAt(i) === ' ') {
        i++;
        continue;
      }
      if (length <= 0) {
        j++;
        if (j > 0) {
          if (b[j] === '%H') {
            text += ' ';
          } else if (b[j] === '%M' || b[j] === '%S') {
            text += separatorH;
          } else {
            text += separator;
          }
        }
        switch (b[j]) {
          case '%d':
          case '%e':
            text += strDate.charAt(i);
            length = 2;
            break;
          case '%m':
            text += strDate.charAt(i);
            length = 2;
            break;
          case '%Y':
            text += strDate.charAt(i);
            length = 4;
            break;
          case '%y':
            text += strDate.charAt(i);
            length = 2;
            break;
          case '%H':
          case '%I':
          case '%k':
          case '%l':
            text += strDate.charAt(i);
            length = 2;
            break;
          case '%M':
            text += strDate.charAt(i);
            length = 2;
            break;
          case '%S':
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
    // if (i > 7 && (typeof (field.onchange)!='undefined'))
    // field.onchange();
    // }
  },
  
  // ** {{{ getDateBlock }}} **
  //
  // Return the part of the date denoted by the block (which is a number).
  // Parameters:
  // * {{{str_date}}}: the complete date
  // * {{{block}}}: number from 1 to 3, denotes the part of the date to
  // return
  getDateBlock: function(str_date, block){
    var datePattern = '^(\\d+)[\\-|\\/|/|:|.|\\.](\\d+)[\\-|\\/|/|:|.|\\.](\\d+)$';
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
  // * {{{dateFormat}}}: the date format in OB syntax
  // * {{{value}}}: the date to expand
  expandDateYear: function(dateFormat, value){
    var str_date = value;
    var str_dateFormat = dateFormat.replace('yyyy', 'YYYY');
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
    } else if (str_dateFormat.substr(7, 1) === 'Y') {
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
    
    var dateSeparator = str_dateFormat.toUpperCase().replace(/D/g, '').replace(/M/g, '').replace(/Y/g, '').substr(0, 1);
    var normalizedDate = dateBlock[1] + dateSeparator + dateBlock[2] +
    dateSeparator +
    dateBlock[3];
    return normalizedDate;
  }
});

// == OBDateItem properties ==
isc.OBDateItem.addProperties({
  // ** {{{ pickerConstructor }}} **
  // Picker constructor class
  pickerConstructor: 'OBDateChooser',

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
  
  // ** {{{ change }}} **
  // Called when changing a value.
  change: function(form, item, value, oldValue){ /* transformInput */
    var isADate = value !== null &&
    Object.prototype.toString.call(value) === '[object Date]';
    if (isADate) {
      return;
    }
    item.setValue(OBDateItem.autoCompleteDate(item.dateFormat, value, this));
  },
  
  // to prevent infinite looping as setFormErrors will also blur
  inBlur: false,
  
  // ** {{{ blur }}} **
  // Called when the focus leaves the field (sets value and validates)
  blur: function(){
    this.setValue(OBDateItem.expandDateYear(this.dateFormat, this.getValue()));
    if (!this.inBlur) {
      this.inBlur = true;
      this.checkOBDateItemValue();
      this.inBlur = false;
    }
    return this.Super('blur', arguments);
  },
  
  // ** {{{ displayFormat }}} **
  // Formats a date object to a String.
  //  displayFormat: function(){
  //    // this: is the date object to format
  //    var displayedDate = OB.Utilities.Date.JSToOB(this, OB.Format.date);
  //    return displayedDate;
  //  },
  //  
  //  // ** {{{ inputFormat }}} **
  //  // Parses the inputted value to javascript Date.
  //  inputFormat: function(value){
  //    return OB.Utilities.Date.OBToJS(value, OB.Format.date);
  //  },
  
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
    if (this.getValue() !== null && dateValue === null) {
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

isc.ClassFactory.defineClass('OBDateTimeItem', isc.OBDateItem);

// = OBDateTimeItem =
// The Openbravo DateTime form item.
//isc.OBDateTimeItem.addProperties({
//  // ** {{{ dateFormat }}} **
//  // The date time format function.
//  dateFormat: OB.Format.dateTime,
//  
//  // ** {{{ displayFormat }}} **
//  // Formats the date to a string.
//  displayFormat: function(){
//    // this: is the date object to format
//    var displayedDate = OB.Utilities.Date.JSToOB(this, OB.Format.dateTime);
//    return displayedDate;
//  },
//  
//  // ** {{{ inputFormat }}} **
//  // Parses an input to a Date.
//  inputFormat: function(value){
//    return OB.Utilities.Date.OBToJS(value, OB.Format.dateTime);
//  }
//});

isc.ClassFactory.defineClass('OBNumberItem', TextItem);

// = OBNumberItem =
// The Openbravo numeric form item.
isc.OBNumberItem.addProperties({
  typeInstance: null,
  
  keyPressFilter: '[0-9.,-]',
  
  validateOnExit: true,
  valueValidator: null,
  
  init: function(){
    this.typeInstance = SimpleType.getType(this.type);
    var newValidators = [];
    // get rid of the isFloat validators, as we have 
    // specific validation based on the format definition
    for (var i = 0; i < this.validators.length; i++) {
      if (this.validators[i].type !== 'isFloat') {
        newValidators.push(this.validators[i]);
      }
      if (this.validators[i].type === 'custom') {
        this.valueValidator = this.validators[i];
      }
    }
    this.validators = newValidators;
    return this.Super('init', arguments);
  },
  
  getMaskNumeric: function(){
    return this.typeInstance.maskNumeric;
  },
  
  getDecSeparator: function(){
    return this.typeInstance.decSeparator;
  },
  
  getGroupSeparator: function(){
    return this.typeInstance.groupSeparator;
  },
  
  getGlobalGroupInterval: function(){
    return OB.Format.defaultGroupingSize;
  },
  
  returnNewCaretPosition: function(numberStr, oldCaretPosition){
    var newCaretPosition = oldCaretPosition;
    for (var i = oldCaretPosition; i > 0; i--) {
      if (numberStr.substring(i - 1, i) === this.getGroupSeparator()) {
        newCaretPosition = newCaretPosition - 1;
      }
    }
    return newCaretPosition;
  },
  
  // focus changes the formatted value to one without grouping
  focusNumberInput: function(){
    var oldCaretPosition = 0;
    if (this.getSelectionRange()) {
      oldCaretPosition = this.getSelectionRange()[0];
    }
    // getElementValue returns the current value string, so not the typed value
    var newCaretPosition = this.returnNewCaretPosition(this.getElementValue(), oldCaretPosition);
    // update the value shown, mapValueToDisplay will call the editFormatter
    
    // get the edit value, without grouping symbol.
    var editValue = OB.Utilities.Number.OBMaskedToOBPlain(this.getElementValue(), this.getDecSeparator(), this.getGroupSeparator());
    this.setElementValue(editValue);
    this.setSelectionRange(newCaretPosition, newCaretPosition);
  },
  
  replaceAt: function(string, what, ini, end){
    if (typeof end === 'undefined' || end === null || end === 'null' ||
    end === '') {
      end = ini;
    }
    if (ini > end) {
      var temp = ini;
      ini = end;
      end = temp;
    }
    var newString = '';
    newString = string.substring(0, ini) + what +
    string.substring(end + 1, string.length);
    return newString;
  },
  
  // handles the decimal point of the numeric keyboard
  manageDecPoint: function(keyCode){
    var decSeparator = this.typeInstance.decSeparator;
    
    if (decSeparator === '.') {
      return true;
    }
    
    var caretPosition = 0;
    if (this.getSelectionRange()) {
      caretPosition = this.getSelectionRange()[0];
    }
    /*
     * if(keyCode>=65 && keyCode<=90) { setTimeout(function() {obj.value =
     * replaceAt(obj.value, '', caretPosition); setCaretToPos(obj,
     * caretPosition);},5); }
     */
    var inpMaxlength = this.length;
    var inpLength = this.getElementValue().length;
    var isInpMaxLength = false;
    if (inpMaxlength === null) {
      isInpMaxLength = false;
    } else if (inpLength >= inpMaxlength) {
      isInpMaxLength = true;
    }
    
    if (navigator.userAgent.toUpperCase().indexOf('OPERA') !== -1 &&
    keyCode === 78) {
      keyCode = 110;
    }
    
    var obj = this;
    if (keyCode === 110) {
      setTimeout(function(){
        var newValue = obj.replaceAt(obj.getElementValue(), decSeparator, caretPosition);
        obj.setElementValue(newValue);
        obj.setSelectionRange(caretPosition + 1, caretPosition + 1);
      }, 5);
    }
    return true;
  },
  
  keyDown: function(item, form, keyName){
    var keyCode = OB.Utilities.getKeyCode();
    this.manageDecPoint(keyCode);
  },
  
  validateOBNumberItem: function(){
    var value = this.getElementValue();
    var isValid = this.valueValidator.condition(this, this.form, value);
    var isRequired = this.required;
    if (isValid === false) {
      this.form.setFieldErrors(this.name, isc.OBDateItem.invalidValueLabel, false);
      this.form.markForRedraw();
      return false;
    } else if (isRequired === true &&
    (value === null || value === '' || typeof value === 'undefined')) {
      this.form.setFieldErrors(this.name, isc.OBDateItem.requiredValueLabel, false);
      this.form.markForRedraw();
      return false;
    } else {
      this.form.clearFieldErrors(this.name, false);
      this.form.markForRedraw();
    }
    return true;
  },
  
  focus: function(form, item){
    if (!this.getErrors()) {
      // only do the focus/reformat if no errors
      this.focusNumberInput();
    }
    return this.Super('focus', arguments);
  },
  
  handleEditorExit: function(){
    var ret = this.Super('handleEditorExit', arguments);
    return ret;
  },
  
  blur: function(){
    // first check if the number is valid
    if (!isc.isA.String(this.getValue())) {
      // format the value displayed
      this.setElementValue(this.mapValueToDisplay(this.getValue()));
    }
    return this.Super('blur', arguments);
  },
  
  validators: [{
    type: 'custom',
    condition: function(item, validator, value){
      if (!item.typeInstance) {
        // this happens when data is validated which is returned from the system
        // and added to the grid
        return true;
      }
      var type = item.typeInstance;
      this.resultingValue = null;
      
      
      // return a formatted value, if it was valid
      if (isc.isA.String(value)) {
        if (OB.Utilities.Number.IsValidValueString(type, value)) {
          this.resultingValue = OB.Utilities.Number.OBMaskedToJS(value, type.decSeparator, type.groupSeparator);
          return true;
        } else {
          return false;
        }
      }
      return OB.Utilities.Number.IsValidValueString(type, item.getElementValue());
    }
  }]
});

