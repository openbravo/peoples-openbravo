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
 * All portions are Copyright (C) 2010-2011 Openbravo SLU
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
// * OBListFilterItem: used in the filter editor

// == OBCheckboxItem ==
// Item used for Openbravo yes/no fields.
isc.ClassFactory.defineClass('OBCheckboxItem', CheckboxItem);

isc.OBCheckboxItem.addProperties({
  // no validation on change or exit here
  textBoxStyle: 'OBFormFieldLabel',
  showValueIconOver: true,
  showValueIconFocused: true,
  defaultValue: false,
  checkedImage: '[SKIN]/../../org.openbravo.client.application/images/form/checked.png',
  uncheckedImage: '[SKIN]/../../org.openbravo.client.application/images/form/unchecked.png'
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
  var length, i, hiddenInputName, targetFld = isc.OBSearchItem.openSearchItem,
    currentValue = targetFld.getValue();
  if (action === 'SAVE' && currentValue !== value) {    
    if (!targetFld.valueMap) {
      targetFld.valueMap = {};
    }
    
    targetFld.setValue(value);
    if (!targetFld.valueMap) {
      targetFld.valueMap = {};
    }
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
  showFocused: true,
  wrap: false,
  clipValue: true,
  
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
  
  // NOTE: FormItem don't have initWidget but use init
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

  // show the complete displayed value, handy when the display value got clipped
  itemHoverHTML: function (item, form) {
    return this.getDisplayValue(this.getValue());
  },
  
  click: function() {
    this.showPicker();
    return false;
  },
  
  keyPress: function(item, form, keyName, characterValue){
    if (keyName === 'Enter') {
      this.showPicker();
      return false;
    }
    return true;
  }, 

  showPicker: function(){
    if (this.isDisabled()) {
      return;
    }
    var parameters = [], index = 0, i = 0, length, propDef, inpName, values;
    var form = this.form, view = form.view;
    if (this.isFocusable()) {
      this.focusInItem(); 
    }
    parameters[index++] = 'inpIDValue';
    if (this.getValue()) {
      parameters[index++] = this.getValue();
    } else {
      parameters[index++] = '';
    }
    parameters[index++] = 'WindowID';
    parameters[index++] = view.standardWindow.windowId;
    values = view.getContextInfo(false, true, true, true);
    length = this.inFields.length;
    for (i = 0; i < length; i++) {
      inpName = this.inFields[i];
      propDef = view.getPropertyDefinitionFromInpColumnName(inpName);
      if (propDef && values[inpName]) {        
        // note the name passed is not the same as the inp name, it is inp + dbcolumn
        parameters[index++] = 'inp' + propDef.dbColumn;
        parameters[index++] = values[inpName];
        // and to be save also pass the value as the input name
        parameters[index++] = inpName;
        parameters[index++] = values[inpName];
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

isc.ClassFactory.defineClass('OBPAttributeSearchItem', OBSearchItem);

isc.OBPAttributeSearchItem.addProperties({
  showPicker: function(){
    if (this.isDisabled()) {
      return;
    }
    var parameters = [], index = 0, i = 0, length, propDef, inpName, values;
    var form = this.form, view = form.view;
    if (this.isFocusable()) {
      this.focusInItem();
    }
    parameters[index++] = 'inpKeyValue';
    if (this.getValue()) {
      parameters[index++] = this.getValue();
    } else {
      parameters[index++] = '';
    }
    values = view.getContextInfo(false, true, true, true);
    parameters[index++] = 'WindowID';
    parameters[index++] = view.standardWindow.windowId;
    parameters[index++] = 'inpwindowId';
    parameters[index++] = view.standardWindow.windowId;
    parameters[index++] = 'inpProduct';
    parameters[index++] = values.inpmProductId;
    this.openSearchWindow('/info/AttributeSetInstance.html', parameters, this.getValue());
  }
});
// == OBEncryptedItem ==
// The type used for encrypted items.
isc.ClassFactory.defineClass('OBEncryptedItem', isc.PasswordItem);

// add specific properties here
isc.OBEncryptedItem.addProperties({
  changed : function(form,item,value) {
	this.form.setValue(item.name + '.cleartext', value);
  }
});

// == OBFormButton ==
// The default form button.
isc.ClassFactory.defineClass('OBFormButton', Button);

isc.OBFormButton.addProperties({
  autoFit: true,
  baseStyle: 'OBFormButton',
  titleStyle: 'OBFormButtonTitle'
});

// == OBTextItem ==
// Input for normal strings
isc.ClassFactory.defineClass('OBTextItem', TextItem);

isc.OBTextItem.addProperties({
  validateOnExit: true
});

//== OBFKFilterTextItem ==
//Input used for filtering on FK fields.
isc.ClassFactory.defineClass('OBFKFilterTextItem', TextItem);

isc.OBFKFilterTextItem.addProperties({
  validateOnExit: false,
  validateOnChange: false
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
  canFocus: true,
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
  },
  
  collapseSection: function(){
    // when collapsing set the focus to the header
    this.form.setFocusItem(this);
    var ret = this.Super('collapseSection', arguments);
    return ret;
  },
  
  expandSection: function(){
    var ret = this.Super('expandSection', arguments);
    
    // when expanding set the focus to the first focusable item     
    // set focus with a short delay to give the section time to draw
    this.delayCall('setNewFocusItemExpanding', [], 100);
    
    // NOTE: if the layout structure changes then this needs to be 
    // changed probably to see where the scrollbar is to scroll
    // the parentElement is not set initially when drawing
    if (this.form.parentElement) {
      // scroll after things have been expanded
      this.form.parentElement.delayCall('scrollTo', [null, this.getTop()], 100);    
    }

    return ret;
  },
    
  setNewFocusItemExpanding: function(){
    var newFocusItem = this;
    for (var i = 0; i < this.itemIds.length; i++) {
      var itemName = this.itemIds[i], item = this.form.getItem(itemName);
      // isFocusable is a method added in ob-smartclient.js
      if (item.isFocusable()) {
        newFocusItem = item;
        break;
      }
    }
    newFocusItem.focusInItem();
  }
  
});

// == OBListItem ==
// Combo box for list references, note is extended by OBFKItem again
isc.ClassFactory.defineClass('OBListItem', ComboBoxItem);

isc.OBListItem.addProperties({

  showPickListOnKeypress: true,  
  cachePickListResults: false,
  validateOnExit: true,  
  completeOnTab: true,
  // setting this to false means that the change handler is called when picking
  // a value and not earlier
  addUnknownValues: false,
  selectOnFocus: true,

  pickListProperties: {
    showHeaderContextMenu: false
  },

  // prevent ids from showing up
  mapValueToDisplay : function (value) {
    var ret = this.Super('mapValueToDisplay', arguments);
    if (ret === value && this.isDisabled()) {
      return '';
    }
    if (ret === value && !this.valueMap) {
      this.valueMap = {};
      this.valueMap[value] = '';
      return '';
    }
    return ret;
  }
  
});

//== OBListFilterItem ==
// Combo box for list references in filter editors
isc.ClassFactory.defineClass('OBListFilterItem', OBListItem);

isc.OBListFilterItem.addProperties({
});

// == OBFKItem ==
// Extends OBListItem
isc.ClassFactory.defineClass('OBFKItem', isc.OBListItem);

isc.ClassFactory.mixInInterface('OBFKItem', 'OBLinkTitleItem');

isc.OBFKItem.addProperties({
  textMatchStyle: 'substring',
    
  // set the identifier field also, that's what gets displayed in the grid
  changed: function (form, item, value) {
    var display = this.mapValueToDisplay(value);
    form.setValue(this.name + '.' + OB.Constants.IDENTIFIER, display);
    return this.Super('changed', [arguments]);
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
  firstDayOfWeek: 1,
  autoHide: true,
  showCancelButton: true,
  todayButtonTitle: OB.I18N.getLabel('OBUISC_DateChooser.todayButtonTitle'),
  cancelButtonTitle: OB.I18N.getLabel('OBUISC_DateChooser.cancelButtonTitle')
});

if (isc.OBDateChooser) {  // To force SC to load OBDateChooser instead of DateChooser
  isc.DateChooser.getSharedDateChooser = function(properties) {
    return isc.OBDateChooser.create(properties);
  };
}


//== OBTimeItem ==
//OBTimeItem handles time values.
isc.ClassFactory.defineClass('OBTimeItem', TimeItem);

isc.OBTimeItem.addProperties({
  validateOnExit: true,
  showHint: false,
  displayFormat: 'to24HourTime',
  short24TimeFormat: 'HH:MM:SS',
  shortTimeFormat: 'HH:MM:SS',
  long24TimeFormat: 'HH:MM:SS',
  longTimeFormat: 'HH:MM:SS'
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
  
  // ** {{{ change }}} **
  // Called when changing a value.
  change: function(form, item, value, oldValue){ /* transformInput */
    var isADate = value !== null &&
              Object.prototype.toString.call(value) === '[object Date]';
    if (isADate) {
      return;
    }
    // prevent change events from happening
    var completedDate = OBDateItem.autoCompleteDate(item.dateFormat, value, this);
    if (completedDate !== oldValue) {
      item.setValue(completedDate);
    }
  },
  
  // to prevent infinite looping as setFormErrors will also blur
  inBlur: false,

  // compare while ignoring milli difference
  compareValues : function (value1, value2) {
    // not a date let the super class do it
    if (!isc.isA.Date(value1) || !isc.isA.Date(value2)) {
      return this.Super('compareValues', arguments);
    }
    var difference = value1.getTime() - value2.getTime();
    if (difference < -1000) {
      return 1;
    } else if (difference > 1000) {
      return -1;
    } else {
      return 0;
    }
  },
  
  // ** {{{ blur }}} **
  // Called when the focus leaves the field (sets value and validates)
  blur: function(){
    var newValue = OBDateItem.expandDateYear(this.dateFormat, this.getValue()),
    oldValue = this.getValue();
    
    if (oldValue !== newValue) {      
      this.setValue(newValue);
    }
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
  
  keyPressFilterNumeric: '[0-9.,-=]',

  allowMath: true,
  
  validateOnExit: true,
  valueValidator: null,
  
  init: function(){
    this.setKeyPressFilter(this.keyPressFilterNumeric);
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
    var decSeparator = this.getDecSeparator();
    
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

  manageEqualSymbol: function() {
    var obj = this;
    var caretPosition = 0;
    if (this.getSelectionRange()) {
      caretPosition = obj.getSelectionRange()[0];
    }
    setTimeout(function(){
      // can happen when a dynamic form has already been removed
      if (!obj.getElementValue()) {
        return;
      }
      var inputValue = obj.getElementValue().toString();
      var checkA = false; // Checks if there is a = in the beginning
      var checkB = false; // Checks if any undesired = is/has to be removed from the inputValue

      if (inputValue.indexOf('=') === 0) {
        checkA = true;
      }
      if (obj.allowMath) {
        while (inputValue.indexOf('=',1) !== -1) {
          checkB = true;
          if (checkA) {
            inputValue = inputValue.substring(1, inputValue.length);
          }
          inputValue = inputValue.replace('=', '');
          if (checkA) {
            inputValue = '=' + inputValue;
          }
        }
      } else {
        while (inputValue.indexOf('=') !== -1) {
          checkB = true;
          inputValue = inputValue.replace('=', '');
        }
      }

      if (checkA && obj.allowMath) {
        obj.setKeyPressFilter('');
      } else {
        obj.setKeyPressFilter(obj.keyPressFilterNumeric);
      }

      if (checkB) {
        obj.setElementValue(inputValue);
        obj.setSelectionRange(caretPosition, caretPosition);
      }
    }, 5);
  },
  
  keyDown: function(item, form, keyName){
    this.keyDownAction(item, form, keyName);
  },

  keyDownAction: function(item, form, keyName){
    var keyCode = isc.EventHandler.lastEvent.nativeKeyCode;
    this.manageEqualSymbol();
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

  checkMathExpression: function(expression) {
    var jsExpression = expression;
    var dummy = 'xyxdummyxyx';

    function replaceAll(text, what, byWhat) {
      while (text.toString().indexOf(what) !== -1) {
        text = text.toString().replace(what, dummy);
      }
      while (text.toString().indexOf(dummy) !== -1) {
        text = text.toString().replace(dummy, byWhat);
      }
      return text;
    }
    jsExpression = jsExpression.substring(1, jsExpression.length);

    jsExpression = replaceAll(jsExpression, '.', '');
    jsExpression = replaceAll(jsExpression, ',', '');
    jsExpression = replaceAll(jsExpression, ';', '');
    jsExpression = replaceAll(jsExpression, '(', '');
    jsExpression = replaceAll(jsExpression, ')', '');
    jsExpression = replaceAll(jsExpression, ' ', '');

    jsExpression = replaceAll(jsExpression, '0', '');
    jsExpression = replaceAll(jsExpression, '1', '');
    jsExpression = replaceAll(jsExpression, '2', '');
    jsExpression = replaceAll(jsExpression, '3', '');
    jsExpression = replaceAll(jsExpression, '4', '');
    jsExpression = replaceAll(jsExpression, '5', '');
    jsExpression = replaceAll(jsExpression, '6', '');
    jsExpression = replaceAll(jsExpression, '7', '');
    jsExpression = replaceAll(jsExpression, '8', '');
    jsExpression = replaceAll(jsExpression, '9', '');

    jsExpression = replaceAll(jsExpression, '+', '');
    jsExpression = replaceAll(jsExpression, '-', '');
    jsExpression = replaceAll(jsExpression, '*', '');
    jsExpression = replaceAll(jsExpression, '/', '');
    jsExpression = replaceAll(jsExpression, '%', '');

    jsExpression = replaceAll(jsExpression, 'E', '');
    jsExpression = replaceAll(jsExpression, 'LN2', '');
    jsExpression = replaceAll(jsExpression, 'LN10', '');
    jsExpression = replaceAll(jsExpression, 'LOG2E', '');
    jsExpression = replaceAll(jsExpression, 'LOG10E', '');
    jsExpression = replaceAll(jsExpression, 'PI', '');
    jsExpression = replaceAll(jsExpression, 'SQRT1_2', '');
    jsExpression = replaceAll(jsExpression, 'SQRT2', '');

    jsExpression = replaceAll(jsExpression, 'abs', '');
    jsExpression = replaceAll(jsExpression, 'acos', '');
    jsExpression = replaceAll(jsExpression, 'asin', '');
    jsExpression = replaceAll(jsExpression, 'atan', '');
    jsExpression = replaceAll(jsExpression, 'atan2', '');
    jsExpression = replaceAll(jsExpression, 'ceil', '');
    jsExpression = replaceAll(jsExpression, 'cos', '');
    jsExpression = replaceAll(jsExpression, 'exp', '');
    jsExpression = replaceAll(jsExpression, 'floor', '');
    jsExpression = replaceAll(jsExpression, 'log', '');
    jsExpression = replaceAll(jsExpression, 'max', '');
    jsExpression = replaceAll(jsExpression, 'min', '');
    jsExpression = replaceAll(jsExpression, 'pow', '');
    jsExpression = replaceAll(jsExpression, 'random', '');
    jsExpression = replaceAll(jsExpression, 'round', '');
    jsExpression = replaceAll(jsExpression, 'sin', '');
    jsExpression = replaceAll(jsExpression, 'sqrt', '');
    jsExpression = replaceAll(jsExpression, 'tan', '');

    if (jsExpression === '') {
      return true;
    } else {
      return false;
    }
  },

  // ** {{{ evalMathExpression }}} **
  // evalMathExpression allows you to perform mathematical tasks.
  //
  // All operations can be done by using the symbol = at the beginning of the numeric input
  //
  // Syntax examples:
  // =PI // Returns 3.14159
  // =1+2+3 // Returns 6
  // =sqrt(16) // Returns 4
  //
  // Binary operations:
  // a + b             Add a and b
  // a - b             Subtract b from a
  // a * b             Multiply a by b
  // a / b             Divide a by b
  // a % b             Find the remainder of division of a by b
  //
  // Constants:
  // E                 Returns Euler's number (approx. 2.718)
  // LN2               Returns the natural logarithm of 2 (approx. 0.693)
  // LN10              Returns the natural logarithm of 10 (approx. 2.302)
  // LOG2E             Returns the base-2 logarithm of E (approx. 1.442)
  // LOG10E            Returns the base-10 logarithm of E (approx. 0.434)
  // PI                Returns PI (approx. 3.14159)
  // SQRT1_2           Returns the square root of 1/2 (approx. 0.707)
  // SQRT2             Returns the square root of 2 (approx. 1.414)
  //
  // Operator functions
  // abs(x)            Returns the absolute value of x
  // acos(x)           Returns the arccosine of x, in radians
  // asin(x)           Returns the arcsine of x, in radians
  // atan(x)           Returns the arctangent of x as a numeric value between -PI/2 and PI/2 radians
  // atan2(y;x)        Returns the arctangent of the quotient of its arguments
  // ceil(x)           Returns x, rounded upwards to the nearest integer
  // cos(x)            Returns the cosine of x (x is in radians)
  // exp(x)            Returns the value of Ex
  // floor(x)          Returns x, rounded downwards to the nearest integer
  // log(x)            Returns the natural logarithm (base E) of x
  // max(x;y;z;...;n)  Returns the number with the highest value
  // min(x;y;z;...;n)  Returns the number with the lowest value
  // pow(x;y)          Returns the value of x to the power of y
  // random()          Returns a random number between 0 and 1
  // round(x)          Rounds x to the nearest integer
  // sin(x)            Returns the sine of x (x is in radians)
  // sqrt(x)           Returns the square root of x
  // tan(x)            Returns the tangent of an angle
  evalMathExpression: function(expression) {
    if (!this.checkMathExpression(expression)) {
      return 'error';
    }
    var jsExpression = expression;
    var dummy = 'xyxdummyxyx';
    var result;
    var decSeparator = this.getDecSeparator();
    var groupSeparator = this.getGroupSeparator();
    function replaceAll(text, what, byWhat) {
      while (text.toString().indexOf(what) !== -1) {
        text = text.toString().replace(what, dummy);
      }
      while (text.toString().indexOf(dummy) !== -1) {
        text = text.toString().replace(dummy, byWhat);
      }
      return text;
    }
    jsExpression = jsExpression.substring(1, jsExpression.length);

    jsExpression = replaceAll(jsExpression, groupSeparator, '');
    jsExpression = replaceAll(jsExpression, decSeparator, '.');
    jsExpression = replaceAll(jsExpression, ';', ',');

    jsExpression = replaceAll(jsExpression, 'E', 'Math.E');
    jsExpression = replaceAll(jsExpression, 'LN2', 'Math.LN2');
    jsExpression = replaceAll(jsExpression, 'LN10', 'Math.LN10');
    jsExpression = replaceAll(jsExpression, 'LOG2E', 'Math.LOG2E');
    jsExpression = replaceAll(jsExpression, 'LOG10E', 'Math.LOG10E');
    jsExpression = replaceAll(jsExpression, 'PI', 'Math.PI');
    jsExpression = replaceAll(jsExpression, 'SQRT1_2', 'Math.SQRT1_2');
    jsExpression = replaceAll(jsExpression, 'SQRT2', 'Math.SQRT2');

    jsExpression = replaceAll(jsExpression, 'abs', 'Math.abs');
    jsExpression = replaceAll(jsExpression, 'acos', 'Math.acos');
    jsExpression = replaceAll(jsExpression, 'asin', 'Math.asin');
    jsExpression = replaceAll(jsExpression, 'atan', 'Math.atan');
    jsExpression = replaceAll(jsExpression, 'atan2', 'Math.atan2');
    jsExpression = replaceAll(jsExpression, 'ceil', 'Math.ceil');
    jsExpression = replaceAll(jsExpression, 'cos', 'Math.cos');
    jsExpression = replaceAll(jsExpression, 'exp', 'Math.exp');
    jsExpression = replaceAll(jsExpression, 'floor', 'Math.floor');
    jsExpression = replaceAll(jsExpression, 'log', 'Math.log');
    jsExpression = replaceAll(jsExpression, 'max', 'Math.max');
    jsExpression = replaceAll(jsExpression, 'min', 'Math.min');
    jsExpression = replaceAll(jsExpression, 'pow', 'Math.pow');
    jsExpression = replaceAll(jsExpression, 'random', 'Math.random');
    jsExpression = replaceAll(jsExpression, 'round', 'Math.round');
    jsExpression = replaceAll(jsExpression, 'sin', 'Math.sin');
    jsExpression = replaceAll(jsExpression, 'sqrt', 'Math.sqrt');
    jsExpression = replaceAll(jsExpression, 'tan', 'Math.tan');

    try {
      result = eval(jsExpression);
      if (isNaN(result)) {
        result = 'error';
      }
    } catch (e) {
      result = 'error';
    }

    //result = replaceAll(result, '.', decSeparator);
    return result;
  },
  
  blur: function(){
    var value = this.getElementValue().toString();
    var expressionValue;
    var obj = this;
    if (this.allowMath && value.indexOf('=') === 0) {
      expressionValue = this.evalMathExpression(value);
      if (expressionValue !== 'error') {
        expressionValue = parseFloat(expressionValue, 10);
      } else {
        setTimeout(function() {
          obj.setElementValue(value);
        }, 50);
        return this.Super('blur', arguments);
      }
      this.setValue(expressionValue);
      this.validate();
    }

    value = this.getValue();
    // first check if the number is valid
    if (!isc.isA.String(value)) {
      // format the value displayed
      this.setElementValue(this.mapValueToDisplay(value));
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