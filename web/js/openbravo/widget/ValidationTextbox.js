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

dojo.provide("openbravo.widget.ValidationTextbox");
dojo.require("dojo.widget.ValidationTextbox");
dojo.require("openbravo.messages.common");

// <namespace>, <namespace>.widget is now considered 'conventional'
// therefore the registerNamespace call below is no longer necessary here

// Tell dojo that widgets prefixed with "openbravo:" namespace are found in the "acme.widget" module
//dojo.registerNamespace("acme", "acme.widget");

// define UserButton's constructor
dojo.widget.defineWidget(
	// class
	"openbravo.widget.ValidationTextbox",

	// superclass	
	dojo.widget.ValidationTextbox,
	
	// member variables/functions
	{
		// readonly: Boolean
		//		Can be true or false, default is false.
		readonly: false,
		// disabled: Boolean
		//		Can be true or false, default is false.
		disabled: false,
		listenOnKeyPress:false,
		// override css
		templatePath: dojo.uri.dojoUri("../openbravo/widget/templates/ValidationTextbox.html"),
		templateCssPath: null, // Defined in the skin --- ValidationTextbox.css

    postMixInProperties: function(localProperties, frag) {
    openbravo.widget.ValidationTextbox.superclass.postMixInProperties.apply(this, arguments);
    this.invalidMessage= openbravo.messages.getDataBaseMessage("Invalid");
    this.missingMessage= openbravo.messages.getDataBaseMessage("Missing");
    this.rangeMessage= openbravo.messages.getDataBaseMessage("Range");
    this.messages.invalidMessage= openbravo.messages.getDataBaseMessage("Invalid");
    this.messages.missingMessage= openbravo.messages.getDataBaseMessage("Missing");
    this.messages.rangeMessage= openbravo.messages.getDataBaseMessage("Range");
    },

		fillInTemplate: function(args, frag){
			openbravo.widget.ValidationTextbox.superclass.fillInTemplate.apply(this, arguments);
			if(this.required){ dojo.html.addClass(this.textbox, "required"); }
			if(this.readonly){ dojo.html.addClass(this.textbox, "readonly"); }
			if(this.disabled){ dojo.html.addClass(this.textbox, "disabled"); }
			if (this.disabled) this.textbox.setAttribute("disabled", "true");
			if (this.readonly) this.textbox.setAttribute("readonly", "true");
			this.invalidSpan.style.display="none";
			this.missingSpan.style.display="none";
			this.rangeSpan.style.display="none";
		},

		onkeydown: function(){ 
		},

		onclick: function() {
		},

		onchange: function() {
		}
	}
);
