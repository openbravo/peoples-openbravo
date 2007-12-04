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
dojo.provide("openbravo");
dojo.provide("openbravo.core");
//dojo.registerModulePath("openbravo", "../openbravo");
//dojo.registerNamespace("openbravo", "openbravo");
//dojo.setModulePrefix("openbravo", "../openbravo");


openbravo = {
	
	version: "0.1",

	loadScriptFromUrl: function(url){
	    var scripts = document.getElementsByTagName("script");
	    for (var i = 0; i < scripts.length; i++) {
	        var src = scripts[i].src;
	        if (src && src.length > 0 && src.indexOf(url)>=0 ) {
	            return;
	        }
	    }
		
	    var script = document.createElement("script");
	    script.src = url;
	    script.type = "text/javascript";
	    document.getElementsByTagName("head")[0].appendChild(script);
	}

}
