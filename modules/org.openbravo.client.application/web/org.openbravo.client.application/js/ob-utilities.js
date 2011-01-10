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
 * All portions are Copyright (C) 2009-2011 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
// = Openbravo Utilities =
// Defines utility methods in the top-level OB.Utilities object. Utility methods
// are related to date and number formatting, opening popups, displaying yes/no, etc. 
OB.Utilities = {};
OB.Utilities.Date = {};
OB.Utilities.Number = {};

// ** {{{OB.Utilities.openDirectView}}} **
// Open the correct view for a passed in target definition, coming from a certain source Window.
OB.Utilities.openDirectView = function(sourceWindowId, keyColumn, targetEntity, recordId){
  var actionURL = OB.Application.contextUrl + 'utility/ReferencedLink.html';
  
  var callback = function(response, data, request){
    var openObject = {
      viewId: '_' + data.windowId,
      targetEntity: response.clientContext.targetEntity,
      targetRecordId: data.recordId,
      targetTabId: data.tabId,
      tabTitle: data.tabTitle,
      windowId: data.windowId
    };
    OB.Layout.ViewManager.openView(openObject.viewId, openObject);
  };
  
  var reqObj = {
    params: {
      Command: 'JSON',
      inpEntityName: targetEntity,
      inpKeyReferenceId: recordId,
      inpwindowId: sourceWindowId,
      inpKeyReferenceColumnName: keyColumn
    },
    clientContext: {
      targetEntity: targetEntity
    },
    callback: callback,
    evalResult: true,
    httpMethod: 'GET',
    useSimpleHttp: true,
    actionURL: actionURL
  };
  var request = isc.RPCManager.sendRequest(reqObj);
};

// ** {{{OB.Utilities.hasUrlParameter}}} **
// Returns true if the url has a certain parameter with a certain value.
OB.Utilities.hasUrlParameter = function(name, value){
  var url = window.document.location.href, checkPoint = url.indexOf(name + '=' +
  value);
  return checkPoint !== -1;
};

// ** {{{OB.Utilities.getLocationUrlWithoutFragment()}}} **
// Returns the url of the page without the fragment (the part starting with #)
OB.Utilities.getLocationUrlWithoutFragment = function(){
  var url = window.document.location.href, checkPoint = url.indexOf('#');
  if (checkPoint !== -1) {
    url = url.substring(0, checkPoint);
  }
  return url;
};

// ** {{{ OB.Utilities.openProcessPopup(/*String*/ processId }}} **
// Opens a separate window for classic OB windows.
// Parameters:
// * {{{url}}}: the url of the html page to open
// * {{{noFrameSet}}}: if true then the page is opened directly without a
// frameset
OB.Utilities.openProcessPopup = function(/* String */url, noFrameSet){
  var height = 450;
  var width = 625;
  var top = (screen.height - height) / 2;
  var left = (screen.width - width) / 2;
  var adds = 'height=' + height + ', width=' + width + ', left=' + left +
  ', top=' +
  top;
  adds += ', location=0';
  adds += ', scrollbars=1';
  adds += ', status=1';
  adds += ', menubar=0';
  adds += ', toolbar=0';
  adds += ', resizable=1';
  var winPopUp;
  
  if (noFrameSet) {
    winPopUp = window.open(url, 'PROCESS', adds);
  } else {
    winPopUp = window.open('', 'PROCESS', adds);
    var html = '<html>' +
    '<frameset cols="0%,100%" frameborder="no" border="0" framespacing="0" rows="*" id="framesetMenu">' +
    '<frame name="frameMenu" scrolling="no" src="' +
    OB.Application.contextUrl +
    'utility/VerticalMenu.html?Command=LOADING" id="paramFrameMenuLoading"></FRAME>' +
    '<frame name="mainframe" noresize="" src="' +
    url +
    '" id="fieldProcessId"></frame>' +
    '<frame name="hiddenFrame" scrolling="no" noresize="" src=""></frame>' +
    '</frameset>' +
    '</html>';
    
    winPopUp.document.write(html);
    winPopUp.document.close();
  }
  winPopUp.focus();
  return winPopUp;
};

// ** {{{ OB.Utilities.isNonEmptyString(/*String*/ strValue }}} **
// Returns true if the parameter is a valid String which has length > 0
// Parameters:
// * {{{strValue}}}: the value to check
OB.Utilities.isNonEmptyString = function(/* String */strValue){
  if (!strValue) {
    return false;
  }
  strValue = strValue.replace(/^\s*/, '').replace(/\s*$/, '');
  return strValue.length > 0;
};

// ** {{{ OB.Utilities.areEqualWithTrim(/*String*/ str1, /*String*/ str2}}} **
// Returns true if the two strings are equal after trimming them.
// Parameters:
// * {{{str1}}}: the first String to check
// * {{{str2}}}: the second String to compare
OB.Utilities.areEqualWithTrim = function(/* String */str1, /* String */ str2){
  if (!str1 || !str2) {
    return false;
  }
  str1 = str1.replace(/^\s*/, '').replace(/\s*$/, '');
  str2 = str2.replace(/^\s*/, '').replace(/\s*$/, '');
  return str1 === str2;
};

OB.Utilities.processLogoutQueue = function(){
  var q = OB.Utilities.logoutWorkQueue, qElement, result, tab, tabID;
  
  if (q && q.length === 0) {
    return;
  }
  
  if (typeof arguments[1] === 'string') {
    // The 2nd parameter in a sendRequest callback is the 'data' parameter
    // http://www.smartclient.com/docs/7.0rc2/a/b/c/go.html#type..RPCCallback
    result = eval('(' + arguments[1] + ')');
    if (result && result.oberror) {
      if (result.oberror.type === 'Error') {
        tab = OB.MainView.TabSet.getTab(arguments[2].params.tabID);
        if (tab) {
          tab.pane.getAppFrameWindow().location.href = result.redirect;
        }
        q = [];
        return;
      }
    }
  }
  // Process one element at the time,
  // the save callbacks will empty the queue
  qElement = q.pop();
  if (qElement.func && qElement.args !== undefined) {
    qElement.func.apply(qElement.self, qElement.args);
  }
};

// ** {{{ OB.Utilities.logout }}} **
// Logout from the application, removes server side session info and redirects
// the client to the Login page.
OB.Utilities.logout = function(){
  OB.Utilities.logoutWorkQueue = [];
  var q = OB.Utilities.logoutWorkQueue, i, tabs = OB.MainView.TabSet.tabs, tabsLength = tabs.length, appFrame;
  
  // Push the logout process to the 'end' of the queue
  q.push({
    func: OB.RemoteCallManager.call,
    self: this,
    args: ['org.openbravo.client.application.LogOutActionHandler', {}, {}, function(){
      window.location.href = OB.Application.contextUrl;
    }
]
  });
  
  for (i = 0; i < tabsLength; i++) {
    if (tabs[i].pane.Class === 'OBClassicWindow') {
      appFrame = tabs[i].pane.appFrameWindow ||
      tabs[i].pane.getAppFrameWindow();
      if (appFrame && appFrame.isUserChanges) {
        if (appFrame.validate && !appFrame.validate()) {
          q = [];
          return;
        }
        q.push({
          func: tabs[i].pane.saveRecord,
          self: tabs[i].pane,
          args: [tabs[i].ID, OB.Utilities.processLogoutQueue]
        });
      }
    }
  }
  OB.Utilities.processLogoutQueue();
};

// ** {{{ OB.Utilities.getYesNoDisplayValue }}} **
// Returns the Yes label if the passed value is true, the No label if false.
OB.Utilities.getYesNoDisplayValue = function(/* Boolean */value){
  if (value) {
    return OB.I18N.getLabel('OBUISC_Yes');
  } else if (value === false) {
    return OB.I18N.getLabel('OBUISC_No');
  } else {
    return '';
  }
};

// ** {{{ OB.Utilities.applyDefaultValues }}} **
//
// Sets the value for each property in the defaultValues in the Fields object
// if it is not set there yet.
//
// Parameters:
// * {{{fields}}}: the current values
// * {{{defaultValues}}}: the default values to set in the fields object (if the
// property is not set in the fields object).
OB.Utilities.applyDefaultValues = function(/* Object */fields, /* Object */ defaultValues){
  var fieldsLength = fields.length;
  for (var i = 0; i < fieldsLength; i++) {
    var field = fields[i];
    for (var property in defaultValues) {
      if (defaultValues.hasOwnProperty(property)) {
        if (!field[property] && field[property] !== false) {
          field[property] = defaultValues[property];
        }
      }
    }
  }
};

// ** {{{ OB.Utilities.addFormInputsToCriteria }}} **
//
// Adds all input values on the standard OB form (document.frmMain) to the
// criteria object.
// 
// Parameters:
// * {{{criteria}}}: the current criteria object.
// * {{{win}}}: (Optional) a reference to the global context (window) where to
// get the document
// and functions are located, if not passed, the current window is used
OB.Utilities.addFormInputsToCriteria = function(/* Object */criteria, /* Window */ win){
  var d = (win && win.document ? win.document : null) || window.document, elementsLength = (d.frmMain ? d.frmMain.elements.length : 0), inputValue = (win && win.inputValue ? win.inputValue : null) ||
  window.inputValue, i, elem;
  
  for (i = 0; i < elementsLength; i++) {
    elem = d.frmMain.elements[i];
    if (elem.name) {
      criteria[elem.name] = inputValue(elem);
    }
  }
  
  // the form can have an organization field,
  // in the server it is used to determine the accessible orgs
  // TODO: make this optional or make it possible to set the orgid html id
  if (d.frmMain.inpadOrgId) {
    criteria[OB.Constants.ORG_PARAMETER] = inputValue(d.frmMain.inpadOrgId);
  }
};

// ** {{{ OB.Utilities.updateSmartClientComponentValue }}} **
//
// Updates the value of a smartclient component.
//
// Parameters:
// * {{{input}}}: the input field (html dom input element)
// * {{{component}}}: the Smartclient component (must have a setValue function)
OB.Utilities.updateSmartClientComponentValue = function(/* Object */input, /* Object */ component){
  component.setValue(input.value);
};

// ** {{{ OB.Utilities.getKeyCode }}} **
//
// Returns the keycode related to a keydown event.
OB.Utilities.getKeyCode = function(){
  return isc.EventHandler.lastEvent.nativeKeyCode;
};

// ** {{{ OB.Utilities.Date.centuryReference }}} **
// For a two-digit year display format, it establishes where is the frontier
// between the 20th and the 21st century
// The range is taken between 1900+centuryReference and 2100-centuryReference-1
OB.Utilities.Date.centuryReference = 50; // Notice that change this value
// implies that also the QUnit test
// case should be changed

// ** {{{ OB.Utilities.Date.normalizeDisplayFormat }}} **
// Repairs the displayFormat definition (passed in as a parameter) to a value
// expected by the rest of the system. For example mm is replaced by MM,
// dd is replacecd by DD, YYYY to %Y.
//
// Parameters:
// * {{{displayFormat}}}: the displayFormat definition to repair.
OB.Utilities.Date.normalizeDisplayFormat = function(/* String */displayFormat){
  var newFormat = '';
  displayFormat = displayFormat.replace('mm', 'MM').replace('dd', 'DD').replace('yyyy', 'YYYY').replace('yy', 'YY');
  displayFormat = displayFormat.replace('%D', '%d').replace('%M', '%m');
  if (displayFormat !== null && displayFormat !== '') {
    newFormat = displayFormat;
    newFormat = newFormat.replace('YYYY', '%Y');
    newFormat = newFormat.replace('YY', '%y');
    newFormat = newFormat.replace('MM', '%m');
    newFormat = newFormat.replace('DD', '%d');
    newFormat = newFormat.substring(0, 8);
  }
  displayFormat = displayFormat.replace('hh', 'HH').replace('HH24', 'HH').replace('mi', 'MI').replace('ss', 'SS');
  displayFormat = displayFormat.replace('%H', 'HH').replace('HH:%m', 'HH:MI').replace('HH.%m', 'HH.MI').replace('%S', 'SS');
  displayFormat = displayFormat.replace('HH:mm', 'HH:MI').replace('HH.mm', 'HH.MI');
  displayFormat = displayFormat.replace('HH:MM', 'HH:MI').replace('HH.MM', 'HH.MI');
  if (displayFormat.indexOf(' HH:MI:SS') !== -1) {
    newFormat += ' %H:%M:%S';
  } else if (displayFormat.indexOf(' HH:MI') !== -1) {
    newFormat += ' %H:%M';
  } else if (displayFormat.indexOf(' HH.MI.SS') !== -1) {
    newFormat += ' %H.%M.%S';
  } else if (displayFormat.indexOf(' HH.MI') !== -1) {
    newFormat += ' %H.%M';
  }
  return newFormat;
};

// ** {{{ OB.Utilities.Date.OBToJS }}} **
//
// Converts a String to a Date object.
//
// Parameters:
// * {{{OBDate}}}: the date string to convert
// * {{{dateFormat}}}: the dateFormat pattern to use
// Return:
// * a Date object or null if conversion was not possible.
OB.Utilities.Date.OBToJS = function(/* String */OBDate, /* String */ dateFormat){
  if (!OBDate) {
    return null;
  }
  
  // if already a date then return true
  var isADate = Object.prototype.toString.call(OBDate) === '[object Date]';
  if (isADate) {
    return OBDate;
  }
  
  dateFormat = OB.Utilities.Date.normalizeDisplayFormat(dateFormat);
  var dateSeparator = dateFormat.substring(2, 3);
  var timeSeparator = dateFormat.substring(11, 12);
  var isFullYear = (dateFormat.indexOf('%Y') !== -1);
  
  if ((isFullYear ? OBDate.length - 2 : OBDate.length) !== dateFormat.length) {
    return null;
  }
  if (isFullYear) {
    dateFormat = dateFormat.replace('%Y', '%YYY');
  }
  
  if (dateFormat.indexOf('-') !== -1 && OBDate.indexOf('-') === -1) {
    return null;
  } else if (dateFormat.indexOf('/') !== -1 && OBDate.indexOf('/') === -1) {
    return null;
  } else if (dateFormat.indexOf(':') !== -1 && OBDate.indexOf(':') === -1) {
    return null;
  } else if (dateFormat.indexOf('.') !== -1 && OBDate.indexOf('.') === -1) {
    return null;
  }
  
  var year = dateFormat.indexOf('%y') !== -1 ? OBDate.substring(dateFormat.indexOf('%y'), dateFormat.indexOf('%y') + 2) : 0;
  var fullYear = dateFormat.indexOf('%Y') !== -1 ? OBDate.substring(dateFormat.indexOf('%Y'), dateFormat.indexOf('%Y') + 4) : 0;
  var month = dateFormat.indexOf('%m') !== -1 ? OBDate.substring(dateFormat.indexOf('%m'), dateFormat.indexOf('%m') + 2) : 0;
  var day = dateFormat.indexOf('%d') !== -1 ? OBDate.substring(dateFormat.indexOf('%d'), dateFormat.indexOf('%d') + 2) : 0;
  // note if there is no hour in the dateformat then use the value 12 to prevent winter/summertime issues 
  var hours = dateFormat.indexOf('%H') !== -1 ? OBDate.substring(dateFormat.indexOf('%H'), dateFormat.indexOf('%H') + 2) : 12;
  var minutes = dateFormat.indexOf('%M') !== -1 ? OBDate.substring(dateFormat.indexOf('%M'), dateFormat.indexOf('%M') + 2) : 0;
  var seconds = dateFormat.indexOf('%S') !== -1 ? OBDate.substring(dateFormat.indexOf('%S'), dateFormat.indexOf('%S') + 2) : 0;
  
  if (day < 1 || day > 31 || month < 1 || month > 12 || year > 99 ||
  fullYear > 9999) {
    return null;
  }
  
  if (hours > 23 || minutes > 59 || seconds > 59) {
    return null;
  }
  
  // alert('year: ' + year + '\n' + 'fullYear: ' + fullYear + '\n' + 'month: ' +
  // month + '\n' + 'day: ' + day + '\n' + 'hours: ' + hours + '\n' + 'minutes:
  // ' + minutes + '\n' + 'seconds: ' + seconds);
  
  // var JSDate = isc.Date.create(); /**It doesn't work in IE**/
  var JSDate = new Date();
  var centuryReference = OB.Utilities.Date.centuryReference;
  if (isFullYear) {
    JSDate.setFullYear(fullYear);
  } else {
    if (parseInt(year, 10) < centuryReference) {
      year = '20' + year;
    } else {
      year = '19' + year;
    }
    JSDate.setFullYear(year);
  }
  JSDate.setMonth(month - 1);
  JSDate.setDate(day);
  JSDate.setHours(hours);
  JSDate.setMinutes(minutes);
  JSDate.setSeconds(seconds);
  
  if (JSDate.toString() === 'Invalid Date' || JSDate.toString() === 'NaN') {
    return null;
  } else {
    return JSDate;
  }
};

// ** {{{ OB.Utilities.Date.JSToOB }}} **
//
// Converts a Date to a String
//
// Parameters:
// * {{{JSDate}}}: the javascript Date object
// * {{{dateFormat}}}: the dateFormat pattern to use
// Return:
// * a String or null if the JSDate is not a date.
OB.Utilities.Date.JSToOB = function(/* Date */JSDate, /* String */ dateFormat){
  dateFormat = OB.Utilities.Date.normalizeDisplayFormat(dateFormat);
  
  var isADate = Object.prototype.toString.call(JSDate) === '[object Date]';
  if (!isADate) {
    return null;
  }
  
  var year = JSDate.getYear().toString();
  var fullYear = JSDate.getFullYear().toString();
  var month = (JSDate.getMonth() + 1).toString();
  var day = JSDate.getDate().toString();
  var hours = JSDate.getHours().toString();
  var minutes = JSDate.getMinutes().toString();
  var seconds = JSDate.getSeconds().toString();
  
  var centuryReference = OB.Utilities.Date.centuryReference;
  if (dateFormat.indexOf('%y') !== -1) {
    if (parseInt(fullYear, 10) >= 1900 + centuryReference &&
    parseInt(fullYear, 10) < 2100 - centuryReference) {
      if (parseInt(year, 10) >= 100) {
        year = parseInt(year, 10) - 100;
        year = year.toString();
      }
    } else {
      return null;
    }
  }
  
  while (year.length < 2) {
    year = '0' + year;
  }
  while (fullYear.length < 4) {
    fullYear = '0' + fullYear;
  }
  while (month.length < 2) {
    month = '0' + month;
  }
  while (day.length < 2) {
    day = '0' + day;
  }
  while (hours.length < 2) {
    hours = '0' + hours;
  }
  while (minutes.length < 2) {
    minutes = '0' + minutes;
  }
  while (seconds.length < 2) {
    seconds = '0' + seconds;
  }
  var OBDate = dateFormat;
  OBDate = OBDate.replace('%y', year);
  OBDate = OBDate.replace('%Y', fullYear);
  OBDate = OBDate.replace('%m', month);
  OBDate = OBDate.replace('%d', day);
  OBDate = OBDate.replace('%H', hours);
  OBDate = OBDate.replace('%M', minutes);
  OBDate = OBDate.replace('%S', seconds);
  
  return OBDate;
};

// ** {{{ OB.Utilities.Number.roundJSNumber }}} **
//
// Function that rounds a JS number to a given decimal number
//
// Parameters:
// * {{{num}}}: the JS number
// * {{{dec}}}: the JS number of decimals
// Return:
// * The rounded JS number
OB.Utilities.Number.roundJSNumber = function(/* Number */num, /* Number */ dec){
  var result = Math.round(num * Math.pow(10, dec)) / Math.pow(10, dec);
  return result;
};

// ** {{{ OB.Utilities.Number.OBMaskedToOBPlain }}} **
//
// Function that returns a plain OB number just with the decimal Separator
//
// Parameters:
// * {{{number}}}: the formatted OB number
// * {{{decSeparator}}}: the decimal separator of the OB number
// * {{{groupSeparator}}}: the group separator of the OB number
// Return:
// * The plain OB number
OB.Utilities.Number.OBMaskedToOBPlain = function(/* String */number, /* String */ decSeparator, /* String */ groupSeparator){
  number = number.toString();
  var plainNumber = number;
  
  // Remove group separators
  var groupRegExp = new RegExp('\\' + groupSeparator, 'g');
  plainNumber = plainNumber.replace(groupRegExp, '');
  
  // Catch sign
  var numberSign = '';
  if (plainNumber.substring(0, 1) === '+') {
    numberSign = '';
    plainNumber = plainNumber.substring(1, number.length);
  } else if (plainNumber.substring(0, 1) === '-') {
    numberSign = '-';
    plainNumber = plainNumber.substring(1, number.length);
  }
  
  // Remove ending decimal '0'
  if (plainNumber.indexOf(decSeparator) !== -1) {
    while (plainNumber.substring(plainNumber.length - 1, plainNumber.length) === '0') {
      plainNumber = plainNumber.substring(0, plainNumber.length - 1);
    }
  }
  
  // Remove starting integer '0'
  while (plainNumber.substring(0, 1) === '0' &&
  plainNumber.substring(1, 2) !== decSeparator &&
  plainNumber.length > 1) {
    plainNumber = plainNumber.substring(1, plainNumber.length);
  }
  
  // Remove decimal separator if is the last character
  if (plainNumber.substring(plainNumber.length - 1, plainNumber.length) === decSeparator) {
    plainNumber = plainNumber.substring(0, plainNumber.length - 1);
  }
  
  // Re-set sign
  if (plainNumber !== '0') {
    plainNumber = numberSign + plainNumber;
  }
  
  // Return plain number
  return plainNumber;
};

// ** {{{ OB.Utilities.Number.OBPlainToOBMasked }}} **
//
// Function that transform a OB plain number into a OB formatted one (by
// applying a mask).
//
// Parameters:
// * {{{number}}}: The OB plain number
// * {{{maskNumeric}}}: The numeric mask of the OB number
// * {{{decSeparator}}}: The decimal separator of the OB number
// * {{{groupSeparator}}}: The group separator of the OB number
// * {{{groupInterval}}}: The group interval of the OB number
// Return:
// * The OB formatted number.
OB.Utilities.Number.OBPlainToOBMasked = function(/* Number */number, /* String */ maskNumeric, /* String */ decSeparator, /* String */ groupSeparator, /* String */ groupInterval){
  if (number === '' || number === null || number === undefined) {
    return number;
  }
  
  // Management of the mask
  if (maskNumeric.indexOf('+') === 0 || maskNumeric.indexOf('-') === 0) {
    maskNumeric = maskNumeric.substring(1, maskNumeric.length);
  }
  if (maskNumeric.indexOf(groupSeparator) !== -1 &&
  maskNumeric.indexOf(decSeparator) !== -1 &&
  maskNumeric.indexOf(groupSeparator) >
  maskNumeric.indexOf(decSeparator)) {
    var fixRegExp = new RegExp('\\' + groupSeparator, 'g');
    maskNumeric = maskNumeric.replace(fixRegExp, '');
  }
  var maskLength = maskNumeric.length;
  var decMaskPosition = maskNumeric.indexOf(decSeparator);
  if (decMaskPosition === -1) {
    decMaskPosition = maskLength;
  }
  var intMask = maskNumeric.substring(0, decMaskPosition);
  var decMask = maskNumeric.substring(decMaskPosition + 1, maskLength);
  
  if (decMask.indexOf(groupSeparator) !== -1 ||
  decMask.indexOf(decSeparator) !== -1) {
    var fixRegExp_1 = new RegExp('\\' + groupSeparator, 'g');
    decMask = decMask.replace(fixRegExp_1, '');
    var fixRegExp_2 = new RegExp('\\' + decSeparator, 'g');
    decMask = decMask.replace(fixRegExp_2, '');
  }
  
  // Management of the number
  number = number.toString();
  number = OB.Utilities.Number.OBMaskedToOBPlain(number, decSeparator, groupSeparator);
  var numberSign = '';
  if (number.substring(0, 1) === '+') {
    numberSign = '';
    number = number.substring(1, number.length);
  } else if (number.substring(0, 1) === '-') {
    numberSign = '-';
    number = number.substring(1, number.length);
  }
  
  // //Splitting the number
  var formattedNumber = '';
  var numberLength = number.length;
  var decPosition = number.indexOf(decSeparator);
  if (decPosition === -1) {
    decPosition = numberLength;
  }
  var intNumber = number.substring(0, decPosition);
  var decNumber = number.substring(decPosition + 1, numberLength);
  
  // //Management of the decimal part
  if (decNumber.length > decMask.length) {
    decNumber = '0.' + decNumber;
    decNumber = OB.Utilities.Number.roundJSNumber(decNumber, decMask.length);
    decNumber = decNumber.toString();
    if (decNumber.substring(0, 1) === '1') {
      intNumber = parseFloat(intNumber);
      intNumber = intNumber + 1;
      intNumber = intNumber.toString();
    }
    decNumber = decNumber.substring(2, decNumber.length);
  }
  
  if (decNumber.length < decMask.length) {
    var decNumber_temp = '';
    var decMaskLength = decMask.length;
    for (var i = 0; i < decMaskLength; i++) {
      if (decMask.substring(i, i + 1) === '#') {
        if (decNumber.substring(i, i + 1) !== '') {
          decNumber_temp = decNumber_temp + decNumber.substring(i, i + 1);
        }
      } else if (decMask.substring(i, i + 1) === '0') {
        if (decNumber.substring(i, i + 1) !== '') {
          decNumber_temp = decNumber_temp + decNumber.substring(i, i + 1);
        } else {
          decNumber_temp = decNumber_temp + '0';
        }
      }
    }
    decNumber = decNumber_temp;
  }
  
  // Management of the integer part
  var isGroup = false;
  
  if (intMask.indexOf(groupSeparator) !== -1) {
    isGroup = true;
  }
  
  var groupRegExp = new RegExp('\\' + groupSeparator, 'g');
  intMask = intMask.replace(groupRegExp, '');
  
  var intNumber_temp;
  if (intNumber.length < intMask.length) {
    intNumber_temp = '';
    var diff = intMask.length - intNumber.length;
    for (var j = intMask.length; j > 0; j--) {
      if (intMask.substring(j - 1, j) === '#') {
        if (intNumber.substring(j - 1 - diff, j - diff) !== '') {
          intNumber_temp = intNumber.substring(j - 1 - diff, j - diff) +
          intNumber_temp;
        }
      } else if (intMask.substring(j - 1, j) === '0') {
        if (intNumber.substring(j - 1 - diff, j - diff) !== '') {
          intNumber_temp = intNumber.substring(j - 1 - diff, j - diff) +
          intNumber_temp;
        } else {
          intNumber_temp = '0' + intNumber_temp;
        }
      }
    }
    intNumber = intNumber_temp;
  }
  
  if (isGroup === true) {
    intNumber_temp = '';
    var groupCounter = 0;
    for (var k = intNumber.length; k > 0; k--) {
      intNumber_temp = intNumber.substring(k - 1, k) + intNumber_temp;
      groupCounter++;
      if (groupCounter.toString() === groupInterval.toString() &&
      k !== 1) {
        groupCounter = 0;
        intNumber_temp = groupSeparator + intNumber_temp;
      }
    }
    intNumber = intNumber_temp;
  }
  
  // Building the final number
  if (intNumber === '' && decNumber !== '') {
    intNumber = '0';
  }
  
  formattedNumber = numberSign + intNumber;
  if (decNumber !== '') {
    formattedNumber += decSeparator + decNumber;
  }
  return formattedNumber;
};

// ** {{{ OB.Utilities.Number.OBMaskedToJS }}} **
//
// Function that returns a JS number just with the decimal separator which
// always is '.'. It is used for math operations
//
// Parameters:
// * {{{number}}}: The OB formatted (or plain) number
// * {{{decSeparator}}}: The decimal separator of the OB number
// * {{{groupSeparator}}}: The group separator of the OB number
// Return:
// * The JS number.
OB.Utilities.Number.OBMaskedToJS = function(numberStr, decSeparator, groupSeparator){
  if (!numberStr || numberStr.trim() === '') {
    return null;
  }
  var calcNumber = OB.Utilities.Number.OBMaskedToOBPlain(numberStr, decSeparator, groupSeparator);
  calcNumber = calcNumber.replace(decSeparator, '.');
  var numberResult = parseFloat(calcNumber);
  if (isNaN(numberResult)) {
    return numberStr;
  }
  return numberResult;
};

// ** {{{ OB.Utilities.Number.JSToOBMasked }}} **
//
// Function that returns a OB formatted number given as input a JS number just
// with the decimal separator which always is '.'
//
// Parameters:
// * {{{number}}}: The JS number
// * {{{maskNumeric}}}: The numeric mask of the OB number
// * {{{decSeparator}}}: The decimal separator of the OB number
// * {{{groupSeparator}}}: The group separator of the OB number
// * {{{groupInterval}}}: The group interval of the OB number
// Return:
// * The OB formatted number.
OB.Utilities.Number.JSToOBMasked = function(number, maskNumeric, decSeparator, groupSeparator, groupInterval){
  if (!isc.isA.Number(number)) {
    return number;
  }
  var formattedNumber = number;
  formattedNumber = formattedNumber.toString();
  formattedNumber = formattedNumber.replace('.', decSeparator);
  formattedNumber = OB.Utilities.Number.OBPlainToOBMasked(formattedNumber, maskNumeric, decSeparator, groupSeparator, groupInterval);
  return formattedNumber;
};

OB.Utilities.Number.IsValidValueString = function(type, numberStr){
  var maskNumeric = type.maskNumeric;
  // note 0 is also okay to return true
  if (!numberStr) {
    return true;
  }
  
  var bolNegative = true;
  if (maskNumeric.indexOf('+') === 0) {
    bolNegative = false;
    maskNumeric = maskNumeric.substring(1, maskNumeric.length);
  }
  
  var bolDecimal = true;
  if (maskNumeric.indexOf(type.decSeparator) === -1) {
    bolDecimal = false;
  }
  var checkPattern = '';
  checkPattern += '^';
  if (bolNegative) {
    checkPattern += '([+]|[-])?';
  }
  checkPattern += '(\\d+)?((\\' + type.groupSeparator + '\\d{' + OB.Format.defaultGroupingSize + '})?)+';
  if (bolDecimal) {
    checkPattern += '(\\' + type.decSeparator + '\\d+)?';
  }
  checkPattern += '$';
  var checkRegExp = new RegExp(checkPattern);
  if (numberStr.match(checkRegExp) && numberStr.substring(0, 1) !== type.groupSeparator) {
    return true;
  }
  return false;
};

// ** {{{ OB.Utilities._getTabInfoRequestProperties }}} **
//
// Adds tab and module information to the requestProperties.
//
// Parameters:
// * {{{theView}}}: view to obtain tab and module info from.
// * {{{requestProperties}}}: original requestProperties.
// Return:
// * Original requestProperties including the new module and tab properties.
OB.Utilities._getTabInfoRequestProperties = function(theView, requestProperties){
  if (theView && theView.tabId) {
    var tabParam = {
      params: {
        tabId: theView.tabId,
        moduleId: theView.moduleId
      }
    };
    if (requestProperties) {
      isc.addProperties(requestProperties, tabParam);
    } else {
      requestProperties = tabParam;
    }
  }
  return requestProperties;
};

OB.Utilities.openActionButton = function(button, o){
  var theView = button.parentElement.parentElement.view;
  var selectedRecord = theView.viewGrid.getSelectedRecord();
  
  if (!selectedRecord) {
    isc.warn('No record selected');
    return;
  }
  
  var allProperties = theView.getContextInfo(false, true);
  var sessionProperties = theView.getContextInfo(true, true);
  
  OB.Utilities.viewCallingProcess = theView;
  
  for (var param in allProperties) {
    if (allProperties.hasOwnProperty(param)) {
      o.command += '&' + param + '=' + allProperties[param];
    }
  }
  
  theView.setContextInfo(sessionProperties, function(){
    OB.Layout.ViewManager.openView('OBPopupClassicWindow', o);
  });
};

OB.Utilities.openActionButtonCallback = function(button, o){
  var theView = button.parentElement.parentElement.view;
  var selectedRecord = theView.viewGrid.getSelectedRecord();
  
  if (!selectedRecord) {
    isc.warn('No record selected');
    return;
  }
  
  // TODO: this has to be checked, these variables are not used?
  var allProperties = theView.getContextInfo(false, true); 
  var sessionProperties = theView.getContextInfo(true, true);
  // this was old code, but this could not have worked
//    var params = theView.getContextInfo(allProperties, sessionProperties, true);

  for (var param in params) {
    if (params.hasOwnProperty(param)) {
      o.command += '&' + param + '=' + params[param];
    }
  }
    
  theView.setContextInfo(sessionProperties, function(){
    OB.Layout.ViewManager.openView('OBPopupClassicWindow', o);
  });

  OB.Layout.ViewManager.openView('OBPopupClassicWindow', o);
};


//** {{{ OB.Utilities.postThroughHiddenForm }}} **
//
// Global method to post a request through a hidden form located on:
// org.openbravo.client.application/index.html
//
// Parameters:
// * {{{url}}}: the url to post the request.
// * {{{data}}}: the data to include in the request.

OB.Utilities.postThroughHiddenForm = function(url, data){
  OB.GlobalHiddenForm.setAttribute('action', url);
  
  // remove all children, needs to be done like this as the 
  // children array is getting updated while removing a child  
  while (OB.GlobalHiddenForm.children[0]) {
    OB.GlobalHiddenForm.removeChild(OB.GlobalHiddenForm.children[0]);
  }
  
  for (var key in data) {
    if (data.hasOwnProperty(key)) {
      var field = document.createElement('input');
      field.setAttribute('type', 'hidden');
      field.setAttribute('name', key);
      field.setAttribute('value', data[key]);
      OB.GlobalHiddenForm.appendChild(field);
    }
  }
  
  OB.GlobalHiddenForm.submit();
};
