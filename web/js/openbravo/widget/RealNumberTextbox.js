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

dojo.provide("openbravo.widget.RealNumberTextbox");
dojo.require("dojo.widget.RealNumberTextbox");
dojo.require("openbravo.messages.common");

// <namespace>, <namespace>.widget is now considered 'conventional'
// therefore the registerNamespace call below is no longer necessary here

// Tell dojo that widgets prefixed with "openbravo:" namespace are found in the "openbravo.widget" module
// dojo.registerNamespace("openbravo", "openbravo.widget");

// define UserButton's constructor
dojo.widget.defineWidget(
	// class
	"openbravo.widget.RealNumberTextbox",

	// superclass	
	dojo.widget.RealNumberTextbox,
	
	// member variables/functions
	{
		// readonly: Boolean
		//		Can be true or false, default is false.
		readonly: false,
		// disabled: Boolean
		//		Can be true or false, default is false.
		disabled: false,
		listenOnKeyPress:false,
    // Id of the other member of the comparison.
    greaterThan:"",
    lowerThan:"",
    isWrong:false,
		// override html
		templatePath: dojo.uri.dojoUri("../openbravo/widget/templates/ValidationTextbox.html"),
		// override css
		templateCssPath: null, // Defined in the skin --- ValidationTextbox.css

    postMixInProperties: function(localProperties, frag) {
    openbravo.widget.RealNumberTextbox.superclass.postMixInProperties.apply(this, arguments);
    this.invalidMessage= openbravo.messages.getDataBaseMessage("Invalid");
    this.missingMessage= openbravo.messages.getDataBaseMessage("Missing");
    this.rangeMessage= openbravo.messages.getDataBaseMessage("Range");
    this.messages.invalidMessage= openbravo.messages.getDataBaseMessage("Invalid");
    this.messages.missingMessage= openbravo.messages.getDataBaseMessage("Missing");
    this.messages.rangeMessage= openbravo.messages.getDataBaseMessage("Range");
    },

		fillInTemplate: function(args, frag){
			openbravo.widget.RealNumberTextbox.superclass.fillInTemplate.apply(this, arguments);
			if(this.required){ dojo.html.addClass(this.textbox, "required"); }
			if(this.readonly){ dojo.html.addClass(this.textbox, "readonly"); }
			if(this.disabled){ dojo.html.addClass(this.textbox, "disabled"); }
			this.textbox.disabled = this.disabled;
			this.textbox.readonly = this.readonly;
			this.invalidSpan.style.display="none";
			this.missingSpan.style.display="none";
			this.rangeSpan.style.display="none";
		},

		isInRange: function(){
      if ((this.greaterThan == "" || this.greaterThan == null) && (this.lowerThan == "" || this.lowerThan == null)) {
        return true;
      }
      if (this.greaterThan != "") {
        if (document.getElementById(this.greaterThan).value == null || document.getElementById(this.greaterThan).value == "") {
          if (this.isWrong == true) {
            this.isWrong = false;
            dojo.widget.byId(this.greaterThan).update();
          }
          return true;
        } else if (this.textbox.value == "" || this.textbox.value == null) {
          if (this.isWrong == true) {
            this.isWrong = false;
            dojo.widget.byId(this.greaterThan).update();
          }
          return true;
        } else if (parseFloat(this.textbox.value) < parseFloat(document.getElementById(this.greaterThan).value)) {
          if (this.isWrong == false) {
            this.isWrong = true;
            dojo.widget.byId(this.greaterThan).update();
          }
          return false;
        } else {
          if (this.isWrong == true) {
            this.isWrong = false;
            dojo.widget.byId(this.greaterThan).update();
          }
          return true;
        }
      }
      if (this.lowerThan != "") {
        if (document.getElementById(this.lowerThan).value == null || document.getElementById(this.lowerThan).value == "") {
          if (this.isWrong == true) {
            this.isWrong = false;
            dojo.widget.byId(this.lowerThan).update();
          }
          return true;
        } else if (parseFloat(this.textbox.value) > parseFloat(document.getElementById(this.lowerThan).value)) {
          if (this.isWrong == false) {
            this.isWrong = true;
            dojo.widget.byId(this.lowerThan).update();
          }
          return false;
        } else {
          if (this.isWrong == true) {
            this.isWrong = false;
            dojo.widget.byId(this.lowerThan).update();
          }
          return true;
        }
      }


		},

		onkeydown: function(){ 

		},

		onclick: function() {
		},

		onchange: function() {
		},

		update: function() {
			// summary:
			//		Called by oninit, onblur, and onkeypress.
			// description:
			//		Show missing or invalid messages if appropriate, and highlight textbox field.
			this.lastCheckedValue = this.textbox.value;
			this.missingSpan.style.display = "none";
			this.invalidSpan.style.display = "none";
			this.rangeSpan.style.display = "none";
	
			var empty = this.isEmpty();
			var valid = true;
			if(this.promptMessage != this.textbox.value){ 
				valid = this.isValid(); 
			}
			var missing = this.isMissing();
	
			// Display at most one error message
			if(missing){
				this.missingSpan.style.display = "";
			}else if( !empty && !valid ){
				this.invalidSpan.style.display = "";
			}else if( !this.isInRange() ){
				this.rangeSpan.style.display = "";
			}
			this.highlight();
		}

	}
);