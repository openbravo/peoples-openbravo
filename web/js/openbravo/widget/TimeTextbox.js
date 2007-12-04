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

dojo.provide("openbravo.widget.TimeTextbox");
dojo.require("dojo.widget.DateTextbox");
dojo.require("openbravo.messages.common");

// <namespace>, <namespace>.widget is now considered 'conventional'
// therefore the registerNamespace call below is no longer necessary here

// Tell dojo that widgets prefixed with "openbravo:" namespace are found in the "acme.widget" module
//dojo.registerNamespace("acme", "acme.widget");

// define UserButton's constructor
dojo.widget.defineWidget(
	// class
	"openbravo.widget.TimeTextbox",

	// superclass	
	dojo.widget.TimeTextbox,
	
	// member variables/functions
	{
		// override background images
		listenOnKeyPress:false,

    postMixInProperties: function(localProperties, frag) {
    openbravo.widget.TimeTextbox.superclass.postMixInProperties.apply(this, arguments);
    this.invalidMessage= openbravo.messages.getDataBaseMessage("Invalid");
    this.missingMessage= openbravo.messages.getDataBaseMessage("Missing");
    this.rangeMessage= openbravo.messages.getDataBaseMessage("Range");
    this.messages.invalidMessage= openbravo.messages.getDataBaseMessage("Invalid");
    this.messages.missingMessage= openbravo.messages.getDataBaseMessage("Missing");
    this.messages.rangeMessage= openbravo.messages.getDataBaseMessage("Range");
    },

		fillInTemplate: function(args, frag){
			openbravo.widget.TimeTextbox.superclass.fillInTemplate.apply(this, arguments);
			dojo.event.connect(this.textbox, "onchange", this, "onInputChange");
			dojo.event.connect(this.textbox, "onclick", this, "onIconClick");
      this.invalidSpan.style.display="none";
      this.missingSpan.style.display="none";
      this.rangeSpan.style.display="none";
		},

		onclick: function() {},

		onchange: function() {},

		onIconClick: function(){
		  this.onclick();
		},

		onInputChange: function(){
		  this.onchange();
		}
	}
);
