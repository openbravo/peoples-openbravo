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

// = ClassicOBCompatibility =
//
// The ClassicOBCompatibility handles the interaction between the classic OB window
// and the {{{Main View}}}
//
(function(OB, isc) {

  if (!OB || !isc) {
    throw {name: "ReferenceError",
        message: "openbravo and isc objects are required"};
  }

  // cache object references locally
  var L = OB.Layout,
       M = OB.MainView,
     ISC = isc,
     cobcomp; // Local reference to ClassicOBCompatibility instance

  function ClassicOBCompatibility() {}

  ClassicOBCompatibility.prototype = {


// ** {{{ ClassicOBCompatibility.openLinkedItem(tabId, recordId) }}} **
//
// Opens a window from the linked item view.
//
// Parameters:
// * {{{tabId}}} id of the tab to open
// * {{{recordId}}} the record to show
//
  openLinkedItem: function (tabId, recordId) {
    var doOpenClassicWindow = function (response, data, request) {
      
      if (!data.recordId || data.recordId.length === 0) {
        L.ViewManager.openView("ClassicOBWindow", {tabTitle: data.tabTitle, windowId: data.windowId, tabId: data.tabId, mappingName: data.mappingName, command: "DEFAULT"});
      } else {
        L.ViewManager.openView("ClassicOBWindow", {tabTitle: data.tabTitle, windowId: data.windowId, tabId: data.tabId, mappingName: data.mappingName, keyParameter: data.keyParameter, recordId: data.recordId, command: "DIRECT"});
      }
    }
    
    OB.RemoteCallManager.call('org.openbravo.client.application.ComputeWindowActionHandler', {}, {'tabId': tabId, 'recordId': recordId}, doOpenClassicWindow);
  },

// ** {{{ ClassicOBCompatibility.sendDirectLink(action, form) }}} **
//
// Shows a new tab with the clicked link content
//
// Parameters:
// * {{{action}}} is a String with the Command action to call the servlet, e.g. {{{ JSON }}}
// * {{{form}}} is an Object of the form from where it has been submitted
//
    sendDirectLink: function (/*String*/ action, /*Object*/ form) {

      //
      // Opens the MDI tab of the clicked link
      //
      function openDirectLink(/*String*/ address) {
        var addressObj = ISC.eval("("+address+")");
        
        if (!addressObj.recordId || addressObj.recordId.length === 0) {
          L.ViewManager.openView("ClassicOBWindow", {tabTitle: addressObj.tabTitle, windowId: addressObj.windowId, tabId: addressObj.tabId, mappingName: addressObj.mappingName, command: "DEFAULT"});
        } else {
          L.ViewManager.openView("ClassicOBWindow", {tabTitle: addressObj.tabTitle, windowId: addressObj.windowId, tabId: addressObj.tabId, mappingName: addressObj.mappingName, keyParameter: addressObj.keyParameter, recordId: addressObj.recordId, command: "DIRECT"});
        }
      }

      //
      // Returns a form field and value as a JSON parameter
      //
      function inputValueForms(/*String*/ name, /*Object*/ field) {
        var result = "";
        if (!field || name.toString().replace(/^\s*/, "").replace(/\s*$/, "") === "") {
          return "";
        }
        if (!field.type && field.length>1) {
          field = field[0];
        }
        if (field.type) {
          if (field.type.toUpperCase().indexOf("SELECT") !== -1) {
            if (field.selectedIndex === -1) {
              return "";
            } else {
              var length = field.options.length;
              for (var fieldsCount=0;fieldsCount<length;fieldsCount++) {
                if (field.options[fieldsCount].selected) {
                  if (result !== "") {
                    result += ", ";
                  }
                  result += name + ": " + "\"" + escape(field.options[fieldsCount].value) + "\"";
                }
              }
              return result;
            }
          } else if (field.type.toUpperCase().indexOf("RADIO") !== -1 || field.type.toUpperCase().indexOf("CHECK") !== -1) {
            if (!field.length) {
              if (field.checked) {
                return (name + ": " + "\"" + escape(field.value) + "\"");
              } else {
                return "";
              }
            } else {
              var total = field.length;
              for (var i=0;i<total;i++) {
                if (field[i].checked) {
                  if (result !== "") {
                    result += ", ";
                  }
                  result += name + ": " + "\"" + escape(field[i].value) + "\"";
                }
              }
              return result;
            }
          } else {
            return name + ": " + "\"" + escape(field.value) + "\"";
          }
        }

        return "";
      }

      //
      // Returns a JSON of all the form fields. Also adds Command and IsAjaxCall parameters to communicate with the classic OB class
      //
      function getXHRParamsObj(/*String*/ action, /*Object*/ formObject) {
        var paramsObj = "Command: " + "\"" + escape(action) + "\"" + ", ";
        paramsObj += "IsAjaxCall: " + "\"" + "1" + "\"";
        for (var i = 0; i < formObject.elements.length; i++) {
          if (formObject.elements[i].type) {
            var text = inputValueForms(formObject.elements[i].name, formObject.elements[i]);
            if (text !== null && text !== "" && text !== "=") {
              paramsObj += ", " + text;
            }
          }
        }
        paramsObj = "{" + paramsObj + "}";
        var secCommandIndex = paramsObj.indexOf('Command', paramsObj.indexOf('Command')+1);
        if (paramsObj.indexOf('Command') !== secCommandIndex && secCommandIndex !== -1) {
          var secCommandIndexComma =  paramsObj.indexOf(",", secCommandIndex+1);
          paramsObj = paramsObj.substring(0, secCommandIndex) + paramsObj.substring(secCommandIndexComma+2, paramsObj.length);// + paramsObj.substring(85, paramsObj.length);
        }
        paramsObj = ISC.eval("("+paramsObj+")");
        return paramsObj;
      }


      //
      // Function used by the {{ ISC.RPCManager }} after receiving the
      // target address from the back-end
      //
      function fetchSendDirectLinkCallback(/*Object*/ response, /*String*/ data) {
        openDirectLink(data);
      }

      //
      // Fetch the target address from the server. After fetching
      // the implementation, stores a reference in the cache
      //
      function fetchSendDirectLink(/*String*/ action, /*Object*/ formObject) {
        var paramsObj = getXHRParamsObj(action, formObject);
        // hardcode the reference link url
        // formObject.action.toString();
        // hardcode to prevent this issue with chrome:
        // https://issues.openbravo.com/view.php?id=13837
        var actionURL = OB.Application.contextUrl + "utility/ReferencedLink.html"; 

        var rpcMgr = ISC.RPCManager;
        var reqObj = {params: paramsObj,
                    callback: fetchSendDirectLinkCallback,
                  evalResult: false,
                  httpMethod: "GET",
               useSimpleHttp: true,
                   actionURL: actionURL};
        var request = rpcMgr.sendRequest(reqObj);
      }
      fetchSendDirectLink(action, form);
      },
      
      setTabInformation: function (windowId, tabId, recordId, mode, obManualURL, title) {
        var tabNumber = null, tabSet, tabPane;
        tabSet = M.TabSet;
        if (windowId) {
          tabNumber = L.ViewManager.views.getTabNumberFromViewParam("windowId", windowId);
        } else if (obManualURL) {
          tabNumber = L.ViewManager.views.getTabNumberFromViewParam("obManualURL", obManualURL);
        } else if (tabId) {
          tabNumber = L.ViewManager.views.getTabNumberFromViewParam("tabId", tabId);
        }
              
        if (!tabNumber) {
          tabNumber = tabSet.getTabNumber(tabSet.getSelectedTab());
        }
        
        if (tabNumber === null) {
          return false;
        }
        
        tabPane = tabSet.getTabPane(tabNumber);
        
        tabSet.setTabTitle(tabNumber, title);
        tabPane.updateTabInformation(windowId, tabId, recordId, mode, obManualURL, title);
    }
  };

  // Initialize ClassicOBCompatibility object
  cobcomp = L.ClassicOBCompatibility = new ClassicOBCompatibility();
})(OB, isc);