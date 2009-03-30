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
 * All portions are Copyright (C) 2001-2009 Openbravo SL 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
*/

/**
* @fileoverview Main functions for displaying preloaded critical messages in
* corresponding languages (save changes, number out of range, invalid value, 
* etc.). This library works hand in hand with the MessagesJS.java (mapped 
* to utility/DynamicJS.js) servlet found in the erpCommon/utility folder.
*/

//Valores por defecto
//var defaultLang = "en_US";
var gDefaultType = 0;

function messageType(_messageID, _messageType) {
	this.id = _messageID;
	this.type = _messageType;
}

function messagesTexts(_language, _message, _text, _defaultText) {
	this.language = _language;
	this.message = _message;
	this.text = _text;
	this.defaultText = _defaultText;
}

function getMessage(index, _language) {
	if (_language==null) {
		if (typeof defaultLang != "undefined") {
			_language = defaultLang;
		} else if (typeof LNG_POR_DEFECTO != "undefined") {
			// Deprecated in 2.50, the following code is only for compatibility
			_language = LNG_POR_DEFECTO;
		}
	}
    var total = arrMessages.length;
	for (var i=0;i<total;i++) {
		if (arrMessages[i].language == _language)
			if (arrMessages[i].message == index)
				return (arrMessages[i].text);
	}
	return null;
}

function getDefaultText(index, _language) {
  if (_language==null) {
    if (typeof defaultLang != "undefined") {
      _language = defaultLang;
    } else if (typeof LNG_POR_DEFECTO != "undefined") {
      // Deprecated in 2.50, the following code is only for compatibility
      _language = LNG_POR_DEFECTO;
    }
  }
  var total = arrMessages.length;
	for (var i=0;i<total;i++) {
		if (arrMessages[i].language == _language)
			if (arrMessages[i].message == index)
				return (arrMessages[i].defaultText);
	}
	return null;
}

function getType(index) {
  var total = arrTypes.length;
	for (var i=0;i<total;i++) {
		if (arrTypes[i].id == index)
			return (arrTypes[i].type);
	}
	return null;
}

/*	Los tipos de mensajes son:
		0.- Alert -> muestra una ventana de mensaje normal con un botón aceptar
		1.- Confirm -> muestra una ventana de confirmación que tiene 2 botones (OK y CANCEL)
		2.- Prompt -> muestra una ventana de petición de un parámetro con 2 botones (OK y CANCEL)
*/
function showMessage(_text, _type, _defaultValue) {
	switch (_type) {
	case 1:return confirm(_text);
			 break;
	case 2:return prompt(_text, _defaultValue);
			 break;
	default: alert(_text);
	}
	return true;
}

// Deprecated in 2.50, use showJSMessage instead
function mensaje(index, _language)
{
	showJSMessage(index, _language);
}

function showJSMessage(index, _language, clean)
{
  var clearMsgBox = typeof clean == 'undefined' || clean == null ? true : clean;
  if(clearMsgBox) {
	  try {
	    initialize_MessageBox('messageBoxID');
	  } catch (ignored) {}
  }
	var strMessage = getMessage(index, _language);
  if (strMessage == null)  strMessage = getMessage(index, "en_US");
	if (strMessage == null) {
    getDataBaseMessage(index);
    return true;
  }
	var strDefault = getDefaultText(index, _language);
	if (strDefault == null) { 
		strDefault = getDefaultText(index, 'en_US');
	}
	var type = getType(index, _language);
	if (type==null) type=gDefaultType;
	return showMessage(strMessage, type, strDefault);
}


function renderMessageBox(type, title, text) {
  try {
    dojo.widget.byId('messageBoxID').setValues(type, title, text);
  } catch (err) {
    alert(title + ":\n" + text);
  }
  return true;
}

function getUrl() {
  var url = window.location.href;
  var pos = url.indexOf("://");
  var pos2 = url.indexOf("/", pos+3);
  if (pos2!=-1) {
    pos2 = url.indexOf("/", pos2+1);
    if (pos2!=-1) url = url.substring(0, pos2);
  }
  return url;
}

function getDataBaseMessage(value, responseFunction) {
  //paramXMLRequest = new Array('field', 'fieldName');
  submitXmlHttpRequestUrl(((responseFunction==null)?messageResponse:responseFunction), (getUrl() + "/businessUtility/MessageJS.html?inpvalue=JS" + escape(value)), false)
}

function getDataBaseStandardMessage(value, responseFunction) {
  submitXmlHttpRequestUrl(((responseFunction==null)?messageResponse:responseFunction), (getUrl() + "/businessUtility/MessageJS.html?inpvalue=" + escape(value)), false)
}

function messageResponse(paramArray) {
   var obj;
   if (getReadyStateHandler(xmlreq)) {
    try {
      if (xmlreq.responseXML) obj = xmlreq.responseXML.documentElement;
    } catch (e) {
    }
    /*if (paramArray!=null && paramArray.length>0) {
      field = paramArray[0];
      try {
        var obj = document.getElementById(field);
        setFocus(obj);
      } catch (ignore) {}
    }*/
    if (obj) {
      var status = obj.getElementsByTagName('status');
      if (status.length>0) {
        var type = status[0].getElementsByTagName('type');
        var title = status[0].getElementsByTagName('title');
        var description = status[0].getElementsByTagName('description');
        try {
          setValues_MessageBox('messageBoxID',type[0].firstChild.nodeValue.toUpperCase(), title[0].firstChild.nodeValue, description[0].firstChild.nodeValue);
        } catch (err) {
          alert(title[0].firstChild.nodeValue + ":\n" + description[0].firstChild.nodeValue);
        }
      }
    }
  }
  return true;
}
