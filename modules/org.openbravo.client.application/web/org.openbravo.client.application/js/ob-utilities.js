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
 * All portions are Copyright (C) 2011 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
// = Openbravo Utilities =
// Defines utility methods in the top-level OB.Utilities object. Utility methods
// are related to opening views, opening popups, displaying yes/no, etc. 
OB.Utilities = {};

// ** {{{OB.Utilities.useClassicMode}}} **
// Returns true if the user wants to work in classic mode, checks the url parameter
// as well as a property value.
OB.Utilities.useClassicMode = function(windowId){
  if (OB.Utilities.hasUrlParameter('mode', 'classic')) {
    return true;
  }
  var propValue = OB.PropertyStore.get('OBUIAPP_UseClassicMode', windowId);
  if (propValue === 'Y') {
    return true;
  }
  if (OB.WindowDefinitions[windowId] && OB.WindowDefinitions[windowId].showInClassicMode) {
    return true;
  }
  return false;
};

// ** {{{OB.Utilities.openView}}} **
// Open a view taking into account if a specific window should be opened in classic mode or not.
// Returns the object used to open the window.
OB.Utilities.openView = function(windowId, tabId, tabTitle, recordId, command, icon){
    var isClassicEnvironment = OB.Utilities.useClassicMode(windowId);

    var openObject;
    if (isClassicEnvironment) {
      if (recordId) {
        OB.Layout.ClassicOBCompatibility.openLinkedItem(tabId, recordId);
        return null;
      } 
      openObject = {
          viewId: 'OBClassicWindow',
          windowId: windowId, 
          tabId: tabId, 
          id: tabId, 
          command: 'DEFAULT', 
          tabTitle: tabTitle,
          icon: icon
       };
    } else if (recordId) {
       openObject = {
        viewId: '_' + windowId,
        id: tabId,
        targetRecordId: recordId,
        targetTabId: tabId,
        tabTitle: tabTitle,
        windowId: windowId
      };      
    } else {
       openObject = {
        viewId: '_' + windowId,
        id: tabId,
        tabId: tabId,
        tabTitle: tabTitle,
        windowId: windowId,
        icon: icon
      };      
    }
    if (command) {
      openObject.command = command;
    }
    OB.Layout.ViewManager.openView(openObject.viewId, openObject);
    return openObject;
};

// ** {{{OB.Utilities.openDirectView}}} **
// Open the correct view for a passed in target definition, coming from a certain source Window.
OB.Utilities.openDirectView = function(sourceWindowId, keyColumn, targetEntity, recordId){

  var actionURL = OB.Application.contextUrl + 'utility/ReferencedLink.html';
  
  var callback = function(response, data, request){
    OB.Utilities.openView(data.windowId, data.tabId, data.tabTitle, data.recordId);
  };
  
  var reqObj = {
    params: {
      Command: 'JSON',
      inpEntityName: targetEntity,
      inpKeyReferenceId: recordId,
      inpwindowId: sourceWindowId,
      inpKeyReferenceColumnName: keyColumn
    },
    callback: callback,
    evalResult: true,
    httpMethod: 'GET',
    useSimpleHttp: true,
    actionURL: actionURL
  };
  var request = isc.RPCManager.sendRequest(reqObj);
};
  
// ** {{{OB.Utilities.getPromptString}}} **
// Translates a string or array of strings to a string with html returns.
OB.Utilities.getPromptString = function(msg){
  var msgString = '';
  if (!isc.isAn.Array(msg)) {
    msg = [msg];
  }
  for (var i = 0; i < msg.length; i++) {
    msgString += (i > 0 ? '<br>' : '') + msg[i].asHTML();
  }
  return msgString;
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
// * {{{postParams}}}: if this object is set and noFrameSet is not true, main Framset send
//                     properties of this object to url as POST, other case a GET to url is
//                     performed
// frameset
OB.Utilities.openProcessPopup = function(/* String */url, noFrameSet, postParams){
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
    var mainFrameSrc = !postParams?('src="' + url + '"'):'',
       html = '<html>' +
    '<frameset cols="0%,100%" frameborder="no" border="0" framespacing="0" rows="*" id="framesetMenu">' +
    '<frame name="frameMenu" scrolling="no" src="' +
    OB.Application.contextUrl +
    'utility/VerticalMenu.html?Command=LOADING" id="paramFrameMenuLoading"></FRAME>' +
    '<frame name="mainframe" noresize="" '+ mainFrameSrc +
    ' id="fieldProcessId"></frame>' +
    '<frame name="hiddenFrame" scrolling="no" noresize="" src=""></frame>' +
    '</frameset>' +
    '</html>';
    
    winPopUp.document.write(html);
    if (postParams) {
      var frm = winPopUp.document.createElement('form');
      frm.setAttribute('method','post');
      frm.setAttribute('action', url);
      for (var i in postParams) {
        if (postParams.hasOwnProperty(i)){
          var inp = winPopUp.document.createElement('input');
          inp.setAttribute('type', 'hidden');
          inp.setAttribute('name', i);
          inp.setAttribute('value', postParams[i]);
          frm.appendChild(inp);
        }
      }
      winPopUp.document.body.appendChild(frm);
      frm.submit();
    }
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

// ** {{{ OB.Utilities.getClassicValue }}} **
// Returns the Y if the passed value is true, and N if false.
OB.Utilities.getClassicValue = function(/* Boolean */value){
  if (value) {
    return 'Y';
  } else if (value === false) {
    return 'N';
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
