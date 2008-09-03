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
dojo.provide("openbravo.messages.common");
dojo.require("dojo.i18n.common");

		// invalidMessage: String
		// 		The message to display if value is invalid.
		var invalidMessage = "";
		// missingMessage: String
		//		The message to display if value is missing.
		var missingMessage = "";
		// rangeMessage: String
		//		The message to display if value is out of range.
		var rangeMessage = "";

openbravo.messages.getDataBaseMessage = function(/*String*/value, /*String*/responseFunction) {
  if (value=="Invalid") {
    if (invalidMessage=="") {openbravo.messages.getMessage(value); }
    return invalidMessage;
  }
  if (value=="Missing") {
    if (missingMessage=="") {openbravo.messages.getMessage(value); }
    return missingMessage;
  }
  if (value=="Range") {
    if (rangeMessage=="") openbravo.messages.getMessage(value); 
    return rangeMessage;
  }
  return obtenerMensaje(value);
}

openbravo.messages.getMessage = function(index, _language)
{
  if (_language==null) {
	if (typeof defaultLang != "undefined") {
      _language = defaultLang;
	} else {
      // Deprecated in 2.50, only for compatibility
      _language = LNG_POR_DEFECTO;
    }
  }
  if (typeof arrMessages != "undefined") {
    var total = arrMessages.length;
    for (var i=0;i<total;i++)
    {
      if (arrMessages[i].language == _language)
        if (arrMessages[i].message == index){
          if (index=="Invalid") invalidMessage = (arrMessages[i].text);
          if (index=="Missing") missingMessage = (arrMessages[i].text);
          if (index=="Range") rangeMessage = (arrMessages[i].text);
        }
    }
    return null;
  }
  else {
    this.messages = dojo.i18n.getLocalization("dojo.widget", "validate", this.lang);
    invalidMessage = this.messages.invalidMessage;
    missingMessage = this.messages.missingMessage;
    rangeMessage = this.messages.rangeMessage;
  }
}
