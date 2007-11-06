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
 * All portions are Copyright (C) 2001-2007 Openbravo SL 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
*/
dojo.provide("openbravo.io");

dojo.require("openbravo.core");
dojo.require("dojo.widget.Dialog");

openbravo.io = {
	
	asyncCall: function(serviceUrl, content) {
		dojo.io.bind({
			url: serviceUrl.url,
            useCache: true,
            preventCache: true,
            content: content,
            load: serviceUrl.handler,
            error: openbravo.io.handleError,
            method: serviceUrl.method,
            mimetype: serviceUrl.mimetype,
            encoding: "UTF-8"
        });
	},
	
	handleError: function(type, exception, http, kwArgs){
//		dojo.log.exception("Error received in IO response.", exception);
//		openbravo.io.presentException(exception, kwArgs);
		dojo.debug("Error received in IO response.", exception.message);
	},
	
	presentException:function(exception, kwArgs) {
		var excnode=document.createElement("div");
		document.body.appendChild(excnode);
		var contentnode=document.createElement("div");
		contentnode.innerHTML= exception.message;
		var navnode=document.createElement("div");
		var closeButton = document.createElement("input");
		closeButton.type= "button";
		closeButton.value= "Close";
		navnode.appendChild(closeButton);
		excnode.appendChild(contentnode);
		excnode.appendChild(navnode);
		var options = {
			bgColor: "white",
			bgOpacity: "0.5",
			toggle: "fade",
			toggleDuration: "250",
			widgetId: "exception"
		}
		var dialog=dojo.widget.createWidget("Dialog", options, excnode);
		dojo.event.connect(navnode, "onclick", dialog, "hide");
		dojo.event.connect(contentnode, "onclick", dialog, "hide");
		dojo.event.connect(dialog, "hide", dialog, "destroy");
		
		dialog.show();
	}	
}