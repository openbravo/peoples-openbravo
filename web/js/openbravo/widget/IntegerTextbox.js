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

dojo.provide("openbravo.widget.IntegerTextbox");
dojo.require("dojo.widget.IntegerTextbox");

// <namespace>, <namespace>.widget is now considered 'conventional'
// therefore the registerNamespace call below is no longer necessary here

// Tell dojo that widgets prefixed with "openbravo:" namespace are found in the "acme.widget" module
//dojo.registerNamespace("acme", "acme.widget");

// define UserButton's constructor
dojo.widget.defineWidget(
	// class
	"openbravo.widget.IntegerTextbox",

	// superclass	
	dojo.widget.IntegerTextbox,
	
	// member variables/functions
	{
		// readonly: Boolean
		//		Can be true or false, default is false.
		readonly: false,
		// disabled: Boolean
		//		Can be true or false, default is false.
		disabled: false,
		listenOnKeyPress:false,
		// override html
		templatePath: dojo.uri.dojoUri("../openbravo/widget/templates/ValidationTextbox.html"),
		// override css
		templateCssPath: null,  // Defined in the skin --- IntegerTextbox.css ,


		fillInTemplate: function(args, frag){
			openbravo.widget.IntegerTextbox.superclass.fillInTemplate.apply(this, arguments);
			if(this.required){ dojo.html.addClass(this.textbox, "required"); }
			if(this.readonly){ dojo.html.addClass(this.textbox, "readonly"); }
			if(this.disabled){ dojo.html.addClass(this.textbox, "disabled"); }
			this.textbox.disabled = this.disabled;
			this.textbox.readonly = this.readonly;
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
