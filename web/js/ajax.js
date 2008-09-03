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
 * The Initial Developer of the Original Code is Openbravo SL 
 * All portions are Copyright (C) 2001-2006 Openbravo SL 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
*/

/**
* @fileoverview Contains methods for asynchronous ajax calls. (eg. used by some
* HTML reports to display/hide a subreport)
*/

var xmlreq = false;
var paramXMLRequest = null;

function getXMLHttpRequest() {
  // Create XMLHttpRequest object in non-Microsoft browsers
  if (!xmlreq && typeof XMLHttpRequest != 'undefined') {
    try {
      xmlreq = new XMLHttpRequest();
    } catch (e) {
      xmlreq = false;
    }
  }
  else if (window.ActiveXObject) {
    try {
      // Try to create XMLHttpRequest in later versions
      // of Internet Explorer
      xmlreq = new ActiveXObject("Msxml2.XMLHTTP");
    } catch (e1) {
      // Failed to create required ActiveXObject
      try {
        // Try version supported by older versions
        // of Internet Explorer
        xmlreq = new ActiveXObject("Microsoft.XMLHTTP");
      } catch (e2) {
        // Unable to create an XMLHttpRequest by any means
        xmlreq = false;
      }
    }
  }
  return xmlreq;
}

function getReadyStateHandler(req, responseXmlHandler, notifyError) {
  if (req==null) return false;
  if (notifyError==null) notifyError=true;
  // If the request's status is "complete"
  if (req.readyState == 4) {
    // Check that we received a successful response from the server
    if (req.status == 200) {
      // Pass the XML payload of the response to the handler function.
      //responseXmlHandler(req.responseXML);
      return true;
    } else {
      // An HTTP problem has occurred
      if (notifyError)
        alert("HTTP error "+req.status+": "+req.statusText);
      return false;
    }
    return false;
  }
  return false;
}

function submitXmlHttpRequestUrl(callbackFunction, url, debug) {
  if (!xmlreq) xmlreq = getXMLHttpRequest();
  if (debug==null) debug=false;
  if (!xmlreq) {
    alert("Your browser doesn't support this technology");
    return false;
  }
  if (debug)
    if (!debugXmlHttpRequest(url)) return false;
  xmlreq.open("GET", url);
  var paramXMLParticular = paramXMLRequest;
  xmlreq.onreadystatechange = function () {
    return callbackFunction(paramXMLParticular);
  }
  paramXMLRequest = null;
  xmlreq.send(null);

  return true;
}

function submitXmlHttpRequest(callbackFunction, formObject, Command, Action, debug) {
  if (!xmlreq) xmlreq = getXMLHttpRequest();
  if (formObject==null) formObject = document.forms[0];
  if (debug==null) debug=false;
  if (Action==null) Action = formObject.action;
  if (!xmlreq) {
    alert("Your browser doesn't support this technology");
    return false;
  }
  var sendText = "Command=" + escape(Command);
  sendText += "&IsAjaxCall=1";
  var length = formObject.elements.length;
  for (var i=0;i<length;i++) {
    if (formObject.elements[i].type) {
      var text = inputValueForms(formObject.elements[i].name, formObject.elements[i]);
      if (text!=null && text!="" && text!="=") sendText += "&" + text;
    }
  }
  if (debug)
    if (!debugXmlHttpRequest(Command)) return false;
  //xmlreq.open("GET", Action + "?" + sendText);
  xmlreq.open("POST", Action);
  try {
    xmlreq.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
  } catch (e) {}
  var paramXMLParticular = paramXMLRequest;
  xmlreq.onreadystatechange = function () {
    return callbackFunction(paramXMLParticular);
  }
  //xmlreq.send(null);
  paramXMLRequest = null;
  xmlreq.send(sendText);

  return true;
}

function lockField(inputField) {
  if (inputField==null) return false;
  if (!inputField.type) return false;
  inputField.disabled=true;
  return true;
}

function unlockField(inputField) {
  if (inputField==null) return false;
  if (!inputField.type) return false;
  inputField.disabled=false;
  return true;
}
